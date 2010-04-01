package noisetube.audio.jni;

import noisetube.audio.AbstractLoudnessComponent;
import noisetube.audio.ILeqListener;
import noisetube.audio.ILoudnessComponent;
import noisetube.audio.java.IStreamRecorder;

/**
 * Loudness interface for C++ Implementation (JNI)
 * 
 * @author maisonneuve
 * 
 */
public class LoudnessComponentJNI extends AbstractLoudnessComponent implements
		ILoudnessComponent, ILeqListener
{

	protected IStreamRecorder getRecorder()
	{
		NativeRecorder r = new NativeRecorder();
		r.setLeqListener(this);
		r.setRecordingTime((int) recordingTime);
		r.setTimeInterval(0);
		return r;
	}

	// for native recording
	public void sendLeq(double leq)
	{
		leq += 100;
		if(corrected)
		{
			leq = correct_leq(leq);
		}
		if(leq > 0)
		{
			listener.sendLeq(leq);
		}
		else
			log.error("error: negative leq: " + leq);
	}
}
