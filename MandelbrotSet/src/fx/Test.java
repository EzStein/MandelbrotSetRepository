package fx;

import java.math.*;
import java.util.ArrayList;

import colorFunction.CustomColor;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;

public class Test
{
	public static void main(String[] args)
	{
		ArrayList<Stop> stops = new ArrayList<Stop>();
		stops.add(new Stop(11,Color.WHITE));
		stops.add(new Stop(2,Color.WHITE));
		stops.add(new Stop(3,Color.WHITE));
		stops.add(new Stop(11,Color.WHITE));
		stops.add(new Stop(5,Color.WHITE));
		stops.add(new Stop(6,Color.WHITE));
		stops.add(new Stop(7,Color.WHITE));
		stops.add(new Stop(4,Color.WHITE));
		new CustomColor(stops,100);
	}
}
