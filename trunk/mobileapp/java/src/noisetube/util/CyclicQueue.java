package noisetube.util;

/**
 * 
 * @author maisonneuve
 * 
 */
public class CyclicQueue
{

	private boolean boucle_ = false;
	int capacity = 10;
	int start_idx = -1;
	private Object queue[];

	public CyclicQueue()
	{
		this(10);
	}

	public CyclicQueue(int capacity)
	{
		this.capacity = capacity;
		queue = new Object[capacity];
	}

	public int getCapacity()
	{
		return capacity;
	}

	public Object get(int i)
	{
		return queue[get_idx(i)];
	}

	public int getSize()
	{
		if(boucle_)
			return capacity;
		else
			return(start_idx + 1);
	}

	/**
	 * add a value
	 */
	public void push(Object object)
	{

		if(!boucle_ && start_idx + 1 == capacity)
			boucle_ = true;

		start_idx = (start_idx + 1) % capacity;

		queue[start_idx] = object;
	}

	/**
	 * get index
	 * 
	 * @param i
	 * @return
	 */
	private int get_idx(int i)
	{
		if(boucle_)
			return (start_idx + 1 + i) % capacity;
		else
			return i;
	}
}
