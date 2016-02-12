/*
    Copyright (C) 2010 Nicolas JEAN <njean42@gmail.com>
                  2010-2013 Stephan Schiffel <stephan.schiffel@gmx.de>

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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import tud.gamecontroller.auxiliary.Utils;
import tud.gamecontroller.game.GameInterface;
import tud.gamecontroller.game.JointMoveInterface;
import tud.gamecontroller.game.MoveInterface;
import tud.gamecontroller.game.RoleInterface;
import tud.gamecontroller.game.StateInterface;
import tud.gamecontroller.term.TermInterface;

/**
 * A StatesTracker keeps track of all possible states of a game from the perspective of some player. 
 * @param <TermType>
 * @param <StateType>
 */
public class StatesTracker<TermType extends TermInterface<TermType>, StateType extends StateInterface<TermType, ? extends StateType>> {
	
	protected static final Logger logger = Logger.getLogger(StatesTracker.class.getName());
	
	protected GameInterface<TermType, StateType> game;
	protected Collection<StateType> currentPossibleStates;
	protected RoleInterface<TermType> role;

	public StatesTracker(GameInterface<TermType, StateType> game, StateType initialState, RoleInterface<TermType> role) {
		this.game = game;
		this.currentPossibleStates = Collections.singleton(initialState);
		this.role = role;
		logger.info("StatesTracker()");
	}
	
	public Collection<StateType> statesUpdate(RoleInterface<TermType> role, MoveInterface<TermType> lastMove, Collection<TermType> seesTerms) {
		Set<StateType> nextPossibleStates = new HashSet<StateType>();
		for (StateType state: currentPossibleStates) {
			for (JointMoveInterface<TermType> jointMove: Utils.computeJointMoves(state, game)) {
				if (jointMove.get(role).equals(lastMove)) { // the joint move is consistent with the move we did 
					if (isPossible(state, jointMove, seesTerms)) {
						StateType newState = state.getSuccessor(jointMove);
						nextPossibleStates.add(newState);
					}
				}
			}
		}
		logger.info(
				"statesUpdate for \"" + role + "\" with move \"" + lastMove + "\" seeing " + seesTerms
				+ " with " + currentPossibleStates.size() + " currentPossibleStates yields "
				+ nextPossibleStates.size() + " nextPossibleStates");
		if (nextPossibleStates.size()==0) {
			logger.severe("no successor state for states: " + Arrays.toString(currentPossibleStates.toArray()) + ", role: " + role + ", move:" + lastMove + ", seesTerms: " + seesTerms);
		}
		currentPossibleStates = nextPossibleStates;
		return Collections.unmodifiableCollection(currentPossibleStates);
	}
	
	private boolean isPossible(StateType state, JointMoveInterface<TermType> jointMove, Collection<TermType> seesTerms) {
		Collection<TermType> shouldSee = state.getSeesTerms(role, jointMove);
		// logger.info(role + " sees "+ shouldSee + " in " + state + " with " + jointMove);
		return shouldSee.equals(seesTerms);
	}
	
	/** 
	 * @return moves that are legal in all of the current possible states 
	 */
	public Collection<? extends MoveInterface<TermType>> computeLegalMoves() {
		
		Collection<? extends MoveInterface<TermType>> legalMoves = null;
		for (StateType state: currentPossibleStates) {
			Collection<? extends MoveInterface<TermType>> stateLegalMoves = state.getLegalMoves(role);
			//System.out.println( "stateLegalMoves = "+stateLegalMoves );
			if (legalMoves == null) {
				legalMoves = new HashSet<MoveInterface<TermType>>(stateLegalMoves);
			} else {
				legalMoves.retainAll(stateLegalMoves);
			}
			//System.out.println( "Until now, our legalMoves are = "+legalMoves );
		}
		// System.out.println( "oneStatesTracker.legalMoves() for "+this.role+"("+legalMoves.size()+") = "+legalMoves );
		return legalMoves;
	}
	
}