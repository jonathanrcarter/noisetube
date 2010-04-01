package noisetube.location.gps;

import javax.microedition.location.Criteria;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;

import noisetube.util.Logger;

/**
 * Modified from Sony Ericsson example code
 * 
 */
public class LocationProviderAdapter
{

	private static Logger log = Logger.getInstance();

	private static Criteria criteria = null;
	private static LocationProvider lp = null;

	public static synchronized LocationProvider getLocationProvider()
	{
		return getLocationProvider(null);
	}

	public static synchronized LocationProvider getLocationProvider(
			Criteria criteria)
	{
		if(lp == null
				|| (criteria != null && !criteria
						.equals(LocationProviderAdapter.criteria)))
		{
			lp = null;
			try
			{
				lp = LocationProvider.getInstance(criteria);
			}
			catch(LocationException locationexception)
			{
			}
			if(lp == null && criteria != null)
			{
				log
						.debug("No LocationProvider found for requested criteria, trying default");
				return getLocationProvider(null);
			}
			LocationProviderAdapter.criteria = criteria;
		}
		return lp;
	}

}