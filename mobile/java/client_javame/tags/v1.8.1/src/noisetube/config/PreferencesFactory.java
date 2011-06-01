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

package noisetube.config;

import noisetube.MainMidlet;

/**
 * @author maisonneuve, mstevens
 *
 */
public class PreferencesFactory
{
	public static Preferences build_preferences()
	{
		// Watch Option
		Preferences pref;

//		if(get_default_watch_setting() && Device.supportsBluetoothAPI())
//		{
//			pref = watch();
//		}
//		else

		if(MainMidlet.environment == MainMidlet.EMULATOR_ENV || MainMidlet.environment == MainMidlet.PHONE_DEV_ENV)
			pref = development();
		else
			pref = new Preferences();

		pref.configure();
		return pref;
	}

	protected static boolean get_default_watch_setting()
	{
		String watchSupported = MainMidlet.getInstance().getAppProperty("NoiseTube-WatchSupported");
		if(watchSupported != null && !watchSupported.equals(""))
			return watchSupported.equalsIgnoreCase("true");
		else
			return false;
	}

	protected static boolean get_default_gps_setting()
	{
		// GPS SUPPORTED IN THE CLIENT (!= GPS support of the device, which is
		// checked in the Device class)
		String gpsSupported = MainMidlet.getInstance().getAppProperty("NoiseTube-GPSSupported");
		if(gpsSupported != null && !gpsSupported.equals(""))
			return gpsSupported.equalsIgnoreCase("true");
		else
			return false;
	}

	protected static int get_default_saving_mode() //TODO remove this, this was already in pref's
	{
		// Saving mode
		if(Device.supportsHTTP())
		{
			return Preferences.SAVE_HTTP;
		}
		else if(Device.supportsSavingToFile())
			return Preferences.SAVE_FILE;
		/*
		 * else if(Device.supportsSMS()) savingMode = SAVE_SMS;
		 */
		else
			return Preferences.SAVE_NO;
	}

	public static WatchPreferences watch()
	{
		return new WatchPreferences();
	}

	public static Preferences development()
	{
		Preferences p = new Preferences();
		p.setSavingMode(Preferences.SAVE_FILE);
		p.setUseGPS(true);
		return p;
	}

	public static Preferences default_pref()
	{
		// GPS SUPPORTED IN THE CLIENT (!= GPS support of the device, which is
		// checked in the Device class)
		Preferences p = new Preferences();
		p.setUseGPS(get_default_gps_setting() && Device.supportsGPS());

		// WATCH SUPPORTED BY THIS CLIENT
		// Saving Mode
		p.setSavingMode(get_default_saving_mode());
		return p;
	}
}
