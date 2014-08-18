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
 * Classe responsável por administrar os arquivos de dados. <p>
 * [Eng] Class responsible for managing data text files.
 * @author Alex Lacerda
 * @author Fernando Rodrigues
 */
public class FileHandler {
	
	/**
	 * Número de leituras iniciais de nós sensores (para cada sensor) para ser ignorado no processo de filtragem<p>
	 * [Eng] Number of initial sensor nodes readings (by each sensor) to be disregarded in the filtering process
	 */
	static final int quantLearning = 70;
	
	/**
	 * Número de nós sensores na simulação (Intel Lab Data)<p>
	 * [Eng] Number of sensor nodes in the simulation (Intel Lab Data).
	 */
	public static final Integer NUMBER_OF_SENSOR_NODES = 54;
	
	/**
	 * Armazena todas as linhas do arquivo de leitura dos sensores. O arquivo de leitura dos sensores contém leituras de todos os sensores da rede.<p>
	 * [Eng] Stores all the lines of the sensor readings file. The sensor readings file contains sensor readings from all sensor nodes of the network.
	 */
	private static List<String> nodesSensorReadingsList = new ArrayList<String>();

	/**
	 * Carrega todas as linhas do arquivo de leitura dos sensores na memória (<code>nodesSensorReadingsList</code> variável estática). O arquivo de leitura dos sensores contém leituras de todos os sensores da rede.<p>
	 * [Eng] Loads all the lines of the sensor readings file into the memory (<code>nodesSensorReadingsList</code> static variable).
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
	 * Retorna o objeto <code>bufferedReader</code> do caminho do arquivo (<code>filePath</code>) especificado como parametro.
	 * Um <code>bufferedReader</code> pode ser usado para leitura de linhas de um arquivo de texto de maneira mais fácil.<p>
	 * [Eng] Returns <code>bufferedReader</code> object for the file path (<code>filePath</code>) specified as parameter.
	 * A <code>bufferedReader</code> can be used for reading lines of text files in a easier manner.
	 * @param filePath Caminho do arquivo onde o <code>bufferedReader</code> será criado.<p> [Eng] <b>filePath</b> Path of the file for which the <code>bufferedReader</code> is going to be created.
	 * @return Retorna um <code>bufferedReader</code> criado através do <code>filePath</code> passado como parametro. Returns  <code>null</code>  se o caminho do arquivo náo existir ou se <code>filePath</code> for nulo <p>
	 * [Eng] Returns a <code>bufferedReader</code> created for the <code>filePath</code> passed as parameter. <code>null</code> if the filePath does not exist or if <code>filePath</code> is null.
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
	 * Cria um novo arquivo que é parte do arquivo especificado.
	 * O novo arquivo pode ter uma porcentagem do arquivo especificado ou pode ser um novo arquivo com uma quantidade minima de leitura de sensores.
	 * Você pode especificar ambos os parametros (porcetage e minQuantity). Vocë pode também especificar apenas um deles.
	 * Se você quiser ignorar o parametro porcentage, você pode passá-lo como nulo ou valor zero.
	 * Da mesma forma, se você quiser ignorar o minQuantity, você pode passá-lo como nulo ou valor zero.<p>
	 * [Eng] Generates a new file that is part of the file specified.
	 * The new file may be a percentage of the file specified or it can be a new file with a minimum quantity of sensor readings.
	 * You can specify both parameters (percentage and minQuantity). However, you can also specify only one of them.
	 * If you want to ignore the percentage parameter you can pass it as a null or zero value.
	 * Likewise, if you want to ignore the minQuantity  you can pass it as a null or zero value.
	 * @param filePath Caminho onde o novo arquivo será criado.<p> [Eng] <b>filePath</b> Path for which a new file is going to be created.
	 * @param percentage Um número real no intervalo [0,100] representando a porcetagem do arquivo que você quer que seja copiado para o novo arquivo. <p> [Eng] <b>percentage</b> A real number in the [0,100] interval representing the percentage of the file that you want 
	 * to be copied to the new file.
	 * @param minQuantity um número inteiro não negativo representando a quantidade mínima  de leituras de sensores que você quer que um nó faça neste novo arquivo. 
	 * Mesmo se o número percentual de leituras do sensor é inferior a minQuantity, o nó sensor irá conter a minQuantity de leitura de sensores especificado. 
	 * Por exemplo, considere um nó com 100 leituras de sensor e nós queremos um novo arquivo com 10% destas leituras.  No entanto, eu também informo que eu não quero menos de 15 leituras de sensores, especificado pelo minQuantity.<p>
	 * [Eng]<b>minQuantity</b> A non-negative integer number representing the minimum quantity of sensor readings that you 
	 * want a node to have in the new file. Even if the percentage number of sensor readings is less than minQuantity,
	 * the sensor node will contain the minQuantity number of sensor readings specified. For example, consider a node
	 * with 100 sensor readings and we want a new file with 10 percent of these readings. However, I also inform that I
	 * do not want less than 15 sensor readings for this sensor node. Thus, although 10 percent represents only 10 
	 * sensor readings, the new file will have 15 sensor readings as specified by the minQuantity.
	 * @throws Exception Lança exceção se:
	 * <ol>
	 * 	<li> filePath é nulo. </li>
	 * 	<li> percentage não está no intervalo [0,100]. </li>
	 * 	<li> minQuantity é um número negativo. </li>
	 * 	<li> Ambos percentage e minQuantity são nulos ou zero. </li>
	 * </ol>
	 *  [Eng] Exception Throws exceptions if:
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
	 * Tipo que representa os campos do arquivo de dados da experiência Intel Lab Data<p>
	 * [Eng] Type to represents the fields from data file of Intel Lab Data experience
	 * @author Fernando Rodrigues
	 * @date 20/04/2013
	 */
	public static enum TypeData{
		DATE, TIME, EPOCH, SENSORID, TEMP, LUM, HUM, VOLT;
	}
	
	/**
	 * Gera um novo arquivo que é parte do arquivo especificado.
	 * O novo arquivo será parte do arquivo especificado, filtrado pelos valores dos campos especificados no parametro "typeDataToBeFiltrated", i.e, a
	 * diferença máxima aceitável entre os valores sequenciais do mesmo campo é o valor no parametro "acceptDiff".<p>
	 * [Eng] Generates a new file that is part of the file specified.
	 * The new file will be a part of the file specified, filtrated by the field values specified in "typeDataToBeFiltrated" parameter, i.e., the
	 * max difference acceptable between the sequential values of the same field is the value in "acceptDiff" parameter.
	 * @param filePath Caminho onde um novo arquivo será criado. <p> [Eng] <b>filePath</b> Path for which a new file is going to be created. 
	 * @param typeDataToBeFiltrated Tipos que podem ser filtrados são: TypeData.DATA, TypeData.TIME, TypeData.EPOCH, TypeData.SENSORID, TypeData.TEMP, 
	 * TypeData.HUM, TypeData.LUM, TypeData.VOLT <p> [Eng] <b>typeDataToBeFiltrated</b> Types that can be filtrated are: TypeData.DATA, TypeData.TIME, TypeData.EPOCH, TypeData.SENSORID, TypeData.TEMP, 
	 * TypeData.HUM, TypeData.LUM, TypeData.VOLT
	 * @param acceptDiff Um número real representando o limiar de erro aceitável nos valores sequencias do campo do arquivo <p> [Eng] <b>acceptDiff</b> A real number representing the threshold acceptable of the sequential values from field of file
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
	 * Deleta o arquivo especificado. Este método pode ser usado quando você quer substituir um arquivo, então você deleta-o antes de criar um novo. Pode ser feito para evitar que você acrescente linhas no arquivo ao invés de substituí-lo. <p>
	 * [Eng] Deletes the file specified. This method can be used when you want to override a file, so you delete it before
	 * creating a new one. It can be done to avoid that you append lines to the file rather than overringding it.
	 * @param outputFilePath Arquivo a ser deletado <p> [Eng] <b>outputFilePath</b> File to be deleted 
	 */
	private static void deletePreviousFile(String outputFilePath) {
		File file = new File(outputFilePath);
		file.delete();
	}

	/**
	 * Carrega as linhas (leituras de sensor) do arquivo especificado para um mapa de leituras de sensores agrupados pelo ID dos sensores.<p>
	 * [Eng] Loads the lines (sensor readings) of the specified file to a map of sensor readings grouped by sensor ID.
	 * @param filePath Arquivo contendo as leituras de sensores que serão carregadas no mapa. <p> [Eng] <b>filePath</b> File containing the sensor readings to be loaded to the map.
	 * @return Retorna um mapa classificado contendo listas de leituras de sensores (String) agrupado pelo ID dos sensores(String). O mapa retornado é do tipo <em>map&lt;sensorID, List&lt;sensorReading&gt;&gt;</em>. 
	 * Um mapa classificado é usado por que mantém os IDs dos sensores em ordem crescente. <p> [Eng] Returns a sorted map containing lists of sensor readings (String) grouped by sensor ID (String).
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
	 * Compara duas linhas (leituras de sensores) passada por parametro "sensorReadingPrevious" e "sensorReadingCurrent") para testar se o campo especificado no parametro "td"está dentro do valor "acceptInterval", retornando "true" se isto está acontecendo e "false" se não.<p>
	 * [Eng] Compares the two lines (sensor readings) passed by parameters ("sensorReadingPrevious" and "sensorReadingCurrent") to test if the field 
	 * specified in "td" parameter is inside the "acceptInterval" value, returning "true" if this is happening and "false" otherwise.
	 * @param sensorReadingPrevious Linha do arquivo de dados(leitura de sensores) lida antes (anterior) <p> [Eng] <b>sensorReadingPrevious</b> Line of data file (sensor reading) read before (previous)
	 * @param sensorReadingCurrent Linha do arquivo de dados(leitura de sensores) lida depois(atual) <p> [Eng] <b>sensorReadingCurrent</b> Line of data file (sensor reading) read after (current)
	 * @param td Tipo de dado identificando o campo do arquivo a ser comparado (testado) <p> [Eng]  <b>td</b> Type Data identifying the field of data to the compared (tested)  
	 * @param acceptInterval Um número real representando o limiar de erro aceitável para a sequencia de valores do campo de arquivo. <p> [Eng] <b>acceptInterval</b> A real number representing the threshold acceptable of the sequential values from field of file
	 * @return "true" se o campo especificado no parametro "td" estiver dentro do valor "acceptInterval" e "false" se não estiver. <p> [Eng]lf "true" if the field specified in "td" parameter is inside the "acceptInterval" value and "false" otherwise.
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
					Double tempPrevius = ((tempStrPrevius != null && tempStrPrevius != "")?Double.parseDouble(tempStrPrevius): 0.0);
					Double tempCurrent = ((tempStrCurrent != null && tempStrCurrent != "")?Double.parseDouble(tempStrCurrent): 0.0);
/*					
					if (tempPrevius==122.153 || tempCurrent==122.153){
						System.out.println("AQUI!!");
					}
*/					
					double diffTemp = Math.abs(tempCurrent - tempPrevius);
					if (diffTemp > acceptInterval) {
						includeLine = false;
					}
				}
				catch (NumberFormatException pe) {
					includeLine = false;
					//System.out.println("tempStrPrevius = ("+tempStrPrevius+")");
					//System.out.println("tempStrCurrent = ("+tempStrCurrent+")");
					pe.printStackTrace();
				}
				break;
			case HUM:
				String humStrPrevius = sensorReadingPreviousValues[5];
				String humStrCurrent = sensorReadingCurrentValues[5];
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
			case LUM:
				String lumStrPrevius = sensorReadingPreviousValues[6];
				String lumStrCurrent = sensorReadingCurrentValues[6];
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
	 * Carrega as linhas (leituras  de  sensores) do arquivo especificado para o mapa de leitura de sensor agrupado pelos IDs dos sensores.<p>
	 * [Eng] Loads the lines (sensor readings) of the specified file to a map of sensor readings grouped by sensor ID.
	 * @param filePath Arquivo contendo as leituras de sensores que serão carregadas no mapa. <p> [Eng] <b>filePath</b> File containing the sensor readings to be loaded to the map.
	 * @param td Tipo de dado identificando o campo de dado a ser comparado (testado) <p> [Eng] <b>td</b> Type Data identifying the field of data to the compared (tested)  
	 * @param acceptInterval Um número real representando o limiar de erro aceitável na sequencia de valores do campo do arquivo. <p> [Eng] <b>acceptInterval</b> A real number representing the threshold acceptable of the sequential values from field of file
	 * @return Retorna um mapa classificado contendo listas de leituras de sensores (String) agrupado pelo ID dos sensores(String). O mapa retornado é do tipo <em>map&lt;sensorID, List&lt;sensorReading&gt;&gt;</em>. 
	 * Um mapa classificado é usado por que mantém os IDs dos sensores em ordem crescente.<p> [Eng] Returns a sorted map containing lists of sensor readings (String) grouped by sensor ID (String).
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
	 * Imprime linhas para o arquivo especificado de acordo com o tamanho limite informado. <p>
	 * [Eng] Prints lines to the file specified according to the limit size informed.
	 * @param filePath Caminho do arquivo para ser impresso. <p> [Eng] filePath Path of the file to be printed to.
	 * @param sensorReadingsList Lista de linhas (leituras de sensores) a ser impressas no arquivo. <p> [Eng] <b>sensorReadingsList</b> List of lines (sensor readings) to be printed to the file.
	 * @param limitSize Número limite de linhas do <code>sensorReadingsList</code> a ser impressas no arquivo. <p> [Eng] <b>limitSize</b> limit number of lines of the <code>sensorReadingsList</code> to be printed to the file.
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
	 * Retorna um objeto <code>printWriter</code> do caminho (<code>filePath</code>) especificado como parametro.
	 * Um <code>printWriter</code> pode ser usado para escrever linhas em arquivos de texto de uma maneira mais fácil.
	 * O <code>printWriter</code> retornado por este método é do tipo adição, isto é, ele imprimirá linhas do arquivo mas não irá deletar as linhas antigas que já estavam no arquivo. <p>
	 * [Eng] Returns <code>printWriter</code> object for the file path (<code>filePath</code>) specified as parameter.
	 * A <code>printWriter</code> can be used for writing lines of text files in a easier manner.
	 * The <code>printWriter</code> returned by this method is of the append type, that is, it will print
	 * lines to the file but will not delete the previous lines that were already in the file.
	 * @param filePath Caminho do arquivo onde o <code>printWriter</code> será criado. <p> [Eng] <b>filePath</b> Path of the file for which the <code>printWriter</code> is going to be created.
	 * @return Retorna o <code>printWriter</code> criado a partir do <code>filePath</code> passado como parametro. <p> [Eng]  Returns a <code>printWriter</code> created for the <code>filePath</code> passed as parameter. 
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
	 * Executa o programa, criando um novo arquivo de dados, usando como base os 3 parametros passados, que podem ser: <p>
	 * A) Para usar o método "generatePercentageFile": <br>
	 * 1) o caminho dos dados originais. <br>
	 * 2) a porcentagem de dados de cada sensor a ser copiado para o novo arquivo, e <br> 
	 * 3) a quantidade minima de leituras de cada sensor a ser copiado para o novo arquivo; OU <p>
	 * B) Para usar o método "generateFiltratedFile": <br>
	 * 1) o caminho dos dados originais. <br>
	 * 2) O tipo (campo) de dado de cada sensor que será filtrado no novo arquivo, que pode ser um desses:
	 * TypeData.DATE, TypeData.TIME, TypeData.EPOCH, TypeData.SENSORID, TypeData.TEMP, TypeData.HUM, TypeData.LUM, TypeData.VOLT, e <br> 
	 * 3) A diferença máxima aceitável entre as duas leituras sequenciais de cada sensor para ser copiadas para o novo arquivo; <p>
	 * [Eng] Run the program, creating a new data file, using as a basis the past 3 parameters, that can be: <p>
	 * A) To use the "generatePercentageFile" method: <br>
	 * 1) the path of the original data, <br>
	 * 2) the percentage of data from each sensor to be copied to the new file, and <br> 
	 * 3) the minimum amount of readings from each sensor to be copied to the new file; OR <p>
	 * B) To use the "generateFiltratedFile" method: <br>
	 * 1) the path of the original data, <br>
	 * 2) the type (field) of data from each sensor to be filtrated to the new file, which can be one of these:
	 * TypeData.DATE, TypeData.TIME, TypeData.EPOCH, TypeData.SENSORID, TypeData.TEMP, TypeData.HUM, TypeData.LUM, TypeData.VOLT, and <br> 
	 * 3) the maximum difference acceptable between of two sequential readings from each sensor to be copied to the new file; <p>
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			//FileHandler.generatePercentageFile("data/sensor_readings/data.txt", null, 10000);
			//FileHandler.generatePercentageFile("data/sensor_readings/data.txt", null, 20000);
			
			//TypeData.DATE, TypeData.TIME, TypeData.EPOCH, TypeData.SENSORID, TypeData.TEMP, TypeData.HUM, TypeData.LUM, TypeData.VOLT
			//Remember of configuring "quantLearning" attribute value
			//FileHandler.generateFiltratedFile("data/sensor_readings/data_0.0_percent_min_10000.txt", TypeData.TEMP, 10.0);
			FileHandler.generateFiltratedFile("data/sensor_readings/data_0.0_percent_min_20000_2.0_filtrated_by_DATE-ErrorSens15.txt", TypeData.DATE, 2.0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}