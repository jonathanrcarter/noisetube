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

package net.noisetube.audio;

import net.noisetube.core.IProcessor;
import net.noisetube.model.Measurement;
import net.noisetube.model.Track;

/**
 * Uses the Daily Noise Dose level formula specified by the NIOSH (US governmental agency)
 * 
 * http://www.engineeringtoolbox.com/noise-exposure-level-d_718.html
 * http://www.engineeringtoolbox.com/noise-exposure-level-duration-d_717.html
 * 
 * @author mstevens
 * 
 * TODO account for intervals
 *
 */
public class DoseMeter implements IProcessor
{
	
	//Statics------------------------------------------------------------------
	private int DB_RANGE_MIN = 80;
	private int DB_RANGE_MAX = 130;
	
	private static int[] MAX_EXPOSURE_AT_DB =
	{
		/* dBA		Hours			Minutes		Seconds	*/
		/* -------------------------------------------- */
		/* 80 */	25	* 3600 +	24	* 60			,
		/* 81 */	20	* 3600 +	10 	* 60			,
		/* 82 */	16	* 3600							,		
		/* 83 */	12	* 3600 +	42 	* 60			,
		/* 84 */	10	* 3600 +	5	* 60			,
		/* 85 */	8	* 3600							,	
		/* 86 */	6	* 3600 +	21	* 60			,
		/* 87 */	5	* 3600 +	2	* 60			,
		/* 88 */	4	* 3600							,	
		/* 89 */	3	* 3600 +	10	* 60			,
		/* 90 */	2	* 3600 +	31	* 60			,
		/* 91 */	2	* 3600							,
		/* 92 */	1	* 3600 +	35	* 60			,
		/* 93 */	1	* 3600 +	16	* 60			,
		/* 94 */	1	* 3600							,	
		/* 95 */  					47 	* 60	+	37	,
		/* 96 */					37 	* 60	+	48	,
		/* 97 */					30 	* 60			,
		/* 98 */					23 	* 60	+	49	,
		/* 99 */					18 	* 60	+	59	,
		/* 100 */					15 	* 60			,
		/* 101 */					11 	* 60	+	54	,
		/* 102 */					9 	* 60	+	27	,
		/* 103 */					7 	* 60	+	30	,
		/* 104 */					5 	* 60	+	57	,
		/* 105 */					4 	* 60	+	43	,
		/* 106 */					3 	* 60	+	45	,
		/* 107 */					2 	* 60	+	59	,
		/* 108 */					2 	* 60	+	22	,
		/* 109 */					1	* 60	+	53	,
		/* 110 */					1	* 60	+	29	,
		/* 111 */					1	* 60	+	11	,
		/* 112 */									56	,
		/* 113 */									45	,
		/* 114 */									35	,
		/* 115 */									28	,
		/* 116 */									22	,
		/* 117 */									18	,
		/* 118 */									14	,
		/* 119 */									11	,
		/* 120 */									9	,
		/* 121 */									7	,
		/* 122 */									6	,
		/* 123 */									4	,
		/* 124 */									3	,
		/* 125 */									3	,
		/* 126 */									2	,
		/* 127 */									1	,
		/* 128 */									1	,
		/* 129 */									1	,
		/* 130 */									1
	};
	
	private static int DEFAULT_UPDATE_FREQUENCY = 60;
	
	//Dynamics-----------------------------------------------------------------	
	private int[] exposureAtDB = new int[DB_RANGE_MAX - DB_RANGE_MIN + 1];
	private float dose;
	private int updateFrequency;
	private int counter = 0;
	
	public DoseMeter()
	{
		this(DEFAULT_UPDATE_FREQUENCY);
	}
	
	public DoseMeter(int updateFrequency)
	{
		this.updateFrequency = updateFrequency;
	}
	
	private void updateDose()
	{
		float levelAddition = 0;
		for(int l = DB_RANGE_MIN; l < DB_RANGE_MAX; l++)
			levelAddition += (float)exposureAtDB[l - DB_RANGE_MIN] / (float)MAX_EXPOSURE_AT_DB[l - DB_RANGE_MIN];
		dose = levelAddition * 100.0f;
	}

	public float getDose()
	{
		return dose;
	}

	public void process(Measurement newMeasurement, Track track)
	{
		int exposure = (int) Math.floor(newMeasurement.getLeqDBA() + 0.5d);
		if(exposure >= DB_RANGE_MIN && exposure <= DB_RANGE_MAX)
			exposureAtDB[exposure - DB_RANGE_MIN]++; //we assume the Leq measurement was for 1 second
		else if(exposure > DB_RANGE_MAX)
			exposureAtDB[DB_RANGE_MAX - DB_RANGE_MIN]++; //> MAX counts as MAX
		counter++;
		if(counter == updateFrequency)
		{
			updateDose();
			counter = 0;
		}		
	}

	public void reset()
	{
		dose = 0;
		counter = 0;
	}

	public String getName()
	{
		return "Dose meter";
	}
	
}