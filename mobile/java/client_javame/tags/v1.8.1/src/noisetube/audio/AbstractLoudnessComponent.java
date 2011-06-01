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

package noisetube.audio;

import noisetube.MainMidlet;
import noisetube.audio.java.Calibration;
import noisetube.audio.java.IStreamRecorder;
import noisetube.config.Preferences;
import noisetube.util.Logger;

/**
 * @author maisonneuve, mstevens
 * 
 */
public abstract class AbstractLoudnessComponent implements ILoudnessComponent
{

	protected Logger log = Logger.getInstance();

	/**
	 * Time of the recording
	 */
	private final int DEFAULT_RECORDING_TIME = 1200; // 1000

	protected double recordingTime = DEFAULT_RECORDING_TIME;

	protected ILeqListener listener;

	protected IStreamRecorder recorder;

	protected boolean corrected = true;
	protected boolean running = false;

	protected Calibration calibration;

	protected Preferences preferences = MainMidlet.getInstance().getPreferences();

	// compute correction
	protected double correct_leq(double leq)
	{
		leq = calibration.correctLeq(leq);
		return leq;
	}

	public void setLeqListener(ILeqListener listener)
	{
		this.listener = listener;
	}

	protected abstract IStreamRecorder getRecorder();

	public void start()
	{
		// updated the calibration
		calibration = preferences.getCalibration();

		recorder = getRecorder();
		recorder.start();
		running = true;
	}

	public void stop()
	{
		running = false;
		if(recorder != null && recorder.isRunning())
			recorder.stop();
	}

	public double getLeqInterval()
	{
		return recordingTime;
	}

	public boolean isRunning()
	{
		return running;
	}

	public void setRecordingTime(int i)
	{
		recordingTime = i;
	}

	public void setCorrected(boolean corrected)
	{
		this.corrected = corrected;
	}

}
