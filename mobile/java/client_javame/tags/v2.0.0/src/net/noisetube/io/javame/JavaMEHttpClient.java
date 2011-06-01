/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Java ME version)
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

package net.noisetube.io.javame;

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.noisetube.io.HttpClient;
import net.noisetube.util.ErrorCallback;

/**
 *
 * @author maisonneuve, mstevens
 *
 */
public class JavaMEHttpClient extends HttpClient
{

	public JavaMEHttpClient(String agent)
	{
		super(agent);
	}

	/**
	 * @see net.noisetube.io.javame.HTTPClient#postJSONRequest(java.lang.String, java.lang.String)
	 */
	public void postJSONRequest(String url, String json) throws IOException
	{
		HttpConnection c = null;
		OutputStream os = null;
		try
		{
			//prepare connection
			c = (HttpConnection) Connector.open(url);
			c.setRequestMethod("POST");
			c.setRequestProperty("Content-Type", "application/json");
			//c.setRequestProperty("Accept","application/json");
			os = c.openOutputStream();
			os.write(json.getBytes());
			//send info
			int rc = c.getResponseCode();
			if(rc != HttpConnection.HTTP_OK)
			{
				throw new IOException("HTTP response code: " + rc);
			}
			//c.getResponseMessage();
		}
		catch(IOException e)
		{
			throw new IOException("Sending error for " + url + ": " + e.getMessage());
		}
		finally
		{
			closeOutputStream(os);
			closeConnection(c);
		}
	}
	
	public HttpInputStream getRequestInputStream(String url) throws IOException
	{
		try
		{
			HttpConnection c = (HttpConnection) Connector.open(url);
			c.setRequestMethod("GET");
			c.setRequestProperty("User-Agent", agent);
			if(c.getResponseCode() != HttpConnection.HTTP_OK)
				throw new IOException("HTTP response code: " + c.getResponseCode());
			return new JavaMEHttpInputStream(c);
		}
		catch(IOException e)
		{
			throw new IOException("HTTP GET request failed for URL " + url + ": " + e.getMessage());
		}
	}
	
	public void getRequestAsync(final String url, final ErrorCallback callback)
	{	
		new Thread(new Runnable()
		{
			public void run()
			{
				HttpConnection c = null;
				try
				{
					c = (HttpConnection) Connector.open(url);
					c.setRequestMethod("GET");
					c.setRequestProperty("User-Agent", agent);
					if(c.getResponseCode() != HttpConnection.HTTP_OK)
					{
						callback.error(new Exception("HTTP response code: " + c.getResponseCode()));
						return;
					}
				}
				catch(Exception e)
				{
					callback.error(new Exception("Sending error for " + url + ": " + e.getMessage()));
					return;
				}
				finally
				{
					closeConnection(c);
				}
			}
		}).start();
	}

	private void closeConnection(HttpConnection c)
	{
		if(c != null)
		{
			try
			{
				c.close();
			}
			catch(Exception ignore) { }
		}
	}
	
	protected class JavaMEHttpInputStream extends HttpInputStream
	{
		
		private HttpConnection connection;
		
		public JavaMEHttpInputStream(HttpConnection connection) throws IOException
		{
			super(connection.openInputStream());
			this.connection = connection;
		}
        
		public void closeConnection()
		{
			JavaMEHttpClient.this.closeConnection(connection);
		}	
		
	}

}
