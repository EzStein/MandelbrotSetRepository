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

public class HTTPTester {
	public static void main(String[] args)
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
		HttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost("http://www.ezstein.xyz/uploader.php");
		
		HttpResponse response = null;
		BufferedReader in = null;
		try {
			
			File file = new File("/Users/Ezra/Pictures/MSet17.png");
			HttpEntity entity = MultipartEntityBuilder.create().addBinaryBody(
					"uploadFile", file, ContentType.create("image/png"), file.getName())
					.addTextBody("pass", "uploaderPassword")
					.addTextBody("Name", file.getName())
					.addTextBody("Author", "Ezra")
					.addTextBody("Description", "THIS IS A DESCRIPTION OF UP TO 65535 chars.")
					.addTextBody("UploadDate", dateFormat.format(new Date()))
					.build();
			post.setEntity(entity);
			
			response = client.execute(post);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String input;
			while((input = in.readLine()) !=null)
			{
				System.out.println(input);
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
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
}