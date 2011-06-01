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

import net.noisetube.core.javame.MainMIDlet;
import net.noisetube.tagging.TaggingComponent;

import com.sun.lwuit.ComboBox;
import com.sun.lwuit.Container;
import com.sun.lwuit.Label;
import com.sun.lwuit.List;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.list.ListModel;

/**
 * Source of noise
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class TaggingUIComponent extends Container
{

	private TextField txtTags = new TextField();
	private Label lblInfo = new Label("Tag the sound");

	private TaggingComponent taggingComp;
	private ActionListener action;
	private List list = new List();
	private ListModel m;

	public TaggingUIComponent()
	{
		super();
		lblInfo.setPreferredW(200);
		m = list.getModel();
		addComponent(lblInfo);
		Label lblExamples = new Label("(bird, klaxon, loud)");
		lblExamples.getStyle().setFont(Fonts.SMALL_FONT);
		addComponent(lblExamples);
		addComponent(txtTags);
		//Suggestions:
		addTag("[suggestions]");
		addTag("traffic");
		addTag("construction");
		addTag("neighbors");
		addTag("pets");
		addTag("aircraft");
		addTag("industry");
		addTag("annoying");
		addTag("pleasant");
		addTag("loud");

		setPreferredSize(new Dimension(170, 200));
		action = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				fire(txtTags.getText());
			}
		};

		txtTags.setMaxSize(100);
		txtTags.setEnabled(false);
		txtTags.addActionListener(action);
		ComboBox sources_box = new ComboBox();
		sources_box.setModel(m);
		sources_box.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				// not the [suggestion] item
				if(m.getSelectedIndex() > 0)
				{
					fire((String) m.getItemAt(m.getSelectedIndex()));
				}
			}
		});
		addComponent(sources_box);
	}

	private boolean contains(String tag)
	{
		for(int i = 0; i < m.getSize(); i++)
		{
			if(m.getItemAt(i).equals(tag))
				return true;
		}
		return false;
	}

	private void addTag(String name)
	{
		if(!contains(name))
			m.addItem(name);
	}

	private void fire(String tagstring)
	{
		if(MainMIDlet.getInstance().getEngine().isRunning() && tagstring != null && !tagstring.equals(""))
		{
			taggingComp.addTag(tagstring);
			addTag(tagstring);
			m.setSelectedIndex(0);
			// tag.removeComponent(tagfield);
			// tagfield= new TextField();
			// tagfield.addActionListener(action);
			// tag.addComponent(tagfield);
			txtTags.setEnabled(false);
			setEnabled(false);
			lblInfo.setText("Thank you!");
		}
	}

	public void clear()
	{
		txtTags.setEnabled(true);
		txtTags.requestFocus();
		lblInfo.setText("Tag the sound");
	}

	public void setTaggingComponent(TaggingComponent tc)
	{
		this.taggingComp = tc;
	}

}
