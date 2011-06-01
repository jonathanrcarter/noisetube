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

import net.noisetube.model.IMeasurementListener;
import net.noisetube.model.Measurement;
import net.noisetube.util.CyclicQueue;

/**
 * @author maisonneuve
 *
 */
public class HighExposureTagger implements IMeasurementListener, IAutomaticTagger
{

	final int HIGH_STATE_MEMORY = 10;
	final int STATE_HIGH = 1;
	final int STATE_NOHIGH = 2;
	int state = 0;

	ITagListener listener;

	CyclicQueue memory = new CyclicQueue(HIGH_STATE_MEMORY);

	
	public void receiveMeasurement(Measurement m)
	{
		memory.offer(new Double(m.getLoudnessLeqDBA()));
		if(high_state())
		{
			if(getState() != STATE_HIGH)
			{
				setState(STATE_HIGH);
			}
		}
		else
		{
			setState(STATE_NOHIGH);
		}
	}

	private int getState()
	{
		return state;
	}

	private void setState(int state)
	{
		this.state = state;
		if(listener != null && state == STATE_HIGH)
		{
			listener.sendTag("exposure:high");
		}
	}

	private double getMem(int i)
	{
		return ((Double) memory.getElement(i)).doubleValue();
	}

	private boolean high_state()
	{
		boolean high_state = false;

		// find peak
		if(memory.getSize() == HIGH_STATE_MEMORY)
		{
			high_state = true;
			for(int i = 0; i < HIGH_STATE_MEMORY; i++)
			{
				if(getMem(i) < 80)
				{
					high_state = false;
				}
			}
		}
		return high_state;
	}

	public void setListener(ITagListener taglistener)
	{
		this.listener = taglistener;

	}

}
