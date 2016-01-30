package fx;

import java.io.*;
import java.math.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.*;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.entity.mime.*;
import org.apache.http.impl.client.*;
import colorFunction.*;
import javafx.application.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.control.ButtonBar.*;
import javafx.scene.control.cell.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.*;

/**
 * This class is used to control all the options of the program including colors iterations threads etc.
 * @author Ezra
 *
 */
public class OptionsEditor
{
	private MainGUI gui;
	private Stage window;
	private Label centerValue, set, seed;
	private Label boxValue;
	private TextField threadCountField;
	private TextField iterationsField;
	private TextField precisionField;
	private CheckBox autoIterationsCheckBox;
	private ChoiceBox<SavedRegion> savedRegionsChoiceBox, uploadRegionsChoiceBox;
	private ChoiceBox<CustomColorFunction> colorChoiceBox, uploadColorsChoiceBox;
	private TextField colorPositionField;
	private TextField rangeField;
	private Slider colorPositionSlider;
	private ListView<Stop> stopList;
	private RadioButton arbitraryPrecision;
	private RadioButton doublePrecision;
	private Rectangle gradientRectangle;
	private ColorPicker colorPicker;
	private ObjectOutputStream out, colorOut;
	private SimpleListProperty<SavedRegion> savedRegions;
	private Region<BigDecimal> currentRegion;
	private boolean currentJulia;
	private ComplexBigDecimal currentSeed;
	private int tabNumber;
	//private ArrayList<CustomColorFunction> savedColors;
	private SimpleListProperty<CustomColorFunction> savedColors;
	private TextField uploadNameField, uploadAuthorField;
	private TextArea uploadDescriptionArea;
	private ChoiceBox<String> uploadTypeChoiceBox;
	private Statement stmt = null;
	private TableView<ImageRow> downloadImageTable;
	private TableView<RegionRow> downloadRegionTable;
	private TableView<ColorRow> downloadColorTable;
	private TabPane tables;
	private Thread databaseUpdater;
	private Connection conn;
	
	/**
	 * Constructs this editor with a reference to the gui that created it.
	 * @param gui
	 */
	public OptionsEditor(MainGUI gui)
	{
		
		
		this.gui = gui;
		currentRegion = gui.currentRegion;
		currentJulia = gui.julia;
		currentSeed = gui.juliaSeed;
	}
	
	/**
	 * Shoes the view with the given tab number.
	 * @param tabNumber
	 */
	public void showEditDialog(int tabNumber)
	{
		this.tabNumber = tabNumber;
		stmt = null;
		readFiles();
		buildEditDialog();
		resetValues();
		buildThreads();
		window.show();
	}
	
	private void buildThreads()
	{
		databaseUpdater = new Thread(()->{
			while(true)
			{
				if(!isConnectedToInternet())
				{
					Platform.runLater(()->{
						downloadRegionTable.setItems(FXCollections.observableArrayList(new ArrayList<RegionRow>()));
						downloadColorTable.setItems(FXCollections.observableArrayList(new ArrayList<ColorRow>()));
						downloadImageTable.setItems(FXCollections.observableArrayList(new ArrayList<ImageRow>()));
					});
					
					stmt = null;
				}
				else
				{
					if(stmt == null)
					{
						openConnection();
					}
					connect();
				}
				
				try
				{
					Thread.sleep(1000);
				}
				catch(InterruptedException ie)
				{
					break;
				}
			}
		});
		databaseUpdater.start();
	}
	
	private void openConnection()
	{
		//THIS DISPLAYS PASS IN PLAINTEXT!!!!
		try {
			conn = DriverManager.getConnection("jdbc:mysql://www.ezstein.xyz:3306/WebDatabase", "java", "javaPass");
			stmt = conn.createStatement();
			System.out.println(stmt.isClosed());
			
		} catch (SQLException e) {
			downloadRegionTable.setItems(FXCollections.observableArrayList(new ArrayList<RegionRow>()));
			downloadColorTable.setItems(FXCollections.observableArrayList(new ArrayList<ColorRow>()));
			downloadImageTable.setItems(FXCollections.observableArrayList(new ArrayList<ImageRow>()));
		}
		
	}
	
	private void connect()
	{
		try
		{
			if(!isConnectedToInternet())
			{
				return;
			}
			ArrayList<ImageRow> dataImage = new ArrayList<ImageRow>();
			ResultSet set = stmt.executeQuery("SELECT * FROM Images");
			while(set.next()){
				
					dataImage.add(new ImageRow(
							set.getInt("ID"),
							set.getString("Name"),
							set.getString("Author"),
							set.getString("Description"),
							set.getString("file"),
							set.getString("SetType"),
							set.getInt("Width"),
							set.getInt("Height"),
							set.getInt("Size"),
							set.getInt("Date"),
							set.getString("FileType")
							));
				}
			Set<ImageRow> imageRowA = new HashSet<ImageRow>(dataImage);
			Set<ImageRow> imageRowB = new HashSet<ImageRow>(downloadImageTable.getItems());
			/*Subtracts B From A to find difference in the two sets*/
			imageRowA.removeAll(imageRowB);
			downloadImageTable.getItems().addAll(imageRowA);
			
			if(!isConnectedToInternet())
			{
				return;
			}
			ArrayList<RegionRow> dataRegion = new ArrayList<RegionRow>();
			set = stmt.executeQuery("SELECT * FROM Regions");
			while(set.next()){
					dataRegion.add(new RegionRow(
							set.getInt("ID"),
							set.getString("Name"),
							set.getString("Author"),
							set.getString("Description"),
							set.getString("file"),
							set.getInt("Size"),
							set.getInt("Date"),
							set.getInt("HashCode")
							));
			}
			
			Set<RegionRow> regionRowA = new HashSet<RegionRow>(dataRegion);
			Set<RegionRow> regionRowB = new HashSet<RegionRow>(downloadRegionTable.getItems());
			/*Subtracts B From A to find difference in the two sets*/
			regionRowA.removeAll(regionRowB);
			downloadRegionTable.getItems().addAll(regionRowA);
			
			if(!isConnectedToInternet())
			{
				return;
			}
			ArrayList<ColorRow> dataColor = new ArrayList<ColorRow>();
			set = stmt.executeQuery("SELECT * FROM Colors");
			while(set.next()){
					dataColor.add(new ColorRow(
							set.getInt("ID"),
							set.getString("Name"),
							set.getString("Author"),
							set.getString("Description"),
							set.getString("file"),
							set.getInt("Size"),
							set.getInt("Date"),
							set.getInt("HashCode")));
				}
			Set<ColorRow> colorRowA = new HashSet<ColorRow>(dataColor);
			Set<ColorRow> colorRowB = new HashSet<ColorRow>(downloadColorTable.getItems());
			
			/*Subtracts B From A to find difference in the two sets*/
			colorRowA.removeAll(colorRowB);
			downloadColorTable.getItems().addAll(colorRowA);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void readFiles()
	{
		ObjectInputStream in = null;
		ObjectInputStream colorIn = null;
		
		savedRegions = new SimpleListProperty<SavedRegion>();
		savedColors = new SimpleListProperty<CustomColorFunction>();
		try
		{
			colorIn = new ObjectInputStream(new FileInputStream(Locator.locateFile("SavedColors.txt").toFile()));
			savedColors = new SimpleListProperty<CustomColorFunction>(FXCollections.observableList((ArrayList<CustomColorFunction>) colorIn.readObject()));
			
			in = new ObjectInputStream(new FileInputStream(Locator.locateFile("SavedRegions.txt").toFile()));
			savedRegions = new SimpleListProperty<SavedRegion>(FXCollections.observableList((ArrayList<SavedRegion>)in.readObject()));
		}
		catch(EOFException eofe)
		{
			/*File Empty And inputStream is null*/
			savedRegions = new SimpleListProperty<SavedRegion>(FXCollections.observableList(new ArrayList<SavedRegion>()));
			//eofe.printStackTrace();
		}
		catch (IOException | ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(in!=null)
				{
					
					in.close();
				}
				if(colorIn != null)
				{
					colorIn.close();
				}
			}
			catch (IOException e)
			{
				System.out.println("Could Not Close Object InputStream");
				e.printStackTrace();
			}
		}
		
		savedColors.addListener(new ChangeListener<ObservableList<CustomColorFunction>>(){

			@Override
			public void changed(ObservableValue<? extends ObservableList<CustomColorFunction>> observable,
					ObservableList<CustomColorFunction> oldValue, ObservableList<CustomColorFunction> newValue) {
				

				
				colorChoiceBox.setItems(savedColors);
				uploadColorsChoiceBox.setItems(savedColors);
				
				try
				{
					colorOut = new ObjectOutputStream(new FileOutputStream(Locator.locateFile("SavedColors.txt").toFile()));
					colorOut.writeObject(new ArrayList<CustomColorFunction>( Arrays.asList(savedColors.toArray(new CustomColorFunction[0])) ));
				}
				catch(IOException ioe)
				{
					ioe.printStackTrace();
				}
				finally
				{
					try
					{
						colorOut.close();
					}
					catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				
			}
			
		});
		
		savedRegions.addListener(new ChangeListener<ObservableList<SavedRegion>>(){

			@Override
			public void changed(ObservableValue<? extends ObservableList<SavedRegion>> observable,
					ObservableList<SavedRegion> oldValue, ObservableList<SavedRegion> newValue) {
				
				savedRegionsChoiceBox.setItems(savedRegions);
				uploadRegionsChoiceBox.setItems(savedRegions);
				try
				{
					/*Overwrites File*/
					out = new ObjectOutputStream(new FileOutputStream(Locator.locateFile("SavedRegions.txt").toFile()));
					out.writeObject(new ArrayList<SavedRegion>( Arrays.asList(savedRegions.toArray(new SavedRegion[0])) ));
				}
				catch (IOException ioe)
				{
					ioe.printStackTrace();
				}
				finally
				{
					try
					{
						out.close();
					}
					catch (IOException ioe)
					{
						System.out.println("Could Not Close ObjectOutputStream");
						ioe.printStackTrace();
					}
				}
				
			}
			
		});
	}
	
	private void buildEditDialog()
	{
		window = new Stage();
		window.setTitle("Edit...");
		window.initModality(Modality.APPLICATION_MODAL);
		window.setOnCloseRequest(e ->{
			ResultType result = askToSaveColor();
			if(result == ResultType.CANCEL)
			{
				e.consume();
			}
			close();
		});
		BorderPane layout = new BorderPane();
		
		TabPane tabPane = new TabPane();
		tabPane.getTabs().add(buildOptionsTab());
		tabPane.getTabs().add(buildColorTab());
		tabPane.getTabs().add(buildUploadTab());
		tabPane.getTabs().add(buildDownloadTab());
		layout.setCenter(tabPane);
		tabPane.getSelectionModel().select(tabNumber);
		tabPane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if(newValue.intValue()==3)
				{
					//new Thread(()->{connect();}).start();
				}
			}
			
		});
		
		HBox buttonBox = new HBox(10);
		buttonBox.setPadding(new Insets(10,10,10,10));
		buttonBox.getChildren().addAll(buildSaveButton(), buildApplyAndRerenderButton(), buildApplyButton(), buildCancelButton());
		layout.setBottom(buttonBox);
		
		Scene scene = new Scene(layout);
		scene.getStylesheets().add(this.getClass().getResource("OptionsStyleSheet.css").toExternalForm());
		window.setScene(scene);
	}
	
	public void close(){
		databaseUpdater.interrupt();
		try {
			databaseUpdater.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try{
			if(stmt != null)
			{	
				stmt.close();
			}
			if(conn !=null)
			{
				conn.close();
			}
		} catch(SQLException sqle){
			sqle.printStackTrace();
		}
		
	}
	
	private Tab buildOptionsTab()
	{
		Tab optionsTab = new Tab("Options");
		optionsTab.setClosable(false);
		GridPane optionsGridPane;
		optionsGridPane= new GridPane();
		optionsGridPane.setVgap(10);
		optionsGridPane.setHgap(10);
		optionsGridPane.setPadding(new Insets(30,30,30,30));
		
		
		centerValue = new Label(gui.currentRegion.getCenterX().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()
				+ " + "
				+ gui.currentRegion.getCenterY().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString() + "i");
		boxValue = new Label(gui.currentRegion.x1.subtract(gui.currentRegion.x2).abs().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()
				+ "x"
				+ gui.currentRegion.y1.subtract(gui.currentRegion.y2).abs().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString());
		
		/*Text Fields*/
		threadCountField = new TextField(""+gui.threadCount);
		iterationsField = new TextField("" +gui.iterations);
		precisionField = new TextField("" + gui.precision);
		
		arbitraryPrecision = new RadioButton("Arbitrary Precision");
		doublePrecision = new RadioButton("Double Precision");
		ToggleGroup precisionGroup = new ToggleGroup();
		arbitraryPrecision.setToggleGroup(precisionGroup);
		doublePrecision.setToggleGroup(precisionGroup);
		if(gui.arbitraryPrecision)
		{
			arbitraryPrecision.setSelected(true);
		}
		else
		{
			doublePrecision.setSelected(true);
		}
		
		
		
		
		
		Label savedRegionsLabel = new Label("Saved Regions:");
		savedRegionsLabel.setFont(new Font(20));
		optionsGridPane.add(savedRegionsLabel, 0, 0,2,1);
		optionsGridPane.add(new Label("Iterations:"), 0, 1);
		optionsGridPane.add(new Label("Precision:"), 0, 3);
		optionsGridPane.add(new Label("Threads:"), 0,4);
		
		optionsGridPane.add(buildAutoIterationsCheckBox(), 1, 1);
		optionsGridPane.add(iterationsField, 1, 2);
		optionsGridPane.add(precisionField, 1, 3);
		optionsGridPane.add(threadCountField, 1, 4);
		optionsGridPane.add(doublePrecision, 1, 5);
		optionsGridPane.add(arbitraryPrecision, 1, 6);
		
		optionsGridPane.add(buildSavedRegionsChoiceBox(), 2, 0);
		optionsGridPane.add(buildSetLabel(), 2, 1);
		optionsGridPane.add(new Label("Center:"), 2, 2);
		optionsGridPane.add(new Label("Box Dimensions:"), 2, 3);
		
		optionsGridPane.add(buildRegionRemoveButton(), 3, 0);
		optionsGridPane.add(buildSeedLabel(), 3, 1);
		optionsGridPane.add(centerValue, 3, 2);
		optionsGridPane.add(boxValue, 3, 3);
		
		
		optionsTab.setContent(optionsGridPane);
		return optionsTab;
	}
	
	private Label buildSetLabel()
	{
		set = new Label();
		if(gui.julia)
		{
			set.setText("Julia set: ");
		}
		else
		{
			set.setText("Mandelbrot set: ");
		}
		return set;
	}
	
	private Label buildSeedLabel()
	{
		seed = new Label();
		if(gui.julia)
		{
			seed.setText(gui.juliaSeed.toString());
		}
		else
		{
			seed.setText("0+0i");
		}
		return seed;
	}
	
	private CheckBox buildAutoIterationsCheckBox()
	{
		autoIterationsCheckBox = new CheckBox("Auto Iterations");
		if(gui.autoIterations)
		{
			autoIterationsCheckBox.setSelected(true);
			iterationsField.setDisable(true);
		}
		else
		{
			autoIterationsCheckBox.setSelected(false);
			iterationsField.setDisable(false);
		}
		
		autoIterationsCheckBox.setOnAction(e ->{
			if(autoIterationsCheckBox.isSelected())
			{
				iterationsField.setDisable(true);
				iterationsField.setText(gui.calcAutoIterations(gui.magnification) + "");
			}
			else
			{
				iterationsField.setDisable(false);
			}
		});
		return autoIterationsCheckBox;
	}
	
	private ChoiceBox<SavedRegion> buildSavedRegionsChoiceBox()
	{
		savedRegionsChoiceBox = new ChoiceBox<SavedRegion>(savedRegions);
		savedRegionsChoiceBox.setConverter(new StringConverter<SavedRegion>(){

			@Override
			public String toString(SavedRegion sr)
			{
				return sr.name;
			}

			@Override
			public SavedRegion fromString(String string) {
				return null;
			}
			
			
		});
		savedRegionsChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Platform.runLater(() -> {
					if(savedRegionsChoiceBox.getSelectionModel().getSelectedItem() != null)
					{
						loadRegion();
					}
					});
				}
		});
		return savedRegionsChoiceBox;
	}
	
	private Button buildRegionRemoveButton()
	{
		Button removeButton = new Button("Remove");
		removeButton.setOnAction(e ->{
			SavedRegion deletedRegion = savedRegionsChoiceBox.getValue();
			if(deletedRegion == null)
			{ 
				Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText("No region is selected");
				alert.show();
				return;
			}
			savedRegions.remove(deletedRegion);
			
		});
		return removeButton;
	}
	
	private Tab buildColorTab()
	{
		Tab colorTab = new Tab("Color");
		colorTab.setClosable(false);
		
		GridPane colorGridPane= new GridPane();
		colorGridPane.setVgap(10);
		colorGridPane.setHgap(10);
		colorGridPane.setPadding(new Insets(30,30,30,30));
		
		
		colorGridPane.add(buildColorChoiceBox(), 0, 0);
		//colorGridPane.add(buildGradientCheckBox(), 0, 1);
		colorGridPane.add(buildGradientRectangle(), 0, 2, 2, 2);
		colorGridPane.add(buildSaveColorButton(), 0, 3);
		
		colorGridPane.add(buildColorPositionSlider(), 1, 1);
		colorGridPane.add(buildColorPositionField(), 2, 1);
		colorGridPane.add(buildColorPicker(), 1, 0);
		colorGridPane.add(buildRemoveColorButton(), 1, 3);
		
		colorGridPane.add(buildStopList(), 2, 2,2,1);
		colorGridPane.add(buildAddStopButton(), 2, 3);
		colorGridPane.add(new Label("Range:"), 2, 0);
		rangeField = new TextField();
		colorGridPane.add(rangeField, 3, 0);
		colorGridPane.add(buildRemoveStopButton(), 3, 3);
		
		colorTab.setContent(colorGridPane);
		
		initializeValues();
		return colorTab;
	}
	
	private Button buildRemoveColorButton()
	{
		Button removeColorButton = new Button("Delete Color");
		removeColorButton.setOnAction(e->{
			
			CustomColorFunction colorToRemove = colorChoiceBox.getValue();
			int index = colorChoiceBox.getItems().indexOf(colorToRemove);
			if(index >0)
			{
				colorChoiceBox.setValue(colorChoiceBox.getItems().get(index-1));
			}
			else if(colorChoiceBox.getItems().size() > 1)
			{
				colorChoiceBox.setValue(colorChoiceBox.getItems().get(index+1));
			}
			else
			{
				Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText("You must keep at least one color");
				alert.show();
				return;
			}
			
			savedColors.remove(colorToRemove);
			
		});
		return removeColorButton;
	}
	
	private void initializeValues()
	{
		rangeField.setText(colorChoiceBox.getValue().getRange()+"");
		stopList.getItems().remove(0, stopList.getItems().size());
		for(Stop stop : colorChoiceBox.getValue().getStops())
		{
			stopList.getItems().add(stop);
		}
		
	}
	
	private Rectangle buildGradientRectangle()
	{
		gradientRectangle = new Rectangle();
		gradientRectangle.setWidth(200);
		gradientRectangle.setHeight(300);
		
		return gradientRectangle;
	}
	
	private ColorPicker buildColorPicker()
	{
		colorPicker = new ColorPicker(Color.BLACK);
		colorPicker.valueProperty().addListener(new ChangeListener<Color>(){

			@Override
			public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue)
			{
				int index = stopList.getSelectionModel().getSelectedIndex();
				if(index < 0)
				{
					/*Nothing selected*/
					return;
				}
				stopList.getItems().set(index, new Stop(stopList.getItems().get(index).getOffset(), colorPicker.getValue()));
			}
		});
		return colorPicker;
	}
	
	private ArrayList<Stop> createGradientStops()
	{
		
		ArrayList<Stop> returnValue = new ArrayList<Stop>(Arrays.asList(stopList.getItems().toArray(new Stop[1])));
		return returnValue;
	}
	
	private ChoiceBox<CustomColorFunction> buildColorChoiceBox()
	{
		colorChoiceBox = new ChoiceBox<CustomColorFunction>(savedColors);
		colorChoiceBox.setValue(gui.mainCalculator.getColorFunction());
		
		colorChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				
				CustomColorFunction colorFunction = (CustomColorFunction)colorChoiceBox.getItems().get(newValue.intValue());
				rangeField.setText(colorFunction.getRange()+"");
				
				stopList.getItems().remove(0, stopList.getItems().size());
				for(Stop stop : colorFunction.getStops())
				{
					stopList.getItems().add(stop);
				}
				
				
			}
		});
		return colorChoiceBox;
	}
	
	private TextField buildColorPositionField()
	{
		colorPositionField = new TextField("0.5");
		colorPositionField.setOnMouseDragEntered(ae->{
			colorPositionField.requestFocus();
		});
		colorPositionField.setOnKeyReleased(ae ->{
			if(ae.getCode() == KeyCode.ENTER)
			{
				double val;
				try
				{
					val = Double.parseDouble(colorPositionField.getText());
				}
				catch(NumberFormatException nfe)
				{
					colorPositionField.setStyle("-fx-background-color:red");
					return;
				}
				if(val >= 0 && val<=1)
				{
					Platform.runLater(()->{
						colorPositionField.setStyle("-fx-background-color:white");
						colorPositionSlider.setValue(val);
					});
				}
				else
				{
					colorPositionField.setStyle("-fx-background-color:red");
				}
			}
		});
		return colorPositionField;
	}
	
	private Slider buildColorPositionSlider()
	{
		colorPositionSlider = new Slider(0,1,0.5);
		colorPositionSlider.valueProperty().addListener(new ChangeListener<Number>(){

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				Platform.runLater(()->{
					colorPositionField.setStyle("-fx-background-color:white");
					colorPositionField.setText(newValue.doubleValue() + "");
					int index = stopList.getSelectionModel().getSelectedIndex();
					if(index < 0)
					{
						/*Nothing selected*/
						return;
					}
					stopList.getItems().set(index, new Stop(colorPositionSlider.getValue(), stopList.getItems().get(index).getColor()));
				});
			}
		});
		return colorPositionSlider;
	}
	
	private ListView<Stop> buildStopList()
	{
		stopList = new ListView<Stop>();
		stopList.setCellFactory(new Callback<ListView<Stop>, ListCell<Stop>>(){
			@Override
			public ListCell<Stop> call(ListView<Stop> param) {
				return new CustomListCell();
			}
			
		});
		stopList.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Stop stop = stopList.getSelectionModel().getSelectedItem();
				if(stop == null)
				{
					return;
				}
				Platform.runLater(()->{
					colorPicker.setValue(stop.getColor());
					colorPositionSlider.setValue(stop.getOffset());
				});
				
			}
			
		});
		stopList.getItems().addListener(new ListChangeListener<Stop>(){

			@Override
			public void onChanged(ListChangeListener.Change<? extends Stop> c) {
				gradientRectangle.setFill(new LinearGradient(0,0.5,1,0.5,true, CycleMethod.NO_CYCLE, createGradientStops()));
				
			}
			
		});
		return stopList;
	}
	
	private Button buildSaveColorButton()
	{
		Button saveColorButton = new Button("Save Color");
		saveColorButton.setOnAction(ae ->{
			if(!validateForSaveColor())
			{
				return;
			}
			
			saveColor();
			
		});
		return saveColorButton;
	}
	
	/**
	 * 
	 * @return true if the color was actually saved.
	 */
	private boolean saveColor()
	{
		String name;
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Save Color");
		dialog.setContentText("Enter a name:");
		Optional<String> result = dialog.showAndWait();
		if(result.isPresent())
		{
			name = result.get();
			for(CustomColorFunction c : colorChoiceBox.getItems())
			{
				if(c.getName().equals(name))
				{
					Alert alert = new Alert(AlertType.ERROR);
					alert.setContentText("That name already exists");
					alert.show();
					return false;
				}
			}
		}
		else
		{
			return false;
		}
		rangeField.setStyle("-fx-background-color:white");
		CustomColorFunction color = new CustomColorFunction(new ArrayList<Stop>(stopList.getItems()),Integer.parseInt(rangeField.getText()), name);
		savedColors.add(color);
		return true;
	}
	
	private boolean validateForSaveColor()
	{
		TreeSet<Double> set = new TreeSet<Double>();
		for(Stop stop : stopList.getItems())
		{
			/*Checks for duplicates*/
			if(!set.add(stop.getOffset()))
			{
				Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText("That color has the same position as another!");
				alert.show();
				return false;
			}
		}
		
		
		if(stopList.getItems().size()<2)
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("There must be atleast two colors positioned at 0 and 1 (beginning and end)");
			alert.show();
			return false;
		}
		
		
		
		boolean validStart = false;
		boolean validEnd = false;
		for(Stop stop : stopList.getItems())
		{
			if(stop.getOffset() == 0)
			{
				validStart = true;
			}
			if(stop.getOffset() == 1)
			{
				validEnd = true;
			}
		}
		if(!(validStart && validEnd))
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("There must be atleast two colors positioned at 0 and 1 (beginning and end)");
			alert.show();
			return false;
		}
		
		
		
		int val;
		try
		{
			val = Integer.parseInt(rangeField.getText());
		}
		catch(NumberFormatException nfe)
		{
			rangeField.setStyle("-fx-background-color:red");
			return false;
		}
		if(val <= 1)
		{
			rangeField.setStyle("-fx-background-color:red");
			return false;
		}
		return true;
	}
	
	private Button buildRemoveStopButton()
	{
		Button removeStopButton = new Button("Remove Color");
		removeStopButton.setOnAction(ae->{
			Stop stop = stopList.getSelectionModel().getSelectedItem();
			stopList.getItems().remove(stop);
			
		});
		return removeStopButton;
	}
	
	private Button buildAddStopButton()
	{
		Button addStopButton = new Button("Add Color");
		addStopButton.setOnAction(ae->{
			Stop stopToAdd = new Stop(colorPositionSlider.getValue(), colorPicker.getValue());
			
			
			stopList.getItems().add(stopToAdd);
			stopList.getSelectionModel().select(stopList.getItems().size()-1);
			
		});
		return addStopButton;
	}

	private Tab buildUploadTab()
	{
		Tab tab = new Tab("Upload");
		tab.setClosable(false);
		GridPane uploadGrid= new GridPane();
		uploadGrid.setVgap(10);
		uploadGrid.setHgap(10);
		uploadGrid.setPadding(new Insets(30,30,30,30));
		
		uploadNameField = new TextField();
		uploadAuthorField = new TextField();
		uploadDescriptionArea = new TextArea();
		
		uploadGrid.add(new Label("Name:"), 0, 0);
		uploadGrid.add(uploadNameField,0,1);
		uploadGrid.add(new Label("Author (leave blank for anonymous):"), 0, 2);
		uploadGrid.add(uploadAuthorField, 0, 3);
		uploadGrid.add(new Label("Description:"), 0, 4);
		uploadGrid.add(uploadDescriptionArea, 0, 5);
		uploadGrid.add(buildUploadButton(), 0, 6);
		
		
		
		uploadGrid.add(buildUploadTypeChoiceBox(), 1, 0);
		uploadGrid.add(new Label("Upload Region: "), 1, 1);
		uploadGrid.add(buildUploadRegionsChoiceBox(), 1, 2);
		uploadGrid.add(new Label("Upload Color: "), 1, 3);
		uploadGrid.add(buildUploadColorsChoiceBox(), 1, 4);
		tab.setContent(uploadGrid);
		
		uploadColorsChoiceBox.getSelectionModel().select(0);
		uploadRegionsChoiceBox.getSelectionModel().select(0);
		uploadTypeChoiceBox.getSelectionModel().select(0);
		return tab;
	}
	
	private ChoiceBox<SavedRegion> buildUploadRegionsChoiceBox(){
		uploadRegionsChoiceBox = new ChoiceBox<SavedRegion>(savedRegions);
		uploadRegionsChoiceBox.setConverter(new StringConverter<SavedRegion>(){

			@Override
			public String toString(SavedRegion sr)
			{
				return sr.name;
			}

			@Override
			public SavedRegion fromString(String string) {
				return null;
			}
			
		});
		uploadRegionsChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SavedRegion>(){

			@Override
			public void changed(ObservableValue<? extends SavedRegion> observable, SavedRegion oldValue,
					SavedRegion newValue) {
				if(newValue != null)
				uploadNameField.setText(newValue.name);
			}
		});
		uploadRegionsChoiceBox.setDisable(true);
		return uploadRegionsChoiceBox;
	}
	
	private ChoiceBox<CustomColorFunction> buildUploadColorsChoiceBox(){
		uploadColorsChoiceBox = new ChoiceBox<CustomColorFunction>(savedColors);
		uploadColorsChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CustomColorFunction>(){

			@Override
			public void changed(ObservableValue<? extends CustomColorFunction> observable, CustomColorFunction oldValue,
					CustomColorFunction newValue) {
				uploadNameField.setText(newValue.getName());
			}
		});
		uploadColorsChoiceBox.setDisable(true);
		return uploadColorsChoiceBox;
	}
	
	private Button buildUploadButton()
	{
		Button uploadButton = new Button("Upload Image");
		uploadButton.setOnAction(e->{
			String type = uploadTypeChoiceBox.getSelectionModel().getSelectedItem();
			if(type.equals("Image")){
				showUploadImageDialog();
			} else if(type.equals("Region")) {
				showUploadRegionDialog();
			} else if(type.equals("Color")) {
				showUploadColorDialog();
			} else {
				System.out.println("UNKNOWN TYPE");
			}
		});
		return uploadButton;
	}
	
	private void showUploadImageDialog() {
		System.out.println(checkUploadValues());
		if(!checkUploadValues()){
			return;
		}
		new ImageSaverDialog(gui).showSaverDialog(false, ()->{
			uploadImage();
		});
	}
	
	private boolean checkUploadValues() {
		if(uploadNameField.getText() == null){
			return false;
		}
		if((uploadNameField.getText().equals("") || uploadDescriptionArea.getText().equals(""))){
			return false;
		}
		if(uploadTypeChoiceBox.getSelectionModel().getSelectedItem().equals("Region"))
		{
			if(uploadRegionsChoiceBox.getSelectionModel().getSelectedItem() == null)
			{
				return false;
			}
		}
		else if(uploadTypeChoiceBox.getSelectionModel().getSelectedItem().equals("Color"))
		{
			if(uploadColorsChoiceBox.getSelectionModel().getSelectedItem() == null)
			{
				return false;
			}
		}
		return true;
	}
	
	private void uploadRegion(){
		SavedRegion sr = uploadRegionsChoiceBox.getSelectionModel().getSelectedItem();
		if(existsInDatabase(sr)>=0)
		{
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setContentText("That region already exists in the database!");
			alert.show();
			return;
		}
		UploadDialog dialog = new UploadDialog();
		Platform.runLater(()->{
			dialog.show();
			dialog.getResponseLabel().setText("Uploading...");
		});
		HttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost("http://www.ezstein.xyz/regionUploader.php");
		HttpResponse response = null;
		ObjectOutputStream fileOut = null, fileOut2 = null;
		
		try
		{
			fileOut = new ObjectOutputStream(new FileOutputStream(Locator.locateUniqueFile("tmp/region/uploadFile.txt").toFile()));
			fileOut.writeObject(sr);
			fileOut2 = new ObjectOutputStream(new FileOutputStream(Locator.locateUniqueFile("tmp/color/uploadFile.txt").toFile()));
			fileOut2.writeObject(sr.colorFunction);
			
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			try {
				if(fileOut != null)
				{
					fileOut.close();
				}
				if(fileOut2 != null)
				{
					fileOut2.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		BufferedReader in = null;
		File colorFile = null;
		File regionFile = null;
		try {
			regionFile = Locator.getUniqueFile("tmp/region").toFile();
			colorFile = Locator.getUniqueFile("tmp/color").toFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int idOfReplica;
		HttpEntity entity;
		if((idOfReplica = existsInDatabase(sr.colorFunction))>=0)
		{
			entity = MultipartEntityBuilder.create()
					.addTextBody("pass", "uploaderPassword")
					.addTextBody("Name", uploadNameField.getText())
					.addTextBody("Author", (uploadAuthorField.getText().equals("") ? "Anonymous" : uploadAuthorField.getText()))
					.addTextBody("Description", uploadDescriptionArea.getText())
					.addTextBody("RegionHashCode", sr.hashCode() + "")
					.addBinaryBody("region", regionFile, ContentType.TEXT_PLAIN, regionFile.getName())
					.addTextBody("Replica", "true")
					.addTextBody("ColorLink", idOfReplica + "")
					.build();
		}
		else
		{
			entity = MultipartEntityBuilder.create()
					.addTextBody("pass", "uploaderPassword")
					.addTextBody("Name", uploadNameField.getText())
					.addTextBody("Author", (uploadAuthorField.getText().equals("") ? "Anonymous" : uploadAuthorField.getText()))
					.addTextBody("Description", uploadDescriptionArea.getText())
					.addTextBody("RegionHashCode", sr.hashCode() + "")
					.addBinaryBody("region", regionFile, ContentType.TEXT_PLAIN, regionFile.getName())
					.addTextBody("ColorName", uploadRegionsChoiceBox.getSelectionModel().getSelectedItem().colorFunction.getName())
					.addTextBody("ColorHashCode", sr.colorFunction.hashCode() + "")
					.addBinaryBody("color", colorFile, ContentType.TEXT_PLAIN, colorFile.getName())
					.addTextBody("Replica", "false")
					.build();
		}
		post.setEntity(entity);
		try{
			response = client.execute(post);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String s = "", input = "";
			while((input = in.readLine()) !=null)
			{
				s += input + "\n";
			}
			final String s1 = s;
			Platform.runLater(()->{
				dialog.getResponseLabel().setText(s1);
				dialog.enableClose();
			});
		}
		catch(ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally{
			try {
				if(in !=null)
				{
					in.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void uploadColor()
	{
		if(existsInDatabase(uploadColorsChoiceBox.getSelectionModel().getSelectedItem())>=0)
		{
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setContentText("That color already exists in the database!");
			alert.show();
			return;
		}
		UploadDialog dialog = new UploadDialog();
		Platform.runLater(()->{
			dialog.show();
			dialog.getResponseLabel().setText("Uploading...");
		});
		
		HttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost("http://www.ezstein.xyz/colorUploader.php");
		HttpResponse response = null;
		ObjectOutputStream fileOut = null;
		try
		{
			fileOut = new ObjectOutputStream(new FileOutputStream(Locator.locateUniqueFile("tmp/color/uploadFile.txt").toFile()));
			fileOut.writeObject(uploadColorsChoiceBox.getSelectionModel().getSelectedItem());
			
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			try {
				if(fileOut != null)
				{
					fileOut.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		BufferedReader in = null;
		File colorFile = null;
		try {
			colorFile = Locator.getUniqueFile("tmp/color").toFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HttpEntity entity = MultipartEntityBuilder.create()
				.addTextBody("pass", "uploaderPassword")
				.addTextBody("Name", uploadNameField.getText())
				.addTextBody("Author", (uploadAuthorField.getText().equals("") ? "Anonymous" : uploadAuthorField.getText()))
				.addTextBody("Description", uploadDescriptionArea.getText())
				.addTextBody("ColorHashCode", uploadColorsChoiceBox.getSelectionModel().getSelectedItem().hashCode() + "")
				.addBinaryBody("color", colorFile, ContentType.TEXT_PLAIN, colorFile.getName()).build();
		post.setEntity(entity);
		try{
			response = client.execute(post);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String s = "", input = "";
			while((input = in.readLine()) !=null)
			{
				s += input + "\n";
			}
			final String s1 = s;
			Platform.runLater(()->{
				dialog.getResponseLabel().setText(s1);
				dialog.enableClose();
			});
		}
		catch(ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally{
			try {
				if(in !=null)
				{
					in.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void uploadImage(){
		UploadDialog dialog = new UploadDialog();
		Platform.runLater(()->{
			dialog.show();
			dialog.getResponseLabel().setText("Uploading...");
		});
		
		HttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost("http://www.ezstein.xyz/imageUploader.php");
		HttpResponse response = null;
		BufferedReader in = null;
		

		ObjectOutputStream fileOut = null, fileOut2 = null;
		SavedRegion sr = new SavedRegion("NO NAME", gui.autoIterations, gui.iterations,
				gui.precision, gui.threadCount, gui.currentRegion,gui.arbitraryPrecision,
				gui.julia,gui.juliaSeed,gui.mainCalculator.getColorFunction());
		try
		{
			fileOut = new ObjectOutputStream(new FileOutputStream(Locator.locateUniqueFile("tmp/color/uploadFile.txt").toFile()));
			fileOut.writeObject(gui.mainCalculator.getColorFunction());
			fileOut2 = new ObjectOutputStream(new FileOutputStream(Locator.locateUniqueFile("tmp/region/uploadFile.txt").toFile()));
			fileOut2.writeObject(sr);
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			try {
				if(fileOut != null)
				{
					fileOut.close();
				}
				if(fileOut2 != null)
				{
					fileOut2.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		File imageFile = null, regionFile =null, colorFile=null;
		try
		{
			
			imageFile = Locator.getUniqueFile("tmp/image").toFile();
			regionFile = Locator.getUniqueFile("tmp/region").toFile();
			colorFile = Locator.getUniqueFile("tmp/color").toFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String name = imageFile.getName();
		String imageType = name.substring(name.lastIndexOf(".") + 1);
		
		
		int idOfReplicaRegion, idOfReplicaColor;
		idOfReplicaColor = existsInDatabase(sr.colorFunction);
		idOfReplicaRegion = existsInDatabase(sr);
		HttpEntity entity = null;
		if(idOfReplicaColor <0 && idOfReplicaRegion <0)
		{
			//no replica
			entity = MultipartEntityBuilder.create()
					.addTextBody("pass", "uploaderPassword")
					.addTextBody("Name", uploadNameField.getText())
					.addTextBody("Author", (uploadAuthorField.getText().equals("") ? "Anonymous" : uploadAuthorField.getText()))
					.addTextBody("Description", uploadDescriptionArea.getText())
					.addTextBody("SetType", gui.julia?"J":"M")
					.addBinaryBody("image", imageFile, ContentType.create("image/" + imageType), imageFile.getName())
					.addTextBody("RegionHashCode", sr.hashCode() + "")
					.addBinaryBody("region", regionFile, ContentType.TEXT_PLAIN, regionFile.getName())
					.addTextBody("ColorName", gui.mainCalculator.getColorFunction().getName())
					.addTextBody("ColorHashCode", gui.mainCalculator.getColorFunction().hashCode() + "")
					.addBinaryBody("color", colorFile, ContentType.TEXT_PLAIN, colorFile.getName())
					.addTextBody("ReplicaRegion", "false")
					.addTextBody("ReplicaColor", "false")
					.build();
		} else if(idOfReplicaColor >=0 && idOfReplicaRegion <0)
		{
			//color replica
			entity = MultipartEntityBuilder.create()
					.addTextBody("pass", "uploaderPassword")
					.addTextBody("Name", uploadNameField.getText())
					.addTextBody("Author", (uploadAuthorField.getText().equals("") ? "Anonymous" : uploadAuthorField.getText()))
					.addTextBody("Description", uploadDescriptionArea.getText())
					.addTextBody("SetType", gui.julia?"J":"M")
					.addBinaryBody("image", imageFile, ContentType.create("image/" + imageType), imageFile.getName())
					.addTextBody("RegionHashCode", sr.hashCode() + "")
					.addBinaryBody("region", regionFile, ContentType.TEXT_PLAIN, regionFile.getName())
					.addTextBody("LinkedColor", idOfReplicaColor + "")
					.addTextBody("ReplicaRegion", "false")
					.addTextBody("ReplicaColor", "true")
					.build();
		} else if(idOfReplicaColor <0 && idOfReplicaRegion >=0)
		{
			//region replica
			entity = MultipartEntityBuilder.create()
					.addTextBody("pass", "uploaderPassword")
					.addTextBody("Name", uploadNameField.getText())
					.addTextBody("Author", (uploadAuthorField.getText().equals("") ? "Anonymous" : uploadAuthorField.getText()))
					.addTextBody("Description", uploadDescriptionArea.getText())
					.addTextBody("SetType", gui.julia?"J":"M")
					.addBinaryBody("image", imageFile, ContentType.create("image/" + imageType), imageFile.getName())
					.addTextBody("LinkedRegion", idOfReplicaRegion + "")
					.addTextBody("ReplicaRegion", "true")
					.addTextBody("ReplicaColor", "false")
					.addTextBody("ColorName", gui.mainCalculator.getColorFunction().getName())
					.addTextBody("ColorHashCode", gui.mainCalculator.getColorFunction().hashCode() + "")
					.addBinaryBody("color", colorFile, ContentType.TEXT_PLAIN, colorFile.getName())
					.build();
		} else if(idOfReplicaColor >=0 && idOfReplicaRegion >=0)
		{
			//both replica
			entity = MultipartEntityBuilder.create()
					.addTextBody("pass", "uploaderPassword")
					.addTextBody("Name", uploadNameField.getText())
					.addTextBody("Author", (uploadAuthorField.getText().equals("") ? "Anonymous" : uploadAuthorField.getText()))
					.addTextBody("Description", uploadDescriptionArea.getText())
					.addTextBody("SetType", gui.julia?"J":"M")
					.addBinaryBody("image", imageFile, ContentType.create("image/" + imageType), imageFile.getName())
					.addTextBody("LinkedRegion", idOfReplicaRegion + "")
					.addTextBody("LinkedColor", idOfReplicaColor + "")
					.addTextBody("ReplicaColor", "true")
					.addTextBody("ReplicaRegion", "true")
					.build();
		} 
		post.setEntity(entity);
		try {
			response = client.execute(post);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			
			String s = "", input = "";
			while((input = in.readLine()) !=null)
			{
				s += input + "\n";
			}
			final String s1 = s;
			Platform.runLater(()->{
				dialog.getResponseLabel().setText(s1);
				dialog.enableClose();
			});
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			
				try {
					if(in!=null){
						in.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
	}
	
	private void showUploadRegionDialog() {
		System.out.println(checkUploadValues());
		if(!checkUploadValues()){
			return;
		}
		uploadRegion();
	}
	
	private void showUploadColorDialog() {
		System.out.println(checkUploadValues());
		if(!checkUploadValues()){
			return;
		}
		uploadColor();
	}
	
	private ChoiceBox<String> buildUploadTypeChoiceBox()
	{
		uploadTypeChoiceBox =
				new ChoiceBox<String>(FXCollections.observableList(
						new ArrayList<String>(Arrays.asList("Image","Region","Color"))));
		uploadTypeChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if(uploadTypeChoiceBox.getItems().get(newValue.intValue()).equals("Region")){
					uploadNameField.setDisable(true);
					uploadNameField.setText(
							uploadRegionsChoiceBox.getSelectionModel().getSelectedItem()!= null ? uploadRegionsChoiceBox.getSelectionModel().getSelectedItem().name : "");
					uploadRegionsChoiceBox.setDisable(false);
					uploadColorsChoiceBox.setDisable(true);
					
				} else if(uploadTypeChoiceBox.getItems().get(newValue.intValue()).equals("Color")){
					uploadNameField.setDisable(true);
					uploadNameField.setText(uploadColorsChoiceBox.getSelectionModel().getSelectedItem()!=null ? uploadColorsChoiceBox.getSelectionModel().getSelectedItem().getName():"");
					uploadRegionsChoiceBox.setDisable(true);
					uploadColorsChoiceBox.setDisable(false);
				} if(uploadTypeChoiceBox.getItems().get(newValue.intValue()).equals("Image")){
					uploadNameField.setDisable(false);
					uploadNameField.setText("");
					uploadRegionsChoiceBox.setDisable(true);
					uploadColorsChoiceBox.setDisable(true);
					
				}
			}
		});
		//uploadTypeChoiceBox.getSelectionModel().select(0);
		return  uploadTypeChoiceBox;
	}
	
	private Tab buildDownloadTab(){
		Tab tab = new Tab("Download");
		tab.setClosable(false);
		GridPane downloadGrid = new GridPane();
		downloadGrid.setVgap(10);
		downloadGrid.setHgap(10);
		downloadGrid.setPadding(new Insets(30,30,30,30));
		
		tables = new TabPane();
		tables.getTabs().add(buildImageTableTab());
		tables.getTabs().add(buildRegionTableTab());
		tables.getTabs().add(buildColorTableTab());
		downloadGrid.add(new Label("Downloads:"), 0, 0);
		downloadGrid.add(tables, 0, 1, 2, 5);
		downloadGrid.add(buildDownloadButton(), 0, 6);
		tab.setContent(downloadGrid);
		return tab;
	}
	
	private Tab buildImageTableTab(){
		Tab tab = new Tab("Images");
		Group layout = new Group();
		layout.getChildren().add(buildDownloadImageTable());
		tab.setClosable(false);
		tab.setContent(layout);
		return tab;
	}
	
	private Tab buildRegionTableTab(){
		Tab tab = new Tab("Regions");
		Group layout = new Group();
		layout.getChildren().add(buildDownloadRegionTable());
		tab.setClosable(false);
		tab.setContent(layout);
		return tab;
	}
	
	private Tab buildColorTableTab(){
		Tab tab = new Tab("Colors");
		Group layout = new Group();
		layout.getChildren().add(buildDownloadColorTable());
		tab.setClosable(false);
		tab.setContent(layout);
		return tab;
	}
	
	private TableView<ColorRow> buildDownloadColorTable(){
		downloadColorTable = new TableView<ColorRow>();
		
		TableColumn<ColorRow, String> idColumn = new TableColumn<ColorRow, String>("ID");
		TableColumn<ColorRow, String> nameColumn = new TableColumn<ColorRow, String>("Name");
		TableColumn<ColorRow, String> authorColumn = new TableColumn<ColorRow, String>("Author");
		TableColumn<ColorRow, String> descriptionColumn = new TableColumn<ColorRow, String>("Description");
		TableColumn<ColorRow, String> fileColumn = new TableColumn<ColorRow, String>("File");
		TableColumn<ColorRow, String> sizeColumn = new TableColumn<ColorRow, String>("Size");
		TableColumn<ColorRow, String> dateColumn = new TableColumn<ColorRow, String>("Date");
		TableColumn<ColorRow, String> hashCodeColumn = new TableColumn<ColorRow, String>("HashCode");
		idColumn.setCellValueFactory(new PropertyValueFactory<ColorRow, String>("id"));
		nameColumn.setCellValueFactory(new PropertyValueFactory<ColorRow, String>("name"));
		authorColumn.setCellValueFactory(new PropertyValueFactory<ColorRow, String>("author"));
		descriptionColumn.setCellValueFactory(new PropertyValueFactory<ColorRow, String>("description"));
		sizeColumn.setCellValueFactory(new PropertyValueFactory<ColorRow, String>("size"));
		dateColumn.setCellValueFactory(new PropertyValueFactory<ColorRow, String>("date"));
		fileColumn.setCellValueFactory(new PropertyValueFactory<ColorRow, String>("file"));
		hashCodeColumn.setCellValueFactory(new PropertyValueFactory<ColorRow, String>("hashCode"));
		
		downloadColorTable.getColumns().addAll(idColumn,nameColumn,authorColumn,descriptionColumn,fileColumn,
				sizeColumn,dateColumn,hashCodeColumn);
		
		return downloadColorTable;
	}
	
	public class ColorRow
	{
		private final IntegerProperty id, size, date, hashCode;
		private final StringProperty name, author, description, file;
		
		/**
		 * 
		 * @param id
		 * @param name
		 * @param author
		 * @param description
		 * @param file
		 * @param size
		 * @param date
		 */
		public ColorRow(int id, String name, String author, String description, String file, int size, int date, int hashCode)
		{
			this.id = new SimpleIntegerProperty(id);
			this.size = new SimpleIntegerProperty(size);
			this.date = new SimpleIntegerProperty(date);
			this.name = new SimpleStringProperty(name);
			this.author = new SimpleStringProperty(author);
			this.description = new SimpleStringProperty(description);
			this.file = new SimpleStringProperty(file);
			this.hashCode = new SimpleIntegerProperty(hashCode);
		}
		
		public final IntegerProperty getIdProperty()
		{
			return id;
		}
		public final int getId()
		{
			return id.get();
		}
		public final void setId(int val)
		{
			id.set(val);
		}
		
		public final IntegerProperty getSizeProperty()
		{
			return size;
		}
		public final int getSize()
		{
			return size.get();
		}
		public final void setSize(int val)
		{
			size.set(val);
		}
		
		public final IntegerProperty getDateProperty()
		{
			return date;
		}
		public final int getDate()
		{
			return date.get();
		}
		public final void setDate(int val)
		{
			date.set(val);
		}
		
		public final StringProperty getNameProperty()
		{
			return name;
		}
		public final String getName()
		{
			return name.get();
		}
		public final void setName(String val)
		{
			name.set(val);
		}
		
		public final StringProperty getAuthorProperty()
		{
			return author;
		}
		public final String getAuthor()
		{
			return author.get();
		}
		public final void setAuthor(String val)
		{
			author.set(val);
		}
		
		public final StringProperty getDescriptionProperty()
		{
			return description;
		}
		public final String getDescription()
		{
			return description.get();
		}
		public final void setDescription(String val)
		{
			description.set(val);
		}
		
		public final StringProperty getFileProperty()
		{
			return file;
		}
		public final String getFile()
		{
			return file.get();
		}
		public final void setFile(String val)
		{
			file.set(val);
		}
		
		
		public final IntegerProperty getHashCodeProperty()
		{
			return hashCode;
		}
		public final int getHashCode()
		{
			return hashCode.get();
		}
		public final void setHashCode(int val)
		{
			hashCode.set(val);
		}
		
		@Override
		public boolean equals(Object o)
		{
			if(o==null)
			{
				return false;
			}
			if(o==this)
			{
				return true;
			}
			if(o instanceof ColorRow)
			{
				ColorRow row = (ColorRow) o;
				if(row.getHashCode()==hashCode.get()
				&& row.getFile().equals(file.get())
				&& row.getDescription().equals(description.get())
				&& row.getAuthor().equals(author.get())
				&& row.getName().equals(name.get())
				&& row.getDate() == date.get()
				&& row.getSize() == size.get()
				&& row.getId() == id.get())
				{
					return true;
				}
			}
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return 100*hashCode.get() + file.get().hashCode() +
					description.get().hashCode() + author.get().hashCode() +
					name.get().hashCode() + 11*date.get() + 15*size.get() + 18*id.get();
		}
	}
	
	private TableView<RegionRow> buildDownloadRegionTable(){
		downloadRegionTable = new TableView<RegionRow>();
		
		TableColumn<RegionRow, String> idColumn = new TableColumn<RegionRow, String>("ID");
		TableColumn<RegionRow, String> nameColumn = new TableColumn<RegionRow, String>("Name");
		TableColumn<RegionRow, String> authorColumn = new TableColumn<RegionRow, String>("Author");
		TableColumn<RegionRow, String> descriptionColumn = new TableColumn<RegionRow, String>("Description");
		TableColumn<RegionRow, String> fileColumn = new TableColumn<RegionRow, String>("File");
		TableColumn<RegionRow, String> sizeColumn = new TableColumn<RegionRow, String>("Size");
		TableColumn<RegionRow, String> dateColumn = new TableColumn<RegionRow, String>("Date");
		TableColumn<RegionRow, String> hashCodeColumn = new TableColumn<RegionRow, String>("HashCode");
		idColumn.setCellValueFactory(new PropertyValueFactory<RegionRow, String>("id"));
		nameColumn.setCellValueFactory(new PropertyValueFactory<RegionRow, String>("name"));
		authorColumn.setCellValueFactory(new PropertyValueFactory<RegionRow, String>("author"));
		descriptionColumn.setCellValueFactory(new PropertyValueFactory<RegionRow, String>("description"));
		sizeColumn.setCellValueFactory(new PropertyValueFactory<RegionRow, String>("size"));
		dateColumn.setCellValueFactory(new PropertyValueFactory<RegionRow, String>("date"));
		fileColumn.setCellValueFactory(new PropertyValueFactory<RegionRow, String>("file"));
		hashCodeColumn.setCellValueFactory(new PropertyValueFactory<RegionRow, String>("hashCode"));
		
		downloadRegionTable.getColumns().addAll(idColumn,nameColumn,authorColumn,descriptionColumn,fileColumn,
				sizeColumn,dateColumn,hashCodeColumn);
		
		return downloadRegionTable;
	}
	
	/**
	 * Returns -1 if it does not exist and the id of the color that is the replica if it does.
	 * @param ccf
	 * @return
	 */
	private int existsInDatabase(CustomColorFunction ccf)
	{
		try
		{
			ResultSet set = stmt.executeQuery("SELECT ID, HashCode FROM Colors;");
			while(set.next())
			{
				if(set.getInt("HashCode") == ccf.hashCode())
				{
					return set.getInt("ID");
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Returns -1 if it does not exist and the id of the color that is the replica if it does.
	 * @param sr
	 * @return
	 */
	private int existsInDatabase(SavedRegion sr)
	{
		try
		{
			ResultSet set = stmt.executeQuery("SELECT ID, HashCode FROM Regions;");
			while(set.next())
			{
				if(set.getInt("HashCode") == sr.hashCode())
				{
					return set.getInt("ID");
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	private boolean colorExistsLocally(int hashCode)
	{
		for(CustomColorFunction ccf: savedColors)
		{
			if(ccf.hashCode()==hashCode)
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean regionExistsLocally(int hashCode)
	{
		for(SavedRegion sr: savedRegions)
		{
			if(sr.hashCode()==hashCode)
			{
				return true;
			}
		}
		return false;
	}
	
	public class RegionRow
	{
		private final IntegerProperty id, size, date, hashCode;
		private final StringProperty name, author, description, file;
		
		/**
		 * 
		 * @param id
		 * @param name
		 * @param author
		 * @param description
		 * @param file
		 * @param size
		 * @param date
		 */
		public RegionRow(int id, String name, String author, String description, String file, int size, int date, int hashCode)
		{
			this.id = new SimpleIntegerProperty(id);
			this.size = new SimpleIntegerProperty(size);
			this.date = new SimpleIntegerProperty(date);
			this.name = new SimpleStringProperty(name);
			this.author = new SimpleStringProperty(author);
			this.description = new SimpleStringProperty(description);
			this.file = new SimpleStringProperty(file);
			this.hashCode = new SimpleIntegerProperty(hashCode);
		}
		
		public final IntegerProperty getIdProperty()
		{
			return id;
		}
		public final int getId()
		{
			return id.get();
		}
		public final void setId(int val)
		{
			id.set(val);
		}
		
		public final IntegerProperty getSizeProperty()
		{
			return size;
		}
		public final int getSize()
		{
			return size.get();
		}
		public final void setSize(int val)
		{
			size.set(val);
		}
		
		public final IntegerProperty getDateProperty()
		{
			return date;
		}
		public final int getDate()
		{
			return date.get();
		}
		public final void setDate(int val)
		{
			date.set(val);
		}
		
		public final StringProperty getNameProperty()
		{
			return name;
		}
		public final String getName()
		{
			return name.get();
		}
		public final void setName(String val)
		{
			name.set(val);
		}
		
		public final StringProperty getAuthorProperty()
		{
			return author;
		}
		public final String getAuthor()
		{
			return author.get();
		}
		public final void setAuthor(String val)
		{
			author.set(val);
		}
		
		public final StringProperty getDescriptionProperty()
		{
			return description;
		}
		public final String getDescription()
		{
			return description.get();
		}
		public final void setDescription(String val)
		{
			description.set(val);
		}
		
		public final StringProperty getFileProperty()
		{
			return file;
		}
		public final String getFile()
		{
			return file.get();
		}
		public final void setFile(String val)
		{
			file.set(val);
		}
		
		public final IntegerProperty getHashCodeProperty()
		{
			return hashCode;
		}
		public final int getHashCode()
		{
			return hashCode.get();
		}
		public final void setHashCode(int val)
		{
			hashCode.set(val);
		}
	
		
		@Override
		public boolean equals(Object o)
		{
			if(o==null)
			{
				return false;
			}
			if(o==this)
			{
				return true;
			}
			if(o instanceof RegionRow)
			{
				RegionRow row = (RegionRow) o;
				if(row.getHashCode()==hashCode.get()
				&& row.getFile().equals(file.get())
				&& row.getDescription().equals(description.get())
				&& row.getAuthor().equals(author.get())
				&& row.getName().equals(name.get())
				&& row.getDate() == date.get()
				&& row.getSize() == size.get()
				&& row.getId() == id.get())
				{
					return true;
				}
			}
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return 100*hashCode.get() + file.get().hashCode() +
					description.get().hashCode() + author.get().hashCode() +
					name.get().hashCode() + 11*date.get() + 15*size.get() + 18*id.get();
		}
	}
	
	private TableView<ImageRow> buildDownloadImageTable(){
		downloadImageTable = new TableView<ImageRow>();
		downloadImageTable.setEditable(false);
		TableColumn<ImageRow, String> idColumn = new TableColumn<ImageRow, String>("ID");
		TableColumn<ImageRow, String> nameColumn = new TableColumn<ImageRow, String>("Name");
		TableColumn<ImageRow, String> authorColumn = new TableColumn<ImageRow, String>("Author");
		TableColumn<ImageRow, String> descriptionColumn = new TableColumn<ImageRow, String>("Description");
		TableColumn<ImageRow, String> fileColumn = new TableColumn<ImageRow, String>("File");
		TableColumn<ImageRow, String> setTypeColumn = new TableColumn<ImageRow, String>("SetType");
		TableColumn<ImageRow, String> widthColumn = new TableColumn<ImageRow, String>("Width");
		TableColumn<ImageRow, String> heightColumn = new TableColumn<ImageRow, String>("Height");
		TableColumn<ImageRow, String> sizeColumn = new TableColumn<ImageRow, String>("Size");
		TableColumn<ImageRow, String> dateColumn = new TableColumn<ImageRow, String>("Date");
		TableColumn<ImageRow, String> fileTypeColumn = new TableColumn<ImageRow, String>("FileType");
		idColumn.setCellValueFactory(new PropertyValueFactory<ImageRow, String>("id"));
		nameColumn.setCellValueFactory(new PropertyValueFactory<ImageRow, String>("name"));
		authorColumn.setCellValueFactory(new PropertyValueFactory<ImageRow, String>("author"));
		descriptionColumn.setCellValueFactory(new PropertyValueFactory<ImageRow, String>("description"));
		setTypeColumn.setCellValueFactory(new PropertyValueFactory<ImageRow, String>("setType"));
		fileTypeColumn.setCellValueFactory(new PropertyValueFactory<ImageRow, String>("fileType"));
		sizeColumn.setCellValueFactory(new PropertyValueFactory<ImageRow, String>("size"));
		widthColumn.setCellValueFactory(new PropertyValueFactory<ImageRow, String>("width"));
		heightColumn.setCellValueFactory(new PropertyValueFactory<ImageRow, String>("height"));
		dateColumn.setCellValueFactory(new PropertyValueFactory<ImageRow, String>("date"));
		fileColumn.setCellValueFactory(new PropertyValueFactory<ImageRow, String>("file"));
		
		downloadImageTable.getColumns().addAll(idColumn,nameColumn,authorColumn,descriptionColumn,fileColumn,
				setTypeColumn, widthColumn, heightColumn, sizeColumn,dateColumn, fileTypeColumn);
		
		
		
		return downloadImageTable;
	}
	
	public class ImageRow
	{
		private final IntegerProperty id, width, height, size, date;
		private final StringProperty name, author, description, file, setType, fileType;
		
		/**
		 * 
		 * @param id
		 * @param name
		 * @param author
		 * @param description
		 * @param file
		 * @param setType
		 * @param width
		 * @param height
		 * @param size
		 * @param date
		 * @param fileType
		 */
		public ImageRow(int id, String name, String author, String description, String file, String setType,
				int width, int height, int size, int date, String fileType)
		{
			this.id = new SimpleIntegerProperty(id);
			this.width = new SimpleIntegerProperty(width);
			this.height = new SimpleIntegerProperty(height);
			this.size = new SimpleIntegerProperty(size);
			this.date = new SimpleIntegerProperty(date);
			this.name = new SimpleStringProperty(name);
			this.author = new SimpleStringProperty(author);
			this.description = new SimpleStringProperty(description);
			this.file = new SimpleStringProperty(file);
			this.fileType = new SimpleStringProperty(fileType);
			this.setType = new SimpleStringProperty(setType);
		}
		
		public final IntegerProperty getIdProperty()
		{
			return id;
		}
		public final int getId()
		{
			return id.get();
		}
		public final void setId(int val)
		{
			id.set(val);
		}
		
		public final IntegerProperty getHeightProperty()
		{
			return height;
		}
		public final int getHeight()
		{
			return height.get();
		}
		public final void setHeight(int val)
		{
			height.set(val);
		}
		
		public final IntegerProperty getWidthProperty()
		{
			return width;
		}
		public final int getWidth()
		{
			return width.get();
		}
		public final void setWidth(int val)
		{
			width.set(val);
		}
		
		public final IntegerProperty getSizeProperty()
		{
			return size;
		}
		public final int getSize()
		{
			return size.get();
		}
		public final void setSize(int val)
		{
			size.set(val);
		}
		
		public final IntegerProperty getDateProperty()
		{
			return date;
		}
		public final int getDate()
		{
			return date.get();
		}
		public final void setDate(int val)
		{
			date.set(val);
		}
		
		public final StringProperty getNameProperty()
		{
			return name;
		}
		public final String getName()
		{
			return name.get();
		}
		public final void setName(String val)
		{
			name.set(val);
		}
		
		public final StringProperty getAuthorProperty()
		{
			return author;
		}
		public final String getAuthor()
		{
			return author.get();
		}
		public final void setAuthor(String val)
		{
			author.set(val);
		}
		
		public final StringProperty getDescriptionProperty()
		{
			return description;
		}
		public final String getDescription()
		{
			return description.get();
		}
		public final void setDescription(String val)
		{
			description.set(val);
		}
		
		public final StringProperty getFileProperty()
		{
			return file;
		}
		public final String getFile()
		{
			return file.get();
		}
		public final void setFile(String val)
		{
			file.set(val);
		}
		
		public final StringProperty getFileTypeProperty()
		{
			return fileType;
		}
		public final String getFileType()
		{
			return fileType.get();
		}
		public final void setFileType(String val)
		{
			fileType.set(val);
		}
		
		public final StringProperty getSetTypeProperty()
		{
			return setType;
		}
		public final String getSetType()
		{
			return setType.get();
		}
		public final void setSetType(String val)
		{
			setType.set(val);
		}
	
		@Override
		public boolean equals(Object o)
		{
			if(o==null)
			{
				return false;
			}
			if(o==this)
			{
				return true;
			}
			if(o instanceof ImageRow)
			{
				ImageRow row = (ImageRow) o;
				if(row.getWidth()==width.get()
				&& row.getHeight()==height.get()
				&& row.getSetType().equals(setType.get())
				&& row.getFileType().equals(fileType.get())
				&& row.getFile().equals(file.get())
				&& row.getDescription().equals(description.get())
				&& row.getAuthor().equals(author.get())
				&& row.getName().equals(name.get())
				&& row.getDate() == date.get()
				&& row.getSize() == size.get()
				&& row.getId() == id.get())
				{
					return true;
				}
			}
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return 100*width.get() + 30*height.get() + fileType.get().hashCode()
					+ setType.get().hashCode()+ file.get().hashCode() +
					description.get().hashCode() + author.get().hashCode() +
					name.get().hashCode() + 11*date.get() + 15*size.get() + 18*id.get();
		}
	}
	
	private Button buildDownloadButton()
	{
		Button button = new Button("Download");
		button.setOnAction(ae ->{
			String type = tables.getSelectionModel().getSelectedItem().getText();
			if(type.equals("Images"))
			{
				requestImageDownload();
			} else if (type.equals("Regions")) {
				requestRegionDownload();
			} else if (type.equals("Colors")) {
				requestColorDownload();
			} else {
				System.out.println("Unknown type");
			}
			
		});
		return button;
	}
	
	private void requestColorDownload()
	{
		ColorRow row;
		if((row = downloadColorTable.getSelectionModel().selectedItemProperty().get())==null)
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("Please Choose an entry");
			alert.show();
			return;
		}
		
		if(colorExistsLocally(row.getHashCode()))
		{
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setContentText("That color already exists on your computer.");
			alert.show();
			return;
		}
		
		UploadDialog dialog = new UploadDialog();
		Platform.runLater(()->{
			dialog.show();
			dialog.getResponseLabel().setText("Downloading Image...");
		});
		downloadColor("http://www.ezstein.xyz/uploads/colors/" + row.getFile());
		try
		{
			ResultSet set = stmt.executeQuery("SELECT * FROM Linked WHERE ColorID = " + row.getId() + ";");
			if(set.isBeforeFirst())
			{
				set.next();
				int regionID;
				if((regionID = set.getInt("RegionID")) !=0)
				{
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setContentText("This color is associated with a region.\n"
							+ "Would you like to download the region as well?");
					ButtonType buttonTypeYes = new ButtonType("Yes");
					ButtonType buttonTypeNo = new ButtonType("No", ButtonData.CANCEL_CLOSE);
					alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
					Optional<ButtonType> result = alert.showAndWait();
					if(result.isPresent())
					{
						if(result.get().equals(buttonTypeYes))
						{
							set = stmt.executeQuery("SELECT File, HashCode FROM Regions WHERE ID = " + regionID + ";");
							if(set.isBeforeFirst())
							{
								set.next();
								if(!regionExistsLocally(set.getInt("HashCode")))
								{
									downloadRegion("http://www.ezstein.xyz/uploads/regions/" + set.getString("File"));
								}
							}
						}
					}
				}
				
				set = stmt.executeQuery("SELECT * FROM Linked WHERE ColorID = " + row.getId() + ";");
				if(set.isBeforeFirst())
				{
					set.next();
					int imageID;
					if((imageID = set.getInt("imageID")) !=0)
					{
						
						Alert alert = new Alert(AlertType.CONFIRMATION);
						alert.setContentText("This color is associated with an image.\n"
								+ "Would you like to download the image as well?");
						ButtonType buttonTypeYes = new ButtonType("Yes");
						ButtonType buttonTypeNo = new ButtonType("No", ButtonData.CANCEL_CLOSE);
						alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
						Optional<ButtonType> result = alert.showAndWait();
						if(result.isPresent())
						{
							if(result.get().equals(buttonTypeYes))
							{
								
								
								set = stmt.executeQuery("SELECT File, FileType FROM Images WHERE ID = " + imageID + ";");
								if(set.isBeforeFirst())
								{
									set.next();
									
									FileChooser fc = new FileChooser();
									fc.setTitle("Download");
									File file = null;
									if((file = fc.showSaveDialog(window)) == null)
									{
										return;
									}
									
									String newFile = file.getAbsolutePath();
									String imageType = set.getString("FileType");
									if(! newFile.endsWith("." + imageType))
									{
										newFile = new File(newFile + "." + imageType).getAbsolutePath();
									}
									
									downloadImage("http://www.ezstein.xyz/uploads/images/" + set.getString("File"), newFile);
								}
							}
						}
					}
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		Platform.runLater(()->{
			dialog.getResponseLabel().setText("Done");
			dialog.enableClose();
		});
	}
	
	private void requestRegionDownload(){
		RegionRow row;
		if((row = downloadRegionTable.getSelectionModel().selectedItemProperty().get())==null)
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("Please Choose an entry");
			alert.show();
			return;
		}
		if(regionExistsLocally(row.getHashCode()))
		{
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setContentText("That region already exists on your computer.");
			alert.show();
			return;
		}
		
		UploadDialog dialog = new UploadDialog();
		Platform.runLater(()->{
			dialog.show();
			dialog.getResponseLabel().setText("Downloading...");
		});
		downloadRegion("http://www.ezstein.xyz/uploads/regions/" + row.getFile());
		
		
		try
		{
			ResultSet set = stmt.executeQuery("SELECT * FROM Linked WHERE RegionID = " + row.getId() + ";");
			if(set.isBeforeFirst())
			{
				set.next();
				int imageID;
				if((imageID = set.getInt("imageID")) !=0)
				{
					
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setContentText("This color is associated with an image.\n"
							+ "Would you like to download the image as well?");
					ButtonType buttonTypeYes = new ButtonType("Yes");
					ButtonType buttonTypeNo = new ButtonType("No", ButtonData.CANCEL_CLOSE);
					alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
					Optional<ButtonType> result = alert.showAndWait();
					if(result.isPresent())
					{
						if(result.get().equals(buttonTypeYes))
						{
							set = stmt.executeQuery("SELECT File, FileType FROM Images WHERE ID = " + imageID + ";");
							if(set.isBeforeFirst())
							{
								set.next();
								
								FileChooser fc = new FileChooser();
								fc.setTitle("Download");
								File file = null;
								if((file = fc.showSaveDialog(window)) == null)
								{
									return;
								}
								
								String newFile = file.getAbsolutePath();
								String imageType = set.getString("FileType");
								if(! newFile.endsWith("." + imageType))
								{
									newFile = new File(newFile + "." + imageType).getAbsolutePath();
								}
								
								downloadImage("http://www.ezstein.xyz/uploads/images/" + set.getString("File"), newFile);
							}
						}
					}
				}
			}
		} catch(SQLException sqle)
		{
			sqle.printStackTrace();
		}
	
		
		Platform.runLater(()->{
			dialog.getResponseLabel().setText("Done");
			dialog.enableClose();
		});
	}
	
	private void downloadImage(String uri, String imagePath)
	{
		HttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet(uri);
		InputStream in = null;
		FileOutputStream out = null;
		byte[] buffer = new byte[1024];
		try {
			HttpResponse response = client.execute(get);
			in = response.getEntity().getContent();
			out = new FileOutputStream(imagePath);
			for(int length; (length = in.read(buffer)) >0;)
			{
				out.write(buffer, 0, length);
			}
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			
			try
			{
				if(in!=null)
				{
					in.close();
				}
				if(out !=null)
				{
					out.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void downloadColor(String uri)
	{
		HttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet(uri);
		InputStream in = null;
		OutputStream out = null;
		ObjectInputStream objectIn = null;
		ObjectOutputStream objectOut = null;
		byte[] buffer = new byte[1024];
		try{
			HttpResponse response = client.execute(get);
			in = response.getEntity().getContent();
			out = new FileOutputStream(Locator.locateUniqueFile("tmp/download/downloadFile.txt").toFile());
			for(int length; (length = in.read(buffer)) >0;)
			{
				out.write(buffer, 0, length);
			}
			objectIn = new ObjectInputStream(new FileInputStream(Locator.locateFile("tmp/download/downloadFile.txt").toFile()));
			CustomColorFunction ccf = (CustomColorFunction) objectIn.readObject();
			savedColors.add(ccf);
		} catch(IOException ioe){
			ioe.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(in!=null){
					in.close();
				}
				if(out !=null){
					out.close();
				}
				if(objectIn !=null)
				{
					objectIn.close();
				}
				if(objectOut !=null)
				{
					objectOut.close();
				}
					
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void downloadRegion(String uri)
	{
		HttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet(uri);
		InputStream in = null;
		OutputStream out = null;
		ObjectInputStream objectIn = null;
		ObjectOutputStream objectOut = null;
		ObjectOutputStream objectOutColor = null;
		byte[] buffer = new byte[1024];
		try{
			HttpResponse response = client.execute(get);
			in = response.getEntity().getContent();
			out = new FileOutputStream(Locator.locateUniqueFile("tmp/download/downloadFile.txt").toFile());
			for(int length; (length = in.read(buffer)) >0;)
			{
				out.write(buffer, 0, length);
			}
			objectIn = new ObjectInputStream(new FileInputStream(Locator.locateFile("tmp/download/downloadFile.txt").toFile()));
			SavedRegion sr = (SavedRegion)objectIn.readObject();
			savedRegions.add(sr);
			
			/*The region may require dependencies on color functions*/
			if(!savedColors.contains(sr.colorFunction))
			{
				savedColors.add(sr.colorFunction);
			}
		}catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			
			try {
				if(in!=null){
					in.close();
				}
				if(out !=null){
					out.close();
				}
				if(objectIn !=null)
				{
					objectIn.close();
				}
				if(objectOut !=null)
				{
					objectOut.close();
				}
				if(objectOutColor !=null)
				{
					objectOutColor.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void requestImageDownload(){
		ImageRow row = downloadImageTable.getSelectionModel().selectedItemProperty().get();
		if(row==null)
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("Please Choose an entry");
			alert.show();
			return;
		}
		
		FileChooser fc = new FileChooser();
		fc.setTitle("Download");
		File file = null;
		if((file = fc.showSaveDialog(window)) == null)
		{
			return;
		}
		
		String newFile = file.getAbsolutePath();
		String imageType = row.getFileType();
		if(! newFile.endsWith("." + imageType))
		{
			newFile = new File(newFile + "." + imageType).getAbsolutePath();
		}
		
		
		UploadDialog dialog = new UploadDialog();
		Platform.runLater(()->{
			dialog.show();
			dialog.getResponseLabel().setText("Downloading...");
		});
		downloadImage("http://www.ezstein.xyz/uploads/images/" + row.getFile(),newFile);
		try
		{
			ResultSet set = stmt.executeQuery("SELECT * FROM Linked WHERE ImageID = " + row.getId() + ";");
			if(set.isBeforeFirst())
			{
				set.next();
				int regionID;
				if((regionID = set.getInt("RegionID")) !=0)
				{
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setContentText("This Image is associated with a region and a color.\n"
							+ "Would you like to download the region and color as well?");
					ButtonType buttonTypeYes = new ButtonType("Yes");
					ButtonType buttonTypeNo = new ButtonType("No", ButtonData.CANCEL_CLOSE);
					alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
					Optional<ButtonType> result = alert.showAndWait();
					if(result.isPresent())
					{
						if(result.get().equals(buttonTypeYes))
						{
							set = stmt.executeQuery("SELECT File, HashCode FROM Regions WHERE ID = " + regionID + ";");
							if(set.isBeforeFirst())
							{
								set.next();
								if(!regionExistsLocally(set.getInt("HashCode")))
								{
									downloadRegion("http://www.ezstein.xyz/uploads/regions/" + set.getString("File"));
								}
							}
						}
					}
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			Platform.runLater(()->{
				dialog.getResponseLabel().setText("Done");
				dialog.enableClose();
			});
		
	}
	
	private Button buildApplyAndRerenderButton()
	{
		Button applyAndRerenderButton = new Button("Apply And Rerender");
		applyAndRerenderButton.setOnAction(e->{
			
			ResultType result = askToSaveColor();
			if(result == ResultType.CANCEL)
			{
				return;
			}
			gui.threadQueue.callLater(()->{
				if(!checkValues())
				{
					return;
				}
				gui.interrupt();
				
				Platform.runLater(()->{
					setValues();
					gui.drawSet();
					gui.updateJuliaSetViewer();
					window.close();
				});
				
			});
		});
		
		return applyAndRerenderButton;
	}
	
	private Button buildSaveButton()
	{
		Button saveButton = new Button("Save As...");
		saveButton.setOnAction(e ->{
			if(!checkValues())
			{
				return;
			}
			String name;
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("Save Region");
			dialog.setContentText("Enter a name:");
			Optional<String> result = dialog.showAndWait();
			if(result.isPresent())
			{
				name = result.get();
				for(SavedRegion sr : savedRegionsChoiceBox.getItems())
				{
					if(sr.name.equals(name))
					{
						Alert alert = new Alert(AlertType.ERROR);
						alert.setContentText("That Name Already Exists");
						alert.show();
						return;
					}
				}
			}
			else
			{
				return;
			}
			
			/*Resets the values if a region has already been loaded*/
			if(savedRegionsChoiceBox.getValue()!=null)
			{
				resetValues();
			}
			
			SavedRegion savedRegion = new SavedRegion(name, autoIterationsCheckBox.isSelected(),
					Integer.parseInt(iterationsField.getText()),
					Integer.parseInt(precisionField.getText()), Integer.parseInt(threadCountField.getText()),
					currentRegion,arbitraryPrecision.isSelected(),currentJulia,currentSeed,colorChoiceBox.getValue());
			savedRegions.add(savedRegion);
			//savedRegionsChoiceBox.setValue(savedRegion);
		});
		return saveButton;
	}
	
	private Button buildApplyButton()
	{
		Button applyButton = new Button("Apply");
		applyButton.setOnAction(e ->{
			
			ResultType result = askToSaveColor();
			if(result == ResultType.CANCEL)
			{
				return;
			}
				
				
			gui.threadQueue.callLater(()->{
				if(!checkValues())
				{
					return;
				}
				gui.interrupt();
				
				Platform.runLater(()->{
					setValues();
					window.close();
					
				});
				
			});
		});
		return applyButton;
	}
	
	private Button buildCancelButton()
	{
		Button cancelButton = new Button("Cancel");
		
		cancelButton.setOnAction(e ->{
			ResultType result = askToSaveColor();
			if(result == ResultType.CANCEL)
			{
				return;
			}
			window.close();
		});
		return cancelButton;
	}
	
	private boolean isConnectedToInternet()
	{
		try {
			InetAddress address = InetAddress.getByName("www.ezstein.xyz");
			return address.isReachable(1000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return false;
		
	}
	
	/**
	 * Opens dialogs for prompting to save.
	 * @return  No if it is not necessary to save a color or if No is selected.
	 * 			Cancel if cancel is selected or if the window is closed or if the save process was in some other way canceled.
	 * 			Yes was selected and if the save was successful
	 */
	public ResultType askToSaveColor()
	{
		boolean save = true;
		for(CustomColorFunction cf: colorChoiceBox.getItems())
		{
			if(cf.getStops().equals(stopList.getItems()))
			{
				save = false;
				break;
			}
		}
		if(save)
		{
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setContentText("Would you like to save the color.");
			ButtonType buttonTypeYes = new ButtonType("Yes");
			ButtonType buttonTypeNo = new ButtonType("No");
			ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
			alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);
			Optional<ButtonType> result = alert.showAndWait();
			if(result.get() == buttonTypeCancel)
			{
				/*Also executes if the dialog is closed*/
				return ResultType.CANCEL;
			}
			else if (result.get() == buttonTypeYes)
			{
				if(!validateForSaveColor())
				{
					return ResultType.CANCEL;
				}
				
				if(!saveColor())
				{
					/*The cancel button was pressed when asking to name the color*/
					return ResultType.CANCEL;
				}
				return ResultType.YES;
			}
			else if (result.get() == buttonTypeNo)
			{
				return ResultType.NO;
			}
			else
			{
				return ResultType.CANCEL;
			}
		}
		else
		{
			/*A save was not necessary*/
			return ResultType.NO;
		}
	}
	
	/**
	 * Resets all the values to those currently in use by the gui.
	 */
 	public void resetValues()
	{
		threadCountField.setText(gui.threadCount + "");
		
		autoIterationsCheckBox.setSelected(gui.autoIterations);
		iterationsField.setDisable(gui.autoIterations);
		iterationsField.setText(gui.iterations + "");
		
		
		precisionField.setText(gui.precision + "");
		colorChoiceBox.setValue(gui.mainCalculator.getColorFunction());
		
		arbitraryPrecision.setSelected(gui.arbitraryPrecision);
		doublePrecision.setSelected(!gui.arbitraryPrecision);
		currentRegion = gui.currentRegion;
		currentJulia = gui.julia;
		currentSeed = gui.juliaSeed;
		
		if(currentJulia)
		{
			set.setText("Julia set: ");
			seed.setText(currentSeed.toString());
		}
		else
		{
			set.setText("Mandelbrot set: ");
			seed.setText("0+0i");
		}
		centerValue.setText(currentRegion.getCenterX().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()+ " + "
				+ currentRegion.getCenterY().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString() + "i");
		boxValue.setText(currentRegion.x1.subtract(currentRegion.x2).abs().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString() + "x" +
				currentRegion.y1.subtract(currentRegion.y2).abs().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString());
		
		
		initializeValues();
	}
	
	/**
	 * Loads a region from the choice box and sets all the values of the options editor.
	 */
	public void loadRegion()
	{
		SavedRegion sr = savedRegionsChoiceBox.getValue();
		iterationsField.setText("" + sr.iterations);
		precisionField.setText("" + sr.precision);
		threadCountField.setText("" + sr.threadCount);
		
		autoIterationsCheckBox.setSelected(sr.autoIterations);
		if(sr.autoIterations)
		{
			iterationsField.setDisable(true);
		}
		else
		{
			iterationsField.setDisable(false);
		}
		
		if(sr.arbitraryPrecision)
		{
			arbitraryPrecision.setSelected(true);
			doublePrecision.setSelected(false);
		}
		else
		{
			arbitraryPrecision.setSelected(false);
			doublePrecision.setSelected(true);
		}
		
		
		if(colorChoiceBox.getItems().contains(sr.colorFunction))
		{
			colorChoiceBox.setValue(sr.colorFunction);
		}
		else
		{
			/*Will NOT add color to file*/
			
		}
		
		
		currentRegion = sr.region;
		currentJulia = sr.julia;
		currentSeed = sr.seed;
		if(currentJulia)
		{
			set.setText("Julia set: ");
			seed.setText(currentSeed.toString());
		}
		else
		{
			set.setText("Mandelbrot set: ");
			seed.setText("0+0i");
		}
		centerValue.setText(currentRegion.getCenterX().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()+ " + "
				+ currentRegion.getCenterY().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString() + "i");
		boxValue.setText(currentRegion.x1.subtract(currentRegion.x2).abs().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString() + "x" +
				currentRegion.y1.subtract(currentRegion.y2).abs().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString());
		gui.loggedRegions = new ArrayList<>();
	}
	
	/**
	 * Sets all the values of the gui to the values of the editor.
	 */
	public void setValues()
	{
		gui.threadCount = Integer.parseInt(threadCountField.getText());
		
		gui.autoIterations = autoIterationsCheckBox.isSelected();
		if(!gui.autoIterations)
		{
			gui.iterations = Integer.parseInt(iterationsField.getText());
		}
		
		
		gui.precision = Integer.parseInt(precisionField.getText());
		gui.mainCalculator.setColorFunction(colorChoiceBox.getValue());
		gui.previewCalculator.setColorFunction(colorChoiceBox.getValue());
		gui.arbitraryPrecision = arbitraryPrecision.isSelected();
		gui.currentRegion = currentRegion;
		gui.julia = currentJulia;
		gui.juliaSeed = currentSeed;
	}
	
	/**
	 * Validates all the values.
	 * @return false if some values are not valid.
	 */
	public boolean checkValues()
	{
		boolean returnValue = true;
		int threadCount = 0;
		int iterations = 0;
		int precision = 0;
		try
		{
			threadCount = Integer.parseInt(threadCountField.getText());
		}
		catch(NumberFormatException nfe)
		{
			threadCountField.setStyle("-fx-background-color:red");
			returnValue = false;
		}
		try
		{
			iterations = Integer.parseInt(iterationsField.getText());
		}
		catch(NumberFormatException nfe)
		{
			iterationsField.setStyle("-fx-background-color:red");
			returnValue =  false;
		}
		try
		{
			precision = Integer.parseInt(precisionField.getText());
		}
		catch(NumberFormatException nfe)
		{
			precisionField.setStyle("-fx-background-color:red");
			returnValue = false;
		}
		if(threadCount <= 0 || threadCount >= gui.height)
		{
			threadCountField.setStyle("-fx-background-color:red");
			returnValue =  false;
		}
		if(iterations <= 0)
		{
			iterationsField.setStyle("-fx-background-color:red");
			returnValue =  false;
		}
		if(precision <=0)
		{
			precisionField.setStyle("-fx-background-color:red");
			returnValue = false;
		}
		
		return returnValue;
	}

	private class UploadDialog
	{
		private Label responseLabel;
		private Stage stage;
		private Button closeButton;
		public UploadDialog(){
			Platform.runLater(()->{
				stage = new Stage();
				GridPane grid = new GridPane();
				grid.setPadding(new Insets(25,25,25,25));
				grid.setHgap(10);
				grid.setVgap(10);
				
				responseLabel = new Label("");
				closeButton = new Button("Close");
				closeButton.setDisable(true);
				closeButton.setOnAction(e->{
					stage.close();
				});
				
				//grid.add(new Label("Uploading..."),0,0);
				grid.add(responseLabel,0,1);
				grid.add(closeButton,0,2);
				
				stage.setTitle("Waiting");
				Scene scene = new Scene(grid);
				stage.setHeight(200);
				stage.setWidth(300);
				stage.setScene(scene);
			});
		}
		
		public Label getResponseLabel(){
			return responseLabel;
		}
		
		public void enableClose()
		{
			closeButton.setDisable(false);
		}
		
		public void show()
		{
			stage.show();
		}
	}
	
	enum ResultType {YES, CANCEL, NO}
	/**
	 * A custom cell for use in the list view.
	 * @author Ezra
	 *
	 */
	public class CustomListCell extends ListCell<Stop>
	{
		HBox hbox;
		Canvas canvas;
		Label label;
		
		/**
		 * Constructs this cell with a label and a canvas that will hold the color of the cell.
		 */
		public CustomListCell()
		{
			super();
			hbox = new HBox(10);
			canvas = new Canvas();
			canvas.setHeight(10);
			canvas.setWidth(30);
			label = new Label();
			hbox.getChildren().addAll(label, canvas);
		}
		
		@Override
		protected void updateItem(Stop item, boolean empty)
		{
			super.updateItem(item, empty);
			setText(null);
			if(empty)
			{
				setGraphic(null);
			}
			else
			{
				label.setText(Math.round(item.getOffset()*100) + "%");
				GraphicsContext gc = canvas.getGraphicsContext2D();
				gc.setFill(item.getColor());
				gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				setGraphic(hbox);
			}
		}
	}
}
