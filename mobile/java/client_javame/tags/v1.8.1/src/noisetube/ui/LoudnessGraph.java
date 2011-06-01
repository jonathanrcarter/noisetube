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

package noisetube.ui;

import noisetube.model.Measure;
import noisetube.util.CyclicQueue;
import noisetube.util.Logger;

import com.sun.lwuit.Container;
import com.sun.lwuit.Font;
import com.sun.lwuit.Graphics;

/**
 * Displaying the decibels in a graph
 * 
 * @author maisonneuve
 * 
 */
public class LoudnessGraph extends Container
{

	Logger log = Logger.getInstance();

	int marge_x = 20;

	class Point
	{
		double dbOffset;
		boolean annotate = false;
		boolean automaticTag = false;

		Point(Measure m)
		{
			double leq = Math.min(Math.max(m.getLeq(), minimum_decibel), maximum_decibel);
			dbOffset = (leq - minimum_decibel);
			annotate = (m.getTags() != null);
			automaticTag = (annotate && (m.getTags().equals("exposure:high") || m.getTags().equals("variation:high"))); // bit of a hack...
		}
	}

	private double minimum_decibel = 30D;
	private double maximum_decibel = 100D;
	double scale_x_pixel, scale_y_pixel;

	private CyclicQueue dbs;

	public LoudnessGraph(int capacity)
	{
		super();
		dbs = new CyclicQueue(capacity);

		getStyle().setMargin(5, 5, 0, 0);
		getStyle().setPadding(0, 0, 0, 0);
	}

	public void scale()
	{

		scale_x_pixel = ((double) getWidth() - 30) / dbs.getCapacity();
		scale_y_pixel = (getHeight()) / (maximum_decibel - minimum_decibel);
		if(scale_y_pixel == 0)
			scale_y_pixel = 1;
	}

	/**
	 * Set the minimum displayable decibel
	 * 
	 * @param minimum_decibel
	 */
	public void setMinimum_decibel(int minimum_decibel)
	{
		this.minimum_decibel = minimum_decibel;
	}

	/**
	 * Set the maximum displayable decibel
	 * 
	 * @param minimum_decibel
	 */
	public void setMaximum_decibel(int maximum_decibel)
	{
		this.maximum_decibel = maximum_decibel;
	}

	/**
	 * add a new decibel value
	 */
	public void push(Measure measure)
	{
		dbs.push(new Point(measure));
		// TODO to move

	}

	private Point getdb(int i)
	{
		return (Point) dbs.get(i);
	}

	private void draw_axis(Graphics g, double x)
	{
		g.setFont(Font.createSystemFont(Font.FACE_PROPORTIONAL,
				Font.STYLE_PLAIN, Font.SIZE_SMALL));
		g.drawLine(marge_x, getHeight() - (int) (x * scale_y_pixel), getWidth()
				+ marge_x, getHeight() - (int) (x * scale_y_pixel));
		g.setColor(0xFFFFFF);
		g.drawString("" + (int) (x + minimum_decibel), 0, getHeight()
				- (int) (x * scale_y_pixel) - 10);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.lwuit.Container#paint(com.sun.lwuit.Graphics)
	 */
	public void paint(Graphics g)
	{

		scale();

		g.setColor(0x55FF55);
		draw_axis(g, 10);
		g.setColor(0x555555);
		draw_axis(g, 30);
		g.setColor(0xFF5555);
		draw_axis(g, 50);

		g.setColor(0xFFFFFF);
		int size = dbs.getSize();
		for(int i = 1; i < size; i++)
		{

			if(dbs.get(i) == null)
				return;

			g.drawLine(marge_x + (int) ((i - 1) * scale_x_pixel), getHeight()
					- (int) (getdb(i - 1).dbOffset * scale_y_pixel), marge_x
					+ (int) (i * scale_x_pixel), getHeight()
					- (int) (getdb(i).dbOffset * scale_y_pixel));

			if(getdb(i - 1).annotate)
			{
				if(getdb(i - 1).automaticTag)
					g.setColor(0xFF0000); // red
				else
					g.setColor(0x0000FF); // blue
				g.drawLine(marge_x + (int) ((i - 1) * scale_x_pixel), 0,
						marge_x + (int) ((i - 1) * scale_x_pixel), getHeight());
				g.setColor(0xFFFFFF);
			}
		}
	}

	/**
	 * method to test
	 */
	/*
	 * private void generate_item() { Random generator = new Random(); ;
	 * generator.setSeed(System.currentTimeMillis()); int i =
	 * generator.nextInt(100); //push(i); }
	 */

}