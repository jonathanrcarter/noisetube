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

import net.noisetube.config.NTAccount;
import net.noisetube.config.Preferences;
import net.noisetube.core.NTClient;
import net.noisetube.ui.android.MainActivity;
import net.noisetube.io.NTWebAPI;
import net.noisetube.util.Logger;
import net.noisetube.R;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @author sbarthol, mstevens
 *
 */
public class LoginActivity extends Activity
{

	private Logger log = Logger.getInstance();
	private EditText loginName;
	private EditText loginPassword;
	
	private Preferences preferences;
	private NTWebAPI api;

	/** Called when the activity is first created.
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginactivity);

		preferences = NTClient.getInstance().getPreferences();
		api = new NTWebAPI();
		
		// Bind the two text boxes:
		loginName = (EditText) findViewById(R.id.txt_name);
		loginName.setTag("");
		loginPassword = (EditText) findViewById(R.id.txt_password);
		loginPassword.setTag("");
		
		// Bind the two buttons:
		Button loginButton = (Button) findViewById(R.id.loginButton);
		Button skipButton = (Button) findViewById(R.id.skipButton);
		
		// Register the onClick listener:
		loginButton.setOnClickListener(loginButtonListener);
		skipButton.setOnClickListener(skipButtonListener);
		
		TextView registerText = (TextView) findViewById(R.id.registerHereText);
		
		registerText.setTextSize(20);
		registerText.setText(Html.fromHtml("<br>Don't have an account yet? Register <a href=\"http://www.noisetube.net/signup\">here</a>"));
		registerText.setMovementMethod(LinkMovementMethod.getInstance());
	}

	/**
	 * There are two ways to implement a button: an OnClickListener or binding the method call in the XML.
	 * We use the first one.
	 */
	private OnClickListener loginButtonListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			if(loginName.getText().toString().length() > 0 && loginPassword.getText().toString().length() > 0)
			{
				NTAccount account = null;
				try
				{
					account = api.login(loginName.getText().toString().trim(), loginPassword.getText().toString().trim());
				}
				catch(Exception e)
				{
					log.error(e, "Error upon authentication");
					preferences.setSavingMode(NTClient.getInstance().getDevice().supportsFileAccess() ? Preferences.SAVE_FILE : Preferences.SAVE_NO);
					preferences.saveToStorage();
					Toaster.displayToast("Connection problem. Data will " + (preferences.getSavingMode() == Preferences.SAVE_FILE ? "be locally stored" : "not be stored"));
					MainActivity.getInstance().startMeasuring();
					finish();
					return;
				}
				if(account != null)
				{
					log.debug("Logged in (API key: " + account.getAPIKey() + ")");
					preferences.setAccount(account);
					preferences.saveToStorage();
					Toaster.displayShortToast("Login successful");
					Toaster.displayToast("Note that tracks & locations are shared publicly by default. Settings can be changed on the NoiseTube website.");
					MainActivity.getInstance().startMeasuring();
					finish();
					return;
				}
				else
				{
					Toaster.displayToast("Wrong username-password combination");
				}
			}
		}
	};
	
	private OnClickListener skipButtonListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			preferences.setSavingMode(NTClient.getInstance().getDevice().supportsFileAccess() ? Preferences.SAVE_FILE : Preferences.SAVE_NO);
			preferences.saveToStorage();
			Toaster.displayToast("Preferences changed. Data will " + (preferences.getSavingMode() == Preferences.SAVE_FILE ? "be locally stored" : "not be stored"));
			MainActivity.getInstance().startMeasuring();
			finish();
		}
	};
	
}
