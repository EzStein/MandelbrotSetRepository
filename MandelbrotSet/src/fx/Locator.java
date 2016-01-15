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
		
		File dir = new File(getBaseDirectoryPath());
		if(!dir.exists())
		{
			dir.mkdirs();
		}
		
		if(new File(pathName).getParent() != null)
		{
			File dir2 =new File(dir.getAbsolutePath() + File.separator + new File(pathName).getParent());
			if(!dir2.exists())
			{
				dir2.mkdirs();
			}
		}
		
		
		File file = new File(dir.getAbsolutePath() + File.separator + pathName);
		if(!file.exists())
		{
			/*Creates the text file and overwrites it*/
			PrintWriter writer = new PrintWriter(file);
			writer.close();
		}
		return file.getAbsolutePath();
	}
	
	public static String locateFileAndDelete(String pathName) throws FileNotFoundException
	{
		File dir = new File(getBaseDirectoryPath());
		if(!dir.exists())
		{
			dir.mkdirs();
		}
		
		if(new File(pathName).getParent() != null)
		{
			File dir2 =new File(dir.getAbsolutePath() + File.separator + new File(pathName).getParent());
			if(!dir2.exists())
			{
				dir2.mkdirs();
			}
		}
		
		
		File file = new File(dir.getAbsolutePath() + File.separator + pathName);
		if(file.exists())
		{
			file.delete();
		}
		return file.getAbsolutePath();
	}
	
	/**
	 * Deletes the contents of the tmp folder returns the path to the file to be created in it.
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String locateFileInTmp(String fileName) throws FileNotFoundException
	{
		File dir = new File(getBaseDirectoryPath() + "/tmp");
		if(!dir.exists())
		{
			dir.mkdirs();
		}
		File[] files = dir.listFiles();
		for(File file: files)
		{
			file.delete();
		}
		
		return dir.getAbsolutePath() + "/" + fileName;
	}
	
	/**
	 * Determines whether the file exists in the file system.
	 * @param fileName	The path of the file which may or may not exist.
	 * @return			True if the file exists. False otherwise.
	 */
	public static boolean exists(String fileName)
	{
		File file = new File(getBaseDirectoryPath() + File.separator + fileName);
		return file.exists();
	}
	
	/**
	 * Returns the OS. Should be compared with the fields OS_MAC etc.
	 * @return the OS. Should be compared with the fields OS_MAC etc.
	 */
	public static OS getOS()
	{
		if(OS_NAME.indexOf("mac")>=0)
		{
			return OS.MAC;
		}
		else if(OS_NAME.indexOf("win")>=0)
		{
			return OS.WINDOWS;
		}
		else if(OS_NAME.indexOf("nix") >= 0 || OS_NAME.indexOf("nux") >= 0 || OS_NAME.indexOf("aix") > 0 )
		{
			return OS.LINUX;
		}
		else if(OS_NAME.indexOf("sunos") >= 0)
		{
			return OS.SOLARIS;
		}
		else
		{
			return OS.UNKNOWN;
		}
	}
	
	/**
	 * Returns the absolute path to the base directory of the file system.
	 * For Mac it is ~/Library/Application\ Support/AppName
	 * For Windows it is APPDATA\AppName
	 * For Linux and Solaris it is a hidden directory ~/.AppName
	 * @return the absolute path to the base directory of the file system.
	 */
	public static String getBaseDirectoryPath()
	{
		File dir = new File("");
		if(isMac())
		{
			dir = new File(System.getProperty("user.home")+"/Library/Application Support/" + appTitle);
		}
		else if(isWindows())
		{
			dir = new File(System.getenv("APPDATA")+ File.separator + appTitle);
		}
		else if(isLinux())
		{
			dir = new File(System.getProperty("user.home") + "/." + appTitle);
			
		}
		else if(isSolaris())
		{
			dir = new File(System.getProperty("user.home") + "/." + appTitle);
		}
		else
		{
			System.out.println("Unknown OS!");
		}
		
		return dir.getAbsolutePath();
	}
	
	/**
	 * Returns true if this is a mac.
	 * @return true if this is a mac.
	 */
	public static boolean isMac()
	{
		return getOS().equals(OS.MAC);
	}
	
	/**
	 * Returns true if this is a windows system.
	 * @return true if this is a windows system.
	 */
	public static boolean isWindows()
	{
		return getOS().equals(OS.WINDOWS);
	}
	
	/**
	 * Returns true if this is a linux system.
	 * @return true if this is a linux system.
	 */
	public static boolean isLinux()
	{
		return getOS().equals(OS.LINUX);
	}
	
	/**
	 * Returns true if this is a solaris system.
	 * @return true if this is a solaris system.
	 */
	public static boolean isSolaris()
	{
		return getOS().equals(OS.SOLARIS);
	}
}
