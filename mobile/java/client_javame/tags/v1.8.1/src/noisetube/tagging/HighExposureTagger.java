/** 
 * -------------------------------------------------------------------------------
 * NoiseTube - Mobile client (J2ME version)
 * Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 * 
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License, version 2.1,
 * as published by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * --------------------------------------------------------------------------------
 * 
 * Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 * NoiseTube project source code repository: http://code.google.com/p/noisetube
 * NoiseTube project website: http://www.noisetube.net
 */

package noisetube.tagging;

import noisetube.audio.ILeqListener;
import noisetube.util.CyclicQueue;

public class HighExposureTagger implements ILeqListener, IAutomaticTagger
{

	final int HIGH_STATE_MEMORY = 10;
	final int STATE_HIGH = 1;
	final int STATE_NOHIGH = 2;
	int state = 0;

	TagListener listener;

	CyclicQueue memory = new CyclicQueue(HIGH_STATE_MEMORY);

	public void sendLeq(double leq)
	{
		memory.push(new Double(leq));
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
		return ((Double) memory.get(i)).doubleValue();
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

	public void setListener(TagListener taglistener)
	{
		this.listener = taglistener;

	}
}
