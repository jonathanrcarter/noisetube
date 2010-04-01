package noisetube.audio;

import noisetube.MainMidlet;
import noisetube.audio.java.LoudnessComponentJava;
import noisetube.audio.jni.LoudnessComponentJNI;
import noisetube.config.Preferences;
import noisetube.config.WatchPreferences;
import noisetube.util.Logger;

/**
 * (Simple) Factory
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class LoudnessComponentFactory
{

	public static ILoudnessComponent getLoudnessComponent()
	{
		Logger log = Logger.getInstance();
		Preferences preferences = MainMidlet.getInstance().getPreferences();

		ILoudnessComponent component = null;

		// Watch implementation
		if(preferences instanceof WatchPreferences)
		{
			try
			{
				Class LoudnessCompClass = Class
						.forName("noisetube.watch.LoudnessComponentWatch");
				component = (ILoudnessComponent) LoudnessCompClass
						.newInstance();

			}
			catch(Exception e)
			{
				log.error(e, "Cannot create LoudnessComponentWatch instance");
				component = new LoudnessComponentJava(); // use default Java
															// implementation
			}
		}

		// JNI implementation
		else if(preferences.isJniRecording())
			component = new LoudnessComponentJNI();

		// Default Java implementation
		else
			component = new LoudnessComponentJava();

		return component;
	}
}
