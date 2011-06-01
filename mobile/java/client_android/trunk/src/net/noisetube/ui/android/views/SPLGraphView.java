/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Android version)
 *  
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011
 *  Android port by Vrije Universiteit Brussel (BrusSense team), 2010-2011
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

package net.noisetube.ui.android.views;

import net.noisetube.ui.ISPLGraphUI;
import net.noisetube.ui.NTColor;
import net.noisetube.ui.SPLGraph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Android Sound Level Pressure (SPL) graph
 * 
 * Used to display a graph showing the recorded dbA output, an actual integer
 * of this dbA and the amount of measurements made.
 * 
 * @author mstevens, sbarthol
 *
 */
public class SPLGraphView extends View implements ISPLGraphUI
{
	
	private static int LABEL_TEXT_SIZE = 12;
	private SPLGraph splG;
		
	public SPLGraphView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.splG = new SPLGraph(this, 25, 105);
	}
	
	/**
	 * onDraw is called whenever this view is drawn.
	 * @param canvas The canvas bound to this view.
	 */
	@Override
	protected void onDraw(Canvas canvas)
	{
		splG.draw(canvas);
	}

	public SPLGraph getSoundLevelGraph()
	{
		return splG;
	}

	public float getLabelWidth(String label)
	{
		Paint labelPaint = new Paint();
		labelPaint.setTextSize(LABEL_TEXT_SIZE);
		return labelPaint.measureText(label);
	}

	public float getLabelHeight(String label)
	{
		Paint labelPaint = new Paint();
		labelPaint.setAntiAlias(true);
		labelPaint.setTextSize(LABEL_TEXT_SIZE);
		Rect labelBounds = new Rect();
		labelPaint.getTextBounds(label, 0, label.length(), labelBounds);
		return labelBounds.height();
	}

	public void drawLine(Object graphics_canvas, NTColor color, float x1, float y1, float x2, float y2, boolean antiAlias)
	{
		Canvas c = (Canvas) graphics_canvas;
		Paint linePaint = new Paint();
		linePaint.setAntiAlias(antiAlias);
		linePaint.setColor(color.getARGBValue());
		c.drawLine(x1, y1, x2, y2, linePaint);
	}

	public void drawLabel(Object graphics_canvas, String label, NTColor labelColor, float x, float y, boolean antiAlias)
	{
		Canvas c = (Canvas) graphics_canvas;
		Paint labelPaint = new Paint();
		labelPaint.setAntiAlias(antiAlias);
		labelPaint.setColor(labelColor.getARGBValue());
		labelPaint.setTextSize(LABEL_TEXT_SIZE);
		c.drawText(label ,x, y, labelPaint);
	}
	
}
