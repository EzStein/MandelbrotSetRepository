package colorFunction;

import java.util.HashMap;

import javafx.scene.paint.Color;

public class BluePurpleFunctionLinear implements ColorFunction
{
	private HashMap<Integer, Color> hm;
	public BluePurpleFunctionLinear()
    {
		hm = new HashMap<Integer, Color>();
		int i = 0;
        double R = 0, G = 127, B = 127;
        while(i < 127)
        {
            hm.put(i, new Color(R/255, G/255, B/255,1));
            G++;
            B++;
            i++;
        }
        while(i < 254)
        {
           
            G--;
            R++;
            hm.put(i, new Color(R/255, G/255, B/255,1));
            i++;
        }
        while(i<381)
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
		return hm.get(iterations%381);
	}
	
	@Override
	public String toString()
	{
		return "Blue Purple Linear";
	}
}