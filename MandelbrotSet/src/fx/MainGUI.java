package fx;
import javafx.animation.*;
import javafx.animation.Animation.*;
import javafx.application.*;
import javafx.beans.value.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.stage.*;
import javafx.util.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.transform.*;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.io.*;
import java.math.*;
import java.text.*;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import colorFunction.CustomColorFunction;
import de.codecentric.centerdevice.*;
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
	BorderPane layout;
	TextArea textArea;
	Stage window;
	FlowPane flowPane;
	Canvas viewerCanvas, juliaViewer, orbitCanvasLayer1, orbitCanvasLayer2;
	GraphicsContext mainGC, juliaGC, orbitGC1, orbitGC2;
	ProgressBar progressBar;
	ProgressIndicator progressIndicator;
	Region<BigDecimal> currentRegion;
	Region<Integer> viewerPixelRegion, previewPixelRegion;
	WritableImage currentImage,previewViewerImage, displayImage,  actualImage;
	Calculator mainCalculator, previewCalculator;
	ComplexBigDecimal juliaSeed;
	ArrayList<PannedImage> pannedImages;
	ArrayList<Region<BigDecimal>> loggedRegions;
	//ArrayList<Thread> runningThreads;
	OptionsEditor optionsEditor;
	Timeline timeline;
	BigDecimal magnification;
	Object lock = new Object();
	ThreadQueue threadQueue;
	ArrayList<Future<?>> futureList;
	ExecutorService executor = Executors.newCachedThreadPool();
	private final Region<BigDecimal> originalRegion = new Region<BigDecimal>(new BigDecimal("-2"),
			new BigDecimal("2"),
			new BigDecimal("2"),
			new BigDecimal("-2"));
	Thread orbitThread;
	boolean skip = true;
	boolean closed, idle, julia, arbitraryPrecision, autoIterations,resizable, waitForImage;
	int width, height, previewWidth, previewHeight, iterations, precision, initX, initY, imageX, imageY, threadCount;
	int scrollX, scrollY;
	long startTime, endTime;
	double estimatedTime;
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
		writeFiles();
		//showStartDialog();
		width = 600;
		height = 600;
		initializeVariables();
		
		/*Sets the root*/
		this.window = window;
		Group root = new Group();
		Scene scene = new Scene(root);
		scene.getStylesheets().add(this.getClass().getResource("MainStyle.css").toExternalForm());
		window.setScene(scene);
		
		/*Fills the root entirely with a border pane layout*/
		layout = new BorderPane();
		layout.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		root.getChildren().add(layout);
		
		flowPane = new FlowPane();
		flowPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		VBox vbox = new VBox(10);
		vbox.getChildren().addAll(buildMenus(),buildProgressBar());
		layout.setTop(vbox);
		layout.setCenter(new Pane(buildViewerCanvas()));
		
		
		flowPane.getChildren().add(new Pane(buildPreviewCanvas()));
		flowPane.getChildren().add(buildTextArea());
		flowPane.getChildren().add(new Pane(buildOrbitCanvas()));
		layout.setRight(flowPane);
		BorderPane.setAlignment(flowPane, Pos.TOP_LEFT);
		
		/*Add Listeners*/
		addKeyPanListener();
		addMouseListeners();
		addScrollListener();
		addResizeListeners();
		addWindowListener();
		
		
		window.show();
		initializeMoreValues();
		/*Initializes the program by drawing the mandelbrot set*/
		updateJuliaSetViewer();
		drawSet();
	}
	
	private void writeFiles()
	{
		if(!Locator.exists("SavedColors.txt"))
		{
			ObjectOutputStream out = null;
			try {
				 out = new ObjectOutputStream(new FileOutputStream(Locator.locateFile("SavedColors.txt").toFile()));
				out.writeObject(CustomColorFunction.COLOR_FUNCTIONS);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				try {
					if(out != null)
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void initializeMoreValues()
	{
		progressBar.setPrefWidth(window.getScene().getWidth()-50);
		optionsEditor = new OptionsEditor(this);
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
		//runningThreads = new ArrayList<Thread>();
		timeline = new Timeline(new KeyFrame(Duration.millis(2000),ae->{}));
		updater = new Thread();
		orbitThread = new Thread();
		magnification = new BigDecimal("1");
		threadQueue = new ThreadQueue();
		new Thread(threadQueue).start();
		futureList = new ArrayList<Future<?>>();
		currentRegion = originalRegion;
		closed = false;
		idle = false;
		julia = false;
		arbitraryPrecision = false;
		autoIterations = true;
		resizable = false;
		waitForImage = false;
		startTime = 0;
		endTime = 0;
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
		textArea.setPrefWidth(previewWidth);
		textArea.setPrefHeight(previewHeight);
		updateTextArea();
		textArea.setEditable(false);
		textArea.setFocusTraversable(false);
		return textArea;	
	}
	
	private MenuBar buildMenus()
	{
		if(Locator.isMac())
		{
			MenuToolkit tk = MenuToolkit.toolkit();
			Menu appMenu = tk.createDefaultApplicationMenu("Test");
			tk.setApplicationMenu(appMenu);
			appMenu.getItems().remove(0, appMenu.getItems().size());
			appMenu.getItems().addAll(buildAboutMenuItem(),
					new SeparatorMenuItem(),
					buildPreferencesMenuItem(),
					new SeparatorMenuItem(),
					tk.createHideMenuItem(Locator.appTitle),
					tk.createHideOthersMenuItem(),
					tk.createBringAllToFrontItem(),
					new SeparatorMenuItem(),
					buildQuitMenuItem());
		}
		
		/*Create MenuBar*/
		MenuBar menuBar = new MenuBar();
		menuBar.useSystemMenuBarProperty().set(true);
		
		/*Builds File Menu*/
		Menu fileMenu = new Menu("File");
		fileMenu.getItems().addAll(buildReadMeMenuItem(),buildSaveMenuItem());
		if(! Locator.isMac())
		{
			fileMenu.getItems().addAll(new SeparatorMenuItem(),
					buildAboutMenuItem(),
					buildPreferencesMenuItem(),
					buildQuitMenuItem());
		}
		
		/*Build Edit Menu*/
		RadioMenuItem mset = buildMandelbrotSetMenuItem();
		RadioMenuItem jset = buildJuliaSetMenuItem();
		ToggleGroup toggleGroup = new ToggleGroup();
		mset.setToggleGroup(toggleGroup);
		jset.setToggleGroup(toggleGroup);
		
		
		Menu editMenu = new Menu("Edit");
		editMenu.getItems().addAll(mset,jset,
				new SeparatorMenuItem(),
				buildResetMenuItem(),buildRerenderMenuItem(), buildUndoMenuItem(),buildInterruptMenuItem(),
				new SeparatorMenuItem(),
				buildEditMenuItem());
		
		
		/*Color Menu*/
		Menu colorMenu = new Menu("Colors");
		colorMenu.getItems().addAll(
				buildDefaultColorsMenuItem(),
				buildColorEditMenuItem(),
				new SeparatorMenuItem(),
				buildClearColorDataMenuItem());
		
		menuBar.getMenus().add(fileMenu);
		menuBar.getMenus().add(editMenu);
		menuBar.getMenus().add(colorMenu);
		
		return menuBar;
	}
	
	private MenuItem buildQuitMenuItem()
	{
		MenuItem quitMenuItem = new MenuItem("Quit Fractal App");
		quitMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));
		quitMenuItem.setOnAction(e->{
			close();
		});
		return quitMenuItem;
	}
	
	private MenuItem buildPreferencesMenuItem()
	{
		MenuItem prefMenuItem = new MenuItem("Preferences...");
		prefMenuItem.setOnAction(e ->{
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setContentText("No Preferences at the moment");
			alert.show();
		});
		return prefMenuItem;
	}
	
	private Menu buildDefaultColorsMenuItem()
	{
		Menu defaultColorsMenu = new Menu("Default Colors");
		
		for(CustomColorFunction cf: CustomColorFunction.COLOR_FUNCTIONS)
		{
			MenuItem colorMenuItem = new MenuItem(cf.getName());
			colorMenuItem.setOnAction(e ->{
				threadQueue.callLater(()->{
					interrupt();
					previewCalculator.setColorFunction(cf);
					mainCalculator.setColorFunction(cf);
					drawSet();
					updateJuliaSetViewer();
				});
			});
			defaultColorsMenu.getItems().add(colorMenuItem);
		}
		return defaultColorsMenu;
	}
	
	private MenuItem buildColorEditMenuItem()
	{
		MenuItem menuItem = new MenuItem("Make Your Own...");
		menuItem.setOnAction(e ->{
			optionsEditor.showEditDialog(1);
		});
		return menuItem;
	}
	
	private MenuItem buildClearColorDataMenuItem()
	{
		MenuItem clearData = new MenuItem("Clear Color Data");
		clearData.setOnAction(e->{
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setContentText("Are you sure you want to overwrite all saved colors with the original default values?");
			
			ButtonType buttonTypeYes = new ButtonType("YES");
			ButtonType buttonTypeNo = new ButtonType("NO",ButtonData.CANCEL_CLOSE);
			alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
			Optional<ButtonType> result = alert.showAndWait();
			if(result.get() != buttonTypeYes)
			{
				return;
			}
			
			ObjectOutputStream out = null;
			try {
				 out = new ObjectOutputStream(new FileOutputStream(Locator.locateFile("SavedColors.txt").toFile()));
				out.writeObject(CustomColorFunction.COLOR_FUNCTIONS);
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
				ioe.printStackTrace();
			}
			finally
			{
				try {
					if(out != null)
					out.close();
				} catch (IOException ioe) {
					// TODO Auto-generated catch block
					ioe.printStackTrace();
				}
			}
		});
		return clearData;
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
				optionsEditor.showEditDialog(0);
			});
		return edit;
	}
	
	private MenuItem buildReadMeMenuItem()
	{
		MenuItem readMe = new MenuItem("Read Me");
		readMe.setOnAction(e->{
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setContentText("For Zooming:\nDrag a box.\nLeft click.\nScroll using the scroll wheel (platform dependent).\n\n"
					+ "For Panning:\nUse the arrow keys.\n\n"
					+ "For Previewing the Julia Sets:\nRight click\n\n"
					+ "Use the Edit menu to switch between Mandelbrot and Julia sets, and to toggle double and arbitrary precision. "
					+ "You may also use it to rerender an image or to go back to a previous image.\n\n"
					+ "Use the Edit > Edit... menu item to change the color of the set, "
					+ "the iterations used, the precision, or the number of threads used.\n\n"
					+ "Use the File menu item to save an image.");
			alert.show();
		});
		return readMe;
	}
	
	/**
	 * Save Menu Item:
	 * Opens up the image saver dialog.
	 * @return Returns the created menuItem
	 */
	private MenuItem buildSaveMenuItem()
	{
		MenuItem saveMenu = new MenuItem("Save Image...");
		saveMenu.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
		saveMenu.setOnAction(e->{
				new ImageSaverDialog(this).showSaverDialog(true, ()->{});
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
		BorderPane.setAlignment(viewerCanvas, Pos.TOP_LEFT);
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
		
		HBox hbox = new HBox(10);
		hbox.getChildren().addAll(progressBar, progressIndicator);
		return hbox;
	}
	
	private Pane buildOrbitCanvas()
	{
		orbitCanvasLayer1 = new Canvas();
		orbitCanvasLayer1.setWidth(previewWidth*2);
		orbitCanvasLayer1.setHeight(previewHeight*2);
		orbitCanvasLayer2 = new Canvas();
		orbitCanvasLayer2.setWidth(previewWidth*2);
		orbitCanvasLayer2.setHeight(previewHeight*2);
		orbitGC1 = orbitCanvasLayer1.getGraphicsContext2D();
		orbitGC1.setFill(Color.WHITE);
		orbitGC1.fillRect(0, 0, width, height);
		orbitGC2 = orbitCanvasLayer2.getGraphicsContext2D();
		orbitGC2.setFill(Color.TRANSPARENT);
		orbitGC2.fillRect(0, 0, width, height);
		orbitCanvasLayer2.toFront();
		return new Pane(orbitCanvasLayer1, orbitCanvasLayer2);
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
						width = (int) Math.min(scene.getHeight()-70, scene.getWidth()-width/3);
						height = width;
						updateAfterResize();
					}
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
						height = (int) Math.min(scene.getHeight()-70, scene.getWidth()-width/3);
						width = height;
						updateAfterResize();
					}
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
			if(isScrolling())
			{
				return;
			}
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
					//new Thread(pi).start();
					executor.execute(pi);
					pannedImages.add(pi);
				}
				else if(e.getCode() == KeyCode.DOWN)
				{
					imageY-=change;
					currentRegion = Calculator.toBigDecimalRegion(new Region<Integer>(0,change,width,height+change), currentRegion, viewerPixelRegion, precision);
					PannedImage pi = new PannedImage(mainCalculator,-imageX,height-imageY-change, new Region<Integer>(0,height-change,width,height),currentRegion, viewerPixelRegion, iterations,
							arbitraryPrecision, precision, julia,juliaSeed, mainGC,this);
					//new Thread(pi).start();
					executor.execute(pi);
					pannedImages.add(pi);
				}
				else if(e.getCode() == KeyCode.LEFT)
				{
					imageX+=change;
					currentRegion = Calculator.toBigDecimalRegion(new Region<Integer>(-change,0,width-change,height), currentRegion, viewerPixelRegion, precision);
					PannedImage pi = new PannedImage(mainCalculator,-imageX,-imageY, new Region<Integer>(0,0,change,height),currentRegion, viewerPixelRegion, iterations,
							arbitraryPrecision, precision, julia,juliaSeed, mainGC,this);
					//new Thread(pi).start();
					executor.execute(pi);
					pannedImages.add(pi);
				}
				else if(e.getCode() == KeyCode.RIGHT)
				{
					imageX-=change;
					currentRegion = Calculator.toBigDecimalRegion(new Region<Integer>(change,0, width+change,height), currentRegion, viewerPixelRegion, precision);
					PannedImage pi = new PannedImage(mainCalculator,-imageX+width-change,-imageY, new Region<Integer>(width-change,0,width,height),currentRegion, viewerPixelRegion, iterations,
							arbitraryPrecision, precision, julia,juliaSeed, mainGC,this);
					//new Thread(pi).start();
					executor.execute(pi);
					pannedImages.add(pi);
				}
				
				
				
				
				Platform.runLater(() ->{
					mainGC.drawImage(currentImage, imageX, imageY);
				});
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
					displayImage = viewerCanvas.snapshot(new SnapshotParameters(), null);
					actualImage = displayImage;
				});
				
					
				
					updateTextArea();
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
			});
			
		});
	}
	
	private void updateAfterResize()
	{
		/*previewWidth = width/3;
		previewHeight = height/3;*/
		orbitThread.interrupt();
		try {
			orbitThread.join();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Platform.runLater(()->{
			progressBar.setPrefWidth(window.getScene().getWidth()-50);
			viewerCanvas.setHeight(height);
			viewerCanvas.setWidth(width);
			
			/*juliaViewer.setHeight(previewHeight);
			juliaViewer.setWidth(previewWidth);
			juliaGC.drawImage(previewViewerImage, 0, 0, previewWidth,previewHeight);
			orbitCanvas.setWidth(previewWidth);
			orbitCanvas.setHeight(previewHeight);
			textArea.setPrefWidth(previewWidth);
			textArea.setPrefHeight(previewHeight);*/
			
			flowPane.setPrefWidth(window.getScene().getWidth()-width);
			
			//orbitGC1.setFill(Color.WHITE);
			//orbitGC1.fillRect(0, 0, previewWidth, previewHeight);
			mainGC.drawImage(displayImage, 0, 0,width,height);
			
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
				System.exit(0);
			});
			threadQueue.terminate();
			
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
		mainCalculator.resetPixelsCalculated();
		updater = (new Thread(new Updater()));
		updater.start();
		
		allocateThreadsInRectangles();
		
		updateTextArea();
		startTime = System.currentTimeMillis();
	}
	
	private void allocateThreadsInSquares()
	{
		int length = (int) Math.sqrt((width*height)/threadCount);
		int squaresPerSide = (int) (width/length);
		for(int i = 0; i<Math.pow(squaresPerSide,2); i++)
		{
			int x = (int) (i%squaresPerSide)*length;
			int y = ((int)(i/squaresPerSide))*length;
			Region<Integer> pixelRegionSection = new Region<Integer>(x,y,x+length, y + length);
			
			futureList.add(executor.submit(new Generator(pixelRegionSection, currentRegion, viewerPixelRegion, iterations,
					arbitraryPrecision, precision,julia,juliaSeed,mainGC, mainCalculator)));
		}
		if(squaresPerSide*length != width)
		{
			Region<Integer> pixelRegionSection = new Region<Integer>(squaresPerSide*length,0,width, height);
			
			futureList.add(executor.submit(new Generator(pixelRegionSection, currentRegion, viewerPixelRegion, iterations,arbitraryPrecision,
					precision,julia,juliaSeed,mainGC, mainCalculator)));
			
			pixelRegionSection = new Region<Integer>(0,squaresPerSide*length,squaresPerSide*length, height);
			
			futureList.add(executor.submit(new Generator(pixelRegionSection, currentRegion, viewerPixelRegion,
					iterations,arbitraryPrecision,
					precision,julia,juliaSeed,mainGC, mainCalculator)));
		}
		
		
	}
	
	private void allocateThreadsInRectangles()
	{
		int h = (int) height/threadCount;
		for(int i = 0; i<threadCount; i++)
		{
			Region<Integer> pixelRegionSection = new Region<Integer>(0,i*h,width,i*h+h);
			
			futureList.add(
					executor.submit(
					new Generator(pixelRegionSection, currentRegion, viewerPixelRegion, iterations,
							arbitraryPrecision, precision,julia,juliaSeed,mainGC, mainCalculator)));
		}
		
		if(h*threadCount < height)
		{
			Region<Integer> pixelRegionSection = new Region<Integer>(0,h*threadCount,width,height);
			futureList.add(
					executor.submit(
							new Generator(pixelRegionSection, currentRegion, viewerPixelRegion, iterations,arbitraryPrecision,
									precision,julia,juliaSeed,mainGC, mainCalculator)));
		}
	}
	
	/**
	 * Updates the text in the text Area.
	 */
	public void updateTextArea()
	{
		Platform.runLater(()->{
			long seconds = Math.round((estimatedTime/1000)) % (24*60*60-1);
			LocalTime timeOfDay = LocalTime.ofSecondOfDay(seconds);
			textArea.setText("Magnification: " + format(magnification.setScale(1, BigDecimal.ROUND_HALF_UP))+ "x\n" + 
					"Iterations: " + iterations + "\n"
					+ "Precision: " + precision + "\n"
					+ "Julia Set: " + julia + "\n"
					+ "Center: " + currentRegion.getCenterX().setScale(6, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString() + " + "
					+ currentRegion.getCenterY().setScale(6, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString() + "i" + "\n"
					+ "Threads: " + threadCount + "\n"
					+ "Color: " + mainCalculator.getColorFunction().toString() + "\n"
					+ "Arbitrary Precision: " + arbitraryPrecision + "\n"
					+ "Estimated Time: " + timeOfDay.toString() +"\n");
		});
	}
	
	/**
	 * Returns the bigDecimal as a string in scientific notation
	 * @param number
	 * @return A string
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
		int h = (int) (previewHeight/10);
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
	
	private boolean isScrolling()
	{
		return (timeline.getStatus() == Status.RUNNING);
	}
	
	
	/**
	 * A thread that renders orbits onto a  superimposed image of the julia set.
	 * Currently it uses the swing image and converts it. Consider finding a pure fx solution.
	 * @author Ezra
	 *
	 */
	public class OrbitThread implements Runnable
	{
		private ComplexBigDecimal seed;
		private int x,y, oldX, oldY;
		
		/**
		 * Constructs an orbit 
		 * @param seed
		 */
		public OrbitThread(ComplexBigDecimal seed)
		{
			this.seed = seed;
		}
		
		@Override
		public void run()
		{
			Region<Integer> pixelRegion = new Region<Integer>(
					0,0,
					(int) orbitCanvasLayer1.getWidth(),
					(int) orbitCanvasLayer1.getHeight());
			
			x = Calculator.pointToPixelX(seed.getRealPart(), originalRegion, pixelRegion, precision);
			y = -Calculator.pointToPixelY(seed.getImaginaryPart(), originalRegion, pixelRegion, precision);
			oldX = x;
			oldY = y;
			
			Platform.runLater(()->{
				orbitGC2.clearRect(0, 0, orbitCanvasLayer2.getWidth(),  orbitCanvasLayer2.getHeight());
				orbitGC1.drawImage(previewViewerImage, 0, 0, orbitCanvasLayer1.getWidth(), orbitCanvasLayer1.getHeight());
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
					
					orbitGC2.setFill(Color.WHITE);
					orbitGC2.setStroke(Color.WHITE);
					orbitGC2.strokeLine(oldX, oldY, x, y);
					orbitGC2.fillRect(x, y, 1, 1);
					orbitGC2.strokeOval(x-5, y-5, 10, 10);
					
					orbitGC1.drawImage(previewViewerImage, 0, 0, orbitCanvasLayer1.getWidth(), orbitCanvasLayer1.getHeight());
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
					Thread.sleep(50);
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
				orbitGC2.setFill(Color.WHITE);
				orbitGC2.setStroke(Color.WHITE);
				orbitGC2.strokeLine(oldX, oldY, x, y);
				orbitGC2.fillRect(x, y, 1, 1);
				orbitGC2.strokeOval(x-5, y-5, 10, 10);
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
				Platform.runLater(() -> {
					double percentComplete = (double)mainCalculator.getPixelsCalculated()/((double)viewerCanvas.getWidth()*viewerCanvas.getHeight());
						progressBar.setProgress(percentComplete);
						progressIndicator.setProgress(percentComplete);
						
						estimatedTime=((System.currentTimeMillis()-startTime)/percentComplete) - (System.currentTimeMillis()-startTime);
						updateTextArea();
					});
				if(mainCalculator.getPixelsCalculated() >= viewerCanvas.getWidth()*viewerCanvas.getHeight())
				{
					idle = true;
				}
			}
			
			try
			{
				for(Future<?> task : futureList)
				{
					task.get();
				}
			}
			catch(ExecutionException | InterruptedException ee)
			{
				ee.printStackTrace();
			}
			futureList = new ArrayList<Future<?>>();
			
			Platform.runLater(()->{
				progressBar.setProgress(1);
				progressIndicator.setProgress(1);
				});
			
			mainCalculator.setInterrupt(false);
			
			//ImageReady ready = new ImageReady();
			SnapshotParameters sp = new SnapshotParameters();
			Platform.runLater(() -> viewerCanvas.snapshot((SnapshotResult result) ->{
				currentImage = result.getImage();
				displayImage = result.getImage();
				actualImage = result.getImage();
				waitForImage = false;
				synchronized(lock)
				{
					lock.notifyAll();
				}
				return null;
				
			}, sp, null));
			
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
			endTime = System.currentTimeMillis();
			updateTextArea();
		}
	}
	
	/*public class ImageReady implements Callback<SnapshotResult, Void>
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
		
	}*/
	
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
	
	
	/**
	 * A thread that is always running. It accepts CallableClass objects whose call method
	 * will be executed on threadQueue thread some time after callLater(CallableClass) is invoked.
	 * 
	 * Consider replacing this with a threadPool.
	 * @author Ezra
	 *
	 */
	public class ThreadQueue implements Runnable
	{
		private ArrayBlockingQueue<CallableClass> queue;
		private boolean terminate;
		
		/**
		 * Constructs this threadQueue.
		 */
		public ThreadQueue()
		{
			queue = new ArrayBlockingQueue<CallableClass>(100);
			terminate = false;
		}
		
		/**
		 * Schedules this callable to be called on a separate thread.
		 * If the thread has already terminated, it will not accept any more values.
		 * @param callable		Should return false unless it wants the thread queue to terminate.
		 */
		public void callLater(CallableClass callable)
		{
			/*Consider Changing to Put*/
			if(!terminate)
			{
				queue.offer(callable);
			}
		}
		
		/**
		 * Terminates the thread.
		 * It sets terminate to true then it feeds the queue an empty CallableClass to unblock it.
		 */
		public void terminate()
		{
			terminate = true;
			queue.offer(() -> {});
		}
		
		/**
		 * Returns the blocking queue.
		 * @return the blocking queue.
		 */
		public ArrayBlockingQueue<CallableClass> getQueue()
		{
			return queue;
		}

		
		/**
		 * Loops through each callableClass in the queue blocking if none is available. 
		 */
		@Override
		public void run()
		{
			try
			{
				while(!terminate)
				{
					CallableClass callable = queue.take();
					callable.call();
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
