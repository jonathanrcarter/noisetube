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

/**
 * File I/O helpers
 * 
 * @author mstevens
 *
 */
public final class FileIO
{
	
	//Strategies for opening FileConnection on an existing file:
	static final public int FILE_EXISTS_STRATEGY_REPLACE = 0;
	static final public int FILE_EXISTS_STRATEGY_REJECT = 1;
	static final public int FILE_EXISTS_STRATEGY_CREATE_RENAMED_FILE = 2;
	static final public int FILE_EXISTS_STRATEGY_RENAME_EXISTING_FILE = 3;
	static final public int FILE_EXISTS_STRATEGY_APPEND = 4;
	static final public int FILE_EXISTS_STRATEGY_TRUNCATE = 5;
	
	//Strategies for opening FileConnection on a non-existing file:
	static final public int FILE_DOES_NOT_EXIST_STRATEGY_REJECT = 1;
	static final public int FILE_DOES_NOT_EXIST_STRATEGY_CREATE = 2;

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
	
	private FileIO() { } //no-one should instantiate this class
	
}
