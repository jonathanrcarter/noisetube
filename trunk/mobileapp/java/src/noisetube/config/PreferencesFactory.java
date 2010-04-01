package noisetube.config;

import noisetube.MainMidlet;

public class PreferencesFactory
{
	public static Preferences build_preferences()
	{
		// Watch Option
		Preferences pref;
		if(get_default_watch_setting() && Device.supportsBluetoothAPI())
		{
			pref = watch();
		}
		else

		if(MainMidlet.environment == MainMidlet.EMULATOR_ENV
				|| MainMidlet.environment == MainMidlet.PHONE_DEV_ENV)
			pref = development();
		else
			pref = new Preferences();

		pref.configure();
		return pref;
	}

	protected static boolean get_default_watch_setting()
	{
		String watchSupported = MainMidlet.getInstance().getAppProperty(
				"NoiseTube-WatchSupported");
		if(watchSupported != null && !watchSupported.equals(""))
			return watchSupported.equalsIgnoreCase("true");
		else
			return false;
	}

	protected static boolean get_default_gps_setting()
	{
		// GPS SUPPORTED IN THE CLIENT (!= GPS support of the device, which is
		// checked in the Device class)
		String gpsSupported = MainMidlet.getInstance().getAppProperty(
				"NoiseTube-GPSSupported");
		if(gpsSupported != null && !gpsSupported.equals(""))
			return gpsSupported.equalsIgnoreCase("true");
		else
			return false;
	}

	protected static int get_default_saving_mode()
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
		p.setUsername("nico");
		p.setAPIKey("6ba11c291138bc630ae9dfa1378214d5c2262ec2");
		p.setSavingMode(Preferences.SAVE_HTTP);
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
