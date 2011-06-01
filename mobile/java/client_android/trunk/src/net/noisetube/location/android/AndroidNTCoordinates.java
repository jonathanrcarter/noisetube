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

import com.google.android.maps.GeoPoint;

import android.location.Location;
import android.location.LocationManager;
import net.noisetube.model.INTCoordinates;

/**
 * Wrapper around net.noisetube.location.android
 * 
 * @author sbarthol
 *
 */
public class AndroidNTCoordinates implements INTCoordinates
{
	
	private Location location = null;
	private long timeStamp;
	
	public AndroidNTCoordinates(Location location)
	{
		this.location = location;
		this.timeStamp = System.currentTimeMillis(); //don't use location.getTime() because that's in UTC and we want timeStamp to be like the system clock (for comparisons with System.currentTimeMillis())
	}
	
	public AndroidNTCoordinates(double latitude, double longitude, double altitude)
	{
		//To create a new Location, we need an old location (which isn't always present) or a location provider
		//The latter will not send any data unless specifically asked, so there is no overhead.
		Location newLocation = new Location(LocationManager.GPS_PROVIDER);
		newLocation.setLatitude(latitude);
		newLocation.setLongitude(longitude);
		newLocation.setAltitude(altitude);
		this.location = newLocation;
	}
	
	/**
	 * @see net.noisetube.model.INTCoordinates#getLatitude()
	 */
	public double getLatitude()
	{
		return location.getLatitude();
	}

	/**
	 * @see net.noisetube.model.INTCoordinates#getLongitude()
	 */
	public double getLongitude()
	{
		return location.getLongitude();
	}

	/**
	 * @see net.noisetube.model.INTCoordinates#getAltitude()
	 */
	public double getAltitude()
	{
		return location.getAltitude();
	}

	/**
	 * @see net.noisetube.model.INTCoordinates#distance(net.noisetube.model.INTCoordinates)
	 */
	public double distance(INTCoordinates otherCoordinates)
	{
		if(otherCoordinates instanceof AndroidNTCoordinates)
			return location.distanceTo(((AndroidNTCoordinates) otherCoordinates).location);
		else
			throw new IllegalArgumentException("Wrong instance type");
	}

	public boolean equals(INTCoordinates otherCoordinates)
	{
		if(otherCoordinates instanceof AndroidNTCoordinates)
		{
			Location c1 = this.location;
			Location c2 = ((AndroidNTCoordinates) otherCoordinates).location;
			if(c1.getLatitude() != c2.getLatitude())
				return false;
			if(c1.getLongitude() != c2.getLongitude())
				return false;
			if(c1.getAltitude() != c2.getAltitude())
				return false;
			return true;
		}
		else
			return false;		
	}

	public double azimuthTo(INTCoordinates otherCoordinates)
	{
		if(otherCoordinates instanceof AndroidNTCoordinates)
			return location.bearingTo(((AndroidNTCoordinates) otherCoordinates).location);
		else
			throw new IllegalArgumentException("Wrong instance type");
	}

	public void setLatitude(double latitude)
	{
		location.setLatitude(latitude);		
	}

	public void setLongitude(double longitude)
	{
		location.setLongitude(longitude);
	}

	public void setAltitude(double altitude)
	{
		location.setAltitude(altitude);
	}

	public AndroidNTCoordinates copy()
	{
		return new AndroidNTCoordinates(location.getLatitude(), location.getLongitude(), location.getAltitude());
	}
	
	public GeoPoint toGeoPoint()
	{
		return new GeoPoint((int) (location.getLatitude() * 1E6),
							(int) (location.getLongitude() * 1E6));
	}
	
	/**
	 * @return time at which these coordinates were determined (in same clock and time representation as System.currentTimeMillis() 
	 */
	public long getTimeStamp()
	{
		return timeStamp;
	}
	
}
