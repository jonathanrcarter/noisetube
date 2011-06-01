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

package net.noisetube.io.javame;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.noisetube.io.FileIO;
import net.noisetube.io.FileWriter;
import net.noisetube.util.Logger;


public class JavaMEFileWriter extends FileWriter
{

	private FileConnection fileConnection = null;
	
	
	public JavaMEFileWriter(String fullPath)
	{
		this(fullPath, null); //will use system default char encoding
	}
	
	public JavaMEFileWriter(String fullPath, String characterEncoding)
	{
		super(fullPath, characterEncoding);
		try
		{
			fileConnection = (FileConnection) Connector.open(fullPath, Connector.READ_WRITE);
		}
		catch(IOException e)
		{
			Logger.getInstance().error(e, "Could not open file connection");
		}
	}
	
	public void open(int fileExistsStrategy, int fileDoesNotExistStrategy) throws Exception
	{
		if(fullPath == null)
			throw new NullPointerException("fullPath cannot be null");
		fullPath = FileAccess.addFilePathPrefix(fullPath);
		if(!FileAccess.isFilePath(fullPath))
			throw new Exception("Not a valid file path (" + fullPath + ")");
		if(fileExistsStrategy < 0 || fileExistsStrategy > 4)
			throw new IllegalArgumentException("Invalid file exists strategy");
		if(fileDoesNotExistStrategy < 1 || fileDoesNotExistStrategy > 2)
			throw new IllegalArgumentException("Invalid file does not exist strategy");
		boolean seekToEOF = false;
		if(fileConnection.exists())
		{   //file already exists
			switch(fileExistsStrategy)
			{
				case FileIO.FILE_EXISTS_STRATEGY_REPLACE :
					break;
				case FileIO.FILE_EXISTS_STRATEGY_REJECT :
					fileConnection.close();
					fileConnection = null;
					break;
				case FileIO.FILE_EXISTS_STRATEGY_CREATE_RENAMED_FILE :
					//find a filename that does not exist yet (by adding a counter):
					String extension = FileAccess.getFileExtension(fullPath);
					String pathWithoutExtension = FileAccess.trimFileExtensionAndDot(fullPath);
					int i = 1; //counter
					do
					{
						fullPath = pathWithoutExtension + "-" + i + "."	+ extension;
						fileConnection.close();
						fileConnection = (FileConnection) Connector.open(fullPath, Connector.READ_WRITE);
						i++;
					}
					while(fileConnection.exists()); //try until non-existing file found
					fileConnection.create(); //then create that file
					break;
				/*case FileIO.FILE_EXISTS_STRATEGY_RENAME_EXISTING_FILE : 
					break; */
				case FileIO.FILE_EXISTS_STRATEGY_APPEND :
					seekToEOF = true; //!!!
					break;
				case FileIO.FILE_EXISTS_STRATEGY_TRUNCATE :
					fileConnection.truncate(0);
					break;
			}
		}
		else
		{   //file does not exist
			switch(fileDoesNotExistStrategy)
			{
				case FileIO.FILE_DOES_NOT_EXIST_STRATEGY_REJECT :
					fileConnection.close();
					fileConnection = null;
					break;
				case  FileIO.FILE_DOES_NOT_EXIST_STRATEGY_CREATE :
					//check if containing folder exists, attempt creation if needed
					if(FileAccess.doesFolderExist(FileAccess.getContainingFolderPath(fullPath), true))
						fileConnection.create();
					else
						throw new Exception("Containing folder does not exist and could not be created");
					//newKnownFileOrFolder(fullPath); //remember that this file exists now
					break;
			}
		}
		if(fileConnection != null)
		{
			OutputStream outputStream = (seekToEOF ? fileConnection.openOutputStream(fileConnection.fileSize()) /* seek to EOF to append... */: fileConnection.openOutputStream());
			writer = ((characterEncoding == null || characterEncoding.equals("")) ? new OutputStreamWriter(outputStream) : new OutputStreamWriter(outputStream, characterEncoding));
		}
		else
			throw new Exception("Could not open JavaMEFileWriter");
	}

	public void dispose()
	{
		close();
		try
		{
			fileConnection.close();
		}
		catch(IOException ignore) {}
		finally
		{
			fileConnection = null;
		}
	}
	
	public boolean fileExists()
	{
		if(fileConnection != null)
			return fileConnection.exists();
		else
			throw new IllegalStateException("File connection is null, don't use this FileWriter");
	}
	
	public long fileLastChanged()
	{
		if(fileConnection != null)
			return fileConnection.lastModified();
		else
			throw new IllegalStateException("File connection is null, don't use this FileWriter");
	}

	/**
	 * Closes the FileWriter if it is open
	 * 
	 * @see net.noisetube.io.FileWriter#rename(java.lang.String, int)
	 */
	public void rename(String newName, int fileExistsStrategy) throws Exception
	{	//TODO use fileExistsStrategy
		if(writer != null)
			close();
		fileConnection.rename(newName);
		fullPath = fileConnection.getPath() + newName;
	}

	public String getContainingFolderPath()
	{
		if(fileConnection != null)
			return fileConnection.getPath();
		else
			return null;
	}

	public String getFileName()
	{
		if(fileConnection != null)
			return fileConnection.getName();
		else
			return null;
	}

}
