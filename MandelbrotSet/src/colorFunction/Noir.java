package colorFunction;

import java.util.HashMap;

import javafx.scene.paint.Color;

public class Noir implements ColorFunction
{
	private String name;
	private HashMap<Integer, Color> hm;
	public Noir()
	{
		name = "Noir";
		hm = new HashMap<Integer, Color>();
		int i = 0;
        double R = 0, G = 0, B = 0;
        while(i < 84)
        {
            hm.put(i, new Color(R/255, G/255, B/255,1));
            G++;
            B++;
            R++;
            
            i++;
        }
        while(i < 168)
        {
        	hm.put(i, new Color(R/255, G/255, B/255,1));
            G++;
            B++;
            R++;
            
            i++;
        }
        while(i <= 255)
        {
        	hm.put(i, new Color(R/255, G/255, B/255,1));
        	G++;
        	B++;
        	R++;
            i++;
        }
        while(i < 339)
        {
        	G--;
        	B--;
        	R--;
            hm.put(i, new Color(R/255, G/255, B/255,1));
            i++;
        }
        while(i < 424)
        {
        	G--;
        	B--;
        	R--;
            hm.put(i, new Color(R/255, G/255, B/255,1));
            i++;
        }
        while(i <= 510)
        {
        	G--;
        	B--;
        	R--;
            hm.put(i, new Color(R/255, G/255, B/255,1));
            i++;
        }
    }
	@Override
	public Color getColor(int iterations)
	{
		//return hm.get(iterations % 511);
		return hm.get(((int) (Math.sqrt(100*iterations)*Math.log(iterations))%511));
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
		if(!(o instanceof Noir))
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
