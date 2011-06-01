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

import net.noisetube.config.Preferences;

import net.noisetube.location.LocationComponent;
import net.noisetube.model.Measurement;
import net.noisetube.model.Track;

import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.table.TableLayout;
import com.sun.lwuit.table.TableLayout.Constraint;

/**
 * Full blown MeasuringForm
 * 
 * @author maisonneuve, mstevens
 */
public class FullMeasuringForm extends MeasuringForm
{

	//GUI
	protected Component focusComponent;
	protected LocationUIComponent locationUI;
	protected TaggingUIComponent taggingUI;
	protected LogUIComponent logUI;
	protected SPLUIComponent splUI;
	protected StatisticsUIComponent statisticsUI;
	protected TabComponent tabs;
	protected Container tabsAndComponent;

	protected void initialize()
	{
		//Form
		setScrollable(false);
		getStyle().setMargin(0, 0, 0, 0);
		getStyle().setPadding(0, 0, 0, 0);
		TableLayout tl = new TableLayout(2, 1);		
		setLayout(tl);
		
		//SPL (upper part of GUI)
		splUI = new SPLUIComponent();
		Constraint constr = tl.createConstraint();
		constr.setHeightPercentage(50);
		constr.setWidthPercentage(100);
		addComponent(constr, splUI);
		
		//Tabs+compoments (bottom part of GUI)
		tabsAndComponent = new Container(new BorderLayout());
		addComponent(tabsAndComponent);
		
		tabs = new TabComponent(this);
		tabs.setScrollableY(true);
		tabs.setLocationComponent(engine.getLocationComponent());
		statisticsUI = new StatisticsUIComponent();
		statisticsUI.setScrollableY(true);
		statisticsUI.setIsScrollVisible(true);
		locationUI = new LocationUIComponent(this);
		engine.getLocationComponent().setUI(locationUI); //!!!
		taggingUI = new TaggingUIComponent();
		logUI = new LogUIComponent();
		logUI.setScrollableY(true);
		tabsAndComponent.addComponent(BorderLayout.WEST, tabs);

		//Update location UI component:
		locationUI.gpsStateChanged(LocationComponent.GPS_STATE_DISABLED, LocationComponent.GPS_STATE_DISABLED);

		//Log file location:
		if(log.isFileModeActive())
			logUI.setPrimaryMessage("Log file location: " + log.getLogFilePath().substring(8));
	}

	protected void updateCommands()
	{
		removeAllCommands();
		if(engine.isRunning())
		{
			cmdPauseResume.setCommandName(engine.isPaused() ? "Resume" : "Pause");
			addCommand(cmdPauseResume);
			addCommand(cmdStop);
		}
		else
			addCommand(cmdStart);
		addCommand(cmdExit);
		addCommand(cmdAbout);
		if(preferences.getSavingMode() == Preferences.SAVE_HTTP && preferences.isAuthenticated())
			addCommand(cmdSwitchUser);
		addCommand(cmdPreferences);
		//setBackCommand(cmdExit);
	}
	
	public void displayLocation()
	{
		displaySubComponent(locationUI);
	}

	public void displayTagging()
	{
		//tabs.btnTags.getStyle().setBgColor(0xffffff);
		displaySubComponent(taggingUI);
	}

	public void displayLog()
	{
		displaySubComponent(logUI);
		logUI.displayLog();
	}
	
	public void displayStatistics()
	{
		displaySubComponent(statisticsUI);
	}

	public void displaySubComponent(Component component)
	{
		tabsAndComponent.removeComponent(component);
		tabsAndComponent.addComponent(BorderLayout.CENTER, component);
		//focusComponent = component;
		repaint();
	}

	/**
	 * @return the tabs
	 */
	public TabComponent getTabs()
	{
		return tabs;
	}

	/**
	 * @return the log_ui
	 */
	public LogUIComponent getLogUI()
	{
		return logUI;
	}

	/**
	 * @return the loudness_ui
	 */
	public SPLUIComponent getLoudnessUI()
	{
		return splUI;
	}
	
	/**
	 * @return the taggingUI
	 */
	public TaggingUIComponent getTaggingUI()
	{
		return taggingUI;
	}

	/**
	 * called from Engine
	 * 
	 */
	public void newMeasurement(Track track, Measurement measurement, Measurement savedMeasurement)
	{
		splUI.update(track, measurement);
		statisticsUI.update(track);
		if(preferences.isUseDoseMeter())
			setTitle("Measuring" + "; D=" + (int) Math.floor(engine.getDoseMeter().getDose() + 0.5d) + "%");
	}

	public void measuringStopped(Track track)
	{
		super.measuringStopped(track);
		splUI.reset();		
	}
	
	protected void sizeChanged(int w, int h)
	{
		super.sizeChanged(w, h);
	}

}
