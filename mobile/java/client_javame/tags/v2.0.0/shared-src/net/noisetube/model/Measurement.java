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
	private Double loudnessLeqDB;
	private Double loudnessLeqDBA;
	
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
	
	/**
	 * @return the userTags
	 */
	public Vector getUserTags()
	{
		return userTags;
	}

	/**
	 * @return the automaticTags
	 */
	public Vector getAutomaticTags()
	{
		return automaticTags;
	}

	public String getTagsString()
	{
		if(userTags == null && automaticTags == null)
			return "";
		else
		{
			StringBuffer bff = new StringBuffer();
			Enumeration userTagEnum = userTags.elements();
			while(userTagEnum.hasMoreElements())
				bff.append((String) userTagEnum.nextElement() + (userTagEnum.hasMoreElements() ? ", " : ""));
			Enumeration autoTagEnum = automaticTags.elements();
			while(autoTagEnum.hasMoreElements())
				bff.append((bff.length() > 0 ? ", " : "") + (String) autoTagEnum.nextElement());
			return bff.toString();
		}
	}
	
	public boolean hasTags()
	{
		return (userTags != null || automaticTags != null);
	}
	
	public void addUserTag(String tag)
	{
		if(!tag.equals("") && !tag.equalsIgnoreCase("null"))
		{
			if(userTags == null)
				userTags = new Vector();
			userTags.addElement(tag);
		}
	}
	
	public void addTags(Vector tagsToAdd)
	{
		if(tagsToAdd != null)
		{
			if(userTags == null)
				userTags = new Vector();
			Enumeration tagEnum = tagsToAdd.elements();
			while(tagEnum.hasMoreElements())
				userTags.addElement(tagEnum.nextElement());
		}
	}
	
	/**
	 * @return the loudnessLeqDB
	 */
	public double getLoudnessLeqDB()
	{
		return loudnessLeqDB.doubleValue();
	}

	/**
	 * @param loudnessLeqDB the loudnessLeqDB to set
	 */
	public void setLoudnessLeqDB(double loudnessLeqDB)
	{
		this.loudnessLeqDB = new Double(loudnessLeqDB);
	}
	
	public boolean isLoudnessLeqDBSet()
	{
		return loudnessLeqDB != null;
	}

	/**
	 * @return the loudnessLeqDBA
	 */
	public double getLoudnessLeqDBA()
	{
		return loudnessLeqDBA.doubleValue();
	}

	/**
	 * @param loudnessLeqDBA the loudnessLeqDBA to set
	 */
	public void setLoudnessLeqDBA(double loudnessLeqDBA)
	{
		this.loudnessLeqDBA = new Double(loudnessLeqDBA);
	}

	public boolean isLoudnessLeqDBASet()
	{
		return loudnessLeqDBA != null;
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
	 * @return the reduced samples
	 */
	/*
	public double[][] getReducedSamples()
	{
		return reducedSamples;
	}
	*/
	
	/**
	 * @param reducedSamples the reduced samples to set
	 */
	/*
	public void setReducedSamples(double[][] reducedSamples)
	{
		this.reducedSamples = reducedSamples;
	}
	*/
	
	/**
	 * String representation
	 */
	public String toString()
	{
		return "date: " + timeStamp + (location != null ? "; location: " + location.toString() : "") + "; 1rst leq: " + loudnessLeqDBA + " dB(A)";
	}
	
	/**
	 * URL representation
	 */
	public String toUrl()
	{
		StringBuffer bff = new StringBuffer();
		bff.append("db=" + (int)loudnessLeqDBA.doubleValue());
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
		bff.append((int)loudnessLeqDBA.doubleValue() + ",");
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
		bff.append(" loudness=\"" + (int)loudnessLeqDBA.doubleValue() + "\"");
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
