package fx;

import java.io.Serializable;
import java.math.*;

public class Region<T extends Number> implements Serializable
{
	public final T x1,x2,y1,y2;
	public Region(T x1, T y1, T x2, T y2)
	{
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	
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
}
