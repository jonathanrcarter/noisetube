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

import java.util.Timer;
import java.util.TimerTask;

import noisetube.util.Logger;

/**
 * 
 * Controller to loop recording
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class AudioRecorderRunner implements IStreamRecorder
{

	Logger log = Logger.getInstance();

	private volatile boolean recording = false;

	long start;

	private Timer timer = new Timer();

	private AudioRecorder recorder = new AudioRecorder();

	private long timeInterval;

	private long recordingTime;

	public boolean isRunning()
	{
		return recording;
	}

	public AudioRecorder getRecorder()
	{
		return recorder;
	}

	public void setStreamAudioListener(StreamAudioListener listener)
	{
		recorder.setRecorderListener(listener);
	}

//	public void start()
//	{
//		running = true;
//		
//		//start = System.currentTimeMillis();
//		//log.debug("Start audio runner (recording for " + recordingTime + " every " + timeInterval + ")");
//		
//		String captureURL = "capture://audio?encoding=" + encoding + "&rate=" + sampleRate + "&bits=" + bitsPerSample + "&channels=" + channels;
//		log.info("Recording with player: " + captureURL);
//		//Player player = AudioUtils.getPlayerForURL(captureURL);
//		int actualRecordTimeMS = (int) ((leqRecordTime * 1000) * (1f + recordTimeOvershoot));
//		
//		timer = new Timer();
//		timer.schedule(new RecordTask(captureURL, actualRecordTimeMS, this), 0, intervalTimeMS);
//	}
	
	public void start()
	{
		recording = true;
		start = System.currentTimeMillis();
		timer = new Timer();
		log.debug("Start audio runner (recording for " + recordingTime + " every " + timeInterval + ")");
		// new Thread(record_task).start();
		timer.schedule(new TimerTask() //ALTERNATIVE: Use RecordTask
		{
			public void run()
			{
				recorder.record_during(recordingTime);
			}
		}, 0, timeInterval);
	}

	public void stop()
	{
		recording = false;
		timer.cancel();
		timer = null;
		log.debug("Stop audio runner");
	}

	public void setRecordingTime(int i)
	{
		this.recordingTime = i;

	}

	public void setTimeInterval(int i)
	{
		this.timeInterval = i;

	}
}
