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

import net.noisetube.config.NTAccount;
import net.noisetube.config.Preferences;
import net.noisetube.core.NTClient;
import net.noisetube.core.javame.MainMIDlet;
import net.noisetube.io.NTWebAPI;
import net.noisetube.util.Logger;

import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;

/**
 * Login Form
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class LoginForm extends Form implements ActionListener
{

	//STATICS
	private static Logger log = Logger.getInstance();
	
	private static final String MSG_SUCCESSFUL_LOGIN = "Login successful";
	private static final String MSG_INCORRECT_LOGIN = "Username/passwd incorrect, try again please";
	private static final String MSG_CONNECTION_PROBLEM = "Connection error, skipping login";

	//DYNAMIC
	private TextField txtExplanation;
	private Label lblInfo;
	private TextField txtUsername, txtPassword;

	private Container cntButtons;
	private Button btnLogin, btnSkip;
	
	private Command cmdSkip, cmdLogin;

	private MainMIDlet midlet = MainMIDlet.getInstance();

	public LoginForm()
	{
		super("NoiseTube Account");
		
		txtExplanation = new TextField("Please login with your NoiseTube account. If you do not have one you can register at NoiseTube.net. If you want to use NoiseTube offline, select Skip.");
		txtExplanation.getStyle().setFont(Fonts.SMALL_FONT);
		txtExplanation.setSingleLineTextArea(false);
		txtExplanation.setEditable(false);
		txtExplanation.setFocusable(false);
		txtExplanation.getStyle().setFgColor(this.getStyle().getFgColor());
		txtExplanation.getStyle().setBgColor(this.getStyle().getBgColor());
		txtExplanation.getStyle().setBorder(null);
		
		txtUsername = new TextField();
		txtUsername.getStyle().setMargin(2, 2, 5, 10);
		txtPassword = new TextField();
		txtPassword.getStyle().setMargin(2, 2, 5, 10);

		lblInfo = new Label();
		lblInfo.getStyle().setFont(Fonts.SMALL_FONT);
		
		cmdLogin = new Command("Login");
		cmdSkip = new Command("Skip");		
		
		btnLogin = new Button("Login");
		btnLogin.addActionListener(this);
		btnSkip = new Button("Skip");
		btnSkip.addActionListener(this);
		
		cntButtons = new Container();
		cntButtons.getStyle().setMargin(2, 2, 2, 2);
		cntButtons.addComponent(btnLogin);
		cntButtons.addComponent(btnSkip);
		
		setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		addComponent(txtExplanation);
		addComponent(new Label("Username:"));
		addComponent(txtUsername);
		addComponent(new Label("Password:"));
		addComponent(txtPassword);
		addComponent(cntButtons);
		addComponent(lblInfo);
		
		addCommand(cmdLogin);
		addCommand(cmdSkip);
		addCommandListener(this);		
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == btnLogin || ae.getCommand() == cmdLogin)
			authenticate();
		else if(ae.getSource() == btnSkip || ae.getCommand() == cmdSkip)
			skip();
		else
			midlet.exitApp(true);
	}

	private void authenticate()
	{
		NTWebAPI api = new NTWebAPI();
		NTAccount account = null;
		if(txtUsername.getText().equals("") || txtPassword.getText().equals(""))
			showDialog("Username and password cannot be empty");
		else
		{
			try
			{
				account = api.login(txtUsername.getText().trim().toLowerCase(), txtPassword.getText().trim().toLowerCase());
			}
			catch(Exception e)
			{	//Connection or server problem
				log.error(e, MSG_CONNECTION_PROBLEM);
				showDialog(MSG_CONNECTION_PROBLEM);
				//Stay offline:
				skip();
				return; //!!!
			}
			if(account != null)
			{	//Login succeeded
				log.info("Authentication succesful (API key: " + account.getAPIKey() + ")");
				//Save account in pref's...			
				Preferences prefs =	NTClient.getInstance().getPreferences();
				prefs.setAccount(account);
				prefs.saveToStorage();
				showDialog(MSG_SUCCESSFUL_LOGIN);
				midlet.startMeasuring(); //Start measuring (will also show the MeasuringForm)
				return; //!!!
			}
			else
			{	//incorrect username/password combination
				showDialog(MSG_INCORRECT_LOGIN);
				txtUsername.setText("");
				txtPassword.setText("");
			}
		}
	}
	
	private void skip()
	{
		NTClient.getInstance().getPreferences().setSavingMode(NTClient.getInstance().getDevice().supportsFileAccess() ? Preferences.SAVE_FILE : Preferences.SAVE_NO);
		midlet.startMeasuring();
	}
	
	private void showDialog(String msg)
	{
		TextField txtMsg = new TextField(msg);
		txtMsg.getStyle().setFont(Fonts.SMALL_FONT);
		txtMsg.setSingleLineTextArea(false);
		txtMsg.setEditable(false);
		txtMsg.setFocusable(false);
		txtMsg.getStyle().setFgColor(this.getStyle().getFgColor());
		txtMsg.getStyle().setBgColor(this.getStyle().getBgColor());
		txtMsg.getStyle().setBorder(null);		
		Dialog.show("NoiseTube", txtMsg, new Command[] { new Command("OK") });
	}
	
}
