package projects.wsn.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;

/**
 * Class responsible for managing the data text files.
 * @author Alex Lacerda
 *
 */
public class FileHandler {
	/**
	 * Stores all the lines of the sensor readings file
	 */
	private static LinkedList<String> lines = new LinkedList<String>();
	
	/**
	 * the mode of debug is used to print debug messages that are spread throughout the project.
	 */
	private static boolean inDebugMode = true;
	
	/**
	 * Loads all the lines of the sensor readings file into the memory.
	 */
	public static void loadFileLines(){
		FileHandler.printForDebug("staring loading file lines...");
		long initTime = System.currentTimeMillis();
		try {
			String sensorReadingsFilePath = Configuration.getStringParameter("ExternalFilesPath/SensorReadingsFilePath");
			BufferedReader bufferedReader = getBufferedReader(sensorReadingsFilePath);
			String line = bufferedReader.readLine();
			while (line != null) {
				lines.add(line);
				line = bufferedReader.readLine();
			}
			bufferedReader.close();
			long finishTime = System.currentTimeMillis();
			long totalTimeInSecs = (finishTime - initTime)/1000;
			FileHandler.printForDebug("all " + lines.size() + " successfully loaded to memory in " + totalTimeInSecs + " second(s)");
		} catch (CorruptConfigurationEntryException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Returns a reader (<code>bufferedReader</code>) for the file path (<code>filePath</code>) passed as parameter.
	 * A <code>bufferedReader</code> can be used for reading lines of text files in a easier manner.
	 * @param filePath Path of the file for which the <code>bufferedReader</code> is going to be created.
	 * @return Returns a <code>bufferedReader</code> created for the <code>filePath</code> passed as parameter. 
	 * Returns <code>null</code> if no file is found.
	 */
	public static BufferedReader getBufferedReader(String filePath)
	{
		try {
			File file = new File(filePath);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			return br;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Print debug messages only if the inDebugMode variable is <code>true</code>.
	 * @param message message to be printed.
	 */
	public static void printForDebug(String message) {
		if (inDebugMode) {
			System.out.println("inDebugMode: " + message);
		}
	}
}
