package fx;

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
	Label centerValue;
	Label boxValue;
	TextField threadCountField;
	TextField iterationsField;
	TextField precisionField;
	ChoiceBox<String> savedRegionsChoiceBox;
	ChoiceBox<String> colorChoiceBox;
	RadioButton arbitraryPrecision;
	RadioButton doublePrecision;
	public OptionsEditor(MainGUI gui)
	{
		this.gui = gui;
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
		savedRegionsChoiceBox = new ChoiceBox<String>(FXCollections.observableArrayList(
				"Region1",
				"Region2",
				"Region3"));
		colorChoiceBox = new ChoiceBox<String>(FXCollections.observableArrayList(
				"Rainbow",
				"Gothic Black",
				"Winter Wonderland"));
		colorChoiceBox.setValue(gui.color);
		
		
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
		
		Label set = new Label();
		Label seed = new Label();
		if(gui.julia)
		{
			set.setText("Julia Set");
			seed.setText(gui.juliaSeed.toString());
		}
		else
		{
			set.setText("Mandelbrot Set");
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
		
		
		
		applyAndRerenderButton.setOnAction(e->{
			if(!checkValues())
			{
				return;
			}
			gui.interrupt();
			setValues();
			gui.drawSet();
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
		
		
		//gridPane.setGridLinesVisible(true);
		Scene scene = new Scene(gridPane);
		scene.getStylesheets().add(this.getClass().getResource("OptionsStyleSheet.css").toExternalForm());
		window.setScene(scene);
		window.show();
	}
	
	public void setValues()
	{
		gui.threadCount = Integer.parseInt(threadCountField.getText());
		gui.iterations = Integer.parseInt(iterationsField.getText());
		gui.precision = Integer.parseInt(precisionField.getText());
		gui.color = colorChoiceBox.getValue();
		gui.arbitraryPrecision = arbitraryPrecision.isSelected();
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
