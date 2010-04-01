package noisetube.ui;

import noisetube.tagging.NotesComponent;

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
public class NotesUIComponent extends Container
{

	// TAGS
	private TextField tagfield = new TextField();
	Label address_label = new Label("Tag the sound");
	private MeasureForm ui;

	NotesComponent note;
	Container tag;
	ActionListener action;
	List list = new List();
	ListModel m;

	public NotesUIComponent(MeasureForm measureForm)
	{
		super();
		this.ui = measureForm;

		tag = new Container();
		address_label.setPreferredW(200);
		m = list.getModel();
		tag.addComponent(address_label);
		tag.addComponent(new Label("(bird, klaxon, loud)"));
		tag.addComponent(tagfield);
		// neighbors, traffic, Construction, pets, aircraft, industry
		// tag.addC
		addTag2("[suggestions]");
		addTag2("traffic");
		addTag2("construction");
		addTag2("neighbors");
		addTag2("pets");
		addTag2("aircraft");
		addTag2("industry");
		addTag2("annoying");
		addTag2("pleasant");
		addTag2("loud");

		tag.setPreferredSize(new Dimension(170, 200));
		addComponent(tag);
		action = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				fire(tagfield.getText());
			}
		};

		tagfield.setMaxSize(100);
		tagfield.setEnabled(false);
		tagfield.addActionListener(action);
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
		tag.addComponent(sources_box);
		sources_box.setPreferredSize(new Dimension(140, 20));

	}

	// private void addTag(String name) {
	// final Button b = new Button(name);
	// b.addActionListener(new ActionListener() {
	// public void actionPerformed(ActionEvent e)
	// {
	// fire(b.getText());
	// }
	// });
	// tag.addComponent(b);
	// }

	private boolean contains(String tag)
	{
		for(int i = 0; i < m.getSize(); i++)
		{
			if(m.getItemAt(i).equals(tag))
				return true;
		}
		return false;
	}

	private void addTag2(String name)
	{
		if(!contains(name))
			m.addItem(name);
	}

	private void fire(String tagstring)
	{
		if(ui.isRunning() && tagstring != null && !tagstring.equals(""))
		{
			note.setTags(tagstring);
			addTag2(tagstring);
			m.setSelectedIndex(0);
			// tag.removeComponent(tagfield);
			// tagfield= new TextField();
			// tagfield.addActionListener(action);
			// tag.addComponent(tagfield);
			tagfield.setEnabled(false);
			tag.setEnabled(false);
			address_label.setText("Thank you!");
		}
	}

	public void clear()
	{
		tagfield.setEnabled(true);
		tagfield.requestFocus();
		address_label.setText("Tag the sound");
	}

	public void setNote(NotesComponent note)
	{
		this.note = note;
	}

}
