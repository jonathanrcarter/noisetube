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

package noisetube.io;

import noisetube.MainMidlet;
import noisetube.config.Preferences;
import noisetube.model.Measure;
import noisetube.util.IService;
import noisetube.util.Logger;

public abstract class Saver implements IService
{

	protected MainMidlet midlet;
	protected Preferences preferences;
	protected Logger log = Logger.getInstance();

	protected volatile boolean running = false;

	public Saver()
	{
		this.midlet = MainMidlet.getInstance();
		this.preferences = midlet.getPreferences();
	}

	/**
	 * @see noisetube.io.ISender#isRunning()
	 */
	public boolean isRunning()
	{
		return running;
	}

	public abstract void save(Measure measure);

	protected void setMessage(String msg)
	{
		midlet.getMeasureForm().getLog_ui().setSecondaryMessage(msg);
	}

	protected void clearMessage()
	{
		midlet.getMeasureForm().getLog_ui().setSecondaryMessage("");
	}

}
