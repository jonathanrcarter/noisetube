package noisetube.audio.java;

import java.util.Hashtable;

import noisetube.audio.LoudnessException;

/**
 * A Weighting Filter
 * 
 * @author maisonneuve
 * 
 */
public class AFilter
{

	public int freq;

	public double[] Acoef;

	public double[] Bcoef;
	static final Hashtable filters = new Hashtable();

	static
	{

		// 8000
		final AFilter filter_8000 = new AFilter(8000);
		filter_8000.Acoef = new double[] { 1.0D, -2.1284671930091217D,
				0.29486689801012067D, 1.8241838307350515D,
				-0.80566289431197835D, -0.39474979828429368D,
				0.20985485460803321D };
		filter_8000.Bcoef = new double[] { 0.63062094682387282D,
				-1.2612418936477434D, -0.63062094682387637D,
				2.5224837872954899D, -0.6306209468238686D,
				-1.2612418936477467D, 0.63062094682387237D };
		// 16000
		final AFilter filter_16000 = new AFilter(16000);
		filter_16000.Acoef = new double[] { 1.0D, -2.867832572992166100D,
				2.221144410202319500D, 0.455268334788656860D,
				-0.983386863616282910D, 0.055929941424134225D,
				0.118878103828561270D };
		filter_16000.Bcoef = new double[] { 0.53148982982355708D,
				-1.0629796596471122D, -0.53148982982356319D,
				2.1259593192942332D, -0.53148982982355686D,
				-1.0629796596471166D, 0.53148982982355797D };

		// 22050
		final AFilter filter_22050 = new AFilter(22050);
		filter_22050.Acoef = new double[] { 1.0D, -3.2290788052250736D,
				3.3544948812360302D, -0.73178436806573255D,
				-0.6271627581807262D, 0.17721420050208803D,
				0.056317166973834924D };
		filter_22050.Bcoef = new double[] { 0.44929985042991927D,
				-0.89859970085984164D, -0.4492998504299115D,
				1.7971994017196726D, -0.44929985042992043D,
				-0.89859970085983754D, 0.44929985042991943D };

		// 44100
		final AFilter filter_44100 = new AFilter(44100);
		filter_44100.Acoef = new double[] { 1.0D, -4.0195761811158315D,
				6.1894064429206921D, -4.4531989035441155D, 1.4208429496218764D,
				-0.14182547383030436D, 0.0043511772334950787D };
		filter_44100.Bcoef = new double[] { 0.2557411252042574D,
				-0.51148225040851436D, -0.25574112520425807D,
				1.0229645008170318D, -0.25574112520425918D,
				-0.51148225040851414D, 0.25574112520425729D };

		// 48000
		final AFilter filter_48000 = new AFilter(48000);
		filter_48000.Acoef = new double[] { 1.0000000000000000D,
				-4.113043408775872D, 6.5531217526550503D, -4.9908492941633842D,
				1.7857373029375754D, -0.24619059531948789D,
				0.011224250033231334D };
		filter_48000.Bcoef = new double[] { 0.2343017922995132D,
				-0.4686035845990264D, -0.23430179229951431D,
				0.9372071691980528D, -0.23430179229951364D,
				-0.46860358459902524D, 0.23430179229951273D };

		final AFilter filter_24000 = new AFilter(24000);
		filter_24000.Acoef = new double[] { 1.0000000000, -3.3259960042,
				3.6771610793, -1.1064760768, -0.4726706735, 0.1861941760,
				0.0417877134 };
		filter_24000.Bcoef = new double[] { 0.4256263893, -0.8512527786,
				-0.4256263893, 1.7025055572, -0.4256263893, -0.8512527786,
				0.4256263893

		};

		final AFilter filter_32000 = new AFilter(32000);
		filter_32000.Acoef = new double[] { 1.0000000000, -3.6564460432,
				4.8314684507, -2.5575974966, 0.2533680394, 0.1224430322,
				0.0067640722 };
		filter_32000.Bcoef = new double[] { 0.3434583387, -0.6869166774,
				-0.3434583387, 1.3738333547, -0.3434583387, -0.6869166774,
				0.3434583387 };

		filters.put(new Integer(8000), filter_8000);
		filters.put(new Integer(16000), filter_16000);
		filters.put(new Integer(22050), filter_22050);
		filters.put(new Integer(24000), filter_24000);
		filters.put(new Integer(44100), filter_44100);
		filters.put(new Integer(32000), filter_32000);
		filters.put(new Integer(48000), filter_48000);
	}

	static public AFilter getAFilter(long freq)
	{
		AFilter f = (AFilter) filters.get(new Integer((int) freq));
		if(f == null)
		{
			new LoudnessException("no A weighting filter for the frequence "
					+ freq);
		}
		return f;
	}

	private double[] filter(double samples[])
	{
		double results[] = new double[samples.length];
		int k = Acoef.length - 1;
		double ad4[] = new double[k];

		for(int i = 0; i < samples.length; i++)
		{
			double d = samples[i];
			double d1 = d * Bcoef[0] + ad4[0];
			results[i] = d1;
			int j;
			for(j = 0; j < k - 1; j++)
			{
				ad4[j] = (d * Bcoef[j + 1] - d1 * Acoef[j + 1]) + ad4[j + 1];
			}
			ad4[k - 1] = d * Bcoef[j + 1] - d1 * Acoef[j + 1];
		}
		return results;
	}

	private AFilter(int freq)
	{
		this.freq = freq;
	}

}
