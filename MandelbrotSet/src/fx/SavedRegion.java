package fx;

import java.io.*;
import java.math.*;

import colorFunction.*;

public class SavedRegion implements Serializable
{
	public String name;
	public int iterations, precision, threadCount;
	public String colorFunction;
	public Region<BigDecimal> region;
	public boolean julia, arbitraryPrecision;
	public ComplexBigDecimal seed;
	
	/**
	 * @param name
	 * @param iterations
	 * @param precision
	 * @param threadCount
	 * @param region
	 * @param arbitraryPrecision
	 * @param julia
	 * @param seed
	 * @param colorFunction
	 */
	public SavedRegion(String name,
			int iterations, int precision, int threadCount,
			Region<BigDecimal> region,
			boolean arbitraryPrecision,  boolean julia,
			ComplexBigDecimal seed, String colorFunction)
	{
		this.name = name;
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
	public String toString()
	{
		return name;
	}
}
