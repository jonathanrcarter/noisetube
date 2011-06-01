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
import net.noisetube.core.Engine;
import net.noisetube.core.NTClient;
import net.noisetube.core.javame.MainMIDlet;
import net.noisetube.model.Track;
import net.noisetube.ui.IMeasurementUI;
import net.noisetube.util.Logger;

import com.sun.lwuit.Command;
import com.sun.lwuit.Form;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

/**
 * MeasuringForm (main form of the program)
 * 
 * @author mstevens, maisonneuve
 */
public abstract class MeasuringForm extends Form implements IMeasurementUI, ActionListener
{
	
	protected static String measuringTitle = "Measuring noise exposure";
	
	protected Logger log = Logger.getInstance();
	protected MainMIDlet midlet;
	protected NTClient ntClient;
	protected Engine engine;
	protected Preferences preferences;
	
	//COMMANDS
	public Command cmdStart = new Command("Start");
	public Command cmdStop = new Command("Stop");
	public Command cmdPauseResume = new Command(MainMIDlet.BRUSSENSE ? "Pauze" : "Pause");
	public Command cmdExit = new Command(MainMIDlet.BRUSSENSE ? "Afsluiten" : "Exit");
	public Command cmdPreferences = new Command("Preferences");
	public Command cmdSwitchUser = new Command("Switch user");
	public Command cmdAbout = new Command("About");

	public MeasuringForm()
	{
		super(measuringTitle);
		ntClient = NTClient.getInstance();
		preferences = ntClient.getPreferences();
		midlet = MainMIDlet.getInstance();
		engine = midlet.getEngine();
		initialize();
		updateCommands();
		addCommandListener(this);
	}
	
	protected abstract void initialize();
	
	protected abstract void updateCommands();
	
	public void measuringStarted(Track track)
	{
		updateCommands();
		setTitle(measuringTitle);
	}

	public void measuringPaused(Track track)
	{
		updateCommands();
		setTitle("Paused");
	}
	
	public void measuringResumed(Track track)
	{
		updateCommands();
		setTitle(measuringTitle);
	}
	
	public void measuringStopped(Track track)
	{
		updateCommands();
		setTitle("Stopped");
	}
	
	public void actionPerformed(ActionEvent actionevent)
	{
		try
		{
			Command cmd = actionevent.getCommand();
			if(cmd == null)
				return;
			if(cmd == cmdStart)
			{
				midlet.startMeasuring();
				updateCommands();
			}
			else if(cmd == cmdPauseResume)
			{
				if(engine.isPaused())
					engine.resume();
				else
				{
					engine.pause();
					setTitle("Paused");
				}
				updateCommands();
			}
			else if(cmd == cmdStop)
				engine.stop();
			else if(cmd == cmdExit)
				midlet.showConfirmExitForm();
			else if(cmd == cmdSwitchUser)
			{
				engine.stop();
				midlet.showLoginForm();
			}
			else if(cmd == cmdPreferences)
				midlet.showPreferencesForm();
			else if(cmd == cmdAbout)
				midlet.showAboutForm();
		}
		catch(Exception e)
		{
			log.error(e, "MeasuringForm.actionPerformed()");
		}
	}

}
