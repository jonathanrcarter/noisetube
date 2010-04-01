package noisetube.io;

import java.util.Enumeration;
import java.util.Vector;

import noisetube.model.Measure;

/**
 * @author mstevens
 * 
 */
public class MultiSaver extends Saver
{

	Vector savers;

	public MultiSaver()
	{
		savers = new Vector();
	}

	public void addSaver(Saver saver)
	{
		savers.addElement(saver);
	}

	/**
	 * @see noisetube.io.Saver#save(noisetube.model.Measure)
	 */
	public void save(Measure measure)
	{
		Enumeration saverEnum = savers.elements();
		while(saverEnum.hasMoreElements())
			((Saver) saverEnum.nextElement()).save(measure);
	}

	/**
	 * @see noisetube.util.IService#start()
	 */
	public void start()
	{
		Enumeration saverEnum = savers.elements();
		while(saverEnum.hasMoreElements())
			((Saver) saverEnum.nextElement()).start();
		running = true;
	}

	/**
	 * @see noisetube.util.IService#stop()
	 */
	public void stop()
	{
		running = false;
		Enumeration saverEnum = savers.elements();
		while(saverEnum.hasMoreElements())
			((Saver) saverEnum.nextElement()).stop();
	}

}
