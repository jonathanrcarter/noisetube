package noisetube.audio.java;

import java.io.ByteArrayOutputStream;

import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

import noisetube.config.Device;
import noisetube.util.Logger;

/**
 * 
 * Audio Recorder
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class AudioRecorder
{

	Logger log = Logger.getInstance();

	/**
	 * default sample rate
	 */
	private final int DEFAULT_SAMPLERATE = 16000;

	private int sampleRate = DEFAULT_SAMPLERATE;
	// private static final int KB = 1024;
	// private static final int RecordSizeLimit = 50 * KB;

	/**
	 * MMAPI recorder and player
	 */
	private RecordControl rc;
	private Player player;

	private String encoding;
	private static String audio_url = null;

	private volatile boolean recording = false;

	private ByteArrayOutputStream sound_stream = new ByteArrayOutputStream(
			DEFAULT_SAMPLERATE);

	private StreamAudioListener listener;

	public AudioRecorder() // throws Exception
	{
		// if(Device.getBestAudioEncoding() != null)
		this.encoding = Device.getBestAudioEncoding();
		/*
		 * else throw new Exception("Encoding cannot be null!");
		 */
	}

	public void setSampleRate(int sampleRate)
	{
		this.sampleRate = sampleRate;
	}

	public void setRecorderListener(StreamAudioListener l)
	{
		listener = l;
	}

	public boolean isRecording()
	{
		return recording;
	}

	public boolean record_during(long recordTime)
	{
		// Start recording
		// long start_time = System.currentTimeMillis();
		if(!startRecording())
			return false;

		// Wait...
		try
		{
			Thread.sleep(recordTime);
		}
		catch(Exception e)
		{
		}

		// Stop recording
		if(!stopRecording())
			return false;

		// Get duration
		// long end_time = System.currentTimeMillis();
		// long time = ((end_time - start_time) / 1000);
		// log.debug("latency: " + (end_time - start_time));

		return true;
	}

	public boolean startRecording()
	{
		try
		{
			if(recording)
			{
				log.debug("Recording has already started");
				return false;
			}

			// to be sure that there is any issue
			// reset(); //!!!

			player = getPlayer();

			// no recorder....
			if(player == null)
			{
				log.error("startRecording: Could not get player!");
				return false;
			}

			player.realize();
			rc = (RecordControl) player.getControl("RecordControl");
			sound_stream.reset();
			rc.setRecordStream(sound_stream);

			// in some j2me platforms the recordSizeLimit function is not
			// implemented (MEMORY Leak if use that MAX.Integer)
			/*
			 * try { rc.setRecordSizeLimit(RecordSizeLimit); } catch (Exception
			 * ignore) { log.error("record size error"); }
			 */

			// Start...
			player.start();
			rc.startRecord();
			recording = true;
			return true;

		}
		catch(Exception e)
		{
			log.error(e, "Start recording");
			reset();
			return false;
		}
	}

	public boolean stopRecording()
	{
		try
		{
			if(!recording)
			{
				log.debug("Recording has already stopped");
				return false;
			}

			// Stop...
			rc.stopRecord();
			rc.commit();

			// Send data it to listener...
			if(listener != null)
				listener.soundRecorded(sound_stream.toByteArray());

			reset(); // !!!
			return true;
		}
		catch(Exception e)
		{
			reset();
			try
			{
				Thread.sleep(2000);
			}
			catch(InterruptedException ie)
			{
			}
			log.error(e, "Stop recording");
			return false;
		}
	}

	private void reset()
	{
		try
		{
			if(player != null)
			{
				if(player.getState() == Player.STARTED)
					player.stop();
				if(player.getState() == Player.PREFETCHED)
					player.deallocate();
				if(player.getState() != Player.CLOSED)
					player.close();
				player = null;
				rc = null;
				System.gc();
				System.gc();
			}
		}
		catch(Exception e)
		{
			log.error(e, "Reset AudioRecorder");
		}
		recording = false;
	}

	private Player getPlayer()
	{
		if(audio_url == null)
		{ // try to get a player for different possibly working urls:
			Player p = getPlayerForURL("capture://audio?encoding=" + encoding
					+ "&rate=" + sampleRate + "&bits=16");
			if(p != null)
				return p;
			p = getPlayerForURL("capture://audio?encoding=" + encoding);
			if(p != null)
				return p;
			p = getPlayerForURL("capture://audio");
			if(p != null)
				return p;
			else
				return null;
		}
		else
			return getPlayerForURL(audio_url); // reuse the known-to-be-working
												// audio_url
	}

	private Player getPlayerForURL(String url)
	{
		Player p = null;
		try
		{
			p = Manager.createPlayer(url);
		}
		catch(Exception e)
		{
			log.error(e, "Failed to get player for audio url: " + url);
			if(p != null)
				p.close();
			return null;
		}
		if(audio_url == null)
			log.debug("Found working audio url: " + url);
		audio_url = url; // store this url as the known-to-be-working audio_url
		return p;
	}

}
