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

/**
 * Implementation of the encoding of array of bytes to Int (x Endian)
 * (Nokia specific?)
 * TODO check if this is Nokia specific or the same for all J2ME CLDC implementations
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
	 * @param l index of the sample
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
