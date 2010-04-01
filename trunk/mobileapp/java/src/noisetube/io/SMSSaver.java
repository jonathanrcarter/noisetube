package noisetube.io;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import noisetube.config.Preferences;
import noisetube.model.Measure;

/** Sends an SMS message */
public class SMSSaver extends Saver implements Runnable
{
	private String message;
	private String phoneNumber;

	public void run()
	{
		StringBuffer addr = new StringBuffer(20);
		addr.append("sms://+");
		addr.append(phoneNumber);
		String address = addr.toString();

		MessageConnection smsconn = null;
		try
		{
			// Open the message connection.
			smsconn = (MessageConnection) Connector.open(address);

			// Create the message.
			TextMessage txtmessage = (TextMessage) smsconn
					.newMessage(MessageConnection.TEXT_MESSAGE);
			txtmessage.setAddress(address);// !!
			txtmessage.setPayloadText(message);

			// send it
			smsconn.send(txtmessage);

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		if(smsconn != null)
		{
			try
			{
				smsconn.close();
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
	}

	private void send(String message, String phoneNumber)
	{
		this.message = message;
		this.phoneNumber = phoneNumber;
		Thread t = new Thread(this);
		t.start();
	}

	public void save(Measure measurement)
	{
		if(running && measurement != null)
		{
			// TODO measure-> SMS
			send(measurement.toString(), "SOME PHONE NUMBER"); // measure needs
																// a toSMS
																// method; phone
																// number needs
																// to be looked
																// up per
																// country
		}
	}

	public void start()
	{
		// ...
		running = true;
		if(preferences.getSavingMode() == Preferences.SAVE_SMS)
			setMessage("Saving by SMS");
	}

	public void stop()
	{
		running = false;
		// ...
		if(preferences.getSavingMode() == Preferences.SAVE_SMS)
			clearMessage();
	}

}