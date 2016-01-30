package colorFunction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

import javafx.scene.paint.*;

/**
 * Represents a serializable color function that is generated from Stops.
 * @author Ezra Stein
 * @version 1.0.
 * @since 2015.
 */
public class CustomColorFunction implements Serializable {
	
	/**
	 * Creates and holds the default colors.
	 */
	public final static ArrayList<CustomColorFunction> COLOR_FUNCTIONS = new ArrayList<CustomColorFunction>();
	
	static
	{
		COLOR_FUNCTIONS.add(
				new CustomColorFunction(400,"Rainbow Linear",
				new Stop((double)0, new Color(1,0,0,1)),
				new Stop((double) 1/3, new Color(0,1,0,1)),
				new Stop((double)2/3, new Color(0,0,1,1)),
				new Stop((double)1, new Color(1,0,0,1))));
		
		COLOR_FUNCTIONS.add(
				new CustomColorFunction(400,"Blue Magenta",
				new Stop((double)0, new Color(0,0.5,1,1)),
				new Stop((double) 1/3, new Color(0.5,0,0.5,1)),
				new Stop((double)2/3, new Color(0.5,0,0,1)),
				new Stop((double)1, new Color(0,0.5,1,1))));
		
		COLOR_FUNCTIONS.add(
				new CustomColorFunction(400,"Blue Purple",
				new Stop((double)0, new Color(0,0.5,0.5,1)),
				new Stop((double) 1/3, new Color(0,1,1,1)),
				new Stop((double)2/3, new Color(0.5,0.5,1,1)),
				new Stop((double)1, new Color(0,0.5,0.5,1))));
		
		COLOR_FUNCTIONS.add(
				new CustomColorFunction(2,"Dr. Seuss",
				new Stop((double)0, new Color(0,1,0,1)),
				new Stop((double)1, new Color(0,0,1,1))));
		
		COLOR_FUNCTIONS.add(
				new CustomColorFunction(400,"Gothic Black",
				new Stop((double)0, new Color(0,0,0,1)),
				new Stop((double) 1/3, new Color(0.5,0,0.5,1)),
				new Stop((double)2/3, new Color(0.5,0,0,1)),
				new Stop((double)1, new Color(0,0,0,1))));
		
		COLOR_FUNCTIONS.add(
				new CustomColorFunction(400,"Gray Blue",
				new Stop((double)0, new Color(0.5,0.5,0.5,1)),
				new Stop((double) 1/3, new Color(0,0,1,1)),
				new Stop((double)2/3, new Color(0,0,0.5,1)),
				new Stop((double)1, new Color(0.5,0.5,0.5,1))));
		
		COLOR_FUNCTIONS.add(
				new CustomColorFunction(400,"Bubble Yum",
				new Stop((double)0, new Color(0.5,0,0,1)),
				new Stop((double) 1/3, new Color(1,0.5,0.5,1)),
				new Stop((double)2/3, new Color(1,0,0.5,1)),
				new Stop((double)1, new Color(0.5,0,0,1))));
		
		COLOR_FUNCTIONS.add(
				new CustomColorFunction(400,"Blue Goddess",
				new Stop((double)0, new Color(0,0,0.5,1)),
				new Stop((double) 1/3, new Color(0.5,0.5,1,1)),
				new Stop((double)2/3, new Color(0.5,0,1,1)),
				new Stop((double)1, new Color(0,0,0.5,1))));
		
		COLOR_FUNCTIONS.add(
				new CustomColorFunction(400,"Pink Blue",
				new Stop((double)0, new Color(1,0.5,0.5,1)),
				new Stop((double) 1/3, new Color(0,0.5,1,1)),
				new Stop((double)2/3, new Color(0.5,1,0.5,1)),
				new Stop((double)1, new Color(1,0.5,0.5,1))));
		
		COLOR_FUNCTIONS.add(
				new CustomColorFunction(500,"Noir",
				new Stop((double)0, new Color(0,0,0,1)),
				new Stop((double) 1/2, new Color(1,1,1,1)),
				new Stop((double)1, new Color(0,0,0,1))));
		
		COLOR_FUNCTIONS.add(
				new CustomColorFunction(400,"Green Purple",
				new Stop((double)0, new Color(0,1,0.5,1)),
				new Stop((double) 1/3, new Color(0.5, 0, 0.5, 1)),
				new Stop((double)2/3, new Color(1, 0, 1, 1)),
				new Stop((double)1, new Color(0,1,0.5,1))));
		
		COLOR_FUNCTIONS.add(
				new CustomColorFunction(400,"Winter Wonderland",
				new Stop((double)0, new Color(1,1,1,1)),
				new Stop((double) 1/3, new Color(0.5,0.5,1,1)),
				new Stop((double)2/3, new Color(0.5,0.5,0.5,1)),
				new Stop((double)1, new Color(1,1,1,1))));
		
		COLOR_FUNCTIONS.add(
				new CustomColorFunction(400, "Yellow Orange",
				new Stop((double)0, new Color(1,0.5,0,1)),
				new Stop((double) 1/3, new Color(1,1,0,1)),
				new Stop((double)2/3, new Color(0.5,0.5,0,1)),
				new Stop((double)1, new Color(1,0.5,0,1))));
		
		COLOR_FUNCTIONS.add(
				new CustomColorFunction(500,"Cool Color",
				new Stop((double)0, new Color(0,0,0,1)),
				new Stop((double)1/6, new Color(0,0,1,1)),
				new Stop((double)2/6, new Color((double)1/3,(double)1/3,1,1)),
				new Stop((double)3/6, new Color((double)2/3,(double)1/3,1,1)),
				new Stop((double)4/6, new Color(0,(double)1/3,1,1)),
				new Stop((double)5/6, new Color(0,(double)1/3,(double)1/3,1)),
				new Stop((double)1, new Color(0,0,0,1))));
		
	}
	
	
	
	
	
	/**
	 * Serial id.
	 */
	private static final long serialVersionUID = 1L;
	/*
	 *Consider making these fields final. 
	 */
	private final String name;
	private final int range;
	private transient HashMap<Integer, Color> colorMap;
	private transient ArrayList<Stop> initialStops;
	
	/**
	 * Creates a colorFunction with these parameters.
	 * @param range			The range over which the the color changes. Repeats over each range.
	 * @param name			The name of this color function.
	 * @param initialStops	A list of stops. Each stop must range from zero to one.
	 * 						No two stops can have the same offset. The list may not be ordered.
	 */
	public CustomColorFunction(int range, String name, Stop...initialStops)
	{
		/*Consider making initial stops a set.*/
		this.initialStops =new ArrayList<Stop>(Arrays.asList(initialStops));
		this.range = range;
		this.name = name;
		
		createColorMap(this.initialStops);
	}
	
	/**
	 * Creates a colorFunction with these parameters.
	 * @param range			The range over which the the color changes. Repeats over each range.
	 * @param name			The name of this color function.
	 * @param initialStops	A list of stops. Each stop must range from zero to one.
	 * 						No two stops can have the same offset. The list may not be ordered.
	 */
	public CustomColorFunction(ArrayList<Stop> initialStops, int range, String name)
	{
		/*Consider making initial stops a set.*/
		this.initialStops = initialStops;
		this.range = range;
		this.name = name;
		
		createColorMap(initialStops);
	}
	
	/**
	 * Generates a hash map that maps iteration number to color using initialStops/
	 * @param initialStops the stops used. 
	 */
	public void createColorMap(ArrayList<Stop> initialStops)
	{
		colorMap = new HashMap<Integer, Color>();
		ArrayList<ComparableStop> stops = new ArrayList<ComparableStop>();
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
			double a = stops.get(j).getOffset()*range;
			double b = stops.get(j-1).getOffset()*range;
			double rIncrement = (stops.get(j).getColor().getRed()-stops.get(j-1).getColor().getRed())/(a-b-1);
			double bIncrement = (stops.get(j).getColor().getBlue()-stops.get(j-1).getColor().getBlue())/(a-b-1);
			double gIncrement = (stops.get(j).getColor().getGreen()-stops.get(j-1).getColor().getGreen())/(a-b-1);
			
			while(i<a-1)
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
	
	
	/**
	 * Returns the color associated with a given set of iterations.
	 * @param iterations
	 * @return the color associated with a certain number of iterations.
	 */
	public Color getColor(int iterations) {
		Color c;
		if(name.equals("Dr. Seuss"))
		{
			c = colorMap.get( iterations%range);
		}
		else
		{
			c = colorMap.get(((int) (Math.pow(100*iterations,0.5)*Math.log(iterations))%range));
		}
		
		if(c== null)
		{
			System.out.println(iterations + " " + colorMap.keySet().size());
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
		if(o instanceof CustomColorFunction)
		{
			CustomColorFunction other = (CustomColorFunction) o;
			if(other.getRange() == range && other.getColorMap().equals(colorMap))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public int hashCode()
	{
		return range + colorMap.hashCode();
	}
	
	/**
	 * Returns the range.
	 * @return the range.
	 */
	public int getRange()
	{
		return range;
	}
	
	/**
	 * Returns the name.
	 * @return the name.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Returns the color map.
	 * @return the color map.
	 */
	public HashMap<Integer, Color> getColorMap()
	{
		return colorMap;
	}
	
	/**
	 * Returns a deep copy of initial stops.
	 * @return a deep copy of initial stops.
	 */
	public ArrayList<Stop> getStops()
	{
		ArrayList<Stop> returnValue = new ArrayList<Stop>();
		for(Stop s: initialStops)
		{
			returnValue.add(s);
		}
		return returnValue;
	}

	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
		ArrayList<ComparableStop> stops = new ArrayList<ComparableStop>();
		for(Stop stop : initialStops)
		{
			stops.add(new ComparableStop(stop));
		}
		out.writeObject(stops);
	}
	
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		
		ArrayList<ComparableStop> stops = (ArrayList<ComparableStop>)in.readObject();
		initialStops = new ArrayList<Stop>();
		for(ComparableStop comparableStop: stops)
		{
			initialStops.add(comparableStop.getStop());
		}
		createColorMap(initialStops);
	}
}
