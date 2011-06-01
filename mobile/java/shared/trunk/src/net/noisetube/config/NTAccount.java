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

package net.noisetube.config;

import net.noisetube.util.StringUtils;

public class NTAccount
{

	private static final char separator = '%';
	
	private String APIKey;
	private String username;
	//private String password;

	/**
	 * @param username
	 * @param aPIKey
	 */
	public NTAccount(String username, String APIKey)
	{
		this.username = username;
		this.APIKey = APIKey;
	}

	/**
	 * @return the aPIKey
	 */
	public String getAPIKey()
	{
		return APIKey;
	}

	/**
	 * @return the username
	 */
	public String getUsername()
	{
		return username;
	}
	
	public String serialise()
	{
		return username + separator + APIKey;
	}
	
	public static NTAccount deserialise(String serialisedAccount)
	{
		if(serialisedAccount == null || serialisedAccount.equals("") || serialisedAccount.indexOf(separator) == -1)
			throw new IllegalArgumentException("serialisedAccount is invalid");
		String[] tokens = StringUtils.split(serialisedAccount, separator);
		return new NTAccount(tokens[0], tokens[1]);
	}

}
