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
 * @author mstevens, sbarthol
 *
 */
public interface ICoordinates
{

	public abstract double getLatitude();
	
	public abstract void setLatitude(double latitude);
	
	public abstract double getLongitude();
	
	public abstract void setLongitude(double longitude);
	
	public abstract double getAltitude();
	
	public abstract void setAltitude(double altitude);
	
	public abstract double distance(ICoordinates otherCoordinates);
	
	public abstract boolean equals(ICoordinates otherCoordinates);

	public abstract double azimuthTo(ICoordinates toPosition);
	
	public abstract ICoordinates copy();
	
	/**
	 * @return time at which these coordinates were determined (in same clock and time representation as System.currentTimeMillis() 
	 */
	public abstract long getTimeStamp();

}
