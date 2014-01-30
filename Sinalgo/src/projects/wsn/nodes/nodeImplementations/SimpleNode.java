package projects.wsn.nodes.nodeImplementations;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import projects.wsn.nodes.messages.WsnMsg;
import projects.wsn.nodes.messages.WsnMsgResponse;
import projects.wsn.nodes.timers.PredictionTimer;
import projects.wsn.nodes.timers.ReadingSendingTimer;
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
 * Classe que representa um nó de sensor ordinário que está habilitado a sentir fenomenos naturais
 * (ex. temperatura, pressão, umidade) e manda para os sink nodes.
 * Em nossa simulação, esse nó também é capaz de fazer predição de dados in-networking usando os modelos de regressão.<p>
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
	 * Indica o tamanho da janela deslizante das leituras do sensor que serão enviadas ao sink node quando houver uma "novidade"<p>
	 * [Eng] Indicates the size of Sliding Window from sensor readings to be send to sink node when there is a "novelty".
	 */
	protected static int slidingWindowSize = 7;
	
	/**
	 * Armazenar o nó que será usado para alcançar a Estação-Base<p>
	 * [Eng] Stores the node that will be used to get the Base-Station
	 */
	protected Node proximoNoAteEstacaoBase;
	
	/**
	 * Armazena o número de sequencia da última mensagem recebida<p>
	 * [Eng] Stores the sequence number from the last received message
	 */
	protected Integer sequenceNumber = 0;
	
	/**
	 * Valor do último round em que houve leitura do sensor (que teve valor lido do arquivo) <p>
	 * [Eng] Value of the last round that had reading from the sensor (that had value read from the file)
	 */
	protected int ultimoRoundLido;
	
	/**
	 * Valor (grandeza/magnitude) da última leitura do sensor <p>
	 * [Eng] Value (size) of the last read of the sensor
	 */
	protected double lastValueRead;
	
	/**
	 * Tempo (data/hora em milisegundos) da última leitura do sensor <p>
	 * [Eng] Time (date/hour in miliseconds) of the last read of the sensor
	 */
	protected double lastTimeRead;
	
	/**
	 * Armazena as leituras do sensor desse nó carregadas do arquivo de leituras do sensor.
	 * Uma lista referenciada está sendo usada aqui porque como as leituras
	 * (lidas dessa lista) estão sendo feitas por esse nó sensor elas são descartadas.<p>
	 * [Eng] Stores sensor readings of this node loaded from the sensor readings file.
	 * A linked list is being used here because as the readings are being performed 
	 * (being read from this list) by this sensor node they are discarded.
	 */
	private LinkedList<String> sensorReadingsQueue = new LinkedList<String>();
	
	/**
	 * Armazena o valor referente a ultima linha carregada do arquivo de leituras do sensor, para que
	 * quando o <code>sensorReadingQueue</code> estiver vazio um novo carregamente terá que ser executado
	 * com o fim de preencher a lista <code>SensorReadingsLoadedFromFile</code>, o carregamento começa da
	 * ultima linha lida do arquivo.<p>
	 * [Eng] Stores the value referring to the last line loaded from the sensor readings file in order that
	 * when the <code>sensorReadingsQueue</code> is empty and a new load from the file has to be
	 * executed to fill the <code>SensorReadingsLoadedFromFile</code> list, the loading starts from the last
	 * line read from the file.
	 */
	private int lastLineLoadedFromSensorReadingsFile;

	/**
	 * Indica se as leituras do sensor deverão ser carregadas do arquivo ou da memória.
	 * Se esse atributo for "true" (verdadeiro), o nó deverá carregar as leituras diretamente do arquivo de leituras.
	 * Se não, deverá carregar as leituras da memória, isto é, da lista de leituras do sensor de todos os nós que
	 * estavam carregadas na memoria previamente pela classe {@link FileHandler}. Esse procedimento é necessário
	 * porque o arquivo de leituras do sensor é muito grande e pode demorar muito mais para serem carregador dependendo
	 * da configuração do computador. Para os casos que não for possível carregar todas as leituras do sensor para
	 * a memoria (no <code>FileHandler</code>), as leituras do sensor serão carregadas apartir do arquivo na demanda 
	 * por cada nó.<p>
	 * [Eng] Indicates whether the sensor readings must be loaded from the file or from the memory.
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
	 * Variavel local para armazenar a soma do "square error" (SE) usada para calcular o RMSE (Root mean square error - "media de erro") para cada sensor<p>
	 * [Eng] Local variable to store the sum of square error(SE) used to calculate the RMSE (Root mean square error) from each sensor
	 */
	public double squaredError = 0;
	
	/**
	 * Contador local do numero de predições<p>
	 * [Eng] Local counter of the number of predictions
	 */
	public int predictionsCount = 0;
	
	/**
	 * Numero de erros de predições do nó do sensor nesse slot de tempo<p>
	 * [Eng] Number of prediction errors of the sensor node in this timeslot
	 */
	protected int numPredictionErrors = 0;
	
	/**
	 * Máxino (limite) de numero de erros de predições de qualquer nó de sensor - Isso também pode ser expressado em porcentagem (Ex: double - real) do total do slot de tempo<p>
	 * [Eng] Maximum (limit) Number of prediction errors of any sensor node - It also could be expressed in percentage (i.e., double) from total timeSlot
	 */
	protected static final int limitPredictionError = 5; // delay (abbrev.)
	
	protected boolean newCoefsReceived = false; // Indicates whether the current sensor node already received the first (new) coefficients from the sink node
	
	protected int currentCoefsVersion = 0;
	
	static boolean newContinuousSensingModeEnable = true; // Enables or disables continuous sensing sensors in Adaga-P* (making the data sensing number equals to the Naive approach)

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
		while (inbox.hasNext()) {
			Message message = inbox.next();
			if (message instanceof WsnMsg) {
					Boolean encaminhar = Boolean.TRUE;
					WsnMsg wsnMessage = (WsnMsg) message;
					
					if (wsnMessage.forwardingHop.equals(this)) { //A mensagem voltou, o nó vai descartá-la
						encaminhar = Boolean.FALSE;
					}
					else if (wsnMessage.typeMsg != 1) {// Mensagem que vai do sink para os nós sensores e é um flood. Devemos atualizar a rota
						this.setColor(Color.BLUE);
						if (proximoNoAteEstacaoBase == null) {

							proximoNoAteEstacaoBase = inbox.getSender();
							sequenceNumber = wsnMessage.sequenceID;

						} else if (sequenceNumber < wsnMessage.sequenceID) {
							//Recurso simples para evitar loop.
							//Exemplo: Nó A transmite em brodcast. Nó B recebe a msg e retransmite em broadcast.
							//Consequentemente, nó A irá receber a msg. Sem esse condicional, nó A iria retransmitir novamente, gerando um loop.
							
							sequenceNumber = wsnMessage.sequenceID;
						} else {
							encaminhar = Boolean.FALSE;
						}
					}

					else if (wsnMessage.typeMsg == 1) {// Mensagem que vai do sink para os nós sensores e é um pacote transmissor de dados (coeficientes). Devemos atualizar a rota
							
							encaminhar = Boolean.FALSE;

							//Definir roteamento de mensagem
							if (wsnMessage.target != this) {
								
								sendToNextNodeInPath(wsnMessage);

							} else if (wsnMessage.target == this)  {//Se este for o nó de destino da mensagem...
								this.setColor(Color.RED);
								//...então o nó deve receber os coeficientes enviados pelo sink e...
								if (newContinuousSensingModeEnable) {
									newCoefsReceived = true; // Makes the sensors stop sensing without coefficients
								}
								receiveCoefficients(wsnMessage);
								//...não deve mais encaminhar esta mensagem
							}
					}

					if (encaminhar && wsnMessage.typeMsg == 1) {
						wsnMessage.forwardingHop = this;
						broadcast(wsnMessage);
					}
					else if (encaminhar) {//Nó sensor recebe uma mensagem de flooding (com wsnMessage) e deve responder ao sink com uma WsnMsgResponse... (continua em "...além de") 
						WsnMsgResponse wsnMsgResp = new WsnMsgResponse(1, this, null, this, 0, 1, "");

						if (wsnMessage.typeMsg == 0 && wsnMessage != null) {
							wsnMsgResp = new WsnMsgResponse(1, this, null, this, 0, wsnMessage.sizeTimeSlot, wsnMessage.dataSensedType); 
							prepararMensagem(wsnMsgResp, wsnMessage.sizeTimeSlot, wsnMessage.dataSensedType);
						}

//						addThisNodeToPath(wsnMsgResp);
//						WsnMessageResponseTimer timer = new WsnMessageResponseTimer(wsnMsgResp, proximoNoAteEstacaoBase);
//						timer.startRelative(wsnMessage.sizeTimeSlot, this); // Espera por "wsnMessage.sizeTimeSlot" rounds e envia a mensagem para o nó sink (próximo nó no caminho do sink)

						if (newContinuousSensingModeEnable) {
							// TODO: Altera o método Adaga-P* (wsnMessage.typeMsg == 0) para que os sensores continuem sensoriando após terminarem de ler os dados iniciais até receberem os coeficientes calculados e enviados pelo sink
							// Change the Adaga-P* approach (wsnMessage.typeMsg == 0) to the sensors nodes continue sensing after reading initial data until receiving the coefficients calculated and sended by sink
							if (wsnMessage.typeMsg == 0 && wsnMessage != null) {
								addThisNodeToPath(wsnMsgResp);
								WsnMessageResponseTimer timer = new WsnMessageResponseTimer(wsnMsgResp, proximoNoAteEstacaoBase);
								timer.startRelative(wsnMessage.sizeTimeSlot, this); // Espera por "wsnMessage.sizeTimeSlot" rounds e envia a mensagem para o nó sink (próximo nó no caminho do sink)
	
								ReadingSendingTimer newReadingSendingTimer = new ReadingSendingTimer(wsnMessage.dataSensedType);
								newReadingSendingTimer.startRelative(wsnMessage.sizeTimeSlot, this);
							}
						}
						else {
							addThisNodeToPath(wsnMsgResp);
							WsnMessageResponseTimer timer = new WsnMessageResponseTimer(wsnMsgResp, proximoNoAteEstacaoBase);
							timer.startRelative(wsnMessage.sizeTimeSlot, this); // Espera por "wsnMessage.sizeTimeSlot" rounds e envia a mensagem para o nó sink (próximo nó no caminho do sink)
						}
							

						if (wsnMessage.typeMsg == 2 && wsnMessage != null){ // approachType = 2 = Naive
							makeSensorReadingAndSendind(wsnMessage.dataSensedType); // Chama o método de sensoriamento / envio de dados da abordagem naive
						}
						else if (wsnMessage == null){
							Utils.printForDebug("wsnMessage é NULL!");
						}

						wsnMessage.forwardingHop = this; //Devemos alterar o campo forwardingHop(da mensagem) para armazenar o noh que vai encaminhar a mensagem.
						broadcast(wsnMessage);//...além de repassar a wsnMessage para os próximos nós
					}
			}//if (message instanceof WsnMsg)

			else if (message instanceof WsnMsgResponse ) {
				WsnMsgResponse wsnMsgResp = (WsnMsgResponse) message;
				addThisNodeToPath(wsnMsgResp);
				WsnMessageResponseTimer timer = new WsnMessageResponseTimer(wsnMsgResp, proximoNoAteEstacaoBase);
				timer.startRelative(1, this); // Envia a mensagem para o próximo nó no caminho do sink no próximo round (1)
			}//else if (message instanceof WsnMsgResponse )
		}//while (inbox.hasnext())
	}//public void handleMessages
	
	private void addThisNodeToPath(WsnMsgResponse wsnMsgResp)
	{
		wsnMsgResp.pushToPath(this.ID);
	}
	
	/**
	 * Prepara a mensagem "wsnMsgResp" para ser enviada para o sink acrescentando os dados lidos pelo nó atual<p>
	 * [Eng] Prepare the message "wsnMsgResp" to be sensed to the sink plus the data readed by the current node
	 * @param wsnMsgResp Mensagem a ser preparada para envio <p> [Eng] <b>wsnMsgResp</b> Message to be prepared to send
	 * @param sizeTimeSlot Tamanho do slot de tempo (intervalo) a ser lido pelo nó sensor, ou tamanho da quantidade de dados a ser enviado para o sink <p> [Eng] <b>sizeTimeSlot</b> Size of the time slot to be read by the sensor node, or the quantity size of data to be sended to the sink
	 * @param dataSensedType Tipo de dado (temperatura, humidade, luminosidade, etc) a ser sensoriado (lido) pelo nó sensor <p> [Eng] <b>dateSensedType</b> Type of data (temperature, humidity, luminosity, etc) to be shown
	 */
	private void prepararMensagem(WsnMsgResponse wsnMsgResp, Integer sizeTimeSlot, String dataSensedType)
	{
		
		int medida = 0;
		if (dataSensedType != null)
		{
			medida = identifyType(dataSensedType);
		}
		String dataLine = performSensorReading();
		int i=0;//cont = 0;
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
//					cont++;
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
					quantTime = parseCalendarHours(linhas[0], linhas[1]);
					
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
	 * Verifica se o ID do sensor passado como paramêtro é igual ao ID desse nó.<p>
	 * [Eng] Verifies whether the sensor ID passed as parameter is equal to the ID of this node.
	 * @param sensorID ID do sensor a ser comparado com o ID desse nó <p> [Eng] <b>Sensor ID</b> to be compared to this node's ID
	 * @return Retorna <code>true</code> se os IDs forem os mesmos, se não, retorna <code>false</code> <p> [Eng] Returns <code>true</code> if the IDs are the same. Returns <code>false</code> otherwise.
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
	 * <p>Simula uma leitura física de dadps dp sensor, feita para todos os dispositivos sensiveis disponiveis nesse nó (ex: temperatura, pressão, humidade).</p>
	 * <p>Em fato, uma leitura real do sensor não é feita por esse nó. Em vez disso, uma leitura do sensor é coletada do seu atributo <code>sensorReadingQueue</code></p>
	 * <p>No caso de a lista estar vazia, ela é preenchida com leituras do sensor carregadas do arquivo de leituras do sensor.</p><p>
	 * [Eng]
	 * <p>Simulates a physical sensor data reading performed for all the sensing devices available in this
	 * node (e.g. temperature, pressure, humidity).</p>
	 * <p>In fact, a real sensor data reading is not done by this node. Instead, a sensor reading is
	 * collected from its <code>sensorReadingsQueue</code> attribute. </p>
	 * <p>In the case that the list is empty, it is filled with sensor readings loaded from the 
	 * sensor readings file.</p>
	 */
	public String performSensorReading()
	{
		Global.sensorReadingsCount++;
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
	 * Carrega as leituras do sensor para o <code>sensorReadingsQueue</code>
	 * Se o atributo <code>loadSensorReadingsFromFile</code> for verdadeiro, o nó deverá carrecgar as leituras do sensor diretamente do arquivo de leituras do sensor.
	 * Caso contrário, ele deverá carregar as leituras do sensor da memória, ou seja, da lista de leituras do sensor de todos os nós que foram previamente carregados na memória pela classe {@link FileHandler}.
	 * Esse procedimento é necessário porque o arquivo de leituras do sensor é muito grande e pode demorar muito para ser carregado dependendo da configuração do computador. Para os casos que não for possivel carregar
	 * todas as leituras do sensor para a memória (no <code>FileHandler</code>), as leituras do sensor serão carregadas do arquivo por demanda de cada nó.<p>
	 * [Eng] Loads the sensor readings to the <code>sensorReadingsQueue</code>
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
	 * Preenche o <code>sensorReadingQueue<code> com as leituras do sensor apartir do arquivo.
	 * A quantidade de leituras (linhas do arquivo) a serem carregadas são informadas no arquivo<code>Config.xml</code> na tag (<code>SensorReadingsBlockSize</code>).<p>
	 * [Eng] Fills the <code>sensorReadingsQueue</code> with sensor readings from the file.
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
	 * Coloca o bufferedReader na posição especifica da linha.<p>
	 * [Eng] Places the bufferedReader on the line position specified.
	 * @param bufferedReader leitor de buffer a ser usado <p> [Eng] <b>bufferedReader</b> Buffered Reader to be used
	 * @param linePosition posição que o leitor de buffer deve estar no arquivo <p> [Eng] <b>linePosition</b> position in the file that the buffered reader must be
	 * @return Retorna o leitor de buffer na linha correspondente a posição especifica. Isto é, quando o bufferedReader.readline() é chamado, a linha retornada vai corresponder a posição especifica. por exemplo, se a linePosition = 3 então o bufferedReader.readline() vai pegar a quarta linha. <p> [Eng] the buffered reader in the line corresponding to the position specified. That is, when bufferedReader.readline() is called, the line returned will correspond position specified. For example, if linePosition = 3 then bufferedReader.readline() will get the forth line. 
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
	 *       Volt é a medida (grandeza) do nível de bateria do sensor (voltagem aferida)<p>
	 *       
	 * [Eng] Identify the data type to be read (line position) according to the string passed, as the example bellow:
	 * 				2004-03-21 19:02:26.792489 65528 4 87.383 45.4402 5.52 2.31097
	 * Position          [1]         [2]         [3] [4]  [5]    [6]    [7]   [8]
	 * Data type        Data         Hora       Roundp ID  Temp   Hum    Lum   Volt
	 * 
	 * Where: Data and Hora are the date and time that the sensed values reading were done
	 *        Round is the number of the round (execution) of the reading, that occurs each 31 seconds
	 *        ID is the identifier number of the sensor
	 *        Temp is the dimension of the measured temperature
	 *        Hum is the dimension of the measured humidity
	 *        Lum is the dimension of the measured luminositiy
	 *        Volt is the dimension of the battery level of the sensor (measured voltage)
	 * 
	 * @param type Pode ser t: temperatura, h: humidade, l: luminosidade ou v: voltagem <p> [Eng] <b>type</b> Can be t: temperature, h: humidity, l:luminosity or v: voltage.
	 * @return Posição correspondente do tipo de dado a ser aferido na string lida do arquivo de dados (data.txt) <p> [Eng] Position corresponding to the data type to be measured on the read String of the data archive (data.txt)
	 */
	private int identifyType(String type) 
	{
		switch (type) {
		case "t" : return 4;
		case "h" : return 5;
		case "l" : return 6;
		case "v" : return 7;
		default : return 0;
		}
	}
	
	/**
	 * Transforma os valores de data (AnoMesDia) e hora (hora) passados em uma grandeza inteira com a quantidade de milisegundos total<p>
	 * [Eng] Turn the data values (AnoMesDia) and hour (hora) passed in long number with the total quantity of miliseconds
	 * @param yearMonthDay String no formato AAAA-MM-DD representando a data da leitura do valor pelo sensor (A-Ano, M-Mes, D-Dia) <p> [Eng] <b>yearMonthDay</b> String in the format YY-MM-DD representing the date of the reading value by the sensor (Y - Year, M - Month, D - Day)
	 * @param hour String no formato HH:MM:SS.LLLLL representando a hora da leitura do valor pelo sensor (H-Hora, M-Minuto, S-Segundo, L-Milisegundo) <p> [Eng] <b>hour</b> String in the format HH:MM:SS.LLLLL representing the time of the reading value by the sensor (H - Hour, M - Minute, S-Second, L- Milisecond)
	 * @return Quantidade de milisegundos total representando aquele instante de tempo (Data + Hora) segundo o padrão do Java <p> [Eng] Quantity of total miliseconds represented in that instant of time  (Date + hour) by the java default.
	 */
	private long parseCalendarHours(String yearMonthDay, String hour)
	{
		String[] datas = yearMonthDay.split("-");
		String[] horas = hour.split(":");
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
	 * Faz o cálculo da predição do valor sensoreado de acordo com os coeficientes (A e B) informados e o parâmetro de tempo<p>
	 * [Eng] Calculate the prediction of the sensed value according with the other coefficients (A and B) informed and the param of time
	 * @param A Coeficiente A (interceptor) da equação de regressão, dada por S(t) = A + B.t <p> [Eng] <b>A</b> Coefficient A (interceptor) of the regression equation, given by S(t) = A + B.t
	 * @param B Coeficiente B (slope, inclinação) da equação de regressão, dada por S(t) = A + B.t <p> [Eng] <b>B</b> Coefficient B (slope, initiantion) of the regression equation, given by S(t) = A + B.t
	 * @param timeParam Parâmetro de tempo a ter o valor da grandeza predito <p> [Eng] <b>timeParam</b> Time param has the value of the predicted hugeness 
	 * @return Valor predito para o parâmetro sensoreado no tempo dado <p> [Eng] Predicted value to the sensed param in the given time
	 */
	private double makePrediction(double A, double B, double timeParam)
	{
		double time;
		time = A + B*timeParam;
		return time;
	}
	
	/**
	 * Começa um timer para mandsr as messagens passadas para o próximo nó no caminho para o nó de destino <p>
	 * [Eng] It starts a timer to send the message passed to the next node in path to destination node 
	 * @param wsnMessage Mensagem a ser mandada para o nó de destino <p> [Eng] <b>wsnMessage</b> Message to be sended to destination node
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
	 * Pega os coeficientes da equação de regressão e a limiar de erro da mensagem passara por um gatilho de predições desse nó <p>
	 * [Eng] Get the coefficients from the Regression Equation and the threshold error from the message passed by and trigger the predictions for this node 
	 * @param wsnMessage Mensagem que conterá os coeficientes lidos <p> [Eng] <b>wsnMessage</b> Message to have the coefficients read
	 */
	protected void receiveCoefficients(WsnMsg wsnMessage)
	{
		double coefA = wsnMessage.getCoefA();
		double coefB = wsnMessage.getCoefB();
		double maxError = wsnMessage.getThresholdError();
		currentCoefsVersion++;
		triggerPredictions(wsnMessage.dataSensedType, coefA, coefB, maxError, currentCoefsVersion);
	}
	
	/**
	 * Lê o próximo valor do sensor atual, faz a predição e, de acordo com a predição (certa ou errada), engatilha a próxima ação <p>
	 * [Eng] Read the next value from present sensor, make the prediction and, according with the predition (hit or miss), trigges the next action 
	 * @param dataSensedType Tipo de dado a ser lido pelo sensor: "t"=temperatura, "h"=humidade, "l"=luminosidade or "v"=voltagem <p> [Eng] <b>dataSensedType</b> Type of data to be read from sensor: "t"=temperature, "h"=humity, "l"=luminosity or "v"=voltage
	 * @param coefA Coeficiente A da equação de regressão para esse sensor <p> [Eng] <b>coefA</b> Coefficient A from the Regression Equation for this sensor
	 * @param coefB Coeficiente B da equação de regressão para esse sensor <p> [eng] <b>coefB</b> Coefficient B from the Regression Equation for this sensor
	 * @param maxError Limiar de erro para o calculo da predição desse sensor <p> [Eng] <b>maxError</b> Threshold error to the calculation of prediction for this sensor
	 */
	protected void triggerPredictions(String dataSensedType, double coefA, double coefB, double maxError, int coefsVersion)
	{
		int medida = 0;
		if (dataSensedType != null)
		{
			medida = identifyType(dataSensedType);
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
				quantTime = parseCalendarHours(linhas[0], linhas[1]);
				
				
				
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
				if (numPredictionErrors <= limitPredictionError) // Se o número máximo de erros de predição for menor que o limite máximo permitido
				{
					ultimoRoundLido = Integer.parseInt(linhas[2]);
//					lastValueRead = value;
//					lastTimeRead = quantTime;
					
					PredictionTimer newPredictionTimer = new PredictionTimer(dataSensedType, coefA, coefB, maxError, coefsVersion);
					newPredictionTimer.startRelative(1, this);
/*					
					Utils.printForDebug(" @ @ O valor predito ESTA dentro da margem de erro do valor lido! NoID = "+this.ID);
					Utils.printForDebug("Round = "+ultimoRoundLido+": Vpredito = "+predictionValue+", Vlido = "+value+", Limiar = "+maxError);
*/
				} // end if (numPredictionErrors < limitPredictionError)
				else
				{
					WsnMsgResponse wsnMsgResp = new WsnMsgResponse(1, this, null, this, 0, 2, dataSensedType);
//					WsnMsgResponse wsnMsgResp = new WsnMsgResponse(1, this, null, this, 1, 2, dataSensedType);

					//Adds the last values ​​previously read to the message that goes to the sink
					for (int cont=0; cont<dataRecordItens.size(); cont++) //for(int cont=0; cont<slidingWindowSize; cont++)
					{
						wsnMsgResp.addDataRecordItens(dataRecordItens.get(cont).type, dataRecordItens.get(cont).value, dataRecordItens.get(cont).time); 
					}
					
					addThisNodeToPath(wsnMsgResp);
					
					WsnMessageResponseTimer timer = new WsnMessageResponseTimer(wsnMsgResp, proximoNoAteEstacaoBase);
					
					timer.startRelative(1, this); // Espera por "wsnMessage.sizeTimeSlot" rounds e envia a mensagem para o nó sink (próximo nó no caminho do sink)

					ultimoRoundLido = Integer.parseInt(linhas[2]);

					Utils.printForDebug("\n\n * * * * O valor predito NAO esta dentro da margem de erro do valor lido! NoID = "+this.ID);
					Utils.printForDebug("\nRound = "+ultimoRoundLido+": Vpredito = "+predictionValue+", Vlido = "+value+", Limiar = "+maxError);
					
					numPredictionErrors = 0;
					
					PredictionTimer newPredictionTimer = new PredictionTimer(dataSensedType, coefA, coefB, maxError, coefsVersion);
					newPredictionTimer.startRelative(1, this);
				} // end else if (numPredictionErrors < limitPredictionError)
				
			} // end if (linhas.length > 4)
			
		} // end if (sensorReading != null && medida != 0)
	}
	
	/**
	 * Chama o método triggerPredictions<p>
	 * [Eng] Calls the method triggerPredictions
	 * @param dataSensedType Tipo de dado a serem lidos pelo sensor: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem <p> [Eng] <b>dataSnsedType</b> Type of data to be read from sensor: "t"=temperature, "h"=humidity, "l"=luminosity or "v"=voltage
	 * @param coefA Coeficiente A da equação de regressão para esse sensor <p> [Eng] <b>coefA</b> Coefficient A from the Regression Equation for this sensor
	 * @param coefB Coeficiente B da equação de regressão para esse sensor <p> [Eng] <b>coefB</b> Coefficient B from the Regression Equation for this sensor
	 * @param maxError Limiar de erro para o calculo da predição desse sensor <p> [Eng] <b>maxError</b> Threshold error to the calculation of prediction for this sensor
	 */
	public final void triggerPrediction(String dataSensedType, double coefA, double coefB, double maxError, int version)
	{
		// TODO:
		if (version >= currentCoefsVersion) {
			triggerPredictions(dataSensedType, coefA, coefB, maxError, version);
		}
	}
	
	/**
	 * Compares the read value('value') to the predict value('predictionValue') using 'maxError' as threshold error
	 * @param value Valor lido apartir do sensor <p> [Eng] <b>value</b> Value read from the sensor
	 * @param predictionValue Valor predito a ser comparado <p> [Eng] <b>predictionValue</b> Value predict to be compared
	 * @param maxError Limiar de erro para o calculo da predição desse sensor <p> [Eng] <b>maxError</b> Threshold error to the calculation of prediction for this sensor
	 * @return True se o valor sensorado (lido) está no valor predito (mais ou menos o limiar de erro), senão False <p> [Eng] True if the sensed (read) value is in the predicted value (more or less the threshold error) ou False, otherwise
	 */
	protected boolean isValuePredictInValueReading(double value, double predictionValue, double maxError)
	{
/*
		Global.predictionsCount++;
		Global.squaredError += Math.pow((predictionValue - value), 2);

		this.predictionsCount++;
		this.squaredError += Math.pow((predictionValue - value), 2);
*/
/*
		double RMSE = Math.sqrt(this.squaredError / this.predictionsCount);
		System.out.println("SensorID\t"+this.ID+"\tRound\t"+NumberFormat.getIntegerInstance().format(Global.currentTime)+"\t"+NumberFormat.getNumberInstance().format(RMSE));
*/
		boolean hit;
		if (value >= (predictionValue - value*maxError) && value <= (predictionValue + value*maxError))
		{
			Global.numberOfHitsInThisRound++;

			Global.predictionsCount++;
			Global.squaredError += Math.pow((predictionValue - value), 2);

			this.predictionsCount++;
			this.squaredError += Math.pow((predictionValue - value), 2);

			hit = true;
		}
		else
		{
			Global.numberOfMissesInThisRound++;
			hit = false;
		}
		return hit;
	}
	

	/**
	 * Lê o próximo valor do sensor atual, manda para o sink e engatilha a proxima leitura<p>
	 * [Eng] Read the next value from present sensor, send to sink and trigger the next reading 
	 * @param dataSensedType Tipo de dado a ser lido pelo sensor <p> [Eng] <b>dataSensedType</b> Type of data to be read from sensor: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem
	 */
	protected void makeSensorReadingAndSendind(String dataSensedType)
	{
		int medida = 0;
		if (dataSensedType != null)
		{
			medida = identifyType(dataSensedType);
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
				quantTime = parseCalendarHours(linhas[0], linhas[1]);
				
				
				
				addDataRecordItens(dataSensedType.charAt(0), value, quantTime);
				
					

				WsnMsgResponse wsnMsgResp = new WsnMsgResponse(1, this, null, this, 1, 2, dataSensedType);

				//Adds the last values ​​previously read to the message that goes to the sink
				for (int cont=0; cont<dataRecordItens.size(); cont++) //for(int cont=0; cont<slidingWindowSize; cont++)
				{
					wsnMsgResp.addDataRecordItens(dataRecordItens.get(cont).type, dataRecordItens.get(cont).value, dataRecordItens.get(cont).time); 
				}
				
				addThisNodeToPath(wsnMsgResp);
				
				WsnMessageResponseTimer timer = new WsnMessageResponseTimer(wsnMsgResp, proximoNoAteEstacaoBase);
				
				timer.startRelative(1, this); // Espera por "wsnMessage.sizeTimeSlot" rounds e envia a mensagem para o nó sink (próximo nó no caminho do sink)

				ReadingSendingTimer newReadingSendingTimer = new ReadingSendingTimer(dataSensedType);
				newReadingSendingTimer.startRelative(1, this);
				
			} // end if (linhas.length > 4)
			
		} // end if (sensorReading != null && medida != 0)
	}
	
	/**
	 * Chama o método makeSensorReagingAndSending em loop<p>
	 * [Eng] Calls the method makeSensorReadingAndSendind in looping
	 * @param dataSensedType Tipo de dado a serem lidos pelo sensor: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem <p> [Eng] <b>dataSnsedType</b> Type of data to be read from sensor: "t"=temperature, "h"=humidity, "l"=luminosity or "v"=voltage
	 * @param coefA Coeficiente A da equação de regressão para esse sensor <p> [Eng] <b>coefA</b> Coefficient A from the Regression Equation for this sensor
	 * @param coefB Coeficiente B da equação de regressão para esse sensor <p> [Eng] <b>coefB</b> Coefficient B from the Regression Equation for this sensor
	 * @param maxError Limiar de erro para o calculo da predição desse sensor <p> [Eng] <b>maxError</b> Threshold error to the calculation of prediction for this sensor
	 */
	public final void makeSensorReadingAndSendingLoop(String dataSensedType)
	{
		if (newContinuousSensingModeEnable) {
			// TODO: Implementar um flag booleano (newCoefsReceived) para indicar que, no Adaga-P*, os nós sensores não podem continuar sensoriando em loop pois já receberam os coeficientes para predição
			if (!newCoefsReceived) {
				makeSensorReadingAndSendind(dataSensedType);
			}
		}
		else {
			makeSensorReadingAndSendind(dataSensedType);
		}
	}
	
	/**
	 * It prints the RMSE (Root Mean Square Error) for this sensor
	 */
	public void printNodeRMSE(){
		double RMSE = 0.0;
		if(this.predictionsCount > 0)
		{
			RMSE = Math.sqrt(this.squaredError / this.predictionsCount);
		}
		System.out.println(this.ID+"\t"+NumberFormat.getNumberInstance().format(RMSE));
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
