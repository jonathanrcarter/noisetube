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
import net.noisetube.core.Engine;
import net.noisetube.core.MeasurementStatistics;
import net.noisetube.model.Track;
import net.noisetube.util.MathME;
import net.noisetube.util.StringUtils;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author mstevens
 *
 */
public class TrackSummaryDialog extends Dialog
{

	private Track track;
	private boolean restartMeasuringAfterwards;
	
	private TextView txtWaiting;
	private Button btnOK;
	
	public TrackSummaryDialog(Context context, Track track, boolean restartMeasuringAfterwards)
	{
		super(context);
		
		this.track = track;
		this.restartMeasuringAfterwards = restartMeasuringAfterwards;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setCancelable(false); //will be set to true in stopWaiting()
		setOnCancelListener(new OnCancelListener()
		{
			public void onCancel(DialogInterface dialog)
			{
				if(restartMeasuringAfterwards)
					MainActivity.getInstance().startMeasuring(); //will show login if needed
			}
		});
		
		setContentView(R.layout.tracksummary);
		setTitle(R.string.summary_title);

		txtWaiting = (TextView) findViewById(R.id.txtSummaryWaiting);
		txtWaiting.setText("Waiting (max. " + Math.round(Engine.WAIT_FOR_SAVING_TO_COMPLETE_MS / 1000f) + "s) for last measurements to be saved...");
		
		if(track.isTrackIDSet())
			((TextView) findViewById(R.id.txtTrackID)).setText(Integer.toString(track.getTrackID()));
		((TextView) findViewById(R.id.txtSummaryElapsedTime)).setText(StringUtils.formatTimeSpanColons(track.getTotalElapsedTime()));
		
		//Statistics:
		MeasurementStatistics stats = track.getStatistics();
		((TextView) findViewById(R.id.txtSummaryNrOfMeasurements)).setText(Integer.toString(stats.getNumMeasurements()));
		((TextView) findViewById(R.id.lblSummaryMinLeq)).setText(Html.fromHtml("Minimum L<sub>eq,1s</sub>:"));
		((TextView) findViewById(R.id.txtSummaryMinLeq)).setText(MathME.roundTo(stats.getMindBA(), 2) + " dB(A)");
		((TextView) findViewById(R.id.lblSummaryMaxLeq)).setText(Html.fromHtml("Maximum L<sub>eq,1s</sub>:"));
		((TextView) findViewById(R.id.txtSummaryMaxLeq)).setText(MathME.roundTo(stats.getMaxdBA(), 2) + " dB(A)");
		((TextView) findViewById(R.id.lblSummaryAvgLeq)).setText(Html.fromHtml("Average L<sub>eq,1s</sub>:"));
		((TextView) findViewById(R.id.txtSummaryAvgLeq)).setText(MathME.roundTo(stats.getAvrdBA(), 2) + " dB(A)");
		((TextView) findViewById(R.id.txtSummaryDistance)).setText(StringUtils.formatDistance(stats.getDistanceCovered(), -2));
		
		//Button & events:
		btnOK = (Button) findViewById(R.id.btnSummaryOK);
		btnOK.setEnabled(false);
		btnOK.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View btn)
			{
				cancel(); //will trigger OnCancel event
			}
		});
	}
	
	public void stopWaiting(boolean savingCompleted)
	{
		setCancelable(true); //!!!
		txtWaiting.setText(savingCompleted ? R.string.summary_waiting_done : R.string.summary_waiting_failed);
		btnOK.setEnabled(true);
	}
	
}
