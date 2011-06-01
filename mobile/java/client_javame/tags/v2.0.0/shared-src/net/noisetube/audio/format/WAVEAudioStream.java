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

import net.noisetube.util.Logger;

/**
 * An AudioStream class representing a WAVE sound file/stream
 * 
 * Partially based example code by Evan Merz (reuse permitted under the sole - and hereby
 * complied with - condition of attribution):
 *  - http://computermusicblog.com/blog/2008/08/29/reading-and-writing-wav-files-in-java/
 *  - http://www.computermusicblog.com/sourcecode/javaWavIO/wavIO.java
 * 
 * RIFF/WAVE specification described here:
 *  - http://ccrma.stanford.edu/courses/422/projects/WaveFormat
 *  - http://www-mmsp.ece.mcgill.ca/Documents/AudioFormats/WAVE/WAVE.html
 *  - http://www.sonicspot.com/guide/wavefiles.html
 *  - http://wiki.multimedia.cx/index.php?title=PCM
 *  - http://en.wikipedia.org/wiki/WAV
 *  - http://billposer.org/Linguistics/Computation/LectureNotes/AudioData.html
 * 
 * NOTE:
 *  the thing that makes reading wav files tricky is that Java has no unsigned types.
 *  This means that the binary data can't just be read and cast appropriately. Also,
 *  we have to use larger types than are normally necessary.
 *  In many languages including Java, an integer is represented by 4 bytes. The issue
 *  here is that in most languages, integers can be signed or unsigned, and in wav files
 *  the integers (in the headers) are unsigned. So, to make sure that we can store the
 *  proper values, we have to use longs to hold integers, and integers to hold shorts.
 *  To summarize, in the specs below:
 *   - "uint" = 32bit (4 bytes) unsigned integer, which we will need to store in a
 *     Java variable of type long, because Java's int is a 32bit SIGNED integer.
 *   - "ushort" = 16bit (2 bytes) unsigned integer, which we will need to store in a
 *     Java variable of type int, because Java's short is a 16bit SIGNED integer.
 * 
 * SPECIFICATION (RIFF/WAVE file containing PCM data):
 *  The canonical WAVE format starts with the RIFF header:
 *    0		4 (char[4])		ChunkID				Contains the letters "RIFF" in ASCII form (0x52494646 big-endian form).
 *    4		4 (uint)		ChunkSize			= 4 + (8 + SubChunk1Size) + (8 + SubChunk2Size); = 36 + SubChunk2Size if SubChunk1Size=16
 * 												 This is the size of the rest of the chunk following this number.  This is the size of the 
 * 												 entire file in bytes minus 8 bytes for the two fields not included in this count:
 * 												 ChunkID and ChunkSize.
 *    8		4 (char[4])		Format				Contains the letters "WAVE" (0x57415645 big-endian form).
 *
 *  The "WAVE" format consists of two subchunks: "fmt " and "data":
 *   The "fmt " subchunk describes the sound data's format:
 *    12	4 (char[4])		Subchunk1ID			Contains the letters "fmt " (0x666d7420 big-endian form).
 *    16	4 (uint)		Subchunk1Size		16 for PCM. This is the size of the rest of the Subchunk which follows this number.
 *    20	2 (ushort)		AudioFormat			PCM = 1 (i.e. Linear quantization). Values other than 1 indicate some form of compression.
 *    22	2 (ushort)		NumChannels			Mono = 1, Stereo = 2, etc.
 *    24	4 (uint)		SampleRate			8000, 44100, etc.
 *    28	4 (uint)		ByteRate			= SampleRate * NumChannels * BitsPerSample/8 = SampleRate * BlockAlign
 *    32	2 (ushort)		BlockAlign			= NumChannels * BitsPerSample/8 (= the number of bytes for one sample including all channels)
 *    34	2 (ushort)		BitsPerSample		8 bits = 8, 16 bits = 16, etc.
 *   [36	2 (ushort)		ExtSize				Size of format chunk extension (0 or 22)]		--> only present is Subchunk1Size = 18 or 40
 *   [38	2 (ushort)		ValidBitsPerSample	Number of valid bits]							--> only present is Subchunk1Size = 40
 *   [40	4 (uint)		ChannelMask			Speaker position mask (for multichannel files)]	--> only present is Subchunk1Size = 40
 *   [44	16 (GUID)		SubFormat			GUID, including the data format code]			--> only present is Subchunk1Size = 40
 *
 *   The "data" subchunk contains the size of the data and the actual sound:
 *    20+Subchunk1Size   (default 36)	4 (char[4])		Subchunk2ID			Contains the letters "data" (0x64617461 big-endian form).
 *    20+Subchunk1Size+4 (default 40)	4 (uint)		Subchunk2Size		= NumSamples * NumChannels * BitsPerSample/8
 * 														 This is the number of bytes in the data. You can also think of this
 * 														 as the size of the read of the subchunk following this number.
 *    20+Subchunk1Size+8 (default 44)	*				Data						The actual sound data, subChunk2Size bytes long.
 * 
 * @author mstevens, maisonneuve
 * 
 * TODO PCM: complete 24bit support, 32/64bit floating point support, multi-channel support
 * TODO implement additional encodings (maybe as subclasses)
 */
public class WAVEAudioStream extends AudioStream
{

	private static Logger log = Logger.getInstance();
	
    public final static int WAVE_FORMAT_PCM 		= 0x0001; //linear PCM
    public final static int WAVE_FORMAT_ADPCM 		= 0x0002;
    public final static int WAVE_FORMAT_IEEE_FLOAT 	= 0x0003;
    public final static int WAVE_FORMAT_ALAW 		= 0x0006;  //A-law logarithmic PCM (8-bit ITU-T G.711 A-law)
    public final static int WAVE_FORMAT_MULAW 		= 0x0007; //µ-law logarithmic PCM (8-bit ITU-T G.711 µ-law)
    public final static int WAVE_FORMAT_EXTENSIBLE 	= 0xFFFE;
    //there are many more... cfr. the urls above and com.sun.media.format.WavFormat (J2SE)
    
    
	private static final int DEFAULT_HEADER_SIZE = 44; //this is the case in canonical wave files containing uncompressed non-multichannel (mono/stereo) PCM data 
	
	private int headerSize = DEFAULT_HEADER_SIZE;
	private String chunkID;			//"RIFF"
	private long chunkSize; 
	private String format;			//"WAVE"
	private String subChunk1ID;		//"fmt "
	private long subChunk1Size;
	private int waveAudioFormat;
	private int extSize;
	private int validBitsPerSample;
	private long channelMask;
	private String subFormatGUID;
	private String subChunk2ID;		//"data"
	private long subChunk2Size;
	private int blockAlign;
	
	/**
	 * This will parse the header(s) of the stream and will
	 * check if it conforms, if not an exception is thrown
	 * 
	 * @throws Exception
	 */
	protected void readHeader() throws Exception
	{	
		if(dataBytes.length <= DEFAULT_HEADER_SIZE)
			throw new Exception("Stream not valid (only " + dataBytes.length + " bytes; should be > 44)");
		
		//Format defining fields-------------------------------------
		
		//RIFF type chunk ("RIFF....WAVE")-----------------
		//read the ChunkID (should be ="RIFF"; or "RIFX")
		chunkID = readString(0, 4);
		
		if(chunkID.equals("RIFF"))
			endianness = AudioFormat.LITTLE_ENDIAN;
		else if(chunkID.equals("RIFX"))
			endianness = AudioFormat.BIG_ENDIAN; //RIFX wave files are very rare
		else //default
			endianness = AudioFormat.LITTLE_ENDIAN;
		
		/*read the ChunkSize (should be = 36 + SubChunk2Size; or more precisely: 4 + (8 + SubChunk1Size) + (8 + SubChunk2Size)
        	This is the size of the rest of the chunk following this number. This is the size of the entire file/stream
        	in bytes minus 8 bytes for the two fields not included in this count: ChunkID and ChunkSize itself.*/
		chunkSize = readUnsignedInt(4);
		
		//read the Format (should be ="WAVE")
		format = readString(8, 4);
		
		//Check if this is indeed a WAVE (RIFF) file/stream
		if(!chunkID.equals("RIFF") && chunkID.equals("RIFX"))
			throw new Exception("Invalid ChunkID (\"" + chunkID + "\"), should be \"RIFF\"");
		//(chunkSize is tested further down)
		if(!format.equals("WAVE"))
			throw new Exception("Invalid Format (\"" + format + "\"), should be \"WAVE\"");
		
		//Format chunk ("fmt ")----------------------------
		//read the SubChunk1ID (should be ="fmt ")
		subChunk1ID = readString(12, 4);
		
		//read the SubChunk1Size (should be = 16, 18 or 40)
		subChunk1Size = readUnsignedInt(16);
		headerSize =	/*RIFF type chunk: */				12 +
						/*First 2 fields of Format chunk*/	8 +
						/*remainder of the Format chunk*/	(int) subChunk1Size +
						/*First 2 fields of the Data chunk*/ 8;
		
		//Check beginning of the Format chunk
		if(!subChunk1ID.equals("fmt "))
			throw new Exception("Invalid SubChunk1ID (\"" + subChunk1ID + "\"), should be \"fmt \"");
		if(subChunk1Size < 16)
			throw new Exception("Invalid SubChunk1Size (" + subChunk1Size + "), should be at least 16");
		
		//read the AudioFormat (should be = 1 for PCM)
		waveAudioFormat = readUnsignedShort(20);
		
		//read the # of channels (1 or 2)
		numChannels = readUnsignedShort(22);
		
		//read the SampleRate
		sampleRate = readUnsignedInt(24);
		
		//read the ByteRate (should be = SampleRate * NumChannels * BitsPerSample/8 = SampleRate * BlockAlign)
		byteRate = readUnsignedInt(28);

		//read the BlockAlign (should be = NumChannels * BitsPerSample/8)
		blockAlign = readUnsignedShort(32);
		
		//read the BitsPerSample
		bitsPerSample = readUnsignedShort(34);
		switch(bitsPerSample)
		{
			case 8 : signed = AudioFormat.UNSIGNED; break;
			case 16 : signed = AudioFormat.SIGNED; break;
			//TODO case 24 : signed = ??
			//TODO case 32
			//TODO case 64
		}
		
		//Format chunk extensions------
		if(subChunk1Size >= 18)
		{
			//read the extSize (should be 0 or 22)
			extSize = readUnsignedShort(36);
			
			if(subChunk1Size == 40 && extSize == 22) //checking both conditions just to be sure
			{	//read extension fields
				validBitsPerSample = readUnsignedShort(38);
				channelMask = readUnsignedInt(40);
				subFormatGUID = readString(44, 16);
				
				//debug
				log.debug("Format chunk extension fields:");
				log.debug(" - validBitsPerSample = " + validBitsPerSample);
				log.debug(" - channelMask = " + channelMask);
				log.debug(" - subFormatGUID = " + subFormatGUID);
			}
		}
		
		//debug
		//log.debug("subChunk1ID = " + subChunk1ID);
		//log.debug("subChunk1Size = " + subChunk1Size);
		//log.debug("audioFormat = " + audioFormat + (audioFormat == 1 ? " (PCM)" : ""));
		
		//Check remainder if the Format chunk
		if(waveAudioFormat < 1)
			throw new Exception("Invalid AudioFormat (" + waveAudioFormat + "), should be >0");
		switch(waveAudioFormat)
		{
			case WAVE_FORMAT_PCM :			encoding = AudioFormat.ENCODING_LINEAR_PCM; break;
			case WAVE_FORMAT_IEEE_FLOAT :	encoding = AudioFormat.ENCODING_IEEE_FLOAT_PCM; break;
			case WAVE_FORMAT_ADPCM : 		encoding = AudioFormat.ENCODING_MSADPCM; break;
			case WAVE_FORMAT_ALAW : 		encoding = AudioFormat.ENCODING_ALAW; break;
			case WAVE_FORMAT_MULAW : 		encoding = AudioFormat.ENCODING_ULAW; break;
			case WAVE_FORMAT_EXTENSIBLE : 	/*TODO parse SubFormat GUID*/ break;
			default : 						throw new Exception("Unknown audioFormat (" + waveAudioFormat + ")");
		}
		if(numChannels < 1)
			throw new Exception("Invalid NumChannels (" + numChannels + ") should be > 0");
		if(sampleRate <= 0)
			throw new Exception("Invalid NumChannels (" + sampleRate + ") should be > 0");
		if(bitsPerSample <= 0 || bitsPerSample % 8 != 0)
			throw new Exception("bitsPerSample (" + bitsPerSample + ") should be > 0 and multiple of 8");
		if(blockAlign != numChannels * bitsPerSample/8)
			throw new Exception("Invalid BlockAlign (" + blockAlign + "), should be " + (numChannels * bitsPerSample / 8));
		if(byteRate != sampleRate * blockAlign)
			throw new Exception("Invalid ByteRate (" + byteRate + "), should be " + (sampleRate * blockAlign));
		
		//debug
		//log.debug("numChannels: " + numChannels);
		//log.debug("sampleRate: " + sampleRate + " Hz");
		//log.debug("byteRate: " + byteRate + " Bytes/s");
		//log.debug("blockAlign: " + blockAlign);
		//log.debug("bitsPerSample: " + bitsPerSample);
		
		//Data Chunk ("data")------------------------------
		//read the SubChunk2ID (should be ="data")
		subChunk2ID = readString(20 + (int) subChunk1Size, 4);
		
		//read the SubChunk2Size (should be = NumSamples * NumChannels * BitsPerSample/8 = NumSamples * BlockAlign)
		subChunk2Size = readUnsignedInt(20 + (int) subChunk1Size + 4);
		numSamples = subChunk2Size / blockAlign; //number of samples (per channel!)

		//Check if the Data chunk is valid
		if(!subChunk2ID.equals("data"))
			throw new Exception("Invalid SubChunk2ID (" + subChunk2ID + "), should be \"data\"");
		
		//Check chunkSize
		if(chunkSize != 20 + subChunk1Size + subChunk2Size)
			throw new Exception("Invalid chunkSize");
		
		//debug
		//Compare subChunk2Size (reported size of the data) with the actual size
		//if(subChunk2Size < dataBytes.length - headerSize)
		//	log.debug("Reported data size (subChunk2Size) is smaller than actual size in the byte array, but we ignore the additional bytes");
		//log.debug("Audio data size: " + getAudioDataSize() + " Bytes");
		//log.debug("Number of samples: " + numSamples);
		//log.debug("Duration: " + Math.floor(getLengthSeconds() * 1000f) + "ms");
		
		//We don't support all possible encodings (which could be found in a WAVE file)...
		//if(encoding != AudioFormat.ENCODING_LINEAR_PCM /* && encoding != OTHER_SUPPORTED_ENCODING */) //already tested with decodeablity
		//	throw new Exception("Unsupported encoding (WAVE audioformat: " + waveAudioFormat + "), support may be implemented in the future");
		
		//Done reading & checking header
	}
	
	public int getAudioDataSize()
	{
		return (int) subChunk2Size;
	}

	public String toString()
	{
		return 	"Format: " + waveAudioFormat + (waveAudioFormat == 1 ? " (LINEAR PCM)" : "") +
				" | sampleRate: " + sampleRate + "Hz" +
				" | channels: " + numChannels +
				" | byteRate: " + byteRate + " Bytes/s" +
				" | bitsPerSample: " + bitsPerSample + " bits" +
				" | blockAlign: " + blockAlign +
				" | subChunk2Size (sample data): " + subChunk2Size + " Bytes" +
				/*" | real size: " + (rawData.length - HEADER_SIZE) + " Bytes" +*/
				" | elapsed time: " + Math.floor(getLengthSeconds() * 1000f) + "ms"
				/*+ " | elasped time (real): " + MathME.round(((float) (rawData.length - HEADER_SIZE) / (float) byteRate) * 1000f) + "ms"*/;
	}
	
	public void fixSampleRate(int correctSampleRate)
	{
		writeUnsignedInt(24, correctSampleRate);
		sampleRate = correctSampleRate;	
		//update depending field:
		byteRate = sampleRate * blockAlign; //ByteRate = SampleRate * NumChannels * BitsPerSample/8 = SampleRate * BlockAlign
		writeUnsignedInt(28, byteRate);
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

	public String getFileExtension()
	{
		return "wav";
	}

	public int getContainerType()
	{
		return AudioFormat.CONTAINER_WAVE;
	}
	
}
