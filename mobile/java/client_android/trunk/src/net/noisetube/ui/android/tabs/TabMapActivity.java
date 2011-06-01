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

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.ReticleDrawMode;

import net.noisetube.R;
import net.noisetube.ui.ILocationUI;
import net.noisetube.ui.android.MainActivity;
import net.noisetube.location.LocationComponent;
import net.noisetube.location.android.AndroidNTCoordinates;
import net.noisetube.model.Measurement;
import net.noisetube.model.Track;
import net.noisetube.ui.android.NoiseLevelOverlay;
import net.noisetube.util.Logger;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ZoomButtonsController;
import android.widget.ZoomControls;

/**
 * @author sbarthol, mstevens
 *
 */
public class TabMapActivity extends MapActivity implements Tab, ILocationUI
{
	
	/* The following parameters store the Views and Layouts part of this Activity */
	//The view containing the Google Map
	private MapView mapView;
	private MapController mapController;
	private NoiseLevelOverlay noiseLevelOverlay;
	private TextView lblGPSState;
	
	private boolean gotFirstLocation = false;
	private boolean followLocation = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		MainActivity.getInstance().registerTab(this);

		setContentView(R.layout.tabmapactivity);

		/* Getting information concerning the child views */
		mapView = (MapView) findViewById(R.id.mapview);
		/* Initiate the map controls and display */
		mapController = mapView.getController();
		noiseLevelOverlay = MainActivity.getInstance().getNoiselevelOverlay();
		mapView.getOverlays().add(noiseLevelOverlay);
		
		lblGPSState = (TextView) findViewById(R.id.lblGPSState);
		lblGPSState.setBackgroundColor(Color.parseColor("#55FFFFFF"));
		
		ToggleButton tglFollowLocation = (ToggleButton) findViewById(R.id.tglFollowLocation);
		tglFollowLocation.setChecked(true);
		tglFollowLocation.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view)
			{
				followLocation = ((ToggleButton) view).isChecked();				
			}
		});
		
		try
		{
			LocationComponent lc = MainActivity.getInstance().getEngine().getLocationComponent();
			lc.setUI(this);
			lblGPSState.setText("GPS: " + LocationComponent.getGPSStateString(lc.getGPSState()));
		}
		catch(Exception e)
		{
			Logger.getInstance().error(e, "Could not register TabMapActivity as the location UI");
		}
		
		initMap(); //!!!
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return true;
	}

	private void initMap()
	{
		mapView.setClickable(true);
		mapView.setReticleDrawMode(ReticleDrawMode.DRAW_RETICLE_NEVER);
		mapController.setZoom(2);
		mapController.setCenter(new GeoPoint(0, 0));
		
		//Zoom controls in right-bottom corner:
		mapView.setBuiltInZoomControls(true);
		ZoomButtonsController zbc = mapView.getZoomButtonsController();
		ViewGroup container = zbc.getContainer();
		for(int i = 0; i < container.getChildCount(); i++)
		{
			View child = container.getChildAt(i);
			if (child instanceof ZoomControls)
			{
				FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
				lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
				child.requestLayout();
				break;
			} 
		}
		//TODO fix bug which makes zoom in/out buttons appear under instead of next to each other
		
		//Add points for measurements that are already in the track
		try
		{
			Track track = MainActivity.getInstance().getEngine().getTrack();
			Enumeration<Measurement> mEnum = track.getMeasurements();
			while(mEnum.hasMoreElements())
			{
				update(mEnum.nextElement(), track);
			}
		}
		catch(Exception ignore) { } //protect against nullness ;-)
	}
	
	/**
	 * When a new Measurement is send we add it to the overlay.
	 */
	public void update(Measurement newMeasurement, Track track)
	{
		try
		{
			if(followLocation && newMeasurement.getLocation() != null && newMeasurement.getLocation().hasCoordinates())
			{
				if(!gotFirstLocation)
				{
					mapController.setZoom(21);
					gotFirstLocation = true;
				}
				
				mapController.animateTo(((AndroidNTCoordinates) newMeasurement.getLocation().getCoordinates()).toGeoPoint()); //only animate for new points, so don't use last point of noise overlay
			}
			mapView.postInvalidate();
		}
		catch(Exception ignore) { } //something wasn't initialized (yet)(?)
	}

	public boolean mustRemainInformed()
	{
		return false;
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	public void start()
	{
		gotFirstLocation = false;
		//noiseLevelOverlay = MainActivity.getInstance().getNoiselevelOverlay();
	}

	public void stop()
	{
		//Do nothing
	}

	public void refresh()
	{
		//Do nothing
	}

	public void gpsStateChanged(final int newGPSState, int previousState)
	{
		runOnUiThread(new Runnable()
		{
		    public void run()
		    {
		    	lblGPSState.setText("GPS: " + LocationComponent.getGPSStateString(newGPSState));
		    }
		});
	}
	
}
