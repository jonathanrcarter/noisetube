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

package noisetube.util;

import java.io.OutputStreamWriter;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import noisetube.MainMidlet;

/**
 * Logger
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class Logger
{

	// STATIC---------------------------------------------------------
	static public final int DEFAULT_CAPACITY = 30;

	static public int INFORMATION = 0;
	static public int ERROR = 1;
	static public int DEBUG = 2;

	static Logger instance = new Logger(DEFAULT_CAPACITY); //Singleton

	public static Logger getInstance()
	{
		return instance;
	}

	// DYNAMIC--------------------------------------------------------
	private int level = INFORMATION; //current level
	private CyclicQueue lineBuffer;  //the last X lines saved
	private OutputStreamWriter logFileWriter = null;
	private String logFilePath = null;

	private Logger(int capacity)
	{
		lineBuffer = new CyclicQueue(capacity);
		if(MainMidlet.CLIENT_IS_TEST_VERSION || MainMidlet.environment == MainMidlet.EMULATOR_ENV)
			level = DEBUG;
		else
			level = ERROR; //default even for production versions
	}

	public int getLevel()
	{
		return level;
	}

	public boolean isDebug()
	{
		return getLevel() == Logger.DEBUG;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

	public boolean isFileModeActive()
	{
		return(logFileWriter != null);
	}

	public void enableFileMode()
	{
		if(!isFileModeActive())
		{
			try
			{
				String folderPath = MainMidlet.getInstance().getPreferences().getDataFolderPath();
				if(folderPath == null)
					throw new Exception("No accessible data folder");
				logFilePath = folderPath + "Debug_" + StringUtils.formatDateTime(System.currentTimeMillis(), "-", "", "T") + ".log";
				//debug("Trying to enable Logger file mode (file: " + logFilePath);
				FileConnection fc = (FileConnection) Connector.open(logFilePath, Connector.WRITE);
				fc.create();
				logFileWriter = new OutputStreamWriter(fc.openOutputStream());
				// write log buffer:
				logFileWriter.write(getBuffer(true));
				logFileWriter.flush();
				//debug("Logger file mode enabled");
			}
			catch(Exception e)
			{
				disableFileMode();
				debug("Failed to enable file mode in Logger");
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
			}
			catch(Exception ignore)
			{
			}
			finally
			{
				logFileWriter = null;
				logFilePath = null;
			}
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
		//if(level >= INFORMATION)
		save(msg);
	}

	public void error(String msg)
	{
		if(level >= ERROR)
			save("Error: " + msg);
	}

	public void error(Exception e, String comment)
	{
		if(level >= ERROR)
		{
			save("Exception: " + e.getMessage() + " (" + comment + ")");
			if(MainMidlet.environment == MainMidlet.EMULATOR_ENV)
				e.printStackTrace();
		}
	}

	public void save(String msg)
	{
		if(MainMidlet.environment == MainMidlet.EMULATOR_ENV)
			System.out.println(msg);
		LogEntry entry = new LogEntry(msg);
		lineBuffer.push(entry);
		if(isFileModeActive())
		{
			try
			{
				logFileWriter.write(entry.toString() + "\n");
				logFileWriter.flush();
			}
			catch(Exception e)
			{
				disableFileMode();
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
		StringBuffer buf = new StringBuffer();
		if(withTimeStamps)
			for(int i = 0; i < lineBuffer.getSize(); i++)
				buf.append(((LogEntry) lineBuffer.get(i)).toString() + "\n");
		else
			for(int i = 0; i < lineBuffer.getSize(); i++)
				buf.append(((LogEntry) lineBuffer.get(i)).getMessage() + "\n");
		return buf.toString();
	}

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
