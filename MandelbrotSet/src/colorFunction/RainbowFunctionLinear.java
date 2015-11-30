package colorFunction;

import java.util.HashMap;

import javafx.scene.paint.Color;

public class RainbowFunctionLinear implements ColorFunction
{
	private String name;
	private HashMap<Integer, Color> hm;
	public RainbowFunctionLinear()
	{
		name = "Rainbow Linear";
		hm = new HashMap<Integer, Color>();
		int i = 0;
		double R = 255, G = 0, B = 0;
		while(i < 127)
		{
			hm.put(i, Color.color(R/255, G/255, B/255,1));
			R-= 2;
			G+= 2;
			i++;
		}
		R = 0; G = 255; B = 0;
		while(i < 254)
		{
			hm.put(i, Color.color(R/255, G/255, B/255,1));
			G -= 2;
			B += 2;
			i++;
		}
		R = 0; G = 0; B = 255;
		while(i < 381)
		{
			hm.put(i, Color.color(R/255, G/255, B/255,1));
			R += 2;
			B -= 2;
			i ++;
		}
	}
	
	@Override
	public Color getColor(int iterations)
	{
		return hm.get(iterations%381);
	}
	
	@Override
	public String toString()
	{
		return name;
	}
	@Override
	public boolean equals(Object o)
	{
		if(o == null)
		{
			return false;
		}
		if(o == this)
		{
			return true;
		}
		if(!(o instanceof RainbowFunctionLinear))
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	@Override
	public int hashCode()
	{
		return name.hashCode();
	}
	
}
