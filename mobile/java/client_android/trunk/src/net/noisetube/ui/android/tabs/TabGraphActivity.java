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

import net.noisetube.R;
import net.noisetube.core.MeasurementStatistics;
import net.noisetube.model.Measurement;
import net.noisetube.model.Track;
import net.noisetube.ui.android.MainActivity;
import net.noisetube.ui.android.views.SPLGraphView;
import net.noisetube.ui.android.views.SPLView;
import net.noisetube.ui.android.views.StatisticsView;
import net.noisetube.util.StringUtils;

import android.app.Activity;
import android.os.Bundle;


/**
 * This activity displays the measurements stored in a track by plotting them on a graph, showing the current db(A) level
 * and displaying additional information.
 * 
 * @author sbarthol, mstevens
 *
 */
public class TabGraphActivity extends Activity implements Tab
{

	/* The following parameters store the Views and Layouts part of this Activity */
	// The view containing the graph:
	private SPLGraphView graphView;
	// The view containing a number:
	private SPLView splView;
	// The stat views:
	private StatisticsView statTime;
	private StatisticsView statMinMaxAvg;
	private StatisticsView statDistance;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		MainActivity.getInstance().registerTab(this);
		
		setContentView(R.layout.tabgraphactivity);
		/* Getting information concerning the child views */
		graphView = (SPLGraphView) findViewById(R.id.splGraphView);
		splView = (SPLView) findViewById(R.id.splView);
		statTime = (StatisticsView) findViewById(R.id.statTime);
		statMinMaxAvg = (StatisticsView) findViewById(R.id.statMinMaxAvg);
		statDistance = (StatisticsView) findViewById(R.id.statDistance);
	}

	public void update(final Measurement newMeasurement, final Track track)
	{
		graphView.getSoundLevelGraph().update(track);
		splView.update(newMeasurement);
				
		//Statistics
		final MeasurementStatistics stats = track.getStatistics();
				
		statTime.setText(StringUtils.formatTimeSpanColons(track.getTotalElapsedTime())); //"# " + stats.getNumMeasurements()
		statMinMaxAvg.setText((int) stats.getMindBA() + "/" + (int) stats.getMaxdBA() + "/" + (int) stats.getAvrdBA());
		statDistance.setText((stats.getDistanceCovered() == 0 ? "--" : StringUtils.formatDistance(stats.getDistanceCovered(), -2)));
				
		graphView.postInvalidate();
		splView.postInvalidate();
		statTime.postInvalidate();
		statMinMaxAvg.postInvalidate();
		statDistance.postInvalidate();
	}

	public boolean mustRemainInformed()
	{
		return false;
	}

	public void start() { }

	public void stop()
	{
		splView.clear();
//		statTime.setText("00:00:00");
//		statMinMaxAvg.setText("");
//		statDistance.setText("");
	}

	public void refresh() { }
	
}
