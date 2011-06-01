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

package net.noisetube.ui.android.tabs;

import java.util.Enumeration;

import net.noisetube.R;
import net.noisetube.model.Measurement;
import net.noisetube.model.Track;
import net.noisetube.ui.android.MainActivity;
import net.noisetube.ui.android.Toaster;
import net.noisetube.util.CyclicQueue;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This Activity is used to tag the last 1 to X (default: 10) measurements with a custom tag(s). There where some design choices made: 1. You can tag the last 1
 * to 10 measurements that where measured BEFORE you opened this tab. 2. If you do not close this tab and keep on tagging, you'll tag the same measurements. 3.
 * The measuring isn't automatically being paused, since this is also a tab, and should therefore not act radically different from other tabs. The pause button
 * is also always available. 4. We do need to take into account that when the measuring isn't paused, the locally stored measurements might get outdated. In
 * other word, when we wait to long with adding the tag, they may have been thrown out the track and may have already be saved, without tag.
 * 
 * TODO dropdown box of suggested &/or previously used tags
 * 
 * @author sbartholn, mstevens
 */
public class TabTagActivity extends Activity implements Tab
{

	private static int MAX_TAGGABLE_MEASUREMENTS_SERIES = 10;

	private EditText txtTag;
	private TextView txtNumberOfMeasurements;
	private Button btnTag;
	private int numberOfMeasurementsToTag = MAX_TAGGABLE_MEASUREMENTS_SERIES; // default at max
	private boolean needsNewTrack = false;
	private CyclicQueue tagQueue = new CyclicQueue();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		MainActivity.getInstance().registerTab(this);

		setContentView(R.layout.tabtagactivity);
		
		/* Getting information concerning the child views */
		txtTag = (EditText) findViewById(R.id.txt_tag);
		txtTag.setFocusable(true);
		txtTag.setFocusableInTouchMode(true);
		txtNumberOfMeasurements = (TextView) findViewById(R.id.txtNumberOfMeasurements);
		txtNumberOfMeasurements.setText(Integer.toString(numberOfMeasurementsToTag));
		
		//Tag button
		btnTag = (Button) findViewById(R.id.tagButton);
		btnTag.setOnClickListener(onTagListener);
		
		//+/- buttons
		((Button) findViewById(R.id.plusButton)).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if(numberOfMeasurementsToTag < MAX_TAGGABLE_MEASUREMENTS_SERIES)
					txtNumberOfMeasurements.setText(Integer.toString(++numberOfMeasurementsToTag));
			}
		});
		((Button) findViewById(R.id.minButton)).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if(numberOfMeasurementsToTag > 1)
					txtNumberOfMeasurements.setText(Integer.toString(--numberOfMeasurementsToTag));
			}
		});
		
		//Spinner spinner = (Spinner) findViewById(R.id.amount_spinner);
		//ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.amount_of_measurements_array, android.R.layout.simple_spinner_item);
		//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//spinner.setAdapter(adapter);
		//spinner.setOnItemSelectedListener(...);
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		(new Handler()).post(new Runnable()
		{
		    public void run()
		    {
		        txtTag.requestFocus();
		    }
		});
	}

	public void update(Measurement newMeasurement, Track track)
	{
		// When we haven't stored the last X measurements since we chose to tag some of them...
		if(needsNewTrack == true)
		{
			// ... we need to store them and make sure no new ones will be stored.
			needsNewTrack = false;

			Enumeration enumerator = track.getMeasurementsNewestFirst();
			int mnumber = 0;
			if(enumerator.hasMoreElements())
				// We skip the newest element, since this element is the first measurement recorded when we where in the
				// current screen. This element should not be part of the tagging, since the user didn't saw this element
				// yet when he chose to tag an interval.
				enumerator.nextElement();
			while(mnumber++ < MAX_TAGGABLE_MEASUREMENTS_SERIES && enumerator.hasMoreElements())
			{
				tagQueue.offer(enumerator.nextElement());
			}
		}
	}
	
	private OnClickListener onTagListener = new OnClickListener()
	{
		//adds the tag that can currently be found to the tag-variable, to the stored measurements.
		public void onClick(View v)
		{
			if(txtTag.getText().toString().equals(""))
				return;
			// Since we use the CyclicQueue, the element that is pop'ed, is the element first added to the queue, is
			// the newest measurement. So we may just pop for example the first five elements and tag these.
			// In other words, we do not need to worry about not tagging the newest five (which could be possible, since
			// we store the newest measurements and might as well tag the 5 oldest from these measurements.
			int toTag = Math.min(tagQueue.getSize(), numberOfMeasurementsToTag);
			for(int i = 0; i < toTag; i++)
			{
				Measurement m = (Measurement) tagQueue.getElement(i);
				m.addUserTag(txtTag.getText().toString());
			}
			txtTag.setText(""); //clear field
			//TODO store tag (in pref's?) for suggestion dropdown
			Toaster.displayToast("The last " + toTag + " measurements have been tagged with \"" + txtTag.getText().toString() + "\"");
			MainActivity.getInstance().showGraph(); //go back to graph tab
		}
	};

	public boolean mustRemainInformed()
	{
		return false;
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		needsNewTrack = true;
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		needsNewTrack = false;
		tagQueue = new CyclicQueue();
	}

	public void start()
	{
		txtTag.setEnabled(true);
		btnTag.setEnabled(true);
	}

	public void stop()
	{
		txtTag.setEnabled(false);
		btnTag.setEnabled(false);
		MainActivity.getInstance().showGraph(); //go back to graph tab
	}

	public void refresh()
	{
		txtTag.setEnabled(MainActivity.getInstance().getEngine().isRunning());
		btnTag.setEnabled(MainActivity.getInstance().getEngine().isRunning());
	}

}
