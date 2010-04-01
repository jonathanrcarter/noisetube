package noisetube.audio.java;

/**
 * Receive the stream of the audio
 * 
 * @author maisonneuve
 * 
 */
public interface StreamAudioListener
{

	public void soundRecorded(byte[] stream);
}
