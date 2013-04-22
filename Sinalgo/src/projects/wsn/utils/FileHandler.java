package projects.wsn.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
	 * Number of initial sensor nodes readings (by each sensor) to be disregarded in the filtering process
	 */
	static final int quantLearning = 10;
	
	/**
	 * Number of sensor nodes in the simulation (Intel Lab Data).
	 */
	public static final Integer NUMBER_OF_SENSOR_NODES = 54;
	
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
	 * Type to represents the fields from data file of Intel Lab Data experience
	 * @author Fernando Rodrigues
	 * @date 20/04/2013
	 */
	public static enum TypeData{
		DATE, TIME, EPOCH, SENSORID, TEMP, LUM, HUM, VOLT;
	}
	
	/**
	 * Generates a new file that is part of the file specified.
	 * The new file will be a part of the file specified, filtrated by the field values specified in "typeDataToBeFiltrated" parameter, i.e., the
	 * max difference acceptable between the sequential values of the same field is the value in "acceptDiff" parameter.
	 * @param filePath File for which a new file is going to be created.
	 * @param typeDataToBeFiltrated Types that can be filtrated are: TypeData.DATA, TypeData.TIME, TypeData.EPOCH, TypeData.SENSORID, TypeData.TEMP, 
	 * TypeData.HUM, TypeData.LUM, TypeData.VOLT
	 * @param acceptDiff A real number representing the threshold acceptable of the sequential values from field of file
	 * @throws Exception
	 */
	public static void generateFiltratedFile(String filePath, TypeData typeDataToBeFiltrated, Double acceptDiff) throws Exception {
		
		if (filePath == null) {
			throw new Exception("Filepath cannot be a null value.");
		}
		
		if (typeDataToBeFiltrated == null) {
			throw new Exception("Type cannot be a null value.");
		}
		
		if (acceptDiff == null || (acceptDiff != null && acceptDiff == 0.0)) {
			acceptDiff = 5.0; // Default difference 
		}
/*
		if (acceptDiff < 0 || acceptDiff > 100) {
			throw new Exception("Percentage must be a float value in the [0,100] interval.");
		}
*/
		System.out.println("generating filtrated file...");
		
		String outputFilePath = filePath.substring(0, filePath.length() - 4) + "_" + acceptDiff + "_filtrated_by_" + typeDataToBeFiltrated + ".txt";
		deletePreviousFile(outputFilePath);
		
		SortedMap<Integer, List<String>> sensorReadingsMap = getSensorReadingsMap(filePath, typeDataToBeFiltrated, acceptDiff);
		int totalSize = 0;
		long initTime = System.currentTimeMillis();
		for (Integer sensorID : sensorReadingsMap.keySet()) {
			List<String> sensorReadingsList = sensorReadingsMap.get(sensorID);
			
			System.out.println("Node " + sensorID + " has " + sensorReadingsList.size() + " sensor readings.");
			totalSize++;
			printToFile(outputFilePath, sensorReadingsList, sensorReadingsList.size());
			
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
	 * Compares the two lines (sensor readings) passed by parameters ("sensorReadingPrevious" and "sensorReadingCurrent") to test if the field 
	 * specified in "td" parameter is inside the "acceptInterval" value, returning "true" if this is happening and "false" otherwise.
	 * @param sensorReadingPrevious Line of data file (sensor reading) read before (previous)
	 * @param sensorReadingCurrent Line of data file (sensor reading) read after (current)
	 * @param td Type Data identifying the field of data to the compared (tested)  
	 * @param acceptInterval A real number representing the threshold acceptable of the sequential values from field of file
	 * @return "true" if the field specified in "td" parameter is inside the "acceptInterval" value and "false" otherwise.
	 */
	public static Boolean isSensorReadingsDataInsideAcceptInterval(String sensorReadingPrevious, String sensorReadingCurrent, TypeData td, Double acceptInterval) {
		
		Boolean includeLine = true;

		String[] sensorReadingPreviousValues = sensorReadingPrevious.split(" ");
		String[] sensorReadingCurrentValues = sensorReadingCurrent.split(" ");
		
		if (sensorReadingCurrentValues.length < 7) { //evita linhas quebradas que estão no final do arquivo
			return false;
		}
		Integer sensorIDPrev = Integer.parseInt(sensorReadingPreviousValues[3]);
		Integer sensorIDCur = Integer.parseInt(sensorReadingCurrentValues[3]);
		
		if (sensorIDPrev != (sensorIDCur)) { // In this case, the two sequential sensors (readings) are from two difference sensors (ID1 != ID2) 
		//if (sensorIDPrev == (sensorIDCur-1)) { // In this case, the two sequential sensors (readings) are from two difference sensors (ID1 != ID2) 
			return true; // So, there aren't relationship between the readings from two sensors.
		}
		// else if (sensorIDPrev == (sensorIDCur-1)) {
		switch(td){
			case DATE:
				String dateStrPrevius = sensorReadingPreviousValues[0];
				String dateStrCurrent = sensorReadingCurrentValues[0];
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				Date datePrevius;
				Date dateCurrent;
				try {
					datePrevius = df.parse(dateStrPrevius);
					dateCurrent = df.parse(dateStrCurrent);
					long diff = dateCurrent.getTime() - datePrevius.getTime();
					long diffDays = diff / (24 * 60 * 60 * 1000);
					if (diffDays > acceptInterval) {
						includeLine = false;
					}
				}
				catch (ParseException pe) {
					pe.printStackTrace();
				}
				break;
			case TIME:
				String timeStrPrevius = sensorReadingPreviousValues[1];
				String timeStrCurrent = sensorReadingCurrentValues[1];
				df = new SimpleDateFormat("HH:mm:ss.SSSSSS"); // DateFormat df = new SimpleDateFormat("HH:mm:ss.SSSSS");
				Date timePrevius;
				Date timeCurrent;
				try {
					timePrevius = df.parse(timeStrPrevius);
					timeCurrent = df.parse(timeStrCurrent);
					long diff = timeCurrent.getTime() - timePrevius.getTime();
					long diffMinutes = diff / (60 * 1000);
					if (diffMinutes > acceptInterval) {
						includeLine = false;
					}
				}
				catch (ParseException pe) {
					includeLine = false;
					pe.printStackTrace();
				}
				break;
			case EPOCH: // It doesn't apply
				break;
			case SENSORID: // It doesn't apply
				break;
			case TEMP:
				String tempStrPrevius = sensorReadingPreviousValues[4];
				String tempStrCurrent = sensorReadingCurrentValues[4];
				try {
					Double tempPrevius = ((tempStrPrevius != null)?Double.parseDouble(tempStrPrevius): 0.0);
					Double tempCurrent = ((tempStrCurrent != null)?Double.parseDouble(tempStrCurrent): 0.0);
					double diffTemp = Math.abs(tempCurrent - tempPrevius);
					if (diffTemp > acceptInterval) {
						includeLine = false;
					}
				}
				catch (NumberFormatException pe) {
					includeLine = false;
					pe.printStackTrace();
				}
				break;
			case LUM:
				String lumStrPrevius = sensorReadingPreviousValues[5];
				String lumStrCurrent = sensorReadingCurrentValues[5];
				try {
					Double lumPrevius = Double.parseDouble(lumStrPrevius);
					Double lumCurrent = Double.parseDouble(lumStrCurrent);
					double diffLum = Math.abs(lumCurrent - lumPrevius);
					if (diffLum > acceptInterval) {
						includeLine = false;
					}
				}
				catch (NumberFormatException pe) {
					includeLine = false;
					pe.printStackTrace();
				}
				break;
			case HUM:
				String humStrPrevius = sensorReadingPreviousValues[6];
				String humStrCurrent = sensorReadingCurrentValues[6];
				try {
					Double humPrevius = Double.parseDouble(humStrPrevius);
					Double humCurrent = Double.parseDouble(humStrCurrent);
					double diffHum = Math.abs(humCurrent - humPrevius);
					if (diffHum > acceptInterval) {
						includeLine = false;
					}
				}
				catch (NumberFormatException pe) {
					includeLine = false;
					pe.printStackTrace();
				}
				break;
			case VOLT:
				String voltStrPrevius = sensorReadingPreviousValues[7];
				String voltStrCurrent = sensorReadingCurrentValues[7];
				try {
					Double voltPrevius = Double.parseDouble(voltStrPrevius);
					Double voltCurrent = Double.parseDouble(voltStrCurrent);
					double diffVolt = Math.abs(voltCurrent - voltPrevius);
					if (diffVolt > acceptInterval) {
						includeLine = false;
					}
				}
				catch (NumberFormatException pe) {
					includeLine = false;
					pe.printStackTrace();
				}
				break;
			default:
				includeLine = false;
				System.out.println("This is NOT a valid TypeData: td = "+td+"!");
								
		}
		return includeLine;
	}
	
	/**
	 * Loads the lines (sensor readings) of the specified file to a map of sensor readings grouped by sensor ID.
	 * @param filePath File containing the sensor readings to be loaded to the map.
	 * @param td Type Data identifying the field of data to the compared (tested)  
	 * @param acceptInterval A real number representing the threshold acceptable of the sequential values from field of file
	 * @return Returns a sorted map containing lists of sensor readings (String) grouped by sensor ID (String).
	 * The map returned is of the type <em>map&lt;sensorID, List&lt;sensorReading&gt;&gt;</em>.
	 * A sorted map is used because it keeps the sensor IDs in ascending order.
	 */
	private static SortedMap<Integer, List<String>> getSensorReadingsMap(String filePath, TypeData td, Double acceptInterval) {
		System.out.println("Loading sensor readings map with typeData...");
		long initTime = System.currentTimeMillis();
		SortedMap<Integer, List<String>> sensorReadingsMap = new TreeMap<Integer, List<String>>();
		BufferedReader bufferedReader = getBufferedReader(filePath);
		try {
			String sensorReadingPrevious = bufferedReader.readLine();
			
			Integer sensorIDPrevius = 1, sensorIDCurrent;
			
			if (sensorReadingPrevious != null) {
				String[] sensorReadingPreviousValues = sensorReadingPrevious.split(" ");
				if (sensorReadingPreviousValues.length > 7) { //evita linhas quebradas
					
					sensorIDPrevius = Integer.parseInt(sensorReadingPreviousValues[3]);
					if (sensorIDPrevius <= NUMBER_OF_SENSOR_NODES) {
						List<String> sensorReadingsList = sensorReadingsMap.get(sensorIDPrevius);
						if (sensorReadingsList == null) {
							sensorReadingsList = new ArrayList<String>();
						}
						
						sensorReadingsList.add(sensorReadingPrevious);
						sensorReadingsMap.put(sensorIDPrevius, sensorReadingsList);
					}
				}
				
				String sensorReadingCurrent = bufferedReader.readLine();
				int quantSameID = 0;
				//final int quantLearning = 100;
				
				while (sensorReadingCurrent != null) {
					
					Boolean testAcceptance = isSensorReadingsDataInsideAcceptInterval(sensorReadingPrevious, sensorReadingCurrent, td, acceptInterval);
					
					// Tests case the sequential sensor readings are inside the acceptable interval OR case the quantity of sensor readings learning  
					// is less than the threshold amount
					if (testAcceptance || (quantSameID < quantLearning)) { 
					// Tests only if the sequential sensor readings are inside the acceptable interval
//					if (testAcceptance) { 
						
						String[] sensorReadingCurrentValues = sensorReadingCurrent.split(" ");
						sensorIDCurrent = Integer.parseInt(sensorReadingCurrentValues[3]);
						if (sensorIDCurrent <= NUMBER_OF_SENSOR_NODES) {
							List<String> sensorReadingsList = sensorReadingsMap.get(sensorIDCurrent);
							if (sensorReadingsList == null) {
								sensorReadingsList = new ArrayList<String>();
							}
							
							sensorReadingsList.add(sensorReadingCurrent);
							sensorReadingsMap.put(sensorIDCurrent, sensorReadingsList);
							
							sensorReadingPrevious = sensorReadingCurrent;
							
							if (sensorIDCurrent == sensorIDPrevius) {
								quantSameID++;
							}
							else {
								quantSameID = 0;
								sensorIDPrevius = sensorIDCurrent;
							}
							
						}
					}
					sensorReadingCurrent = bufferedReader.readLine();
					
				}
				bufferedReader.close();
				long finishTime = System.currentTimeMillis();
				System.out.println("Sensor readings map successfully loaded in " + Utils.getTimeIntervalMessage(initTime, finishTime));
			}
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

	
	/**
	 * Run the program, creating a new data file, using as a basis the past 3 parameters, that can be: <p>
	 * A) To use the "generatePercentageFile" method: <br>
	 * 1) the path of the original data, <br>
	 * 2) the percentage of data from each sensor to be copied to the new file, and <br> 
	 * 3) the minimum amount of readings from each sensor to be copied to the new file; OR <p>
	 * B) To use the "generateFiltratedFile" method: <br>
	 * 1) the path of the original data, <br>
	 * 2) the type (field) of data from each sensor to be filtrated to the new file, which can be one of these:
	 * TypeData.DATA, TypeData.TIME, TypeData.EPOCH, TypeData.SENSORID, TypeData.TEMP, TypeData.HUM, TypeData.LUM, TypeData.VOLT, and <br> 
	 * 3) the maximum difference acceptable between of two sequential readings from each sensor to be copied to the new file; <p>
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			//FileHandler.generatePercentageFile("data/sensor_readings/data.txt", null, 1000);
			
			//TypeData.DATA, TypeData.TIME, TypeData.EPOCH, TypeData.SENSORID, TypeData.TEMP, TypeData.HUM, TypeData.LUM, TypeData.VOLT
			FileHandler.generateFiltratedFile("data/sensor_readings/data_0.0_percent_min_1000_5.0_filtrated_by_LUM.txt", TypeData.TIME, 1.0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}