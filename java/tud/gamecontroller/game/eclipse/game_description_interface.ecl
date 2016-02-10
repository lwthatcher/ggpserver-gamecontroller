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

:- module(game_description_interface, [
	% interface to GDL / GDL-II games

	% game stuff
	d_goal/3,
	d_legal/3,
	d_terminal/1,
	d_role/2,
	d_next/2,
	d_init/2,
	d_does/3,
	d_true/2,
	d_sees/3,
	d_sees_xml/3,

	% loading the rules of the game, and/or overriding the rules with better ones
	compile_new_rules_from_file/1,
	compile_new_rules/1
	], eclipse_language).
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

:- mode compile_new_rules_from_file(++).
compile_new_rules_from_file(Filename) :-
	compile(Filename).

:- mode compile_new_rules(+).
compile_new_rules(Clauses) :-
	expand_goal(Clauses, Clauses1),
	% make sure we don't get interrupted during compiling (we could end up in some inconsistent state if we have only half of the clauses)
	(events_defer ->
		(compile_term(Clauses1) -> events_nodefer ; events_nodefer, fail)
	;
		compile_term(Clauses1)
	).

:- mode d_does(?, ?, ++).
d_does(R,A,JointMove-_Z) :-
	member(did(R,A),JointMove).

:- mode d_true(?, +).
d_true(F,_-Z) :-
	member(F,Z).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% the following will be overwritten by created stuff 

:- mode d_role(?, ++).
d_role(_, _) :- fail.

:- mode d_init(?, ++).
d_init(_, _) :- fail.

:- mode d_next(?, ++).
d_next(_, _) :- fail.

:- mode d_legal(?, ?, ++).
d_legal(_, _, _) :- fail.

:- mode d_goal(?, ?, ++).
d_goal(_,_, _) :- fail.

:- mode d_terminal(++).
d_terminal(_) :- fail.

:- mode d_sees(++, ?, ++).
d_sees(_, _, _) :- fail.

:- mode d_sees_xml(++, ?, ++).
d_sees_xml(_, _, _) :- fail.
