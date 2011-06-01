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

import java.io.InputStream;

import net.noisetube.audio.calibration.CalibrationFactory.CalibrationsList;

/**
 * Calibrations XML parser interface
 * 
 * @author mstevens
 *
 */
public interface ICalibrationsParser
{
	
	/**
	 * @param InputStream of Calibrations xml file or Calibration node
	 * @param source
	 * @return a CalibrationsList object, containing Calibration objects parsed from supplied InputStream, or null when parsing was unsuccessful
	 */
	public abstract CalibrationsList parseList(InputStream is, int source);

	/**
	 * @param Calibration xml node as String
	 * @param source
	 * @return a calibrations object, parsed from supplied xml string, or null when parsing was unsuccessful
	 */
	public abstract Calibration parseCalibration(String singleCalibrationAsXML, int source);

}
