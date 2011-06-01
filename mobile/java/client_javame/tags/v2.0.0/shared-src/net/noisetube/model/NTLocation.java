/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation)
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

package net.noisetube.model;

/**
 * @author mstevens
 * 
 */
public class NTLocation
{
	
	protected String locationTag;
	protected ICoordinates coordinates;

	/**
	 * @param locationTag
	 */
	public NTLocation(String locationTag)
	{
		if(locationTag == null)
			throw new NullPointerException("locationTag cannot be null!");
		if(locationTag.equals("") || locationTag.equalsIgnoreCase("null"))
			throw new IllegalArgumentException("location tag cannot be empty or \"null\"");
		this.locationTag = locationTag;
	}
	
	public NTLocation(ICoordinates coordinates)
	{
		this.coordinates = coordinates;
	}

	/**
	 * @return the locationTag
	 */
	public String getLocationTag()
	{
		return locationTag;
	}

	/**
	 * @param locationTag the locationTag to set
	 */
	public void setLocationTag(String locationTag)
	{
		this.locationTag = locationTag;
	}

	public String toString()
	{
		if(coordinates != null)
			return "geo:" + Double.toString(coordinates.getLatitude()) + "," + Double.toString(coordinates.getLongitude());
		else
			return locationTag;
	}

	public boolean hasCoordinates()
	{
		return coordinates != null;
	}
	
	/**
	 * @return the coordinates
	 */
	public ICoordinates getCoordinates()
	{
		return coordinates;
	}

	/**
	 * @param coordinates the coordinates to set
	 */
	public void setCoordinates(ICoordinates coordinates)
	{
		this.coordinates = coordinates;
	}
	
	public boolean equals(Object o)
	{
		if(o == null)
			return false;
		if(!(o instanceof NTLocation))
			return false;
		else
		{
			NTLocation otherLoc = (NTLocation) o;
			if(this.coordinates != null)
			{
				if(otherLoc.coordinates == null || !this.coordinates.equals(otherLoc.coordinates))
					return false;
			}
			else
			{
				if(otherLoc.coordinates != null)
					return false;
			}
			if(this.locationTag != null)
			{
				if(otherLoc.locationTag == null || !this.locationTag.equalsIgnoreCase(otherLoc.locationTag))
					return false;
			}
			else
			{
				if(otherLoc.locationTag != null)
					return false;
			}
		}
		return true;
	}

}
