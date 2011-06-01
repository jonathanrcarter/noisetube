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

import javax.microedition.location.Location;

import noisetube.MainMidlet;
import noisetube.config.Preferences;
import noisetube.io.SavingBuffer;
import noisetube.location.LocationComponent;
import noisetube.model.NTLocation;

/**
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class LocationComponentGPS extends LocationComponent
{

	// protected boolean usesWatch = false;
	protected boolean autoMode = false; // = "GPS mode"
	protected int numberOfValidLocations;

	private Preferences preferences;

	private GPSPositioningRunner gps;

	// once started, wait 15 seconds to check if GPS is working, else go to the
	// manual mode
	protected final int TRY_ERROR = 200; //20;
	protected int location_error = 0;

	// retry gps every minute
	protected final int RETRY_GPS = 1 * 60;
	protected int gps_retry_counter = 0;

	public void start()
	{
		numberOfValidLocations = 0;
		location_error = 0;
		running = true;
		preferences = MainMidlet.getInstance().getPreferences();
		// try auto mode
		if(preferences.isUseGPS())
			enableAutoMode(); // calls updateObserver
		else
			updateObserver();
		// log.debug("LocationComponent (" + (usesWatch ? "Watch" : "GPS") +
		// ") started");
	}

	public void stop()
	{
		if(autoMode)
		{
			setStatusMessage("GPS Stopped"); // setter calls updateObserver
												// autonomously
			if(gps != null)
				gps.stop();
		}
		running = false;
	}

	public void toggleAutoMode()
	{
		if(running)
		{
			if(autoMode)
			{
				disableAutoMode();
				SavingBuffer interpolBff = engine.getInterpolationBuffer();
				if(interpolBff != null)
					interpolBff.flush(); //user asked to disable, so flush and don't interpolate later
			}
			else
				enableAutoMode();
		}
	}

	public boolean isAutoModeEnabled()
	{
		return autoMode;
	}

	public void enableAutoMode()
	{
		if(running							//LocationComponent is running
				&& preferences.isUseGPS() 	//AND GPS usage allowed by preferences (and thus also by the device and the NoiseTube client)
				&& !autoMode) 				//AND autoMode is currently not enabled (there's no point in enabling otherwise)
		{
			try
			{
				location_error = 0;
				if(gps == null)
					gps = new GPSPositioningRunner(this); // instantiate only
															// when required
				gps.start();
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
		if(running && autoMode) // LocationComponent is running AND autoMode is
								// enabled (there's no point in disabling
								// otherwise)
		{
			try
			{
				if(gps != null && gps.isRunning())
					gps.stop(); // stop the gps if running
				log.debug("Localisation mode: Manual");
			}
			catch(Exception e)
			{
				log.error(e, "disableAutoMode");
			}
			finally
			{
				autoMode = false;
				updateObserver();
			}
		}
	}

	public NTLocation getLastLocation()
	{
		if(autoMode)
		{
			getGPSLocation();
		}
		else
		{
			if(preferences.isUseGPS()) // if GPS is allowed at all...
			{
				// try to re-enable GPS every 2 minutes
				gps_retry_counter++;
				if(gps_retry_counter > RETRY_GPS)
				{
					gps_retry_counter = 0;
					enableAutoMode();
				}
			}
		}
		return lastLocation;
	}

	protected void getGPSLocation()
	{
		if(!gps.isInitialized())
		{ // not yet initialized
			setStatusMessage("GPS not yet initialized");
			log
					.debug("Unable to get GPS location because GPSPositionRunner is not initialized");
			return;
		}
		Location location = gps.getLastLocation();
		if(location == null || !location.isValid())
		{ // bad location
			location_error++;
			setStatusMessage("Invalid location (#" + location_error + ")");
			if(location_error > TRY_ERROR) // too many invalid locations -> go
											// back to manual mode
			{
				location_error = 0;
				disableAutoMode();
			}
			lastLocation = null;
		}
		else
		{
			lastLocation = new NTLocationGPS(location.getQualifiedCoordinates());
			log.debug("GPS fix: " + lastLocation.toString());
			numberOfValidLocations++;
			setStatusMessage("Location found (#" + numberOfValidLocations + ")");
		}
	}

	/*
	 * //TEST SUITE FOR INTERPOLATION BUFFER: static Vector testCoords; static {
	 * testCoords = new Vector(); //1st for(int i = 0; i < 10; i++)
	 * testCoords.addElement(new Coordinates(48.843584060668945d,
	 * 2.34757661819458d, 0)); //2nd for(int i = 0; i < 7; i++)
	 * testCoords.addElement(new Coordinates(48.84339094161987d,
	 * 2.347877025604248d, 0)); //3rd for(int i = 0; i < 13; i++)
	 * testCoords.addElement(new Coordinates(48.843111991882324d,
	 * 2.3479628562927246d, 0)); //4th for(int i = 0; i < 9; i++)
	 * testCoords.addElement(new Coordinates(48.84299397468567d,
	 * 2.34832763671875d, 0)); //5th for(int i = 0; i < 5; i++)
	 * testCoords.addElement(new Coordinates(48.84304761886597d,
	 * 2.348756790161133d, 0)); //6th for(int i = 0; i < 11; i++)
	 * testCoords.addElement(new Coordinates(48.84321928024292d,
	 * 2.34907865524292d, 0)); //7th for(int i = 0; i < 2; i++)
	 * testCoords.addElement(new Coordinates(48.84353041648865,
	 * 2.3491644859313965d, 0)); //8th for(int i = 0; i < 8; i++)
	 * testCoords.addElement(new Coordinates(48.843809366226196d,
	 * 2.3491859436035156d, 0)); //9th for(int i = 0; i < 3; i++)
	 * testCoords.addElement(new Coordinates(48.84422779083252d,
	 * 2.349228858947754d, 0)); //10th for(int i = 0; i < 2; i++)
	 * testCoords.addElement(new Coordinates(48.84451746940613d,
	 * 2.349271774291992d, 0)); //11th for(int i = 0; i < 9; i++)
	 * testCoords.addElement(new Coordinates(48.84466767311096d,
	 * 2.3491644859313965d, 0)); } static int c = -1;
	 * 
	 * //Put this in getLastLocation(): //c++; //return new
	 * NTLocationGPS((Coordinates)testCoords.elementAt(c));
	 */

}
