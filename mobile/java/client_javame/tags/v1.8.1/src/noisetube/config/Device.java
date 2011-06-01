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

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.file.FileSystemRegistry;

import me.regexp.RE;
import noisetube.MainMidlet;
import noisetube.audio.java.Calibration;
import noisetube.io.web.HTTPWebAPI;
import noisetube.util.Logger;

/**
 * @author mstevens, maisonneuve
 * 
 */
public class Device
{

	// Brands
	public static final int BRAND_UNKOWN = 0;
	public static final int BRAND_SONYERICSSON = 1;
	public static final int BRAND_NOKIA = 2;
	public static final int BRAND_SAMSUNG = 3;
	public static final int BRAND_LG = 4;
	// ...
	// Device info
	public static String DEVICE_J2ME_PLATFORM = "";
	public static int DEVICE_BRAND = 0;
	public static String DEVICE_BRAND_NAME;
	public static String DEVICE_BRAND_NAME_SHORT = null;
	public static String DEVICE_MODEL;
	public static String DEVICE_MODEL_VERSION;
	public static String DEVICE_PLATFORM = null;
	public static String DEVICE_PLATFORM_VERSION = null;
	// HTTP:
	private static Boolean httpSupported = null;
	// File system:
	private static final String APPLICATION_DATA_FOLDER_NAME = "NoiseTube";
	private static Character fileSeparator = null;
	private static String[] fileSystemRoots = null;
	private static boolean fileSavingDisabled = false;
	// Audio capture:
	private static int bestSampleRate = 16000; //default (in Hz)
	private static String bestAudioEncoding = null;
	// Logger:
	private static Logger log = Logger.getInstance();

	private static final double[][] NOKIA_5230 = {{24.64, 30.00}, {25.83, 35.00}, {27.24, 40.10}, {29.05, 44.90}, 
		{35.00, 50.10}, {43.34, 55.00}, {51.84, 60.05}, {59.95, 65.05}, {68.51, 70.05}, {72.92, 75.10}, {77.02, 79.95}, 
		{81.20, 84.95},{85.18, 89.95}, {87.25, 95.00}, {88.84, 100.00}, {90.18, 103.8}};

	//TODO UPDATE FOR NEW SCALING OF 93.x instead of 100	
	private static final double[][] NOKIA_N95_8GB = { { 36, 37 }, { 37, 39 },
			{ 42, 44 }, { 45, 47 }, { 49, 49 }, { 56, 54 }, { 62, 59 },
			{ 68, 64 }, { 73, 69 }, { 75, 74 }, { 80, 79 }, { 82, 84 },
			{ 85, 89 }, { 87, 94 }, { 89, 99 }, { 90, 102 } };
	private static final double[][] SE_W995 = { { 63.5, 47 }, { 68.4, 53 },
			{ 71.4, 55 }, { 75.6, 60 }, { 80.6, 65 }, { 82.3, 70.7 },
			{ 83.8, 76 }, { 84.7, 80 }, { 85.2, 85 } };
	private static final double[][] NOKIA_E65 = { { 64.8, 60 }, { 65.4, 65 },
			{ 67.8, 70 }, { 69.5, 75 }, { 71.9, 80 }, { 74.2, 85 },
			{ 76.7, 90 }, { 81.0, 95 }, { 86.2, 100 } };
	private static final double[][] NOKIA_N96 = { { 60.0, 60 }, { 60.0, 60 },
			{ 58.5, 65 }, { 67.9, 70 }, { 71.4, 75 }, { 74.8, 80 },
			{ 77.6, 85 }, { 82.4, 90 }, { 87.4, 95 }, { 91.6, 100 } };
	private static final double[][] NOKIA_5800 = { { 50, 64 }, { 67, 75 },
			{ 73, 83 }, { 74, 86 }, { 77, 89 }, { 90, 100 } };
	private static final double[][] NOKIA_N85 = { { 50, 64 }, { 67, 75 },
			{ 73, 83 }, { 74, 86 }, { 77, 89 }, { 90, 100 } };

	//TODO make samplerate device dependent
	
	
	public static int getIntervalTime() //TODO maybe make this truely device dependent, otherwise move it somewhere else
	{
		return 1200;
	}

	public static Calibration getCalibration()
	{
		double[][] DEFAULT = NOKIA_N95_8GB; //TODO make this NOKIA_5230 ?
		// Brand/model specific:
		if(Device.DEVICE_MODEL != null)
		{
			// SE W995
			if(Device.DEVICE_BRAND == Device.BRAND_SONYERICSSON)
			{
				if(Device.DEVICE_MODEL.equalsIgnoreCase("W995"))
				{
					DEFAULT = SE_W995;
					log.debug("Calibration for W995");
				}
			}
			else if(Device.DEVICE_BRAND == Device.BRAND_NOKIA)
			{
				if(Device.DEVICE_MODEL.equalsIgnoreCase("N95_8GB"))
				{
					DEFAULT = NOKIA_N95_8GB;
					log.debug("Calibration for Nokia N95 8GB");
				}
				else if(Device.DEVICE_MODEL.equalsIgnoreCase("N96"))
				{
					DEFAULT = NOKIA_N96;
					log.debug("Calibration for Nokia N96");
				}
				else if(Device.DEVICE_MODEL.equalsIgnoreCase("E65"))
				{
					DEFAULT = NOKIA_E65;
					log.debug("Calibration for Nokia E65");
				}
				else if(Device.DEVICE_MODEL.equalsIgnoreCase("N85"))
				{
					DEFAULT = NOKIA_N85;
					log.debug("Calibration for Nokia N85");
				}
				else if(Device.DEVICE_MODEL.equals("5800"))
				{
					DEFAULT = NOKIA_5800;
					log.debug("Calibration for Nokia 5800");
				}
				else if(Device.DEVICE_MODEL.equals("5230"))
				{
					DEFAULT = NOKIA_5230;
					bestSampleRate = 48000; //TODO move this to a generic sample rate checking function?
					log.debug("Calibration for Nokia 5230 and sample rate of 48000Hz");
				}
				else //other Nokia's
				{
					DEFAULT = NOKIA_N95_8GB; //TODO make this NOKIA_5230 ?
					log.debug("Calibration for an unknown Nokia");
				}
			}
			//Other brands?
		}
		else
		{
			log.debug("default Calibration for unknown model");
		}
		return new Calibration(DEFAULT);
	}

	public static void checkDevice() throws Exception
	{
		identifyDeviceAndPlatform(); //!!!

		// Log debug
		// info--------------------------------------------------------------
		log.debug("Device info:");
		log.debug(" - J2ME platform string: " + DEVICE_J2ME_PLATFORM);
		log.debug(" - Brand: " + DEVICE_BRAND_NAME);
		log.debug(" - Model: " + DEVICE_MODEL);
		log.debug(" - Model (firmware) version: " + DEVICE_MODEL_VERSION);
		log.debug(" - Software platform: " + DEVICE_PLATFORM);
		log.debug(" - Software platform version: " + DEVICE_PLATFORM_VERSION);
		// ...
		// ----------------------------------------------------------------------------

		// Audio-----------------------------------------------------------------------
		if(!supportsAudioCapture())
		{
			throw new Exception("No support for audio capturing. NoiseTube Mobile cannot function.");
		}
		else
		{
			log.debug(" - Multimedia API (JSR-135) & audio capturing supported");
		}
		if(getBestAudioEncoding() == null)
		{
			throw new Exception("No support for capturing audio in WAV, PCM, AU or RAW format.\nSupported: " + System.getProperty("audio.encodings") + "\nNoiseTube Mobile cannot function.");
		}
		else
		{
			log.debug(" - Suitable audio encoding(s) supported; chosen: " + getBestAudioEncoding());
		}
		// ----------------------------------------------------------------------------

		// Saving----------------------------------------------------------------------
		if(!supportsHTTP())
		{
			log.info(" - Warning: No Internet access. Saving to NoiseTube.net account may not work.");
		}
		else
		{
			log.debug(" - Internet access available (NoiseTube server responded to ping)");
		}
		if(!supportsFileConnectionAPI())
		{
			log.info(" - Warning: No support for FileConnection API (JSR-75). Saving to file will not be available.");
		}
		else
		{
			log.debug(" - FileConnection API (JSR-75) supported");
			log.debug(" - File separator is: " + getFileSeparator());
			String[] roots = getFileSystemRoots();
			if(roots != null)
			{
				log.debug(" - Available file system roots:");
				for(int r = 0; r < roots.length; r++)
				{
					log.debug("    * " + roots[r]);
				}
			}
			else
			{
				log.info(" - Warning: Cannot detect filesystem roots. Saving to file will not be available.");
				disableFileSaving();
			}
		}
		if(!supportsSMS())
		{
			// ...
		}
		// ----------------------------------------------------------------------------

		// Localisation/GPS------------------------------------------------------------
		if(!supportsLocationAPI())
		{
			log.info(" - Warning: No support for Location API (JSR-179). Localisation through GPS will be unavailable.");
		}
		else
		{
			log.debug(" - Location API (JSR-179) supported");
		}
		// ----------------------------------------------------------------------------

		// Bluetooth-------------------------------------------------------------------
		// if (MainMidlet.getInstance().getPreferences().isUseWatch()) {
		// if (!supportsBluetoothAPI()) {
		// log.info(" - Warning: No support for Bluetooth API (JSR-82). Watch mode will be unavailable.");
		// } else {
		// log.debug(" - Bluetooth API (JSR-82) supported. Watch mode is available.");
		// }
		// }
		// ----------------------------------------------------------------------------

		// Nokia UI
		// API----------------------------------------------------------------
		// if (!supportsNokiaUIAPI()) {
		// log.info(" - Warning: No support for Nokia UI API. Screensaver blocker will be unavailable.");
		// } else {
		// log.debug(" - Nokia UI API supported. Screensaver blocker available.");
		// }
		// ----------------------------------------------------------------------------
	}

	private static void identifyDeviceAndPlatform()
	{
		// DEVICE_PLATFORM
		DEVICE_J2ME_PLATFORM = System.getProperty("microedition.platform");
		if(DEVICE_J2ME_PLATFORM != null && !DEVICE_J2ME_PLATFORM.equals("")
				&& !DEVICE_J2ME_PLATFORM.equalsIgnoreCase("null")
				&& !DEVICE_J2ME_PLATFORM.equalsIgnoreCase("j2me"))
		{
			// Parse platform:
			if(DEVICE_J2ME_PLATFORM.startsWith("Nokia"))
			{
				DEVICE_BRAND = BRAND_NOKIA;
				DEVICE_BRAND_NAME = "Nokia";
				// Example platform strings for Nokia:
				// Nokia6220c-1/.13/sw_platform=S60;sw_platform_version=3.2;java_build_version=74701
				// NokiaN85-1/.047/sw_platform=S60;sw_platform_version=3.2;java_build_version=74701
				// NokiaN79-1/30.019/sw_platform=S60;sw_platform_version=3.2;java_build_version=1.0.4
				// Nokia5800/21.0.025.1/sw_platform=S60;sw_platform_version=5.0;java_build_version=1.3.4
				RE regex = new RE(
						"Nokia(.+)/(.+)/sw_platform=(.+);sw_platform_version=(.+);.+");
				if(regex.match(DEVICE_J2ME_PLATFORM))
				{
					DEVICE_MODEL = regex.getParen(1);
					DEVICE_MODEL_VERSION = regex.getParen(2);
					DEVICE_PLATFORM = regex.getParen(3);
					DEVICE_PLATFORM_VERSION = regex.getParen(4);
				}
				else
				{ // Examples with only brand, model and version:
					// - Nokia6500s-1/04.80
					// - NokiaN95_8GB/31.0.015
					regex = new RE("Nokia(.+)/(.+)");
					if(regex.match(DEVICE_J2ME_PLATFORM))
					{
						DEVICE_MODEL = regex.getParen(1);
						DEVICE_MODEL_VERSION = regex.getParen(2);
					}

				}
			}
			else if(DEVICE_J2ME_PLATFORM.startsWith("SonyEricsson"))
			{
				DEVICE_BRAND = BRAND_SONYERICSSON;
				DEVICE_BRAND_NAME = "Sony Ericsson";
				DEVICE_BRAND_NAME_SHORT = "SE";
				/*
				 * Examples platform string for SE: - SonyEricssonP1i/R6E30 -
				 * SonyEricssonW995/R1GA019 - SonyEricssonW890i/R1DA028
				 */
				RE regex = new RE("SonyEricsson(.+)/(.+)");
				if(regex.match(DEVICE_J2ME_PLATFORM))
				{
					DEVICE_MODEL = regex.getParen(1);
					DEVICE_MODEL_VERSION = regex.getParen(2);
				}

				if(System.getProperty("com.sonyericsson.java.platform") != null)
				{ // examples: "SJP-3.3", "JP-8.4.4"
					DEVICE_PLATFORM = "Sony Ericsson Java Platform";
					DEVICE_PLATFORM_VERSION = System
							.getProperty("com.sonyericsson.java.platform");
				}

			}
			else if(DEVICE_J2ME_PLATFORM.startsWith("SAMSUNG")
					|| DEVICE_J2ME_PLATFORM.startsWith("Samsung")
					|| DEVICE_J2ME_PLATFORM.startsWith("SEC"))
			{
				DEVICE_BRAND = BRAND_SAMSUNG;
				DEVICE_BRAND_NAME = "Samsung";
				/*
				 * Examples: SAMSUNG-SGH-D880/D880XEGK4
				 * SAMSUNG-GT-S5503/S5503DXIJ1 SAMSUNG-GT-S3650C/S3650CZCII5
				 * SAMSUNG-E250i/E250iXEIA5 Samsung/SGH-i550/50DBHD1
				 * SEC-SGHU600/MSGC4 SEC-SGHE840/XEGE4 SEC-SGHJ600/BBGF2
				 */
				RE regex = new RE("(SAMSUNG|Samsung|SEC)(-|/)(.+)/(.+)");
				if(regex.match(DEVICE_J2ME_PLATFORM))
				{
					DEVICE_MODEL = regex.getParen(3);
					DEVICE_MODEL_VERSION = regex.getParen(4);
				}

			}
			else if(DEVICE_J2ME_PLATFORM.startsWith("GT-")
					|| DEVICE_J2ME_PLATFORM.startsWith("SGH-"))
			{
				DEVICE_BRAND = BRAND_SAMSUNG;
				DEVICE_BRAND_NAME = "Samsung";
				// GT-S5233S/S5233SDXIF2
				// GT-M8800/M8800XXHK4
				// GT-S5233W/S5233WJVIG7

				// SGH-F480/F480XEHJ1
				// SGH-T919/T919UVHL3

				// SGH-E576
				// SGH-U700V

			} // also samsung:
			// S8000/S8000XPII1
			// 1.0/SamsungSGHi560/60XXHC1
			else
			{
				DEVICE_BRAND = BRAND_UNKOWN;
				DEVICE_BRAND_NAME = "Unknown";
			}
			// More brands/models later...

			if(DEVICE_BRAND_NAME_SHORT == null)
			{
				DEVICE_BRAND_NAME_SHORT = DEVICE_BRAND_NAME;
			}
		}

	}

	public static boolean supportsHTTP()
	{
		if(httpSupported == null)
		{
			HTTPWebAPI api = new HTTPWebAPI();
			httpSupported = new Boolean(api.pingNoiseTubeService());
		}

		return httpSupported.booleanValue();
	}

	public static boolean supportsSMS()
	{ // TODO detect SMS access/permission
		return false;
	}

	private static boolean supportsLocationAPI() // private because other classes should use supportsGPS()
	{
		return System.getProperty("microedition.location.version") != null;
	}

	public static boolean supportsGPS()
	{
		//device should support the LocationAPI but this NoiseTube client should also have been compiled with GPS support!:
		return supportsLocationAPI() && MainMidlet.CLIENT_HAS_GPS_SUPPORT;
	}

	private static boolean supportsFileConnectionAPI() // private because other classes should use supportsSavingToFile()
	{
		return System.getProperty("microedition.io.file.FileConnection.version") != null;
	}

	public static void disableFileSaving()
	{
		fileSavingDisabled = true;
	}

	public static boolean supportsSavingToFile()
	{
		return(supportsFileConnectionAPI() && !fileSavingDisabled);
	}

	public static char getFileSeparator()
	{
		if(supportsFileConnectionAPI())
		{
			if(fileSeparator == null)
			{
				try
				{
					String sep = System.getProperty("file.separator");
					fileSeparator = new Character(
							(sep != null || sep != "") ? sep.charAt(0) : '/');
				}
				catch(Exception e)
				{
					fileSeparator = new Character('/'); // default
				}

			}
			return fileSeparator.charValue();
		}
		else
		{
			throw new IllegalStateException("FileConnection API not supported");
		}
	}

	public static String[] getFileSystemRoots()
	{
		if(supportsFileConnectionAPI())
		{
			if(fileSystemRoots == null)
			{
				try
				{
					Enumeration e = FileSystemRegistry.listRoots();
					Vector rootsVect = new Vector();
					while(e.hasMoreElements())
					{
						rootsVect.addElement(e.nextElement());
					}
					if(rootsVect.size() > 0)
					{
						fileSystemRoots = new String[rootsVect.size()];
						rootsVect.copyInto(fileSystemRoots);
					}

				}
				catch(Exception ex)
				{
					fileSystemRoots = null;
				}

			}
			return fileSystemRoots;
		}
		else
		{
			return null;
		}
	}

	public static String getMemoryCardRoot()
	{
		if(supportsFileConnectionAPI())
		{
			String[] roots = getFileSystemRoots();
			if(roots == null)
			{
				return null;
			}
			for(int r = roots.length - 1; r >= 0; r--)
			{
				switch(DEVICE_BRAND)
				{
				case BRAND_NOKIA:
					if(roots[r].equalsIgnoreCase("Memory card/")
							|| roots[r].equalsIgnoreCase("E:/"))
					{
						return roots[r];
					}
					else
					{
						break;
					}
				case BRAND_SONYERICSSON:
					if(roots[r].startsWith("e:") || roots[r].startsWith("E:"))
					{
						return roots[r];
					}
					else
					{
						break;
					}
					// ...
				}

			}
			return null;
		}
		else
		{
			return null;
		}
	}

	public static String getDataFolderPathForRoot(String root)
	{
		char separator = getFileSeparator();
		String path = "file:///" + root;
		// Some brands don't allow folders to be created at root level, so we
		// need to use an (existing) subfolder:
		switch(DEVICE_BRAND)
		{
		case BRAND_NOKIA:
			if(root.equalsIgnoreCase("C:/"))
			{
				path += "Data" + separator + "Images" + separator;
			}
			if(root.equalsIgnoreCase("Phone memory/"))
			{
				path += "Images" + separator;
			}
			break;

		case BRAND_SONYERICSSON:
			path += "other" + separator;
			break;
		// ...

		}

		return path + APPLICATION_DATA_FOLDER_NAME + separator;
	}

	public static boolean supportsMultiMediaAPI()
	{
		return System.getProperty("microedition.media.version") != null;
	}

	public static boolean supportsAudioCapture()
	{
		if(!supportsMultiMediaAPI()
				|| System.getProperty("supports.audio.capture") == null)
		{
			return false;
		}
		return System.getProperty("supports.audio.capture").equalsIgnoreCase(
				"true");
	}

	public static boolean supportsNativeRecorder()
	{ // TODO detect if platform supports JNI stuff and if we can connect to the
		// native recorder
		return false;
	}
	
	/**
	 * @return the bestSampleRate
	 */
	public static int getBestSampleRate()
	{
		return bestSampleRate;
	}

	/**
	 * Return the best audio encoding codecs
	 */
	public static String getBestAudioEncoding()
	{
		if(bestAudioEncoding == null)
		{
			bestAudioEncoding = findBestAudioEncoding();
		}
		return bestAudioEncoding;
	}

	private static String findBestAudioEncoding()
	{
		String[] enc = supportedAudioEncodings();
		if(enc == null)
		{
			return null;
		}
		String encoding = null;
		int priority = -1;
		for(int i = 0; i < enc.length; i++)
		{
			
			//TODO "audio/x-wav" ok as well? exclude until we are sure
			if(enc[i].indexOf("wav") > -1 && enc[i].indexOf("x-wav") == -1) // "audio/wav", , "wav", etc.
			{
				encoding = enc[i];
				// we prefer wav so break once we found it
				priority = 4;
			}

			if((enc[i].indexOf("pcm") > -1) && (priority < 3)) // "audio/pcm", "pcm", etc.
			{
				encoding = enc[i];
				priority = 3;
			}

			if(enc[i].equalsIgnoreCase("audio/au") && (priority < 2)) //indexOf("au") causes problem because "audio" matches "au" of course
			{
				encoding = enc[i];
				priority = 2;
			}

			if((enc[i].indexOf("raw") > -1) && (priority < 1)) // "audio/raw"
			{
				encoding = enc[i];
				priority = 1;
			}

		}
		return encoding;
	}

	/**
	 * Retrieve all the audio encoding codecs
	 * 
	 * @return a list of encoding codecs
	 */
	public static String[] supportedAudioEncodings()
	{
		String s = System.getProperty("audio.encodings");
		if(s == null || s.equals(""))
		{
			return null;
		}
		int start = s.indexOf("encoding=");
		Vector tmp = new Vector();
		String codecstring;

		while(start != -1)
		{
			int next = s.indexOf("encoding=", start + 1);
			if(next == -1)
			{
				codecstring = s.substring(start + 9, s.length());
				start = -1;
			}
			else
			{
				codecstring = s.substring(start + 9, next);
				start = next;
			}

			int codec_name_end = codecstring.indexOf("&");
			if(codec_name_end > -1)
			{
				codecstring = codecstring.substring(0, codec_name_end);
			}

			tmp.addElement(codecstring.trim());
		}

		String[] encodings = new String[tmp.size()];
		tmp.copyInto(encodings);
		return encodings;
	}

	public static boolean supportsBluetoothAPI()
	{
		try
		{
			Class.forName("javax.bluetooth.LocalDevice");
			return true;
		}
		catch(ClassNotFoundException cnfe)
		{
			return false;
		}

	}

	public static boolean supportsNokiaUIAPI()
	{
		try
		{
			Class.forName("com.nokia.mid.ui.DeviceControl");
			return true;
		}
		catch(ClassNotFoundException cnfe)
		{
			return false;
		}

	}
	public static boolean supportsWatch()
	{
		return(supportsBluetoothAPI() && supportsLocationAPI());
	}

	/**
	 * Detecting if a bluetooth device is connected?
	 * 
	 * @param deviceAddress
	 * @return
	 */
	public static boolean detectWatch(String deviceAddress)
	{
		if(supportsWatch())
		{
			String url = "btspp://" + deviceAddress
					+ ":01;authenticate=false;encrypt=false;master=false";
			log.debug("Connecting to watch " + deviceAddress);
			try
			{
				StreamConnection connection = (StreamConnection) Connector
						.open(url);
				InputStream is = connection.openInputStream();
				is.close();
				connection.close();
				return true;
			}
			catch(IOException e)
			{
				log.error(e, "watch not dectected");
				return false;
			}

		}
		else
		{
			return false;
		}
	}
}
