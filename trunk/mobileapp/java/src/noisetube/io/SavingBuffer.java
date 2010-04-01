package noisetube.io;

import noisetube.model.Measure;

/**
 * @author mstevens
 * 
 */
public interface SavingBuffer
{

	public void setSaver(Saver saver);

	public void enqueueMeasure(Measure measure);

	public void flush();

	public void flush(boolean stopSaverAfterwards);

}
