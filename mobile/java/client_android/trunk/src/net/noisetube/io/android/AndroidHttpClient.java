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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import net.noisetube.io.HttpClient;
import net.noisetube.io.IInputStreamReader;

/**
 * @author mstevens, sbarthol
 *
 */
public class AndroidHttpClient extends HttpClient
{
	
	private org.apache.http.client.HttpClient httpClient;
	
	/**
	 * @param agent
	 * 
	 * Note: setting the time-outs seems to cause A LOT more connection problems than without, so we don't use them (for now) 
	 */
	public AndroidHttpClient(String agent)
	{
		super(agent);
		
	    HttpParams httpParameters = new BasicHttpParams();
	    httpParameters.setParameter("http.useragent", agent);
	    //HttpConnectionParams.setConnectionTimeout(httpParameters, timeout); //Set the timeout in milliseconds until a connection is established
		//HttpConnectionParams.setSoTimeout(httpParameters, timeout); //Set the default socket timeout in milliseconds which is the timeout for waiting for data.
		//ConnManagerParams.setTimeout(httpParameters, timeout);
		
	    final SchemeRegistry registry = new SchemeRegistry();
	    registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	    //SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
		//sslSocketFactory.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
		//registry.register(new Scheme("https", sslSocketFactory, 443));
	    
	    final ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(httpParameters, registry);
	    httpClient = new DefaultHttpClient(manager, httpParameters);
	}

	/**
	 * Sending a POST Request
	 * 
	 * @param url
	 * @param json
	 * @throws Exception
	 */
	@Override
	public void postJSONRequest(String url, String json) throws Exception
	{
		HttpPost httpPost = null;
		try
		{
			httpPost = new HttpPost(url);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-Type", "application/json");
			httpPost.setHeader("User-Agent", agent);
			httpPost.setEntity(new StringEntity(json, HTTP.UTF_8));
			HttpResponse response = httpClient.execute(httpPost); //response contains the response message and the status code
			if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
				throw new IOException("HTTP response code: " + response.getStatusLine().getStatusCode());
		}
		catch(RuntimeException re)
		{
			if(httpPost != null)
				httpPost.abort(); //!!!
			throw new Exception("POST request (JSON) failed for URL: " + url, re);
		}
		catch(Exception e)
		{
			throw new Exception("HTTP POST request (JSON) failed " +
								(e instanceof HttpResponseException ?
									"(response code: " + ((HttpResponseException) e).getStatusCode() + ") " :
									"") +
									"for URL: " + url, e);
		}
	}
	
	/**
	 * Sends a GET request and processes the response with an IInputStreamProcessor
	 * 
	 * @param url
	 * @param reader to process the response with
	 * @throws Exception
	 */
	public void getRequest(String url, IInputStreamReader reader) throws Exception
	{
		HttpGet httpGet = null;
		HttpEntity entity = null;
		try
		{
			httpGet = new HttpGet(url);
			httpGet.setHeader("User-Agent", agent);
			HttpResponse response = httpClient.execute(httpGet);
			if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
				throw new IOException("HTTP response code: " + response.getStatusLine().getStatusCode());
			entity = response.getEntity();
			reader.read(entity.getContent()); //closes the stream
		}
		catch(RuntimeException re)
		{
			if(httpGet != null)
				httpGet.abort(); //!!!
			throw new Exception("HTTP GET request failed for URL: " + url, re);
		}
		catch(Exception e)
		{
			throw new Exception("HTTP GET request failed for URL: " + url, e);
		}
		finally
		{
			try
			{
				if(entity != null)
					entity.consumeContent(); //!!!
			}
			catch(Exception ignore) { }
		}
	}

	@Override
	protected InputStreamToStringReader getInputStreamToStringReader()
	{
		return new AndroidInputStreamToStringReader();
	}
	
	/**
	 * @author mstevens
	 *
	 */
	protected class AndroidInputStreamToStringReader extends InputStreamToStringReader
	{
		
		private String characterEncoding = null;

		public AndroidInputStreamToStringReader()
		{
		}
		
		public AndroidInputStreamToStringReader(String characterEncoding)
		{
			this.characterEncoding = characterEncoding;
		}
		
		/*
		* To convert the InputStream to String we use the
		* Reader.read(char[] buffer) method. We iterate until the
		* Reader return -1 which means there's no more data to
		* read. We use the StringWriter class to produce the string.
		*/
		public void read(InputStream inputStream) throws IOException
		{
			if(inputStream != null)
			{
				Writer writer = new StringWriter();
				char[] buffer = new char[512];
				try
				{
					Reader reader = new BufferedReader((characterEncoding == null ?
															new InputStreamReader(inputStream) :
															new InputStreamReader(inputStream, characterEncoding)),
														buffer.length);
					int n;
					while((n = reader.read(buffer)) != -1)
						writer.write(buffer, 0, n);
				}
				finally
				{
					inputStream.close(); //!!!
				}
				string = writer.toString();
			}
			else
			{        
				string = null;
			}
		}
		
	}

}
