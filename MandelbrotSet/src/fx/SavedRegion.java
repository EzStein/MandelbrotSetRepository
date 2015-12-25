package fx;

import java.io.*;
import java.math.*;
import colorFunction.*;

public class SavedRegion implements Serializable
{
	public final String name;
	public final int iterations, precision, threadCount;
	public final Region<BigDecimal> region;
	public final boolean julia, arbitraryPrecision, autoIterations;
	public final ComplexBigDecimal seed;
	public final CustomColor colorFunction;
	
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
			ComplexBigDecimal seed, CustomColor colorFunction)
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
	
	public String getName()
	{
		return name;
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
		if(sr.getName().equals(name))
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
		return name.hashCode();
	}
}
