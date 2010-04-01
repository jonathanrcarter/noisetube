package noisetube.audio.java;

import java.io.IOException;

/**
 * Implementation of the encoding of array of bytes to Int (x Endian) (proper to
 * Nokia?)
 * 
 * @author maisonneuve
 * 
 */
public final class SoundStreamNokia extends AbstractSoundStream
{

	public SoundStreamNokia(byte[] s) throws IOException
	{
		super(s);
	}

	protected byte readByte(int l) throws IOException
	{
		return data[l];
	}

	protected int readInt(int i) throws IOException
	{
		return (int) byteArrayToLong(data, i);
	}

	/**
	 * @param l
	 *            index of the sample
	 * @return
	 * @throws IOException
	 */
	protected int readShort(int l) throws IOException
	{
		return (short) ((data[l + 1] & 0xff) << 8 | data[l] & 0xff);
	}

	// ===========================
	// CONVERT BYTES TO JAVA TYPES
	// ===========================

	// these two routines convert a byte array to a unsigned short
	public static int byteArrayToInt(byte[] b, int start)
	{
		int low = b[start] & 0xff;
		int high = b[start + 1] & 0xff;
		return (short) (high << 8 | low);
	}

	// these two routines convert a byte array to an unsigned integer
	public static long byteArrayToLong(byte[] b, int start)
	{

		int i = 0;
		int len = 4;
		int cnt = 0;
		byte[] tmp = new byte[len];
		for(i = start; i < (start + len); i++)
		{
			tmp[cnt] = b[i];
			cnt++;
		}
		long accum = 0;
		i = 0;
		for(int shiftBy = 0; shiftBy < 32; shiftBy += 8)
		{
			accum |= ((long) (tmp[i] & 0xff)) << shiftBy;
			i++;
		}
		return accum;
	}
}
