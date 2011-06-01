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

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.noisetube.util.StringUtils;

/**
 * A class with FileAccess routines
 * 
 * @author mstevens
 * 
 */
public class FileAccess
{
	
	static private Character directorySeparator;

	/**
	 * @return the directory separator (e.g.: \ on Windows, / on Unix)
	 */
	static public char getDirectorySeparator()
	{
		if(directorySeparator == null)
		{
			String sep = System.getProperty("file.separator");
			directorySeparator = new Character((sep != null && !sep.equals("")) ? sep.charAt(0) : '/');
		}		
		return directorySeparator.charValue();
	}
	
	static public String addTrailingSeparatorToFolderPath(String folderPath)
	{
		return folderPath + ((StringUtils.right(folderPath, 1).charAt(0) != getDirectorySeparator()) ? getDirectorySeparator() + "" : "");
	}

	static public String addFilePathPrefix(String path)
	{
		return (((path.length() <= 8) || !path.substring(0, 8).equals("file:///")) ? "file:///" : "") + path;
	}

	static public boolean isFolderPath(String fullPath)
	{
		return fullPath.charAt(fullPath.length() - 1) == getDirectorySeparator();
	}

	static public boolean isFilePath(String fullPath)
	{
		return !isFolderPath(fullPath);
	}

	static public String getFileExtension(String fullFilePath)
	{
		if(isFilePath(fullFilePath))
		{
			int extensionDotIdx = fullFilePath.lastIndexOf('.');
			return fullFilePath.substring(extensionDotIdx + 1);
		}
		else
			throw new IllegalArgumentException("This (" + fullFilePath + ") is not a valid file path");
	}

	static public String trimFileExtensionAndDot(String fullFilePath)
	{
		if(isFilePath(fullFilePath))
		{
			int extensionDotIdx = fullFilePath.lastIndexOf('.');
			return fullFilePath.substring(0, extensionDotIdx);
		}
		else
			throw new IllegalArgumentException("This (" + fullFilePath + ") is not a valid file path");
	}

	static public boolean isRootPath(String fullPath)
	{
		return StringUtils.countOccurences(fullPath.substring(8), getDirectorySeparator()) <= 1;
	}

	static public String getContainingFolderPath(String fullPath)
	{
		if(!isRootPath(fullPath))
		{
			String fullPathToContainingFolder;
			if(isFilePath(fullPath))
				//it is a file:
				fullPathToContainingFolder = StringUtils.left(fullPath, fullPath.lastIndexOf(getDirectorySeparator()) + 1);
			else
				//it is a folder:
				fullPathToContainingFolder = fullPath.substring(0, StringUtils.left(fullPath, fullPath.length() - 1).lastIndexOf(getDirectorySeparator()) + 1);
			return fullPathToContainingFolder;
		}
		return null; //root has no containing folder
	}

	static public boolean doesFileOrFolderExist(String fullPath)
	{
		if(fullPath == null)
			throw new NullPointerException();
		fullPath = addFilePathPrefix(fullPath);
		boolean exists = false;
		try
		{
			FileConnection fc = (FileConnection) Connector.open(fullPath, Connector.READ);
			exists = fc.exists();
			fc.close();
		}
		catch(Exception ex)
		{   //this probably means that the file/folder is not there, or otherwise not accessible
			exists = false;
		}
		return exists;
	}

	static private FileConnection getFolderConnection(String fullPath, boolean attemptCreation)
	{
		if(fullPath == null)
			throw new NullPointerException();
		boolean exists = false;
		try
		{
			FileConnection fc = (FileConnection) Connector.open(fullPath,
					Connector.READ_WRITE);
			if(fc.exists())
				exists = true;
			else if(attemptCreation && !isRootPath(fullPath)) //Trying to create the root makes no sense
			{
				//check if containing folder/root exists (and try to create it if it does not)
				if(doesFolderExist(getContainingFolderPath(fullPath), true)) 
				{
					try
					{
						fc.mkdir();
						exists = true;
					}
					catch(Exception e)
					{
						exists = false;
					}
				}
				else
					exists = false;
			}
			else
				exists = false;
			if(exists)
				return fc;
		}
		catch(Exception e)
		{   //this probably means that the file/folder is not there, or otherwise not accessible
			return null;
		}
		return null;
	}

	static public boolean doesFolderExist(String fullPath, boolean attemptCreation)
	{
		FileConnection fc = getFolderConnection(fullPath, attemptCreation);
		if(fc != null)
		{
			try
			{
				fc.close();
			}
			catch(IOException e)
			{
			}
			return true;
		}
		return false;
	}

	static public boolean canWriteToFolder(String fullPath, boolean attemptCreation)
	{
		boolean canWrite = false;
		FileConnection fc = getFolderConnection(fullPath, attemptCreation);
		if(fc != null)
		{
			canWrite = fc.canWrite();
			try
			{
				fc.close();
			}
			catch(IOException e)
			{
			}
		}
		return canWrite;
	}

}
