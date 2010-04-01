package noisetube.audio;

/**
 * ILoudnessComponent
 * 
 * @author maisonneuve
 * 
 */
public interface ILoudnessComponent
{
	public void setLeqListener(ILeqListener listener);

	public void start();

	public void stop();

	public boolean isRunning();

}
