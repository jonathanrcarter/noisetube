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
import net.noisetube.location.LocationComponent;
import net.noisetube.ui.ILocationUI;

import com.sun.lwuit.Button;
import com.sun.lwuit.ComboBox;
import com.sun.lwuit.Container;
import com.sun.lwuit.Label;
import com.sun.lwuit.List;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;


/**
 * Location UI component
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class LocationUIComponent extends Container implements ILocationUI
{

	private Preferences preferences = NTClient.getInstance().getPreferences();
	private Engine engine = MainMIDlet.getInstance().getEngine();
	private FullMeasuringForm measuringForm;
	private LocationComponent locationComponent = engine.getLocationComponent();

	private Button btnGPSToggle;
	private Label lblGPSState;
	private TextField txtLocationTag = new TextField();
	private List lstLocationSuggestions = new List();

	public LocationUIComponent(FullMeasuringForm measuringForm)
	{
		super();
		
		this.measuringForm = measuringForm;
		setLayout(new BorderLayout());
		
		//GPS
		Container cntGPS = new Container();		
		if(NTClient.getInstance().getDevice().hasTouchScreen())
		{
			btnGPSToggle = new Button(preferences.isUseGPS() ? "Disable GPS" : "Enable GPS");
			btnGPSToggle.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					if(preferences.isUseGPS())
						locationComponent.toggleGPS();
				}
			});
			cntGPS.addComponent(btnGPSToggle);
		}
		lblGPSState = new Label();
		lblGPSState.getStyle().setFont(Fonts.SMALL_FONT);
		cntGPS.addComponent(lblGPSState);		
		addComponent(BorderLayout.NORTH, cntGPS);
		
		//"Human" location:
		Container cntHumanLocation = new Container();		
		Label lblExamples = new Label("Location (eg. address):");
		lblExamples.getStyle().setFont(Fonts.SMALL_FONT);
		cntHumanLocation.addComponent(lblExamples);
		cntHumanLocation.addComponent(txtLocationTag);

		//tag from suggestions
		final ComboBox comboLocationSuggestions = new ComboBox();
		comboLocationSuggestions.setModel(lstLocationSuggestions.getModel());
		comboLocationSuggestions.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent aE)
			{
				//not the [suggestions] item
				if(lstLocationSuggestions.getModel().getSelectedIndex() > 0)
				{
					String locationTag = (String) lstLocationSuggestions.getModel().getItemAt(comboLocationSuggestions.getSelectedIndex());
					fireTag(locationTag);
				}
			}
		});
		cntHumanLocation.addComponent(comboLocationSuggestions);
		addTagToList("[suggestions]");
		addTagToList("office");
		addTagToList("home");

		//tag by textfield
		txtLocationTag.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				fireTag(txtLocationTag.getText());
			}
		});		
		addComponent(BorderLayout.CENTER, cntHumanLocation);
	}

	private void addTagToList(String tag_name)
	{
		if(!isContainedInList(tag_name))
			lstLocationSuggestions.getModel().addItem(tag_name);
	}

	private boolean isContainedInList(String tag)
	{
		for(int i = 0; i < lstLocationSuggestions.getModel().getSize(); i++)
		{
			if(lstLocationSuggestions.getModel().getItemAt(i).equals(tag))
				return true;
		}
		return false;
	}

	private void fireTag(String location_tag)
	{
		if(engine.isRunning())
		{
			//send to the Location component
			locationComponent.setLocationTag(location_tag);

			//maybe add tag in the list if it is new
			addTagToList(location_tag);
		}
	}

	public void gpsStateChanged(int newState, int previousState)
	{
		if(NTClient.getInstance().getDevice().supportsPositioning() && preferences.isUseGPS())
		{
			this.lblGPSState.setText(/*"GPS: " +*/ LocationComponent.getGPSStateString(locationComponent.getGPSState()));
			if(previousState == LocationComponent.GPS_STATE_DISABLED || newState == LocationComponent.GPS_STATE_DISABLED)
			{
				measuringForm.getTabs().updateGPSMode(locationComponent.isGPSEnabled()); //change GPS on/off icon
				if(btnGPSToggle != null)
					btnGPSToggle.setText(locationComponent.isGPSEnabled() ? "Disable GPS" : "Enable GPS");
			}
		}
	}

}
