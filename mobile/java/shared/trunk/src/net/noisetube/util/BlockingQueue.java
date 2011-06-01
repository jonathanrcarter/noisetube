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

package net.noisetube.util;

import net.noisetube.model.Track;

/**
 * Blocking Queue data structure
 * 
 * @author maisonneuve, mstevens
 */
public class BlockingQueue
{

	private static final int DEFAULT_CAPACITY = Track.DEFAULT_BUFFER_CAPACITY;
	
	private int queue_size;
	private Object queue[];
	private int head;
	private int tail;

	public BlockingQueue()
	{
		this(DEFAULT_CAPACITY);
	}

	public BlockingQueue(int capacity)
	{
		queue_size = capacity;
		queue = new Object[queue_size];
		head = 0;
		tail = 0;
	}

	public int size()
	{
		return head - tail;
	}

	public synchronized void enqueue(Object obj)
	{
		head++;
		queue[head %= queue_size] = obj;
		notify();
	}

	public synchronized Object dequeue()
	{
		try
		{
			if(head == tail)
				wait();
		}
		catch(InterruptedException _ex)
		{
			return null;
		}

		tail++;
		return queue[tail %= queue_size];
	}

}