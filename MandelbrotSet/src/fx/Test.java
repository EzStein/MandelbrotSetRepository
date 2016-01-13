package fx;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.entity.mime.*;
import org.apache.http.impl.client.*;

public class Test {
	public static void main(String[] args)
	{
		HttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet("http://www.ezstein.xyz/uploads/MSet1024p3j.png");
		InputStream in = null;
		FileOutputStream out = null;
		byte[] buffer = new byte[1024];
		try {
			HttpResponse response = client.execute(get);
			in = response.getEntity().getContent();
			out = new FileOutputStream(Locator.locateFile("tmp/downloaded.png"));
			for(int length; (length = in.read(buffer)) >0;)
			{
				out.write(buffer, 0, length);
			}
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			
				try {
					if(in!=null){
						in.close();
					}
					if(out !=null){
						out.close();
					}
						
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		System.out.println("SUCCESSFUL");
	}
}