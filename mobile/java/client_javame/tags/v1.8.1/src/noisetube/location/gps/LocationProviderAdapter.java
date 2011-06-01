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

import javax.microedition.location.Criteria;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;

import noisetube.util.Logger;

/**
 * Modified from Sony Ericsson example code
 * 
 */
public class LocationProviderAdapter
{

	private static Logger log = Logger.getInstance();

	private static Criteria criteria = null;
	private static LocationProvider lp = null;

	public static synchronized LocationProvider getLocationProvider()
	{
		return getLocationProvider(null);
	}

	public static synchronized LocationProvider getLocationProvider(
			Criteria criteria)
	{
		if(lp == null
				|| (criteria != null && !criteria
						.equals(LocationProviderAdapter.criteria)))
		{
			lp = null;
			try
			{
				lp = LocationProvider.getInstance(criteria);
			}
			catch(LocationException locationexception)
			{
			}
			if(lp == null && criteria != null)
			{
				log
						.debug("No LocationProvider found for requested criteria, trying default");
				return getLocationProvider(null);
			}
			LocationProviderAdapter.criteria = criteria;
		}
		return lp;
	}

}