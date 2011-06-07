/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Phone Tester (Java implementation; Android version)
 *  
 *  Copyright (C) 2010-2011 Vrije Universiteit Brussel (BrusSense team)
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
 * ------------------------------------------------------------------------------
 *  NoiseTube Phone Tester uses the NoiseTube Mobile library:
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011
 *  Used under the terms of the GNU Lesser General Public License, version 2.1.
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
import net.noisetube.model.INTCoordinates;

/**
 * DUMMY CLASS
 * Tester has no location support but this class needs to be present for AndroidNTClient
 * 
 * @author mstevens
 */
public class AndroidNTCoordinates implements INTCoordinates
{
	
	public AndroidNTCoordinates(Location location)
	{
	}
	
	public AndroidNTCoordinates(double latitude, double longitude, double altitude)
	{
	}

	@Override
	public double getLatitude()
	{
		return 0;
	}

	@Override
	public void setLatitude(double latitude)
	{	
	}

	@Override
	public double getLongitude()
	{
		return 0;
	}

	@Override
	public void setLongitude(double longitude)
	{	
	}

	@Override
	public double getAltitude()
	{
		return 0;
	}

	@Override
	public void setAltitude(double altitude)
	{
	}

	@Override
	public double distance(INTCoordinates otherCoordinates)
	{
		return 0;
	}

	@Override
	public boolean equals(INTCoordinates otherCoordinates)
	{
		return false;
	}

	@Override
	public double azimuthTo(INTCoordinates toPosition)
	{
		return 0;
	}

	@Override
	public INTCoordinates copy()
	{
		return null;
	}

	@Override
	public long getTimeStamp()
	{
		return 0;
	}
		
}
