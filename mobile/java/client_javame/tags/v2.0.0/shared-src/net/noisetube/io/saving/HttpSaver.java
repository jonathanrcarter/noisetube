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
import net.noisetube.util.Logger;

/**
 * HTTP based track and measurement saving class
 * Uses the NoiseTube Web API
 * 
 * @author maisonneuve, mstevens
 */
public class HttpSaver extends Saver implements Runnable, ErrorCallback
{

	protected static Logger log = Logger.getInstance();
	
	BlockingQueue dataSendingQueue;

	//HTTP Connection recovery mode
	private JSONCache cache;

	private NTWebAPI api;

	protected Preferences preferences;

	public HttpSaver(Track track)
	{
		super(track);
		dataSendingQueue = new BlockingQueue();
		preferences = NTClient.getInstance().getPreferences();
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
		cache.enable(true); //!!! so we don't flood the connection
	}
	
	public void start()
	{
		if(!running)
		{
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
			setStatus(getStatus() + " [paused]");
		}
	}

	public void resume()
	{
		if(paused)
		{
			paused = false; //resume from pause
			setStatus(getStatus().substring(0, getStatus().length() - " [paused]".length()));
			cache.disable(true); //disable cache (even when forced)
		}
	}
	
	public void stop()
	{	//do not set running = false here!
		if(running)
			dataSendingQueue.enqueue(new Object()); //"stopper" object
	}

	public void error(Exception sendingException)
	{
		cache.enable(); //turn on the connection recovery mode
	}

	public void run()
	{
		Measurement measurementment = null;
		try
		{
			if(!track.isTrackIDSet())
				api.startTrack(track); //send signal for a new track
		}
		catch(SaveException se)
		{
			cache.enable(); //turn on the connection recovery mode
		}
		while(running)
		{
			try
			{
				Object o = dataSendingQueue.dequeue();
				//FYI: this cast is needed to stop!
				if(!(o instanceof Measurement))
					break; //got "stopper" object: break out the while loop
				measurementment = (Measurement) o;
				if(!cache.enabled)
					api.sendMeasurement(track, measurementment); //send over http directly (using NoiseTube API)
				else
				{
					cache.addMeasurement(measurementment); //cache the measurement as JSON
					if(cache.getSize() % 15 == 0) //try to send cache contents one batch every 10 measurements
					{
						log.debug("Batch mode: trying to send data");
						api.sendBatch(track, cache); //clears the cache if successful, throws an exception otherwise
						cache.disable(); //turn off recovery mode (only if sending was successful, otherwise api.sendBatch throws an exception and this line is skipped)
					}
				}
			}
			catch(SaveException e)
			{
				log.error(e, "sending measurementment through http");

				//turn on the connection recovery mode
				cache.enable(); //does nothing if already enabled
				
				//cache the information to send
				cache.addMeasurement(measurementment);				
			}
		}
		//stopping...
		try
		{
			//send remaining checked measurements:
			if(cache.enabled)
				api.sendBatch(track, cache);
		}
		catch(Exception e)
		{
			log.error("Unable to save last " + cache.getSize() + " cached measurements");
		}
		try
		{
			if(NTClient.getInstance().isLastRun())
				api.endTrack(track);
		}
		catch(SaveException e)
		{
			log.error(e, "error sending end session");
		}
		running = false; //!!!
		log.debug("HTTPSaver stopped");
		if(preferences.getSavingMode() == Preferences.SAVE_HTTP)
			setStatus("Stopped");
	}
	
	public class JSONCache
	{

		static final int INITIAL_CAPACITY = 60;
		static final int MAXIMUM_SIZE = 3600;
		
		private Vector measurementsAsJSON = new Vector(INITIAL_CAPACITY);
		private boolean enabled = false;
		private boolean forced = false;
		
		public void clear()
		{
			measurementsAsJSON.removeAllElements();
		}

		public int getSize()
		{
			return measurementsAsJSON.size();
		}
		
		public void enable()
		{
			enable(false);
		}
		
		public void enable(boolean neverDisable)
		{
			boolean oldState = enabled;
			enabled = true;
			forced = neverDisable;
			if(!oldState || neverDisable)
			{	//cache was not enabled before
				log.debug("Cache activated (" + (neverDisable ? "forced" : "network connection may have been lost") + ")");
				setStatus("Saving to NoiseTube.net (user: " + preferences.getAccount().getUsername() + "; sending delayed)");
			}
		}
		
		public void disable()
		{
			disable(false);
		}
		
		public void disable(boolean undoForced)
		{
			if(!forced || undoForced) //never disable if forced = true
			{
				boolean oldState = enabled;
				enabled = false;
				forced = false;
				if(oldState)
				{	//cache was enabled before
					log.debug("Cache deactivated (sending real-time updates)");
					setStatus("Saving to NoiseTube.net (user: " + preferences.getAccount().getUsername() + ")");
					//clear(); //not needed, is called after successful submission in NTWebAPI
				}
			}
		}

		/**
		 * @return the enabled
		 */
		public boolean isEnabled()
		{
			return enabled;
		}

		/**
		 * add a measurement
		 * 
		 * @param measurement
		 */
		public boolean addMeasurement(Measurement measurement)
		{
			if(!enabled)
				throw new IllegalStateException("Cache is not enabled");
			if(isFull())
			{
				log.error("cache full");
				return false;
			}
			else
			{
				measurementsAsJSON.addElement(measurement.toJSON());
				return true;
			}
		}

		/**
		 * 
		 * @return
		 */
		public boolean isFull()
		{
			return measurementsAsJSON.size() > MAXIMUM_SIZE;
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