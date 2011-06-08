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

package net.noisetube.audio.javame;

import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.noisetube.audio.AudioStreamSaver;

/**
 * @author mstevens
 *
 */
public class JavaMEAudioStreamSaver extends AudioStreamSaver
{
	
	private String folderPath;
	
	public JavaMEAudioStreamSaver(String folderPath)
	{
		this.folderPath = folderPath;
	}

	protected void saveBytes(String fileName, byte[] bytes)
	{
		try
		{
			String filePath = folderPath + fileName;
			FileConnection fconn = (FileConnection) Connector.open(filePath);
			if(fconn.exists())
				fconn.delete();
			fconn.create();
			OutputStream os = fconn.openOutputStream();
			os.write(bytes);
			os.flush();
			os.close();
			fconn.close();
		}
		catch(Exception savingEx)
		{
			log.error(savingEx, "Failed to save stream to file (" + savingEx.getMessage() + ")");
		}		
	}

}
