package mandelbrotSet;

import java.awt.event.*;
import javax.swing.*;

/**
 * This class represents a basic GUI. For convenience, it creates a JFrame, a contentPane, and a JMenuBar
 * and it implements an ActionListener to listen for JMenuItem clicks.
 * It is a superclass to any GUI.
 * 
 * @author Ezra Stein
 * @version 1.0
 * @since 2015
 */
public abstract class GUI implements ActionListener
{
	/**
	 * Holds the JMenuBar for this GUI.
	 */
	protected JMenuBar menuBar;
	/**
	 * Holds the JFrame for this GUI.
	 */
	protected JFrame frame;
	
	/**
	 * Holds the contentPane as a JPanel for this GUI.
	 */
	protected JPanel contentPane;
	
	/**
	 * Creates a basic GUI and calls the createContent() method which must be implemented by its subclass.
	 */
	public GUI()
	{
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		frame = new JFrame("GUI");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = (JPanel) frame.getContentPane();
		createContent();
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * Creates a GUI with a title name and calls the createContent() method which must be implemented by its subclass.
	 * @param name - The title of this GUI. Appears at the top of the JFrame.
	 */
	public GUI(String name)
	{
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		frame = new JFrame(name);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = (JPanel) frame.getContentPane();
		createContent();
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * Creates a basic GUI and a JMenuBar with JMenuItems.
	 * It calls the createContent() method which must be implemented by its subclass.
	 * The menuItems come in a 2D matrix of the form {{"MenuTitle1", "MenuItem1", "MenuItem2"},{"MenuTitle2", "MenuItem1", "MenuItem2"},{"MenuTitle3", "MenuItem1", "MenuItem2"},...}
	 * It attaches an ActionListener to each of these menuItems. The subclass must implement the ActionListener methods.
	 * 
	 * @param name - name of GUI.
	 * @param menuItems - 2D matrix of menuItmes: {{"MenuTitle1", "MenuItem1", "MenuItem2"},{"MenuTitle2", "MenuItem1", "MenuItem2"},...}
	 */
	public GUI(String name,String[][] menuItems)
	{
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		frame = new JFrame(name);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = (JPanel) frame.getContentPane();
		if(menuItems.length>=1)
		{	
			createMenuBar(menuItems);
		}
		frame.setJMenuBar(menuBar);
		
		createContent();
		frame.pack();
		frame.setVisible(true);	
	}
	
	/**
	 * Called by the program to add the JMenuItems stored in a 2D matrix to the JMenuBar.
	 * @param menuItems - 2D matrix of menuItems.
	 */
	private void createMenuBar(String[][] menuItems)
	{
		JMenuBar menuBar = new JMenuBar();
	
		for(int i=0; i < menuItems.length; i++ )
		{
			
			if(menuItems[i].length >=2)
			{
				JMenu menu = new JMenu(menuItems[i][0]);
				menu.setName(menuItems[i][0]);
				for(int k = 1; k < menuItems[i].length; k++)
				{
					JMenuItem item = new JMenuItem(menuItems[i][k]);
					item.setName(menuItems[i][k]);
					item.addActionListener(this);
					menu.add(item);
				}
				menuBar.add(menu);
			}
		}
		this.menuBar = menuBar;
	}
	
	/**
	 * Finds and returns the JMenuItem whose name is given.
	 * @param menuItem - The name of the JMenuItem.
	 * @return The JMenuItem if it exists, or else null.
	 */
	public JMenuItem getMenuItem(String menuItem)
	{
		for(int i = 0; i<= menuBar.getMenuCount()-1; i++)
		{
			for(int j = 0; j<= menuBar.getMenu(i).getMenuComponentCount() - 1; j++)
			{
				if(menuBar.getMenu(i).getMenuComponent(j).getName().equals(menuItem))
				{
					return (JMenuItem) menuBar.getMenu(i).getMenuComponent(j);
				}
			}
		}
		return null;
	}
	
	/**
	 * Overridden by subclass, called when the GUI initially wants to create content.
	 * Used to add content to the contentPane.
	 */
	public abstract void createContent();
}
