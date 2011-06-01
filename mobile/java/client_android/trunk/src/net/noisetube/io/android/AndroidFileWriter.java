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

package net.noisetube.io.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import net.noisetube.io.FileIO;
import net.noisetube.io.FileWriter;
/**
 * This class is an Android specific implementation of the FileWriter abstract class.
 * 
 * @author sbarthol
 *
 */
public class AndroidFileWriter extends FileWriter
{

	private File folder;
	private File file = null;

	public AndroidFileWriter(String fullPath)
	{
		this(fullPath, null); //will use system default char encoding
	}
	
	/**
	 * We create this writer, providing the full filepath (e.g. "/my/path/myfile.raw")
	 * @param fullPath The full filepath (e.g. "/my/path/myfile.raw")
	 */
	public AndroidFileWriter(String fullPath, String characterEncoding)
	{
		super(fullPath, characterEncoding);
		String folderPath = FileAccess.getFolderPath(fullPath);
		folder = FileAccess.getFolder(folderPath);
		/*log.debug("AndroidFileWriter.java -- called with\n" +
				"fullPath = " + fullPath + " -- " +
				"characterEncoding = " + characterEncoding + " -- " +
				"folderPath = " + folderPath);*/
	}

	@Override
	public void open(int fileExistsStrategy, int fileDoesNotExistStrategy) throws Exception
	{
		if(fullPath == null)
			throw new NullPointerException();
		if(!FileAccess.isFilePath(fullPath))
			throw new Exception("Not a valid file path (" + fullPath + ")");
		if(fileExistsStrategy < 0 || fileExistsStrategy > 4)
			throw new IllegalArgumentException("Invalid file exists strategy");
		if(fileDoesNotExistStrategy < 1 || fileDoesNotExistStrategy > 2)
			throw new IllegalArgumentException("Invalid file does not exist strategy");
		boolean seekToEOF = false;
		if((new File(fullPath)).exists())
		{   //file already exists
			switch(fileExistsStrategy)
			{
				case(FileIO.FILE_EXISTS_STRATEGY_REPLACE):
					break;
				case(FileIO.FILE_EXISTS_STRATEGY_REJECT):
					folder = null;
				break;
				case(FileIO.FILE_EXISTS_STRATEGY_CREATE_RENAMED_FILE):
					//find a filename that does not exist yet (by adding a counter):
					String extension = FileAccess.getFileExtension(fullPath);
					String pathWithoutExtension = FileAccess.trimFileExtensionAndDot(fullPath);
					int i = 1; //counter
					do
					{
						fullPath = pathWithoutExtension + "-" + i + "."	+ extension;
						i++;
					}
					while((new File(fullPath)).exists()); //try until non-existing file found
					break;
				case(FileIO.FILE_EXISTS_STRATEGY_APPEND):
					seekToEOF = true;
					break;
				case(FileIO.FILE_EXISTS_STRATEGY_TRUNCATE):
					break;
			}
		}
		else
		{   //file does not exist
			switch(fileDoesNotExistStrategy)
			{
				case (FileIO.FILE_DOES_NOT_EXIST_STRATEGY_REJECT):
					folder = null;
				break;
				case (FileIO.FILE_DOES_NOT_EXIST_STRATEGY_CREATE):
					folder.mkdirs(); //file will be created lower, but we need to make sure the folder is created here.
				break;
			}
		}
		if(folder != null)
		{
			//Create file:
			File file = new File(folder, FileAccess.getFileName(fullPath));

			//Open file:
			OutputStream outputStream = new FileOutputStream(file, seekToEOF);
			writer = ((characterEncoding == null || characterEncoding.equals("")) ? new OutputStreamWriter(outputStream) : new OutputStreamWriter(outputStream, characterEncoding));
		}
		else
			throw new Exception("Could not open AndroidFileWriter");
	}

	@Override
	public void dispose()
	{
		close();
		file = null;
		folder = null;
	}

	@Override
	public void rename(String newName, int fileExistsStrategy) throws Exception
	{	//TODO make sure the FileIO.FILE_DOES_NOT_EXIST_STRATEGY_CREATE is ok?
		if(writer!=null)
			close();
		fullPath = FileAccess.getFolderPath(fullPath) + newName;
		file.renameTo(new File(fullPath));	
	}

	@Override
	public String getContainingFolderPath()
	{
		return FileAccess.getFolderPath(fullPath);
	}

	@Override
	public String getFileName()
	{
		return FileAccess.getFileName(fullPath);
	}

	@Override
	public boolean fileExists()
	{
		if(file != null)
			return file.exists();
		else
			throw new IllegalStateException("File is null, don't use this FileWriter");
	}

	@Override
	public long fileLastChanged()
	{
		if(file != null)
			return file.lastModified();
		else
			throw new IllegalStateException("File is null, don't use this FileWriter");
	}
	
}
