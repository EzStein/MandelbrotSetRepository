package fx;

import java.math.BigDecimal;

import javafx.application.Platform;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.*;

public class PannedImage implements Runnable
{
	private Region<Integer> pixelRegionSection;
	private Region<BigDecimal> region;
	private Region<Integer> pixelRegion;
	private int iterations, precision;
	private ComplexBigDecimal seed;
	private GraphicsContext gc;
	private boolean jSet;
	private boolean arbPrecision;
	public int relX, relY;
	public WritableImage image;
	private MainGUI gui;
	private Calculator calculator;
	
	public PannedImage(int relX, int relY, Region<Integer> pixelRegionSection, Region<BigDecimal> region, Region<Integer> pixelRegion,
			int iterations, boolean arbPrecision, int precision,  boolean jSet, ComplexBigDecimal seed, GraphicsContext gc, MainGUI gui)
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
		calculator = new Calculator();
	}
	
	@Override
	public void run()
	{
		if(jSet)
		{
			image = calculator.generateJuliaSetRough(seed, pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision);
			render();
			image = calculator.generateJuliaSetMed(seed, pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,image);
			render();
			image = calculator.generateJuliaSetFine(seed, pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,image);
			render();
		}
		else
		{
			image = calculator.generateSetRough(pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision);
			render();
			image = calculator.generateSetMed(pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,image);
			render();
			image = calculator.generateSetFine(pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,image);
			render();
		}
	}
	
	public void render()
	{
		Platform.runLater(() -> {gc.drawImage(image, gui.imageX+relX, gui.imageY+relY);});
	}
}
