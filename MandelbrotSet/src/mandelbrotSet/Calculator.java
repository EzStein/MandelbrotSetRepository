package mandelbrotSet;
import java.awt.image.*;
import java.math.*;
import java.util.*;
import java.awt.*;
/**
 * 
 * This class creates a Thread used to calculate a region of the Mandelbrot or Julia set
 * Multiple Calculators are created and each is assigned a different region of the ViewerPanel to calculate.
 * After the calculator has terminated, it sends its image to the viewerPanel to be drawn.
 * The Thread part of this class uses a zoom algorithm, which first calculates
 * every 64th pixel, then every 16th pixel and then the remaining pixels.
 * The non-Runnable part of this class is used for off-screen calculations.
 * It returns a BufferedImage to the caller for instance to be saved to a file.
 * This class also defines several hashes of equal length representing color-schemes.
 * Each has receives an int from 0-380 and returns a color. Using the modulus function on the returned number of iterations,
 * a color may be assigned to every pixel, with zero iterations indicating black.
 * 
 * @author Ezra Stein
 * @version 1.0
 * @since 2015
 */
public class Calculator implements Runnable
{
	int fWidth, fHeight, cornerX1, cornerY1, cornerX2, cornerY2, precision, iteration,relativeX, relativeY;;
	double x1,y1,x2,y2, sWidth, sHeight, juliaX, juliaY;
	BigDecimal bx1,by1,bx2,by2, bsWidth, bsHeight, bJuliaX,bJuliaY;
	
	/**
	 * Holds a reference to the drawingPanel which created this object.
	 * The drawingPanel could be either the previewPanel or the viewerPanel.
	 */
	DrawingPanel viewerPanel;
	
	/**
	 * True when arbitrary precision is desired.
	 */
	boolean exact;
	
	/**
	 * True when the Julia set should be calculated.
	 */
	boolean julia;
	
	/**
	 * Initially false. Becomes true when the Calculator is asked to end its operations.
	 */
	boolean interrupted;
	
	/**
	 * Holds the color scheme of this calculator.
	 */
	String colorScheme;
	
	/**
	 * A hash which contains information about the color scheme.
	 * Maps a given iteration int to a color.
	 */
	HashMap<Integer, Color> hm = new HashMap<Integer, Color>();
	
	/**
	 * Constructs a Calculator with multiple parameters. The calculator will be given a region (boundary) of the set in absolute coordinates.
	 * Using the width and height of the viewerPanel, it will convert each pixel into absolute coordinates within the boundary and test
	 * them to see if they are in the set. The calculator will only calculate a small region of the panel. This region is called the calculatedRegion.
	 * This allows for multiple calculators to focus to work on the same boundary but to calculate different parts of that boundary. The calculator will test to
	 * if it should calculate the corresponding Julia or Mandelbrot sets and will use the seedOfJulia variables if necessary.
	 * If arbitrary precision is turned on, the calculator will use a given precision in calculations. It passes all images to
	 * an instance of a drawingPanel. If the drawingPanel handles the image as a pannedImage, it will use relativeX and relativeY to specify
	 * the relative distance from the top left hand corner of the main image. A colorScheme is used.
	 * 
	 * @param width				width of the drawingPanel that is calling  this constructor.
	 * @param height			height of the drawingPanel that is calling  this constructor.
	 * @param calculatedRegion	the rectangle or pixels which this calculator will test.
	 * @param boundary			the total region that corresponds to the drawingPanel.
	 * @param iterations		the number of iterations to be used.
	 * @param julia				whether the calculator should create a Julia or a Mandelbrot set.
	 * @param seedOfJuliaX		used to calculate Julia set.
	 * @param seedOfJuliaY		used to calculate Julia set.
	 * @param exact				whether to use double precision (inexact) or arbitrary precision (exact).
	 * @param precision			the precision of the BigDecimal values used when exact is true.
	 * @param drawingPanel		a reference to the drawingPanel which called this constructor.
	 * @param relativeX			if a panned image, this represents the relative distance from the top left hand corner of the main image.
	 * @param relativeY			if a panned image, this represents the relative distance from the top left hand corner of the main image.
	 * @param colorScheme		the color scheme to be used.
	 */
	public Calculator(int width, int height, Rectangle calculatedRegion, Region boundary, int iterations, boolean julia, BigDecimal seedOfJuliaX, BigDecimal seedOfJuliaY, boolean exact,int precision, DrawingPanel drawingPanel, int relativeX, int relativeY, String colorScheme)
	{
		
		this.fWidth = width;
		this.fHeight = height;
		this.cornerX1 = (int)calculatedRegion.getX();
		this.cornerY1 = (int)calculatedRegion.getY();
		this.cornerX2 = (int)(calculatedRegion.getX()+calculatedRegion.getWidth());
		this.cornerY2 = (int)(calculatedRegion.getY()+calculatedRegion.getHeight());
		this.exact = exact;
		this.viewerPanel = drawingPanel;
		this.julia = julia;
		this.precision = precision;
		this.iteration = iterations;
		this.relativeX = relativeX;
		this.relativeY = relativeY;
		this.colorScheme = colorScheme;
		makeHash();
		interrupted = false;
		if(exact)
		{
			bx1 = boundary.getX1();
			by1 = boundary.getY1();
			bx2 = boundary.getX2();
			by2 = boundary.getY2();
			bJuliaX = seedOfJuliaX;
			bJuliaY = seedOfJuliaY;
			bsWidth = bx1.subtract(bx2).abs();
			bsHeight = by1.subtract(by2).abs();
			
		}
		else
		{
			x1 = boundary.getX1().doubleValue();
			y1 = boundary.getY1().doubleValue();
			x2 = boundary.getX2().doubleValue();
			y2 = boundary.getY2().doubleValue();
			juliaX = seedOfJuliaX.doubleValue();
			juliaY = seedOfJuliaY.doubleValue();
			sWidth = Math.abs(x1-x2);
			sHeight = Math.abs(y1-y2);
		}
	}
	
	/**
	 * Creates a calculator which will only make the hashes of different colorSchemes.
	 * Used by the viewerPanel when it wants to get a specific hash for a colorScheme without making a real calculator.
	 * 
	 * @param colorScheme	The colorScheme that is associated with this Calculator.
	 */
	public Calculator(String colorScheme)
	{
		this.colorScheme = colorScheme;
		makeHash();
	}
	
	/**
	 * Called when the user decides to interrupt the loading process. This sets the interrupted field to true
	 * which is caught by all the thread causing it to return.
	 */
	public void interrupt()
	{
		interrupted = true;
	}
	
	/**
	 * Called by the drawing panel when it is ready to begin calculations.
	 * Decides to create either an exact or inexact image.
	 */
	@Override	
	public void run()
	{
		if(exact)
		{
			exactZoomAlgorithm3Step();
		}
		else
		{
			zoomAlgorithm3Step();
		}
	}
	
	/**
	 * Called by the viewerPanel when it is saving an image. It initiates calculation of the image, and returns the result.
	 * @return the image to be saved.
	 */
	public BufferedImage getImage()
	{
		if(exact)
		{
			return getExactImage();
		}
		else
		{
			return getDoubleImage();
		}
	}
	
	/**
	 * Calculates an inexact image based on the parameters and returns the result.
	 * @return the image to be saved or used.
	 */
	public BufferedImage getDoubleImage()
	{
		BufferedImage bi = new BufferedImage(Math.abs(cornerX1-cornerX2), Math.abs(cornerY1-cornerY2), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bi.createGraphics();
		g2d.setColor(Color.BLACK);
		int x = cornerX1;
		int y = cornerY1;
		int a = 0;
		Complex c;
		while(x<= cornerX2)
		{
			while(y<=cornerY2)
			{
				if(interrupted)
				{
					return null;
				}
				c = toComplex(x,y);
				if(julia)
				{
					a = MandelbrotFunction.testPoint(new Complex(juliaX, juliaY), c, iteration);
				}
				else
				{
					a = MandelbrotFunction.testPoint(c, new Complex(0,0), iteration);
				}
				if(a == 0)
				{
					g2d.setColor(Color.BLACK);
				}
				else
				{
					g2d.setColor(hm.get(new Integer(a % hm.size())));
				}
				viewerPanel.updateProgress();
				g2d.fillRect(x-cornerX1,y - cornerY1,1,1);
				y++;
			}
			y= cornerY1;
			x++;
		}
		return bi;
	}
	
	/**
	 * Calculates an exact image using arbitrary precision based on the parameters and returns the result.
	 * @return the image to be saved or used.
	 */
	public BufferedImage getExactImage()
	{
		BufferedImage bi = new BufferedImage(Math.abs(cornerX1-cornerX2), Math.abs(cornerY1-cornerY2), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bi.createGraphics();
		g2d.setColor(Color.BLACK);
		int x = cornerX1;
		int y = cornerY1;
		int a = 0;
		ComplexBigDecimal c;
		while(x<= cornerX2)
		{
			while(y<=cornerY2)
			{
				if(interrupted)
				{
					return null;
				}
				c = toComplexBigDecimal(x,y);
				if(julia)
				{
					a = MandelbrotFunction.testPointBigDecimal(new ComplexBigDecimal(bJuliaX,bJuliaY,precision), c, iteration);
				}
				else
				{
					a = MandelbrotFunction.testPointBigDecimal(c, new ComplexBigDecimal("0","0",precision), iteration);
				}
				if(a == 0)
				{
					g2d.setColor(Color.BLACK);
				}
				else
				{
					g2d.setColor(hm.get(new Integer(a % hm.size())));
				}
				viewerPanel.updateProgress();
				g2d.fillRect(x-cornerX1,y - cornerY1,1,1);
				viewerPanel.updateProgress();
				y++;
			}
			y= cornerY1;
			x++;
		}
		return bi;
	}
	
	/**
	 * Calculates an image and sends that to the drawingPanel. It uses a 3 step algorithm.
	 * It first calculates the top left pixel in every 8x8 square of pixels and sends the rough
	 * drawing to the viewerPanel. It then calculates the the top left pixel in every 4x4 square
	 * that hasn't already been calculated, and sends that result to the panel. Then it calculates the rest of the pixels.
	 * Uses double precision
	 */
	public void zoomAlgorithm3Step()
	{
		BufferedImage bi = new BufferedImage(Math.abs(cornerX1-cornerX2), Math.abs(cornerY1-cornerY2), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bi.createGraphics();
		g2d.setColor(Color.BLACK);
		int x = cornerX1;
		int y = cornerY1;
		int a = 0;
		Complex c;
		while(x<= cornerX2)
		{
			while(y<=cornerY2)
			{
				if(interrupted)
				{
					return;
				}
				c = toComplex(x,y);
				if(julia)
				{
					a = MandelbrotFunction.testPoint(new Complex(juliaX, juliaY), c, iteration);
				}
				else
				{
					a = MandelbrotFunction.testPoint(c, new Complex(0,0), iteration);
				}
				if(a == 0)
				{
					g2d.setColor(Color.BLACK);
				}
				else
				{
					g2d.setColor(hm.get(new Integer(a % hm.size())));
				}
				g2d.fillRect(x-cornerX1,y - cornerY1,8,8);
				viewerPanel.updateProgress();
				y+=8;
			}
			y= cornerY1;
			x+=8;
		}
		x = cornerX1;
		y = cornerY1+2;
		viewerPanel.drawImage(bi,cornerX1, cornerY1, relativeX, relativeY);
		while(x<= cornerX2)
		{
			while(y<=cornerY2)
			{
				if(interrupted)
				{
					return;
				}
				c = toComplex(x,y);
				if(julia)
				{
					a = MandelbrotFunction.testPoint(new Complex(juliaX, juliaY), c, iteration);
				}
				else
				{
					a = MandelbrotFunction.testPoint(c, new Complex(0,0), iteration);
				}
				if(a == 0)
				{
					g2d.setColor(Color.BLACK);
				}
				else
				{
					g2d.setColor(hm.get(new Integer(a % hm.size())));
				}
				g2d.fillRect(x-cornerX1,y - cornerY1,2,2);
				viewerPanel.updateProgress();
				if(x%8 == 0)
				{
					if((y+2)%8==0)
					{
						y+=4;
					}
					else
					{
						y += 2;
					}	
				}
				else
				{
					y+=2;
				}	
			}
			y= cornerY1;
			x+=2;	
		}
		
		x = cornerX1;
		y = cornerY1+1;
		viewerPanel.drawImage(bi,cornerX1, cornerY1, relativeX, relativeY);
		
		while(x<= cornerX2)
		{
			while(y<=cornerY2)
			{
				if(interrupted)
				{
					return;
				}
				c = toComplex(x,y);
				if(julia)
				{
					a = MandelbrotFunction.testPoint(new Complex(juliaX, juliaY), c, iteration);
				}
				else
				{
					a = MandelbrotFunction.testPoint(c, new Complex(0,0), iteration);
				}
				if(a == 0)
				{
					g2d.setColor(Color.BLACK);
				}
				else
				{
					g2d.setColor(hm.get(new Integer(a % hm.size())));
				}
				g2d.fillRect(x-cornerX1,y - cornerY1,1,1);
				viewerPanel.updateProgress();
				if(x%2 == 0)
				{
					y+=2;
				}
				else
				{
					y++;
				}
			}
			x++;
			if(x%2 == 0)
			{
				y= cornerY1+1;
			}
			else
			{
				y = cornerY1;
			}
			
		}
		viewerPanel.drawImage(bi,cornerX1, cornerY1, relativeX, relativeY);
	}
	
	/**
	 * Calculates an image and sends that to the drawingPanel. It uses a 3 step algorithm.
	 * It first calculates the top left pixel in every 8x8 square of pixels and sends the rough
	 * drawing to the viewerPanel. It then calculates the the top left pixel in every 4x4 square
	 * that hasn't already been calculated, and sends that result to the panel. Then it calculates the rest of the pixels.
	 * Uses arbitrary precision
	 */
	public void exactZoomAlgorithm3Step()
	{
		BufferedImage bi = new BufferedImage(Math.abs(cornerX1-cornerX2), Math.abs(cornerY1-cornerY2), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bi.createGraphics();
		g2d.setColor(Color.BLACK);
		int x = cornerX1;
		int y = cornerY1;
		int a = 0;
		ComplexBigDecimal c;
		while(x<= cornerX2)
		{
			while(y<=cornerY2)
			{
				if(interrupted)
				{
					return;
				}
				c = toComplexBigDecimal(x,y);
				if(julia)
				{
					a = MandelbrotFunction.testPointBigDecimal(new ComplexBigDecimal(bJuliaX,bJuliaY,precision), c, iteration);
				}
				else
				{
					a = MandelbrotFunction.testPointBigDecimal(c, new ComplexBigDecimal("0","0",precision), iteration);
				}
				if(a == 0)
				{
					g2d.setColor(Color.BLACK);
				}
				else
				{
					g2d.setColor(hm.get(new Integer(a % hm.size())));
				}
				g2d.fillRect(x-cornerX1,y - cornerY1,8,8);
				viewerPanel.updateProgress();
				y+=8;
			}
			y= cornerY1;
			x+=8;
		}
		x = cornerX1;
		y = cornerY1+2;
		viewerPanel.drawImage(bi,cornerX1, cornerY1, relativeX, relativeY);
		while(x<= cornerX2)
		{
			while(y<=cornerY2)
			{
				if(interrupted)
				{
					return;
				}
				c = toComplexBigDecimal(x,y);
				if(julia)
				{
					a = MandelbrotFunction.testPointBigDecimal(new ComplexBigDecimal(bJuliaX,bJuliaY,precision), c, iteration);
				}
				else
				{
					a = MandelbrotFunction.testPointBigDecimal(c, new ComplexBigDecimal("0","0",precision), iteration);
				}
				if(a == 0)
				{
					g2d.setColor(Color.BLACK);
				}
				else
				{
					g2d.setColor(hm.get(new Integer(a % hm.size())));
				}
				g2d.fillRect(x-cornerX1,y - cornerY1,2,2);
				viewerPanel.updateProgress();
				if(x%8 == 0)
				{
					if((y+2)%8==0)
					{
						y+=4;
					}
					else
					{
						y += 2;
					}
					
				}
				else
				{
					y+=2;
				}
				
			}
			
			y= cornerY1;
			x+=2;
			
		}
		
		
		x = cornerX1;
		y = cornerY1+1;
		viewerPanel.drawImage(bi,cornerX1, cornerY1, relativeX, relativeY);
		
		while(x<= cornerX2)
		{
			while(y<=cornerY2)
			{
				if(interrupted)
				{
					return;
				}
				c = toComplexBigDecimal(x,y);
				if(julia)
				{
					a = MandelbrotFunction.testPointBigDecimal(new ComplexBigDecimal(bJuliaX,bJuliaY,precision), c, iteration);
				}
				else
				{
					a = MandelbrotFunction.testPointBigDecimal(c, new ComplexBigDecimal("0","0",precision), iteration);
				}
				if(a == 0)
				{
					g2d.setColor(Color.BLACK);
				}
				else
				{
					g2d.setColor(hm.get(new Integer(a % hm.size())));
				}
				g2d.fillRect(x-cornerX1,y - cornerY1,1,1);
				viewerPanel.updateProgress();
				if(x%2 == 0)
				{
					y+=2;
				}
				else
				{
					y++;
				}
			}
			x++;
			if(x%2 == 0)
			{
				y= cornerY1+1;
			}
			else
			{
				y = cornerY1;
			}
			
		}
		viewerPanel.drawImage(bi,cornerX1, cornerY1, relativeX, relativeY);
	}
	
	/**
	 * Takes a pixel of (x,y) form and converts it to a complex number based on the
	 * boundary of the region and its the width and height of the panel.
	 * @param x	the x-coordinate of the pixel to be converted to a complex number.
	 * @param y	the y-coordinate of the pixel to be converted to a complex number.
	 * @return the corresponding complex number representing a pixel on the panel.
	 */
	public Complex toComplex(double x, double y)
	{
		double scaledWidth = sWidth/fWidth;
		double scaledHeight = sHeight/fHeight;
		double scaledX = x1+x*scaledWidth;
		double scaledY = y1-y*scaledHeight;
		
		return new Complex(scaledX,scaledY);
	}
	
	/**
	 * Takes a pixel of (x,y) form and converts it to a complex number based on the
	 * boundary of the region and its the width and height of the panel. Uses arbitrary precision.
	 * @param x	the x-coordinate of the pixel to be converted to a complex number.
	 * @param y	the y-coordinate of the pixel to be converted to a complex number.
	 * @return the corresponding complex number representing a pixel on the panel.
	 */
	public ComplexBigDecimal toComplexBigDecimal(int x, int y)
	{
		BigDecimal scaledWidth = bsWidth.divide(new BigDecimal(fWidth), precision, BigDecimal.ROUND_HALF_UP);
		BigDecimal scaledHeight = bsHeight.divide(new BigDecimal(fHeight), precision, BigDecimal.ROUND_HALF_UP);
		BigDecimal scaledX = scaledWidth.multiply(new BigDecimal(x)).add(bx1);
		BigDecimal scaledY = by1.subtract(scaledHeight.multiply(new BigDecimal(y)));
		
		return new ComplexBigDecimal(scaledX,scaledY,precision);
	}
	
	/**
	 * Makes a rainbow color scheme stored in a hash.
	 */
	public void makeHashRainbow()
    {
        int i = 0;
        int R = 255, G = 0, B = 0;
        while(i < 127)
        {
            hm.put(i, new Color(R, G, B));
            R-= 2;
            G+= 2;
            i++;
        }
        R = 0; G = 255; B = 0;
        while(i < 254)
        {
            hm.put(i, new Color(R, G, B));
            G -= 2;
            B += 2;
            i++;
        }
        R = 0; G = 0; B = 255;
        while(i < 381)
        {
            hm.put(i, new Color(R, G, B));
            R += 2;
            B -= 2;
            i ++;
        }
    }
	
	/**
	 * Makes a pink blue color scheme stored in a hash.
	 */
	public void makeHashPinkBlue()
    {
        int i = 0;
        int R = 255, G = 127, B = 127;
        while(i < 127)
        {
            hm.put(i, new Color(R, G, B));
            R-= 2;
            B++;
            i++;
        }
        while(i<254)
        {
        	R++;
        	G++;
        	hm.put(i, new Color(R, G, B));
        	i++;
        }
        while(i<381)
        {
        	R++;
        	B--;
        	G--;
        	hm.put(i, new Color(R, G, B));
        	i++;
        }
    }
	
	/**
	 * Makes a blue purple color scheme stored in a hash.
	 */
	public void makeHashBluePurple()
	{
		int i = 0;
        int R = 0, G = 127, B = 127;
        while(i < 127)
        {
            hm.put(i, new Color(R, G, B));
            G++;
            B++;
            i++;
        }
        while(i < 254)
        {
           
            G--;
            R++;
            hm.put(i, new Color(R, G, B));
            i++;
        }
        while(i<381)
        {
        	R--;
            B--;
            hm.put(i, new Color(R, G, B));
            i++;
        }
	}
	
	/**
	 * Makes a green purple color scheme stored in a hash.
	 */
	public void makeHashGreenPurple()
	{
		int i = 0;
        int R = 0, G = 255, B = 127;
        while(i < 127)
        {
            hm.put(i, new Color(R, G, B));
            R++;
            G--;
            i++;
        }
        while(i < 254)
        {
           
            R++;
            B++;
            hm.put(i, new Color(R, G, B));
            i++;
        }
        while(i< 381)
        {
        	R-= 2;
        	B--;
        	G++;
            hm.put(i, new Color(R, G, B));
            i++;
        }
	}
	
	/**
	 * Makes a Dr. Seuss color scheme stored in a hash.
	 * Since this hash has a smaller length then the others, when the viewerPanel converts from this scheme to
	 * another it looses information and the region must be recalculated.
	 */
	public void makeHashDrSeuss()
	{
		hm.put(0, new Color(0, 0, 255));
		hm.put(1, new Color(0, 255, 0));
	}
	
	/**
	 * Makes a blue gray blue scheme stored in a hash.
	 */
	public void makeHashGrayBlue()
	{
		int i = 0;
        int R = 127, G = 127, B = 127;
        while(i < 127)
        {
            hm.put(i, new Color(R, G, B));
            R--;
            G--;
            B++;
            i++;
        }
        while(i < 254)
        {
           
            B--;
            hm.put(i, new Color(R, G, B));
            i++;
        }
        while(i< 381)
        {
        	R++;
        	G++;
            hm.put(i, new Color(R, G, B));
            i++;
        }
	}
	
	/**
	 * Makes a blue winter wonderland scheme stored in a hash.
	 */
	public void makeHashWinterWonderland()
	{
		int i = 0;
        int R = 255, G = 255, B = 255;
        while(i < 127)
        {
            hm.put(i, new Color(R, G, B));
            R--;
            G--;
            i++;
        }
        while(i < 254)
        {
           
            B--;
            hm.put(i, new Color(R, G, B));
            i++;
        }
        while(i< 381)
        {
        	R++;
        	G++;
        	B++;
            hm.put(i, new Color(R, G, B));
            i++;
        }
	}
	
	/**
	 * Makes a gothic black color scheme stored in a hash.
	 */
	public void makeHashGothicBlack()
	{
		int i = 0;
        int R = 0, G = 0, B = 0;
        while(i < 127)
        {
            hm.put(i, new Color(R, G, B));
            R++;
            B++;
            i++;
        }
        while(i < 254)
        {
           
            B--;
            hm.put(i, new Color(R, G, B));
            i++;
        }
        while(i< 381)
        {
        	R--;
            hm.put(i, new Color(R, G, B));
            i++;
        }
	}
	
	/**
	 * Makes a blue magenta color scheme stored in a hash.
	 */
	public void makeHashBlueMagenta()
	{
		int i = 0;
        int R = 0, G = 127, B = 255;
        while(i < 127)
        {
            hm.put(i, new Color(R, G, B));
            R++;
            G--;
            B--;
            i++;
        }
        while(i < 254)
        {
           
            B--;
            hm.put(i, new Color(R, G, B));
            i++;
        }
        while(i< 381)
        {
        	R--;
        	G++;
        	B+=2;
            hm.put(i, new Color(R, G, B));
            i++;
        }
	}
	
	/**
	 * Makes a yellow orange color scheme stored in a hash.
	 */
	public void makeHashYellowOrange()
	{
		int i = 0;
        int R = 255, G = 127, B = 0;
        while(i < 127)
        {
            hm.put(i, new Color(R, G, B));
            G++;
            i++;
        }
        while(i < 254)
        {
           
            R--;
            G--;
            hm.put(i, new Color(R, G, B));
            i++;
        }
        while(i< 381)
        {
        	R++;
            hm.put(i, new Color(R, G, B));
            i++;
        }
	}
	
	/**
	 * Determines which color scheme to use and makes the corresponding hash.
	 */
	public void makeHash()
	{
		if(colorScheme.equals("Winter Wonderland"))
		{
			makeHashWinterWonderland();
		}
		else if(colorScheme.equals("Rainbow"))
		{
			makeHashRainbow();
		}
		else if(colorScheme.equals("Pink Blue"))
		{
			makeHashPinkBlue();
		}
		else if(colorScheme.equals("Blue Purple"))
		{
			makeHashBluePurple();
		}
		else if(colorScheme.equals("Green Purple"))
		{
			makeHashGreenPurple();
		}
		else if(colorScheme.equals("Dr. Seuss"))
		{
			makeHashDrSeuss();
		}
		else if(colorScheme.equals("Gray Blue"))
		{
			makeHashGrayBlue();
		}
		else if(colorScheme.equals("Gothic Black"))
		{
			makeHashGothicBlack();
		}
		else if(colorScheme.equals("Blue Magenta"))
		{
			makeHashBlueMagenta();
		}
		else if(colorScheme.equals("Yellow Orange"))
		{
			makeHashYellowOrange();
		}
		else
		{
			makeHashRainbow();
		}
	}
	
	/**
	 * Returns the color scheme of this calculator in the form of a hash.
	 * @return the hash representing this color scheme.
	 */
	public HashMap<Integer, Color> getHash()
	{
		return hm;
	}
}
