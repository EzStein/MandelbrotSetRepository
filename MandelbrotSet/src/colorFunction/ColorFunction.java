package colorFunction;

import java.io.Serializable;
import java.util.HashMap;

import javafx.scene.paint.Color;

public interface ColorFunction
{
	public abstract Color getColor(int iterations);
	public static class ColorInfo
	{
		public static final HashMap<String, ColorFunction> COLOR_FUNCTIONS = new HashMap<String, ColorFunction>();
		
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
		};
	}
}
