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

package noisetube.audio.java;

import java.io.ByteArrayOutputStream;
import java.util.TimerTask;

import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

import noisetube.audio.java.StreamAudioListener;
import noisetube.util.Logger;

/**
 * @author mstevens
 *
 */
public class RecordTask extends TimerTask
{
	
	private String captureURL;
	//private Player player;
	private int recordTime;
	private StreamAudioListener listener;
	
	public RecordTask(String captureURL, int recordTimeMS, StreamAudioListener listener)
	{
		//this(AudioUtils.getPlayerForURL(captureURL), recordTimeMS, listener);
		this(recordTimeMS, listener);
		this.captureURL = captureURL;
	}
	
//	public RecordTask(Player player, int recordTimeMS, StreamAudioListener listener)
//	{
//		this(recordTimeMS, listener);
//		if(player != null)
//			this.player = player;
//		else
//			throw new IllegalArgumentException("player cannot be null!");
//	}
	
	private RecordTask(int recordTimeMS, StreamAudioListener listener)
	{
		if(recordTimeMS > 0)
			recordTime = recordTimeMS;
		else
			throw new IllegalArgumentException("record time should be positive!");		
		if(listener != null)
			this.listener = listener;
		else
			throw new IllegalArgumentException("listener cannot be null!");
	}
	
	public void run()
	{
		try
		{
			Player player = AudioUtils.getPlayerForURL(captureURL);
			
			player.realize();
			RecordControl rc = (RecordControl) player.getControl("RecordControl");
			ByteArrayOutputStream soundStream = new ByteArrayOutputStream(); //sampleRate
			soundStream.reset();
			rc.setRecordStream(soundStream);
	
			// Start...
			rc.startRecord();
			player.start();
			
			//Wait...
			Thread.sleep(recordTime);
			
			//Stop
			rc.commit(); //implicitly calls rc.stopRecord();
			player.stop();
			
			listener.soundRecorded(soundStream.toByteArray());
		}
		catch(Exception e)
		{
			(Logger.getInstance()).debug("Recording failed: " + e.getMessage());
			this.cancel();
		}

	}

}
