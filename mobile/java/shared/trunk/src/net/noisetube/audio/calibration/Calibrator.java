/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile Calibrator (Java implementation)
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

package net.noisetube.audio.calibration;

import net.noisetube.audio.AudioComponent;
import net.noisetube.audio.format.AudioSpecification;
import net.noisetube.config.Preferences;
import net.noisetube.core.IService;
import net.noisetube.core.NTClient;
import net.noisetube.io.FileIO;
import net.noisetube.io.FileWriter;
import net.noisetube.model.IMeasurementListener;
import net.noisetube.model.Measurement;
import net.noisetube.util.Logger;
import net.noisetube.util.StringUtils;

/**
 * @author mstevens
 *
 */
public class Calibrator implements IService, IMeasurementListener
{

	private Logger log = Logger.getInstance();

	private NTClient ntClient;
	protected Preferences preferences;
	private String folderPath;
	private FileWriter fileWriter;
	private IMeasurementListener listener;
	
	//Audio-related:
	private long startTimeMS;
	private long previouslyElapsedMS;
	private int measurementCount;
	private AudioSpecification audioSpec = null;
	private AudioComponent audioComp;

	private String additionalDeviceInfo;

	public Calibrator(AudioSpecification audioSpec, IMeasurementListener listener, String additionalDeviceInfo, int measurementCount, long previouslyElapsedMS) throws Exception
	{
		this.audioSpec = audioSpec;
		ntClient = NTClient.getInstance();
		this.preferences = ntClient.getPreferences();
		this.folderPath = preferences.getDataFolderPath();
		audioComp = ntClient.getAudioComponent(audioSpec, this, null);
		audioComp.setLoudnessMode(AudioComponent.SPL_DB_AND_DBA);
		this.listener = listener;
		this.additionalDeviceInfo = additionalDeviceInfo;
		
		//For restarts:
		this.measurementCount = measurementCount;
		this.previouslyElapsedMS = previouslyElapsedMS;
	}
	
	public Calibrator(AudioSpecification audioSpec, IMeasurementListener listener, String additionalDeviceInfo) throws Exception
	{
		this(audioSpec, listener, additionalDeviceInfo, 0, 0);
	}
		
	public void start()
	{
		if(!audioComp.isRunning())
		{
			//Report file:
			try
			{
				String filePath = folderPath + "Report_";
				if(ntClient.isRestartingModeEnabled())
					filePath += "RUNNING";
				else
					filePath += StringUtils.formatDateTime(System.currentTimeMillis(), "-", "", "T");
				fileWriter = ntClient.getUTF8FileWriter(filePath + ".csv");
				fileWriter.open((ntClient.isRestartingModeEnabled() ? FileIO.FILE_EXISTS_STRATEGY_APPEND : FileIO.FILE_EXISTS_STRATEGY_CREATE_RENAMED_FILE), FileIO.FILE_DOES_NOT_EXIST_STRATEGY_CREATE);
			}
			catch(Exception e)
			{
				log.error(e, "Cannot create FileWriter");
				fileWriter = null;
				return; //!!!
			}
			if(ntClient.isFirstRun())
				writeHeader();
			
			//Audio stuff:
			measurementCount = 0;
			startTimeMS = System.currentTimeMillis();
			try
			{
				audioComp.start();
			}
			catch(Exception e)
			{
				log.error(e, "Could not (re)start calibration");
			}
		}		
	}
	
	protected void writeHeader()
	{
		fileWriter.writeLine("ElapsedTime;MeasurementNumber;dB;dBA;INFO");
		fileWriter.writeLine(";;;;Device: " + ntClient.getDevice().getBrand() + " " + ntClient.getDevice().getModel());
		fileWriter.writeLine(";;;;Audio spec: " + audioSpec.toVerboseString());
		fileWriter.writeLine(";;;;Additional info: " + additionalDeviceInfo);
	}
	
	public void stop()
	{
		if(audioComp.isRunning())
		{
			audioComp.stop(); //!!!
			try
			{
				fileWriter.close();
				if(ntClient.isRestartingModeEnabled() && ntClient.isLastRun())
					fileWriter.rename("Report_" + StringUtils.formatDateTime(System.currentTimeMillis(), "-", "", "T") + ".csv", FileIO.FILE_EXISTS_STRATEGY_CREATE_RENAMED_FILE); //rename file to seal it
				fileWriter.dispose();
				fileWriter = null;
			}
			catch(Exception ignore) {}
		}
	}

	public boolean isRunning()
	{
		return audioComp.isRunning();
	}
	
	public void receiveMeasurement(Measurement m)
	{
		measurementCount++;
		long elapsedTimeMS = previouslyElapsedMS + (m.getTimeStamp().getTime() - startTimeMS);
		fileWriter.writeLine(StringUtils.formatTimeSpanColons(elapsedTimeMS) + ";" + measurementCount + ";" + Double.toString(m.getLeqDB()) + ";" + Double.toString(m.getLeqDBA()));
		listener.receiveMeasurement(m);
	}

	/**
	 * @return the audioSpec
	 */
	public AudioSpecification getAudioSpec()
	{
		return audioSpec;
	}

	public long getElapsedTimeMS()
	{
		return previouslyElapsedMS + (System.currentTimeMillis() - startTimeMS);
	}

	/**
	 * @return the measurementCount
	 */
	public int getMeasurementCount()
	{
		return measurementCount;
	}

	/**
	 * @param listener the listener to set
	 */
	public void setListener(IMeasurementListener listener)
	{
		this.listener = listener;
	}

}
