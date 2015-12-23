package colorFunction;

import java.util.HashMap;

import javafx.scene.paint.Color;

public class NameLater implements ColorFunction
{
	private String name;
	private HashMap<Integer, Color> hm;
	public NameLater()
	{
		name = "NameLater";
		hm = new HashMap<Integer, Color>();
		int i = 0;
        double R = 0, G = 0, B = 0;
        hm.put(i, new Color(R/255, G/255, B/255,1));
        while(i < 85)
        {
            B+=3;
            i++;
            hm.put(i, new Color(R/255, G/255, B/255,1));
            
        }
        while(i < 170)
        {
            R++;
            G++;
            i++;
            hm.put(i, new Color(R/255, G/255, B/255,1));
            
        }
        while(i < 255)
        {
        	R++;
        	i++;
        	hm.put(i, new Color(R/255, G/255, B/255,1));
            
        }
        while(i < 340)
        {
        	R-=2;
        	i++;
            hm.put(i, new Color(R/255, G/255, B/255,1));
           
        }
        while(i < 425)
        {
        	B-=2;
        	i++;
            hm.put(i, new Color(R/255, G/255, B/255,1));
            
        }
        while(i < 510)
        {
        	B--;
        	G--;
        	i++;
            hm.put(i, new Color(R/255, G/255, B/255,1));
            
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
		if(!(o instanceof NameLater))
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
