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

/**
 * @author mstevens
 *
 */
public class NTColor
{

	int alpha = 255;
	int red = 0;
	int green = 0;
	int blue = 0;
	
	/**
	 * @param alpha
	 * @param red
	 * @param green
	 * @param blue
	 */
	public NTColor(int alpha, int red, int green, int blue)
	{
		this.alpha = alpha;
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	
	/**
	 * @param red
	 * @param green
	 * @param blue
	 */
	public NTColor(int red, int green, int blue)
	{
		this(255, red, green, blue);
	}

	/**
	 * @return the alpha
	 */
	public int getAlpha()
	{
		return alpha;
	}

	/**
	 * @return the red
	 */
	public int getRed()
	{
		return red;
	}

	/**
	 * @return the green
	 */
	public int getGreen()
	{
		return green;
	}

	/**
	 * @return the blue
	 */
	public int getBlue()
	{
		return blue;
	}
	
	public int getRGBValue()
	{
		return 0xff000000 | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
	}
	
	public int getARGBValue()
	{
		return 0xff000000 | ((alpha & 0xff) << 24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
	}
	
}
