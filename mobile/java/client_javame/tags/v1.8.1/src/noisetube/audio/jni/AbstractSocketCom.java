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

package noisetube.audio.jni;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import noisetube.util.Logger;

public abstract class AbstractSocketCom
{

	Logger log = Logger.getInstance();

	final int MAX_RETRY = 3;

	protected String url = "socket://127.0.0.1:";

	int port;

	protected SocketConnection conn;
	protected OutputStream out;
	protected InputStream in;

	protected void setPort(int port)
	{
		this.port = port;
	}

	protected boolean connect()
	{

		boolean error = true;
		int retry = 0;
		while(error && (retry < MAX_RETRY))
		{
			try
			{
				conn = (SocketConnection) Connector.open(url + port,
						Connector.READ_WRITE, true);
				out = conn.openOutputStream();

				in = conn.openInputStream();
				error = false;
			}
			catch(IOException e)
			{
				retry++;
				e.printStackTrace();
				try
				{
					// maybe useless due to the timeout
					Thread.sleep(100);
				}
				catch(InterruptedException e1)
				{
					e1.printStackTrace();
				}
			}

		}
		return !error;
	}

	protected void close()
	{
		try
		{
			if(out != null)
				out.close();
			if(in != null)
				in.close();
			if(conn != null)
				conn.close();
		}
		catch(IOException e)
		{
		}
	}
}
