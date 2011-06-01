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

package noisetube.audio.java;

import java.util.Enumeration;
import java.util.Vector;

import noisetube.util.Logger;
import noisetube.util.Sort;

/**
 * @author maisonneuve, mstevens
 * 
 *         TODO add more brands/models TODO calibration setting sharing /
 *         downloading feature?
 */
public class Calibration
{

	// STATICS------------------------------------------------------------------
	private static Logger log = Logger.getInstance();
	public static int INPUT_IDX = 0;
	public static int OUTPUT_IDX = 1;

	// default of the defaults:
	double[][] DEFAULT;
	// DYNAMICS-----------------------------------------------------------------
	public double[][] corrector;
	public Vector corrector_vector;
	public boolean manuallyChanged = false;

	public Calibration(double[][] corrector)
	{
		DEFAULT = corrector;
		this.corrector = corrector;
		corrector_vector = generate_vector(corrector);
		//log.debug("Loaded previously saved calibration settings");
	}

	public void reset()
	{
		corrector = DEFAULT;
		corrector_vector = generate_vector(DEFAULT);
		manuallyChanged = true;
	}

	public void addCorrection(double ref, double mobile)
	{
		corrector_vector.addElement(new CalibValue(ref, mobile));
		sort();
		manuallyChanged = true;
	}

	public void removeCorrectionAt(int i)
	{
		corrector_vector.removeElementAt(i);
		sort();
		manuallyChanged = true;
	}

	public String toString()
	{
		String s = "";
		for(int i = 0; i < corrector.length; i++)
		{
			s += "(" + corrector[i][OUTPUT_IDX] + "," + corrector[i][INPUT_IDX]
					+ ")\n";
		}
		return s;
	}

	public double correctLeq(double leq)
	{
		return correctLeq(corrector, leq);
	}

	public double correctLeq(double[][] correction_parameters, double leq)
	{
		int i = 0;
		while(i < correction_parameters.length
				&& leq > correction_parameters[i][INPUT_IDX])
		{
			i++;
		}
		if(i == correction_parameters.length)
		{
			i--;
		}

		double ratio;
		double constant;

		if(i == 0)
		{
			ratio = (correction_parameters[i][OUTPUT_IDX] - 0)
					/ (correction_parameters[i][INPUT_IDX] - 0);
		}
		else
		{
			ratio = (correction_parameters[i][OUTPUT_IDX] - correction_parameters[i - 1][OUTPUT_IDX])
					/ (correction_parameters[i][INPUT_IDX] - correction_parameters[i - 1][INPUT_IDX]);
		}

		constant = correction_parameters[i][OUTPUT_IDX] - ratio
				* correction_parameters[i][INPUT_IDX];
		return(leq * ratio + constant);
	}

	private static Vector generate_vector(double[][] corrector)
	{
		Vector correctorVect = new Vector();
		for(int i = 0; i < corrector.length; i++)
		{
			correctorVect.addElement(new CalibValue(corrector[i][OUTPUT_IDX],
					corrector[i][INPUT_IDX]));
		}
		return correctorVect;
	}

	public void update()
	{
		corrector = toDoubleArray();
	}

	private void sort()
	{
		Object[] src = toArray();
		Object[] dest = toArray();
		Sort.mergeSort(src, dest, 0, dest.length, 0);
		for(int j = 0; j < dest.length; j++)
		{
			corrector_vector.setElementAt(dest[j], j);
		}
	}

	private double[][] toDoubleArray()
	{
		double[][] new_corrector = new double[corrector_vector.size()][2];
		Enumeration en = corrector_vector.elements();
		int i = 0;
		while(en.hasMoreElements())
		{
			CalibValue value = (CalibValue) en.nextElement();
			new_corrector[i][OUTPUT_IDX] = value.ref;
			new_corrector[i][INPUT_IDX] = value.phone;
			i++;
		}
		return new_corrector;
	}

	private Object[] toArray()
	{
		Object[] array = new Object[corrector_vector.size()];
		Enumeration en = corrector_vector.elements();
		int i = 0;
		while(en.hasMoreElements())
		{
			array[i] = en.nextElement();
			i++;
		}
		return array;
	}

	public void removeAll()
	{
		corrector_vector.removeAllElements();
		update();
		manuallyChanged = true;
	}
}
