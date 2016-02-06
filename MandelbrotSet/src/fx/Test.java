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
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost("http://www.ezstein.xyz/pages/signIn.php");
		File file = new File("/Users/Ezra/Hello.txt");
		HttpEntity entity = MultipartEntityBuilder.create().addTextBody("username", "EzEzz")
				.addTextBody("password", "MyPass")
				.addTextBody("submit", "submit")
				.addBinaryBody("MyFile", file, ContentType.TEXT_PLAIN, file.getName())
				.build();
		
		CloseableHttpResponse response = null;
		try {
			post.setEntity(entity);
			response = client.execute(post);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(getFullRequest(post));
		System.out.println("****************************************Response*********************************************");
		System.out.println(getFullResponse(response));
		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static String getFullResponse(CloseableHttpResponse response){
		String out = "";
		try {
			out += response.getStatusLine().toString() + "\n";
			for(Header header: response.getAllHeaders())
			{
				out += header.toString() + "\n";
			}
			out+="\n";
			ByteArrayOutputStream outPut=new ByteArrayOutputStream();
			response.getEntity().writeTo(outPut);
			out +=outPut.toString("UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try {
				if(response !=null)
				response.close();
				
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return out;
	}
	
	private static String getFullRequest(HttpRequest request) {
		
		String out ="";
		out +=request.getRequestLine().toString() + "\n";
		for(Header header: request.getAllHeaders())
		{
			out += header.toString() + "\n";
		}
		out+="\n";
		if(request instanceof HttpEntityEnclosingRequest){
			ByteArrayOutputStream outPut=new ByteArrayOutputStream();
			try {
				((HttpEntityEnclosingRequest) request).getEntity().writeTo(outPut);
				out +=outPut.toString("UTF-8");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return out;
	}
}