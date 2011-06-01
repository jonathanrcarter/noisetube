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

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;

import net.noisetube.core.NTClient;
import net.noisetube.location.LocationComponent;
import net.noisetube.model.NTLocation;

/**
 * @author mstevens, maisonneuve
 * 
 */
public class JavaMELocationComponent extends LocationComponent implements LocationListener
{

	//STATICS:
	private static Criteria DEFAULT_LOCATIONPROVIDER_CRITERIA;

	static
	{
		DEFAULT_LOCATIONPROVIDER_CRITERIA = new Criteria();
		DEFAULT_LOCATIONPROVIDER_CRITERIA.setHorizontalAccuracy(Criteria.NO_REQUIREMENT);
		DEFAULT_LOCATIONPROVIDER_CRITERIA.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
		DEFAULT_LOCATIONPROVIDER_CRITERIA.setPreferredResponseTime(5000); //in ms; default: Criteria.NO_REQUIREMENT
		DEFAULT_LOCATIONPROVIDER_CRITERIA.setPreferredPowerConsumption(Criteria.NO_REQUIREMENT); //Criteria.POWER_USAGE_HIGH
		DEFAULT_LOCATIONPROVIDER_CRITERIA.setCostAllowed(true); // allowed to cost
		DEFAULT_LOCATIONPROVIDER_CRITERIA.setSpeedAndCourseRequired(false);
		DEFAULT_LOCATIONPROVIDER_CRITERIA.setAltitudeRequired(false);
		DEFAULT_LOCATIONPROVIDER_CRITERIA.setAddressInfoRequired(false);
	}
	
	private static final int DEFAULT_INTERVAL_TIME = 2;
	private static final int DEFAULT_TIMEOUT = 1;
	private static final int DEFAULT_MAX_AGE = 1;

	protected final int DISABLE_GPS_AFTER_LOCATION_ERRORS = 200;
	
	//DYNAMICS:
	private LocationProvider locationProvider;
	protected int locationErrors = 0;

	protected boolean startGPS()
	{
		if(running							//LocationComponent is running
			&& preferences.isUseGPS() 		//AND GPS usage allowed by preferences (and thus also by the device and the NoiseTube client)
			&& locationProvider == null) 	//AND GPS is currently not enabled (there's no point in enabling otherwise)
		{
			try
			{
				locationErrors = 0;
				locationProvider = LocationProvider.getInstance(DEFAULT_LOCATIONPROVIDER_CRITERIA);
				if(locationProvider == null)
					locationProvider = LocationProvider.getInstance(null);
				if(locationProvider != null)
					locationProvider.setLocationListener(this, DEFAULT_INTERVAL_TIME, DEFAULT_TIMEOUT, DEFAULT_MAX_AGE);
				else
					throw new Exception("Could not instanciate a LocationProvider");
			}
			catch(Exception e)
			{
				locationProvider = null;
				NTClient.getInstance().getPreferences().setUseGPS(false); //so GPS is not retried (avoid to keep bugging user with Location API permission requests)
				log.error(e, "startGPS");
			}
		}
		return locationProvider != null;	
	}

	protected void stopGPS()
	{
		try
		{
			locationProvider.setLocationListener(null, DEFAULT_INTERVAL_TIME, DEFAULT_TIMEOUT, DEFAULT_MAX_AGE);
			locationProvider.reset();
			locationProvider = null;
		}
		catch(Exception ignore) {}
	}
	
	public boolean isGPSEnabled()
	{
		return locationProvider != null;
	}

	public synchronized void locationUpdated(LocationProvider lp, Location location)
	{
		if(running && locationProvider != null)
		{
			if(location != null && location.isValid())
				setLastLocation(new NTLocation(new JavaMENTCoordinates(location)));
			else
				locationErrors++;
			if(!preferences.isForceGPS() && locationErrors > DISABLE_GPS_AFTER_LOCATION_ERRORS)
			{
				suspendGPS();
				locationErrors = 0;
			}
		}
	}

	public void providerStateChanged(LocationProvider lp, int newLPState)
	{
		switch(newLPState)
		{
			case LocationProvider.OUT_OF_SERVICE : disableGPS(); break;
			case LocationProvider.AVAILABLE : setGPSState(LocationComponent.GPS_STATE_WAITING); break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE : setGPSState(LocationComponent.GPS_STATE_SUSPENDED); break;
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
