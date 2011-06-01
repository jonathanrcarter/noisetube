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

import net.noisetube.audio.DoseMeter;
import net.noisetube.audio.AudioComponent;
import net.noisetube.config.Preferences;
import net.noisetube.io.saving.FileSaver;
import net.noisetube.io.saving.HttpSaver;
import net.noisetube.io.saving.MultiSaver;
import net.noisetube.io.saving.Saver;
import net.noisetube.location.CoordinateInterpolator;
import net.noisetube.location.LocationComponent;
import net.noisetube.model.IMeasurementListener;
import net.noisetube.model.Measurement;
import net.noisetube.model.Track;
import net.noisetube.ui.IMeasurementUI;
import net.noisetube.util.Logger;
import net.noisetube.util.MathME;
import net.noisetube.util.StringUtils;


/**
 * Core Engine
 * 
 * @author maisonneuve, mstevens, sbarthol
 * 
 */
public class Engine implements IMeasurementListener, IPausableService
{

	public static int WAIT_FOR_SAVING_TO_COMPLETE_MS = 30000; //30 seconds
	
	protected NTClient ntClient;
	protected Preferences preferences;
	protected Logger log = Logger.getInstance();	

	//Components
	protected AudioComponent audioComponent;
	protected LocationComponent locationComponent;
	
	//Track
	protected Track track;

	//Saver
	protected Saver saver;

	//UI
	protected IMeasurementUI ui;

	//Misc
	private DoseMeter doseMeter;

	public Engine()
	{
		ntClient = NTClient.getInstance();
		preferences = ntClient.getPreferences();
		try
		{
			//Audio
			audioComponent = ntClient.getAudioComponent(ntClient.getDevice().getAudioSpecification(), this, preferences.getCalibration()); 

			//Localisation
			locationComponent = ntClient.getLocationComponent();
		}
		catch(Exception e)
		{
			log.error(e, "Engine initialization");
		}
	}

	public void start()
	{
		restart(new Track());
	}

	public void restart(Track track)
	{
		if(audioComponent.isRunning())
		{
			//stop();
			log.error("Engine already running");
			return;
		}

		log.info("Starting NoiseTube Mobile engine");
		
		//Track
		this.track = track;
		ntClient.annotateTrack(track);

		//UI
		if(ui != null)
			track.setUI(ui);		
		
		//Saving
		switch(preferences.getSavingMode())
		{
			//case Preferences.SAVE_NO : break;
			case Preferences.SAVE_HTTP:
				if(preferences.isAlsoSaveToFileWhenInHTTPMode())
				{
					saver = new MultiSaver(track);
					((MultiSaver) saver).addSaver(new HttpSaver(track));
					((MultiSaver) saver).addSaver(new FileSaver(track));
				}
				else
					saver = new HttpSaver(track);
				break;
			case Preferences.SAVE_FILE:
				saver = new FileSaver(track);
				break;
			case Preferences.SAVE_SMS: /* ... */
				break;
				//...
		}
		if(preferences.getSavingMode() != Preferences.SAVE_NO)
		{
			try
			{
				saver.start();
				track.setSaver(saver); //!!!
			}
			catch(Exception e)
			{
				log.error(e, "starting saver failed; not saving measurements");
				saver = null;
			}
		}
		else
			log.info("Not saving measurements");

		//Processing
		if(preferences.isUseCoordinateInterpolation())
			track.addProcessor(new CoordinateInterpolator());
		if(preferences.isUseDoseMeter())
		{
			doseMeter = new DoseMeter();
			track.addProcessor(doseMeter);
		}
		//track.addProcessor(new HighExposureTagger());
		//track.addProcessor(new PeakTagger());
		ntClient.addTrackProcessors(track); //clients can add additional processors
		
		locationComponent.start();
		audioComponent.start(); //!!!
		if(ui != null)
			ui.measuringStarted(track);
	}
	
	public void pause(boolean preStop)
	{
		if(isRunning() && !isPaused())
		{
			audioComponent.pause();
			if(!preStop)
			{
				track.pause();
				if(saver != null)
					saver.pause();
				if(ui != null)
					ui.measuringPaused(track);
				log.info("Measuring paused");
			}
		}
	}

	public void pause()
	{
		pause(false);
	}

	public void resume()
	{
		if(isRunning() && isPaused())
		{
			audioComponent.resume();
			track.resume();
			if(saver != null)
				saver.resume();
			if(ui != null)
				ui.measuringResumed(track);
			log.info("Measuring resumed");
		}
	}

	public boolean isPaused()
	{
		return audioComponent.isPaused();
	}

	public void stop()
	{
		stop(false);
	}
	
	public void stop(boolean silent)
	{
		if(!audioComponent.isRunning())
			return;
		log.info("Stopping NoiseTube Mobile engine");
		audioComponent.stop();
		locationComponent.stop();
		if(saver != null)
		{
			track.flushBuffer(); //!!!
			saver.stop(); //don't move this before flushBuffer!
			//Block up to WAIT_FOR_SAVING_TO_COMPLETE_MS ms until saver is done (or all savers in case of a multisaver):
			log.debug("Waiting for saving to complete");
			int waited = 0;
			while(saver.isRunning() && waited < WAIT_FOR_SAVING_TO_COMPLETE_MS)
			{
				try
				{
					Thread.sleep(500);
					waited += 500;
				}
				catch(InterruptedException e)
				{
					break;
				}
			}
			if(saver.isRunning())
				log.error("Saver still running after waiting " + MathME.round(Engine.WAIT_FOR_SAVING_TO_COMPLETE_MS / 1000f) + "s");
		}
		if(!silent && ui != null)
			ui.measuringStopped(track);
		log.info("Track" + (track.isTrackIDSet() ? " " + track.getTrackID() : "") + " summary: ");
		log.info(" - duration: " + StringUtils.formatTimeSpanColons(track.getTotalElapsedTime()));
		log.info(" - # measurements: " + track.getStatistics().getNumMeasurements());
	}

	/**
	 * called from LoudnessComponent
	 * 
	 * @param a measurement
	 */
	public void receiveMeasurement(Measurement m)
	{
		if(audioComponent.isRunning())
		{
			try
			{
				//get & set location
				if(locationComponent.isRunning())
					m.setLocation(locationComponent.getLastLocation());
				
				//log.debug("Measurement: " + m.toString());
				
				track.addMeasurement(m); //handles postprocessing & saving and passes newMeasurement to the UI
			}
			catch(Exception e)
			{
				log.error(e, "Engine.receiveMeasurement()");
			}
		}
	}

	/**
	 * @return the track
	 */
	public Track getTrack()
	{
		return track;
	}

	public void setUI(IMeasurementUI ui)
	{
		this.ui = ui;
		if(track != null)
			track.setUI(ui);
	}
	
	public boolean isRunning()
	{
		return audioComponent.isRunning();
	}

	public LocationComponent getLocationComponent()
	{
		return locationComponent;
	}

	/**
	 * @return the audioComponent
	 */
	public AudioComponent getAudioComponent()
	{
		return audioComponent;
	}

	/**
	 * @return the saver
	 */
	public Saver getSaver()
	{
		return saver;
	}

	/**
	 * @return the doseMeter
	 */
	public DoseMeter getDoseMeter()
	{
		return doseMeter;
	}

}
