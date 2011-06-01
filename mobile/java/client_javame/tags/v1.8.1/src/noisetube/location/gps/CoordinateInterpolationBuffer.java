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

package noisetube.location.gps;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.location.Coordinates;

import noisetube.io.Saver;
import noisetube.io.SavingBuffer;
import noisetube.model.Measure;
import noisetube.model.NTLocation;
import noisetube.util.Logger;

//TODO make asynchronous?

/**
 * @author mstevens
 * 
 */
public class CoordinateInterpolationBuffer implements SavingBuffer
{

	private Logger log = Logger.getInstance();
	private Saver saver;
	private Vector queue;
	private static int maxBufferSize = 40;
	private Measure lastMeasureWithUniquePosition;
	private boolean resetUponNullCoordinates;

	public CoordinateInterpolationBuffer()
	{
		resetUponNullCoordinates = false;
		queue = new Vector();
	}

	public CoordinateInterpolationBuffer(Saver saver)
	{
		this(saver, false); //reset upon null coordinates by default
	}

	public CoordinateInterpolationBuffer(Saver saver, boolean resetUponNullCoordinates)
	{
		this();
		this.saver = saver;
		this.resetUponNullCoordinates = resetUponNullCoordinates;
	}

	/**
	 * @param saver the saver to set
	 */
	public void setSaver(Saver saver)
	{
		this.saver = saver;
	}

	public void enqueueMeasure(Measure measure)
	{
		NTLocation measureLocation = measure.getLocation();
		Coordinates measureCoordinates = ((measureLocation != null && measureLocation instanceof NTLocationGPS) ? ((NTLocationGPS) measureLocation).getCoordinates() : null);
		if(lastMeasureWithUniquePosition != null)
		{ // Previous coordinates known
			Coordinates lastUniqueCoordinates = ((NTLocationGPS) lastMeasureWithUniquePosition.getLocation()).getCoordinates();
			if(measureCoordinates != null)
			{ // New measure has coordinates
				if(CoordinateUtils.sameCoordines(lastUniqueCoordinates, measureCoordinates))
				{ // Position (still) clamped
					measure.setLocation(lastMeasureWithUniquePosition.getLocation()); //limit memory use, location will be replaced anyway
					enqueue(measure); // enqueue for interpolation and delayed sending
				}
				else
				{ // Got new position
					interpolateAndSaveQueued(lastMeasureWithUniquePosition, measure);
					save(measure);
					lastMeasureWithUniquePosition = measure;
				}
			}
			else
			{ // New measure has no coordinates
				if(resetUponNullCoordinates)
				{
					flush(); // save currently queued measures without interpolation
					save(measure); // save this measure
				}
				else
					enqueue(measure); // enqueue for interpolation and delayed sending
			}
		}
		else
		{ // No previous coordinates known
			if(measureCoordinates != null)
				//New measure has coordinates, store it as the last one with unique ones
				lastMeasureWithUniquePosition = measure;
			else
			{
				//New measure has no coordinates, but cannot interpolate because there is no previous position
			}
			save(measure);
		}
	}
	
	private void enqueue(Measure measure)
	{
		if(queue.size() >= maxBufferSize)
		{	//buffer is full, flush it and don't interpolate
			flush(); // save currently queued measures without interpolation
			save(measure); // save this measure
		}
		else
			queue.addElement(measure);
	}

	protected void interpolateAndSaveQueued(Measure from, Measure to)
	{
		if(!queue.isEmpty())
		{
			int size = queue.size();
			try
			{
				// interpolateAndSaveQueuedGeodetic(from, to); //has major unresolved bug, maybe fix this later
				interpolateAndSaveQueuedCartesian(from, to); // sufficiently accurate over short distances
			}
			catch(Exception e)
			{
				log.error(e, "error in interpolateAndSaveQueued(), flushing");
				flush();
				return;
			}
			log.debug("Saved " + size + " measurements with interpolated coordinates");
		}
	}

	/**
	 * Cartesian interpolation of coordinates, not geographically correct but
	 * error should be negligible over short distances
	 * 
	 * @param from
	 * @param to
	 */
	protected void interpolateAndSaveQueuedCartesian(Measure from, Measure to)
	{
		if(!queue.isEmpty())
		{
			Coordinates fromPosition = ((NTLocationGPS) from.getLocation()).getCoordinates();
			Coordinates toPosition = ((NTLocationGPS) to.getLocation()).getCoordinates();
			double latitudeDifference = fromPosition.getLatitude() - toPosition.getLatitude();
			double longitudeDifference = fromPosition.getLongitude() - toPosition.getLongitude();
			float altitudeDifference = fromPosition.getAltitude() - toPosition.getAltitude();
			long fromTime = from.getDate().getTime();
			long totalTime = to.getDate().getTime() - fromTime;
			double latitudePerT = (latitudeDifference / (double) totalTime);
			double longitudePerT = (longitudeDifference / (double) totalTime);
			float altitudePerT = (altitudeDifference / (float) totalTime);
			/* log.debug(	"Cartesian interpolation of " + queue.size() + " coordinates over a timespan of " +
			 * 				(totalTime / 1000d) + " seconds, a latitude difference of " + Double.toString(latitudeDifference) +
			 * 				" degrees, a longitude difference of " + longitudeDifference + " degrees and an altitude difference of " +
			 * 				altitudeDifference + " meters.");
			 */
			while(!queue.isEmpty())
			{
				Measure measureToInterpolate = (Measure) queue.elementAt(0);
				long deltaT = measureToInterpolate.getDate().getTime() - fromTime;
				double deltaLat = latitudePerT * (double) deltaT;
				double deltaLon = longitudePerT * (double) deltaT;
				float deltaAlt = altitudePerT * (float) deltaT;
				/* log.debug(	"Interpolation for: deltaT = " + (deltaT / 1000d) +
				 * 				"s; deltaLat = " + deltaLat + "°; deltaLon = " + deltaLon +
				 * 				"°; deltaAlt = " + deltaAlt + "m");
				 */
				try
				{
					measureToInterpolate.setLocation(new NTLocationGPS(new Coordinates(	fromPosition.getLatitude() - deltaLat,
																						fromPosition.getLongitude()- deltaLon,
																						fromPosition.getAltitude() - deltaAlt)));
				}
				catch(Exception e)
				{
					log.error(e, "cartesian coordinate translation failed");
				}
				finally
				{
					save(measureToInterpolate);
					queue.removeElementAt(0);
				}
			}
		}
	}

	/**
	 * Geographically correct interpolation (using geodetic parameters for WSG84)
	 * TODO Major BUG (results completely incorrect), cartesian interpolation is sufficient for now, fix this later
	 * 
	 * @param from
	 * @param to
	 */
	protected void interpolateAndSaveQueuedGeodetic(Measure from, Measure to)
	{
		if(!queue.isEmpty())
		{
			Coordinates fromPosition = ((NTLocationGPS) from.getLocation()).getCoordinates();
			Coordinates toPosition = ((NTLocationGPS) to.getLocation()).getCoordinates();
			float totalDistance = fromPosition.distance(toPosition);
			float altitudeDifference = fromPosition.getAltitude() - toPosition.getAltitude();
			double course = (double) fromPosition.azimuthTo(toPosition);
			long fromTime = from.getDate().getTime();
			long totalTime = to.getDate().getTime() - fromTime;
			double distancePerT = ((double) totalDistance / (double) totalTime);
			float altitudePerT = (altitudeDifference / (float) totalTime);
			/* log.debug("Geodetic interpolation of " + queue.size() +
			 * " coordinates over a timespan of " + (totalTime / 1000d) +
			 * " seconds and a distance of " + totalDistance +
			 * " meters with an altitude difference of " + altitudeDifference +
			 * " meters, along a course of " + course + " degrees.");
			 */
			while(!queue.isEmpty())
			{
				Measure measureToInterpolate = (Measure) queue.elementAt(0);
				long deltaT = measureToInterpolate.getDate().getTime() - fromTime;
				double deltaD = distancePerT * (double) deltaT;
				float deltaA = altitudePerT * (float) deltaT;
				/* log.debug("Interpolation for: deltaT = " + (deltaT / 1000d) +
				 * "s; deltaD = " + deltaD + "m; deltaA = " + deltaA + "m");
				 */
				try
				{
					Coordinates translatedCoordinates = CoordinateUtils.translate(fromPosition, course, deltaD);
					translatedCoordinates.setAltitude(fromPosition.getAltitude() - deltaA);
					measureToInterpolate.setLocation(new NTLocationGPS(translatedCoordinates));
				}
				catch(Exception e)
				{
					log.error(e, "geodetic coordinate translation failed");
				}
				finally
				{
					save(measureToInterpolate);
					queue.removeElementAt(0);
				}
			}
		}
	}

	public void flush()
	{
		flush(false);
	}

	public void flush(boolean stopSaverAfterwards)
	{
		if(queue.size() > 0)
		{
			Enumeration e = queue.elements();
			while(e.hasMoreElements())
				save((Measure) e.nextElement());
			log.debug("Saved " + queue.size() + " measurements without interpolating coordinates");
		}
		queue.removeAllElements();
		lastMeasureWithUniquePosition = null; // reset last known
		if(stopSaverAfterwards)
			saver.stop();
	}

	protected void save(Measure measure)
	{
		if(saver.isRunning())
			saver.save(measure);
	}

}
