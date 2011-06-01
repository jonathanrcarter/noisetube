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

package net.noisetube.io;

import java.io.IOException;
import java.io.InputStream;

import net.noisetube.util.ErrorCallback;
import net.noisetube.util.Logger;

/**
 * @author mstevens
 *
 */
public abstract class HttpClient
{
	
	protected static final int DEFAULT_TIME_OUT_MS = 6000;
	
	protected Logger log = Logger.getInstance();
	
	protected String agent;
	protected int timeout;
	InputStreamToStringReader stringReader; 
	
	public HttpClient(String agent)
	{
		this.agent = agent;
		this.timeout = DEFAULT_TIME_OUT_MS;
		this.stringReader = getInputStreamToStringReader();
	}

	/**
	 * Sending a POST Request with JSON content
	 * 
	 * @param url
	 * @param json
	 * @throws Exception
	 */
	public abstract void postJSONRequest(String url, String json) throws Exception;
	
	/**
	 * Sending an asynchronous GET request
	 * 
	 * @param url
	 */
	public void getRequestAsync(final String url, final String purpose, final ErrorCallback callback)
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					getRequest(url);
				}
				catch(Exception e)
				{
					callback.error(e, "Asynchronous getRequest failed (purpose: " + purpose + ")");
					return;
				}
			}
		}).start();
	}

	/**
	 * Sends a GET request and returns the response as a string
	 * 
	 * @param url
	 * @return response
	 * @throws Exception
	 */
	public String getRequest(String url) throws Exception
	{
		getRequest(url, stringReader);
		return stringReader.getAndClearString();
	}
	
	protected abstract InputStreamToStringReader getInputStreamToStringReader();
	
	/**
	 * Sends a GET request and processes the response with an IInputStreamProcessor
	 * 
	 * @param url
	 * @param reader to process the response with
	 * @throws Exception
	 */
	public abstract void getRequest(String url, IInputStreamReader reader) throws Exception;
	
	/**
	 * @author mstevens
	 *
	 */
	protected abstract class InputStreamToStringReader implements IInputStreamReader
	{
		
		protected String string;
		
		public abstract void read(InputStream inputStream) throws IOException;
		
		public String getAndClearString()
		{
			String tmp = string;
			string = null;
			return tmp;
		}
		
	}
	
}