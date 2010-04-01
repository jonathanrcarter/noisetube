package noisetube.ui;

import noisetube.MainMidlet;
import noisetube.config.Preferences;
import noisetube.core.Engine;
import noisetube.core.IMeasureListener;
import noisetube.model.Measure;
import noisetube.util.Logger;

import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Form;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.FocusListener;
import com.sun.lwuit.layouts.BorderLayout;

/**
 * MeasureForm (main form of the program)
 * 
 * @author maisonneuve, mstevens
 */
public class MeasureForm implements ActionListener, IMeasureListener
{

	private Logger log = Logger.getInstance();
	private MainMidlet midlet;
	private Engine engine;
	private Preferences preferences;

	// UI
	private Form f;
	protected Component focus_component;
	private LocationUIComponent location_ui;
	private NotesUIComponent notes_ui;
	private LogUIComponent log_ui;
	private LoudnessUIComponent loudness_ui;
	private TabUIComponent tabs;

	// COMMANDS
	public Command cmdStart = new Command("Start");
	public Command cmdStop = new Command("Stop");
	public Command cmdExit = new Command("Exit");
	public Command cmdPreferences = new Command("Preferences");
	public Command cancel_selection = new Command("Cancel");
	public Command cmdCalibrate = new Command("Calibrate");
	public Command cmdSwitchUser = new Command("Switch user");
	public Command cmdAbout = new Command("About");

	public MeasureForm()
	{
		midlet = MainMidlet.getInstance();
		engine = midlet.getEngine();
		engine.setForm(this);
		preferences = midlet.getPreferences();
	}

	public Form getForm()
	{
		if(f != null)
			return f;

		// UI
		f = new Form("Your noise exposure");

		tabs = new TabUIComponent(this);

		loudness_ui = new LoudnessUIComponent();
		loudness_ui.setPreferredH(70);
		location_ui = new LocationUIComponent();
		engine.getLocationComponent().setObserver(location_ui); // !!!
		notes_ui = new NotesUIComponent(this);
		engine.getNotesComponent().setUIComponent(notes_ui);
		log_ui = new LogUIComponent();

		f.setScrollable(false);
		f.setLayout(new BorderLayout());

		tabs.setLocationComponent(engine.getLocationComponent());
		tabs.setNoteComponent(engine.getNotesComponent());

		f.addComponent(BorderLayout.NORTH, loudness_ui);
		f.addComponent(BorderLayout.WEST, tabs);

		setCommands(); // !!!
		f.addCommandListener(this);

		f.addFocusListener(new FocusListener()
		{
			public void focusGained(Component component)
			{
				focus_component = component;
			}

			public void focusLost(Component component)
			{
				focus_component = null;
			}
		});

		f.getStyle().setMargin(0, 0, 0, 0);
		f.getStyle().setPadding(0, 0, 0, 0);

		// Update location ui component:
		location_ui.update();

		// Log file location:
		if(log.isFileModeActive())
			log_ui.setPrimaryMessage("Log file location: "
					+ log.getLogFilePath().substring(8));

		return f;
	}

	public boolean isRunning()
	{
		return engine.isRunning();
	}

	private void setCommands()
	{
		f.removeAllCommands();
		if(isRunning())
			f.addCommand(cmdStop);
		else
			f.addCommand(cmdStart);
		f.addCommand(cmdExit);
		f.addCommand(cmdAbout);
		if(preferences.getSavingMode() == Preferences.SAVE_HTTP
				&& preferences.isAuthenticated())
			f.addCommand(cmdSwitchUser);
		f.addCommand(cmdCalibrate);
		f.addCommand(cmdPreferences);
		// f.setBackCommand(cmdExit);
	}

	public void start()
	{
		// Engine:
		engine.start();
		setCommands();
	}

	public void stop()
	{
		if(engine.isRunning())
		{
			engine.stop();
			setCommands();
			getLoudness_ui().reset();
			f.setTitle("Stopped");
		}
	}

	public void actionPerformed(ActionEvent actionevent)
	{
		try
		{
			if(cmdStart.equals(actionevent.getCommand()))
			{
				start();
			}
			else
			{
				if(cmdStop.equals(actionevent.getCommand()))
					stop();
				else if(cmdExit.equals(actionevent.getCommand()))
					midlet.destroyApp(true);
				else if(cmdSwitchUser.equals(actionevent.getCommand()))
				{
					stop();
					midlet.showLoginForm();
				}
				else if(cmdPreferences.equals(actionevent.getCommand()))
				{
					stop();
					midlet.showPreferencesForm();
				}
				else if(cmdAbout.equals(actionevent.getCommand()))
					midlet.showAboutForm();
				else if(cmdCalibrate.equals(actionevent.getCommand()))
				{
					stop();
					midlet.showCalibrationForm();
				}
			}
		}
		catch(Exception e)
		{
			log.error(e, "action measure");
		}
	}

	/**
	 * called from Engine
	 * 
	 * @param sound
	 */
	public void receive(Measure sound)
	{
		loudness_ui.update_graph(sound);
	}

	public void display_location_ui_component()
	{
		displaySubComponent(location_ui);
	}

	public void display_notes_ui_component()
	{
		displaySubComponent(notes_ui);
	}

	public void display_log_ui_component()
	{
		displaySubComponent(log_ui);
		log_ui.displayLog();
	}

	public void displaySubComponent(Component component)
	{
		f.removeComponent(component);
		f.addComponent(BorderLayout.CENTER, component);
		f.repaint();
	}

	public void setTitle(long milli, long num, float dose)
	{
		long seconds = (milli / 1000) % 60;
		long minutes = (milli / (60 * 1000)) % 60;
		long hours = (minutes / 60) % 60;
		f.setTitle("#" + num + " (" + hours + "h" + minutes + "m" + seconds
				+ "); D=" + (int) Math.floor(dose + 0.5d) + "%");
	}

	/**
	 * @return the tabs
	 */
	public TabUIComponent getTabs()
	{
		return tabs;
	}

	/**
	 * @return the log_ui
	 */
	public LogUIComponent getLog_ui()
	{
		return log_ui;
	}

	/**
	 * @return the loudness_ui
	 */
	public LoudnessUIComponent getLoudness_ui()
	{
		return loudness_ui;
	}

}
