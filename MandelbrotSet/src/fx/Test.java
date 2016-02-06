package fx;

import java.io.*;
import java.net.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.apache.http.util.*;

import java.nio.charset.*;
import java.util.ArrayList;

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
			InetAddress address;
			try {
				address = InetAddress.getByName("www.ezstein.xyz");
				System.out.println(address.isReachable(10000));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}
}