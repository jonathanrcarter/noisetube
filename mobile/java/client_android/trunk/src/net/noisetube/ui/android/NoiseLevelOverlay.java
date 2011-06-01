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

package net.noisetube.ui.android;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.noisetube.location.android.AndroidNTCoordinates;
import net.noisetube.model.Measurement;
import net.noisetube.model.SoundLevelScale;
import net.noisetube.model.Track;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * This ItemizedOverlay manages a whole set of map overlays.
 * These overlays will be used to display the path the user has taken, together
 * with the SPL measurements on this path (expressed using a color code).
 *
 * @author sbarthol, mstevens
 *
 */
public class NoiseLevelOverlay extends Overlay
{

	private Track track;
	private ArrayList<MeasurementPoint> pointsNoLongerInTrack;
	private Paint pathPaint;
	private Paint circlePaint;
	private Paint blackPaint;

	public NoiseLevelOverlay()
	{
		super();
		pathPaint = new Paint();
		pathPaint.setAntiAlias(true);
		pathPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		pathPaint.setStrokeJoin(Paint.Join.ROUND);
		pathPaint.setStrokeCap(Paint.Cap.ROUND);
		pathPaint.setStrokeWidth(4);
		blackPaint = new Paint();
		blackPaint.setAntiAlias(true);
		blackPaint.setColor(Color.BLACK);
		circlePaint = new Paint();
		circlePaint.setAntiAlias(true);
	}
	
	/**
	 * @param track
	 * @param savedMeasurement a measurement which has been removed from the track and saved to make room for the new one
	 */
	public synchronized void update(Track track, Measurement savedMeasurement)
	{
		if(this.track != track)
		{	//new track: clear overlay contents
			pointsNoLongerInTrack = new ArrayList<MeasurementPoint>();
			this.track = track;
		}
		if(savedMeasurement != null)
		{
			try
			{
				pointsNoLongerInTrack.add(new MeasurementPoint(savedMeasurement));
			}
			catch(IllegalArgumentException ignore) { } //ignore (happens if measurement has no location/coordinates
		}
	}

	@Override
	public synchronized void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow)
	{
		MeasurementPointIterator mpi = new MeasurementPointIterator(pointsNoLongerInTrack, track);
		if(!mpi.hasNext())
			return;
		
		//Draw path covered so far
		MeasurementPoint lastMP = mpi.next();
		Point lastPointOnMap =  new Point();
		mapView.getProjection().toPixels(lastMP.geoPoint, lastPointOnMap);
		while(mpi.hasNext())
		{
			MeasurementPoint newMP = mpi.next();
			Point newPointOnMap = new Point();
			mapView.getProjection().toPixels(newMP.geoPoint, newPointOnMap);
			Path p = new Path();
			p.moveTo(lastPointOnMap.x, lastPointOnMap.y);
			p.lineTo(newPointOnMap.x, newPointOnMap.y);
			pathPaint.setColor(SoundLevelScale.getColor((lastMP.noiselevel + newMP.noiselevel) / 2).getARGBValue()); //color for average of the two measurements
			canvas.drawPath(p, pathPaint);
			lastMP = newMP;
			lastPointOnMap = newPointOnMap;
		}
		
		//Draw most recent measurement:
		canvas.drawCircle(lastPointOnMap.x, lastPointOnMap.y, 15, blackPaint);
		circlePaint.setColor(SoundLevelScale.getColor(lastMP.noiselevel).getARGBValue());
		canvas.drawCircle(lastPointOnMap.x, lastPointOnMap.y, 12, circlePaint);
	}
	
	private class MeasurementPointIterator implements Iterator<MeasurementPoint>
	{

		private Iterator<MeasurementPoint> mpIter = null;
		private Enumeration<Measurement> mEnum = null;
		private MeasurementPoint nextFromTrack = null;
		
		public MeasurementPointIterator(ArrayList<MeasurementPoint> firstPartAsPoints, Track secondPartAsTrack)
		{
			if(firstPartAsPoints != null)
				mpIter = firstPartAsPoints.iterator();
			if(secondPartAsTrack != null)
				mEnum = secondPartAsTrack.getMeasurements();
		}
		
		public boolean hasNext()
		{
			if(mpIter != null && mpIter.hasNext())
				return true;
			return trackHasNext();
		}
		
		private boolean trackHasNext()
		{
			if(mEnum == null)
				return false;
			else
			{	
				while(nextFromTrack == null && mEnum.hasMoreElements())
				{
					try
					{
						nextFromTrack = new MeasurementPoint(mEnum.nextElement());
					}
					catch(IllegalArgumentException ignore) { } //ignore (happens if measurement has no location/coordinates
				}
				return (nextFromTrack != null);
			}
		}

		public MeasurementPoint next()
		{
			if(mpIter != null && mpIter.hasNext())
				return mpIter.next();
			if(trackHasNext())
			{
				MeasurementPoint temp = nextFromTrack;
				nextFromTrack = null;
				return temp;
			}
			else
				throw new NoSuchElementException("Iterator has no more elements");
		}

		public void remove()
		{
		}
		
	}
	
	/**
	 * @author mstevens
	 *
	 */
	private class MeasurementPoint
	{
		
		private GeoPoint geoPoint;
		private double noiselevel;
		
		/**
		 * @param point
		 * @param noiselevel
		 */
		public MeasurementPoint(Measurement measurement)
		{
			if(measurement.getLocation() == null || !measurement.getLocation().hasCoordinates())
				throw new IllegalArgumentException("Measurement has no location or coordinates");
			this.geoPoint = ((AndroidNTCoordinates) measurement.getLocation().getCoordinates()).toGeoPoint();
			this.noiselevel = measurement.isLeqDBASet() ? measurement.getLeqDBA() : measurement.getLeqDB();
		}
		
	}
	
}
