package fx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class Locator
{
	public static final String OS_NAME = System.getProperty("os.name");
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
		if(OS_NAME.equals("Mac OS X"))
		{
			File dir = new File(System.getProperty("user.home")+"/Library/Application Support/" + appTitle);
			if(!dir.exists())
			{
				dir.mkdirs();
			}
			file = new File(dir.getAbsolutePath() + "/" + pathName);
			if(!file.exists())
			{
				PrintWriter writer = new PrintWriter(file);
				writer.close();
			}
		}
		
		return file.getAbsolutePath();
	}
}
