/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation)
 *  
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011
 * --------------------------------------------------------------------------------
 *  This library is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU Lesser General Public License, version 2.1, as published
 *  by the Free Software Foundation.
 *  
 *  This library is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License along
 *  with this library; if not, write to:
 *    Free Software Foundation, Inc.,
 *    51 Franklin Street, Fifth Floor,
 *    Boston, MA  02110-1301, USA.
 *  
 *  Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *  NoiseTube project source code repository: http://code.google.com/p/noisetube
 * --------------------------------------------------------------------------------
 *  More information:
 *   - NoiseTube project website: http://www.noisetube.net
 *   - Sony Computer Science Laboratory Paris: http://csl.sony.fr
 *   - VUB BrusSense team: http://www.brussense.be
 * --------------------------------------------------------------------------------
 */

package net.noisetube.model;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import net.noisetube.util.CustomStringBuffer;
import net.noisetube.util.StringUtils;
import net.noisetube.util.URLUTF8Encoder;
import net.noisetube.util.XMLUtils;

/**
 * Measurement Model
 * 
 * @author maisonneuve, mstevens, sbarthol
 *
 */
public class Measurement
{
	
	private Date timeStamp;
	
	//Equivalent Continuous Sound Pressure Level (unweighted and A-weighted):
	private Double LeqDB;
	private Double LeqDBA;
	
	private Vector userTags;
	private Vector automaticTags;
	private NTLocation location = null;
 
	public Measurement()
	{
		timeStamp = new Date(System.currentTimeMillis());
	}
	
	public Measurement(long timeStampMS)
	{
		this.timeStamp = new Date(timeStampMS);
	}
	
	/**
	 * @return the timeStamp
	 */
	public Date getTimeStamp()
	{
		return timeStamp;
	}
		
	public boolean hasTags()
	{
		return (userTags != null || automaticTags != null);
	}
	
	public boolean hasUserTags()
	{
		return (userTags != null);
	}
	
	public boolean hasAutomaticTags()
	{
		return (automaticTags != null);
	}
	
	public void addUserTag(String tag)
	{
		if(userTags == null)
			userTags = new Vector();
		userTags.addElement(tag);
	}
	
	public void addAutomaticTag(String tag)
	{
		if(automaticTags == null)
			automaticTags = new Vector();
		automaticTags.addElement(tag);
	}
	
	public void addUserTags(String commaSeparatedTags)
	{
		String[] tags = StringUtils.split(commaSeparatedTags, new char[] { ',', ';'} );
		if(tags.length > 0)
		{
			if(userTags == null)
				userTags = new Vector();
			for(int t = 0; t < tags.length; t++)
				userTags.addElement(tags[t].trim());
		}
	}

	public String getTagsString()
	{
		if(userTags == null && automaticTags == null)
			return "";
		else
		{
			StringBuffer bff = new StringBuffer();
			if(userTags != null)
			{
				Enumeration userTagEnum = userTags.elements();
				while(userTagEnum.hasMoreElements())
					bff.append((String) userTagEnum.nextElement() + (userTagEnum.hasMoreElements() ? ", " : ""));
			}
			if(automaticTags != null)
			{
				Enumeration autoTagEnum = automaticTags.elements();
				while(autoTagEnum.hasMoreElements())
					bff.append((bff.length() > 0 ? ", " : "") + (String) autoTagEnum.nextElement());
			}
			return bff.toString();
		}
	}
	
	/**
	 * @return the LeqDB
	 */
	public double getLeqDB()
	{
		return LeqDB.doubleValue();
	}

	/**
	 * @param LeqDB the LeqDB to set
	 */
	public void setLeqDB(double LeqDB)
	{
		this.LeqDB = new Double(LeqDB);
	}
	
	public boolean isLeqDBSet()
	{
		return LeqDB != null;
	}

	/**
	 * @return the LeqDBA
	 */
	public double getLeqDBA()
	{
		return LeqDBA.doubleValue();
	}

	/**
	 * @param LeqDBA the LeqDBA to set
	 */
	public void setLeqDBA(double LeqDBA)
	{
		this.LeqDBA = new Double(LeqDBA);
	}

	public boolean isLeqDBASet()
	{
		return LeqDBA != null;
	}
	
	/**
	 * @return the location
	 */
	public NTLocation getLocation()
	{
		return location;
	}
	/**
	 * @param location the location to set
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
		return "date: " + timeStamp + (location != null ? "; location: " + location.toString() : "") + "; 1rst leq: " + LeqDBA + " dB(A)";
	}
	
	/**
	 * URL representation
	 */
	public String toUrl()
	{
		StringBuffer bff = new StringBuffer();
		bff.append("db=" + (int)LeqDBA.doubleValue());
		bff.append("&time=" + lowerCaseURLEncode(XMLUtils.timeDateValue(timeStamp.getTime())));
		if(location != null)
			bff.append("&l=" + location.toString());
		if(userTags != null)
			bff.append("&tag=" + lowerCaseURLEncode(getTagsString()));
		return bff.toString();
	}
    
	/**
	 * JSON representation [local time(YYYY-MM-DDThh:mm:ss),db,location,tag]
	 */
	public String toJSON()
	{
		StringBuffer bff = new StringBuffer("[");
		bff.append("\"" + XMLUtils.timeDateValue( timeStamp.getTime()) + "\",");
		bff.append((int)LeqDBA.doubleValue() + ",");
		if(location != null)
			bff.append("\"" + location.toString() + "\",");
		if(userTags != null)
			bff.append("\"" + getTagsString() + "\"");
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
		bff.append("<measurement timeStamp=\"" + XMLUtils.timeDateValue(timeStamp.getTime()) + "\"");
		bff.append(" loudness=\"" + (int)LeqDBA.doubleValue() + "\"");
		if(location != null)
			bff.append(" location=\"" + location.toString() + "\"");
		if(userTags != null)
			bff.append(" tags=\"" + getTagsString() + "\"");
		bff.append("/>");
		return bff.toString();
	}
	
	private String lowerCaseURLEncode(String s)
	{
		return URLUTF8Encoder.encode(s.toLowerCase());
	}
	
}
