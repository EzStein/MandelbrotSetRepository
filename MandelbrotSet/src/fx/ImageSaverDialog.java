package fx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.*;

/**
 * A dialog box that will save the current image.
 * @author Ezra
 *
 */
public class ImageSaverDialog
{
	private TextField sizeField;
	private ChoiceBox<String> typeChooser;
	private MainGUI gui;
	private Stage window;
	private boolean chooseFile;
	private CallableClass call;
	
	/**
	 * Initializes this dialog box with a reference to the mainGui.
	 * @param gui	A reference to the main gui.
	 */
	public ImageSaverDialog(MainGUI gui)
	{
		this.gui = gui;
	}
	
	/**
	 * 
	 * @param chooseFile	True if the user should choose the file himself.
	 * @param call			A class whose call method is invoked when the image has been written succesfully to a file.
	 */
	public void showSaverDialog(boolean chooseFile, CallableClass call)
	{
		this.chooseFile = chooseFile;
		this.call = call;
		window = new Stage();
		window.setTitle("Image Saver");
		window.initModality(Modality.APPLICATION_MODAL);
		GridPane layout = new GridPane();
		layout.setHgap(10);
		layout.setVgap(10);
		layout.setPadding(new Insets(25,25,25,25));
		Scene scene = new Scene(layout);
		Label sizeLabel = new Label("Size:");
		Label typeLabel = new Label("Image Type:");
		sizeField = new TextField();
		
		ArrayList<String> writableImages = new ArrayList<String>();
		for(String type : ImageIO.getWriterFormatNames())
		{
			if(!Character.isUpperCase(type.charAt(0))){
				writableImages.add(type);
			}
		}
		typeChooser = new ChoiceBox<String>(FXCollections.observableArrayList(writableImages));
		typeChooser.setValue("png");
		
		Button cancelButton = new Button("Cancel");
		Button saveImageButton = new Button("Save Image");
		
		layout.add(sizeLabel, 0, 0);
		layout.add(typeLabel, 0, 1);
		layout.add(cancelButton, 0, 2);
		
		layout.add(sizeField, 1, 0);
		layout.add(typeChooser, 1, 1);
		layout.add(saveImageButton, 1, 2);
		
		cancelButton.setOnAction(e ->window.close());
		saveImageButton.setOnAction(e -> {
			if(!checkValues())
			{
				return;
			}
			saveImage();
			
		});
		window.setScene(scene);
		window.show();
	}
	
	/**
	 * Opens a file chooser and initializes an image saver thread and an updater thread to mark the progress.
	 */
	public void saveImage()
	{
		File file = null;
		String imageType = typeChooser.getValue();
		if(!chooseFile)
		{
			try {
				file = new File(Locator.locateFile("tmp/uploadFile." + imageType));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			FileChooser fc = new FileChooser();
			fc.setTitle("Save Image");
			
			if((file = fc.showSaveDialog(window)) == null)
			{
				return;
			}
			
			String fileName = file.getAbsolutePath();
			if(! fileName.endsWith("." + imageType))
			{
				file = new File(fileName + "." + imageType);
			}
		}
		
		Calculator calc = new Calculator(gui.mainCalculator.getColorFunction());
		ImageGenerator imageGen = new ImageGenerator(calc);
		new Thread(imageGen).start();
		Updater updater = new Updater(calc, imageGen, file, imageType, new ProgressMonitor());
		new Thread(updater).start();
		window.close();
	}
	
	/**
	 * Validates the values. Returns true only if all values are valid.
	 * @return true if all values are valid. False otherwise.
	 */
	public boolean checkValues()
	{
		int size = 0;
		boolean returnValue = true;
		try
		{
			size = Integer.parseInt(sizeField.getText());
		}
		catch(NumberFormatException nfe)
		{
			sizeField.setStyle("-fx-background-color:red");
			returnValue = false;
		}
		if(size<=0 || size>=6000)
		{
			sizeField.setStyle("-fx-background-color:red");
			returnValue = false;
		}
		return returnValue;
	}
	
	/**
	 * A class that updates the progress monitor and when it is done, it calls the getImage method and writes it to a file.
	 * @author Ezra Stein
	 * @version 1.0
	 * @since 2015
	 */
	private class Updater implements Runnable
	{
		private Calculator calc;
		private ProgressMonitor pm;
		private File file;
		private ImageGenerator imageGen;
		private String imageType;
		public Updater(Calculator calc, ImageGenerator imageGen, File file, String imageType, ProgressMonitor pm)
		{
			this.calc = calc;
			this.file = file;
			this.imageGen = imageGen;
			this.pm = pm;
			this.imageType = imageType;
		}
		
		public void run()
		{
			Platform.runLater(()->{
				pm.show();
				});
			while(calc.getPixelsCalculated() < Math.pow(Integer.parseInt(sizeField.getText()),2))
			{
				if(pm.isCanceled())
				{
					calc.setInterrupt(true);
					return;
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Platform.runLater(()->{
					pm.update(calc.getPixelsCalculated()/Math.pow(Integer.parseInt(sizeField.getText()),2));
					});
			}
			
			
			try
			{
				ImageIO.write(SwingFXUtils.fromFXImage(imageGen.getImage(),null), imageType, file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Platform.runLater(() -> {pm.close();});
			call.call();
		}
	}
	
	/**
	 * A progress monitor that displays the progress of the image saver.
	 * @author Ezra Stein
	 * @version 1.0
	 * @since 2015
	 *
	 */
	private class ProgressMonitor
	{
		private boolean canceled = false;
		private ProgressBar progressBar;
		private ProgressIndicator progressIndicator;
		private Stage stage;
		
		public void show()
		{
			stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			VBox layout = new VBox(10);
			layout.setPadding(new Insets(15,15,15,15));
			HBox top = new HBox(10);
			Button cancelButton = new Button("Cancel");
			layout.getChildren().add(top);
			layout.getChildren().add(cancelButton);
			progressBar = new ProgressBar();
			progressIndicator = new ProgressIndicator();
			top.getChildren().addAll(progressBar, progressIndicator);
			
			stage.setOnCloseRequest(e ->{
				e.consume();
				cancel();
				close();
			});
			cancelButton.setOnAction(e->{
				cancel();
				close();
			});
			
			
			Scene scene = new Scene(layout);
			stage.setScene(scene);
			stage.show();
		}
		
		public void update(double progress)
		{
			progressBar.setProgress(progress);
			progressIndicator.setProgress(progress);
		}
		
		public void cancel()
		{
			canceled = true;
		}
		
		public void close()
		{
			stage.close();
		}
		
		public boolean isCanceled()
		{
			return canceled;
		}
	}
	
	/**
	 * A runnable that calculates the image. The getImage() method may be used to access this image.
	 * @author Ezra Stein
	 * @version 1.0
	 * @since 2015.
	 *
	 */
	private class ImageGenerator implements Runnable
	{
		private Calculator calc;
		private WritableImage image;
		public ImageGenerator(Calculator calc)
		{
			this.calc = calc;
		}
		
		@Override
		public void run()
		{
			Region<Integer> pixelRegion = new Region<Integer>(0,0, Integer.parseInt(sizeField.getText()),Integer.parseInt(sizeField.getText()));
			
			if(gui.julia)
			{
				image = calc.generateJuliaSet(gui.juliaSeed, pixelRegion, gui.currentRegion, pixelRegion, gui.iterations, gui.arbitraryPrecision, gui.precision);
			}
			else
			{
				image = calc.generateSet(pixelRegion, gui.currentRegion, pixelRegion, gui.iterations, gui.arbitraryPrecision, gui.precision);
			}	
		}
		
		public WritableImage getImage()
		{
			return image;
		}
		
	}
}
