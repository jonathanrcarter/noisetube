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
import net.noisetube.util.Logger;

/**
 * @author mstevens
 *
 */
public abstract class AudioRecorder
{
	
	protected AudioSpecification audioSpec;
	protected int recordTimeMS; //in ms
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
		this.listener = listener;
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
						listener.receiveAudioStream(stream);
				}
				catch(Exception e)
				{
					(Logger.getInstance()).error("Recording failed: " + e.getMessage());
					this.cancel();
				}
			}
		};
	}
	
	public AudioStream getRecordedStream() throws Exception //for synchronous recording
	{
		return record();
	}
	
	public abstract AudioStream record() throws Exception;

}
