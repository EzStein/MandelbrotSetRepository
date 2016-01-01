package fx;

import java.math.BigDecimal;


import javafx.application.*;
import javafx.scene.canvas.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;

/**
 * Represents a panned image that will be rendered after a pan.
 * @author Ezra
 * @version 1.0
 * @since 2015
 */
public class PannedImage implements Runnable
{
	private final Region<Integer> pixelRegionSection;
	private final Region<BigDecimal> region;
	private final Region<Integer> pixelRegion;
	private final int iterations, precision;
	private final ComplexBigDecimal seed;
	private final GraphicsContext gc;
	private final boolean jSet;
	private final boolean arbPrecision;
	/**
	 * The relative x position that this image should be drawn from the initial image.
	 */
	public final int relX;
	/**
	 * The relative x position that this image should be drawn from the initial image.
	 */
	public final int relY;
	/**
	 * The image that this panned image renders.
	 */
	public WritableImage image;
	private final MainGUI gui;
	private final Calculator calculator;
	
	/**
	 * Initializes the panned image with parameters.
	 * @param calculator			The calculator object to be used in these calculations.
	 * @param relX					The relative x distance that this should be drawn from the initial image.
	 * @param relY					The relative y distance that this should be drawn from the initial image.
	 * @param pixelRegionSection	The section of pixels that this panned image represents.
	 * @param region				The region of points that maps to pixelRegion.
	 * @param pixelRegion			The pixelRegion of the entire viewer.
	 * @param iterations			The number of iterations to be used in this calculation.
	 * @param arbPrecision			Whether or not to use arbitrary precision.
	 * @param precision				The precision to be used in BigDecimal calculations.
	 * @param jSet					Whether or not to render the julia set or mandelbrot set.
	 * @param seed					Seed to use if rendering julia set.
	 * @param gc					The graphics context of the canvas that this panned image will paint.
	 * @param gui					A reference to the underlying gui.
	 */
	public PannedImage(Calculator calculator,
			int relX,
			int relY,
			Region<Integer> pixelRegionSection,
			Region<BigDecimal> region,
			Region<Integer> pixelRegion,
			int iterations,
			boolean arbPrecision,
			int precision,
			boolean jSet,
			ComplexBigDecimal seed,
			GraphicsContext gc,
			MainGUI gui)
	{
		this.pixelRegionSection = pixelRegionSection;
		this.region = region;
		this.pixelRegion = pixelRegion;
		this.iterations = iterations;
		this.precision = precision;
		this.seed = seed;
		this.gc = gc;
		this.jSet = jSet;
		this.arbPrecision = arbPrecision;
		this.relX = relX;
		this.relY = relY;
		this.gui = gui;
		this.calculator = calculator;
		image = new WritableImage(pixelRegionSection.getWidth().intValue(), pixelRegionSection.getHeight().intValue());
		for(int i = 0; i<image.getWidth(); i++)
		{
			for(int j = 0; j<image.getHeight(); j++)
			{
				image.getPixelWriter().setColor(i, j, Color.WHITE);
			}
		}
		
	}
	
	/**
	 * Calculates different roughness of the set and renders them.
	 */
	@Override
	public void run()
	{
		if(jSet)
		{
			image = calculator.generateJuliaSet(seed, pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,16,0,image);
			render();
			image = calculator.generateJuliaSet(seed, pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,4,16,image);
			render();
			image = calculator.generateJuliaSet(seed, pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,1,4,image);
			render();
		}
		else
		{
			image = calculator.generateSet(pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,16,0,image);
			render();
			image = calculator.generateSet(pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,4,16,image);
			render();
			image = calculator.generateSet(pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,1,4,image);
			render();
		}
	}
	
	/**
	 * Renders this image relative to the initial image after panning.
	 */
	public void render()
	{
		Platform.runLater(() -> {
			gc.drawImage(image, gui.imageX+relX, gui.imageY+relY);
			/*THIS LINE CAUSED A LOT OF ISSUES*/
			//gui.displayImage = gui.viewerCanvas.snapshot(new SnapshotParameters(), null);
		});
	}
}
