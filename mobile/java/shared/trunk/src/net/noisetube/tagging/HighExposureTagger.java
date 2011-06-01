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

package net.noisetube.tagging;

import net.noisetube.core.IProcessor;
import net.noisetube.model.Measurement;
import net.noisetube.model.Track;
import net.noisetube.util.CyclicQueue;

/**
 * @author maisonneuve, mstevens
 * 
 */
public class HighExposureTagger implements IProcessor
{

	private static final int MEMORY_CAPACITY = 10;
	private static final int STATE_HIGH = 1;
	private static final int STATE_NOTHIGH = 2;
	public static final String HIGH_EXPOSURE_TAG = "exposure:high";
	
	private int state = 0;
	private CyclicQueue memory = new CyclicQueue(MEMORY_CAPACITY);
	
	public void process(Measurement newMeasurement, Track track)
	{
		memory.offer(new Double(newMeasurement.getLeqDBA()));
		if(isHighExposure())
			if(state != STATE_HIGH)
			{
				this.state = STATE_HIGH;
				newMeasurement.addAutomaticTag(HIGH_EXPOSURE_TAG);
			}
		else
			this.state = STATE_NOTHIGH;
	}

	private boolean isHighExposure()
	{
		boolean high_state = false;

		// find peak
		if(memory.getSize() == MEMORY_CAPACITY)
		{
			high_state = true;
			for(int i = 0; i < MEMORY_CAPACITY; i++)
			{
				if(((Double) memory.getElement(i)).doubleValue() < 80)
					high_state = false;
			}
		}
		return high_state;
	}
	
	public void reset()
	{
		state = 0;
		memory = new CyclicQueue(MEMORY_CAPACITY);
	}

	public String getName()
	{
		return "High exposure tagger";
	}

}
