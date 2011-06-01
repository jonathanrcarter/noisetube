/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Java ME version)
 *  
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011
 * --------------------------------------------------------------------------------
 *  This library is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU Lesser General Public License, version 2.1, as published
 *  by the Free Software Foundation.
 *  
 *  This library is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License along
 *  with this library; if not, write to:
 *    Free Software Foundation, Inc.,
 *    51 Franklin Street, Fifth Floor,
 *    Boston, MA  02110-1301, USA.
 *  
 *  Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *  NoiseTube project source code repository: http://code.google.com/p/noisetube
 * --------------------------------------------------------------------------------
 *  More information:
 *   - NoiseTube project website: http://www.noisetube.net
 *   - Sony Computer Science Laboratory Paris: http://csl.sony.fr
 *   - VUB BrusSense team: http://www.brussense.be
 * --------------------------------------------------------------------------------
 */

package net.noisetube.core.javame;

import java.io.IOException;

import javax.microedition.io.PushRegistry;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import net.noisetube.config.Preferences;
import net.noisetube.config.javame.JavaMEDevice;
import net.noisetube.config.javame.JavaMEPreferences;
import net.noisetube.core.Engine;
import net.noisetube.core.IService;
import net.noisetube.core.javame.restarting.IRunStateOwner;
import net.noisetube.core.javame.restarting.RunState;
import net.noisetube.ui.javame.*;
import net.noisetube.util.Logger;
import net.noisetube.util.StringUtils;

import com.sun.lwuit.Command;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.Resources;

/**
 * MainMIDlet
 * 
 * @author mstevens, maisonneuve
 * 
 */
public class MainMIDlet extends MIDlet implements IRunStateOwner
{

	//STATICS-------------------------------------------------------
	static public boolean RESEARCH_BUILD = false; //TODO make sure this is false for web release
	static public boolean BRUSSENSE = RESEARCH_BUILD && false; //TODO make sure this is false for web release
	
	//Client info
	static public JavaMENTClient ntClient;
	static public String CLIENT_TYPE = "NoiseTubeMobileJavaME";
	static public String CLIENT_VERSION = "v2.0.0"; //no need to change this version string here in the code, current value will be loaded from JAR/JAD in checkClient()
	static public boolean CLIENT_IS_TEST_VERSION = true; //TODO change for web release
	
	//Environment
	static final public int EMULATOR_ENV = 0; 		//Emulator environment
	static final public int PHONE_DEV_ENV = 1; 		//Development phone environment
	static final public int PHONE_PROD_ENV = 2; 	//Production phone environment
	
	//Set active environment:
	static public int ENVIRONMENT = PHONE_PROD_ENV; //TODO make sure this is PHONE_PROD_ENV for web release
	
	public static String getEnvironmentName(int environment)
	{
		String name = null;
    	switch(environment)
    	{
    		case PHONE_PROD_ENV : name = "Phone/Production"; break;
    		case PHONE_DEV_ENV : name = "Phone/Development"; break;
    		case EMULATOR_ENV : name = "Emulator"; break;
	        default : name = "unknown";
    	}
    	return name;
	}

	//Instance
	static private MainMIDlet INSTANCE;
	
	//Log
	protected static Logger log = Logger.getInstance();
	
	//Restarting stuff
	static private TrackRunState RUNSTATE;
	static protected boolean FIRST_RUN = true;
	static protected boolean LAST_RUN = true;
	static private int RESTART_DELAY = 3*1000; //3"
	static private int MAX_RUNSTATE_AGE = 5*60*1000; //5'
	static final public int RESTARTING_MODE_DISABLED = 0;
	static final public int RESTARTING_MODE_AUTO = 1;
	static final public int RESTARTING_MODE_MANUAL = 2;
	static /*package*/ int RESTARTING_MODE = RESTARTING_MODE_DISABLED; //!!!
	
	/**
	 * @return the instance
	 */
	static public MainMIDlet getInstance()
	{
		return INSTANCE;
	}

	// DYNAMICS------------------------------------------------------
	private JavaMEPreferences preferences;
	private Engine engine;
	
	private Form currentForm;	
	private MeasuringForm measuringForm;
	private IService screensaverBlocker;
	
	private Thread acCheckingThread;
	
	public MainMIDlet()
	{
		super();
		INSTANCE = this; //!!!
		
		//Client version...
		String version = getAppProperty("MIDlet-Version"); //gets value stored in JAD/JAR
		String versionPostfix = getAppProperty("NoiseTube-VersionPostfix");
		if(version != null)
			CLIENT_VERSION = 	"v" + version.trim()
								+ ((versionPostfix != null && !versionPostfix.equals("")) ? ("_" + versionPostfix) : "");
		CLIENT_IS_TEST_VERSION = (versionPostfix != null && (versionPostfix.equalsIgnoreCase("beta") || versionPostfix.equalsIgnoreCase("alpha"))) || CLIENT_IS_TEST_VERSION;
		
		//Print log info:
		log = Logger.getInstance();
		if(MainMIDlet.CLIENT_IS_TEST_VERSION || MainMIDlet.ENVIRONMENT == MainMIDlet.EMULATOR_ENV)
			log.setLevel(Logger.DEBUG);
		log.info(CLIENT_TYPE + " " + CLIENT_VERSION + " (build: " + BuildInfo.timeStamp + ") started");
		log.info("Environment: " + getEnvironmentName(ENVIRONMENT));
	}

	/**
	 * 
	 * @see javax.microedition.midlet.MIDlet#startApp()
	 */
	public void startApp()
	{
		//log.debug("startApp()");
		try
		{
			//GUI init's:
			Display.init(this);
			loadTheme();
			
			if(ntClient == null) //TODO DEAL PROPERLY WITH startApp/pauseApp restart and engine.pause
			{				
				//Create NTClient object (checks phone capabilities, creates/loads preferences, etc.)			
				ntClient = new JavaMENTClient(CLIENT_TYPE, CLIENT_VERSION, CLIENT_IS_TEST_VERSION, (ENVIRONMENT == EMULATOR_ENV));
				JavaMEDevice device = (JavaMEDevice) ntClient.getDevice();
				preferences = (JavaMEPreferences) ntClient.getPreferences();
				engine = ntClient.getEngine();
				
				//Deal with Nokia crash bug:
				if(device.getBrandID() == JavaMEDevice.BRAND_NOKIA)
				{
					if(RESEARCH_BUILD) //maximizes sample rate and does automatic restarting
						RESTARTING_MODE = RESTARTING_MODE_AUTO; //application restarts automatically (permission to allow this must be set!)
					else 
						RESTARTING_MODE = RESTARTING_MODE_MANUAL; //user needs to restart manually
					log.debug(" - Restarting mode: " + (RESTARTING_MODE == RESTARTING_MODE_AUTO ? "Automatic" : "Manual"));
					checkRecordingTime(device.getAudioSpecification().getSampleRate()); //avoid crash by limiting total recording time and forcing graceful exit before crash happens
				}
				else
					log.debug(" - Restarting mode: disabled");
				
				//Deal with a restart
				if(RESTARTING_MODE != RESTARTING_MODE_DISABLED)
					checkPreviousRun();
				
				//Enable file mode in the logger:
				if(preferences != null)
					log.enableFileMode(ntClient); //don't move this before the restart determining code
				
				//GUI
				//Touch screen:
				if(device.hasTouchScreen())
					UIManager.getInstance().getLookAndFeel().setTouchMenus(true);
				//Screensaver blocker:
				setScreensaverBlocker(); //will do nothing if not supported or disabled in pref's
				//Initialize MeasuringForm (don't move this code, it needs to be done BEFORE measuring is started)
				if(preferences.isUseLightGUI())
					measuringForm = new LightMeasuringForm();
				else
					measuringForm = new FullMeasuringForm();
				engine.setUI(measuringForm); //!!!
				
				//Check runstate & start measuring
				if(FIRST_RUN)
					startMeasuring(); //auto-start measuring (will first let the user log-in if needed)
				else
					restartMeasuring(RUNSTATE); //resume track of previous run
			}
		}
		catch(Exception e)
		{
			log.error(e, "MainMidlet.startApp()");
			showErrorForm(e);
		}
	}
	
	/**
	 * 
	 * @see javax.microedition.midlet.MIDlet#pauseApp()
	 */
	public void pauseApp()
	{
		//log.debug("pauseApp()");
	}
	
	/**
	 * 
	 * @see javax.microedition.midlet.MIDlet#destroyApp()
	 */
	protected void destroyApp(boolean unconditional) throws MIDletStateChangeException
	{
		exitApp(true);		
	}
	
	public void startMeasuring()
	{
		restartMeasuring(null);
	}
	
	public void restartMeasuring(TrackRunState mrs)
	{
		//First show login form if needed:
		if(preferences.getSavingMode() == Preferences.SAVE_HTTP && !preferences.isAuthenticated())
			showLoginForm();
		else
		{
			if(mrs == null)
				engine.start();
			else
				engine.restart(mrs.getTrack()); //!!!
			showMeasureForm();			
		}
	}
	
	/**
	 * @param definitive if true the application will not save its runstate
	 */
	public void exitApp(boolean definitive)
	{
		try
		{
			LAST_RUN = definitive;
			try
			{
				cancelRecordTimeCheck();
				engine.stop(); //!!! blocks until all saving is done
				if(screensaverBlocker != null)
					screensaverBlocker.stop();
			}
			catch(Exception ignore) {}
			if(definitive)
				log.debug("Exit requested, reason: User (or crash)");
			else
			{
				log.debug("Exit requested, reason: Time-up");
				RUNSTATE.setTrack(engine.getTrack());
				RUNSTATE.setStopTime(System.currentTimeMillis());
				log.debug("Runstate: " + RUNSTATE.prettyPrint());
				log.debug("Writing runstate...");
				RUNSTATE.save();
				log.debug("Runstate saved.");
			}
			log.debug("Exiting\n\n");
			log.disableFileMode(); //(won't do anything if not in file mode)
			//System.gc();
			notifyDestroyed(); //!!!
		}
		catch(Exception e)
		{ 
			if(ENVIRONMENT == EMULATOR_ENV)
				e.printStackTrace();
		}
	}
	
	public void showMeasureForm()
	{
		currentForm = measuringForm;
		currentForm.show();
	}

	public void showPreferencesForm()
	{
		currentForm = new PreferenceForm(engine);
		currentForm.show();
	}

	public void showLoginForm()
	{
		currentForm = new LoginForm();
		currentForm.show();
	}

	public void showAboutForm()
	{
		currentForm = new AboutForm();
		currentForm.show();
	}
	
	public void showConfirmExitForm()
	{
		currentForm = new ConfirmExitForm();
		currentForm.show();
	}

	private void loadTheme() throws IOException
	{
		Resources res = Resources.open("/businessTheme.res");
		UIManager.getInstance().setThemeProps(res.getTheme(res.getThemeResourceNames()[0]));
	}

	private void showErrorForm(Exception e)
	{
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
				try
				{
					exitApp(true);
				}
				catch(Exception ignore) {}
			}
		});
	}

	/**
	 * @return the engine
	 */
	public Engine getEngine()
	{
		return engine;
	}
	
	public void setScreensaverBlocker()
	{
		if(JavaMEDevice.supportsNokiaUIAPI())
		{
			if(preferences.isBlockScreensaver())
			{
				if(screensaverBlocker == null)
				{
					try
					{
						Class SBClass = Class.forName("noisetube.util.ScreensaverBlocker");
						if(SBClass != null)
							screensaverBlocker = (IService) SBClass.newInstance();
					}
					catch(Exception ignore) {}
				}
				if(screensaverBlocker != null)
					screensaverBlocker.start();
			}
			else
			{
				if(screensaverBlocker != null)
					screensaverBlocker.stop();
			}
		}
	}
	
	/*package*/ void checkRecordingTime(int sampleRate)
	{		
		//Determine maximum recording time (need to be determined experimentally, probably depends on RAM size of device)
		final long maxRecordingTimeMS;
		switch(sampleRate)
		{
			case 48000 : maxRecordingTimeMS = 7*60*1000; break; //7'
			case 44100 : maxRecordingTimeMS = 9*60*1000; break; //9'
			case 16000  : maxRecordingTimeMS = 19*60*1000; break; //19'
			default : maxRecordingTimeMS = 30*60*1000; //30' //7*60*1000 * (48000 / ntClient.getDevice().getAudioSpecification().getSampleRate());
		}
		log.debug(" - Maximum recording time: " + StringUtils.formatTimeSpanColons(maxRecordingTimeMS));
		acCheckingThread = new Thread(new Runnable()
		{
			public void run()
			{
				while(true)
				{
					try
					{
						synchronized(acCheckingThread)
						{
							Thread.currentThread().wait(45000); //wait 45s before checking (again)
						}
					}
					catch(InterruptedException e)
					{
						return; //!!!
					}					
					log.debug("Checking AC time active: " + StringUtils.formatTimeSpanColons(engine.getAudioComponent().getTotalTimeActiveMS()));
					if(engine.getAudioComponent().getTotalTimeActiveMS() >= maxRecordingTimeMS)
					{
						log.debug("Time up!");
						break; //break out the while
					}
				}
				//stop recording immediately:
				engine.getAudioComponent().stop(); //SLIGHT HACK: we by-pass the engine so the saver is only stopped when engine.stop() is called in exitApp
				//DISABLED: show msg & exit(true)
				//AUTO: schedule & exit(false)
				//MANUAL: show msg & exit(false)
				if(RESTARTING_MODE != RESTARTING_MODE_AUTO)
					showTimeupMessage();
				else
					scheduleNextRun(System.currentTimeMillis() + RESTART_DELAY); //schedule restart (with 2s delay)
				exitApp((RESTARTING_MODE == RESTARTING_MODE_DISABLED));
			}
		});
		acCheckingThread.start();
	}
	
	private void showTimeupMessage()
	{
		TextField txtMsg = new TextField("Because of a bug in the Java runtime of your Nokia phone NoiseTube must now stop measuring and exit. If you want to continue measuring, please restart the application manually.");
		txtMsg.getStyle().setFont(Fonts.SMALL_FONT);
		txtMsg.setSingleLineTextArea(false);
		txtMsg.setEditable(false);
		txtMsg.setFocusable(false);
		txtMsg.getStyle().setBorder(null);	
		Dialog.show("Maximum measuring time reached", txtMsg, new Command[] { new Command("OK") }); //blocks
	}
	
	public void cancelRecordTimeCheck()
	{
		if(acCheckingThread != null && Thread.currentThread() != acCheckingThread)
		{
			try
			{
				synchronized(acCheckingThread)
				{
					acCheckingThread.interrupt(); //interrupt waiting thread so MIDlet can exit immediately
				}
			}
			catch(Exception e)
			{
				log.error(e, "cancelRecordTimeCheck()");
			}
		}
	}
	
	public String getFolderPathForRunstateFile()
	{
		return preferences.getDataFolderPath();
	}

	public RunState deserialiseRunState(String state)
	{
		return new TrackRunState(this, state);
	}
	
	private void checkPreviousRun()
	{
		log.debug("Reading previous runstate...");
		TrackRunState previousState = (TrackRunState) TrackRunState.load(this); //reads and deletes runstate file (if present)
		if(previousState != null)
			log.debug("Previous runstate: " + previousState.prettyPrint());
		else
			log.debug("No previous runstate found");
		if(	previousState != null && 				//there IS a previous runstate (loaded from file)
			previousState.getStopTime() > RunState.NOT_SET &&		//the previous runstate has a valid stopTime
			previousState.getStopTime() + RESTART_DELAY + MAX_RUNSTATE_AGE >= System.currentTimeMillis()) //previous state is too old, discard, treat this execution as a first run
		{
			FIRST_RUN = false; //!!! this is not the first run
			RUNSTATE = previousState; //!!!
			RUNSTATE.incrementRunCount();
		}
		else
		{	
			FIRST_RUN = true; //just to be sure
			RUNSTATE = new TrackRunState(this);
		}	
		log.debug("The current execution is" + (FIRST_RUN ? "" : " not") + " the first run");
	}
	
	private void scheduleNextRun(long nextStartTime)
	{
		if(System.currentTimeMillis() > nextStartTime)
			throw new IllegalArgumentException("Cannot schedule in the past");
		log.debug("Scheduling next run");
		long previousRun = 0;
		try
		{
			previousRun = PushRegistry.registerAlarm(this.getClass().getName(), nextStartTime);
		}
		catch(ClassNotFoundException cnf)
		{
			log.error("Error scheduling next run (class not found): " + cnf.getMessage());
		}
		catch(javax.microedition.io.ConnectionNotFoundException connnf)
		{
			log.error("Error scheduling next run (connection not found): " + connnf.getMessage());
		}
		if(previousRun > 0 && previousRun < System.currentTimeMillis())
		{
			//log.debug("The current execution is not the first run");
			log.debug("Time of previous run: " + StringUtils.formatDateTime(previousRun, "/", ":", " "));
			//FIRST_RUN = false;
		}
		/*else
			log.debug("The current execution is the first run");*/
	}

}