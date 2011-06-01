/** 
 * -------------------------------------------------------------------------------
 * NoiseTube - Mobile client (J2ME version)
 * Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 * 
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License, version 2.1,
 * as published by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * --------------------------------------------------------------------------------
 * 
 * Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 * NoiseTube project source code repository: http://code.google.com/p/noisetube
 * NoiseTube project website: http://www.noisetube.net
 */

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