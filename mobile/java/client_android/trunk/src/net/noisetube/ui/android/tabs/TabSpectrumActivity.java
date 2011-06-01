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

package net.noisetube.ui.android.tabs;

import net.noisetube.R;
import net.noisetube.audio.android.KJFFT;
import net.noisetube.model.Measurement;
import net.noisetube.model.Track;
import net.noisetube.ui.android.tab.TabContainerActivity;
import net.noisetube.ui.android.view.SpectrumView;
import android.app.Activity;
import android.os.Bundle;

/**
 * @author sbarthol
 *
 */
public class TabSpectrumActivity extends Activity implements Tab
{

	private KJFFT fastFourierTransformer;
	private SpectrumView spectrumView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TabContainerActivity.getInstance().setActiveTab(this);
		setContentView(R.layout.tabspectrumactivity);
		/* Getting information concerning the child views */
		spectrumView = (SpectrumView) findViewById(R.id.spectrumView);
		this.fastFourierTransformer = new KJFFT(Measurement.REDUCED_SAMPLES_LENGTH);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		TabContainerActivity.getInstance().setActiveTab(this);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	private void setAsPrimary()
	{
		TabContainerActivity.getInstance().setPrimaryTab(instance);
	}

	public void newMeasurement(Track track) {
		Measurement newMeasurement = track.getNewestMeasurement();
		double[][] reducedSamplesAndIntervals = newMeasurement.getReducedSamples();
		//reducedSamples contains a list of averages of intervals of the original sample
		//Until now, these averages are made linear.
		//e.g. [avg samples [0-479], avg samples [480-959], ...]
		double[] reducedSamples = new double[reducedSamplesAndIntervals.length];
		for(int i = 0; i < reducedSamplesAndIntervals.length; i++)
		{
			reducedSamples[i] = reducedSamplesAndIntervals[i][0];
		}
		//pressureSamples contains the pressure on the average samples/reducedSamples.
		float pressureSamples[] = fastFourierTransformer.calculate(reducedSamples);

		//Since the pressureSamples is REDUCED_SAMPLES_LENGTH long
		//we will display the loudest value of the following intervals:
		//[first pressure sample - first pressure sample]
		//[second pressure sample - PRESSURE_SAMPLES_LENGHT * 1/10]
		//[PRESSURE_SAMPLES_LENGHT * 1/10 - PRESSURE_SAMPLES_LENGHT * 3/10]
		//[PRESSURE_SAMPLES_LENGHT * 3/10 - PRESSURE_SAMPLES_LENGHT * 6/10]
		//[PRESSURE_SAMPLES_LENGHT * 6/10 - PRESSURE_SAMPLES_LENGHT]
		int pressureSamplesLenght = pressureSamples.length;
		float loudestPressure1 = pressureSamples[0];
		float loudestPressure2 = findLoudestIntervalPressure(2, pressureSamplesLenght/10, pressureSamples);
		float loudestPressure3 = findLoudestIntervalPressure(pressureSamplesLenght/10, (pressureSamplesLenght/10*3), pressureSamples);
		float loudestPressure4 = findLoudestIntervalPressure((pressureSamplesLenght/10*3), (pressureSamplesLenght/10*6), pressureSamples);
		float loudestPressure5 = findLoudestIntervalPressure((pressureSamplesLenght/10*6), pressureSamplesLenght, pressureSamples);

		/*
		System.out.println("Result =\n" +
				loudestPressure1 + "\n" +
				loudestPressure2 + "\n" +
				loudestPressure3 + "\n" +
				loudestPressure4 + "\n" +
				loudestPressure5);
		*/
		
		//Multiply to make more useful
		loudestPressure1 *= 10000;
		loudestPressure2 *= 10000;
		loudestPressure3 *= 10000;
		loudestPressure4 *= 10000;
		loudestPressure5 *= 10000;
		
		//Create percentages-object
		spectrumView.setPercentages(
				((loudestPressure1>100)?100:loudestPressure1),
				((loudestPressure2>100)?100:loudestPressure2),
				((loudestPressure3>100)?100:loudestPressure3),
				((loudestPressure4>100)?100:loudestPressure4),
				((loudestPressure5>100)?100:loudestPressure5));
		spectrumView.postInvalidate();
						
	}

	/**
	 * Used to find the highest element from the pressure-samples array,
	 * limited by a certain search-interval.
	 * @param intervalLowerBound
	 * @param intervalUpperBound
	 * @param pressureSamples
	 * @return
	 */
	private float findLoudestIntervalPressure(int intervalLowerBound, int intervalUpperBound, float[] pressureSamples)
	{
		float loudestIntervalPressure = 0;
		int loudestLocation = 0;
		//start by finding loudest band under 4
		for ( int currentLocation = intervalLowerBound; currentLocation < (intervalUpperBound > pressureSamples.length ? pressureSamples.length : intervalUpperBound); currentLocation++ ) {
			float currentIntervalPressure = pressureSamples[currentLocation];
			if(currentIntervalPressure > loudestIntervalPressure)
			{
				loudestIntervalPressure = currentIntervalPressure;
				loudestLocation = currentLocation;
			}
		}
		return loudestIntervalPressure;
	}

	public boolean mustRemainInformed() {
		return false;
	}

	public void start() { }

	public void stop() { }

	public void refresh() {
		// Do nothing
	}
}
