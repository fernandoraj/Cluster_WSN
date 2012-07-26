package projects.wsn.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
	
	public static void generatePercentageFile(String filePath, Float percentage, Integer minQuantity) {
		SortedMap<Integer, List<String>> sensorReadingsMap = getSensorReadingsMap(filePath);
		for (Integer sensorID : sensorReadingsMap.keySet()) {
			List<String> sensorReadingsList = sensorReadingsMap.get(sensorID);
			Float sizeLimit = sensorReadingsList.size()*percentage/100;
			if (sizeLimit.intValue() < minQuantity) {
				sizeLimit = sensorReadingsList.size() < minQuantity ? sensorReadingsList.size() : minQuantity.floatValue();
			}
			System.out.println("Node " + sensorID + " has " +sizeLimit.intValue() + " sensor readings.");
			for (int i = 0; i < sizeLimit.intValue(); i++) {
				String sensorReading = sensorReadingsList.get(i);
//				System.out.println("\t" + sensorReading); escrever no arquivo
			}
		}
	}

	public static SortedMap<Integer, List<String>> getSensorReadingsMap(String filePath) {
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
					if (sensorID <= 54) {
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
			long finishTime = System.currentTimeMillis();
			System.out.println("Sensor readings map successfully loaded in " + Utils.getTimeIntervalMessage(initTime, finishTime));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sensorReadingsMap;
	}
	
	public static void main(String[] args) {
		FileHandler.generatePercentageFile("data/sensor_readings/all_nodes_sensor_readings.txt", 0.5f, 200);
	}
	
}
