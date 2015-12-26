package colorFunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;

public interface ColorFunction
{
	public abstract Color getColor(int iterations);
	public static class ColorInfo
	{
		public static ArrayList<CustomColor> COLOR_FUNCTIONS = new ArrayList<CustomColor>();
		
		static
		{
			COLOR_FUNCTIONS.add(
					new CustomColor(400,"Rainbow Linear",
					new Stop((double)0, new Color(1,0,0,1)),
					new Stop((double) 1/3, new Color(0,1,0,1)),
					new Stop((double)2/3, new Color(0,0,1,1)),
					new Stop((double)1, new Color(1,0,0,1))));
			
			COLOR_FUNCTIONS.add(
					new CustomColor(400,"Blue Magenta",
					new Stop((double)0, new Color(0,0.5,1,1)),
					new Stop((double) 1/3, new Color(0.5,0,0.5,1)),
					new Stop((double)2/3, new Color(0.5,0,0,1)),
					new Stop((double)1, new Color(0,0.5,1,1))));
			
			COLOR_FUNCTIONS.add(
					new CustomColor(400,"Blue Purple",
					new Stop((double)0, new Color(0,0.5,0.5,1)),
					new Stop((double) 1/3, new Color(0,1,1,1)),
					new Stop((double)2/3, new Color(0.5,0.5,1,1)),
					new Stop((double)1, new Color(0,0.5,0.5,1))));
			
			COLOR_FUNCTIONS.add(
					new CustomColor(400,"Pink Blue",
					new Stop((double)0, new Color(1,0.5,0.5,1)),
					new Stop((double) 1/3, new Color(0,0.5,1,1)),
					new Stop((double)2/3, new Color(0.5,1,0.5,1)),
					new Stop((double)1, new Color(1,0.5,0.5,1))));
			
			COLOR_FUNCTIONS.add(
					new CustomColor(500,"Noir",
					new Stop((double)0, new Color(0,0,0,1)),
					new Stop((double) 1/2, new Color(1,1,1,1)),
					new Stop((double)1, new Color(0,0,0,1))));
			
			COLOR_FUNCTIONS.add(
					new CustomColor(500,"Cool Color",
					new Stop((double)0, new Color(0,0,0,1)),
					new Stop((double)1/6, new Color(0,0,1,1)),
					new Stop((double)2/6, new Color((double)1/3,(double)1/3,1,1)),
					new Stop((double)3/6, new Color((double)2/3,(double)1/3,1,1)),
					new Stop((double)4/6, new Color(0,(double)1/3,1,1)),
					new Stop((double)5/6, new Color(0,(double)1/3,(double)1/3,1)),
					new Stop((double)1, new Color(0,0,0,1))));
			
		}
		
		/*public static final HashMap<String, ColorFunction> COLOR_FUNCTIONS = new HashMap<String, ColorFunction>();
		
		static {
			COLOR_FUNCTIONS.put(new RainbowFunctionLinear().toString(), new RainbowFunctionLinear());
			COLOR_FUNCTIONS.put(new RainbowFunctionLogarithmic().toString(),new RainbowFunctionLogarithmic());
			COLOR_FUNCTIONS.put(new BlueMagentaFunctionLinear().toString(), new BlueMagentaFunctionLinear());
			COLOR_FUNCTIONS.put(new BluePurpleFunctionLinear().toString(), new BluePurpleFunctionLinear());
			COLOR_FUNCTIONS.put(new DrSeussFunctionLinear().toString(), new DrSeussFunctionLinear());
			COLOR_FUNCTIONS.put(new GothicBlackFunctionLinear().toString(), new GothicBlackFunctionLinear());
			COLOR_FUNCTIONS.put(new GrayBlueFunctionLinear().toString(), new GrayBlueFunctionLinear());
			COLOR_FUNCTIONS.put(new GreenPurpleFunctionLinear().toString(), new GreenPurpleFunctionLinear());
			COLOR_FUNCTIONS.put(new PinkBlueFunctionLinear().toString(), new PinkBlueFunctionLinear());
			COLOR_FUNCTIONS.put(new WinterWonderlandFunctionLinear().toString(), new WinterWonderlandFunctionLinear());
			COLOR_FUNCTIONS.put(new YellowOrangeFunctionLinear().toString(), new YellowOrangeFunctionLinear());
			COLOR_FUNCTIONS.put(new YellowOrangeFunctionLogarithmic().toString(), new YellowOrangeFunctionLogarithmic());
			COLOR_FUNCTIONS.put(new BlueGodess().toString(), new BlueGodess());
			COLOR_FUNCTIONS.put(new BubbleYum().toString(), new BubbleYum());
			COLOR_FUNCTIONS.put(new Noir().toString(), new Noir());
			COLOR_FUNCTIONS.put(new NameLater().toString(), new NameLater());
		};*/
	}
}
