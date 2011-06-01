/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Android version)
 *  
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011
 *  Android port by Vrije Universiteit Brussel (BrusSense team), 2010-2011
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

package net.noisetube.audio.calibration.android;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.noisetube.audio.calibration.Calibration;

import net.noisetube.audio.calibration.Calibration.CorrectionPair;
import net.noisetube.audio.calibration.CalibrationFactory.CalibrationsList;
import net.noisetube.audio.calibration.ICalibrationsParser;
import net.noisetube.util.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * *DOM based Calibrations XML parser
 * 
 * @author sbarthol, mstevens
 */
public class DOMCalibrationsParser implements ICalibrationsParser
{

	private String lastChanged;
	private Vector parsedCalibrations;	

	public CalibrationsList parseList(InputStream is, int source)
	{		
		if(is == null)
			return null;
		try
		{
			lastChanged = null;
			parsedCalibrations = new Vector();

			//Prepare for parsing:
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			
			//Parse:
			Document doc = dBuilder.parse(is);
			
			//Calibrations element
			Element calibrationsE = (Element) doc.getElementsByTagName("calibrations").item(0);
			lastChanged = calibrationsE.getAttribute("lastChanged");
						
			//Calibration elements
			NodeList calibrationL = doc.getElementsByTagName("calibration");
			for(int i = 0; i < calibrationL.getLength(); i++)
			{
				Calibration cal = parseCalibrationNode(calibrationL.item(i), source);
				if(cal != null)
					parsedCalibrations.addElement(cal);
			}
			
			//Return list...
			if(!parsedCalibrations.isEmpty()) //at least 1 calibration parsed --> return list
				return new CalibrationsList(source, lastChanged, parsedCalibrations);
			else
				return null;
		}
		catch(Exception e)
		{
			Logger.getInstance().error(e, "Error upon parsing Calibrations XML from source: " + Calibration.getSourceString(source));
			return null;
		}
		finally
		{
			try
			{
				is.close();
			}
			catch(IOException ignore) {}
		}
	}

	public Calibration parseCalibration(String singleCalibrationAsXML, int source)
	{
		try
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new ByteArrayInputStream(singleCalibrationAsXML.getBytes("UTF-8")));
			return parseCalibrationNode(doc.getElementsByTagName("calibration").item(0), source);
		}
		catch(Exception e)
		{
			Logger.getInstance().error(e, "Error upon parsing Calibration XML node from source: " + Calibration.getSourceString(source));
			return null;
		}
	}
	
	private Calibration parseCalibrationNode(Node calibrationNode, int source)
	{
		if(calibrationNode.getNodeType() != Node.ELEMENT_NODE)
			return null;
		try
		{
			Element calibrationE = (Element) calibrationNode;
			String brandAttr = calibrationE.getAttribute("deviceBrand");
			String brand = (brandAttr != null && !brandAttr.trim().equals("") ? brandAttr.trim() : null);
			String modelAttr = calibrationE.getAttribute("deviceModel");
			String model = (modelAttr != null && !modelAttr.trim().equals("") ? modelAttr.trim() : null);
			String brandDefaultAttr = calibrationE.getAttribute("brandDefault");  
			String overallDefaultAttr = calibrationE.getAttribute("overallDefault");
			//rest of attributes and instantiate Calibration:
			Calibration calib = new Calibration(brand,
												model,
												calibrationE.getAttribute("credibilityIndex").trim().toUpperCase().charAt(0),
												(brandDefaultAttr != null && brandDefaultAttr.trim().equalsIgnoreCase("true") && brand != null ? true : false),
												(overallDefaultAttr != null && overallDefaultAttr.trim().equalsIgnoreCase("true") ? true : false),
												source);
			//Add corrections:
			NodeList correctionL = calibrationE.getElementsByTagName("correction");
			for(int j = 0; j < correctionL.getLength(); j++)
			{
				Node correctionN = correctionL.item(j);
				if (correctionN.getNodeType() == Node.ELEMENT_NODE)
				{
					Element correctionE = (Element) correctionN;
					calib.addCorrectionPair(new CorrectionPair(	Double.parseDouble(correctionE.getAttribute("input").trim()),
																Double.parseDouble(correctionE.getAttribute("output").trim())));
				}
			}
			try
			{	//Add comment:
				calib.setComment(((Element) calibrationE.getElementsByTagName("comment").item(0)).getFirstChild().getNodeValue().trim());
			}
			catch(Exception ignore) { } //there is no comment tag
			try
			{	//Add creator:
				calib.setCreator(((Element) calibrationE.getElementsByTagName("creator").item(0)).getFirstChild().getNodeValue().trim());
			}
			catch(Exception ignore) { } //there is no creator tag
			return calib;
		}
		catch(Exception e)
		{
			Logger.getInstance().error(e, "Error upon parsing Calibration node from source: " + Calibration.getSourceString(source));
			return null;
		}
	}
	
}