package noisetube.config;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

import noisetube.MainMidlet;

public class WatchPreferences extends Preferences
{

	private boolean useWatch;

	private String watchAddress = "000780894889"; // "00078089487F";

	public WatchPreferences()
	{
		super();
	}

	public String getWatchAddress()
	{
		return watchAddress;
	}

	public void setWatchAddress(String watchAddress)
	{
		this.watchAddress = watchAddress;
	}

	/**
	 * @return the useWatch
	 */
	public boolean isUseWatch()
	{
		return useWatch;
	}

	/**
	 * @param useWatch
	 *            the useWatch to set
	 */
	public void setUseWatch(boolean useWatch)
	{
		this.useWatch = useWatch;
	}

	protected void _save(DataOutput dos) throws IOException
	{
		dos.writeBoolean(isUseWatch());
		dos.writeUTF(getWatchAddress());
		super._save(dos);
	}

	protected void _read(DataInputStream dis) throws IOException
	{
		setUseWatch(dis.readBoolean());
		setWatchAddress(dis.readUTF());
		super._read(dis);
	}

	public void find_client_name()
	{
		MainMidlet app = MainMidlet.getInstance();
		if(isUseWatch())
			MainMidlet.CLIENT_TYPE += "_Watch";
		Device.DEVICE_BRAND_NAME = "Watch";
		super.find_client_name();
	}
}
