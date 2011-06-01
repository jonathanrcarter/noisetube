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

import java.util.TimerTask;

import net.noisetube.audio.format.AudioSpecification;
import net.noisetube.audio.format.AudioStream;
import net.noisetube.audio.format.IAudioStreamCorrector;
import net.noisetube.util.Logger;

/**
 * @author mstevens
 *
 */
public abstract class AudioRecorder
{
	
	public static final float DEFAULT_RECORDING_TIME_OVERSHOOT_RATIO = 0.2f; //overshoot for recording
	
	protected AudioSpecification audioSpec;
	protected int recordTimeMS;
	protected float recordingTimeOvershootRatio = DEFAULT_RECORDING_TIME_OVERSHOOT_RATIO;
	protected int actualRecordTimeMS;
	private IAudioStreamCorrector corrector = null;
	private AudioStreamListener listener;
	

	public AudioRecorder(AudioSpecification audioSpec, int recordTimeMS, AudioStreamListener listener)
	{
		if(audioSpec != null)
			this.audioSpec = audioSpec;
		else
			throw new NullPointerException("AudioSpecification cannot be null!");
		if(recordTimeMS > 0)
			this.recordTimeMS = recordTimeMS;
		else
			throw new IllegalArgumentException("record time should be positive!");
		actualRecordTimeMS = (int) (recordTimeMS * (1f + recordingTimeOvershootRatio));
		this.listener = listener;
	}

	/**
	 * @param corrector the corrector to set
	 */
	public void setCorrector(IAudioStreamCorrector corrector)
	{
		this.corrector = corrector;
	}
	
	/**
	 * @return the audioSpec
	 */
	public AudioSpecification getAudioSpec()
	{
		return audioSpec;
	}
	
	public TimerTask getTimerTask() //for asynchronous recording
	{
		return new TimerTask()
		{
			public void run()
			{
				try
				{
					AudioStream stream = record();
					if(listener != null && stream != null)
					{
						if(corrector != null)
							corrector.correct(stream);
						listener.receiveAudioStream(stream);
					}
				}
				catch(Exception e)
				{
					Logger.getInstance().error(e, "Recording failed");
					this.cancel();
				}
			}
			
			public boolean cancel()
			{
				release();
				return super.cancel();
			}
			
		};
	}
	
	public AudioStream getRecordedStream() throws Exception //for synchronous recording
	{
		AudioStream stream = record();
		if(stream != null)
		{
			if(corrector != null)
				corrector.correct(stream); //!!!
		}
		return stream;
	}
	
	public boolean testRecord() throws Exception
	{
		AudioStream stream = record();
		if(stream != null)
		{
			if(corrector != null)
				corrector.correct(stream); //!!!
			audioSpec.inferResultsFrom(stream); //do not move this before the corrector
			return stream.isValid(recordTimeMS) && stream.isDecodeable();
		}
		else
			return false;
	}
	
	protected abstract AudioStream record() throws Exception;

	public void release() {	} //to be overridden

}
