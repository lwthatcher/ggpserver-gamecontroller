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

import tud.gamecontroller.auxiliary.InvalidKIFException;
import tud.gamecontroller.term.TermFactoryInterface;

public class TermFactory implements TermFactoryInterface<Term> {

	EclipseConnector eclipse;
	
	public TermFactory() {
		eclipse = new EclipseConnector();
	}
	
	@Override
	public Term getTermFromKIF(String kif) throws InvalidKIFException {
		return new Term(eclipse.parseTerm(kif));
	}

}
