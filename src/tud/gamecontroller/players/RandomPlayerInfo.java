/*
    Copyright (C) 2008,2011 Stephan Schiffel <stephan.schiffel@gmx.de>

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

package tud.gamecontroller.players;

import tud.gamecontroller.GDLVersion;

public class RandomPlayerInfo extends LocalPlayerInfo {

	/**
	 * the seed used for the random number generator in the random player
	 * seed == null means a random seed is chosen
	 */
	private Long seed;
	
	public RandomPlayerInfo(int roleindex, GDLVersion gdlVersion) {
		this(roleindex, gdlVersion, null);
	}

	public RandomPlayerInfo(int roleindex, GDLVersion gdlVersion, Long seed) {
		super(roleindex, RANDOM_PLAYER_NAME, gdlVersion);
		this.seed = seed;
	}

	@Override
	public String getType() {
		return TYPE_RANDOM;
	}

	public Long getSeed() {
		return seed;
	}

}
