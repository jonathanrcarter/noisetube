package noisetube.watch;

import javax.microedition.location.Coordinates;

/**
 * 
 * @author maisonneuve
 * 
 */
public class WatchDataFrame
{

	private long timeStamp;

	private float loudness;
	private float ozone;
	private float batteryLevel;
	private Coordinates coordinates;

	public WatchDataFrame()
	{
		timeStamp = System.currentTimeMillis();
	}

	public void setLocation(float latitude, float longitude)
	{
		this.coordinates = new Coordinates(latitude, longitude, 0);
	}

	public void setLoudness(float loudness)
	{
		this.loudness = loudness;
	}

	/**
	 * @return the timeStamp
	 */
	public long getTimeStamp()
	{
		return timeStamp;
	}

	/**
	 * @return the coordinates
	 */
	public Coordinates getCoordinates()
	{
		return coordinates;
	}

	/**
	 * @return the loudness
	 */
	public float getLoudness()
	{
		return loudness;
	}

	/**
	 * @return the ozone
	 */
	public float getOzone()
	{
		return ozone;
	}

	/**
	 * @return the batteryLevel
	 */
	public float getBatteryLevel()
	{
		return batteryLevel;
	}

}
