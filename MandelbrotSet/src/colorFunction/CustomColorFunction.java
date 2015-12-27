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
public class CustomColorFunction implements ColorFunction, Serializable {
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
			if(other.getRange() == range && other.getName().equals(name) && other.getColorMap().equals(colorMap))
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
		return name.hashCode()*range + colorMap.hashCode();
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
