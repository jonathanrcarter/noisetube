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

package net.noisetube.audio.android;

import net.noisetube.audio.AudioComponent;
import net.noisetube.audio.calibration.Calibration;
import net.noisetube.audio.format.AudioSpecification;
import net.noisetube.model.IMeasurementListener;
import net.noisetube.model.Measurement;

/**
 * @author sbarthol, mstevens
 * 
 * Adds frequency spectrum analysis (prototype implementation)
 *
 */
public class AndroidAudioComponent extends AudioComponent
{

	int loop = 0;
	//private KJFFT fastFourierTransformer;


	public AndroidAudioComponent(AudioSpecification audioSpec, IMeasurementListener listener) throws Exception
	{
		super(audioSpec, listener, null);
	}

	public AndroidAudioComponent(AudioSpecification audioSpec, IMeasurementListener listener, Calibration calibration) throws Exception
	{
		super(audioSpec, listener, calibration);
	}

	@Override
	protected void analyseSamples(double[] samples, Measurement measurement) throws Exception
	{
		//SPECTRUM
		//We reduce the recorded samples to 100 samples and then store them
		//measurement.setReducedSamples(reduceSamples(samples));
		//TODO move actual analysis over here (it's in the GUI code now)

		//LOUDNESS (we do this last because A-filtering changes the sample values):
		super.analyseSamples(samples, measurement);
	}

	/**
	 * reduceSamples makes a new double[][] array with less samples than the original one.
	 * The algorithm works as follows: 
	 * We reduce the samples by only storing the peek of intervals from the provided samples.
	 * So we will try to find the peek in some intervals taken from the source,
	 * and store these in a new array.
	 * We also store the size of the intervals, for every individual interval.
	 * The peek can be a lower- or upper-peek. Depending on the samples read.
	 * This means that data is inevitably lost when we have a high lower-peek and a high upper-
	 * peek, since only one of them is stored.  
	 * @param samples the array which will be reduced
	 * @return an array contain the peeks and the size of the interval they where taken from.
	 */
	/*
	private double[][] reduceSamples(double[] samples)
	{
		//TODO This reduction was a temporary ad-hoc solution and should be replaced.
		//e.g. [Peek of first 5 samples, peek of next 20 samples, peek of next 80 samples, ...]


		//The amount of intervals that should be stored
		int reducedSamplesLength = Measurement.REDUCED_SAMPLES_LENGTH;
		//The length of the original interval
		int remainingSamplesLenght = samples.length;

		double[][] reducedSamples = new double[reducedSamplesLength][];

		//to get a better effect, we store the "subtones" (= 5% lowest samples) separately.
		int subtonesIntervalLenght = (int) (remainingSamplesLenght * 0.05);
		double lPeek = 0;
		double uPeek = 0;
		for(int j = 0; j < subtonesIntervalLenght; j++)
		{
			if(samples[j]<lPeek)
				lPeek = samples[j];
			else if(samples[j]>uPeek)
				uPeek = samples[j];
		}

		reducedSamples[0] = new double[] {(lPeek*lPeek>uPeek*uPeek) ? lPeek : uPeek, subtonesIntervalLenght};
		//remainingSamplesLenght is the length minus the first 5% taken for the subtones
		remainingSamplesLenght = remainingSamplesLenght - subtonesIntervalLenght;
		//One sample has bees used for the five percent storage
		reducedSamplesLength--;

		//Jump is the amount of samples that will be used for each interval
		int jump = remainingSamplesLenght / reducedSamplesLength;
		//If the jump is rounded wrong (it is to big), make it smaller
		if((jump * reducedSamplesLength) + subtonesIntervalLenght >= samples.length)
			jump--;

		//For i going from 1 to Measurement.REDUCED_SAMPLES_LENGTH (= 100)
		for(int i = 0; i < reducedSamplesLength; i++)
		{
			double lowerPeek = 0;
			double upperPeek = 0;
			for(int j = i*jump + subtonesIntervalLenght; j < (i+1)*jump + subtonesIntervalLenght; j++)
			{
				if(samples[j]<lowerPeek)
					lowerPeek = samples[j];
				else if(samples[j]>upperPeek)
					upperPeek = samples[j];
			}
			//The reduction can cut a lot of detail: if in a certain interval their is a high lower- and
			//a high upper-peek, only one of them will not be stored.
			reducedSamples[i+1]= 
				new double[] {(lowerPeek*lowerPeek>upperPeek*upperPeek) ? lowerPeek : upperPeek, jump};
		}
		return reducedSamples;
	 }
	 */

}
