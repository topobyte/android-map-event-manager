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

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;

public class EventManager<T extends View & EventManagerManaged>
{

	final static int TWO_FINGER_TAP_MAX_DELTA_TIME = 100;
	final static int TWO_FINGER_TAP_MAX_PRESSURE_LENGTH = 250;

	final static int TRACKBALL_MOVE_SPEED = 40;

	float TWO_FINGER_TAP_MIN_DISTANCE;
	float TWO_FINGER_TAP_MAX_MOVEMENT;
	float TWO_FINGER_TAP_MAX_LOG_ZOOM;
	float TWO_FINGER_TAP_MAX_ZOOM;

	private final T view;

	private float density;

	private GestureDetector gd;
	private ScaleGestureDetector sgd;

	// used to prevent two-finger taps when scrolling or zooming occurs
	private float accDistance = 0;
	private float accLogZoom = 0;
	private float accZoom = 1;

	private boolean doubleTap = false;
	private Point doubleTapPoint = null;

	private boolean allowTrackball = true;
	private boolean allowTouchMovement = true;
	private boolean allowLongPress = true;
	private boolean allowZoomAtPosition = true;

	private boolean logarithmicZoom;

	public EventManager(T view, boolean logarithmicZoom)
	{
		this.view = view;
		this.logarithmicZoom = logarithmicZoom;
		Context context = view.getContext();

		WindowManager windowManager = (WindowManager) view.getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics displaymetrics = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(displaymetrics);
		density = displaymetrics.density;

		TWO_FINGER_TAP_MIN_DISTANCE = 100 * density;
		TWO_FINGER_TAP_MAX_MOVEMENT = 20 * density;
		TWO_FINGER_TAP_MAX_LOG_ZOOM = 0.1f;
		TWO_FINGER_TAP_MAX_ZOOM = (float) Math.pow(2,
				TWO_FINGER_TAP_MAX_LOG_ZOOM);

		gd = new GestureDetector(context,
				new GestureDetector.SimpleOnGestureListener() {

					@Override
					public boolean onScroll(MotionEvent e1, MotionEvent e2,
							float distanceX, float distanceY)
					{
						Vector2 direction = new Vector2(distanceX, distanceY);
						accDistance += direction.length();
						if (isAllowTouchMovement()) {
							EventManager.this.view.move(direction);
						}
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e)
					{
						doubleTap = true;
						doubleTapPoint = new Point(e.getX(), e.getY());
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e)
					{
						EventManager.this.view.longClick(e.getX(), e.getY());
					}
				});

		sgd = new ScaleGestureDetector(context,
				new ScaleGestureDetector.SimpleOnScaleGestureListener() {
					@Override
					public boolean onScale(ScaleGestureDetector detector)
					{
						float factor = detector.getScaleFactor();
						float focusX = detector.getFocusX();
						float focusY = detector.getFocusY();

						disableDoubleTap();

						if (EventManager.this.logarithmicZoom) {
							float zoomDistance = (float) (Math.log(factor)
									/ Math.log(2));
							accLogZoom += Math.abs(zoomDistance);
							if (isAllowZoomAtPosition()) {
								EventManager.this.view.zoom(focusX, focusY,
										zoomDistance);
							} else {
								EventManager.this.view.zoom(zoomDistance);
							}
						} else {
							if (factor > 1) {
								accZoom *= factor;
							} else {
								accZoom /= factor;
							}
							if (isAllowZoomAtPosition()) {
								EventManager.this.view.zoom(focusX, focusY,
										factor);
							} else {
								EventManager.this.view.zoom(factor);
							}
						}
						return true;
					}
				});
	}

	/*
	 * configurable parameters
	 */

	public boolean isAllowTrackball()
	{
		return allowTrackball;
	}

	public boolean isAllowTouchMovement()
	{
		return allowTouchMovement;
	}

	public boolean isAllowLongPress()
	{
		return allowLongPress;
	}

	public boolean isAllowZoomAtPosition()
	{
		return allowZoomAtPosition;
	}

	public void setAllowTrackball(boolean allow)
	{
		allowTrackball = allow;
	}

	public void setAllowTouchMovement(boolean allow)
	{
		allowTouchMovement = allow;
	}

	public void setAllowLongPress(boolean allowLongPress)
	{
		this.allowLongPress = allowLongPress;
	}

	public void setAllowZoomAtPosition(boolean allowZoomAtPosition)
	{
		this.allowZoomAtPosition = allowZoomAtPosition;
	}

	public boolean onTrackballEvent(MotionEvent event)
	{
		if (!allowTrackball) {
			return false;
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			float moveSpeedFactor = view.getMoveSpeed();
			float moveX = event.getX()
					* (TRACKBALL_MOVE_SPEED * moveSpeedFactor);
			float moveY = event.getY()
					* (TRACKBALL_MOVE_SPEED * moveSpeedFactor);

			view.move(new Vector2(moveX, moveY));
			return true;
		}
		return false;
	}

	public boolean onTouchEvent(MotionEvent event)
	{
		updateNumDown(event);
		// printStatus(event);
		int action = event.getActionMasked();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			actionDown(event);
			break;
		case MotionEvent.ACTION_UP:
			actionUp(event);
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			actionPointerDown(event);
			break;
		case MotionEvent.ACTION_POINTER_UP:
			actionPointerUp(event);
			break;
		}
		sgd.onTouchEvent(event);
		gd.onTouchEvent(event);
		return true;
	}

	/*
	 * number of pointers down, before and after the current event respectively
	 * 
	 * we maintain these across all events to be able to classify situations
	 * conveniently.
	 */
	private int numDownBefore = 0;
	private int numDownNow = 0;

	/*
	 * update the number of currently pressed pointers according to the current
	 * event.
	 */
	private void updateNumDown(MotionEvent event)
	{
		int count = event.getPointerCount();

		int action = event.getActionMasked();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			numDownNow = count;
			numDownBefore = count - 1;
			break;
		case MotionEvent.ACTION_UP:
			numDownNow = count - 1;
			numDownBefore = count;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			numDownNow = count;
			numDownBefore = count - 1;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			numDownNow = count - 1;
			numDownBefore = count;
			break;
		case MotionEvent.ACTION_MOVE:
			numDownNow = numDownBefore = count;
			break;
		case MotionEvent.ACTION_CANCEL:
			numDownBefore = numDownNow;
			numDownNow = 0;
			break;
		}
	}


	/*
	 * methods for specific touch event actions
	 * 
	 * when these get called, internal variable are assumed to be set correctly.
	 */

	private void actionDown(MotionEvent event)
	{
		updateTwoFingerTap(event);
	}

	private void actionPointerDown(MotionEvent event)
	{
		updateTwoFingerTap(event);
	}

	private void actionUp(MotionEvent event)
	{
		if (doubleTap) {
			if (allowZoomAtPosition) {
				view.zoomIn(doubleTapPoint.x, doubleTapPoint.y);
			} else {
				view.zoomIn();
			}
			disableDoubleTap();
		}

		updateTwoFingerTap(event);
		if (!invalid && twoFingerTapPress && twoFingerTapRelease) {
			twoFingerTap();
		}
	}

	private void disableDoubleTap()
	{
		doubleTap = false;
		doubleTapPoint = null;
	}

	private void actionPointerUp(MotionEvent event)
	{
		updateTwoFingerTap(event);
	}

	/*
	 * two pointer clicking
	 */

	// used to prevent 3 or more finger events
	boolean invalid = true;
	// information about pointers
	long time1down, time2down, time2up, time1up;
	boolean twoFingerTapPress = false;
	boolean twoFingerTapRelease = false;
	Point twoFingerTapPoint1 = null;
	Point twoFingerTapPoint2 = null;

	private int firstPointerId;

	private void updateTwoFingerTap(MotionEvent event)
	{
		if (numDownNow > 2) {
			invalid = true;
		} else if (numDownBefore == 0 && numDownNow == 1) {
			// first pointer pressed
			invalid = false;
			accDistance = 0;
			accLogZoom = 0;
			accZoom = 1;
			time1down = event.getEventTime();
			twoFingerTapPoint1 = point(event, 0);
			firstPointerId = event.getPointerId(0);
		} else if (numDownBefore == 1 && numDownNow == 2) {
			// second pointer pressed
			time2down = event.getEventTime();
			twoFingerTapPress = time2down
					- time1down < TWO_FINGER_TAP_MAX_DELTA_TIME;
			for (int i = 0; i < event.getPointerCount(); i++) {
				int pointerId = event.getPointerId(i);
				if (pointerId != firstPointerId) {
					twoFingerTapPoint2 = point(event, i);
				}
			}
		} else if (numDownBefore == 2 && numDownNow == 1) {
			// second pointer up
			time2up = event.getEventTime();
		} else if (numDownBefore == 1 && numDownNow == 0) {
			// first pointer up
			time1up = event.getEventTime();
			twoFingerTapRelease = time1up
					- time2up < TWO_FINGER_TAP_MAX_DELTA_TIME;
		}
	}

	private void twoFingerTap()
	{
		if (twoFingerTapPoint1 == null || twoFingerTapPoint2 == null) {
			return;
		}
		Vector2 diff = new Vector2(twoFingerTapPoint1, twoFingerTapPoint2);
		float distance = diff.length();
		Log.i("twotap", "distance: " + (distance / density));
		Log.i("twotap", "accDistance: " + (accDistance / density));
		Log.i("twotap", "accLogZoom: " + accLogZoom);
		Log.i("twotap", "accZoom: " + accZoom);
		long timePassed = time1up - time1down;
		if (timePassed < TWO_FINGER_TAP_MAX_PRESSURE_LENGTH
				&& distance > TWO_FINGER_TAP_MIN_DISTANCE
				&& accDistance < TWO_FINGER_TAP_MAX_MOVEMENT
				&& accLogZoom < TWO_FINGER_TAP_MAX_LOG_ZOOM
				&& accZoom < TWO_FINGER_TAP_MAX_ZOOM) {
			float x = (twoFingerTapPoint1.getX() + twoFingerTapPoint2.getX())
					/ 2;
			float y = (twoFingerTapPoint1.getY() + twoFingerTapPoint2.getY())
					/ 2;
			if (view.canZoomOut()) {
				if (allowZoomAtPosition) {
					view.zoomOut(x, y);
				} else {
					view.zoomOut();
				}
			}
		}
	}

	private Point point(MotionEvent event, int i)
	{
		return new Point(event.getX(i), event.getY(i));
	}

}
