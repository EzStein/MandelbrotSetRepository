package colorFunction;

import javafx.scene.paint.*;

public class ComparableStop implements Comparable
{
	private double offset;
	private Color color;
	
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

}
