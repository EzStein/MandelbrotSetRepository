package mandelbrotSet;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * <p>
 * Mandelbrot Set Program
 * </p>
 * This class defines the GUI of the MandelbrotSet program and holds all the JComponents in one JFrame.
 * These components include the main ViewerPanel, the PreviewPanel, the JMenuBar, the JTextField, and the JProgressBar in a BorderLayout.
 * It subclasses GUI allowing for much overhead JMenuItem code to be kept out of sight.
 * It implements the ActionListener from GUI and defines what should happen on each MenuItem click.
 * It also contains the main() method which starts the whole program.
 * 
 * @author Ezra Stein
 * @version 1.0
 * @since 2015
 */

public class MandelbrotGUI extends GUI
{
	/**
	 * Holds the center JPanel or main viewing panel which displays the mandelbrot and julia sets.
	 */
	ViewerPanel viewerPanel;
	
	/**
	 * Holds the preview panel in the EAST section which will show a small preview of the Julia set every time the user right clicks.
	 */
	PreviewPanel previewPanel;
	
	/**
	 * Holds the NORTH progressBar which displays how much of the image has rendered.
	 */
	JProgressBar progressBar;
	
	/**
	 * Holds the SOUTH JTextField which displays information such as magnification and center coordinates.	
	 */
	JTextField textField;
	
	/**
	 * Contains the two components in the east display;
	 */
	JPanel eastPanel;
	
	/**
	 * Contains extra status info.
	 */
	JTextArea statusArea;
	
	/**
	 * Holds the size of the viewerPanel which is chosen by the user upon starting.
	 */
	int size;
	
	/**
	 * True if this is a retina display mac.
	 */
	boolean retina;
	
	/**
	 * Constructs a GUI from the super constructor passing it a title of the frame and a list of menuItems in a 2D array
	 * @param title - title of JFrame
	 * @param menuItems - 2D Matrix of the form: {{Menu1,MenuItem1,MenuItem2},{Menu2,MenuItem1, MenuItem2},...}
	 * @see GUI
	 */
	public MandelbrotGUI(String title, String[][] menuItems)
	{
		super(title, menuItems);
		//com.apple.eawt.Application.getApplication().setDockIconImage(new ImageIcon(getClass().getResource("MSet.png")).getImage());
	}
	
	/**
	 * The main() method starts the program. Creates a list of JMenuItems and passes it to the constructor.
	 * 
	 * @param args - Unused
	 */
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{	
			public void run()
			{
				String[][] s = 
							{{"File","Save Image...","Read Me"},
							{"Options","Switch to Julia set", "Switch to Mandelbrot set", "Switch to Arbitrary Precision", "Switch to Double Precision", "Reset", "Rerender", "Undo","Interrupt"},
							{"Edit","Change Color...", "Change Iterations...", "Change Precision...", "Change Thread Count..."}, 
							{"Info", "Status", "About"}};
				new MandelbrotGUI("Mandelbrot Set",s);
			}
		});
	}
	
	/**
	 * Overrides superclass createContent() method. Adds in all the necessary components to the contentPane.
	 */
	@Override
	public void createContent()
	{
		setRetina();
		setSize();
		progressBar = new JProgressBar(0,2000);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		textField = new JTextField();
		textField.setEditable(false);
		previewPanel = new PreviewPanel((int)size/3,(int)size/3, this);
		previewPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		statusArea = new JTextArea();
		statusArea.setPreferredSize(new Dimension((int) size/3, (int) 2*size/3));
		statusArea.setEditable(false);
		statusArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		eastPanel = new JPanel();
		eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.PAGE_AXIS));
		eastPanel.add(previewPanel);
		eastPanel.add(statusArea);
		try
		{
			viewerPanel = new ViewerPanel(size,size,10,this);
		}
		catch(OutOfMemoryError ome)
		{
			JOptionPane.showMessageDialog(frame, "ERROR: That frame size is too large for memory. The program will now shut down", "Fatal Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		//viewerPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		contentPane.setLayout(new BorderLayout());
		contentPane.add(viewerPanel,BorderLayout.CENTER);
		contentPane.add(textField, BorderLayout.SOUTH);
		contentPane.add(eastPanel, BorderLayout.EAST);
		contentPane.add(progressBar, BorderLayout.NORTH);
		getMenuItem("Switch to Julia set").setEnabled(true);
		getMenuItem("Switch to Mandelbrot set").setEnabled(false);
		getMenuItem("Switch to Double Precision").setEnabled(false);
		getMenuItem("Switch to Arbitrary Precision").setEnabled(true);
		frame.setResizable(false);
	}
	
	/**
	 * Implements the method from the inherited interface ActionListener.
	 * Checks for clicks on all menu items.
	 */
	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getActionCommand().equals("Read Me"))
		{
			JOptionPane.showMessageDialog(frame, "For Zooming:\nDrag a box.\nLeft click.\nScroll using the scroll wheel (platform dependent).\n\n"
					+ "For Panning:\nUse the arrow keys.\n\n"
					+ "For Previewing the Julia Sets:\nRight click\n\n"
					+ "Use the Edit menu item to change the color of the set, the iterations used, the precision, or the number of threads used.\n"
					+ "Use the Options menu item to switch between Mandelbrot and Julia sets, and to toggle double and arbitrary precision.\n"
					+ "You may also use it to rerender an image or to go back to a previous image.\n"
					+ "Use the File menu item to save an image.","Read Me", JOptionPane.INFORMATION_MESSAGE);
		}
		else if(ae.getActionCommand().equals("Switch to Julia set"))
		{
			viewerPanel.toJuliaSet();
		}
		else if(ae.getActionCommand().equals("Switch to Mandelbrot set"))
		{
			viewerPanel.toMandelbrotSet();
		}
		else if(ae.getActionCommand().equals("Reset"))
		{
			viewerPanel.reset();
		}
		else if(ae.getActionCommand().equals("Rerender"))
		{
			viewerPanel.rerender();
		}
		else if(ae.getActionCommand().equals("Status"))
		{
			viewerPanel.displayStatus();
		}
		else if(ae.getActionCommand().equals("Undo"))
		{
			viewerPanel.undo();
		}
		else if(ae.getActionCommand().equals("Change Iterations..."))
		{
			viewerPanel.changeIterations();
		}
		else if(ae.getActionCommand().equals("Change Precision..."))
		{
			viewerPanel.changePrecision();
		}
		else if(ae.getActionCommand().equals("Switch to Arbitrary Precision"))
		{
			viewerPanel.toArbitraryPrecision();
		}
		else if(ae.getActionCommand().equals("Switch to Double Precision"))
		{
			viewerPanel.toDoublePrecision();
		}
		else if(ae.getActionCommand().equals("Save Image..."))
		{
			viewerPanel.saveImage();
		}
		else if(ae.getActionCommand().equals("Interrupt"))
		{
			viewerPanel.interruptThreads();
		}
		else if(ae.getActionCommand().equals("Change Color..."))
		{
			viewerPanel.changeColor();
		}
		else if(ae.getActionCommand().equals("About"))
		{
			JOptionPane.showMessageDialog(frame, "Version 1.0\n © Ezra Stein, 2015", "About", JOptionPane.INFORMATION_MESSAGE);
		}
		else if(ae.getActionCommand().equals("Change Thread Count..."))
		{
			viewerPanel.changeThreadNumber();
		}
	}
	
	/**
	 * Returns the SOUTH text field.
	 * @return the SOUTH text field.
	 */
	public JTextField getJTextField()
	{
		return textField;
	}
	
	/**
	 * Returns the EAST previewPanel.
	 * @return the EAST previewPanel.
	 */
	public PreviewPanel getPreviewPanel()
	{
		return previewPanel;
	}
	
	/**
	 * Returns the viewerPanel.
	 * @return the viewerPanel.
	 */
	public ViewerPanel getViewerPanel()
	{
		return viewerPanel;
	}
	
	/**
	 * Returns the NORTH progressBar.
	 * @return the NORTH progressBar.
	 */
	public JProgressBar getProgressBar()
	{
		return progressBar;
	}
	
	/**
	 * Returns the JMenuBar.
	 * @return the JMenuBar.
	 */
	public JMenuBar getMenuBar()
	{
		return frame.getJMenuBar();
	}
	
	/**
	 * Returns this GUI's JFrame
	 * @return the frame of this GUI.
	 */
	public JFrame getJFrame()
	{
		return frame;
	}
	
	/**
	 * Prompts the user to set a size of the main viewerPanel.
	 * If it is too large, the system will exit.
	 */
	public void setSize()
	{
		boolean inquire = true;
		int aHolderNumber = 400;
		String s;
		while(inquire)
		{
			s = JOptionPane.showInputDialog(frame, 
					"Enter the desired image size in terms of pixel width (the size of your display is " + (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth() + "x" + (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight() + ")\n"
							+ "For most laptops, 800 pixels is a reasonable size:", 
					"Choose Size", JOptionPane.QUESTION_MESSAGE);
			if(s == null)
			{
				System.exit(0);
				return;
			}
			try
			{
				
				aHolderNumber = new Integer(s).intValue();
				if(aHolderNumber <300 || aHolderNumber>3000)
				{
					JOptionPane.showMessageDialog(frame, "The size must be at least 300 pixels and less than 3000", "Error", JOptionPane.ERROR_MESSAGE);
					inquire = true;
				}
				else
				{
					inquire = false;
				}
			}
			catch(NumberFormatException nfe)
			{
				JOptionPane.showMessageDialog(frame, "That is not a readable integer! Try again.", "Error", JOptionPane.ERROR_MESSAGE);
				inquire = true;
			}
		}
		
		size = aHolderNumber;
	}
	
	/**
	 * Prompts the user whether he uses a retina display.
	 */
	public void setRetina()
	{
		JOptionPane.showMessageDialog(frame, "Welcome to the Mandelbrot set fractal viewing program\n\n"
				+ "This program allows you to zoom into the depths of the Mandelbrot and Julia sets.\n"
				+ "Read the \"Read Me\" menu item under the File tab to learn how to use the program controls", "Welcome",JOptionPane.INFORMATION_MESSAGE);
		
		int input = JOptionPane.showConfirmDialog(frame, "Are you using a retina display?", "Retina Display?", JOptionPane.YES_NO_OPTION);
		if(input == JOptionPane.YES_OPTION)
		{
			retina = true;
		}
		else
		{
			retina = false;
		}
	}
	/**
	 * Returns true if this is a retina display.
	 * @return true if this is a retina display.
	 */
	public boolean getRetina()
	{
		return retina;
	}
	
	/**
	 * Returns the status area.
	 * @return the status area.
	 */
	public JTextArea getStatusArea()
	{
		return statusArea;
	}
}
