package colorFunction;

import java.util.HashMap;

import javafx.scene.paint.Color;

public class YellowOrangeFunctionLogarithmic implements ColorFunction
{
	private String name;
	private HashMap<Integer, Color> hm;
	public YellowOrangeFunctionLogarithmic()
	{
		name = "Yellow Orange Logarithmic";
		hm = new HashMap<Integer, Color>();
		int i = 0;
        double R = 255, G = 127, B = 0;
        while(i < 127)
        {
            hm.put(i, new Color(R/255, G/255, B/255,1));
            G++;
            i++;
        }
        while(i < 254)
        {
           
            R--;
            G--;
            hm.put(i, new Color(R/255, G/255, B/255,1));
            i++;
        }
        while(i< 381)
        {
        	R++;
            hm.put(i, new Color(R/255, G/255, B/255,1));
            i++;
        }
    }
	@Override
	public Color getColor(int iterations)
	{
		return hm.get(((int) (100*Math.log(iterations))%381));
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
		if(!(o instanceof YellowOrangeFunctionLogarithmic))
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
