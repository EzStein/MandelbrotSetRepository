package colorFunction;


import java.io.*;

import javafx.scene.paint.*;

public class ComparableStop implements Comparable, Serializable
{
	private double offset;
	private transient Color color;
	
	public ComparableStop(Stop stop)
	{
		offset = stop.getOffset();
		color = stop.getColor();
	}
	
	public Color getColor()
	{
		return color;
	}
	
	public double getOffset()
	{
		return offset;
	}
	
	@Override
	public int compareTo(Object o)
	{
		ComparableStop other = (ComparableStop) o;
		if(other.getOffset()>offset)
		{
			return -1;
		}
		else if(other.getOffset()<offset)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
	
	public Stop getStop()
	{
		return new Stop(offset, color);
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
		out.writeDouble(color.getRed());
		out.writeDouble(color.getGreen());
		out.writeDouble(color.getBlue());
		out.writeDouble(color.getOpacity());
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		color = new Color(in.readDouble(),in.readDouble(),in.readDouble(),in.readDouble());
	}

}
