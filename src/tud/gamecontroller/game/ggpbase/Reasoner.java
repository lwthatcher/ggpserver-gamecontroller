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

package tud.gamecontroller.game.ggpbase;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.factory.GdlFactory;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.symbol.factory.SymbolFactory;
import org.ggp.base.util.symbol.factory.exceptions.SymbolFormatException;
import org.ggp.base.util.symbol.grammar.SymbolList;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tud.auxiliary.CollectionWrapper;
import tud.gamecontroller.auxiliary.InvalidKIFException;
import tud.gamecontroller.game.FluentInterface;
import tud.gamecontroller.game.JointMoveInterface;
import tud.gamecontroller.game.MoveInterface;
import tud.gamecontroller.game.ReasonerInterface;
import tud.gamecontroller.game.RoleInterface;
import tud.gamecontroller.game.impl.Fluent;
import tud.gamecontroller.game.impl.Move;
import tud.gamecontroller.game.impl.Role;

public class Reasoner implements ReasonerInterface<Term, MachineState> {
	
	private StateMachine stateMachine;
	private String gameDescription;
	
	public Reasoner(String gameDescription, StateMachine stateMachine) {
		this.gameDescription=gameDescription;
		this.stateMachine = stateMachine;
		String preprocessedRules = Game.preprocessRulesheet(gameDescription);
		Game game = Game.createEphemeralGame(preprocessedRules);
		stateMachine.initialize(game.getRules());
	}

	public boolean isTerminal(MachineState state) {
		return stateMachine.isTerminal(state);
	}

	public List<? extends RoleInterface<Term>> getRoles() {
		final List<org.ggp.base.util.statemachine.Role> roles = stateMachine.getRoles();
		return new AbstractList<RoleInterface<Term>>() {
			@Override
			public RoleInterface<Term> get(int index) {
				return new Role<Term>(new Term(roles.get(index).getName()));
			}
			@Override
			public int size() {
				return roles.size();
			}
		};
	}

	public MachineState getSuccessorState(MachineState state, JointMoveInterface<Term> jointMove) {
		List<org.ggp.base.util.statemachine.Move> moves = getMovesListForJointMove(jointMove, getRoles());
		try {
			return stateMachine.getNextState(state, moves);
		} catch (TransitionDefinitionException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<org.ggp.base.util.statemachine.Move> getMovesListForJointMove(JointMoveInterface<Term> jointMove, List<? extends RoleInterface<Term>> roles) {
		assert(jointMove!=null);
		List<org.ggp.base.util.statemachine.Move> moves = new ArrayList<org.ggp.base.util.statemachine.Move>();
		for (RoleInterface<Term> role:roles) {
			moves.add(getGgpbaseMove(jointMove.get(role)));
		}
		return moves;
	}
	
	private static org.ggp.base.util.statemachine.Move getGgpbaseMove(MoveInterface<Term> move) {
		return new org.ggp.base.util.statemachine.Move(move.getTerm().getNativeTerm());
	}

	private org.ggp.base.util.statemachine.Role getGgpbaseRole(RoleInterface<Term> role) {
		return new org.ggp.base.util.statemachine.Role((GdlConstant)role.getTerm().getNativeTerm());
	}
	
	public boolean isLegal(MachineState state, RoleInterface<Term> role, MoveInterface<Term> move) {
		try {
			return stateMachine.getLegalMoves(state, getGgpbaseRole(role))
				.contains(getGgpbaseMove(move));
		} catch (MoveDefinitionException e) {
			throw new RuntimeException(e);
		}
	}

	public int getGoalValue(MachineState state, RoleInterface<Term> role) {
		try {
			return stateMachine.getGoal(state, getGgpbaseRole(role));
		} catch (GoalDefinitionException e) {
			throw new RuntimeException(e);
		}
	}

	public Collection<? extends MoveInterface<Term>> getLegalMoves(MachineState state, RoleInterface<Term> role) {
		try {
			final List<org.ggp.base.util.statemachine.Move> ggpbaseMoves = stateMachine.getLegalMoves(state, getGgpbaseRole(role));
			return new AbstractList<MoveInterface<Term>>() {
				@Override
				public MoveInterface<Term> get(int index) {
					return new Move<Term>(new Term(ggpbaseMoves.get(index).getContents()));
				}
				@Override
				public int size() {
					return ggpbaseMoves.size();
				}
			};
		} catch (MoveDefinitionException e) {
			throw new RuntimeException(e);
		}
	}

	public MachineState getInitialState() {
		return stateMachine.getInitialState();
	}

	public String getKIFGameDescription() {
		String preprocessedRules = Game.preprocessRulesheet(gameDescription);
		Game game = Game.createEphemeralGame(preprocessedRules);
		return game.getRulesheet();
	}

	public Collection<? extends FluentInterface<Term>> getFluents(MachineState state) {
		return new CollectionWrapper<GdlSentence, FluentInterface<Term>>(state.getContents()) {
			@Override
			public FluentInterface<Term> convertStoT(GdlSentence s) {
				return new Fluent<Term>(new Term(s.toTerm()));
			}
		}; 
	}
	
	public Collection<Term> getSeesTerms(MachineState state, RoleInterface<Term> role, JointMoveInterface<Term> jointMove) {
		throw new NotImplementedException();
	}
	
	public Collection<Term> getSeesXMLTerms(MachineState state, RoleInterface<Term> role) {
		throw new NotImplementedException();
	}
	
	public MachineState getStateFromString(String state) throws InvalidKIFException {
		try {
			SymbolList symbolList = (SymbolList)SymbolFactory.create(state);
			Set<GdlSentence> sentenceList = new HashSet<GdlSentence>();
			for (int i=0; i<symbolList.size(); ++i) {
				GdlTerm term = GdlFactory.createTerm(symbolList.get(i));
				sentenceList.add(term.toSentence());
			}
			return stateMachine.getMachineStateFromSentenceList(sentenceList);
		} catch (SymbolFormatException e) {
			throw new InvalidKIFException(e.getMessage());
		}
	}
}
