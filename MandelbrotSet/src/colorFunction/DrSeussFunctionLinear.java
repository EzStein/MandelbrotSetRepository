package colorFunction;

import java.util.HashMap;

import javafx.scene.paint.Color;

public class DrSeussFunctionLinear implements ColorFunction
{
	private HashMap<Integer, Color> hm;
	public DrSeussFunctionLinear()
    {
		hm = new HashMap<Integer, Color>();
		hm.put(0, new Color(0, 0, 255,1));
		hm.put(1, new Color(0, 255, 0,1));
    }
	@Override
	public Color getColor(int iterations)
	{
		return hm.get(iterations%2);
	}
	
	@Override
	public String toString()
	{
		return "Dr Seuss Linear";
	}
}
