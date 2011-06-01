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

package net.noisetube.audio.calibration;

import net.noisetube.audio.AudioComponent;
import net.noisetube.audio.format.AudioSpecification;
import net.noisetube.core.IService;
import net.noisetube.model.IMeasurementListener;
import net.noisetube.model.Measurement;
import net.noisetube.util.Logger;

/**
 * @author mstevens
 *
 */
public class Calibrator implements IService, IMeasurementListener
{

	private static Logger log = Logger.getInstance();

	private boolean running = false;
	private IMeasurementListener listener;
	
	//Audio-related:
	private long startTimeMS;
	private long previouslyElapsedMS;
	private int measurementCount;
	private AudioSpecification audioSpec = null;
	private AudioComponent loudnessComp;

	public Calibrator(AudioSpecification audioSpec, int measurementCount, long previouslyElapsedMS) throws Exception
	{
		this.audioSpec = audioSpec;
		this.measurementCount = measurementCount;
		this.previouslyElapsedMS = previouslyElapsedMS;
		loudnessComp = new AudioComponent(audioSpec, this);
		loudnessComp.setLoudnessMode(AudioComponent.LOUDNESS_DB_AND_DBA);
	}
	
	public Calibrator(AudioSpecification audioSpec, IMeasurementListener listener) throws Exception
	{
		this(audioSpec, 0, 0);
		this.listener = listener;
	}
	
	public void restart()
	{
		try
		{
			running = true;
			startTimeMS = System.currentTimeMillis();
			loudnessComp.start();
		}
		catch(Exception e)
		{
			log.error("Could not resume calibration: " + e.getMessage());
		}
	}
	
	public void start()
	{
		try
		{
			running = true;
			measurementCount = 0;
			startTimeMS = System.currentTimeMillis();
			loudnessComp.start();
		}
		catch(Exception e)
		{
			log.error("Could not start calibration: " + e.getMessage());
		}
	}
	
	public void stop()
	{
		running = false;
		loudnessComp.stop();
	}

	public boolean isRunning()
	{
		return running && loudnessComp.isRunning();
	}
	
	public void receiveMeasurement(Measurement m)
	{
		if(running)
		{
			measurementCount++;
			long elapsedTimeMS = previouslyElapsedMS + (m.getTimeStamp().getTime() - startTimeMS);
			long seconds = (elapsedTimeMS / 1000) % 60;
			long minutes = (elapsedTimeMS / (60 * 1000)) % 60;
			long hours = (minutes / 60) % 60;
			String timeString = (hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
			log.debug(timeString + ";" + measurementCount + ";" + Double.toString(m.getLoudnessLeqDB()) + ";" + Double.toString(m.getLoudnessLeqDBA()));
			listener.receiveMeasurement(m);
		}
	}

	/**
	 * @return the audioSpec
	 */
	public AudioSpecification getAudioSpec()
	{
		return audioSpec;
	}

	public long getElapsedTimeMS()
	{
		return previouslyElapsedMS + (System.currentTimeMillis() - startTimeMS);
	}

	/**
	 * @return the measurementCount
	 */
	public int getMeasurementCount()
	{
		return measurementCount;
	}

	/**
	 * @param listener the listener to set
	 */
	public void setListener(IMeasurementListener listener)
	{
		this.listener = listener;
	}

}
