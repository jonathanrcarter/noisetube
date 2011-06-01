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

package net.noisetube.io;

import java.io.InputStream;

import net.noisetube.audio.calibration.CalibrationFactory.CalibrationsList;
import net.noisetube.audio.calibration.Calibration;
import net.noisetube.audio.calibration.ICalibrationsParser;
import net.noisetube.config.NTAccount;
import net.noisetube.core.NTClient;
import net.noisetube.io.saving.HttpSaver;
import net.noisetube.model.Measurement;
import net.noisetube.model.Track;
import net.noisetube.util.ErrorCallback;
import net.noisetube.util.JSONUtils;
import net.noisetube.util.Logger;
import net.noisetube.util.URLUTF8Encoder;

/**
 * @author mstevens, maisonneuve
 *
 */
public class NTWebAPI
{

	private static final boolean DEFAULT_ASYNC = true;
	
	private static final String AGENT = NTClient.getInstance().getClientType() + "/" + NTClient.getInstance().getClientVersion(); //"NoiseTubeClient/1.x.y";
	
	static final String DEFAULT_API_BASE_URL = "http://www.noisetube.net/api/";
	static final String DEV_API_BASE_URL = "http://localhost:3000/api/";
	
	private Logger log = Logger.getInstance();
	
	private String apiBaseURL = DEFAULT_API_BASE_URL;
	private HttpClient httpClient;
	private NTAccount account;
	private boolean async = DEFAULT_ASYNC;
	private ErrorCallback errorCallback;


	public NTWebAPI()
	{
		httpClient = NTClient.getInstance().getHttpClient(AGENT);
		//config api base according to the environment
		/*if(NTClient.getInstance().isRunningInEmulator())
			apiBaseURL = DEV_API_BASE_URL; */
	}
	
	public NTWebAPI(NTAccount account)
	{
		this(); //!!!
		this.account = account;
	}
	
	public boolean ping()
	{
		try
		{
			String response = httpClient.getRequest(apiBaseURL + "ping");
			return(response != null && response.equalsIgnoreCase("ok"));
		}
		catch(Exception e)
		{
			log.debug("Ping failed");
			return false; //ping failed
		}
	}
	
	public void setErrorCallBack(ErrorCallback errorCallBack)
	{
		this.errorCallback = errorCallBack;
	}

	public boolean authenticated()
	{
		return account != null;
	}
	
	public void logout()
	{
		account = null;
	}
	
	/**
	 *  
	 * @param username
	 * @param password
	 * @return an account object if login was successful, null if username/password combination was incorrect
	 * @throws Exception in case of a connection or server problem
	 * 
	 * TODO encrypt username & password
	 */
	public NTAccount login(String username, String password) throws Exception
	{	
		if(authenticated())
			logout(); //discard current account
		String response = httpClient.getRequest(apiBaseURL + "authenticate?login=" + username.toLowerCase() + "&password=" + password); //throws exception in case of connection problem or HttpResponseCode != OK
		if(response.length() == 40)
		{	//API key received: correct login
			account = new NTAccount(username, response);
			return account;
		}
		else if(response.equalsIgnoreCase("error"))
			return null; //incorrect login
		else
			throw new Exception("Login failed, unknown server response: " + response);
	}

	public void startTrack(Track track) throws Exception
	{
		if(authenticated())
		{
			String url = apiBaseURL + "newsession?key="	+ account.getAPIKey() + "&" + track.getMetaDataString("=", "&", true, new URLUTF8Encoder.URLStringEncoder());
			//log.debug("Starting new track (" + url + ")");
			String response;
			try
			{
				response = httpClient.getRequest(url);
				if(!response.substring(0, 2).equalsIgnoreCase("ok"))
					throw new Exception("Server response: " + response);
				track.setTrackID(Integer.parseInt(response.substring(3, response.length()))); //set the trackID!!!
				log.info("New track started (ID in NoiseTube database: " + track.getTrackID() + ")");
			}
			catch(Exception e)
			{
				//log.debug("Could not start track: " + e.getMessage());
				throw e;
			}
		}
		else
			throw new Exception("Not logged in");
	}

	public void endTrack(Track track) throws Exception
	{
		if(authenticated())
		{
			String url = apiBaseURL + "endsession?key=" + account.getAPIKey() + "&track=" + track.getTrackID();
			//log.debug("Ending track (" + url + ")");
			try
			{
				httpClient.getRequest(url);
				log.info("Track " + track.getTrackID() + " ended on server");
			}
			catch(Exception e)
			{
				//log.debug("Could not end track: " + e.getMessage());
				throw e;
			}
		}
		else
			throw new Exception("Not logged in");
	}

	public void sendMeasurement(Track track, Measurement measurement) throws Exception
	{
		if(authenticated())
		{
			String url = apiBaseURL + "update?" + measurement.toUrl() + "&track=" + track.getTrackID() + "&key=" + account.getAPIKey();
			//log.debug("Sending measurement: " + url);
			try
			{
				if(async)
					httpClient.getRequestAsync(url, "send measurement", errorCallback);
				else
					httpClient.getRequest(url);
			}
			catch(Exception e)
			{
				//log.debug("Could not send measurement: " + e.getMessage());
				throw e;
			}
		}
		else
			throw new Exception("Not logged in");
	}

	public void sendBatch(Track track, HttpSaver.JSONCache cache) throws Exception
	{
		if(cache.getSize() > 0)
		{
			if(authenticated())
			{
				if(!track.isTrackIDSet())
				{	//this is a new track
					startTrack(track); //throws SaveException if failed
				}
				//send the cached measures
				log.debug("Sending a batch of " + cache.getSize() + " measurements of track " + track.getTrackID());
				try
				{
					httpClient.postJSONRequest(apiBaseURL + "upload?key=" + account.getAPIKey() + "&track=" + track.getTrackID(), cache.toJSON());
				}
				catch(Exception e)
				{
					//log.debug("Could not send batch: " + e.getMessage());
					throw e;
				}
				cache.clear(); //only if sending was successful
			}
			else
				throw new Exception("Not logged in");
		}
	}
	
	public void postLog(String logMessage) throws Exception
	{
		if(authenticated())
		{
			try
			{
				httpClient.postJSONRequest(apiBaseURL + "postlog?key=" + account.getAPIKey(), "{log:\"" + JSONUtils.escape(logMessage) + "\"}");
			}
			catch(Exception e)
			{
				//log.error(e, "Could not send log message"); //don't this here, could cause endless loop
			}
			log.debug("Log posted");
		}
		else
			throw new Exception("Not logged in");
	}
	
	public CalibrationsList downloadCalibrations(final ICalibrationsParser parser)
	{
		try
		{
			DownloadedCalibrationsReader reader = new DownloadedCalibrationsReader(parser);
			httpClient.getRequest(apiBaseURL + "mobilecalibrations", reader);
			return reader.calibrationsList;
		}
		catch(Exception e)
		{
			log.error(e, "Could not download calibrations XML file from server");
			return null;
		}
	}
	
	/**
	 * @author mstevens
	 */
	private class DownloadedCalibrationsReader implements IInputStreamReader
	{
		
		private ICalibrationsParser parser;
		private CalibrationsList calibrationsList = null;
		
		public DownloadedCalibrationsReader(ICalibrationsParser parser)
		{
			this.parser = parser;
		}
		
		public void read(InputStream inputStream) throws Exception
		{
			calibrationsList = parser.parseList(inputStream, Calibration.SOURCE_DOWNLOADED); //closes the stream
		}
		
	}

}