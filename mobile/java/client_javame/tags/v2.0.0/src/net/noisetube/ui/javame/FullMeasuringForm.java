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

import net.noisetube.core.NTClient;
import net.noisetube.core.javame.MainMIDlet;
import net.noisetube.location.LocationComponent;
import net.noisetube.model.Measurement;
import net.noisetube.model.Track;

import com.sun.lwuit.Component;
import com.sun.lwuit.layouts.BorderLayout;

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

	public FullMeasuringForm()
	{
		ntClient = NTClient.getInstance();
		preferences = ntClient.getPreferences();
		midlet = MainMIDlet.getInstance();
		engine = midlet.getEngine();
	}

	protected void initialize()
	{
		//GUI
		tabs = new TabComponent(this);

		splUI = new SPLUIComponent();
		splUI.setPreferredH(95);
		locationUI = new LocationUIComponent(this);
		engine.getLocationComponent().setUI(locationUI); //!!!
		taggingUI = new TaggingUIComponent();
		taggingUI.setTaggingComponent(engine.getTaggingComponent());
		logUI = new LogUIComponent();
		statisticsUI = new StatisticsUIComponent();

		setScrollable(false);
		setLayout(new BorderLayout());

		tabs.setLocationComponent(engine.getLocationComponent());
		tabs.setTaggingComponent(engine.getTaggingComponent());

		addComponent(BorderLayout.NORTH, splUI);
		addComponent(BorderLayout.WEST, tabs);

		getStyle().setMargin(0, 0, 0, 0);
		getStyle().setPadding(0, 0, 0, 0);

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
		removeComponent(component);
		addComponent(BorderLayout.CENTER, component);
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
	public void newMeasurement(Track track, Measurement measurement)
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
