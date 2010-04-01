/**
 * com.sony.csl.j2me.io
 *  
 * Description:
 *   Utilities for J2ME CLDC/MIDP applications
 *   
 *   This code was developed within the scope of the NoiseTube project at
 *   Sony Computer Science Laboratory (CSL) Paris, for more information please refer to: 
 *     - http://noisetube.net
 *     - http://code.google.com/p/noisetube
 *     - http://www.csl.sony.fr
 * 
 * Author:
 *   Matthias Stevens (Sony CSL Paris / Vrije Universiteit Brussel)
 *   Contact: matthias.stevens@gmail.com
 * 	
 * License: 
 *   Copyright 2008 Matthias Stevens (Sony CSL Paris / Vrije Universiteit Brussel)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *          http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package noisetube.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import noisetube.config.Device;
import noisetube.util.StringUtils;

/**
 * A class with FileAccess routines
 * 
 * @author mstevens
 * 
 */
public class FileAccess
{

	// Strategies for opening FileConnection on an existing file:
	static final public int FILE_EXISTS_STRATEGY_OK = 0;
	static final public int FILE_EXISTS_STRATEGY_REJECT = 1;
	static final public int FILE_EXISTS_STRATEGY_CREATE_RENAMED_FILE = 2;
	static final public int FILE_EXISTS_STRATEGY_APPEND = 3;
	static final public int FILE_EXISTS_STRATEGY_TRUNCATE = 4;
	// Strategies for opening FileConnection on a non-existing file:
	static final public int FILE_DOES_NOT_EXIST_STRATEGY_REJECT = 1;
	static final public int FILE_DOES_NOT_EXIST_STRATEGY_CREATE = 2;

	// Store files & folders that are known to exist in a hashtable:
	// static private Hashtable knownFilesAndFolders = new Hashtable();

	// static private Logger log = Logger.getInstance();

	static public String addTrailingSeparatorToFolderPath(String folderPath)
	{
		return folderPath
				+ ((StringUtils.right(folderPath, 1).charAt(0) != Device
						.getFileSeparator()) ? Device.getFileSeparator() + ""
						: "");
	}

	static public String addFilePathPrefix(String path)
	{
		return (((path.length() <= 8) || !path.substring(0, 8).equals(
				"file:///")) ? "file:///" : "")
				+ path;
	}

	static public boolean isFolderPath(String fullPath)
	{
		return fullPath.charAt(fullPath.length() - 1) == Device
				.getFileSeparator();
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
			throw new IllegalArgumentException("This (" + fullFilePath
					+ ") is not a valid file path");
	}

	static public String trimFileExtensionAndDot(String fullFilePath)
	{
		if(isFilePath(fullFilePath))
		{
			int extensionDotIdx = fullFilePath.lastIndexOf('.');
			return fullFilePath.substring(0, extensionDotIdx);
		}
		else
			throw new IllegalArgumentException("This (" + fullFilePath
					+ ") is not a valid file path");
	}

	static public boolean isRootPath(String fullPath)
	{
		return !(StringUtils.countOccurences(fullPath.substring(8), Device
				.getFileSeparator()) > 1);
	}

	static public String getContainingFolderPath(String fullPath)
	{
		if(!isRootPath(fullPath))
		{
			String fullPathToContainingFolder;
			if(isFilePath(fullPath))
				// it is a file:
				fullPathToContainingFolder = StringUtils.left(fullPath,
						fullPath.lastIndexOf('/') + 1);
			else
				// it is a folder:
				fullPathToContainingFolder = fullPath.substring(0, StringUtils
						.left(fullPath, fullPath.length() - 1).lastIndexOf(
								Device.getFileSeparator()) + 1);
			return fullPathToContainingFolder;
		}
		return null; // root has no containing folder
	}

	// static private void newKnownFileOrFolder(String fullPath)
	// {
	// knownFilesAndFolders.put(fullPath, Boolean.TRUE);
	// String fullPathToContainingFolder = getContainingFolderPath(fullPath);
	// if(fullPathToContainingFolder != null)
	// newKnownFileOrFolder(fullPathToContainingFolder); //recursive!
	// }
	//	
	// static private boolean isKnownFileOrFolder(String fullPath)
	// {
	// return knownFilesAndFolders.containsKey(fullPath);
	// }

	static public boolean doesFileOrFolderExist(String fullPath)
	{
		if(fullPath == null)
			throw new NullPointerException();
		fullPath = addFilePathPrefix(fullPath);
		// if(isKnownFileOrFolder(fullPath))
		// return true;
		boolean exists = false;
		try
		{
			FileConnection fc = (FileConnection) Connector.open(fullPath,
					Connector.READ);
			exists = fc.exists();
			fc.close();
		}
		catch(Exception ex)
		{ // this probably means that the file/folder is not there, or otherwise
			// not accessible
			exists = false;
		}
		// if(exists)
		// newKnownFileOrFolder(fullPath);
		return exists;
	}

	static private FileConnection getFolderConnection(String fullPath,
			boolean attemptCreation)
	{
		if(fullPath == null)
			throw new NullPointerException();
		// fullPath =
		// addFilePathPrefix(addTrailingSeparatorToFolderPath(fullPath));
		boolean exists = false;
		try
		{
			FileConnection fc = (FileConnection) Connector.open(fullPath,
					Connector.READ_WRITE);
			if(fc.exists())
				exists = true;
			else if(attemptCreation && !isRootPath(fullPath)) // Trying to
																// create the
																// root makes no
																// sense
			{
				if(doesFolderExist(getContainingFolderPath(fullPath), true)) // check
																				// if
																				// containing
																				// folder/root
																				// exists
																				// (and
																				// try
																				// to
																				// create
																				// it
																				// if
																				// not)
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
		{ // this probably means that the file/folder is not there, or otherwise
			// not accessible
			return null;
		}
		return null;
	}

	static public boolean doesFolderExist(String fullPath,
			boolean attemptCreation)
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

	static public boolean canWriteToFolder(String fullPath,
			boolean attemptCreation)
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

	static public OutputStreamWriter getFileWriter(String fullPath,
			int fileExistsStrategy, int fileDoesNotExistStrategy)
			throws IllegalArgumentException, Exception
	{
		return getFileWriter(fullPath, fileExistsStrategy,
				fileDoesNotExistStrategy, null);
	}

	static public OutputStreamWriter getFileWriter(String fullPath,
			int fileExistsStrategy, int fileDoesNotExistStrategy,
			String characterEncoding) throws IllegalArgumentException,
			Exception
	{
		if(fullPath == null)
			throw new NullPointerException();
		fullPath = addFilePathPrefix(fullPath);
		if(!isFilePath(fullPath))
			throw new Exception("Not a valid file path (" + fullPath + ")");
		if(fileExistsStrategy < 0 || fileExistsStrategy > 4)
			throw new IllegalArgumentException("Invalid file exists strategy");
		if(fileDoesNotExistStrategy < 1 || fileDoesNotExistStrategy > 2)
			throw new IllegalArgumentException(
					"Invalid file does not exist strategy");
		FileConnection fc = null;
		OutputStreamWriter fileWriter = null;
		boolean seekToEOF = false;
		try
		{
			// boolean knownToExist = false;
			// if(isKnownFileOrFolder(fullPath))
			// knownToExist = true;
			// if(!knownToExist)
			fc = (FileConnection) Connector
					.open(fullPath, Connector.READ_WRITE);
			if(/* knownToExist || */fc.exists())
			{ // file already exists
				// if(!knownToExist)
				// newKnownFileOrFolder(fullPath); //remember that this file
				// exists
				switch(fileExistsStrategy)
				{
				case (FILE_EXISTS_STRATEGY_OK):
					if(fc == null)
						fc = (FileConnection) Connector.open(fullPath,
								Connector.READ_WRITE);
					break;
				case (FILE_EXISTS_STRATEGY_REJECT):
					if(fc != null)
					{
						fc.close();
						fc = null;
					}
					break;
				case (FILE_EXISTS_STRATEGY_CREATE_RENAMED_FILE):
					// find a filename that does not exist yet (by adding a
					// counter):
					String extension = getFileExtension(fullPath);
					String pathWithoutExtension = trimFileExtensionAndDot(fullPath);
					int i = 1; // counter
					do
					{
						fullPath = pathWithoutExtension + "-" + i + "."
								+ extension;
						if(fc != null)
							fc.close();
						fc = (FileConnection) Connector.open(fullPath,
								Connector.READ_WRITE);
						// if(fc.exists())
						// newKnownFileOrFolder(fullPath); //remember that this
						// file exists
						i++;
					}
					while(fc.exists()); // try until non-existing file found
					fc.create(); // then create that file
					// newKnownFileOrFolder(fullPath); //remember that this file
					// exists now
					break;
				case (FILE_EXISTS_STRATEGY_APPEND):
					if(fc == null)
						fc = (FileConnection) Connector.open(fullPath,
								Connector.READ_WRITE);
					seekToEOF = true; // !!!
					break;
				case (FILE_EXISTS_STRATEGY_TRUNCATE):
					if(fc == null)
						fc = (FileConnection) Connector.open(fullPath,
								Connector.READ_WRITE);
					fc.truncate(0);
					break;
				}
			}
			else
			{ // file does not exist
				switch(fileDoesNotExistStrategy)
				{
				case (FILE_DOES_NOT_EXIST_STRATEGY_REJECT):
					if(fc != null)
					{
						fc.close();
						fc = null;
					}
					break;
				case (FILE_DOES_NOT_EXIST_STRATEGY_CREATE):
					if(fc == null)
						fc = (FileConnection) Connector.open(fullPath,
								Connector.READ_WRITE);
					if(doesFolderExist(getContainingFolderPath(fullPath), true)) // check
																					// if
																					// containing
																					// folder
																					// exists,
																					// attempt
																					// creation
																					// if
																					// needed
						fc.create();
					else
						throw new Exception(
								"Containing folder does not exist and could not be created");
					// newKnownFileOrFolder(fullPath); //remember that this file
					// exists now
					break;
				}
			}
			if(fc != null)
			{
				OutputStream outputStream = (seekToEOF ? fc.openOutputStream(fc
						.fileSize()) /* seek to EOF to append... */: fc
						.openOutputStream());
				fileWriter = ((characterEncoding == null || characterEncoding
						.equals("")) ? new OutputStreamWriter(outputStream)
						: new OutputStreamWriter(outputStream,
								characterEncoding));
			}
		}
		catch(Exception e)
		{
			try
			{
				if(fileWriter != null)
					fileWriter.close();
				if(fc != null)
					fc.close();
			}
			catch(Exception ex)
			{
			}
			fileWriter = null;
		}
		return fileWriter;
	}

	static public boolean isValidFileName(String filename)
	{
		boolean valid = true;
		if(filename.indexOf("*") != -1)
			valid = false;
		if(filename.indexOf("?") != -1)
			valid = false;
		if(filename.indexOf("<") != -1)
			valid = false;
		if(filename.indexOf(">") != -1)
			valid = false;
		if(filename.indexOf(":") != -1)
			valid = false;
		if(filename.indexOf("\"") != -1)
			valid = false;
		if(filename.indexOf("\\") != -1)
			valid = false;
		if(filename.indexOf("/") != -1)
			valid = false;
		if(filename.indexOf("|") != -1)
			valid = false;
		if(filename.indexOf("\n") != -1)
			valid = false;
		if(filename.indexOf("\t") != -1)
			valid = false;
		return valid;
	}

	static public String makeValidFileName(String filename)
	{
		if(filename != null)
		{
			filename = filename.replace('*', '+');
			filename = filename.replace('?', '_');
			filename = filename.replace('<', '(');
			filename = filename.replace('>', ')');
			filename = filename.replace(':', '-');
			filename = filename.replace('"', '\'');
			filename = filename.replace('\\', '_');
			filename = filename.replace('/', '_');
			filename = filename.replace('|', ';');
			filename = filename.replace('\n', '_');
			filename = filename.replace('\t', '_');
		}
		return filename;
	}

}
