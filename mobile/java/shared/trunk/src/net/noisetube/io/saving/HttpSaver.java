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

package net.noisetube.io.saving;

import java.util.Enumeration;
import java.util.Vector;

import net.noisetube.config.Preferences;
import net.noisetube.core.NTClient;
import net.noisetube.io.NTWebAPI;
import net.noisetube.model.Measurement;
import net.noisetube.model.Track;
import net.noisetube.util.BlockingQueue;
import net.noisetube.util.ErrorCallback;

/**
 * HTTP based track and measurement saving class
 * Uses the NoiseTube Web API
 * 
 * @author maisonneuve, mstevens
 */
public class HttpSaver extends Saver implements Runnable, ErrorCallback
{

	private BlockingQueue dataSendingQueue;
	private NTWebAPI api;
	private volatile boolean batchMode;
	private volatile boolean recoveryMode;
	private JSONCache cache; //for HTTP Connection recovery mode

	public HttpSaver(Track track)
	{
		super(track);
		dataSendingQueue = new BlockingQueue();
		if(preferences.isLoggedIn())
			api = new NTWebAPI(preferences.getAccount());
		else
			throw new IllegalStateException("Not logged in!");
		api.setErrorCallBack(this);
	}
	
	public void save(Measurement measurement)
	{
		if(running && !paused && measurement != null)
			dataSendingQueue.enqueue(measurement);
	}
	
	protected void enableBatchMode()
	{
		batchMode = true;
		log.debug("HttpSaver: Cache activated (batch mode)");
	}
	
	public void start()
	{
		if(!running)
		{
			if(preferences.isAlwaysUseBatchModeForHTTP())
				enableBatchMode();
			cache = new JSONCache(); //disabled at first
			running = true;
			new Thread(this).start();
			log.debug("HTTPSaver started");
			if(preferences.getSavingMode() == Preferences.SAVE_HTTP)
				setStatus("Saving to NoiseTube.net (user: " + preferences.getAccount().getUsername() + ")");
		}
	}
	
	public void pause()
	{
		if(running)
		{
			paused = true;
			//setStatus(getStatus() + " [paused]");
		}
	}

	public void resume()
	{
		if(paused)
		{
			paused = false; //resume from pause
			//setStatus(getStatus().substring(0, getStatus().length() - " [paused]".length()));
		}
	}
	
	public void stop()
	{	//do not set running = false here!
		if(running)
			dataSendingQueue.enqueue(new Object()); //"stopper" object
	}

	public void error(Exception exception, String errorInfo)
	{
		enableRecoveryMode(exception, errorInfo);
	}

	private synchronized void enableRecoveryMode(Exception reason, String infoMessage)
	{
		if(!recoveryMode)
		{
			recoveryMode = true; //turn on the connection recovery mode
			log.error(reason, "HttpSaver: " + infoMessage);
			log.debug("HttpSaver: Cache activated (network connection may have been lost)");
		}
		else
		{
			log.debug("HttpSaver: " + infoMessage); //only log the message (not full stack trace) if we are already in recovery mode
		}
	}
	
	public void run()
	{
		try
		{
			if(!track.isTrackIDSet())
				api.startTrack(track); //send signal for a new track
		}
		catch(Exception se)
		{
			enableRecoveryMode(se, "Error upon starting track");
		}
		while(running)
		{
			Measurement measurement = null;
			try
			{
				Object o = dataSendingQueue.dequeue();
				if(!(o instanceof Measurement))
					break; //got "stopper" object: break out the while loop
				measurement = (Measurement) o;
				if(!recoveryMode && !batchMode)
					api.sendMeasurement(track, measurement); //send single measurement directly
				else
				{
					cache.addMeasurement(measurement); //cache the measurement as JSON
					measurement = null; //so it is not re-added in the catch-block below
					if(cache.getSize() % 30 == 0) //try to send cache contents in one batch every 30 measurements
					{
						if(recoveryMode && !batchMode)
							log.debug("HttpSaver: Connection recovery attempt: trying to send data");
						api.sendBatch(track, cache); //clears the cache if successful, throws an exception otherwise
						if(recoveryMode) //connection recovered
						{
							recoveryMode = false;
							log.debug("HttpSaver: Connection recovered, cache deactivated");
						}
					}
				}
			}
			catch(Exception e)
			{
				enableRecoveryMode(e, "Error upon sending measurement(s)");
				
				//cache the measurement to send later
				if(measurement != null)
					cache.addMeasurement(measurement);
			}
		}
		//stopping...
		try
		{
			//send remaining cached measurements:
			if(batchMode || recoveryMode)
				api.sendBatch(track, cache);
		}
		catch(Exception e)
		{
			log.error(e, "HttpSaver: Unable to save last " + cache.getSize() + " cached measurements");
		}
		try
		{
			if(NTClient.getInstance().isLastRun())
				api.endTrack(track);
		}
		catch(Exception e)
		{
			log.error(e, "HttpSaver: Error upon ending track");
		}
		running = false; //!!!
		log.debug("HTTPSaver stopped (Track ID " + track.getTrackID() + ")");
		if(preferences.getSavingMode() == Preferences.SAVE_HTTP)
			setStatus("Stopped");
	}
	
	public class JSONCache
	{

		static final int INITIAL_CAPACITY = Track.DEFAULT_BUFFER_CAPACITY;
		static final int MAXIMUM_SIZE = 3600;
		
		private Vector measurementsAsJSON = new Vector(INITIAL_CAPACITY);
		
		public void clear()
		{
			measurementsAsJSON.removeAllElements();
		}

		public int getSize()
		{
			return measurementsAsJSON.size();
		}

		/**
		 * add a measurement
		 * 
		 * @param measurement
		 */
		public void addMeasurement(Measurement measurement)
		{
			if(measurementsAsJSON.size() >= MAXIMUM_SIZE)
			{
				log.error("cache is full, deleting oldest entry");
				measurementsAsJSON.removeElementAt(0);
			}
			measurementsAsJSON.addElement(measurement.toJSON());
		}

		public String toJSON()
		{
			Enumeration e = measurementsAsJSON.elements();
			StringBuffer bff = new StringBuffer("{measures:[");
			while(e.hasMoreElements())
			{
				bff.append((String) e.nextElement());
				if(e.hasMoreElements())
					bff.append(",");
			}
			bff.append("]}");
			return bff.toString();
		}
		
	}

}