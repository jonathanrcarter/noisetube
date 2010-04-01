package noisetube.location.gps;

import javax.microedition.location.Coordinates;

import noisetube.model.NTLocation;

/**
 * @author mstevens
 * 
 */
public class NTLocationGPS extends NTLocation
{

	private Coordinates coordinates = null;

	public NTLocationGPS(Coordinates coordinates)
	{
		// super();
		this.coordinates = coordinates;
	}

	/**
	 * @return the coordinates
	 */
	public Coordinates getCoordinates()
	{
		return coordinates;
	}

	public String toString()
	{
		return "geo:" + coordinates.getLatitude() + ","
				+ coordinates.getLongitude();
	}

}
