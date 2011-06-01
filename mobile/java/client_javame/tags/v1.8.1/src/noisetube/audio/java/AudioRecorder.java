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

//import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

import noisetube.config.Device;
import noisetube.util.Logger;

/**
 * 
 * Audio Recorder
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class AudioRecorder
{

	Logger log = Logger.getInstance();
	
	// private static final int KB = 1024;
	// private static final int RecordSizeLimit = 50 * KB;
	
	static int bitsPerSample = 16; //bits
	static int channels = 1; //1 = Mono, 2 = Stereo
	
	private String encoding;
	private int sampleRate;
	private String captureURL;
	
	/**
	 * MMAPI recorder and player
	 */
	private RecordControl rc;
	private Player player;

	//private static String audio_url = null;

	private volatile boolean recording = false;

	private ByteArrayOutputStream sound_stream = new ByteArrayOutputStream(); //DEFAULT_SAMPLERATE);

	private StreamAudioListener listener;

	public AudioRecorder() // throws Exception
	{
		this.sampleRate = Device.getBestSampleRate();
		// if(Device.getBestAudioEncoding() != null)
		this.encoding = Device.getBestAudioEncoding();
		/*
		 * else throw new Exception("Encoding cannot be null!");
		 */
		captureURL = "capture://audio?encoding=" + encoding + "&rate=" + sampleRate + "&bits=" + bitsPerSample + "&channels=" + channels;
		log.debug("Using capture string: " + captureURL);
	}

	public void setSampleRate(int sampleRate)
	{
		this.sampleRate = sampleRate;
	}

	public void setRecorderListener(StreamAudioListener l)
	{
		listener = l;
	}

	public boolean isRecording()
	{
		return recording;
	}

	public boolean record_during(long recordTime)
	{
		// Start recording
		// long start_time = System.currentTimeMillis();
		if(!startRecording())
			return false;

		// Wait...
		try
		{
			Thread.sleep(recordTime);
		}
		catch(Exception e)
		{
		}

		// Stop recording
		if(!stopRecording())
			return false;

		// Get duration
		// long end_time = System.currentTimeMillis();
		// long time = ((end_time - start_time) / 1000);
		// log.debug("latency: " + (end_time - start_time));

		return true;
	}

	public boolean startRecording()
	{
		try
		{
			if(recording)
			{
				log.debug("Recording has already started");
				return false;
			}

			//To be sure that there is any issue
			//reset(); //!!!

			player = AudioUtils.getPlayerForURL(captureURL);
			
			//No recorder....
			if(player == null)
			{
				log.error("startRecording: Could not get player!");
				return false;
			}

			player.realize();
			rc = (RecordControl) player.getControl("RecordControl");
			sound_stream.reset();
			rc.setRecordStream(sound_stream);

			// in some j2me platforms the recordSizeLimit function is not implemented (MEMORY Leak if use that MAX.Integer)
			/*
			 * try
			 * {
			 * 		rc.setRecordSizeLimit(RecordSizeLimit);
			 * }
			 * catch (Exception ignore)
			 * {
			 * 		log.error("record size error");
			 * }
			 */

			// Start...
			rc.startRecord();
			player.start();
			recording = true;
			return true;
		}
		catch(Exception e)
		{
			log.error(e, "Start recording");
			reset();
			return false;
		}
	}

	public boolean stopRecording()
	{
		try
		{
			if(!recording)
			{
				log.debug("Recording has already stopped");
				return false;
			}
			
			//Stop...
			rc.stopRecord();
			rc.commit();
			
			//Send data it to listener...
			if(listener != null)
				listener.soundRecorded(sound_stream.toByteArray());
			
			reset(); //!!!
			return true;
		}
		catch(Exception e)
		{
			reset();
			try
			{
				Thread.sleep(2000);
			}
			catch(InterruptedException ie)
			{
			}
			log.error(e, "Stop recording");
			return false;
		}
	}

	private void reset()
	{
		try
		{
			if(player != null)
			{
				if(player.getState() == Player.STARTED)
					player.stop();
				if(player.getState() == Player.PREFETCHED)
					player.deallocate();
				if(player.getState() != Player.CLOSED)
					player.close();
				player = null;
				rc = null;
			}
			System.gc();
		}
		catch(Exception e)
		{
			log.error(e, "Reset AudioRecorder");
		}
		recording = false;
	}

	//TODO bring back?
//	private Player getPlayer()
//	{
//		if(audio_url == null)
//		{ // try to get a player for different possibly working urls:
//			Player p = getPlayerForURL("capture://audio?encoding=" + encoding + "&rate=" + sampleRate + "&bits=" + bitsPerSample + "&channels=" + channels);
//			if(p != null)
//				return p;
//			p = getPlayerForURL("capture://audio?encoding=" + encoding);
//			if(p != null)
//				return p;
//			p = getPlayerForURL("capture://audio");
//			if(p != null)
//				return p;
//			else
//				return null;
//		}
//		else
//			return getPlayerForURL(audio_url); // reuse the known-to-be-working audio_url
//	}

//	private Player getPlayerForURL(String url)
//	{
//		Player p = null;
//		try
//		{
//			p = Manager.createPlayer(url);
//		}
//		catch(Exception e)
//		{
//			log.error(e, "Failed to get player for audio url: " + url);
//			if(p != null)
//				p.close();
//			return null;
//		}
//		if(audio_url == null)
//			log.debug("Found working audio url: " + url);
//		audio_url = url; // store this url as the known-to-be-working audio_url
//		return p;
//	}

}
