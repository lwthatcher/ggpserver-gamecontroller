/*
    Copyright (C) 2008-2013 Stephan Schiffel <stephan.schiffel@gmx.de>

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

package tud.gamecontroller.game.jocular;

import javax.swing.SwingUtilities;

import stanfordlogic.gdl.Parser;
import stanfordlogic.prover.ProofContext;
import tud.gamecontroller.ReasonerFactoryInterface;
import tud.gamecontroller.gui.AbstractGameControllerGuiRunner;
import tud.gamecontroller.term.TermFactoryInterface;

public class GameControllerGuiRunner extends
		AbstractGameControllerGuiRunner<Term, ProofContext> {

	private Parser parser; 
	
	public GameControllerGuiRunner(ReasonerFactoryInterface<Term, ProofContext> reasonerFactory, String[] args) {
		super(reasonerFactory, args);
		this.parser=new Parser();
	}

	@Override
	protected TermFactoryInterface<Term> getTermFactory() {
		return new TermFactory(parser);
	}

	public static void main(final String[] args){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				AbstractGameControllerGuiRunner<?, ?> runner = new GameControllerGuiRunner(new ReasonerFactory(), args);
				runner.runGui();
			}
		});
	}
}
