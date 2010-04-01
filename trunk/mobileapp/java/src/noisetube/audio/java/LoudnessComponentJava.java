package noisetube.audio.java;

import noisetube.audio.AbstractLoudnessComponent;

public class LoudnessComponentJava extends AbstractLoudnessComponent implements
		StreamAudioListener
{

	protected LoudnessExtraction extraction = new LoudnessExtraction();

	boolean AFilter = true;

	/**
	 * Interval between 2 recordings
	 */
	private final int DEFAULT_INTERVAL = 1000; // 2000

	int timeInterval = DEFAULT_INTERVAL;

	public void start()
	{
		extraction.setAfilterActive(AFilter);
		super.start();
		// log.debug("corrected: " + corrected + "; afilter: " + AFilter);
	}

	public void setAFilterActive(boolean afilter)
	{
		this.AFilter = afilter;
	}

	protected IStreamRecorder getRecorder()
	{
		recorder = new AudioRecorderRunner();
		recorder.setStreamAudioListener(this);
		recorder.setRecordingTime((int) recordingTime);
		recorder.setTimeInterval(timeInterval);
		return recorder;
	}

	public void setTimeInterval(int i)
	{
		this.timeInterval = i;
	}

	// for java recording
	public void soundRecorded(byte[] stream)
	{
		double leq = 0;

		// compute leq
		try
		{
			leq = extraction.compute_leqs(stream)[0];
			leq += 100; // A CONSTANT to get positive value
			// log.debug("l: " + leq);
			if(corrected)
				leq = correct_leq(leq);
			if(leq > 0)
				listener.sendLeq(leq);
			else
				log.error("leq negative  " + leq);
		}
		catch(Exception e)
		{
			log.error(e.getMessage());
		}
	}
}
