package projects.wsneeFD.utils;

/**
 * Class containing auxiliary methods to ease the development.
 * @author Alex Lacerda
 *
 */
public class Utils {
	
	/**
	 * the mode of debug is used to print debug messages that are spread throughout the project.
	 */
	private static boolean inDebugMode = false; // true => "Verbose mode" (for debug) | false =>"Non verbose mode" (normal mode)
	
	/**
	 * Print debug messages only if the <code>inDebugMode</code> static variable is <code>true</code>.
	 * @param message Message to be printed in the console.
	 */
	public static void printForDebug(Object message) {
		if (inDebugMode) {
			System.out.println("inDebugMode: " + message);
		}
	}
	
	/**
	 * Prints to the console the time interval between the two values passed as parameter.
	 * @param initTime Initial time in milliseconds.
	 * @param finishTime Finish Time in milliseconds.
	 * @return 
	 */
	public static String getTimeIntervalMessage(Long initTime, Long finishTime) {
		Float timeInterval = (float) (finishTime - initTime);
		if (timeInterval < 1000) { // prints time in milliseconds
			return timeInterval + " millisecond(s)";
			
		} else if (timeInterval < 60000) { // prints time in seconds
			return (timeInterval/1000f) + " second(s)";
			
		} else if (timeInterval < 3600000) { // prints time in seconds
			return (timeInterval/60000f) + " minute(s)";
			
		} else { // prints time in hours
			return (timeInterval/3600000f) + " hour(s)";
		}
	}
}
