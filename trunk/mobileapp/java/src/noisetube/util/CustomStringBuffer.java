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

/**
 * A wrapper around a StringBuffer, which add useful extra behavior
 * 
 * @author mstevens
 * 
 */
public class CustomStringBuffer
{
	private StringBuffer buffer;

	public CustomStringBuffer()
	{
		buffer = new StringBuffer();
	}

	public CustomStringBuffer(int length)
	{
		buffer = new StringBuffer(length);
	}

	public void append(String string)
	{
		buffer.append(string);
	}

	public void appendTabbed(String string, int tabs)
	{
		buffer.append(StringUtils.addTabsFront(string, tabs));
	}

	public void appendLine(String string)
	{
		append(string);
		newLine();
	}

	public void appendTabbedLine(String string, int tabs)
	{
		appendLine(StringUtils.addTabsFront(string, tabs));
	}

	public void newLine()
	{
		append("\n");
	}

	public String toString()
	{
		return buffer.toString();
	}
}
