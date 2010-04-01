package noisetube.tagging;

import noisetube.audio.ILeqListener;
import noisetube.util.CyclicQueue;

public class PeakTagger implements ILeqListener, IAutomaticTagger
{

	final int MEMORY = 4;
	final int STATE_HIGHVARIATION = 1;
	final int STATE_NOHIGH = 2;
	int state = 0;

	TagListener listener;

	CyclicQueue memory = new CyclicQueue(MEMORY);

	public void sendLeq(double leq)
	{
		memory.push(new Double(leq));
		if(high_variation_state())
		{
			if(getState() != STATE_HIGHVARIATION)
			{
				setState(STATE_HIGHVARIATION);
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
		if(listener != null && state == STATE_HIGHVARIATION)
		{
			listener.sendTag("variation:high");
		}
	}

	private double getMem(int i)
	{
		return ((Double) memory.get(i)).doubleValue();
	}

	private boolean high_variation_state()
	{

		// find peak
		if(memory.getSize() > 3)
		{
			int size = memory.getSize();
			double past = Math.min(getMem(size - 3), getMem(size - 2));
			double now = getMem(size - 1);
			// if more than 10 db in 2 second -> high variation
			if(now > (past + 10))
			{
				return true;
			}
		}
		return false;
	}

	public void setListener(TagListener taglistener)
	{
		this.listener = taglistener;

	}
}
