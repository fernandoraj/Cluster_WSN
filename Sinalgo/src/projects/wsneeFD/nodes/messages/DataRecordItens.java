package projects.wsneeFD.nodes.messages;

import java.util.Vector;

/**
 * Class (structure) to store important data from sersors readings, like: <p>
 * char type (Type of sensor data, e.g.: t=Temp., h=Hum., l=Lum. or v=Volt.), <br>
 * double value (Absolute value), <br>
 * double time (Date/time from value reading), <br> 
 * double batLevel (Battery power level sensor) and <br> 
 * int round (Round number) 
 * @author Fernando Rodrigues
 * 
 */

public class DataRecordItens
{
	public Vector<DataRecord> dataRecords;

	private boolean nonRead = true;
	
	private int[][] types2;
	
	private double[][] values2; // deverá conter somente a variavel independente e os valores não correlacionados
	
	private double[] times;	// deverá conter somente a variavel independente e os valores não correlacionados
	
	private double[] batLevels;
	
	private int[] rounds;
//	//TODO Colocar um flag(e/ou array de flags) indicando a presença(e/ou a quantidade) de coeficientes no dataRecordItensToSink para a remontagem de dados no sink
	private boolean thereIsCoefficients;
	
	private boolean[] correlationFlags;
	
	private int independentIndex;
	
	private double[] regressionB;
	
	private double[] regressionA;
// adicionar double[][] cofficients;
	/**
	 * Retorna o tamanho do "dataRecords" contido neste "DataRecordItens"
	 * @return tamanho do dataRecords
	 */
	public int size() {
		return dataRecords.size();
	} // end size()
	
	/**
	 * Returns the element at the specified position in "dataRecords" Vector.
	 * @param i index of element to return
	 * @return object at the specified index
	 */
	public DataRecord get(int i) {
		return dataRecords.get(i);
	} // end get(int i)
	
	public double[] getDataRecordValues(int i){
		return dataRecords.get(i).values;
	}
	
	public double getDataRecordTimes(int i){
		return dataRecords.get(i).time;
	}
	
	public int[] getDataRecordTyps(int i){
		return dataRecords.get(i).typs;
	}
	
	public double getDataRecordBatLevel(int i){
		return dataRecords.get(i).batLevel;
	}
	
	public int getDataRecordRound(int i){
		return dataRecords.get(i).round;
	}
	/**
	 * Atualiza o "dataRecords" estrutura do "currentNode" pela leitura de "windowSize" quantidade de dados do tipo "dataType" <p>
	 * [Eng] Updates the "dataRecords" structure from the "currentNode" by the reading of "windowSize" quantity of data from type "dataType"
	 * @param currentNode Sensor node to have the dataRecords updated
	 * @param dataTypes Types of data to be read by sensor
	 */
//	private void updatedataRecords(SimpleNode currentNode, int[] dataTypes, int windowSize) {
//		for (int i=0; i < windowSize; i++) {
//			currentNode.dataRecords.add(getData(currentNode, dataTypes));
//		}
//		while (currentNode.dataRecords.size() > windowSize) {
//			currentNode.dataRecords.removeElementAt(0);
//		}
//	}

	/**
	 * Adiciona os respectivos valores para o atributo dataRecords do sensor (SimpleNode)<p>[Eng] Adds the respective values to dataRecords attribute from this sensor (SimpleNode)
	 * @param typ Tipo de dado do sensor, como t=Temp., h=Hum., l=Lum. or v=Volt.<p>[Eng] Type of sensor data, like t=Temp., h=Hum., l=Lum. or v=Volt.
	 * @param val Valor Absoluto<p>[Eng] Absolute value
	 * @param tim Data/Tempo do valor lido no formato double<p> [Eng] Date/time from value reading in double format
	 * @param bat Nível de Potência da bateria no sensor<p>[Eng] Battery power level sensor
	 * @param rnd Número do round<p>[Eng] Round number
	 */
	public void add(int[] typs, double[] vals, double tim, double bat, int rnd, int windowSize)
	{
		if (this.dataRecords == null) {
			this.dataRecords = new Vector<DataRecord>();
		}
		DataRecord dr = new DataRecord();
		
		dr.typs = typs;
		dr.values = vals;
		dr.time = tim;
		dr.batLevel = bat;
		dr.round = rnd;
		
		dataRecords.add(dr);
		//Implements a FIFO structure with the vector 'dataRecords' with at most 'slidingWindowSize' elements
		while (dataRecords.size() > windowSize)
		{
			dataRecords.removeElementAt(0);
		}
		nonRead = true;
	} // end add(char typ, double val, double tim, double bat, int rnd, int windowSize)
	/**
	 * Adiciona os respectivos valores para o atributo dataRecords do sensor (SimpleNode)<p>[Eng] Adds the respective values to dataRecords attribute from this sensor (SimpleNode)
	 * @param dataRecord O registo de dados com os dados a serem adicionados ao "dataRecords" vector a partir do sensor atual <p> [Eng] Data record with the data to be add to "dataRecords" vector from the current sensor
	 * @param slidingWindowSize Indica o tamanho da janela de dados a serem mantidos
	 */	
	public void add(DataRecord dataRecord, int slidingWindowSize)
	{
		if (dataRecord == null) {
			return;
		}

		if (this.dataRecords == null)
		{
			this.dataRecords = new Vector<DataRecord>();
		}
		
		dataRecords.add(dataRecord);
		
		//Implements a FIFO structure with the vector 'dataRecords' with at most 'slidingWindowSize' elements
		while (dataRecords.size() > slidingWindowSize)
		{
			dataRecords.removeElementAt(0);
		}
		nonRead = true;
	} // end adddataRecords(char typ, double val, double tim, double bat, int rnd)

	
	private void readData()
	{
		if (nonRead)
		{
			int tam = 0;
			if (dataRecords != null)
			{
				tam = dataRecords.size();
			}
//			// AQUI! verificar motivo do types2 ser um array bidimensional
//			types2 = new int[tam][];
//			values2 = new double[tam][];
//			times = new double[tam];
//			batLevels = new double[tam];
//			rounds = new int[tam];
//			
//			for (int i=0; i<tam; i++)
//			{
//				if (dataRecords.get(i) != null)
//				{
//					types2[i] = ((DataRecord)dataRecords.get(i)).typs;
//					values2[i] = ((DataRecord)dataRecords.get(i)).values;
//					times[i] = ((DataRecord)dataRecords.get(i)).time;
//					batLevels[i] = ((DataRecord)dataRecords.get(i)).batLevel;
//					rounds[i] = ((DataRecord)dataRecords.get(i)).round;
//				}
//				else
//				{
//					types2[i] = null;
//					values2[i] = null;
//					times[i] = 0.0;
//					batLevels[i] = 0.0;
//					rounds[i] = 0;
//				}
//			}
//			
//			nonRead = false;
		}
	}
	
	public int[] getDataRecordTypes(int ind)
	{
		readData();
		return types2[ind];
	}
	
//	public double[] getDataRecordValues(int ind)
//	{
//		readData();
//		return values2[ind];
//	}
	
	public double[][] getDataRecordValues2()
	{
		readData();
		return values2;
	}

	public double[] getDataRecordTimes()
	{
		readData();
		return times;
	}

	public double[] getDataRecordBatLevels()
	{
		readData();
		return batLevels;
	}
	
	public int[] getDataRecordRounds()
	{
		readData();
		return rounds;
	}
	public boolean getThereIsCoefficients(){
		return thereIsCoefficients;
	}
	
	public void setThereIsCoefficients(boolean result){
		thereIsCoefficients = result;
	}
	
	public boolean[] getCorrelationFlags(){
		return correlationFlags;
	}
	
	public void setCorrelationFlags(boolean[] results){
		correlationFlags = results;
	}
	
	public void setIndependentIndex(int ind){
		
		independentIndex = ind;
	}
	
	public double[] getRegreesionCoefA(){
		return regressionA;
	}
	
	public double[] getRegressionCoefB(){
		return regressionB;
	}
	
	public void setRegressionCoefs(double[] b, double[] a)
	{
		regressionB = b;
		regressionA = a;

	}

	public void clearDRValuesOf (int index[]){
		double tempValues[] = new double[(dataRecords.get(0).values.length)-(index.length)];
		int aux = 0;
		for (int i=0; i < dataRecords.size(); i++){
			for (int j=0; j < dataRecords.get(0).values.length; j++){
				for(int k=0; k < index.length; k++){
					if(k != index[j]){
						tempValues[aux]= dataRecords.get(i).values[k];
						aux++;
					}
				}
			}
			//dataRecords.get(i).values = new double[(dataRecords.get(0).values.length)-(index.length)];
			dataRecords.get(i).values = tempValues;
		}
		
//		if (dataRecords != null && dataRecords.get(index[i]) != null){
//			for (int j=0; j < dataRecords.size(); j++){
//				dataRecords.get(i).values[index[i]] = 0.0;
//			}
//		}
	} // end dataRecords
}