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
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import net.noisetube.location.LocationComponent;

/**
 * DUMMY CLASS
 * Tester has no location support but this class needs to be present for AndroidNTClient
 * 
 * @author mstevens
 */
public class AndroidLocationComponent extends LocationComponent implements LocationListener
{
	
	public AndroidLocationComponent(LocationManager locationManager)
	{
	}

	@Override
	public void onLocationChanged(Location arg0)
	{	
	}

	@Override
	public void onProviderDisabled(String arg0)
	{
	}

	@Override
	public void onProviderEnabled(String arg0)
	{	
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2)
	{	
	}

	@Override
	protected boolean startGPS()
	{
		return false;
	}

	@Override
	protected void stopGPS()
	{
	}

	@Override
	public boolean isGPSEnabled()
	{
		return false;
	}
	
}
