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

package net.noisetube.config.javame;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.file.FileSystemRegistry;

import com.sun.lwuit.Display;

import me.regexp.RE;

import net.noisetube.audio.format.AudioFormat;
import net.noisetube.audio.format.AudioSpecification;
import net.noisetube.audio.javame.JavaMEAudioSpecification;
import net.noisetube.config.Device;

import net.noisetube.core.NTClient;
import net.noisetube.core.javame.JavaMENTClient;
import net.noisetube.core.javame.MainMIDlet;
import net.noisetube.io.javame.FileAccess;
import net.noisetube.util.StringUtils;

/**
 * @author mstevens, maisonneuve
 * 
 */
public class JavaMEDevice extends Device
{
	
	//STATIC-----------------------------------------------
	
	//Brands
	public static final int BRAND_UNKNOWN = 0;
	public static final int BRAND_SONYERICSSON = 1;
	public static final int BRAND_NOKIA = 2;
	public static final int BRAND_SAMSUNG = 3;
	public static final int BRAND_LG = 4;
	public static final int BRAND_HTC = 5;
	public static final int BRAND_APPLE = 6;
	public static final int BRAND_MOTOROLA = 7;
	public static final int BRAND_ACER = 8;
	public static final int BRAND_ASUS = 9;
	public static final int BRAND_ZTE = 10;
	//...
	
	public static String getBrandName(int brandID)
	{
		String name = null;
    	switch(brandID)
    	{
    		case BRAND_UNKNOWN : name = "unknown"; break;
    		case BRAND_SONYERICSSON : name = "Sony Ericsson"; break;
    		case BRAND_NOKIA : name = "Nokia"; break;
	        case BRAND_SAMSUNG : name = "Samsung"; break;
	        case BRAND_LG : name = "LG"; break;
	        case BRAND_HTC : name = "HTC"; break;
	        case BRAND_APPLE : name = "Apple"; break;
	        case BRAND_MOTOROLA : name = "Motorola"; break;
	        case BRAND_ACER : name = "Acer"; break;
	        case BRAND_ASUS : name = "Asus"; break;
	        case BRAND_ZTE : name = "ZTE"; break;
	        default : name = "unknown";
    	}
    	return name;
	}
	
	//DYNAMIC----------------------------------------------
	protected int brandID;
	
	//File system:
	private static String[] fileSystemRoots = null;
	private static final String APPLICATION_DATA_FOLDER_NAME = "NoiseTube";
	
	public void identifyDevice()
	{
		javaPlatform = System.getProperty("microedition.platform");
		if(javaPlatform != null && !javaPlatform.equals("") && !javaPlatform.equalsIgnoreCase("null") && !javaPlatform.equalsIgnoreCase("j2me"))
		{
			//Parse platform:
			if(javaPlatform.substring(0, 5).equalsIgnoreCase("Nokia"))
			{
				brandID = BRAND_NOKIA;
				// Example platform strings for Nokia:
				// - Nokia6220c-1/.13/sw_platform=S60;sw_platform_version=3.2;java_build_version=74701
				// - NokiaN85-1/.047/sw_platform=S60;sw_platform_version=3.2;java_build_version=74701
				// - NokiaN79-1/30.019/sw_platform=S60;sw_platform_version=3.2;java_build_version=1.0.4
				// - Nokia5800/21.0.025.1/sw_platform=S60;sw_platform_version=5.0;java_build_version=1.3.4
				// - NokiaN8-00/1.012/sw_platform=S60;sw_platform_version=5.2;java_build_version=2.1.41
				RE regex = new RE("Nokia(.+)/(.+)/sw_platform=(.+);sw_platform_version=(.+);java_build_version=(.+)");
				if(regex.match(javaPlatform))
				{
					model = regex.getParen(1).trim();
					firmwareVersion = regex.getParen(2).trim();
					platform = regex.getParen(3).trim();
					platformVersion = regex.getParen(4).trim();
					javaPlatform = "Nokia Java Runtime";
					javaPlatformVersion = regex.getParen(5).trim();
				}
				else
				{ // Examples with only brand, model and version:
					// - Nokia6500s-1/04.80
					// - NokiaN95_8GB/31.0.015
					regex = new RE("Nokia(.+)/(.+)");
					if(regex.match(javaPlatform))
					{
						model = regex.getParen(1).trim();
						firmwareVersion = regex.getParen(2).trim();
						javaPlatform = "Nokia Java Runtime";
					}
				}
			}
			else if(javaPlatform.substring(0, 12).equalsIgnoreCase("SonyEricsson"))
			{
				brandID = BRAND_SONYERICSSON;
				/*
				 * Examples platform string for SE: - SonyEricssonP1i/R6E30 -
				 * SonyEricssonW995/R1GA019 - SonyEricssonW890i/R1DA028
				 */
				RE regex = new RE("SonyEricsson(.+)/(.+)");
				if(regex.match(javaPlatform))
				{
					model = regex.getParen(1).trim();
					modelVersion = regex.getParen(2).trim();
				}
				javaPlatform = "Sony Ericsson Java Platform";
				if(System.getProperty("com.sonyericsson.java.platform") != null)
					javaPlatformVersion = System.getProperty("com.sonyericsson.java.platform").trim(); //examples: "SJP-3.3", "JP-8.4.4"
				else
					javaPlatform = "JP-6 or earlier"; //the com.sonyericsson.java.platform property is only supported since JP-7
			}
			else if(javaPlatform.substring(0, 7).equalsIgnoreCase("Samsung") || javaPlatform.substring(0, 3).equalsIgnoreCase("SEC"))
			{
				brandID = BRAND_SAMSUNG;
				/*
				 * Examples: SAMSUNG-SGH-D880/D880XEGK4
				 * SAMSUNG-GT-S5503/S5503DXIJ1 SAMSUNG-GT-S3650C/S3650CZCII5
				 * SAMSUNG-E250i/E250iXEIA5 Samsung/SGH-i550/50DBHD1
				 * SEC-SGHU600/MSGC4 SEC-SGHE840/XEGE4 SEC-SGHJ600/BBGF2
				 */
				RE regex = new RE("(SAMSUNG|Samsung|SEC)(-|/)(.+)/(.+)");
				if(regex.match(javaPlatform))
				{
					model = regex.getParen(3).trim();
					modelVersion = regex.getParen(4).trim();
				}
				javaPlatform = "Samsung J2ME platform";
			}
			else if(javaPlatform.substring(0, 3).equalsIgnoreCase("GT-") || javaPlatform.substring(0, 4).equalsIgnoreCase("SGH-"))
			{
				brandID = BRAND_SAMSUNG;
				//TODO detect Samsung models
				/*Examples:
				 * - GT-S5233S/S5233SDXIF2
				 * - GT-M8800/M8800XXHK4
				 * - GT-S5233W/S5233WJVIG7
				 * - SGH-F480/F480XEHJ1
				 * - SGH-T919/T919UVHL3
				 * - SGH-E576
				 * - SGH-U700V */
			}
			//TODO also Samsung:
			// - S8000/S8000XPII1
			// - 1.0/SamsungSGHi560/60XXHC1
			//TODO More brands/models later...
			else
				brandID = BRAND_UNKNOWN;
		}
		else
			brandID = BRAND_UNKNOWN;
		brand = getBrandName(brandID); //!!!
	}
	
	/**
	 * @return the brandID
	 */
	public int getBrandID()
	{
		return brandID;
	}

	public void logFunctionalities()
	{
		//Audio---------------------------------------------------------------------
		if(!supportsMultiMediaAPI())
		{
			log.info(" - MultiMedia API (JSR-135) is not supported (required for recording audio). NoiseTube Mobile cannot function.");
		}
		else
		{
			log.info(" - Multimedia API (JSR-135) supported.");
			AudioSpecification as = getAudioSpecification(); 
			if(as == null)
			{
				log.info(" - No support for capturing audio in a NoiseTube-supported format.");
			}
			else
			{
				log.info(" - Audio recording specification which will be used: " + as.toVerboseString());
			}
		}
		//--------------------------------------------------------------------------

		//IO------------------------------------------------------------------------
		if(!supportsInternetAccess())
		{
			log.info(" - Warning: No Internet access. Saving to NoiseTube.net account may not work.");
		}
		else
		{
			log.info(" - Internet access available (NoiseTube server responded to ping).");
		}
		if(!supportsFileAccess())
		{
			log.info(" - Warning: No support for FileConnection API (JSR-75). Saving to file will not be available.");
		}
		else
		{
			log.debug(" - FileConnection API (JSR-75) supported");
			log.debug(" - Directory separator is: " + FileAccess.getDirectorySeparator());
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
				log.info(" - Warning: Cannot detect filesystem roots. Saving to file will not be available.");
		}
		//--------------------------------------------------------------------------

		//Positioning/GPS-----------------------------------------------------------
		if(!supportsPositioning())
			log.info(" - Warning: No support for Location API (JSR-179). Localisation through GPS will be unavailable.");
		else
			log.info(" - Location API (JSR-179) supported.");
		//--------------------------------------------------------------------------

		//Bluetooth-----------------------------------------------------------------
		if (!supportsBluetooth())
			log.info(" - No support for Bluetooth API (JSR-82).");
		else
			log.info(" - Bluetooth API (JSR-82) supported.");
		//--------------------------------------------------------------------------

		//Touch screen--------------------------------------------------------------
		log.info(" - Device " + (hasTouchScreen() ? "has" : "does not have") + " a touch screen.");
		//--------------------------------------------------------------------------
		
		//Nokia UI API--------------------------------------------------------------
		 if (!supportsNokiaUIAPI())
			 log.info(" - No support for Nokia UI API. Screensaver blocker will be unavailable.");
		 else
			 log.debug(" - Nokia UI API supported. Screensaver blocker available.");
		//--------------------------------------------------------------------------
	}

	public boolean supportsPositioning()
	{
		return System.getProperty("microedition.location.version") != null;
	}

	public static boolean supportsFileConnectionAPI()
	{
		return System.getProperty("microedition.io.file.FileConnection.version") != null;
	}

	public boolean supportsFileAccess()
	{
		return supportsFileConnectionAPI() && getFileSystemRoots() != null;
	}

	private String[] getFileSystemRoots()
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
	
	public String getMemoryCardRoot()
	{
		if(supportsFileAccess())
		{
			String[] roots = getFileSystemRoots();
			if(roots == null)
			{
				return null;
			}
			for(int r = roots.length - 1; r >= 0; r--)
			{
				switch(brandID)
				{
					case BRAND_NOKIA:
						if(roots[r].equalsIgnoreCase("Memory card/") || roots[r].equalsIgnoreCase("E:/"))
							return roots[r];
						else
							break;
					case BRAND_SONYERICSSON:
						if(roots[r].startsWith("e:") || roots[r].startsWith("E:"))
							return roots[r];
						else
							break;
					//...
				}
			}
			return null;
		}
		else
		{
			return null;
		}
	}

	private String getDataFolderPathForRoot(String root)
	{
		char separator = FileAccess.getDirectorySeparator();
		String path = "file:///" + root;
		// Some brands don't allow folders to be created at root level, so we
		// need to use an (existing) subfolder:
		switch(brandID)
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
			//...
		}
		return path + APPLICATION_DATA_FOLDER_NAME + separator;
	}

	public static boolean supportsMultiMediaAPI()
	{
		return System.getProperty("microedition.media.version") != null;
	}
	
	public boolean supportsAudioRecording()
	{
		return supportsMultiMediaAPI() && super.supportsAudioRecording();
	}

	public String getDataFolderPath(boolean preferMemoryCard)
	{
		if(!supportsFileAccess())
			return null;
		if(NTClient.ENVIRONMENT == NTClient.EMULATOR_ENV)
			return "file:///root1/NoiseTube/";
		String path = null;
		try
		{
			String memoryCardRoot = null;
			if(preferMemoryCard)
			{
				memoryCardRoot = getMemoryCardRoot();
				if(memoryCardRoot != null)
				{
					path = getDataFolderPathForRoot(memoryCardRoot);
					if(!FileAccess.canWriteToFolder(path, true))
						path = null;
				}
			}
			if(path == null) //memory card not preferred or memory card could not be accessed
			{	//try every root...
				String[] roots = getFileSystemRoots();
				if(roots != null)
				{
					for(int r = 0; r < roots.length; r++)
					{
						String p = getDataFolderPathForRoot(roots[r]);
						if(FileAccess.canWriteToFolder(p, true))
						{
							path = p;
							break; //no need to try the other roots
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			log.error(e, "getDataFolderPath()");
			path = null;
		}
		return path;
	}

	protected AudioSpecification getSuitableAudioSpecification()
	{
		String encodingsProperty = System.getProperty("audio.encodings");
		if(supportsMultiMediaAPI() && encodingsProperty != null && !encodingsProperty.equals(""))
		{
			/* Examples of audio.encodings system property strings:
			 *  - Sun Java(TM) Platform Micro Edition SDK 3.0 emulator:
			 * 		encoding=pcm&rate=22050&bits=16&channels=1
			 *  - Nokia 5320:
			 *		encoding=audio/amr encoding=audio/basic encoding=audio/x-au encoding=audio/au encoding=audio/x-basic encoding=audio/wav encoding=audio/x-wav encoding=pcm encoding=ulaw encoding=gsm
			 *  - Sony Ericsson W995:
			 *  	encoding=amr encoding=amr-nb encoding=pcm
			 *  - Sony Ericsson Satio:
			 *		encoding=audio/wav encoding=audio/amr encoding=audio/amr-wb  
			 */
			String[] entries = StringUtils.split(encodingsProperty, ' ');
			
			/*DEBUG*/
			log.debug(" - Supported encodings:");
			for(int e = 0; e < entries.length; e++)
				log.debug("    * " + entries[e].trim());
			
			for(int e = 0; e < entries.length; e++)
			{
				String encodingsEntry = entries[e].trim();
				if(JavaMEAudioSpecification.isWAVEPCMEncoding(encodingsEntry)) //to skip non-WAVE/PCM entries right away
				{
					int[] sampleRatesToTry = AudioFormat.SAMPLE_RATES; //ordered from high to low!
					switch(brandID)
					{
						case BRAND_NOKIA :
							/* Nokia's Java Runtime platform has a long-standing bug which causes midlets that repeatedly
							 * record bits of audio (such as NoiseTube does) to freeze or crash/exit after a while.
							 * The duration before the freeze/crash happens is correlated with the sample rate of the recordings
							 * (and thus with the size of the recording buffer, which is why we think that the cause is most
							 * likely a memory leak in the underlying Java ME runtime implementation).
							 * In versions of NoiseTube compiled for experimental usage we have worked-around this problem by
							 * automatically exiting (in a clean way) and restarting (using the PushRegistry API) the application
							 * _before_ the crash happens. However, this is considered too "user-unfriendly" for the version of
							 * NoiseTube that is offered for download to the general public. Therefore we will limit the sample rate
							 * for Nokia's to 16kHz (postponing the crash to well beyond an hour hopefully) if the restarting behavior
							 * is not activated.
							 * 
							 * Forum thread about the bug: http://discussion.forum.nokia.com/forum/showthread.php?129876-N80-MIDlet-crashes-after-recording-short-audio-segments-for-7-20-minutes
							 *  
							 * TODO (re)try buffer and recording size tweaks to maybe avoid the bug (details in JavaMERecordTask)
							 * TODO check if the bug is still present in Nokia Java Runtime v2.1 for S60 5th Edition (http://betalabs.nokia.com/apps/java-runtime-for-Symbian)
							 * TODO check if this bug is still present in Symbian^3 and above
							 */
							if(!JavaMENTClient.RESEARCH_BUILD) //if this is not a research build we want to maximise running time by lowering samplerate:
								sampleRatesToTry = new int[] { 16000, 11025, 8000/*, 4000*/ }; //Hz
							break;
						case BRAND_SONYERICSSON :
							/* Sony Ericsson Java ME Platform CLDC MIDP 2 Developers' Guidelines (Jun 23, 2010):
							 * 	 Supported Audio codecs for recordings:
							 *    • AMR (NB) : 8KHz - 128b/s
							 *    • PCM : 16KHz - 256kb/s (from JP-8)
							 *  (http://developer.sonyericsson.com/cws/download/1/717/011/1277362267/DW-65067-dg_java_me_r36a.pdf) */
							sampleRatesToTry = new int[] { 16000 };
							break;
						//...other brands?
					}
					//Try to create an AudioSpec for each sample rate and test them...
					for(int s = 0; s < sampleRatesToTry.length; s++)
					{
						JavaMEAudioSpecification as = new JavaMEAudioSpecification(encodingsEntry, sampleRatesToTry[s], 16, AudioFormat.CHANNELS_MONO, false);
						if(testAudioSpecification(as))
							return as; //working spec found, return it
					}
				}
			}
		}
		//if we get here no suitable audio specification was found
		return null;
	}
	
	public static boolean hasSmallScreen()
	{
		return Display.getInstance().getDisplayHeight() < 360;
	}
	
	public boolean supportsBluetooth()
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
	
	public String getIMEI()
	{
		String[] imeiProps = { 	"phone.imei", "com.nokia.IMEI", "com.nokia.mid.imei",
								"com.sonyericsson.imei", "IMEI", "com.motorola.IMEI",
								"com.samsung.imei", "com.siemens.imei" };
		String imei = null;
		for(int p = 0; p < imeiProps.length; p++)
		{
			imei = MainMIDlet.getInstance().getAppProperty(imeiProps[p]);
			if(imei != null && !imei.equals(""))
				break;
			imei = MainMIDlet.getInstance().getAppProperty(imeiProps[p].toLowerCase()); //try same with all lower case
			if(imei != null && !imei.equals(""))
				break;
		}
		return imei;
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

	public boolean hasTouchScreen()
	{
		return Display.getInstance().isTouchScreenDevice();
	}

}