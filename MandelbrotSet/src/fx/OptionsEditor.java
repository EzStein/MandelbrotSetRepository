package fx;

import java.io.*;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import colorFunction.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.*;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class OptionsEditor
{
	MainGUI gui;
	Stage window;
	GridPane optionsGridPane, colorGridPane;
	Label centerValue, set, seed;
	Label boxValue;
	TextField threadCountField;
	TextField iterationsField;
	TextField precisionField;
	BorderPane layout;
	HBox buttonBox;
	CheckBox autoIterationsCheckBox;
	ChoiceBox<SavedRegion> savedRegionsChoiceBox;
	ChoiceBox<ColorFunction> colorChoiceBox;
	RadioButton arbitraryPrecision;
	RadioButton doublePrecision;
	Rectangle gradientRectangle;
	ColorPicker colorPicker;
	ObjectInputStream in;
	ObjectOutputStream out;
	File file;
	ArrayList<SavedRegion> savedRegions;
	Region<BigDecimal> currentRegion;
	boolean currentJulia;
	ComplexBigDecimal currentSeed;
	ArrayList<Stop> gradientStops;
	
	public OptionsEditor(MainGUI gui)
	{
		savedRegions = new ArrayList<SavedRegion>();
		
		try
		{
			file = new File(Locator.locateFile("SavedRegions.txt"));
			in = new ObjectInputStream(new FileInputStream(file));
			savedRegions = (ArrayList<SavedRegion>)in.readObject();
		}
		catch(EOFException eofe)
		{
			/*File Empty And in is null*/
			savedRegions = new ArrayList<SavedRegion>();
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
		layout = new BorderPane();
		buttonBox = new HBox(10);
		TabPane tabPane = new TabPane();
		Tab colorTab = new Tab("Color");
		Tab optionsTab = new Tab("Options");
		colorTab.setClosable(false);
		optionsTab.setClosable(false);
		tabPane.getTabs().add(optionsTab);
		tabPane.getTabs().add(colorTab);
		optionsGridPane= new GridPane();
		optionsGridPane.setVgap(10);
		optionsGridPane.setHgap(10);
		optionsGridPane.setPadding(new Insets(30,30,30,30));
		
		colorGridPane= new GridPane();
		colorGridPane.setVgap(10);
		colorGridPane.setHgap(10);
		colorGridPane.setPadding(new Insets(30,30,30,30));
		colorTab.setContent(colorGridPane);
		layout.setCenter(tabPane);
		layout.setBottom(buttonBox);
		//layout.setPadding(new Insets(20,20,20,20));
		buttonBox.setPadding(new Insets(10,10,10,10));
		/*Labels*/
		Label threadCountLabel = new Label("Threads:");
		Label iterationsLabel = new Label("Iterations:");
		Label precisionLabel = new Label("Precision:");
		Label colorLabel = new Label("Color:");
		Label savedRegionsLabel = new Label("Saved Regions:");
		savedRegionsLabel.setFont(new Font(20));
		Label centerLabel = new Label("Center:");
		Label boxLabel = new Label("Box Dimensions:");
		
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
		savedRegionsChoiceBox = new ChoiceBox<SavedRegion>(FXCollections.observableArrayList(savedRegions));
		savedRegionsChoiceBox.setConverter(new StringConverter<SavedRegion>(){

			@Override
			public String toString(SavedRegion sr)
			{
				return sr.getName();
			}

			@Override
			public SavedRegion fromString(String string) {
				return null;
			}
			
			
		});
		
		
		colorChoiceBox = new ChoiceBox<ColorFunction>(FXCollections.observableArrayList(ColorFunction.ColorInfo.COLOR_FUNCTIONS.values()));
		colorChoiceBox.setValue(gui.mainCalculator.getColorFunction());
		
		/*Buttons*/
		Button applyButton = new Button("Apply");
		Button cancelButton = new Button("Cancel");
		Button applyAndRerenderButton = new Button("Apply And Rerender");
		Button saveButton = new Button("Save As...");
		
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
		
		set = new Label();
		seed = new Label();
		if(gui.julia)
		{
			set.setText("Julia set: ");
			seed.setText(gui.juliaSeed.toString());
		}
		else
		{
			set.setText("Mandelbrot set: ");
			seed.setText("0+0i");
		}
		
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
		
		optionsGridPane.add(savedRegionsLabel, 0, 0,2,1);
		optionsGridPane.add(iterationsLabel, 0, 1);
		optionsGridPane.add(precisionLabel, 0, 3);
		optionsGridPane.add(threadCountLabel, 0,4);
		
		
		
		optionsGridPane.add(autoIterationsCheckBox, 1, 1);
		optionsGridPane.add(iterationsField, 1, 2);
		optionsGridPane.add(precisionField, 1, 3);
		optionsGridPane.add(threadCountField, 1, 4);
		optionsGridPane.add(doublePrecision, 1, 5);
		optionsGridPane.add(arbitraryPrecision, 1, 6);
		
		
		
		optionsGridPane.add(savedRegionsChoiceBox, 2, 0);
		optionsGridPane.add(set, 2, 1);
		optionsGridPane.add(centerLabel, 2, 2);
		optionsGridPane.add(boxLabel, 2, 3);
		
		optionsGridPane.add(removeButton, 3, 0);
		optionsGridPane.add(seed, 3, 1);
		optionsGridPane.add(centerValue, 3, 2);
		optionsGridPane.add(boxValue, 3, 3);
		
		
		
		
		gradientStops = new ArrayList<Stop>();
		ListView<Stop> stopList = new ListView<Stop>();
		/*stopList.setCellFactory(new Callback<ListView<Stop>, ListCell<Stop>>(){
			@Override
			public ListCell<Stop> call(ListView<Stop> param) {
				// TODO Auto-generated method stub
				return null;
			}
			
		});*/
		gradientStops.add(new Stop(0, Color.WHITE));
		TextField colorPositionField = new TextField("1");
		Button addStopButton = new Button("Add Color");
		Button removeStopButton = new Button("Remove Color");
		removeStopButton.setOnAction(ae->{
			Stop stop = stopList.getSelectionModel().getSelectedItem();
			gradientStops.remove(stop);
			stopList.getItems().remove(stop);
			System.out.println(gradientStops.toString());
			gradientRectangle.setFill(new LinearGradient(0,0.5,1,0.5,true, CycleMethod.NO_CYCLE, gradientStops));
			
		});
		addStopButton.setOnAction(ae->{
			stopList.getItems().add(gradientStops.get(gradientStops.size()-1));
			gradientStops.add(new Stop(0,Color.TRANSPARENT));
		});
		
		Slider colorPositionSlider = new Slider(0,1,0.5);
		colorPositionSlider.valueProperty().addListener(new ChangeListener<Number>(){

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				Platform.runLater(()->{
					colorPositionField.setStyle("-fx-background-color:white");
					colorPositionField.setText(newValue.doubleValue() + "");
					gradientStops.remove(gradientStops.size()-1);
					gradientStops.add(new Stop(colorPositionSlider.getValue(), colorPicker.getValue()));
					gradientRectangle.setFill(new LinearGradient(0,0.5,1,0.5,true, CycleMethod.NO_CYCLE, gradientStops));
				});
			}
		});
		
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
		
		
		
		
		
		gradientRectangle = new Rectangle();
		gradientRectangle.setWidth(200);
		gradientRectangle.setHeight(200);
		gradientRectangle.setFill(new LinearGradient(0,0.5,1,0.5,true, CycleMethod.NO_CYCLE, gradientStops));
		
		colorPicker = new ColorPicker(Color.BLACK);
		colorPicker.valueProperty().addListener(new ChangeListener<Color>(){

			@Override
			public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue)
			{
				Platform.runLater(()->{
					gradientStops.remove(gradientStops.size()-1);
					gradientStops.add(new Stop(colorPositionSlider.getValue(), colorPicker.getValue()));
					gradientRectangle.setFill(new LinearGradient(0,0.5,1,0.5,true, CycleMethod.NO_CYCLE, gradientStops));
				});
			}
		});
		
		
		
		
		colorGridPane.add(gradientRectangle, 0, 1, 2, 2);
		colorGridPane.add(colorChoiceBox, 0, 0);
		
		colorGridPane.add(colorPositionSlider, 1, 1);
		
		colorGridPane.add(colorPositionField, 2, 1);
		colorGridPane.add(colorPicker, 1, 0);
		colorGridPane.add(stopList, 2, 2,2,1);
		colorGridPane.add(removeStopButton, 3, 3);
		colorGridPane.add(addStopButton, 2, 3);
		
		buttonBox.getChildren().addAll(saveButton, applyAndRerenderButton, applyButton, cancelButton);
		
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
					if(sr.getName().equals(name))
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
					currentRegion,arbitraryPrecision.isSelected(),currentJulia,currentSeed,colorChoiceBox.getValue().toString());
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
		
		cancelButton.setOnAction(e ->{
			window.close();
		});
		
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
		
		threadCountField.setOnMouseClicked(e -> {
			threadCountField.setStyle("-fx-background-color:white");
		});
		iterationsField.setOnMouseClicked(e -> {
			iterationsField.setStyle("-fx-background-color:white");
		});
		precisionField.setOnMouseClicked(e -> {
			precisionField.setStyle("-fx-background-color:white");
		});
		savedRegionsChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Platform.runLater(() -> {
					loadRegion();
					});
				}
			
		});
		
		//gridPane.setGridLinesVisible(true);
		optionsTab.setContent(optionsGridPane);
		Scene scene = new Scene(layout);
		scene.getStylesheets().add(this.getClass().getResource("OptionsStyleSheet.css").toExternalForm());
		window.setScene(scene);
		window.show();
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
		
		
		colorChoiceBox.setValue(ColorFunction.ColorInfo.COLOR_FUNCTIONS.get(sr.colorFunction));
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
}
