package fx;

import java.io.*;

/**
 * A class used to locate files in a file structure in an OS independent way.
 * For mac, it creates a directory ~/Library/Application\ support/FractalApp which contains all the files for saved colors and regions.
 * For windows, it creates a directory in the appData data folder.
 * For linux, it will create a hidden directory .FractalApp in the home folder.
 * @author Ezra
 *
 */
public class Locator
{
	
	public static final int OS_WINDOWS = 0;
	public static final int OS_MAC = 1;
	public static final int OS_LINUX = 2;
	public static final int OS_SOLARIS = 3;
	public static final int OS_UNKNOWN =4;
	
	/**
	 * Contains the name of the current operating system.
	 */
	public static final String OS_NAME = System.getProperty("os.name").toLowerCase();
	
	/**
	 * Contains the name of this program.
	 */
	public static final String appTitle = "FractalApp";
	
	/**
	 * Locates this file in the file system structure.
	 * If the text file does not currently exist, it will create it.
	 * Returns the path to that file.
	 * @param pathName Pathname relative to the parent directory of the file structure system.
	 * @return the absolute path to this file.
	 * @throws FileNotFoundException
	 */
	public static String locateFile(String pathName) throws FileNotFoundException
	{
		File file = new File("");
		if(OS_NAME.indexOf("mac")>=0)
		{
			File dir = new File(System.getProperty("user.home")+"/Library/Application Support/" + appTitle);
			if(!dir.exists())
			{
				dir.mkdirs();
			}
			file = new File(dir.getAbsolutePath() + "/" + pathName);
			if(!file.exists())
			{
				/*Creates the file and overwrites it*/
				PrintWriter writer = new PrintWriter(file);
				writer.close();
			}
		}
		else if(OS_NAME.indexOf("win")>=0)
		{
			File dir = new File(System.getenv("APPDATA")+ File.pathSeparator + appTitle);
			if(!dir.exists())
			{
				dir.mkdirs();
			}
			file = new File(dir.getAbsolutePath() + File.pathSeparator + pathName);
			if(!file.exists())
			{
				/*Creates the file and overwrites it*/
				PrintWriter writer = new PrintWriter(file);
				writer.close();
			}
		}
		else if(OS_NAME.indexOf("nix") >= 0 || OS_NAME.indexOf("nux") >= 0 || OS_NAME.indexOf("aix") > 0 )
		{
			File dir = new File(System.getProperty("user.home") + "/." + appTitle);
			if(!dir.exists())
			{
				dir.mkdirs();
			}
			file = new File(dir.getAbsolutePath() + "/" + pathName);
			if(!file.exists())
			{
				/*Creates the file and overwrites it*/
				PrintWriter writer = new PrintWriter(file);
				writer.close();
			}
			
		}
		else if(OS_NAME.indexOf("sunos") >= 0)
		{
			File dir = new File(System.getProperty("user.home") + "/." + appTitle);
			if(!dir.exists())
			{
				dir.mkdirs();
			}
			file = new File(dir.getAbsolutePath() + "/" + pathName);
			if(!file.exists())
			{
				/*Creates the file and overwrites it*/
				PrintWriter writer = new PrintWriter(file);
				writer.close();
			}
		}
		else
		{
			System.out.println("Unknown OS!");
		}
		
		return file.getAbsolutePath();
	}
	
	
	public static boolean exists(String fileName)
	{
		if(OS_NAME.indexOf("mac")>=0)
		{
			File file = new File(System.getProperty("user.home")+"/Library/Application Support/" + appTitle + "/" + fileName);
			return file.exists();
		}
		else
		{
			return false;
		}
	}
	
	public static int getOS()
	{
		if(OS_NAME.indexOf("mac")>=0)
		{
			return OS_MAC;
		}
		else if(OS_NAME.indexOf("win")>=0)
		{
			return OS_WINDOWS;
		}
		else if(OS_NAME.indexOf("nix") >= 0 || OS_NAME.indexOf("nux") >= 0 || OS_NAME.indexOf("aix") > 0 )
		{
			return OS_LINUX;
		}
		else if(OS_NAME.indexOf("sunos") >= 0)
		{
			return OS_SOLARIS;
		}
		else
		{
			return OS_UNKNOWN;
		}
	}
}
