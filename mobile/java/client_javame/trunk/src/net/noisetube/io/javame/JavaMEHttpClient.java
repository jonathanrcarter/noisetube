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
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.noisetube.io.HttpClient;
import net.noisetube.io.IInputStreamReader;

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
			c = (HttpConnection) Connector.open(url, Connector.READ_WRITE, true);
			c.setRequestMethod("POST");
			c.setRequestProperty("User-Agent", agent);
			c.setRequestProperty("Content-Type", "application/json");
			//c.setRequestProperty("Accept", "application/json");
			os = c.openOutputStream();
			os.write(json.getBytes());
			//send info
			int rc = HttpConnectionHelper.getResponseCode(c, timeout); //rc = c.getResponseCode();
			if(rc != HttpConnection.HTTP_OK)
				throw new IOException("HTTP response code: " + rc);
		}
		catch(IOException e)
		{
			throw new IOException("Sending error for " + url + ": " + e.getMessage());
		}
		finally
		{
			if(os != null)
			{
				try
				{
					os.close();
				}
				catch(Exception ex) { }
			}
			closeConnection(c);
		}
	}
	
	public void getRequest(String url, IInputStreamReader reader) throws Exception
	{
		HttpConnection c = null;
		try
		{
			c = (HttpConnection) Connector.open(url, Connector.READ_WRITE, true);
			c.setRequestMethod("GET");
			c.setRequestProperty("User-Agent", agent);
			int rc = HttpConnectionHelper.getResponseCode(c, timeout); //rc = c.getResponseCode();
			if(rc != HttpConnection.HTTP_OK)
				throw new IOException("HTTP response code: " + rc);
			reader.read(HttpConnectionHelper.openInputStream(c, timeout)); //closes the stream
		}
		catch(IOException e)
		{
			throw new Exception("HTTP GET request failed for URL " + url + ": " + e.getMessage());
		}
		finally
		{
			closeConnection(c);
		}
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
	
	/**
	 * Avoid hanging when server is down or not responding
	 *
	 * @author mstevens
	 */
	private static class HttpConnectionHelper extends Thread
	{
		
		private static final int TASK_GET_RESPONSE_CODE = 1;
		private static final int TASK_OPEN_INPUT_STREAM = 2;
		
		private HttpConnection httpConnection;
		private int task;
		
		//Return values:
		private Integer responseCode = null;
		private InputStream inputStream = null;
		
		//Possible exception:
		private IOException ioException = null;
		
		public HttpConnectionHelper(HttpConnection httpConnection, int task)
		{
			this.httpConnection = httpConnection;
			this.task = task;
		}
		
		public static int getResponseCode(HttpConnection httpConnection, int timeout) throws IOException
		{
			HttpConnectionHelper httpThread = new HttpConnectionHelper(httpConnection, TASK_GET_RESPONSE_CODE);
			httpThread.start(); //async
			httpThread.block(timeout); //blocks
			if(httpThread.responseCode == null)
			{
				if(httpThread.isAlive())
					httpThread.interrupt();
				throw new IOException("Time-out reached");
			}
			else
				return httpThread.responseCode.intValue();
		}
		
		public static InputStream openInputStream(HttpConnection httpConnection, int timeout) throws IOException
		{
			HttpConnectionHelper httpThread = new HttpConnectionHelper(httpConnection, TASK_OPEN_INPUT_STREAM);
			httpThread.start(); //async
			httpThread.block(timeout); //blocks
			if(httpThread.inputStream == null)
			{
				if(httpThread.isAlive())
					httpThread.interrupt();
				throw new IOException("Time-out reached");
			}
			else
				return httpThread.inputStream;
		}
		
		private synchronized void block(int timeout) throws IOException
		{
			try
			{
				this.wait(timeout);
			}
			catch(Exception ignore) { }
			if(ioException != null)
				throw ioException;
		}

		public synchronized void run()
		{
			try
			{
				if(task == TASK_GET_RESPONSE_CODE)
					responseCode = new Integer(httpConnection.getResponseCode());
				else if(task == TASK_OPEN_INPUT_STREAM)
					inputStream = httpConnection.openInputStream();
			}
			catch(Exception e)
			{
				if(e instanceof IOException)
					ioException = (IOException) e;
			}
			finally
			{
				notifyAll(); //wake up waiting thread
			}
		}
		
	}

	protected InputStreamToStringReader getInputStreamToStringReader()
	{
		return new JavaMEInputStreamToStringReader();
	}
	
	/**
	 * @author mstevens
	 *
	 */
	protected class JavaMEInputStreamToStringReader extends InputStreamToStringReader
	{

		public void read(InputStream inputStream) throws IOException
		{
			if(inputStream != null)
			{
				try
				{
					StringBuffer responseMessage = new StringBuffer();
					int ch;
					while((ch = inputStream.read()) != -1)
						responseMessage.append((char) ch);
					string = responseMessage.toString();
				}
				finally
				{
					inputStream.close(); //!!!
				}
			}
			else
			{        
				string = null;
			}
		}
		
	}

}
