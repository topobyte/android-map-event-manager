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

import android.view.MotionEvent;

public class EventPoint
{

	private final float x;
	private final float y;

	public EventPoint(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public EventPoint(MotionEvent event)
	{
		this.x = event.getX();
		this.y = event.getY();
	}

	public EventPoint(MotionEvent event, int pointerIndex)
	{
		this.x = event.getX(pointerIndex);
		this.y = event.getY(pointerIndex);
	}

	public float getX()
	{
		return x;
	}

	public float getY()
	{
		return y;
	}

	public float distance(EventPoint other)
	{
		float dx = x - other.x;
		float dy = y - other.y;
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

}
