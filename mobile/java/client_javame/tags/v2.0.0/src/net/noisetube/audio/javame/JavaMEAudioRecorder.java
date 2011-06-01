/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Java ME version)
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

package net.noisetube.audio.javame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

import net.noisetube.audio.AudioStreamListener;
import net.noisetube.audio.AudioRecorder;
import net.noisetube.audio.format.AudioStream;

public class JavaMEAudioRecorder extends AudioRecorder
{
	
	private ByteArrayOutputStream byteStream = null;
	private boolean recyclePlayer = false;
	private boolean recycleByteStream = false;
	private Player player = null;
	
	public JavaMEAudioRecorder(JavaMEAudioSpecification audioSpec, int recordTimeMS, AudioStreamListener listener)
	{
		this(audioSpec, recordTimeMS, listener, false); //do not recycle the player instance by default
	}

	public JavaMEAudioRecorder(JavaMEAudioSpecification audioSpec, int recordTimeMS, AudioStreamListener listener, boolean recyclePlayer)
	{
		super(audioSpec, recordTimeMS, listener);
		this.recyclePlayer = recyclePlayer;
	}
	
	public AudioStream record() throws Exception
	{
		if(!recyclePlayer || player == null)
			player = ((JavaMEAudioSpecification) audioSpec).getPlayer();
		if(!recycleByteStream || byteStream == null)
			byteStream = new ByteArrayOutputStream(/*expectedDataSize*/); //TODO test if specifying size makes a difference for the crash bug
		else
			byteStream.reset();
		RecordControl rc = null;
		try
		{
			if(player == null)
				throw new Exception("Unable to instanciate Player");	
			player.realize();
			rc = (RecordControl) player.getControl("RecordControl");
			rc.setRecordStream(byteStream);
			//if(audioSpec.isSampleRateSet() && audioSpec.isBitsPerSampleSet() && audioSpec.isNumChannelsSet()) //TODO test if this makes a difference for the crash bug
			//	rc.setRecordSizeLimit(recordTime/1000 * audioSpec.getSampleRate() * audioSpec.getBitsPerSample()/8 * audioSpec.getNumChannels());
			//Start...
			long recordStartTime = System.currentTimeMillis();
			rc.startRecord();
			player.start();
			//Wait...
			Thread.sleep(recordTimeMS);
			//Stop
			rc.commit(); //implicitly calls rc.stopRecord();
			player.stop();
			//(Logger.getInstance()).info(rc.getContentType());
			if(byteStream.size() > 0)
				return AudioStream.packageInSuitableStream(audioSpec, recordStartTime, byteStream.toByteArray());
			else
				return null;
		}
		catch(InterruptedException ie)
		{
			try
			{
				rc.commit(); //implicitly calls rc.stopRecord();
				player.stop();
			}
			catch(Exception ignore) { }
			return null;
		}
		finally
		{
			if(!recycleByteStream)
				try
				{
					byteStream.close();
				}
				catch(IOException igonore) {}
		}
	}

}
