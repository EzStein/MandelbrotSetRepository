package fx;

import java.io.*;
import java.net.*;

/**
 *
 * @author Ezra
 *
 */
public class Test {
	/**
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		try {
			InetAddress address = InetAddress.getByName("www.ezstein.xyz");
			System.out.println(address.isReachable(10000));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}