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

import net.noisetube.R;
import net.noisetube.config.Preferences;
import net.noisetube.core.NTClient;
import net.noisetube.ui.android.MainActivity;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;

/**
 * @author sbarthol, mstevens
 *
 */
public class PreferencesActivity extends Activity
{
	
	private Preferences preferences;
	private boolean changesMade = false;
	private boolean requiresStop = false;
	
	private RadioButton  radioHTTP;
	private RadioButton  radioFile;
	private RadioButton  radioNone;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferencesactivity);

		preferences = NTClient.getInstance().getPreferences();
		
		/*
		 * There are multiple settings that can be adapted:
		 * Use GPS?
		 * Store HTTP/FILE/NONE?
		 * Use external Storage?
		 */
		CheckBox checkGPS = (CheckBox) findViewById(R.id.usegps);
		checkGPS.setChecked(preferences.isUseGPS());

		CheckBox checkMemoryCard = (CheckBox) findViewById(R.id.memorycard);
		checkMemoryCard.setChecked(preferences.isPreferMemoryCard());

		radioHTTP = (RadioButton) findViewById(R.id.usehttp);
		radioHTTP.setChecked(preferences.getSavingMode() == Preferences.SAVE_HTTP);
		radioFile = (RadioButton) findViewById(R.id.usefile);
		radioFile.setChecked(preferences.getSavingMode() == Preferences.SAVE_FILE);
		radioNone = (RadioButton) findViewById(R.id.usenone);
		radioNone.setChecked(preferences.getSavingMode() == Preferences.SAVE_NO);	
		
		CheckBox checkPauseOnBackground = (CheckBox) findViewById(R.id.pauseWhenInBackground);
		checkPauseOnBackground.setChecked(preferences.isPauseWhenInBackground());
		
		checkGPS.setOnClickListener(gpsListener);
		checkMemoryCard.setOnClickListener(memoryListener);
		radioHTTP.setOnClickListener(httpListener);
		radioFile.setOnClickListener(fileListener);
		radioNone.setOnClickListener(noneListener);
		checkPauseOnBackground.setOnClickListener(pauseOnBackgroundListener);

		//Exiting this screen can be done by using standard Android return button or by custom Done button
		Button doneButton = (Button) findViewById(R.id.btnPreferencesOK);
		doneButton.setOnClickListener(doneButtonListener);
	}

	private OnClickListener gpsListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			CheckBox gpsBox = (CheckBox) v;
			preferences.setUseGPS(gpsBox.isChecked());
			MainActivity.getInstance().getEngine().getLocationComponent().toggleGPS();
			changesMade = true;
			//requiresStop = false;
		}
	};

	private OnClickListener memoryListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			CheckBox memoryBox = (CheckBox) v;
			preferences.setPreferMemoryCard(memoryBox.isChecked());
			changesMade = true;
			requiresStop = true;
		}
	};
	
	private OnClickListener pauseOnBackgroundListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			CheckBox memoryBox = (CheckBox) v;
			preferences.setPauseWhenInBackground(memoryBox.isChecked());
			changesMade = true;
			//requiresStop = false;
		}
	};

	private OnClickListener httpListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			if(preferences.getSavingMode() != Preferences.SAVE_HTTP || !preferences.isAlsoSaveToFileWhenInHTTPMode())
			{
				preferences.setSavingMode(Preferences.SAVE_HTTP);
				preferences.setAlsoSaveToFileWhenInHTTPMode(true);
				changesMade = true;
				requiresStop = true;
			}
		}
	};

	private OnClickListener fileListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			if(preferences.getSavingMode() != Preferences.SAVE_FILE)
			{
				preferences.setSavingMode(Preferences.SAVE_FILE);
				changesMade = true;
				requiresStop = true;
			}
		}
	};

	private OnClickListener noneListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			if(preferences.getSavingMode() != Preferences.SAVE_NO)
			{
				preferences.setSavingMode(Preferences.SAVE_NO);
				changesMade = true;
				requiresStop = true;
			}
		}
	};

	private OnClickListener doneButtonListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			finish();
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
		if(!isFinishing())
			MainActivity.getInstance().pause(); //to pause measuring if use wants that
	}

	@Override
	protected void onResume()
	{
		super.onPause();
		if(!isFinishing())
			MainActivity.getInstance().resume(); //to resume measuring
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if(isFinishing() && changesMade)
		{
			preferences.saveToStorage();
			if(MainActivity.getInstance().getEngine().isRunning() && requiresStop)
			{
				MainActivity.getInstance().stopMeasuring(true); //!!! will automatically restart with new settings (and show login if needed)
				requiresStop = false;
			}
			changesMade = false;
		}
	}
}
