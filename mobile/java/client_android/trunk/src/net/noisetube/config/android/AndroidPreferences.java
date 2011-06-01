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

package net.noisetube.config.android;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import net.noisetube.audio.calibration.Calibration;
import net.noisetube.config.NTAccount;
import net.noisetube.config.Preferences;
import net.noisetube.core.NTClient;
import net.noisetube.io.android.FileAccess;

/**
 * Account information
 * 
 * @author mstevens, sbarthol
 * 
 */
public class AndroidPreferences extends Preferences
{

	private static final String NOT_LOGGED_IN = "notloggedin";
	private static final String UNCHANGED_CALIBRATION = "unchangedCalibration";
	private static final String STORAGE_PATH = "preferences_" + NTClient.getInstance().getClientVersion() + "/store";

	public AndroidPreferences(AndroidDevice device)
	{
		super(device); //sets defaults, loads stored pref's & checks/corrects consistency
	}

	@Override
	protected void setAdditionalDefaults()
	{
		//No additional Android defaults (for now)
		//alwaysUseBatchModeForHTTP = true;
		forceGPS = true; //don't suspend to save power
	}

	@Override
	public void loadFromStorage()
	{	
		try
		{
			//First get folder
			File folder = FileAccess.getFolder(FileAccess.getFolderPath(getInternalDataFolderPath() + STORAGE_PATH));

			//Then build dirs of this folder (if they don't exists yet)
			folder.mkdirs();

			//Then get File object of the actual file
			File file = new File(folder, FileAccess.getFileName(getInternalDataFolderPath() + STORAGE_PATH));

			if(!file.exists())
				return;

			//Open a FileWriter
			FileReader fr = new FileReader(file);
			//Write information to FileWriter
			readSettingsFrom(fr);
		}
		catch (Exception e)
		{
			log.error(e, "AndroidPreferences.java -- loadFromStorage");
		}
	}
	
	protected void readSettingsFrom(FileReader fr) throws IOException
	{
		char[] buf = new char[500];
		fr.read(buf);

		String buffer = new String(buf);
		buffer = buffer.trim();
		String bufferPart;
		int nextNewline;

		//Account
		nextNewline = buffer.indexOf("\n");
		bufferPart = buffer.substring(0, nextNewline);
		buffer = buffer.substring(nextNewline+1);
		if(bufferPart != null && !bufferPart.equals("") && !bufferPart.equals(NOT_LOGGED_IN))
			account = NTAccount.deserialise(bufferPart);

		//IO (1)
		nextNewline = buffer.indexOf("\n");
		bufferPart = buffer.substring(0, nextNewline);
		buffer = buffer.substring(nextNewline+1);
		boolean preferMem = Boolean.parseBoolean(bufferPart);
		setPreferMemoryCard(preferMem);

		//IO (2)
		nextNewline = buffer.indexOf("\n");
		bufferPart = buffer.substring(0, nextNewline);
		buffer = buffer.substring(nextNewline+1);
		setSavingMode(Integer.parseInt(bufferPart));

		//IO (3)
		nextNewline = buffer.indexOf("\n");
		bufferPart = buffer.substring(0, nextNewline);
		buffer = buffer.substring(nextNewline+1);
		setAlsoSaveToFileWhenInHTTPMode(Boolean.parseBoolean(bufferPart));

		//IO (4)
		nextNewline = buffer.indexOf("\n");
		bufferPart = buffer.substring(0, nextNewline);
		buffer = buffer.substring(nextNewline+1);
		setAlwaysUseBatchModeForHTTP(Boolean.parseBoolean(bufferPart));
		
		//GPS (1)
		nextNewline = buffer.indexOf("\n");
		bufferPart = buffer.substring(0, nextNewline);
		buffer = buffer.substring(nextNewline+1);
		setUseGPS(Boolean.parseBoolean(bufferPart));

		//GPS (2)
		nextNewline = buffer.indexOf("\n");
		bufferPart = buffer.substring(0, nextNewline);
		buffer = buffer.substring(nextNewline+1);
		setForceGPS(Boolean.parseBoolean(bufferPart));

		//GPS (3)
		nextNewline = buffer.indexOf("\n");
		bufferPart = buffer.substring(0, nextNewline);
		buffer = buffer.substring(nextNewline+1);
		setUseCoordinateInterpolation(Boolean.parseBoolean(bufferPart));

		//UI
		nextNewline = buffer.indexOf("\n");
		bufferPart = buffer.substring(0, nextNewline);
		buffer = buffer.substring(nextNewline+1);
		setPauseWhenInBackground(Boolean.parseBoolean(bufferPart));
		
		//AUDIO (1)
		nextNewline = buffer.indexOf("\n");
		bufferPart = buffer.substring(0, nextNewline);
		buffer = buffer.substring(nextNewline+1);
		setUseDoseMeter(Boolean.parseBoolean(bufferPart));

		//AUDIO (2)
		bufferPart = buffer;
		if(bufferPart != null && !bufferPart.equals("") && !bufferPart.equals(UNCHANGED_CALIBRATION))
		{	
			Calibration parsedCal = NTClient.getInstance().getCalibrationParser().parseCalibration(bufferPart, Calibration.SOURCE_USER_PREFERECES);
			if(parsedCal != null)
				calibration = parsedCal;
		}

		fr.close();
	}

	@Override
	public void saveToStorage()
	{
		try
		{
			//First get folder
			File folder = FileAccess.getFolder(FileAccess.getFolderPath(getInternalDataFolderPath() + STORAGE_PATH));

			//Then build dirs of this folder (if they don't exists yet)
			folder.mkdirs();

			//Then get File object of the actual file
			File file = new File(folder, FileAccess.getFileName(getInternalDataFolderPath() + STORAGE_PATH));
			
			//Open a FileWriter
			FileWriter fw = new FileWriter(file);

			//Write information to FileWriter
			writeSettingsTo(fw);
		}
		catch (Exception e)
		{
			log.error(e, "saveToStorate()");
		}
	}

	protected void writeSettingsTo(FileWriter fw) throws IOException
	{
		//Account
		if(account != null)
			fw.write(account.serialise());
		else
			fw.write(NOT_LOGGED_IN);
		fw.write("\n");

		//IO
		fw.write(new Boolean(isPreferMemoryCard()).toString()); fw.write("\n");
		fw.write(""+getSavingMode()); fw.write("\n");
		fw.write(new Boolean(isAlsoSaveToFileWhenInHTTPMode()).toString()); fw.write("\n");
		fw.write(new Boolean(isAlwaysUseBatchModeForHTTP()).toString()); fw.write("\n");

		//GPS
		fw.write(new Boolean(isUseGPS()).toString()); fw.write("\n");
		fw.write(new Boolean(isForceGPS()).toString()); fw.write("\n");
		fw.write(new Boolean(isUseCoordinateInterpolation()).toString()); fw.write("\n");

		//UI
		fw.write(new Boolean(isPauseWhenInBackground()).toString()); fw.write("\n");
		
		//AUDIO
		fw.write(new Boolean(isUseDoseMeter()).toString()); fw.write("\n");
		if(calibration != null && calibration.isManuallyChanged())
			fw.write(calibration.toXML());
		else
			fw.write(UNCHANGED_CALIBRATION);
		
		fw.flush();
		fw.close();
	}

	/**
	 * Used to store and read preferences onto/from the internal storage.
	 * @return The internal storage path.
	 */
	private String getInternalDataFolderPath()
	{
		return device.getDataFolderPath(false);
	}
}
