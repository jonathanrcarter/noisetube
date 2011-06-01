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

package net.noisetube.location;

import java.util.Enumeration;

import net.noisetube.core.NTClient;
import net.noisetube.core.IProcessor;
import net.noisetube.model.Measurement;
import net.noisetube.model.INTCoordinates;
import net.noisetube.model.NTLocation;
import net.noisetube.model.Track;
import net.noisetube.util.Logger;


/**
 * @author mstevens, sbarthol
 * 
 */
public class CoordinateInterpolator implements IProcessor
{

	/**
	 * We implement interpolation in the following manner:
	 * Interpolation can be performed on a queue of the following form:
	 * E.g. when the queue is: [4, 7, 7, 7] (newest on the left), after interpolation, it can be reformed to: [4, 5, 6, 7].
	 * So this gives us the following process:
	 * 
	 * Receiving the buffer we start by looking at the three most recent measurements. This gives us three options:<br>
	 * - the location of the newest is the same as the 2nd one, no interpolation is possible since the most newest one needs to be different.<br>
	 * - the location of newest is different from the 2nd one and the 2nd one is different from the 3rd one, no interpolation is needed, since
	 *   no two elements are the same. You always presume that if there is interpolation needed further down the queue, it has
	 * already been done.<br>
	 * - The newest is different from the 2nd one and the 2nd one is the same as the 3rd one, interpolation is needed (see example).
	 * It is possible that we need to go deeper than the 3rd element, if more measurements were the same.<br>
	 * 
	 * We also need to take into account that it is possible that there is no location bound to a measurement (the location is null).
	 * In this case we treat this measurement as a measurement that is the same as the one before.
	 * When there is no older element in the queue that actually has a location bound to it, no interpolation is possible either.<br>
	 * E.g. [4, null, null, 7] = [4, 7, 7, 7] and [null, 7, 7, 7] = [7, 7, 7, 7], but when [4, null, null, null] = [4, ?, ?, ?], no interpolation is possible.
	 * 
	 * We also have chosen to change the null-values only when interpolation is possible.
	 * E.g. [null, 2, null] stays exactly the same when a new 2 or null is added in front (so [2, null, 2, null] or [null, null, null, null]<br>
	 * 
	 * @param measurementBuffer is the buffer containing the last measurements that aren't saved yet.
	 */
	public void process(Measurement newMeasurement, Track track)
	{
		int bufferSize = track.getBufferSize();
		// When the size is smaller than 3, interpolation is not possible :
		if(bufferSize < 3)
			return;

		// We than start working on the reversed enumeration of the internal buffer (newer elements first) :
		Enumeration/*<Measurement>*/ measurements = track.getMeasurementsNewestFirst();

		// We peek at the three newest elements in the queue :
		if(!measurements.hasMoreElements())
			// Whenever there are no three elements, interpolation is not possible.
			return;
		Measurement first = (Measurement) measurements.nextElement(); //== newMeasurement
		if(!measurements.hasMoreElements())
			return;
		Measurement second = (Measurement) measurements.nextElement();
		if(!measurements.hasMoreElements())
			return;
		Measurement third = (Measurement) measurements.nextElement();

		// In case the newest element is or null, or the same as the one before, we can not perform interpolation :
		// e.g. [7, 7, x, x]
		if(first.getLocation() == null || (second.getLocation() != null && first.getLocation().equals(second.getLocation())))
			return;

		// In case the second element isn't null, but the third one is, all older elements also have to be null :
		// Otherwise the third element would have been interpolated earlier.
		//e.g. [7, 4, null, null]
		//TODO This is actually possible, e.g. [7, 4, null, 4, null]
		//if(second.getLocation() != null && third.getLocation() == null)
		//	return;

		// In case the second element is different from the third, no interpolation is needed for the second element :
		// e.g. [7, 4, 5, x]
		if(second.getLocation() != null && third.getLocation() != null && !second.getLocation().equals(third.getLocation()))
			return;

		// When we arrive here, we can presume the first one is different from the second on, but the second one is the same as the third one.


		// Now we try to find the oldest element that is the same as the third one :
		// This will give us a view on how many elements have to be interpolated.

		/* When searching for the oldest location that is the same as the newer ones, we need to take different possibilities into account :
		 * [4, 7, 7, 7, 7, 8]
		 * [4, 7, 7, 7, null, 8] ( = [4, 7, 7, 7, 7, 8])
		 * [4, null, 7, 7, 7, 8] ( = [4, 7, 7, 7, 7, 8])
		 * [4, 7, null, 7, 7, 8] ( = [4, 7, 7, 7, 7, 8])
		 * [4, null, 7, 7, null, 8] ( = [4, 7, 7, 7, 7, 8])
		 * [4, 7, 7, 7, null] ( = [4, 7, 7, 7, 7])
		 * ...
		 * 
		 * Therefore we start by trying to find the first element of the tail, which has a location, which we can than use to compare with the other older elements,
		 * so in the above examples, this element will always be a "7".
		 */

		measurements = track.getMeasurementsNewestFirst();
		// We start with the second measurement, since the first one is the one we already use to interpolate. So in the example we skip "4" :
		measurements.nextElement();
		Measurement newestMeasurement = (Measurement) measurements.nextElement();

		while(newestMeasurement.getLocation() == null)
		{
			// In this case they are all null, so we have nothing to interpolate with (you'll need at least two different values).
			// So here we are excluding the [4, null, null, null, null, null] case :
			if(!measurements.hasMoreElements())
				return;
			newestMeasurement = (Measurement) measurements.nextElement();
		}

		// So newestLocation stores the newest location that is different from the head of the measurements
		// In our example this is the first location "7" (or another "7", since they are all the same)
		NTLocation newestLocation = newestMeasurement.getLocation();

		// Now we try to find the last measurement that needs to be interpolated. This is the last one before the first element with a location different from newestLocation.
		// So in the example the last element before "8" (which has as location null or "7").

		measurements = track.getMeasurementsNewestFirst();
		// We skip the first one, since the first one is the one we already use to interpolate. So in the example we skip "4" :
		measurements.nextElement();
		// We need the oldest measurement (a candidate for the last element with the same location) with the next element :
		Measurement oldestMeasurement = (Measurement) measurements.nextElement();
		// We also need the next one, to compare with, so we can find out the oldest "the same" element :
		Measurement oldestMeasurementPlusOne = (Measurement) measurements.nextElement();

		while(true)
		{
			// When the oldestMeasurementPlusOne has no location, we assume it is the same as oldestMeasurement and we can shift its pointer :
			if(oldestMeasurementPlusOne.getLocation() == null)
			{
				// First we move the oldestMeasurement pointer (since the new "oldest element" is the old "oldest element plus one" :
				oldestMeasurement = oldestMeasurementPlusOne;
				// If we ran over all the elements, we may stop and assume that at this time, oldestMeasurement stores the oldest element with the same location :
				if(!measurements.hasMoreElements())
					break;
				// Otherwise also shift the pointer to "the next element after the oldest checked element" to the next one :
				oldestMeasurementPlusOne = (Measurement) measurements.nextElement();
				continue;
			}

			// If the newestLocation is the same as oldestMeasurementPlusOne, we may shift the pointer of oldestMeasurement to oldestMeasurementPlusOne,
			// knowing that oldestMeasurementPlusOne also has the same location as all the previous ones :
			if(newestLocation.equals(oldestMeasurementPlusOne.getLocation()))
			{
				oldestMeasurement = oldestMeasurementPlusOne;
				// If we ran over all the elements, we may stop and assume that at this time, oldestMeasurement stores the oldest element with the same location :
				if(!measurements.hasMoreElements())
					break;
				// Otherwise also shift the pointer to "the next element after the oldest checked element" to the next one :
				oldestMeasurementPlusOne = (Measurement) measurements.nextElement();
				continue;
			}

			// In this case, oldestMeasurementPlusOne has a location that is different from the one we're comparing with (= newestLocation).
			// This means that the oldest location that is the same is stored in the element just before oldestMeasurementPlusOne (= oldestMeasurement).
			break;
		}

		// A final test to see if the top is really different from the rest :
		// e.g. [7, null, null, null, 7] would have gotten this far.
		// We have also already excluded the [null, x, x, x, x] case and the [x, null, null, null, null] case.
		if(!newestLocation.equals(track.getLastMeasurement().getLocation()))
			// Once this is done, the interpolation can begin :
			interpolateBufferGeodetic(track.getLastMeasurement(), newestLocation, oldestMeasurement, track);
	}

	/**
	 * 
	 * Cartesian interpolation of coordinates, not geographically correct but
	 * error should be negligible over short distances.
	 * @param newMeasurement is the top of the queue, the element which we use to interpolate all the older ones with.
	 * @param oldLocation is the old location that is used to do interpolation. It must be different from the location of newMeasurement.
	 * @param oldestMeasurement is the oldest measurement which has to be interpolated. It's location is "null" or the same as oldLocation.
	 * @param track is the Track element in which the Measurements are stored.
	 */
	protected void interpolateBufferCartesian(Measurement newMeasurement, NTLocation oldLocation, Measurement oldestMeasurement, Track track)
	{
		// take a copy, since oldLocation serves as a reference and may not be (accidently) changed
		oldLocation = new NTLocation(oldLocation.getCoordinates());

		// from = the oldest position, to = the new position which allows us to interpolate.
		INTCoordinates fromPosition = oldLocation.getCoordinates();
		// take a copy, since toPosition serves as a reference and may not be (accidently) changed
		INTCoordinates toPosition = newMeasurement.getLocation().getCoordinates().copy();

		double latitudeDifference = fromPosition.getLatitude() - toPosition.getLatitude();
		double longitudeDifference = fromPosition.getLongitude() - toPosition.getLongitude();
		float altitudeDifference = (float) (fromPosition.getAltitude() - toPosition.getAltitude());
		long fromTime = oldestMeasurement.getTimeStamp().getTime();
		// totalTime stores the time difference between the oldest measurement and the newest one
		long totalTime = newMeasurement.getTimeStamp().getTime() - fromTime;
		double latitudePerT = (double) (latitudeDifference / totalTime);
		double longitudePerT = (double) (longitudeDifference / totalTime);
		float altitudePerT = (float) (altitudeDifference / totalTime);

		// Using the previously calculated information, we will interpolate each element between the top of the queue and the oldest element with the same location.
		// Note that "the same location as the previous one" means that it is actually the same OR that is is "null".

		Enumeration/*<Measurement>*/ measurements = track.getMeasurementsNewestFirst();
		// We start with the second measurement, since the first one is the one we already use to interpolate (= newMeasurement) :
		measurements.nextElement();

		while(measurements.hasMoreElements())
		{
			Measurement measurementToInterpolate = (Measurement) measurements.nextElement();
			NTLocation locationToInterpolate = measurementToInterpolate.getLocation();

			// If the element we are currently looking at has a different location from the one we're comparing with,
			// this measurement and the following ones shouldn't be interpolated anymore :
			if(locationToInterpolate!=null && !oldLocation.equals(locationToInterpolate))
			{
				break;
			}
			long deltaT = measurementToInterpolate.getTimeStamp().getTime() - fromTime;
			double deltaLat = latitudePerT * (double) deltaT;
			double deltaLon = longitudePerT * (double) deltaT;
			float deltaAlt = altitudePerT * (float) deltaT;

			INTCoordinates newCoordinates = NTClient.getInstance().getNTCoordinates(fromPosition.getLatitude() - deltaLat, fromPosition.getLongitude()- deltaLon, fromPosition.getAltitude() - deltaAlt);
			if(measurementToInterpolate.getLocation() != null)
				measurementToInterpolate.getLocation().setCoordinates(newCoordinates);
			else
				measurementToInterpolate.setLocation(new NTLocation(newCoordinates));		
		}
	}

	protected void interpolateBufferGeodetic(Measurement newMeasurement, NTLocation oldLocation, Measurement oldestMeasurement, Track track)
	{
		// take a copy, since oldLocation serves as a reference and may not be (accidently) changed
		oldLocation = new NTLocation(oldLocation.getCoordinates());

		// from = the oldest position, to = the new position which allows us to interpolate.
		INTCoordinates fromPosition = oldLocation.getCoordinates();
		// take a copy, since toPosition serves as a reference and may not be (accidently) changed
		INTCoordinates toPosition = newMeasurement.getLocation().getCoordinates().copy();

		float totalDistance = (float) fromPosition.distance(toPosition);
		float altitudeDifference = (float) (fromPosition.getAltitude() - toPosition.getAltitude());
		double course = (double) fromPosition.azimuthTo(toPosition);
		long fromTime = oldestMeasurement.getTimeStamp().getTime();
		// totalTime stores the time difference between the oldest measurement and the newest one
		long totalTime = newMeasurement.getTimeStamp().getTime() - fromTime;
		double distancePerT = ((double) totalDistance / (double) totalTime);
		float altitudePerT = (altitudeDifference / (float) totalTime);

		// Using the previously calculated information, we will interpolate each element between the top of the queue and the oldest element with the same location.
		// Note that "the same location as the previous one" means that it is actually the same OR that is is "null".

		Enumeration/*<Measurement>*/ measurements = track.getMeasurementsNewestFirst();
		// We start with the second measurement, since the first one is the one we already use to interpolate (= newMeasurement) :
		measurements.nextElement();
		while(measurements.hasMoreElements())
		{
			Measurement measurementToInterpolate = (Measurement) measurements.nextElement();
			NTLocation locationToInterpolate = measurementToInterpolate.getLocation();

			// If the element we are currently looking at has a different location from the one we're comparing with,
			// this measurement and the following ones shouldn't be interpolated anymore :
			if(locationToInterpolate!=null && !oldLocation.equals(locationToInterpolate))
			{
				break;
			}

			long deltaT = measurementToInterpolate.getTimeStamp().getTime() - fromTime;
			double deltaD = distancePerT * (double) deltaT;
			float deltaA = altitudePerT * (float) deltaT;
			try
			{
				INTCoordinates translatedCoordinates = CoordinateUtils.translate(fromPosition, course, deltaD);
				translatedCoordinates.setAltitude(fromPosition.getAltitude() - deltaA);
				if(measurementToInterpolate.getLocation() != null)
					measurementToInterpolate.getLocation().setCoordinates(translatedCoordinates);
				else
					measurementToInterpolate.setLocation(new NTLocation(translatedCoordinates));	
			}
			catch(Exception e)
			{
				Logger.getInstance().error(e, "CoordinateInterpolation.java -- interpolateBufferGeodetic(...) failed");
			}
		}
	}

	public void reset()
	{	
	}

	public String getName()
	{
		return "Coordinate interpolator";
	}

}
