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

package noisetube.util;

/**
 * 
 * @author maisonneuve
 * 
 */
public class CyclicQueue
{

	private boolean boucle_ = false;
	int capacity = 10;
	int start_idx = -1;
	private Object queue[];

	public CyclicQueue()
	{
		this(10);
	}

	public CyclicQueue(int capacity)
	{
		this.capacity = capacity;
		queue = new Object[capacity];
	}

	public int getCapacity()
	{
		return capacity;
	}

	public Object get(int i)
	{
		return queue[get_idx(i)];
	}

	public int getSize()
	{
		if(boucle_)
			return capacity;
		else
			return(start_idx + 1);
	}

	/**
	 * add a value
	 */
	public void push(Object object)
	{
		if(!boucle_ && start_idx + 1 == capacity)
			boucle_ = true;
		start_idx = (start_idx + 1) % capacity;
		queue[start_idx] = object;
	}

	/**
	 * get index
	 * 
	 * @param i
	 * @return
	 */
	private int get_idx(int i)
	{
		if(boucle_)
			return (start_idx + 1 + i) % capacity;
		else
			return i;
	}
}
