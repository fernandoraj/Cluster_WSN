package projects.wsn.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;

/**
 * Class responsible for managing data text files.
 * @author Alex Lacerda
 *
 */
public class FileHandler {
	
	/**
	 * Stores all the lines of the sensor readings file.
	 * The sensor readings file contains sensor readings from all sensor nodes of the network.
	 */
	private static List<String> nodesSensorReadingsList = new ArrayList<String>();

	/**
	 * Loads all the lines of the sensor readings file into the memory (<code>nodesSensorReadingsList</code> static variable).
	 * The sensor readings file contains sensor readings from all sensor nodes of the network.
	 */
	private static void loadNodesSensorReadingsFromFile(){
		Utils.printForDebug("staring loading sensor readings file lines...");
		long initTime = System.currentTimeMillis();
		try {
			String sensorReadingsFilePath = Configuration.getStringParameter("ExternalFilesPath/SensorReadingsFilePath");
			BufferedReader bufferedReader = getBufferedReader(sensorReadingsFilePath);
			String line = bufferedReader.readLine();
			while (line != null) {
				nodesSensorReadingsList.add(line);
				line = bufferedReader.readLine();
			}
			bufferedReader.close();
			long finishTime = System.currentTimeMillis();
			Utils.printForDebug("all " + nodesSensorReadingsList.size() + " sensor readings successfully loaded to memory in " + Utils.getTimeIntervalMessage(initTime, finishTime));
		} catch (CorruptConfigurationEntryException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Returns <code>bufferedReader</code> object for the file path (<code>filePath</code>) specified as parameter.
	 * A <code>bufferedReader</code> can be used for reading lines of text files in a easier manner.
	 * @param filePath Path of the file for which the <code>bufferedReader</code> is going to be created.
	 * @return Returns a <code>bufferedReader</code> created for the <code>filePath</code> passed as parameter. 
	 * Returns <code>null</code> if the filePath does not exist.
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
	 * Returns a List containing sensor readings of all nodes loaded from the file.
	 * @return Returns a List of all nodes sensor readings.
	 */
	public static List<String> getNodesSensorReadingsQueue () {
		if (nodesSensorReadingsList.isEmpty()) {
			loadNodesSensorReadingsFromFile();
		} 
		return nodesSensorReadingsList;
	}
	
}
