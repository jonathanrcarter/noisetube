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
import net.noisetube.tagging.HighExposureTagger;
import net.noisetube.tagging.TaggingComponent;
import net.noisetube.tagging.PeakTagger;
import net.noisetube.ui.IMeasurementUI;
import net.noisetube.util.Logger;


/**
 * Core Engine
 * 
 * @author maisonneuve, mstevens, sbarthol
 * 
 */
public class Engine implements IMeasurementListener, IPausableService
{

	protected NTClient ntClient;
	protected Preferences preferences;
	protected Logger log = Logger.getInstance();	

	//Components
	protected AudioComponent audioComponent;
	protected LocationComponent locationComponent;
	private TaggingComponent taggingComponent;

	//Track
	protected Track track;

	//Postprocessors
	private HighExposureTagger longExposureTagger = new HighExposureTagger();
	private PeakTagger peakTagger = new PeakTagger();

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
			if(preferences.isUseDoseMeter())
				doseMeter = new DoseMeter();

			//Localisation
			locationComponent = ntClient.getLocationComponent();

			//Tagging
			taggingComponent = new TaggingComponent();

			//Tag observer
			longExposureTagger.setListener(taggingComponent);
			peakTagger.setListener(taggingComponent);
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

		log.debug("Starting NoiseTube Mobile engine");

		//Track
		this.track = track;
		ntClient.annotateTrack(track);

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
			log.debug("Not saving measurements");

		//Processing
		if(preferences.isUseCoordinateInterpolation())
		{
			track.addProcessor(new CoordinateInterpolator());
			log.debug("Coordinate interpolation enabled");
		}
		//TODO add automatic taggers as postprocessors

		locationComponent.start();
		audioComponent.start(); //!!!
		if(ui!=null)
			ui.measuringStarted(track);
	}

	public void pause()
	{
		if(isRunning())
		{
			audioComponent.pause();
			track.pause(); //also flushes the buffer
			if(saver != null)
				saver.pause();
			if(ui!=null)
				ui.measuringPaused(track);
		}
	}

	public void resume()
	{
		if(isRunning() && isPaused())
		{
			audioComponent.resume();
			track.resume();
			if(saver != null)
				saver.resume();
			if(ui!=null)
				ui.measuringResumed(track);
		}
	}

	public boolean isPaused()
	{
		return audioComponent.isPaused();
	}

	public void stop()
	{
		audioComponent.stop();
		locationComponent.stop();
		if(saver != null)
		{
			track.flushBuffer(); //!!!
			saver.stop(); //don't move this before flushBuffer!
			//Block until saver is done (or all savers in case of a multisaver):
			while(saver.isRunning())
			{
				try
				{
					synchronized(Thread.currentThread())
					{
						Thread.currentThread().wait(500);
					}
				}
				catch(InterruptedException e)
				{
					break;
				}
			}
		}
		if(ui != null)
			ui.measuringStopped(track);
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
			//longExposureTagger.receiveMeasurement(m);
			//peakTagger.receiveMeasurement(m);
			if(preferences.isUseDoseMeter())
				doseMeter.receiveMeasurement(m);
			try
			{
				//get tag
				if(taggingComponent.hasTags())
				{
					m.addTags(taggingComponent.getTags());
					//log.debug("Noise measurement tagged: " + taggingComponent.getTags());
				}
				taggingComponent.clear();

				//get & set location
				if(locationComponent.isRunning())
					m.setLocation(locationComponent.getLastLocation());

				//log.debug("Measurement: " + m.toString());

				track.addMeasurement(m); //handles postprocessing & saving
				if(ui != null)
					ui.newMeasurement(track, m);
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
	}

	public boolean isRunning()
	{
		return audioComponent.isRunning();
	}

	public LocationComponent getLocationComponent()
	{
		return locationComponent;
	}

	public TaggingComponent getTaggingComponent()
	{
		return taggingComponent;
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
