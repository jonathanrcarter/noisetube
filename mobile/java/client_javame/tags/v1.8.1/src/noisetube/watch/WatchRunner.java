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

package noisetube.watch;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.location.Coordinates;

import noisetube.audio.ILeqListener;
import noisetube.config.WatchPreferences;
import noisetube.util.Logger;

/**
 * @author maisonneuve
 * 
 */
public class WatchRunner implements Runnable
{

	// STATIC---------------------------------------------------------
	private static WatchRunner instance;

	public static WatchRunner getInstance()
	{
		if(instance == null)
		{
			instance = new WatchRunner();
		}
		return instance;
	}

	// DYNAMIC--------------------------------------------------------
	private Logger log = Logger.getInstance();
	private volatile boolean running = false;
	private boolean initialized;
	private WatchDataFrame data = new WatchDataFrame();
	private ILeqListener leqListener;
	private WatchPreferences preferences;

	private WatchRunner()
	{
		// ...
	}

	public void start()
	{
		if(!running)
		{
			running = true;
			new Thread(this).start();
		}
	}

	public void stop()
	{
		if(running)
		{
			log.debug("Disconnecting from Watch");
			running = false;
		}
	}

	/**
	 * @return the running
	 */
	public boolean isRunning()
	{
		return running;
	}

	public boolean initialized()
	{
		return initialized;
	}

	/**
	 * @return the lastLoudness
	 */
	public float getLastLoudness()
	{
		if(initialized)
		{
			return data.getLoudness();
		}
		else
		{
			return 0;
		}
	}

	/**
	 * @return the lastCoordinates
	 */
	public Coordinates getLastCoordinates()
	{
		if(initialized)
			return data.getCoordinates();
		else
			return null;
	}

	public void run()
	{
		initialized = false;
		InputStream is = null;

		// String url = "btspp://" + preferences.getWatchAddress() +
		// ":01;authenticate=false;encrypt=false;master=false";
		String url = "btspp://" + preferences.getWatchAddress() + ":01";
		log.debug("Connecting to Bluetooth url: " + url);

		try
		{
			StreamConnection connection = (StreamConnection) Connector
					.open(url);
			is = connection.openInputStream();
			log.debug("Connected to watch " + is);

			// send(REQUEST_CHAR);
			while(running)
			{

				// wait next frame
				while(is.read() != 0x02)
				{
				}

				// be sure that is a frame
				if(is.read() == 0x031)
				{
					// System.out.println("new frame ");
				}

				// according to the type of frame
				switch(is.read())
				{

				/**
				 * 
				 * !! Limited capture of the information !! Only GPS and
				 * Loudness not battery and ozone
				 */
				// GPS
				case 0x033:

					// decode GPS info
					byte[] gps = new byte[26];
					is.read(gps);
					handle_gps_frame(new String(gps));
					break;

				// loudness
				case 0x035:
					byte[] loudness = new byte[3];
					is.read(loudness);
					if(!initialized)
					{
						initialized = true;
					}

					handle_loudness_frame(new String(loudness));

					break;
				}
			}
			// }
			is.close();
			initialized = false;

		}
		catch(IOException e)
		{
			running = false;
			log.error(e, "watch error");
			initialized = false;
		}

	}

	private void handle_gps_frame(String gps)
	{
		// valid GPS
		if(gps.charAt(0) == 'V')
		{
			float latitude = Float.parseFloat(gps.substring(2, 11)) / 100.0f;
			float longitude = Float.parseFloat(gps.substring(14, 24)) / 100.0f;
			data.setLocation(latitude, longitude);
		} // not valid GPS
		else
		{
			// System.out.println("GPS not valid");
		}
	}

	private void handle_loudness_frame(String loudness_s)
	{
		float loudness = Float.parseFloat(loudness_s);
		// not usefull
		data.setLoudness(loudness);

		this.leqListener.sendLeq(loudness);
		// System.out.println("Loudness " + loudness);
	}

	public void setLeqListener(ILeqListener listener)
	{
		this.leqListener = listener;
	}

	public void setPreferences(WatchPreferences preferences)
	{
		this.preferences = preferences;
	}
}
