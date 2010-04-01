package noisetube.tagging;

import noisetube.audio.ILeqListener;
import noisetube.util.CyclicQueue;

public class HighExposureTagger implements ILeqListener, IAutomaticTagger
{

	final int HIGH_STATE_MEMORY = 10;
	final int STATE_HIGH = 1;
	final int STATE_NOHIGH = 2;
	int state = 0;

	TagListener listener;

	CyclicQueue memory = new CyclicQueue(HIGH_STATE_MEMORY);

	public void sendLeq(double leq)
	{
		memory.push(new Double(leq));
		if(high_state())
		{
			if(getState() != STATE_HIGH)
			{
				setState(STATE_HIGH);
			}
		}
		else
		{
			setState(STATE_NOHIGH);
		}
	}

	private int getState()
	{
		return state;
	}

	private void setState(int state)
	{
		this.state = state;
		if(listener != null && state == STATE_HIGH)
		{
			listener.sendTag("exposure:high");
		}
	}

	private double getMem(int i)
	{
		return ((Double) memory.get(i)).doubleValue();
	}

	private boolean high_state()
	{
		boolean high_state = false;

		// find peak
		if(memory.getSize() == HIGH_STATE_MEMORY)
		{
			high_state = true;
			for(int i = 0; i < HIGH_STATE_MEMORY; i++)
			{
				if(getMem(i) < 80)
				{
					high_state = false;
				}
			}
		}
		return high_state;
	}

	public void setListener(TagListener taglistener)
	{
		this.listener = taglistener;

	}
}
