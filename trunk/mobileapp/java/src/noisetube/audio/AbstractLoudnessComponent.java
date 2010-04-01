package noisetube.audio;

import noisetube.MainMidlet;
import noisetube.audio.java.Calibration;
import noisetube.audio.java.IStreamRecorder;
import noisetube.config.Preferences;
import noisetube.util.Logger;

/**
 * @author maisonneuve, mstevens
 * 
 */
public abstract class AbstractLoudnessComponent implements ILoudnessComponent
{

	protected Logger log = Logger.getInstance();

	/**
	 * Time of the recording
	 */
	private final int DEFAULT_RECORDING_TIME = 1200; // 1000

	protected double recordingTime = DEFAULT_RECORDING_TIME;

	protected ILeqListener listener;

	protected IStreamRecorder recorder;

	protected boolean corrected = true;
	protected boolean running = false;

	protected Calibration calibration;

	protected Preferences preferences = MainMidlet.getInstance()
			.getPreferences();

	// compute correction
	protected double correct_leq(double leq)
	{
		leq = calibration.correctLeq(leq);
		return leq;
	}

	public void setLeqListener(ILeqListener listener)
	{
		this.listener = listener;
	}

	protected abstract IStreamRecorder getRecorder();

	public void start()
	{
		// updated the calibration
		calibration = preferences.getCalibration();

		recorder = getRecorder();
		recorder.start();
		running = true;
	}

	public void stop()
	{
		running = false;
		if(recorder != null && recorder.isRunning())
			recorder.stop();
	}

	public double getLeqInterval()
	{
		return recordingTime;
	}

	public boolean isRunning()
	{
		return running;
	}

	public void setRecordingTime(int i)
	{
		recordingTime = i;
	}

	public void setCorrected(boolean corrected)
	{
		this.corrected = corrected;
	}

}
