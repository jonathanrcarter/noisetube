/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Java ME version)
 *  
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011
 * --------------------------------------------------------------------------------
 *  This library is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU Lesser General Public License, version 2.1, as published
 *  by the Free Software Foundation.
 *  
 *  This library is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License along
 *  with this library; if not, write to:
 *    Free Software Foundation, Inc.,
 *    51 Franklin Street, Fifth Floor,
 *    Boston, MA  02110-1301, USA.
 *  
 *  Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *  NoiseTube project source code repository: http://code.google.com/p/noisetube
 * --------------------------------------------------------------------------------
 *  More information:
 *   - NoiseTube project website: http://www.noisetube.net
 *   - Sony Computer Science Laboratory Paris: http://csl.sony.fr
 *   - VUB BrusSense team: http://www.brussense.be
 * --------------------------------------------------------------------------------
 */

package net.noisetube.config.javame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;

import net.noisetube.config.NTAccount;
import net.noisetube.config.Preferences;
import net.noisetube.core.NTClient;
import net.noisetube.core.javame.MainMIDlet;
import net.noisetube.audio.calibration.Calibration;


/**
 * Account information
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class JavaMEPreferences extends Preferences
{

	static private final String STORE_NAME = "noisetube" + MainMIDlet.CLIENT_VERSION + (MainMIDlet.RESEARCH_BUILD ? "R" : "");
	private static final String NOT_LOGGED_IN = "notloggedin";
	private static final String UNCHANGED_CALIBRATION = "unchangedCalibration";

	//SETTINGS												CAN BE STORED?
	private boolean blockScreensaver = false;				//YES
	private boolean useLightGUI = false;					//YES
	
	
	/**
	 * @param device
	 */
	public JavaMEPreferences(JavaMEDevice device)
	{
		super(device); //sets defaults, loads stored pref's & checks/corrects consistency
	}
	
	/**
	 * Special constructor to override Pref's/Device for experiments
	 * 
	 * @param device
	 * @param account
	 * @param calibration
	 * @param audioSpecification
	 */
	public JavaMEPreferences(JavaMEDevice device, NTAccount account, Calibration calibration)
	{
		this(device); //!!!
		this.account = account;
		this.calibration = calibration;
	}
	
	protected void setAdditionalDefaults()
	{
		if(isUseGPS())
			if(((JavaMEDevice) device).getBrandID() == JavaMEDevice.BRAND_SONYERICSSON)
				setUseCoordinateInterpolation(true);
		blockScreensaver = false;
		useLightGUI = false;
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
	
	/**
	 * @return the useLightGUI
	 */
	public boolean isUseLightGUI()
	{
		return useLightGUI;
	}

	/**
	 * @param useLightGUI the useLightGUI to set
	 */
	public void setUseLightGUI(boolean useLightGUI)
	{
		this.useLightGUI = useLightGUI;
	}

	/**
	 * save the preferences
	 * 
	 * @return
	 */
	public void saveToStorage()
	{
		RecordStore store = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutput dos = new DataOutputStream(bos);
		try
		{
			log.debug("Saving preferences");
			writeSettingsTo(dos);
			try
			{
				RecordStore.deleteRecordStore(STORE_NAME);
			}
			catch(Exception ignore) {}
			store = RecordStore.openRecordStore(STORE_NAME, true);
			final byte[] bits = bos.toByteArray();
			store.addRecord(bits, 0, bits.length);
			log.debug("Preferences saved");
		}
		catch(Exception ex)
		{
			log.error("Exception upon saving preferences: " + ex.getMessage());
		}
		finally
		{
			try
			{
				store.closeRecordStore();
			}
			catch(Exception ignore) {}
		}
	}
	
	/**
	 * load the preferences
	 * 
	 * @return
	 */
	public void loadFromStorage()
	{
		RecordStore store = null;
		try
		{
			log.debug("Loading preferences");
			store = RecordStore.openRecordStore(STORE_NAME, false);
			final RecordEnumeration en = store.enumerateRecords(null, null, false);
			if(!en.hasNextElement())
				log.debug("No previous preferences found");
			final ByteArrayInputStream bis = new ByteArrayInputStream(en.nextRecord());
			final DataInputStream dis = new DataInputStream(bis);
			readSettingsFrom(dis);
			log.debug("Preferences loaded");
		}
		catch(Exception ex)
		{
			log.error("Exception upon loading preferences: " + ex.getMessage());
		}
		finally
		{
			try
			{
				store.closeRecordStore();
			}
			catch(Exception ignore) {}
		}
	}
		
	protected void writeSettingsTo(DataOutput dos) throws IOException
	{
		//Account
		if(account != null)
			dos.writeUTF(account.serialise());
		else
			dos.writeUTF(NOT_LOGGED_IN);
		
		//IO
		dos.writeBoolean(isPreferMemoryCard());
		dos.writeInt(getSavingMode());
		dos.writeBoolean(isAlsoSaveToFileWhenInHTTPMode());
		
		//AUDIO
		if(calibration != null && calibration.isManuallyChanged())
			dos.writeUTF(calibration.toXML());
		else
			dos.writeUTF(UNCHANGED_CALIBRATION);
		dos.writeBoolean(isUseDoseMeter());
		
		//GPS
		dos.writeBoolean(isUseGPS());
		dos.writeBoolean(isForceGPS());
		dos.writeBoolean(isUseCoordinateInterpolation());
		
		//Java ME specific
		dos.writeBoolean(isBlockScreensaver());
	}
	
	protected void readSettingsFrom(DataInputStream dis) throws IOException
	{
		//Account
		String acc = dis.readUTF();
		if(acc != null && !acc.equals("") && !acc.equals(NOT_LOGGED_IN))
			account = NTAccount.deserialise(acc);
		
		//IO
		setPreferMemoryCard(dis.readBoolean());
		setSavingMode(dis.readInt());
		setAlsoSaveToFileWhenInHTTPMode(dis.readBoolean());
		
		//AUDIO
		String calXML = dis.readUTF();
		if(calXML != null && !calXML.equals("") && !calXML.equals(UNCHANGED_CALIBRATION))
		{	
			Calibration parsedCal = NTClient.getInstance().getCalibrationParser().parseCalibration(calXML, Calibration.SOURCE_USER_PREFERECES);
			if(parsedCal != null)
				calibration = parsedCal;
		}
		setUseDoseMeter(dis.readBoolean());
		
		//GPS
		setUseGPS(dis.readBoolean());
		setForceGPS(dis.readBoolean());
		setUseCoordinateInterpolation(dis.readBoolean());
		
		//Java ME specific
		setBlockScreensaver(dis.readBoolean());
	}
	
}
