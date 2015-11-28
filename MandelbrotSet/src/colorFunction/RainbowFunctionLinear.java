package colorFunction;

import java.util.HashMap;

import javafx.scene.paint.Color;

public class RainbowFunctionLinear implements ColorFunction
{
	private HashMap<Integer, Color> hm;
	public RainbowFunctionLinear()
	{
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
	
}
