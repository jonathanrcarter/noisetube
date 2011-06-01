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
import net.noisetube.model.ICoordinates;
import net.noisetube.model.IMeasurementListener;
import net.noisetube.model.Track;
import net.noisetube.util.Logger;
import net.noisetube.util.StringUtils;


/**
 * @author mstevens
 *
 */
/**
 * @author mstevens
 *
 */
public abstract class NTClient
{

	//STATIC-----------------------------------------------
	private static NTClient INSTANCE = null;
	
	public static NTClient getInstance()
	{
		if(INSTANCE == null)
			throw new IllegalStateException("NTClient instance is not initialized!");
		return INSTANCE;
	}
	
	protected static Logger log = Logger.getInstance();
	
	//DYNAMIC----------------------------------------------
	protected String clientType;
	protected String clientVersion;
	protected String clientBuildDate;
	protected boolean testVersion;
	protected boolean runningInEmulator;
	
	protected Device device;
	protected Preferences preferences;
	protected Engine engine;
	
	
	protected NTClient(String clientType, String clientVersion, String clientBuildDate, boolean testVersion, boolean runningInEmulator) throws Exception
	{
		if(INSTANCE != null)
			throw new IllegalStateException("Cannot create more than one instance of NTClient (Singleton)");
		INSTANCE = this; //!!!
		this.clientType = clientType;
		this.clientVersion = clientVersion;
		this.clientBuildDate = clientBuildDate;
		this.testVersion = testVersion;
		this.runningInEmulator = runningInEmulator;
		initialize();
	}
	
	/**
	 * Sets the Device and Preferences
	 */
	protected void initialize() throws Exception
	{		
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
			throw new Exception("Unsupported device! Required functionality missing: " + StringUtils.StringVectorToString(missingFeatures, "; "));
		
		//Create preferences:
		preferences = createPreferences();
		
		//Initialize engine:
		engine = new Engine();
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
	 * @return the runningInEmulator
	 */
	public boolean isRunningInEmulator()
	{
		return runningInEmulator;
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
	
	public abstract InputStream getFileInputStream(String filePath);
	
	public abstract ICoordinates getNTCoordinates(double latitude, double longitude, double altitude);
	
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
	
	public void stop()
	{
		if(log!=null)
			log.stop();
		INSTANCE = null;
	}
	
}
