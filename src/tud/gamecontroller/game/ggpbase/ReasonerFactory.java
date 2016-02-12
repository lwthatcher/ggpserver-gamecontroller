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

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

import tud.gamecontroller.AbstractReasonerFactory;
import tud.gamecontroller.game.ReasonerInterface;
import tud.gamecontroller.term.TermFactoryInterface;

public final class ReasonerFactory extends
		AbstractReasonerFactory<Term, MachineState> {
	
	public static enum ReasonerType {Prover, PropNet};

	// default is Prover
	private ReasonerType reasonerType = ReasonerType.Prover;
	
	public void setReasonerType(ReasonerType reasonerType) {
		this.reasonerType = reasonerType;
	}
	
	public ReasonerInterface<Term, MachineState> createReasoner(String gameDescription, String gameName) {
		StateMachine stateMachine = null;
		switch(reasonerType) {
			case Prover:
				stateMachine = new ProverStateMachine();
				break;
			case PropNet:
				stateMachine = new PropNetStateMachine();
		}
		return new Reasoner(gameDescription, stateMachine);
	}

	public TermFactoryInterface<Term> getTermFactory() {
		return new TermFactory();
	}
}