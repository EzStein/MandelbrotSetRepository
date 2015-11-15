package mandelbrotSet;
import javax.swing.*;
import javax.imageio.*;

import java.io.*;
import java.math.*;
import java.awt.*;
import java.util.*;
import java.util.Timer;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;

/**
 * The viewer panel acts as the main viewing panel in the CENTER of the GUI's container. It renders the Mandelbrot and Julia sets.
 * 
 * @author Ezra Stein
 * @version 1.0
 * @since 2015
 */
public class ViewerPanel extends DrawingPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
{

	private static final long serialVersionUID = 1L;
	
	/**
	 * Stores the main image that is painted onto the panel after each zoom.
	 */
	BufferedImage centerImage;
	
	/**
	 * Used only during a continuous zoom with the mouse wheel. It holds the
	 * original image of of the set before the zoom was initiated allowing for
	 * a correct transformation of that image.
	 */
	BufferedImage centerImageOriginal;
	
	/**
	 * The width of this panel.
	 */
	int width;
	
	/**
	 * The height of this panel.
	 */
	int height;
	
	/**
	 * Holds the precision of the BigDecimal numbers used in this program.
	 */
	int precision;
	
	/**
	 * Holds the magnification of the current image.
	 */
	BigDecimal magnification;
	
	BigDecimal centerX;
	BigDecimal centerY;
	BigDecimal juliaX = new BigDecimal("0");
	BigDecimal juliaY = new BigDecimal("0");
	
	/**
	 * Used to measure the efficiency of each zoom.
	 */
	long startTime, stopTime;
	
	/**
	 * Describes the position of the top left corner of the main image or central image which
	 * is initially drawn when after a zoom. These values are zero until a the user pans across the image using
	 * key press.
	 */
	int centerImageX, centerImageY;
	
	boolean idle = false, exact = false, asked = false, julia = false;
	
	/**
	 * A reference to the GUI that contains this panel.
	 */
	MandelbrotGUI mandelbrotGUI;
	int iterations;
	ArrayList<Region> savedRegions = new ArrayList<Region>();
	ArrayList<Calculator> calculators = new ArrayList<Calculator>();
	Updater updater;
	int totalPixels, pixelsComplete = 0;
	JFileChooser fileChooser = new JFileChooser();
	ProgressMonitor progressMonitor;
	ViewerPanel viewerPanel = this;
	ArrayList<PannedImage> pannedImages;
	int boxX1 = 0,boxX2 = 0,boxY1 = 0,boxY2 = 0;
	String colorScheme;
	boolean first = true;
	double zoomFactor = 0;
	double zoomDivisor = 200;
	int mouseX, mouseY;
	int val;
	boolean retina;
	Timer timer;
	int threadNumber, actualThreadNumber,threadsTerminated;
	
	/**
	 * This represents the region of the panel after it has just completed a zoom.
	 * IT DOES NOT CHANGE DURING A PAN. It must remain constant at that time so
	 * that the {@code toComplex(Rectangle rectangle)} method makes the correct conversion
	 * from pixels to region during a pan. Once a zoom command has been given (such as a click) this
	 * field is given the same value as {@code temporaryFrameRegion}. From here it can be used by the
	 * {@code toComplex(Rectangle rectangle)} method to properly convert the proposed zoom rectangle into a
	 * region. It then sets itself to that new value and {@code temporaryFrameRegion} is also set to it.
	 */
	Region region;
	
	/**
	 * This field always represents the region around the panel at all times including after it has panned.
	 * This field should only differ from the {@code region} field during or after a pan, but before a zoom command
	 * has been given. At that time, the {@code region} will be assigned to the same value as this one in order to calculate
	 * zoomed in region. That calculated region will then be assigned to both this field and the {@code region} field.
	 */
	Region temporaryFrameRegion;
	
	/**
	 * Constructs a viewerPanel with a certain width and height and a reference to the GUI higher up in the hierarchy.
	 * A certain number of threads are used to calculate the panel. Each thread is a calculator. The program
	 * divides up the panel evenly and gives a section to each calculator for calculation.
	 * 
	 * @param width - The width of the panel.
	 * @param height - The height of the panel.
	 * @param threads - The initial number of threads to use in the calculation. Should not exceed the size of the panel and must be greater than zero.
	 * @param mgui - Reference to the upper GUI.
	 */
	public ViewerPanel(int width,int height, int threads, MandelbrotGUI mgui)
	{
		mandelbrotGUI = mgui;
		retina = mandelbrotGUI.getRetina();
		threadNumber = threads;
		actualThreadNumber = threadNumber;
		this.width = width;
		this.height = height;
		
		if(retina)
		{
			setPreferredSize(new Dimension((int) (0.5*width),(int) (0.5*height)));
		}
		else
		{
			setPreferredSize(new Dimension(width,height));
		}
		
		threadsTerminated = 0;
		precision = 50;
		iterations = 1500;
		magnification = new BigDecimal("1");
		colorScheme = "Rainbow";
		totalPixels = width*height;
		mandelbrotGUI.getProgressBar().setMaximum(totalPixels);
		mandelbrotGUI.getPreviewPanel().setColorScheme(colorScheme);
		
		mandelbrotGUI.getPreviewPanel().redraw(new BigDecimal("0"), new BigDecimal("0"));
		centerImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		centerImageOriginal = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		region = new Region(new BigDecimal("-2"),new BigDecimal("2"),new BigDecimal("2"),new BigDecimal("-2"),precision);
		temporaryFrameRegion = region;
		savedRegions.add(region);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		
		setFocusable(true);
		getFocus();
		
		calculateRegion();
		createThreads();
		updateStatusArea();
	}	
	
	/**
	 * Called whenever threads are needed to draw an image on the panel. It divides up the panel
	 * and allocates rectangles for each calculator to work on. If size of the panel does not divide
	 * evenly, it will an extra thread to pick up any additional regions.
	 */
	
	public void createThreads()
	{
		idle = false;
		first = true;
		pixelsComplete = 0;
		centerImageX = 0;
		centerImageY = 0;
		zoomFactor = 0;
		
		calculators = new ArrayList<Calculator>();
		pannedImages = new ArrayList<PannedImage>();
		
		updater= new Updater(mandelbrotGUI);
		updater.execute();
		
		startTime = System.currentTimeMillis();
		int length = (int) height/threadNumber;
		actualThreadNumber = threadNumber;
		
		boxX1 = 0;
		boxY1 = 0;
		boxX2 = 0;
		boxY2 = 0;
		
		Calculator calc;
		Rectangle rectangleToDraw;
		for(int i=threadNumber-1; i>=0; i--)
		{
			rectangleToDraw = new Rectangle(0,length*i,width,length);
			calc = new Calculator(width,height,rectangleToDraw,region, iterations, julia, juliaX, juliaY, exact, precision, this,0,0, colorScheme);
			calculators.add(calc);
			(new Thread(calc)).start();
		}
		if(length*threadNumber != height)
		{
			rectangleToDraw = new Rectangle(0,length*threadNumber,width, height - length*threadNumber);
			calc = new Calculator(width,height,rectangleToDraw,region, iterations, julia, juliaX, juliaY, exact, precision, this,0,0, colorScheme);
			calculators.add(calc);
			(new Thread(calc)).start();
			actualThreadNumber = 1+threadNumber;
		}
		updateStatusArea();
	}	
	
	/**
	 * Overrides the superclass method. Paints the main image {@code centerImage}
	 * at its proper coordinates. It also paints any pannedImages which are stored in the {@code pannedImages}
	 * array. These are painted relative to the centerImage coordinates.
	 * It is also responsible for painting the rectangle zoom box when the mouse is dragged.
	 */
	@Override
	protected void paintComponent(Graphics g)
	{
		updateStatusArea();
		if(retina)
		{
			Graphics2D g2d = (Graphics2D) g;
			AffineTransform transform = new AffineTransform();
			transform.scale(0.5, 0.5);
			g2d.setTransform(transform);
			g = g2d;
		}
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.drawImage(centerImage, centerImageX, centerImageY, this);
		synchronized(pannedImages)
		{
			for(PannedImage pi : pannedImages)
			{
				g.drawImage(pi.getBufferedImage(), centerImageX+pi.getRelativeX(), centerImageY + pi.getRelativeY(),this);
			}
		}
		
		if(idle)
		{
			/*
			 * Never scale the box.
			 */
			Graphics2D g2d = (Graphics2D) g;
			AffineTransform transform = new AffineTransform();
			g2d.setTransform(transform);
			g = g2d;
			g.drawRect(boxX1, boxY1, boxX2-boxX1, boxY2-boxY1);
		}
	}
	
	/**
	 * Called whenever a calculator is ready to send an image to be drawn. It is drawn at
	 * (x,y) coordinates in the centerImage, the centerImage is then painted onto the screen. It will check to see if all the images have been drawn and if so,
	 * set the program in its idle state. If it receives the image when it is idle, it will treat the image as a panned image. The panned image
	 * is drawn relative to the centerImage with relativeX and relativeY specifying its distance from the top left
	 * corner of the centerImage.
	 * 
	 * @param image - The Image to be drawn.
	 * @param x - Draws the image at this x-coordinate.
	 * @param y - Draws the image at this y-coordinate.
	 * @param relativeX - If it is a panned image, it will be drawn relative to the top left corner of the centerImage.
	 * @param relativeY - If it is a panned image, it will be drawn relative to the top left corner of the centerImage.
	 */
	public synchronized void drawImage(BufferedImage image, int x, int y,int relativeX, int relativeY)
	{
		if(! idle)
		{
			Graphics2D g2d = centerImage.createGraphics();
			g2d.drawImage(image, x, y, this);
			threadsTerminated++;
			repaint();
			if(threadsTerminated == 3*actualThreadNumber)
			{
				threadsTerminated = 0;
				stopTime = System.currentTimeMillis();
				long g = stopTime-startTime;
				mandelbrotGUI.getJTextField().setText("Magnification: x" + String.format("%.0f",magnification) + 
						"     Time: "+ g +
						"     Center: "+ centerX.stripTrailingZeros().toPlainString() + " + " + centerY.stripTrailingZeros().toPlainString() + "i");
				mandelbrotGUI.getJTextField().setCaretPosition(0);
				updater.cancel(false);
				boxX1 = 0;
				boxY1 = 0;
				boxX2 = 0;
				boxY2 = 0;
				temporaryFrameRegion = region;
				getFocus();
				idle = true;
			}
			else
			{
				
				mandelbrotGUI.getJTextField().setText("Processes complete: " + 100*threadsTerminated/(3*actualThreadNumber) + "%");
				mandelbrotGUI.getJTextField().setCaretPosition(0);
			}
		}
		else
		{
			synchronized(pannedImages)
			{
				pannedImages.add(new PannedImage(image, relativeX, relativeY));
			}
			repaint();
		}
	}
	
	/**
	 * Calculates the magnification and center of the region at any given time.
	 */
	public void calculateRegion()
	{
		magnification = (new BigDecimal("4").divide(region.getWidth(),BigDecimal.ROUND_HALF_UP)).setScale(precision, BigDecimal.ROUND_HALF_UP);
		centerX = temporaryFrameRegion.getX1().add(temporaryFrameRegion.getX2()).divide(new BigDecimal("2"), precision, BigDecimal.ROUND_HALF_UP );
		centerY = temporaryFrameRegion.getY1().add(temporaryFrameRegion.getY2()).divide(new BigDecimal("2"), precision, BigDecimal.ROUND_HALF_UP );
	}
	
	/**
	 * Converts the pixel coordinate (x,y) into a Complex number BASED ON THE VALUE OF THE {@code region} FIELD.
	 * In other words, if the region field were superimposed onto the panel, this method maps
	 * the pixel to its corresponding absolute coordinates in the region.
	 * 
	 * @param x - The x-coordinate of the pixel.
	 * @param y - The y-coordinate of the pixel.
	 * @return A complex number in that represents the absolute cartesian coordinates of this pixel.
	 */
	public ComplexBigDecimal toComplex(int x, int y)
	{
		BigDecimal scaledWidth = region.getWidth().divide(new BigDecimal(width), precision, BigDecimal.ROUND_HALF_UP);
		BigDecimal scaledHeight = region.getHeight().divide(new BigDecimal(height), precision, BigDecimal.ROUND_HALF_UP);
		BigDecimal scaledX = scaledWidth.multiply(new BigDecimal(x)).add(region.getX1());
		BigDecimal scaledY = region.getY1().subtract(scaledHeight.multiply(new BigDecimal(y)));
		return new ComplexBigDecimal(scaledX,scaledY,precision);
	}
	
	/**
	 * Converts the nonScaledRectangle of pixels into an absolute Region that represents points on a graph.
	 * 
	 * @param nonScaledRectangle	The rectangle of points to be converted into a region.
	 * @return						The region that corresponds to the rectangle of given points.
	 */
	public Region toComplex(Rectangle nonScaledRectangle)
	{
		Region absoluteRegion;
		BigDecimal x1, y1,x2,y2;
		x1 = toComplex((int) nonScaledRectangle.getX(), (int) nonScaledRectangle.getY()).getRealPart();
		y1 = toComplex((int) nonScaledRectangle.getX(), (int) nonScaledRectangle.getY()).getImaginaryPart();
		x2 = toComplex((int) (nonScaledRectangle.getX()+nonScaledRectangle.getWidth()), (int) (nonScaledRectangle.getY()+nonScaledRectangle.getHeight())).getRealPart();
		y2 = toComplex((int) (nonScaledRectangle.getX()+nonScaledRectangle.getWidth()), (int) (nonScaledRectangle.getY()+nonScaledRectangle.getHeight())).getImaginaryPart();
		absoluteRegion = new Region(x1,y1,x2,y2,precision);
		return absoluteRegion;
	}
	
	/**
	 * Converts the pixel coordinate (x,y) into a Complex number BASED ON THE VALUE OF THE {@code temporaryFrameRegion} FIELD.
	 * In other words, if the temporaryFrameRegion field were superimposed onto the panel, this method maps
	 * the pixel to its corresponding absolute coordinates in the region.
	 * 
	 * @param x - The x-coordinate of the pixel.
	 * @param y - The y-coordinate of the pixel.
	 * @return A complex number in that represents the absolute cartesian coordinates of this pixel.
	 */
	public ComplexBigDecimal toComplexRelative(int x, int y)
	{
		BigDecimal scaledWidth = temporaryFrameRegion.getWidth().divide(new BigDecimal(width), precision, BigDecimal.ROUND_HALF_UP);
		BigDecimal scaledHeight = temporaryFrameRegion.getHeight().divide(new BigDecimal(height), precision, BigDecimal.ROUND_HALF_UP);
		BigDecimal scaledX = scaledWidth.multiply(new BigDecimal(x)).add(temporaryFrameRegion.getX1());
		BigDecimal scaledY = temporaryFrameRegion.getY1().subtract(scaledHeight.multiply(new BigDecimal(y)));
		return new ComplexBigDecimal(scaledX,scaledY,precision);
	}
	
	/**
	 * Called to safely interrupt all the calculator threads mid calculation.
	 * Can be used when a very long calculation prevents the user from interacting with the program.
	 */
	public void interruptThreads()
	{
		if(! idle)
		{
			for(Calculator c: calculators)
			{
				c.interrupt();
			}
			threadsTerminated = 0;
			long stopTime = System.currentTimeMillis();
			pixelsComplete = 0;
			updater.cancel(false);
			long g = stopTime-startTime;
			mandelbrotGUI.getJTextField().setText("Magnification: x" + String.format("%.0f",magnification) + 
					"     Time: "+ g +
					"     Center: "+ centerX.stripTrailingZeros().toPlainString() + " + " + centerY.stripTrailingZeros().toPlainString() + "i");
			mandelbrotGUI.getJTextField().setCaretPosition(0);
			idle = true;
		}
	}
	
	/**
	 * Switches back to the Mandelbrot set.
	 */
	public void toJuliaSet()
	{
		if(idle)
		{
			if(! julia)
			{
				mandelbrotGUI.getMenuItem("Switch to Julia set").setEnabled(false);
				mandelbrotGUI.getMenuItem("Switch to Mandelbrot set").setEnabled(true);
				idle = false;
				asked = false;
				exact = false;
				julia = true;
				savedRegions = new ArrayList<Region>();
				region = new Region(new BigDecimal("-2"),new BigDecimal("2"),new BigDecimal("2"),new BigDecimal("-2"),precision);
				centerX = new BigDecimal("0").setScale(precision,BigDecimal.ROUND_HALF_UP);
				centerY = new BigDecimal("0").setScale(precision,BigDecimal.ROUND_HALF_UP);
				savedRegions.add(region);
				calculateRegion();
				createThreads();
			}
		}
	}
	
	/**
	 * Switches the view to display the Julia set that was initially in on the preview panel.
	 */
	public void toMandelbrotSet()
	{
		if(idle)
		{
			if(julia)
			{
				mandelbrotGUI.getMenuItem("Switch to Julia set").setEnabled(true);
				mandelbrotGUI.getMenuItem("Switch to Mandelbrot set").setEnabled(false);
				idle = false;
				asked = false;
				exact = false;
				julia = false;
				
				region = new Region(new BigDecimal("-2"),new BigDecimal("2"),new BigDecimal("2"),new BigDecimal("-2"),precision);
				centerX = new BigDecimal("0").setScale(precision,BigDecimal.ROUND_HALF_UP);
				centerY = new BigDecimal("0").setScale(precision,BigDecimal.ROUND_HALF_UP);
				savedRegions = new ArrayList<Region>();
				savedRegions.add(region);
				calculateRegion();
				createThreads();
			}
		}
	}
	
	/**
	 * Resets to either the original Mandelbrot set view, or the original Julia set view.
	 */
	public void reset()
	{
		if(idle)
		{
			idle = false;
			asked = false;
			exact = false;
			region = new Region(new BigDecimal("-2"),new BigDecimal("2"),new BigDecimal("2"),new BigDecimal("-2"),precision);
			temporaryFrameRegion = region;
			
			savedRegions = new ArrayList<Region>();
			savedRegions.add(region);
			calculateRegion();
			createThreads();
		}
	}
	
	/**
	 * Redraws the panel exactly where it is.
	 */
	public void rerender()
	{
		if(idle)
		{
			idle = false;
			region = temporaryFrameRegion;
			calculateRegion();
			createThreads();
		}
	}
	
	/**
	 * Displays the the current state of the program
	 * with some useful info.
	 */
	public void displayStatus()
	{
		String s;
		if(julia)
		{
			s = "Julia";
		}
		else
		{
			s = "Mandelbrot";
		}
		JOptionPane.showMessageDialog(mandelbrotGUI.getJFrame(), "ThreadCount: " + threadNumber + "\nSet: " + s + 
				"\nCenter: " + centerX.stripTrailingZeros().toPlainString() + " + " + centerY.stripTrailingZeros().toPlainString() + "i" + 
				"\nMagnification: x" +magnification.stripTrailingZeros().toPlainString() +"\nIterations: " + iterations + "\nPrecision: " + precision +
				"\nArbitrary Precision: " + exact +"\nIdle: " + idle + "\nColor Scheme: " + colorScheme,
				"Status", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Called whenever a field has changed.
	 * Updates the status area of the GUI.
	 */
	public void updateStatusArea()
	{
		String s;
		if(julia)
		{
			s = "Julia";
		}
		else
		{
			s = "Mandelbrot";
		}
		mandelbrotGUI.getStatusArea().setText("ThreadCount: " + threadNumber + "\nSet: " + s +
				"\nMagnification: x" +magnification.stripTrailingZeros().toPlainString() +"\nIterations: " + iterations + "\nPrecision: " + precision +
				"\nArbitrary Precision: " + exact +"\nIdle: " + idle + "\nColor Scheme: " + colorScheme);
	}
	
	/**
	 * Prompts the user to change the number of threads used to draw the image.
	 * The number of threads should ideally divide the height of the panel evenly.
	 * Often times, the process slows down with more than 15 threads.
	 * 
	 */
	public void changeThreadNumber()
	{
		if(idle)
		{
			boolean inquire = true;
			int aHolderNumber = threadNumber;
			String s = "";
			while(inquire)
			{
				s = JOptionPane.showInputDialog(mandelbrotGUI.getJFrame(), 
						"Current threads: " + threadNumber + "\nEnter the desired number of threads (for best performance do not exceed four times the number of computer cores):", 
						"Change Thread Number", JOptionPane.QUESTION_MESSAGE);
				if(s == null)
				{
					return;
				}
				try
				{
					
					aHolderNumber = new Integer(s).intValue();
					if(aHolderNumber <=0)
					{
						JOptionPane.showMessageDialog(mandelbrotGUI.getJFrame(), "That is not a proper integer! Try again.", "Error", JOptionPane.ERROR_MESSAGE);
						inquire = true;
					}
					else
					{
						inquire = false;
					}
				}
				catch(NumberFormatException nfe)
				{
					JOptionPane.showMessageDialog(mandelbrotGUI.getJFrame(), "That is not a readable integer! Try again.", "Error", JOptionPane.ERROR_MESSAGE);
					inquire = true;
				}
			}
			threadNumber = aHolderNumber;
		}
		updateStatusArea();
	}
	
	/**
	 * Prompts the user for a change in the maxIterations of the set.
	 */
	public void changeIterations()
	{
		if(idle)
		{
			boolean inquire = true;
			int aHolderNumber = iterations;
			String s = "";
			while(inquire)
			{
				s = JOptionPane.showInputDialog(mandelbrotGUI.getJFrame(), 
						"Current Iterations: "+ iterations +"\nEnter the desired number of iterations:", 
						"Change Iterations", JOptionPane.QUESTION_MESSAGE);
				if(s == null)
				{
					return;
				}
				try
				{
					
					aHolderNumber = new Integer(s).intValue();
					if(aHolderNumber <=0)
					{
						JOptionPane.showMessageDialog(mandelbrotGUI.getJFrame(), "That is not a proper integer! Try again.", "Error", JOptionPane.ERROR_MESSAGE);
						inquire = true;
					}
					else
					{
						inquire = false;
					}
				}
				catch(NumberFormatException nfe)
				{
					JOptionPane.showMessageDialog(mandelbrotGUI.getJFrame(), "That is not a readable integer! Try again.", "Error", JOptionPane.ERROR_MESSAGE);
					inquire = true;
				}
			}
			iterations = aHolderNumber;
		}
		updateStatusArea();
	}
	
	/**
	 * Prompts the user for a change in precision.
	 * Only has an effect when arbitrary precision is turned on.
	 */
	public void changePrecision()
	{
		if(idle)
		{
			boolean inquire = true;
			int aHolderNumber = precision;
			String s = "";
			while(inquire)
			{
				s = JOptionPane.showInputDialog(mandelbrotGUI.getJFrame(), 
						"Current Precision: " + precision + "\nEnter the desired precision (will only have an effect when Arbitrary Precision is turned on):", 
						"Change Precision", JOptionPane.QUESTION_MESSAGE);
				if(s == null)
				{
					return;
				}
				try
				{
					
					aHolderNumber = new Integer(s).intValue();
					if(aHolderNumber <=0)
					{
						JOptionPane.showMessageDialog(mandelbrotGUI.getJFrame(), "That is not a proper integer! Try again.", "Error", JOptionPane.ERROR_MESSAGE);
						inquire = true;
					}
					else
					{
						inquire = false;
					}
				}
				catch(NumberFormatException nfe)
				{
					JOptionPane.showMessageDialog(mandelbrotGUI.getJFrame(), "That is not a readable integer! Try again.", "Error", JOptionPane.ERROR_MESSAGE);
					inquire = true;
				}
			}
			precision = aHolderNumber;
		}
		updateStatusArea();
	}
	
	/**
	 * Changes the setting to double precision if it wasn't already.
	 */
	public void toArbitraryPrecision()
	{
		if(idle)
		{
			mandelbrotGUI.getMenuItem("Switch to Double Precision").setEnabled(true);
			mandelbrotGUI.getMenuItem("Switch to Arbitrary Precision").setEnabled(false);
			exact = true;
		}
	}
	
	/**
	 * Changes the setting to double precision if it wasn't already.
	 */
	public void toDoublePrecision()
	{
		if(idle)
		{
			mandelbrotGUI.getMenuItem("Switch to Double Precision").setEnabled(false);
			mandelbrotGUI.getMenuItem("Switch to Arbitrary Precision").setEnabled(true);
			exact = false;
		}
		updateStatusArea();
	}
	
	/**
	 * Called by the GUI during a menuItem click.
	 * Draws the previous image visited before the zoom in or zoom out.
	 */
	public void undo()
	{
		if(idle)
		{
			if(savedRegions.size() >= 2)
			{
				region = savedRegions.get(savedRegions.size()-2);
				temporaryFrameRegion = region;
				savedRegions.remove(savedRegions.size()-1);
				calculateRegion();
				createThreads();
			}
			else
			{
				JOptionPane.showMessageDialog(mandelbrotGUI.getJFrame(), "There is no previous region to go back to!","Error",JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Called by GUI during a menuItem click.
	 * Creates an instance the ImageSaver thread and runs it. This must
	 * happen on a separate thread because the event dispatch thread
	 * must be free to repaint the ProgressMonitor while computing the image.
	 */
	public void saveImage()
	{
		new Thread(new ImageSaver()).start();
	}
	
	/**
	 * Returns the gui of this Panel
	 * @return the gui of this panel.
	 */
	public MandelbrotGUI getGUI()
	{
		return mandelbrotGUI;
	}
	
	/**
	 * Called whenever the JPanel needs to receive the focus of the key board.
	 */
	public void getFocus()
	{
		requestFocusInWindow(true);
	}
	
	/**
	 * Called by a calculator to indicate that it has completed calculating another pixel.
	 * This method increments the value of pixelsComplete, which is registered by
	 * the {@code updater} to update the progress bar.
	 */
	@Override
	public void updateProgress()
	{
		pixelsComplete++;
	}
	
	/**
	 * Initiates a zoom into the 40x40 rectangle around the clicked pixel.
	 */
	@Override
	public void mouseClicked(MouseEvent me)
	{
		if(!((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK))
		{
			if(idle)
			{
				if((! exact) &&  (! asked))
				{
					if(magnification.compareTo(new BigDecimal("1000000000000"))==1)
					{
						int n = JOptionPane.showConfirmDialog(mandelbrotGUI.getJFrame(), 
								"Double precision is not accurate enough for calculations at high magnification.\nWould you like to switch to arbitrary position (preformance will decrease)?"
								,"Arbitrary Precision", JOptionPane.YES_NO_CANCEL_OPTION);
						 if(n == JOptionPane.YES_OPTION)
						 {
							 exact = true;
							 asked = true;
							 mandelbrotGUI.getMenuItem("Switch to Double Precision").setEnabled(true);
							 mandelbrotGUI.getMenuItem("Switch to Arbitrary Precision").setEnabled(false);
						 }
						 else if(n == JOptionPane.NO_OPTION)
						 {
							 asked = true;
						 }
						 else
						 {
							 return;
						 }
					}
				}
				idle = false;
				mandelbrotGUI.getJTextField().setCaretPosition(0);
				mandelbrotGUI.getJTextField().setText("Processes complete: 0%");
				
				//sets region to be the actual current region
				region = temporaryFrameRegion;
				
				if(retina)
				{
					region = toComplex(new Rectangle(2*me.getX()-20,2*me.getY()-20, 40,40));
				}
				else
				{
					region = toComplex(new Rectangle(me.getX()-20,me.getY()-20, 40,40));
				}
				
				
				temporaryFrameRegion = region;
				savedRegions.add(region);
				
				
				calculateRegion();
				createThreads();
			}
		}
		else
		{
			if(idle)
			{
				if (! julia)
				{
					if(retina)
					{
						juliaX = toComplexRelative(2*me.getX(),2*me.getY()).getRealPart();
						juliaY = toComplexRelative(2*me.getX(),2*me.getY()).getImaginaryPart();
						mandelbrotGUI.getPreviewPanel().redraw(juliaX, juliaY);
					}
					else
					{
						juliaX = toComplexRelative(me.getX(),me.getY()).getRealPart();
						juliaY = toComplexRelative(me.getX(),me.getY()).getImaginaryPart();
						mandelbrotGUI.getPreviewPanel().redraw(juliaX, juliaY);
					}
					
				}
			}
		}
	}
	
	/**
	 * If the for whatever reason the panel looses focus. When the mouse reenters it,
	 * it will receive the focus allowing for it to listen for key events.
	 */
	@Override
	public void mouseEntered(MouseEvent me)
	{
		getFocus();
	}

	@Override
	public void mouseExited(MouseEvent me)
	{
	}
	
	/**
	 * sets up the top left corner for drawing a zoom box on the panel.
	 */
	@Override
	public void mousePressed(MouseEvent me)
	{
		if(idle)
		{
			boxX1 = me.getX();
			boxY1 = me.getY();
		}
	}

	/**
	 * Closes the zoom rectangle and initiates the zoom.
	 */
	@Override
	public void mouseReleased(MouseEvent me) 
	{
		if(idle)
		{
			boxX2 = Math.max(me.getX()-boxX1, me.getY()-boxY1) + boxX1;
			boxY2 = Math.max(me.getX()-boxX1, me.getY()-boxY1) + boxY1;
			if(boxX2==boxX1)
			{
				return;
			}
			else if(boxX2< boxX1)
			{
				return;
			}
			
			if((! exact) &&  (! asked))
			{
				if(magnification.compareTo(new BigDecimal("1000000000000"))==1)
				{
					int n = JOptionPane.showConfirmDialog(mandelbrotGUI.getJFrame(), 
							"Double precision is not accurate enough for calculations at high magnification.\nWould you like to switch to arbitrary position (preformance will decrease)?"
							,"Arbitrary Precision", JOptionPane.YES_NO_CANCEL_OPTION);
					 if(n == JOptionPane.YES_OPTION)
					 {
						 exact = true;
						 asked = true;
						 mandelbrotGUI.getMenuItem("Switch to Double Precision").setEnabled(true);
						 mandelbrotGUI.getMenuItem("Switch to Arbitrary Precision").setEnabled(false);
					 }
					 else if(n == JOptionPane.NO_OPTION)
					 {
						 asked = true;
					 }
					 else
					 {
						 return;
					 }
				}
			}
			idle = false;
			mandelbrotGUI.getJTextField().setCaretPosition(0);
			mandelbrotGUI.getJTextField().setText("Processes complete: 0%");
			
			//Sets region to actual region
			region = temporaryFrameRegion;
			
			
			if(retina)
			{
				region = toComplex(new Rectangle(2*boxX1, 2*boxY1, 2*(boxX2-boxX1),2*(boxY2-boxY1)));
			}
			else
			{
				region = toComplex(new Rectangle(boxX1, boxY1, boxX2-boxX1,boxY2-boxY1));
			}
			
			
			//Set temporaryRegion to zoomed box
			temporaryFrameRegion = region;
			savedRegions.add(region);
			
			calculateRegion();
			createThreads();
		}
	}
	
	/**
	 * Creates a zoom box which is painted onto the panel.
	 */
	@Override
	public void mouseDragged(MouseEvent me)
	{
		if(idle)
		{
			boxX2 = Math.max(me.getX()-boxX1, me.getY()-boxY1) + boxX1;
			boxY2 = Math.max(me.getX()-boxX1, me.getY()-boxY1) + boxY1;
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent me)
	{
	}
	
	/**
	 * Called when any arrow key is pressed to handle the panning of the image.
	 * It firsts shifts the position of the main or central image by changing the {@code centerImageY}.
	 * It sets the {@code temporaryFrameRegion}. It cannot set the actual {@code region} because that region must remain constant
	 * in order to convert the pixelsRectangles into regions to be calculated.
	 * It updates the center position.
	 * It then creates a calculator to calculate the a section of the {@code temporaryFrameRegion}. When it sends this image back
	 * to this panel, it passes the relativeX and relativeY coordinates. These represent where the image should be drawn
	 * relative to the centerImage which has just been moved to accommodate the pan. The image is drawn on the point
	 * {@code (centerImageX + relativeX, centerImageY + relativeY)}.
	 * 
	 * 
	 */
	@Override
	public void keyPressed(KeyEvent ke)
	{

		if(ke.getKeyCode() == KeyEvent.VK_UP)
		{
			if(idle)
			{
				centerImageY+= 8;
				temporaryFrameRegion = toComplex(new Rectangle(-centerImageX,-centerImageY,width,height));
				centerX = temporaryFrameRegion.getX1().add(temporaryFrameRegion.getX2()).divide(new BigDecimal("2"), precision,BigDecimal.ROUND_HALF_UP);
				centerY = temporaryFrameRegion.getY1().add(temporaryFrameRegion.getY2()).divide(new BigDecimal("2"), precision,BigDecimal.ROUND_HALF_UP);
				long g = stopTime-startTime;
				
				mandelbrotGUI.getJTextField().setText("Magnification: x" + String.format("%.0f",magnification) + 
						"     Time: "+ g +
						"     Center: "+ centerX.stripTrailingZeros().toPlainString() + " + " + centerY.stripTrailingZeros().toPlainString() + "i");
				mandelbrotGUI.getJTextField().setCaretPosition(0);
				new Thread(new Calculator(
				width, height, new Rectangle(0, 0, width,8),temporaryFrameRegion, iterations,
				julia, juliaX, juliaY,exact, precision, this,-centerImageX,-centerImageY, colorScheme)).start();
				
				repaint();
			}
		}
		else if(ke.getKeyCode() == KeyEvent.VK_DOWN)
		{
			if(idle)
			{
				centerImageY+= -8;
				temporaryFrameRegion = toComplex(new Rectangle(-centerImageX,-centerImageY, width,height));
				centerX = temporaryFrameRegion.getX1().add(temporaryFrameRegion.getX2()).divide(new BigDecimal("2"), precision,BigDecimal.ROUND_HALF_UP);
				centerY = temporaryFrameRegion.getY1().add(temporaryFrameRegion.getY2()).divide(new BigDecimal("2"), precision,BigDecimal.ROUND_HALF_UP);
				long g = stopTime-startTime;
				
				mandelbrotGUI.getJTextField().setText("Magnification: x" + String.format("%.0f",magnification) + 
						"     Time: "+ g +
						"     Center: "+ centerX.stripTrailingZeros().toPlainString() + " + " + centerY.stripTrailingZeros().toPlainString() + "i");
				mandelbrotGUI.getJTextField().setCaretPosition(0);
				new Thread(new Calculator(
							width, height, new Rectangle(0, height-8, width,8),temporaryFrameRegion,
							iterations, julia, juliaX, juliaY,exact, precision, this, -centerImageX, -8+height - centerImageY, colorScheme)).start();
				
				repaint();
			}
		}
		else if(ke.getKeyCode() == KeyEvent.VK_LEFT)
		{
			if(idle)
			{
				centerImageX += 8;
				temporaryFrameRegion = toComplex(new Rectangle(-centerImageX,-centerImageY, width,height));
				centerX = temporaryFrameRegion.getX1().add(temporaryFrameRegion.getX2()).divide(new BigDecimal("2"), precision,BigDecimal.ROUND_HALF_UP);
				centerY = temporaryFrameRegion.getY1().add(temporaryFrameRegion.getY2()).divide(new BigDecimal("2"), precision,BigDecimal.ROUND_HALF_UP);
				long g = stopTime-startTime;
				
				mandelbrotGUI.getJTextField().setText("Magnification: x" + String.format("%.0f",magnification) + 
						"     Time: "+ g +
						"     Center: "+ centerX.stripTrailingZeros().toPlainString() + " + " + centerY.stripTrailingZeros().toPlainString() + "i");
				mandelbrotGUI.getJTextField().setCaretPosition(0);
				new Thread(new Calculator(
						width, height, new Rectangle(0, 0, 8,height),temporaryFrameRegion,
						iterations, julia, juliaX, juliaY,exact, precision, this,-centerImageX,-centerImageY, colorScheme)).start();
				
				repaint();
			}
		}
		else if(ke.getKeyCode() == KeyEvent.VK_RIGHT)
		{
			if(idle)
			{
				centerImageX += -8;
				temporaryFrameRegion = toComplex(new Rectangle(-centerImageX,-centerImageY, width,height));
				centerX = temporaryFrameRegion.getX1().add(temporaryFrameRegion.getX2()).divide(new BigDecimal("2"), precision,BigDecimal.ROUND_HALF_UP);
				centerY = temporaryFrameRegion.getY1().add(temporaryFrameRegion.getY2()).divide(new BigDecimal("2"), precision,BigDecimal.ROUND_HALF_UP);
				long g = stopTime-startTime;
				
				mandelbrotGUI.getJTextField().setText("Magnification: x" + String.format("%.0f",magnification) + 
						"     Time: "+ g +
						"     Center: "+ centerX.stripTrailingZeros().toPlainString() + " + " + centerY.stripTrailingZeros().toPlainString() + "i");
				mandelbrotGUI.getJTextField().setCaretPosition(0);
				new Thread(new Calculator(
						width, height, new Rectangle(width-8, 0, 8,height),temporaryFrameRegion,
						iterations, julia, juliaX, juliaY,exact, precision, this, -8+width-centerImageX, -centerImageY, colorScheme)).start();
				
				repaint();
			}
		}
		
	}

	@Override
	public void keyReleased(KeyEvent ke)
	{
	}

	@Override
	public void keyTyped(KeyEvent ke)
	{
	}
	
	/**
	 * Called when the mouse wheel moves or when there is a slide on the track pad.
	 * It will dilate the image to simulate zooming in. It sets a timer. If the timer is not reset
	 * by another call to this method, the timer causes the image to rerender at the new magnification.
	 * 
	 * @param mwe - The MouseWheelEvent used to get information on the wheel listener.
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent mwe)
	{
		if(idle && ! retina)
		{
			if(first)
			{
				viewerPanel.paintAll(centerImageOriginal.createGraphics());
				pannedImages = new ArrayList<PannedImage>();
				centerImageX = 0;
				centerImageY = 0;
				region = temporaryFrameRegion;
				if(retina)
				{
					mouseX = 2*mwe.getX();
					mouseY = 2*mwe.getY();
				}
				else
				{
					mouseX = mwe.getX();
					mouseY = mwe.getY();
				}
				
				timer = new Timer();
			}
			
			first = false;
			zoomFactor+= mwe.getWheelRotation();
			BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = bi.createGraphics();
			double scaleFactor = Math.pow(Math.E, zoomFactor/zoomDivisor);
			AffineTransform at = new AffineTransform();
			
			at.translate(mouseX, mouseY);
			at.scale(scaleFactor,scaleFactor);
			at.translate(-mouseX, -mouseY);
			
			g2d.setTransform(at);
			g2d.drawImage(centerImageOriginal, 0,0, this);
			centerImage=bi;
			
			int x1, y1,x2,y2;
			x1 =(int) (mouseX - mouseX/scaleFactor);
			y1 = (int) (mouseY - mouseY/scaleFactor);
			x2 = (int) (mouseX - mouseX/scaleFactor+ (int) (width/scaleFactor));
			y2 = (int) (mouseY - mouseY/scaleFactor +  (int) (height/scaleFactor));
			
			if((int) (width/scaleFactor) == 0)
			{
				temporaryFrameRegion = toComplex(new Rectangle(x1,y1,1,1));
			}
			else
			{
				temporaryFrameRegion = toComplex(new Rectangle(x1,y1,x2-x1,y2-y1));
			}
			centerX = toComplex((int) ((x1+x2)/2), (int) ((y1+y2)/2)).getRealPart();
			centerY = toComplex((int) ((x1+x2)/2), (int) ((y1+y2)/2)).getImaginaryPart();
			timer.cancel();
			timer = new Timer();
			timer.schedule(new TimedTask(), 100);
			repaint();
			
		}
	}
	
	/**
	 * Called by the GUI when it detects a JMenuItem click on "Change Color...".
	 * It prompts the user for a new color scheme. It converts the current image into
	 * into a new color scheme by using a reversed hash. This avoids recalculating
	 * the image. If the new color scheme uses
	 * a larger hash than the old one (as is the case for Dr. seuss going to any other), imformation is lost,
	 * and the image must be recalculated. 
	 */
	public void changeColor()
	{
		if(idle)
		{
			HashMap<Integer, Color> hm = new Calculator(colorScheme).getHash();
			
			String[] sArray = {"Rainbow", "Gothic Black", "Blue Magenta", "Pink Blue", "Blue Purple", "Green Purple", "Dr. Seuss", "Gray Blue", "Winter Wonderland", "Yellow Orange"};
			String color = (String) JOptionPane.showInputDialog(mandelbrotGUI.getJFrame(), 
					"Current color scheme: " + colorScheme + "\nChoose color scheme:", 
							"Change Color", JOptionPane.QUESTION_MESSAGE, null, sArray, sArray[0]);
			if(color == null || colorScheme.equals(color))
			{
				return;
			}
			else if(colorScheme.equals("Dr. Seuss"))
			{
				colorScheme = color;
				mandelbrotGUI.getPreviewPanel().setColorScheme(colorScheme);
				mandelbrotGUI.getPreviewPanel().redraw(juliaX, juliaY);
				rerender();
				return;
			}
			idle =false;
			colorScheme = color;
			mandelbrotGUI.getPreviewPanel().setColorScheme(colorScheme);
			mandelbrotGUI.getPreviewPanel().redraw(juliaX, juliaY);
			BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = bi.createGraphics();
			viewerPanel.paintAll(g2d);
			
			HashMap<Color, Integer> reverseMap = reverse(hm);
			reverseMap.put(new Color(0,0,0), -1);
			hm = new Calculator(colorScheme).getHash();
			hm.put(-1, new Color(0,0,0));
			if(colorScheme.equals("Dr. Seuss"))
			{
				for(int i = 0; i< width; i++)
				{
					for(int j = 0; j< height; j++)
					{
						g2d.setColor(hm.get(  (reverseMap.get(new Color(bi.getRGB(i, j))))   % 2 ));
						g2d.fillRect(i, j, 1, 1);
					}
				}
			}
			else
			{
				for(int i = 0; i< width; i++)
				{
					for(int j = 0; j< height; j++)
					{
						g2d.setColor(hm.get(  (reverseMap.get(new Color(bi.getRGB(i, j))))));
						g2d.fillRect(i, j, 1, 1);
					}
				}
			}
			
			
			
			
			pannedImages = new ArrayList<PannedImage>();
			region = temporaryFrameRegion;
			centerImageX = 0;
			centerImageY = 0;
			if(retina)
			{
				AffineTransform transform = new AffineTransform();
				transform.scale(2, 2);
				BufferedImage scaledImage = new BufferedImage(bi.getWidth(),bi.getHeight(),BufferedImage.TYPE_INT_RGB);
				Graphics2D scaledG2D = scaledImage.createGraphics();
				scaledG2D.setTransform(transform);
				scaledG2D.drawImage(bi,0,0,this);
				bi = scaledImage;
			}
			centerImage = bi;
			repaint();
			
			idle = true;
		}
	}
	
	/**
	 * Reverses the given hash map to create a new hash map.
	 * The keys of the new hash map are the values of the old one and the new
	 * values are the old keys.
	 * @param map - The HashMap to be reversed.
	 * @return The reversed HashMap.
	 */
	public <K,V> HashMap<V,K> reverse(HashMap<K,V> map)
	{
		HashMap<V,K> rev = new HashMap<V,K>();
		for(Map.Entry<K, V> entry: map.entrySet())
		{
			rev.put(entry.getValue(), entry.getKey());
		}
		return rev;
	}
	
	private class TimedTask extends TimerTask
	{
		public void run()
		{
			region = temporaryFrameRegion;
			savedRegions.add(region);
			rerender();
		}
	}
	
	private class Updater extends SwingWorker<Integer, Void>
	{
		MandelbrotGUI mandelbrotGUI;
		public Updater(MandelbrotGUI mg)
		{
			mandelbrotGUI = mg;
			mandelbrotGUI.getProgressBar().setValue(0);
		}
		@Override
		protected Integer doInBackground()
		{
			while(! isCancelled())
			{
				mandelbrotGUI.getProgressBar().setValue(pixelsComplete);
			}
			return new Integer(0);
		}
		
		@Override
		protected void done()
		{
			mandelbrotGUI.getProgressBar().setValue(totalPixels);
		}	
	}
	
	private class SaveImageUpdater implements Runnable
	{
		ProgressMonitor progressMonitor;
		Calculator calc;
		int max;
		boolean interrupted;
		public SaveImageUpdater(int w, int h, Calculator c)
		{
			calc = c;
			progressMonitor = new ProgressMonitor(mandelbrotGUI.getJFrame(), "Creating Image", "", 0, w*h);
			max = w*h;
			interrupted = false;
		}
		
		@Override
		public void run()
		{
			while(! progressMonitor.isCanceled() && pixelsComplete <= max && ! interrupted)
			{
				progressMonitor.setNote(String.valueOf((long) 100*pixelsComplete/max) + "%");
				progressMonitor.setProgress(pixelsComplete);
			}
			if(progressMonitor.isCanceled())
			{
				calc.interrupt();
			}
			else
			{
				progressMonitor.close();
			}
		}
		
		public void interrupt()
		{
			interrupted = true;
		}
	}
	
	private class ImageSaver implements Runnable
	{
		@Override
		public void run()
		{
			if(idle)
			{
				SaveImageUpdater saveImageUpdater;
				int w = width,h = height, aHolderNumber = width;
				boolean inquire = true;
				String s;
				idle = false;
				while(inquire)
				{
					s = JOptionPane.showInputDialog(mandelbrotGUI.getJFrame(), 
							"Enter the desired image size in terms of pixel width:", 
							"Save Image...", JOptionPane.QUESTION_MESSAGE);
					if(s == null)
					{
						idle = true;
						return;
					}
					try
					{
						
						aHolderNumber = new Integer(s).intValue();
						if(aHolderNumber <=0)
						{
							JOptionPane.showMessageDialog(mandelbrotGUI.getJFrame(), "That is not a proper integer! Try again.", "Error", JOptionPane.ERROR_MESSAGE);
							inquire = true;
						}
						else
						{
							inquire = false;
						}
					}
					catch(NumberFormatException nfe)
					{
						JOptionPane.showMessageDialog(mandelbrotGUI.getJFrame(), "That is not a readable integer! Try again.", "Error", JOptionPane.ERROR_MESSAGE);
						inquire = true;
					}
				}
				w = aHolderNumber;
				h = w;
				
				String[] sArray = {"png", "jpg", "gif"};
				String imageType = (String) JOptionPane.showInputDialog(mandelbrotGUI.getJFrame(), 
						"Choose the type of image:", 
								"Save Image...", JOptionPane.QUESTION_MESSAGE, null, sArray, sArray[0]);
				if(imageType == null)
				{
					idle = true;
					return;
				}
				val = 0;
				try
				{
					SwingUtilities.invokeAndWait(new CreateFileChooser());
				}
				catch(Exception e)
				{
				}
				
				if(val == JFileChooser.APPROVE_OPTION)
				{
					pixelsComplete = 0;
					Calculator calcImage;
					try
					{
						calcImage = new Calculator(w,h, new Rectangle(0,0,w,h),temporaryFrameRegion, iterations, julia, juliaX, juliaY, exact, precision, viewerPanel,0,0, colorScheme);
					}
					catch(OutOfMemoryError ome)
					{
						JOptionPane.showMessageDialog(mandelbrotGUI.getJFrame(), "The file size was too large to be handled.\nYou may have to restart the program. (i.e. they tell me I shouldn't try to catch this exception as it is actually an \"Error\")",
								"FATAL ERROR", JOptionPane.ERROR_MESSAGE);
						idle = true;
						return;
					}
					saveImageUpdater = new SaveImageUpdater(w,h, calcImage);
					new Thread(saveImageUpdater).start();
					BufferedImage saveImage = calcImage.getImage();
					if(saveImage == null)
					{
						JOptionPane.showMessageDialog(mandelbrotGUI.getJFrame(), "Interrupted", "ERROR", JOptionPane.ERROR_MESSAGE);
						idle = true;
						return;
					}
					try
					{
						File file = fileChooser.getSelectedFile();
						
						if(file == null)
						{
							JOptionPane.showMessageDialog(mandelbrotGUI.getJFrame(), "We were unable to save your picture to that location!", "ERROR", JOptionPane.ERROR_MESSAGE);
							idle = true;
							return;
						}
						String fileName = file.getCanonicalPath();
						if(! fileName.endsWith("." + imageType))
						{
							file = new File(fileName + "." + imageType);
						}
						
						if(retina)
						{
							BufferedImage scaledImage = new BufferedImage((int) (0.5*saveImage.getWidth()),(int) (0.5*saveImage.getHeight()),BufferedImage.TYPE_INT_RGB);
							Graphics2D g2d = (Graphics2D) scaledImage.getGraphics();
							AffineTransform transform = new AffineTransform();
							transform.scale(0.5, 0.5);
							g2d.setTransform(transform);
							g2d.drawImage(saveImage, 0, 0, new JPanel());
							saveImage = scaledImage;
						}
						ImageIO.write(saveImage, imageType, file);
					}
					catch(IOException ioe)
					{
					}
				}
				else
				{
					idle = true;
					return;
				}
				JOptionPane.showMessageDialog(mandelbrotGUI.getJFrame(), "Image Saved", "Saved", JOptionPane.INFORMATION_MESSAGE);
				saveImageUpdater.interrupt();
				idle = true;
			}
		}
	}
	
	private class CreateFileChooser implements Runnable
	{
		@Override
		public void run()
		{
			fileChooser = new JFileChooser();
			val = fileChooser.showSaveDialog(mandelbrotGUI.getJFrame());
		}
	}
}
