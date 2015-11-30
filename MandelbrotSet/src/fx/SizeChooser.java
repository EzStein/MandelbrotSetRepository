package fx;

import java.util.*;

import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class SizeChooser
{
	Optional<Integer> result;
	TextField inputField;
	public Optional<Integer> showAndWait()
	{
		Stage window = new Stage();
		window.initModality(Modality.APPLICATION_MODAL);
		window.setOnCloseRequest(e ->{
			result = Optional.empty();
		});
		window.setTitle("Size");
		GridPane layout = new GridPane();
		layout.setHgap(10);
		layout.setVgap(10);
		layout.setPadding(new Insets(25,25,25,25));

		Button cancelButton = new Button("Cancel");
		Button okButton = new Button("OK");
		
		inputField = new TextField();
		Label sizeLabel = new Label("Choose the size for your viewer: ");
		
		layout.add(sizeLabel, 0, 0);
		layout.add(inputField, 0, 1);
		layout.add(cancelButton, 1, 0);
		layout.add(okButton, 1, 1);
		
		cancelButton.setOnAction(e ->{
			result = Optional.empty();
			window.close();
			
		});
		
		okButton.setOnAction(e ->{
			if(!checkValues())
			{
				return;
			}
			result = Optional.of(Integer.parseInt(inputField.getText()));
			window.close();
		});
		
		Scene scene = new Scene(layout);
		window.setScene(scene);
		window.showAndWait();
		return result;
	}
	
	public boolean checkValues()
	{
		int input;
		try
		{
			input = Integer.parseInt(inputField.getText());
		}
		catch(NumberFormatException nfe)
		{
			return false;
		}
		if(input<=100 || input >=1600)
		{
			return false;
		}
		return true;
	}
}
