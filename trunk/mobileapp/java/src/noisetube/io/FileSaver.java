package noisetube.io;

import java.io.OutputStreamWriter;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import noisetube.MainMidlet;
import noisetube.config.Device;
import noisetube.config.Preferences;
import noisetube.model.Measure;
import noisetube.util.CustomStringBuffer;
import noisetube.util.StringUtils;
import noisetube.util.XMLUtils;

/**
 * @author mstevens
 * 
 */
public class FileSaver extends Saver
{

	static public String CHARACTER_ENCODING = "UTF-8";

	private String filePath;
	private OutputStreamWriter fileWriter = null;
	private long startTime;

	/**
	 * @see noisetube.io.ISender#start()
	 */
	public void start()
	{
		if(!running)
		{
			if(Device.supportsSavingToFile())
			{
				try
				{
					String folderPath = preferences.getDataFolderPath();
					if(folderPath == null)
						throw new Exception("No accessible data folder");
					filePath = folderPath
							+ "Track_"
							+ StringUtils.formatDateTime(System
									.currentTimeMillis(), "-", "", "T")
							+ ".xml";
					FileConnection fc = (FileConnection) Connector.open(
							filePath, Connector.READ_WRITE);
					fc.create();
					fileWriter = new OutputStreamWriter(fc.openOutputStream(),
							CHARACTER_ENCODING);
					// fileWriter = FileAccess.getFileWriter(filePath,
					// FileAccess.FILE_EXISTS_STRATEGY_CREATE_RENAMED_FILE,
					// FileAccess.FILE_DOES_NOT_EXIST_STRATEGY_CREATE,
					// CHARACTER_ENCODING);
				}
				catch(Exception e)
				{
					log.error(e, "Cannot create FileWriter");
					fileWriter = null;
					filePath = null;
				}
				if(fileWriter != null)
				{
					startTime = System.currentTimeMillis();
					running = true;
					writeHeader();
					log.debug("FileSaver started (file: " + filePath + ")");
					if(preferences.getSavingMode() == Preferences.SAVE_FILE)
						setMessage("Saving to file: " + filePath.substring(8));
				}
			}
		}
	}

	/**
	 * @see noisetube.io.ISender#stop()
	 */
	public void stop()
	{
		stop(false);
	}

	public void stop(boolean force)
	{
		if(running)
		{
			if(!force)
				writeFooter();
			running = false; // Note: do not move this before writeFooter!
			try
			{
				fileWriter.close();
				fileWriter = null;
			}
			catch(Exception e)
			{
			}
			log.debug("FileSaver stopped" + (force ? " (forced)" : ""));
			if(preferences.getSavingMode() == Preferences.SAVE_FILE)
				clearMessage();
		}
	}

	/**
	 * @see noisetube.io.ISender#save(noisetube.model.Measure)
	 */
	public void save(Measure measurement)
	{
		if(running && measurement != null)
			writeToFile("\t" + measurement.toXML() + "\n");
	}

	protected final void writeToFile(String stringToWrite)
	{
		if(running)
		{
			try
			{
				fileWriter.write(stringToWrite);
				fileWriter.flush();
			}
			catch(Exception e)
			{
				log.error(e, "Could not write to file");
				stop(true);
			}
		}
	}

	protected void writeHeader()
	{
		CustomStringBuffer bff = new CustomStringBuffer();
		// XML file headers
		bff.appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		bff
				.appendLine("<NoiseTube-Mobile-Session startTime=\""
						+ XMLUtils.timeDateValue(startTime)
						+ "\""
						+
						// User:
						(preferences.isAuthenticated() ? " userKey=\""
								+ preferences.getAPIKey() + "\"" : "")
						+
						// Client type + version:
						" client=\""
						+ XMLUtils.escapeCharacters(MainMidlet.CLIENT_TYPE)
						+ "\""
						+ " clientVersion=\""
						+ XMLUtils.escapeCharacters(MainMidlet.CLIENT_VERSION)
						+ "\""
						+
						// Device:
						(((Device.DEVICE_BRAND_NAME != null) ? " deviceBrand=\""
								+ XMLUtils
										.escapeCharacters(Device.DEVICE_BRAND_NAME)
								+ "\""
								: "")
								+ ((Device.DEVICE_MODEL != null) ? " deviceModel=\""
										+ XMLUtils
												.escapeCharacters(Device.DEVICE_MODEL)
										+ "\""
										: "")
								+ ((Device.DEVICE_MODEL_VERSION != null) ? " deviceModelVersion=\""
										+ XMLUtils
												.escapeCharacters(Device.DEVICE_MODEL_VERSION)
										+ "\""
										: "")
								+ ((Device.DEVICE_PLATFORM != null) ? " devicePlatform=\""
										+ XMLUtils
												.escapeCharacters(Device.DEVICE_PLATFORM)
										+ "\""
										: "") + ((Device.DEVICE_PLATFORM_VERSION != null) ? " devicePlatformVersion=\""
								+ XMLUtils
										.escapeCharacters(Device.DEVICE_PLATFORM_VERSION)
								+ "\""
								: ""))
						+ ((Device.DEVICE_J2ME_PLATFORM != null) ? " deviceJ2MEPlatform=\""
								+ XMLUtils
										.escapeCharacters(Device.DEVICE_J2ME_PLATFORM)
								+ "\""
								: "") + ">");
		// Write to file
		writeToFile(bff.toString());
	}

	protected void writeFooter()
	{
		writeToFile("</NoiseTube-Mobile-Session>");
	}

}
