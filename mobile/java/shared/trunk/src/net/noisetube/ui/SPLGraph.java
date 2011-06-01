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

package net.noisetube.ui;

import java.util.Enumeration;

import net.noisetube.model.Measurement;
import net.noisetube.model.SoundLevelScale;
import net.noisetube.model.Track;

/**
 * Sound Pressure Level (SPL) graph
 * 
 * @author mstevens
 *
 */
public class SPLGraph
{

	public static final int ALIGN_RIGHT = 0;
	public static final int ALIGN_LEFT = 1;
	//public static final int ALIGN_CENTER = 2;
	
	private int labelAlignment = ALIGN_RIGHT;
	private int width = 0;
	private int height = 0;

	private ISPLGraphUI gui;
	private double minimumDB = 25D;
	private double maximumDB = 105D;
	private Track track = null;
	
	protected int yLabelsMargin; //in pixels
	private float scaleXpx, scaleYpx; //pixels per value unit
	
	public SPLGraph(ISPLGraphUI gui, double minimumDB, double maximumDB)
	{
		this.gui = gui;
		this.minimumDB = minimumDB;
		this.maximumDB = maximumDB;
	}
	
	/**
	 * @return the minimumDB
	 */
	public double getMinimumDB()
	{
		return minimumDB;
	}

	/**
	 * @param minimumDB the minimumDB to set
	 */
	public void setMinimumDB(double minimumDB)
	{
		this.minimumDB = minimumDB;
	}

	/**
	 * @return the maximumDB
	 */
	public double getMaximumDB()
	{
		return maximumDB;
	}

	/**
	 * @param maximumDB the maximumDB to set
	 */
	public void setMaximumDB(double maximumDB)
	{
		this.maximumDB = maximumDB;
	}

	public void update(Track track)
	{
		this.track = track;
	}
	
	/**
	 * This calculates the actual amount of pixels that should be used to represent one decibel.
	 * It also calculates the width between the displayed measurements.
	 */
	private void updateDimensions()
	{
		width = gui.getWidth();
		height = gui.getHeight();
		yLabelsMargin = (int) gui.getLabelWidth(Integer.toString(((int) maximumDB / 10) * 10)) + 2;
		if(track != null)
			scaleXpx = ((float) width - yLabelsMargin) / track.getBufferCapacity(); 
		else
			scaleXpx = 1;
		scaleYpx = (float) ((height - 1) / (maximumDB - minimumDB));
		if(scaleYpx == 0)
			scaleYpx = 1;
	}
	
	public void draw(Object graphics_canvas)
	{
		updateDimensions(); //!!!
		
		//Colored line each 10dB, starting from the first multiple of 10 above the minimumDB till and including the biggest multiple of 10  below maximumDB
		for(int l = (((int) minimumDB / 10) + 1); l <= ((int) maximumDB / 10); l++)
		{
			int db = l * 10;
			NTColor color = SoundLevelScale.getColor(db); //used to be gray (0x555555)
			//Line:
			float y = (float) (height - 1 - ((db - minimumDB) * scaleYpx));
			gui.drawLine(graphics_canvas, color, yLabelsMargin, y, width - 1, y, false);
			//Label:
			String lbl = Integer.toString((int) db);
			gui.drawLabel(	graphics_canvas,
							lbl,
							color,
							(labelAlignment == ALIGN_LEFT ? 0 : yLabelsMargin - 2 - gui.getLabelWidth(lbl)),
							(float) (height - ((db - minimumDB) * scaleYpx) + (gui.getLabelHeight(lbl) / 2)),
							true);
		}
		
		//Measurements:
		if(track == null || track.getBufferSize() < 2)
			return;
		Enumeration e = track.getMeasurements();
		Measurement next = (Measurement) e.nextElement();
		Measurement current;
		int i = 0;
		while(e.hasMoreElements())
		{
			i++;
			current = next;
			next = (Measurement) e.nextElement();
			double currentOffset = (current.isLeqDBASet() ? current.getLeqDBA() : current.getLeqDB()) - minimumDB;
			double nextOffset = (next.isLeqDBASet() ? next.getLeqDBA() : next.getLeqDB()) - minimumDB;
			gui.drawLine(	graphics_canvas,
							new NTColor(0xFF, 0xFF, 0xFF), //white
							yLabelsMargin + ((i - 1) * scaleXpx),
							(float) (height - 1 - (currentOffset * scaleYpx)),
							yLabelsMargin + (i * scaleXpx),
							height - 1 - (int) (nextOffset * scaleYpx),
							true);
			//Indicate tags with horizontal lines:
			if(current.hasUserTags())
			{
				float x = yLabelsMargin + ((i - 1) * scaleXpx);
				gui.drawLine(graphics_canvas, new NTColor(0, 0, 0xFF) /*blue*/, x, 0, x, height - 1, false);
			}
			else if(current.hasAutomaticTags())
			{
				float x = yLabelsMargin + ((i - 1) * scaleXpx);
				gui.drawLine(graphics_canvas, new NTColor(0xFF, 0, 0) /*red*/, x, 0, x, height - 1, false);
			}
				
		}
	}
	
}
