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

public interface EventManagerManaged
{

	public void move(Vector2 distance);

	public void zoom(float zoomDistance);

	public void zoomIn();

	public void zoomOut();

	public void zoom(float x, float y, float zoomDistance);

	public void zoomIn(float x, float y);

	public void zoomOut(float x, float y);

	public float getMoveSpeed();

	public void longClick(float x, float y);

	public boolean canZoomIn();

	public boolean canZoomOut();

}
