package fx;

import java.math.*;
import colorFunction.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;

/**
 * Used to calculate images of the Mandelbrot set
 * Provides functions from converting pixels to actual BigDecimal points and back.
 * @author Ezra
 *
 */
public class Calculator
{
	private boolean interrupted;
	private int pixelsCalculated;
	private CustomColorFunction colorFunction;
	
	/**
	 * Constructs a Calculator whose color function will be the first color function in COLOR_FUNTIONS
	 */
	public Calculator()
	{
		colorFunction = CustomColorFunction.COLOR_FUNCTIONS.get(0);
		pixelsCalculated = 0;
		interrupted = false;
	}
	
	/**
	 * Constructs a Calculator whose color function is provided.
	 * @param cf
	 */
	public Calculator(CustomColorFunction cf)
	{
		this();
		colorFunction = cf;
	}
	
	/**
	 * Returns the color function of this calculator.
	 * @return the color function of this calculator
	 */
	public CustomColorFunction getColorFunction()
	{
		return colorFunction;
	}
	
	/**
	 * Sets the color function of this calculator.
	 * @param cf	The new color function.
	 */
	public void setColorFunction(CustomColorFunction cf)
	{
		colorFunction = cf;
	}
	
	/**
	 * Converts this bigDecimal point in the complex plane to a pixel on the screen in the x direction.
	 * @param x				The point to be converted given relative to the origin of the complex plane
	 * @param region		The region of points that represents the currently viewed area of the screen
	 * @param pixelRegion	The region of pixels that corresponds to the region of points given above.
	 * 						This will typically be the square that represents the width and height of the canvas.
	 * @param precision		The precision to be used in BigDecimal calculations.
	 * @return				An integer representing the pixel corresponding to the point given.
	 * 						This integer is given relative to the top left corner of the pixelRegion
	 */
	public static int pointToPixelX(BigDecimal x, Region<BigDecimal> region, Region<Integer> pixelRegion, int precision)
	{
		BigDecimal scale = pixelRegion.getWidth().divide(region.getWidth(),precision,BigDecimal.ROUND_HALF_UP);
		return x.subtract(region.x1).multiply(scale).add(new BigDecimal(pixelRegion.x1)).intValue();
	}
	
	/**
	 * Converts this bigDecimal point in the complex plane to a pixel on the screen in the y direction.
	 * @param y				The point to be converted given relative to the origin of the complex plane
	 * @param region		The region of points that represents the currently viewed area of the screen
	 * @param pixelRegion	The region of pixels that corresponds to the region of points given above.
	 * 						This will typically be the square that represents the width and height of the canvas.
	 * @param precision		The precision to be used in BigDecimal calculations.
	 * @return				An integer representing the pixel corresponding to the point given.
	 * 						This integer is given relative to the top left corner of the viewer
	 */
	public static int pointToPixelY(BigDecimal y, Region<BigDecimal> region, Region<Integer> pixelRegion, int precision)
	{
		BigDecimal scale = pixelRegion.getHeight().divide(region.getHeight(),precision,BigDecimal.ROUND_HALF_UP);
		return y.subtract(region.y1).multiply(scale).add(new BigDecimal(pixelRegion.y1)).intValue();
	}
	
	/**
	 * Converts this pixel to the corresponding point in the complex plane.
	 * @param x				The pixel to be converted given relative to the top left corner of the viewer.
	 * @param region		The region of points that represents the currently viewed area of the screen
	 * @param pixelRegion	The region of pixels that corresponds to the region of points given above.
	 * 						This will typically be the square that represents the width and height of the canvas.
	 * @param precision		The precision to be used in BigDecimal calculations.
	 * @return				A BigDecimal representing the point relative to the origin of the complex Plane
	 * 						that corresponds with the given pixel.
	 */
	public static BigDecimal pixelToPointX(int x, Region<BigDecimal> region, Region<Integer> pixelRegion, int precision)
	{
		BigDecimal scale = pixelRegion.getWidth().divide(region.getWidth(),precision,BigDecimal.ROUND_HALF_UP);
		return new BigDecimal(x-pixelRegion.x1).divide(scale,precision,BigDecimal.ROUND_HALF_UP).add(region.x1);
	}
	
	/**
	 * Converts this pixel to the corresponding point in the complex plane.
	 * @param y				The pixel to be converted given relative to the top left corner of the viewer.
	 * @param region		The region of points that represents the currently viewed area of the screen
	 * @param pixelRegion	The region of pixels that corresponds to the region of points given above.
	 * 						This will typically be the square that represents the width and height of the canvas.
	 * @param precision		The precision to be used in BigDecimal calculations.
	 * @return				A BigDecimal representing the point relative to the origin of the complex Plane
	 * 						that corresponds with the given pixel.
	 */
	public static BigDecimal pixelToPointY(int y, Region<BigDecimal> region, Region<Integer> pixelRegion, int precision)
	{
		BigDecimal scale = pixelRegion.getHeight().divide(region.getHeight(),precision,BigDecimal.ROUND_HALF_UP);
		return region.y1.subtract((new BigDecimal(y-pixelRegion.y1).divide(scale,precision,BigDecimal.ROUND_HALF_UP)));
	}
	
	/**
	 * Converts this pixel point relative to the top left of the viewer screen to the corresponding point in the complex plane.
	 * @param x 			The pixel X value to be converted given relative to the top left corner of the viewer.
	 * @param y				The pixel Y value to be converted given relative to the top left corner of the viewer.
	 * @param region		The region of points that represents the currently viewed area of the screen
	 * @param pixelRegion	The region of pixels that corresponds to the region of points given above.
	 * 						This will typically be the square that represents the width and height of the canvas.
	 * @param precision		The precision to be used in BigDecimal calculations.
	 * @return				A ComplexBigDecimal representing the point relative to the origin of the complex Plane
	 * 						that corresponds with the given pixel.
	 */
	public static ComplexBigDecimal toComplexBigDecimal(int x, int y, Region<BigDecimal> region, Region<Integer> pixelRegion, int precision)
	{
		return new ComplexBigDecimal(pixelToPointX(x, region, pixelRegion, precision), pixelToPointY(y, region, pixelRegion, precision),precision);
	}
	
	/**
	 * A convenience method that converts this region of BigDecimal points relative to the origin of the complex plane to a region of pixels.
	 * @param r 			The BigDecimal Region that is to be converted.
	 * @param region		The region of points that represents the currently viewed area of the screen
	 * @param pixelRegion	The region of pixels that corresponds to the region of points given above.
	 * 						This will typically be the square that represents the width and height of the canvas.
	 * @param precision		The precision to be used in BigDecimal calculations.
	 * @return				A Region of integers representing the region of pixels relative to the top left corner of the viewer.
	 */
	public static Region<Integer> toPixelRegion(Region<BigDecimal> r, Region<BigDecimal> region, Region<Integer> pixelRegion, int precision)
	{
		int x1 = pointToPixelX(r.x1, region, pixelRegion, precision);
		int y1 = pointToPixelY(r.y1, region, pixelRegion, precision);
		int x2 = pointToPixelX(r.x2, region, pixelRegion, precision);
		int y2 = pointToPixelY(r.y2, region, pixelRegion, precision);
		return new Region<Integer>(x1,y1,x2,y2);
	}
	
	/**
	 * A convenience method that converts this region of Integer pixels relative to
	 * the top left corner of the viewer to a region of BigDecimal points relative to the origin of the complex plane.
	 * @param r 			The Integer Region that is to be converted.
	 * @param region		The region of points that represents the currently viewed area of the screen
	 * @param pixelRegion	The region of pixels that corresponds to the region of points given above.
	 * 						This will typically be the square that represents the width and height of the canvas.
	 * @param precision		The precision to be used in BigDecimal calculations.
	 * @return				A region of BigDecimal points relative to the origin of the complex plane.
	 */
	public static Region<BigDecimal> toBigDecimalRegion(Region<Integer> r, Region<BigDecimal> region, Region<Integer> pixelRegion, int precision)
	{
		BigDecimal x1 = pixelToPointX(r.x1,region,pixelRegion,precision);
		BigDecimal y1 = pixelToPointY(r.y1,region,pixelRegion,precision);
		BigDecimal x2 = pixelToPointX(r.x2,region,pixelRegion,precision);
		BigDecimal y2 = pixelToPointY(r.y2,region,pixelRegion,precision);
		return new Region<BigDecimal>(x1,y1,x2,y2);
	}
	
	
	/**
	 * Generates a complete image of the Julia set with the given parameters. This may be a long running method.
	 * @param seed					The seed used to generate the Julia set. NOTE that this is the
	 * 								added to the initial seed during each iterations and not the "seed" itself
	 * 								and thus it is poorly named.
	 * @param pixelRegionSection	This is the section of pixels that will be calculated. It usually corresponds to a slim
	 * 								section of the pixelRegion, but it could be any rectangular shape. If you want to generate
	 * 								the full view, set this to be the same as pixelRegion.
	 * @param region				The region of BigDecimal points relative to the origin of the complex plane that maps
	 * 								to the pixelRegion provided.
	 * @param pixelRegion			The region of integer pixels relative to the top left corner of the viewer/image that maps
	 * 								to the region of BigDecimals points provided.
	 * @param iterations			The number of iterations used to render this image.
	 * @param arbPrecision			True if this method should use arbitrary precision BigDecimal to render.
	 * 								False if this method should use double precision. The use of double precision
	 * 								accumulates significant errors at magnifications of 10^16 and above, or earlier
	 * 								if the region is close to zero.
	 * @param precision				The precision that BigDecimal calculations should use.
	 * 								Has no meaningful effect if arbPrecision is set to false.
	 * @return						An Image that is the section of the Julia set given by pixelRegionSection of region.
	 */
	public WritableImage generateJuliaSet(ComplexBigDecimal seed,
			Region<Integer> pixelRegionSection,
			Region<BigDecimal> region,
			Region<Integer> pixelRegion,
			int iterations,
			boolean arbPrecision,
			int precision)
	{
		WritableImage image = new WritableImage(pixelRegionSection.getWidth().intValue(), pixelRegionSection.getHeight().intValue());
		PixelWriter writer = image.getPixelWriter();
		int x1,y1,x2,y2;
		x1 = pixelRegionSection.x1;
		x2 = pixelRegionSection.x2;
		y1 = pixelRegionSection.y1;
		y2 = pixelRegionSection.y2;
		for(int x = x1; x<x2; x++)
		{
			for(int y = y1; y<y2; y++)
			{
				if(interrupted)
				{
					return image;
				}
				int it;
				if(arbPrecision)
				{
					if((it=MandelbrotFunction.testPointBigDecimal(seed,toComplexBigDecimal(x,y, region, pixelRegion, precision), iterations))==0)
					{
						writer.setColor(x-x1, y-y1, Color.BLACK);
					}
					else
					{
						writer.setColor(x-x1, y-y1, colorFunction.getColor(it));
					}
				}
				else
				{
					if((it=MandelbrotFunction.testPoint(seed.toComplex(),toComplexBigDecimal(x,y, region, pixelRegion, precision).toComplex(), iterations))==0)
					{
						writer.setColor(x-x1, y-y1, Color.BLACK);
					}
					else
					{
						writer.setColor(x-x1, y-y1, colorFunction.getColor(it));
					}
				}
				incrementPixels();
			}
		}
		return image;
	}
	
	/**
	 * Generates a complete image of the Mandelbrot set with the given parameters. This may be a long running method.
	 * @param pixelRegionSection	This is the section of pixels that will be calculated. It usually corresponds to a slim
	 * 								section of the pixelRegion, but it could be any rectangular shape. If you want to generate
	 * 								the full view, set this to be the same as pixelRegion.
	 * @param region				The region of BigDecimal points relative to the origin of the complex plane that maps
	 * 								to the pixelRegion provided.
	 * @param pixelRegion			The region of integer pixels relative to the top left corner of the viewer/image that maps
	 * 								to the region of BigDecimals points provided.
	 * @param iterations			The number of iterations used to render this image.
	 * @param arbitraryPrecision	True if this method should use arbitrary precision BigDecimal to render.
	 * 								False if this method should use double precision. The use of double precision
	 * 								accumulates significant errors at magnifications of 10^16 and above, or earlier
	 * 								if the region is close to zero.
	 * @param precision				The precision that BigDecimal calculations should use.
	 * 								Has no meaningful effect if arbPrecision is set to false.
	 * @return						An Image that is the section of the Mandelbrot set given by pixelRegionSection of region.
	 */
	public WritableImage generateSet(Region<Integer> pixelRegionSection,
			Region<BigDecimal> region,
			Region<Integer> pixelRegion,
			int iterations,
			boolean arbitraryPrecision,
			int precision)
	{
		WritableImage image = new WritableImage(pixelRegionSection.getWidth().intValue(), pixelRegionSection.getHeight().intValue());
		PixelWriter writer = image.getPixelWriter();
		int x1,y1,x2,y2;
		x1 = pixelRegionSection.x1;
		x2 = pixelRegionSection.x2;
		y1 = pixelRegionSection.y1;
		y2 = pixelRegionSection.y2;
		if(arbitraryPrecision)
		{
			for(int x = x1; x<x2; x++)
			{
				for(int y = y1; y<y2; y++)
				{
					if(interrupted)
					{
						return image;
					}
					int it;
					if((it=MandelbrotFunction.testPointBigDecimal(toComplexBigDecimal(x,y, region, pixelRegion, precision),
							new ComplexBigDecimal("0","0", precision), iterations))==0)
					{
						writer.setColor(x-x1, y-y1, Color.BLACK);
					}
					else
					{
						writer.setColor(x-x1, y-y1, colorFunction.getColor(it));
					}
					incrementPixels();
				}
			}
		}
		else
		{
			for(int x = x1; x<x2; x++)
			{
				for(int y = y1; y<y2; y++)
				{
					if(interrupted)
					{
						return image;
					}
					int it;
					if((it=MandelbrotFunction.testPoint(toComplexBigDecimal(x,y, region, pixelRegion, precision).toComplex(), new Complex(0,0), iterations))==0)
					{
						writer.setColor(x-x1, y-y1, Color.BLACK);
					}
					else
					{
						writer.setColor(x-x1, y-y1, colorFunction.getColor(it));
					}
					incrementPixels();
				}
			}
		}
		return image;
	}
	
	/**
	 * Generates a complete image of the Mandelbrot set with the given parameters. 
	 * This method does not necessarily generate a complete image. It has the option
	 * of skipping pixels and filling in rough squares in there place.
	 * It also starts with an initial image and writes on top of that image.
	 * Returns the new image.
	 * The purpose of this method is to provide rough images that approximate the mandelbrot set in little time
	 * so as to provide intermediate view of the set.
	 * This may be a long running method.
	 * 
	 * @param pixelRegionSection	This is the section of pixels that will be calculated. It usually corresponds to a slim
	 * 								section of the pixelRegion, but it could be any rectangular shape. If you want to generate
	 * 								the full view, set this to be the same as pixelRegion.
	 * @param region				The region of BigDecimal points relative to the origin of the complex plane that maps
	 * 								to the pixelRegion provided.
	 * @param pixelRegion			The region of integer pixels relative to the top left corner of the viewer/image that maps
	 * 								to the region of BigDecimals points provided.
	 * @param iterations			The number of iterations used to render this image.
	 * @param arbPrecision			True if this method should use arbitrary precision BigDecimal to render.
	 * 								False if this method should use double precision. The use of double precision
	 * 								accumulates significant errors at magnifications of 10^16 and above, or earlier
	 * 								if the region is close to zero.
	 * @param precision				The precision that BigDecimal calculations should use.
	 * 								Has no meaningful effect if arbPrecision is set to false.
	 * @param pixelCalc 			The pixels to be calculated. Every multiple of this number will be calculated excluding
	 * 								multiples of the number {@code skip}. Pixels not calculated will be filled in with the color
	 * 								of the nearest pixel calculated.
	 * @param skip 					The pixels that will be skipped. Every multiple of this number pixel in both x and y
	 * 								directions will is guaranteed not to be calculated.
	 * @param image 				The initial image that this method writes over. For pixels that are skipped, this
	 * 								method leaves the underlying colors that image provides. For calculated pixels
	 * 								and pixels in the corresponding square, the method will write over the pixels.
	 * @return						An Image that is the section of the Mandelbrot set given by pixelRegionSection of region.
	 */
	public WritableImage generateSet(Region<Integer> pixelRegionSection,
			Region<BigDecimal> region,
			Region<Integer> pixelRegion,
			int iterations,
			boolean arbPrecision,
			int precision,
			int pixelCalc,
			int skip,
			WritableImage image)
	{
		boolean skipPixels = true;
		PixelWriter writer = image.getPixelWriter();
		int x1,y1,x2,y2;
		x1 = pixelRegionSection.x1;
		x2 = pixelRegionSection.x2;
		y1 = pixelRegionSection.y1;
		y2 = pixelRegionSection.y2;
		if(skip <=0)
		{
			skipPixels = false;
			//skip = 1;
		}
		for(int x = x1; x<x2; x+=pixelCalc)
		{
			for(int y = y1; y<y2; y+=pixelCalc)
			{
				if(!skipPixels || !(x%skip==0 && y%skip==0))
				{
					if(interrupted)
					{
						return image;
					}
					int it;
					if(arbPrecision)
					{
						if((it = MandelbrotFunction.testPointBigDecimal(toComplexBigDecimal(x,y, region, pixelRegion, precision),
								new ComplexBigDecimal(new BigDecimal("0"),new BigDecimal("0"),precision), iterations))==0)
						{
							for(int i = 0; i<pixelCalc; i++)
							{
								for(int j = 0; j<pixelCalc; j++)
								{
									if(x-x1+i<image.getWidth()&&y-y1+j<image.getHeight())
									writer.setColor(x-x1+i, y-y1+j, Color.BLACK);
								}
							}
						}
						else
						{
							for(int i = 0; i<pixelCalc; i++)
							{
								for(int j = 0; j<pixelCalc; j++)
								{
									if(x-x1+i<image.getWidth()&&y-y1+j<image.getHeight())
									writer.setColor(x-x1+i, y-y1+j, colorFunction.getColor(it));
								}
							}
						}
					}
					else
					{	
						if((it = MandelbrotFunction.testPoint(toComplexBigDecimal(x,y, region, pixelRegion, precision).toComplex(),
								new Complex(0,0), iterations))==0)
						{
							for(int i = 0; i<pixelCalc; i++)
							{
								for(int j = 0; j<pixelCalc; j++)
								{
									if(x-x1+i<image.getWidth()&&y-y1+j<image.getHeight())
									writer.setColor(x-x1+i, y-y1+j, Color.BLACK);
								}
							}
						}
						else
						{
							for(int i = 0; i<pixelCalc; i++)
							{
								for(int j = 0; j<pixelCalc; j++)
								{
									if(x-x1+i<image.getWidth()&&y-y1+j<image.getHeight())
									writer.setColor(x-x1+i, y-y1+j, colorFunction.getColor(it));
								}
							}
						}
					}
					incrementPixels();
				}
			}
		}
		return image;
	}
	
	/**
	 * Generates a complete image of the Julia set with the given parameters. 
	 * This method does not necessarily generate a complete image. It has the option
	 * of skipping pixels and filling in rough squares in there place.
	 * It also starts with an initial image and writes on top of that image.
	 * Returns the new image.
	 * The purpose of this method is to provide rough images that approximate the Julia set in little time
	 * so as to provide intermediate view of the set.
	 * This may be a long running method.
	 * @param seed 					The seed used to generate the Julia set. NOTE that this is the
	 * 								added to the initial seed during each iterations and not the "seed" itself
	 * 								and thus it is poorly named.
	 * @param pixelRegionSection	This is the section of pixels that will be calculated. It usually corresponds to a slim
	 * 								section of the pixelRegion, but it could be any rectangular shape. If you want to generate
	 * 								the full view, set this to be the same as pixelRegion.
	 * @param region				The region of BigDecimal points relative to the origin of the complex plane that maps
	 * 								to the pixelRegion provided.
	 * @param pixelRegion			The region of integer pixels relative to the top left corner of the viewer/image that maps
	 * 								to the region of BigDecimals points provided.
	 * @param iterations			The number of iterations used to render this image.
	 * @param arbPrecision			True if this method should use arbitrary precision BigDecimal to render.
	 * 								False if this method should use double precision. The use of double precision
	 * 								accumulates significant errors at magnifications of 10^16 and above, or earlier
	 * 								if the region is close to zero.
	 * @param precision				The precision that BigDecimal calculations should use.
	 * 								Has no meaningful effect if arbPrecision is set to false.
	 * @param pixelCalc 			The pixels to be calculated. Every multiple of this number will be calculated excluding
	 * 								multiples of the number {@code skip}. Pixels not calculated will be filled in with the color
	 * 								of the nearest pixel calculated.
	 * @param skip 					The pixels that will be skipped. Every multiple of this number pixel in both x and y
	 * 								directions will is guaranteed not to be calculated.
	 * @param image 				The initial image that this method writes over. For pixels that are skipped, this
	 * 								method leaves the underlying colors that image provides. For calculated pixels
	 * 								and pixels in the corresponding square, the method will write over the pixels.
	 * @return						An Image that is the section of the Julia set given by pixelRegionSection of region.
	 */
	public WritableImage generateJuliaSet(ComplexBigDecimal seed,
			Region<Integer> pixelRegionSection,
			Region<BigDecimal> region,
			Region<Integer> pixelRegion,
			int iterations,
			boolean arbPrecision,
			int precision,
			int pixelCalc,
			int skip,
			WritableImage image)
	{
		boolean skipPixels = true;
		PixelWriter writer = image.getPixelWriter();
		int x1,y1,x2,y2;
		x1 = pixelRegionSection.x1;
		x2 = pixelRegionSection.x2;
		y1 = pixelRegionSection.y1;
		y2 = pixelRegionSection.y2;
		if(skip <=0)
		{
			skipPixels = false;
			//skip = 1;
		}
		for(int x = x1; x<x2; x+=pixelCalc)
		{
			for(int y = y1; y<y2; y+=pixelCalc)
			{
				if(!skipPixels || !(x%skip==0 && y%skip==0))
				{
					if(interrupted)
					{
						return image;
					}
					int it;
					if(arbPrecision)
					{
						if((it=MandelbrotFunction.testPointBigDecimal(seed,toComplexBigDecimal(x,y, region, pixelRegion, precision), iterations))==0)
						{
							for(int i = 0; i<pixelCalc; i++)
							{
								for(int j = 0; j<pixelCalc; j++)
								{
									if(x-x1+i<image.getWidth()&&y-y1+j<image.getHeight())
									writer.setColor(x-x1+i, y-y1+j, Color.BLACK);
								}
							}
						}
						else
						{
							for(int i = 0; i<pixelCalc; i++)
							{
								for(int j = 0; j<pixelCalc; j++)
								{
									if(x-x1+i<image.getWidth()&&y-y1+j<image.getHeight())
									writer.setColor(x-x1+i, y-y1+j, colorFunction.getColor(it));
								}
							}
						}
					}
					else
					{	
						if((it=MandelbrotFunction.testPoint(seed.toComplex(),toComplexBigDecimal(x,y, region, pixelRegion, precision).toComplex(), iterations))==0)
						{
							for(int i = 0; i<pixelCalc; i++)
							{
								for(int j = 0; j<pixelCalc; j++)
								{
									if(x-x1+i<image.getWidth()&&y-y1+j<image.getHeight())
									writer.setColor(x-x1+i, y-y1+j, Color.BLACK);
								}
							}
						}
						else
						{
							for(int i = 0; i<pixelCalc; i++)
							{
								for(int j = 0; j<pixelCalc; j++)
								{
									if(x-x1+i<image.getWidth()&&y-y1+j<image.getHeight())
									writer.setColor(x-x1+i, y-y1+j, colorFunction.getColor(it));
								}
							}
						}
					}
					incrementPixels();
				}
			}
		}
		return image;
	}
	
	
	/**
	 * Sets this interrupted flag to the corresponding value. When this flag is true,
	 * any generator methods will return immediately after finishing the next pixel calculation
	 * and will return any image that they have calculated.
	 * @param interrupt			The value to be set.
	 */
	public void setInterrupt(boolean interrupt)
	{
		interrupted = interrupt;
	}
	
	/**
	 * Gets the interrupt status.
	 * @return the interrupt status.
	 */
	public boolean getInterrupt()
	{
		return interrupted;
	}
	
	/**
	 * Increments the pixelCalculated.
	 * Since this will be called by multiple threads, it must be synchronized.
	 * Counts the number of pixels that have been calculated for use in progress detection.
	 */
	public synchronized void incrementPixels()
	{
		pixelsCalculated++;
	}
	
	/**
	 * Returns the number of pixels that have been calculated.
	 * @return the number of pixels calculated.
	 */
	public int getPixelsCalculated()
	{
		return pixelsCalculated;
	}
	
	
	/**
	 * Resets the pixels calculated to zero.
	 * It is the responsibility of the class that uses the
	 * calculator to determine when the pixelsCalculated field should be reset.
	 */
	public synchronized void resetPixelsCalculated()
	{
		pixelsCalculated = 0;
	}
	

}
