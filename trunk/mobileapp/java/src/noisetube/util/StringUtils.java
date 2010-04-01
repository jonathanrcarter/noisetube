/**
 * com.sony.csl.j2me.utils
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
import java.util.Vector;

/**
 * A class with useful string operations
 * 
 * @author mstevens
 * 
 */
public class StringUtils
{

	static public int countOccurences(String toSearch, char toFind)
	{
		int count = 0;
		for(int i = 0; i < toSearch.length(); i++)
			if(toSearch.charAt(i) == toFind)
				count++;
		return count;
	}

	static public String addTabsFront(String string, int tabs)
	{
		StringBuffer bff = new StringBuffer();
		for(int t = 0; t < tabs; t++)
			bff.append("\t");
		bff.append(string);
		return bff.toString();
	}

	static public int lastIndexOf(String toSearch, String toFind)
	{
		if(toSearch == null || toFind == null)
			throw new NullPointerException();
		// the dirty way:
		String toSearchReversed = (new StringBuffer(toSearch)).reverse()
				.toString();
		String toFindReversed = (new StringBuffer(toFind)).reverse().toString();
		return toSearch.length()
				- (toSearchReversed.indexOf(toFindReversed) + toFind.length() - 1)
				- 1;
	}

	static public String replace(String toSearch, String toFind,
			String replaceWith) throws IllegalArgumentException
	{
		toSearch = new String(toSearch); // make copy
		if(toSearch != null && toFind != null && replaceWith != null)
		{
			int idx = toSearch.indexOf(toFind);
			while(idx > -1)
			{
				String rest = right(toSearch, toSearch.length()
						- (idx + toFind.length()));
				toSearch = left(toSearch, idx) + replaceWith + rest;
				int idxInRest = rest.indexOf(toFind);
				idx = ((idxInRest > -1) ? (idxInRest + idx + replaceWith
						.length()) : -1);
			}
		}
		return toSearch;
	}

	static public String replaceWithEscape(String toSearch, char toFind,
			String replaceWith, char escape) throws IllegalArgumentException
	{
		toSearch = new String(toSearch); // make copy
		if(toSearch != null)
		{
			int idx = toSearch.indexOf(toFind);
			while(idx > -1)
			{
				String rest = right(toSearch, toSearch.length() - (idx + 1));
				int shift = 0;
				if(idx > 0 && toSearch.charAt(idx - 1) == escape)
					toSearch = left(toSearch, idx - 1) + toFind + rest; // escape:
																		// "!$"
																		// = "$"
				else
				{
					toSearch = left(toSearch, idx) + replaceWith + rest;
					shift = replaceWith.length();
				}
				int idxInRest = rest.indexOf(toFind);
				idx = ((idxInRest > -1) ? (idxInRest + idx + shift) : -1);
			}
		}
		return toSearch;
	}

	static public String replaceWithEscape(String toSearch, char toFind,
			String[] replaceWith, char escape) throws IllegalArgumentException
	{
		toSearch = new String(toSearch); // make copy
		if(toSearch != null && replaceWith != null)
		{
			int idx = toSearch.indexOf(toFind);
			int r = 0;
			while(idx > -1 && replaceWith.length > r && replaceWith[r] != null)
			{
				String rest = right(toSearch, toSearch.length() - (idx + 1));
				int shift = 0;
				if(idx > 0 && toSearch.charAt(idx - 1) == escape)
					toSearch = left(toSearch, idx - 1) + toFind + rest; // escape:
																		// "!$"
																		// = "$"
				else
				{
					toSearch = left(toSearch, idx) + replaceWith[r] + rest;
					shift = replaceWith[r].length();
					r++;
				}
				int idxInRest = rest.indexOf(toFind);
				idx = ((idxInRest > -1) ? (idxInRest + idx + shift) : -1);
			}
		}
		return toSearch;
	}

	static public String replace(String toSearch, char toFind,
			String replaceWith)
	{
		return replace(toSearch, new String(new char[] { toFind }), replaceWith);
	}

	static public String replace(String toSearch, String toFind,
			char replaceWith)
	{
		return replace(toSearch, toFind, new String(new char[] { replaceWith }));
	}

	static public String left(String string, int substringLength)
	{
		if(substringLength < 0)
			throw new IllegalArgumentException(
					"Substring length cannot be negative!");
		return string.substring(0, substringLength);
	}

	static public String right(String string, int substringLength)
	{
		if(substringLength < 0)
			throw new IllegalArgumentException(
					"Substring length cannot be negative!");
		return string.substring(string.length() - substringLength, string
				.length());
	}

	static public String padWithLeadingZeros(String string, int desiredLength)
	{
		StringBuffer bff = new StringBuffer(string);
		while(bff.length() < desiredLength)
			bff.insert(0, '0');
		return bff.toString();
	}

	static public String cutOrExtendAtTail(String string, int desiredLength,
			char filler)
	{
		if(string.length() > desiredLength)
			return left(string, desiredLength);
		else if(string.length() < desiredLength)
		{
			StringBuffer bff = new StringBuffer(string);
			for(int i = 0; i < desiredLength - string.length(); i++)
				bff.append(filler);
			return bff.toString();
		}
		else
			return string;
	}

	static public String removeWhiteSpace(String string)
	{
		string = replace(string, '\n', "");
		string = replace(string, '\r', "");
		string = replace(string, '\t', "");
		string = replace(string, ' ', "");
		return string;
	}

	static public String contractToCamelCase(String string)
	{
		StringBuffer bff = new StringBuffer(string.length());
		boolean previousWasSpace = false;
		for(int i = 0; i < string.length(); i++)
		{
			char currentChar = string.charAt(i);
			if(currentChar == ' ')
			{
				previousWasSpace = true;
			}
			else
			{
				if(previousWasSpace)
					bff.append(Character.toUpperCase(currentChar));
				else
					bff.append(currentChar);
				previousWasSpace = false;
			}
		}
		return bff.toString();
	}

	static public String removeWhiteSpaceAndContractToCamelCase(String string)
	{
		string = replace(string, '\n', "");
		string = replace(string, '\r', "");
		string = replace(string, '\t', "");
		return contractToCamelCase(string);
	}

	public static String[] split(String string, char separatorChar)
	{
		return split(string, new char[] { separatorChar });
	}

	public static String[] split(String string, char[] separatorChars)
	{
		if(string == null || string.equals(""))
			return new String[] { string };
		int len = string.length();
		Vector separators = new Vector(separatorChars.length);
		for(int s = 0; s < separatorChars.length; s++)
			separators.addElement(new Character(separatorChars[s]));
		Vector list = new Vector();
		int i = 0;
		int start = 0;
		boolean match = false;
		while(i < len)
		{
			if(separators.contains(new Character(string.charAt(i))))
			{
				if(match)
				{
					list.addElement(string.substring(start, i));
					match = false;
				}
				start = ++i;
				continue;
			}
			match = true;
			i++;
		}
		if(match)
		{
			list.addElement(string.substring(start, i));
		}
		String[] arr = new String[list.size()];
		list.copyInto(arr);
		return arr;
	}

	/**
	 * Converts a long timestamp to a string in the following format:</br>
	 * YYYY[dateSeparator
	 * ]MM[dateSeparator]DD[dateTimeSeparator]hh[timeSeparator]
	 * mm[timeSeparator]ss
	 * 
	 * @param timeStamp
	 * @param dateSeparator
	 * @param timeSeparator
	 * @param dateTimeSeparator
	 * @return A String containing the formatted timestamp
	 */
	static public String formatDateTime(long timeStamp, String dateSeparator,
			String timeSeparator, String dateTimeSeparator)
	{
		if(dateSeparator == null)
			dateSeparator = "";
		if(dateTimeSeparator == null)
			dateTimeSeparator = "";
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getDefault()); // use system time zone
		cal.setTime(new Date(timeStamp)); // set the time
		// Format the string:
		StringBuffer bff = new StringBuffer();
		// Date (YYYY[dateSeparator]MM[dateSeparator]DD)
		bff.append(StringUtils.padWithLeadingZeros(String.valueOf(cal
				.get(Calendar.YEAR)), 4)
				+ dateSeparator);
		bff.append(StringUtils.padWithLeadingZeros(String.valueOf(cal
				.get(Calendar.MONTH) + 1), 2)
				+ dateSeparator);
		bff.append(StringUtils.padWithLeadingZeros(String.valueOf(cal
				.get(Calendar.DAY_OF_MONTH)), 2));
		// [dateTimeSeparator]
		bff.append(dateTimeSeparator);
		// Time (hh[timeSeparator]mm[timeSeparator]ss)
		bff.append(formatTime(timeStamp, timeSeparator));
		// return the result
		return bff.toString();
	}

	/**
	 * Converts a long timestamp to a string in the following format:</br>
	 * hh[timeSeparator]mm[timeSeparator]ss
	 * 
	 * @param timeStamp
	 * @param timeSeparator
	 * @return A String containing the formatted timestamp
	 */
	static public String formatTime(long timeStamp, String timeSeparator)
	{
		if(timeSeparator == null)
			timeSeparator = "";
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getDefault()); // use system time zone
		cal.setTime(new Date(timeStamp)); // set the time
		// Format the string:
		StringBuffer bff = new StringBuffer();
		// Time (hh[timeSeparator]mm[timeSeparator]ss)
		bff.append(StringUtils.padWithLeadingZeros(String.valueOf(cal
				.get(Calendar.HOUR_OF_DAY)), 2)
				+ timeSeparator);
		bff.append(StringUtils.padWithLeadingZeros(String.valueOf(cal
				.get(Calendar.MINUTE)), 2)
				+ timeSeparator);
		bff.append(StringUtils.padWithLeadingZeros(String.valueOf(cal
				.get(Calendar.SECOND)), 2));
		// return the result
		return bff.toString();
	}

	public static String formatTimeSpan(long milliseconds)
	{
		// Days
		long days = milliseconds / 86400000;
		// Hours
		long hours = (milliseconds % 86400000) / 3600000;
		// Minutes
		long minutes = ((milliseconds % 86400000) % 3600000) / 60000;
		// Seconds.millis
		float seconds = (((milliseconds % 86400000) % 3600000) % 60000) / 1000;
		return "" + ((days > 0) ? (days + "d ") : "")
				+ ((hours > 0) ? (hours + "h ") : "")
				+ ((minutes > 0) ? (minutes + "m ") : "") + seconds + "s";
	}

	public static String StringArrayToString(String[] array, String separator)
	{
		if(array != null && array.length > 0)
		{
			StringBuffer bff = new StringBuffer(array[0]);
			for(int i = 1; i < array.length; i++)
				bff.append(separator + array[i]);
			return bff.toString();
		}
		else
			return "";
	}

}
