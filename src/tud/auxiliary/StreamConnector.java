package tud.auxiliary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * connects an input and an output stream
 * 
 * The run method reads all data from the InputStream and writes it to the OutputStream (unbuffered).
 *
 */
public class StreamConnector implements Runnable {

	private InputStream in;
	private OutputStream out;

	public StreamConnector(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}

	@Override
	public void run()  {
		try {
			byte[] buffer = new byte[1024]; // Adjust if you want
		    int bytesRead;
		    while ((bytesRead = in.read(buffer, 0, buffer.length)) != -1)
		    {
		        out.write(buffer, 0, bytesRead);
		        System.out.println("write " + bytesRead + " bytes");
		    }
	        out.flush();
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
	}

	public static void connectStreams(InputStream in, OutputStream out) {
		assert(in != null && out != null);
		Thread t = new Thread(new StreamConnector(in, out));
		t.setDaemon(true);
		t.start();
	}

}
