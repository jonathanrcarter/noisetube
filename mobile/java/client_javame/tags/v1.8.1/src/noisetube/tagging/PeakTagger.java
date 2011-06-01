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

public class PeakTagger implements ILeqListener, IAutomaticTagger
{

	final int MEMORY = 4;
	final int STATE_HIGHVARIATION = 1;
	final int STATE_NOHIGH = 2;
	int state = 0;

	TagListener listener;

	CyclicQueue memory = new CyclicQueue(MEMORY);

	public void sendLeq(double leq)
	{
		memory.push(new Double(leq));
		if(high_variation_state())
		{
			if(getState() != STATE_HIGHVARIATION)
			{
				setState(STATE_HIGHVARIATION);
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
		if(listener != null && state == STATE_HIGHVARIATION)
		{
			listener.sendTag("variation:high");
		}
	}

	private double getMem(int i)
	{
		return ((Double) memory.get(i)).doubleValue();
	}

	private boolean high_variation_state()
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

	public void setListener(TagListener taglistener)
	{
		this.listener = taglistener;

	}
}
