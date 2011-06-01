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
public final class AudioFormat
{

    public static final int NOT_SPECIFIED = -1;
    
    public static final int TRUE = 1;
    public static final int FALSE = 0;

    public static final int BIG_ENDIAN = 1; 	//Motorola byte order
    public static final int LITTLE_ENDIAN = 0;	//Intel byte order

    public static final int SIGNED = 1;
    public static final int UNSIGNED = 0;
    
    //Containers (each conresponds to an AudioStream subclass)
    public static final int CONTAINER_WAVE = 0;
    public static final int CONTAINER_AIFF = 1;
    public static final int CONTAINER_RAW = 2;
    public static final int CONTAINER_MP4 = 3;
    //3GPP...
    
    //Audio encodings
    public static final int ENCODING_LINEAR_PCM = 0;
    public static final int ENCODING_IEEE_FLOAT_PCM = 1;
    public static final int ENCODING_ULAW = 2;
    public static final int ENCODING_ALAW = 3;
    public static final int ENCODING_MSADPCM = 4;
    public static final int ENCODING_MPEG = 5;
    //...
    
    //Channels
    public static final int CHANNELS_MONO = 1;
    public static final int CHANNELS_STEREO = 2;
    //...
    
    /**
     * Common sample rates, ordered from high to low
     */
    public static final int[] SAMPLE_RATES = new int[] { /*384000, 192000, 96000, */ 48000, 44100, 32000, 22050, 16000, 11025, 8000/*, 4000*/ }; //Hz
    
    public static String getContainerName(int container)
    {
    	switch(container)
    	{
	        case CONTAINER_WAVE : return "WAVE";
	        case CONTAINER_AIFF : return "AIFF";
	        case CONTAINER_RAW : return "RAW";
	        case CONTAINER_MP4 : return "MP4";
	        //...
	        case NOT_SPECIFIED : return "unknown/unsupported";
	        default : return "unknown/unsupported";
    	}
    }
    
    public static String getEncodingName(int encoding)
    {
    	switch(encoding)
    	{
	        case ENCODING_LINEAR_PCM : return "LINEAR_PCM";
	        case ENCODING_IEEE_FLOAT_PCM : return "IEEE_FLOAT_PCM";
	        case ENCODING_ULAW : return "ULAW";
	        case ENCODING_ALAW : return "alaw";
	        case ENCODING_MSADPCM : return "msadpcm";
	        case ENCODING_MPEG : return "mpegaudio";
	        //...
	        case NOT_SPECIFIED : return "unknown/unsupported";
	        default : return "unknown/unsupported";
    	}
    }
		
	private AudioFormat() { } //no-one should instantiate this class
	
}
