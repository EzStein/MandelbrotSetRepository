package colorFunction;

import java.util.*;

import javafx.scene.paint.*;

public class CustomColor implements ColorFunction {

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
		for(int j = 1; j<stops.size(); j++)
		{
			double a = stops.get(j).getOffset()*range;
			double b = stops.get(j-1).getOffset()*range;
			double rIncrement = (stops.get(j).getColor().getRed()-stops.get(j-1).getColor().getRed())/(a-b);
			double bIncrement = (stops.get(j).getColor().getBlue()-stops.get(j-1).getColor().getBlue())/(a-b);
			double gIncrement = (stops.get(j).getColor().getGreen()-stops.get(j-1).getColor().getGreen())/(a-b);
			while(i<=a)
			{
				B+=bIncrement;
				R+=rIncrement;
				G+=gIncrement;
				i++;
				colorMap.put(i, new Color(R,G,B,1));
			}
		}
	}
	
	@Override
	public Color getColor(int iterations) {
		return colorMap.get(iterations%(range+1));
	}

}
