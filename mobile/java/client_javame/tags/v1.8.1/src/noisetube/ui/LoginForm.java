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

import javax.microedition.rms.RecordStoreException;

import noisetube.MainMidlet;
import noisetube.config.Device;
import noisetube.config.Preferences;
import noisetube.io.SaveException;
import noisetube.io.web.HTTPWebAPI;
import noisetube.util.Logger;

import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;

/**
 * Login Form
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class LoginForm
{

	Logger log = Logger.getInstance();

	private Form loginForm;
	// private Container buttonPanel;
	private Container loginPanel;

	private Label titleLbl;

	private Label usernameLbl;
	private Label passwordLbl;
	private TextField usernameTxt = new TextField();
	private TextField passwordTxt;
	private Command skipCmd, loginCmd;

	final String ERROR_MSG = "Username/passwd incorrect";
	final String CONNECTION_MSG = "Connection error";

	private MainMidlet midlet = MainMidlet.getInstance();

	public Form getForm()
	{

		if(loginForm != null)
		{
			titleLbl.setText("Login:");
			return loginForm;
		}

		loginForm = new Form("NoiseTube Account");
		loginPanel = new Container();
		loginPanel.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		titleLbl = new Label(" ");

		usernameLbl = new Label();
		usernameTxt = new TextField();
		usernameLbl.setText("Username:");
		usernameLbl.getStyle().setBgTransparency(0);
		usernameTxt.getStyle().setMargin(2, 2, 5, 10);

		passwordTxt = new TextField();
		passwordLbl = new Label();
		passwordLbl.setText("Password:");
		passwordLbl.getStyle().setBgTransparency(0);
		passwordTxt.getStyle().setMargin(2, 2, 5, 10);

		skipCmd = new Command("Skip");
		loginCmd = new Command("Login");

		// buttonPanel = new Container();
		// buttonPanel.addComponent(loginBtn);
		// buttonPanel.getStyle().setMargin(2, 2, 2, 2);

		titleLbl.setText("Login:");
		// titleLbl.getStyle().setFont(Font.createSystemFont(Font.FACE_PROPORTIONAL,
		// Font.STYLE_BOLD, Font.SIZE_MEDIUM));
		// loginPanel.getStyle().setBgImage(Theme.getBackground());

		loginPanel.addComponent(titleLbl);
		loginPanel.addComponent(usernameLbl);
		loginPanel.addComponent(usernameTxt);
		loginPanel.addComponent(passwordLbl);
		loginPanel.addComponent(passwordTxt);

		loginForm.setLayout(new BorderLayout());
		loginForm.addComponent(BorderLayout.CENTER, loginPanel);
		loginForm.addCommand(loginCmd);
		loginForm.addCommand(skipCmd);
		loginForm.addCommandListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				if(loginCmd.equals(arg0.getCommand()))
				{
					try
					{

						if(!authentication())
						{
							// not successful
							titleLbl.setText(ERROR_MSG);
							titleLbl.repaint();
						}
						else
							// successful
							midlet.showMeasureForm();

					}
					catch(SaveException e)
					{
						log.error(e, "Connexion error");
						titleLbl.setText("Connexion error");
					}
					catch(RecordStoreException e1)
					{
						titleLbl.setText("Error saving APIkey");
						log.error(e1, "Error saving APIkey");
					}
				}
				else if(skipCmd.equals(arg0.getCommand()))
				{
					MainMidlet
							.getInstance()
							.getPreferences()
							.setSavingMode(
									Device.supportsSavingToFile() ? Preferences.SAVE_FILE
											: Preferences.SAVE_NO);
					midlet.showMeasureForm();
				}
				else
					midlet.notifyDestroyed();
			}
		});
		return loginForm;
	}

	private boolean authentication() throws SaveException, RecordStoreException
	{

		titleLbl.setText("Authenticating...");
		String username = usernameTxt.getText().trim().toLowerCase();
		String password = passwordTxt.getText().trim().toLowerCase();

		// TODO crytped password + encoding
		HTTPWebAPI api = new HTTPWebAPI();
		String apikey = api.getAPIKey(username, password);
		log.debug("API key: " + apikey);
		if(apikey.length() == 40)
		{
			titleLbl.setText("Saving API key...");
			midlet.getPreferences().setAPIKey(apikey);
			midlet.getPreferences().setUsername(usernameTxt.getText().trim());
			midlet.getPreferences().save();
			titleLbl.setText("API key saved");
			return true;
		}
		else
		{

			return false;
		}
	}

}
