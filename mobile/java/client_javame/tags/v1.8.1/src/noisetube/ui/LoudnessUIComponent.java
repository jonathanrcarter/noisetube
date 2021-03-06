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

package noisetube.ui;

//import noisetube.MainMidlet;
import noisetube.model.Measure;
//import noisetube.ui.graph.Graph;
//import noisetube.util.MemoryGraph;

import com.sun.lwuit.Container;
import com.sun.lwuit.Font;
import com.sun.lwuit.Label;
import com.sun.lwuit.layouts.BorderLayout;

/**
 * @author maisonneuve, mstevens
 * 
 */
public class LoudnessUIComponent extends Container
{

	private int graphCapacity = 50;
	private Label leqLabel;

	private LoudnessGraph graph;

	public LoudnessUIComponent()
	{
		super();

		setLayout(new BorderLayout());
		// setLayout(new FlowLayout());

		// decibel value l value
		leqLabel = new Label("00");
		leqLabel.getStyle().setFont(Font.getBitmapFont("decibel"));
		// size for 1xx db
		// label.setPreferredSize(new Dimension(50, 70));
		addComponent(BorderLayout.EAST, leqLabel);
		// addComponent(label);

		// graphical curve
		setGraph();

		getStyle().setMargin(5, 5, 0, 0);
		getStyle().setPadding(0, 0, 0, 0);
		// getStyle().setBorder(Border.createLineBorder(1));
		// getStyle().setBgTransparency(100);
	}

	private void setGraph()
	{
		/*if(MainMidlet.CLIENT_IS_TEST_VERSION)
			graph = new MemoryGraph(graphCapacity, MainMidlet.baseLevelMemoryUsage, MainMidlet.totalMemory);
		else
		{*/
			graph = new LoudnessGraph(graphCapacity);
			// g.setPreferredSize(new Dimension(300, 70));
			// g.getStyle().setPadding(0,0,80,10);
		//}
		addComponent(BorderLayout.CENTER, graph);

		graph.getStyle().setBgColor(0x00FF00);
	}

	public void reset()
	{
		removeComponent(graph);
		leqLabel.setText("00");
		leqLabel.getStyle().setFgColor(0xFFFFFF);
		leqLabel.getStyle().setBgColor(this.getStyle().getBgColor());
		setGraph();
	}

	public void updateUI(Measure m)
	{
		graph.push(m); //graph.addMeasure(m);
		double leqs = m.getLeq();
		// red
		if(leqs >= 80)
		{
			leqLabel.getStyle().setFgColor(0xFFFFFF);
			leqLabel.getStyle().setBgColor(0xCC0000);
		}
		else
		// yellow/orange
		if(leqs >= 70)
		{
			leqLabel.getStyle().setFgColor(0x000000);
			leqLabel.getStyle().setBgColor(0xFF6600);
		}
		else
		// yellow
		if(leqs >= 60)
		{
			leqLabel.getStyle().setFgColor(0x000000);
			leqLabel.getStyle().setBgColor(0xFFFF00);
		}
		else
		{
			// green
			leqLabel.getStyle().setFgColor(0x000000);
			leqLabel.getStyle().setBgColor(0x66FF00);
		}
		leqLabel.setText((int) leqs + "");
		graph.repaint();
	}

}
