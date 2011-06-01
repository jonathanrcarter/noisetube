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

import javax.microedition.media.Manager;
import javax.microedition.media.Player;

import net.noisetube.audio.format.AudioFormat;
import net.noisetube.audio.format.AudioSpecification;
import net.noisetube.util.Logger;
import net.noisetube.util.StringUtils;

/**
 * @author mstevens
 *
 */
public class JavaMEAudioSpecification extends AudioSpecification
{

	//STATICS----------------------------------------------
	public static boolean isWAVEPCMEncoding(String encoding)
	{
		if(encoding == null)
			return false; //throw new NullPointerException("Passed encoding is null");
		return (encoding.toLowerCase().indexOf("wav") > -1 || encoding.toLowerCase().indexOf("pcm") > -1);
	}
	
	public static Player getPlayerForURL(String url)
	{
		Player p = null;
		try
		{
			p = Manager.createPlayer(url);
		}
		catch(Exception e)
		{
			(Logger.getInstance()).error(e, "Failed to get player for audio url: " + url);
			if(p != null)
				p.close();
			return null;
		}
		return p;
	}
	
	public static AudioSpecification deserialise(String serialisedAudioSpec)
	{	
		return (JavaMEAudioSpecification) AudioSpecification.deserialise(serialisedAudioSpec, new JavaMEAudioSpecification()); //this will call deserialiseAdditionalParameters 
	}
	
	//DYNAMICS---------------------------------------------
	private String encodingString = null;
	
	private JavaMEAudioSpecification()
	{
	}
		
	/**
	 * @param encodingsPropertyEntry (an entry in the list returned by System.getProperty("audio.encodings");)
	 * @param defaultSampleRate used if no sampleRate is specified in encodingString, or if forceDefaults = true
	 * @param defaultBitsPerSample used if no bitsPerSample is specified in encodingString, or if forceDefaults = true
	 * @param defaultNumChannels used if no numChannels is specified in encodingString, or if forceDefaults = true
	 * @param forceDefaults if this is true the provided default sampleRate, bitsPerSample and numChannels will be used regardless of what's in the PropertyEntry (only the codec is used as specified in the PropertyEntry)
	 * 
	 * Example encodingStrings:
	 *  - encoding=pcm&rate=22050&bits=16&channels=1
	 *  - encoding=audio/wav
	 */
	public JavaMEAudioSpecification(String encodingsPropertyEntry, int defaultSampleRate, int defaultBitsPerSample, int defaultNumChannels, boolean forceDefaults)
	{
		String codec = null;
		int rate = defaultSampleRate, bits = defaultBitsPerSample, chans = defaultNumChannels;
		String[] parts = StringUtils.split(encodingsPropertyEntry, '&');
		for(int p = 0; p < parts.length; p++)
		{
			if(parts[p].startsWith("encoding="))
				codec = parts[p].substring("encoding=".length()).trim();
			else
			{
				if(!forceDefaults)
				{
					if(parts[p].startsWith("rate="))
						rate = Integer.parseInt(parts[p].substring("rate=".length()).trim());
					else if(parts[p].startsWith("bits="))
						bits = Integer.parseInt(parts[p].substring("bits=".length()).trim());
					else if(parts[p].startsWith("channels="))
						chans = Integer.parseInt(parts[p].substring("channels=".length()).trim());
				}
			}
		}
		initialize(codec, rate, bits, chans);
	}
	
	/**
	 * @param encodingString
	 * @param sampleRate
	 * @param bitsPerSample
	 * @param channels
	 */
	public JavaMEAudioSpecification(String encodingString, int sampleRate, int bitsPerSample, int numChannels)
	{
		initialize(encodingString, sampleRate, bitsPerSample, numChannels);
	}
	
	protected void initialize(String encodingString, int sampleRate, int bitsPerSample, int numChannels)
	{
		if(encodingString == null || encodingString.equals(""))
			throw new IllegalArgumentException("encodingString cannot be null or empty string");
		this.encodingString = encodingString;
		int enc = AudioFormat.NOT_SPECIFIED;
		if(encodingString.indexOf("wav") > -1 || encodingString.indexOf("pcm") > -1)
			enc = AudioFormat.ENCODING_LINEAR_PCM;
		/*else if(...)
		 	enc = ...*/
		super.initialize(enc, sampleRate, bitsPerSample, numChannels); //!!!
	}
	
	/**
	 * @param encodingString
	 */
	public JavaMEAudioSpecification(String encodingString)
	{
		this(encodingString, AudioFormat.NOT_SPECIFIED, AudioFormat.NOT_SPECIFIED, AudioFormat.NOT_SPECIFIED);
	}
	
	/**
	 * @param encodingString
	 * @param sampleRate
	 */
	public JavaMEAudioSpecification(String encodingString, int sampleRate)
	{
		this(encodingString, sampleRate, AudioFormat.NOT_SPECIFIED, AudioFormat.NOT_SPECIFIED);
	}
	
	public String serialise()
	{
		return super.serialise() + serialisationSeparator + (encodingString == null ? "" : encodingString);
	}
	
	protected void deserialiseAdditionalParameters(String[] params)
	{
		if(params == null)
			throw new IllegalArgumentException("params is null");
		if(params[0] != null && !params[0].equals(""))
			encodingString = params[0];
	}
	
	public Player getPlayer()
	{
		Player p = getPlayerForURL(getCaptureURL());
		if(p != null)
			return p;
		//try forgetting stuff (for some/all Sony Ericsson phones, and maybe others that can't deal with complete capture url's)
		numChannels = AudioFormat.NOT_SPECIFIED;
		bitsPerSample = AudioFormat.NOT_SPECIFIED;
		p = getPlayerForURL(getCaptureURL());
		if(p != null)
			return p;
		//also forget samplerate
		sampleRate = AudioFormat.NOT_SPECIFIED;
		p = getPlayerForURL(getCaptureURL());
		if(p != null)
			return p;
		//also forget encoding
		encodingString = null;
		encoding = AudioFormat.NOT_SPECIFIED;
		p = getPlayerForURL(getCaptureURL());
		return p;
	}
	
	public String getCaptureURL()
	{
		String urlTail = "";
		if(encodingString != null && !encodingString.equals(""))
			urlTail += (urlTail.length() == 0 ? "?" : "&") + "encoding=" + encodingString;
		if(sampleRate != AudioFormat.NOT_SPECIFIED)
			urlTail += (urlTail.length() == 0 ? "?" : "&") + "rate=" + Integer.toString(sampleRate);
		if(bitsPerSample != AudioFormat.NOT_SPECIFIED)
			urlTail += (urlTail.length() == 0 ? "?" : "&") + "bits=" + Integer.toString(bitsPerSample);
		if(numChannels != AudioFormat.NOT_SPECIFIED)
			urlTail += (urlTail.length() == 0 ? "?" : "&") + "channels=" + Integer.toString(numChannels);
		return "capture://audio" + urlTail;
	}
	
	public String getPrescriptionString()
	{
		return (encodingString != null ? encodingString : "?") + "," + super.getPrescriptionString();
	}
	
	public String prettyPrintString()
	{
		StringBuffer bff = new StringBuffer();
		bff.append("AudioSpec: Player URL string \"" + getCaptureURL() + "\"");
		if(resultKnown)
		{
			bff.append(" results in a " + (resultContainerType == AudioFormat.NOT_SPECIFIED ? "unknown" : AudioFormat.getContainerName(resultContainerType)) + " stream containing ");
			if(resultNumChannels != AudioFormat.NOT_SPECIFIED)
				bff.append(resultNumChannels);
			else
				bff.append(" an unknown number of");
			bff.append(" channel" + (resultNumChannels != 1 ? "s" : "") + " of audio in ");
			if(resultEncoding != AudioFormat.NOT_SPECIFIED)
				bff.append(AudioFormat.getEncodingName(resultEncoding));
			else
				bff.append("an unknown");
			bff.append(" encoding ");
			if(resultSampleRate == AudioFormat.NOT_SPECIFIED && resultBitsPerSample == AudioFormat.NOT_SPECIFIED)
			{
				bff.append("with unknown properties");
			}
			else
			{
				bff.append("sampled at ");
				if(resultSampleRate != AudioFormat.NOT_SPECIFIED)
					bff.append(resultSampleRate + "Hz");
				else
					bff.append("an unknown samplerate");
				bff.append(" using ");
				if(resultBitsPerSample != AudioFormat.NOT_SPECIFIED)
					bff.append(resultBitsPerSample + " bits");
				else
					bff.append("an unknown number of bits");
				bff.append(" per sample");
			}
		}	
		return bff.toString();
	}
	
}
