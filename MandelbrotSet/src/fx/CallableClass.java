package fx;

/**
 * An interface that defines one method, the call method.
 * Used for anonymous inner classes that define a custom call method.
 * @author Ezra
 * @version 1.0
 * @since 2015
 */
public interface CallableClass
{
	/*Consider making this class have generic return type*/
	/**
	 * Called by another class.
	 * Must be implemented by called class.
	 */
	public abstract void call();
}
