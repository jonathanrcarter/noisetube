/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Java ME version)
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

package net.noisetube.core.javame.restarting;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.noisetube.util.Logger;
import net.noisetube.util.StringUtils;

/**
 * @author mstevens
 *
 */
public class RunState
{

	public static final int NOT_SET = -1;
	
	protected static final String STATE_FILE_NAME = "running";
	protected static final char SEPARATOR = '|';

	protected IRunStateOwner owner;	
	protected int runCount = 1;
	protected long startTime;
	protected long stopTime = NOT_SET; //running
	
	
	public RunState(IRunStateOwner owner) //default for first run
	{
		this.startTime = System.currentTimeMillis();
		this.runCount = 1;
		this.owner = owner;
	}
	
	public RunState(IRunStateOwner owner, String state)
	{
		this.owner = owner;
		String[] parts = StringUtils.split(state, SEPARATOR);
		if(parts == null || parts.length < 3)
			throw new IllegalArgumentException("Invalid runstate string: " + state);
		this.runCount = Integer.parseInt(parts[0]);
		this.startTime = Long.parseLong(parts[1]);
		this.stopTime = Long.parseLong(parts[2]);
	}
	
	public String toString()
	{
		return 	Integer.toString(runCount) + SEPARATOR +
				Long.toString(startTime) + SEPARATOR +
				Long.toString(stopTime);
	}
	
	public String prettyPrint()
	{
		return 	"\n - Runs: " + Integer.toString(runCount) +
				"\n - Starttime: " + StringUtils.formatDateTime(startTime, "/", ":", " ") +
				"\n - Stoptime: " + (stopTime > -1 ? StringUtils.formatDateTime(stopTime, "/", ":", " ") : Long.toString(stopTime));
	}

	public static RunState load(IRunStateOwner midlet)
	{
		RunState runState = null;
		String filePath = midlet.getFolderPathForRunstateFile() + STATE_FILE_NAME;
		try
		{
			FileConnection fc = (FileConnection) Connector.open(filePath, Connector.READ_WRITE);
			if(fc.exists())
			{
				InputStreamReader reader = new InputStreamReader(fc.openInputStream());
				StringBuffer bff = new StringBuffer();
				for(int c = reader.read(); c != -1; c = reader.read())
					bff.append((char) c);
				if(bff.length() > 0)
					runState = midlet.deserialiseRunState(bff.toString());
				fc.delete(); //delete the file!
				fc.close();
			}
		}
		catch(Exception e)
		{
			(Logger.getInstance()).debug("Could not load runstate: " + e.getMessage());
		}
		return runState;
	}
	
	public void save() //shorthand
	{
		RunState.save(owner, this);
	}
	
	public static void save(IRunStateOwner midlet, RunState runState)
	{
		String filePath = midlet.getFolderPathForRunstateFile() + STATE_FILE_NAME;
		try
		{
			FileConnection fc = (FileConnection) Connector.open(filePath, Connector.READ_WRITE);
			if(fc.exists())
				fc.truncate(0); //empty out the existing file
			else
				fc.create(); //create file
			OutputStreamWriter writer = new OutputStreamWriter(fc.openOutputStream());
			writer.write(runState.toString());
			writer.flush();
			writer.close();
		}
		catch(Exception e)
		{
			(Logger.getInstance()).debug("Could not save run state: " + e.getMessage());
		}		
	}
	/**
	 * @return the runCount
	 */
	public int getRunCount()
	{
		return runCount;
	}

	public void incrementRunCount()
	{
		this.runCount++;
	}

	/**
	 * @return the stopTime
	 */
	public long getStopTime()
	{
		return stopTime;
	}

	/**
	 * @param stopTime the stopTime to set
	 */
	public void setStopTime(long stopTime)
	{
		this.stopTime = stopTime;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime()
	{
		return startTime;
	}
	
}
