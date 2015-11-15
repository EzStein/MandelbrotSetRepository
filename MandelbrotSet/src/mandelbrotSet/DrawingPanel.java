package mandelbrotSet;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * This class is a superclass to the previewPanel and ViewerPanel
 * It is used to define two abstract methods that the Calculator will use to transfer information to both
 * the ViewerPanel and PreviewPanel
 * 
 * @author Ezra Stein
 * @version 1.0
 * @since 2015
 */
public abstract class DrawingPanel extends JPanel
{
	/**
	 * Don't entirely know what this does. Here to satisfy a warning.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Called by the calculator asking the panel to draw the image at the specified coordinates.
	 * When a panned image is sent to the panel, the relativeX and relativeY fields are used to specify the relative distance
	 * from the top left hand corner of the main or initially drawn image that this image should be drawn.
	 * 
	 * @param image			image to be drawn
	 * @param x				x-coordinate for top left hand corner of this image
	 * @param y				y-coordinate for top left hand corner of this image
	 * @param relativeX		x-distance from top left hand corner of main image. Used for sending a panned image.
	 * @param relativeY		y-distance from top left hand corner of main image. Used for sending a panned image.
	 */
	public abstract void drawImage(BufferedImage image, int x, int y, int relativeX, int relativeY);
	
	/**
	 * Used to tell the panel to update its progress, often for updating a progress bar.
	 */
	public abstract void updateProgress();
}
