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
import java.util.Vector;

import net.noisetube.model.Measurement;
import net.noisetube.model.Track;
import net.noisetube.ui.ISaverUI;
import net.noisetube.util.CustomStringBuffer;

/**
 * Multiplexing saver class
 * 
 * @author mstevens
 * 
 */
public class MultiSaver extends Saver implements ISaverUI
{

	private Vector savers;

	public MultiSaver(Track track)
	{
		super(track);
		savers = new Vector();
	}

	public void addSaver(Saver saver)
	{
		savers.addElement(saver);
		saver.setUI(this);
	}

	/**
	 * @see net.noisetube.io.saving.Saver#save(net.noisetube.model.Measurement)
	 */
	public void save(Measurement measurement)
	{
		if(running && !paused && measurement != null)
		{
			Enumeration saverEnum = savers.elements();
			while(saverEnum.hasMoreElements())
				((Saver) saverEnum.nextElement()).save(measurement);
		}
	}

	/**
	 * @see net.noisetube.core.IService#start()
	 */
	public void start()
	{
		if(!running)
		{
			Enumeration saverEnum = savers.elements();
			while(saverEnum.hasMoreElements())
				((Saver) saverEnum.nextElement()).start();
			running = true;
			paused = false;
		}
	}
	
	public void pause()
	{
		if(running)
		{
			Enumeration saverEnum = savers.elements();
			while(saverEnum.hasMoreElements())
				((Saver) saverEnum.nextElement()).pause();
			paused = true;
		}
	}
	
	public void resume()
	{
		if(paused)
		{
			Enumeration saverEnum = savers.elements();
			while(saverEnum.hasMoreElements())
				((Saver) saverEnum.nextElement()).resume();
			paused = false; //resume from pause
		}
	}

	/**
	 * @see net.noisetube.core.IService#stop()
	 */
	public void stop()
	{
		if(running)
		{
			running = false;
			paused = false;
			Enumeration saverEnum = savers.elements();
			while(saverEnum.hasMoreElements())
				((Saver) saverEnum.nextElement()).stop();
		}
	}
	
	/**
	 * @see noisetube.io.ISender#isRunning()
	 */
	public boolean isRunning()
	{
		if(this.running)
			return true;
		else //if multisaver not running, check if underlying servers are also stopped
		{
			Enumeration saverEnum = savers.elements();
			while(saverEnum.hasMoreElements())
				if(((Saver) saverEnum.nextElement()).isRunning())
					return true; //one saver running -> multisaver is running
			return false;
		}
	}
	
	public void enableBatchMode()
	{
		if(running)
		{
			Enumeration saverEnum = savers.elements();
			while(saverEnum.hasMoreElements())
				((Saver) saverEnum.nextElement()).enableBatchMode();
		}
	}
	
	public String getStatus()
	{
		CustomStringBuffer bff = new CustomStringBuffer();
		Enumeration saverEnum = savers.elements();
		while(saverEnum.hasMoreElements())
			bff.appendLine(" - " + ((Saver) saverEnum.nextElement()).getStatus());
		return bff.toString();
	}

	public void updated(Saver saver, String message)
	{
		if(ui != null)
			ui.updated(this, getStatus());		
	}

}
