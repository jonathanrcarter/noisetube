package noisetube.io;

import noisetube.MainMidlet;
import noisetube.config.Preferences;
import noisetube.model.Measure;
import noisetube.util.IService;
import noisetube.util.Logger;

public abstract class Saver implements IService
{

	protected MainMidlet midlet;
	protected Preferences preferences;
	protected Logger log = Logger.getInstance();

	protected volatile boolean running = false;

	public Saver()
	{
		this.midlet = MainMidlet.getInstance();
		this.preferences = midlet.getPreferences();
	}

	/**
	 * @see noisetube.io.ISender#isRunning()
	 */
	public boolean isRunning()
	{
		return running;
	}

	public abstract void save(Measure measure);

	protected void setMessage(String msg)
	{
		midlet.getMeasureForm().getLog_ui().setSecondaryMessage(msg);
	}

	protected void clearMessage()
	{
		midlet.getMeasureForm().getLog_ui().setSecondaryMessage("");
	}

}
