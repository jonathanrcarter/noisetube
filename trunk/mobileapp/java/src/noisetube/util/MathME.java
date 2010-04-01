package noisetube.util;

public class MathME
{

	public static long round(double a)
	{
		return (long) Math.floor(a + 0.5d);
	}

	public static double powerOfTen(int power)
	{
		double r = 1;
		if(power != 0)
			for(int p = 0; p < Math.abs(power); p++)
				r *= 10;
		if(power < 0)
			r = 1 / r;
		return r;
	}

	public static double atan(double a)
	{
		return 1.0d / Math.tan(a);
	}

	// //code taken from:
	// http://today.java.net/pub/a/today/2007/11/06/creating-java-me-math-pow-method.html
	// public static double pow(double x, double y)
	// {
	// //Convert the real power to a fractional form
	// int den = 1024; //declare the denominator to be 1024
	//
	// /*Conveniently 2^10=1024, so taking the square root 10
	// times will yield our estimate for n. In our example
	// n^3=8^2 n^1024 = 8^683.*/
	//
	// int num = (int)(y*den); // declare numerator
	//
	// int iterations = 10; /*declare the number of square root
	// iterations associated with our denominator, 1024.*/
	//
	// double n = Double.MAX_VALUE; /* we initialize our
	// estimate, setting it to max*/
	//
	// while( n >= Double.MAX_VALUE && iterations > 1)
	// {
	// /* We try to set our estimate equal to the right
	// hand side of the equation (e.g., 8^2048). If this
	// number is too large, we will have to rescale. */
	//
	// n = x;
	//	                
	// for( int i=1; i < num; i++ )n*=x;
	//
	// /*here, we handle the condition where our starting
	// point is too large*/
	// if( n >= Double.MAX_VALUE )
	// {
	// iterations--; /*reduce the iterations by one*/
	//	                        
	// den = (int)(den / 2); /*redefine the denominator*/
	//	                        
	// num = (int)(y*den); //redefine the numerator
	// }
	// }
	//	        
	// /*************************************************
	// ** We now have an appropriately sized right-hand-side.
	// ** Starting with this estimate for n, we proceed.
	// **************************************************/
	//	        
	// for( int i = 0; i < iterations; i++ )
	// {
	// n = Math.sqrt(n);
	// }
	//	        
	// // Return our estimate
	// return n;
	// }

}
