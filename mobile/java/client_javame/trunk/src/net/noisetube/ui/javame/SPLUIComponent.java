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

package net.noisetube.ui.javame;

import net.noisetube.model.Measurement;
import net.noisetube.model.SoundLevelScale;
import net.noisetube.model.Track;
import net.noisetube.util.MathME;

import com.sun.lwuit.Container;
import com.sun.lwuit.Font;
import com.sun.lwuit.Label;
import com.sun.lwuit.layouts.BorderLayout;

/**
 * Sound Pressure Level GUI
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class SPLUIComponent extends Container
{

	private Label leqLabel;
	
	private Font dbFontDefault;
	private Font dbFontSmaller = Fonts.LARGE_FONT;
	private SPLGraphContainer graph;

	public SPLUIComponent()
	{
		super();

		setLayout(new BorderLayout());

		dbFontDefault = Font.getBitmapFont("decibel");
		leqLabel = new Label();
		leqLabel.setPreferredW(dbFontDefault.stringWidth("00") + 8); //size for xx, with 4px margin on either side
		leqLabel.getStyle().setFont(dbFontDefault);
		leqLabel.setAlignment(Label.CENTER);
		
		addComponent(BorderLayout.EAST, leqLabel);
		
		//graphical curve
		setGraph();

		getStyle().setMargin(2, 2, 2, 2);
		getStyle().setPadding(0, 0, 0, 0);
	}

	private void setGraph()
	{
		graph = new SPLGraphContainer();
		addComponent(BorderLayout.CENTER, graph);
		graph.getStyle().setBgColor(0x00FF00);
	}

	public void reset()
	{
		removeComponent(graph);
		leqLabel.setText("");
		leqLabel.getStyle().setFgColor(0xFFFFFF);
		leqLabel.getStyle().setBgColor(this.getStyle().getBgColor());
		setGraph();
	}
	
	private double previousDB = 0; //to only switch fonts when needed
	
	/*
	 * The font swithcing hack is to work around a bug in LWUIT v1.4 which makes the GUI hang when
	 * the String passed to Label.setText is too long to fit on the label using the current style/font.
	 * Dynamic resizing of the label does not seem to work so we work around the bug by switching to a
	 * smaller font when db >= 100.
	 * 
	 * Bug info: http://www.java.net/forum/topic/mobile-embedded/lwuit/maybe-new-bug-lwuit-14-when-string-too-long
	 * 
	 * TODO remove this font-switching nonsense if this bug is fixed in a new LWUIT version
	 */
	public void update(Track track, Measurement measurement)
	{
		graph.getSoundLevelGraph().update(track);
		double db = measurement.isLeqDBASet() ? measurement.getLeqDBA() : measurement.getLeqDB();
		if(db >= 80)
			leqLabel.getStyle().setFgColor(0xFFFFFF);
		else
			leqLabel.getStyle().setFgColor(0x000000);
		leqLabel.getStyle().setBgColor(SoundLevelScale.getColor(db).getRGBValue());
		if(db >= 100 && previousDB < 100)
			leqLabel.getStyle().setFont(dbFontSmaller);
		else if(db < 100 && previousDB >= 100)
			leqLabel.getStyle().setFont(dbFontDefault);
		previousDB = db;
		leqLabel.setText(Integer.toString((int) MathME.round(db)));
		graph.repaint(); //!!!
	}

}
