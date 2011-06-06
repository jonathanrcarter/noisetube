/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Android version)
 *  
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011
 *  Android port by Vrije Universiteit Brussel (BrusSense team), 2010-2011
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

package net.noisetube.config.android;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;

import net.noisetube.audio.android.AndroidAudioSpecification;
import net.noisetube.audio.format.AudioFormat;
import net.noisetube.audio.format.AudioSpecification;
import net.noisetube.config.Device;
import net.noisetube.core.NTClient;
import net.noisetube.core.android.AndroidNTClient;

/**
 * @author mstevens, sbarthol
 *
 */
public class AndroidDevice extends Device
{

	// relativeDataFolderPath is the relative path where data should be stored
	// It uses the dataFolderPath as a starting point
	private static String relativeDataFolderPath = "/data/";
	
	private String androidDeviceInfo;

	@Override
	protected void identifyDevice()
	{
		androidDeviceInfo = Build.BRAND + "|" + Build.MANUFACTURER + "|" + Build.MODEL + "|" + Build.PRODUCT + "|" + Build.DEVICE + "|" + Build.DISPLAY + "|" + Build.VERSION.CODENAME + "|" + Build.VERSION.INCREMENTAL + "|" + Build.VERSION.SDK_INT;
		/*Examples:
		 * - HTC Desire Z: htc_wwe|HTC|HTC Vision|htc_vision|vision||FRG83D|REL|317545|8
		 * - Sony Ericsson Xperia X10: SEMC|Sony Ericsson|X10i|X10i_1235-7836|X10i||2.1.A.0.435|REL|TP7d|7
		 */
		
		brand = Build.MANUFACTURER; //used to be BRAND in versions prior to v1.2.3 (the server has a hack that delivers a calibration.xml file with BRAND instead of MANUFACTURER to these versions)
		model = Build.MODEL;
		modelVersion = "unknown";
		firmwareVersion = "unknown";
		platform = "Android";
		platformVersion = Integer.toString(Build.VERSION.SDK_INT);	
		javaPlatform = "Dalvik";
		javaPlatformVersion = "unknown";
	}
	
	@Override
	public void logIdentification()
	{
		super.logIdentification();
		log.debug("Android device info: " + androidDeviceInfo);
	}
	
	@Override
	public boolean supportsFileAccess()
	{
		return true; //TODO check if permission is set
	}

	/**
	 * Returns if their is a memory card connected to the device
	 * @return
	 */
	public static boolean isMemoryCardPresent()
	{
		//Check if an external storage is connected:
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	/**
	 * Returns the path to the internal data folder root for this package
	 * @return internal data folder root. e.g. "/data/data/com.noisetube/files"
	 */
	public String getInternalRoot()
	{
		if(supportsFileAccess())
			return ((AndroidNTClient) NTClient.getInstance()).getContextWrapper().getFilesDir().getAbsolutePath();
		else
			return null;
	}

	/**
	 * Returns the path to the memory card root for this package
	 * @return memory card root. e.g. "/mnt/sdcard/com.noisetube/files/"
	 */
	public String getMemoryCardRoot()
	{
		if(supportsFileAccess() && isMemoryCardPresent())
			return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + ((AndroidNTClient) NTClient.getInstance()).getContextWrapper().getPackageName() + "/files";
		else
			return null;
	}

	@Override
	public boolean supportsPositioning()
	{
		return true;
	}

	@Override
	public boolean supportsBluetooth()
	{
		return (BluetoothAdapter.getDefaultAdapter().getState() == BluetoothAdapter.STATE_ON);
	}

	/** 
	 * @return the dataFolderPath (e.g. "/path/to/memory/root/my/data/")
	 */
	@Override
	public String getDataFolderPath(boolean preferMemoryCard)
	{
		String path = null;
		try
		{
			if(preferMemoryCard)
			{
				String memoryCardRoot = getMemoryCardRoot();
				if(memoryCardRoot != null)
				{
					path =  memoryCardRoot + relativeDataFolderPath;
				}
			}
			if(path == null)
			{
				//memory card is not preferred or we could not substract the path
				path = getInternalRoot() + relativeDataFolderPath;
			}
		}
		catch(Exception e)
		{
			log.error(e, "datafolder could not be made");
		}
		return path;
	}

	@Override
	public String getIMEI()
	{
		TelephonyManager manager = (TelephonyManager) ((AndroidNTClient) NTClient.getInstance()).getContextWrapper().getSystemService(Context.TELEPHONY_SERVICE);
		return manager.getDeviceId();
	}

	@Override
	protected AudioSpecification getSuitableAudioSpecification()
	{
		AUDIO_RECORDING_TEST_DURATION_MS = 1000; //default time (250ms) seems to result in missing samples on Android phones
		for(int s = 0; s < AudioFormat.SAMPLE_RATES.length; s++)
		{
			AudioSpecification as = new AndroidAudioSpecification(AudioFormat.SAMPLE_RATES[s], 16, AudioFormat.CHANNELS_MONO); 
			if(testAudioSpecification(as))
				return as; //working spec found, return it
		}
		//if we get here no suitable audio specification was found
		return null;
	}

	@Override
	public boolean hasTouchScreen()
	{
		//TODO do actual system check
		return true;
	}

	/**
	 * @return the androidDeviceInfo
	 */
	public String getAndroidDeviceInfo()
	{
		return androidDeviceInfo;
	}

}
