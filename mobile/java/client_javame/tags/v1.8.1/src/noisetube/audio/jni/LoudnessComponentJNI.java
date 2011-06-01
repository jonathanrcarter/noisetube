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

package noisetube.audio.jni;

import noisetube.audio.AbstractLoudnessComponent;
import noisetube.audio.ILeqListener;
import noisetube.audio.ILoudnessComponent;
import noisetube.audio.java.IStreamRecorder;

/**
 * Loudness interface for C++ Implementation (JNI)
 * 
 * @author maisonneuve
 * 
 */
public class LoudnessComponentJNI extends AbstractLoudnessComponent implements
		ILoudnessComponent, ILeqListener
{

	protected IStreamRecorder getRecorder()
	{
		NativeRecorder r = new NativeRecorder();
		r.setLeqListener(this);
		r.setRecordingTime((int) recordingTime);
		r.setTimeInterval(0);
		return r;
	}

	// for native recording
	public void sendLeq(double leq)
	{
		leq += 100;
		if(corrected)
		{
			leq = correct_leq(leq);
		}
		if(leq > 0)
		{
			listener.sendLeq(leq);
		}
		else
			log.error("error: negative leq: " + leq);
	}
}
