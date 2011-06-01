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

import java.util.Enumeration;
import java.util.NoSuchElementException;

/** 
 * Cyclic Queue implementation
 * 
 * Method names loosely inspired by http://download.oracle.com/javase/6/docs/api/java/util/Queue.html 
 * 
 * @author mstevens, maisonneuve, sbarthol
 */
public class CyclicQueue
{

	private static final int DEFAULT_CAPACITY = 20;

	private int size = 0;
	private int head = 0;
	private Object queue[];

	public CyclicQueue()
	{
		this(DEFAULT_CAPACITY);
	}
	
	public CyclicQueue(int capacity)
	{
		if(capacity <= 0)
			throw new IllegalArgumentException("Queue capacity needs to be > 0");
		this.queue = new Object[capacity];
		
		/*DEBUG*/
		//System.out.println("array\t\tsize\thead\tnextPushIdx");
		//System.out.println(print() + "\t" + size + "\t" + head + "\t" + ((size + head) % queue.length));
	}
	
	public void clear()
	{
		size = 0;
		head = 0;
	}

	public int getCapacity()
	{
		return queue.length;
	}

	/**
	 * Returns the size of this queue. Notice that it can not be larger than the capacity, but it is well possible
	 * that is is smaller, in case the queue isn't completely filled.
	 * @return The size of the queue
	 */
	public int getSize()
	{
		return size;
	}

	public boolean isEmpty()
	{
		return size == 0;
	}

	public boolean isFull()
	{
		return size == queue.length;
	}
	
	/**
	 * Debugging method
	 * 
	 * @return String representation of the state of the internal array
	 */
//	private String print()
//	{
//		StringBuffer bff = new StringBuffer("|");
//		for(int i = 0; i < queue.length; i++)
//			bff.append((queue[i] == null ? " " : queue[i].toString()) + "|");
//		return bff.toString();
//	}
	
	/**
	 * add an element to the queue (at "the back")
	 * 
	 * @return the element that was dropped from the queue if it was already full, null if it was not
	 */
	public Object offer(Object o)
	{
		int pushIdx = 0;
		int cap = queue.length;
		try
		{
			pushIdx = (size + head) % cap;
		}
		catch(Exception e)
		{
			return null;
		}
		Object dropped = null;
		if(isFull())
			dropped = queue[pushIdx];
		queue[pushIdx] = o;
		if(isFull())
			head = (head + 1) % queue.length;
		else
			size++;
		
		/*DEBUG*/
		//System.out.println("Pushed: " + o.toString() + (dropped != null ? "; Dropped: " + dropped.toString() : ""));
		//System.out.println(print() + "\t" + size + "\t" + head + "\t" + ((size + head) % queue.length));
		
		return dropped;
	}
	
	/**
	 * Retrieves and removes(!) the head of this queue, or null if this queue is empty.
	 * 
	 * @return the head of this queue (= oldest element), or null if this queue is empty
	 */
	public Object poll()
	{
		try
		{
			return serve();
		}
		catch(NoSuchElementException nsee)
		{
			return null;
		}
	}
	
	/**
	 * Retrieves and removes(!) the head of this queue (= the oldest element).
	 * This method differs from the poll method in that it throws an exception if this queue is empty.
	 * 
	 * @return oldest element
	 * @throws NoSuchElementException - if this queue is empty
	 */
	public Object serve()
	{
		if(!isEmpty())
		{
			Object toServe = queue[head];			
			size--;
			head = (isEmpty() ? 0 : (head + 1) % queue.length);
			
			/*DEBUG*/
			//System.out.println("Served: " + toServe);
			//System.out.println(print() + "\t" + size + "\t" + head + "\t" + ((size + head) % queue.length));
			
			return toServe;
		}
		else
			throw new NoSuchElementException("Queue is empty");
	}
	
	/**
	 * Retrieves, but does not remove, the head of this queue, returning null if this queue is empty.
	 * 
	 * @return the item at the head of the queue (= the oldest element still in the queue)
	 */
	public Object peek()
	{
		if(!isEmpty())
			return queue[head];
		else
			return null;
	}
	
	/**
	 * Retrieves, but does not remove, the head of this queue. This method differs from the peek method only in that it throws an exception if this queue is empty.
	 * 
	 * @return the element at the head of the queue (= the oldest element still in the queue)
	 * @throws NoSuchElementException - if this queue is empty
	 */
	public Object head()
	{
		if(!isEmpty())
			return queue[head];
		else
			throw new NoSuchElementException("Queue is empty");
	}
	
	/**
	 * Retrieves the last element which was added to the queue
	 * 
	 * @return the most recently added element
	 * @throws NoSuchElementException - if this queue is empty
	 */
	public Object tail()
	{
		return getElement(size - 1);
	}
	
	/**
	 * Returns the element on logical position i, starting from 0, running till size-1
	 * A higher i denotes a newer (more recently offered) element.
	 * 
	 * @param logical position index
	 * @return the element at logical position i
	 * @throws NoSuchElementException - if this queue is empty
	 */
	public Object getElement(int i)
	{
		if(isEmpty())
			throw new NoSuchElementException("Queue is empty");
		return queue[getIndex(i)];
	}
	
	/**
	 * get internal (array) index for logical index i
	 * @param i logical index (0 = oldest element; size-1 = newest element)
	 * @return
	 */
	private int getIndex(int i)
	{
		return (head + i) % queue.length;
	}

	/**
	 * Gives us an enumeration which enumerates starting from the oldest element (the first element added).
	 * @return The enumeration.
	 */
	public Enumeration getElements()
	{
		return new CQEnumerator();
	}
	
	/**
	 * Gives us a reversed enumeration which enumerates starting from the newest element (the last element added).
	 * @return The reversed enumeration.
	 */
	public Enumeration getElementsReversed()
	{
		return new CQEnumerator(true);
	}

	private class CQEnumerator implements Enumeration
	{

		boolean reverse;
		int currentPos;

		public CQEnumerator()
		{
			this(false);
		}

		public CQEnumerator(boolean reverse)
		{
			this.reverse = reverse;
			currentPos = 0;
		}
		
		public boolean hasMoreElements()
		{
			return (currentPos < getSize());
		}

		public Object nextElement()
		{
			if(currentPos >= getSize())
				throw new NoSuchElementException("No more elements");
			Object element = (reverse ? getElement(getSize() - currentPos -1) : getElement(currentPos));
			currentPos++;
			return element;
		}

	}
	
	public boolean contains(Object element)
	{
		Enumeration enumerator = getElements();
		while(enumerator.hasMoreElements())
		{
			if(enumerator.nextElement()==element)
				return true;
		}
		return false;
	}

}
