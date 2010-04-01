package noisetube.location;

import noisetube.MainMidlet;
import noisetube.config.Device;
import noisetube.config.Preferences;
import noisetube.config.WatchPreferences;
import noisetube.core.Engine;
import noisetube.model.NTLocation;
import noisetube.ui.LocationUIComponent;
import noisetube.util.Logger;

/**
 * @author maisonneuve, mstevens
 * 
 */
public class LocationComponent
{

	protected Logger log = Logger.getInstance();

	protected Engine engine;
	protected LocationUIComponent ui;

	protected boolean running = false;
	protected String statusMessage = "";

	protected NTLocation lastLocation = null;

	/**
	 * @return either GPS-supporting (LocationComponentGPS subclass) or GPS-less
	 *         (LocationComponent superclass) version depending on device and
	 *         client capabilities
	 * @throws Exception
	 */
	public static LocationComponent getLocationComponent(Engine engine)
			throws Exception
	{
		LocationComponent lc = null;
		if(Device.supportsGPS()) // Location API supported AND this NoiseTube
									// client has GPS support compiled in
		{
			Class LCClass;
			Preferences preferences = MainMidlet.getInstance().getPreferences();
			if(preferences instanceof WatchPreferences)
				LCClass = Class
						.forName("noisetube.watch.LocationComponentWatch");
			else
				LCClass = Class
						.forName("noisetube.location.gps.LocationComponentGPS");
			lc = (LocationComponent) LCClass.newInstance();
		}
		else
			lc = new LocationComponent(); // use basic GPS-less
											// LocationComponent (this class)
		lc.engine = engine;
		return lc;
	}

	public void setObserver(LocationUIComponent ui)
	{
		this.ui = ui;
	}

	protected void updateObserver()
	{
		if(ui != null)
			ui.update();
	}

	public void start()
	{
		running = true;
		log.debug("LocationComponent (GPS-less) started");
	}

	public boolean isRunning()
	{
		return running;
	}

	public void stop()
	{
		running = false;
	}

	public void toggleAutoMode()
	{
	} // should be overridden in LocationComponentGPS

	public boolean isAutoModeEnabled() // should be overridden in
										// LocationComponentGPS
	{
		return false;
	}

	/**
	 * @return the statusMessage
	 */
	public String getStatusMessage()
	{
		return statusMessage;
	}

	/**
	 * @param statusMessage
	 */
	public void setStatusMessage(String statusMessage)
	{
		this.statusMessage = statusMessage;
		updateObserver(); // !!!
	}

	public void setLocationTag(String locationTag)
	{
		if(locationTag == null || locationTag.equals("")
				|| locationTag.equalsIgnoreCase("null"))
			this.lastLocation = null;
		else
		{
			this.lastLocation = new NTLocation(locationTag);
			log.debug("Manual location: " + lastLocation.toString());
		}
	}

	/**
	 * @return the lastLocation
	 */
	public NTLocation getLastLocation()
	{
		return lastLocation;
	}

}
