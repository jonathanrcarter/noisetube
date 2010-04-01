package noisetube.ui;

import java.io.IOException;

import noisetube.MainMidlet;
import noisetube.config.Device;
import noisetube.config.Preferences;
import noisetube.location.LocationComponent;
import noisetube.tagging.NotesComponent;

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
 * TOFIX: not DRY design....:)
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class TabUIComponent extends Container
{

	private Preferences preferences = MainMidlet.getInstance().getPreferences();
	private LocationComponent locationComponent;
	private NotesComponent noteComponent;

	private Button btnNote;
	private Button btnLoc;
	private Button btnLog;

	private Image imgGPSon;
	private Image imgGPSoff;

	private boolean uiShowingGPSOn = false;

	public TabUIComponent(final MeasureForm ui)
	{
		super();
		// setLayout(new BorderLayout());
		setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		// Container c = new Container(new BorderLayout());
		// addComponent(BorderLayout.WEST, c);

		// NOTE
		try
		{
			btnNote = new Button(Image.createImage("/note.png"));
		}
		catch(IOException ioE)
		{
			btnNote = new Button("Tag");
		}
		btnNote.getStyle().setMargin(1, 1, 0, 0);
		btnNote.addFocusListener(new FocusListener()
		{
			public void focusGained(Component arg0)
			{
				ui.display_notes_ui_component();
			}

			public void focusLost(Component arg0)
			{
			}
		});
		btnNote.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				if(noteComponent.getNote() == null)
				{
					noteComponent.edit();
				}
			}
		});
		Container cntNote = new Container();
		cntNote.addComponent(btnNote);
		addComponent(cntNote);

		// LOCALISATION
		uiShowingGPSOn = preferences.isUseGPS();
		try
		{
			imgGPSon = Image.createImage("/GPS_on.png");
			imgGPSoff = Image.createImage("/GPS_off.png");
			btnLoc = new Button((uiShowingGPSOn ? imgGPSon : imgGPSoff));
		}
		catch(IOException ioE)
		{
			btnLoc = new Button((uiShowingGPSOn ? "GPS on" : "GPS off"));
		}
		btnLoc.getStyle().setMargin(1, 0, 0, 0);
		btnLoc.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(preferences.isUseGPS())
					locationComponent.toggleAutoMode();
			}
		});
		btnLoc.addFocusListener(new FocusListener()
		{
			public void focusGained(Component arg0)
			{
				ui.display_location_ui_component();
			}

			public void focusLost(Component arg0)
			{
			}

		});

		Container cntLoc = new Container();
		cntLoc.addComponent(btnLoc);
		addComponent(cntLoc);

		// LOG/Messages
		try
		{
			btnLog = new Button(Image.createImage("/event.png"));
		}
		catch(IOException ioE)
		{
			btnLog = new Button(
					MainMidlet.environment == MainMidlet.PHONE_PROD_ENV ? "Info"
							: "Log");
		}
		btnLog.getStyle().setMargin(1, 1, 0, 0);
		btnLog.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
			}
		});
		btnLog.addFocusListener(new FocusListener()
		{
			public void focusGained(Component arg0)
			{
				ui.display_log_ui_component();
			}

			public void focusLost(Component arg0)
			{
			}
		});
		Container cntLog = new Container();
		cntLog.addComponent(btnLog);
		addComponent(cntLog);

		/*
		 * // USER image = Image.createImage("/user.png"); picture = new
		 * Button(image); picture.getStyle().setMargin(1, 1, 0, 0);
		 * picture.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent e) {
		 * 
		 * } }); picture.addFocusListener(new FocusListener() {
		 * 
		 * public void focusGained(Component arg0) {
		 * 
		 * }
		 * 
		 * public void focusLost(Component arg0) {}
		 * 
		 * }); Container cntUser = new Container();
		 * cntUser.addComponent(picture); //addComponent(cntUser);
		 */
	}

	public void enable_note()
	{
		this.requestFocus();
		btnNote.requestFocus();
	}

	public void setLocationComponent(LocationComponent locationComponent)
	{
		this.locationComponent = locationComponent;
	}

	public void setNoteComponent(NotesComponent noteComponent)
	{
		this.noteComponent = noteComponent;
	}

	public void updateAutoMode(boolean autoModeEnabled)
	{
		if(Device.supportsGPS() && uiShowingGPSOn != autoModeEnabled) // only
																		// update
																		// when
																		// needed
		{
			if(imgGPSon != null && imgGPSoff != null)
			{
				btnLoc.setIcon((autoModeEnabled ? imgGPSon : imgGPSoff));
				btnLoc
						.setRolloverIcon((autoModeEnabled ? imgGPSon
								: imgGPSoff));
				btnLoc.setPressedIcon((autoModeEnabled ? imgGPSon : imgGPSoff));

			}
			else
				btnLoc.setText(autoModeEnabled ? "GPS on" : "GPS off");
			uiShowingGPSOn = autoModeEnabled;
		}
	}

}
