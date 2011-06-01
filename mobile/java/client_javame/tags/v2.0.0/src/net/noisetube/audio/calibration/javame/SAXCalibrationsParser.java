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

package net.noisetube.audio.calibration.javame;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;
import java.util.Vector;

import net.noisetube.audio.calibration.Calibration;
import net.noisetube.audio.calibration.Calibration.CorrectionPair;
import net.noisetube.audio.calibration.CalibrationFactory.CalibrationsList;
import net.noisetube.audio.calibration.ICalibrationsParser;
import net.noisetube.util.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX based Calibrations XML parser
 * 
 * @author mstevens
 */
public class SAXCalibrationsParser extends DefaultHandler implements ICalibrationsParser
{
	
	private static Logger log = Logger.getInstance();
	
	private int source;
	private String lastChanged;
	private Vector parsedCalibrations;
	private Calibration currentCalibration;
	
	private Stack elementStack;
	
	public CalibrationsList parseList(InputStream is, int source)
	{		
		if(is == null)
			return null;
		try
		{
			this.source = source;
			lastChanged = null;
			parsedCalibrations = new Vector();
			
			//Prepare for parsing:
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			
			//Parse:
			saxParser.parse(is, this);
			
			//Return list...
			if(!parsedCalibrations.isEmpty()) //at least 1 calibration parsed --> return list
				return new CalibrationsList(source, lastChanged, parsedCalibrations);
			else
				return null;
		}
		catch(Exception e)
		{
			log.error("Error upon parsing Calibrations XML from source " + source + ": " + e.getMessage());
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
			return (Calibration) parseList(new ByteArrayInputStream(singleCalibrationAsXML.getBytes("UTF-8")), source).getCalibrations().firstElement(); //if parseList returns null a NullPointException will be thrown but it will be caught below
		}
		catch(Exception e)
		{
			log.error("Error upon parsing Calibration XML node from source " + source + ": " + e.getMessage());
			return null;
		}
	}
	
	public void startDocument() throws SAXException
	{
		elementStack = new Stack();
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if(qName.equals("calibrations"))
		{
			lastChanged = attributes.getValue("lastChanged").trim();
		}
		else if(qName.equals("calibration"))
		{
			String brandAttr = attributes.getValue("deviceBrand");
			String brand = (brandAttr != null && !brandAttr.trim().equals("") ? brandAttr.trim() : null);
			String modelAttr = attributes.getValue("deviceModel");
			String model = (modelAttr != null && !modelAttr.trim().equals("") ? modelAttr.trim() : null);
			String brandDefaultAttr = attributes.getValue("brandDefault");  
			String overallDefaultAttr = attributes.getValue("overallDefault");
			//rest of attributes and instantiate Calibration:
			currentCalibration = new Calibration(	brand,
													model,
													attributes.getValue("credibilityIndex").trim().toUpperCase().charAt(0),
													(brandDefaultAttr != null && brandDefaultAttr.trim().equalsIgnoreCase("true") && brand != null ? true : false),
													(overallDefaultAttr != null && overallDefaultAttr.trim().equalsIgnoreCase("true") ? true : false),
													source);
			parsedCalibrations.addElement(currentCalibration);
		}
		else if(qName.equals("correction"))
		{
			currentCalibration.addCorrectionPair(new CorrectionPair(Double.parseDouble(attributes.getValue("input").trim()),
																	Double.parseDouble(attributes.getValue("output").trim())));
		}
		elementStack.push(qName);
	}

	public void characters(char[] ch, int start, int length) throws SAXException
	{
		String chars = new String(ch, start, length).trim();
		if (chars.length() > 0)
		{
			String qName = (String) elementStack.peek();
			if(qName.equals("creator"))
				currentCalibration.setCreator(chars);
			else if(qName.equals("comment"))
				currentCalibration.setComment(chars);
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if(!elementStack.empty()) //just to be sure
			elementStack.pop();
	}

	public void endDocument() throws SAXException
	{
	}

	public void error(SAXParseException e) throws SAXException
	{
		throw e;
	}

	public void fatalError(SAXParseException e) throws SAXException
	{
		throw e;
	}

	public void warning(SAXParseException e) throws SAXException
	{
		throw e;
	}
	
}
