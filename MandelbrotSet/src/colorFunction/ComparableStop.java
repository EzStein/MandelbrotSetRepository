package colorFunction;


import java.io.*;

import javafx.scene.paint.*;

/**
 * A class that is essentially identical to Stop except it is comparable and serializable.
 * Used to make ordered lists of stops.
 * @author Ezra Stein
 * @version 1.0
 * @since 2015
 *
 */
public class ComparableStop implements Comparable<ComparableStop>, Serializable
{
	/**
	 * Version ID
	 */
	private static final long serialVersionUID = 1L;
	private double offset;
	private transient Color color;
	
	/**
	 * Constructs this object with a stop argument.
	 * @param stop	The stop used to construct the offset and the color.
	 */
	public ComparableStop(Stop stop)
	{
		offset = stop.getOffset();
		color = stop.getColor();
	}
	
	/**
	 * Returns the color of this stop.
	 * @return the color of this stop.
	 */
	public Color getColor()
	{
		return color;
	}
	
	/**
	 * Returns the offset.
	 * @return the offset.
	 */
	public double getOffset()
	{
		return offset;
	}
	
	@Override
	public int compareTo(ComparableStop other)
	{
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
	
	/**
	 * Returns a stop with the same color and offset of this object.
	 * @return a stop with the same color and offset of this object.
	 */
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
