package mandelbrotSet;
import java.awt.image.*;
/**
 * 
 * Represents an image to be stored by the viewerPanel in an array of images to be redrawn.
 * These images are small slits of 8 pixels by the length of the viewerPanel.
 * Each slit is created when the user pans from side to side using the arrow keys.
 * relativeX and relativeY are the number of pixels away from the top left corner of the main image that is initially rendered.
 * 
 * @author Ezra Stein
 * @version 1.0
 * @since 2015
 */
public class PannedImage
{
	/**
	 * Holds the image
	 */
	BufferedImage bi;
	
	/**
	 * This panned image should be drawn a certain relative number of pixels away from the main image.
	 * This value holds the number of pixels away from the top left corner of the main image
	 */
	int relativeX, relativeY;
	
	/**
	 * Constructs a panned image from some parameters.
	 * 
	 * @param image - the 8xheight or widthx8 size image to be drawn.
	 * @param relativeX - distance in pixels from top left corner of main image.
	 * @param relativeY - distance in pixels from top left corner of main image.
	 */
	public PannedImage(BufferedImage image, int relativeX, int relativeY)
	{
		bi = image;
		this.relativeX = relativeX;
		this.relativeY = relativeY;
	}
	
	/**
	 * Returns the panned image.
	 * @return the panned image.
	 */
	public BufferedImage getBufferedImage()
	{
		return bi;
	}
	
	/**
	 * Returns the distance in pixels from top left corner of main image.
	 * @return distance in pixels from top left corner of main image.
	 */
	public int getRelativeX()
	{
		return relativeX;
	}
	
	/**
	 * Returns the distance in pixels from top left corner of main image.
	 * @return distance in pixels from top left corner of main image.
	 */
	public int getRelativeY()
	{
		return relativeY;
	}
}
