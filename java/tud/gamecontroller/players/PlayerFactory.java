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

package tud.gamecontroller.players;

import tud.gamecontroller.game.StateInterface;
import tud.gamecontroller.scrambling.GameScramblerInterface;
import tud.gamecontroller.term.TermInterface;

public class PlayerFactory {

	public static <TermType extends TermInterface<TermType>, StateType extends StateInterface<TermType, ? extends StateType>>
		Player<TermType, StateType> createRemotePlayer(RemotePlayerInfo info, GameScramblerInterface gameScrambler) {
		return new RemotePlayer<TermType, StateType>(info.getName(), info.getHost(), info.getPort(), info.getGdlVersion(), gameScrambler);
	}
	
	public static <TermType extends TermInterface<TermType>, StateType extends StateInterface<TermType, ? extends StateType>>
		Player<TermType, StateType> createRandomPlayer(RandomPlayerInfo info) {
		return new RandomPlayer<TermType, StateType>(info.getName(), info.getGdlVersion(), info.getSeed());
	}
	
	public static <TermType extends TermInterface<TermType>, StateType extends StateInterface<TermType, ? extends StateType>>
		Player<TermType, StateType> createLegalPlayer(LegalPlayerInfo info) {
		return new LegalPlayer<TermType, StateType>(info.getName(), info.getGdlVersion());
	}
	
	public static <TermType extends TermInterface<TermType>, StateType extends StateInterface<TermType, ? extends StateType>>
		Player<TermType, StateType> createHumanPlayer(HumanPlayerInfo info) {
		return new HumanPlayer<TermType, StateType>(info.getName());
	}
	
	public static <TermType extends TermInterface<TermType>, StateType extends StateInterface<TermType, ? extends StateType>>
		Player<TermType, StateType> createPlayer(PlayerInfo info, GameScramblerInterface gameScrambler) {
		if(info instanceof RemotePlayerInfo){
			return PlayerFactory. <TermType, StateType> createRemotePlayer((RemotePlayerInfo)info, gameScrambler);
		}else if(info instanceof RandomPlayerInfo){
			return PlayerFactory. <TermType, StateType> createRandomPlayer((RandomPlayerInfo)info);
		}else if(info instanceof LegalPlayerInfo){
			return PlayerFactory. <TermType, StateType> createLegalPlayer((LegalPlayerInfo)info);
		}else if(info instanceof HumanPlayerInfo){
			return PlayerFactory. <TermType, StateType> createHumanPlayer((HumanPlayerInfo)info);
		}
		return null;
	}


}
