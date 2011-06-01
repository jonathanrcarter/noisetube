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
public abstract class AudioDecoder
{
	
	//STATICS:
	public static AudioDecoder getAudioDecoderFor(AudioStream audioStream) throws Exception
	{
		AudioDecoder decoder = null;
		switch(audioStream.encoding)
		{
			case AudioFormat.ENCODING_LINEAR_PCM : decoder = new PCMAudioDecoder(audioStream); break;
			//...
			case AudioFormat.NOT_SPECIFIED : 	//log.debug("Cannot decide in decoder (no encoding specified)");
												break; //method will return null
			default : throw new Exception("Unsupported encoding (no suitable decoder class)");
		}
		return decoder;
	}
	
	
	//DYNAMICS:
	protected AudioStream audioStream;	
	
	/**
	 * @param audioStream
	 */
	protected AudioDecoder(AudioStream audioStream)
	{
		this.audioStream = audioStream;
	}

	/**
	 * @param sampleIndex in interval [0, numSamples[
	 * @param channel numbered 0, 1, ...
	 * @return the wave amplitude at/for this sample as a double precision integer in the [-(2^(bitsPerSample-1)); 2^(bitsPerSample-1)-1] interval
	 */
	public abstract long getSampleAmplitude_Integer(int sampleIndex, int channel);
	
	/**
	 * @param sampleIndex
	 * @param channel numbered 0, 1, ...
	 * @return the wave amplitude at/for this sample as a double precision floating point number in the interval [-1.0; 1.0]
	 */
	public abstract double getSampleAmplitude_Floating(int sampleIndex, int channel);
	
	public double[] getSamplesAmplitude_Floating(int start, int end, int channel)
	{
		if(end < start)
			throw new IllegalArgumentException("Invalid sample range: end before start");
		double[] samples = new double[end - start + 1]; //TODO check this wrt LoudnessExtraction (also end < start check, deliver 1 sample?)
		for(int i = 0; i < samples.length; i++)
			samples[i] = getSampleAmplitude_Floating(start + i, channel);
		return samples;
	}

}
