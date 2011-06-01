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

package net.noisetube.io.saving;

import net.noisetube.config.Preferences;
import net.noisetube.core.NTClient;
import net.noisetube.io.FileIO;
import net.noisetube.io.FileWriter;
import net.noisetube.model.Measurement;
import net.noisetube.model.Track;
import net.noisetube.util.CustomStringBuffer;
import net.noisetube.util.StringUtils;
import net.noisetube.util.XMLUtils;

/**
 * File based track and measurement saving class
 * 
 * @author mstevens
 * 
 */
public class FileSaver extends Saver
{

	private NTClient ntClient;
	private String folderPath;
	private FileWriter fileWriter;
	
	public FileSaver(Track track)
	{
		super(track);
		ntClient = NTClient.getInstance();
		this.folderPath = preferences.getDataFolderPath();
	}

	/**
	 * @see noisetube.io.ISender#start()
	 */
	public void start()
	{
		if(!running)
		{
			try
			{
				if(folderPath == null)
					throw new NullPointerException("folderPath is null");
				String filePath = folderPath + "Track_";
				if(ntClient.isRestartingModeEnabled())
				{	
					filePath += "RUNNING.xml"; //will be renamed after last run
					fileWriter = ntClient.getUTF8FileWriter(filePath);
					if(ntClient.isFirstRun() && fileWriter.fileExists())
					{	//This is a track file of an older unresumed running session, let's close it:
						log.debug("Found unresumed \"RUNNING\" track file, closing & renaming");
						long lastChanged = fileWriter.fileLastChanged();
						fileWriter.open(FileIO.FILE_EXISTS_STRATEGY_APPEND, FileIO.FILE_DOES_NOT_EXIST_STRATEGY_CREATE);
						fileWriter.writeLine(XMLUtils.comment("Track never resumed", 1));
						writeFooter();
						fileWriter.rename("Track_" + StringUtils.formatDateTime(lastChanged, "-", "", "T") + ".xml", FileIO.FILE_EXISTS_STRATEGY_REPLACE);
						fileWriter.dispose(); //also calls close()
						//Again take the (new) xml file:
						fileWriter = ntClient.getUTF8FileWriter(filePath);
					}
					fileWriter.open(FileIO.FILE_EXISTS_STRATEGY_APPEND, FileIO.FILE_DOES_NOT_EXIST_STRATEGY_CREATE);
				}
				else
				{
					filePath += StringUtils.formatDateTime(System.currentTimeMillis(), "-", "", "T") + ".xml";
					fileWriter = ntClient.getUTF8FileWriter(filePath);
					fileWriter.open(FileIO.FILE_EXISTS_STRATEGY_CREATE_RENAMED_FILE, FileIO.FILE_DOES_NOT_EXIST_STRATEGY_CREATE);
				}
			}
			catch(Exception e)
			{
				log.error(e, "Cannot create FileWriter");
				fileWriter = null;
				return; //!!!
			}
			running = true;
			if(ntClient.isFirstRun())
				writeHeader();
			log.debug("FileSaver started (file: " + fileWriter.getFullPath() + ")");
			if(preferences.getSavingMode() == Preferences.SAVE_FILE)
				setStatus("Saving to file: " + fileWriter.getFileName());
		}
	}
	
	public void pause()
	{
		if(running)
		{
			paused = true;
			fileWriter.writeLine(XMLUtils.comment(XMLUtils.timeDateValue(System.currentTimeMillis()) + ": measuring paused", 1));
			//setStatus(getStatus() + " [paused]");
		}
	}
	
	public void resume()
	{
		if(paused)
		{
			paused = false; //resume from pause
			fileWriter.writeLine(XMLUtils.comment(XMLUtils.timeDateValue(System.currentTimeMillis()) + ": measuring resumed", 1));
			//setStatus(getStatus().substring(0, getStatus().length() - " [paused]".length()));
		}
	}

	/**
	 * @see noisetube.io.ISender#stop()
	 */
	public void stop()
	{
		stop(false);
	}

	protected void stop(boolean force)
	{
		if(running)
		{
			if(!force)
				if(ntClient.isRestartingModeEnabled() && !ntClient.isLastRun())
					fileWriter.writeLine(XMLUtils.comment("End of run", 1));
				else
					writeFooter();
			running = false; //Note: do not move this before writeFooter!
			try
			{
				fileWriter.close();
				if(ntClient.isRestartingModeEnabled() && ntClient.isLastRun())
					fileWriter.rename("Track_" + StringUtils.formatDateTime(System.currentTimeMillis(), "-", "", "T") + ".xml", FileIO.FILE_EXISTS_STRATEGY_CREATE_RENAMED_FILE); //rename file to seal it
				fileWriter.dispose();
				fileWriter = null;
			}
			catch(Exception ignore) {}
			//log.debug("FileSaver stopped" + (force ? " (forced)" : ""));
			if(preferences.getSavingMode() == Preferences.SAVE_FILE)
				setStatus("Stopped");
		}
	}
	
	/**
	 * @see noisetube.io.ISender#save(net.noisetube.model.Measurement)
	 */
	public void save(Measurement measurement)
	{
		if(running && !paused && measurement != null)
			fileWriter.writeLine("\t" + measurement.toXML());
	}
	
	protected void writeHeader()
	{
		CustomStringBuffer bff = new CustomStringBuffer();
		//XML file headers
		bff.appendLine(XMLUtils.header());
		bff.append("<NoiseTube-Mobile-Session startTime=\""	+ XMLUtils.timeDateValue(track.getStartTime())	+ "\" ");
		if(preferences.isAuthenticated())
			bff.append("userKey=\"" + preferences.getAccount().getAPIKey() + "\" ");
		//Metadata (includes client and device info):
		bff.append(track.getMetaDataString("=", " ", '\"', false, new XMLUtils.XMLStringEncoder()));
		bff.appendLine(">");
		//Write to file
		fileWriter.write(bff.toString());
	}

	protected void writeFooter()
	{
		fileWriter.writeLine("</NoiseTube-Mobile-Session>");
	}

	public void enableBatchMode() { }

}
