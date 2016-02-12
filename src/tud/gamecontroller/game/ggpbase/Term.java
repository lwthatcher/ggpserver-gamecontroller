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
import java.util.Collections;
import java.util.List;

import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlFunction;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.gdl.grammar.GdlVariable;

import tud.gamecontroller.term.AbstractTerm;

public class Term extends AbstractTerm<Term, GdlTerm>{
	
	public Term(GdlTerm nativeTerm){
		super(nativeTerm);
	}

	public String getName() {
		if (nativeTerm instanceof GdlFunction) {
			return ((GdlFunction)nativeTerm).getName().getValue();
		} else if (nativeTerm instanceof GdlConstant) {
			return ((GdlConstant)nativeTerm).getValue();
		} else if (nativeTerm instanceof GdlVariable) {
			return ((GdlVariable)nativeTerm).getName();
		} else {
			throw new RuntimeException("unrecognized term type: " + nativeTerm);
		}
	}

	public GdlTerm getExpr(){
		return nativeTerm;
	}
	
	public boolean isConstant() {
		return nativeTerm instanceof GdlConstant;
	}

	public boolean isVariable() {
		return nativeTerm instanceof GdlVariable;
	}
	
	public boolean isGround() {
		return nativeTerm.isGround();
	}

	public List<Term> getArgs() {
		if(nativeTerm instanceof GdlFunction){
			return new TermList(((GdlFunction)nativeTerm).getBody());
		}else{
			return new TermList(Collections.<GdlTerm>emptyList());
		}
	}
	
	private static class TermList extends AbstractList<Term>{
		private List<GdlTerm> termList;

		public TermList(List<GdlTerm> l){
			this.termList=l;
		}
		
		public Term get(int index) {
			return new Term(termList.get(index));
		}

		public int size() {
			return termList.size();
		}
		
	}

}
