package fx;

import java.io.*;
import java.math.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;

import colorFunction.*;
import javafx.application.*;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
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
	private boolean currentJulia;
	private ComplexBigDecimal currentSeed;
	private int tabNumber;
	private ArrayList<CustomColorFunction> savedColors;
	private TextField uploadNameField, uploadAuthorField;
	private TextArea uploadDescriptionArea;
	private ChoiceBox<String> uploadTypeChoiceBox;
	
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
		readFiles();
		buildEditDialog();
		resetValues();
		window.show();
	}
	
	@SuppressWarnings("unchecked")
	private void readFiles()
	{
		ObjectInputStream in = null;
		ObjectInputStream colorIn = null;
		
		savedRegions = new ArrayList<SavedRegion>();
		savedColors = new ArrayList<CustomColorFunction>();
		try
		{
			colorFile = new File(Locator.locateFile("SavedColors.txt"));
			colorIn = new ObjectInputStream(new FileInputStream(colorFile));
			savedColors = (ArrayList<CustomColorFunction>) colorIn.readObject();
			
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
		});
		BorderPane layout = new BorderPane();
		
		TabPane tabPane = new TabPane();
		tabPane.getTabs().add(buildOptionsTab());
		tabPane.getTabs().add(buildColorTab());
		tabPane.getTabs().add(buildUploadTab());
		layout.setCenter(tabPane);
		tabPane.getSelectionModel().select(tabNumber);
		
		HBox buttonBox = new HBox(10);
		buttonBox.setPadding(new Insets(10,10,10,10));
		buttonBox.getChildren().addAll(buildSaveButton(), buildApplyAndRerenderButton(), buildApplyButton(), buildCancelButton());
		layout.setBottom(buttonBox);
		
		Scene scene = new Scene(layout);
		scene.getStylesheets().add(this.getClass().getResource("OptionsStyleSheet.css").toExternalForm());
		window.setScene(scene);
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
			colorChoiceBox.getItems().remove(colorToRemove);
			
			try
			{
				colorOut = new ObjectOutputStream(new FileOutputStream(colorFile));
				colorOut.writeObject(savedColors);
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
		colorChoiceBox = new ChoiceBox<CustomColorFunction>(FXCollections.observableArrayList(savedColors));
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
		stopList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Stop>(){

			@Override
			public void changed(ObservableValue<? extends Stop> observable, Stop oldValue, Stop newValue) {
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
		colorChoiceBox.getItems().add(color);
		colorChoiceBox.setValue(color);
		savedColors.add(color);
		try
		{
			colorOut = new ObjectOutputStream(new FileOutputStream(colorFile));
			colorOut.writeObject(savedColors);
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
		GridPane grid;
		grid= new GridPane();
		grid.setVgap(10);
		grid.setHgap(10);
		grid.setPadding(new Insets(30,30,30,30));
		
		uploadNameField = new TextField();
		uploadAuthorField = new TextField();
		uploadDescriptionArea = new TextArea();
		
		grid.add(new Label("Name:"), 0, 0);
		grid.add(uploadNameField,0,1);
		grid.add(new Label("Author (leave blank for anonymous):"), 0, 2);
		grid.add(uploadAuthorField, 0, 3);
		grid.add(new Label("Description:"), 0, 4);
		grid.add(uploadDescriptionArea, 0, 5);
		grid.add(buildUploadButton(), 0, 6);
		
		grid.add(buildUploadTypeChoiceBox(), 1, 0);
		tab.setContent(grid);
		return tab;
		
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
		if(!checkUploadValues()){
			return;
		}
		new ImageSaverDialog(gui).showSaverDialog(false, ()->{
			uploadImage();
			
		});
	}
	
	private boolean checkUploadValues() {
		return !(uploadNameField.getText() =="" || uploadDescriptionArea.getText() =="");
	}
	
	private void uploadImage(){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
		HttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost("http://www.ezstein.xyz/uploader.php");
		HttpResponse response = null;
		BufferedReader in = null;
		try {
			//String imageType = "png";
			//File file = new File(Locator.locateFile("tmp/uploadFile." + imageType));
			File[] files = new File(Locator.getBaseDirectoryPath() + File.separator + "tmp").listFiles(new FilenameFilter(){

				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith("uploadFile.");
				}
			});
			File file = files[0];
			String name = file.getName();
			String imageType = name.substring(name.lastIndexOf(".") + 1);
			
			HttpEntity entity = MultipartEntityBuilder.create().addBinaryBody(
					"uploadFile", file, ContentType.create("image/" + imageType), file.getName())
					.addTextBody("pass", "uploaderPassword")
					.addTextBody("Name", uploadNameField.getText())
					.addTextBody("Author", (uploadAuthorField.getText().equals("") ? "Anonymous" : uploadAuthorField.getText()))
					.addTextBody("Description", uploadDescriptionArea.getText())
					.addTextBody("UploadDate", dateFormat.format(new Date()))
					.addTextBody("SetType", gui.julia?"J":"M")
					.build();
			post.setEntity(entity);
			response = client.execute(post);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			
			String input;
			while((input = in.readLine()) !=null)
			{
				System.out.println(input);
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
			
				try {
					if(in!=null){
						in.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		Platform.runLater(()->{
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setContentText("Upload Success.");
			alert.show();
			});
		
	}
	
	private void showUploadRegionDialog() {
		
	}
	private void showUploadColorDialog() {
	
	}
	
	private ChoiceBox<String> buildUploadTypeChoiceBox()
	{
		uploadTypeChoiceBox =
				new ChoiceBox<String>(FXCollections.observableList(
						new ArrayList<String>(Arrays.asList("Image","Region","Color"))));
		uploadTypeChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
			}
		});
		uploadTypeChoiceBox.getSelectionModel().select(0);
		return  uploadTypeChoiceBox;
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
			colorChoiceBox.getItems().add(sr.colorFunction);
			savedColors.add(sr.colorFunction);
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
