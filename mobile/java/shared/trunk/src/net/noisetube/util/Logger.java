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

package net.noisetube.util;

import java.util.Enumeration;

import net.noisetube.config.NTAccount;
import net.noisetube.core.NTClient;
import net.noisetube.io.FileIO;
import net.noisetube.io.FileWriter;
import net.noisetube.io.NTWebAPI;

/**
 * Logger
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class Logger
{

	//STATIC---------------------------------------------------------
	static public final int DEFAULT_CAPACITY = 70;
	
	static public final int ERROR = 0; //least verbose (only errors)
	static public final int INFORMATION = 1;
	static public final int DEBUG = 2; //most verbose

	static private Logger instance; //Singleton

	public static Logger getInstance()
	{
		if(instance == null)
			instance = new Logger();
		return instance;
	}
	
	public static void dispose()
	{
		instance = null;
	}

	//DYNAMIC--------------------------------------------------------
	private int level; //current level
	private CyclicQueue lineBuffer;  //the last X lines
	private NTClient ntClient;
	private FileWriter logFileWriter = null;
	private String logFilePath = null;
	private NTWebAPI ntWebAPI = null;

	private Logger()
	{
		level = INFORMATION; //default for production versions
		enableLogBuffer(DEFAULT_CAPACITY);
	}

	public void enableLogBuffer(int capacity)
	{
		lineBuffer = new CyclicQueue(capacity);
	}
	
	public void disableLogBuffer()
	{
		lineBuffer = null;
	}
	
	public void setNTClient(NTClient ntClient)
	{
		this.ntClient = ntClient;
	}
	
	public int getLevel()
	{
		return level;
	}
	
	public void setLevel(int level)
	{
		this.level = level;
	}

	public boolean isFileModeActive()
	{
		return(logFileWriter != null);
	}
	
	public void dumpCrashLog(String folderPath) throws Exception
	{
		if(ntClient == null)
			throw new NullPointerException("NTClient cannot be null (Logger.dumpCrashLog())");
		try
		{
			logFileWriter = ntClient.getFileWriter(folderPath + "Crash_" + StringUtils.formatDateTime(System.currentTimeMillis(), "-", "", "T") + ".log");
			logFileWriter.open(FileIO.FILE_EXISTS_STRATEGY_REPLACE, FileIO.FILE_DOES_NOT_EXIST_STRATEGY_CREATE);
			debug("Dumping crash log...");
			logFileWriter.write(getBuffer(true));
			logFileWriter.close();
			logFileWriter.dispose();
		}
		catch(Exception ignore) { }
		finally
		{
			logFileWriter = null;
		}
	}
	
	public void enableFileMode()
	{
		if(!isFileModeActive())
		{
			if(ntClient == null)
				throw new NullPointerException("NTClient cannot be null (Logger.enableFileMode())");
			try
			{
				String folderPath = ntClient.getPreferences().getDataFolderPath();
				if(folderPath == null)
					throw new Exception("No accessible data folder");	
				logFilePath = folderPath + "Debug_";
				if(ntClient.isRestartingModeEnabled())
				{					
					logFilePath += "RUNNING.log"; //will be renamed after last run
					logFileWriter = ntClient.getFileWriter(logFilePath);
					if(ntClient.isFirstRun() && logFileWriter.fileExists())
					{	//This is a log file of an older unresumed running session, let's close it:
						debug("Found unresumed \"RUNNING\" log file, renaming");
						long lastChanged = logFileWriter.fileLastChanged();
						logFileWriter.open(FileIO.FILE_EXISTS_STRATEGY_APPEND, FileIO.FILE_DOES_NOT_EXIST_STRATEGY_CREATE);
						logFileWriter.writeLine("[LATER: " + StringUtils.formatDateTime(System.currentTimeMillis(), "/", ":", " ") + "] Session was never resumed, closing.");
						logFileWriter.rename("Debug_" + StringUtils.formatDateTime(lastChanged, "-", "", "T") + ".log", FileIO.FILE_EXISTS_STRATEGY_REPLACE);
						logFileWriter.dispose(); //also calls close()
						//Again take the (new) log file:
						logFileWriter = ntClient.getFileWriter(logFilePath);
					}
					logFileWriter.open(FileIO.FILE_EXISTS_STRATEGY_APPEND, FileIO.FILE_DOES_NOT_EXIST_STRATEGY_CREATE);
				}
				else
				{
					logFilePath += StringUtils.formatDateTime(System.currentTimeMillis(), "-", "", "T") + ".log";
					logFileWriter = ntClient.getFileWriter(logFilePath);
					logFileWriter.open(FileIO.FILE_EXISTS_STRATEGY_CREATE_RENAMED_FILE, FileIO.FILE_DOES_NOT_EXIST_STRATEGY_CREATE);
				}
				//write log buffer:
				logFileWriter.write(getBuffer(true));
				//debug("Logger file mode enabled");
			}
			catch(Exception e)
			{
				disableFileMode();
				error(e, "Failed to enable file mode in Logger");
			}
		}
	}

	public void disableFileMode()
	{
		if(isFileModeActive())
		{
			try
			{
				logFileWriter.close();
				if(ntClient.isRestartingModeEnabled() && ntClient.isLastRun())
					logFileWriter.rename("Debug_" + StringUtils.formatDateTime(System.currentTimeMillis(), "-", "", "T") + ".log", FileIO.FILE_EXISTS_STRATEGY_CREATE_RENAMED_FILE); //rename file to seal it
				logFileWriter.dispose();
			}
			catch(Exception ignore) {}
			finally
			{
				logFileWriter = null;
				logFilePath = null;
			}
		}
	}
	
	public void enableWebMode(NTAccount account)
	{
		ntWebAPI = new NTWebAPI(account);
		try
		{
			ntWebAPI.postLog(getBuffer(true));
		}
		catch(Exception e)
		{
			ntWebAPI = null; //disable web mode
		}
	}

	/**
	 * @return the logFilePath
	 */
	public String getLogFilePath()
	{
		return logFilePath;
	}

	public void debug(String msg)
	{
		if(level >= DEBUG)
			save(/* "Debug: " + */msg);
	}

	public void info(String msg)
	{
		if(level >= INFORMATION)
			save(msg);
	}

	public void error(String msg)
	{
		//if(level >= ERROR)
		save("Error: " + msg);
	}

	public void error(Exception e, String comment)
	{
		//if(level >= ERROR)
		//{
		save("Exception: " + e.getMessage() + " (" + comment + ")");
		if(NTClient.ENVIRONMENT != NTClient.PHONE_PROD_ENV)
			e.printStackTrace();
		if(ntClient != null)
		{
			String info = ntClient.additionalErrorReporting(e);
			if(info != null)
				save("Additional info: " + info);
		}
		//}
	}

	public void save(String msg)
	{

		if(NTClient.ENVIRONMENT != NTClient.PHONE_PROD_ENV)
			System.out.println(msg);
		LogEntry entry = new LogEntry(msg);
		if(lineBuffer != null)
			lineBuffer.offer(entry);
		if(isFileModeActive())
		{
			synchronized(logFileWriter)
			{
				try
				{
					logFileWriter.write(entry.toString() + "\n");
				}
				catch(Exception e)
				{
					disableFileMode();
				}	
			}
		}
		if(ntWebAPI != null)
		{
			try
			{
				ntWebAPI.postLog(entry.toString() + "\n");
			}
			catch(Exception e)
			{
				ntWebAPI = null; //disable web mode
			}
		}
	}

	public String getBuffer()
	{
		return getBuffer(false);
	}

	/**
	 * @return log buffer contents as String
	 */
	public String getBuffer(boolean withTimeStamps)
	{
		if(lineBuffer == null)
			return null;
		StringBuffer buf = new StringBuffer();
		Enumeration lines = lineBuffer.getElements();
		while(lines.hasMoreElements())
				buf.append((withTimeStamps ? ((LogEntry) lines.nextElement()).toString() : ((LogEntry) lines.nextElement()).getMessage()) + "\n");
		return buf.toString();
	}

	/**
	 * @author mstevens
	 *
	 */
	public class LogEntry
	{
		private long timeStamp;
		private String msg;

		public LogEntry(String msg)
		{
			this.timeStamp = System.currentTimeMillis();
			this.msg = msg;
		}

		/**
		 * @return the timeStamp
		 */
		public long getTimeStamp()
		{
			return timeStamp;
		}

		/**
		 * @return the msg
		 */
		public String getMessage()
		{
			return msg;
		}

		public String toString()
		{
			return "[" + StringUtils.formatDateTime(timeStamp, "/", ":", " ") + "] " + msg;
		}

	}

}
