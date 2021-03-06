/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Android version)
 *  
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011
 *  Android port by Vrije Universiteit Brussel (BrusSense team), 2010-2011
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

package net.noisetube.ui.android.tabs;

import net.noisetube.model.Measurement;
import net.noisetube.model.Track;

/**
 * Interface used to represent a Tab object (which is part of a Tabcontainer).
 * 
 * @author sbarthol, mstevens
 *
 */
public interface Tab
{

	public void update(Measurement newMeasurement, Track track);
	
	/**
	 * Used to inform the containing Tabcontainer if this tab needs to receive measurements even if it's not on top.
	 */
	public boolean mustRemainInformed();
	
	/**
	 * What to do when the engine is started (e.g. enable certain functionalities)
	 */
	
	public void start();
	
	/**
	 * What to do when the engine is stopped (e.g. clear screen)
	 */
	public void stop();

	public void refresh();
}
