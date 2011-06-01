package noisetube.io.web;

import noisetube.MainMidlet;
import noisetube.config.Device;
import noisetube.io.Cache;
import noisetube.io.SaveException;
import noisetube.model.Measure;
import noisetube.util.ErrorCallback;
import noisetube.util.Logger;
import noisetube.util.URLUTF8Encoder;

/**
 * NoiseTube - Mobile client (J2ME version) Web API interface
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class HTTPWebAPI
{

	final static String API_BASE = "http://www.noisetube.net/api/";
	final static String DEV_API_BASE = "http://localhost:3000/api/";

	String api_base = API_BASE;

	private final Logger log = Logger.getInstance();

	boolean async = true;
	ErrorCallback errorCallback;

	private MainMidlet midlet;

	public HTTPWebAPI()
	{
		this.midlet = MainMidlet.getInstance();

		api_base = API_BASE;

		// config api base according to the environment
		/*
		 * if (MainMidlet.environment == MainMidlet.EMULATOR_ENV) api_base =
		 * DEV_API_BASE;
		 */
	}

	/**
	 * @return true is ping succeeded (internet access available and noisetube server responded), false otherwise
	 */
	public boolean pingNoiseTubeService()
	{
		try
		{
			String response = HTTPUtils.get_request(api_base + "ping");
			return(response != null && response.equalsIgnoreCase("ok"));
		}
		catch(Exception e)
		{
			return false; // ping failed
		}
	}

	public void setErrorCallBack(ErrorCallback errorCallBack)
	{
		this.errorCallback = errorCallBack;
	}

	public String getAPIKey(String login, String password) throws SaveException //=login
	{	//TODO encrypt
		String url = api_base + "authenticate?login=" + login + "&password=" + password;
		return HTTPUtils.get_request(url);
	}

	public int sendNewTrack() throws SaveException
	{
		String url = api_base
				+ "newsession?key="
				+ getApiKey()
				// Client type + version:
				+ "&client=" + URLUTF8Encoder.encode(MainMidlet.CLIENT_TYPE)
				+ "&clientversion=" + URLUTF8Encoder.encode(MainMidlet.CLIENT_VERSION)
				// Device:
				+ ("&devicebrand=" + ((Device.DEVICE_BRAND_NAME != null) ? URLUTF8Encoder.encode(Device.DEVICE_BRAND_NAME) : "unknown")
				+ "&devicemodel=" + ((Device.DEVICE_MODEL != null) ? URLUTF8Encoder.encode(Device.DEVICE_MODEL) : "unknown")
				+ ((Device.DEVICE_MODEL_VERSION != null) ? "&devicemodelversion=" + URLUTF8Encoder.encode(Device.DEVICE_MODEL_VERSION) : "")
				+ ((Device.DEVICE_PLATFORM != null) ? "&deviceplatform=" + URLUTF8Encoder.encode(Device.DEVICE_PLATFORM): "")
				+ ((Device.DEVICE_PLATFORM_VERSION != null) ? "&deviceplatformversion=" + URLUTF8Encoder.encode(Device.DEVICE_PLATFORM_VERSION) : ""))
				+ ((Device.DEVICE_J2ME_PLATFORM != null) ? "&devicej2meplatform=" + URLUTF8Encoder.encode(Device.DEVICE_J2ME_PLATFORM) : "");
		log.debug("Sending new session " + url);
		String response = HTTPUtils.get_request(url);
		if(!response.substring(0, 2).equalsIgnoreCase("ok"))
			throw new SaveException("Could not start session, server response: " + response);
		return Integer.parseInt(response.substring(3, response.length())); //return the trackID
	}

	public void sendEndTrack() throws SaveException
	{
		String url = api_base + "endsession?key=" + getApiKey();
		log.debug("Sending end session: " + url);
		HTTPUtils.get_request(url);
	}

	public void sendMeasure(Measure measure) throws SaveException
	{
		String url = api_base + "update?" + measure.toUrl() + "&key="
				+ getApiKey();
		log.debug("Sending measurement: " + url);
		if(async)
			HTTPUtils.get_request_async(url, errorCallback);
		else
			HTTPUtils.get_request(url);
	}

	public void sendBatch(Cache batch) throws SaveException
	{

		// send start track information
		if(batch.isNewTrack())
		{
			sendNewTrack();
			batch.setNewTrack(false);
		}

		// send the cached measures
		String json = batch.toJSON();
		log.debug("Sending a set of measures");
		HTTPUtils.post_json_request(api_base + "upload?key=" + getApiKey(),
				json.toString());
		batch.reset();
	}

	private String getApiKey() throws SaveException
	{
		if(!midlet.getPreferences().isAuthenticated())
			throw new SaveException("No API key (not authenticated!)");
		return midlet.getPreferences().getAPIKey();
	}

}
