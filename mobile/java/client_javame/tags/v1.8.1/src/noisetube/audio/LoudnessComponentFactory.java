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

package noisetube.audio;

import noisetube.MainMidlet;
import noisetube.audio.java.LoudnessComponentJava;
import noisetube.audio.jni.LoudnessComponentJNI;
import noisetube.config.Preferences;
import noisetube.config.WatchPreferences;
import noisetube.util.Logger;

/**
 * (Simple) Factory
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class LoudnessComponentFactory
{

	public static ILoudnessComponent getLoudnessComponent()
	{
		Logger log = Logger.getInstance();
		Preferences preferences = MainMidlet.getInstance().getPreferences();

		ILoudnessComponent component = null;

		// Watch implementation
		if(preferences instanceof WatchPreferences)
		{
			try
			{
				Class LoudnessCompClass = Class.forName("noisetube.watch.LoudnessComponentWatch");
				component = (ILoudnessComponent) LoudnessCompClass.newInstance();

			}
			catch(Exception e)
			{
				log.error(e, "Cannot create LoudnessComponentWatch instance");
				component = new LoudnessComponentJava(); // use default Java implementation
			}
		}

		// JNI implementation
		else if(preferences.isJniRecording())
			component = new LoudnessComponentJNI();

		// Default Java implementation
		else
			component = new LoudnessComponentJava();

		return component;
	}
}
