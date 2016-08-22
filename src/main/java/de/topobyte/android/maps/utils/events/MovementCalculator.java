// Copyright 2018 Sebastian Kuerten
//
// This file is part of android-map-event-manager.
//
// android-map-event-manager is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// android-map-event-manager is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with android-map-event-manager. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.android.maps.utils.events;

public class MovementCalculator
{

	private float x = 0, y = 0;

	/**
	 * Create a new MovementCalculator initialized at the specified starting
	 * position.
	 * 
	 * @param x
	 *            the initial x position.
	 * @param y
	 *            the initial y position.
	 */
	public MovementCalculator(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public void reset(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public Vector2 update(float x, float y)
	{
		Vector2 distance = new Vector2(this.x - x, this.y - y);
		this.x = x;
		this.y = y;
		return distance;
	}

}
