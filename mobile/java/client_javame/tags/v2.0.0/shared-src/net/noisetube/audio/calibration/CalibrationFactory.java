/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation)
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

package net.noisetube.audio.calibration;

import java.util.Enumeration;
import java.util.Vector;

import net.noisetube.config.Device;
import net.noisetube.config.Preferences;
import net.noisetube.core.NTClient;
import net.noisetube.io.FileIO;
import net.noisetube.io.FileWriter;
import net.noisetube.io.NTWebAPI;
import net.noisetube.util.Logger;
import net.noisetube.util.XMLUtils;


/**
 * Factory class for Calibrations, using various sources to get calibration specifications
 * 
 * @author mstevens 
 */
public class CalibrationFactory
{

	//STATIC---------------------------------------------------------
	private static NTClient client = NTClient.getInstance();
	private static Logger log = Logger.getInstance();

	private static final String CALIBRATIONS_XML_FILENAME = "calibrations.xml";

	private static final String DEFAULT_CALIBRATION_XML_NODE = //TODO use average of all 11 Nokia 5230's OR make a brand/model agnostic one (w/o brand and model attributes)
		"<calibration deviceBrandID=\"2\" deviceBrand=\"Nokia\" deviceModel=\"5230\" credibilityIndex=\"B\" overallDefault=\"true\" brandDefault=\"true\">\n	<creator>BrusSense-VUB</creator>\n	<comment>\n		Calibration done by Ellie D'Hondt in summer-autumn 2010 in an anechoic chamber at the VUB. Note values of phone #0 (TODO: use \"average\" over all 11 phones).\n	</comment>	\n	<correction input=\"24.64\" output=\"30.0\"/>\n	<correction input=\"25.83\" output=\"35.0\"/>\n	<correction input=\"27.24\" output=\"40.1\"/>\n	<correction input=\"29.05\" output=\"44.9\"/>\n	<correction input=\"35.0\" output=\"50.1\"/>\n	<correction input=\"43.34\" output=\"55.0\"/>\n	<correction input=\"51.84\" output=\"60.05\"/>\n	<correction input=\"59.95\" output=\"65.05\"/>\n	<correction input=\"68.51\" output=\"70.05\"/>\n	<correction input=\"72.92\" output=\"75.1\"/>\n	<correction input=\"77.02\" output=\"79.95\"/>\n	<correction input=\"81.2\" output=\"84.95\"/>\n	<correction input=\"85.18\" output=\"89.95\"/>\n	<correction input=\"87.25\" output=\"95.0\"/>\n	<correction input=\"88.84\" output=\"100.0\"/>\n	<correction input=\"90.18\" output=\"103.8\"/>\n</calibration>";

	//DYNAMIC--------------------------------------------------------
	private Device device;
	private CalibrationsList calibrationsList = null;

	public CalibrationFactory()
	{
		this.device = client.getDevice();
		Preferences preferences = client.getPreferences();
		
		//Initialize the calibrations list, trying several approaches...
		ICalibrationsParser parser = client.getCalibrationParser();
		
		//First, try to download calibrations list from the NoiseTube server		
		if(device.supportsInternetAccess())
			calibrationsList = parser.parseList((new NTWebAPI()).getCalibrationsXML(), Calibration.SOURCE_DOWNLOADED);

		//Second, if that didn't work, try to parse a previously downloaded copy of the list
		if(calibrationsList == null /*!!!*/ && device.supportsFileAccess() && preferences != null && preferences.getDataFolderPath() != null)
			calibrationsList = parser.parseList(client.getFileInputStream(preferences.getDataFolderPath() + CALIBRATIONS_XML_FILENAME), Calibration.SOURCE_PREVIOUSLY_DOWNLOADED);

		//Third, if that also failed, use the calibrations list in the application resources (may not up-to-date)
		if(calibrationsList == null) //!!!
			calibrationsList = parser.parseList((client.getResourceReader("/" + CALIBRATIONS_XML_FILENAME)).getInputStream(), Calibration.SOURCE_RESOURCE);
		
		if(calibrationsList != null)
		{
			log.info(" - Got " + calibrationsList.getCount() + " available calibrations from source: " + Calibration.getSourceString(calibrationsList.getSource()));
			if(calibrationsList.getSource() == Calibration.SOURCE_DOWNLOADED && device.supportsFileAccess() && preferences != null)
				calibrationsList.saveToFile(); //save downloaded calibrations to local file
		}
		else
			log.error("Unable to get calibrations from website, filesystem or application resources");
	}

	public Calibration getCalibration()
	{
		return getCalibration(device.getBrand(), device.getModel());
	}

	public Calibration getCalibration(String brand, String model)
	{
		if(calibrationsList != null)
		{	//a CalibrationsList was successfully parsed from either the website, the filesystem or the application resources
			Calibration overallDefault = null;
			Calibration brandDefault = null;
			//First try to select fitting calibration (brand and model should match), meanwhile look for brand- and overall defaults
			Enumeration cEnum = calibrationsList.getCalibrations().elements();
			while(cEnum.hasMoreElements())
			{
				Calibration c = (Calibration) cEnum.nextElement();
				if(brand != null && brand.equalsIgnoreCase(c.getDeviceBrand()))
				{
					if(model != null && model.equalsIgnoreCase(c.getDeviceModel()))
						return c; //brand and model match
					if(c.canBeUsedAsBrandDefault())
						brandDefault = c; //found default for this brand
				}
				if(c.canBeUsedAsOverallDefault())
					overallDefault = c;	//found overall default
			}
			//No fitting calibration found, use the brand default if there is one
			if(brandDefault != null)
				return brandDefault;
			//Still no fitting calibration found, use the "overall default" if there is one
			if(overallDefault != null)
				return overallDefault;
			//STILL nothing found? --> something really went wrong (source file didn't include an overall default)...
			throw new IllegalStateException("No (fitting) calibration found!");
		}
		else
		{	//No calibrations list could be parsed, we we will use the single hard-coded calibration (DEFAULT_CALIBRATION_XML_NODE)
			Calibration defaultCal = client.getCalibrationParser().parseCalibration(DEFAULT_CALIBRATION_XML_NODE, Calibration.SOURCE_HARDCODED);
			if(defaultCal != null)
			{
				defaultCal.setCanBeUsedAsOverallDefault(true); //just to be sure
				return defaultCal;
			}
			else
				throw new IllegalStateException("Calibration fetching failed completely"); //this should never happen
		}
	}
	
	public Calibration getDummyCalibation()
	{
		return getDummyCalibation(device.getBrand(), device.getModel());
	}

	public Calibration getDummyCalibation(String brand, String model)
	{
		return new Calibration(brand, model, Calibration.CREDIBILITY_INDEX_X, true, true, Calibration.SOURCE_DUMMY);
	}

	public static class CalibrationsList
	{

		private int source;
		private String lastChanged;
		private Vector calibrations;

		/**
		 * @param lastChanged
		 * @param calibrations
		 */
		public CalibrationsList(int source, String lastChanged, Vector calibrations)
		{
			this.source = source;
			this.lastChanged = lastChanged;
			this.calibrations = calibrations;
		}

		public void saveToFile()
		{
			NTClient client = NTClient.getInstance();
			if(client.getPreferences().getDataFolderPath() == null)
				return;
			try
			{
				FileWriter calXMLWriter = client.getUTF8FileWriter(client.getPreferences().getDataFolderPath() + CALIBRATIONS_XML_FILENAME);
				calXMLWriter.open(FileIO.FILE_EXISTS_STRATEGY_REPLACE, FileIO.FILE_DOES_NOT_EXIST_STRATEGY_CREATE);
				calXMLWriter.writeLine(XMLUtils.header());
				calXMLWriter.writeLine("<calibrations lastChanged=\"" + getLastChanged() + "\">");
				Enumeration cEnum = calibrations.elements();
				while(cEnum.hasMoreElements())
				{
					Calibration c = (Calibration) cEnum.nextElement();
					calXMLWriter.write(c.toXML(1) + "\n");
				}
				calXMLWriter.writeLine("</calibrations>");
				calXMLWriter.dispose(); //closes the file
			}
			catch(Exception e)
			{
				log.error("Could not write calibrations to file: " + e.getMessage());
			}		
		}
		
		/**
		 * @return the source
		 */
		public int getSource()
		{
			return source;
		}

		/**
		 * @return the lastChanged
		 */
		public String getLastChanged()
		{
			return lastChanged;
		}

		/**
		 * @return the calibrations
		 */
		public Vector getCalibrations()
		{
			return calibrations;
		}
		
		/**
		 * @return the number of available calibrations
		 */
		public int getCount()
		{
			return calibrations.size();
		}

	}

}
