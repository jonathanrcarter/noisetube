/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Java ME version)
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

package net.noisetube.ui.javame;

import net.noisetube.audio.Calibration;
import net.noisetube.util.Logger;

import com.sun.lwuit.Container;
import com.sun.lwuit.Font;
import com.sun.lwuit.Graphics;

/**
 * Displaying the decibels in a graph
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class CalibrationGraph extends Container
{

	Logger log = Logger.getInstance();

	public double minx, miny, maxx, maxy;

	public double[][] data = new double[0][0];
	int marge_bottom = 30;
	int marge_x = 30;

	public CalibrationGraph()
	{
		super();
	}

	public void reset_axis()
	{
		minx = miny = 200;
		maxx = maxy = -10;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.lwuit.Container#paint(com.sun.lwuit.Graphics)
	 */
	public void paint(Graphics g)
	{

		if(data.length == 0)
			return;

		g.setColor(0xFFFFF);
		// Y AXIS
		g.drawLine(marge_x, marge_bottom, marge_x, getHeight() - marge_bottom);

		g.setFont(Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL));
		int size = 6;
		int FONT_HEIGHT = 8;
		int step = (int) ((maxy - miny) / size);

		int max_y = getHeight() - marge_bottom;

		for(int i = 0; i < (size + 1); i++)
		{
			int x = marge_x - 25;
			int y = max_y - (int) ((i * step) / (maxy - miny) * (max_y));
			g.drawString("" + (int) (i * step + miny), x, y - FONT_HEIGHT);
			g.drawLine(x + 20, y, x + 25, y);
		}

		// X AXIS
		// g.setColor(0xFF0000);
		g.drawLine(marge_x, max_y, marge_x + getWidth(), max_y);
		step = (int) (maxx - minx) / size;
		for(int i = 0; i < (size + 1); i++)
		{
			int x = marge_x
					+ (int) ((i * step) / (maxx - minx) * (getWidth() - marge_x));
			g.drawString("" + (int) (i * step + minx), x - 5, max_y + 5);
			g.drawLine(x, max_y + 5, x, max_y);
		}

		g.setFont(Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL));
		g.setColor(0xAA0000);

		// X
		g.drawLine(marge_x + (int) (getX(0)), max_y, marge_x + (int) (getX(0)),
				max_y + -(int) (getY(0)));
		// Y
		g.drawLine(marge_x + 0, max_y - (int) (getY(0)), marge_x
				+ (int) (getX(0)), max_y - (int) (getY(0)));

		// System.out.println("\n\n");
		for(int i = 1; i < data.length; i++)
		{

			g.setColor(0xFFFFFF);

			if(getX(i - 1) < 0)
				continue;

			// System.out.println(data[i][0]+","+data[i][1]);
			g.drawLine(marge_x + (int) (getX(i - 1)), max_y
					- (int) (getY(i - 1)), marge_x + (int) (getX(i)), max_y
					- (int) (getY(i)));

			g.setColor(0xAA0000);

			// X
			g.drawLine(marge_x + (int) (getX(i)), max_y, marge_x
					+ (int) (getX(i)), max_y - (int) (getY(i)));
			// Y
			g.drawLine(marge_x + 0, max_y - (int) (getY(i)), marge_x
					+ (int) (getX(i)), max_y - (int) (getY(i)));
		}
	}

	public void setMinx(int minx)
	{
		this.minx = minx;
	}

	public void setMiny(int miny)
	{
		this.miny = miny;
	}

	public void setMaxx(int maxx)
	{
		this.maxx = maxx;
	}

	public void setMaxy(int maxy)
	{
		this.maxy = maxy;
	}

	private double getX(int i)
	{
		return to_x(data[i][Calibration.INPUT_IDX]);
	}

	private double getY(int i)
	{
		return to_y(data[i][Calibration.OUTPUT_IDX]);
	}

	private double to_x(double db)
	{
		return (db - minx) / (maxx - minx) * (getWidth() - marge_x);
	}

	private double to_y(double db)
	{
		return (db - miny) / (maxy - miny) * (getHeight() - marge_bottom);
	}

	/**
	 * method to test
	 */
	/*
	 * private void generate_item() { Random generator = new Random();
	 * generator.setSeed(System.currentTimeMillis()); int i =
	 * generator.nextInt(100); //push(i); }
	 */

}