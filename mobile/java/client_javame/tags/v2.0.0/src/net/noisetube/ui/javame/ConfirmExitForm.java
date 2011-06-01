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

import net.noisetube.core.javame.MainMIDlet;

import com.sun.lwuit.Command;

import com.sun.lwuit.Form;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;

/**
 * @author mstevens
 * 
 */
public class ConfirmExitForm extends Form
{
	
	private MainMIDlet midlet = MainMIDlet.getInstance();

	public ConfirmExitForm()
	{
		super("Exit NoiseTube?");
		setLayout(new BorderLayout());

		TextArea txtInfo = new TextArea();
		txtInfo.getStyle().setBorder(null);
		txtInfo.getStyle().setFont(Fonts.SMALL_FONT);
		txtInfo.getStyle().setBgColor(getStyle().getBgColor());
		txtInfo.setEditable(false);
		txtInfo.setFocusable(false);
		
		txtInfo.setText(MainMIDlet.BRUSSENSE ? "Ben je zeker dat je deze meetsessie wil afsluiten?" : "Are you sure you want to exit NoiseTube?");

		addComponent(BorderLayout.NORTH, txtInfo);

		final Command cmdOk = new Command(MainMIDlet.BRUSSENSE ? "Ja" :"Yes");
		addCommand(cmdOk);

		final Command cmdCancel = new Command(MainMIDlet.BRUSSENSE ? "Nee" :"No");
		addCommand(cmdCancel);
		
		addCommandListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent actionEvent)
			{
				if(cmdOk.equals(actionEvent.getCommand()))
					midlet.exitApp(true); //!!! user wants to exit, so don't restart
				else if(cmdCancel.equals(actionEvent.getCommand()))
					midlet.showMeasureForm();
			}
		});
	}
	
}
