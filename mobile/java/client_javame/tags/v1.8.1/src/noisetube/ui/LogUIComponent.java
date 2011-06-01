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

import noisetube.MainMidlet;
import noisetube.util.Logger;

import com.sun.lwuit.Container;
import com.sun.lwuit.Font;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.GridLayout;

/**
 * Log UI
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class LogUIComponent extends Container
{

	Logger log = Logger.getInstance();

	private TextArea txtMessage1;
	private TextArea txtMessage2;
	TextArea txtLog;

	public LogUIComponent()
	{
		super();
		setLayout(new BorderLayout());

		if(MainMidlet.environment != MainMidlet.PHONE_PROD_ENV)
		{
			txtLog = new TextArea();
			txtLog.setSingleLineTextArea(false);
			txtLog.getStyle().setFont(Font.createSystemFont(0, 0, 8));
			txtLog.setRows(0);
			txtLog.setEditable(false);
			addComponent(BorderLayout.NORTH, new Label("Log"));
			addComponent(BorderLayout.CENTER, txtLog);
		}
		else
		{
			Container container = new Container(new GridLayout(2, 1));
			txtMessage1 = new TextArea();
			txtMessage1.getStyle().setBorder(null);
			txtMessage1.getStyle().setFont(Font.createSystemFont(0, 0, 8));
			txtMessage1.getStyle().setBgColor(this.getStyle().getBgColor());
			txtMessage1.setEditable(false);
			txtMessage1.setFocusable(false);

			txtMessage2 = new TextArea();
			txtMessage2.getStyle().setBorder(null);
			txtMessage2.getStyle().setFont(Font.createSystemFont(0, 0, 8));
			txtMessage2.getStyle().setBgColor(this.getStyle().getBgColor());
			txtMessage2.setEditable(false);
			txtMessage2.setFocusable(false);

			container.addComponent(txtMessage1);
			container.addComponent(txtMessage2);
			addComponent(BorderLayout.NORTH, new Label("Program info"));
			addComponent(BorderLayout.CENTER, container);
		}
	}

	public void displayLog()
	{
		if(txtLog != null)
		{
			txtLog.setText(log.getBuffer());
			txtLog.repaint();
		}
	}

	public void setPrimaryMessage(String msg)
	{
		if(txtMessage1 != null)
			txtMessage1.setText(msg);
	}

	public void setSecondaryMessage(String msg)
	{
		if(txtMessage2 != null)
			txtMessage2.setText(msg);
	}

}
