package mandelbrotSet;
/**
 * 
 * This class represents an immutable complex number with real and imaginary components.
 * It also defines some basic functions.
 *
 * @author Ezra Stein
 * @version 1.0
 * @since 2015
 */
public class Complex 
{
	/**
	 * Holds the real part for this complex number.
	 */
	double real;
	/**
	 * Holds the imaginary part for this complex number.
	 */
	double imaginary;
	
	/**
	 * Constructs a complex number with real and imaginary components.
	 * 
	 * @param realPart - The real component
	 * @param imaginaryPart - The imaginary component
	 */
	public Complex(double realPart,double imaginaryPart)
	{
		real = realPart;
		imaginary = imaginaryPart;
	}
	
	/**
	 * Returns the sum of this complex number and another.
	 * 
	 * @param addend - The second complex number to be added.
	 * @return The sum.
	 */
	public Complex add(Complex addend)
	{
		return new Complex(addend.getRealPart() + real, addend.getImaginaryPart() + imaginary);
	}
	
	/**
	 * Returns the difference of this complex number minus the subtrahend.
	 * 
	 * @param subtrahend - The complex number which is subtracted from this
	 * @return The difference.
	 */
	public Complex subtract(Complex subtrahend)
	{
		return new Complex(real - subtrahend.getRealPart(), imaginary - subtrahend.getImaginaryPart());
	}
	
	/**
	 * Returns the product of this complex number and the multiplicand.
	 * 
	 * @param multiplicand - A complex number which multiplies this.
	 * @return The product.
	 */
	public Complex multiply(Complex multiplicand)
	{
		return new Complex(real*multiplicand.getRealPart()-imaginary*multiplicand.getImaginaryPart(), real*multiplicand.getImaginaryPart()+imaginary*multiplicand.getRealPart());
	}
	
	/**
	 * Finds the square of this complex number (z^2).
	 * @return The square.
	 */
	public Complex square()
	{
		return new Complex(real*real - imaginary*imaginary, 2*real*imaginary);
	}
	
	/**
	 * Finds the absolute value (distance from zero) of this complex number.
	 * @return The absolute value.
	 */
	public double ABS()
	{
		return Math.sqrt(Math.pow(real, 2)+Math.pow(imaginary, 2));
	}
	
	/**
	 * Returns the real part.
	 * @return The real part.
	 */
	public double getRealPart()
	{
		return real;
	}
	
	/**
	 * Returns the imaginary part.
	 * @return the imaginary part.
	 */
	public double getImaginaryPart()
	{
		return imaginary;
	}
	
	
	public String toString()
	{
		if(imaginary > 0)
		{
			return real + " + " + imaginary + "i";
		}
		else if(imaginary == 0)
		{
			return real + "";
		}
		else
		{
			return real + " - " + -imaginary + "i";
		}
		
	}
	
}
