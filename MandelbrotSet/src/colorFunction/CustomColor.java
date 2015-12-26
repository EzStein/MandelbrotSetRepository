package colorFunction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

import javafx.scene.paint.*;

public class CustomColor implements ColorFunction, Serializable {

	private String name;
	private int range;
	private transient HashMap<Integer, Color> colorMap;
	private transient ArrayList<Stop> initialStops;
	
	public CustomColor(int range, String name, Stop...initialStops)
	{
		this.initialStops =new ArrayList<Stop>(Arrays.asList(initialStops));
		this.range = range;
		this.name = name;
		
		createColorMap(this.initialStops);
	}
	
	public CustomColor(ArrayList<Stop> initialStops, int range, String name)
	{
		this.initialStops = initialStops;
		this.range = range;
		this.name = name;
		
		createColorMap(initialStops);
	}
	
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
		if(o instanceof CustomColor)
		{
			CustomColor other = (CustomColor) o;
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
	
	public int getRange()
	{
		return range;
	}
	
	public String getName()
	{
		return name;
	}
	
	public HashMap<Integer, Color> getColorMap()
	{
		return colorMap;
	}
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
