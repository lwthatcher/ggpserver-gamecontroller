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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

public class Reasoner implements ReasonerInterface<Term, Collection<Object>> {

	private static final class FluentCollectionWrapper extends
			CollectionWrapper<Object, FluentInterface<Term>> {
		private FluentCollectionWrapper(Collection<Object> collection) {
			super(collection);
		}

		@Override
		public FluentInterface<Term> convertStoT(Object s) {
			return new Fluent<Term>(new Term(s));
		}
	}

	private static final class TermCollectionWrapper extends CollectionWrapper<Object, Term> {
		private TermCollectionWrapper(Collection<? extends Object> collection) {
			super(collection);
		}
		
		@Override
		public Term convertStoT(Object s) {
			return new Term(s);
		}
	}
	
	private static final class MoveCollectionWrapper extends
			CollectionWrapper<Object, MoveInterface<Term>> {
		private MoveCollectionWrapper(Collection<Object> collection) {
			super(collection);
		}

		@Override
		public MoveInterface<Term> convertStoT(Object s) {
			return new Move<Term>(new Term(s));
		}
	}

	private EclipseConnector eclipse;
	
	private List<? extends RoleInterface<Term>> roles = null;
	
	private String gameDescription;
	
	public Reasoner(String gameDescription) {
		eclipse = new EclipseConnector();
		this.gameDescription = gameDescription;
		eclipse.parseGDL(gameDescription);
	}
	
	@Override
	public Collection<? extends FluentInterface<Term>> getFluents(Collection<Object> state) {
		return new FluentCollectionWrapper(state);
	}

	@Override
	public int getGoalValue(Collection<Object> state, RoleInterface<Term> role) {
		return eclipse.getGoalValue(role.getTerm().getNativeTerm(), state);
	}

	@Override
	public Collection<Object> getInitialState() {
		return eclipse.getInitialState();
	}

	@Override
	public String getKIFGameDescription() {
		return gameDescription;
	}

	@Override
	public Collection<? extends MoveInterface<Term>> getLegalMoves(Collection<Object> state, RoleInterface<Term> role) {
		Collection<Object> moves = eclipse.getLegalMoves(role.getTerm().getNativeTerm(), state);
		return new MoveCollectionWrapper(moves);
	}

	@Override
	public List<? extends RoleInterface<Term>> getRoles() {
		if (roles == null) {
			Collection<? extends Object> roleObjects = eclipse.getRoles();
			roles = new ArrayList<Role<Term>>(new CollectionWrapper<Object, Role<Term>>(roleObjects) {
				@Override
				public Role<Term> convertStoT(Object s) {
					return new Role<Term>(new Term(s));
				}
			});
		}
		return roles;
	}

	@Override
	public Collection<Term> getSeesTerms(Collection<Object> state, RoleInterface<Term> role, JointMoveInterface<Term> jointMove) {
		return new TermCollectionWrapper(eclipse.getSeesTerms(role.getTerm().getNativeTerm(), getEclipseJointMove(jointMove), state));
	}

	@Override
	public Collection<Term> getSeesXMLTerms(Collection<Object> state,	RoleInterface<Term> role) {
		return new TermCollectionWrapper(eclipse.getSeesXMLTerms(role.getTerm().getNativeTerm(), state));
	}

	@Override
	public Collection<Object> getStateFromString(String state)
			throws InvalidKIFException {
		return eclipse.parseTermList(state);
	}

	private  List<Object> getEclipseJointMove(final JointMoveInterface<Term> jointMove) {
		final List<? extends RoleInterface<Term>> roles = getRoles();
		return new AbstractList<Object>() {
			@Override
			public Object get(int index) {
				return jointMove.get(roles.get(index)).getTerm().getNativeTerm();
			}
			@Override
			public int size() {
				return jointMove.size();
			}
		};

	}

	@Override
	public Collection<Object> getSuccessorState(Collection<Object> state, JointMoveInterface<Term> jointMove) {
		return eclipse.getNextState(getEclipseJointMove(jointMove), state);
	}

	@Override
	public boolean isLegal(Collection<Object> state, RoleInterface<Term> role, MoveInterface<Term> move) {
		return eclipse.isLegalMove(role.getTerm().getNativeTerm(), move.getTerm().getNativeTerm(), state);
	}

	@Override
	public boolean isTerminal(Collection<Object> state) {
		return eclipse.isTerminal(state);
	}

}
