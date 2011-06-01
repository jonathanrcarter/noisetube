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

package noisetube.location;

import noisetube.MainMidlet;
import noisetube.config.Device;
import noisetube.config.Preferences;
import noisetube.config.WatchPreferences;
import noisetube.core.Engine;
import noisetube.model.NTLocation;
import noisetube.ui.LocationUIComponent;
import noisetube.util.Logger;

/**
 * @author maisonneuve, mstevens
 * 
 */
public class LocationComponent
{

	protected Logger log = Logger.getInstance();

	protected Engine engine;
	protected LocationUIComponent ui;

	protected boolean running = false;
	protected String statusMessage = "";

	protected NTLocation lastLocation = null;

	/**
	 * @return either GPS-supporting (LocationComponentGPS subclass) or GPS-less
	 *         (LocationComponent superclass) version depending on device and
	 *         client capabilities
	 * @throws Exception
	 */
	public static LocationComponent getLocationComponent(Engine engine)
			throws Exception
	{
		LocationComponent lc = null;
		if(Device.supportsGPS()) //NoiseTube client has GPS support compiled in
		{
			Class LCClass;
			Preferences preferences = MainMidlet.getInstance().getPreferences();
			if(preferences instanceof WatchPreferences)
				LCClass = Class
						.forName("noisetube.watch.LocationComponentWatch");
			else
				LCClass = Class
						.forName("noisetube.location.gps.LocationComponentGPS");
			lc = (LocationComponent) LCClass.newInstance();
		}
		else
			lc = new LocationComponent(); //use basic GPS-less LocationComponent (this class)
		lc.engine = engine;
		return lc;
	}

	public void setObserver(LocationUIComponent ui)
	{
		this.ui = ui;
	}

	protected void updateObserver()
	{
		if(ui != null)
			ui.update();
	}

	public void start()
	{
		running = true;
		log.debug("LocationComponent (GPS-less) started");
	}

	public boolean isRunning()
	{
		return running;
	}

	public void stop()
	{
		running = false;
	}

	//should be overridden in LocationComponentGPS
	public void toggleAutoMode()
	{
	}

	//should be overridden in LocationComponentGPS
	public boolean isAutoModeEnabled()
	{
		return false;
	}

	/**
	 * @return the statusMessage
	 */
	public String getStatusMessage()
	{
		return statusMessage;
	}

	/**
	 * @param statusMessage
	 */
	public void setStatusMessage(String statusMessage)
	{
		this.statusMessage = statusMessage;
		updateObserver(); //!!!
	}

	public void setLocationTag(String locationTag)
	{
		if(locationTag == null || locationTag.equals("") || locationTag.equalsIgnoreCase("null"))
			this.lastLocation = null;
		else
		{
			this.lastLocation = new NTLocation(locationTag);
			log.debug("Manual location: " + lastLocation.toString());
		}
	}

	/**
	 * @return the lastLocation
	 */
	public NTLocation getLastLocation()
	{
		return lastLocation;
	}

}
