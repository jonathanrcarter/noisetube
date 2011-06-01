/** 
 * -------------------------------------------------------------------------------
 * NoiseTube - Mobile client (J2ME version)
 * Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 * 
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License, version 2.1,
 * as published by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * --------------------------------------------------------------------------------
 * 
 * Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 * NoiseTube project source code repository: http://code.google.com/p/noisetube
 * NoiseTube project website: http://www.noisetube.net
 */

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
	static public String CLIENT_VERSION = "v1.8.1_beta"; //no need to change this version string here in the code, current value will be loaded from JAR/JAD in checkClient()
	static public boolean CLIENT_HAS_GPS_SUPPORT = true;
	static public boolean CLIENT_HAS_WATCH_SUPPORT = false;
	static public boolean CLIENT_IS_TEST_VERSION = true; //TODO change for web release

	// Environment
	static final public int EMULATOR_ENV = 0; 		//Emulator environment
	static final public int PHONE_DEV_ENV = 1; 		//Development phone environment
	static final public int PHONE_PROD_ENV = 2; 	//Production phone environment
	static public int environment = PHONE_PROD_ENV; //default

	// Instance
	static private MainMidlet instance;
	
	// Debug
	static public long totalMemory;
	static public long baseLevelMemoryUsage; 

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
	private IService screensaverBlocker;

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
			environment = PHONE_PROD_ENV; //EMULATOR_ENV;
			
			// Print log info:
			log.info(CLIENT_TYPE + " " + CLIENT_VERSION + " started");
			log.debug("Environment: " + environment);
			
			// Check phone capabilities:
			Device.checkDevice();

			// Load & Check application preferences/capabilities
			preferences = loadPreferences();

			// File access:
			if(Device.supportsSavingToFile())
				log.enableFileMode(); //enable file mode in logger

			// Initialize engine:
			engine = new Engine();

//			//Debug:
//			if(CLIENT_IS_TEST_VERSION)
//			{
//				totalMemory = Runtime.getRuntime().totalMemory();
//				baseLevelMemoryUsage = totalMemory - Runtime.getRuntime().freeMemory();
//				int baseLevelPrc = (int) (((double) baseLevelMemoryUsage / (double) totalMemory) * 100);
//				log.debug("Total available memory: " + totalMemory + " bytes");				
//				log.debug("Memory in use now (base level): " + baseLevelMemoryUsage + " bytes (" + baseLevelPrc + "%)");
//			}
			
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
		if(preferences.getSavingMode() == Preferences.SAVE_HTTP && !preferences.isAuthenticated())
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
		UIManager.getInstance().setThemeProps(res.getTheme(res.getThemeResourceNames()[0]));
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