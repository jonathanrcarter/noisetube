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
 * @author mstevens
 *
 */
public class RawAudioStream extends AudioStream
{

	private static final int headerSize = 0;
	private int blockAlign;
	
	protected void readHeader() throws Exception
	{
		//There is no header so we trust the audiospec:
		encoding = audioSpecUsedForRecording.getEncoding();
		sampleRate = audioSpecUsedForRecording.getSampleRate();
		bitsPerSample = audioSpecUsedForRecording.getBitsPerSample();
		numChannels = audioSpecUsedForRecording.getNumChannels();
		endianness = audioSpecUsedForRecording.getEndianness();
		signed = audioSpecUsedForRecording.getSigned();
		if(numChannels != AudioFormat.NOT_SPECIFIED && bitsPerSample != AudioFormat.NOT_SPECIFIED)
		{
			blockAlign = numChannels * bitsPerSample/8;
			numSamples = dataBytes.length / blockAlign;
			if(sampleRate != AudioFormat.NOT_SPECIFIED)
				byteRate = sampleRate * blockAlign;
		}
	}
	
	public void fixSampleRate(int correctSampleRate)
	{
		sampleRate = correctSampleRate;	
		//update depending field:
		if(blockAlign != AudioFormat.NOT_SPECIFIED)
			byteRate = sampleRate * blockAlign; //ByteRate = SampleRate * NumChannels * BitsPerSample/8 = SampleRate * BlockAlign
	}
	
	public boolean isValidWrtAudioSpec(AudioSpecification audioSpec) //override
	{
		return true; //we trust the AudioSpec so there's no point in validating
	}

	public int getAudioDataSize()
	{
		return dataBytes.length;
	}
	
	public int getContainerType()
	{
		return AudioFormat.CONTAINER_RAW;
	}
	
	public String getFileExtension()
	{
		return "raw"; //for lack of something better
	}
	
	public byte[] getDataBytes(boolean forWaveFile)
	{
		byte[] result = null;
		if(!forWaveFile)
			result = dataBytes;
		else
		{
			byte[] keep = dataBytes; //swap
			//Write RIFF/WAVE header:
			dataBytes = new byte[44 + keep.length];
			writeString(0, "RIFF");
			writeUnsignedInt(4, 36 + keep.length);
			writeString(8, "WAVE");
			writeString(12, "fmt ");
			writeUnsignedInt(16, 16);
			writeUnsignedShort(20, 1);
			writeUnsignedShort(22, numChannels);
			writeUnsignedInt(24, sampleRate);
			writeUnsignedInt(28, byteRate);
			writeUnsignedShort(32, blockAlign);
			writeUnsignedShort(34, bitsPerSample);
			writeString(36, "data");
			writeUnsignedInt(40, keep.length);
			for(int b = 0; b < keep.length; b++)
				dataBytes[44 + b] = keep[b];
			result = dataBytes;
			dataBytes = keep; //swap back
		}
		return result;
	}
	
	/**
	 * @param sampleIndex in interval [0, numSamples[
	 * @return offset (within the dataBytes byte array) of the first byte of the sample with number <sampleIndex>
	 */
	public int getSampleOffset(int sampleIndex)
	{
		if(sampleIndex < 0 || sampleIndex >= numSamples)
			throw new IllegalArgumentException("Invalid sample index, should be 0 <= sampleIndex < " + numSamples);
		return 	headerSize /*skip header*/ +
				(sampleIndex * blockAlign); /*skip previous samples*/
	}

}
