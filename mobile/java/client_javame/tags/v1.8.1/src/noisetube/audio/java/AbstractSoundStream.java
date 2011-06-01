/** 
 * -------------------------------------------------------------------------------
 * NoiseTube - Mobile client (J2ME version)
 * Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 * 
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License, version 2.1,
 * as published by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * --------------------------------------------------------------------------------
 * 
 * Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 * NoiseTube project source code repository: http://code.google.com/p/noisetube
 * NoiseTube project website: http://www.noisetube.net
 */

package noisetube.audio.java;

import java.io.IOException;

import noisetube.util.Logger;
import noisetube.util.MathME;

/**
 * Read a wav sound file Specification from
 * http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
 * 
 * @author maisonneuve, mstevens
 * 
 */
public abstract class AbstractSoundStream
{
	
	public static final int HEADER_SIZE = 44;

	private int format;
	private int channels;
	private long chunkSize;
	private long sampleRate;
	private long byteRate;
	private int blockAlign;
	private int bitsPerSample;
	private long dataSize;
	protected byte[] data;
	private boolean header_read = false;

	/**
	 * Read the sound file from a array of byte
	 * 
	 * @param stream
	 * @throws IOException
	 */
	public AbstractSoundStream(byte[] stream) throws IOException
	{

		if(stream == null)
		{
			throw new IllegalArgumentException("stream null");
		}

		if(stream.length <= HEADER_SIZE)
		{
			throw new IllegalArgumentException("Stream not valid (only " + stream.length + ")");
		}

		dataSize = stream.length - HEADER_SIZE;
		data = stream;
	}

	private void readHeader()
	{
		try
		{
			// skip the chunkID
			String chunkID = "" + (char) readByte(0) + (char) readByte(1) + (char) readByte(2) + (char) readByte(3);

			String formatS = "" + (char) readByte(8) + (char) readByte(9) + (char) readByte(10) + (char) readByte(11);

			// read the audio format. This should be 1 for PCM
			format = readShort(20);

			// read the # of channels (1 or 2)
			channels = readShort(22);

			// read the samplerate
			sampleRate = readInt(24);

			// read the bitspersample
			bitsPerSample = readShort(34);

			//blockAlign = readShort(32);
			blockAlign = channels * bitsPerSample / 8;

			// read the byterate
			//byteRate = readInt(28);
			byteRate = blockAlign * sampleRate;
			
			//ByteRate         == SampleRate * NumChannels * BitsPerSample/8
			//BlockAlign       == NumChannels * BitsPerSample/8

			// read the chunksize
			chunkSize = readInt(40);

			// fix bug
			if(chunkSize < dataSize)
			{
				dataSize = chunkSize;
			}

			header_read = true;

		}
		catch(IOException ignore)
		{
		}
	}

	public double[] readSamples2(int start, int end) throws IOException
	{
		double[] samples = new double[end - start + 1];
		for(int i = 0; i < samples.length; i++)
		{
			int start_byte = getByteIndex(start + i);
			// if (d > 32767 || d<0) allowed?
			int low = data[start_byte] & 0xff;
			int hi = data[start_byte + 1] << 8;
			samples[i] = (((double) (hi | low)) / (double) (Short.MAX_VALUE));
		}
		return samples;
	}

	public double[] readSamples(int start, int end) throws IOException
	{

		double[] samples = new double[end - start + 1];

		// for each sample
		for(int i = 0; i < samples.length; i++)
		{
			int start_byte = getByteIndex(start + i);
			int d = readShort(start_byte);
			samples[i] = (d) / 32768D;
		}
		return samples;
	}

	public byte[] getStream()
	{
		return data;
	}
	
	

	public int getFormat()
	{
		if(!header_read)
		{
			readHeader();
		}
		return format;
	}

	public long getChannels()
	{
		if(!header_read)
		{
			readHeader();
		}
		return channels;
	}

	public long getSampleRate()
	{
		if(!header_read)
		{
			readHeader();
		}
		return sampleRate;
	}

	public long getByteRate()
	{
		if(!header_read)
		{
			readHeader();
		}
		return byteRate;
	}

	public int getBlockAlign()
	{
		if(!header_read)
		{
			readHeader();
		}
		return blockAlign;
	}

	public int getBitsPerSample()
	{
		if(!header_read)
		{
			readHeader();
		}
		return bitsPerSample;
	}

	public long getDataSize()
	{
		if(!header_read)
		{
			readHeader();
		}
		return dataSize;
	}

	public String toString()
	{
		if(!header_read)
			readHeader();
		return 	"Format: " + format + (format == 1 ? " (PCM)" : "") +
				" | sampleRate: " + sampleRate + "Hz" +
				" | channels: " + channels +
				" | byteRate: " + byteRate + " Bytes/s" +
				" | bitsPerSample: " + bitsPerSample + " bits" +
				" | blockAlign: " + blockAlign +
				" | chunkSize: " + chunkSize + " Bytes" +
				" | real size: " + (data.length - HEADER_SIZE) + " Bytes" +
				" | time (chunk): " + MathME.round(((float) chunkSize / (float) byteRate) * 1000f) + "ms" +
				" | time (real): " + MathME.round(((float) (data.length - HEADER_SIZE) / (float) byteRate) * 1000f) + "ms";
	}
	
	public boolean checkPCMStream(int expectedSampleRate, int expectedChannels, long expectedDataSize)
	{
		Logger log = Logger.getInstance();
		if(!header_read)
			readHeader();
		if(format == 1)
		{
			if(sampleRate == expectedSampleRate)
			{
				if(channels == expectedChannels)
				{
					if(dataSize >= expectedDataSize)
					{
						return true; //!!!
					}
					else
					{
						log.debug("Data size too small");
					}
				}
				else
					log.debug("Incorrect channel count");					
			}
			else
				log.debug("Incorrect sample rate");
		}
		else
			log.debug("Stream not in PCM format!");
		return false; //!!!		
	}

	protected abstract byte readByte(int l) throws IOException;

	protected abstract int readShort(int l) throws IOException;

	protected abstract int readInt(int i) throws IOException;

	private int getByteIndex(int sample_idx)
	{
		return HEADER_SIZE + sample_idx * blockAlign;
	}
}
