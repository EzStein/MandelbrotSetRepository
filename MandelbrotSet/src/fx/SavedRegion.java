package fx;

import java.io.*;
import java.math.*;
import colorFunction.*;

/**
 * A data structure container that contains all the information necessary to reconstruct the state of the program.
 * @author Ezra Stein
 * @version 1.0
 * @since 2015
 *
 */
public class SavedRegion implements Serializable
{
	/**
	 * Version ID.
	 */
	private static final long serialVersionUID = 1L;
	final String name;
	final int iterations, precision, threadCount;
	final Region<BigDecimal> region;
	final boolean julia, arbitraryPrecision, autoIterations;
	final ComplexBigDecimal seed;
	final CustomColorFunction colorFunction;
	
	/**
	 * @param name
	 * @param autoIterations
	 * @param iterations
	 * @param precision
	 * @param threadCount
	 * @param region
	 * @param arbitraryPrecision
	 * @param julia
	 * @param seed
	 * @param colorFunction
	 */
	public SavedRegion(String name, boolean autoIterations,
			int iterations, int precision, int threadCount,
			Region<BigDecimal> region,
			boolean arbitraryPrecision,  boolean julia,
			ComplexBigDecimal seed, CustomColorFunction colorFunction)
	{
		this.name = name;
		this.autoIterations = autoIterations;
		this.iterations = iterations;
		this.precision = precision;
		this.threadCount = threadCount;
		this.colorFunction = colorFunction;
		this.region = region;
		this.julia = julia;
		this.arbitraryPrecision = arbitraryPrecision;
		this.seed = seed;
	}
	
	
	@Override
	public boolean equals(Object object)
	{
		if(object == null)
		{
			return false;
		}
		if(object == this)
		{
			return true;
		}
		if(!(object instanceof SavedRegion))
		{
			return false;
		}
		
		SavedRegion sr = (SavedRegion) object;
		if(sr.autoIterations == autoIterations
				&& sr.iterations == iterations
				&& sr.precision == precision
				&& sr.threadCount == threadCount
				&& sr.colorFunction.equals(colorFunction)
				&& sr.region.equals(region)
				&& sr.julia == julia
				&& sr.arbitraryPrecision == arbitraryPrecision
				&& sr.seed.equals(seed))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public int hashCode()
	{
		return (autoIterations?1:2) + 11*iterations
				+ 100*precision + 55*threadCount + colorFunction.hashCode()
				+ region.hashCode() + (julia?100:200) + (arbitraryPrecision?1000:2000) + seed.hashCode();
	}
}
