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

package noisetube.watch;

import javax.microedition.location.Coordinates;

/**
 * 
 * @author maisonneuve
 * 
 */
public class WatchDataFrame
{

	private long timeStamp;

	private float loudness;
	private float ozone;
	private float batteryLevel;
	private Coordinates coordinates;

	public WatchDataFrame()
	{
		timeStamp = System.currentTimeMillis();
	}

	public void setLocation(float latitude, float longitude)
	{
		this.coordinates = new Coordinates(latitude, longitude, 0);
	}

	public void setLoudness(float loudness)
	{
		this.loudness = loudness;
	}

	/**
	 * @return the timeStamp
	 */
	public long getTimeStamp()
	{
		return timeStamp;
	}

	/**
	 * @return the coordinates
	 */
	public Coordinates getCoordinates()
	{
		return coordinates;
	}

	/**
	 * @return the loudness
	 */
	public float getLoudness()
	{
		return loudness;
	}

	/**
	 * @return the ozone
	 */
	public float getOzone()
	{
		return ozone;
	}

	/**
	 * @return the batteryLevel
	 */
	public float getBatteryLevel()
	{
		return batteryLevel;
	}

}
