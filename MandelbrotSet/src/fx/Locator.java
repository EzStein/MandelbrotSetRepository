package fx;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

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
	private static final OS OS_NAME;
	static {
		String osName = System.getProperty("os.name").toLowerCase();
		
		if(osName.indexOf("mac")>=0)
		{
			OS_NAME =  OS.MAC;
		}
		else if(osName.indexOf("win")>=0)
		{
			OS_NAME =  OS.WINDOWS;
		}
		else if(osName.indexOf("nix") >= 0 || osName.indexOf("nux") >= 0 || osName.indexOf("aix") > 0 )
		{
			OS_NAME = OS.LINUX;
		}
		else if(osName.indexOf("sunos") >= 0)
		{
			OS_NAME = OS.SOLARIS;
		}
		else
		{
			OS_NAME = OS.UNKNOWN;
		}
	}
	
	/**
	 * Contains the name of this program.
	 */
	public static final String appTitle = "FractalApp";
	
	/**
	 * Locates this file in the file system structure.
	 * If the file does not currently exist, it will create it.
	 * Returns the path to that file.
	 * @param filePath Pathname relative to the parent directory of the file structure system.
	 * @return the absolute path to this file.
	 * @throws IOException
	 */
	public static Path locateFile(Path filePath) throws IOException
	{
		Path file = getBaseDirectoryPath().resolve(filePath);
		Files.createDirectories(file.getParent());
		if(!Files.exists(file))
		{
			Files.createFile(file);
		}
		return file;
	}
	
	/**
	 * Locates this file in the file system structure.
	 * If the file does not currently exist, it will create it.
	 * Returns the path to that file.
	 * @param filePath Pathname relative to the parent directory of the file structure system.
	 * @return the absolute path to this file.
	 * @throws IOException
	 */
	public static Path locateFile(String filePath) throws IOException
	{
		return locateFile(Paths.get(filePath));
	}
	
	/**
	 * Deletes the contents of the parent folder and then creates the file.
	 * @param filePath
	 * @return The path to the located file.
	 * @throws IOException
	 */
	public static Path locateUniqueFile(Path filePath) throws IOException
	{
		Path file = getBaseDirectoryPath().resolve(filePath);
		Files.createDirectories(file.getParent());
		
		/*Deletes contents of parent directory*/
		Files.walk(file.getParent()).filter(Files::isRegularFile).forEach(fileToDelete ->{
			try {
				//System.out.println(fileToDelete.toString());
				Files.delete(fileToDelete);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			});
		if(!Files.exists(file))
		{
			Files.createFile(file);
		}
		return file;
	}
	
	/**
	 * Deletes the contents of the parent folder and then creates the file.
	 * @param filePath
	 * @return The path to the located file.
	 * @throws IOException
	 */
	public static Path locateUniqueFile(String filePath) throws IOException
	{
		return locateUniqueFile(Paths.get(filePath));
	}
	
	/**
	 * Attempts to locate the unique file in a directory. Use this if you know the location of
	 * a file, but not its name. This method fails if there is not exactly one file
	 * in the directory, or if the path is not a directory.
	 * @param dirPath
	 * @return the path to the unique file.
	 * @throws IOException
	 */
	public static Path getUniqueFile(Path dirPath) throws IOException
	{
		Path dir = getBaseDirectoryPath().resolve(dirPath);
		if(! Files.isDirectory(dir))
		{
			throw new IOException("NOT A DIRECTORY");
		}
		List<Path> files = Files.walk(dir).filter(Files::isRegularFile).collect(Collectors.toList());
		if(files.size()>1)
		{
			throw new IOException("NOT UNIQUE FILE");
		}
		else if(files.size()==0)
		{
			throw new IOException("EMPTY DIRECTORY");
		}
		
		return files.get(0);
	}
	
	/**
	 * Attempts to locate the unique file in a directory. Use this if you know the location of
	 * a file, but not its name. This method fails if there is not exactly one file
	 * in the directory, or if the path is not a directory.
	 * @param dirPath
	 * @return the path to the unique file.
	 * @throws IOException
	 */
	public static Path getUniqueFile(String dirPath) throws IOException
	{
		return getUniqueFile(Paths.get(dirPath));
	}
	
	/**
	 * Determines whether the file exists in the file system.
	 * @param filePath	The path of the file which may or may not exist.
	 * @return			True if the file exists. False otherwise.
	 */
	public static boolean exists(Path filePath)
	{
		Path file = getBaseDirectoryPath().resolve(filePath);
		return Files.exists(file);
	}
	
	/**
	 * Determines whether the file exists in the file system.
	 * @param filePath	The path of the file which may or may not exist.
	 * @return			True if the file exists. False otherwise.
	 */
	public static boolean exists(String filePath)
	{
		return exists(Paths.get(filePath));
	}
	
	/**
	 * Returns the absolute path to the base directory of the file system.
	 * For Mac it is ~/Library/Application\ Support/AppName
	 * For Windows it is APPDATA\AppName
	 * For Linux and Solaris it is a hidden directory ~/.AppName
	 * @return the absolute path to the base directory of the file system.
	 */
	public static Path getBaseDirectoryPath()
	{
		if(isMac())
		{
			return Paths.get(System.getProperty("user.home")+"/Library/Application Support/" + appTitle);
		}
		else if(isWindows())
		{
			return Paths.get(System.getenv("APPDATA") + File.separator + appTitle);
		}
		else if(isLinux())
		{
			return Paths.get(System.getProperty("user.home") + "/." + appTitle);
			
		}
		else if(isSolaris())
		{
			return Paths.get(System.getProperty("user.home") + "/." + appTitle);
		}
		else
		{
			System.out.println("***UNKNOWN OS***");
			return Paths.get("");
		}
	}
	
	/**
	 * Returns true if this is a mac.
	 * @return true if this is a mac.
	 */
	public static boolean isMac()
	{
		return OS_NAME.equals(OS.MAC);
	}
	
	/**
	 * Returns true if this is a windows system.
	 * @return true if this is a windows system.
	 */
	public static boolean isWindows()
	{
		return OS_NAME.equals(OS.WINDOWS);
	}
	
	/**
	 * Returns true if this is a linux system.
	 * @return true if this is a linux system.
	 */
	public static boolean isLinux()
	{
		return OS_NAME.equals(OS.LINUX);
	}
	
	/**
	 * Returns true if this is a solaris system.
	 * @return true if this is a solaris system.
	 */
	public static boolean isSolaris()
	{
		return OS_NAME.equals(OS.SOLARIS);
	}
	
}
