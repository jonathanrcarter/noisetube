package noisetube.model;

/**
 * @author mstevens
 * 
 */
public class NTLocation
{

	private String locationTag;

	protected NTLocation()
	{
	}

	/**
	 * @param locationTag
	 */
	public NTLocation(String locationTag)
	{
		if(locationTag == null)
			throw new NullPointerException("locationTag cannot be null!");
		if(locationTag.equals("") || locationTag.equalsIgnoreCase("null"))
			throw new IllegalArgumentException(
					"location tag cannot be empty or \"null\"");
		this.locationTag = locationTag;
	}

	/**
	 * @return the locationTag
	 */
	public String getLocationTag()
	{
		return locationTag;
	}

	public String toString()
	{
		return locationTag;
	}

}
