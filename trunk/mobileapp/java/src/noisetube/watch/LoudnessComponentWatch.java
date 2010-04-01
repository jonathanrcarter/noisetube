package noisetube.watch;

import noisetube.MainMidlet;
import noisetube.audio.ILeqListener;
import noisetube.audio.ILoudnessComponent;
import noisetube.config.WatchPreferences;
import noisetube.util.Logger;

/**
 * Loudness Component for Watch
 * 
 * @author maisonneuve
 * 
 */
public class LoudnessComponentWatch implements ILoudnessComponent
{

	Logger log = Logger.getInstance();

	private WatchRunner watch = WatchRunner.getInstance();

	public LoudnessComponentWatch()
	{
		watch.setPreferences((WatchPreferences) MainMidlet.getInstance()
				.getPreferences());
	}

	public void start()
	{
		if(!watch.isRunning())
			watch.start();
	}

	public boolean isRunning()
	{
		return watch.isRunning();
	}

	public void stop()
	{
		watch.stop();
	}

	public void setLeqListener(ILeqListener listener)
	{
		watch.setLeqListener(listener);
	}
}
