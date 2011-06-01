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

import net.noisetube.model.Measurement;
import net.noisetube.model.SoundLevelScale;
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
 * Used to display the currently recorded dB/dB(A)
 * 
 * @author sbarthol, mstevens
 *
 */
public class SPLView extends View
{

	private static int margin = 4;
	private Rect canvasBounds;
	private Paint textPaint;	
	private String text = "---      ";

	public SPLView(Context context, AttributeSet attrs) {
		super(context, attrs);

		textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setColor(Color.GRAY);
		textPaint.setTextAlign(Paint.Align.CENTER);
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
				int size = UIHelper.getMaxTextSize("100 dB(A)", new Rect(margin, margin, canvasBounds.right - canvasBounds.left - margin, canvasBounds.bottom - canvasBounds.top - margin), textPaint);
				textPaint.setTextSize(size);
			}
			Rect textBounds = new Rect();
			textPaint.getTextBounds(text, 0, text.length(), textBounds);
			canvas.drawText(text, canvasBounds.width() / 2.0f, (canvasBounds.height() + textBounds.height()) / 2.0f - 2 * margin, textPaint);
		}
	}

	/**
	 * This function is called whenever a new measurement should be displayed
	 */
	public void update(Measurement newMeasurement)
	{
		double db;
		String unit = " dB";
		if(newMeasurement.isLeqDBASet())
		{
			db = newMeasurement.getLeqDBA();
			unit += "(A)";
		}
		else
			db = newMeasurement.getLeqDB();
		text = Integer.toString((int) Math.round(db)) + unit;
		textPaint.setColor(SoundLevelScale.getColor(db).getARGBValue());
	}
	
	public void clear()
	{
		text = "---      ";
		textPaint.setColor(Color.GREEN);
	}
	
}
