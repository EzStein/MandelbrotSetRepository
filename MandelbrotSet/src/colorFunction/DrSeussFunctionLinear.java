package colorFunction;

import java.util.HashMap;

import javafx.scene.paint.Color;

public class DrSeussFunctionLinear implements ColorFunction
{
	private String name;
	private HashMap<Integer, Color> hm;
	public DrSeussFunctionLinear()
    {
		name = "Dr. Seuss";
		hm = new HashMap<Integer, Color>();
		hm.put(0, new Color(0, 0, 1,1));
		hm.put(1, new Color(0, 1, 0,1));
    }
	@Override
	public Color getColor(int iterations)
	{
		return hm.get(iterations%2);
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
		if(!(o instanceof DrSeussFunctionLinear))
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
