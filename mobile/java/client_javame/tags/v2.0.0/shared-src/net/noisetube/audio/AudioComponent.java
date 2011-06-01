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

package net.noisetube.audio;

import java.util.Timer;

import net.noisetube.audio.calibration.Calibration;
import net.noisetube.audio.calibration.Calibration.Corrector;
import net.noisetube.audio.format.AudioSpecification;
import net.noisetube.audio.format.AudioStream;
import net.noisetube.core.IPausableService;
import net.noisetube.core.NTClient;
import net.noisetube.model.IMeasurementListener;
import net.noisetube.model.Measurement;
import net.noisetube.util.Logger;
import net.noisetube.util.MathME;

/**
 * @author mstevens, maisonneuve
 *
 */
public class AudioComponent implements IPausableService, AudioStreamListener
{

	//STATICS--------------------------------------------------------
	public static final int DEFAULT_RECORDING_TIME_MS = 1000;
	public static final int DEFAULT_INTERVAL_MS = 1000; //interval between 2 recordings
	public static final float DEFAULT_RECORDING_TIME_OVERSHOOT_RATIO = 0.2f; //overshoot for recording

	public static final int LOUDNESS_DB_ONLY = 0;
	public static final int LOUDNESS_DBA_ONLY = 1;
	public static final int LOUDNESS_DB_AND_DBA = 2;	
	public static final int DEFAULT_LOUDNESS_DB_MODE = LOUDNESS_DBA_ONLY;

	//http://www.noisemeters.com/help/faq/time-weighting.asp //TODO is response the right terminology?
	public static final double SLOW_RESPONSE_TIME_S = 1.0;
	public static final double FAST_RESPONSE_TIME_S = 0.125;
	public static final double IMPULSE_RESPONSE_TIME_S = 0.035;
	public static final double DEFAULT_RESPONSE_TIME_S = SLOW_RESPONSE_TIME_S;

	private static Logger log = Logger.getInstance();

	//DYNAMICS-------------------------------------------------------
	protected int recordingTimeMS = DEFAULT_RECORDING_TIME_MS;
	protected int intervalTimeMS = DEFAULT_INTERVAL_MS;
	protected float recordingTimeOvershootRatio = DEFAULT_RECORDING_TIME_OVERSHOOT_RATIO;
	protected double responseTimeS = DEFAULT_RESPONSE_TIME_S;
	protected int dbMode = DEFAULT_LOUDNESS_DB_MODE;

	protected AudioSpecification audioSpec;
	protected AudioRecorder recorder;
	protected AFilter theAFilter = null;
	protected Calibration calibration;
	protected Corrector calibrationCorrector;
	protected IMeasurementListener listener;
	protected Timer timer;
	protected boolean paused = false;
	private long totalTimeActive = 0; //in ms
	private long startTime = 0;
	
	public AudioComponent(AudioSpecification audioSpec, IMeasurementListener listener) throws Exception
	{
		this(audioSpec, listener, null);
	}

	public AudioComponent(AudioSpecification audioSpec, IMeasurementListener listener, Calibration calibration) throws Exception
	{
		this.audioSpec = audioSpec;
		if(dbMode > LOUDNESS_DB_ONLY)
		{
			if(audioSpec.isResultSampleRateSet())
				theAFilter = AFilter.getAFilter((int) audioSpec.getResultSampleRate());
			else if(audioSpec.isSampleRateSet())
				theAFilter = AFilter.getAFilter((int) audioSpec.getSampleRate());
			else
				throw new Exception("Cannot configure A-filter because samplerate is unknown");
		}
		this.calibration = calibration;
		this.listener = listener;
	}

	public void start()
	{
		if(recorder == null) //if(!running)
		{
			if(calibration != null)
				calibrationCorrector = calibration.getCorrector();
			int actualRecordTimeMS = (int) (recordingTimeMS * (1f + recordingTimeOvershootRatio));
			startRecording(NTClient.getInstance().getAudioRecorder(audioSpec, actualRecordTimeMS, this));
			log.info("AudioComponent started: ");
			log.info(" - recording for " + recordingTimeMS/1000 + "s every " + intervalTimeMS/1000 + "s");
			log.info(" - using audio spec: " + audioSpec.toVerboseString());
			log.info(" - " + (calibration != null ? "using " + calibration.toString() : "without calibration"));
			if(calibration != null)
				log.info(" - effective calibration credibility: " + calibration.getEffeciveCredibilityIndex());
		}
	}
	
	private void startRecording(AudioRecorder recorder)
	{
		timer = new Timer();
		timer.schedule(recorder.getTimerTask(), 0, intervalTimeMS);
		startTime = System.currentTimeMillis();
		this.recorder = recorder; //!!! running=true
	}
	
	private void stopRecording()
	{
		if(timer != null)
		{
			timer.cancel();
			timer = null;
			totalTimeActive += System.currentTimeMillis() - startTime;
			startTime = 0;
		}
	}

	public void pause()
	{
		if(recorder != null /*running*/ && !paused)
		{
			paused = true;
			stopRecording();
			log.info("AudioComponent paused");
		}
	}
	
	public void resume()
	{
		if(recorder != null /*running*/ && paused)
		{
			paused = false;
			startRecording(this.recorder); //resume with same recorder (and same corrector)
			log.info("AudioComponent resumed from pause");
		}
	}
	
	public void stop()
	{
		if(recorder != null) //if(running)
		{
			recorder = null; //!!! running=false
			stopRecording();
			calibrationCorrector = null;
			paused = false;
			//log.info("AudioComponent stopped");
		}
	}

	public boolean isRunning()
	{
		return recorder != null;
	}

	public boolean isPaused()
	{
		return paused;
	}
	
	public long getTotalTimeActiveMS()
	{
		return totalTimeActive + (startTime > 0 ? System.currentTimeMillis() - startTime : 0);
	}
	
	public void receiveAudioStream(final AudioStream stream)
	{
		if(recorder != null /*running*/ && !paused)
		{
			try
			{
				if(!stream.isValid(recordingTimeMS) || !stream.isDecodeable())
					throw new Exception("Invalid or undecodeable audio stream");				
				final int numMeasurements = (int) Math.max(Math.floor(recordingTimeMS/1000 / responseTimeS), 1);
				if(numMeasurements == 1) //when responseTimeS = SLOW (1s) and recordingTimeMS = 1000ms (1s)
				{
					Measurement m = new Measurement(stream.getRecordStartTime());
					analyseSamples(	stream.getDecoder().getSamplesAmplitude_Floating(0, (int) stream.getSampleRate() - 1, 0), //we only look at the first channel (for now)
									m);					
					listener.receiveMeasurement(m);
				}
				else //numMeasurements > 1
				{
					final int samplesPerMeasurement = (int) (recordingTimeMS/1000 * responseTimeS * stream.getSampleRate());
					new Thread(new Runnable()
					{
						public void run()
						{
							try
							{
								long measurementTime = stream.getRecordStartTime();
								for(int s = 0; s < numMeasurements; s++) //for each set of samples
								{
									measurementTime += responseTimeS * 1000;
									Measurement m = new Measurement(measurementTime);
									analyseSamples(	stream.getDecoder().getSamplesAmplitude_Floating(s * samplesPerMeasurement, ((s + 1) * samplesPerMeasurement) - 1, 0), //we only look at the first channel (for now)
													m);
									listener.receiveMeasurement(m);
									wait((long) responseTimeS*1000); //!!!
								}
							}
							catch(Exception e)
							{
								log.error("Exception during audio analysis: " + e.getMessage());
							}
						}
					}).start();
				}
			}
			catch(Exception e)
			{
				log.error("Exception during audio analysis: " + e.getMessage());
				//stop();
			}
		}
	}
	
	protected void analyseSamples(double[] samples, Measurement measurement) throws Exception
	{
		//LOUDNESS
		if(dbMode == LOUDNESS_DB_ONLY || dbMode == LOUDNESS_DB_AND_DBA)
		{
			measurement.setLoudnessLeqDB((calibrationCorrector != null ? calibrationCorrector.correctDB(computeLeq(samples)) : computeLeq(samples)));
		}
		if(dbMode > LOUDNESS_DB_ONLY)
		{	//Apply A-weighting filtering
			samples = theAFilter.apply(samples);
			measurement.setLoudnessLeqDBA((calibrationCorrector != null ? calibrationCorrector.correctDBA(computeLeq(samples)) : computeLeq(samples)));
		}
	}

	/**
	 * compute Leq from the array of samples
	 * 
	 * Leq = 10*log10[(1/T)*S[0,T]((pt/p0)^2)] dt
	 * 	with S[x,y]=integral over interval [x,y]
	 *  and p0 = 2x10^-5 = 0,00002
	 *     
	 * Integral computed as a Riemann sum (http://en.wikipedia.org/wiki/Riemann_integral):
	 * 
	 * Leq = 10*log10[(1/T)*E[0,T]((pt/p0)^2)]
	 *     = 10*log10[(1/T)*E[0,T]((pt*(1/p0))^2)]
	 *     = 10*log10[(1/T)*E[0,T](pt^2*(1/p0)^2)]
	 *     = 10*log10[(1/T)*E[0,T](pt^2)*(1/p0)^2]
	 *     = 10*log10[(1/T)*E[0,T](pt^2)*(1/p0)^2]
	 *     = 10*[log10[(1/T)*E[0,T](pt^2)] + log10[(1/p0)^2]]
	 *     = 10*log10[(E[0,T](pt^2)/T] + 10*log10[(1/p0)^2]
	 *     = 10*log10[(E[0,T](pt^2)/T] + 20*log10[(1/p0)]
	 *     = 10*log10[(E[0,T](pt^2)/T] + 20*log10[50000]
	 *     = 10*log10[(E[0,T](pt^2)/T] + 93.97940008672037609572522210551
	 * Log info: http://oakroadsystems.com/math/loglaws.htm
	 * 
	 * @param samples : array of sound samples (double)
	 * @return the Leq
	 */
	private double computeLeq(double samples[]) throws Exception
	{
		double d = 0.0D, leq;
		for(int i = 0; i < samples.length; i++)
		{
			double p_t = samples[i];
			d += p_t * p_t;
		}
		d /= samples.length;
		leq = (10D * MathME.log10(d)) + 93.97940008672037609572522210551d;
		if(leq <= 0)
			throw new Exception("Leq is negative: " + leq);
		return leq;
	}

	/**
	 * @return the dbMode
	 */
	public int getLoudnessMode()
	{
		return dbMode;
	}

	/**
	 * @param dbMode the dbMode to set
	 */
	public void setLoudnessMode(int dbMode)
	{
		this.dbMode = dbMode;
	}

	/**
	 * @return the intervalTimeMS
	 */
	public int getIntervalTimeMS()
	{
		return intervalTimeMS;
	}

	/**
	 * @return the audioSpec
	 */
	public AudioSpecification getAudioSpecification()
	{
		return audioSpec;
	}

	/**
	 * @return the calibration
	 */
	public Calibration getCalibration()
	{
		return calibration;
	}

}