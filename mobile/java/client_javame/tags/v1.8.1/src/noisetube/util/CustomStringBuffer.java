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
