package colorFunction;

import java.util.HashMap;

import javafx.scene.paint.Color;

public class BlueGodess implements ColorFunction
{
	private String name;
	private HashMap<Integer, Color> hm;
	public BlueGodess()
	{
		name = "Blue Godess";
		hm = new HashMap<Integer, Color>();
		int i = 0;
        double R = 0, G = 0, B = 127;
        while(i < 127)
        {
            hm.put(i, new Color(R/255, G/255, B/255,1));
            G++;
            B++;
            R++;
            i++;
        }
        while(i < 254)
        {
           
            G--;
            hm.put(i, new Color(R/255, G/255, B/255,1));
            i++;
        }
        while(i< 381)
        {
        	R--;
        	B--;
            hm.put(i, new Color(R/255, G/255, B/255,1));
            i++;
        }
    }
	@Override
	public Color getColor(int iterations)
	{
		return hm.get(iterations % 381);
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
		if(!(o instanceof BlueGodess))
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
