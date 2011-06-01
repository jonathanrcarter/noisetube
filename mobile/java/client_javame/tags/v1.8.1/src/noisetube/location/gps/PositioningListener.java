/** 
 * -------------------------------------------------------------------------------
 * NoiseTube - Mobile client (J2ME version)
 * Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 * 
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License, version 2.1,
 * as published by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * --------------------------------------------------------------------------------
 * 
 * Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 * NoiseTube project source code repository: http://code.google.com/p/noisetube
 * NoiseTube project website: http://www.noisetube.net
 */

package noisetube.location.gps;

import javax.microedition.location.Location;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;

/**
 * Modified from Sony Ericsson example code
 * 
 * PositioningListener
 */
public class PositioningListener implements LocationListener
{

	private Location currentLocation = null;
	private int currentState = 0;
	private static final Object STATE_LOCK = new Object();
	private static final Object LOCATION_LOCK = new Object();
	private boolean locationUpdated = false;
	private boolean stateUpdated = false;

	public Location getCurrentLocation()
	{
		synchronized(LOCATION_LOCK)
		{
			return currentLocation;
		}
	}

	public Location waitForLocation()
	{
		synchronized(LOCATION_LOCK)
		{
			try
			{
				while(!locationUpdated)
				{
					LOCATION_LOCK.wait();
				}
				locationUpdated = false; // reset flag
			}
			catch(InterruptedException i)
			{
				i.printStackTrace();
			}
			return currentLocation;
		}
	}

	public int waitForStateChange()
	{
		synchronized(STATE_LOCK)
		{
			try
			{
				while(!stateUpdated)
				{
					STATE_LOCK.wait();
				}
				stateUpdated = false; // reset flag
			}
			catch(InterruptedException i)
			{
				i.printStackTrace();
			}
		}
		return currentState;
	}

	public void providerStateChanged(LocationProvider locationProvider, int i)
	{
		synchronized(STATE_LOCK)
		{
			currentState = i;
			stateUpdated = true;
			STATE_LOCK.notifyAll();
		}
		locationProvider.reset();
	}

	public void locationUpdated(LocationProvider locationProvider,
			Location location)
	{
		synchronized(LOCATION_LOCK)
		{
			currentLocation = location;
			locationUpdated = true;
			LOCATION_LOCK.notifyAll();
		}
	}
}
