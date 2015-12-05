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
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.*;

public class OptionsEditor
{
	MainGUI gui;
	Stage window;
	GridPane gridPane;
	Label centerValue, set, seed;
	Label boxValue;
	TextField threadCountField;
	TextField iterationsField;
	TextField precisionField;
	ChoiceBox<SavedRegion> savedRegionsChoiceBox;
	ChoiceBox<ColorFunction> colorChoiceBox;
	RadioButton arbitraryPrecision;
	RadioButton doublePrecision;
	ObjectInputStream in;
	ObjectOutputStream out;
	File file;
	ArrayList<SavedRegion> savedRegions;
	Region<BigDecimal> currentRegion;
	boolean currentJulia;
	ComplexBigDecimal currentSeed;
	
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
		gridPane = new GridPane();
		gridPane.setVgap(10);
		gridPane.setHgap(10);
		gridPane.setPadding(new Insets(30,30,30,30));
		
		/*Labels*/
		Label threadCountLabel = new Label("Threads:");
		Label iterationsLabel = new Label("Iterations:");
		Label precisionLabel = new Label("Precision:");
		Label colorLabel = new Label("Color:");
		Label savedRegionsLabel = new Label("Saved Regions:");
		savedRegionsLabel.setFont(new Font(20));
		Label centerLabel = new Label("Center:");
		Label boxLabel = new Label("Box Dimensions:");
		
		centerValue = new Label(gui.currentRegion.getCenterX().stripTrailingZeros().toPlainString()+ " + "
									+ gui.currentRegion.getCenterY().stripTrailingZeros().toPlainString() + "i");
		boxValue = new Label(gui.currentRegion.x1.subtract(gui.currentRegion.x2).abs().stripTrailingZeros().toPlainString() + "x" +
				gui.currentRegion.y1.subtract(gui.currentRegion.y2).abs().stripTrailingZeros().toPlainString());
		
		/*Text Fields*/
		threadCountField = new TextField(""+gui.threadCount);
		iterationsField = new TextField("" +gui.iterations);
		precisionField = new TextField("" + gui.precision);
		savedRegionsChoiceBox = new ChoiceBox<SavedRegion>(FXCollections.observableArrayList(savedRegions));
		
		
		
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
		
		gridPane.add(savedRegionsLabel, 0, 0,2,1);
		gridPane.add(colorLabel, 0, 1);
		gridPane.add(iterationsLabel, 0, 2);
		gridPane.add(precisionLabel, 0, 3);
		gridPane.add(threadCountLabel, 0,4);
		
		gridPane.add(savedRegionsChoiceBox, 2, 0);
		gridPane.add(colorChoiceBox, 1, 1);
		gridPane.add(iterationsField, 1, 2);
		gridPane.add(precisionField, 1, 3);
		gridPane.add(threadCountField, 1, 4);
		gridPane.add(doublePrecision, 1, 5);
		gridPane.add(arbitraryPrecision, 1, 6);
		
		
		
		gridPane.add(set, 2, 2);
		gridPane.add(centerLabel, 2, 3);
		gridPane.add(boxLabel, 2, 4);
		gridPane.add(applyButton, 2, 5);
		gridPane.add(cancelButton, 2, 6);
		
		gridPane.add(seed, 3, 2);
		gridPane.add(centerValue, 3, 3);
		gridPane.add(boxValue, 3, 4);
		gridPane.add(saveButton, 3, 5);
		gridPane.add(applyAndRerenderButton, 3, 6);
		
		
		
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
			}
			else
			{
				return;
			}
			SavedRegion savedRegion = new SavedRegion(name,Integer.parseInt(iterationsField.getText()),
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
			if(!checkValues())
			{
				return;
			}
			gui.interrupt();
			setValues();
			gui.drawSet();
			gui.updateJuliaSetViewer();
			window.close();
		});
		
		cancelButton.setOnAction(e ->{
			window.close();
		});
		
		applyButton.setOnAction(e ->{
			if(!checkValues())
			{
				return;
			}
			gui.interrupt();
			setValues();
			window.close();
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
		Scene scene = new Scene(gridPane);
		scene.getStylesheets().add(this.getClass().getResource("OptionsStyleSheet.css").toExternalForm());
		window.setScene(scene);
		window.show();
	}
	
	public void loadRegion()
	{
		SavedRegion sr = savedRegionsChoiceBox.getValue();
		iterationsField.setText("" + sr.iterations);
		precisionField.setText("" + sr.precision);
		threadCountField.setText("" + sr.threadCount);
		
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
		centerValue.setText(currentRegion.getCenterX().stripTrailingZeros().toPlainString()+ " + "
				+ currentRegion.getCenterY().stripTrailingZeros().toPlainString() + "i");
		boxValue.setText(currentRegion.x1.subtract(currentRegion.x2).abs().stripTrailingZeros().toPlainString() + "x" +
				currentRegion.y1.subtract(currentRegion.y2).abs().stripTrailingZeros().toPlainString());
		gui.loggedRegions = new ArrayList<>();
	}
	
	public void setValues()
	{
		gui.threadCount = Integer.parseInt(threadCountField.getText());
		gui.iterations = Integer.parseInt(iterationsField.getText());
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
