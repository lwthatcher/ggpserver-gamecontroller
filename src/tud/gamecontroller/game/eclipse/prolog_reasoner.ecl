%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%    Copyright (C) 2013 Stephan Schiffel <stephan.schiffel@gmx.de>
%
%    This file is part of GameController.
%
%    GameController is free software: you can redistribute it and/or modify
%    it under the terms of the GNU General Public License as published by
%    the Free Software Foundation, either version 3 of the License, or
%    (at your option) any later version.
%
%    GameController is distributed in the hope that it will be useful,
%    but WITHOUT ANY WARRANTY; without even the implied warranty of
%    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
%    GNU General Public License for more details.
%
%    You should have received a copy of the GNU General Public License
%    along with GameController.  If not, see <http://www.gnu.org/licenses/>.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

:- module(prolog_reasoner, [
	parse_term/2,
	parse_term_list/2,
	translate_to_sexpr/2,
	goal/3,
	legal_moves/3,
	is_legal/3,
	state_update/3,
	terminal/1,
	setgame/1,
	init/1,
	roles/1,
	sees/4,
	sees_xml/3
	], eclipse_language).
	
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

:- use_module(gdl_parser).
:- use_module(game_description_interface).

:- lib(lists).

:- mode parse_term(++, ?).
parse_term(String, Term) :-
	parse_gdl_term_string(String, Term).

:- mode parse_term_list(++, ?).
parse_term_list(String, TermList) :-
	parse_gdl_term_list_string(String, TermList).

:- mode translate_to_sexpr(+, ?).
translate_to_sexpr(Term, String) :-
	translate_to_sexpr_string(Term, String).

:- mode goal(++, ?, ++).
goal(Role, Value, State) :-
	once(d_goal(Role, Value, nil-State)).

:- mode legal_moves(++, ?, ++).
legal_moves(Role, Moves, State) :-
	fast_setof(M, d_legal(Role, M, nil-State), Moves).

:- mode is_legal(++, ++, ++).
is_legal(Role, Move, State) :-
	once(d_legal(Role, Move, nil-State)).

:- mode state_update(++, ++, ?).
state_update(State, Moves, NextState) :-
	% writeln(state_update(State, Moves, NextState)), flush(output),
	moves_to_joint_move(Moves, JointMove), 
	fast_setof(F, d_next(F, JointMove-State), NextState).

:- mode terminal(++).
terminal(State) :-
	once(d_terminal(nil-State)).

:- mode sees(++, ?, ++, ++).
sees(Role, Percepts, Moves, State) :-
	moves_to_joint_move(Moves, JointMove), 
	fast_setof(Percept, d_sees(Role, Percept, JointMove-State), Percepts).

:- mode sees_xml(++, ?, ++).
sees_xml(Role, Percepts, State) :-
	fast_setof(Percept, d_sees_xml(Role, Percept, nil-State), Percepts).

:- mode setgame(++).
setgame(GDLDescriptionString) :-
	parse_gdl_description_string(GDLDescriptionString, Rules),
	sort_clauses(Rules, Rules1),
	compile_new_rules(Rules1).

% sort the clauses in a List alphabetically according to the name of the head
:- mode sort_clauses(+,-).
sort_clauses(List1, List2) :-
	(foreach(Clause,List1), foreach(key(Name,Arity)-Clause,KeyList1) do
		(Clause= (:- _) ->
			Name=0, Arity=0 % 0 is a number and comes before every atom according to @</2
		;
			(Clause=(Head:-_) -> true ; Clause=Head),
			functor(Head,Name,Arity)
		)
	),
	keysort(KeyList1,KeyList2),
	(foreach(key(_,_)-Clause,KeyList2), foreach(Clause,List2) do true),!.


:- mode init(?).
init(State) :-
	fast_setof(F, d_init(F, nil-nil), State).
	
:- mode roles(?).
roles(Roles) :-
	findall(R, d_role(R, nil-nil), Roles).
	
moves_to_joint_move(Moves, JointMove) :-
	roles(Roles),
	(foreach(Role, Roles),
	 foreach(Move, Moves),
	 foreach(did(Role,Move), JointMove) do true).

fast_setof(X,Expr,Xs) :-
	findall(X,Expr,Xs1),
	sort(0,<,Xs1,Xs).
