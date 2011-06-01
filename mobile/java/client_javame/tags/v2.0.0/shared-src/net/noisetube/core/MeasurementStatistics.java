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

package net.noisetube.core;

import net.noisetube.model.ICoordinates;
import net.noisetube.model.Measurement;
import net.noisetube.model.Track;
import net.noisetube.util.StringUtils;

/**
 * @author mstevens
 *
 */
public class MeasurementStatistics implements IProcessor
{

	static private final char SEPARATOR = '$';
	
	/**
	 * For restarted Tracks
	 */
	static public MeasurementStatistics parse(String serialisedMeasurementStatistics)
	{
		String[] parts = StringUtils.split(serialisedMeasurementStatistics, SEPARATOR);
		if(parts == null || parts.length < 5)
			throw new IllegalArgumentException("Invalid runstate string: " + serialisedMeasurementStatistics);
		MeasurementStatistics stats = new MeasurementStatistics();
		stats.numMeasurements = Integer.parseInt(parts[0]);
		stats.avrdBA = Double.parseDouble(parts[1]);
		stats.maxdBA = Double.parseDouble(parts[2]);
		stats.mindBA = Double.parseDouble(parts[3]);
		stats.distanceMeters = Float.parseFloat(parts[4]);
		return stats;
	}
	
	//NumMeasurements
	private int numMeasurements;
	
	//dB
	//private double avrdB = 0;
	//private double maxdB = 0;
	//private double mindB = Double.MAX_VALUE;
	
	//dB(A)
	private double avrdBA = 0;
	private double maxdBA = 0;
	private double mindBA = Double.MAX_VALUE;
	
	//Distance covered
	private ICoordinates previousCoordinates = null;
	private float distanceMeters = 0;


	public void process(Measurement measurement, Track track)
	{
		numMeasurements++;
		/*//dB
		if(measurement.isLoudnessLeqDBSet())
		{
			double db = measurement.getLoudnessLeqDB();
			if(db > maxdB)
				maxdB = db;
			if(db < mindB)
				mindB = db;
			avrdB = ((avrdB * (numMeasurements - 1)) + db) / numMeasurements; 
		}*/
		//dB(A)
		if(measurement.isLoudnessLeqDBASet())
		{
			double dba = measurement.getLoudnessLeqDBA();
			if(dba > maxdBA)
				maxdBA = dba;
			if(dba < mindBA)
				mindBA = dba;
			avrdBA = ((avrdBA * (numMeasurements - 1)) + dba) / numMeasurements;
		}
		//Distance:
		if(measurement.getLocation() != null && measurement.getLocation().hasCoordinates())
		{
			ICoordinates coords = measurement.getLocation().getCoordinates();
			if(previousCoordinates != null)
				distanceMeters += previousCoordinates.distance(coords);
			previousCoordinates = coords;
		}
	}

	/**
	 * @return the numMeasurements
	 */
	public int getNumMeasurements()
	{
		return numMeasurements;
	}

//	/**
//	 * @return the avrdB
//	 */
//	public double getAvrdB()
//	{
//		return avrdB;
//	}
//
//	/**
//	 * @return the maxdB
//	 */
//	public double getMaxdB()
//	{
//		return maxdB;
//	}
//
//	/**
//	 * @return the mindB
//	 */
//	public double getMindB()
//	{
//		return mindB;
//	}

	/**
	 * @return the avrdBA
	 */
	public double getAvrdBA()
	{
		return avrdBA;
	}

	/**
	 * @return the maxdBA
	 */
	public double getMaxdBA()
	{
		return maxdBA;
	}

	/**
	 * @return the mindBA
	 */
	public double getMindBA()
	{
		return mindBA;
	}

	/**
	 * @return the distanceMeters
	 */
	public float getDistanceCovered()
	{
		return distanceMeters;
	}
	
	public String serialize()
	{
		return 	Integer.toString(numMeasurements) + SEPARATOR +
				Double.toString(avrdBA) + SEPARATOR +
				Double.toString(maxdBA) + SEPARATOR +
				Double.toString(mindBA) + SEPARATOR +
				Float.toString(distanceMeters);
	}
	
	public String prettyPrint()
	{
		return 	" - Measurements: " + numMeasurements +
				"\n - Average Leq(1s): " + avrdBA + " dB(A)" +
				"\n - Maximum Leq(1s): " + maxdBA + " dB(A)" +
				"\n - Minimum Leq(1s): " + mindBA + " dB(A)" +
				"\n - Distance covered: " + StringUtils.formatDistance(distanceMeters, -2);
	}
	
}
