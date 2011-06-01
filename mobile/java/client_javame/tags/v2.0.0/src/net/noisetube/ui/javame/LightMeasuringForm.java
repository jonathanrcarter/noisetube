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

import com.sun.lwuit.Container;
import com.sun.lwuit.Label;
import com.sun.lwuit.layouts.GridLayout;
import com.sun.lwuit.table.TableLayout;
import com.sun.lwuit.table.TableLayout.Constraint;

import net.noisetube.core.MeasurementStatistics;
import net.noisetube.core.javame.MainMIDlet;
import net.noisetube.model.Measurement;
import net.noisetube.model.Track;
import net.noisetube.ui.javame.SPLUIComponent;
import net.noisetube.util.MathME;

/**
 * Light-weight ("dummy proof") MeasuringForm
 * 
 * @author mstevens
 */
public class LightMeasuringForm extends MeasuringForm
{
	
	private SPLUIComponent splUI;
	private Label lblAvrDBA;
	private Label lblMinDBA;
	private Label lblMaxDBA;
	
	protected void initialize()
	{
		//UI
		splUI = new SPLUIComponent();

		setScrollable(false);
		TableLayout tl = new TableLayout(2, 1);		
		setLayout(tl);

		Constraint constr = tl.createConstraint();
		constr.setHeightPercentage(90);
		constr.setWidthPercentage(100);
		addComponent(constr, splUI);
		
		lblAvrDBA = new Label((MainMIDlet.BRUSSENSE ? "Gem" : "Avr") + ": 0.0");
		lblMinDBA = new Label("Min: 0.0");
		lblMaxDBA = new Label("Max: 0.0");

		Container cntStats = new Container(new GridLayout(1, 3));
		addLabel(cntStats, lblAvrDBA);
		addLabel(cntStats, lblMinDBA);
		addLabel(cntStats, lblMaxDBA);

		addComponent(cntStats);
		
		getStyle().setMargin(0, 0, 0, 0);
		getStyle().setPadding(0, 0, 0, 0);
	}
	
	protected void updateCommands()
	{
		removeAllCommands();
		if(engine.isRunning())
		{
			cmdPauseResume.setCommandName(engine.isPaused() ? "Resume" : "Pause");
			addCommand(cmdPauseResume);
		}
		addCommand(cmdExit);
	}
	
	public void newMeasurement(Track track, Measurement measurement)
	{		
		splUI.update(track, measurement);
		long elapsedMS = track.getTotalElapsedTime();
		long seconds = (elapsedMS / 1000) % 60;
		long minutes = (elapsedMS / (60 * 1000)) % 60;
		long hours = (minutes / 60) % 60;
		String timeString = (hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
		String title = "#" + track.getStatistics().getNumMeasurements() + " (" + timeString + ")";
		setTitle(title);
		MeasurementStatistics stats = track.getStatistics();
		lblAvrDBA.setText((MainMIDlet.BRUSSENSE ? "Gem" : "Avr") + ": " + MathME.roundTo(stats.getAvrdBA(), 2));
		lblMinDBA.setText("Min: " + MathME.roundTo(stats.getMindBA(), 2));
		lblMaxDBA.setText("Max: " + MathME.roundTo(stats.getMaxdBA(), 2));
	}

	private void addLabel(Container container, Label label)
	{
		label.getStyle().setFont(Fonts.SMALL_FONT);
		container.addComponent(label);
	}
	
}
