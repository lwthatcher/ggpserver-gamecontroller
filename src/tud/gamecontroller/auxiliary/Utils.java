package tud.gamecontroller.auxiliary;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tud.auxiliary.CrossProductMap;
import tud.gamecontroller.game.GameInterface;
import tud.gamecontroller.game.JointMoveInterface;
import tud.gamecontroller.game.MoveInterface;
import tud.gamecontroller.game.RoleInterface;
import tud.gamecontroller.game.StateInterface;
import tud.gamecontroller.game.impl.JointMove;
import tud.gamecontroller.term.TermInterface;

public class Utils {

	/**
	 * 
	 * @param <TermType>
	 * @param <StateType>
	 * @param state a state of the game
	 * @param game a game
	 * @return a Collection of the legal joint moves of the game (the collection is created on the fly while iterating over it)
	 */
	public static <TermType extends TermInterface<TermType>, StateType extends StateInterface<TermType, ?>>
	Collection<JointMoveInterface<TermType>> computeJointMoves(StateType state, final GameInterface<TermType, StateType> game) {
		// compute legal moves for all roles
		HashMap<RoleInterface<TermType>, Collection<? extends MoveInterface<TermType>>> legalMovesMap = new HashMap<RoleInterface<TermType>, Collection<? extends MoveInterface<TermType>>>();
		for(RoleInterface<TermType> role: game.getOrderedRoles()) {
			legalMovesMap.put(role, state.getLegalMoves(role));
		}
		// build the cross product
		final CrossProductMap<RoleInterface<TermType>, MoveInterface<TermType>> jointMovesMap = new CrossProductMap<RoleInterface<TermType>, MoveInterface<TermType>>(legalMovesMap);
		// wrap the elements of the cross product in JointMove<TermType>
		// the following is an on-the-fly collection that just refers to "jointMoves" above 
		Collection<JointMoveInterface<TermType>> jointMoves = new AbstractCollection<JointMoveInterface<TermType>>(){
			@Override
			public Iterator<JointMoveInterface<TermType>> iterator() {
				final Iterator<Map<RoleInterface<TermType>, MoveInterface<TermType>>> iterator = jointMovesMap.iterator();
				return new Iterator<JointMoveInterface<TermType>>(){
					@Override public boolean hasNext() { return iterator.hasNext(); }

					@Override public JointMoveInterface<TermType> next() { return new JointMove<TermType>(game.getOrderedRoles(), iterator.next()); }

					@Override public void remove() { iterator.remove();	}
				};
			}

			@Override
			public int size() {
				return jointMovesMap.size();
			}
		};
		// System.out.println("legal joint moves: " + jointMoves);
		return jointMoves;
	}

}
