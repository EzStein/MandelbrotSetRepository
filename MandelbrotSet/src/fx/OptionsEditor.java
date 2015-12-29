package fx;

import java.io.*;
import java.math.*;
import java.util.*;
import colorFunction.*;
import javafx.application.*;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.*;

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
	private ChoiceBox<SavedRegion> savedRegionsChoiceBox;
	private ChoiceBox<CustomColorFunction> colorChoiceBox;
	private TextField colorPositionField;
	private TextField rangeField;
	private Slider colorPositionSlider;
	private ListView<Stop> stopList;
	private RadioButton arbitraryPrecision;
	private RadioButton doublePrecision;
	private Rectangle gradientRectangle;
	private ColorPicker colorPicker;
	private ObjectOutputStream out, colorOut;
	private File file, colorFile;
	private ArrayList<SavedRegion> savedRegions;
	private Region<BigDecimal> currentRegion;
	private boolean currentJulia, showNewColor;
	private ComplexBigDecimal currentSeed;
	private int tabNumber;
	
	/**
	 * 
	 * @param gui
	 * @param tabNumber		The index of the tab that should be initially displayed.
	 */
	public OptionsEditor(MainGUI gui, int tabNumber)
	{
		this.tabNumber = tabNumber;
		ObjectInputStream in = null;
		ObjectInputStream colorIn = null;
		
		savedRegions = new ArrayList<SavedRegion>();
		
		try
		{
			colorFile = new File(Locator.locateFile("SavedColors.txt"));
			colorIn = new ObjectInputStream(new FileInputStream(colorFile));
			ArrayList<CustomColorFunction> savedColors = (ArrayList<CustomColorFunction>) colorIn.readObject();
			CustomColorFunction.COLOR_FUNCTIONS = savedColors;
			
			file = new File(Locator.locateFile("SavedRegions.txt"));
			in = new ObjectInputStream(new FileInputStream(file));
			savedRegions = (ArrayList<SavedRegion>)in.readObject();
		}
		catch(EOFException eofe)
		{
			/*File Empty And inputStream is null*/
			savedRegions = new ArrayList<SavedRegion>();
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
		
		this.gui = gui;
		currentRegion = gui.currentRegion;
		currentJulia = gui.julia;
		currentSeed = gui.juliaSeed;
	}
	
	public void showEditDialog()
	{
		window = new Stage();
		window.setTitle("Edit...");
		window.initModality(Modality.APPLICATION_MODAL);
		BorderPane layout = new BorderPane();
		
		TabPane tabPane = new TabPane();
		tabPane.getTabs().add(buildOptionsTab());
		tabPane.getTabs().add(buildColorTab());
		layout.setCenter(tabPane);
		tabPane.getSelectionModel().select(tabNumber);
		
		
		HBox buttonBox = new HBox(10);
		buttonBox.setPadding(new Insets(10,10,10,10));
		buttonBox.getChildren().addAll(buildSaveButton(), buildApplyAndRerenderButton(), buildApplyButton(), buildCancelButton());
		layout.setBottom(buttonBox);
		
		Scene scene = new Scene(layout);
		scene.getStylesheets().add(this.getClass().getResource("OptionsStyleSheet.css").toExternalForm());
		window.setScene(scene);
		window.show();
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
		savedRegionsChoiceBox = new ChoiceBox<SavedRegion>(FXCollections.observableArrayList(savedRegions));
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
					loadRegion();
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
			savedRegionsChoiceBox.getItems().remove(deletedRegion);
			try
			{
				/*Overwrites File*/
				out = new ObjectOutputStream(new FileOutputStream(file));
				out.writeObject(savedRegions);
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
		colorGridPane.add(buildGradientCheckBox(), 0, 1);
		colorGridPane.add(buildGradientRectangle(), 0, 2, 2, 2);
		
		colorGridPane.add(buildColorPositionSlider(), 1, 1);
		colorGridPane.add(buildColorPositionField(), 2, 1);
		colorGridPane.add(buildColorPicker(), 1, 0);
		
		colorGridPane.add(buildStopList(), 2, 2,2,1);
		colorGridPane.add(buildAddStopButton(), 2, 3);
		colorGridPane.add(buildSaveColorButton(), 0, 3);
		colorGridPane.add(new Label("Range:"), 2, 0);
		rangeField = new TextField();
		colorGridPane.add(rangeField, 3, 0);
		colorGridPane.add(buildRemoveStopButton(), 3, 3);
		
		colorTab.setContent(colorGridPane);
		
		initializeValues();
		return colorTab;
	}
	
	private void initializeValues()
	{
		rangeField.setText(colorChoiceBox.getValue().getRange()+"");
		
		stopList.getItems().remove(0, stopList.getItems().size());
		for(Stop stop : colorChoiceBox.getValue().getStops())
		{
			stopList.getItems().add(stop);
		}
		
		gradientRectangle.setFill(new LinearGradient(0,0.5,1,0.5,true, CycleMethod.NO_CYCLE, createGradientStops()));
		
	}
	
	private Rectangle buildGradientRectangle()
	{
		gradientRectangle = new Rectangle();
		gradientRectangle.setWidth(200);
		gradientRectangle.setHeight(200);
		
		return gradientRectangle;
	}
	
	private ColorPicker buildColorPicker()
	{
		colorPicker = new ColorPicker(Color.BLACK);
		colorPicker.valueProperty().addListener(new ChangeListener<Color>(){

			@Override
			public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue)
			{
				gradientRectangle.setFill(new LinearGradient(0,0.5,1,0.5,true, CycleMethod.NO_CYCLE, createGradientStops()));
			}
		});
		return colorPicker;
	}
	
	private ArrayList<Stop> createGradientStops()
	{
		
		ArrayList<Stop> returnValue = new ArrayList<Stop>(Arrays.asList(stopList.getItems().toArray(new Stop[1])));
		if(showNewColor)
		{
			returnValue.add(new Stop(colorPositionSlider.getValue(),colorPicker.getValue()));
		}
		return returnValue;
	}
	
	private ChoiceBox<CustomColorFunction> buildColorChoiceBox()
	{
		colorChoiceBox = new ChoiceBox<CustomColorFunction>(FXCollections.observableArrayList(CustomColorFunction.COLOR_FUNCTIONS));
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
				
				gradientRectangle.setFill(new LinearGradient(0,0.5,1,0.5,true, CycleMethod.NO_CYCLE, createGradientStops()));
				
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
					
					gradientRectangle.setFill(new LinearGradient(0,0.5,1,0.5,true, CycleMethod.NO_CYCLE, createGradientStops()));
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
				/*return new TextFieldListCell<Stop>(new StringConverter<Stop>(){

					@Override
					public String toString(Stop stop) {
						return stop.getOffset()+", " + stop.getColor().toString();
					}

					@Override
					public Stop fromString(String string) {
						// TODO Auto-generated method stub
						return null;
					}
					
				});*/
				return new CustomListCell();
			}
			
		});
		
		return stopList;
	}
	
	private Button buildSaveColorButton()
	{
		Button saveColorButton = new Button("Save Color");
		saveColorButton.setOnAction(ae ->{
			
			if(stopList.getItems().size()<2)
			{
				Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText("There must be atleast two colors positioned at 0 and 1 (beginning and end)");
				alert.show();
				return;
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
				return;
			}
			
			int val;
			try
			{
				val = Integer.parseInt(rangeField.getText());
			}
			catch(NumberFormatException nfe)
			{
				rangeField.setStyle("-fx-background-color:red");
				return;
			}
			if(val <= 1)
			{
				rangeField.setStyle("-fx-background-color:red");
				return;
			}
			
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
						return;
					}
				} 
				/*Add in name validation here*/
			}
			else
			{
				return;
			}
			rangeField.setStyle("-fx-background-color:white");
			CustomColorFunction color = new CustomColorFunction(new ArrayList<Stop>(stopList.getItems()),val, name);
			colorChoiceBox.getItems().add(color);
			colorChoiceBox.setValue(color);
			CustomColorFunction.COLOR_FUNCTIONS.add(color);
			try
			{
				colorOut = new ObjectOutputStream(new FileOutputStream(colorFile));
				colorOut.writeObject(CustomColorFunction.COLOR_FUNCTIONS);
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
		});
		return saveColorButton;
	}
	
	private Button buildRemoveStopButton()
	{
		Button removeStopButton = new Button("Remove Color");
		removeStopButton.setOnAction(ae->{
			Stop stop = stopList.getSelectionModel().getSelectedItem();
			stopList.getItems().remove(stop);
			gradientRectangle.setFill(new LinearGradient(0,0.5,1,0.5,true, CycleMethod.NO_CYCLE, createGradientStops()));
			
		});
		return removeStopButton;
	}
	
	private Button buildAddStopButton()
	{
		Button addStopButton = new Button("Add Color");
		addStopButton.setOnAction(ae->{
			Stop stopToAdd = new Stop(colorPositionSlider.getValue(), colorPicker.getValue());
			for(Stop stop : stopList.getItems())
			{
				if(stop.getOffset() == stopToAdd.getOffset())
				{
					Alert alert = new Alert(AlertType.ERROR);
					alert.setContentText("That color has the same position as another!");
					alert.show();
					return;
				}
			}
			stopList.getItems().add(stopToAdd);
		});
		return addStopButton;
	}
	
	private CheckBox buildGradientCheckBox()
	{
		CheckBox checkBox = new CheckBox("Show New Color");
		checkBox.setSelected(false);
		showNewColor = false;
		checkBox.setOnAction(e ->{
			showNewColor = checkBox.isSelected();
			gradientRectangle.setFill(new LinearGradient(0,0.5,1,0.5,true, CycleMethod.NO_CYCLE, createGradientStops()));
		});
		return checkBox;
	}

	private Button buildApplyAndRerenderButton()
	{
		Button applyAndRerenderButton = new Button("Apply And Rerender");
		applyAndRerenderButton.setOnAction(e->{
			gui.threadQueue.callLater(()->{
				if(!checkValues())
				{
					return false;
				}
				gui.interrupt();
				
				Platform.runLater(()->{
					setValues();
					gui.drawSet();
					gui.updateJuliaSetViewer();
					window.close();
				});
				return false;
				
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
			savedRegionsChoiceBox.getItems().add(savedRegion);
			savedRegionsChoiceBox.setValue(savedRegion);
			try
			{
				/*Overwrites File*/
				out = new ObjectOutputStream(new FileOutputStream(file));
				out.writeObject(savedRegions);
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
		});
		return saveButton;
	}
	
	private Button buildApplyButton()
	{
		Button applyButton = new Button("Apply");
		applyButton.setOnAction(e ->{
			gui.threadQueue.callLater(()->{
				if(!checkValues())
				{
					return false;
				}
				gui.interrupt();
				
				Platform.runLater(()->{
					setValues();
					window.close();
					
				});
				
				return false;
			});
		});
		return applyButton;
	}
	
	private Button buildCancelButton()
	{
		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(e ->{
			window.close();
		});
		return cancelButton;
	}
	
	
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
	}
	
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
			colorChoiceBox.getItems().add(sr.colorFunction);
			CustomColorFunction.COLOR_FUNCTIONS.add(sr.colorFunction);
			colorChoiceBox.setValue(sr.colorFunction);
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
	
	public class CustomListCell extends ListCell<Stop>
	{
		HBox hbox;
		Canvas canvas;
		Label label;
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
				label.setText(item.getOffset()*100 + "%");
				GraphicsContext gc = canvas.getGraphicsContext2D();
				gc.setFill(item.getColor());
				gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				setGraphic(hbox);
			}
		}
	}
}
