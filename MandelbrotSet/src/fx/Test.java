package fx;

import java.io.*;
import java.math.BigDecimal;
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
		
		//ALIAJFEPOIJEW
		Region<BigDecimal> a = new Region<BigDecimal>(
				new BigDecimal("-2"),
				new BigDecimal("2"),
				new BigDecimal("2"),
				new BigDecimal("-2"));
		
		Region<BigDecimal> b = new Region<BigDecimal>(
				new BigDecimal("-2"),
				new BigDecimal("2"),
				new BigDecimal("2"),
				new BigDecimal("-2"));
		
		SavedRegion sr1 = new SavedRegion("", true, 10,11,12,a, true, false,
				new ComplexBigDecimal(new BigDecimal("11"), new BigDecimal("23"), 100), CustomColorFunction.COLOR_FUNCTIONS.get(0));
		
		System.out.println(a.hashCode() + " " + b.hashCode());
	}
}