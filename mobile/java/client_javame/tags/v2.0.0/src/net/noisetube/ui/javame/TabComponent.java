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

import java.io.IOException;

import net.noisetube.config.Preferences;
import net.noisetube.core.NTClient;
import net.noisetube.core.javame.MainMIDlet;
import net.noisetube.location.LocationComponent;
import net.noisetube.tagging.TaggingComponent;

import com.sun.lwuit.Button;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Image;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.FocusListener;
import com.sun.lwuit.layouts.BoxLayout;

/**
 * Tab component
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class TabComponent extends Container
{

	private Preferences preferences = NTClient.getInstance().getPreferences();
	private LocationComponent locationComponent;
	private TaggingComponent taggingComponent;

	public Button btnTags;
	public Button btnLoc;
	public Button btnLog;
	public Button btnStats;

	private Image imgGPSon;
	private Image imgGPSoff;

	//private boolean uiShowingGPSOn = false;

	public TabComponent(final FullMeasuringForm measuringForm)
	{
		super();
		//setLayout(new BorderLayout());
		setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		//Container c = new Container(new BorderLayout());
		//addComponent(BorderLayout.WEST, c);

		//Statistics
		try
		{
			btnStats = new Button(Image.createImage("/icons/statistics.png"));
		}
		catch(IOException ioE)
		{
			btnStats = new Button("Stats");
		}
		btnStats.addFocusListener(new FocusListener()
		{
			public void focusGained(Component comp)
			{
				btnStats.getStyle().setBgColor(0xffffff);
				measuringForm.displayStatistics();				
			}

			public void focusLost(Component comp) { }
		});
		addComponent(btnStats);
		
		//TAGGING
		try
		{
			btnTags = new Button(Image.createImage("/icons/tag.png"));
		}
		catch(IOException ioE)
		{
			btnTags = new Button("Tag");
		}
		btnTags.addFocusListener(new FocusListener()
		{
			public void focusGained(Component comp)
			{
				btnTags.getStyle().setBgColor(0xffffff);
				measuringForm.displayTagging();
			}

			public void focusLost(Component comp) { }
		});
		btnTags.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				if(taggingComponent.getTags() == null)
				{
					taggingComponent.clear();
					measuringForm.getTaggingUI().clear();
				}
			}
		});
		addComponent(btnTags);

		//LOCALISATION		
		try
		{
			imgGPSon = Image.createImage("/icons/GPS_on.png");
			imgGPSoff = Image.createImage("/icons/GPS_off.png");
			btnLoc = new Button(preferences.isUseGPS() ? imgGPSon : imgGPSoff);
		}
		catch(IOException ioE)
		{
			btnLoc = new Button((preferences.isUseGPS() ? "GPS on" : "GPS off"));
		}
		btnLoc.addFocusListener(new FocusListener()
		{
			public void focusGained(Component comp)
			{
				btnLoc.getStyle().setBgColor(0xffffff);
				measuringForm.displayLocation();				
			}

			public void focusLost(Component comp) { }
		});
		if(!NTClient.getInstance().getDevice().hasTouchScreen())
		{	//only for non-touch screens
			btnLoc.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					if(preferences.isUseGPS())
						locationComponent.toggleGPS();
				}
			});
		}
		addComponent(btnLoc);
		
		//LOG/Messages
		try
		{
			btnLog = new Button(Image.createImage("/icons/event.png"));
		}
		catch(IOException ioE)
		{
			btnLog = new Button(MainMIDlet.ENVIRONMENT == MainMIDlet.PHONE_PROD_ENV ? "Info" : "Log");
		}
		btnLog.addFocusListener(new FocusListener()
		{
			public void focusGained(Component comp)
			{
				btnLog.getStyle().setBgColor(0xffffff);
				measuringForm.displayLog();
			}

			public void focusLost(Component comp) { }
		});
		addComponent(btnLog);		
	}

	public void setLocationComponent(LocationComponent locationComponent)
	{
		this.locationComponent = locationComponent;
	}

	public void setTaggingComponent(TaggingComponent noteComponent)
	{
		this.taggingComponent = noteComponent;
	}

	public void updateGPSMode(boolean gpsEnabled)
	{
		if(imgGPSon != null && imgGPSoff != null)
		{
			btnLoc.setIcon((gpsEnabled ? imgGPSon : imgGPSoff));
			btnLoc.setRolloverIcon((gpsEnabled ? imgGPSon : imgGPSoff));
			btnLoc.setPressedIcon((gpsEnabled ? imgGPSon : imgGPSoff));
		}
		else
			btnLoc.setText(gpsEnabled ? "GPS on" : "GPS off");
	}

}
