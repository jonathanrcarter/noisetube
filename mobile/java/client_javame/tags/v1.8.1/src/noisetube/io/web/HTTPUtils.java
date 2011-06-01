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

package noisetube.io.web;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import noisetube.MainMidlet;
import noisetube.io.SaveException;
import noisetube.util.ErrorCallback;
import noisetube.util.Logger;

public class HTTPUtils
{

	static final String AGENT = MainMidlet.CLIENT_TYPE + "/" + MainMidlet.CLIENT_VERSION; //"NoiseTubeClient/1.x.y";

	static Logger log = Logger.getInstance();

	public static void main(String[] args) throws Exception
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getDefault()); //use system time zone
		System.out.println(TimeZone.getDefault().getID());
		cal.setTime(new Date());
		Date d = new Date();

		long time_offset = TimeZone.getDefault().getRawOffset();

		System.out.println(time_offset);
		System.out.println(System.currentTimeMillis());
		Date d2 = new Date(d.getTime() + time_offset);

		System.out.println(d2);
		/*
		 * Cache b = new Cache(); b.add_measure(new Measure(12));
		 * b.add_measure(new Measure(13)); b.add_measure(new Measure(14));
		 * 
		 * System.out.println(b.toJSON()); Stringurl=
		 * "http://www.noisetube.net/api/exposure/batch?key=6ba11c291138bc630ae9dfa1378214d5c2262ec2"
		 * ; post_json_request(url,b.toJSON());
		 */
	}

	/**
	 * Sending a POST Request
	 * 
	 * @param url
	 * @param json
	 * @throws IOException
	 */
	public static void post_json_request(String url, String json)
			throws SaveException
	{
		HttpConnection c = null;
		OutputStream os = null;
		try
		{
			// prepare connection
			c = (HttpConnection) Connector.open(url);

			c.setRequestMethod("POST");
			c.setRequestProperty("Content-Type", "application/json");
			// c.setRequestProperty("Accept","application/json");
			os = c.openOutputStream();
			os.write(json.getBytes());

			// send info
			int rc = c.getResponseCode();
			if(rc != HttpConnection.HTTP_OK)
			{
				throw new SaveException("HTTP response code: " + rc);
			}
			c.getResponseMessage();
		}
		catch(IOException e)
		{
			throw new SaveException("sending error for " + url + ": "
					+ e.getMessage());
		}
		finally
		{
			closeOutputStream(os);
			closeConnection(c);
		}
	}

	/**
	 * Sending a GET request
	 * 
	 * @param url
	 * @return
	 * @throws SaveException
	 */
	public static void get_request_async(final String url,
			final ErrorCallback callback)
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				HttpConnection c = null;
				DataInputStream dis = null;
				try
				{
					// log.debug(url);
					// prepare connection
					c = (HttpConnection) Connector.open(url);
					c.setRequestMethod("GET");
					c.setRequestProperty("User-Agent", AGENT);

					// send info
					int rc = c.getResponseCode();
					if(rc != HttpConnection.HTTP_OK)
					{
						callback.error(new SaveException("HTTP response code: "
								+ rc));
						return;
					}
					StringBuffer responseMessage = new StringBuffer();
					dis = new DataInputStream(c.openInputStream());
					// retrieve the response from the server
					int ch;
					while((ch = dis.read()) != -1)
					{
						responseMessage.append((char) ch);
					}
				}
				catch(Exception e)
				{
					callback.error(new SaveException("sending error for " + url
							+ ": " + e.getMessage()));
					return;
				}
				finally
				{
					closeInputStream(dis);
					closeConnection(c);
				}
			}
		}).start();
	}

	/**
	 * Sending a GET request
	 * 
	 * @param url
	 * @return
	 * @throws SaveException
	 */
	public static String get_request(String url) throws SaveException
	{
		HttpConnection c = null;
		DataInputStream dis = null;
		try
		{
			// log.debug("GET request: " + url);
			// prepare connection
			c = (HttpConnection) Connector.open(url);
			c.setRequestMethod("GET");
			c.setRequestProperty("User-Agent", AGENT);

			// send info
			int rc = c.getResponseCode();
			if(rc != HttpConnection.HTTP_OK)
			{
				throw new SaveException("HTTP response code: " + rc);
			}

			StringBuffer responseMessage = new StringBuffer();
			dis = new DataInputStream(c.openInputStream());
			// retrieve the response from the server
			int ch;
			while((ch = dis.read()) != -1)
			{
				responseMessage.append((char) ch);
			}
			return responseMessage.toString();
		}
		catch(Exception e)
		{
			throw new SaveException("Sending error for " + url + ": "
					+ e.getMessage());
		}
		finally
		{
			closeInputStream(dis);
			closeConnection(c);
		}
	}

	private static void closeOutputStream(OutputStream os)
	{
		if(os != null)
		{
			try
			{
				os.close();
			}
			catch(Exception ex)
			{
			}
			finally
			{
				os = null;
				System.gc();
			}
		}
	}

	private static void closeInputStream(InputStream is)
	{
		if(is != null)
		{
			try
			{
				is.close();
			}
			catch(Exception ex)
			{
			}
			finally
			{
				is = null;
				System.gc();
			}
		}
	}

	private static void closeConnection(HttpConnection c)
	{
		if(c != null)
		{
			try
			{
				c.close();
			}
			catch(Exception ex)
			{
			}
			finally
			{
				c = null;
				System.gc();
			}
		}
	}
}
