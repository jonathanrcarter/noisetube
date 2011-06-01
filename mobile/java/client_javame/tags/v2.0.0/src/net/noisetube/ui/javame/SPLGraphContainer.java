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

import net.noisetube.ui.ISPLGraphUI;
import net.noisetube.ui.NTColor;
import net.noisetube.ui.SPLGraph;

import com.sun.lwuit.Container;
import com.sun.lwuit.Graphics;

/**
 * Java ME (LWUIT) Sound Pressure Level (SPL) graph 
 * 
 * @author mstevens, maisonneuve
 *
 */
public class SPLGraphContainer extends Container implements ISPLGraphUI
{
	
	private SPLGraph splG;
	
	public SPLGraphContainer()
	{
		super();
		this.splG = new SPLGraph(this, 25, 105);
		getStyle().setMargin(0, 0, 0, 0);
		getStyle().setPadding(0, 0, 0, 0);
	}
	
	/**
	 * 
	 * @see com.sun.lwuit.Container#paint(com.sun.lwuit.Graphics)
	 */
	public void paint(Graphics g)
	{
		splG.draw(g);
	}
	
	public SPLGraph getSoundLevelGraph()
	{
		return splG;
	}

	public float getLabelWidth(String label)
	{
		return Fonts.SMALL_FONT.stringWidth(label);
	}

	public float getLabelHeight(String label)
	{
		return Fonts.SMALL_FONT.getHeight() * -1; //HACK
	}

	public void drawLine(Object graphics_canvas, NTColor color, float x1, float y1, float x2, float y2, boolean antiAlias)
	{
		Graphics g = (Graphics) graphics_canvas;
		g.setAntiAliased(antiAlias);
		int holdColor = g.getColor(); //hold current color to reset it after the line and its label have been drawn
		g.setColor(color.getRGBValue());
		g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
		g.setColor(holdColor);
	}

	public void drawLabel(Object graphics_canvas, String label, NTColor labelColor, float x, float y, boolean antiAlias)
	{
		Graphics g = (Graphics) graphics_canvas;
		g.setAntiAliasedText(antiAlias);
		int holdColor = g.getColor(); //hold current color to reset it after the line and its label have been drawn
		g.setColor(labelColor.getRGBValue());
		g.setFont(Fonts.SMALL_FONT);
		g.drawString(label, (int) x, ((int) y) - 1 /*HACK*/);
		g.setColor(holdColor);
	}

}
