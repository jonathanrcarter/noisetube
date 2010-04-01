/*
 * PositioningListener.java
 *
 */
package noisetube.location.gps;

import javax.microedition.location.Location;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;

/**
 * Modified from Sony Ericsson example code
 * 
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
