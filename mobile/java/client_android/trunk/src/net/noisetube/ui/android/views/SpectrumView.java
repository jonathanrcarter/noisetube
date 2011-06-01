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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Used to display a spectrum of the sound pressure levels of each recording.
 * 
 * @author sbarthol
 *
 */
public class SpectrumView extends View {

	private Paint bandPaint;
	private int boxWidth = 0;
	private int boxHeight = 0;
	private float[] percentages = {0,0,0,0,0};

	private float bandWith;

	private int bands = 5;

	public SpectrumView(Context context, AttributeSet attrs) {
		super(context, attrs);
		bandPaint = new Paint();
		bandPaint.setAntiAlias(true);
		bandPaint.setColor(Color.CYAN);
	}

	/**
	 * onDraw is called whenever this view is drawn.
	 * @param canvas The canvas bound to this view.
	 */
	@Override
	protected void onDraw(Canvas canvas)
	{
		boxWidth = getWidth();
		boxHeight = getHeight();
		calculateScale();
		for(int i = 0; i < bands; i++)
		{
			float currentPercentage = percentages[i];
			drawBand(canvas, i, currentPercentage);
		}
	}

	/**
	 * This calculates the actual amount of pixels that should be used for 1 db.
	 * It also calculates the width between the displayed measurements.
	 */
	public void calculateScale()
	{
		bandWith = boxWidth / bands;
	}

	private void drawBand(Canvas canvas, float xOffset, float yPercentage)
	{
		canvas.drawRect(xOffset*bandWith+2, (boxHeight - yPercentage*(boxHeight/100)), (xOffset+1)*bandWith-2, boxHeight, bandPaint);
	}
	
	public void setPercentages(float band1P, float band2P, float band3P, float band4P, float band5P)
	{
		percentages = new float [] {band1P, band2P, band3P, band4P, band5P};
	}
}
