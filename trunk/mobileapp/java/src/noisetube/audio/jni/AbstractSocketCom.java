package noisetube.audio.jni;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import noisetube.util.Logger;

public abstract class AbstractSocketCom
{

	Logger log = Logger.getInstance();

	final int MAX_RETRY = 3;

	protected String url = "socket://127.0.0.1:";

	int port;

	protected SocketConnection conn;
	protected OutputStream out;
	protected InputStream in;

	protected void setPort(int port)
	{
		this.port = port;
	}

	protected boolean connect()
	{

		boolean error = true;
		int retry = 0;
		while(error && (retry < MAX_RETRY))
		{
			try
			{
				conn = (SocketConnection) Connector.open(url + port,
						Connector.READ_WRITE, true);
				out = conn.openOutputStream();

				in = conn.openInputStream();
				error = false;
			}
			catch(IOException e)
			{
				retry++;
				e.printStackTrace();
				try
				{
					// maybe useless due to the timeout
					Thread.sleep(100);
				}
				catch(InterruptedException e1)
				{
					e1.printStackTrace();
				}
			}

		}
		return !error;
	}

	protected void close()
	{
		try
		{
			if(out != null)
				out.close();
			if(in != null)
				in.close();
			if(conn != null)
				conn.close();
		}
		catch(IOException e)
		{
		}
	}
}
