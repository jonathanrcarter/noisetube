package noisetube.util;

public class Float11
{

	public Float11()
	{
	}

	public static double acos(double d)
	{
		double d1 = asin(d);
		if(d1 == (0.0D / 0.0D))
			return d1;
		else
			return 1.5707963267948966D - d1;
	}

	public static double asin(double d)
	{
		if(d < -1D || d > 1.0D)
			return(0.0D / 0.0D);
		if(d == -1D)
			return -1.5707963267948966D;
		if(d == 1.0D)
			return 1.5707963267948966D;
		else
			return atan(d / Math.sqrt(1.0D - d * d));
	}

	public static double atan(double d)
	{
		boolean flag = false;
		boolean flag1 = false;
		int i = 0;
		if(d < 0.0D)
		{
			d = -d;
			flag = true;
		}
		if(d > 1.0D)
		{
			d = 1.0D / d;
			flag1 = true;
		}
		double d2;
		for(; d > 0.26179938779914941D; d *= d2)
		{
			i++;
			d2 = d + 1.7320508075688772D;
			d2 = 1.0D / d2;
			d *= 1.7320508075688772D;
			d--;
		}

		double d1 = d * d;
		double d3 = d1 + 1.4087812D;
		d3 = 0.55913709D / d3;
		d3 += 0.60310578999999997D;
		d3 -= d1 * 0.051604539999999997D;
		d3 *= d;
		for(; i > 0; i--)
			d3 += 0.52359877559829882D;

		if(flag1)
			d3 = 1.5707963267948966D - d3;
		if(flag)
			d3 = -d3;
		return d3;
	}

	public static double atan2(double d, double d1)
	{
		if(d == 0.0D && d1 == 0.0D)
			return 0.0D;
		if(d1 > 0.0D)
			return atan(d / d1);
		if(d1 < 0.0D)
			if(d < 0.0D)
				return -(3.1415926535897931D - atan(d / d1));
			else
				return 3.1415926535897931D - atan(-d / d1);
		return d >= 0.0D ? 1.5707963267948966D : -1.5707963267948966D;
	}

	public static double exp(double d)
	{
		if(d == 0.0D)
			return 1.0D;
		double d1 = 1.0D;
		long l = 1L;
		boolean flag = d < 0.0D;
		if(flag)
			d = -d;
		double d2 = d / l;
		for(long l1 = 2L; l1 < 50L; l1++)
		{
			d1 += d2;
			d2 = (d2 * d) / l1;
		}

		if(flag)
			return 1.0D / d1;
		else
			return d1;
	}

	private static double _log(double d)
	{
		if(d <= 0.0D)
			return(0.0D / 0.0D);
		double d1 = 0.0D;
		int i;
		for(i = 0; d > 0.0D && d <= 1.0D; i++)
			d *= 2D;

		d /= 2D;
		i--;
		double d2 = d - 1.0D;
		double d3 = d + 1.0D;
		double d4 = d2 / d3;
		double d5 = d4;
		d3 = d5 * d4;
		for(long l = 1L; l < 50L; l += 2L)
		{
			d1 += d5 / l;
			d5 *= d3;
		}

		d1 *= 2D;
		for(int j = 0; j < i; j++)
			d1 += -0.69314718055994529D;

		return d1;
	}

	public static double log(double d)
	{
		if(d <= 0.0D)
			return(0.0D / 0.0D);
		if(d == 1.0D)
			return 0.0D;
		if(d > 1.0D)
		{
			d = 1.0D / d;
			return -_log(d);
		}
		else
		{
			return _log(d);
		}
	}

	public static double log10(double d)
	{
		return log(d) / 2.3025850929940459D;
	}

	public static double pow(double d, double d1)
	{
		if(d1 == 0.0D)
			return 1.0D;
		if(d1 == 1.0D)
			return d;
		if(d == 0.0D)
			return 0.0D;
		if(d == 1.0D)
			return 1.0D;
		long l = (long) Math.floor(d1);
		boolean flag = d1 == l;
		if(flag)
		{
			boolean flag1 = false;
			if(d1 < 0.0D)
				flag1 = true;
			double d2 = d;
			for(long l1 = 1L; l1 < (flag1 ? -l : l); l1++)
				d2 *= d;

			if(flag1)
				return 1.0D / d2;
			else
				return d2;
		}
		if(d > 0.0D)
			return exp(d1 * log(d));
		else
			return(0.0D / 0.0D);
	}

	public static final double SQRT3 = 1.7320508075688772D;
	public static final double LOG10 = 2.3025850929940459D;
	public static final double LOGdiv2 = -0.69314718055994529D;
}