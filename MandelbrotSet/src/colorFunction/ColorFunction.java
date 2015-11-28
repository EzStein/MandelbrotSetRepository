package colorFunction;

import java.util.HashMap;

import javafx.scene.paint.Color;

public interface ColorFunction
{
	public static final ColorFunction[] COLOR_FUNCTIONS = {
			new RainbowFunctionLinear(),
			new RainbowFunctionLogarithmic(),
			new BlueMagentaFunctionLinear(),
			new BluePurpleFunctionLinear(),
			new DrSeussFunctionLinear(),
			new GothicBlackFunctionLinear(),
			new GrayBlueFunctionLinear(),
			new GreenPurpleFunctionLinear(),
			new PinkBlueFunctionLinear(),
			new WinterWonderlandFunctionLinear()
	};
	public abstract Color getColor(int iterations);
	
}
