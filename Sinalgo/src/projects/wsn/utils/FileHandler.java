package projects.wsn.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;

/**
 * Class responsible for managing data text files.
 * @author Alex Lacerda
 *
 */
public class FileHandler {
	
	/**
	 * Number of sensor nodes in the simulation (Intel Lab Data).
	 */
	private static final Integer NUMBER_OF_SENSOR_NODES = 54;
	
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
	 * Returns <code>null</code> if the filePath does not exist or if <code>filePath</code> is null.
	 */
	public static BufferedReader getBufferedReader(String filePath)
	{
		try {
			File file = new File(filePath);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			return bufferedReader;
		} catch (Exception e) {
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
	
	/**
	 * Generates a new file that is part of the file specified.
	 * The new file may be a percentage of the file specified or it can be a new file with a minimum quantity of sensor readings.
	 * You can specify both parameters (percentage and minQuantity). However, you can also specify only one of them.
	 * If you want to ignore the percentage parameter you can pass it as a null or zero value.
	 * Likewise, if you want to ignore the minQuantity  you can pass it as a null or zero value.
	 * @param filePath File for which a new file is going to be created.
	 * @param percentage A real number in the [0,100] interval representing the percentage of the file that you want 
	 * to be copied to the new file.
	 * @param minQuantity A non-negative integer number representing the minimum quantity of sensor readings that you 
	 * want a node to have in the new file. Even if the percentage number of sensor readings is less than minQuantity,
	 * the sensor node will contain the minQuantity number of sensor readings specified. For example, consider a node
	 * with 100 sensor readings and we want a new file with 10 percent of these readings. However, I also inform that I
	 * do not want less than 15 sensor readings for this sensor node. Thus, although 10 percent represents only 10 
	 * sensor readings, the new file will have 15 sensor readings as specified by the minQuantity.
	 * @throws Exception Throws exceptions if:
	 * <ol>
	 * 	<li> filePath is null. </li>
	 * 	<li> percentage is not in the [0,100] interval. </li>
	 * 	<li> minQuantity is a negative number. </li>
	 * 	<li> Both percentage and minQuantity are null or zero. </li>
	 * </ol>
	 *  
	 */
	public static void generatePercentageFile(String filePath, Float percentage, Integer minQuantity) throws Exception {
		
		if (filePath == null) {
			throw new Exception("Filepath cannot be a null value.");
		}
		
		if (percentage == null) {
			percentage = 0f;
		}
		
		if (percentage < 0 || percentage > 100) {
			throw new Exception("Percentage must be a float value in the [0,100] interval.");
		}
		
		if (minQuantity == null) {
			minQuantity = 0;
		}
		
		if (minQuantity < 0) {
			throw new Exception("minQuantity must be natural number ([0,∞]).");
		}
		
		if (percentage == 0 && minQuantity == 0) {
			throw new Exception("No files were generated because both percentage and minQuantity parameters were specified as null or zero.");
		}
		
		System.out.println("generating percentage file...");
		
		String outputFilePath = filePath.substring(0, filePath.length() - 4) + "_" + percentage + "_percent_min_" + minQuantity + ".txt";
		deletePreviousFile(outputFilePath);
		
		SortedMap<Integer, List<String>> sensorReadingsMap = getSensorReadingsMap(filePath);
		int totalSize = 0;
		long initTime = System.currentTimeMillis();
		for (Integer sensorID : sensorReadingsMap.keySet()) {
			List<String> sensorReadingsList = sensorReadingsMap.get(sensorID);
			Float limitSize = sensorReadingsList.size()*percentage/100;
			
			if (limitSize.intValue() < minQuantity) {
				limitSize = sensorReadingsList.size() < minQuantity ? sensorReadingsList.size() : minQuantity.floatValue();
			}
			
			System.out.println("Node " + sensorID + " has " +limitSize.intValue() + " sensor readings.");
			totalSize += limitSize.intValue();
			printToFile(outputFilePath, sensorReadingsList, limitSize.intValue());
			
		}
		long finishTime = System.currentTimeMillis();
		System.out.println(totalSize + " sensor readings printed to file " + outputFilePath + " in " + Utils.getTimeIntervalMessage(initTime, finishTime));
	}

	/**
	 * Deletes the file specified. This method can be used when you want to override a file, so you delete it before
	 * creating a new one. It can be done to avoid that you append lines to the file rather than overringding it.
	 * @param outputFilePath File to be deleted
	 */
	private static void deletePreviousFile(String outputFilePath) {
		File file = new File(outputFilePath);
		file.delete();
	}

	/**
	 * Loads the lines (sensor readings) of the specified file to a map of sensor readings grouped by sensor ID.
	 * @param filePath File containing the sensor readings to be loaded to the map.
	 * @return Returns a sorted map containing lists of sensor readings (String) grouped by sensor ID (String).
	 * The map returned is of the type <em>map&lt;sensorID, List&lt;sensorReading&gt;&gt;</em>.
	 * A sorted map is used because it keeps the sensor IDs in ascending order.
	 */
	private static SortedMap<Integer, List<String>> getSensorReadingsMap(String filePath) {
		System.out.println("Loading sensor readings map...");
		long initTime = System.currentTimeMillis();
		SortedMap<Integer, List<String>> sensorReadingsMap = new TreeMap<Integer, List<String>>();
		BufferedReader bufferedReader = getBufferedReader(filePath);
		try {
			String sensorReading = bufferedReader.readLine();
			while (sensorReading != null) {
				String[] sensorReadingValues = sensorReading.split(" ");
				if (sensorReadingValues.length > 4) { //evita linhas quebradas que estão no final do arquivo
					Integer sensorID = Integer.parseInt(sensorReadingValues[3]);
					if (sensorID <= NUMBER_OF_SENSOR_NODES) {
						List<String> sensorReadingsList = sensorReadingsMap.get(sensorID);
						if (sensorReadingsList == null) {
							sensorReadingsList = new ArrayList<String>();
						}
						
						sensorReadingsList.add(sensorReading);
						sensorReadingsMap.put(sensorID, sensorReadingsList);
					}
				}
				sensorReading = bufferedReader.readLine();
			}
			bufferedReader.close();
			long finishTime = System.currentTimeMillis();
			System.out.println("Sensor readings map successfully loaded in " + Utils.getTimeIntervalMessage(initTime, finishTime));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sensorReadingsMap;
	}
	
	/**
	 * Prints lines to the file specified according to the limit size informed.
	 * @param filePath Path of the file to be printed to.
	 * @param sensorReadingsList List of lines (sensor readings) to be printed to the file.
	 * @param limitSize limit number of lines of the <code>sensorReadingsList</code> to be printed to the file.
	 */
	private static void printToFile(String filePath, List<String> sensorReadingsList, Integer limitSize){
		PrintWriter printWriter = getPrintWriter(filePath);
		for (int i = 0; i < limitSize.intValue(); i++) {
			String sensorReading = sensorReadingsList.get(i);
			printWriter.append(sensorReading+"\n");
		}
		printWriter.close();
	}
	
	/**
	 * Returns <code>printWriter</code> object for the file path (<code>filePath</code>) specified as parameter.
	 * A <code>printWriter</code> can be used for writing lines of text files in a easier manner.
	 * The <code>printWriter</code> returned by this method is of the append type, that is, it will print
	 * lines to the file but will not delete the previous lines that were already in the file.
	 * @param filePath Path of the file for which the <code>printWriter</code> is going to be created.
	 * @return Returns a <code>printWriter</code> created for the <code>filePath</code> passed as parameter. 
	 * Returns <code>null</code> if the filePath does not exist.
	 */
	private static PrintWriter getPrintWriter(String filePath) {
		try {
			File file = new File(filePath);
			FileWriter fileWriter = new FileWriter(file, true);
			return new PrintWriter(fileWriter);

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;		
	}

	public static void main(String[] args) {
		try {
			FileHandler.generatePercentageFile("data/sensor_readings/data.txt", 0.5f, 200);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}