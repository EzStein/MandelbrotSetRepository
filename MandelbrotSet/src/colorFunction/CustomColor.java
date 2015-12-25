package colorFunction;

import java.io.Serializable;
import java.util.*;

import javafx.scene.paint.*;

public class CustomColor implements ColorFunction, Serializable {

	private int range;
	private ArrayList<ComparableStop> stops;
	private HashMap<Integer, Color> colorMap;
	public CustomColor(ArrayList<Stop> initialStops, int range)
	{
		stops = new ArrayList<ComparableStop>();
		this.range = range;
		colorMap = new HashMap<Integer, Color>();
		double R,G,B;
		for(Stop stop : initialStops)
		{
			stops.add(new ComparableStop(stop));
		}
		Collections.sort(stops);
		int i = 0;
		ComparableStop init = stops.get(0);
		B = init.getColor().getBlue();
		R = init.getColor().getRed();
		G = init.getColor().getGreen();
		colorMap.put(0, new Color(R,G,B,1));
		for(int j = 1; j<stops.size(); j++)
		{
			System.out.println(stops.get(j).getOffset());
			double a = stops.get(j).getOffset()*range;
			double b = stops.get(j-1).getOffset()*range;
			System.out.println(a + " " + b);
			double rIncrement = (stops.get(j).getColor().getRed()-stops.get(j-1).getColor().getRed())/(a-b);
			double bIncrement = (stops.get(j).getColor().getBlue()-stops.get(j-1).getColor().getBlue())/(a-b);
			double gIncrement = (stops.get(j).getColor().getGreen()-stops.get(j-1).getColor().getGreen())/(a-b);
			
			while(i<=a)
			{
				B+=bIncrement;
				R+=rIncrement;
				G+=gIncrement;
				if(B>1)
				{
					B = 1;
				}
				else if(B<0)
				{
					B = 0;
				}
				if(R>1)
				{
					R = 1;
				}
				else if(R<0)
				{
					R = 0;
				}
				if(G>1)
				{
					G = 1;
				}
				else if(G<0)
				{
					G = 0;
				}
				i++;
				//System.out.println(i+ " " + R + " " +G+ " " +B);
				colorMap.put(i, new Color(R,G,B,1));
			}
		}
	}
	
	@Override
	public Color getColor(int iterations) {

		Color c = colorMap.get(((int) (Math.sqrt(100*iterations)*Math.log(iterations))%range));
		if(c== null)
		{
			System.out.println(iterations);
			return Color.WHITE;
		}
		else
		{
			return c;
		}
	}

	@Override
	public String toString()
	{
		return "Custom Color";
	}
}
