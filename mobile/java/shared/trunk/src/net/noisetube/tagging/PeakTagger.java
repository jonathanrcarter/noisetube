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
 * TODO improve this to tag the actual peaks
 *
 */
public class PeakTagger implements IProcessor
{

	private static final int MEMORY_CAPACITY = 6;
	private static final int STATE_HIGHVARIATION = 1;
	private static final int STATE_NOHIGHVARIATION = 2;
	public static final String HIGH_VARIATION_TAG = "variation:high";
	
	private int state = 0;
	private CyclicQueue memory = new CyclicQueue(MEMORY_CAPACITY);

	public void process(Measurement newMeasurement, Track track)
	{
		memory.offer(new Double(newMeasurement.getLeqDBA()));
		if(hasHighVariation())
			if(state != STATE_HIGHVARIATION)
			{
				this.state = STATE_HIGHVARIATION;
				newMeasurement.addAutomaticTag(HIGH_VARIATION_TAG);
			}
		else
			state = STATE_NOHIGHVARIATION;
	}

	private double getMem(int i)
	{
		return ((Double) memory.getElement(i)).doubleValue();
	}

	private boolean hasHighVariation()
	{
		// find peak
		if(memory.getSize() > 3)
		{
			int size = memory.getSize();
			double past = Math.min(getMem(size - 3), getMem(size - 2));
			double now = getMem(size - 1);
			// if more than 10 db in 2 second -> high variation
			if(now > (past + 10))
			{
				return true;
			}
		}
		return false;
	}

	public void reset()
	{
		state = 0;
		memory = new CyclicQueue(MEMORY_CAPACITY);
	}

	public String getName()
	{
		return "High variation tagger";
	}
	
}
