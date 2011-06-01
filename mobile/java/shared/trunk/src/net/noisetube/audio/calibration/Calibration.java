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
import net.noisetube.core.NTClient;
import net.noisetube.util.CustomStringBuffer;
import net.noisetube.util.Logger;
import net.noisetube.util.Sort;


/**
 * @author mstevens, maisonneuve
 * 
 */
public class Calibration
{
	
	//STATICS------------------------------------------------------------------
	public static final int SOURCE_NONE = -1;
	public static final int SOURCE_DOWNLOADED = 0;
	public static final int SOURCE_PREVIOUSLY_DOWNLOADED = 1;
	public static final int SOURCE_RESOURCE = 2;
	public static final int SOURCE_HARDCODED = 3;
	public static final int SOURCE_HARDCODED_FOR_EXPERIMENT = 4;
	public static final int SOURCE_DUMMY = 5;
	public static final int SOURCE_USER_PREFERECES = 6;
	
	public static final char CREDIBILITY_INDEX_A = 'A'; //same as B but specific to a single physical instance of a device model
	public static final char CREDIBILITY_INDEX_B = 'B'; //internal (=SonyCSL/VUB) professional in ideal conditions (verified)
	public static final char CREDIBILITY_INDEX_C = 'C'; //internal professional (verified)
	public static final char CREDIBILITY_INDEX_D = 'D'; //internal professional (unverified)
	public static final char CREDIBILITY_INDEX_E = 'E'; //external professional (unverified)
	public static final char CREDIBILITY_INDEX_F = 'F'; //end user (unverified)
	public static final char CREDIBILITY_INDEX_G = 'G'; //brand match, model mismatch
	public static final char CREDIBILITY_INDEX_H = 'H'; //brand and model mismatch (default used)
	public static final char CREDIBILITY_INDEX_X = 'X'; //no calibration at all
	
	
	public static String getSourceString(int source)
	{
		switch(source)
		{
			case SOURCE_NONE : return "unknown";
			case SOURCE_DOWNLOADED : return "downloaded from NoiseTube.net";
			case SOURCE_PREVIOUSLY_DOWNLOADED : return "Saved previous download from NoiseTube.net";
			case SOURCE_RESOURCE : return "loaded from resources";
			case SOURCE_HARDCODED : return "hard-coded";
			case SOURCE_HARDCODED_FOR_EXPERIMENT : return "hard-coded for experiment";
			case SOURCE_DUMMY : return "hard-coded non-functional dummy";
			case SOURCE_USER_PREFERECES : return "user preferences";
			default : return "unknown";
		}
	}
		
	//DYNAMICS-----------------------------------------------------------------
	private String deviceBrand;
	private String deviceModel;
	private boolean canBeUsedAsBrandDefault = false;
	private boolean canBeUsedAsOverallDefault = false;
	private char credibilityIndex;
	private int source = SOURCE_NONE;
	private String creator;
	private String comment;
	private Vector/*<CorrectionPair>*/ correctionPairs;
	private boolean manuallyChanged = false;
	
	public Calibration(double[][] calibrationArray, char credibilityIndex, int source)
	{
		this(null, null, calibrationArray, credibilityIndex, source);
	}
	
	public Calibration(String deviceBrand, String deviceModel, double[][] calibrationArray, char credibilityIndex, int source)
	{
		this(deviceBrand, deviceModel, credibilityIndex, false, false, source);
		for(int i = 0; i < calibrationArray.length; i++)
			correctionPairs.addElement(new CorrectionPair(calibrationArray[i][Corrector.INPUT_IDX], calibrationArray[i][Corrector.OUTPUT_IDX]));
	}
	
	public Calibration(String deviceBrand, String deviceModel, char credibilityIndex, boolean canBeUsedAsBrandDefault, boolean canBeUsedAsOverallDefault, int source)
	{
		this.deviceBrand = deviceBrand;
		this.deviceModel = deviceModel;
		if(credibilityIndex >= CREDIBILITY_INDEX_A && credibilityIndex <= CREDIBILITY_INDEX_H || credibilityIndex == CREDIBILITY_INDEX_X)
			this.credibilityIndex = credibilityIndex;
		else
			Logger.getInstance().error("Invalid credibilityIndex: " + credibilityIndex);
		this.source = source;
		this.canBeUsedAsBrandDefault = canBeUsedAsBrandDefault;
		this.canBeUsedAsOverallDefault = canBeUsedAsOverallDefault;
		correctionPairs = new Vector();
	}
	
	/**
	 * @return the deviceBrand
	 */
	public String getDeviceBrand()
	{
		return deviceBrand;
	}
	
	/**
	 * @return the deviceModel
	 */
	public String getDeviceModel()
	{
		return deviceModel;
	}
	
	/**
	 * @return the creator
	 */
	public String getCreator()
	{
		return creator;
	}
	
	/**
	 * @param creator the creator to set
	 */
	public void setCreator(String creator)
	{
		this.creator = creator;
	}

	/**
	 * @return the comment
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment)
	{
		this.comment = comment;
	}
	
	/**
	 * @return the source
	 */
	public int getSource()
	{
		return source;
	}

	/**
	 * @return the credibilityIndex
	 */
	public char getCredibilityIndex()
	{
		return credibilityIndex;
	}
	
	/**
	 * @return the credibilityIndex
	 */
	public char getEffeciveCredibilityIndex()
	{
		char effective = credibilityIndex;
		Device device = NTClient.getInstance().getDevice();
		if(device.getBrand() == null || device.getModel() == null)
			effective = CREDIBILITY_INDEX_H; //brand/model unknown
		else
		{
			if(this.deviceBrand == null || !device.getBrand().equalsIgnoreCase(this.deviceBrand))
				effective = CREDIBILITY_INDEX_H; //brand mismatch (model too most likely)
			else if(this.deviceModel == null || !device.getModel().equalsIgnoreCase(this.deviceModel))
				effective = CREDIBILITY_INDEX_G; //model mismatch (but brand matched)
		}
		return (char) Math.max(credibilityIndex, effective); //NOT Math.min (we are comparing characters)
	}
	
	/**
	 * @return the correctionPairs
	 */
	public Vector getCorrectionPairs()
	{
		return correctionPairs;
	}

	/**
	 * @return the canBeUsedAsBrandDefault
	 */
	public boolean canBeUsedAsBrandDefault()
	{
		return canBeUsedAsBrandDefault;
	}
	
	/**
	 * @param canBeUsedAsBrandDefault the canBeUsedAsBrandDefault to set
	 */
	public void setCanBeUsedAsBrandDefault(boolean canBeUsedAsBrandDefault)
	{
		this.canBeUsedAsBrandDefault = canBeUsedAsBrandDefault;
	}
	
	/**
	 * @return the canBeUsedAsOverallDefault
	 */
	public boolean canBeUsedAsOverallDefault()
	{
		return canBeUsedAsOverallDefault;
	}

	/**
	 * @param canBeUsedAsOverallDefault the canBeUsedAsOverallDefault to set
	 */
	public void setCanBeUsedAsOverallDefault(boolean canBeUsedAsOverallDefault)
	{
		this.canBeUsedAsOverallDefault = canBeUsedAsOverallDefault;
	}

	/**
	 * @return the manuallyChanged
	 */
	public boolean isManuallyChanged()
	{
		return manuallyChanged;
	}
	
	/**
	 * Only for use in the parsers!
	 * 
	 * @param cPair
	 */
	public void addCorrectionPair(CorrectionPair cPair)
	{
		addCorrectionPair(cPair, null);
	}
	
	public void addCorrectionPair(CorrectionPair cPair, String username)
	{
		correctionPairs.addElement(cPair);
		if(username != null)
			manuallyChanged(username);
	}
	
	public void removeCorrectionPair(CorrectionPair cPair, String username)
	{
		correctionPairs.removeElement(cPair);
		manuallyChanged(username);
	}
	
	private void manuallyChanged(String byUser)
	{
		if(!manuallyChanged)
		{
			comment = "Created by user " + byUser + " based on " + this.toString();
			creator = byUser;
			source = SOURCE_USER_PREFERECES;
			deviceBrand = NTClient.getInstance().getDevice().getBrand();
			deviceModel = NTClient.getInstance().getDevice().getModel();
			credibilityIndex = CREDIBILITY_INDEX_F; //end user made
			this.manuallyChanged = true;
		}
	}
	
	public String toString()
	{
		return 	"Calibration for " + (deviceBrand != null ? (deviceBrand + " " + (deviceModel != null ? deviceModel : "(generic model)")) : "unknown device")
				+ " (source: " + getSourceString(source) + ")";
	}
	
	public String toXML()
	{
		return toXML(0);
	}
	
	public String toXML(int tabs)
	{
		CustomStringBuffer bff = new CustomStringBuffer();
		bff.appendTabbed("<calibration", tabs);
		if(deviceBrand != null)
			bff.append(" deviceBrand=\"" + deviceBrand + "\"");
		if(deviceModel != null)
			bff.append(" deviceModel=\"" + deviceModel + "\"");	
		bff.append(" credibilityIndex=\"" + credibilityIndex + "\"");
		if(canBeUsedAsOverallDefault)
			bff.append(" overallDefault=\"true\"");
		if(canBeUsedAsBrandDefault)
			bff.append(" brandDefault=\"true\"");
		bff.appendLine(">");
		if(creator != null && !creator.equals(""))
			bff.appendTabbedLine("<creator>" + creator + "</creator>", tabs+1);
		if(comment != null && !comment.equals(""))
		{
			bff.appendTabbedLine("<comment>", tabs+1);
			bff.appendTabbedLine(comment, tabs+2);
			bff.appendTabbedLine("</comment>", tabs+1);
		}
		Enumeration en = correctionPairs.elements();
		while(en.hasMoreElements())
			bff.appendTabbedLine(((CorrectionPair) en.nextElement()).toXML(), tabs+1);
		bff.appendTabbedLine("</calibration>", tabs);
		return bff.toString();
	}

	public Corrector getCorrector()
	{
		if(correctionPairs.isEmpty())
			return null;
		double[][] calibrationArray = new double[correctionPairs.size()][2];
		Sort.quickSort(correctionPairs); //!!!
		Enumeration en = correctionPairs.elements();
		int i = 0;
		while(en.hasMoreElements())
		{
			CorrectionPair pair = (CorrectionPair) en.nextElement();
			calibrationArray[i][Corrector.OUTPUT_IDX] = pair.output;
			calibrationArray[i][Corrector.INPUT_IDX] = pair.input;
			i++;
		}
		return new Corrector(calibrationArray);
	}
	
	public static class CorrectionPair implements net.noisetube.util.Comparable
	{
		private double input;	//phone
		private double output; 	//ref
		
		public CorrectionPair(double input, double output)
		{
			this.input = input;
			this.output = output;
		}
		
		public int compareTo(Object a)
		{
			CorrectionPair o = (CorrectionPair) a;
			if(this.output == o.output)
				return 0;
			if(this.output > o.output)
				return 1;
			return -1;
		}
		
		public String toXML()
		{
			return "<correction input=\"" + Double.toString(input) + "\" output=\"" + Double.toString(output) + "\"/>";
		}
		
	}
	
	public class Corrector
	{
		
		//STATICS------------------------------------------------------------------
		public final static int INPUT_IDX = 0;
		public final static int OUTPUT_IDX = 1; 
		
		
		//DYNAMICS-----------------------------------------------------------------
		private double[][] dBACalibrationArray;
		private double[][] dBCalibrationArray; //needed?
		
		
		public Corrector(double[][] dBACalibrationArray /*, double[][] dBCalibrationArray*/) //TODO use separate values for DB correction?
		{
			this.dBACalibrationArray = dBACalibrationArray;
			this.dBCalibrationArray = dBACalibrationArray; //use same values for now
		}
		
		public double correctDB(double leq)
		{
			return correctLeq(dBCalibrationArray, leq); 
		}

		public double correctDBA(double leq)
		{
			return correctLeq(dBACalibrationArray, leq);
		}

		private double correctLeq(double[][] calibrationArray, double leq)
		{
			int i = 0;
			while(i < calibrationArray.length && leq > calibrationArray[i][INPUT_IDX])
				i++;
			if(i == calibrationArray.length)
				i--;
			double ratio, constant;
			if(i == 0)
			{
				ratio = (calibrationArray[i][OUTPUT_IDX] - 0) / (calibrationArray[i][INPUT_IDX] - 0);
			}
			else
			{
				ratio = (calibrationArray[i][OUTPUT_IDX] - calibrationArray[i - 1][OUTPUT_IDX])
						/ (calibrationArray[i][INPUT_IDX] - calibrationArray[i - 1][INPUT_IDX]);
			}
			constant = calibrationArray[i][OUTPUT_IDX] - ratio * calibrationArray[i][INPUT_IDX];
			return(leq * ratio + constant);
		}
		
		/**
		 * @return the dBACalibrationArray
		 */
		public double[][] getdBACalibrationArray()
		{
			return dBACalibrationArray;
		}

		/**
		 * @return the dBCalibrationArray
		 */
		public double[][] getdBCalibrationArray()
		{
			return dBCalibrationArray;
		}

		public String toString()
		{
			StringBuffer bff = new StringBuffer();
			bff.append("dB(A) correction values: [");
			for(int i = 0; i < dBACalibrationArray.length; i++)
				bff.append((i == 0 ? "" : "; ") + "(" + dBACalibrationArray[i][INPUT_IDX] + ", " + dBACalibrationArray[i][OUTPUT_IDX] + ")");
			//bff.append("\ndB correction values: [");
			//for(int i = 0; i < dBCalibrationArray.length; i++)
			//	bff.append((i == 0 ? "" : "; ") + "(" + dBCalibrationArray[i][INPUT_IDX] + ", " + dBCalibrationArray[i][OUTPUT_IDX] + ")");
			return bff.toString();
		}
		
	}

}
