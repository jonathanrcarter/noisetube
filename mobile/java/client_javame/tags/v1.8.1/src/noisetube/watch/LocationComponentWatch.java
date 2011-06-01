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

package noisetube.watch;

import javax.microedition.location.Coordinates;

import noisetube.MainMidlet;
import noisetube.config.WatchPreferences;
import noisetube.location.gps.LocationComponentGPS;
import noisetube.location.gps.NTLocationGPS;
import noisetube.util.Logger;

/**
 * @author mstevens
 * 
 */
public class LocationComponentWatch extends LocationComponentGPS
{

	private Logger log = Logger.getInstance();

	private WatchPreferences preferences = (WatchPreferences) MainMidlet.getInstance().getPreferences();

	private WatchRunner watch;

	public void stop()
	{
		if(autoMode)
		{
			setStatusMessage("GPS Stopped"); // setter calls updateObserver autonomously
			if(watch != null)
				watch.stop();
		}
		running = false;
	}

	public void enableAutoMode()
	{
		if(running // LocationComponent is running
				&& preferences.isUseGPS() // AND GPS usage allowed by preferences (and thus also by the NoiseTube client)
				&& !autoMode) // AND autoMode is currently not enabled (there's no point in enabling otherwise)
		{
			try
			{
				if(watch == null)
					watch = WatchRunner.getInstance();
				watch.start();
				autoMode = true;
				statusMessage = "GPS started";
				log.debug("Localisation mode: Automatic (GPS)");
			}
			catch(Exception e)
			{
				autoMode = false;
				log.error(e, "enableAutoMode");
				log.debug("Localisation mode: Manual");
			}
			finally
			{
				updateObserver(); // !!!
			}
		}
	}

	public void disableAutoMode()
	{
		if(running && autoMode) // LocationComponent is running AND autoMode is enabled (there's no point in disabling otherwise)
		{
			try
			{
				// Don't stop WatchRunner here, as it is also used by LoudnessComponent
				/*
				 * if(watch != null && watch.isRunning()) watch.stop(); //stop
				 * WatchRunner if running
				 */
				autoMode = false;
				log.debug("Localisation mode: Manual");
			}
			catch(Exception e)
			{
				autoMode = false;
				log.error(e, "disableAutoMode");
			}
			finally
			{
				updateObserver();
			}
		}
	}

	protected void getGPSLocation()
	{
		if(!watch.initialized())
		{   //not yet initialized
			setStatusMessage("GPS not yet initialized");
			log
					.debug("Unable to get GPS location because WatchRunner is not initialized");
			return;
		}
		Coordinates coords = watch.getLastCoordinates();
		if(coords == null)
		{   //bad location
			location_error++;
			setStatusMessage("Invalid location (#" + location_error + ")");
			if(location_error > TRY_ERROR) // too many invalid locations -> go back to manual mode
			{
				location_error = 0;
				disableAutoMode();
			}
		}
		else
		{
			lastLocation = new NTLocationGPS(coords);
			numberOfValidLocations++;
			setStatusMessage("Location found (#" + numberOfValidLocations + ")");
		}
	}

}
