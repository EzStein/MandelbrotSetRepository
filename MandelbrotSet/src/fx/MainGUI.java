package fx;
import javafx.application.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.stage.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;

import java.io.File;
import java.io.IOException;
import java.math.*;
import java.util.*;
import fx.Region;

public class MainGUI extends Application
{
	Thread updater;
	Label textArea;
	Stage window;
	Scene scene;
	BorderPane layout;
	Canvas viewerCanvas;
	GraphicsContext mainGC, juliaGC;
	ProgressBar progressBar;
	ProgressIndicator progressIndicator;
	Region<BigDecimal> currentRegion;
	Region<Integer> viewerPixelRegion;
	Region<Integer> previewPixelRegion;
	WritableImage currentImage, displayImage;
	int width=617,height=617;
	int previewWidth, previewHeight;
	boolean closed = false;
	boolean idle = true;
	boolean julia = false;
	boolean arbitraryPrecision = false;
	int iterations = 500;
	int precision = 50;
	int initX=0, initY=0;
	int threadCount = 10;
	int imageX=0, imageY=0;
	ComplexBigDecimal juliaSeed;
	ArrayList<PannedImage> pannedImages;
	ArrayList<Region<BigDecimal>> loggedRegions = new ArrayList<>();
	ArrayList<Thread> runningThreads = new ArrayList<Thread>();
	private final Region<BigDecimal> originalRegion = new Region<BigDecimal>(new BigDecimal("-2"),
			new BigDecimal("2"),
			new BigDecimal("2"),
			new BigDecimal("-2"));
	Calculator mainCalculator, previewCalculator;
	
	public static void main(String[] args)
	{
		
		launch(args);
	}
	
	@Override
	public void start(Stage window)
	{
		
		TextInputDialog inputDialog = new TextInputDialog();
		inputDialog.setContentText("Enter in the preferred size of the fractal viewer: ");
		Optional<String> result = inputDialog.showAndWait();
		if(result.isPresent())
		{
			try
			{
				int size = Integer.parseInt(result.get());
				width = size;
				height = size;
			}
			catch(NumberFormatException nfe)
			{
				System.out.println("Wrong Number");
				return;
			}
		}
		else
		{
			return;
		}
		mainCalculator = new Calculator();
		previewCalculator = new Calculator();
		currentImage = new WritableImage(width,height);
		pannedImages = new ArrayList<PannedImage>();
		juliaSeed = new ComplexBigDecimal("0","0",precision);
		currentRegion = originalRegion;
		this.window = window;
		layout = new BorderPane();
		scene = new Scene(layout);
		window.setOnCloseRequest(e->{
			e.consume();
			close();
			});
		scene.getStylesheets().add(this.getClass().getResource("MainStyle.css").toExternalForm());
		
		/*Create Menus*/
		MenuBar menuBar = new MenuBar();
		menuBar.useSystemMenuBarProperty().set(true);
		
		/*File*/
		Menu fileMenu = new Menu("File");
		MenuItem saveMenu = new MenuItem("Save Image...");
		MenuItem aboutMenu = new MenuItem("About");
		aboutMenu.setOnAction(e -> {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("About");
			alert.setContentText("ABOUT THIS PROGRAM");
			alert.show();
		});
		saveMenu.setOnAction(e->{
			Platform.runLater(()->{new ImageSaverDialog(this).showSaverDialog();});
			});
		fileMenu.getItems().addAll(saveMenu,aboutMenu);
		menuBar.getMenus().add(fileMenu);
		
		
		/*Edit*/
		Menu editMenu = new Menu("Edit");
		ToggleGroup toggleGroup = new ToggleGroup();
		RadioMenuItem mandelbrotSetMenu = new RadioMenuItem("Mandelbrot Set");
		mandelbrotSetMenu.setSelected(true);
		
		RadioMenuItem juliaSetMenu = new RadioMenuItem("Julia Set");
		juliaSetMenu.setToggleGroup(toggleGroup);
		
		menuBar.getMenus().add(editMenu);
		mandelbrotSetMenu.setOnAction(e -> {
			interrupt();
			loggedRegions = new ArrayList<>();
			currentRegion = originalRegion;
			mandelbrotSetMenu.setSelected(true);
			juliaSetMenu.setSelected(false);
			julia = false;
			drawSet();
		});
		juliaSetMenu.setOnAction(e -> {
			interrupt();
			loggedRegions = new ArrayList<>();
			currentRegion = originalRegion;
			mandelbrotSetMenu.setSelected(false);
			juliaSetMenu.setSelected(true);
			julia = true;
			drawSet();
			
		});
		MenuItem reset = new MenuItem("Reset");
		reset.setOnAction(e -> {
			interrupt();
			loggedRegions.add(currentRegion);
			currentRegion = originalRegion;
			arbitraryPrecision = false;
			drawSet();
		});
		
		MenuItem rerender = new MenuItem("Rerender");
		rerender.setOnAction(e -> {
			interrupt();
			drawSet();
			
		});
		
		
		MenuItem undo = new MenuItem("undo");
		undo.setOnAction(e -> {
			if(idle)
			{
				if(loggedRegions.size()>=1)
				{
					currentRegion = loggedRegions.get(loggedRegions.size()-1);
					loggedRegions.remove(loggedRegions.size()-1);
					drawSet();
				}
				else
				{
					
				}
			}
		});
		
		
		MenuItem editMenuItem = new MenuItem("Edit...");
		
		editMenuItem.setOnAction(e -> {
			Platform.runLater(() -> {
				new OptionsEditor(this).showEditDialog();
			});
			});
		
		MenuItem interrupt = new MenuItem("interrupt");
		
		interrupt.setOnAction(e -> interrupt());
		
		editMenu.getItems().addAll(mandelbrotSetMenu, juliaSetMenu,new SeparatorMenuItem(),reset,rerender, undo,interrupt, new SeparatorMenuItem(), editMenuItem);
		
		/*Info*/
		layout.setBottom(menuBar);
		
		/*Create Progress Bar*/
		progressBar = new ProgressBar();
		progressIndicator = new ProgressIndicator();
		progressBar.setPrefWidth(width+200);
		HBox hbox = new HBox(10);
		hbox.getChildren().addAll(progressBar, progressIndicator);
		layout.setTop(hbox);
		
		/*Create main canvas explorer*/
		viewerCanvas = new Canvas(width,height);
		width = (int) viewerCanvas.getWidth();
		height = (int) viewerCanvas.getHeight();
		mainGC = viewerCanvas.getGraphicsContext2D();
		layout.setCenter(viewerCanvas);
		
		viewerCanvas.setOnMouseEntered(e ->{
			viewerCanvas.requestFocus();
		});
		
		viewerCanvas.setOnMousePressed(e->{
			if(e.getButton() == MouseButton.PRIMARY)
			{
				int x = (int)e.getX(), y = (int)e.getY();
				initX = x;
				initY = y;
				Platform.runLater(() -> displayImage = viewerCanvas.snapshot(new SnapshotParameters(), null));
			}
			
		});
		viewerCanvas.setOnMouseDragged(e ->{
			interrupt();
			if(e.getButton() == MouseButton.PRIMARY)
			{
				int x = (int)e.getX(), y = (int)e.getY();
				int max = Math.max(Math.abs(initX-x), Math.abs(initY-y));
				Platform.runLater(() ->{
					mainGC.drawImage(displayImage, 0, 0);
					mainGC.setStroke(Color.WHITE);
					mainGC.strokeRect(initX, initY, max, max);
				});
			}
		
		});
		
		viewerCanvas.setOnMouseReleased(e ->{
			interrupt();
			int x = (int)e.getX(), y = (int)e.getY();
			if(e.getButton() == MouseButton.PRIMARY)
			{
				if(initX==x&&initY==y)
				{
					int length = (int) width/50;
					loggedRegions.add(currentRegion);
					currentRegion = mainCalculator.toBigDecimalRegion(new Region<Integer>(initX-length,initY-length,initX+length,initY+length), currentRegion, viewerPixelRegion, precision);
					drawSet();
				}
				else
				{
					int max = Math.max(Math.abs(initX-x), Math.abs(initY-y));
					loggedRegions.add(currentRegion);
					currentRegion = mainCalculator.toBigDecimalRegion(new Region<Integer>(initX,initY,initX+max,initY+max), currentRegion, viewerPixelRegion, precision);
					drawSet();
				}
			}
			else
			{
				if(!julia)
				{
					juliaSeed = mainCalculator.toComplexBigDecimal(x, y, currentRegion, viewerPixelRegion, precision);
					updateJuliaSetViewer();
				}
			}
			
		});
		
		viewerCanvas.setOnKeyPressed(e ->{
			interrupt();
			int change = 10;
			if(e.getCode() == KeyCode.UP)
			{
				imageY+=change;
				currentRegion = mainCalculator.toBigDecimalRegion(new Region<Integer>(0,-change,width,height-change), currentRegion, viewerPixelRegion, precision);
				PannedImage pi = new PannedImage(mainCalculator, -imageX,-imageY, new Region<Integer>(0,0,width,change),currentRegion, viewerPixelRegion, iterations,
						arbitraryPrecision, precision, julia,juliaSeed, mainGC,this);
				new Thread(pi).start();
				pannedImages.add(pi);
			}
			else if(e.getCode() == KeyCode.DOWN)
			{
				imageY-=change;
				currentRegion = mainCalculator.toBigDecimalRegion(new Region<Integer>(0,change,width,height+change), currentRegion, viewerPixelRegion, precision);
				PannedImage pi = new PannedImage(mainCalculator,-imageX,height-imageY-change, new Region<Integer>(0,height-change,width,height),currentRegion, viewerPixelRegion, iterations,
						arbitraryPrecision, precision, julia,juliaSeed, mainGC,this);
				new Thread(pi).start();
				pannedImages.add(pi);
			}
			else if(e.getCode() == KeyCode.LEFT)
			{
				imageX+=change;
				currentRegion = mainCalculator.toBigDecimalRegion(new Region<Integer>(-change,0,width-change,height), currentRegion, viewerPixelRegion, precision);
				PannedImage pi = new PannedImage(mainCalculator,-imageX,-imageY, new Region<Integer>(0,0,change,height),currentRegion, viewerPixelRegion, iterations,
						arbitraryPrecision, precision, julia,juliaSeed, mainGC,this);
				new Thread(pi).start();
				pannedImages.add(pi);
			}
			else if(e.getCode() == KeyCode.RIGHT)
			{
				imageX-=change;
				currentRegion = mainCalculator.toBigDecimalRegion(new Region<Integer>(change,0, width+change,height), currentRegion, viewerPixelRegion, precision);
				PannedImage pi = new PannedImage(mainCalculator,-imageX+width-change,-imageY, new Region<Integer>(width-change,0,width,height),currentRegion, viewerPixelRegion, iterations,
						arbitraryPrecision, precision, julia,juliaSeed, mainGC,this);
				new Thread(pi).start();
				pannedImages.add(pi);
			}
			
			Platform.runLater(() ->{
				
				mainGC.drawImage(currentImage, imageX, imageY);
				for(PannedImage p: pannedImages)
				{
					mainGC.drawImage(p.image, imageX+p.relX, imageY + p.relY);
				}
			});
			textArea.setText("Iterations: " + iterations + "\n"
				+ "Precision: " + precision + "\n"
				+ "Julia Set: " + julia + "\n"
				+ "Center: " + currentRegion.getCenterX().stripTrailingZeros().toPlainString() + " + " + currentRegion.getCenterY().stripTrailingZeros().toPlainString() + "i" + "\n"
				+ "Threads: " + threadCount + "\n"
				+ "Color: " + mainCalculator.getColorFunction().toString() + "\n"
				+ "Arbitrary Precision: " + arbitraryPrecision + "\n");
		});
		
		/*Create right views*/
		VBox vbox = new VBox();
		Canvas juliaViewer = new Canvas(width/3,height/3);
		previewWidth = (int) juliaViewer.getWidth();
		previewHeight = (int)juliaViewer.getHeight();
		juliaGC = juliaViewer.getGraphicsContext2D();
		vbox.getChildren().add(juliaViewer);
		textArea = new Label("Iterations: " + iterations + "\n"
				+ "Precision: " + precision + "\n"
				+ "Julia Set: " + julia + "\n"
				+ "Center: " + currentRegion.getCenterX().stripTrailingZeros().toPlainString() + " + " + currentRegion.getCenterY().stripTrailingZeros().toPlainString() + "i" + "\n"
				+ "Threads: " + threadCount + "\n"
				+ "Color: " + mainCalculator.getColorFunction().toString() + "\n"
				+ "Arbitrary Precision: " + arbitraryPrecision + "\n");
		vbox.getChildren().add(textArea);
		layout.setRight(vbox);
		viewerPixelRegion = new Region<Integer>(0,0,width,height);
		previewPixelRegion = new Region<Integer>(0,0,previewWidth,previewHeight);
		
		window.setScene(scene);
		window.show();
		
		updateJuliaSetViewer();
		drawSet();
	}
	
	public void interrupt()
	{
		if(!idle)
		{
			mainCalculator.setInterrupt(true);
			idle = true;
			try {
				updater.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void drawSet()
	{
		idle = false;
		pannedImages = new ArrayList<PannedImage>();
		imageX = 0;
		imageY = 0;
		mainCalculator.setPixelsCalculated(0);
		updater = (new Thread(new Updater()));
		updater.start();
		int h = (int) height/threadCount;
		for(int i = 0; i<threadCount; i++)
		{
			Region<Integer> pixelRegionSection = new Region<Integer>(0,i*h,width,i*h+h);
			
			Thread  g = new Thread(new Generator(pixelRegionSection, currentRegion, viewerPixelRegion, iterations,arbitraryPrecision, precision,julia,juliaSeed,mainGC, mainCalculator));
			runningThreads.add(g);
			g.start();
		}
		
		if(h*threadCount < height)
		{
			Region<Integer> pixelRegionSection = new Region<Integer>(0,h*threadCount,width,height);
			Thread  g = new Thread(new Generator(pixelRegionSection, currentRegion, viewerPixelRegion, iterations,arbitraryPrecision, precision,julia,juliaSeed,mainGC, mainCalculator));
			runningThreads.add(g);
			g.start();
		}
		
		
		textArea.setText("Iterations: " + iterations + "\n"
				+ "Precision: " + precision + "\n"
				+ "Julia Set: " + julia + "\n"
				+ "Center: " + currentRegion.getCenterX().stripTrailingZeros().toPlainString() + " + " + currentRegion.getCenterY().stripTrailingZeros().toPlainString() + "i" + "\n"
				+ "Threads: " + threadCount + "\n"
				+ "Color: " + mainCalculator.getColorFunction().toString() + "\n"
				+ "Arbitrary Precision: " + arbitraryPrecision + "\n");
	}
	
	public void close()
	{
		interrupt();
		closed = true;
		window.close();
	}
	
	public class Updater implements Runnable
	{

		@Override
		public void run()
		{
			while(!idle && !closed)
			{
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Platform.runLater(new Runnable(){
					public void run()
					{
						progressBar.setProgress(mainCalculator.getPixelsCalculated()/(viewerCanvas.getWidth()*viewerCanvas.getHeight()));
						progressIndicator.setProgress(mainCalculator.getPixelsCalculated()/(viewerCanvas.getWidth()*viewerCanvas.getHeight()));
					}
				});
				if(mainCalculator.getPixelsCalculated() >= viewerCanvas.getWidth()*viewerCanvas.getHeight())
				{
					idle = true;
				}
			}
			for(Thread r: runningThreads)
			{
				try {
					r.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Platform.runLater(()->{
				progressBar.setProgress(1);
				progressIndicator.setProgress(1);
				});
			
			runningThreads = new ArrayList<Thread>();
			mainCalculator.setInterrupt(false);
		}
	}
	
	public void updateJuliaSetViewer()
	{
		int h = (int) previewHeight/threadCount;
		for(int i = 0; i<threadCount; i++)
		{
			Region<Integer> pixelRegionSection = new Region<Integer>(0,i*h,previewWidth,i*h+h);
			new Thread(new Generator(pixelRegionSection,
					originalRegion, previewPixelRegion, iterations,arbitraryPrecision, precision, true, juliaSeed, juliaGC, previewCalculator)).start();
		}
		
		
		if(h*threadCount < height)
		{
			Region<Integer> pixelRegionSection = new Region<Integer>(0,h*threadCount,previewWidth,previewHeight);
			new Thread(new Generator(pixelRegionSection,
					originalRegion, previewPixelRegion, iterations,arbitraryPrecision, precision, true, juliaSeed, juliaGC, previewCalculator)).start();
		}
	}
	
	public class Generator implements Runnable
	{
		private Region<Integer> pixelRegionSection;
		private Region<BigDecimal> region;
		private Region<Integer> pixelRegion;
		private int iterations, precision;
		private ComplexBigDecimal seed;
		private GraphicsContext gc;
		private boolean jSet;
		private boolean arbPrecision;
		private Calculator calculator;
		
		public Generator(Region<Integer> pixelRegionSection, Region<BigDecimal> region, Region<Integer> pixelRegion,
								int iterations, boolean arbPrecision, int precision,  boolean jSet, ComplexBigDecimal seed, GraphicsContext gc, Calculator calculator)
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
			this.calculator = calculator;
		}
		
		@Override
		public void run() {
			if(jSet)
			{
				WritableImage image = calculator.generateJuliaSetRough(seed, pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision);
				final WritableImage im = image;
				Platform.runLater(new Runnable(){
	
					@Override
					public void run() {
						gc.drawImage(im,pixelRegionSection.x1, pixelRegionSection.y1);
					}
				});
				image = calculator.generateJuliaSetMed(seed, pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,image);
				final WritableImage im2 = image;
				Platform.runLater(new Runnable(){
					@Override
					public void run() {
						gc.drawImage(im2,pixelRegionSection.x1, pixelRegionSection.y1);
					}
				});
				image = calculator.generateJuliaSetFine(seed, pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,image);
				final WritableImage im3 = image;
				Platform.runLater(new Runnable(){
	
					@Override
					public void run() {
						gc.drawImage(im3,pixelRegionSection.x1, pixelRegionSection.y1);
					}
				});
				if(gc.getCanvas().equals(viewerCanvas))
				Platform.runLater(() -> currentImage = viewerCanvas.snapshot(new SnapshotParameters(), null));
			}
			else
			{
				WritableImage image = calculator.generateSetRough(pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision);
				final WritableImage im = image;
				Platform.runLater(new Runnable(){
					@Override
					public void run() {
						gc.drawImage(im,pixelRegionSection.x1, pixelRegionSection.y1);
					}
				});
				image = calculator.generateSetMed(pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,image);
				final WritableImage im2 = image;
				Platform.runLater(new Runnable(){
					@Override
					public void run() {
						gc.drawImage(im2,pixelRegionSection.x1, pixelRegionSection.y1);
					}
				});
				image = calculator.generateSetFine(pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,image);
				final WritableImage im3 = image;
				Platform.runLater(new Runnable(){
					@Override
					public void run() {
						gc.drawImage(im3,pixelRegionSection.x1, pixelRegionSection.y1);
					}
				});
				
				Platform.runLater(() -> currentImage = viewerCanvas.snapshot(new SnapshotParameters(), null));
			}
		}
	}

}
