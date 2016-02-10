/*
    Copyright (C) 2013 Stephan Schiffel <stephan.schiffel@gmx.de>

    This file is part of GameController.

    GameController is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameController is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameController.  If not, see <http://www.gnu.org/licenses/>.
*/

package tud.gamecontroller.game.eclipse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import com.parctechnologies.eclipse.CompoundTerm;
import com.parctechnologies.eclipse.CompoundTermImpl;
import com.parctechnologies.eclipse.EclipseEngine;
import com.parctechnologies.eclipse.EclipseEngineOptions;
import com.parctechnologies.eclipse.EclipseException;
import com.parctechnologies.eclipse.Fail;
import com.parctechnologies.eclipse.FromEclipseQueue;
import com.parctechnologies.eclipse.OutOfProcessEclipse;
import com.parctechnologies.eclipse.QueueListener;
import com.parctechnologies.eclipse.ToEclipseQueue;

public class EclipseConnector {

	private static final String DEFAULT_ECLIPSEDIR = "/usr/local/eclipse";
	private static final String RESOURCEPATH = "tud/gamecontroller/game/eclipse/"; // path to the prolog files relative to the class path
	private static final String[] prologFiles = { "gdl_parser.ecl", "game_description_interface.ecl", "prolog_reasoner.ecl" };
	private static final int GLOBAL_STACK = 128; // in MB
	private static final int LOCAL_STACK = 128;  // in MB
	
	// Create some default Eclipse options
	private EclipseEngineOptions eclipseEngineOptions;

	// Object representing the Eclipse process
	protected EclipseEngine eclipse;

	public EclipseConnector() {
		initEclipse();
	}

	public void initEclipse() {

		String ECLIPSEDIR=System.getenv("ECLIPSEDIR");
		if(ECLIPSEDIR==null){
			ECLIPSEDIR=DEFAULT_ECLIPSEDIR;
		}
		System.setProperty("eclipse.directory", ECLIPSEDIR);

		eclipseEngineOptions = new EclipseEngineOptions();

		eclipseEngineOptions.setUseQueues(true);
		eclipseEngineOptions.setGlobalSize(GLOBAL_STACK);
		eclipseEngineOptions.setLocalSize(LOCAL_STACK);

		// Initialize Eclipse
		try {
			System.out.println("setting up streams");
			//eclipse = EmbeddedEclipse.getInstance(eclipseEngineOptions);
			eclipse = new OutOfProcessEclipse(eclipseEngineOptions);
			// connect stdout and stderr of eclipse to this process
			FromEclipseQueue eclipseOutput;
			eclipseOutput = eclipse.getEclipseStdout();
			if (eclipseOutput == null) {
				throw new RuntimeException("getEclipseStdout() returned null! Cannot setup EclipseConnector!");
			}
			eclipseOutput.setListener(new EclipseQueueReader(eclipseOutput, System.out));

			eclipseOutput = eclipse.getEclipseStderr();
			if (eclipseOutput == null) {
				throw new RuntimeException("getEclipseStderr() returned null! Cannot setup EclipseConnector!");
			}
			eclipseOutput.setListener(new EclipseQueueReader(eclipseOutput, System.err));

			//final ToEclipseQueue eclipseInput = eclipse.getToEclipseQueue("to_eclipse_queue");
			ToEclipseQueue eclipseInput = eclipse.getEclipseStdin();
			if (eclipseInput == null) {
				throw new RuntimeException("getToEclipseQueue(...) returned null! Cannot setup EclipseConnector!");
			}
			// load prolog source files from classpath and compile them in Eclipse
			for (String fileName : prologFiles) {
				fileName = RESOURCEPATH + fileName;
				System.out.println("compiling \"" + fileName + "\" ...");
				InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(fileName);
				if (fileStream == null) {
					throw new RuntimeException("Resource " + fileName + " not found! Cannot setup EclipseConnector!");
				}
				eclipseInput.setListener(new EclipseQueueWriter(fileStream, eclipseInput));
				eclipse.rpc("read_string(input,end_of_file,L,S), open(string(S), read, IS), compile_stream(IS), close(IS)");
			}
			eclipse.rpc("use_module(prolog_reasoner)");
//			System.out.println("compiled");
//			eclipse.rpc("p(X,Y), writeln(X-Y).");
//			System.out.println("executed");
		} catch (EclipseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("initEclipse done.");
	}
	
	public Object parseTerm(String s) {
		CompoundTerm result = null;
		try {
			result = eclipse.rpc(new CompoundTermImpl("parse_term", new Object[]{s, null}));
		} catch (EclipseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (result != null) ? result.arg(2) : null;
	}

	@SuppressWarnings("unchecked")
	public Collection<Object> parseTermList(String s) {
		CompoundTerm result = null;
		try {
			result = eclipse.rpc(new CompoundTermImpl("parse_term_list", new Object[]{s, null}));
		} catch (EclipseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO: check if this works for empty lists
		return (result != null) ? (Collection<Object>) result.arg(2) : null;
	}

	public String toGdlString(Object prologTerm) {
		CompoundTerm result = null;
		try {
			result = eclipse.rpc(new CompoundTermImpl("translate_to_sexpr", new Object[]{prologTerm, null}));
		} catch (EclipseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (result != null) ? (String) result.arg(2) : null;
	}

	public int getGoalValue(Object role, Collection<Object> state) {
		CompoundTerm result = null;
		try {
			result = eclipse.rpc(new CompoundTermImpl("goal", new Object[]{role, null, state}));
		} catch (Fail e){
			return -1;
		} catch (EclipseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (result != null) ? ((Integer) result.arg(2)).intValue() : -1;
	}

	@SuppressWarnings("unchecked")
	public Collection<Object> getLegalMoves(Object role, Collection<Object> state) {
		CompoundTerm result = null;
		try {
			result = eclipse.rpc(new CompoundTermImpl("legal_moves", new Object[]{role, null, state}));
		} catch (EclipseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO: check if this works for empty lists
		return (result != null) ? (Collection<Object>) result.arg(2) : null;
	}

	@SuppressWarnings("unchecked")
	public Collection<Object> getNextState(Collection<Object> movesList, Collection<Object> state) {
		CompoundTerm result = null;
		try {
			result = eclipse.rpc( new CompoundTermImpl("state_update", new Object[]{state, movesList, null}));
		} catch (EclipseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO: check if this works for empty lists
		return (result != null) ? (Collection<Object>) result.arg(3) : null;
	}

	public boolean isTerminal(Collection<Object> state) {
		try {
			eclipse.rpc(new CompoundTermImpl("terminal", new Object[]{state}));
		} catch (Fail e) {
			return false;
		} catch (EclipseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public void parseGDL(String gameDescription) {
		try {
			eclipse.rpc(new CompoundTermImpl("setgame", new Object[]{gameDescription}));
		} catch (EclipseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public Collection<Object> getInitialState() {
		CompoundTerm result = null;
		try {
			result = eclipse.rpc(new CompoundTermImpl("init", new Object[]{null}));
		} catch (EclipseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO: check if this works for empty lists
		return (result != null) ? (Collection<Object>) result.arg(1) : null;
	}

	@SuppressWarnings("unchecked")
	public Collection<Object> getRoles() {
		CompoundTerm result = null;
		try {
			result = eclipse.rpc(new CompoundTermImpl("roles", new Object[]{null}));
		} catch (EclipseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (result != null) ? (Collection<Object>) result.arg(1) : null;
	}

	public boolean isLegalMove(Object role, Object move, Collection<Object> state) {
		CompoundTerm result = null;
		try {
			result = eclipse.rpc(new CompoundTermImpl("is_legal", role, move, state) );
		} catch (Fail e) {
		} catch (EclipseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (result != null);
	}

	@SuppressWarnings("unchecked")
	public Collection<Object> getSeesTerms(Object role, List<Object> movesList, Collection<Object> state) {
		CompoundTerm result = null;
		try {
			result = eclipse.rpc( new CompoundTermImpl("sees", new Object[]{role, state, movesList, null}));
		} catch (EclipseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO: check if this works for empty lists
		return (result != null) ? (Collection<Object>) result.arg(4) : null;
	}

	@SuppressWarnings("unchecked")
	public Collection<Object> getSeesXMLTerms(Object role, Collection<Object> state) {
		CompoundTerm result = null;
		try {
			result = eclipse.rpc( new CompoundTermImpl("sees_xml", new Object[]{role, state, null}));
		} catch (EclipseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO: check if this works for empty lists
		return (result != null) ? (Collection<Object>) result.arg(3) : null;
	}

	static final private class EclipseQueueReader implements QueueListener
	{
		FromEclipseQueue fromEclipseQueue = null;
		OutputStream outputStream = null;
	
		public EclipseQueueReader(FromEclipseQueue fromEclipseQueue, OutputStream outputStream) {
			this.fromEclipseQueue = fromEclipseQueue;
			this.outputStream = outputStream;
		}
	
		public void dataAvailable(Object source)
		{
			byte[] buffer = new byte[1024];
		    int bytesRead;
		    try {
		    	while (fromEclipseQueue.available() > 0) {
					if ((bytesRead = fromEclipseQueue.read(buffer)) != -1)
					{
					    outputStream.write(buffer, 0, bytesRead);
					    // System.out.println("get " + bytesRead + " bytes from eclipse");
					} else {
					    // System.out.println("end of file");
					}
		    	}
			    outputStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void dataRequest(Object source) { }
	}

	static final private class EclipseQueueWriter implements QueueListener
	{
		ToEclipseQueue toEclipseQueue = null;
		InputStream inputStream = null;
	
		public EclipseQueueWriter(InputStream inputStream, ToEclipseQueue toEclipseQueue) {
			this.toEclipseQueue = toEclipseQueue;
			this.inputStream = inputStream;
		}
	
		public void dataAvailable(Object source) { }

		public void dataRequest(Object source) {
			byte[] buffer = new byte[1024];
		    int bytesRead;
		    try {
		    	while (inputStream.available() > 0) {
					if ((bytesRead = inputStream.read(buffer)) != -1)
					{
						toEclipseQueue.write(buffer, 0, bytesRead);
					    // System.out.println("send " + bytesRead + " bytes to eclipse");
					} else {
					    // System.out.println("end of file");
					}
		    	}
		    	toEclipseQueue.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
