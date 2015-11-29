package colorFunction;

import java.util.HashMap;

import javafx.scene.paint.Color;

public class YellowOrangeFunctionLogarithmic implements ColorFunction
{
	HashMap<Integer, Color> hm;
	public YellowOrangeFunctionLogarithmic()
	{
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
		return "Yellow Orange Logarithmic";
	}
	
	
}
