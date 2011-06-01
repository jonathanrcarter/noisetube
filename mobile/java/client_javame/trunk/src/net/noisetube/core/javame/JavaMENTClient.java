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
import net.noisetube.audio.javame.SEAudioStreamCorrector;
import net.noisetube.config.Device;
import net.noisetube.config.Preferences;
import net.noisetube.config.javame.JavaMEDevice;
import net.noisetube.config.javame.JavaMEPreferences;
import net.noisetube.core.NTClient;
import net.noisetube.io.FileWriter;
import net.noisetube.io.HttpClient;
import net.noisetube.io.ResourceReader;
import net.noisetube.io.javame.JavaMEFileWriter;
import net.noisetube.io.javame.JavaMEHttpClient;
import net.noisetube.io.javame.JavaMEResourceReader;
import net.noisetube.location.LocationComponent;
import net.noisetube.location.javame.JavaMELocationComponent;
import net.noisetube.location.javame.JavaMENTCoordinates;
import net.noisetube.model.INTCoordinates;
import net.noisetube.model.Track;
import net.noisetube.tagging.HighExposureTagger;
import net.noisetube.tagging.PeakTagger;

/**
 * @author mstevens
 *
 */
public class JavaMENTClient extends NTClient
{
	
	//STATICS--------------------------------------------------------------------------------------
	static public boolean RESEARCH_BUILD = false; //TODO make sure this is false for web release
	static public boolean BRUSSENSE = RESEARCH_BUILD && false; //TODO make sure this is false for web release
	static
	{
		//NTClient.ENVIRONMENT = NTClient.EMULATOR_ENV; //TODO comment out for web release
	}
	
	//Client info
	static public String CLIENT_TYPE = "NoiseTubeMobileJavaME";
	static public String CLIENT_VERSION = "v2.1.0"; //no need to change this version string here in the code, current value will be loaded from JAR/JAD in checkClient()
	static public boolean CLIENT_IS_TEST_VERSION = false; //TODO change for web release

	private static String setVersion(MainMIDlet midlet)
	{
		String version = midlet.getAppProperty("MIDlet-Version"); //gets value stored in JAD/JAR
		String versionPostfix = midlet.getAppProperty("NoiseTube-VersionPostfix");
		if(version != null)
			CLIENT_VERSION = 	"v" + version.trim()
								+ ((versionPostfix != null && !versionPostfix.equals("")) ? ("_" + versionPostfix) : "");
		CLIENT_IS_TEST_VERSION = (versionPostfix != null && (versionPostfix.equalsIgnoreCase("beta") || versionPostfix.equalsIgnoreCase("alpha"))) || CLIENT_IS_TEST_VERSION;
		return CLIENT_VERSION;
	}
	
	//DYNAMICS-------------------------------------------------------------------------------------
	
	public JavaMENTClient(MainMIDlet midlet) throws Exception
	{
		super(CLIENT_TYPE, setVersion(midlet), BuildInfo.timeStamp, CLIENT_IS_TEST_VERSION);
		initialize(); //!!! DO NOT REMOVE
	}

	protected Device createDevice()
	{
		if(RESEARCH_BUILD)
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
		//if(BRUSSENSE)
		//	return BrusSense.getPreferences((JavaMEDevice) device);
		JavaMEPreferences p = new JavaMEPreferences((JavaMEDevice) device);
		if(RESEARCH_BUILD)
		{
			p.setUseGPS(true);
			p.setForceGPS(true);
			p.setUseCoordinateInterpolation(true);
			p.setAlsoSaveToFileWhenInHTTPMode(true);
			p.setUseLightGUI(true);
		}
		if(ENVIRONMENT != PHONE_PROD_ENV)
		{
			//p.setAccount(new NTAccount("SOME_USERNAME_TO_TEST_WITH", "API_KEY_CORRESPONDING_TO_USERNAME"));
			p.setSavingMode(Preferences.SAVE_HTTP);
			//p.setSavingMode(Preferences.SAVE_FILE);
			p.setAlsoSaveToFileWhenInHTTPMode(true);
			p.setUseGPS(true);
		}
		return p;
	}
	
	public INTCoordinates getNTCoordinates(double latitude, double longitude, double altitude)
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
		AudioRecorder ar = new JavaMEAudioRecorder((JavaMEAudioSpecification) audioSpec, recordTimeMS, listener);
		if(((JavaMEDevice) device).getBrandID() == JavaMEDevice.BRAND_SONYERICSSON)
			ar.setCorrector(new SEAudioStreamCorrector()); //to fix wrong sample rate on some SE phones
		return ar;
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
	
	/**
	 * @param filePath
	 * @return an inputstream is the file exists, null if it does not (or if an error occurred)
	 */
	public InputStream getFileInputStream(String fullPath)
	{
		if(!device.supportsFileAccess())
			return null;
		try
		{
			FileConnection fc = (FileConnection) Connector.open(fullPath, Connector.READ);
			if(fc.exists())
				return fc.openInputStream();
			else
			{
				fc.close();
				return null;
			}
		}
		catch(Exception e)
		{
			log.error(e, "Could not open InputStream from file " + fullPath);
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
		if(RESEARCH_BUILD)
			track.addMetadata("special_client", "research_build");
		if(BRUSSENSE)
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

	public String additionalErrorReporting(Throwable t)
	{
		return null; //Unfortunately CLDC does not support printing stack traces to anything else than System.err
	}

	public void addTrackProcessors(Track track)
	{
		track.addProcessor(new HighExposureTagger());
		track.addProcessor(new PeakTagger());
		if(MainMIDlet.getInstance().getTaggingComponent() != null)
			track.addProcessor(MainMIDlet.getInstance().getTaggingComponent());
	}

}
