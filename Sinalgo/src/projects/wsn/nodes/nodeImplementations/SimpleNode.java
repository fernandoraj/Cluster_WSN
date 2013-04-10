package projects.wsn.nodes.nodeImplementations;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import projects.wsn.nodes.messages.WsnMsg;
import projects.wsn.nodes.messages.WsnMsgResponse;
import projects.wsn.nodes.timers.PredictionTimer;
import projects.wsn.nodes.timers.WsnMessageResponseTimer;
import projects.wsn.nodes.timers.WsnMessageTimer;
import projects.wsn.utils.FileHandler;
import projects.wsn.utils.Utils;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;

/**
 * Class that represents an ordinary sensor node that is able to sense natural phenomena
 * (e.g. temperature, pressure, humidity) and send to sink nodes.
 * In our simulation, this node is also able to do in-networking data prediction using 
 * regression models.
 * @author Fernando Rodrigues / Alex Lacerda
 *
 */
public class SimpleNode extends Node 
{
	/**
	 * Indicates the size of Sliding Window from sensor readings to be send to sink node 
	 * when there is a "novelty".
	 */
	protected static int slidingWindowSize = 4;
	
	/**
	 * Armazenar o nó que será usado para alcançar a Estação-Base
	 */
	protected Node proximoNoAteEstacaoBase;
	
	/**
	 * Armazena o número de sequencia da última mensagem recebida
	 */
	protected Integer sequenceNumber = 0;
	
	/**
	 * Valor do último round em que houve leitura do sensor (que teve valor lido do arquivo) 
	 */
	protected int ultimoRoundLido;
	
	/**
	 * Valor (grandeza/magnitude) da última leitura do sensor 
	 */
	protected double lastValueRead;
	
	/**
	 * Tempo (data/hora em milisegundos) da última leitura do sensor 
	 */
	protected double lastTimeRead;
	
	/**
	 * Stores sensor readings of this node loaded from the sensor readings file.
	 * A linked list is being used here because as the readings are being performed 
	 * (being read from this list) by this sensor node they are discarded.
	 */
	private LinkedList<String> sensorReadingsQueue = new LinkedList<String>();
	
	/**
	 * Stores the value referring to the last line loaded from the sensor readings file in order that
	 * when the <code>sensorReadingsQueue</code> is empty and a new load from the file has to be
	 * executed to fill the <code>SensorReadingsLoadedFromFile</code> list, the loading starts from the last
	 * line read from the file.
	 */
	private int lastLineLoadedFromSensorReadingsFile;

	/**
	 * Indicates whether the sensor readings must be loaded from the file or from the memory.
	 * If this attribute is true, the node must load the sensor readings direct from the
	 * sensor readings file. Otherwise, it must load the sensor readings from the memory, 
	 * that is, from the list of the sensor readings from all nodes that is loaded in memory 
	 * beforehand by the {@link FileHandler} class. This procedure is necessary because the sensor 
	 * readings file is very large and may take too long to be loaded depending on the computer 
	 * configuration. For the cases that loading all the sensor readings to the memory 
	 * (on the <code>FileHandler</code>) is not possible, the sensor readings are loaded from the
	 * file on demand by each node.
	 */
	private boolean loadSensorReadingsFromFile = true;
	
	/**
	 * Number of prediction errors of the sensor node in this timeslot
	 */
	protected int numPredictionErrors = 0;
	
	/**
	 * Maximum (limit) Number of prediction errors of any sensor node - It also could be expressed in percentage (i.e., double) from total timeSlot
	 */
	private static final int limitPredictionError = 2;

	@Override
	public void preStep() {}

	@Override
	public void init() {}

	@Override
	public void neighborhoodChange() {}

	@Override
	public void postStep() {}

	@Override
	public void checkRequirements() throws WrongConfigurationException {}
	
	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()){
			Message message = inbox.next();
			if (message instanceof WsnMsg) //Mensagem que vai do sink para os nós sensores 
			{
				Boolean encaminhar = Boolean.TRUE;
				WsnMsg wsnMessage = (WsnMsg) message;
				
//				Utils.printForDebug("* Entrou em if (message instanceof WsnMsg) * NoID = "+this.ID);
				
				if (wsnMessage.forwardingHop.equals(this)) // A mensagem voltou. O nó deve descarta-la
				{ 
					encaminhar = Boolean.FALSE;
					
//					Utils.printForDebug("** Entrou em if (wsnMessage.forwardingHop.equals(this)) ** NoID = "+this.ID);
				}
				else if (wsnMessage.tipoMsg == 0)// Mensagem que vai do sink para os nós sensores e é um flood. Devemos atualizar a rota
				{ 
					this.setColor(Color.BLUE);

//					Utils.printForDebug("*** Entrou em else if (wsnMessage.tipoMsg == 0) *** NoID = "+this.ID);
					
					if (proximoNoAteEstacaoBase == null)
					{
						proximoNoAteEstacaoBase = inbox.getSender();
						sequenceNumber = wsnMessage.sequenceID;
						
//						Utils.printForDebug("**** Entrou em if (proximoNoAteEstacaoBase == null) **** NoID = "+this.ID);
					}
					else if (sequenceNumber < wsnMessage.sequenceID)
					{ 
					//Recurso simples para evitar loop.
					//Exemplo: Nó A transmite em brodcast. Nó B recebe a msg e retransmite em broadcast.
					//Consequentemente, nó A irá receber a msg. Sem esse condicional, nó A iria retransmitir novamente, gerando um loop.
						sequenceNumber = wsnMessage.sequenceID;
						
//						Utils.printForDebug("***** Entrou em else if (sequenceNumber < wsnMessage.sequenceID) ***** NoID = "+this.ID);
					}
					else
					{
						encaminhar = Boolean.FALSE;
						
//						Utils.printForDebug("****** Entrou em encaminhar = Boolean.FALSE; ****** NoID = "+this.ID);
					}
				} //if (wsnMessage.tipoMsg == 0)
				else if (wsnMessage.tipoMsg == 1)// Mensagem que vai do sink para os nós sensores e é um pacote transmissor de dados (coeficientes). Devemos atualizar a rota
				{ 
//					this.setColor(Color.YELLOW);
//					Integer nextNodeId = wsnMessage.popFromPath();
					
//					Utils.printForDebug("@ Entrou em else if (wsnMessage.tipoMsg == 1) @ NoID = "+this.ID+" nextNodeId = "+nextNodeId);

					encaminhar = Boolean.FALSE;
					
					//Definir roteamento de mensagem
					if (wsnMessage.destino != this)
					{
//						Utils.printForDebug("@@ Entrou em if (nextNodeId != null && wsnMessage.destino != this) @@ NoID = "+this.ID);
						
						sendToNextNodeInPath(wsnMessage);
					}
					else if (wsnMessage.destino == this) //Se este for o nó de destino da mensagem...
					{ 
//						sequenceNumber = wsnMessage.sequenceID;
						this.setColor(Color.RED);
						
//						Utils.printForDebug("@@@ Entrou em else if (wsnMessage.destino == this && nextNodeId == null) @@@ NoID = "+this.ID);
						
						//...então o nó deve receber os coeficientes enviados pelo sink e...
						receiveCoefficients(wsnMessage);
						//...não deve mais encaminhar esta mensagem
					}
				} //if (wsnMessage.tipoMsg == 0)
				
				if (encaminhar && wsnMessage.tipoMsg == 1)
				{
					wsnMessage.forwardingHop = this; 
					broadcast(wsnMessage);
				}
				else if (encaminhar) //Nó sensor recebe uma mensagem de flooding (com wsnMessage) e deve responder ao sink com uma WsnMsgResponse... (continua em "...além de") 
				{
					WsnMsgResponse wsnMsgResp = new WsnMsgResponse(1, this, null, this, 0, 1, "");
					
					if (wsnMessage != null)
					{
						wsnMsgResp = new WsnMsgResponse(1, this, null, this, 0, wsnMessage.sizeTimeSlot, wsnMessage.dataSensedType); 
						prepararMensagem(wsnMsgResp, wsnMessage.sizeTimeSlot, wsnMessage.dataSensedType);
					}
					addThisNodeToPath(wsnMsgResp);
					
					WsnMessageResponseTimer timer = new WsnMessageResponseTimer(wsnMsgResp, proximoNoAteEstacaoBase);
					
					timer.startRelative(wsnMessage.sizeTimeSlot, this); // Espera por "wsnMessage.sizeTimeSlot" rounds e envia a mensagem para o nó sink (próximo nó no caminho do sink)
					
					//Devemos alterar o campo forwardingHop(da mensagem) para armazenar o noh que vai encaminhar a mensagem.
					wsnMessage.forwardingHop = this; 
					//...além de repassar a wsnMessage para os próximos nós
					broadcast(wsnMessage);
					
				} //if (encaminhar)
			} //if (message instanceof WsnMsg)
			else if (message instanceof WsnMsgResponse) //Mensagem de resposta dos nós sensores para o sink que deve ser repassada para o "proximoNoAteEstacaoBase"
			{
				WsnMsgResponse wsnMsgResp = (WsnMsgResponse) message;
				
//				this.setColor(Color.YELLOW);
				
				addThisNodeToPath(wsnMsgResp);
				
				WsnMessageResponseTimer timer = new WsnMessageResponseTimer(wsnMsgResp, proximoNoAteEstacaoBase);
				
				timer.startRelative(1, this); // Envia a mensagem para o próximo nó no caminho do sink no próximo round (1)
			} // else if (message instanceof WsnMsgResponse)
		} //while (inbox.hasNext())
	} //public void handleMessages
	
	private void addThisNodeToPath(WsnMsgResponse wsnMsgResp)
	{
		wsnMsgResp.pushToPath(this.ID);
	}
	
	/**
	 * Prepara a mensagem "wsnMsgResp" para ser enviada para o sink acrescentando os dados lidos pelo nó atual
	 * @param wsnMsgResp Mensagem a ser preparada para envio
	 * @param sizeTimeSlot Tamanho do slot de tempo (intervalo) a ser lido pelo nó sensor, ou tamanho da quantidade de dados a ser enviado para o sink
	 * @param dataSensedType Tipo de dado (temperatura, humidade, luminosidade, etc) a ser sensoreado (lido) pelo nó sensor
	 */
	private void prepararMensagem(WsnMsgResponse wsnMsgResp, Integer sizeTimeSlot, String dataSensedType)
	{
		
		int medida = 0;
		if (dataSensedType != null)
		{
			medida = identificarTipo(dataSensedType);
		}
		String dataLine = performSensorReading();
		int cont = 0, i=0;
		while (i<sizeTimeSlot && dataLine != null)
		{
			i++;
			if (dataLine != null && dataSensedType != null && medida != 0)
			{
				String linhas[] = dataLine.split(" ");
				double value;
				double quantTime;
//				Utils.printForDebug("(ultimoRoundLido + sizeTimeSlot) = "+(ultimoRoundLido + sizeTimeSlot));
//				Utils.printForDebug("cont = "+cont);
				if (linhas.length > 4)
				{
					cont++;
					ultimoRoundLido = Integer.parseInt(linhas[2]); //Número do round 
					if (linhas[medida] == null || linhas[medida].equals(""))
					{
						value = 0.0;
					}
					else
					{
						try
						{
							value = Double.parseDouble(linhas[medida]);
						}//try
						catch (NumberFormatException e)
						{
							value = 0.0;
						}//catch
					}//else
					quantTime = parseCalendarHoras(linhas[0], linhas[1]);
					
					lastValueRead = value;
					lastTimeRead = quantTime;
					addDataRecordItens(dataSensedType.charAt(0), value, quantTime);

					wsnMsgResp.addDataRecordItens(dataSensedType.charAt(0), value, quantTime);
					
				}//if (linhas.length > 4)
			}//if (dataLine != null && dataSensedType != null && medida != 0)
			if (i<sizeTimeSlot) //Impede que seja perdida uma leitura do sensor
			{
				dataLine = performSensorReading();
			}//if (i<sizeTimeSlot)
		}//while (i<sizeTimeSlot && dataLine != null)
	}
	
	/**
	 * Verifies whether the sensor ID passed as parameter is equal to the ID of this node.
	 * @param sensorID Sensor ID to be compared to this node's ID
	 * @return Returns <code>true</code> if the IDs are the same. Returns <code>false</code> otherwise.
	 */
	private boolean isMyID(String sensorID) {
		if(!sensorID.equals("")){
			int intSensorID = Integer.parseInt(sensorID);
			if (this.ID == intSensorID && intSensorID <= FileHandler.NUMBER_OF_SENSOR_NODES){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * <p>Simulates a physical sensor data reading performed for all the sensing devices available in this
	 * node (e.g. temperature, pressure, humidity).</p>
	 * <p>In fact, a real sensor data reading is not done by this node. Instead, a sensor reading is
	 * collected from its <code>sensorReadingsQueue</code> attribute. </p>
	 * <p>In the case that the list is empty, it is filled with sensor readings loaded from the 
	 * sensor readings file.</p>
	 */
	public String performSensorReading()
	{
		if (sensorReadingsQueue != null && sensorReadingsQueue.isEmpty())
		{
			loadSensorReadings();
		}
		if (sensorReadingsQueue != null && !sensorReadingsQueue.isEmpty())
		{
			String data = sensorReadingsQueue.remove();
			return data;
		}
		return null;
	}

	/**
	 * Loads the sensor readings to the <code>sensorReadingsQueue</code>
	 * If the attribute <code>loadSensorReadingsFromFile</code> is true, the node must load the sensor readings direct from the sensor readings file.
	 * Otherwise, it must load the sensor readings from the memory, that is, from the list of the sensor readings from
	 * all nodes that is loaded in memory beforehand by the {@link FileHandler} class.
	 * This procedure is necessary because the sensor readings file is very large and may take
	 * too long to be loaded depending on the computer configuration. For the cases that loading
	 * all the sensor readings to the memory (on the <code>FileHandler</code>) is not possible,
	 * the sensor readings are loaded from the file on demand by each node.
	 */
	private void loadSensorReadings() {
		if (loadSensorReadingsFromFile) {
			loadSensorReadingsFromFile();
		} else {
			loadSensorReadingsFromMemory();
		}
	}
	
	/**
	 * Fills the <code>sensorReadingsQueue</code> with sensor readings from the file.
	 * The amount of readings (file lines) to be loaded is informed in the <code>Config.xml</code> file (<code>SensorReadingsLoadBlockSize</code>) tag.
	 */
	private void loadSensorReadingsFromFile(){
		try {
			long initTime = System.currentTimeMillis();
			int sensorReadingsLoadBlockSize = Configuration.getIntegerParameter("SensorReadingsLoadBlockSize"); //amout of readings to be loaded
			String sensorReadingsFilePath = Configuration.getStringParameter("ExternalFilesPath/SensorReadingsFilePath");
			BufferedReader bufferedReader = FileHandler.getBufferedReader(sensorReadingsFilePath);
			int lineCounter = 0;
			
			bufferedReader = putOnLinePosition(bufferedReader, lastLineLoadedFromSensorReadingsFile); //puts the bufferedReader in the last line read
			
			String line = bufferedReader.readLine();
			while(line != null) {
				String lineValues[] = line.split(" ");
				if (lineValues.length > 4 && this.isMyID(lineValues[3])) { //if the line corresponds to data from this node it enters, otherwise it passes to the next line
					for (int loadedLinesCount = 0; ((line != null) && (loadedLinesCount < sensorReadingsLoadBlockSize)); loadedLinesCount++) { //if the specified number of lines to be loaded was not yet satisfied or if there are no more lines, it enters
						lineValues = line.split(" ");
						if (lineValues.length > 4 && this.isMyID(lineValues[3])) {
							sensorReadingsQueue.add(line); //loads the line to the memory
							line = bufferedReader.readLine();
							lineCounter++;
						} else break;
					}
					break;
				}
				lineCounter++;
				line = bufferedReader.readLine();
			}			
			bufferedReader.close();
		if (sensorReadingsQueue.size() < sensorReadingsLoadBlockSize) {
			System.err.println("NodeID: " + this.ID + " has already read all the sensor readings of the file. " +
					"\n It has only " + sensorReadingsQueue.size() + " readings in its memory (sensorReadingsLoadedFromFile list)");
		}

		lastLineLoadedFromSensorReadingsFile = lastLineLoadedFromSensorReadingsFile + lineCounter; //updates the last line read from the file
		long finishTime = System.currentTimeMillis();
		Utils.printForDebug("Node ID " + this.ID + " successfully loaded " + sensorReadingsQueue.size() + " sensor readings from the file in " + Utils.getTimeIntervalMessage(initTime, finishTime));
		
		} catch (CorruptConfigurationEntryException e) {
			System.out.println("Problems while loading variable sensorReadingsFilePath at simpleNode.loadSensorReadingFromFile()");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Problems while reading lines (fileReader.readLine()) from  sensorReadingsFilePath at simpleNode.loadSensorReadingFromFile()");
			e.printStackTrace();
		}
	}
	/**
	 * Fills the <code>sensorReadingsQueue</code> list with sensor readings from the {@link FileHandler}.
	 */
	private void loadSensorReadingsFromMemory() {
		long initTime = System.currentTimeMillis();
		List<String> nodesSensorReadingsQueue = FileHandler.getNodesSensorReadingsQueue();
		for (String sensorReading : nodesSensorReadingsQueue) {
			String sensorReadingsValues[] = sensorReading.split(" ");
			if (sensorReadingsValues.length > 4 && this.isMyID(sensorReadingsValues[3])) {
//				nodesSensorReadingsQueue.remove(sensorReading);
				sensorReadingsQueue.add(sensorReading);
			}
		}
		long finishTime = System.currentTimeMillis();
		Utils.printForDebug("Node ID " + this.ID + " successfully loaded " + sensorReadingsQueue.size() + " sensor readings from the memory in " + Utils.getTimeIntervalMessage(initTime, finishTime));
		System.out.println("");
	}
	
	/**
	 * Places the bufferedReader on the line position specified.
	 * @param bufferedReader Buffered Reader to be used
	 * @param linePosition position in the file that the buffered reader must be
	 * @return the buffered reader in the line corresponding to the position specified. That is,
	 * when bufferedReader.readline() is called, the line returned will correspond position specified.
	 * For example, if linePosition = 3 then bufferedReader.readline() will get the forth line. 
	 */
	private BufferedReader putOnLinePosition(BufferedReader bufferedReader, Integer linePosition) {
		try {
			for (int lineCount = 0; (lineCount < linePosition); lineCount++) {
				String line = bufferedReader.readLine();
				if (line == null) {
					return bufferedReader;
				}
			}
		} catch (IOException e) {
			System.out.println("Problems while reading lines (fileReader.readLine()) from  sensorReadingsFilePath at simpleNode.putOnLastLineRead()");
			e.printStackTrace();
		}
			
		return bufferedReader;
	}

	/**
	 * Identifica o tipo de dados a ser lido (posição na linha) de acordo com a string passada, conforme o exemplo abaixo:
	 * 				2004-03-21 19:02:26.792489 65528 4 87.383 45.4402 5.52 2.31097
	 * Posição          [1]         [2]         [3] [4]  [5]    [6]    [7]   [8]
	 * Tipo de dado    Data         Hora       Round ID  Temp   Hum    Lum   Volt
	 * 
	 * Onde: Data e Hora são a data e o horário em que ocorreu a leitura dos valores sensoreados
	 *       Round é o número da rodada (execução) da leitura, que ocorre a cada 31 segundos
	 *       ID é o número identificador do sensor
	 *       Temp é a medida (grandeza) da temperatura aferida
	 *       Hum é a medida (grandeza) da humidade aferida
	 *       Lum é a medida (grandeza) da luminosidade aferida
	 *       Volt é a medida (grandeza) do nível de bateria do sensor (voltagem aferida)
	 * 
	 * @param tipo Pode ser t: temperatura, h: humidade, l: luminosidade ou v: voltagem
	 * @return Posição correspondente do tipo de dado a ser aferido na string lida do arquivo de dados (data.txt)
	 */
	private int identificarTipo(String tipo) 
	{
		if (tipo.equals("t"))
			return 4;
		else if (tipo.equals("h"))
			return 5;
		else if (tipo.equals("l"))
			return 6;
		else if (tipo.equals("v"))
			return 7;	
		return 0;
	}
	
	/**
	 * Transforma os valores de data (AnoMesDia) e hora (hora) passados em uma grandeza inteira com a quantidade de milisegundos total
	 * @param AnoMesDia String no formato AAAA-MM-DD representando a data da leitura do valor pelo sensor (A-Ano, M-Mes, D-Dia)
	 * @param hora String no formato HH:MM:SS.LLLLL representando a hora da leitura do valor pelo sensor (H-Hora, M-Minuto, S-Segundo, L-Milisegundo)
	 * @return Quantidade de milisegundos total representando aquele instante de tempo (Data + Hora) segundo o padrão do Java 
	 */
	private long parseCalendarHoras(String AnoMesDia, String hora)
	{
		String[] datas = AnoMesDia.split("-");
		String[] horas = hora.split(":");
		String certo = "";
		String millesegundos = "";
		for (String mille : horas){
			if(mille.contains(".")){
				String correto = mille.substring(0,mille.indexOf("."));
				millesegundos = mille.substring(mille.indexOf(".")+1, mille.length());
				certo = correto;
			}
		}
		horas[2] = certo;
		GregorianCalendar gc = new GregorianCalendar(Integer.parseInt(datas[0]), Integer.parseInt(datas[1]) -1, Integer.parseInt(datas[2]),Integer.parseInt(horas[0]),Integer.parseInt(horas[1]), Integer.parseInt(horas[2]));
		long quantTime = (gc.getTimeInMillis() + Long.parseLong(millesegundos)/1000);
		return quantTime;
	}
	
	/**
	 * Faz o cálculo da predição do valor sensoreado de acordo com os coeficientes (A e B) informados e o parâmetro de tempo
	 * @param A Coeficiente A (interceptor) da equação de regressão, dada por S(t) = A + B.t 
	 * @param B Coeficiente B (slope, inclinação) da equação de regressão, dada por S(t) = A + B.t
	 * @param tempo Parâmetro de tempo a ter o valor da grandeza predito 
	 * @return Valor predito para o parâmetro sensoreado no tempo dado
	 */
	private double makePrediction(double A, double B, double tempo)
	{
		double time;
		time = A + B*tempo;
		return time;
	}
	
	/**
	 * It starts a timer to send the message passed to the next node in path to destination node 
	 * @param wsnMessage Message to be sended to destination node
	 */
	protected void sendToNextNodeInPath(WsnMsg wsnMessage)
	{
		Integer nextNodeId = wsnMessage.popFromPath();
		WsnMessageTimer timer = null;
		Node nextNode = null;
		if (nextNodeId != null)
		{
			nextNode = Tools.getNodeByID(nextNodeId);
			timer = new WsnMessageTimer(wsnMessage, nextNode);
			timer.startRelative(1, this);
		}
	}
	
	/**
	 * Get the coefficients from the Regression Equation and the threshold error from the message passed by and trigger the predictions for this node 
	 * @param wsnMessage Message to have the coefficients read
	 */
	protected void receiveCoefficients(WsnMsg wsnMessage)
	{
		double coefA = wsnMessage.getCoefA();
		double coefB = wsnMessage.getCoefB();
		double maxError = wsnMessage.getThresholdError();
		triggerPredictions(wsnMessage.dataSensedType, coefA, coefB, maxError);
	}
	
	/**
	 * Read the next value from present sensor, make the prediction and, according with the predition (hit or miss), trigges the next action 
	 * @param dataSensedType Type of data to be read from sensor: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem
	 * @param coefA Coefficient A from the Regression Equation for this sensor
	 * @param coefB Coefficient B from the Regression Equation for this sensor
	 * @param maxError Threshold error to the calculation of prediction for this sensor
	 */
	protected void triggerPredictions (String dataSensedType, double coefA, double coefB, double maxError)
	{
		int medida = 0;
		if (dataSensedType != null)
		{
			medida = identificarTipo(dataSensedType);
		}
		String sensorReading = performSensorReading();
		if (sensorReading != null && medida != 0)
		{
			String linhas[] = sensorReading.split(" ");
			double value;
			double quantTime;
			if (linhas.length > 4)
			{
				if (linhas[medida] == null || linhas[medida].equals(""))
				{
					value = 0.0;
				}
				else
				{
					try
					{
						value = Double.parseDouble(linhas[medida]);
					}//try
					catch (NumberFormatException e)
					{
						value = 0.0;
					}//catch
				}//else
				quantTime = parseCalendarHoras(linhas[0], linhas[1]);
				double predictionValue = makePrediction(coefA, coefB, quantTime);
				
				addDataRecordItens(dataSensedType.charAt(0), value, quantTime);
				
				if (!isValuePredictInValueReading(value, predictionValue, maxError))
				{
					numPredictionErrors++; // Contador do número de erros de predição
				}
/*				
				if (isValuePredictInValueReading(value, predictionValue, maxError))
				{
*/
				if (numPredictionErrors < limitPredictionError) // Se o número máximo de erros de predição for menor que o limite máximo permitido
				{
					ultimoRoundLido = Integer.parseInt(linhas[2]);
//					lastValueRead = value;
//					lastTimeRead = quantTime;
					
					PredictionTimer newPredictionTimer = new PredictionTimer(dataSensedType, coefA, coefB, maxError);
					newPredictionTimer.startRelative(1, this);
/*					
					Utils.printForDebug(" @ @ O valor predito ESTA dentro da margem de erro do valor lido! NoID = "+this.ID);
					Utils.printForDebug("Round = "+ultimoRoundLido+": Vpredito = "+predictionValue+", Vlido = "+value+", Limiar = "+maxError);
*/
				} // end if (numPredictionErrors < limitPredictionError)
				else
				{
					WsnMsgResponse wsnMsgResp = new WsnMsgResponse(1, this, null, this, 1, 2, dataSensedType);
/*					
					//Adiciona os últimos valores lidos anteriormente a mensagem que vai para o sink
					wsnMsgResp.addDataRecordItens(dataSensedType.charAt(0), lastValueRead, lastTimeRead);
					//Adiciona os valores lidos (NOVIDADE) na mensagem que vai para o sink
					ultimoRoundLido = Integer.parseInt(linhas[2]);
					lastValueRead = value;
					lastTimeRead = quantTime;
					wsnMsgResp.addDataRecordItens(dataSensedType.charAt(0), lastValueRead, lastTimeRead);
*/					
					//Adds the last values ​​previously read to the message that goes to the sink
					for (int cont=0; cont<dataRecordItens.size(); cont++) //for(int cont=0; cont<slidingWindowSize; cont++)
					{
						wsnMsgResp.addDataRecordItens(dataRecordItens.get(cont).type, dataRecordItens.get(cont).value, dataRecordItens.get(cont).time); 
					}
					
					addThisNodeToPath(wsnMsgResp);
					
					WsnMessageResponseTimer timer = new WsnMessageResponseTimer(wsnMsgResp, proximoNoAteEstacaoBase);
					
					timer.startRelative(1, this); // Espera por "wsnMessage.sizeTimeSlot" rounds e envia a mensagem para o nó sink (próximo nó no caminho do sink)
					
					Utils.printForDebug("\n\n * * * * O valor predito NAO esta dentro da margem de erro do valor lido! NoID = "+this.ID);
					Utils.printForDebug("\nRound = "+ultimoRoundLido+": Vpredito = "+predictionValue+", Vlido = "+value+", Limiar = "+maxError);
					
					numPredictionErrors = 0;
				} // end else if (numPredictionErrors < limitPredictionError)
				
			} // end if (linhas.length > 4)
			
		} // end if (sensorReading != null && medida != 0)
	}
	
	/**
	 * Calls the method triggerPredictions
	 * @param dataSensedType Type of data to be read from sensor: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem
	 * @param coefA Coefficient A from the Regression Equation for this sensor
	 * @param coefB Coefficient B from the Regression Equation for this sensor
	 * @param maxError Threshold error to the calculation of prediction for this sensor
	 */
	public final void triggerPrediction(String dataSensedType, double coefA, double coefB, double maxError)
	{
		triggerPredictions(dataSensedType, coefA, coefB, maxError);
	}
	
	/**
	 * Compares the read value('value') to the predict value('predictionValue') using 'maxError' as threshold error
	 * @param value Value read from the sensor
	 * @param predictionValue Value predict to be compared
	 * @param maxError Threshold error to the calculation of prediction for this sensor
	 * @return True if the sensed (read) value is in the predicted value (more or less the threshold error) ou False, otherwise
	 */
	protected boolean isValuePredictInValueReading(double value, double predictionValue, double maxError)
	{
		Global.predictionsCount++;
		Global.squaredError += Math.pow((predictionValue - value), 2);
		boolean hit;
		if (value >= (predictionValue - value*maxError) && value <= (predictionValue + value*maxError))
		{
			Global.numberOfHitsInThisRound++;
			hit = true;
		}
		else
		{
			Global.numberOfMissesInThisRound++;
			hit = false;
		}
		return hit;
	}
	
	public class DataRecord
	{
		char type;
		double value;
		double time;
	}
	
	public Vector<DataRecord> dataRecordItens;
	
	public void addDataRecordItens(char typ, double val, double tim)
	{
		if (this.dataRecordItens == null)
		{
			this.dataRecordItens = new Vector<DataRecord>();
		}
		DataRecord dr = new DataRecord();
		
		dr.type = typ;
		dr.value = val;
		dr.time = tim;
		
		dataRecordItens.add(dr);
		//Implements a FIFO structure with the vector 'dataRecordItens' with at most 'slidingWindowSize' elements
		while (dataRecordItens.size() > slidingWindowSize)
		{
			dataRecordItens.removeElementAt(0);
		}
	}
}
