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

package net.noisetube.ui.javame;

import net.noisetube.core.MeasurementStatistics;
import net.noisetube.model.Track;
import net.noisetube.util.MathME;
import net.noisetube.util.StringUtils;

import com.sun.lwuit.Container;
import com.sun.lwuit.Label;
import com.sun.lwuit.layouts.GridLayout;


/**
 * @author mstevens
 *
 */
public class StatisticsUIComponent extends Container
{

	private Label lblElapsed;
	private Label lblNumMeasurements;
	private Label lblAvrDBA;
	private Label lblMinDBA;
	private Label lblMaxDBA;
	private Label lblDistance;
	
	public StatisticsUIComponent()
	{
		super();
		
		lblElapsed = new Label("0s");
		lblNumMeasurements = new Label("0");
		lblAvrDBA = new Label("0.0 dB(A)");
		lblMinDBA = new Label("0.0 dB(A)");
		lblMaxDBA = new Label("0.0 dB(A)");
		lblDistance = new Label("0m");
		
		Container cntLines = new Container(new GridLayout(6, 2));
		addLabel(cntLines, new Label("Elapsed time:"));
		addLabel(cntLines, lblElapsed);
		addLabel(cntLines, new Label("#Measurements:"));
		addLabel(cntLines, lblNumMeasurements);
		addLabel(cntLines, new Label("Avr Leq(1s):"));
		addLabel(cntLines, lblAvrDBA);
		addLabel(cntLines, new Label("Min Leq(1s):"));
		addLabel(cntLines, lblMinDBA);
		addLabel(cntLines, new Label("Max Leq(1s):"));
		addLabel(cntLines, lblMaxDBA);
		addLabel(cntLines, new Label("Distance:"));
		addLabel(cntLines, lblDistance);
		
		addComponent(new Label("Statistics"));
		addComponent(cntLines);
	}
	
	private void addLabel(Container container, Label label)
	{
		label.getStyle().setFont(Fonts.SMALL_FONT);
		container.addComponent(label);
	}
	
	public void update(Track track)
	{
		lblElapsed.setText(StringUtils.formatTimeSpanColons(track.getTotalElapsedTime()));
		MeasurementStatistics stats = track.getStatistics();
		lblNumMeasurements.setText(Integer.toString(stats.getNumMeasurements()));
		lblAvrDBA.setText(MathME.roundTo(stats.getAvrdBA(), 2) + " dB(A)");
		lblMinDBA.setText(MathME.roundTo(stats.getMindBA(), 2) + " dB(A)");
		lblMaxDBA.setText(MathME.roundTo(stats.getMaxdBA(), 2) + " dB(A)");
		lblDistance.setText(StringUtils.formatDistance(stats.getDistanceCovered(), -2));
	}

}
