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

package net.noisetube.ui.android;

import net.noisetube.R;
import net.noisetube.core.NTClient;
import net.noisetube.ui.android.MainActivity;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author sbarthol, mstevens
 *
 */
public class AboutActivity extends Activity
{

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aboutactivity);
		TextView version = (TextView) findViewById(R.id.versioninfo);
		version.setText(NTClient.getInstance().getClientType() + " " + NTClient.getInstance().getClientVersion() + "\nBuild date: " + NTClient.getInstance().getClientBuildDate());
		TextView abouttext = (TextView) findViewById(R.id.abouttext);
		abouttext.setText(
				Html.fromHtml(
						"<a href=\"http://www.noisetube.net\">NoiseTube Project</a> © 2008-2010 <a href=\"http://www.csl.sony.fr\">Sony CSL Paris</a><br/>" + 
						"Portions contributed by <a href=\"http://www.brussense.be\">BrusSense team, Vrije Universiteit Brussel</a>, 2008-2011.<br/><br/>" +
						"Android client developed by Sander Bartholomees and Matthias Stevens, on behalf of VUB-BrusSense.<br/><br/>" +
						"<a href=\"http://www.noisetube.net\">NoiseTube</a> is a research project without commercial goals.<br/>" + 
						"<a href=\"http://code.google.com/p/noisetube\">Source code available</a> under the terms of the <a href=\"http://www.gnu.org/licenses/lgpl-2.1.html\">GNU LGPL v2.1</a>.<br/><br/>" +
						"More information at: <a href=\"http://www.noisetube.net\">www.noisetube.net</a>"
				));
		abouttext.setMovementMethod(LinkMovementMethod.getInstance());
		Button btnOk = (Button) findViewById(R.id.btnAboutOK);
		btnOk.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0)
			{
				finish();
			}
		});
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		if(!isFinishing())
			MainActivity.getInstance().pause(); //to pause measuring if use wants that
	}

	@Override
	protected void onResume()
	{
		super.onPause();
		if(!isFinishing())
			MainActivity.getInstance().resume(); //to resume measuring
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
	
}
