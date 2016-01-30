package fx;

import java.io.*;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.text.*;
import java.util.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.entity.mime.*;
import org.apache.http.impl.client.*;

import colorFunction.CustomColorFunction;

public class Test {
	public static void main(String[] args)
	{
		try {
			InetAddress address = InetAddress.getByName("www.ezstein.xyz");
			System.out.println(address.isReachable(1000));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}