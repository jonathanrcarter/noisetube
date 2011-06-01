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

import noisetube.model.Measure;
import noisetube.ui.graph.Graph;

import com.sun.lwuit.Graphics;


/**
 * Displaying the memory usages (based on LoudnessGraph)
 * 
 * @author mstevens
 * 
 */
public class MemoryGraph extends Graph
{

	static Logger log = Logger.getInstance();
	
	protected int margeX = 25;
	private long maximumMemory;
	
	class MemoryGraphValue extends GraphValue
	{

		public MemoryGraphValue(long freeMemory)
		{
			super((((double) (maximumMemory - freeMemory)) / (double) maximumMemory) * 100d);
			long usedMemory = maximumMemory - freeMemory;
			int usedMemoryPrc = (int) (((double) usedMemory / (double) maximumMemory) * 100d);
			log.debug("Memory in use now: " + usedMemory + " bytes (" + usedMemoryPrc + "%)");	
		}
	}
	
	public MemoryGraph(int capacity, long minimumMemory, long maximumMemory)
	{
		super(capacity, (int) (((double) minimumMemory / (double) maximumMemory) * 100), 100);
		this.maximumMemory = maximumMemory;
	}

	/**
	 * add a new memory usage value
	 */
	public void addMeasure(Measure ignore)
	{
		add(new MemoryGraphValue(Runtime.getRuntime().freeMemory()));
	}

	public void additionalPaintTask(Graphics g)
	{
//		int lineSteps = ((maximumY - minimumY) / 5);
//		
//		if(minimumY + 5 < maximumY)
//		{
//			g.setColor(0x55FF55);
//			drawHorizonalRulerWithLabel(g, 5, );
//		}
//		if(minimumY + 15 < maximumY)
//		{
//			g.setColor(0x555555);
//			drawHorizonalLine(g, 15);
//		}
//		if(minimumY + 25 < maximumY)
//		{
//			g.setColor(0xFF5555);
//			drawHorizonalLine(g, 25);
//		}
	}

}
