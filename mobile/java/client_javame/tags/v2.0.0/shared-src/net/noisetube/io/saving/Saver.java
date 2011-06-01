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

package net.noisetube.io.saving;

import java.util.Enumeration;

import net.noisetube.core.IPausableService;
import net.noisetube.model.Measurement;
import net.noisetube.model.Track;
import net.noisetube.ui.ISaverUI;

/**
 * NoiseTube track and measurement saving class (abstract)
 * 
 * @author mstevens
 *
 */
public abstract class Saver implements IPausableService
{
	
	protected volatile boolean running = false;
	protected volatile boolean paused = false;
	private volatile String status;
	
	protected ISaverUI ui;
	protected Track track;	
	
	public Saver(Track track)
	{
		this.track = track;
	}
	
	/**
	 * @see noisetube.io.ISender#isRunning()
	 */
	public boolean isRunning()
	{
		return running;
	}
	
	public boolean isPaused()
	{
		return paused;
	}

	public abstract void save(Measurement measurement);
	
	protected abstract void enableBatchMode();
	
	public void saveBatch(Enumeration measurements)
	{
		enableBatchMode(); //!!! 
		while(measurements.hasMoreElements())
			save((Measurement) measurements.nextElement());
	}
	
	public void setUI(ISaverUI ui)
	{
		this.ui = ui;
	}
	
	protected void setStatus(String message)
	{
		status = message;
		if(ui != null)
			ui.updated(this, message);
	}

	/**
	 * @return the status
	 */
	public String getStatus()
	{
		return status;
	}

}
