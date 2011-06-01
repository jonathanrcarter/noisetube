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

package net.noisetube.core.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.location.LocationManager;

import net.noisetube.audio.AudioComponent;
import net.noisetube.audio.AudioStreamListener;
import net.noisetube.audio.AudioStreamSaver;
import net.noisetube.audio.AudioRecorder;
import net.noisetube.audio.android.AndroidAudioComponent;
import net.noisetube.audio.android.AndroidAudioStreamSaver;
import net.noisetube.audio.android.AndroidAudioRecorder;
import net.noisetube.audio.calibration.Calibration;
import net.noisetube.audio.calibration.ICalibrationsParser;
import net.noisetube.audio.calibration.android.DOMCalibrationsParser;
import net.noisetube.audio.format.AudioSpecification;
import net.noisetube.config.Device;
import net.noisetube.config.Preferences;
import net.noisetube.config.android.AndroidDevice;
import net.noisetube.config.android.AndroidPreferences;
import net.noisetube.core.NTClient;
import net.noisetube.io.FileWriter;
import net.noisetube.io.HttpClient;
import net.noisetube.io.ResourceReader;
import net.noisetube.io.android.AndroidHttpClient;
import net.noisetube.io.android.AndroidResourceReader;
import net.noisetube.io.android.FileAccess;
import net.noisetube.location.LocationComponent;
import net.noisetube.location.android.AndroidLocationComponent;
import net.noisetube.location.android.AndroidNTCoordinates;
import net.noisetube.model.INTCoordinates;
import net.noisetube.model.IMeasurementListener;
import net.noisetube.model.Track;

/**
 * @author sbarthol, mstevens
 *
 */
public class AndroidNTClient extends NTClient
{
	
	//STATICS--------------------------------------------------------------------------------------
	
	//Client info
	static public String CLIENT_TYPE = "NoiseTubeMobileAndroid";
	static public String CLIENT_VERSION = "v1.2.1";
	static public String CLIENT_BUILD_DATE = BuildInfo.timeStamp;
	static public boolean CLIENT_IS_TEST_VERSION = false; //TODO change to false for web release
	static
	{	//TODO comment both out for web release:
		//NTClient.ENVIRONMENT = NTClient.PHONE_DEV_ENV;
		//NTClient.ENVIRONMENT = NTClient.EMULATOR_ENV;
	}

	//DYNAMICS-------------------------------------------------------------------------------------
	private ContextWrapper contextWrapper;
	
	public AndroidNTClient(ContextWrapper contextWrapper) throws Exception
	{
		super(CLIENT_TYPE, "v" + getAppVersion(contextWrapper), CLIENT_BUILD_DATE, CLIENT_IS_TEST_VERSION);
		this.contextWrapper = contextWrapper;
		initialize(); //!!! DO NOT REMOVE
	}
	
	/**
	 * Gets the software version retrieved from the Manifest.
	 */
	private static String getAppVersion(ContextWrapper contextWrapper)
	{
		try
		{
			PackageInfo packageInfo = contextWrapper.getPackageManager().getPackageInfo(contextWrapper.getPackageName(), 0);
			return packageInfo.versionName;
		}
		catch(Exception e)
		{
			return CLIENT_VERSION;
		}
	}
	
	/**
	 * @return the contextWrapper
	 */
	public ContextWrapper getContextWrapper()
	{
		return contextWrapper;
	}

	@Override
	protected Device createDevice()
	{
		return new AndroidDevice();
	}

	@Override
	protected Preferences createPreferences()
	{
		AndroidPreferences p = new AndroidPreferences((AndroidDevice) device);
		if(ENVIRONMENT != PHONE_PROD_ENV)
		{
			//p.setAccount(new NTAccount("SOME_USERNAME_TO_TEST_WITH", "API_KEY_CORRESPONDING_TO_USERNAME"));
			p.setSavingMode(Preferences.SAVE_HTTP);
			//p.setSavingMode(Preferences.SAVE_FILE);
			p.setAlsoSaveToFileWhenInHTTPMode(true);
			if(ENVIRONMENT == EMULATOR_ENV) //Emulator has no GPS
				p.setUseGPS(false);
			return p;
		}
		else
			return p;
	}
	
	@Override
	public AudioSpecification deserialiseAudioSpecification(String serialisedAudioSpec)
	{
		return AudioSpecification.deserialise(serialisedAudioSpec);
	}

	@Override
	public AudioRecorder getAudioRecorder(AudioSpecification audioSpec, int recordTimeMS, AudioStreamListener listener)
	{
		return new AndroidAudioRecorder(audioSpec, recordTimeMS, listener);
	}

	@Override
	public AudioStreamSaver getAudioStreamSaver()
	{
		return new AndroidAudioStreamSaver(preferences.getDataFolderPath());
	}
	
	@Override
	public AudioComponent getAudioComponent(AudioSpecification audioSpec, IMeasurementListener listener, Calibration calibration) throws Exception
	{
		return new AndroidAudioComponent(audioSpec, listener, calibration);
	}

	@Override
	public FileWriter getFileWriter(String filePath, String characterEncoding)
	{
		if(device.supportsFileAccess())
		{
			return new net.noisetube.io.android.AndroidFileWriter(filePath, characterEncoding);
		}
		else
			return null;
	}

	/**
	 * @param filePath
	 * @return an inputstream is the file exists, null if it does not (or if an error occurred)
	 */
	@Override
	public InputStream getFileInputStream(String filePath)
	{
		//must return FileInputStream instance (open File for reading first)
		File folder = FileAccess.getFolder(FileAccess.getFolderPath(filePath));
		try
		{
			File file = new File(folder, FileAccess.getFileName(filePath));
			if(file.exists())
				return new FileInputStream(file);
			else
				return null;
		}
		catch(Exception e)
		{
			log.error(e, "AndroidNTClient.getFileInputStream() for " + filePath);
			return null;
		}
	}

	@Override
	public LocationComponent getLocationComponent()
	{
		return new AndroidLocationComponent((LocationManager) contextWrapper.getSystemService(Context.LOCATION_SERVICE));
	}

	@Override
	public HttpClient getHttpClient(String agent)
	{
		return new AndroidHttpClient(agent);
	}

	@Override
	public INTCoordinates getNTCoordinates(double latitude, double longitude, double altitude)
	{
		return new AndroidNTCoordinates(latitude, longitude, altitude);
	}

	@Override
	protected void additionalTrackAnnotating(Track track)
	{
		//nothing (for now)
	}

	@Override
	public ResourceReader getResourceReader(String path)
	{
		return new AndroidResourceReader(path);
	}

	@Override
	public ICalibrationsParser getCalibrationParser()
	{
		return new DOMCalibrationsParser();
	}

	@Override
	public String additionalErrorReporting(Throwable t)
	{
		try
		{
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			t.printStackTrace(pw);
			pw.flush();
			sw.flush();
			return sw.toString();
		}
		catch(Exception e)
		{
			//return "exception upon printing stack trace";
			return null; 
		}
	}

	@Override
	public void addTrackProcessors(Track track)
	{
		//Nothing for now
	}

}
