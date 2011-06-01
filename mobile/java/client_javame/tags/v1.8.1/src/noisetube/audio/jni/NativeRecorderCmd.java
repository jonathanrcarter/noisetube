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

import noisetube.util.Logger;

public class NativeRecorderCmd extends AbstractSocketCom
{

	Logger log = Logger.getInstance();

	public NativeRecorderCmd()
	{
		setPort(8112);
	}

	public void sendRecordCmd() throws IOException
	{
		sendCmd('r');
	}

	public void sendStopCmd() throws IOException
	{
		sendCmd('s');
	}

	private void sendCmd(char cmd) throws IOException
	{
		log.debug("sending cmd " + cmd);
		if(!connect())
		{
			throw new IOException("Cmd Server connection failed");
		}

		if(!sendRequest(cmd))
		{

			throw new IOException("sending cmd '" + cmd + "' failed");
		}
		close();
	}

	private boolean sendRequest(char cmd)
	{

		try
		{
			byte[] buf = ("" + cmd).getBytes();
			out.write(buf, 0, buf.length);
			out.flush();

			byte[] data = new byte[256]; // maybe use the buff instead of data[]
			int actualLength = in.read(data);

			String response = new String(data, 0, actualLength);
			log.debug("server response: " + response);
			return true;

		}
		catch(IOException ioe)
		{
			log.error("sending error: " + ioe.getMessage());
			return false;
		}
	}

}
