/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Phone Tester (Java implementation; Android version)
 *  
 *  Copyright (C) 2010-2011 Vrije Universiteit Brussel (BrusSense team)
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
 * ------------------------------------------------------------------------------
 *  NoiseTube Phone Tester uses the NoiseTube Mobile library:
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011
 *  Used under the terms of the GNU Lesser General Public License, version 2.1.
 *  NoiseTube project source code repository: http://code.google.com/p/noisetube
 * --------------------------------------------------------------------------------
 *  More information:
 *   - NoiseTube project website: http://www.noisetube.net
 *   - Sony Computer Science Laboratory Paris: http://csl.sony.fr
 *   - VUB BrusSense team: http://www.brussense.be
 * --------------------------------------------------------------------------------
 */

package net.noisetube.tester.android;

import net.noisetube.tester.android.R;
import net.noisetube.audio.calibration.Calibrator;
import net.noisetube.config.android.AndroidDevice;
import net.noisetube.core.android.AndroidNTClient;
import net.noisetube.model.IMeasurementListener;
import net.noisetube.model.Measurement;
import net.noisetube.ui.android.Toaster;
import net.noisetube.util.Logger;
import net.noisetube.util.StringUtils;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author mstevens
 *
 */
public class TesterActivity extends Activity implements IMeasurementListener
{

	//STATICS-------------------------------------------------------

	//Client info
	static public AndroidNTClient ntClient;
	static public String CLIENT_TYPE = "NoiseTubePhoneTesterAndroid";
	static public String CLIENT_VERSION = "v1.1.0";
	static public boolean CLIENT_IS_TEST_VERSION = true;
	
	//Environment
	static
	{	//TODO comment both out for production build:
		//NTClient.ENVIRONMENT = NTClient.PHONE_DEV_ENV;
		//NTClient.ENVIRONMENT = NTClient.EMULATOR_ENV;
	}

	//DYNAMICS------------------------------------------------------
	private Logger log;
	private Calibrator calibrator;
	private Button btnStartStop;
	private TextView txtAudioSpec;
	private TextView txtElapsedTime;
	private TextView txtNumberOfMeasurements;
	private TextView txtDB;
	private TextView txtDBA;

	/** Called when the activity is first created.
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		CLIENT_VERSION = "v" + getSoftwareVersion();
		
		//set client info on AndroidNTClient:
		AndroidNTClient.CLIENT_TYPE = TesterActivity.CLIENT_TYPE;
		AndroidNTClient.CLIENT_VERSION = TesterActivity.CLIENT_VERSION;
		AndroidNTClient.CLIENT_IS_TEST_VERSION = TesterActivity.CLIENT_IS_TEST_VERSION;
		
		try
		{
			//Find log instance:
			log = Logger.getInstance();

			//NTClient...
			ntClient = new AndroidNTClient(this);
			
			//Calibrator
			calibrator = new Calibrator(ntClient.getDevice().getAudioSpecification(), this, ((AndroidDevice) ntClient.getDevice()).getAndroidDeviceInfo());
			
			//enable file mode in the logger:
			if(ntClient.getPreferences() != null)
			{
				log.enableFileMode();
				log.disableLogBuffer();
			}

			//GUI:
			txtAudioSpec = (TextView) findViewById(R.id.txtAudioSpec);
			txtAudioSpec.setText(calibrator.getAudioSpec().toString());
			txtElapsedTime = (TextView) findViewById(R.id.txtElapsedTime);
			txtNumberOfMeasurements = (TextView) findViewById(R.id.txtNrOfMeasurements);
			txtDB = (TextView) findViewById(R.id.txtDB);
			((TextView) findViewById(R.id.txtLblDB)).setText(Html.fromHtml("L<sub>eq,1s</sub>:"));
			((TextView) findViewById(R.id.txtLblDBA)).setText(Html.fromHtml("L<sub>Aeq,1s</sub>:"));
			txtDBA = (TextView) findViewById(R.id.txtDBA);
			
			btnStartStop = (Button) findViewById(R.id.btnStartStop);
			//Bind the button:
			btnStartStop.setOnClickListener(startStopButtonListener);
		}
		catch (Exception e)
		{
			log.error(e, "TesterActivity.onCreate()");
			Toaster.displayToast("Unable to run NoiseTube");
			Toaster.displayToast(e.getMessage());
		}
	}

	/**
	 * Gets the software version retrieved from the Manifest.
	 */
	private String getSoftwareVersion()
	{
		try
		{
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			return packageInfo.versionName;
		}
		catch(Exception e)
		{
			return CLIENT_VERSION;
		}
	}
	
	private OnClickListener startStopButtonListener = new OnClickListener() {
		public void onClick(View v) {
			if(calibrator.isRunning())
			{
				calibrator.stop();
				btnStartStop.setText("Start");
				Toaster.displayShortToast("Measuring stopped");
			}
			else
			{
				calibrator.start();
				btnStartStop.setText("Stop");
				Toaster.displayShortToast("Measuring started");
			}
		}
	};

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		//This has been replaced by back-key specific functionality.
		if(isFinishing() && calibrator != null)
			calibrator.stop();
	}

	@Override
	public void receiveMeasurement(final Measurement m)
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				txtElapsedTime.setText(StringUtils.formatTimeSpanColons(calibrator.getElapsedTimeMS()));
				txtNumberOfMeasurements.setText(Integer.toString(calibrator.getMeasurementCount()));
				txtDB.setText(Double.toString(m.getLeqDB()).substring(0, 8) + " dB");
				txtDBA.setText(Double.toString(m.getLeqDBA()).substring(0, 8) + " dB(A)");
			}
		});
		
	}
	
}