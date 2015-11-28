package colorFunction;

import java.util.HashMap;

import javafx.scene.paint.Color;

public class WinterWonderlandFunctionLinear implements ColorFunction
{
	private HashMap<Integer, Color> hm;
	public WinterWonderlandFunctionLinear()
    {
		hm = new HashMap<Integer, Color>();
		
		int i = 0;
        int R = 255, G = 255, B = 255;
        while(i < 127)
        {
            hm.put(i, new Color(R, G, B,1));
            R--;
            G--;
            i++;
        }
        while(i < 254)
        {
           
            B--;
            hm.put(i, new Color(R, G, B,1));
            i++;
        }
        while(i< 381)
        {
        	R++;
        	G++;
        	B++;
            hm.put(i, new Color(R, G, B,1));
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
		return "Winter Wonderland Linear";
	}
}
