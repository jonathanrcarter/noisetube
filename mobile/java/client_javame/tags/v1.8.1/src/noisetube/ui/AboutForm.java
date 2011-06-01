/** 
 * -------------------------------------------------------------------------------
 * NoiseTube - Mobile client (J2ME version)
 * Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 * 
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License, version 2.1,
 * as published by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * --------------------------------------------------------------------------------
 * 
 * Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 * NoiseTube project source code repository: http://code.google.com/p/noisetube
 * NoiseTube project website: http://www.noisetube.net
 */

package noisetube.ui;

import java.io.IOException;

import noisetube.MainMidlet;

import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Font;
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
public class AboutForm
{
	private Form frmAbout;

	private Command cmdOk;
	private MainMidlet midlet = MainMidlet.getInstance();

	public Form getForm()
	{

		if(frmAbout != null)
		{
			return frmAbout;
		}

		frmAbout = new Form("About NoiseTube Mobile");
		frmAbout.setLayout(new BorderLayout());

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
		txtClient.getStyle().setFont(Font.createSystemFont(0, 0, 8));
		txtClient.getStyle().setBgColor(frmAbout.getStyle().getBgColor());
		txtClient.setEditable(false);
		txtClient.setFocusable(false);
		txtClient.setText(MainMidlet.CLIENT_TYPE + "\n"
				+ MainMidlet.CLIENT_VERSION);

		TextArea txtInfo = new TextArea();
		txtInfo.getStyle().setBorder(null);
		txtInfo.getStyle().setFont(Font.createSystemFont(0, 0, 8));
		txtInfo.getStyle().setBgColor(frmAbout.getStyle().getBgColor());
		txtInfo.setEditable(false);
		txtInfo.setFocusable(false);
		txtInfo
				.setText("© 2008-2009 Sony CSL Paris\nMade by Nicolas Maisonneuve (Sony CSL Paris) and Matthias Stevens (Vrije Universiteit Brussel)\nwww.noisetube.net");

		frmAbout.addComponent(BorderLayout.NORTH, txtClient);
		frmAbout.addComponent(BorderLayout.CENTER, lblLogo);
		frmAbout.addComponent(BorderLayout.SOUTH, txtInfo);

		cmdOk = new Command("Ok");
		frmAbout.addCommand(cmdOk);

		frmAbout.addCommandListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent actionEvent)
			{
				// if(cmdOk.equals(actionEvent.getCommand()))
				midlet.showMeasureForm();
			}
		});

		return frmAbout;
	}
}
