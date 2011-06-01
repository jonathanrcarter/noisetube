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

package net.noisetube.core.javame;

import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.noisetube.audio.AudioStreamListener;
import net.noisetube.audio.AudioStreamSaver;
import net.noisetube.audio.AudioRecorder;
import net.noisetube.audio.calibration.ICalibrationsParser;
import net.noisetube.audio.calibration.javame.SAXCalibrationsParser;
import net.noisetube.audio.format.AudioSpecification;
import net.noisetube.audio.javame.JavaMEAudioSpecification;
import net.noisetube.audio.javame.JavaMEAudioStreamSaver;
import net.noisetube.audio.javame.JavaMEAudioRecorder;
import net.noisetube.config.Device;
import net.noisetube.config.NTAccount;
import net.noisetube.config.Preferences;
import net.noisetube.config.javame.JavaMEDevice;
import net.noisetube.config.javame.JavaMEPreferences;
import net.noisetube.io.FileWriter;
import net.noisetube.io.HttpClient;
import net.noisetube.io.ResourceReader;
import net.noisetube.io.javame.JavaMEFileWriter;
import net.noisetube.io.javame.JavaMEHttpClient;
import net.noisetube.io.javame.JavaMEResourceReader;
import net.noisetube.location.LocationComponent;
import net.noisetube.location.javame.JavaMELocationComponent;
import net.noisetube.location.javame.JavaMENTCoordinates;
import net.noisetube.model.ICoordinates;
import net.noisetube.model.Track;

/**
 * @author mstevens
 *
 */
public class JavaMENTClient extends NTClient
{

	public JavaMENTClient(String clientType, String clientVersion, boolean testVersion, boolean runningInEmulator) throws Exception
	{
		super(clientType, clientVersion, BuildInfo.timeStamp, testVersion, runningInEmulator);
	}

	protected Device createDevice()
	{
		if(MainMIDlet.RESEARCH_BUILD)
		{
			return new JavaMEDevice()
			{
				public boolean supportsInternetAccess()
				{
					return brandID != BRAND_NOKIA && super.supportsInternetAccess(); //research builds on Nokia's use automatic restarting which makes all network access disallowed
				}
			};
		}
		else
			return new JavaMEDevice();
	}

	protected Preferences createPreferences()
	{
		//if(MainMIDlet.BRUSSENSE)
		//	return BrusSense.getPreferences((JavaMEDevice) device);
		JavaMEPreferences p = new JavaMEPreferences((JavaMEDevice) device);
		if(MainMIDlet.RESEARCH_BUILD)
		{
			p.setUseGPS(true);
			p.setForceGPS(true);
			p.setUseCoordinateInterpolation(true);
			p.setAlsoSaveToFileWhenInHTTPMode(true);
			p.setUseLightGUI(true);
		}
		if(runningInEmulator || MainMIDlet.ENVIRONMENT == MainMIDlet.PHONE_DEV_ENV)
		{
			//p.setAccount(new NTAccount("SOME_USERNAME_TO_TEST_WITH", "API_KEY_CORRESPONDING_TO_USERNAME"));
			p.setSavingMode(Preferences.SAVE_HTTP);
			//p.setSavingMode(Preferences.SAVE_FILE);
			p.setAlsoSaveToFileWhenInHTTPMode(true);
			p.setUseGPS(true);
		}
		return p;
	}

	public ICoordinates getNTCoordinates(double latitude, double longitude, double altitude)
	{
		return new JavaMENTCoordinates(latitude, longitude, altitude);
	}

	public HttpClient getHttpClient(String agent)
	{
		return new JavaMEHttpClient(agent);
	}

	public LocationComponent getLocationComponent()
	{
		return new JavaMELocationComponent();
	}

	public AudioSpecification deserialiseAudioSpecification(String serialisedAudioSpec)
	{
		return JavaMEAudioSpecification.deserialise(serialisedAudioSpec);
	}

	public AudioRecorder getAudioRecorder(AudioSpecification audioSpec, int recordTimeMS, AudioStreamListener listener)
	{
		return new JavaMEAudioRecorder((JavaMEAudioSpecification) audioSpec, recordTimeMS, listener);
	}

	public AudioStreamSaver getAudioStreamSaver()
	{
		return new JavaMEAudioStreamSaver(preferences.getDataFolderPath());
	}

	public FileWriter getFileWriter(String filePath, String characterEncoding)
	{
		if(!device.supportsFileAccess())
			return null;
		return new JavaMEFileWriter(filePath, characterEncoding);
	}

	public InputStream getFileInputStream(String fullPath)
	{
		if(!device.supportsFileAccess())
			return null;
		try
		{
			return ((FileConnection) Connector.open(fullPath, Connector.READ)).openInputStream();
		}
		catch(Exception e)
		{
			log.error("Could not open InputStream from file " + fullPath + ": " + e.getMessage());
			return null;
		}
	}

	public boolean isRestartingModeEnabled()
	{
		return MainMIDlet.RESTARTING_MODE != MainMIDlet.RESTARTING_MODE_DISABLED;
	}

	public boolean isFirstRun()
	{
		return MainMIDlet.FIRST_RUN;
	}

	public boolean isLastRun()
	{
		return MainMIDlet.LAST_RUN;
	}

	protected void additionalTrackAnnotating(Track track)
	{
		if(MainMIDlet.RESEARCH_BUILD)
			track.addMetadata("special_client", "research_build");
		if(MainMIDlet.BRUSSENSE)
			track.addMetadata("experiment", "ademloos");
	}

	public ResourceReader getResourceReader(String path)
	{
		return new JavaMEResourceReader(path);
	}

	public ICalibrationsParser getCalibrationParser()
	{
		return new SAXCalibrationsParser();
	}

}
