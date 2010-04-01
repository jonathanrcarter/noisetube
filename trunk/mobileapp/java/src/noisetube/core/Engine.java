package noisetube.core;

import noisetube.MainMidlet;
import noisetube.audio.ILeqListener;
import noisetube.audio.ILoudnessComponent;
import noisetube.audio.LoudnessComponentFactory;
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
 * Core Engine (TODO use observer pattern)
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
	// private DoseMeter doseMeter;

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
			// doseMeter = new DoseMeter();

			// Localisation
			locationComponent = LocationComponent.getLocationComponent(this); // will
																				// give
																				// either
																				// GPS-supporting
																				// or
																				// GPS-less
																				// version
																				// depending
																				// on
																				// user
																				// preferences
																				// and
																				// device
																				// and
																				// client
																				// capabilities

			// Note
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
						Class CIBClass = Class
								.forName("noisetube.location.gps.CoordinateInterpolationBuffer");
						interpolationBuffer = (SavingBuffer) CIBClass
								.newInstance();
						interpolationBuffer.setSaver(saver); // !!!
						log.debug("Coordinate interpolation enabled");
					}
					else
						log.debug("Coordinate interpolation disabled");
				}
				catch(Exception e)
				{
					log.error(e,
							"starting saver failed; not saving measurements");
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
	 * called from AudioRecorder
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
			// doseMeter.sendLeq(leq);

			try
			{
				// get tag
				if(notes.hasNote())
				{
					soundMeasurement.setTags(notes.getNote());
					log.debug("Noise measurement tagged: " + notes.getNote());
				}
				notes.clear();

				// get & set location
				if(locationComponent.isRunning())
					soundMeasurement.setLocation(locationComponent
							.getLastLocation());

				// Save measurement
				if(interpolationBuffer != null)
					interpolationBuffer.enqueueMeasure(soundMeasurement); // use
																			// interpolation
																			// buffer
																			// to
																			// interpolate
																			// coordinates
																			// when
																			// GPS
																			// uses
																			// position
																			// clamping
				else
				{ // save directly
					if(saver != null && saver.isRunning())
						saver.save(soundMeasurement);
				}

				// update time of the ui
				num_measures++;
				// measureForm.setTitle(System.currentTimeMillis() - start_time,
				// num_measures, doseMeter.getDose());
				measureForm.setTitle(System.currentTimeMillis() - start_time,
						num_measures, 0);

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
