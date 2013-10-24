package projects.wsn.utils;

/**
 * Classe contendo métodos auxiliares para facilitar o desenvolvimento. <p>
 * [Eng] Class containing auxiliary methods to ease the development.
 * @author Alex Lacerda
 *
 */
public class Utils {
	
	/**
	 * O modo de debug usado para imprimir as mensagens de debug que estão espalhadas por todo o projeto. <p>
	 * [Eng] the mode of debug is used to print debug messages that are spread throughout the project.
	 */
	private static boolean inDebugMode = false;
	
	/**
	 * Imprime mensagens de debug apenas se a variável estática <code>inDebugMode</code> estiver <code>true</code>.<p>
	 * [Eng] Print debug messages only if the <code>inDebugMode</code> static variable is <code>true</code>.
	 * @param message Mensagem a ser impressa no console. <p> [Eng] <b>message</b> Message to be printed in the console.
	 */
	public static void printForDebug(String message) {
		if (inDebugMode) {
			System.out.println("inDebugMode: " + message);
		}
	}
	
	/**
	 * Imprime no console o intervalo de tempo entre dois valores passados como parametro.<p>
	 * [Eng] Prints to the console the time interval between the two values passed as parameter.
	 * @param initTime Tempo inicial em millisegundos. <p> [Eng] <b>initTime</b> Initial time in milliseconds.
	 * @param finishTime Tempo de finalização em millisegundos. <p> [Eng] <b>finishTime</b> Finish Time in milliseconds.
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
