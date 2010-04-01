package noisetube.audio.java;

import java.util.Timer;
import java.util.TimerTask;

import noisetube.util.Logger;

/**
 * 
 * Controller to loop recording
 * 
 * @author maisonneuve
 * 
 */
public class AudioRecorderRunner implements IStreamRecorder
{

	Logger log = Logger.getInstance();

	private volatile boolean recording = false;

	long start;

	private Timer timer = new Timer();

	private AudioRecorder recorder = new AudioRecorder();

	private long timeInterval;

	private long recordingTime;

	public boolean isRunning()
	{
		return recording;
	}

	public AudioRecorder getRecorder()
	{
		return recorder;
	}

	public void setStreamAudioListener(StreamAudioListener listener)
	{
		recorder.setRecorderListener(listener);
	}

	public void start()
	{
		recording = true;
		start = System.currentTimeMillis();
		timer = new Timer();
		log.debug("Start audio runner (recording for " + recordingTime
				+ " every " + timeInterval + ")");
		// new Thread(record_task).start();
		timer.schedule(new TimerTask()
		{
			public void run()
			{
				recorder.record_during(recordingTime);
			}
		}, 0, timeInterval);
	}

	public void stop()
	{
		recording = false;
		timer.cancel();
		timer = null;
		log.debug("Stop audio runner");
	}

	public void setRecordingTime(int i)
	{
		this.recordingTime = i;

	}

	public void setTimeInterval(int i)
	{
		this.timeInterval = i;

	}
}
