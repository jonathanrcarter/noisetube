/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation)
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

package net.noisetube.core;

import java.io.InputStream;
import java.util.Vector;

import net.noisetube.audio.AudioComponent;
import net.noisetube.audio.AudioStreamListener;
import net.noisetube.audio.AudioStreamSaver;
import net.noisetube.audio.AudioRecorder;
import net.noisetube.audio.calibration.Calibration;
import net.noisetube.audio.calibration.ICalibrationsParser;
import net.noisetube.audio.format.AudioSpecification;
import net.noisetube.config.Device;
import net.noisetube.config.Preferences;
import net.noisetube.io.FileWriter;
import net.noisetube.io.HttpClient;
import net.noisetube.io.ResourceReader;
import net.noisetube.location.LocationComponent;
import net.noisetube.model.INTCoordinates;
import net.noisetube.model.IMeasurementListener;
import net.noisetube.model.Track;
import net.noisetube.util.Logger;
import net.noisetube.util.StringUtils;

/**
 * @author mstevens
 *
 */
public abstract class NTClient
{

	//STATIC-----------------------------------------------
	static final public int EMULATOR_ENV = 0; 		//Emulator environment
	static final public int PHONE_DEV_ENV = 1; 		//Development phone environment
	static final public int PHONE_PROD_ENV = 2; 	//Production phone environment
	
	//Set active environment:
	static public int ENVIRONMENT = PHONE_PROD_ENV; //default
	
	public static String getEnvironmentName(int environment)
	{
		String name = null;
    	switch(environment)
    	{
    		case PHONE_PROD_ENV : name = "Phone/Production"; break;
    		case PHONE_DEV_ENV : name = "Phone/Development"; break;
    		case EMULATOR_ENV : name = "Emulator"; break;
	        default : name = "unknown";
    	}
    	return name;
	}
	
	private static NTClient INSTANCE = null;
	
	public static NTClient getInstance()
	{
		return INSTANCE;
	}
	
	public static void dispose()
	{
		Logger.dispose();
		INSTANCE = null;
	}
	
	//DYNAMIC----------------------------------------------
	protected String clientType;
	protected String clientVersion;
	protected String clientBuildDate;
	protected boolean testVersion;
	
	protected Logger log;
	protected Device device;
	protected Preferences preferences;
	protected Engine engine;
	protected boolean initialized = false;
	
	protected NTClient(String clientType, String clientVersion, String clientBuildDate, boolean testVersion) throws Exception
	{
		if(INSTANCE != null)
			throw new IllegalStateException("Cannot create more than one instance of NTClient (Singleton)");
		INSTANCE = this; //!!!
		this.clientType = clientType;
		this.clientVersion = clientVersion;
		this.clientBuildDate = clientBuildDate;
		this.testVersion = testVersion;
		
		//Logger
		log = Logger.getInstance();
		log.setNTClient(this);
		if(testVersion || ENVIRONMENT == EMULATOR_ENV)
			log.setLevel(Logger.DEBUG);
		
		//Subclasses must call initialize!
	}
	
	/**
	 * Sets the Device and Preferences
	 */
	protected void initialize() throws Exception
	{
		if(!initialized)
		{
			//Print log info:
			log.info(clientType + " " + clientVersion + " (build: " + clientBuildDate + ") started");
			log.info("Environment: " + getEnvironmentName(ENVIRONMENT));
			
			//Create device & log info:
			device = createDevice();
			device.logIdentification();
			device.logFunctionalities();
			
			//Check baseline functionality:
			Vector missingFeatures = new Vector();
			if(!device.supportsFileAccess())
				missingFeatures.addElement("Cannot access filesystem");
			if(!device.supportsAudioRecording())
				missingFeatures.addElement("Cannot record audio");
			if(!device.supportsPositioning())
				missingFeatures.addElement("Cannot use positioning (GPS)");
			
			if(!missingFeatures.isEmpty())
			{
				String errorMsg = "Unsupported device! Required functionality missing: " + StringUtils.StringVectorToString(missingFeatures, "; ");
				//write crash log to file if we can
				try
				{
					if(device.supportsFileAccess())
					{
						log.error(errorMsg);
						log.dumpCrashLog(createPreferences().getDataFolderPath());
					}
				}
				catch(Exception ignore) { } 
				throw new Exception(errorMsg); //!!! (will show up in GUI)
			}
			
			//Create preferences:
			preferences = createPreferences();
			
			//Last but not least:
			initialized = true;
		}
	}
	
	/**
	 * @return the clientType
	 */
	public String getClientType()
	{
		return clientType;
	}

	/**
	 * @return the clientVersion
	 */
	public String getClientVersion()
	{
		return clientVersion;
	}
	
	/**
	 * @return the clientBuildDate
	 */
	public String getClientBuildDate()
	{
		return clientBuildDate;
	}

	public String getClientIdentification()
	{
		return getClientType() + " " + getClientVersion();
	}

	/**
	 * @return the testVersion
	 */
	public boolean isTestVersion()
	{
		return testVersion;
	}

	/**
	 * @return the device
	 */
	public Device getDevice()
	{
		return device;
	}

	/**
	 * @return the preferences
	 */
	public Preferences getPreferences()
	{
		return preferences;
	}
	
	/**
	 * @return the engine
	 */
	public Engine getEngine()
	{
		if(engine == null && device != null && preferences != null)
			engine = new Engine(); //Initialize engine
		return engine;
	}

	/**
	 * @return the restartingModeEnabled
	 */
	public boolean isRestartingModeEnabled()
	{
		return false;
	}

	/**
	 * @return the firstRun
	 */
	public boolean isFirstRun()
	{
		return true;
	}

	public boolean isLastRun()
	{
		return true;
	}
	
	protected abstract Device createDevice();
	
	protected abstract Preferences createPreferences();
	
	public abstract AudioSpecification deserialiseAudioSpecification(String serialisedAudioSpec);
	
	public abstract AudioRecorder getAudioRecorder(AudioSpecification audioSpec, int recordTimeMS, AudioStreamListener listener);
	
	public abstract AudioStreamSaver getAudioStreamSaver();
	
	public AudioComponent getAudioComponent(AudioSpecification audioSpec, IMeasurementListener listener, Calibration calibration) throws Exception
	{
		return new AudioComponent(audioSpec, listener, calibration);
	}
	
	public abstract ICalibrationsParser getCalibrationParser();
	
	public abstract LocationComponent getLocationComponent();

	public abstract ResourceReader getResourceReader(String path);
	
	public FileWriter getUTF8FileWriter(String filePath)
	{
		return getFileWriter(filePath, "UTF-8");
	}
	
	public FileWriter getFileWriter(String filePath)
	{
		return getFileWriter(filePath, null);
	}
	
	public abstract FileWriter getFileWriter(String filePath, String characterEncoding);
	
	public abstract HttpClient getHttpClient(String agent);
	
	/**
	 * @param filePath
	 * @return an inputstream is the file exists, null if it does not (or if an error occurred)
	 */
	public abstract InputStream getFileInputStream(String filePath);
	
	public abstract INTCoordinates getNTCoordinates(double latitude, double longitude, double altitude);
	
	public abstract String additionalErrorReporting(Throwable t);
	
	public abstract void addTrackProcessors(Track track);
	
	public void annotateTrack(Track track)
	{
		track.addMetadata("client", clientType);
		track.addMetadata("clientVersion", clientVersion);
		track.addMetadata("clientBuildDate", clientBuildDate);
		track.addMetadata("deviceBrand", device.getBrand());
		track.addMetadata("deviceModel", device.getModel());
		track.addMetadata("deviceModelVersion", device.getModelVersion());
		track.addMetadata("devicePlatform", device.getPlatform());
		track.addMetadata("devicePlatformVersion", device.getPlatformVersion());
		track.addMetadata("deviceJavaPlatform", device.getJavaPlatform());
		track.addMetadata("deviceJavaPlatformVersion", device.getJavaPlatformVersion());
		if(preferences.getCalibration() != null)
		{
			track.addMetadata("calibration", preferences.getCalibration().toString());
			track.addMetadata("credibility", new String(new char[] {  preferences.getCalibration().getEffeciveCredibilityIndex() }));
		}
		else
			track.addMetadata("credibility", new String(new char[] { Calibration.CREDIBILITY_INDEX_X }));
		//subclasses can add some more:
		additionalTrackAnnotating(track);
	}
	
	protected abstract void additionalTrackAnnotating(Track track);
	
}
