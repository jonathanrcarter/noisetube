package noisetube.audio.java;

import noisetube.util.Comparable;

public class CalibValue implements Comparable
{
	public double phone;
	public double ref;

	public CalibValue(double ref, double phone)
	{
		this.phone = phone;
		this.ref = ref;
	}

	public int compareTo(Object a)
	{
		CalibValue o = (CalibValue) a;
		if(this.ref == o.ref)
			return 0;
		if(this.ref > o.ref)
			return 1;
		else
			return -1;

	}

}
