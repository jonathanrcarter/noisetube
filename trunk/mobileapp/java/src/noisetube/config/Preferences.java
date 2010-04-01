package noisetube.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;

import noisetube.MainMidlet;
import noisetube.audio.java.Calibration;
import noisetube.io.FileAccess;
import noisetube.util.ComboList;
import noisetube.util.Logger;

/**
 * Account information
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class Preferences
{
	// STATICS:
	public static final int SAVE_NO = 0;
	public static final int SAVE_HTTP = 1;
	public static final int SAVE_FILE = 2;
	public static final int SAVE_SMS = 3;

	public static final boolean DEFAULT_SUPPORT_GPS = true;
	public static final boolean DEFAULT_SUPPORT_WATH = false;

	static private final String STORE_NAME = "noisetube"
			+ MainMidlet.CLIENT_VERSION;

	static private Preferences instance;

	// DYNAMICS:
	private Logger log = Logger.getInstance();

	private NoiseTubeAccount account = new NoiseTubeAccount();

	private boolean preferMemoryCard = true;

	private String dataFolderPath = null;

	private int savingMode; // Saving Mode (HTTP, FILE, SMS, NO)

	private boolean alsoSaveToFileWhenInHTTPMode;

	private boolean useGPS;

	private boolean useCoordinateInterpolation = false;

	private boolean blockScreensaver = true;

	private boolean jniRecording = false;

	private Calibration calibration = Device.getCalibration(); // TODO
																// Auto-generated
																// constructor
																// stub;

	public void configure()
	{

		find_client_name();

		if(isUseGPS())
		{
			// if(Device.DEVICE_BRAND == Device.BRAND_SONYERICSSON) {
			// Coordinate interpolation
			setUseCoordinateInterpolation(true);
			// }else
			// setUseCoordinateInterpolation(false);
		}

		if(getSavingMode() == SAVE_FILE
				|| (getSavingMode() == SAVE_HTTP && alsoSaveToFileWhenInHTTPMode))
		{
			dataFolderPath = getDataFolderPath();
			if(dataFolderPath == null)
			{
				// No working path found
				log
						.error("No accessible data folder found, file saving disabled.");
				setSavingMode(SAVE_NO);
			}
		}
	}

	public boolean isPreferMemoryCard()
	{
		return preferMemoryCard;
	}

	public void setPreferMemoryCard(boolean preferMemoryCard)
	{
		if(this.preferMemoryCard != preferMemoryCard)
			dataFolderPath = null; // reset folderpath!
		this.preferMemoryCard = preferMemoryCard;
	}

	public void find_client_name()
	{
		MainMidlet app = MainMidlet.getInstance();

		if(isUseGPS())
			MainMidlet.CLIENT_TYPE += "_GPSless";
		String version = app.getAppProperty("MIDlet-Version"); // gets value
		// stored in
		// JAD/JAR
		String versionPostfix = app.getAppProperty("NoiseTube-VersionPostfix");
		if(version != null)
			MainMidlet.CLIENT_VERSION = "v"
					+ version.trim()
					+ ((versionPostfix != null && !versionPostfix.equals("")) ? ("_" + versionPostfix)
							: "");
		MainMidlet.CLIENT_IS_TEST_VERSION = (versionPostfix != null && (versionPostfix
				.equalsIgnoreCase("beta") || versionPostfix
				.equalsIgnoreCase("alpha")));

	}

	/**
	 * @param dataFolderPath
	 *            the dataFolderPath to set
	 */
	protected void setDataFolderPath(String dataFolderPath)
	{
		if(this.dataFolderPath != dataFolderPath)
			log.info("Data folder in use: " + dataFolderPath.substring(8));
		this.dataFolderPath = dataFolderPath;
	}

	public int getSavingMode()
	{
		return savingMode;
	}

	public void setSavingMode(int savingMode)
	{
		this.savingMode = savingMode;
	}

	public ComboList getAvailableSavingModes()
	{
		ComboList saveModes = new ComboList();
		saveModes.addItem("None", SAVE_NO, (savingMode == SAVE_NO));

		// if(Device.supportsHTTP())
		saveModes
				.addItem("NoiseTube.net", SAVE_HTTP, (savingMode == SAVE_HTTP));

		if(Device.supportsSavingToFile())
			saveModes.addItem("File", SAVE_FILE, (savingMode == SAVE_FILE));

		// if(Device.supportsSMS ...)
		// saveModes.addItem("SMS", SAVE_SMS, (savingMode == SAVE_SMS));

		return saveModes;
	}

	/**
	 * @return the alsoSaveToFileWhenInHTTPMode
	 */
	public boolean isAlsoSaveToFileWhenInHTTPMode()
	{
		return alsoSaveToFileWhenInHTTPMode;
	}

	/**
	 * @param alsoSaveToFileWhenInHTTPMode
	 *            the alsoSaveToFileWhenInHTTPMode to set
	 */
	public void setAlsoSaveToFileWhenInHTTPMode(
			boolean alsoSaveToFileWhenInHTTPMode)
	{
		this.alsoSaveToFileWhenInHTTPMode = alsoSaveToFileWhenInHTTPMode;
	}

	public boolean isUseGPS()
	{
		return useGPS;
	}

	public void setUseGPS(boolean useGPS)
	{
		this.useGPS = useGPS;
	}

	/**
	 * @return the useCoordinateInterpolation
	 */
	public boolean isUseCoordinateInterpolation()
	{
		return useCoordinateInterpolation;
	}

	/**
	 * @param useCoordinateInterpolation
	 *            the useCoordinateInterpolation to set
	 */
	public void setUseCoordinateInterpolation(boolean useCoordinateInterpolation)
	{
		this.useCoordinateInterpolation = useCoordinateInterpolation;
	}

	/**
	 * @return the blockScreensaver
	 */
	public boolean isBlockScreensaver()
	{
		return blockScreensaver;
	}

	/**
	 * @param blockScreensaver
	 */
	public void setBlockScreensaver(boolean blockScreensaver)
	{
		this.blockScreensaver = blockScreensaver;
	}

	public boolean isJniRecording()
	{
		return jniRecording;
	}

	public void setJniRecording(boolean jniRecording)
	{
		this.jniRecording = jniRecording;
	}

	public String getUsername()
	{
		return this.account.username;
	}

	public void setUsername(String username)
	{
		this.account.username = username;
	}

	public String getPassword()
	{
		return this.account.password;
	}

	public void setPassword(String password)
	{
		this.account.password = password;
	}

	public String getAPIKey()
	{
		return this.account.APIKey;
	}

	public void setAPIKey(String key)
	{
		this.account.APIKey = key;
	}

	public boolean isAuthenticated()
	{
		return !getAPIKey().equals("");
	}

	/**
	 * @return the dataFolderPath
	 */
	public String getDataFolderPath()
	{
		if(!Device.supportsSavingToFile())
			return null;

		String pathToSet = null;
		try
		{
			String memoryCardRoot = null;
			if(preferMemoryCard)
			{
				memoryCardRoot = Device.getMemoryCardRoot();
				if(memoryCardRoot != null)
				{
					pathToSet = Device.getDataFolderPathForRoot(memoryCardRoot);
					if(!FileAccess.canWriteToFolder(pathToSet, true))
						pathToSet = null;
				}
			}
			if(pathToSet == null) // memory card not preferred or memory
			// card could not be used
			{
				// try every root...
				String[] roots = Device.getFileSystemRoots();
				if(roots != null)
				{
					for(int r = 0; r < roots.length; r++)
					{
						String p = Device.getDataFolderPathForRoot(roots[r]);
						if(FileAccess.canWriteToFolder(p, true))
						{
							pathToSet = p;
							break; // no need to try the other roots
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			log.error(e, "getDataFolderPath()");
			pathToSet = null;
		}
		if(pathToSet != null)
			setDataFolderPath(pathToSet);
		else
		{
			Device.disableFileSaving(); // to avoid retrying later
		}
		return dataFolderPath;
	}

	/**
	 * save the preferences
	 * 
	 * @return
	 */
	public boolean save()
	{
		RecordStore store = null;

		// temp variables
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutput dos = new DataOutputStream(bos);
		try
		{
			log.debug("Saving preferences");
			_save(dos);
			RecordStore.deleteRecordStore(STORE_NAME);
			store = RecordStore.openRecordStore(STORE_NAME, true);
			final byte[] bits = bos.toByteArray();
			store.addRecord(bits, 0, bits.length);
			log.debug("Preferences saved");
		}
		catch(Exception ex)
		{
			log.error("Exception upon saving preferences: " + ex.getMessage());
			return false;
		}
		finally
		{
			if(store != null)
			{
				try
				{
					store.closeRecordStore();
				}
				catch(Exception ignore)
				{
				}
			}
		}
		return true;
	}

	protected void _save(DataOutput dos) throws IOException
	{
		dos.writeUTF(account.toString());
		dos.writeBoolean(isUseGPS());
		dos.writeBoolean(isPreferMemoryCard());
		dos.writeInt(getSavingMode());
		dos.writeBoolean(isBlockScreensaver());
		dos.writeBoolean(isJniRecording());

		// save calibration
		if(calibration.corrector != null && calibration.manuallyChanged)
		{
			dos.writeInt(calibration.corrector.length);
			for(int i = 0; i < calibration.corrector.length; i++)
			{
				dos.writeDouble(calibration.corrector[i][0]);
				dos.writeDouble(calibration.corrector[i][1]);
			}
		}
		else
			dos.writeInt(0);
	}

	public Calibration getCalibration()
	{
		return calibration;
	}

	protected void _read(DataInputStream dis) throws IOException
	{
		account = NoiseTubeAccount.load(dis.readUTF());
		setUseGPS(dis.readBoolean());
		setPreferMemoryCard(dis.readBoolean());
		setSavingMode(dis.readInt());
		setBlockScreensaver(dis.readBoolean());
		setJniRecording(dis.readBoolean());

		// load calibration
		int size = dis.readInt();
		if(size > 0)
		{
			double[][] calibArray = new double[size][2];
			for(int i = 0; i < size; i++)
			{
				calibArray[i][0] = dis.readDouble();
				calibArray[i][1] = dis.readDouble();
			}
			calibration = new Calibration(calibArray);
		}
	}

	/**
	 * 
	 * @return
	 */
	public boolean read()
	{
		RecordStore store = null;
		try
		{
			store = RecordStore.openRecordStore(STORE_NAME, false);
			final RecordEnumeration en = store.enumerateRecords(null, null,
					false);
			if(!en.hasNextElement())
			{
				log.debug("No previous preferences found");
				return false;
			}
			final ByteArrayInputStream bis = new ByteArrayInputStream(en
					.nextRecord());
			final DataInputStream dis = new DataInputStream(bis);

			_read(dis);
			log.debug("Preferences loaded");
			return true;

		}
		catch(Exception ex)
		{
			return false;
		}
		finally
		{
			if(store != null)
			{
				try
				{
					store.closeRecordStore();
				}
				catch(Exception ignore)
				{
				}
			}
		}
	}
}
