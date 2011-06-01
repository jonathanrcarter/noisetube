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

package net.noisetube.io;

import java.io.IOException;
import java.io.OutputStreamWriter;

import net.noisetube.util.Logger;

/**
 * Platform independent abstract textual file writer class
 * 
 * @author mstevens
 *
 */
public abstract class FileWriter
{

	protected String fullPath;
	protected String characterEncoding = null;
	
	protected OutputStreamWriter writer = null;
	
	public FileWriter(String fullPath)
	{
		this(fullPath, null);
	}
	
	public FileWriter(String fullPath, String characterEncoding)
	{
		this.fullPath = fullPath;
		this.characterEncoding = characterEncoding;
	}
	
	public abstract void open(int fileExistsStrategy, int fileDoesNotExistStrategy) throws Exception;
	
	public boolean isWritable()
	{
		return (writer != null);
	}
	
	public void close()
	{
		if(writer != null)
		{
			try
			{
				writer.close();
			}
			catch(IOException ignore) {}
			finally
			{
				writer = null;
			}
		}
	}
	
	public abstract void dispose();
	
	/**
	 * Closes the FileWriter if it is open
	 * 
	 * @param newName
	 * @param fileExistsStrategy
	 * @throws Exception
	 */
	public abstract void rename(String newName, int fileExistsStrategy) throws Exception;
	
	public abstract boolean fileExists();
	
	public abstract long fileLastChanged();
	
	public void write(char charToWrite)
	{
		if(writer != null)
		{
			try
			{
				writer.write(charToWrite);
				writer.flush();
			}
			catch(Exception e)
			{
				Logger.getInstance().error(e, "Could not write to file");
				close();
			}
		}			
	}
	
	public void write(String stringToWrite)
	{
		if(writer != null)
		{
			try
			{
				writer.write(stringToWrite);
				writer.flush();
			}
			catch(Exception e)
			{
				Logger.getInstance().error(e, "Could not write to file");
				close();
			}
		}	
	}
	
	public void writeLine(String stringToWrite)
	{
		write(stringToWrite + "\n");
	}
	
	public abstract String getContainingFolderPath();

	public abstract String getFileName();
	
	/**
	 * @return the fullPath
	 */
	public String getFullPath()
	{
		return fullPath;
	}

	/**
	 * @return the characterEncoding
	 */
	public String getCharacterEncoding()
	{
		return characterEncoding;
	}
	
}
