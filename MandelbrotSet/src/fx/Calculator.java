package fx;

import java.math.*;
import colorFunction.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;

public class Calculator
{
	private boolean interrupted = false;
	private int pixelsCalculated = 0;
	private CustomColor colorFunction;
	
	public Calculator()
	{
		colorFunction = ColorFunction.ColorInfo.COLOR_FUNCTIONS.get(0);
	}
	
	public Calculator(CustomColor cf)
	{
		colorFunction = cf;
	}
	
	public CustomColor getColorFunction()
	{
		return colorFunction;
	}
	
	public void setColorFunction(CustomColor cf)
	{
		colorFunction = cf;
	}
	
	public static int pointToPixelX(BigDecimal x, Region<BigDecimal> region, Region<Integer> pixelRegion, int precision)
	{
		BigDecimal scale = pixelRegion.getWidth().divide(region.getWidth(),precision,BigDecimal.ROUND_HALF_UP);
		return x.subtract(region.x1).multiply(scale).add(new BigDecimal(pixelRegion.x1)).intValue();
	}
	
	public static int pointToPixelY(BigDecimal y, Region<BigDecimal> region, Region<Integer> pixelRegion, int precision)
	{
		BigDecimal scale = pixelRegion.getHeight().divide(region.getHeight(),precision,BigDecimal.ROUND_HALF_UP);
		return y.subtract(region.y1).multiply(scale).add(new BigDecimal(pixelRegion.y1)).intValue();
	}
	
	public static BigDecimal pixelToPointX(int x, Region<BigDecimal> region, Region<Integer> pixelRegion, int precision)
	{
		BigDecimal scale = pixelRegion.getWidth().divide(region.getWidth(),precision,BigDecimal.ROUND_HALF_UP);
		return new BigDecimal(x-pixelRegion.x1).divide(scale,precision,BigDecimal.ROUND_HALF_UP).add(region.x1);
	}
	
	public static BigDecimal pixelToPointY(int y, Region<BigDecimal> region, Region<Integer> pixelRegion, int precision)
	{
		BigDecimal scale = pixelRegion.getHeight().divide(region.getHeight(),precision,BigDecimal.ROUND_HALF_UP);
		return region.y1.subtract((new BigDecimal(y-pixelRegion.y1).divide(scale,precision,BigDecimal.ROUND_HALF_UP)));
	}
	
	public static ComplexBigDecimal toComplexBigDecimal(int x, int y, Region<BigDecimal> region, Region<Integer> pixelRegion, int precision)
	{
		return new ComplexBigDecimal(pixelToPointX(x, region, pixelRegion, precision), pixelToPointY(y, region, pixelRegion, precision),precision);
	}
	
	public static Region<Integer> toPixelRegion(Region<BigDecimal> r, Region<BigDecimal> region, Region<Integer> pixelRegion, int precision)
	{
		int x1 = pointToPixelX(r.x1, region, pixelRegion, precision);
		int y1 = pointToPixelY(r.y1, region, pixelRegion, precision);
		int x2 = pointToPixelX(r.x2, region, pixelRegion, precision);
		int y2 = pointToPixelY(r.y2, region, pixelRegion, precision);
		return new Region<Integer>(x1,y1,x2,y2);
	}
	
	public static Region<BigDecimal> toBigDecimalRegion(Region<Integer> r, Region<BigDecimal> region, Region<Integer> pixelRegion, int precision)
	{
		BigDecimal x1 = pixelToPointX(r.x1,region,pixelRegion,precision);
		BigDecimal y1 = pixelToPointY(r.y1,region,pixelRegion,precision);
		BigDecimal x2 = pixelToPointX(r.x2,region,pixelRegion,precision);
		BigDecimal y2 = pixelToPointY(r.y2,region,pixelRegion,precision);
		return new Region<BigDecimal>(x1,y1,x2,y2);
	}
	
	
	
	public WritableImage generateJuliaSet(ComplexBigDecimal seed, Region<Integer> pixelRegionSection, Region<BigDecimal> region, Region<Integer> pixelRegion, int iterations, boolean arbPrecision, int precision)
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
	
	public WritableImage generateSet(Region<Integer> pixelRegionSection, Region<BigDecimal> region, Region<Integer> pixelRegion, int iterations, boolean arbitraryPrecision, int precision)
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
	
	public WritableImage generateSet(Region<Integer> pixelRegionSection, Region<BigDecimal> region, Region<Integer> pixelRegion,int iterations, boolean arbPrecision, int precision, int pixelCalc, int skip, WritableImage image)
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
	
	public WritableImage generateJuliaSet(ComplexBigDecimal seed, Region<Integer> pixelRegionSection, Region<BigDecimal> region, Region<Integer> pixelRegion,int iterations, boolean arbPrecision, int precision, int pixelCalc, int skip, WritableImage image)
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
	
	
	
	public void setInterrupt(boolean interrupt)
	{
		interrupted = interrupt;
	}
	
	public boolean getInterrupt()
	{
		return interrupted;
	}
	
	public synchronized void incrementPixels()
	{
		pixelsCalculated++;
	}
	
	public int getPixelsCalculated()
	{
		return pixelsCalculated;
	}
	
	public void setPixelsCalculated(int pixels)
	{
		pixelsCalculated = pixels;
	}
	

}
