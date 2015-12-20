package fx;
import javafx.animation.*;
import javafx.application.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.stage.*;
import javafx.util.Duration;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.transform.*;
import java.math.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import javax.swing.text.NumberFormatter;

import fx.Region;

/**
 * Holds the Main GUI for this program.
 * @author Ezra Stein
 * @version 1.1
 * @since 2015
 */
public class MainGUI extends Application
{
	Thread updater;
	Label textArea;
	Stage window;
	Scene scene;
	BorderPane layout;
	Canvas viewerCanvas, juliaViewer;
	GraphicsContext mainGC, juliaGC;
	ProgressBar progressBar;
	ProgressIndicator progressIndicator;
	Region<BigDecimal> currentRegion;
	Region<Integer> viewerPixelRegion, previewPixelRegion;
	WritableImage currentImage, displayImage;
	Calculator mainCalculator, previewCalculator;
	ComplexBigDecimal juliaSeed;
	ArrayList<PannedImage> pannedImages;
	ArrayList<Region<BigDecimal>> loggedRegions;
	ArrayList<Thread> runningThreads;
	Timeline timeline;
	BigDecimal magnification;
	private final Region<BigDecimal> originalRegion = new Region<BigDecimal>(new BigDecimal("-2"),
			new BigDecimal("2"),
			new BigDecimal("2"),
			new BigDecimal("-2"));
	
	boolean closed, idle, julia, arbitraryPrecision, autoIterations;
	int width, height, previewWidth, previewHeight, iterations, precision, initX, initY, imageX, imageY, threadCount;
	double zoomFactor;
	
	
	/**
	 * The main method that launches this gui.
	 * @param args
	 */
	public static void main(String[] args)
	{
		
		launch(args);
	}
	
	/**
	 * Builds the gui onto the primary stage.
	 * Initially opens a sizeChooser window to choose the initial viewer size.
	 * 
	 * Constructs.
	 */
	@Override
	public void start(Stage window)
	{
		
		/*Opens the size chooser window.*/
		SizeChooser sizeChooser = new SizeChooser();
		Optional<Integer> result = sizeChooser.showAndWait();
		if(result.isPresent())
		{
			width = result.get();
			height = result.get();
		}
		else
		{
			/*Exits the program*/
			return;
		}
		
		
		/*Creates two calculator objects. One for the preview viewer and one for the main viewer.*/
		mainCalculator = new Calculator();
		previewCalculator = new Calculator();
		
		
		/*Initializes variables with the default value.*/
		
		currentImage = new WritableImage(width,height);
		pannedImages = new ArrayList<PannedImage>();
		juliaSeed = new ComplexBigDecimal("0","0",precision);
		loggedRegions = new ArrayList<Region<BigDecimal>>();
		runningThreads = new ArrayList<Thread>();
		timeline = new Timeline(new KeyFrame(Duration.millis(2000),ae->{}));
		updater = new Thread();
		magnification = new BigDecimal("1");
		currentRegion = originalRegion;
		closed = false;
		idle = false;
		julia = false;
		arbitraryPrecision = false;
		autoIterations = true;
		iterations = 500;
		precision = 50;
		initX=0;
		initY=0;
		threadCount = 10;
		imageX=0;
		imageY=0;
		previewWidth = width/3;
		previewHeight = height/3;
		viewerPixelRegion = new Region<Integer>(0,0,width,height);
		previewPixelRegion = new Region<Integer>(0,0,previewWidth,previewHeight);
		
		/*Initializes Basic layout*/
		this.window = window;
		layout = new BorderPane();
		layout.setId("layout");
	
		Group rootGroup = new Group();
		rootGroup.getChildren().add(layout);
		scene = new Scene(rootGroup);
		
		/*Sets up style sheets*/
		scene.getStylesheets().add(this.getClass().getResource("MainStyle.css").toExternalForm());
		
		/*close() method called when window is closed*/
		window.setOnCloseRequest(e->{
			e.consume();
			close();
			});
		
		
		/*Create MenuBar*/
		MenuBar menuBar = new MenuBar();
		menuBar.useSystemMenuBarProperty().set(true);
		rootGroup.getChildren().add(menuBar);
		
		/*Builds File Menu*/
		Menu fileMenu = new Menu("File");
		MenuItem saveMenu = new MenuItem("Save Image...");
		MenuItem aboutMenu = new MenuItem("About");
		fileMenu.getItems().addAll(saveMenu,aboutMenu);
		menuBar.getMenus().add(fileMenu);
		
		/*About Menu Item*/
		aboutMenu.setOnAction(e -> {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("About");
			alert.setHeaderText("About This Program");
			alert.setContentText("FractalApp\nVersion 1.1\n©Ezra Stein\n");
			alert.show();
		});
		
		/*
		 * Save Menu Item:
		 * Opens up the image saver dialog.
		 */
		saveMenu.setOnAction(e->{
				new ImageSaverDialog(this).showSaverDialog();
			});
		
		
		/*Build Edit Menu*/
		Menu editMenu = new Menu("Edit");
		
		ToggleGroup toggleGroup = new ToggleGroup();
		RadioMenuItem mandelbrotSetMenu = new RadioMenuItem("Mandelbrot Set");
		mandelbrotSetMenu.setToggleGroup(toggleGroup);
		mandelbrotSetMenu.setSelected(true);
		
		RadioMenuItem juliaSetMenu = new RadioMenuItem("Julia Set");
		juliaSetMenu.setToggleGroup(toggleGroup);
		
		MenuItem reset = new MenuItem("Reset");
		MenuItem rerender = new MenuItem("Rerender");
		MenuItem undo = new MenuItem("Undo");
		MenuItem interrupt = new MenuItem("Interrupt");
		MenuItem editMenuItem = new MenuItem("Edit...");
		editMenu.getItems().addAll(mandelbrotSetMenu, juliaSetMenu,new SeparatorMenuItem(),
				reset,rerender, undo,interrupt, new SeparatorMenuItem(), editMenuItem);
		menuBar.getMenus().add(editMenu);
		
		/*
		 * Mandelbrot Set Menu; Switches view to Mandelbrot set iff 
		 * you are currently viewing the Julia set.
		 */
		mandelbrotSetMenu.setOnAction(e -> {
			if(julia)
			{
				interrupt();
				loggedRegions = new ArrayList<>();
				currentRegion = originalRegion;
				julia = false;
				drawSet();
			}
		});
		
		/*
		 * Julia Set Menu:
		 * Switches view to Julia Set iff you are currently
		 * viewing the Mandelbrot set. 
		 */
		juliaSetMenu.setOnAction(e -> {
			if(!julia)
			{
				interrupt();
				loggedRegions = new ArrayList<>();
				currentRegion = originalRegion;
				julia = true;
				drawSet();
			}
		});
		
		/*
		 * Reset Menu:
		 * Resets the view to the original region.
		 * Will also change arbitaryPrecision to false.
		 */
		reset.setOnAction(e -> {
			interrupt();
			loggedRegions.add(currentRegion);
			currentRegion = originalRegion;
			arbitraryPrecision = false;
			drawSet();
		});
		
		/*
		 * Rerender Menu:
		 * Redraws the current region. 
		 */
		rerender.setOnAction(e -> {
			interrupt();
			drawSet();
			
		});
		
		/*
		 * Undo menu:
		 * Moves to the last previously logged region;
		 */
		undo.setOnAction(e -> {
			if(loggedRegions.size()>=1)
			{
				interrupt();
				currentRegion = loggedRegions.get(loggedRegions.size()-1);
				loggedRegions.remove(loggedRegions.size()-1);
				drawSet();
			}
			else
			{
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Nothing to Undo");
				alert.setContentText("There are no more previously logged regions");
				alert.show();
			}
		});
		
		/*
		 * Edit Menu Item:
		 * Opens the edit dialog 
		 */
		editMenuItem.setOnAction(e -> {
				new OptionsEditor(this).showEditDialog();
			});
		
		
		/*
		 * Interrupt Menu Item:
		 * Interrupts the render.
		 * Has no effect if idle == true.
		 */
		interrupt.setOnAction(e -> interrupt());
		
		
		/*Create Progress Bar*/
		progressBar = new ProgressBar();
		progressIndicator = new ProgressIndicator();
		progressBar.setPrefWidth(width+previewWidth-40);
		HBox hbox = new HBox(10);
		hbox.getChildren().addAll(progressBar, progressIndicator);
		layout.setTop(hbox);
		
		/*Create main canvas explorer*/
		viewerCanvas = new Canvas(width,height);
		mainGC = viewerCanvas.getGraphicsContext2D();
		layout.setCenter(viewerCanvas);
		
		
		/*
		 * Gives it focus so that whenever the mouse is on top of the canvas,
		 * it will be able to receive mouse events.
		 */
		viewerCanvas.setOnMouseEntered(e ->{
			viewerCanvas.requestFocus();
		});
		
		/*
		 * Mouse Pressed Event:
		 * Records the initial position of the mouse to be used for rendering a zoom box under the mouseDragged listener.
		 */
		viewerCanvas.setOnMousePressed(e->{
			if(e.getButton() == MouseButton.PRIMARY)
			{
				int x = (int)e.getX(), y = (int)e.getY();
				initX = x;
				initY = y;
				Platform.runLater(() -> displayImage = viewerCanvas.snapshot(new SnapshotParameters(), null));
			}
		});
		
		/*
		 * Mouse Dragged Event:
		 * Renders a square box on the screen.
		 */
		viewerCanvas.setOnMouseDragged(e ->{
			interrupt();
			if(e.getButton() == MouseButton.PRIMARY)
			{				
				int x = (int)e.getX(), y = (int)e.getY();
				int max = Math.max(Math.abs(initX-x), Math.abs(initY-y));
				if(x<initX && y>initY)
				{
					Platform.runLater(() ->{
						mainGC.drawImage(displayImage, 0, 0);
						mainGC.setStroke(Color.WHITE);
						mainGC.strokeRect(initX-max, initY, max, max);
					
					});
				}
				else if(x < initX && y< initY)
				{
					Platform.runLater(() ->{
						mainGC.drawImage(displayImage, 0, 0);
						mainGC.setStroke(Color.WHITE);
						mainGC.strokeRect(initX-max, initY-max, max, max);
					
					});
				}
				else if(x>initX && y>initY)
				{
					Platform.runLater(() ->{
						mainGC.drawImage(displayImage, 0, 0);
						mainGC.setStroke(Color.WHITE);
						mainGC.strokeRect(initX, initY, max, max);
					
					});
				}
				else if(x>initX && y<initY)
				{
					Platform.runLater(() ->{
						mainGC.drawImage(displayImage, 0, 0);
						mainGC.setStroke(Color.WHITE);
						mainGC.strokeRect(initX, initY-max, max, max);
					
					});
				}
			}
		});
		
		zoomFactor = 0;
		/*
		 * Scroll Event:
		 * Controls zooming in and out.
		 */
		viewerCanvas.setOnScroll(e ->{
			interrupt();
			zoomFactor = zoomFactor - e.getDeltaY();
			int x = (int) e.getX();
			int y = (int) e.getY();
			double scaleFactor = Math.pow(Math.E, zoomFactor/1000);
			double scaleFactor2 = Math.pow(Math.E, -zoomFactor/1000);
			Region<BigDecimal> temp = currentRegion.scale(scaleFactor2, scaleFactor2,
					Calculator.pixelToPointX(x, currentRegion, viewerPixelRegion, precision),
					Calculator.pixelToPointY(y, currentRegion, viewerPixelRegion, precision));
			Affine transform = new Affine();
			transform.appendScale(scaleFactor, scaleFactor,x,y);
			mainGC.setFill(Color.WHITE);
			mainGC.fillRect(0, 0, width, height);
			mainGC.setTransform(transform);
			mainGC.drawImage(currentImage, 0, 0);
			mainGC.setTransform(new Affine());
			timeline.stop();
			timeline = new Timeline(new KeyFrame(Duration.millis(1000),ae->{
				loggedRegions.add(currentRegion);
				currentRegion = temp;
				zoomFactor = 0;
				drawSet();
			}));
			timeline.play();
		});
		
		
		/*
		 * Mouse Released Event:
		 * Uses initX and initY as well as the current mouse position to
		 * zoom into a new Region of the set.
		 * If a right click occurred then it will update the previewViewer with a julia set image
		 * whose seed is the position of the mouse.
		 */
		viewerCanvas.setOnMouseReleased(e ->{
			interrupt();
			int x = (int)e.getX(), y = (int)e.getY();
			if(e.getButton() == MouseButton.PRIMARY)
			{
				int max = Math.max(Math.abs(initX-x), Math.abs(initY-y));
				loggedRegions.add(currentRegion);
				if(initX==x&&initY==y)
				{
					int length = (int) width/50;
					currentRegion = Calculator.toBigDecimalRegion(new Region<Integer>(initX-length,initY-length,initX+length,initY+length),
							currentRegion, viewerPixelRegion, precision);
				}
				else if(x<initX&&y<initY)
				{
					currentRegion = Calculator.toBigDecimalRegion(new Region<Integer>(initX-max,initY-max,initX,initY),
							currentRegion, viewerPixelRegion, precision);
				}
				else if(x<initX&&y>initY)
				{
					currentRegion = Calculator.toBigDecimalRegion(new Region<Integer>(initX-max,initY,initX,initY+max),
							currentRegion, viewerPixelRegion, precision);
				}
				else if(x>initX&&y<initY)
				{
					currentRegion = Calculator.toBigDecimalRegion(new Region<Integer>(initX,initY-max,initX+max,initY),
							currentRegion, viewerPixelRegion, precision);
				}
				else if(x>initX&&y>initY)
				{
					currentRegion = Calculator.toBigDecimalRegion(new Region<Integer>(initX,initY,initX+max,initY+max),
							currentRegion, viewerPixelRegion, precision);
				}
				
				drawSet();
				
			}
			else
			{
				if(!julia)
				{
					juliaSeed = Calculator.toComplexBigDecimal(x, y, currentRegion, viewerPixelRegion, precision);
					updateJuliaSetViewer();
				}
			}
			
		});
		
		/*
		 * KeyPressed Event:
		 * Pans the image to the right.
		 * ImageX and imageY refer to the position of the original image that has been panned.
		 * It will then update the current region and calculate create a panned image object for the void that was left
		 * when the image was panned.
		 * The panned image object will render its sliver on its own time and then draw it relative to imageX and imageY.
		 */
		viewerCanvas.setOnKeyPressed(e ->{
			interrupt();
			int change = 10;
			if(e.getCode() == KeyCode.UP)
			{
				imageY+=change;
				currentRegion = Calculator.toBigDecimalRegion(new Region<Integer>(0,-change,width,height-change), currentRegion, viewerPixelRegion, precision);
				PannedImage pi = new PannedImage(mainCalculator, -imageX,-imageY, new Region<Integer>(0,0,width,change),currentRegion, viewerPixelRegion, iterations,
						arbitraryPrecision, precision, julia,juliaSeed, mainGC,this);
				new Thread(pi).start();
				pannedImages.add(pi);
			}
			else if(e.getCode() == KeyCode.DOWN)
			{
				imageY-=change;
				currentRegion = Calculator.toBigDecimalRegion(new Region<Integer>(0,change,width,height+change), currentRegion, viewerPixelRegion, precision);
				PannedImage pi = new PannedImage(mainCalculator,-imageX,height-imageY-change, new Region<Integer>(0,height-change,width,height),currentRegion, viewerPixelRegion, iterations,
						arbitraryPrecision, precision, julia,juliaSeed, mainGC,this);
				new Thread(pi).start();
				pannedImages.add(pi);
			}
			else if(e.getCode() == KeyCode.LEFT)
			{
				imageX+=change;
				currentRegion = Calculator.toBigDecimalRegion(new Region<Integer>(-change,0,width-change,height), currentRegion, viewerPixelRegion, precision);
				PannedImage pi = new PannedImage(mainCalculator,-imageX,-imageY, new Region<Integer>(0,0,change,height),currentRegion, viewerPixelRegion, iterations,
						arbitraryPrecision, precision, julia,juliaSeed, mainGC,this);
				new Thread(pi).start();
				pannedImages.add(pi);
			}
			else if(e.getCode() == KeyCode.RIGHT)
			{
				imageX-=change;
				currentRegion = Calculator.toBigDecimalRegion(new Region<Integer>(change,0, width+change,height), currentRegion, viewerPixelRegion, precision);
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
			updateTextArea();
		});
		
		scene.widthProperty().addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue)
			{
				//interrupt();
				if(idle)
				{
					
					System.out.println(oldValue + " " + newValue);
				
					double changeInWidth = newValue.doubleValue()-oldValue.doubleValue();
					width = (int) Math.min(scene.getHeight()+100, scene.getWidth()-width/3);
					height = width;
					previewWidth = width/3;
					previewHeight = height/3;
					updateAfterResize();
					
				}
				
			}
		});
		
		scene.heightProperty().addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue)
			{
				//interrupt();
				if(idle)
				{
					System.out.println(oldValue + " " + newValue);
					double changeInHeight = newValue.doubleValue()-oldValue.doubleValue();
					height = (int) Math.min(scene.getHeight(), scene.getWidth()-width/3);
					width = height;
					previewWidth = width/3;
					previewHeight = height/3;
					updateAfterResize();
				}
				
			}
		});
		
		
		/*layout.setOnKeyTyped(e->{
			if(e.getCode() == KeyCode.ESCAPE)
			{
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setContentText("Are you sure you want to quit?");
				Optional<ButtonType> response = alert.showAndWait();
				if(response.isPresent())
				{
					interrupt();
					close();
				}
				
			}
			
		});*/
		
		/*Preview Viewer*/
		VBox vbox = new VBox();
		juliaViewer = new Canvas(previewWidth,previewHeight);
		juliaGC = juliaViewer.getGraphicsContext2D();
		vbox.getChildren().add(juliaViewer);
		
		/*Create Text Area*/
		textArea = new Label("Magnification: " + magnification.toString() + "x\n" + 
				"Iterations: " + iterations + "\n"
				+ "Precision: " + precision + "\n"
				+ "Julia Set: " + julia + "\n"
				+ "Center: " + currentRegion.getCenterX().stripTrailingZeros().toPlainString() + " + " + currentRegion.getCenterY().stripTrailingZeros().toPlainString() + "i" + "\n"
				+ "Threads: " + threadCount + "\n"
				+ "Color: " + mainCalculator.getColorFunction().toString() + "\n"
				+ "Arbitrary Precision: " + arbitraryPrecision + "\n");
		vbox.getChildren().add(textArea);
		layout.setRight(vbox);
		
		
		BorderPane.setAlignment(viewerCanvas, Pos.TOP_LEFT);
		BorderPane.setAlignment(juliaViewer, Pos.TOP_LEFT);
		BorderPane.setAlignment(textArea, Pos.TOP_LEFT);
		
		/*Shows the window*/
		window.setScene(scene);
		window.show();
		
		/*Initializes the canvases*/
		updateJuliaSetViewer();
		drawSet();
	}
	
	public void updateAfterResize()
	{
		juliaViewer.setHeight(previewHeight);
		juliaViewer.setWidth(previewWidth);
		viewerCanvas.setHeight(height);
		viewerCanvas.setWidth(width);
		layout.setMaxHeight(height);
		layout.setMaxWidth(width+200);
		Platform.runLater(()->{
			mainGC.drawImage(currentImage, 0, 0,width,height);
			juliaGC.drawImage(displayImage, 0, 0, previewWidth,previewHeight);
		});
		
		viewerPixelRegion = new Region<Integer>(0,0,width,height);
		previewPixelRegion = new Region<Integer>(0,0,previewWidth,previewHeight);
	}
	
	/**
	 * Interrupts the calculation causing all threads to halt and return immediately whatever pixels
	 * they have calculated which are immediately rendered. Blocks until the updater thread has terminated thus indicating the
	 * program has successfully become idle.
	 * Has no effect if the program is already idle.
	 */
	public void interrupt()
	{
		if(!idle)
		{
			mainCalculator.setInterrupt(true);
			
			updater.interrupt();
			try {
				updater.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			idle = true;
		}
	}
	
	/**
	 * Called when the user needs to safely close the program.
	 * First it interrupts the threads, then it closes;
	 */
	public void close()
	{
		interrupt();
		closed = true;
		window.close();
	}
	
	public int calcAutoIterations(BigDecimal mag)
	{
		return (int) (300+2000*Math.log10(magnification.longValue()));
	}
	
	/**
	 * Draws the set given the current state of the program.
	 * Creates threadCount number of threads and assigns them to horizontal sections of the canvas.
	 * If threadCount does not divide evenly into the Canvas height, it will make one additional thread to cover the
	 * last pixels.
	 */
	public void drawSet()
	{
		idle = false;
		magnification = originalRegion.getWidth().divide(currentRegion.getWidth(), precision, BigDecimal.ROUND_HALF_UP);
		if(autoIterations)
		{
			iterations = calcAutoIterations(magnification);
		}
		
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
		updateTextArea();
	}
	
	/**
	 * Updates the text in the text Area.
	 */
	public void updateTextArea()
	{
		textArea.setText("Magnification: " + format(magnification.setScale(1, BigDecimal.ROUND_HALF_UP))+ "x\n" + 
				"Iterations: " + iterations + "\n"
				+ "Precision: " + precision + "\n"
				+ "Julia Set: " + julia + "\n"
				+ "Center: " + currentRegion.getCenterX().stripTrailingZeros().toPlainString() + " + " + currentRegion.getCenterY().stripTrailingZeros().toPlainString() + "i" + "\n"
				+ "Threads: " + threadCount + "\n"
				+ "Color: " + mainCalculator.getColorFunction().toString() + "\n"
				+ "Arbitrary Precision: " + arbitraryPrecision + "\n");
	}
	
	/**
	 * Returns the bigDecimal as a string in scientific notation
	 * @param number
	 * @return
	 */
	public String format(BigDecimal number)
	{
		NumberFormat formatter = new DecimalFormat("0.0E0");
		formatter.setRoundingMode(RoundingMode.HALF_UP);
		formatter.setMaximumFractionDigits(number.scale());
		return formatter.format(number);
	}
	
	/**
	 * Called when the preview Julia set viewer needs to be updated (on a right mouse click).
	 * Will generate the necessary threads as well as any extras to cover uncolored pixels.
	 */
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
	
	
	
	/**
	 * Updates the progress bar by continuously checking the number of pixels calculated by the main calculator
	 * This thread breaks from the loop when either all the pixels have been calculated,
	 * or it is interrupted by the interrupt method.
	 * Upon breaking from the loop, the thread will wait for each generator thread to terminate
	 * and then it will clear all the running Threads and reset the interrupt status of the mainCalculator.
	 * @author Ezra Stein
	 * @version 1.1
	 * @since 2015
	 */
	public class Updater implements Runnable
	{

		@Override
		public void run()
		{
			while(!idle && !closed)
			{
				try {
					Thread.sleep(10);
				} catch (InterruptedException e)
				{
					break;
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
			Platform.runLater(() -> currentImage = viewerCanvas.snapshot(new SnapshotParameters(), null));
		}
	}
	
	/**
	 * Used to construct an image and draw to a canvas of either the julia set or the Mandelbrot Set.
	 * @author Ezra
	 * @version 1.1
	 * @since 2015
	 */
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
		
		/**
		 * Constructs a generator object used to generate fractal images and render them onto the canvas.
		 * @param pixelRegionSection	The Section of the canvas that this generator is responsible for drawing. Given in pixels.
		 * @param region				The real-numbered region on the complex plane coordinate that will be mapped
		 * 								To the ENTIRE canvas and not just to the section being calculated by the above region.
		 * @param pixelRegion			The region of pixels that that {@code region} is mapped to. This is most often the
		 * 								the region in pixels corresponding to the canvas ie (0,0,width,height);
		 * @param iterations			The iterations to used when calculating this region. More iterations means higher accuracy.
		 * @param arbPrecision			True if the generator is to use arbitrary precision BigDecimals to calculated the set.
		 * @param precision				The precision each bigDecimal uses in its calculations. Has no effect if arbPrecision is false.
		 * @param jSet					True if the generator will calculate a julia set. False if it will calculate the mandelbrot set.
		 * @param seed					The seed used to calculate the Julia set. Has no effect if jSet is false.
		 * @param gc					The graphicsContext which this generator will draw to. Either the mainViewer or the preivewViewer.
		 * @param calculator			The calculator used to calculate each image.
		 */
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
		
		/**
		 * Renders this image onto the canvas whose graphicsContext is assigned to this thread.
		 * @param im	The image to be Rendered.
		 */
		public void drawImageToCanvas(WritableImage im)
		{
			Platform.runLater(new Runnable(){
				
				@Override
				public void run() {
					gc.drawImage(im,pixelRegionSection.x1, pixelRegionSection.y1);
				}
			});
		}
		
		/**
		 * Generates a rough then medium then fine image, each time rendering it to the Canvas.
		 * Afterwards, it takes a snapshot of the main canvas and sets it to currentImage.
		 */
		@Override
		public void run() {
			if(jSet)
			{
				WritableImage image = new WritableImage(pixelRegionSection.getWidth().intValue(), pixelRegionSection.getHeight().intValue());
				image = calculator.generateJuliaSet(seed, pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision, 16, 0, image);
				drawImageToCanvas(image);
				
				image = calculator.generateJuliaSet(seed, pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,4,16,image);
				drawImageToCanvas(image);
				
				image = calculator.generateJuliaSet(seed, pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,1,4,image);
				drawImageToCanvas(image);
				
				
			}
			else
			{
				WritableImage image = new WritableImage(pixelRegionSection.getWidth().intValue(), pixelRegionSection.getHeight().intValue());
				image = calculator.generateSet(pixelRegionSection, region, pixelRegion,
						iterations, arbPrecision, precision,16,0,image);
				drawImageToCanvas(image);
				
				image = calculator.generateSet(pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,4,16,image);
				drawImageToCanvas(image);
				
				image = calculator.generateSet(pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,1, 4, image);
				drawImageToCanvas(image);
				
				
			}
		}
	}
}
