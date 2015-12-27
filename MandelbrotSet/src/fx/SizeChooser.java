package fx;

import java.util.*;

import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

/**
 * A dialog box used at the beginning of the program to choose the size of the mainCanvas
 * @author Ezra Stein
 * @version 1.0
 * @since
 *
 */
public class SizeChooser
{
	Optional<Integer> result;
	TextField inputField;
	
	
	/**
	 * Shows the dialog box which prompts the user for the size.
	 * Will block until it closes.
	 * @return an optional which is empty if the user exits the dialog box and contains the size otherwise.
	 */
	public Optional<Integer> showAndWait()
	{
		Stage window = new Stage();
		window.setResizable(false);
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
	
	/**
	 * Validates the input values.
	 * @return false if the values are not valid, true otherwise.
	 */
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
