package noisetube.audio.java;

import noisetube.util.IService;

public interface IStreamRecorder extends IService
{

	public void setStreamAudioListener(StreamAudioListener listener);

	public void setRecordingTime(int i);

	public void setTimeInterval(int i);
}
