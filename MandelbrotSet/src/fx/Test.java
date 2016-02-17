package fx;

import java.io.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 *
 * @author Ezra
 *
 */
public class Test extends Application{
	/**
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		launch(args);
	}
	
	public void start(Stage stage){
		Parent root = null;
		try {
			root=FXMLLoader.load(this.getClass().getResource("/fx/OptionsEditor.fxml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stage.setScene(new Scene(root));
		stage.show();
		Image image;
	}
	
}