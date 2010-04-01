/**
 * com.sony.csl.j2me.io
 *  
 * Description:
 *   Utilities for J2ME CLDC/MIDP applications
 *   
 *   This code was developed within the scope of the NoiseTube project at
 *   Sony Computer Science Laboratory (CSL) Paris, for more information please refer to: 
 *     - http://noisetube.net
 *     - http://code.google.com/p/noisetube
 *     - http://www.csl.sony.fr
 * 
 * Author:
 *   Matthias Stevens (Sony CSL Paris / Vrije Universiteit Brussel)
 *   Contact: matthias.stevens@gmail.com
 * 	
 * License: 
 *   Copyright 2008 Matthias Stevens (Sony CSL Paris / Vrije Universiteit Brussel)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *          http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package noisetube.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * A class with helpful methods for dealing with XML
 * 
 * @author mstevens
 * 
 */
public class XMLUtils
{

	/**
	 * Returns an XML comment string with the given text and the given number of
	 * tabs in front
	 * 
	 * @param text
	 * @param tabs
	 * @return xml comment String
	 */
	static public String comment(String text, int tabs)
	{
		return StringUtils.addTabsFront("<!-- " + text + " -->", tabs);
	}

	/**
	 * Replaces reserved XML characters with escapes
	 * 
	 * @param input
	 *            a String to process
	 * @return the same String but with reserved XML characters escaped
	 */
	static public String escapeCharacters(String input)
	{
		input = StringUtils.replace(input, '&', "&amp;");
		input = StringUtils.replace(input, '<', "&lt;");
		input = StringUtils.replace(input, '>', "&gt;");
		input = StringUtils.replace(input, '"', "&quot;");
		input = StringUtils.replace(input, '\'', "&apos;");
		return input;
	}

	/**
	 * Converts a long timestamp to a string in XML dateTime format:</br> -
	 * YYYY-MM-DDThh:mm:ssZ format for UTC times</br> -
	 * YYYY-MM-DDThh:mm:sszzzzzz format for non-UTC times, where zzzzzz
	 * represents ±hh:mm in relation to UTC</br> Info:</br> - <a href=
	 * "http://code.google.com/apis/kml/documentation/kmlreference.html#timestamp"
	 * >http://code.google.com/apis/kml/documentation/kmlreference.html#
	 * timestamp</a></br> - <a
	 * href="http://code.google.com/apis/kml/documentation/time.html"
	 * >http://code.google.com/apis/kml/documentation/time.html</a>
	 * 
	 * @param timeStamp
	 * @return A String containing the formatted timestamp
	 */
	static public String timeDateValue(long timeStamp)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getDefault()); // use system time zone
		cal.setTime(new Date(timeStamp)); // set the time
		// Format the string:
		StringBuffer bff = new StringBuffer();
		// Date (YYYY-MM-DD)
		bff.append(StringUtils.padWithLeadingZeros(String.valueOf(cal
				.get(Calendar.YEAR)), 4)
				+ "-");
		bff.append(StringUtils.padWithLeadingZeros(String.valueOf(cal
				.get(Calendar.MONTH) + 1), 2)
				+ "-");
		bff.append(StringUtils.padWithLeadingZeros(String.valueOf(cal
				.get(Calendar.DAY_OF_MONTH)), 2));
		// Time (Thh:mm:ss)
		bff.append("T"
				+ StringUtils.padWithLeadingZeros(String.valueOf(cal
						.get(Calendar.HOUR_OF_DAY)), 2) + ":");
		bff.append(StringUtils.padWithLeadingZeros(String.valueOf(cal
				.get(Calendar.MINUTE)), 2)
				+ ":");
		bff.append(StringUtils.padWithLeadingZeros(String.valueOf(cal
				.get(Calendar.SECOND)), 2));
		// UTC/GTM offset if needed:
		int gmtOffset = cal.getTimeZone().getRawOffset();
		if(gmtOffset == 0)
			// time is already in UTC/GMT (add "Z")
			bff.append("Z");
		else
		{ // time is not in UTC/GMT (add zzzzzz, which represents ±hh:mm in
			// relation to UTC)
			bff.append((gmtOffset < 0) ? "-" : "+"); // + or -
			bff.append(StringUtils.padWithLeadingZeros(String.valueOf(Math
					.abs(gmtOffset) / 3600000), 2)
					+ ":"); // offset in whole absolute hours
			bff.append(StringUtils.padWithLeadingZeros(String.valueOf((Math
					.abs(gmtOffset) % 3600000) / 60000), 2)); // modulo of whole
																// hour offset
																// in absolute
																// minutes
		}
		// return the result
		return bff.toString();
	}

}
