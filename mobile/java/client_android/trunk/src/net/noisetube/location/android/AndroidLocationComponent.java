/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Android version)
 *  
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011
 *  Android port by Vrije Universiteit Brussel (BrusSense team), 2010-2011
 * --------------------------------------------------------------------------------
 *  This library is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU Lesser General Public License, version 2.1, as published
 *  by the Free Software Foundation.
 *  
 *  This library is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License along
 *  with this library; if not, write to:
 *    Free Software Foundation, Inc.,
 *    51 Franklin Street, Fifth Floor,
 *    Boston, MA  02110-1301, USA.
 *  
 *  Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *  NoiseTube project source code repository: http://code.google.com/p/noisetube
 * --------------------------------------------------------------------------------
 *  More information:
 *   - NoiseTube project website: http://www.noisetube.net
 *   - Sony Computer Science Laboratory Paris: http://csl.sony.fr
 *   - VUB BrusSense team: http://www.brussense.be
 * --------------------------------------------------------------------------------
 */

package net.noisetube.location.android;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import net.noisetube.location.LocationComponent;
import net.noisetube.model.NTLocation;
import net.noisetube.ui.android.MainActivity;

/**
 * @author sbarthol, mstevens
 * 
 */
public class AndroidLocationComponent extends LocationComponent implements LocationListener
{
	
	private LocationManager locationManager;
	private boolean gpsEnabled = false;

	public AndroidLocationComponent(LocationManager locationManager)
	{
		this.locationManager = locationManager;
	}

	@Override
	public boolean startGPS()
	{
		if(running && preferences.isUseGPS())		
		{
			MainActivity.getInstance().runOnUiThread(new Runnable() //!!! to avoid looper problems
			{
				public void run()
				{
					try
					{				
						if(locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
						{	/* requestLocationUpdates (String provider, long minTime, float minDistance, LocationListener listener) */
							locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0 /* ms */, 3.5f /* meter */, AndroidLocationComponent.this);
							//setLastLocation(new NTLocation(new AndroidNTCoordinates(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER))));
							gpsEnabled = true;
						}
					}
					catch(Exception e)
					{
						log.error(e, "startGPS");
						gpsEnabled = false;
					}
				}
			});
		}
		return gpsEnabled;
	}
	
	@Override
	public boolean isGPSEnabled()
	{
		return gpsEnabled;
	}
	
	@Override
	public void stopGPS()
	{
		try
		{
			gpsEnabled = false;
			locationManager.removeUpdates(this);
		}
		catch(Exception ignore) {}
	}
	
	public boolean isAutoModeEnabled()
	{
		return gpsEnabled;
	}

	public void onLocationChanged(Location location)
	{	
		if(running && gpsEnabled)
			setLastLocation(new NTLocation(new AndroidNTCoordinates(location))); //When a new location is found it needs to be stored		
	}

	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		switch(status)
		{
			case LocationProvider.OUT_OF_SERVICE : disableGPS(); break;
			case LocationProvider.AVAILABLE : setGPSState(LocationComponent.GPS_STATE_WAITING); break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE : setGPSState(LocationComponent.GPS_STATE_SUSPENDED); break;
		}
	}

	public void onProviderDisabled(String provider)
	{
		gpsEnabled = false;
	}

	public void onProviderEnabled(String provider)
	{
		gpsEnabled = true;
	}
	
}
