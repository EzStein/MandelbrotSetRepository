package fx;

import java.io.File;
import java.io.IOException;

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

public class ImageSaverDialog
{
	private TextField sizeField;
	private ChoiceBox<String> typeChooser;
	private MainGUI gui;
	private Stage window;
	public ImageSaverDialog(MainGUI gui)
	{
		this.gui = gui;
	}
	
	public void showSaverDialog()
	{
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
		typeChooser = new ChoiceBox<String>(FXCollections.observableArrayList(
				ImageIO.getWriterFormatNames()));
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
	
	public void saveImage()
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Save Image");
		File file;
		if((file = fc.showSaveDialog(window)) == null)
		{
			return;
		}
		String imageType = typeChooser.getValue();
		String fileName = file.getAbsolutePath();
		if(! fileName.endsWith("." + imageType))
		{
			file = new File(fileName + "." + imageType);
		}
		Calculator calc = new Calculator(gui.calculator.getColorFunction());
		ImageGenerator imageGen = new ImageGenerator(calc);
		new Thread(imageGen).start();
		Updater updater = new Updater(calc, imageGen, file, new ProgressMonitor());
		new Thread(updater).start();
	}
	
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
	
	private class Updater implements Runnable
	{
		private Calculator calc;
		private ProgressMonitor pm;
		private File file;
		private ImageGenerator imageGen;
		public Updater(Calculator calc, ImageGenerator imageGen, File file, ProgressMonitor pm)
		{
			this.calc = calc;
			this.file = file;
			this.imageGen = imageGen;
			this.pm = pm;
		}
		
		public void run()
		{
			Platform.runLater(()->{
				pm.show();
				});
			while(calc.getPixelsCalculated()<Math.pow(Integer.parseInt(sizeField.getText()),2))
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
				ImageIO.write(SwingFXUtils.fromFXImage(imageGen.getImage(),null), typeChooser.getValue(), file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Platform.runLater(() -> {pm.close();});
		}
	}
	
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
