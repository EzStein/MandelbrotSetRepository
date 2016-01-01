package fx;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;

import colorFunction.*;

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
		for(int i =0; i<=200000; i++)
		{
			char[] charPair = Character.toChars(i);
			System.out.println(new String(i + ": " + new String(charPair)));
		}
	}
}
