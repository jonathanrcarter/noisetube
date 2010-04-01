package noisetube;

import java.io.IOException;

import javax.microedition.midlet.MIDlet;

import noisetube.config.Device;
import noisetube.config.Preferences;
import noisetube.config.PreferencesFactory;
import noisetube.core.Engine;
import noisetube.ui.AboutForm;
import noisetube.ui.CalibrationForm;
import noisetube.ui.LoginForm;
import noisetube.ui.MeasureForm;
import noisetube.ui.PreferenceForm;
import noisetube.util.IService;
import noisetube.util.Logger;

import com.sun.lwuit.Command;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.Resources;

/**
 * MainMidlet
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class MainMidlet extends MIDlet implements ActionListener
{

	// STATICS-------------------------------------------------------

	// Client info
	static public String CLIENT_TYPE = "NoiseTubeMobileJ2ME";
	static public String CLIENT_VERSION = "v1.6.9_beta"; // no need to change
															// this version
															// string here in
															// the code, current
															// value will be
															// loaded from
															// JAR/JAD in
															// checkClient()
	static public boolean CLIENT_IS_TEST_VERSION = true;

	// Environment
	static final public int EMULATOR_ENV = 0; // Emulator environment
	static final public int PHONE_DEV_ENV = 1; // Development phone environment
	static final public int PHONE_PROD_ENV = 2; // Production phone environment
	static public int environment = PHONE_PROD_ENV; // default

	// Instance
	static private MainMidlet instance;

	/**
	 * @return the instance
	 */
	static public MainMidlet getInstance()
	{
		return instance;
	}

	// DYNAMICS------------------------------------------------------
	private Form current_form;
	private Engine engine;
	private MeasureForm measureForm;
	private Preferences preferences;
	private IService screensaverBlocker = null;

	private Logger log = Logger.getInstance();

	public MainMidlet()
	{
		instance = this;
	}

	public void startApp()
	{
		try
		{
			Display.init(this);

			// Set active environment:
			environment = PHONE_DEV_ENV;

			// Load & Check application preferences/capabilities
			preferences = loadPreferences();

			// Check phone capabilities:
			Device.checkDevice();

			// File access:

			// Start engine:
			engine = new Engine();

			// GUI:
			loadTheme();
			showMeasureForm(); // show main form (maybe first login if needed):
		}
		catch(Exception e)
		{
			log.error(e, "MainMidlet.startApp()");
			showErrorForm(e);
		}
	}

	private Preferences loadPreferences() throws Exception
	{
		return PreferencesFactory.build_preferences();
	}

	public void actionPerformed(ActionEvent ev)
	{
		if(ev.getCommand().getCommandName().equals("Exit"))
		{
			destroyApp(true);
		}
	}

	public MeasureForm getMeasureForm()
	{
		return measureForm;
	}

	public void showMeasureForm()
	{
		// First show login form if needed:
		if(preferences.getSavingMode() == Preferences.SAVE_HTTP
				&& !preferences.isAuthenticated())
			showLoginForm();
		else
		{
			if(measureForm == null)
				measureForm = new MeasureForm();
			setScreensaverBlocker(); // !!!
			current_form = measureForm.getForm();
			current_form.show();
			if(!measureForm.isRunning())
				measureForm.start(); // start automatically (if needed)
		}
	}

	public void showPreferencesForm()
	{
		PreferenceForm calibrate_c = new PreferenceForm();
		current_form = calibrate_c.getForm();
		current_form.show();
	}

	public void showLoginForm()
	{
		LoginForm login_c = new LoginForm();
		current_form = login_c.getForm();
		current_form.show();
	}

	public void showCalibrationForm() throws Exception
	{
		CalibrationForm calib = new CalibrationForm();
		current_form = calib.getForm();
		current_form.show();
	}

	public void showAboutForm()
	{
		AboutForm aboutForm = new AboutForm();
		current_form = aboutForm.getForm();
		current_form.show();
	}

	private void loadTheme() throws IOException
	{
		Resources res = Resources.open("/businessTheme.res");
		UIManager.getInstance().setThemeProps(
				res.getTheme(res.getThemeResourceNames()[0]));
	}

	private void setScreensaverBlocker()
	{
		if(preferences.isBlockScreensaver())
		{
			if(screensaverBlocker == null || !screensaverBlocker.isRunning())
			{
				if(screensaverBlocker == null)
				{
					try
					{
						Class SBClass = Class
								.forName("noisetube.util.ScreensaverBlocker");
						if(SBClass != null)
							screensaverBlocker = (IService) SBClass
									.newInstance();
					}
					catch(Exception ignore)
					{
					}
				}
				if(screensaverBlocker != null)
					screensaverBlocker.start();
			}
		}
		else
		{
			if(screensaverBlocker != null && screensaverBlocker.isRunning())
				screensaverBlocker.stop();
		}
	}

	private void showErrorForm(Exception e)
	{
		// create a form
		Form form = new Form("NoiseTube Error");
		TextArea text = new TextArea(e.getMessage());
		text.setSingleLineTextArea(false);
		form.addComponent(text);
		form.addCommand(new Command("Exit"));
		form.show();
		form.addCommandListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				destroyApp(true);
			}
		});
	}

	/**
	 * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
	 */
	public void destroyApp(boolean unconditional)
	{
		try
		{
			engine.stop();
			log.info("Exiting " + CLIENT_TYPE + " " + CLIENT_VERSION);
			log.disableFileMode();
			if(screensaverBlocker != null)
				screensaverBlocker.stop();
			System.gc();
			notifyDestroyed();
		}
		catch(Exception ignore)
		{
		}
	}

	/**
	 * 
	 * @see javax.microedition.midlet.MIDlet#pauseApp()
	 */
	public void pauseApp()
	{
	}

	/**
	 * @return the engine
	 */
	public Engine getEngine()
	{
		return engine;
	}

	/**
	 * @return the preferences
	 */
	public Preferences getPreferences()
	{
		return preferences;
	}

	// private void test() {
	// new Thread() {
	// public void run() {
	// try {
	// ServerSocketConnection ssc = (ServerSocketConnection) Connector
	// .open("socket://:1234");
	// SocketConnection sc = (SocketConnection) ssc
	// .acceptAndOpen();
	// DataInputStream is = sc.openDataInputStream();
	// // /
	// } catch (Exception e) {
	// // Handle
	// }
	// }
	// }.start();
	// }

}