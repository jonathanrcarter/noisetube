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

package noisetube.config;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

import noisetube.MainMidlet;

public class WatchPreferences extends Preferences
{

	private boolean useWatch;

	private String watchAddress = "000780894889"; // "00078089487F";

	public WatchPreferences()
	{
		super();
	}

	public String getWatchAddress()
	{
		return watchAddress;
	}

	public void setWatchAddress(String watchAddress)
	{
		this.watchAddress = watchAddress;
	}

	/**
	 * @return the useWatch
	 */
	public boolean isUseWatch()
	{
		return useWatch;
	}

	/**
	 * @param useWatch
	 *            the useWatch to set
	 */
	public void setUseWatch(boolean useWatch)
	{
		this.useWatch = useWatch;
	}

	protected void _save(DataOutput dos) throws IOException
	{
		dos.writeBoolean(isUseWatch());
		dos.writeUTF(getWatchAddress());
		super._save(dos);
	}

	protected void _read(DataInputStream dis) throws IOException
	{
		setUseWatch(dis.readBoolean());
		setWatchAddress(dis.readUTF());
		super._read(dis);
	}

	public void find_client_name()
	{
		if(isUseWatch())
			MainMidlet.CLIENT_TYPE += "_Watch";
		Device.DEVICE_BRAND_NAME = "Watch";
		super.find_client_name();
	}
}
