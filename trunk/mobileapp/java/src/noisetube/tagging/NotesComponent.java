package noisetube.tagging;

import noisetube.ui.NotesUIComponent;
import noisetube.util.Logger;

public class NotesComponent implements TagListener
{

	String tag;
	int rating;
	String comments;

	NotesUIComponent ui;
	public boolean ready = true;

	Logger log = Logger.getInstance();

	public boolean hasNote()
	{
		return (tag != null) && (!"".equals(tag));
	}

	public String getNote()
	{
		return tag;
	}

	public boolean isReady()
	{
		return ready;
	}

	public void setUIComponent(NotesUIComponent ui)
	{
		this.ui = ui;
		ui.setNote(this);
	}

	public void setTags(String text)
	{
		tag = text;
	}

	public void edit()
	{
		clear();
		ui.clear();
	}

	public void clear()
	{
		tag = null;
		rating = -1;
		comments = null;
	}

	public void sendTag(String name)
	{
		setTags(name);
	}
}
