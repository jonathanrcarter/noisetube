/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Java ME version)
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

package net.noisetube.core.javame;

import net.noisetube.core.MeasurementStatistics;
import net.noisetube.core.javame.restarting.RunState;
import net.noisetube.model.Track;
import net.noisetube.util.CustomStringBuffer;
import net.noisetube.util.StringUtils;

/**
 * @author mstevens
 *
 */
public class TrackRunState extends RunState
{
	
	private Track track;
	
	public TrackRunState(MainMIDlet owner)
	{
		super(owner);
	}
	
	public TrackRunState(MainMIDlet owner, String state)
	{
		this(owner);
		String[] parts = StringUtils.split(state, SEPARATOR);
		if(parts == null || parts.length < 6)
			throw new IllegalArgumentException("Invalid runstate string: " + state);
		this.runCount = Integer.parseInt(parts[0]);
		this.startTime = Long.parseLong(parts[1]);
		this.stopTime = Long.parseLong(parts[2]);
		this.track = new Track(Integer.parseInt(parts[3]), MeasurementStatistics.parse(parts[4]), Long.parseLong(parts[5]));		
	}
		
	public String toString()
	{
		return 	super.toString() 						+ SEPARATOR +
				Integer.toString(track.getTrackID()) 	+ SEPARATOR +
				track.getStatistics().serialize()		+ SEPARATOR +
				Long.toString(track.getTotalElapsedTime());
	}
	
	public String prettyPrint()
	{
		CustomStringBuffer bff = new CustomStringBuffer();
		bff.appendLine(super.prettyPrint());
		bff.appendLine(" - TrackID: " + Integer.toString(track.getTrackID()));
		bff.appendLine(track.getStatistics().prettyPrint());
		bff.append(" - Total elapsed time: " + Long.toString(track.getTotalElapsedTime()));
		return bff.toString();
	}
	
	/**
	 * @return the track
	 */
	public Track getTrack()
	{
		return track;
	}

	/**
	 * @param track the track to set
	 */
	public void setTrack(Track track)
	{
		this.track = track;
	}

}
