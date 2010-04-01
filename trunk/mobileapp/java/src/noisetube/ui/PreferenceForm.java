package noisetube.ui;

import noisetube.MainMidlet;
import noisetube.config.Device;
import noisetube.config.Preferences;
import noisetube.config.WatchPreferences;
import noisetube.util.ComboList;
import noisetube.util.Logger;

import com.sun.lwuit.CheckBox;
import com.sun.lwuit.ComboBox;
import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.List;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.SelectionListener;
import com.sun.lwuit.layouts.GridLayout;

/**
 * Preference Form
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class PreferenceForm implements ActionListener
{

	private Logger log = Logger.getInstance();
	private Preferences preferences;

	// Form elements:
	private Form prefForm;
	private ComboBox saving_box;
	private CheckBox gps_box;
	private CheckBox memoryCard_box;
	private CheckBox blockScreensaver_box;
	private ComboBox recordmode_box;
	private CheckBox watch_box;
	private TextField watch_address;
	private Command okCmd;

	public PreferenceForm()
	{
		preferences = MainMidlet.getInstance().getPreferences();
		okCmd = new Command("OK");
	}

	public Form getForm()
	{
		if(prefForm == null)
		{
			prefForm = new Form("Preferences");
			Container container = new Container(new GridLayout(5, 1));
			prefForm.addComponent(container);

			// GPS
			if(Device.supportsGPS())
			{
				gps_box = new CheckBox("Use GPS for localisation");
				// Set default/currently active mode:
				gps_box.setSelected(preferences.isUseGPS());
				gps_box.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						preferences.setUseGPS(gps_box.isSelected());
						preferences.save();
					}
				});
				container.addComponent(gps_box);
			}

			// Memory card
			if(Device.supportsSavingToFile())
			{
				memoryCard_box = new CheckBox("Prefer memory card");
				// Set default/currently active mode:
				memoryCard_box.setSelected(preferences.isPreferMemoryCard());
				memoryCard_box.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						preferences.setPreferMemoryCard(memoryCard_box
								.isSelected());
						preferences.save();
					}
				});
				memoryCard_box.setWidth(container.getWidth());
				container.addComponent(memoryCard_box);
			}

			// SAVING
			Container savingContainer = new Container();
			savingContainer.setLayout(new GridLayout(1, 2));
			Label saving_label = new Label("Data saving:");
			savingContainer.addComponent(saving_label);
			final List saving = new List();
			final ComboList savingModes = preferences.getAvailableSavingModes();
			for(int i = 0; i < savingModes.size(); i++)
				saving.addItem(savingModes.getLabelAtIdx(i));
			saving.setSelectedIndex(savingModes.getDefaultIdx()); // Set
																	// default/currently
																	// active
																	// mode
			saving.addSelectionListener(new SelectionListener()
			{
				public void selectionChanged(int i, int j)
				{
					preferences.setSavingMode(savingModes.getValueAtIdx(j));
					preferences.save();
				}
			});
			saving_box = new ComboBox();
			saving_box.setModel(saving.getModel());
			savingContainer.addComponent(saving_box);
			container.addComponent(savingContainer);

			// Block screensaver
			if(Device.supportsNokiaUIAPI())
			{
				blockScreensaver_box = new CheckBox(
						"Disable screensaver/power saving");
				// Set default/currently active mode:
				blockScreensaver_box.setSelected(preferences
						.isBlockScreensaver());
				blockScreensaver_box.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						preferences.setBlockScreensaver(blockScreensaver_box
								.isSelected());
						preferences.save();
					}
				});
				container.addComponent(blockScreensaver_box);
			}

			// RECORD
			if(Device.supportsNativeRecorder())
			{
				Label recorder_mode = new Label("Recording mode:");
				container.addComponent(recorder_mode);
				final List recordingmode = new List();
				recordingmode.addItem("Java");
				recordingmode.addItem("Native");
				// Set default/currently active mode:
				if(preferences.isJniRecording())
					recordingmode.setSelectedIndex(1);
				else
					recordingmode.setSelectedIndex(0);
				recordingmode.addSelectionListener(new SelectionListener()
				{
					public void selectionChanged(int i, int j)
					{
						String record = (String) recordingmode.getModel()
								.getItemAt(j);
						if(record.equals("java"))
						{
							preferences.setJniRecording(false);
						}
						else
						{
							preferences.setJniRecording(true);
						}
						preferences.save();
					}
				});
				recordmode_box = new ComboBox();
				recordmode_box.setModel(recordingmode.getModel());
				container.addComponent(recordmode_box);
			}

			// Watch
			if(preferences instanceof WatchPreferences)
			{
				final WatchPreferences watch_pref = (WatchPreferences) preferences;
				watch_box = new CheckBox("Use NoiseTube watch");
				// Set default/currently active mode:
				watch_box.setSelected(watch_pref.isUseWatch());
				watch_box.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						watch_pref.setUseWatch(watch_box.isSelected());
						watch_pref.save();
					}
				});
				container.addComponent(watch_box);

				try
				{
					watch_address = new TextField();
					final Label watch_label = new Label("Watch Address:");
					container.addComponent(watch_label);
					container.addComponent(watch_address);
					watch_address.setText(watch_pref.getWatchAddress());
					watch_address.addActionListener(new ActionListener()
					{

						public void actionPerformed(ActionEvent arg0)
						{
							String address = watch_address.getText();
							if(address != null && !"".equals(address))
							{
								if(Device.detectWatch(address))
								{
									watch_label.setText("connected");
								}
								else
								{
									watch_label.setText("watch error");
								}
							}
						}
					});
				}
				catch(Exception e)
				{
					log.error(e, "Cannot initialize watch address field");
				}
			}

			prefForm.addCommand(okCmd);
			prefForm.addCommandListener(this);
		}
		else
		{ // Set up existing form

			// GPS
			if(gps_box != null /* && Device.supportsGPS() */)
				gps_box.setSelected(preferences.isUseGPS());

			// Memory card
			if(memoryCard_box != null /* && Device.supportsSavingToFile() */)
				memoryCard_box.setSelected(preferences.isPreferMemoryCard());

			// Saving
			saving_box.setSelectedIndex(preferences.getAvailableSavingModes()
					.getIdxForValue(preferences.getSavingMode()));

			// Recording
			if(recordmode_box != null /* && Device.supportsNativeRecorder() */)
				recordmode_box
						.setSelectedIndex((preferences.isJniRecording() ? 1 : 0));

			// Block screensaver
			if(blockScreensaver_box != null /* && Device.supportsNokiaUIAPI() */)
				blockScreensaver_box.setSelected(preferences
						.isBlockScreensaver());

			// Watch
			if(watch_box != null /* && Device.supportsWatch() */)
				watch_box.setSelected(((WatchPreferences) preferences)
						.isUseWatch());

			// Watch address
			// TODO watch address ...
		}
		return prefForm;
	}

	public void actionPerformed(ActionEvent ev)
	{
		try
		{
			MainMidlet.getInstance().showMeasureForm(); // !!!
		}
		catch(Exception e)
		{
			log.error(e, "Exception in ActionPerformed: " + e.getMessage());
		}
	}

}
