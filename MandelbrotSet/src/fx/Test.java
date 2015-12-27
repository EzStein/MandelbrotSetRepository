package fx;

import java.io.*;

import colorFunction.ColorFunction;

/**
 * A test class
 * @author Ezra
 *
 */
public class Test
{
	/**
	 * The main method
	 * @param args		unused.
	 */
	public static void main(String[] args)
	{
		ObjectOutputStream out = null;
		try {
			File file = new File(Locator.locateFile("SavedColors.txt"));
			 out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(ColorFunction.ColorInfo.COLOR_FUNCTIONS);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try {
				if(out != null)
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
