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

package net.noisetube.audio.android;

import java.nio.ByteOrder;

import net.noisetube.audio.format.AudioFormat;
import net.noisetube.audio.format.AudioSpecification;

import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * @author mstevens, sbarthol
 *
 */
public class AndroidAudioSpecification extends AudioSpecification
{

	public AndroidAudioSpecification(int sampleRate, int bitsPerSample, int numChannels)
	{
		super(AudioFormat.ENCODING_LINEAR_PCM, sampleRate, bitsPerSample, numChannels);
		switch(bitsPerSample)
		{
			case 8 : signed = AudioFormat.UNSIGNED; break;
			case 16 : signed = AudioFormat.SIGNED; break;
			//TODO case 24 : signed = ??
			//TODO case 32
			//TODO case 64
		}
		if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN)
			endianness = AudioFormat.BIG_ENDIAN;
		else
			endianness = AudioFormat.LITTLE_ENDIAN;
	}
	
	public int getChannelConfig()
	{
		if(numChannels == AudioFormat.NOT_SPECIFIED)
			return android.media.AudioFormat.CHANNEL_IN_MONO; 
		switch(numChannels)
		{
			case 1 : return android.media.AudioFormat.CHANNEL_IN_MONO;
			case 2 : return android.media.AudioFormat.CHANNEL_IN_STEREO;
			default : return android.media.AudioFormat.CHANNEL_IN_MONO;
		}
	}
	
	public int getAudioFormat()
	{
		switch(bitsPerSample)
		{
			case 8 : return android.media.AudioFormat.ENCODING_PCM_8BIT;
			case 16 : return android.media.AudioFormat.ENCODING_PCM_16BIT;
			//TODO case 24
			//TODO case 32
			//TODO case 64
			default : return android.media.AudioFormat.ENCODING_PCM_16BIT;
		}
	}

	public AudioRecord getAudioRecord(int bufferSize)
	{
		return new AudioRecord(MediaRecorder.AudioSource.MIC, getSampleRate(), getChannelConfig(), getAudioFormat(), bufferSize);
	}

}
