package mandelbrotSet;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.math.BigDecimal;

/**
 * The PreviewPanel gives a quick preview of the Julia set corresponding to a given right clicked point on the MSet
 * Subclass of DrawingPanel. Creates multiple Threads (Calculators) which draws multiple parts of the preview simultaneously
 * Activated to redraw every right click.
 * 
 * @author Ezra Stein
 * @version 1.0
 * @since 2015
 */
public class PreviewPanel extends DrawingPanel
{
	/**
	 * Not entirely sure what this does. I put it here to satisfy a warning.
	 * TO BE LOOKED UP LATER.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Holds the width of this panel.
	 */
	int width;
	
	/**
	 * Holds the height of this panel.
	 */
	int height;
	
	/**
	 * Holds the color scheme of this panel. Default is set to "Winter Wonderland".
	 */
	String colorScheme;
	
	/**
	 * Holds the image that will fill the previewPanel.
	 */
	BufferedImage bi;
	
	/**
	 * Holds the current number of threads that are used to draw this image.
	 */
	int threadNumber= 1;
	
	/**
	 * Holds the region (-2,2) to (2,-2) to be drawn on the preview panel.
	 */
	Region region;
	
	/**
	 * Holds the x value for the seed of the julia set.
	 * Initially is zero, but changes for each right click.
	 */
	BigDecimal seedOfJuliaX;
	
	/**
	 * Holds the y value for the seed of the julia set.
	 * Initially is zero, but changes for each right click.
	 */
	BigDecimal seedOfJuliaY;
	
	boolean retina;
	
	/**
	 * Constructs a previewPanel with a certain width and height.
	 * 
	 * @param width - constructs a preview panel with this width.
	 * @param height - constructs a preview panel with this height.
	 * @param mandelbrotGUI - A reference to the GUI that created it.
	 */
	public PreviewPanel(int width, int height, MandelbrotGUI mandelbrotGUI)
	{
		retina = mandelbrotGUI.getRetina();
		if(retina)
		{
			setPreferredSize(new Dimension((int) (0.5*width),(int) (0.5*height)));
		}
		else
		{
			setPreferredSize(new Dimension(width,height));
		}
		
		this.width = width;
		this.height = height;
		bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		colorScheme = "Winter Wonderland";
		region = new Region(new BigDecimal("-2"),new BigDecimal("2"),new BigDecimal("2"),new BigDecimal("-2"), 100);
		seedOfJuliaX = new BigDecimal("0");
		seedOfJuliaY = new BigDecimal("0");
		createThreads();
	}
	
	/**
	 * Creates the several threads (Calculator). Each thread calculates part of the preview panel and and sends it the panel to be drawn.
	 */
	public void createThreads()
	{
		Calculator calculator;
		int length = (int) height/threadNumber;
		for(int i=threadNumber-1; i>=0; i--)
		{
			calculator = new Calculator(width,height, new Rectangle(0,length*i, width, length),region, 200, true, seedOfJuliaX, seedOfJuliaY, false, 50, this,0,0, colorScheme);
			(new Thread(calculator)).start();
		}
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		if(retina)
		{
			Graphics2D g2d = (Graphics2D) g;
			AffineTransform transform = new AffineTransform();
			transform.scale(0.5, 0.5);
			g2d.setTransform(transform);
			g = g2d;
		}
		g.setColor(Color.BLUE);
		g.fillRect(0, 0, width, height);
		g.drawImage(bi,0,0,this);
	}
	
	/**
	 * This method is called every time a Calculator needs to send the preview panel an image.
	 * It feeds it the image which is the drawn onto the panel at points x and y.
	 * 
	 * @param image - The image that is drawn.
	 * @param x - The x coordinate of where the upper left corner of the image is drawn.
	 * @param y - The y coordinate of where the upper left corner of the image is drawn.
	 * @param relativeX - Unused.
	 * @param relativeY - Unused.
	 */
	@Override
	public void drawImage(BufferedImage image, int x, int y,int relativeX, int relativeY)
	{
		Graphics2D g2d = bi.createGraphics();
		g2d.drawImage(image,x,y,this);
		repaint();
	}
	
	/**
	 * Redraws the preview panel with the seedOfJuliaX and Y as seeds.
	 * 
	 * @param seedOfJuliaX - Uses this value as the x seed.
	 * @param seedOfJuliaY - Uses this value as the y seed.
	 */
	public void redraw(BigDecimal seedOfJuliaX, BigDecimal seedOfJuliaY)
	{
		this.seedOfJuliaX = seedOfJuliaX;
		this.seedOfJuliaY = seedOfJuliaY;
		createThreads();
	}
	
	/**
	 * Sets the color scheme of the preview panel.
	 * @param colorScheme - the string color scheme.
	 */
	public void setColorScheme(String colorScheme)
	{
		this.colorScheme = colorScheme;
	}
	
	/**
	 * Here only to satisfy the instantiation of abstract method in superclass
	 */
	@Override
	public void updateProgress()
	{
	}
	
}
