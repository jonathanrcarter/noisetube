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
import java.io.OutputStream;

import net.noisetube.util.ErrorCallback;
import net.noisetube.util.Logger;

/**
 * @author mstevens
 *
 */
public abstract class HttpClient
{
	
	protected static Logger log = Logger.getInstance();
	
	protected String agent;
	
	public HttpClient(String agent)
	{
		this.agent = agent;
	}

	/**
	 * Sending a POST Request with JSON content
	 * 
	 * @param url
	 * @param json
	 * @throws Exception
	 */
	public abstract void postJSONRequest(String url, String json) throws IOException;
	
	/**
	 * Sending an asynchronous GET request
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public abstract void getRequestAsync(final String url, final ErrorCallback callback);

	/**
	 * Sending a GET request
	 * 
	 * @param url
	 * @return response
	 * @throws Exception
	 */
	public String getRequest(String url) throws IOException
	{
		InputStream is = getRequestInputStream(url);
		try
		{
			StringBuffer responseMessage = new StringBuffer();
			int ch;
			while((ch = is.read()) != -1)
				responseMessage.append((char) ch);
			return responseMessage.toString();
		}
		catch(Exception e)
		{
			throw new IOException("Error reading response for URL " + url + ": " + e.getMessage());
		}
		finally
		{
			closeInputStream(is);
		}
	}
	
	/**
	 * Get InputStream from GET request
	 * 
	 * @param url
	 * @return InputStream
	 * @throws Exception
	 */
	public abstract HttpInputStream getRequestInputStream(String url) throws IOException;
	
	protected void closeOutputStream(OutputStream os)
	{
		if(os != null)
		{
			try
			{
				os.close();
			}
			catch(Exception ex) { }
		}
	}

	protected void closeInputStream(InputStream is)
	{
		if(is != null)
		{
			try
			{
				is.close();
			}
			catch(Exception ex) { }
		}
	}
	
	/**
	 * @author mstevens
	 *
	 */
	protected abstract class HttpInputStream extends InputStream
	{
		
		private InputStream wrappedStream;
		
		public HttpInputStream(InputStream wrappedStream) throws IOException
		{
			this.wrappedStream = wrappedStream;
		}

		public int available() throws IOException
		{
			return wrappedStream.available();
		}
        
		public void close() throws IOException
		{
			try
			{
				wrappedStream.close();
			}
			finally
			{
				closeConnection();
			}
		}
        
		protected abstract void closeConnection();
		
		public void mark(int readlimit)
		{
			wrappedStream.mark(readlimit);
		}
        
		public boolean markSupported()
		{
			return wrappedStream.markSupported();
		}
        
		public int read() throws IOException
		{
			return wrappedStream.read(); 
		}
        
		public int read(byte[] b) throws IOException
		{
			return wrappedStream.read(b);
		}
        
		public int read(byte[] b, int off, int len) throws IOException
		{
			return wrappedStream.read(b, off, len);
		}
        
		public void reset() throws IOException
		{
			wrappedStream.reset();
		}
        
		public long skip(long n) throws IOException
		{
			return wrappedStream.skip(n);
		}		
		
	}
	
}