/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Java ME version)
 *  
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011
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

package net.noisetube.location.javame;

import javax.microedition.location.Coordinates;
import javax.microedition.location.Location;

import net.noisetube.model.ICoordinates;

/**
 * Wrapper around javax.microedition.location.Coordinates
 * 
 * @author mstevens
 *
 */
public class JavaMENTCoordinates implements ICoordinates
{
	
	private Coordinates coordinates;
	private long timeStamp;
	
	public JavaMENTCoordinates(Location location)
	{
		if(location != null && location.isValid())
		{
			this.coordinates = location.getQualifiedCoordinates();
			this.timeStamp = location.getTimestamp();
		}
		else
			throw new IllegalArgumentException("Invalid location");
	}
	
	public JavaMENTCoordinates(double latitude, double longitude, double altitude)
	{
		this.coordinates = new Coordinates(latitude, longitude, (float) altitude);
	}
	
	/**
	 * @see net.noisetube.model.ICoordinates#getLatitude()
	 */
	public double getLatitude()
	{
		return coordinates.getLatitude();
	}

	/**
	 * @see net.noisetube.model.ICoordinates#getLongitude()
	 */
	public double getLongitude()
	{
		return coordinates.getLongitude();
	}

	/**
	 * @see net.noisetube.model.ICoordinates#getAltitude()
	 */
	public double getAltitude()
	{
		return coordinates.getAltitude();
	}

	/**
	 * @see net.noisetube.model.ICoordinates#distance(net.noisetube.model.ICoordinates)
	 */
	public double distance(ICoordinates otherCoordinates)
	{
		if(otherCoordinates instanceof JavaMENTCoordinates)
			return coordinates.distance(((JavaMENTCoordinates) otherCoordinates).coordinates);
		else
			throw new IllegalArgumentException("Wrong instance type");
	}

	public boolean equals(ICoordinates otherCoordinates)
	{
		if(otherCoordinates instanceof JavaMENTCoordinates)
		{
			//return this.coordinates.equals(((JavaMENTCoordinates) otherCoordinates).coordinates); //not sure if javax.microedition.location.Coordinates.equals() is properly implemented
			Coordinates c1 = this.coordinates, c2 = ((JavaMENTCoordinates) otherCoordinates).coordinates;	
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

	public double azimuthTo(ICoordinates otherCoordinates)
	{
		if(otherCoordinates instanceof JavaMENTCoordinates)
			return coordinates.azimuthTo(((JavaMENTCoordinates) otherCoordinates).coordinates);
		else
			throw new IllegalArgumentException("Wrong instance type");
	}

	public void setLatitude(double latitude)
	{
		coordinates.setLatitude(latitude);		
	}

	public void setLongitude(double longitude)
	{
		coordinates.setLongitude(longitude);
	}

	public void setAltitude(double altitude)
	{
		coordinates.setAltitude((float) altitude);
	}
	
	public ICoordinates copy()
	{
		return new JavaMENTCoordinates(coordinates.getLatitude(), coordinates.getLongitude(), coordinates.getAltitude());
	}

	/**
	 * @return time at which these coordinates were determined (in same clock and time representation as System.currentTimeMillis() 
	 */
	public long getTimeStamp()
	{
		return timeStamp;
	}

}
