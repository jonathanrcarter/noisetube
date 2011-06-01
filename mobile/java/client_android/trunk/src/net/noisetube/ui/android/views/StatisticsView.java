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

import net.noisetube.ui.android.UIHelper;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

/**
 * Used to display the amount of measurements in the Track & additional data
 * 
 * @author sbarthol, mstevens
 *
 */
public class StatisticsView extends View
{

	private Paint textPaint;
	private static int hMargin = 1;
	private static int vMargin = 2;
	private Rect canvasBounds;
	private String mask = "100/100/100";
	private String text;
	
	public StatisticsView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setColor(Color.CYAN);
		textPaint.setTextAlign(Paint.Align.LEFT);
		textPaint.setStyle(Style.STROKE);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		if(text != null && !text.equals(""))
		{
			if(canvasBounds == null || !canvas.getClipBounds().equals(canvasBounds))
			{ 
				canvasBounds = canvas.getClipBounds();
				int size = UIHelper.getMaxTextSize(mask, new Rect(hMargin, vMargin, canvasBounds.right - canvasBounds.left - hMargin, (canvasBounds.bottom - canvasBounds.top) - vMargin), textPaint);
				textPaint.setTextSize(size);
			}
			Rect textBounds = new Rect();
			textPaint.getTextBounds(text, 0, text.length(), textBounds);
			canvas.drawText(text, hMargin, (canvasBounds.height() + textBounds.height()) / 2.0f - vMargin, textPaint);
		}
	}
	
	public void setText(String text)
	{
		this.text = text;
	}
	
}
