package noisetube.io;

import java.util.Enumeration;
import java.util.Vector;

import noisetube.model.Measure;
import noisetube.util.Logger;

/**
 * Command Queue (used for internet recovery mode)
 * 
 * @author maisonneuve
 */
public class Cache
{

	final int CAPACITY_MAX = 200;
	// cache and for time and connection error
	int capacity = CAPACITY_MAX;

	boolean newTrackCmd = false;

	Vector measures = new Vector(capacity);

	Logger log = Logger.getInstance();

	double i = 0;

	/**
	 * has "newtrack" command
	 * 
	 * @return
	 */
	public boolean isNewTrack()
	{
		return newTrackCmd;
	}

	/**
	 * Reset the cache
	 */
	public void reset()
	{
		i = 0;
		setNewTrack(false);
		measures.removeAllElements();
	}

	/**
	 * add a "newtrack" cmd
	 */
	public void setNewTrack(boolean newtrack)
	{
		newTrackCmd = newtrack;
	}

	public double size()
	{
		return i;
	}

	/**
	 * add a measure cmd
	 * 
	 * @param measure
	 * @throws IllegalStateException
	 */
	public boolean add_measure(Object measure)
	{
		i++;
		if(isFull())
		{
			log.error("cache full");
			return false;
		}
		else
		{
			measures.addElement(measure);
			return true;
		}
	}

	/**
	 * get the stored measures
	 * 
	 * @return
	 */
	public Vector get_measures()
	{
		return measures;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isFull()
	{
		return measures.size() > capacity;
	}

	public String toJSON()
	{
		Enumeration e = measures.elements();
		String json = "{measures:[";
		int i = 0;
		while(e.hasMoreElements())
		{
			if(i > 0)
				json += ",";
			json += ((Measure) e.nextElement()).toJSON();
			i++;
		}
		json += "]}";
		return json;
	}
}
