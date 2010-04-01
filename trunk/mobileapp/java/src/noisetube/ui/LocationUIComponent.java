package noisetube.ui;

import noisetube.MainMidlet;
import noisetube.config.Device;
import noisetube.config.Preferences;
import noisetube.core.Engine;
import noisetube.location.LocationComponent;
import noisetube.util.Logger;

import com.sun.lwuit.ComboBox;
import com.sun.lwuit.Container;
import com.sun.lwuit.Font;
import com.sun.lwuit.Label;
import com.sun.lwuit.List;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.list.ListModel;

/**
 * Location UI component
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class LocationUIComponent extends Container
{

	private Logger log = Logger.getInstance();
	private MainMidlet midlet = MainMidlet.getInstance();
	private Preferences preferences = midlet.getPreferences();
	private Engine engine = midlet.getEngine();
	private MeasureForm measureForm = midlet.getMeasureForm();
	private LocationComponent locComp = engine.getLocationComponent();

	private Container autoManualSwitchLabels;
	private Label auto;
	private Label manual;
	private boolean uiShowingGPSAutoMode = false;
	private Container gpsLocation;
	private Label lblGPSState;

	private Container humanLocation;

	private TextField address = new TextField();
	private List list_location = new List();
	private ListModel m;

	public LocationUIComponent()
	{
		super();

		try
		{
			setLayout(new BorderLayout());

			// "Human location":
			humanLocation = new Container();

			m = list_location.getModel();
			addTagToList("[suggestions]");

			Label lblAddress = new Label("Location (eg. address)");
			humanLocation.addComponent(lblAddress);
			humanLocation.addComponent(address);

			// tag from suggestions
			ComboBox sources_box = new ComboBox();
			sources_box.setModel(m);
			sources_box.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent aE)
				{
					// not the [suggestions] item
					if(m.getSelectedIndex() > 0)
					{
						String location_tag = (String) m.getItemAt(m
								.getSelectedIndex());
						fireTag(location_tag);
					}
				}
			});
			humanLocation.addComponent(sources_box);
			sources_box.setPreferredSize(new Dimension(140, 20));
			addTagToList("[suggestions]");
			addTagToList("office");
			addTagToList("home");

			// Tag by textfield
			address.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					fireTag(address.getText());
				}
			});

			if(Device.supportsGPS())
			{
				// A/M switch:
				auto = new Label("Automatic");
				auto.getStyle().setPadding(0, 0, 0, 0);
				manual = new Label("Manual");
				manual.getStyle().setPadding(0, 0, 0, 0);
				Label separator = new Label("/");
				separator.getStyle().setFont(Font.createSystemFont(0, 0, 8));
				separator.getStyle().setPadding(0, 0, 0, 0);
				autoManualSwitchLabels = new Container();
				autoManualSwitchLabels.addComponent(auto);
				autoManualSwitchLabels.addComponent(separator);
				autoManualSwitchLabels.addComponent(manual);

				// GPS state:
				gpsLocation = new Container();
				lblGPSState = new Label("");
				lblGPSState.setPreferredSize(new Dimension(170, 44));
				lblGPSState.getStyle().setFont(Font.createSystemFont(0, 0, 8));
				gpsLocation.addComponent(lblGPSState);
			}
		}
		catch(Exception ignore)
		{
		}
	}

	public void setLocationComponent(LocationComponent loc)
	{
		this.locComp = loc;
	}

	private void addTagToList(String tag_name)
	{
		if(!is_contained_in_list(tag_name))
			m.addItem(tag_name);
	}

	private boolean is_contained_in_list(String tag)
	{
		for(int i = 0; i < m.getSize(); i++)
		{
			if(m.getItemAt(i).equals(tag))
				return true;
		}
		return false;
	}

	private void fireTag(String location_tag)
	{
		if(engine.isRunning())
		{
			// send to the Location component
			locComp.setLocationTag(location_tag);

			// maybe add tag in the list if it is new
			addTagToList(location_tag);
		}
	}

	public void update()
	{
		try
		{
			if(Device.supportsGPS() && preferences.isUseGPS())
			{
				this.lblGPSState.setText(locComp.getStatusMessage());
				if(!contains(autoManualSwitchLabels))
					addComponent(BorderLayout.NORTH, autoManualSwitchLabels);
				boolean autoModeEnabled = locComp.isAutoModeEnabled();
				if(uiShowingGPSAutoMode != autoModeEnabled) // only update when
															// needed
				{
					// GPS button icon:
					measureForm.getTabs().updateAutoMode(autoModeEnabled); // change
																			// GPS
																			// on/off
																			// icon

					// A/M switch:
					auto.getStyle().setFont(
							Font.createSystemFont(0, (autoModeEnabled ? 1 : 0),
									8));
					manual.getStyle().setFont(
							Font.createSystemFont(0, (autoModeEnabled ? 0 : 1),
									8));

					// GPS state / "Humanlocation":
					if(autoModeEnabled)
					{
						removeComponent(humanLocation);
						removeComponent(gpsLocation);
						addComponent(BorderLayout.CENTER, gpsLocation);
					}
					else
					{
						removeComponent(gpsLocation);
						removeComponent(humanLocation);
						addComponent(BorderLayout.CENTER, humanLocation);
						address.setText("");
					}
					uiShowingGPSAutoMode = autoModeEnabled;
				}
			}
			else
			{
				if(contains(autoManualSwitchLabels))
					removeComponent(autoManualSwitchLabels);
				if(!contains(humanLocation))
					addComponent(BorderLayout.CENTER, humanLocation);
				measureForm.getTabs().updateAutoMode(false);
				uiShowingGPSAutoMode = false;
			}
		}
		catch(Exception e)
		{
			log.error(e, "LocationUIComponent.update()");
		}
	}

	/********************* DEPRECATED ********************/
	/*
	 * public int get_selected_location() { return
	 * list.getModel().getSelectedIndex(); }
	 * 
	 * private void minimize(Component c) { Style style = c.getStyle();
	 * style.setPadding(0, 0, 5, 0); style.setMargin(0, 0, 0, 0); }
	 * 
	 * public void addSelectionListener(SelectionListener listener) {
	 * list.addSelectionListener(listener); }
	 * 
	 * public String getSelected() { int i = list.getModel().getSelectedIndex();
	 * return (String) list.getModel().getItemAt(i); }
	 * 
	 * public void setText(String text) { location.setText(text); }
	 */
}
