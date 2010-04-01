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
