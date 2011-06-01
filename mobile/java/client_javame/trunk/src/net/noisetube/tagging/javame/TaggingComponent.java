/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Java ME version)
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

package net.noisetube.tagging.javame;

import java.util.Enumeration;

import net.noisetube.core.IProcessor;
import net.noisetube.model.Measurement;
import net.noisetube.model.Track;

/**
 * @author mstevens, maisonneuve
 *
 */
public class TaggingComponent implements IProcessor
{
	
	private static int TAGGING_DELAY_MS = 4000; //4 seconds

	private String tags = null;
	private long tagTimeStamp;

	public boolean ready = true;


	/**
	 * @param tags string of tags (comma separated)
	 * @param tagTimeStamp time when th user started typing the tag(s)
	 */
	public void setTags(String tags, long tagTimeStamp)
	{
		this.tags = tags;
		this.tagTimeStamp = tagTimeStamp;
	}

	public void process(Measurement newMeasurement, Track track)
	{
		if(tags != null)
		{
			//Associate tag with measurement made TAGGING_DELAY ms before the user started typing the tag:
			Enumeration mEnum = track.getMeasurementsNewestFirst();
			while(mEnum.hasMoreElements())
			{
				Measurement m = (Measurement) mEnum.nextElement();
				if(m.getTimeStamp().getTime() + TAGGING_DELAY_MS < tagTimeStamp)
				{
					m.addUserTags(tags);
					break;
				}
			}
			tags = null;
			tagTimeStamp = 0;
		}
	}

	public void reset()
	{
		tags = null;
		tagTimeStamp = 0;	
	}

	public String getName()
	{
		return "Human tagging";
	}
	
}
