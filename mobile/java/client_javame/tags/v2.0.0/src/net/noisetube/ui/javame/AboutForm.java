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

import net.noisetube.core.javame.MainMIDlet;

import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Form;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;

/**
 * @author mstevens
 * 
 */
public class AboutForm extends Form
{

	private Command cmdOk;
	private MainMIDlet midlet = MainMIDlet.getInstance();

	public AboutForm()
	{
		super("About NoiseTube Mobile");
		setLayout(new BorderLayout());

		Label lblLogo;
		try
		{
			Image logo = Image.createImage("/NoiseTubeLogo.png");
			lblLogo = new Label(logo);
		}
		catch(IOException e)
		{
			lblLogo = new Label();
		}
		lblLogo.setAlignment(Component.CENTER);

		TextArea txtClient = new TextArea();
		txtClient.getStyle().setBorder(null);
		txtClient.getStyle().setFont(Fonts.SMALL_FONT);
		txtClient.getStyle().setBgColor(getStyle().getBgColor());
		txtClient.setEditable(false);
		txtClient.setFocusable(false);
		txtClient.setText(MainMIDlet.CLIENT_TYPE + "\n" + MainMIDlet.CLIENT_VERSION);

		TextArea txtInfo = new TextArea();
		txtInfo.getStyle().setBorder(null);
		txtInfo.getStyle().setFont(Fonts.SMALL_FONT);
		txtInfo.getStyle().setBgColor(getStyle().getBgColor());
		txtInfo.setEditable(false);
		txtInfo.setFocusable(false);
		txtInfo.setText("© 2008-2010 Sony CSL Paris\nPortions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011.\nJava ME client developed by Nicolas Maisonneuve (Sony CSL Paris) and Matthias Stevens (VUB-BrusSense).\nwww.noisetube.net");

		addComponent(BorderLayout.NORTH, txtClient);
		addComponent(BorderLayout.CENTER, lblLogo);
		addComponent(BorderLayout.SOUTH, txtInfo);

		cmdOk = new Command("Ok");
		addCommand(cmdOk);

		addCommandListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent actionEvent)
			{
				//if(cmdOk.equals(actionEvent.getCommand()))
				midlet.showMeasureForm();
			}
		});
	}
	
}
