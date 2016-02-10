package fx;

import java.io.*;
import java.math.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.entity.mime.*;
import org.apache.http.impl.client.*;
import colorFunction.*;
import javafx.application.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.control.ButtonBar.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import javafx.util.*;

/**
 * This class is used to control all the options of the program including colors iterations threads etc.
 * @author Ezra
 *
 */
public class OptionsEditorController
{
	private MainGUI gui;
	private Stage window;
	@FXML private Label centerLabel;
	@FXML private Label setLabel;
	@FXML private Label seedLabel;
	@FXML private Label dimensionsLabel;
	@FXML private TextField threadsField;
	@FXML private TextField iterationsField;
	@FXML private TextField precisionField;
	@FXML private CheckBox autoIterationsCheckBox;
	@FXML private ComboBox<SavedRegion> savedRegionsComboBox;
	@FXML private ComboBox<SavedRegion> uploadRegionComboBox;
	@FXML private ComboBox<CustomColorFunction> colorComboBox;
	@FXML private ComboBox<CustomColorFunction> uploadColorComboBox;
	@FXML private TextField rangeField;
	@FXML private Slider colorPositionSlider;
	@FXML private ListView<Stop> stopListView;
	@FXML private Button removeColorButton;
	@FXML private Button removeStopButton;
	@FXML private RadioButton arbitraryPrecisionRadioButton;
	@FXML private RadioButton doublePrecisionRadioButton;
	@FXML private Rectangle gradientRectangle;
	@FXML private ColorPicker colorPicker;
	@FXML private TextField uploadNameField;
	@FXML private TextField uploadAuthorField;
	@FXML private TextArea uploadDescriptionArea;
	@FXML private ComboBox<String> uploadTypeComboBox;
	@FXML private TableView<ImageRow> downloadImageTable;
	@FXML private TableView<RegionRow> downloadRegionTable;
	@FXML private TableView<ColorRow> downloadColorTable;
	@FXML private Button uploadButton;
	@FXML private TabPane tables;
	@FXML private TabPane tabs;
	@FXML private ImageView imageView;
	
	private ObjectOutputStream out, colorOut;
	private SimpleListProperty<SavedRegion> savedRegions;
	private Region<BigDecimal> currentRegion;
	private boolean currentJulia;
	private ComplexBigDecimal currentSeed;
	private int tabNumber;
	private SimpleListProperty<CustomColorFunction> savedColors;
	
	private Statement stmt = null;
	private Statement stmt2 = null;
	private Thread databaseUpdater;
	private Connection conn;
	
	public void initializeController(MainGUI gui,int tabNumber, Stage window){
		this.gui = gui;
		this.window = window;
		this.tabNumber = tabNumber;
		stmt = null;
		stmt2 = null;
		
		addListeners();
		readFiles();
		buildThreads();
		initializeFXML();
		resetValues();
	}
	
	private void initializeFXML() {
		uploadTypeComboBox.getItems().addAll("Image","Region","Color");
		uploadTypeComboBox.getSelectionModel().select(0);
		savedRegionsComboBox.setItems(savedRegions);
		colorComboBox.setItems(savedColors);
		uploadRegionComboBox.setItems(savedRegions);
		uploadRegionComboBox.getSelectionModel().select(0);
		uploadColorComboBox.setItems(savedColors);
		uploadColorComboBox.getSelectionModel().select(0);
		tabs.getSelectionModel().select(tabNumber);
		uploadColorComboBox.setDisable(true);
		uploadRegionComboBox.setDisable(true);
	}
	
	private void buildThreads()
	{
		databaseUpdater = new Thread(()->{
			while(true)
			{
				if(!isConnectedToInternet())
				{
					Platform.runLater(()->{
						downloadRegionTable.setItems(FXCollections.observableArrayList(new ArrayList<RegionRow>()));
						downloadColorTable.setItems(FXCollections.observableArrayList(new ArrayList<ColorRow>()));
						downloadImageTable.setItems(FXCollections.observableArrayList(new ArrayList<ImageRow>()));
						uploadButton.setDisable(true);
						uploadButton.setText("Check Internet Connection");
					});

					stmt = null;
				}
				else
				{

					if(stmt == null)
					{
						openConnection();
					}
					Platform.runLater(()->{
						uploadButton.setDisable(false);
						uploadButton.setText("Upload");
					});
					connect();
				}
				try
				{
					Thread.sleep(1000);
				}
				catch(InterruptedException ie)
				{
					break;
				}
			}
		});
		databaseUpdater.start();
	}

	private void initializeValues()
	{
		rangeField.setText(colorComboBox.getValue().getRange()+"");
		stopListView.getItems().remove(0, stopListView.getItems().size());
		for(Stop stop : colorComboBox.getValue().getStops())
		{
			stopListView.getItems().add(stop);
		}
	}
	
	private void openConnection()
	{
		//THIS DISPLAYS PASS IN PLAINTEXT!!!!
		try {
			conn = DriverManager.getConnection("jdbc:mysql://www.ezstein.xyz:3306/WebDatabase", "java", "javaPass");
			stmt = conn.createStatement();
			Connection conn2 = DriverManager.getConnection("jdbc:mysql://www.ezstein.xyz:3306/WebDatabase", "java", "javaPass");
			stmt2 = conn2.createStatement();

		} catch (SQLException e) {
			downloadRegionTable.setItems(FXCollections.observableArrayList(new ArrayList<RegionRow>()));
			downloadColorTable.setItems(FXCollections.observableArrayList(new ArrayList<ColorRow>()));
			downloadImageTable.setItems(FXCollections.observableArrayList(new ArrayList<ImageRow>()));
		}
	}

	private void connect()
	{
		try
		{
			if(!isConnectedToInternet())
			{
				return;
			}
			ArrayList<ImageRow> dataImage = new ArrayList<ImageRow>();
			ResultSet set = stmt.executeQuery("SELECT * FROM Images");
			while(set.next()){
					ImageRow imageRow = new ImageRow(
							set.getInt("ID"),
							set.getString("Name"),
							set.getString("Author"),
							set.getString("Description"),
							set.getString("file"),
							set.getString("SetType"),
							set.getInt("Width"),
							set.getInt("Height"),
							set.getInt("Size"),
							set.getInt("Date"),
							set.getString("FileType"));
					if(!downloadImageTable.getItems().contains(imageRow))
					{
						imageRow.setImage(downloadImageAsObject("https://www.ezstein.xyz/uploads/images/thumbnails/" + set.getString("file")));
						dataImage.add(imageRow);
					}
				}
			set.close();
			Set<ImageRow> imageRowA = new HashSet<ImageRow>(dataImage);
			Set<ImageRow> imageRowB = new HashSet<ImageRow>(downloadImageTable.getItems());
			/*Subtracts B From A to find difference in the two sets*/
			imageRowA.removeAll(imageRowB);
			downloadImageTable.getItems().addAll(imageRowA);

			if(!isConnectedToInternet())
			{
				return;
			}
			ArrayList<RegionRow> dataRegion = new ArrayList<RegionRow>();
			set = stmt.executeQuery("SELECT * FROM Regions");
			while(set.next()){
					dataRegion.add(new RegionRow(
							set.getInt("ID"),
							set.getString("Name"),
							set.getString("Author"),
							set.getString("Description"),
							set.getString("file"),
							set.getInt("Size"),
							set.getInt("Date"),
							set.getInt("HashCode")
							));
			}
			set.close();
			Set<RegionRow> regionRowA = new HashSet<RegionRow>(dataRegion);
			Set<RegionRow> regionRowB = new HashSet<RegionRow>(downloadRegionTable.getItems());
			/*Subtracts B From A to find difference in the two sets*/
			regionRowA.removeAll(regionRowB);
			downloadRegionTable.getItems().addAll(regionRowA);

			if(!isConnectedToInternet())
			{
				return;
			}
			ArrayList<ColorRow> dataColor = new ArrayList<ColorRow>();
			set = stmt.executeQuery("SELECT * FROM Colors");
			while(set.next()){
					dataColor.add(new ColorRow(
							set.getInt("ID"),
							set.getString("Name"),
							set.getString("Author"),
							set.getString("Description"),
							set.getString("file"),
							set.getInt("Size"),
							set.getInt("Date"),
							set.getInt("HashCode")));
				}
			set.close();
			Set<ColorRow> colorRowA = new HashSet<ColorRow>(dataColor);
			Set<ColorRow> colorRowB = new HashSet<ColorRow>(downloadColorTable.getItems());

			/*Subtracts B From A to find difference in the two sets*/
			colorRowA.removeAll(colorRowB);
			downloadColorTable.getItems().addAll(colorRowA);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void readFiles()
	{
		ObjectInputStream in = null;
		ObjectInputStream colorIn = null;
		savedRegions = null;
		savedColors = null;

		
		try
		{
			colorIn = new ObjectInputStream(new FileInputStream(Locator.locateFile("SavedColors.txt").toFile()));
			savedColors = new SimpleListProperty<CustomColorFunction>(FXCollections.observableList((ArrayList<CustomColorFunction>) colorIn.readObject()));

			in = new ObjectInputStream(new FileInputStream(Locator.locateFile("SavedRegions.txt").toFile()));
			savedRegions = new SimpleListProperty<SavedRegion>(FXCollections.observableList((ArrayList<SavedRegion>)in.readObject()));
		}
		catch(EOFException eofe)
		{
			/*File Empty And inputStream is null*/
			savedRegions = new SimpleListProperty<SavedRegion>(FXCollections.observableList(new ArrayList<SavedRegion>()));
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
		
		
		
		
		savedColors.addListener(new ChangeListener<ObservableList<CustomColorFunction>>(){

			@Override
			public void changed(ObservableValue<? extends ObservableList<CustomColorFunction>> observable,
					ObservableList<CustomColorFunction> oldValue, ObservableList<CustomColorFunction> newValue) {

				colorComboBox.setItems(savedColors);
				uploadColorComboBox.setItems(savedColors);
				try
				{
					colorOut = new ObjectOutputStream(new FileOutputStream(Locator.locateFile("SavedColors.txt").toFile()));
					colorOut.writeObject(new ArrayList<CustomColorFunction>( Arrays.asList(savedColors.toArray(new CustomColorFunction[0])) ));
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
				colorComboBox.getSelectionModel().selectLast();
				uploadColorComboBox.getSelectionModel().selectLast();
			}

		});

		savedRegions.addListener(new ChangeListener<ObservableList<SavedRegion>>(){

			@Override
			public void changed(ObservableValue<? extends ObservableList<SavedRegion>> observable,
					ObservableList<SavedRegion> oldValue, ObservableList<SavedRegion> newValue) {

				savedRegionsComboBox.setItems(savedRegions);
				uploadRegionComboBox.setItems(savedRegions);
				try
				{
					/*Overwrites File*/
					out = new ObjectOutputStream(new FileOutputStream(Locator.locateFile("SavedRegions.txt").toFile()));
					out.writeObject(new ArrayList<SavedRegion>( Arrays.asList(savedRegions.toArray(new SavedRegion[0])) ));
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
			}

		});
	}

	public void close(){
		
		databaseUpdater.interrupt();
		try {
			databaseUpdater.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try{
			if(stmt != null)
			{
				stmt.close();
			}
			if(stmt2 != null)
			{
				stmt2.close();
			}
			if(conn !=null)
			{
				conn.close();
			}
		} catch(SQLException sqle){
			sqle.printStackTrace();
		}

	}
	
	private void addListeners(){
		builduploadRegionComboBox();
		buildStopList();
		buildColorPositionSlider();
		buildColorComboBox();
		buildSavedRegionsComboBox();
		
		window.setOnCloseRequest(e ->{
			ResultType result = askToSaveColor();
			if(result == ResultType.CANCEL)
			{
				e.consume();
			}
			close();
		});
		
		downloadImageTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ImageRow>(){

			@Override
			public void changed(ObservableValue<? extends ImageRow> observable, ImageRow oldValue, ImageRow newValue) {
				imageView.setImage(downloadImageTable.getSelectionModel().getSelectedItem().getImage());
			}
		});
	}
	
	private void buildSavedRegionsComboBox(){
		savedRegionsComboBox.setConverter(new StringConverter<SavedRegion>(){

			@Override
			public String toString(SavedRegion region) {
				return region.name;
			}

			@Override
			public SavedRegion fromString(String string) {
				// TODO Auto-generated method stub
				return null;
			}
			
		});
	}
	
	private void buildColorComboBox(){
		colorComboBox.setConverter(new StringConverter<CustomColorFunction>(){

			@Override
			public String toString(CustomColorFunction color) {
				return color.getName();
			}
			@Override
			public CustomColorFunction fromString(String string) {
				// TODO Auto-generated method stub
				return null;
			}
			
		});
		
	}

	private void buildColorPositionSlider()
	{
		colorPositionSlider.valueProperty().addListener(new ChangeListener<Number>(){

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				Platform.runLater(()->{
					int index = stopListView.getSelectionModel().getSelectedIndex();
					if(index < 0)
					{
						/*Nothing selected*/
						return;
					}
					stopListView.getItems().set(index, new Stop(colorPositionSlider.getValue(), stopListView.getItems().get(index).getColor()));
				});
			}
		});
	}

	private void builduploadRegionComboBox(){
		uploadRegionComboBox.setConverter(new StringConverter<SavedRegion>(){

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
	}
	
	private void buildStopList()
	{
		
		stopListView.setCellFactory(new Callback<ListView<Stop>, ListCell<Stop>>(){
			@Override
			public ListCell<Stop> call(ListView<Stop> param) {
				return new CustomListCell();
			}

		});
		stopListView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Stop stop = stopListView.getSelectionModel().getSelectedItem();
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
		stopListView.getItems().addListener(new ListChangeListener<Stop>(){

			@Override
			public void onChanged(ListChangeListener.Change<? extends Stop> c) {
				gradientRectangle.setFill(new LinearGradient(0,0.5,1,0.5,true, CycleMethod.NO_CYCLE, createGradientStops()));

			}

		});
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
			for(CustomColorFunction c : colorComboBox.getItems())
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
		CustomColorFunction color = new CustomColorFunction(new ArrayList<Stop>(stopListView.getItems()),Integer.parseInt(rangeField.getText()), name);
		savedColors.add(color);
		
		return true;
	}
	
	private boolean validateForSaveColor()
	{
		TreeSet<Double> set = new TreeSet<Double>();
		for(Stop stop : stopListView.getItems())
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


		if(stopListView.getItems().size()<2)
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("There must be atleast two colors positioned at 0 and 1 (beginning and end)");
			alert.show();
			return false;
		}



		boolean validStart = false;
		boolean validEnd = false;
		for(Stop stop : stopListView.getItems())
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
	
	private ArrayList<Stop> createGradientStops()
	{

		ArrayList<Stop> returnValue = new ArrayList<Stop>(Arrays.asList(stopListView.getItems().toArray(new Stop[1])));
		return returnValue;
	}
	
	private void showUploadImageDialog() {
		System.out.println(checkUploadValues());
		if(!checkUploadValues()){
			return;
		}
		new ImageSaverDialog(gui).showSaverDialog(false, ()->{
			uploadImage();
		});
	}

	private boolean checkUploadValues() {
		if(uploadNameField.getText() == null){
			return false;
		}
		if((uploadNameField.getText().equals("") || uploadDescriptionArea.getText().equals(""))){
			return false;
		}
		if(uploadTypeComboBox.getSelectionModel().getSelectedItem().equals("Region"))
		{
			if(uploadRegionComboBox.getSelectionModel().getSelectedItem() == null)
			{
				return false;
			}
		}
		else if(uploadTypeComboBox.getSelectionModel().getSelectedItem().equals("Color"))
		{
			if(uploadColorComboBox.getSelectionModel().getSelectedItem() == null)
			{
				return false;
			}
		}
		return true;
	}

	private void uploadRegion(){
		SavedRegion sr = uploadRegionComboBox.getSelectionModel().getSelectedItem();
		if(existsInDatabase(sr)>=0)
		{
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setContentText("That region already exists in the database!");
			alert.show();
			return;
		}
		UploadDialog dialog = new UploadDialog();
		Platform.runLater(()->{
			dialog.show();
			dialog.getResponseLabel().setText("Uploading...");
		});
		HttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost("https://www.ezstein.xyz/serverScripts/regionUploader.php");
		HttpResponse response = null;
		ObjectOutputStream fileOut = null, fileOut2 = null;

		try
		{
			fileOut = new ObjectOutputStream(new FileOutputStream(Locator.locateUniqueFile("tmp/region/uploadFile.txt").toFile()));
			fileOut.writeObject(sr);
			fileOut2 = new ObjectOutputStream(new FileOutputStream(Locator.locateUniqueFile("tmp/color/uploadFile.txt").toFile()));
			fileOut2.writeObject(sr.colorFunction);

		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			try {
				if(fileOut != null)
				{
					fileOut.close();
				}
				if(fileOut2 != null)
				{
					fileOut2.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		BufferedReader in = null;
		File colorFile = null;
		File regionFile = null;
		try {
			regionFile = Locator.getUniqueFile("tmp/region").toFile();
			colorFile = Locator.getUniqueFile("tmp/color").toFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int idOfReplica;
		HttpEntity entity;
		if((idOfReplica = existsInDatabase(sr.colorFunction))>=0)
		{
			entity = MultipartEntityBuilder.create()
					.addTextBody("pass", "uploaderPassword")
					.addTextBody("Name", uploadNameField.getText())
					.addTextBody("Author", (uploadAuthorField.getText().equals("") ? "Anonymous" : uploadAuthorField.getText()))
					.addTextBody("Description", uploadDescriptionArea.getText())
					.addTextBody("RegionHashCode", sr.hashCode() + "")
					.addBinaryBody("region", regionFile, ContentType.TEXT_PLAIN, regionFile.getName())
					.addTextBody("Replica", "true")
					.addTextBody("ColorLink", idOfReplica + "")
					.build();
		}
		else
		{
			entity = MultipartEntityBuilder.create()
					.addTextBody("pass", "uploaderPassword")
					.addTextBody("Name", uploadNameField.getText())
					.addTextBody("Author", (uploadAuthorField.getText().equals("") ? "Anonymous" : uploadAuthorField.getText()))
					.addTextBody("Description", uploadDescriptionArea.getText())
					.addTextBody("RegionHashCode", sr.hashCode() + "")
					.addBinaryBody("region", regionFile, ContentType.TEXT_PLAIN, regionFile.getName())
					.addTextBody("ColorName", uploadRegionComboBox.getSelectionModel().getSelectedItem().colorFunction.getName())
					.addTextBody("ColorHashCode", sr.colorFunction.hashCode() + "")
					.addBinaryBody("color", colorFile, ContentType.TEXT_PLAIN, colorFile.getName())
					.addTextBody("Replica", "false")
					.build();
		}
		post.setEntity(entity);
		try{
			response = client.execute(post);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String s = "", input = "";
			while((input = in.readLine()) !=null)
			{
				s += input + "\n";
			}
			final String s1 = s;
			Platform.runLater(()->{
				dialog.getResponseLabel().setText(s1);
				dialog.enableClose();
			});
		}
		catch(ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally{
			try {
				if(in !=null)
				{
					in.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void uploadColor()
	{
		if(existsInDatabase(uploadColorComboBox.getSelectionModel().getSelectedItem())>=0)
		{
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setContentText("That color already exists in the database!");
			alert.show();
			return;
		}
		UploadDialog dialog = new UploadDialog();
		Platform.runLater(()->{
			dialog.show();
			dialog.getResponseLabel().setText("Uploading...");
		});

		HttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost("https://www.ezstein.xyz/serverScripts/colorUploader.php");
		HttpResponse response = null;
		ObjectOutputStream fileOut = null;
		try
		{
			fileOut = new ObjectOutputStream(new FileOutputStream(Locator.locateUniqueFile("tmp/color/uploadFile.txt").toFile()));
			fileOut.writeObject(uploadColorComboBox.getSelectionModel().getSelectedItem());

		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			try {
				if(fileOut != null)
				{
					fileOut.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		BufferedReader in = null;
		File colorFile = null;
		try {
			colorFile = Locator.getUniqueFile("tmp/color").toFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HttpEntity entity = MultipartEntityBuilder.create()
				.addTextBody("pass", "uploaderPassword")
				.addTextBody("Name", uploadNameField.getText())
				.addTextBody("Author", (uploadAuthorField.getText().equals("") ? "Anonymous" : uploadAuthorField.getText()))
				.addTextBody("Description", uploadDescriptionArea.getText())
				.addTextBody("ColorHashCode", uploadColorComboBox.getSelectionModel().getSelectedItem().hashCode() + "")
				.addBinaryBody("color", colorFile, ContentType.TEXT_PLAIN, colorFile.getName()).build();
		post.setEntity(entity);
		try{
			response = client.execute(post);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String s = "", input = "";
			while((input = in.readLine()) !=null)
			{
				s += input + "\n";
			}
			final String s1 = s;
			Platform.runLater(()->{
				dialog.getResponseLabel().setText(s1);
				dialog.enableClose();
			});
		}
		catch(ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally{
			try {
				if(in !=null)
				{
					in.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void uploadImage(){
		UploadDialog dialog = new UploadDialog();
		Platform.runLater(()->{
			dialog.show();
			dialog.getResponseLabel().setText("Uploading...");
		});

		HttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost("https://www.ezstein.xyz/serverScripts/imageUploader.php");
		HttpResponse response = null;
		BufferedReader in = null;


		ObjectOutputStream fileOut = null, fileOut2 = null;
		SavedRegion sr = new SavedRegion(uploadNameField.getText() + "_Region", gui.autoIterations, gui.iterations,
				gui.precision, gui.threadCount, gui.currentRegion,gui.arbitraryPrecision,
				gui.julia,gui.juliaSeed,gui.mainCalculator.getColorFunction());
		try
		{
			fileOut = new ObjectOutputStream(new FileOutputStream(Locator.locateUniqueFile("tmp/color/uploadFile.txt").toFile()));
			fileOut.writeObject(gui.mainCalculator.getColorFunction());
			fileOut2 = new ObjectOutputStream(new FileOutputStream(Locator.locateUniqueFile("tmp/region/uploadFile.txt").toFile()));
			fileOut2.writeObject(sr);
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			try {
				if(fileOut != null)
				{
					fileOut.close();
				}
				if(fileOut2 != null)
				{
					fileOut2.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		File imageFile = null, regionFile =null, colorFile=null;
		try
		{

			imageFile = Locator.getUniqueFile("tmp/image").toFile();
			regionFile = Locator.getUniqueFile("tmp/region").toFile();
			colorFile = Locator.getUniqueFile("tmp/color").toFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String name = imageFile.getName();
		String imageType = name.substring(name.lastIndexOf(".") + 1);


		int idOfReplicaRegion, idOfReplicaColor;
		idOfReplicaColor = existsInDatabase(sr.colorFunction);
		idOfReplicaRegion = existsInDatabase(sr);
		HttpEntity entity = null;
		if(idOfReplicaColor <0 && idOfReplicaRegion <0)
		{
			//no replica
			entity = MultipartEntityBuilder.create()
					.addTextBody("pass", "uploaderPassword")
					.addTextBody("Name", uploadNameField.getText())
					.addTextBody("Author", (uploadAuthorField.getText().equals("") ? "Anonymous" : uploadAuthorField.getText()))
					.addTextBody("Description", uploadDescriptionArea.getText())
					.addTextBody("SetType", gui.julia?"J":"M")
					.addBinaryBody("image", imageFile, ContentType.create("image/" + imageType), imageFile.getName())
					.addTextBody("RegionHashCode", sr.hashCode() + "")
					.addBinaryBody("region", regionFile, ContentType.TEXT_PLAIN, regionFile.getName())
					.addTextBody("ColorName", gui.mainCalculator.getColorFunction().getName())
					.addTextBody("ColorHashCode", gui.mainCalculator.getColorFunction().hashCode() + "")
					.addBinaryBody("color", colorFile, ContentType.TEXT_PLAIN, colorFile.getName())
					.addTextBody("ReplicaRegion", "false")
					.addTextBody("ReplicaColor", "false")
					.build();
		} else if(idOfReplicaColor >=0 && idOfReplicaRegion <0)
		{
			//color replica
			entity = MultipartEntityBuilder.create()
					.addTextBody("pass", "uploaderPassword")
					.addTextBody("Name", uploadNameField.getText())
					.addTextBody("Author", (uploadAuthorField.getText().equals("") ? "Anonymous" : uploadAuthorField.getText()))
					.addTextBody("Description", uploadDescriptionArea.getText())
					.addTextBody("SetType", gui.julia?"J":"M")
					.addBinaryBody("image", imageFile, ContentType.create("image/" + imageType), imageFile.getName())
					.addTextBody("RegionHashCode", sr.hashCode() + "")
					.addBinaryBody("region", regionFile, ContentType.TEXT_PLAIN, regionFile.getName())
					.addTextBody("LinkedColor", idOfReplicaColor + "")
					.addTextBody("ReplicaRegion", "false")
					.addTextBody("ReplicaColor", "true")
					.build();
		} else if(idOfReplicaColor <0 && idOfReplicaRegion >=0)
		{
			//region replica
			entity = MultipartEntityBuilder.create()
					.addTextBody("pass", "uploaderPassword")
					.addTextBody("Name", uploadNameField.getText())
					.addTextBody("Author", (uploadAuthorField.getText().equals("") ? "Anonymous" : uploadAuthorField.getText()))
					.addTextBody("Description", uploadDescriptionArea.getText())
					.addTextBody("SetType", gui.julia?"J":"M")
					.addBinaryBody("image", imageFile, ContentType.create("image/" + imageType), imageFile.getName())
					.addTextBody("LinkedRegion", idOfReplicaRegion + "")
					.addTextBody("ReplicaRegion", "true")
					.addTextBody("ReplicaColor", "false")
					.addTextBody("ColorName", gui.mainCalculator.getColorFunction().getName())
					.addTextBody("ColorHashCode", gui.mainCalculator.getColorFunction().hashCode() + "")
					.addBinaryBody("color", colorFile, ContentType.TEXT_PLAIN, colorFile.getName())
					.build();
		} else if(idOfReplicaColor >=0 && idOfReplicaRegion >=0)
		{
			//both replica
			entity = MultipartEntityBuilder.create()
					.addTextBody("pass", "uploaderPassword")
					.addTextBody("Name", uploadNameField.getText())
					.addTextBody("Author", (uploadAuthorField.getText().equals("") ? "Anonymous" : uploadAuthorField.getText()))
					.addTextBody("Description", uploadDescriptionArea.getText())
					.addTextBody("SetType", gui.julia?"J":"M")
					.addBinaryBody("image", imageFile, ContentType.create("image/" + imageType), imageFile.getName())
					.addTextBody("LinkedRegion", idOfReplicaRegion + "")
					.addTextBody("LinkedColor", idOfReplicaColor + "")
					.addTextBody("ReplicaColor", "true")
					.addTextBody("ReplicaRegion", "true")
					.build();
		}
		post.setEntity(entity);
		try {
			response = client.execute(post);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String s = "", input = "";
			while((input = in.readLine()) !=null)
			{
				s += input + "\n";
			}
			final String s1 = s;
			Platform.runLater(()->{
				dialog.getResponseLabel().setText(s1);
				dialog.enableClose();
			});

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

	}

	private void showUploadRegionDialog() {
		System.out.println(checkUploadValues());
		if(!checkUploadValues()){
			return;
		}
		uploadRegion();
	}

	private void showUploadColorDialog() {
		System.out.println(checkUploadValues());
		if(!checkUploadValues()){
			return;
		}
		uploadColor();
	}

	private boolean isConnectedToInternet()
	{
		try {
			InetAddress address = InetAddress.getByName("www.ezstein.xyz");
			return address.isReachable(20000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return false;

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
		for(CustomColorFunction cf: colorComboBox.getItems())
		{
			if(cf.getStops().equals(stopListView.getItems()))
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
		threadsField.setText(gui.threadCount + "");

		autoIterationsCheckBox.setSelected(gui.autoIterations);
		iterationsField.setDisable(gui.autoIterations);
		iterationsField.setText(gui.iterations + "");


		precisionField.setText(gui.precision + "");
		colorComboBox.setValue(gui.mainCalculator.getColorFunction());

		arbitraryPrecisionRadioButton.setSelected(gui.arbitraryPrecision);
		doublePrecisionRadioButton.setSelected(!gui.arbitraryPrecision);
		currentRegion = gui.currentRegion;
		currentJulia = gui.julia;
		currentSeed = gui.juliaSeed;

		if(currentJulia)
		{
			setLabel.setText("Julia set: ");
			seedLabel.setText(currentSeed.toString());
		}
		else
		{
			setLabel.setText("Mandelbrot set: ");
			seedLabel.setText("0+0i");
		}
		centerLabel.setText(currentRegion.getCenterX().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()+ " + "
				+ currentRegion.getCenterY().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString() + "i");
		dimensionsLabel.setText(currentRegion.x1.subtract(currentRegion.x2).abs().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString() + "x" +
				currentRegion.y1.subtract(currentRegion.y2).abs().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString());


		initializeValues();
	}

	/**
	 * Loads a region from the choice box and sets all the values of the options editor.
	 */
	public void loadRegion()
	{
		SavedRegion sr = savedRegionsComboBox.getValue();
		iterationsField.setText("" + sr.iterations);
		precisionField.setText("" + sr.precision);
		threadsField.setText("" + sr.threadCount);

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
			arbitraryPrecisionRadioButton.setSelected(true);
			doublePrecisionRadioButton.setSelected(false);
		}
		else
		{
			arbitraryPrecisionRadioButton.setSelected(false);
			doublePrecisionRadioButton.setSelected(true);
		}


		if(colorComboBox.getItems().contains(sr.colorFunction))
		{
			colorComboBox.setValue(sr.colorFunction);
		}
		else
		{
			/*Will NOT add color to file*/

		}


		currentRegion = sr.region;
		currentJulia = sr.julia;
		currentSeed = sr.seed;
		if(currentJulia)
		{
			setLabel.setText("Julia set: ");
			seedLabel.setText(currentSeed.toString());
		}
		else
		{
			setLabel.setText("Mandelbrot set: ");
			seedLabel.setText("0+0i");
		}
		centerLabel.setText(currentRegion.getCenterX().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()+ " + "
				+ currentRegion.getCenterY().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString() + "i");
		dimensionsLabel.setText(currentRegion.x1.subtract(currentRegion.x2).abs().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString() + "x" +
				currentRegion.y1.subtract(currentRegion.y2).abs().setScale(5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString());
		gui.loggedRegions = new ArrayList<>();
	}

	/**
	 * Sets all the values of the gui to the values of the editor.
	 */
	public void setValues()
	{
		gui.threadCount = Integer.parseInt(threadsField.getText());

		gui.autoIterations = autoIterationsCheckBox.isSelected();
		if(!gui.autoIterations)
		{
			gui.iterations = Integer.parseInt(iterationsField.getText());
		}


		gui.precision = Integer.parseInt(precisionField.getText());
		gui.mainCalculator.setColorFunction(colorComboBox.getValue());
		gui.previewCalculator.setColorFunction(colorComboBox.getValue());
		gui.arbitraryPrecision = arbitraryPrecisionRadioButton.isSelected();
		gui.currentRegion = currentRegion;
		gui.julia = currentJulia;
		gui.juliaSeed = currentSeed;
	}

	/**
	 * Validates all the values.
	 * @return false if some values are not valid.
	 */
	public boolean checkOptionValues()
	{
		boolean returnValue = true;
		int threadCount = 0;
		int iterations = 0;
		int precision = 0;
		try
		{
			threadCount = Integer.parseInt(threadsField.getText());
		}
		catch(NumberFormatException nfe)
		{
			threadsField.setStyle("-fx-background-color:red");
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
			threadsField.setStyle("-fx-background-color:red");
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
	
	/**
	 * Returns -1 if it does not exist and the id of the color that is the replica if it does.
	 * @param ccf
	 * @return
	 */
	private int existsInDatabase(CustomColorFunction ccf)
	{
		try
		{
			ResultSet set = stmt.executeQuery("SELECT ID, HashCode FROM Colors;");
			while(set.next())
			{
				if(set.getInt("HashCode") == ccf.hashCode())
				{
					return set.getInt("ID");
				}
			}
			set.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Returns -1 if it does not exist and the id of the color that is the replica if it does.
	 * @param sr
	 * @return
	 */
	private int existsInDatabase(SavedRegion sr)
	{
		try
		{
			ResultSet set = stmt.executeQuery("SELECT ID, HashCode FROM Regions;");
			while(set.next())
			{
				if(set.getInt("HashCode") == sr.hashCode())
				{
					return set.getInt("ID");
				}
			}
			set.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	private boolean colorExistsLocally(int hashCode)
	{
		for(CustomColorFunction ccf: savedColors)
		{
			if(ccf.hashCode()==hashCode)
			{
				return true;
			}
		}
		return false;
	}

	private boolean regionExistsLocally(int hashCode)
	{
		for(SavedRegion sr: savedRegions)
		{
			if(sr.hashCode()==hashCode)
			{
				return true;
			}
		}
		return false;
	}
	
	private void requestColorDownload()
	{
		ColorRow row;
		if((row = downloadColorTable.getSelectionModel().selectedItemProperty().get())==null)
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("Please Choose an entry");
			alert.show();
			return;
		}

		if(colorExistsLocally(row.getHashCode()))
		{
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setContentText("That color already exists on your computer.");
			alert.show();
			return;
		}

		boolean downloadRegion = false;
		String downloadRegionName = "";
		boolean downloadImage = false;
		String downloadImageName = "";
		String newFile = "";


		try
		{
			ResultSet set = stmt2.executeQuery("SELECT * FROM Linked WHERE ColorID = " + row.getId() + ";");
			if(set.isBeforeFirst())
			{
				set.next();
				int regionID;
				if((regionID = set.getInt("RegionID")) !=0)
				{
					set.close();
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.initModality(Modality.APPLICATION_MODAL);
					alert.setContentText("This color is associated with a region.\n"
							+ "Would you like to download the region as well?");
					ButtonType buttonTypeYes = new ButtonType("Yes");
					ButtonType buttonTypeNo = new ButtonType("No", ButtonData.CANCEL_CLOSE);
					alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
					Optional<ButtonType> result = alert.showAndWait();
					if(result.isPresent())
					{
						if(result.get().equals(buttonTypeYes))
						{
							set = stmt2.executeQuery("SELECT File, HashCode FROM Regions WHERE ID = " + regionID + ";");
							if(set.isBeforeFirst())
							{
								set.next();
								if(!regionExistsLocally(set.getInt("HashCode")))
								{

									downloadRegion = true;
									downloadRegionName = set.getString("File");
									set.close();
								}
							}
						}
					}
				}
				else
				{
					set.close();
				}

				set = stmt2.executeQuery("SELECT * FROM Linked WHERE ColorID = " + row.getId() + ";");
				if(set.isBeforeFirst())
				{
					set.next();
					int imageID;
					if((imageID = set.getInt("imageID")) !=0)
					{
						set.close();
						Alert alert = new Alert(AlertType.CONFIRMATION);
						alert.initModality(Modality.APPLICATION_MODAL);
						alert.setContentText("This color is associated with an image.\n"
								+ "Would you like to download the image as well?");
						ButtonType buttonTypeYes = new ButtonType("Yes");
						ButtonType buttonTypeNo = new ButtonType("No", ButtonData.CANCEL_CLOSE);
						alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
						Optional<ButtonType> result = alert.showAndWait();
						if(result.isPresent())
						{
							if(result.get().equals(buttonTypeYes))
							{


								set = stmt2.executeQuery("SELECT File, FileType FROM Images WHERE ID = " + imageID + ";");
								if(set.isBeforeFirst())
								{
									set.next();

									FileChooser fc = new FileChooser();
									fc.setTitle("Download");
									File file = null;
									if((file = fc.showSaveDialog(window)) == null)
									{
										return;
									}

									newFile = file.getAbsolutePath();
									String imageType = set.getString("FileType");
									if(! newFile.endsWith("." + imageType))
									{
										newFile = new File(newFile + "." + imageType).getAbsolutePath();
									}


									downloadImage = true;
									downloadImageName = set.getString("File");
									set.close();
								}
								else
								{
									set.close();
								}
							}
						}
					}
					else
					{
						set.close();
					}
				}
				else
				{
					set.close();
				}
			}
			else
			{
				set.close();
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		UploadDialog dialog = new UploadDialog();
		Platform.runLater(()->{
			dialog.show();
			dialog.getResponseLabel().setText("Downloading Image...");
		});
		downloadColor("https://www.ezstein.xyz/serverScripts/download.php?download=/uploads/colors/" + row.getFile());
		if(downloadRegion)
		{
			downloadRegion("https://www.ezstein.xyz/serverScripts/download.php?download=/uploads/regions/" + downloadRegionName);
		}
		if(downloadImage)
		{
			downloadImage("https://www.ezstein.xyz/serverScripts/download.php?download=/uploads/images/" + downloadImageName, newFile);
		}

		Platform.runLater(()->{
			dialog.getResponseLabel().setText("Done");
			dialog.enableClose();
		});
	}

	private void requestRegionDownload(){
		RegionRow row;
		if((row = downloadRegionTable.getSelectionModel().selectedItemProperty().get())==null)
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("Please Choose an entry");
			alert.show();
			return;
		}
		if(regionExistsLocally(row.getHashCode()))
		{
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setContentText("That region already exists on your computer.");
			alert.show();
			return;
		}




		boolean downloadImage = false;
		String newFile = "";
		String fileName = "";
		try
		{
			ResultSet set = stmt2.executeQuery("SELECT * FROM Linked WHERE RegionID = " + row.getId() + ";");
			if(set.isBeforeFirst())
			{
				set.next();
				int imageID;
				if((imageID = set.getInt("imageID")) !=0)
				{
					set.close();
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.initModality(Modality.APPLICATION_MODAL);
					alert.setContentText("This color is associated with an image.\n"
							+ "Would you like to download the image as well?");
					ButtonType buttonTypeYes = new ButtonType("Yes");
					ButtonType buttonTypeNo = new ButtonType("No", ButtonData.CANCEL_CLOSE);
					alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
					Optional<ButtonType> result = alert.showAndWait();
					if(result.isPresent())
					{
						if(result.get().equals(buttonTypeYes))
						{
							set = stmt2.executeQuery("SELECT File, FileType FROM Images WHERE ID = " + imageID + ";");
							System.out.println("***** " + set.isClosed());
							if(set.isBeforeFirst())
							{
								set.next();

								FileChooser fc = new FileChooser();
								fc.setTitle("Download");
								File file = null;
								if((file = fc.showSaveDialog(window)) == null)
								{
									return;
								}

								newFile = file.getAbsolutePath();
								System.out.println("***** " + set.isClosed());
								String imageType = set.getString("FileType");
								if(! newFile.endsWith("." + imageType))
								{
									newFile = new File(newFile + "." + imageType).getAbsolutePath();
								}

								downloadImage = true;
								fileName = set.getString("File");
								set.close();
							}
							else
							{
								set.close();
							}
						}
					}
				}
				else
				{
					set.close();
				}
			}
			else
			{
				set.close();
			}
		} catch(SQLException sqle)
		{
			sqle.printStackTrace();
		}

		UploadDialog dialog = new UploadDialog();
		Platform.runLater(()->{
			dialog.show();
			dialog.getResponseLabel().setText("Downloading...");
		});

		downloadRegion("https://www.ezstein.xyz/serverScripts/download.php?download=/uploads/regions/" + row.getFile());
		
		if(downloadImage)
		{
			downloadImage("https://www.ezstein.xyz/serverScripts/download.php?download=/uploads/images/" + fileName, newFile);
		}

		Platform.runLater(()->{
			dialog.getResponseLabel().setText("Done");
			dialog.enableClose();
		});
	}

	private void downloadImage(String uri, String imagePath)
	{
		HttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet(uri);
		InputStream in = null;
		FileOutputStream out = null;
		byte[] buffer = new byte[1024];
		try {
			HttpResponse response = client.execute(get);
			in = response.getEntity().getContent();
			out = new FileOutputStream(imagePath);
			for(int length; (length = in.read(buffer)) >0;)
			{
				out.write(buffer, 0, length);
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

			try
			{
				if(in!=null)
				{
					in.close();
				}
				if(out !=null)
				{
					out.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void downloadColor(String uri)
	{
		HttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet(uri);
		InputStream in = null;
		OutputStream out = null;
		ObjectInputStream objectIn = null;
		ObjectOutputStream objectOut = null;
		byte[] buffer = new byte[1024];
		try{
			HttpResponse response = client.execute(get);
			in = response.getEntity().getContent();
			out = new FileOutputStream(Locator.locateUniqueFile("tmp/download/downloadFile.txt").toFile());
			for(int length; (length = in.read(buffer)) >0;)
			{
				out.write(buffer, 0, length);
			}
			objectIn = new ObjectInputStream(new FileInputStream(Locator.locateFile("tmp/download/downloadFile.txt").toFile()));
			CustomColorFunction ccf = (CustomColorFunction) objectIn.readObject();
			savedColors.add(ccf);
		} catch(IOException ioe){
			ioe.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(in!=null){
					in.close();
				}
				if(out !=null){
					out.close();
				}
				if(objectIn !=null)
				{
					objectIn.close();
				}
				if(objectOut !=null)
				{
					objectOut.close();
				}


			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void downloadRegion(String uri)
	{
		HttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet(uri);
		InputStream in = null;
		OutputStream out = null;
		ObjectInputStream objectIn = null;
		ObjectOutputStream objectOut = null;
		ObjectOutputStream objectOutColor = null;
		byte[] buffer = new byte[1024];
		try{
			HttpResponse response = client.execute(get);
			in = response.getEntity().getContent();
			out = new FileOutputStream(Locator.locateUniqueFile("tmp/download/downloadFile.txt").toFile());
			for(int length; (length = in.read(buffer)) >0;)
			{
				out.write(buffer, 0, length);
			}
			objectIn = new ObjectInputStream(new FileInputStream(Locator.locateFile("tmp/download/downloadFile.txt").toFile()));
			SavedRegion sr = (SavedRegion)objectIn.readObject();
			savedRegions.add(sr);

			/*The region may require dependencies on color functions*/
			if(!savedColors.contains(sr.colorFunction))
			{
				savedColors.add(sr.colorFunction);
			}
		}catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{

			try {
				if(in!=null){
					in.close();
				}
				if(out !=null){
					out.close();
				}
				if(objectIn !=null)
				{
					objectIn.close();
				}
				if(objectOut !=null)
				{
					objectOut.close();
				}
				if(objectOutColor !=null)
				{
					objectOutColor.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private Image downloadImageAsObject(String uri){
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet(uri);
		Image image = null;
		CloseableHttpResponse response = null;
		try {
			response = client.execute(get);
			image = new Image(response.getEntity().getContent());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			
			try {
				if(client!=null)
				client.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		
			try {
				if(response!=null)
				response.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return image;
	}
	
	private void requestImageDownload(){
		ImageRow row = downloadImageTable.getSelectionModel().selectedItemProperty().get();
		if(row==null)
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("Please Choose an entry");
			alert.show();
			return;
		}

		FileChooser fc = new FileChooser();
		fc.setTitle("Download");
		File file = null;
		if((file = fc.showSaveDialog(window)) == null)
		{
			return;
		}

		String newFile = file.getAbsolutePath();
		String imageType = row.getFileType();
		if(! newFile.endsWith("." + imageType))
		{
			newFile = new File(newFile + "." + imageType).getAbsolutePath();
		}



		boolean downloadRegion =false;
		String downloadFileName = "";
		try
		{
			ResultSet set = stmt2.executeQuery("SELECT * FROM Linked WHERE ImageID = " + row.getId() + ";");
			if(set.isBeforeFirst())
			{
				set.next();
				int regionID;
				if((regionID = set.getInt("RegionID")) !=0)
				{
					set.close();
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.initModality(Modality.APPLICATION_MODAL);
					alert.initOwner(window);
					alert.setContentText("This Image is associated with a region and a color.\n"
							+ "Would you like to download the region and color as well?");
					ButtonType buttonTypeYes = new ButtonType("Yes");
					ButtonType buttonTypeNo = new ButtonType("No", ButtonData.CANCEL_CLOSE);
					alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
					Optional<ButtonType> result = alert.showAndWait();
					if(result.isPresent())
					{
						if(result.get().equals(buttonTypeYes))
						{
							set = stmt2.executeQuery("SELECT File, HashCode FROM Regions WHERE ID = " + regionID + ";");
							if(set.isBeforeFirst())
							{
								set.next();
								if(!regionExistsLocally(set.getInt("HashCode")))
								{
									downloadRegion = true;
									downloadFileName = set.getString("File");
								}
							}
							set.close();
						}
					}
				}
				else
				{
					set.close();
				}
			}
			else
			{
				set.close();
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		UploadDialog dialog = new UploadDialog();
		Platform.runLater(()->{
			dialog.show();
			dialog.getResponseLabel().setText("Downloading...");
		});

		downloadImage("https://www.ezstein.xyz/serverScripts/download.php?download=/uploads/images/" + row.getFile(),newFile);

		if(downloadRegion)
		{
			downloadRegion("https://www.ezstein.xyz/serverScripts/download.php?download=/uploads/regions/" +downloadFileName);
		}

		Platform.runLater(()->{
			dialog.getResponseLabel().setText("Done");
			dialog.enableClose();
		});

	}
	
	@FXML
	private void colorChanged(ActionEvent ae){
		CustomColorFunction colorFunction =colorComboBox.getValue();
		rangeField.setText(colorFunction.getRange()+"");

		stopListView.getItems().remove(0, stopListView.getItems().size());
		for(Stop stop : colorFunction.getStops())
		{
			stopListView.getItems().add(stop);
		}
	}
	
	@FXML
	private void colorPickerEvent(ActionEvent ae){
		int index = stopListView.getSelectionModel().getSelectedIndex();
		if(index < 0)
		{
			/*Nothing selected*/
			return;
		}
		stopListView.getItems().set(index, new Stop(stopListView.getItems().get(index).getOffset(), colorPicker.getValue()));
	}
	
	@FXML
	private void savedRegionChanged(ActionEvent ae){
		if(savedRegionsComboBox.getSelectionModel().getSelectedItem() != null)
		{
			loadRegion();
		}
	}
	
@FXML
	private void uploadRegionChanged(ActionEvent ae){
		SavedRegion newValue = uploadRegionComboBox.getValue();
		if(newValue != null){
			uploadNameField.setText(newValue.name);
		}
			
	}
	
	@FXML
	private void uploadColorChanged(ActionEvent ae){
		uploadNameField.setText(uploadColorComboBox.getValue().getName());
	}

	@FXML
	private void uploadTypeChanged(ActionEvent ae)
	{
		
		if(uploadTypeComboBox.getValue().equals("Region")){
			uploadNameField.setDisable(true);
			uploadNameField.setText(
					uploadRegionComboBox.getSelectionModel().getSelectedItem()!= null ? uploadRegionComboBox.getSelectionModel().getSelectedItem().name : "");
			uploadRegionComboBox.setDisable(false);
			uploadColorComboBox.setDisable(true);

		} else if(uploadTypeComboBox.getValue().equals("Color")){
			uploadNameField.setDisable(true);
			uploadNameField.setText(uploadColorComboBox.getSelectionModel().getSelectedItem()!=null ? uploadColorComboBox.getSelectionModel().getSelectedItem().getName():"");
			uploadRegionComboBox.setDisable(true);
			uploadColorComboBox.setDisable(false);
		} if(uploadTypeComboBox.getValue().equals("Image")){
			uploadNameField.setDisable(false);
			uploadNameField.setText("");
			uploadRegionComboBox.setDisable(true);
			uploadColorComboBox.setDisable(true);
		}
	}
	
	@FXML
	private void toggleAutoIterations(ActionEvent ae){
		if(autoIterationsCheckBox.isSelected())
		{
			iterationsField.setDisable(true);
			iterationsField.setText(gui.calcAutoIterations(gui.magnification) + "");
		}
		else
		{
			iterationsField.setDisable(false);
		}
	}
	
	@FXML
	private void addColor(ActionEvent ae){
		if(!validateForSaveColor())
		{
			return;
		}
		saveColor();
	}
	
	@FXML
	private void removeRegion(ActionEvent ae){
		SavedRegion deletedRegion = savedRegionsComboBox.getValue();
		if(deletedRegion == null)
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("No region is selected");
			alert.show();
			return;
		}
		savedRegions.remove(deletedRegion);
	}
	
	@FXML
	private void removeColor(ActionEvent ae){
		CustomColorFunction colorToRemove = colorComboBox.getValue();
		int index = colorComboBox.getItems().indexOf(colorToRemove);
		if(index >0)
		{
			colorComboBox.setValue(colorComboBox.getItems().get(index-1));
		}
		else if(colorComboBox.getItems().size() > 1)
		{
			colorComboBox.setValue(colorComboBox.getItems().get(index+1));
		}
		else
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("You must keep at least one color");
			alert.show();
			return;
		}

		savedColors.remove(colorToRemove);
	}
	
	@FXML
	private void removeStop(ActionEvent ae){
		Stop stop = stopListView.getSelectionModel().getSelectedItem();
		stopListView.getItems().remove(stop);
	}

	@FXML
	private void addStop(ActionEvent ae){
		Stop stopToAdd = new Stop(colorPositionSlider.getValue(), colorPicker.getValue());
		stopListView.getItems().add(stopToAdd);
		stopListView.getSelectionModel().select(stopListView.getItems().size()-1);

	}
	
	@FXML
	private void download(ActionEvent ae){
		String type = tables.getSelectionModel().getSelectedItem().getText();
		if(type.equals("Images"))
		{
			requestImageDownload();
		} else if (type.equals("Regions")) {
			requestRegionDownload();
		} else if (type.equals("Colors")) {
			requestColorDownload();
		} else {
			System.out.println("Unknown type");
		}
	}
	
	@FXML
	private void applyAndRerender(ActionEvent ae){
		ResultType result = askToSaveColor();
		if(result == ResultType.CANCEL)
		{
			return;
		}
		gui.threadQueue.callLater(()->{
			if(!checkOptionValues())
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
	}
	
	@FXML
	private void save(ActionEvent ae){
		if(!checkOptionValues())
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
			for(SavedRegion sr : savedRegionsComboBox.getItems())
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
		if(savedRegionsComboBox.getValue()!=null)
		{
			resetValues();
		}

		SavedRegion savedRegion = new SavedRegion(name, autoIterationsCheckBox.isSelected(),
				Integer.parseInt(iterationsField.getText()),
				Integer.parseInt(precisionField.getText()), Integer.parseInt(threadsField.getText()),
				currentRegion,arbitraryPrecisionRadioButton.isSelected(),currentJulia,currentSeed,colorComboBox.getValue());
		savedRegions.add(savedRegion);
		//savedRegionsChoiceBox.setValue(savedRegion);
	}
	
	@FXML
	private void apply(ActionEvent ae){
		ResultType result = askToSaveColor();
		if(result == ResultType.CANCEL)
		{
			return;
		}


		gui.threadQueue.callLater(()->{
			if(!checkOptionValues())
			{
				return;
			}
			gui.interrupt();

			Platform.runLater(()->{
				setValues();
				window.close();

			});
		});
	}
	
	@FXML
	private void cancel(ActionEvent ae){
		ResultType result = askToSaveColor();
		if(result == ResultType.CANCEL)
		{
			return;
		}
		window.close();
	}
	
	@FXML
	private void upload(ActionEvent ae){
		String type = uploadTypeComboBox.getSelectionModel().getSelectedItem();
		if(type.equals("Image")){
			showUploadImageDialog();
		} else if(type.equals("Region")) {
			showUploadRegionDialog();
		} else if(type.equals("Color")) {
			showUploadColorDialog();
		} else {
			System.out.println("UNKNOWN TYPE");
		}
	}
	
	private class UploadDialog
	{
		private Label responseLabel;
		private Stage stage;
		private Button closeButton;
		public UploadDialog(){
			Platform.runLater(()->{
				stage = new Stage();
				GridPane grid = new GridPane();
				grid.setPadding(new Insets(25,25,25,25));
				grid.setHgap(10);
				grid.setVgap(10);

				responseLabel = new Label("");
				closeButton = new Button("Close");
				closeButton.setDisable(true);
				closeButton.setOnAction(e->{
					stage.close();
				});

				//grid.add(new Label("Uploading..."),0,0);
				grid.add(responseLabel,0,1);
				grid.add(closeButton,0,2);

				stage.setTitle("Waiting");
				Scene scene = new Scene(grid);
				stage.setHeight(200);
				stage.setWidth(300);
				stage.setScene(scene);
			});
		}

		public Label getResponseLabel(){
			return responseLabel;
		}

		public void enableClose()
		{
			closeButton.setDisable(false);
		}

		public void show()
		{
			stage.show();
		}
	}

	public class RegionRow
	{
		private final IntegerProperty id, size, date, hashCode;
		private final StringProperty name, author, description, file;

		/**
		 *
		 * @param id
		 * @param name
		 * @param author
		 * @param description
		 * @param file
		 * @param size
		 * @param date
		 */
		public RegionRow(int id, String name, String author, String description, String file, int size, int date, int hashCode)
		{
			this.id = new SimpleIntegerProperty(id);
			this.size = new SimpleIntegerProperty(size);
			this.date = new SimpleIntegerProperty(date);
			this.name = new SimpleStringProperty(name);
			this.author = new SimpleStringProperty(author);
			this.description = new SimpleStringProperty(description);
			this.file = new SimpleStringProperty(file);
			this.hashCode = new SimpleIntegerProperty(hashCode);
		}

		public final IntegerProperty getIdProperty()
		{
			return id;
		}
		public final int getId()
		{
			return id.get();
		}
		public final void setId(int val)
		{
			id.set(val);
		}

		public final IntegerProperty getSizeProperty()
		{
			return size;
		}
		public final int getSize()
		{
			return size.get();
		}
		public final void setSize(int val)
		{
			size.set(val);
		}

		public final IntegerProperty getDateProperty()
		{
			return date;
		}
		public final int getDate()
		{
			return date.get();
		}
		public final void setDate(int val)
		{
			date.set(val);
		}

		public final StringProperty getNameProperty()
		{
			return name;
		}
		public final String getName()
		{
			return name.get();
		}
		public final void setName(String val)
		{
			name.set(val);
		}

		public final StringProperty getAuthorProperty()
		{
			return author;
		}
		public final String getAuthor()
		{
			return author.get();
		}
		public final void setAuthor(String val)
		{
			author.set(val);
		}

		public final StringProperty getDescriptionProperty()
		{
			return description;
		}
		public final String getDescription()
		{
			return description.get();
		}
		public final void setDescription(String val)
		{
			description.set(val);
		}

		public final StringProperty getFileProperty()
		{
			return file;
		}
		public final String getFile()
		{
			return file.get();
		}
		public final void setFile(String val)
		{
			file.set(val);
		}

		public final IntegerProperty getHashCodeProperty()
		{
			return hashCode;
		}
		public final int getHashCode()
		{
			return hashCode.get();
		}
		public final void setHashCode(int val)
		{
			hashCode.set(val);
		}


		@Override
		public boolean equals(Object o)
		{
			if(o==null)
			{
				return false;
			}
			if(o==this)
			{
				return true;
			}
			if(o instanceof RegionRow)
			{
				RegionRow row = (RegionRow) o;
				if(row.getHashCode()==hashCode.get()
				&& row.getFile().equals(file.get())
				&& row.getDescription().equals(description.get())
				&& row.getAuthor().equals(author.get())
				&& row.getName().equals(name.get())
				&& row.getDate() == date.get()
				&& row.getSize() == size.get()
				&& row.getId() == id.get())
				{
					return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode()
		{
			return 100*hashCode.get() + file.get().hashCode() +
					description.get().hashCode() + author.get().hashCode() +
					name.get().hashCode() + 11*date.get() + 15*size.get() + 18*id.get();
		}
	}

	public class ColorRow
	{
		private final IntegerProperty id, size, date, hashCode;
		private final StringProperty name, author, description, file;

		/**
		 *
		 * @param id
		 * @param name
		 * @param author
		 * @param description
		 * @param file
		 * @param size
		 * @param date
		 */
		public ColorRow(int id, String name, String author, String description, String file, int size, int date, int hashCode)
		{
			this.id = new SimpleIntegerProperty(id);
			this.size = new SimpleIntegerProperty(size);
			this.date = new SimpleIntegerProperty(date);
			this.name = new SimpleStringProperty(name);
			this.author = new SimpleStringProperty(author);
			this.description = new SimpleStringProperty(description);
			this.file = new SimpleStringProperty(file);
			this.hashCode = new SimpleIntegerProperty(hashCode);
		}

		public final IntegerProperty getIdProperty()
		{
			return id;
		}
		public final int getId()
		{
			return id.get();
		}
		public final void setId(int val)
		{
			id.set(val);
		}

		public final IntegerProperty getSizeProperty()
		{
			return size;
		}
		public final int getSize()
		{
			return size.get();
		}
		public final void setSize(int val)
		{
			size.set(val);
		}

		public final IntegerProperty getDateProperty()
		{
			return date;
		}
		public final int getDate()
		{
			return date.get();
		}
		public final void setDate(int val)
		{
			date.set(val);
		}

		public final StringProperty getNameProperty()
		{
			return name;
		}
		public final String getName()
		{
			return name.get();
		}
		public final void setName(String val)
		{
			name.set(val);
		}

		public final StringProperty getAuthorProperty()
		{
			return author;
		}
		public final String getAuthor()
		{
			return author.get();
		}
		public final void setAuthor(String val)
		{
			author.set(val);
		}

		public final StringProperty getDescriptionProperty()
		{
			return description;
		}
		public final String getDescription()
		{
			return description.get();
		}
		public final void setDescription(String val)
		{
			description.set(val);
		}

		public final StringProperty getFileProperty()
		{
			return file;
		}
		public final String getFile()
		{
			return file.get();
		}
		public final void setFile(String val)
		{
			file.set(val);
		}


		public final IntegerProperty getHashCodeProperty()
		{
			return hashCode;
		}
		public final int getHashCode()
		{
			return hashCode.get();
		}
		public final void setHashCode(int val)
		{
			hashCode.set(val);
		}

		@Override
		public boolean equals(Object o)
		{
			if(o==null)
			{
				return false;
			}
			if(o==this)
			{
				return true;
			}
			if(o instanceof ColorRow)
			{
				ColorRow row = (ColorRow) o;
				if(row.getHashCode()==hashCode.get()
				&& row.getFile().equals(file.get())
				&& row.getDescription().equals(description.get())
				&& row.getAuthor().equals(author.get())
				&& row.getName().equals(name.get())
				&& row.getDate() == date.get()
				&& row.getSize() == size.get()
				&& row.getId() == id.get())
				{
					return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode()
		{
			return 100*hashCode.get() + file.get().hashCode() +
					description.get().hashCode() + author.get().hashCode() +
					name.get().hashCode() + 11*date.get() + 15*size.get() + 18*id.get();
		}
	}
	
	public class ImageRow
	{
		private final IntegerProperty id, width, height, size, date;
		private final StringProperty name, author, description, file, setType, fileType;
		private Image image;
		/**
		 *
		 * @param id
		 * @param name
		 * @param author
		 * @param description
		 * @param file
		 * @param setType
		 * @param width
		 * @param height
		 * @param size
		 * @param date
		 * @param fileType
		 * @param image 
		 */
		public ImageRow(int id, String name, String author, String description, String file, String setType,
				int width, int height, int size, int date, String fileType, Image image)
		{
			this.id = new SimpleIntegerProperty(id);
			this.width = new SimpleIntegerProperty(width);
			this.height = new SimpleIntegerProperty(height);
			this.size = new SimpleIntegerProperty(size);
			this.date = new SimpleIntegerProperty(date);
			this.name = new SimpleStringProperty(name);
			this.author = new SimpleStringProperty(author);
			this.description = new SimpleStringProperty(description);
			this.file = new SimpleStringProperty(file);
			this.fileType = new SimpleStringProperty(fileType);
			this.setType = new SimpleStringProperty(setType);
			this.image = image;
		}
		
		public ImageRow(int id, String name, String author, String description, String file, String setType,
				int width, int height, int size, int date, String fileType)
		{
			this.id = new SimpleIntegerProperty(id);
			this.width = new SimpleIntegerProperty(width);
			this.height = new SimpleIntegerProperty(height);
			this.size = new SimpleIntegerProperty(size);
			this.date = new SimpleIntegerProperty(date);
			this.name = new SimpleStringProperty(name);
			this.author = new SimpleStringProperty(author);
			this.description = new SimpleStringProperty(description);
			this.file = new SimpleStringProperty(file);
			this.fileType = new SimpleStringProperty(fileType);
			this.setType = new SimpleStringProperty(setType);
			this.image = new Image("/resource/background.jpg");
		}

		public final IntegerProperty getIdProperty()
		{
			return id;
		}
		public final int getId()
		{
			return id.get();
		}
		public final void setId(int val)
		{
			id.set(val);
		}

		public final IntegerProperty getHeightProperty()
		{
			return height;
		}
		public final int getHeight()
		{
			return height.get();
		}
		public final void setHeight(int val)
		{
			height.set(val);
		}

		public final IntegerProperty getWidthProperty()
		{
			return width;
		}
		public final int getWidth()
		{
			return width.get();
		}
		public final void setWidth(int val)
		{
			width.set(val);
		}

		public final IntegerProperty getSizeProperty()
		{
			return size;
		}
		public final int getSize()
		{
			return size.get();
		}
		public final void setSize(int val)
		{
			size.set(val);
		}

		public final IntegerProperty getDateProperty()
		{
			return date;
		}
		public final int getDate()
		{
			return date.get();
		}
		public final void setDate(int val)
		{
			date.set(val);
		}

		public final StringProperty getNameProperty()
		{
			return name;
		}
		public final String getName()
		{
			return name.get();
		}
		public final void setName(String val)
		{
			name.set(val);
		}

		public final StringProperty getAuthorProperty()
		{
			return author;
		}
		public final String getAuthor()
		{
			return author.get();
		}
		public final void setAuthor(String val)
		{
			author.set(val);
		}

		public final StringProperty getDescriptionProperty()
		{
			return description;
		}
		public final String getDescription()
		{
			return description.get();
		}
		public final void setDescription(String val)
		{
			description.set(val);
		}

		public final StringProperty getFileProperty()
		{
			return file;
		}
		public final String getFile()
		{
			return file.get();
		}
		public final void setFile(String val)
		{
			file.set(val);
		}

		public final StringProperty getFileTypeProperty()
		{
			return fileType;
		}
		public final String getFileType()
		{
			return fileType.get();
		}
		public final void setFileType(String val)
		{
			fileType.set(val);
		}

		public final StringProperty getSetTypeProperty()
		{
			return setType;
		}
		public final String getSetType()
		{
			return setType.get();
		}
		public final void setSetType(String val)
		{
			setType.set(val);
		}
		
		public final Image getImage(){
			return image;
		}
		public final void setImage(Image image){
			this.image=image;
		}
		
		@Override
		public boolean equals(Object o)
		{
			if(o==null)
			{
				return false;
			}
			if(o==this)
			{
				return true;
			}
			if(o instanceof ImageRow)
			{
				ImageRow row = (ImageRow) o;
				if(row.getWidth()==width.get()
				&& row.getHeight()==height.get()
				&& row.getSetType().equals(setType.get())
				&& row.getFileType().equals(fileType.get())
				&& row.getFile().equals(file.get())
				&& row.getDescription().equals(description.get())
				&& row.getAuthor().equals(author.get())
				&& row.getName().equals(name.get())
				&& row.getDate() == date.get()
				&& row.getSize() == size.get()
				&& row.getId() == id.get())
				{
					return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode()
		{
			return 100*width.get() + 30*height.get() + fileType.get().hashCode()
					+ setType.get().hashCode()+ file.get().hashCode() +
					description.get().hashCode() + author.get().hashCode() +
					name.get().hashCode() + 11*date.get() + 15*size.get() + 18*id.get();
		}
	}
	
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
	
	enum ResultType {YES, CANCEL, NO}
}
