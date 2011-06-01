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

package net.noisetube.model;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.noisetube.ui.IMeasurementUI;
import net.noisetube.util.CyclicQueue;
import net.noisetube.util.IStringEncoder;
import net.noisetube.core.IProcessor;
import net.noisetube.core.MeasurementStatistics;
import net.noisetube.io.saving.Saver;

/**
 * Track
 * 
 * @author sbarthol, mstevens
 * 
 */
public class Track
{

	//STATIC
	static public final int UNSPECIFIED_TRACK_ID = -1;
	static public final int DEFAULT_BUFFER_CAPACITY = 60;

	//DYNAMIC
	private int trackID = UNSPECIFIED_TRACK_ID;
	private Vector/*<PostProcessor>*/ processors;
	private int bufferCapacity;
	private CyclicQueue measurementBuffer;
	private Hashtable/*<String, String>*/ metaDataTable;
	
	private Saver saver = null;
	private IMeasurementUI ui;
	
	private long startTime;
	private long pausedSince = 0;
	private long timeSpentEarlier = 0;
	private long timeSpentInPauseMS = 0;
	private MeasurementStatistics statistics;
	
	/**
	 * Creates a new Track.
	 */
	public Track()
	{
		this(UNSPECIFIED_TRACK_ID, new MeasurementStatistics(), 0);
	}

	/**
	 * For restarting a track
	 */
	public Track(int trackID, MeasurementStatistics statistics, long elapsedTime)
	{
		this.startTime = System.currentTimeMillis();
		this.trackID = trackID;
		this.statistics = statistics;
		this.timeSpentEarlier = elapsedTime;
		
		//Data structures:
		this.bufferCapacity = DEFAULT_BUFFER_CAPACITY;
		measurementBuffer = new CyclicQueue(bufferCapacity);
		processors = new Vector/*<PostProcessor>*/();
		metaDataTable = new Hashtable/*<String, String>*/();
		
		//add default processor(s):
		addProcessor(statistics); //!!!
	}
	
	/**
	 * This function adds a new Measurement to the measurementQueue. This will trigger both the saving of
	 * the element that was on top and is removed from the queue, as running all the PostProcessors over the
	 * new measurementQueue.
	 * 
	 * @param m The new Measurement
	 */
	public void addMeasurement(Measurement m)
	{
		//if the buffer was full the offer operation returns the element that was removed (the oldest one in the queue) to make room for the one being offered
		Measurement oldMeasurement = (Measurement) measurementBuffer.offer(m);
		if(oldMeasurement != null && saver != null && saver.isRunning())
			saver.save(oldMeasurement);		
		runProcessors(); //!!!
		if(ui != null)
			ui.newMeasurement(this, m, oldMeasurement);
	}
	
	public void pause()
	{
		pausedSince = System.currentTimeMillis();
	}
	
	public void resume()
	{
		if(pausedSince != 0)
		{
			timeSpentInPauseMS += System.currentTimeMillis() - pausedSince;
			pausedSince = 0;
		}
	}

	public Measurement getLastMeasurement()
	{
		return (Measurement) measurementBuffer.tail();
	}

	public void addMetadata(String key, String value)
	{
		if(key == null || key.equals(""))
			throw new IllegalArgumentException("Invalid key");
		if(value != null)
			metaDataTable.put(key, value);
	}

	public void addProcessor(IProcessor processor)
	{
		processors.addElement(processor);
		//Logger.getInstance().debug(processor.getName() + " enabled");
	}

	/**
	 * Builds up a String containing all the key-value combinations.
	 * @param equals
	 * @param separator
	 * @param keysAsLowerCase
	 * @param stringEncorder
	 * @return
	 */
	public String getMetaDataString(String equals, String separator, boolean keysAsLowerCase, IStringEncoder stringEncorder)
	{
		return getMetaDataString(equals, separator, null, keysAsLowerCase, stringEncorder);
	}

	/**
	 * Builds up a String containing all the key-value combinations.
	 * @param equals
	 * @param separator
	 * @param valueQuote
	 * @param keysAsLowerCase
	 * @param stringEncorder
	 * @return The resulting String
	 */
	public String getMetaDataString(String equals, String separator, char valueQuote, boolean keysAsLowerCase, IStringEncoder stringEncorder)
	{
		return getMetaDataString(equals, separator, new Character(valueQuote), keysAsLowerCase, stringEncorder);
	}

	/**
	 * Builds up a String containing all the key-value combinations.
	 * @param equals
	 * @param separator
	 * @param valueQuote
	 * @param keysAsLowerCase
	 * @param stringEncorder
	 * @return The resulting String
	 */
	private String getMetaDataString(String equals, String separator, Character valueQuote, boolean keysAsLowerCase, IStringEncoder stringEncorder)
	{
		StringBuffer bff = new StringBuffer();
		Enumeration/*<String>*/ keys = metaDataTable.keys();
		while(keys.hasMoreElements())
		{
			String key = (String) keys.nextElement(), value = (String) metaDataTable.get(key);
			if(bff.length() > 0)
				bff.append(separator);
			bff.append(encodeString(keysAsLowerCase ? key.toLowerCase() : key, stringEncorder));
			bff.append(equals);
			bff.append((valueQuote != null ? valueQuote.toString() : "") + encodeString(value, stringEncorder) + (valueQuote != null ? valueQuote.toString() : ""));
		}
		return bff.toString();
	}

	private String encodeString(String str, IStringEncoder encoder)
	{
		if(encoder != null)
			return encoder.encode(str);
		else
			return str;
	}

	public int getTrackID()
	{
		return trackID;
	}

	/**
	 * @param trackID the trackID to set
	 */
	public void setTrackID(int trackID)
	{
		this.trackID = trackID;
	}

	public boolean isTrackIDSet()
	{
		return trackID != UNSPECIFIED_TRACK_ID;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime()
	{
		return startTime;
	}
	
	public long getTotalElapsedTime()
	{
		return timeSpentEarlier + (System.currentTimeMillis() - startTime - timeSpentInPauseMS);
	}

	public int getBufferCapacity()
	{
		return measurementBuffer.getCapacity();
	}
	
	/**
	 * Returns how many measurements are present in the internal buffer.
	 * @return The amount of measurements present in the internal buffer.
	 */
	public int getBufferSize()
	{
		return measurementBuffer.getSize();
	}

	/**
	 * Will run all the Processors, using the measurementQueue as their parameter.
	 */
	private void runProcessors()
	{
		for(int i = 0; i < processors.size(); i++)
			((IProcessor) processors.elementAt(i)).process(getNewestMeasurement(), this);
	}

	public void setSaver(Saver saver)
	{
		this.saver = saver;
	}
	
	public void setUI(IMeasurementUI ui)
	{
		this.ui = ui;
	}

	public Measurement getNewestMeasurement()
	{
		return (Measurement) measurementBuffer.getElement(measurementBuffer.getSize()-1);
	}

	public Measurement getOldestMeasurement()
	{
		return (Measurement) measurementBuffer.getElement(0);
	}

	/**
	 * Gives us an enumeration which enumerates starting from the oldest element (the first element added).
	 * @return The enumeration.
	 */
	public Enumeration/*<Measurement>*/ getMeasurements()
	{
		return measurementBuffer.getElements();
	}

	/**
	 * Gives us a reversed enumeration which enumerates starting from the newest element (the last element added).
	 * @return The reversed enumeration.
	 */
	public Enumeration/*<Measurement>*/ getMeasurementsNewestFirst()
	{
		return measurementBuffer.getElementsReversed();
	}

	public MeasurementStatistics getStatistics()
	{
		return statistics;
	}

	/**
	 * Saves all the measurements in the buffer and resets the buffer.
	 */
	public void flushBuffer()
	{
		if(saver != null && saver.isRunning())
			saver.saveBatch(measurementBuffer.getElements());
		measurementBuffer = new CyclicQueue(bufferCapacity);
	}

}
