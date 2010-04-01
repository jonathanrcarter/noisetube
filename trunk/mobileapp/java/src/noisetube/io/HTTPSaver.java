package noisetube.io;

import noisetube.config.Preferences;
import noisetube.io.web.HTTPWebAPI;
import noisetube.model.Measure;
import noisetube.util.BlockingQueue;
import noisetube.util.ErrorCallback;

/**
 * HTTP Sender
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class HTTPSaver extends Saver implements Runnable, ErrorCallback
{

	BlockingQueue dataSendingQueue;

	// HTTP Connection recovery mode
	boolean cacheEnabled = false;

	// enable or not the recovery mode
	boolean enable_recovery = true;

	// cache used during the recovery mode
	private Cache cache = new Cache();

	private HTTPWebAPI api;

	public HTTPSaver()
	{
		dataSendingQueue = new BlockingQueue();
		api = new HTTPWebAPI();
		api.setErrorCallBack(this);
	}

	public void save(Measure measurement)
	{
		if(running && measurement != null)
			dataSendingQueue.enqueue(measurement);
	}

	public void start()
	{ // init
		cache.reset();
		cacheEnabled = false;
		running = true;
		new Thread(this).start();
		log.debug("HTTPSaver started");
		if(preferences.getSavingMode() == Preferences.SAVE_HTTP)
			setMessage("Saving to NoiseTube.net (user: "
					+ preferences.getUsername() + ")");
	}

	public void stop()
	{
		// do not set runnin = false here!
		dataSendingQueue.enqueue(new Object()); // "stopper" object
		log.debug("HTTPSaver stopped");
		if(preferences.getSavingMode() == Preferences.SAVE_HTTP)
			clearMessage();
	}

	public void error(Exception sendingException)
	{
		toggleCache(true);
	}

	public void run()
	{
		Measure measurement = null;
		try
		{
			// send signal for a new track
			api.sendNewTrack();
		}
		catch(SaveException se)
		{
			if(enable_recovery)
			{
				toggleCache(true); // turn on the connection recovery mode
				cache.setNewTrack(true);
			}
		}
		while(running)
		{
			try
			{
				Object o = dataSendingQueue.dequeue();
				// FYI: this cast is needed to stop!
				if(!(o instanceof Measure))
				{
					running = false;
					continue;
				}
				measurement = (Measure) o;
				if(!cacheEnabled)
				{
					// send over http using NoiseTube API
					api.sendMeasure(measurement);
				}
				else
				{
					// cache the information
					cache.add_measure(measurement);

					// try to send all the cache every 10 measures
					if(cache.size() % 10 == 0)
					{
						log.debug("Batch mode: trying to send data");
						api.sendBatch(cache);
						toggleCache(false); // turn off recovery mode
					}
				}
			}
			catch(SaveException e)
			{
				log.error(e, "sending measurement through http");

				// if first connection error
				if(!cacheEnabled && enable_recovery)
				{
					// turn on the connection recovery mode
					toggleCache(true);

					// Cache the information to send
					cache.add_measure(measurement);
				}
			}
		}
		// End...
		try
		{
			api.sendEndTrack();
		}
		catch(SaveException e)
		{
			log.error(e, "error sending end session");
		}
	}

	private void toggleCache(boolean enable)
	{
		if(enable)
		{
			log.debug("Cache activated (Cnx lost?)");
			setMessage("Saving to NoiseTube.net (user: "
					+ preferences.getUsername() + "; sending delayed)");
		}
		else
		{
			log.debug("Cache disabled (sending real-time updates)");
			cache.reset(); // reset cache
			setMessage("Saving to NoiseTube.net (user: "
					+ preferences.getUsername() + ")");
		}
		cacheEnabled = enable;
	}

}