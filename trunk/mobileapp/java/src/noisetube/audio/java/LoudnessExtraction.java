package noisetube.audio.java;

import noisetube.audio.LoudnessException;
import noisetube.util.Float11;
import noisetube.util.Logger;

/**
 * Loudness measure in db(a)
 * 
 * @author maisonneuve
 * 
 */
public class LoudnessExtraction
{

	public static double LOW_RESPONSE = 1.0D;
	public static double FAST_RESPONSE = 0.125D;
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
		LeqInterval = (LeqInterval == LOW_RESPONSE) ? FAST_RESPONSE
				: LOW_RESPONSE;
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
		// no delimited interval , compute all the sample
		if(LeqInterval == -1)
		{
			samples_per_leq = nb_samples;
			log.debug("All the samples (" + samples_per_leq + ")");
			nb_leq = 1;
			// else compute interval
		}
		else
		{
			samples_per_leq = (int) (s.getSampleRate() * LeqInterval);
			if(log.isDebug())
				log.debug("limited to " + samples_per_leq + " samples");
			nb_leq = (int) ((double) nb_samples / samples_per_leq);
		}

		if(nb_leq == 0)
		{
			throw new LoudnessException("no enough sample (" + s.getDataSize()
					+ ", " + samples_per_leq * byte_per_sample + ")");
		}

		leqs = new double[nb_leq];
		AFilter filter = AFilter.getAFilter(s.getSampleRate());

		// System.out.println("nbleq: "+nb_leq);
		// for each set of samples
		for(int k = 0; k < nb_leq; k++)
		{

			// read samples
			int start = k * (int) samples_per_leq;
			int end = start + (int) (samples_per_leq - 1);
			double ad[];

			// try {
			ad = s.readSamples2(start, end);
			// } catch (Exception e) {
			// throw new Exception("read samples: " + e.getMessage());
			// }

			// try {
			// //compute A weighting filtering
			if(AfilterActive)
			{
				ad = filter(filter.Acoef, filter.Bcoef, ad);
			}

			// } catch (Exception e) {
			// throw new Exception("filter: " + e.getMessage());
			// }

			// compute leq
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
	 * @param samples
	 *            : array of sound samples
	 * @return the leq
	 */
	private double leq(double samples[])
	{

		double d = 0.0D;
		for(int i = 0; i < samples.length; i++)
		{
			double d1 = samples[i]; // p0 = /0.0001d
			d += d1 * d1;
		}
		d /= samples.length;
		return 10D * Float11.log10(d);
	}

	private static double[] filter(double ACoef[], double BCoef[], double ad2[])
	{
		double ad3[] = new double[ad2.length];
		int k = ACoef.length - 1;
		double ad4[] = new double[k];
		for(int i = 0; i < ad2.length; i++)
		{
			double d = ad2[i];
			double d1 = d * BCoef[0] + ad4[0];
			ad3[i] = d1;
			int j;
			for(j = 0; j < k - 1; j++)
			{
				ad4[j] = (d * BCoef[j + 1] - d1 * ACoef[j + 1]) + ad4[j + 1];
			}
			ad4[k - 1] = d * BCoef[j + 1] - d1 * ACoef[j + 1];
		}

		return ad3;
	}

	public double getLeqInterval()
	{
		return LeqInterval;
	}
}
