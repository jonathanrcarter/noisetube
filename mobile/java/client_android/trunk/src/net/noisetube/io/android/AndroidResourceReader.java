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

package net.noisetube.io.android;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;

import net.noisetube.core.NTClient;
import net.noisetube.core.android.AndroidNTClient;
import net.noisetube.io.ResourceReader;


/**
 * @author sbarthol, mstevens
 *
 */
public class AndroidResourceReader extends ResourceReader
{

	public AndroidResourceReader(String path)
	{
		super(path);
	}

	@Override
	public InputStream getInputStream()
	{
		/*
		DisplayMetrics displayMetrics = new DisplayMetrics();
		MainActivity.getInstance().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		Configuration configuration = new Configuration();
		configuration.setToDefaults();
		 */
		AssetManager assetManager = ((AndroidNTClient) NTClient.getInstance()).getContextWrapper().getAssets();
		String cleanPath = path.startsWith("/") ? path.substring(1) : path;
		try {
			return assetManager.open(cleanPath);
		} catch (IOException e) {
			log.error(e, "AndroidResourceReader (IOException) for " + cleanPath);
			return null;
		}
	}

}
