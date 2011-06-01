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

package net.noisetube.audio.format;

/**
 * Generic AudioStream class for unknown (/unimplemented) encodings/formats, basically only useful to save such streams to file for further investigation
 * 
 * @author mstevens
 */
public class UnknownAudioStream extends AudioStream
{
	
	/**
	 * This will parse the header(s) of the stream and will
	 * check if it conforms, if not an exception is thrown
	 * 
	 * @throws Exception
	 */
	protected void readHeader() throws Exception
	{
	}
	
	public boolean isValidWrtAudioSpec(AudioSpecification audioSpec)
	{
		if(audioSpec == audioSpecUsedForRecording)
			return true; //we can't check, benefit of the doubt
		else
			return false; //we don't know...
	}

	public boolean isValidWrtRecordTime(int recordTimeMS) //override
	{
		return true; //we can't check, benefit of the doubt
	}

	/**
	 * @return the size (in bytes) of the audio data (only the actual samples)
	 */
	public int getAudioDataSize()
	{
		return AudioFormat.NOT_SPECIFIED; //we don't know...
	}

	public int getSampleOffset(int sampleIndex)
	{
		return AudioFormat.NOT_SPECIFIED;
	}
	
	public String getFileExtension()
	{
		return "bin"; //for lack of something better
	}
	
	public int getContainerType()
	{
		return AudioFormat.NOT_SPECIFIED;
	}
	
}
