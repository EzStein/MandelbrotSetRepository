package fx;

import java.io.*;
import java.sql.*;
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
		Connection conn=null;
		Statement stmt=null;
		ResultSet set=null;
		try {
			//THIS DISPLAYS PASS IN PLAINTEXT!!!!
			conn = DriverManager.getConnection("jdbc:mysql://www.ezstein.xyz:3306/WebDatabase", "java", "javaPass");
			stmt = conn.createStatement();
			set = stmt.executeQuery("SELECT * FROM Linked");
			while(set.next()){
				for(int i = 1; i<3; i++)
				{
					System.out.println(set.getString(i));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			
			try {
				set.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}