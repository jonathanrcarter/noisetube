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

import java.util.Enumeration;
import java.util.Vector;

import net.noisetube.config.javame.JavaMEDevice;
import net.noisetube.config.javame.JavaMEPreferences;
import net.noisetube.core.Engine;
import net.noisetube.core.NTClient;
import net.noisetube.core.javame.MainMIDlet;
import net.noisetube.util.ComboList;

import com.sun.lwuit.CheckBox;
import com.sun.lwuit.ComboBox;
import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.List;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.GridLayout;

/**
 * Preference Form
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class PreferenceForm extends Form implements ActionListener
{

	private final JavaMEPreferences preferences;
	private Engine engine;
	
	//Form elements:
	private Container container;
	private Vector settings;
	
	//Commands:
	private Command cmdOK;
	private Command cmdCancel;
	
	public PreferenceForm(Engine engine)
	{
		super("Preferences");
		preferences = (JavaMEPreferences) NTClient.getInstance().getPreferences();
		this.engine = engine;
		
		container = new Container(new GridLayout(5, 1));
		addComponent(container);
		settings = new Vector();
		
		//Add setting widgets to the container:
		initialiseSettings();
		
		//Commands:
		cmdOK = new Command("OK");
		cmdCancel = new Command("Cancel");
		addCommand(cmdOK);
		addCommand(cmdCancel);
		
		addCommandListener(this);
	}
	
	protected void initialiseSettings()
	{
		//GPS
		if(NTClient.getInstance().getDevice().supportsPositioning())
		{
			final CheckBox gps_box = new CheckBox("Use GPS for localisation");
			gps_box.setSelected(preferences.isUseGPS()); //set default/currently active mode
			settings.addElement(new PreferencesSetting()
								{
									public void set()
									{
										preferences.setUseGPS(gps_box.isSelected());
									}
								});
			container.addComponent(gps_box);
		}

		//Memory card
		if(NTClient.getInstance().getDevice().supportsFileAccess())
		{
			final CheckBox memoryCard_box = new CheckBox("Prefer memory card");
			memoryCard_box.setSelected(preferences.isPreferMemoryCard()); //set default/currently active mode:
			memoryCard_box.setWidth(container.getWidth());
			settings.addElement(new PreferencesSetting()
								{
									public void set()
									{
										preferences.setPreferMemoryCard(memoryCard_box.isSelected());
									}
								});
			container.addComponent(memoryCard_box);
		}

		//SAVING
		Container savingContainer = new Container();
		savingContainer.setLayout(new GridLayout(1, 2));
		savingContainer.addComponent(new Label("Data saving:"));
		final List saving_list = new List();
		//Populate list:
		final ComboList savingModes = preferences.getAvailableSavingModes();
		for(int i = 0; i < savingModes.size(); i++)
			saving_list.addItem(savingModes.getLabelAtIdx(i));
		saving_list.setSelectedIndex(savingModes.getDefaultIdx()); //Set default/currently active mode		
		settings.addElement(new PreferencesSetting()
							{
								public void set()
								{
									preferences.setSavingMode(savingModes.getValueAtIdx(saving_list.getSelectedIndex()));
								}
							});		
		savingContainer.addComponent(new ComboBox(saving_list.getModel()));
		container.addComponent(savingContainer);

		//Block screensaver
		if(JavaMEDevice.supportsNokiaUIAPI())
		{
			final CheckBox blockScreensaver_box = new CheckBox("Disable screensaver/power saving");
			// Set default/currently active mode:
			blockScreensaver_box.setSelected(preferences.isBlockScreensaver());
			settings.addElement(new PreferencesSetting()
								{
									public void set()
									{
										preferences.setBlockScreensaver(blockScreensaver_box.isSelected());
										MainMIDlet.getInstance().setScreensaverBlocker();
									}
								});
			container.addComponent(blockScreensaver_box);
		}
	}

	public void actionPerformed(ActionEvent ev)
	{
		if(cmdOK.equals(ev.getCommand()))
		{
			engine.stop(); //stop if we are still measuring
			Enumeration settingsEnum = settings.elements();
			while(settingsEnum.hasMoreElements())
				((PreferencesSetting) settingsEnum.nextElement()).set();
			preferences.saveToStorage();
			MainMIDlet.getInstance().startMeasuring(); //restart measuring (also shows measuring form, and login form if needed)
		}
		else //if(cmdCancel.equals(ev.getCommand()))
		{
			MainMIDlet.getInstance().showMeasureForm(); //leave preferences
		}
	}

	public abstract class PreferencesSetting
	{
		
		public abstract void set();

	}
	
}
