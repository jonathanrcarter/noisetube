package noisetube.model;

import java.util.Date;

import noisetube.util.CustomStringBuffer;
import noisetube.util.URLUTF8Encoder;
import noisetube.util.XMLUtils;

/**
 * Measure Model
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class Measure
{

	private Date date;
	private double leq_dBA;

	private String tags = null;
	private NTLocation location = null;

	public Measure()
	{
		this(0);
	}

	public Measure(double leq_dBA)
	{
		this.leq_dBA = leq_dBA;
		date = new Date(System.currentTimeMillis());
	}

	public String getTags()
	{
		return tags;
	}

	public void setTags(String tags)
	{
		this.tags = tags;
	}

	/**
	 * @return the date
	 */
	public Date getDate()
	{
		return date;
	}

	/**
	 * @return the leqs
	 */
	public double getLeq()
	{
		return leq_dBA;
	}

	/**
	 * @return the location
	 */
	public NTLocation getLocation()
	{
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(NTLocation location)
	{
		this.location = location;
	}

	/**
	 * String representation
	 */
	public String toString()
	{
		return "date: " + date + ", 1rst leq: " + leq_dBA;
	}

	/**
	 * URL representation
	 */
	public String toUrl()
	{
		StringBuffer bff = new StringBuffer();
		bff.append("db=" + (int) leq_dBA);
		bff.append("&time=" + encode(XMLUtils.timeDateValue(date.getTime())));
		if(location != null)
			bff.append("&l=" + location.toString());
		if(tags != null && !tags.equals("") && !tags.equalsIgnoreCase("null"))
			bff.append("&tag=" + encode(tags));
		return bff.toString();
	}

	/**
	 * JSON representation [local time(YYYY-MM-DDThh:mm:ss),db,location,tag]
	 */
	public String toJSON()
	{
		StringBuffer bff = new StringBuffer("[");
		bff.append("\"" + XMLUtils.timeDateValue(date.getTime()) + "\",");
		bff.append((int) leq_dBA + ",");
		if(location != null)
			bff.append("\"" + location.toString() + "\",");
		if(tags != null && !tags.equals("") && !tags.equalsIgnoreCase("null"))
			bff.append("\"" + tags + "\"");
		else
			bff.append("\"\"");
		bff.append("]");
		return bff.toString();
	}

	/**
	 * XML representation
	 */
	public String toXML()
	{
		CustomStringBuffer bff = new CustomStringBuffer();
		bff.append("<measurement timeStamp=\""
				+ XMLUtils.timeDateValue(date.getTime()) + "\"");
		bff.append(" loudness=\"" + (int) leq_dBA + "\"");
		if(location != null)
			bff.append(" location=\"" + location.toString() + "\"");
		if(tags != null && !tags.equals("") && !tags.equalsIgnoreCase("null"))
			bff.append(" tags=\"" + tags + "\"");
		bff.append("/>");
		return bff.toString();
	}

	private String encode(String s)
	{
		return URLUTF8Encoder.encode(s.toLowerCase());
	}

}
