/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Android version)
 *  
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011
 *  Android port by Vrije Universiteit Brussel (BrusSense team), 2010-2011
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

package net.noisetube.ui.android;

import java.util.HashMap;
import java.util.Iterator;

import net.noisetube.R;
import net.noisetube.audio.calibration.Calibration;
import net.noisetube.config.Preferences;
import net.noisetube.config.android.AndroidPreferences;
import net.noisetube.core.Engine;
import net.noisetube.core.NTClient;
import net.noisetube.core.android.AndroidNTClient;
import net.noisetube.model.Measurement;
import net.noisetube.model.Track;
import net.noisetube.ui.IMeasurementUI;
import net.noisetube.ui.android.LoginActivity;
import net.noisetube.ui.android.Toaster;
import net.noisetube.ui.android.tabs.Tab;
import net.noisetube.ui.android.tabs.TabLoadingActivity;
import net.noisetube.ui.android.tabs.TabGraphActivity;
import net.noisetube.ui.android.tabs.TabMapActivity;
import net.noisetube.ui.android.tabs.TabTagActivity;
import net.noisetube.util.Logger;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.app.AlertDialog.Builder;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

/**
 * @author sbarthol, mstevens
 *
 * This class compromises the main GUI of the app
 */
public class MainActivity extends TabActivity implements IMeasurementUI
{

	//STATICS-------------------------------------------------------

	//Instance
	static private MainActivity instance;
	
	/**
	 * @return the instance
	 */
	static public MainActivity getInstance()
	{
		return instance;
	}

	//DYNAMICS------------------------------------------------------
	private Logger log;
	private NTClient ntClient;
	private AndroidPreferences preferences;
	private Engine engine = null;
	
	private Dialog splashScreen;
	private TabHost tabHost;
	private HashMap<String, Tab> tabs = new HashMap<String, Tab>();
	
	/*
	 * We keep this here instead of in the map tab such that we can show the whole track including places
	 * visited before the map tab was first opened (the tab objects are only created when they are first opened)
	 */
	private NoiseLevelOverlay noiselevelOverlay;
	
	private Button pause_resumeButton;
	private Button start_stopButton;
	
	private boolean calibrationToastShown = false;
	private boolean showingAuxActivityOrDialog = false; //to avoid pausing when about/pref's is shown and pauseOnBackground is enabld
	private boolean pausedForBackground = false;
	private boolean pausedForCall = false;
	
	/** Called when the activity is first created.
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{	
		super.onCreate(savedInstanceState);
		instance = this; //!!!
		
		try
		{
			//Get log instance:
			log = Logger.getInstance();

			tabHost = getTabHost();
			
			/* On Android v2.1 (and probably earlier versions too, but we do not support those) a crash happens
			 * when no tabs are set before OnCreate ends.
			 * 		Cfr.: http://stackoverflow.com/questions/2020046/npe-when-drawing-tabwidget-in-android-only-on-htc-magic
			 * Solution: we use a dummy tab as a temporary place holder while loading the AndroidNTClient */
			addTab(TabLoadingActivity.class, getResources().getDrawable(R.drawable.noisetube_dialog_icon), "Loading");
			
			//show splash screen while loading
			showSplashScreen();
			
			new InitializeAndroidNTClient().execute(this); //will call initializeActivity when successful
		}
		catch (Exception e)
		{
			log.error(e, "MainActivity.onCreate()");
			removeSplashScreen();
			showErrorDialog(e);
		}
	}
	
	private void initializeActivity()
	{
		//NTClient...
		ntClient = AndroidNTClient.getInstance();
		preferences = (AndroidPreferences) ntClient.getPreferences();
		engine = ntClient.getEngine();
		engine.setUI(this); //!!!
		
		//enable file mode in the logger:
		if(preferences != null)
		{
			log.enableFileMode();
			log.disableLogBuffer(); //we don't need to log buffer now that all messages are directly saved to file
		}
		
		//Register PhoneStateListener to pause measuring on incoming calls
		try
		{
			TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			telephonyManager.listen(new NTPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
		}
		catch(Exception ignore) { } //this is a non-vital feature anyway
		
		//GUI:
		setContentView(R.layout.mainactivity);
		noiselevelOverlay = new NoiseLevelOverlay(); //map overlay
		initializeTabs();
		
		// Bind the pause_resume button:
		pause_resumeButton = (Button) findViewById(R.id.pauseButton);
		pause_resumeButton.setOnClickListener(pause_resumeButtonListener);

		// Bind the start_stop button:
		start_stopButton = (Button) findViewById(R.id.startstopButton);
		start_stopButton.setOnClickListener(start_stopButtonListener);
		
		//Show gui by removing splash screen that is shown on top of it:
		removeSplashScreen(); //!!!

		//Display main form with measurement (or login form, if needed) and start measuring
		startMeasuring();
	}
	
	private void addTab(Class tabClass, Drawable icon, String title)
	{
		//Initialize a TabSpec for each tab and add it to the TabHost
		tabHost.addTab(tabHost.newTabSpec(tabClass.getSimpleName()).setIndicator(title, icon).setContent(new Intent().setClass(this, tabClass)));
	}
	
	/*
	 * Called from Tab instances
	 */
	public void registerTab(Tab tab)
	{
		if(!tabs.containsKey(tab.getClass().getSimpleName()))
			tabs.put(tab.getClass().getSimpleName(), tab);
	}
	
	/**
	 * This function can be used to renew the tabs, for example, when the GPS is being disabled (in
	 * this case the map tab is useless).
	 * 
	 * TODO hide map tab when gps is disabled: http://stackoverflow.com/questions/2645703/hide-tab-in-tabactivity
	 */
	private void initializeTabs()
	{
		Resources res = getResources(); //Resource object to get Drawables
		tabHost = getTabHost(); //just in case it changed since onCreate (can that happen?)
		tabHost.clearAllTabs();
		tabs.clear();	
		
		addTab(TabGraphActivity.class, res.getDrawable(R.drawable.ic_tab_graph), "Graph");
		
		//Only add the map when the gps is enabled in preferences.
		if(NTClient.getInstance().getPreferences().isUseGPS())
			addTab(TabMapActivity.class, res.getDrawable(R.drawable.ic_tab_map), "Map");

		//Spectrum disabled
		//addTab(TabSpectrumActivity.class, res.getDrawable(R.drawable.ic_tab_spec), "Spectrum");
		
		addTab(TabTagActivity.class, res.getDrawable(R.drawable.ic_tab_tag), "Tag");

		//Tab changed event:
		tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener()
		{
			public void onTabChanged(String tag)
			{
				tabs.get(tag).refresh();
				
				//tab title colors:
				updateTabTitleColors();
			}
		});
		
		updateTabTitleColors();
	}
	
	private void updateTabTitleColors()
	{
		for(int i=0; i< tabHost.getTabWidget().getChildCount();i++) 
		{ 
			TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title); //Unselected Tabs
			tv.setTextColor(Color.WHITE);
		} 
		TextView tv = (TextView) tabHost.getCurrentTabView().findViewById(android.R.id.title); //for Selected Tab
		tv.setTextColor(Color.BLACK);
	}
	
	public void startMeasuring()
	{
		// First show login form if needed:
		if(preferences.getSavingMode() == Preferences.SAVE_HTTP && !preferences.isAuthenticated())
			showLoginActivity();
		else
		{
			engine.start();
			if(!calibrationToastShown)
			{
				Calibration calibration = preferences.getCalibration();
				if(calibration == null || calibration.getEffeciveCredibilityIndex() > Calibration.CREDIBILITY_INDEX_G)
					Toaster.displayToast("NoiseTube is currently not calibrated for your phone model.\nGeneric setting in use, measurements are likely inaccurate.\nNew calibration settings are downloaded automatically when they become available.");
				else if(calibration.getEffeciveCredibilityIndex() == Calibration.CREDIBILITY_INDEX_G)
					Toaster.displayToast("NoiseTube is currently not calibrated for your phone model.\nBrand default setting in use, measurements may be inaccurate.\nNew calibration settings are downloaden automatically when they become available.");
				else
					Toaster.displayToast("Using calibration setting for your phone model for best accuracy.");
				calibrationToastShown = true;
			}
		}
	}
		
	public void stopMeasuring(boolean restartAfterwards)
	{
		engine.pause(true); //"pre-stop" pause (only stops AudioComponent)
		final TrackSummaryDialog trackSummaryDialog = new TrackSummaryDialog(this, engine.getTrack(), restartAfterwards);
		showingAuxActivityOrDialog = true;
		trackSummaryDialog.show();
		(new Thread()
		{
			public void run()
			{
				engine.stop(); //will block while saving completes or waiting time runs out
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						trackSummaryDialog.stopWaiting(!engine.getSaver().isRunning());
					}
				});	
			}
		}).start();
	}

	public void showLoginActivity()
	{
		startActivity(new Intent(MainActivity.this, LoginActivity.class));
	}
	
	private void showPreferences()
	{
		showingAuxActivityOrDialog = true;
		startActivity(new Intent(this, PreferencesActivity.class));
	}
	
	private void showAbout()
	{
		showingAuxActivityOrDialog = true;
		startActivity(new Intent(this, AboutActivity.class));
	}
	
	public void showGraph()
	{
		getTabHost().setCurrentTabByTag(TabGraphActivity.class.getSimpleName());
	}
	
	/**
	 * @param track
	 * @param newMeasurement
	 * @param savedMeasurement the measurement which has been removed from the track and saved to make room for the new one
	 */
	public void newMeasurement(final Track track, final Measurement measurement, final Measurement savedMeasurement)
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				//Add to noiseleveloverlay for the map:
				noiselevelOverlay.update(track, savedMeasurement);
				
				//Update tabs:
				String currentTabTag = tabHost.getCurrentTabTag();
				Iterator<Tab> tabIter = tabs.values().iterator();
				while(tabIter.hasNext())
				{
					Tab tab = tabIter.next();
					if(currentTabTag.equals(tab.getClass().getSimpleName()) || tab.mustRemainInformed())
						tab.update(measurement, track);
				}
			}
		});
	}

	public void measuringStarted(Track track)
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				pause_resumeButton.setEnabled(true);
				start_stopButton.setText("Stop");
				start_stopButton.setEnabled(true); //unblock button
				Iterator<Tab> tabIter = tabs.values().iterator();
				while(tabIter.hasNext())
					tabIter.next().start();
				Toaster.displayShortToast("Measuring started");
			}
		});
	}
	
	public void measuringStopped(Track track)
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				pause_resumeButton.setEnabled(false);
				start_stopButton.setText("Start");
				start_stopButton.setEnabled(true); //unblock button
				Iterator<Tab> tabIter = tabs.values().iterator();
				while(tabIter.hasNext())
					tabIter.next().stop();
				//Toaster.displayShortToast("Measuring stopped"); //is already clear from summary dialog
			}
		});
	}

	public void measuringPaused(Track track)
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				pause_resumeButton.setText("Resume");
				pause_resumeButton.setEnabled(true); //unblock button
				Toaster.displayShortToast("Measuring paused");
			}
		});
	}

	public void measuringResumed(Track track)
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				pause_resumeButton.setText("Pause");
				pause_resumeButton.setEnabled(true); //unblock button
				Toaster.displayShortToast("Measuring resumed");
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.switchlogin).setVisible(preferences.getSavingMode() == Preferences.SAVE_HTTP);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId())
		{
			case R.id.preferences : showPreferences(); return true;
			case R.id.about: showAbout(); return true;
			case R.id.switchlogin:
			{
				preferences.setAccount(null); //= "log-out"
				stopMeasuring(true);
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private OnClickListener pause_resumeButtonListener = new OnClickListener() {
		public void onClick(View v)
		{
			pause_resumeButton.setEnabled(false); //block button to avoid hammering (causing repeated pausing/resuming)
			if(engine.isPaused())
				engine.resume();
			else
				//pausing the engine does also flush the track-buffer, which means that the graph will restart once resumed.
				engine.pause();
		}
	};

	private OnClickListener start_stopButtonListener = new OnClickListener() {
		public void onClick(View v)
		{
			start_stopButton.setEnabled(false); //block button to avoid hammering (causing repeated starts/stops)
			if(engine.isRunning())
				stopMeasuring(false);
			else
				startMeasuring();
		}
	};
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
	    super.onConfigurationChanged(newConfig);
	    //Toaster.displayShortToast("Config changed");
	    //do nothing...
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		if(!showingAuxActivityOrDialog)
			pause();
	}
	
	public void pause()
	{
		if(preferences != null && preferences.isPauseWhenInBackground())
		{
			engine.pause();
			pausedForBackground = true;
		}
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		resume();
		showingAuxActivityOrDialog = false;
	}
	
	public void resume()
	{
		if(pausedForBackground && preferences.isPauseWhenInBackground())
		{
			engine.resume();
			pausedForBackground = false;
		}
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		//This has been replaced by back-key specific functionality.
		if(isFinishing())
		{
			try
			{
				log.debug("Exit requested");
				if(engine != null && engine.isRunning())
				{
					engine.stop(true); //!!! block until saving is done or waiting time is up (silent, does not update gui)
					Toaster.displayShortToast("Measuring stopped");
				}
				log.info("Exiting NoiseTube Mobile\n\n");
				log.disableFileMode();
				Toaster.displayToast("Goodbye");
				//Make sure the client is properly disposed of (static variables are set to null etc).
				NTClient.dispose();
				//Also make sure the appropriate static variables etc, stored in the tabs, are cleaned up
				ntClient = null;
				instance = null;
			}
			catch(Exception ignore) {}
		}
	}
	
	/**
	 * @return the engine
	 */
	public Engine getEngine()
	{
		return engine;
	}
	
	/**
	 * @return the noiselevelOverlay
	 */
	public NoiseLevelOverlay getNoiselevelOverlay()
	{
		return noiselevelOverlay;
	}
	
	public class NTPhoneStateListener extends PhoneStateListener
	{
	
		@Override
		public void onCallStateChanged(int state, String incomingNumber)
		{
			//Log.e("PhoneCallStateNotified", "Incoming number "+incomingNumber);
			try
			{
				switch(state)
				{
					case TelephonyManager.CALL_STATE_RINGING : engine.pause(); pausedForCall = true; break;
					case TelephonyManager.CALL_STATE_OFFHOOK : engine.pause(); pausedForCall = true; break;
					case TelephonyManager.CALL_STATE_IDLE :
						if(pausedForCall)
						{	
							engine.resume();
							pausedForCall = false;
						}
						break;
				}
			}
			catch(Exception ignore) { }
		}

	}
	
	/**
	 * Shows the splash screen over the full Activity
	 */
	protected void showSplashScreen()
	{
		showingAuxActivityOrDialog = true;
		splashScreen = new Dialog(this, R.style.DefaultTheme);
		splashScreen.setTitle(R.string.app_name);
		splashScreen.setContentView(R.layout.splashscreen);
		splashScreen.setCancelable(false);
		splashScreen.show();
	}

	/**
	 * Removes the Dialog that displays the splash screen
	 */
	protected void removeSplashScreen()
	{
		if(splashScreen != null)
		{
			splashScreen.dismiss();
			splashScreen = null;
		}
	}
	
	protected void showErrorDialog(Exception error)
	{
		Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name) + " error");
		builder.setIcon(R.drawable.noisetube_dialog_icon);
		builder.setMessage(error.getMessage());
		builder.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			public void onCancel(DialogInterface dialog)
			{
				finish();
			}
		});
		builder.setNeutralButton("Exit", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				finish();
			}
		});
		showingAuxActivityOrDialog = true;
		builder.show();
	}
	
	/**
	 * @author mstevens
	 *
	 */
	public class InitializeAndroidNTClient extends AsyncTask<ContextWrapper, Void, AndroidNTClient>
	{
		
		private Exception error = null;

		@Override
		protected AndroidNTClient doInBackground(ContextWrapper... wrappers)
		{
			try
			{
				return new AndroidNTClient(wrappers[0]);
			}
			catch(Exception e)
			{
				error = e;
				return null;
			}
		}
			
		@Override
		protected void onPostExecute(AndroidNTClient ntClient)
		{
			if(ntClient == null)
			{
				removeSplashScreen();
				showErrorDialog((error != null ? error : new Exception("Could not create NTClient object")));
			}
			else
				initializeActivity(); //will remove splash screen
		}
		
	}
	
}