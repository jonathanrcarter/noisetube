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

public class NoiseTubeAccount
{

	private static final String sep = "#3";

	public String APIKey = "";
	public String username = "";
	public String password = "";

	public boolean isAvailable()
	{
		return !APIKey.equals("");
	}

	public static NoiseTubeAccount load(String db)
	{
		NoiseTubeAccount account = new NoiseTubeAccount();

		int idx_sep = db.indexOf(sep);
		account.APIKey = db.substring(0, idx_sep);

		String rest = db.substring(idx_sep + sep.length(), db.length());
		idx_sep = rest.indexOf(sep);
		account.username = rest.substring(0, idx_sep);

		rest = rest.substring(idx_sep + sep.length(), rest.length());
		account.password = rest;
		return account;
	}

	public String toString()
	{
		return APIKey + "#3" + username + "#3" + password;
	}

}
