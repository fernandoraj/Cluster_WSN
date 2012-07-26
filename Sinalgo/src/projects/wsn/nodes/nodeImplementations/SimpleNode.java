package projects.wsn.nodes.nodeImplementations;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import projects.wsn.nodes.messages.WsnMsg;
import projects.wsn.nodes.messages.WsnMsgResponse;
import projects.wsn.nodes.timers.WsnMessageResponseTimer;
import projects.wsn.utils.FileHandler;
import projects.wsn.utils.Utils;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

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
	
	//Armazenar o nó que será usado para alcançar a Estação-Base
	protected Node proximoNoAteEstacaoBase;
	
	//Armazena o número de sequencia da última mensagem recebida
	protected Integer sequenceNumber = 0;
	
	protected int ultimoRoundLido;
	
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
			if (message instanceof WsnMsg)
			{
				Boolean encaminhar = Boolean.TRUE;
				WsnMsg wsnMessage = (WsnMsg) message;
				if (wsnMessage.forwardingHop.equals(this)) // A mensagem voltou. O nó deve descarta-la
				{ 
					encaminhar = Boolean.FALSE;
				}
				else if (wsnMessage.tipoMsg == 0)// A mensagem é um flood. Devemos atualizar a rota
				{ 
					this.setColor(Color.BLUE);
					if (proximoNoAteEstacaoBase == null)
					{
						proximoNoAteEstacaoBase = inbox.getSender();
						sequenceNumber = wsnMessage.sequenceID;
					}
					else if (sequenceNumber < wsnMessage.sequenceID)
					{ 
					//Recurso simples para evitar loop.
					//Exemplo: Nó A transmite em brodcast. Nó B recebe a msg e retransmite em broadcast.
					//Consequentemente, nó A irá receber a msg. Sem esse condicional, nó A iria retransmitir novamente, gerando um loop.
						sequenceNumber = wsnMessage.sequenceID;
					}
					else
					{
						encaminhar = Boolean.FALSE;
					}
				} //if (wsnMessage.tipoMsg == 0)
				else if (wsnMessage.tipoMsg == 1)// A mensagem é um pacote transmissor de dados (coeficientes). Devemos atualizar a rota
					{ 
						this.setColor(Color.YELLOW);
						if (wsnMessage.destino == this)
						{

						}
						else if (sequenceNumber < wsnMessage.sequenceID)
						{ 
						//Recurso simples para evitar loop.
						//Exemplo: Nó A transmite em brodcast. Nó B recebe a msg e retransmite em broadcast.
						//Consequentemente, nó A irá receber a msg. Sem esse condicional, nó A iria retransmitir novamente, gerando um loop.
							sequenceNumber = wsnMessage.sequenceID;
						}
						else
						{
							encaminhar = Boolean.FALSE;
						}
					} //if (wsnMessage.tipoMsg == 0)
					if (encaminhar && wsnMessage.tipoMsg == 1)
					{
						wsnMessage.forwardingHop = this; 
						broadcast(wsnMessage);
					}
				else if (encaminhar)
				{
					
					WsnMsgResponse wsnMsgResp = new WsnMsgResponse(1, this, null, this, 0, 100, "t"); 

					prepararMensagem(wsnMsgResp, wsnMessage.sizeTimeSlot, wsnMessage.dataSensedType);
					
					WsnMessageResponseTimer timer = new WsnMessageResponseTimer(wsnMsgResp, proximoNoAteEstacaoBase);
					
					timer.startRelative(wsnMessage.sizeTimeSlot, this); // Espera por "wsnMessage.sizeTimeSlot" rounds e envia a mensagem para o nó sink (próximo nó no caminho do sink)
					
					//Devemos alterar o campo forwardingHop(da mensagem) para armazenar o noh que vai encaminhar a mensagem.
					wsnMessage.forwardingHop = this; 
					broadcast(wsnMessage);
					
				} //if (encaminhar)
			} //if (message instanceof WsnMsg)
			else if (message instanceof WsnMsgResponse)
			{
				WsnMsgResponse wsnMsgResp = (WsnMsgResponse) message;
				
				WsnMessageResponseTimer timer = new WsnMessageResponseTimer(wsnMsgResp, proximoNoAteEstacaoBase);
				
				timer.startRelative(1, this); // Envia a mensagem para o próximo nó no caminho do sink no próximo round (1)
			} // else if (message instanceof WsnMsgResponse)
		} //while (inbox.hasNext())
	} //public void handleMessages
	
	/**
	 * Prepara a mensagem "wsnMsgResp" para ser enviada para o sink acrescentando os dados lidos pelo nó atual
	 * @param wsnMsgResp Mensagem a ser preparada para envio
	 * @param sizeTimeSlot Tamanho do slot de tempo (intervalo) a ser lido pelo nó sensor
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
				System.out.println("sensorID = "+this.ID);
				System.out.println("dataSensedType = "+dataSensedType);
				System.out.println("medida = "+medida);
//				System.out.println("(ultimoRoundLido + sizeTimeSlot) = "+(ultimoRoundLido + sizeTimeSlot));
//				System.out.println("cont = "+cont);
				System.out.println("");
				if (linhas.length > 4)
				{
					System.out.println("Entrou no if (linhas.length > 4) : dataLine = "+dataLine);
					System.out.println("\ncont = "+cont+"\n");
					cont++;
/*
					if (this.ID == 5)
					{
						for (int l=0; l<linhas.length; l++)
						{
							System.out.println("linhas["+l+"] = "+linhas[l]);
						}
//						break;
					}
*/
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
					wsnMsgResp.addDataRecordItens(dataSensedType.charAt(0), value, quantTime);
				}//if (linhas.length > 4)
			}//if (dataLine != null && dataSensedType != null && medida != 0)
			dataLine = performSensorReading();
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
			if (this.ID == intSensorID && intSensorID < 55){
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
						if (lineValues.length > 4 && this.isMyID(lineValues[3])) {
							sensorReadingsQueue.add(line); //loads the line to the memory
							line = bufferedReader.readLine();
							lineCounter++;
						} else {
							break;
						}
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
	synchronized private void loadSensorReadingsFromMemory() {
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
	
	private double fazerPredicao(double A, double B, double tempo)
	{
		double time;
		time = A + B*tempo;
		return time;
	}
}
