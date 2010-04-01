package noisetube.util;

/**
 * 
 * Blocking Queue structure
 * 
 */
public class BlockingQueue
{

	private int queue_size;
	private Object queue[];
	private int head;
	private int tail;

	public BlockingQueue()
	{
		this(50);
	}

	public BlockingQueue(int capacity)
	{
		queue_size = capacity;
		queue = new Object[queue_size];
		head = 0;
		tail = 0;
	}

	public int size()
	{
		return head - tail;
	}

	public synchronized void enqueue(Object obj)
	{
		head++;
		queue[head %= queue_size] = obj;
		notify();
	}

	public synchronized Object dequeue()
	{
		try
		{
			if(head == tail)
				wait();
		}
		catch(InterruptedException _ex)
		{
			return null;
		}

		tail++;
		return queue[tail %= queue_size];
	}

}