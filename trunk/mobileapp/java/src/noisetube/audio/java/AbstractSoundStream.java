package noisetube.audio.java;

import java.io.IOException;

/**
 * Read a wav sound file Specification from
 * http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
 * 
 * @author maisonneuve
 * 
 */
public abstract class AbstractSoundStream
{

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
	final int HEADER_SIZE = 44;

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
			throw new IllegalArgumentException("Stream not valid (only "
					+ stream.length + ")");
		}

		dataSize = stream.length - HEADER_SIZE;
		data = stream;
	}

	private void readHeader()
	{
		try
		{
			// skip the chunkID
			String chunkID = "" + (char) readByte(0) + (char) readByte(1)
					+ (char) readByte(2) + (char) readByte(3);

			String formatS = "" + (char) readByte(8) + (char) readByte(9)
					+ (char) readByte(10) + (char) readByte(11);

			// read the audio format. This should be 1 for PCM
			format = readShort(20);

			// read the # of channels (1 or 2)
			channels = readShort(22);

			// read the samplerate
			sampleRate = readInt(24);

			// read the bitspersample
			bitsPerSample = readShort(34);

			blockAlign = channels * bitsPerSample / 8;

			// read the byterate
			byteRate = blockAlign * sampleRate;

			// read the bitspersample
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
		return "format: " + format + "\nsampleRate: " + sampleRate
				+ "\nchannels: " + channels + "\nbyteRate: " + byteRate
				+ "\nbitsPerSample: " + bitsPerSample + "\nblockAlign: "
				+ blockAlign + "\nchunkSize: " + chunkSize + "\ndataSize: "
				+ dataSize;
	}

	protected abstract byte readByte(int l) throws IOException;

	protected abstract int readShort(int l) throws IOException;

	protected abstract int readInt(int i) throws IOException;

	private int getByteIndex(int sample_idx)
	{
		return HEADER_SIZE + sample_idx * blockAlign;
	}
}
