/** 
 * -------------------------------------------------------------------------------
 * NoiseTube - Mobile client (J2ME version)
 * Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 * 
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License, version 2.1,
 * as published by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * --------------------------------------------------------------------------------
 * 
 * Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 * NoiseTube project source code repository: http://code.google.com/p/noisetube
 * NoiseTube project website: http://www.noisetube.net
 */

package noisetube.core;

import noisetube.MainMidlet;
import noisetube.audio.ILeqListener;
import noisetube.audio.ILoudnessComponent;
import noisetube.audio.LoudnessComponentFactory;
import noisetube.audio.DoseMeter;
import noisetube.config.Preferences;
import noisetube.io.FileSaver;
import noisetube.io.HTTPSaver;
import noisetube.io.MultiSaver;
import noisetube.io.Saver;
import noisetube.io.SavingBuffer;
import noisetube.location.LocationComponent;
import noisetube.model.Measure;
import noisetube.tagging.HighExposureTagger;
import noisetube.tagging.NotesComponent;
import noisetube.tagging.PeakTagger;
import noisetube.ui.MeasureForm;
import noisetube.util.IService;
import noisetube.util.Logger;

/**
 * Core Engine 
 * 
 * TODO use proper observer pattern
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class Engine implements ILeqListener, IService
{
	private MainMidlet midlet;
	private Preferences preferences;
	private MeasureForm measureForm;
	private Logger log = Logger.getInstance();

	// LOUDNESS
	private ILoudnessComponent loudnessComponent;
	private DoseMeter doseMeter;

	// Saver
	private Saver saver;

	// Localization
	private LocationComponent locationComponent;
	private SavingBuffer interpolationBuffer;

	// Tagging/notes
	private HighExposureTagger longExposureTagger = new HighExposureTagger();
	private PeakTagger peakTagger = new PeakTagger();
	private NotesComponent notes;

	private long start_time;
	private long num_measures;

	private IMeasureListener mlistener;

	public Engine()
	{
		midlet = MainMidlet.getInstance();
		preferences = midlet.getPreferences();
		init(); // !!!
	}

	private void init()
	{
		try
		{
			// Loudness
			loudnessComponent = LoudnessComponentFactory.getLoudnessComponent();
			loudnessComponent.setLeqListener(this);
			if(preferences.isUseDoseMeter())
				doseMeter = new DoseMeter();

			//Localisation
			locationComponent = LocationComponent.getLocationComponent(this);
			//					this will give either GPS-supporting or GPS-less version depending on user preferences and device and client capabilities

			//Note
			notes = new NotesComponent();

			// tag observer
			longExposureTagger.setListener(notes);
			peakTagger.setListener(notes);

		}
		catch(Exception e)
		{
			log.error(e, "Engine initialization");
			e.printStackTrace();
		}
	}

	public void start()
	{
		if(!loudnessComponent.isRunning())
		{
			log.debug("Starting NoiseTube Mobile engine");

			start_time = System.currentTimeMillis();
			num_measures = 0;

			// Saving
			switch(preferences.getSavingMode())
			{
			// case Preferences.SAVE_NO : break;
			case Preferences.SAVE_HTTP:
				if(preferences.isAlsoSaveToFileWhenInHTTPMode())
				{
					saver = new MultiSaver();
					((MultiSaver) saver).addSaver(new HTTPSaver());
					((MultiSaver) saver).addSaver(new FileSaver());
				}
				else
					saver = new HTTPSaver();
				break;
			case Preferences.SAVE_FILE:
				saver = new FileSaver();
				break;
			case Preferences.SAVE_SMS: /* ... */
				break;
			// ...
			}
			if(preferences.getSavingMode() != Preferences.SAVE_NO)
			{
				try
				{
					saver.start();
					if(preferences.isUseCoordinateInterpolation())
					{
						Class CIBClass = Class.forName("noisetube.location.gps.CoordinateInterpolationBuffer");
						interpolationBuffer = (SavingBuffer) CIBClass.newInstance();
						interpolationBuffer.setSaver(saver); // !!!
						log.debug("Coordinate interpolation enabled");
					}
					else
						log.debug("Coordinate interpolation disabled");
				}
				catch(Exception e)
				{
					log.error(e, "starting saver failed; not saving measurements");
					saver = null;
					interpolationBuffer = null;
				}
			}
			else
				log.debug("Not saving measurements");

			locationComponent.start();
			loudnessComponent.start();
		}
		else
			log.error("Engine already running");
		return;
	}

	public void stop()
	{
		if(loudnessComponent.isRunning())
		{
			loudnessComponent.stop();
			locationComponent.stop();
			if(interpolationBuffer != null)
			{
				interpolationBuffer.flush(true);
				interpolationBuffer = null;
				saver = null;
			}
			else if(saver != null)
			{
				saver.stop();
				saver = null;
			}
		}
	}

	/**
	 * called from LoudnessComponent
	 * 
	 * @param sound
	 */
	public void sendLeq(double leq)
	{
		if(loudnessComponent.isRunning())
		{
			Measure soundMeasurement = new Measure(leq);
			longExposureTagger.sendLeq(leq);
			peakTagger.sendLeq(leq);
			if(preferences.isUseDoseMeter())
				doseMeter.sendLeq(leq);
			
			try
			{
				//get tag
				if(notes.hasNote())
				{
					soundMeasurement.setTags(notes.getNote());
					log.debug("Noise measurement tagged: " + notes.getNote());
				}
				notes.clear();

				//get & set location
				if(locationComponent.isRunning())
					soundMeasurement.setLocation(locationComponent.getLastLocation());

				//Save measurement
				if(interpolationBuffer != null)
					//use interpolation buffer to interpolate coordinates when position clamping is detected
					interpolationBuffer.enqueueMeasure(soundMeasurement); 
				else
				{ //save directly
					if(saver != null && saver.isRunning())
						saver.save(soundMeasurement);
				}

				//update the ui
				num_measures++;
				if(preferences.isUseDoseMeter())
					measureForm.setTitle(System.currentTimeMillis() - start_time, num_measures, doseMeter.getDose());
				else
					measureForm.setTitle(System.currentTimeMillis() - start_time, num_measures, 0);

				//Debug:
//				if(MainMidlet.CLIENT_IS_TEST_VERSION)
//				{
//					int memoryUsagePrc = (int) (((double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (double) Runtime.getRuntime().totalMemory()) * 100);
//					mlistener.receive(new Measure(memoryUsagePrc)); //HACK
//					log.debug("Memory usage: " + memoryUsagePrc + "%");
//					return;
//				}
				
				mlistener.receive(soundMeasurement);
			}
			catch(Exception e)
			{
				log.error(e, "sendLeq");
			}
		}
	}

	public void setForm(MeasureForm form)
	{
		this.measureForm = form;
		setMeasureListener(form);
	}

	public void setMeasureListener(IMeasureListener listener)
	{
		this.mlistener = listener;
	}

	public boolean isRunning()
	{
		return loudnessComponent.isRunning();
	}

	public ILoudnessComponent getLoudnessComponent()
	{
		return loudnessComponent;
	}

	public LocationComponent getLocationComponent()
	{
		return locationComponent;
	}

	public NotesComponent getNotesComponent()
	{
		return notes;
	}

	public SavingBuffer getInterpolationBuffer()
	{
		return interpolationBuffer;
	}

}
