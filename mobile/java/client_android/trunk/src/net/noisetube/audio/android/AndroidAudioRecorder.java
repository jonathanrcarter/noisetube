/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Android version)
 *  
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011
 *  Android port by Vrije Universiteit Brussel (BrusSense team), 2010-2011
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

package net.noisetube.audio.android;

import net.noisetube.audio.AudioStreamListener;
import net.noisetube.audio.AudioRecorder;
import net.noisetube.audio.format.AudioSpecification;
import net.noisetube.audio.format.AudioStream;


import android.media.AudioRecord;

/**
 * @author sbarthol, mstevens
 *
 */
public class AndroidAudioRecorder extends AudioRecorder
{

	private AudioRecord audioRecord; //!!!
	
	private int bufferSize;
	private byte[] byteBuffer = null;

	public AndroidAudioRecorder(AudioSpecification audioSpec, int recordTimeMS, AudioStreamListener listener)
	{
		super(audioSpec, recordTimeMS, listener); //!!!
		
		//Calculate the bufferSize, depending on actualRecordTimeMS:		
		int minBufferSize = AudioRecord.getMinBufferSize(	audioSpec.getSampleRate(),
															((AndroidAudioSpecification) audioSpec).getChannelConfig(),
															((AndroidAudioSpecification) audioSpec).getAudioFormat());
		if(minBufferSize < 0)
		{
			switch(minBufferSize)
			{
				case AudioRecord.ERROR_INVALID_OPERATION :
					throw new IllegalStateException("AudioRecord.getMinBufferSize() returned AudioRecord.ERROR_INVALID_OPERATION");
				case AudioRecord.ERROR_BAD_VALUE :
					throw new IllegalStateException("AudioRecord.getMinBufferSize() returned AudioRecord.ERROR_BAD_VALUE");
				case AudioRecord.ERROR :
					throw new IllegalStateException("AudioRecord.getMinBufferSize() returned AudioRecord.ERROR");
			}
		}
		this.bufferSize = Math.max(audioSpec.getByteRate() * (actualRecordTimeMS / 1000), minBufferSize);
		//Create AudioRecord:
		audioRecord = ((AndroidAudioSpecification) audioSpec).getAudioRecord(this.bufferSize);
	}
	
	@Override
	public void release()
	{
		if(audioRecord != null)
		{
			audioRecord.release();
			audioRecord = null;
		}
	}

	/**
	 * Using the AudioRecord class, we have to implement this in a different
	 * way than implementing e.g. JavaMERecordTask. An AudioRecord instance
	 * will be polled to read a number of bytes, in stead of reading to a
	 * stream. So it is our task to make sure we ask exact the amount of bytes
	 * to cover the length of the track, asked by recordTimeMS.
	 */
	@Override
	public AudioStream record() throws Exception
	{
		if(audioRecord == null || audioRecord.getRecordingState() != AudioRecord.STATE_INITIALIZED)
			throw new Exception("AudioRecord not initialized");

		//Create buffer:
		/* byteBuffer is a buffer which will be filled with the recorded bytes */
		byteBuffer = new byte[bufferSize];

		//Read bytes:
		/* Set thread priority to high */
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

		//Start recording
		long recordStartTime = System.currentTimeMillis();
		audioRecord.startRecording();
		
		/* The resulting bytesRead states how much bytes are actually read,
		 * which should be the same as the bufferSize. It also contains
		 * errors (if any have occurred). */
		int bytesRead = audioRecord.read(byteBuffer, 0, bufferSize);

		//Stop recording
		try
		{
			audioRecord.stop();
		}
		catch(Exception e)
		{	//this often fails when application is stopping (nothing to worry about)
			//(Logger.getInstance()).error(e, "AndroidAudioRecorder.record(), calling audioRecord.stop()");
			return null;
		}
	
		//Check for errors:
		switch(bytesRead)
		{
			case AudioRecord.ERROR_INVALID_OPERATION :
				throw new IllegalStateException("read() returned AudioRecord.ERROR_INVALID_OPERATION");
			case AudioRecord.ERROR_BAD_VALUE :
				throw new IllegalStateException("read() returned AudioRecord.ERROR_BAD_VALUE");
			case AudioRecord.ERROR :
				throw new IllegalStateException("read() returned AudioRecord.ERROR");
		}

		//Check if there are as many bytes read as their should be read:
		//if (bytesRead == bufferSize) {}

		//Return the headerless bytestream, which will be placed in a RawAudioStream package
		return AudioStream.packageInSuitableStream(audioSpec, recordStartTime, byteBuffer);
	}

}
