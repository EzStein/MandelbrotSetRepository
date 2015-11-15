package mandelbrotSet;
import java.awt.Rectangle;
import java.math.*;
/**
 * Defines a rectangle whose top left corner is (x1,y1) and whose bottom right corner is (x2,y2).
 * Used for convenience and to reduce the length of constructors.
 * BigDecimal is used to get readings up to any given precision.
 * All BigDecimal objects in this program represent absolute coordinates with origin at the center.
 * They do not represent pixels. Rather they are approximation of a pixel (up to precision)
 * in terms of absolute coordinates.
 * To represent pixels in a panel, use the Rectangle class.
 * 
 * @author Ezra Stein
 * @version 1.0
 * @since 2015
 * @see Rectangle
 */	
public class Region
{
	/**
	 * Holds the desired precision of each BigDecimal value.
	 */
	int precision;
	
	/**
	 * X coordinate of top left corner.
	 */
	BigDecimal regionX1;
	
	/**
	 * Y coordinate of top left corner.
	 */
	BigDecimal regionY1;
	
	/**
	 * X coordinate of bottom right corner.
	 */
	BigDecimal regionX2;
	
	/**
	 * Y coordinate of bottom right corner.
	 */
	BigDecimal regionY2;
	
	/**
	 * Takes in four BigDecimal values representing the coordinates of two corners and their precision.
	 * 
	 * @param regionX1 - X coordinate of top left corner.
	 * @param regionY1 - Y coordinate of top left corner.
	 * @param regionX2 - X coordinate of bottom right corner.
	 * @param regionY2 - Y coordinate of bottom right corner.
	 * @param precision
	 */
	public Region(BigDecimal regionX1, BigDecimal regionY1, BigDecimal regionX2, BigDecimal regionY2, int precision)
	{
		this.precision = precision;
		this.regionX1 = regionX1.setScale(precision);
		this.regionY1 = regionY1.setScale(precision);
		this.regionX2 = regionX2.setScale(precision);
		this.regionY2 = regionY2.setScale(precision);
	}
	
	/**
	 * Returns the value of regionX1
	 * @return regionX1 (x coordinate of top left corner).
	 */
	public BigDecimal getX1()
	{
		return regionX1;
	}
	
	/**
	 * Returns the value of regionY1
	 * @return regionY1 (y coordinate of top left corner).
	 */
	public BigDecimal getY1()
	{
		return regionY1;
	}
	
	/**
	 * Returns the value of regionX2
	 * @return regionX2 (x coordinate of bottom right corner).
	 */
	public BigDecimal getX2()
	{
		return regionX2;
	}
	
	/**
	 * Returns the value of regionY2
	 * @return regionY2 (y coordinate of bottom right corner).
	 */
	public BigDecimal getY2()
	{
		return regionY2;
	}
	
	/**
	 * Returns the region's width.
	 * @return the region's width.
	 */
	public BigDecimal getWidth()
	{
		return regionX1.subtract(regionX2).abs().setScale(precision,BigDecimal.ROUND_HALF_UP);
	}
	
	/**
	 * Returns the region's height.
	 * @return the region's height.
	 */
	public BigDecimal getHeight()
	{
		return regionY1.subtract(regionY2).abs().setScale(precision,BigDecimal.ROUND_HALF_UP);
	}
}
