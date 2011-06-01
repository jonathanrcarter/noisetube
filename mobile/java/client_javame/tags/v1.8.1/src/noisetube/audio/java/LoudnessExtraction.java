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

import noisetube.audio.LoudnessException;
import noisetube.util.MathME;
import noisetube.util.Logger;

/**
 * Loudness measure in db(a)
 * 
 * @author maisonneuve
 * 
 */
public class LoudnessExtraction
{

	//STATIC
	public static double LOW_RESPONSE = 1.0D;
	public static double FAST_RESPONSE = 0.125D;
	//private static double p0 = 0.000002d; //0.0001d	
	
	//DYNAMIC
	private double LeqInterval = LOW_RESPONSE;
	private boolean AfilterActive = true;

	protected Logger log = Logger.getInstance();

	public boolean isAfilterActive()
	{
		return AfilterActive;
	}

	public void setAfilterActive(boolean afilterActive)
	{
		AfilterActive = afilterActive;
	}

	public void setLeqDuration(long responseTime)
	{
		this.LeqInterval = responseTime;
	}

	public void switch_response_time()
	{
		LeqInterval = (LeqInterval == LOW_RESPONSE) ? FAST_RESPONSE : LOW_RESPONSE;
	}

	public LoudnessExtraction()
	{
	}

	public final double[] compute_leqs(byte[] buf) throws Exception
	{
		double[] leqs = null;
		AbstractSoundStream s = new SoundStreamNokia(buf);

		if(s.getSampleRate() <= 0)
		{
			throw new LoudnessException("no valid header (sample rate=0)");
		}

		double byte_per_sample = s.getBlockAlign();
		double samples_per_leq = 0;
		int nb_samples = (int) (s.getDataSize() / byte_per_sample);
		int nb_leq = 0;
		
		if(LeqInterval == -1)
		{	//no delimited interval , compute all the samples
			samples_per_leq = nb_samples;
			log.debug("All the samples (" + samples_per_leq + ")");
			nb_leq = 1;
			
		}
		else
		{	//else compute interval
			samples_per_leq = (int) (s.getSampleRate() * LeqInterval);
			//if(log.isDebug())
			//	log.debug("limited to " + samples_per_leq + " samples");
			nb_leq = (int) ((double) nb_samples / samples_per_leq);
		}

		if(nb_leq == 0)
		{
			throw new LoudnessException("Not enough samples (" + s.getDataSize() + ", " + samples_per_leq * byte_per_sample + ")");
		}

		leqs = new double[nb_leq];
		AFilter filter = AFilter.getAFilter((int) s.getSampleRate());

		//System.out.println("nbleq: "+nb_leq);
		
		//for each set of samples:
		for(int k = 0; k < nb_leq; k++)
		{

			//read samples
			int start = k * (int) samples_per_leq;
			int end = start + (int) (samples_per_leq - 1);
			double ad[];

			try
			{
				ad = s.readSamples2(start, end);
			}
			catch (Exception e)
			{
				throw new Exception("read samples: " + e.getMessage());
			}
			
			// try {
			if(AfilterActive)
			{	//Apply A-weighting filtering
				ad = filter.apply(ad);
			}

			// }
			// catch (Exception e)
			// {
			// 	throw new Exception("Exception in A-weighting filter: " + e.getMessage());
			// }

			//compute leq
			// try {
			leqs[k] = leq(ad);
			// } catch (Exception e) {
			// throw new Exception("leq: " + e.getMessage());
			// }

		}

		return leqs;
	}

	/**
	 * compute leq from the array of samples
	 * 
	 * @param samples : array of sound samples
	 * @return the leq
	 */
	private double leq(double samples[])
	{
		double d = 0.0D;
		for(int i = 0; i < samples.length; i++)
		{
			double d1 = samples[i]; // / p0
			d += d1 * d1;
		}
		d /= samples.length;
		return (10D * MathME.log10(d)) + 93.97940008672037609572522210551d; //TODO better than 100? and equivalent to dividing by p0
	}
	
//	private double leq2(double samples[]) //20*log(x) instead of 10*log(x^2) (more efficient?)
//	{
//		double d = 0.0D;
//		for(int i = 0; i < samples.length; i++)
//		{
//			d += samples[i] // / p0;
//		}
//		d /= (samples.length);
//		return 20D * Float11.log10(d);
//	}

	public double getLeqInterval()
	{
		return LeqInterval;
	}
}
