package noisetube.audio.jni;

import java.io.IOException;

import noisetube.util.Logger;

public class NativeRecorderCmd extends AbstractSocketCom
{

	Logger log = Logger.getInstance();

	public NativeRecorderCmd()
	{
		setPort(8112);
	}

	public void sendRecordCmd() throws IOException
	{
		sendCmd('r');
	}

	public void sendStopCmd() throws IOException
	{
		sendCmd('s');
	}

	private void sendCmd(char cmd) throws IOException
	{
		log.debug("sending cmd " + cmd);
		if(!connect())
		{
			throw new IOException("Cmd Server connection failed");
		}

		if(!sendRequest(cmd))
		{

			throw new IOException("sending cmd '" + cmd + "' failed");
		}
		close();
	}

	private boolean sendRequest(char cmd)
	{

		try
		{
			byte[] buf = ("" + cmd).getBytes();
			out.write(buf, 0, buf.length);
			out.flush();

			byte[] data = new byte[256]; // maybe use the buff instead of data[]
			int actualLength = in.read(data);

			String response = new String(data, 0, actualLength);
			log.debug("server response: " + response);
			return true;

		}
		catch(IOException ioe)
		{
			log.error("sending error: " + ioe.getMessage());
			return false;
		}
	}

}
