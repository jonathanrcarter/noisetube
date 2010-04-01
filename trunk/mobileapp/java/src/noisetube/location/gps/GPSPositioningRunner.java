package noisetube.location.gps;

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationProvider;

import noisetube.MainMidlet;
import noisetube.util.Logger;

/**
 * Based on Sony Ericsson example code
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class GPSPositioningRunner implements Runnable
{

	// STATICS:
	private static Criteria DEFAULT_LOCATIONPROVIDER_CRITERIA;

	static
	{
		DEFAULT_LOCATIONPROVIDER_CRITERIA = new Criteria();
		DEFAULT_LOCATIONPROVIDER_CRITERIA
				.setHorizontalAccuracy(Criteria.NO_REQUIREMENT);
		DEFAULT_LOCATIONPROVIDER_CRITERIA
				.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
		DEFAULT_LOCATIONPROVIDER_CRITERIA.setPreferredResponseTime(5000); // in
																			// ms;
																			// default:
																			// Criteria.NO_REQUIREMENT
		DEFAULT_LOCATIONPROVIDER_CRITERIA
				.setPreferredPowerConsumption(Criteria.NO_REQUIREMENT); // Criteria.POWER_USAGE_HIGH
		DEFAULT_LOCATIONPROVIDER_CRITERIA.setCostAllowed(true); // allowed to
																// cost
		DEFAULT_LOCATIONPROVIDER_CRITERIA.setSpeedAndCourseRequired(false);
		DEFAULT_LOCATIONPROVIDER_CRITERIA.setAltitudeRequired(false);
		DEFAULT_LOCATIONPROVIDER_CRITERIA.setAddressInfoRequired(false);
	}

	private static final int DEFAULT_INTERVAL_TIME = 2;
	private static final int DEFAULT_TIMEOUT = 1;
	private static final int DEFAULT_MAX_AGE = 1;

	// DYNAMICS:
	private Logger log = Logger.getInstance();
	private LocationComponentGPS locComp;

	private volatile boolean running = false;
	private volatile boolean initialized = false;

	private final PositioningListener plistener = new PositioningListener();
	private LocationProvider lp = null;
	private Location gpsLocation = null;

	private int interval_time; // interval (in second) of each location request

	public GPSPositioningRunner(LocationComponentGPS locComp)
	{
		this.locComp = locComp;
		setResponseTime(DEFAULT_INTERVAL_TIME);
	}

	public boolean isRunning()
	{
		return running;
	}

	public void start()
	{
		if(!running)
		{
			log.debug("Starting GPS");
			new Thread(this).start();
		}
	}

	public void stop()
	{
		log.debug("Stopping GPS");
		running = false;
	}

	public Location getLastLocation()
	{
		if(gpsLocation != null && gpsLocation.isValid())
			return gpsLocation;
		else
			return null;
	}

	public boolean isInitialized()
	{
		return initialized;
	}

	private void initialize()
	{
		try
		{
			if(lp == null)
			{
				lp = LocationProviderAdapter
						.getLocationProvider(DEFAULT_LOCATIONPROVIDER_CRITERIA);
			}
			if(lp == null)
				log.debug("Failed to get LocationProvider");
			else
			{
				log.debug("Got LocationProvider");
				lp.setLocationListener(plistener, interval_time,
						DEFAULT_TIMEOUT, DEFAULT_MAX_AGE);
				initialized = true;
			}
		}
		catch(SecurityException se)
		{
			log.debug("Permission to use Localisation feature not granted");
			locComp.disableAutoMode();
			MainMidlet.getInstance().getPreferences().setUseGPS(false); // so
																		// GPS
																		// is
																		// not
																		// retried
																		// (avoid
																		// to
																		// keep
																		// bugging
																		// user
																		// with
																		// Location
																		// API
																		// permission
																		// requests)
			initialized = false;
		}
		catch(Exception e)
		{
			log.error(e, "Exception in GPSPositioningRunner.initialize()");
			locComp.disableAutoMode();
			MainMidlet.getInstance().getPreferences().setUseGPS(false); // so
																		// GPS
																		// is
																		// not
																		// retried
																		// (avoid
																		// to
																		// keep
																		// bugging
																		// user
																		// with
																		// Location
																		// API
																		// permission
																		// requests)
			initialized = false;
		}
	}

	public void run()
	{
		// INIT---------------------------------------------
		if(!initialized)
			initialize();
		if(initialized) // did init work?
			running = true;
		// RUN----------------------------------------------
		while(running)
		{
			if(plistener != null)
			{
				Location newGpsLocation = null;
				try
				{
					newGpsLocation = plistener.waitForLocation();
					// newGpsLocation = lp.getLocation(5);
					if(newGpsLocation != null && newGpsLocation.isValid())
						gpsLocation = newGpsLocation;
					else
						gpsLocation = null;
				}
				/*
				 * catch(LocationException le) { log.debug("GPS timeout"); }
				 */
				catch(Exception e)
				{
					log.error(e, "GPSPositioningRunner.run()");
					gpsLocation = null;
				}
			}
			else
			{
				log.error("LocationProvider or pListener is null!");
				running = false;
			}
		}
		// STOP---------------------------------------------
		if(lp != null)
			lp.reset();
	}

	public void setResponseTime(int responseTime)
	{
		interval_time = responseTime;
	}
}