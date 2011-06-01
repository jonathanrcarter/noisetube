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

import noisetube.audio.AbstractLoudnessComponent;

public class LoudnessComponentJava extends AbstractLoudnessComponent implements StreamAudioListener
{

	protected LoudnessExtraction extraction = new LoudnessExtraction();

	boolean AFilter = true;

	/**
	 * Interval between 2 recordings
	 */
	private final int DEFAULT_INTERVAL = 1000; // 2000

	int timeInterval = DEFAULT_INTERVAL;

	public void start()
	{
		extraction.setAfilterActive(AFilter);
		super.start();
	}

	public void setAFilterActive(boolean afilter)
	{
		this.AFilter = afilter;
	}

	protected IStreamRecorder getRecorder()
	{
		recorder = new AudioRecorderRunner();
		recorder.setStreamAudioListener(this);
		recorder.setRecordingTime((int) recordingTime);
		recorder.setTimeInterval(timeInterval);
		return recorder;
	}

	public void setTimeInterval(int i)
	{
		this.timeInterval = i;
	}

	// for java recording
	public void soundRecorded(byte[] stream)
	{
		double leq = 0;

		// compute leq
		try
		{
			leq = extraction.compute_leqs(stream)[0];
			if(corrected)
				leq = correct_leq(leq);
			if(leq > 0)
			{	
				log.debug("Loudness: " + leq + " dB(A)");
				listener.sendLeq(leq);
			}
			else
				log.error("leq negative  " + leq);
		}
		catch(Exception e)
		{
			log.error(e.getMessage());
		}
	}
}
