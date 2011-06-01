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

import net.noisetube.R;
import net.noisetube.model.Measurement;
import net.noisetube.model.Track;

import android.app.Activity;
import android.os.Bundle;


/**
 * Dummy tab set as primary tab when NoiseTube is loading
 * 
 * @author sbarthol, mstevens
 *
 */
public class TabLoadingActivity extends Activity implements Tab
{
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splashscreen); //we reuse the layout of the splash screen
	}

	public void update(Measurement newMeasurement, Track track)
	{
		//Do nothing (this is a dummy tab)
	}

	public boolean mustRemainInformed()
	{
		return false;
	}

	public void start()
	{
		//Do nothing (this is a dummy tab)
	}

	public void stop()
	{
		//Do nothing (this is a dummy tab)
	}

	public void refresh()
	{
		//Do nothing (this is a dummy tab)
	}

}
