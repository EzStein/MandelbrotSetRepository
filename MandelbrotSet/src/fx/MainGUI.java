package fx;
import javafx.animation.*;
import javafx.animation.Animation.*;
import javafx.application.*;
import javafx.beans.value.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.stage.*;
import javafx.util.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.transform.*;
import java.math.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

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
	TextArea textArea;
	Stage window;
	Canvas viewerCanvas, juliaViewer, orbitCanvas;
	GraphicsContext mainGC, juliaGC, orbitGC;
	ProgressBar progressBar;
	ProgressIndicator progressIndicator;
	Region<BigDecimal> currentRegion;
	Region<Integer> viewerPixelRegion, previewPixelRegion;
	WritableImage currentImage,previewViewerImage, displayImage,  actualImage;
	Calculator mainCalculator, previewCalculator;
	ComplexBigDecimal juliaSeed;
	ArrayList<PannedImage> pannedImages;
	ArrayList<Region<BigDecimal>> loggedRegions;
	ArrayList<Thread> runningThreads;
	Timeline timeline;
	BigDecimal magnification;
	Object lock = new Object();
	ThreadQueue threadQueue;
	private final Region<BigDecimal> originalRegion = new Region<BigDecimal>(new BigDecimal("-2"),
			new BigDecimal("2"),
			new BigDecimal("2"),
			new BigDecimal("-2"));
	Thread orbitThread;
	boolean skip = true;
	boolean closed, idle, julia, arbitraryPrecision, autoIterations,resizable, waitForImage;
	int width, height, previewWidth, previewHeight, iterations, precision, initX, initY, imageX, imageY, threadCount;
	int scrollX, scrollY;
	double zoomFactor;
	
	
	/**
	 * The main method that launches this GUI.
	 * @param args		Passed directly to the launch method of application.
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
		//showStartDialog();
		width = 400;
		height = 400;
		initializeVariables();
		
		/*Sets the root*/
		this.window = window;
		Group root = new Group();
		Scene scene = new Scene(root);
		scene.getStylesheets().add(this.getClass().getResource("MainStyle.css").toExternalForm());
		window.setScene(scene);
		
		/*Fills the root entirely with a border pane layout*/
		BorderPane layout = new BorderPane();
		layout.setId("layout");
		root.getChildren().add(layout);
		VBox vbox = new VBox(10);
		
		layout.setBottom(buildMenus());
		layout.setTop(buildProgressBar());
		layout.setCenter(buildViewerCanvas());
		BorderPane.setAlignment(viewerCanvas, Pos.TOP_LEFT);
		
		vbox.getChildren().add(buildPreviewCanvas());
		vbox.getChildren().add(buildTextArea());
		vbox.getChildren().add(buildOrbitCanvas());
		layout.setRight(vbox);
		BorderPane.setAlignment(vbox, Pos.TOP_LEFT);
		
		/*Add Listeners*/
		addKeyPanListener();
		addMouseListeners();
		addScrollListener();
		addResizeListeners();
		addWindowListener();
		
		window.show();
		
		/*Initializes the program by drawing the mandelbrot set*/
		updateJuliaSetViewer();
		drawSet();
	}
	
	private void addWindowListener()
	{
		/*close() method called when window is closed*/
		window.setOnCloseRequest(e->{
			e.consume();
			close();
			});
	}
	
	private void showStartDialog()
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
			System.exit(0);
		}
	}
	
	private void initializeVariables()
	{
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
		orbitThread = new Thread();
		magnification = new BigDecimal("1");
		threadQueue = new ThreadQueue();
		new Thread(threadQueue).start();
		currentRegion = originalRegion;
		closed = false;
		idle = false;
		julia = false;
		arbitraryPrecision = false;
		autoIterations = true;
		resizable = false;
		waitForImage = false;
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
	}
 
	private TextArea buildTextArea()
	{
		/*Create Text Area*/
		textArea = new TextArea("");
		updateTextArea();
		textArea.setEditable(false);
		textArea.setFocusTraversable(false);
		return textArea;	
	}
	
	private MenuBar buildMenus()
	{
		/*Create MenuBar*/
		MenuBar menuBar = new MenuBar();
		//menuBar.useSystemMenuBarProperty().set(true);
		
		/*Builds File Menu*/
		Menu fileMenu = new Menu("File");
		fileMenu.getItems().addAll(buildSaveMenuItem(),buildAboutMenuItem());
		
		
		
		RadioMenuItem mset = buildMandelbrotSetMenuItem();
		RadioMenuItem jset = buildJuliaSetMenuItem();
		ToggleGroup toggleGroup = new ToggleGroup();
		mset.setToggleGroup(toggleGroup);
		jset.setToggleGroup(toggleGroup);
		
		/*Build Edit Menu*/
		Menu editMenu = new Menu("Edit");
		editMenu.getItems().addAll(mset,jset,
				new SeparatorMenuItem(),
				buildResetMenuItem(),buildRerenderMenuItem(), buildUndoMenuItem(),buildInterruptMenuItem(),
				new SeparatorMenuItem(),
				buildEditMenuItem());
		
		menuBar.getMenus().add(fileMenu);
		menuBar.getMenus().add(editMenu);
		
		return menuBar;
	}
	
	private RadioMenuItem buildMandelbrotSetMenuItem()
	{
		RadioMenuItem mandelbrotSetMenu = new RadioMenuItem("Mandelbrot Set");
		mandelbrotSetMenu.setSelected(true);
		/*
		 * Mandelbrot Set Menu; Switches view to Mandelbrot set iff 
		 * you are currently viewing the Julia set.
		 */
		mandelbrotSetMenu.setOnAction(e -> {
			if(julia)
			{
				threadQueue.callLater(() -> {
					interrupt();
					orbitThread.interrupt();
					try {
						orbitThread.join();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					loggedRegions = new ArrayList<>();
					currentRegion = originalRegion;
					julia = false;
					drawSet();
					
					return false;
				});
				
			}
		});
		return mandelbrotSetMenu;
	}
	
	private RadioMenuItem buildJuliaSetMenuItem()
	{
		RadioMenuItem juliaSetMenu = new RadioMenuItem("Julia Set");
		/*
		 * Julia Set Menu:
		 * Switches view to Julia Set iff you are currently
		 * viewing the Mandelbrot set. 
		 */
		juliaSetMenu.setOnAction(e -> {
			if(!julia)
			{
				threadQueue.callLater(() -> {
					interrupt();
					orbitThread.interrupt();
					try {
						orbitThread.join();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					loggedRegions = new ArrayList<>();
					currentRegion = originalRegion;
					julia = true;
					drawSet();
					
					return false;
				});
			}
		});
		return juliaSetMenu;
	}
	
	private MenuItem buildResetMenuItem()
	{
		MenuItem reset = new MenuItem("Reset");
		/*
		 * Reset Menu:
		 * Resets the view to the original region.
		 * Will also change arbitaryPrecision to false.
		 */
		reset.setOnAction(e -> {
			threadQueue.callLater(() -> {
			
				interrupt();
				loggedRegions.add(currentRegion);
				currentRegion = originalRegion;
				arbitraryPrecision = false;
				drawSet();
				
				return false;
			});
		});
		return reset;
	}
	
	private MenuItem buildRerenderMenuItem()
	{
		MenuItem rerender = new MenuItem("Rerender");
		/*
		 * Rerender Menu:
		 * Redraws the current region. 
		 */
		rerender.setOnAction(e -> {
			threadQueue.callLater(() -> {
				interrupt();
				drawSet();
				return false;
			});
		});
		return rerender;
	}
	
	private MenuItem buildUndoMenuItem()
	{
		MenuItem undo = new MenuItem("Undo");
		/*
		 * Undo menu:
		 * Moves to the last previously logged region;
		 */
		undo.setOnAction(e -> {
			if(loggedRegions.size()>=1)
			{
				threadQueue.callLater(() -> {
					
				
					interrupt();
					currentRegion = loggedRegions.get(loggedRegions.size()-1);
					loggedRegions.remove(loggedRegions.size()-1);
					drawSet();
					
					return false;
				});
			}
			else
			{
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Nothing to Undo");
				alert.setContentText("There are no more previously logged regions");
				alert.show();
			}
		});
		
		return undo;
	}
	
	private MenuItem buildInterruptMenuItem()
	{
		MenuItem interrupt = new MenuItem("Interrupt");
		/*
		 * Interrupt Menu Item:
		 * Interrupts the render.
		 * Has no effect if idle == true.
		 */
		interrupt.setOnAction(e -> {
			threadQueue.callLater(() -> {
				interrupt();
				
				return false;
			});
		});
		return interrupt;
	}
	
	private MenuItem buildEditMenuItem()
	{
		MenuItem edit = new MenuItem("Edit...");
		/*
		 * Edit Menu Item:
		 * Opens the edit dialog 
		 */
		edit.setOnAction(e -> {
				new OptionsEditor(this).showEditDialog();
			});
		return edit;
	}

	/**
	 * Save Menu Item:
	 * Opens up the image saver dialog.
	 * @return Returns the created menuItem
	 */
	private MenuItem buildSaveMenuItem()
	{
		MenuItem saveMenu = new MenuItem("Save Image...");
		
		saveMenu.setOnAction(e->{
				new ImageSaverDialog(this).showSaverDialog();
			});
		return saveMenu;
	}
	
	/**
	 * Builds an about menu item which will open an alert describing the program.
	 * @return the build menu item.
	 */
	private MenuItem buildAboutMenuItem()
	{
		MenuItem aboutMenu = new MenuItem("About");
		aboutMenu.setOnAction(e -> {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("About");
			alert.setHeaderText("About This Program");
			alert.setContentText("FractalApp\nVersion 1.1\n©Ezra Stein\n");
			alert.show();
		});
		return aboutMenu;
	}
	
	private Canvas buildViewerCanvas()
	{
		/*Create main canvas explorer*/
		viewerCanvas = new Canvas(width,height);
		mainGC = viewerCanvas.getGraphicsContext2D();
		
		return viewerCanvas;
	}
	
	private Canvas buildPreviewCanvas()
	{
		/*Preview Viewer*/
		juliaViewer = new Canvas(previewWidth,previewHeight);
		juliaGC = juliaViewer.getGraphicsContext2D();
		return juliaViewer;
		
	}
	
	private HBox buildProgressBar()
	{
		/*Create Progress Bar*/
		progressBar = new ProgressBar();
		progressIndicator = new ProgressIndicator();
		progressBar.setPrefWidth(width+previewWidth-40);
		HBox hbox = new HBox(10);
		hbox.getChildren().addAll(progressBar, progressIndicator);
		return hbox;
	}
	
	private Canvas buildOrbitCanvas()
	{
		orbitCanvas = new Canvas();
		orbitCanvas.setWidth(previewWidth);
		orbitCanvas.setHeight(previewHeight);
		orbitGC = orbitCanvas.getGraphicsContext2D();
		orbitGC.setFill(Color.WHITE);
		orbitGC.fillRect(0, 0, width, height);
		return orbitCanvas;
	}
	
	private void addResizeListeners()
	{
		Scene scene = window.getScene();
		scene.widthProperty().addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue)
			{
				threadQueue.callLater(() -> {
					
					if(resizable)
					{
						interrupt();
						width = (int) Math.min(scene.getHeight()-50, scene.getWidth()-width/3);
						height = width;
						previewWidth = width/3;
						previewHeight = height/3;
						updateAfterResize();
					}
					return false;
				});
			}
		});
		
		scene.heightProperty().addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue)
			{
				threadQueue.callLater(() -> {
					if(resizable)
					{
						interrupt();
						height = (int) Math.min(scene.getHeight()-50, scene.getWidth()-width/3);
						width = height;
						previewWidth = width/3;
						previewHeight = height/3;
						updateAfterResize();
					}
					return false;
				});
			}
		});
	}
	
	/**
	 * KeyPressed Event:
	 * Pans the image to the right.
	 * ImageX and imageY refer to the position of the original image that has been panned.
	 * It will then update the current region and calculate create a panned image object for the void that was left
	 * when the image was panned.
	 * The panned image object will render its sliver on its own time and then draw it relative to imageX and imageY.
	 */
	private void addKeyPanListener()
	{
		
		viewerCanvas.setOnKeyPressed(e ->{
			
			if(threadQueue.getQueue().size()>=2)
			{
				return;
			}
			
			
			threadQueue.callLater(() -> {
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
				
				try {
					FXUtilities.runAndWait(() ->{
						for(PannedImage p: pannedImages)
						{
							mainGC.drawImage(p.image, imageX+p.relX, imageY + p.relY);
						}
					});
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
						
				/*for(PannedImage p: pannedImages)
				{
					Platform.runLater(() ->{
						mainGC.drawImage(p.image, imageX+p.relX, imageY + p.relY);
					});
				}*/
				
				
				
					Platform.runLater(() ->{
						mainGC.drawImage(currentImage, imageX, imageY);
						displayImage = viewerCanvas.snapshot(new SnapshotParameters(), null);
						actualImage = displayImage;
					});
				
					updateTextArea();
				return false;
			});
			
		});
	}
	
	/**
	 * Scroll Event:
	 * Controls zooming in and out.
	 */
	private void addScrollListener()
	{
		zoomFactor = 0;
		
		
		viewerCanvas.setOnScroll(e ->{
			
			threadQueue.callLater(() -> {
				
				interrupt();
				
				if(timeline.getStatus()==Status.STOPPED)
				{
					scrollX = (int) e.getX();
					scrollY = (int) e.getY();
				}
				
				zoomFactor = zoomFactor - e.getDeltaY();
				double scaleFactor = Math.pow(Math.E, zoomFactor/1000);
				double scaleFactor2 = Math.pow(Math.E, -zoomFactor/1000);
				Region<BigDecimal> temp = currentRegion.scale(scaleFactor2, scaleFactor2,
						Calculator.pixelToPointX(scrollX, currentRegion, viewerPixelRegion, precision),
						Calculator.pixelToPointY(scrollY, currentRegion, viewerPixelRegion, precision));
				
				
				
				
				
				
				Affine transform = new Affine();
				transform.appendScale(scaleFactor, scaleFactor,scrollX,scrollY);
				mainGC.setFill(Color.WHITE);
				mainGC.fillRect(0, 0, width, height);
				mainGC.setTransform(transform);
				mainGC.drawImage(actualImage, 0, 0);
				mainGC.setTransform(new Affine());
				
				
				/*Calculator calc = new Calculator(mainCalculator.getColorFunction());
				WritableImage im;
				Region<Integer> pixelRegionSection = new Region<Integer>(0,0, width, 20);
				
				if(julia)
				{
					im = calc.generateJuliaSet(juliaSeed, pixelRegionSection, temp, viewerPixelRegion, iterations, arbitraryPrecision, precision);
				}
				else
				{
					im = calc.generateSet(pixelRegionSection, temp, viewerPixelRegion, iterations, arbitraryPrecision, precision);
				}
				mainGC.drawImage(im, 0, 0);
				Platform.runLater(()->{
					actualImage = viewerCanvas.snapshot(new SnapshotParameters(), null);
				});*/
				
				
				
				
				
				timeline.stop();
				timeline = new Timeline(new KeyFrame(Duration.millis(500),ae->{
					loggedRegions.add(currentRegion);
					currentRegion = temp;
					zoomFactor = 0;
					drawSet();
				}));
				timeline.play();
				
				return false;
			});
		});
	}
	
	private void addMouseListeners()
	{
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
			threadQueue.callLater(() -> {
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
				return false;
			});
		});
		
		/*
		 * Mouse Released Event:
		 * Uses initX and initY as well as the current mouse position to
		 * zoom into a new Region of the set.
		 * If a right click occurred then it will update the previewViewer with a julia set image
		 * whose seed is the position of the mouse.
		 */
		viewerCanvas.setOnMouseReleased(e ->{
			
			threadQueue.callLater(() -> {
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
					orbitThread.interrupt();
					try {
						orbitThread.join();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					orbitThread = new Thread(new OrbitThread(Calculator.toComplexBigDecimal(x, y, currentRegion, viewerPixelRegion, precision)));
					orbitThread.start();
					if(!julia)
					{
						juliaSeed = Calculator.toComplexBigDecimal(x, y, currentRegion, viewerPixelRegion, precision);
						updateJuliaSetViewer();
					}
				}
				return false;
			});
			
		});
	}
	
	public void updateAfterResize()
	{
		Platform.runLater(()->{
			progressBar.setPrefWidth(width+previewWidth-40);
			juliaViewer.setHeight(previewHeight);
			juliaViewer.setWidth(previewWidth);
			viewerCanvas.setHeight(height);
			viewerCanvas.setWidth(width);
			textArea.setPrefWidth(previewWidth);
			mainGC.drawImage(displayImage, 0, 0,width,height);
			juliaGC.drawImage(previewViewerImage, 0, 0, previewWidth,previewHeight);
			actualImage = viewerCanvas.snapshot(new SnapshotParameters(), null);
			currentImage = viewerCanvas.snapshot(new SnapshotParameters(), null);
		});
		pannedImages = new ArrayList<PannedImage>();
		imageX = 0;
		imageY = 0;
		viewerPixelRegion = new Region<Integer>(0,0,width,height);
		previewPixelRegion = new Region<Integer>(0,0,previewWidth,previewHeight);
	}
	
	/**
	 * Interrupts the calculation causing all threads to halt and return immediately whatever pixels
	 * they have calculated which are immediately rendered. Blocks until the updater thread has terminated thus indicating the
	 * program has successfully become idle.
	 * Has no effect if the program is already idle.
	 * 
	 * SHOULD NEVER BE CALLED FROM THE JAVAFX APPLICATION THREAD
	 */
	public void interrupt()
	{
		if(Platform.isFxApplicationThread())
		{
			(new Exception("CALLED FROM FX APPLICATION THREAD")).printStackTrace();
			return;
		}
		
		if(!idle)
		{
			previewCalculator.setInterrupt(true);
			mainCalculator.setInterrupt(true);
			updater.interrupt();
			try
			{
				updater.join();
			}
			catch (InterruptedException e)
			{
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
		threadQueue.callLater(()->{
			interrupt();
			closed = true;
			orbitThread.interrupt();
			//threadQueue.terminate();
			Platform.runLater(()->{
				window.close();
			});
			
			return true;
			
		});
		
	}
	
	/**
	 * Calculates the iterations that should be used based on the magnification.
	 * Only used when auto-iterations is turned on.
	 * @param mag		The magnification of the viewer
	 * @return			The number of iterations to use.
	 */
	public int calcAutoIterations(BigDecimal mag)
	{
		int returnValue = (int) (300+2000*Math.log10(mag.longValue()));
		if(returnValue<=100)
		{
			returnValue = 100;
		}
		return returnValue;
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
		//mainCalculator.setPixelsCalculated(0);
		mainCalculator.resetPixelsCalculated();
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
		Platform.runLater(()->{
			
			textArea.setText("Magnification: " + format(magnification.setScale(1, BigDecimal.ROUND_HALF_UP))+ "x\n" + 
					"Iterations: " + iterations + "\n"
					+ "Precision: " + precision + "\n"
					+ "Julia Set: " + julia + "\n"
					+ "Center: " + currentRegion.getCenterX().setScale(6, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString() + " + "
					+ currentRegion.getCenterY().setScale(6, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString() + "i" + "\n"
					+ "Threads: " + threadCount + "\n"
					+ "Color: " + mainCalculator.getColorFunction().toString() + "\n"
					+ "Arbitrary Precision: " + arbitraryPrecision + "\n");
		});
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
		previewCalculator.setInterrupt(false);
		int h = (int) (previewHeight/threadCount);
		for(int i = 0; i<threadCount; i++)
		{
			Region<Integer> pixelRegionSection = new Region<Integer>(0,i*h,previewWidth,i*h+h);
			new Thread(new Generator(pixelRegionSection,
					originalRegion, previewPixelRegion, iterations,arbitraryPrecision, precision, true, juliaSeed, juliaGC, previewCalculator)).start();
		}
		
		if(h*threadCount < previewHeight)
		{
			Region<Integer> pixelRegionSection = new Region<Integer>(0,h*threadCount,previewWidth,previewHeight);
			new Thread(new Generator(pixelRegionSection,
					originalRegion, previewPixelRegion, iterations,arbitraryPrecision, precision, true, juliaSeed, juliaGC, previewCalculator)).start();
		}
	}
	
	public class OrbitThread implements Runnable
	{
		private ComplexBigDecimal seed;
		private int x,y, oldX, oldY;
		public OrbitThread(ComplexBigDecimal seed)
		{
			this.seed = seed;
		}
		
		@Override
		public void run()
		{
			Region<Integer> pixelRegion = new Region<Integer>(
					0,0,
					(int) orbitCanvas.getWidth(),
					(int) orbitCanvas.getHeight());
			
			x = Calculator.pointToPixelX(seed.getRealPart(), originalRegion, pixelRegion, precision);
			y = -Calculator.pointToPixelY(seed.getImaginaryPart(), originalRegion, pixelRegion, precision);
			oldX = x;
			oldY = y;
			Platform.runLater(()->{
				orbitGC.setFill(Color.WHITE);
				orbitGC.fillRect(0, 0, orbitCanvas.getWidth(), orbitCanvas.getHeight());
			});
			
			int i = 0;
			Complex newTerm = new Complex(0,0);
			if(julia)
			{
				newTerm = seed.toComplex();
			}
			while(i<=iterations)
			{
				Platform.runLater(()->{
					/*orbitGC.setGlobalAlpha(1);
					orbitGC.setFill(Color.WHITE);
					orbitGC.fillRect(0, 0, orbitCanvas.getWidth(), orbitCanvas.getHeight());
					orbitGC.setGlobalAlpha(0.5);
					orbitGC.drawImage(previewViewerImage, 0, 0, orbitCanvas.getWidth(), orbitCanvas.getHeight());*/
					orbitGC.setStroke(Color.RED);
					orbitGC.setFill(Color.RED);
					orbitGC.strokeLine(oldX, oldY, x, y);
					orbitGC.fillRect(x, y, 1, 1);
					orbitGC.strokeOval(x-5, y-5, 10, 10);
					
					
				});
				if(julia)
				{
					newTerm = MandelbrotFunction.iterate(juliaSeed.toComplex(),newTerm);
				}
				else
				{
					newTerm = MandelbrotFunction.iterate(seed.toComplex(), newTerm);
				}
				
				if(newTerm.ABS()>=10)
				{
					break;
				}
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e)
				{
					break;
				}
				
				oldX = x;
				oldY = y;
				x = Calculator.pointToPixelX(new BigDecimal(newTerm.getRealPart()), originalRegion, pixelRegion, precision);
				y = -Calculator.pointToPixelY(new BigDecimal(newTerm.getImaginaryPart()), originalRegion, pixelRegion, precision);
				i++;
				
			}
			
			Platform.runLater(()->{
				/*orbitGC.setGlobalAlpha(1);
				orbitGC.setFill(Color.WHITE);
				orbitGC.fillRect(0, 0, orbitCanvas.getWidth(), orbitCanvas.getHeight());
				orbitGC.setGlobalAlpha(0.5);
				orbitGC.drawImage(previewViewerImage, 0, 0, orbitCanvas.getWidth(), orbitCanvas.getHeight());*/
				orbitGC.setStroke(Color.RED);
				orbitGC.setFill(Color.RED);
				orbitGC.fillRect(x, y, 1, 1);
				orbitGC.strokeOval(x-5, y-5, 10, 10);
				orbitGC.strokeLine(oldX, oldY, x, y);
			});
			
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
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			Platform.runLater(()->{
				progressBar.setProgress(1);
				progressIndicator.setProgress(1);
				});
			
			runningThreads = new ArrayList<Thread>();
			mainCalculator.setInterrupt(false);
			
			ImageReady ready = new ImageReady();
			SnapshotParameters sp = new SnapshotParameters();
			Platform.runLater(() -> viewerCanvas.snapshot(ready, sp, null));
			
			waitForImage = true;
			synchronized(lock)
			{
				try {
					while(waitForImage)
					{
						lock.wait();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public class ImageReady implements Callback<SnapshotResult, Void>
	{
		@Override
		public Void call(SnapshotResult result)
		{
			currentImage = result.getImage();
			displayImage = result.getImage();
			actualImage = result.getImage();
			waitForImage = false;
			synchronized(lock)
			{
				lock.notifyAll();
			}
			return null;
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
		 */
		@Override
		public void run() {
			resizable = true;
			if(jSet)
			{
				WritableImage image = new WritableImage(pixelRegionSection.getWidth().intValue(), pixelRegionSection.getHeight().intValue());
				image = calculator.generateJuliaSet(seed, pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision, 16, 0, image);
				drawImageToCanvas(image);
				
				image = calculator.generateJuliaSet(seed, pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,4,16,image);
				drawImageToCanvas(image);
				
				image = calculator.generateJuliaSet(seed, pixelRegionSection, region, pixelRegion, iterations, arbPrecision, precision,1,4,image);
				drawImageToCanvas(image);
				
				Platform.runLater(() -> previewViewerImage = juliaViewer.snapshot(new SnapshotParameters(), null));
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
	
	public class ThreadQueue implements Runnable
	{
		private ArrayBlockingQueue<CallableClass> queue;
		private boolean terminate;
		public ThreadQueue()
		{
			queue = new ArrayBlockingQueue<CallableClass>(100);
			terminate = false;
		}
		
		public void callLater(CallableClass callable)
		{
			/*Consider Changing to Put*/
			queue.offer(callable);
		}
		
		public void terminate()
		{
			terminate = true;
			queue.offer(() -> {return true;});
		}
		
		public ArrayBlockingQueue<CallableClass> getQueue()
		{
			return queue;
		}

		@Override
		public void run()
		{
			try
			{
				while(!terminate)
				{
					CallableClass callable = queue.take();
					if(callable.call())
					{
						return;
					}
				}
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
