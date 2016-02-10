/*
    Copyright (C) 2008-2013 Stephan Schiffel <stephan.schiffel@gmx.de>
                  2010 Nicolas JEAN <njean42@gmail.com>

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

package tud.gamecontroller.gui;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.swing.JFrame;

import tud.gamecontroller.AbstractGameControllerRunner;
import tud.gamecontroller.GDLVersion;
import tud.gamecontroller.ReasonerFactoryInterface;
import tud.gamecontroller.game.impl.Game;
import tud.gamecontroller.players.PlayerInfo;
import tud.gamecontroller.term.TermFactoryInterface;
import tud.gamecontroller.term.TermInterface;

public abstract class AbstractGameControllerGuiRunner<
		TermType extends TermInterface<TermType>,
		ReasonerStateInfoType
		> extends AbstractGameControllerRunner<TermType, ReasonerStateInfoType> {

	private File gameFile=null;
	private String styleSheet=null;
	private String sightFile=null;
	private String xmlOutputDir=null;
	private String matchID=null;
	private int startclock=0, playclock=0;
	private File scrambleWordListFile=null;
	private Collection<PlayerInfo> playerInfos=null;
	/**
	 * for caching the result of getGame() such that a game is only created once
	 * 
	 * If any parameters change that are used in createGame() this has to be reset to null!  
	 */
	private Game<TermType, ReasonerStateInfoType> game=null;
	
	public AbstractGameControllerGuiRunner(ReasonerFactoryInterface<TermType, ReasonerStateInfoType> reasonerFactory, String[] args) {
		super(reasonerFactory);
		if (args.length>0) {
			File gameFile = new File(args[0]);
			if(gameFile.canRead()) {
				this.gameFile = gameFile;
			} else {
				System.err.println("File " + gameFile + " is not readable!");
			}
		}
	}

	@Override
	protected boolean doPrintXML() {
		return xmlOutputDir!=null;
	}

	@Override
	protected Game<TermType, ReasonerStateInfoType> getGame() {
		if (game == null && gameFile != null) {
			try {
				game = createGame(gameFile);
			} catch (IOException e) {
				getLogger().severe("error loading game '" + gameFile +"':" + e.getMessage());
			}
		}
		return game;
	}

	@Override
	protected String getMatchID() {
		return matchID;
	}

	protected abstract TermFactoryInterface<TermType> getTermFactory();

	@Override
	protected int getStartClock() {
		return startclock;
	}

	@Override
	protected int getPlayClock() {
		return playclock;
	}

	@Override
	protected Collection<PlayerInfo> getPlayerInfos() {
		return playerInfos;
	}

	@Override
	protected File getScrambleWordListFile() {
		return scrambleWordListFile;
	}

	@Override
	protected String getStyleSheet() {
		return styleSheet;
	}
	
	@Override
	protected String getSightFile() {
		return sightFile;
	}

	@Override
	protected String getXmlOutputDir() {
		return xmlOutputDir;
	}

	public File getGameFile() {
		return gameFile;
	}

	@Override
	public void setGdlVersion(GDLVersion gdlVersion) {
		super.setGdlVersion(gdlVersion);
		game = null;
	}

	public void setGameFile(File gameFile) {
		this.gameFile = gameFile;
		game = null;
	}

	public void setStyleSheet(String styleSheet) {
		this.styleSheet = styleSheet;
		game = null;
	}
	
	public void setSightFile(String sightFile) {
		this.sightFile = sightFile;
		game = null;
	}

	public void setXmlOutputDir(String xmlOutputDir) {
		this.xmlOutputDir = xmlOutputDir;
	}

	public void setMatchID(String matchID) {
		this.matchID = matchID;
	}

	public void setStartclock(int startclock) {
		this.startclock = startclock;
	}

	public void setPlayclock(int playclock) {
		this.playclock = playclock;
	}

	public void setScrambleWordListFile(File scrambleWordListFile) {
		this.scrambleWordListFile = scrambleWordListFile;
	}

	public void setPlayerInfos(Collection<PlayerInfo> playerInfos) {
		this.playerInfos=playerInfos;
	}

	public void runGui() {
		JFrame jFrame = new GameControllerFrame<TermType, ReasonerStateInfoType>(this);
		jFrame.setVisible(true);
	}
}
