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
import java.util.Collections;
import java.util.List;

import com.parctechnologies.eclipse.CompoundTerm;

import tud.gamecontroller.term.AbstractTerm;

public class Term extends AbstractTerm<Term, Object> {

	public Term(Object nativeTerm) {
		super(nativeTerm);
	}

	@Override
	public List<Term> getArgs() {
		if (nativeTerm instanceof CompoundTerm) {
			final CompoundTerm nativeTerm = (CompoundTerm)this.nativeTerm;
			return new AbstractList<Term>() {
	
				@Override
				public Term get(int index) {
					if (index<0 || index>=nativeTerm.arity()) {
						throw new IndexOutOfBoundsException("index " + index + " is out of bounds for term with arity " + nativeTerm.arity());
					}
					return new Term(nativeTerm.arg(index+1));
				}
	
				@Override
				public int size() {
					return nativeTerm.arity();
				}
			};
		} else if (nativeTerm instanceof Collection<?>) {
			final Collection<?> nativeTerm = (Collection<?>)this.nativeTerm;
			List<Term> args = new ArrayList<Term>();
			for (Object arg:nativeTerm) {
				args.add(new Term(arg));
			}
			return args;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public String getName() {
		if (nativeTerm == null) {
			throw new UnsupportedOperationException("variable terms do not have names");
		}
		if (nativeTerm instanceof CompoundTerm) {
			return ((CompoundTerm)nativeTerm).functor();
		} else if (nativeTerm instanceof Collection<?>) {
			return "[]";
		} else {
			return nativeTerm.toString();
		}
	}

	@Override
	public boolean isConstant() {
		if (nativeTerm == null) return false;
		if (nativeTerm instanceof CompoundTerm) {
			return ((CompoundTerm)nativeTerm).arity() == 0;
		} else if (nativeTerm instanceof Collection<?>) {
			return ((Collection<?>)nativeTerm).size() == 0;
		} else {
			return true;
		}
	}

	@Override
	public boolean isVariable() {
		return nativeTerm == null;
	}

}
