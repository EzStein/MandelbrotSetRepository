package fx;

import java.io.Serializable;
import java.math.*;

/**
 * This class represents a region of numbers. It contains for numbers.
 * The x and y coordinates of the top left corner and the x and y coordinates of the bottom right corner.
 * This class is immutable.
 * @author Ezra
 * @version 1.0
 * @since 2015
 * @param <T> The parameter may be any class that extends Number
 */
public class Region<T extends Number> implements Serializable
{
	/**
	 * The serial version
	 */
	private static final long serialVersionUID = 6631467022497785339L;
	
	/**
	 * The four numbers of this region.
	 * x1, y1, are the top left corner.
	 * x2, y2 are the bottom right corner.
	 */
	public final T x1;
	/**
	 * The four numbers of this region.
	 * x1, y1, are the top left corner.
	 * x2, y2 are the bottom right corner.
	 */
	public final T x2;
	/**
	 * The four numbers of this region.
	 * x1, y1, are the top left corner.
	 * x2, y2 are the bottom right corner.
	 */
	public final T y1;
	/**
	 * The four numbers of this region.
	 * x1, y1, are the top left corner.
	 * x2, y2 are the bottom right corner.
	 */
	public final T y2;
	
	
	/**
	 * Constructs a region object.
	 * @param x1	x coordinate of top left corner.
	 * @param y1	y coordinate of top left corner.
	 * @param x2	x coordinate of bottom right corner.
	 * @param y2	y coordinate of bottom right corner.
	 */
	public Region(T x1, T y1, T x2, T y2)
	{
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	
	/**
	 * Returns the width of this region in the most accurate number, BigDecimal.
	 * @return the width of this region in the most accurate number, BigDecimal.
	 */
	public BigDecimal getWidth()
	{
		if(x1 instanceof BigDecimal)
		{
			return ((BigDecimal) x2).subtract((BigDecimal)x1).abs();
		}
		else
		{
			return new BigDecimal("" + Math.abs(x2.doubleValue()-x1.doubleValue()));
		}
	}
	
	/**
	 * Returns the height of this region in the most accurate number, BigDecimal.
	 * @return the height of this region in the most accurate number, BigDecimal.
	 */
	public BigDecimal getHeight()
	{
		if(x1 instanceof BigDecimal)
		{
			return ((BigDecimal) y2).subtract((BigDecimal)y1).abs();
		}
		else
		{
			return new BigDecimal("" + Math.abs(y2.doubleValue()-y1.doubleValue()));
		}
	}
	
	/**
	 * Returns the center x coordinate of this region in the most accurate number, BigDecimal.
	 * @return the center x coordinate of this region in the most accurate number, BigDecimal.
	 */
	public BigDecimal getCenterX()
	{
		if(x1 instanceof BigDecimal)
		{
			return ((BigDecimal) x2).add((BigDecimal)x1).divide(new BigDecimal("2"), ((BigDecimal) x1).scale(), RoundingMode.HALF_UP);
		}
		else
		{
			return new BigDecimal("" + (x2.doubleValue()+x1.doubleValue())/2);
		}
	}
	
	/**
	 * Returns the center y coordinate of this region in the most accurate number, BigDecimal.
	 * @return the center y coordinate of this region in the most accurate number, BigDecimal.
	 */
	public BigDecimal getCenterY()
	{
		if(x1 instanceof BigDecimal)
		{
			return ((BigDecimal) y2).add((BigDecimal)y1).divide(new BigDecimal("2"), ((BigDecimal) y1).scale(), RoundingMode.HALF_UP);
		}
		else
		{
			return new BigDecimal("" + (y2.doubleValue()+y1.doubleValue())/2);
		}
	}
	
	/**
	 * Returns a region of BigDecimal that is a scaled version of this region.
	 * @param sx	The x scaling factor.
	 * @param sy	The y scaling factor.
	 * @param px	The x point of origin to scale from.
	 * @param py	The y point of origin to scale from.
	 * @return		A Region BigDecimal that represents the scaled version of this region.
	 */
	public Region<BigDecimal> scale(double sx, double sy, BigDecimal px, BigDecimal py)
	{
		BigDecimal bigX1 = (BigDecimal) x1;
		BigDecimal bigX2 = (BigDecimal) x2;
		BigDecimal bigY1 = (BigDecimal) y1;
		BigDecimal bigY2 = (BigDecimal) y2;
		
		Region<BigDecimal> r = new Region<>(
		px.subtract(bigX1.subtract(px).abs().multiply(new BigDecimal(sx))),
		bigY1.subtract(py).abs().multiply(new BigDecimal(sy)).add(py),
		bigX2.subtract(px).abs().multiply(new BigDecimal(sx)).add(px),
		py.subtract(bigY2.subtract(py).abs().multiply(new BigDecimal(sy)))
		);
		return r;
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
		if(!(object instanceof Region<?>))
		{
			return false;
		}
		
		Region<?> r = (Region<?>) object;
		if(r.x1.equals(x1) && r.x2.equals(x2) && r.y1.equals(y1) && r.y2.equals(y2))
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
		return x1.hashCode() + x2.hashCode() + y1.hashCode() + y2.hashCode();
	}
}
