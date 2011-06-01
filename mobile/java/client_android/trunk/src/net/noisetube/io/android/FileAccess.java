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

/**
 * A class with FileAccess routines
 * 
 * @author mstevens, sbarthol
 * 
 */
public class FileAccess
{
	static public boolean isValidPath(String path)
	{		
		return path.charAt(0) == '/';
	}
	
	static public String getDirectorySeparator()
	{
		return File.separator;
	}

	static public boolean isFolderPath(String fullPath)
	{
		return isValidPath(fullPath) && fullPath.charAt(fullPath.length() - 1) == getDirectorySeparator().charAt(0);
	}

	static public boolean isFilePath(String fullPath)
	{
		return isValidPath(fullPath) && !isFolderPath(fullPath);
	}

	/**
	 * Removes or adds necessary /'s in the beginning and at the end of the path
	 * @param filePath
	 * @return
	 */
	static public String cleanFilePath(String filePath)
	{
		String result = filePath;
		if (result.endsWith("/"))
			result = result.substring(0, result.length() - 1);
		if (! result.startsWith("/"))
			result = "/" + result;
		return result;		
	}
	
	/**
	 * This function returns a folder, defined by the folderPath String (e.g. "/my/data/folder/")
	 * It will not create it if it doesn't already exists.
	 * @param folderPath
	 * @return
	 */
	static public File getFolder(String folderPath)
	{
		if (!isFolderPath(folderPath))
			return null;
		File folder = new File(folderPath);
		return folder;
	}
	
	/**
	 * Returns only the fileName of the filePath. e.g. for "/my/path/myfile.raw" : "myFile.raw"
	 * @param filePath
	 * @return The fileName of the filePath. e.g. for "/my/path/myfile.raw" : "myFile.raw"
	 */
	static public String getFileName(String filePath)
	{
		int lastIndex = filePath.lastIndexOf("/");
		String result = filePath.substring(lastIndex+1, filePath.length());
		return result;
	}
	
	/**
	 * Returns only the folderPath of the filePath. e.g. for "/my/path/myfile.raw" : "/my/path/"
	 * @param filePath
	 * @return The folderPath of the filePath. e.g. for "/my/path/myfile.raw" : "/my/path/"
	 */
	static public String getFolderPath(String filePath)
	{
		int lastIndex = filePath.lastIndexOf("/");
		String result = filePath.substring(0, lastIndex+1);
		return result;
	}

	/**
	 * Returns the extension of the filePath. e.g. for "/my/path/myfile.raw" : "raw"
	 * @param filePath
	 * @return The extension of the filePath. e.g. for "/my/path/myfile.raw" : "raw"
	 */
	static public String getFileExtension(String filePath)
	{
		int lastIndex = filePath.lastIndexOf(".");
		String result = filePath.substring(lastIndex, filePath.length());
		return result;
	}

	/**
	 * Returns the filePath without its extension. e.g for "/my/path/myfile.raw" : "/my/path/myfile"
	 * @param filePath
	 * @return The filePath without its extension. e.g for "/my/path/myfile.raw" : "/my/path/myfile"
	 */
	static public String trimFileExtensionAndDot(String filePath)
	{
		int lastIndex = filePath.lastIndexOf(".");
		String result = filePath.substring(0, lastIndex-1);
		return result;
	}
	
	/**
	 * This function appends two paths.
	 * It can be used to append a folderPath (e.g. "/my/path") with another folderPath, or to append
	 * a folderPath with a filePath (e.g. "/my/path/myfile.raw"). 
	 * @param folderPath
	 * @param path
	 * @return A correct concatenation of both pathstrings
	 */
	static public String appendPaths(String folderPath, String path)
	{
		if (!(isFolderPath(folderPath) && isValidPath(path)))
			return null;
		return folderPath + path.substring(1);
	}

}
