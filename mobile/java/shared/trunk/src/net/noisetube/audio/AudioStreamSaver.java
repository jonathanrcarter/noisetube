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

package net.noisetube.audio;

import net.noisetube.audio.AudioStreamListener;
import net.noisetube.audio.format.AudioStream;
import net.noisetube.audio.format.RawAudioStream;
import net.noisetube.io.FileIO;
import net.noisetube.util.Logger;
import net.noisetube.util.StringUtils;

/**
 * @author mstevens
 *
 */
public abstract class AudioStreamSaver implements AudioStreamListener
{

	protected Logger log = Logger.getInstance();
	
	public void save(AudioStream stream, boolean saveRawAsWave)
	{
		String filename = FileIO.makeValidFileName("StreamDump_" + StringUtils.formatDateTime(System.currentTimeMillis(), "-", "", "_"));
		byte[] bytes;
		if(saveRawAsWave && stream instanceof RawAudioStream)
		{
			filename +=  ".wav";
			bytes = ((RawAudioStream) stream).getDataBytes(true);
		}
		else
		{
			filename += "." + stream.getFileExtension();
			bytes = stream.getDataBytes();
		}
		saveBytes(filename, bytes);
		log.debug("Stream (audio specification: " + stream.getAudioSpecUsedForRecording().toVerboseString() + ") saved to: " + filename);
	}
	
	public void save(AudioStream stream)
	{
		save(stream, false);
	}
	
	/**
	 * @see net.noisetube.audio.AudioStreamListener#receiveAudioStream(net.noisetube.audio.format.AudioStream)
	 */
	public void receiveAudioStream(AudioStream stream)
	{
		save(stream);
	}
	
	protected abstract void saveBytes(String fileName, byte[] bytes);

}
