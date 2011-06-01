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

package noisetube.tagging;

import noisetube.ui.NotesUIComponent;
import noisetube.util.Logger;

public class NotesComponent implements TagListener
{

	String tag;
	int rating;
	String comments;

	NotesUIComponent ui;
	public boolean ready = true;

	Logger log = Logger.getInstance();

	public boolean hasNote()
	{
		return (tag != null) && (!"".equals(tag));
	}

	public String getNote()
	{
		return tag;
	}

	public boolean isReady()
	{
		return ready;
	}

	public void setUIComponent(NotesUIComponent ui)
	{
		this.ui = ui;
		ui.setNote(this);
	}

	public void setTags(String text)
	{
		tag = text;
	}

	public void edit()
	{
		clear();
		ui.clear();
	}

	public void clear()
	{
		tag = null;
		rating = -1;
		comments = null;
	}

	public void sendTag(String name)
	{
		setTags(name);
	}
}
