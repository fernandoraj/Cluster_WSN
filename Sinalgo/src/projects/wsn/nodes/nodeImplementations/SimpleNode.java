package projects.wsn.nodes.nodeImplementations;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.LinkedList;

import projects.wsn.nodes.messages.WsnMsg;
import projects.wsn.nodes.messages.WsnMsgResponse;
import projects.wsn.nodes.timers.WsnMessageResponseTimer;
import projects.wsn.utils.FileHandler;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

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
	private LinkedList<String> sensorReadingsLoadedFromFile = new LinkedList<String>();
	
	/**
	 * Stores the value referring to the last line loaded from the sensor readings file in order that
	 * when the <code>SensorReadingsLoadedFromFile</code> list is empty and a new load from the file has to be
	 * executed to fill the <code>SensorReadingsLoadedFromFile</code> list, the loading starts from the last
	 * line read from the file.
	 */
	private int lastLineLoadedFromSensorReadingsFile;
	
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
		
	@Override
	public void preStep() {

	}

	@Override
	public void init() {
//		FileHandler.printForDebug("nodeID: " + this.ID + " loading sensor readings from file");
//		long initTime = System.currentTimeMillis();
//		loadSensorReadingsFromFile();
//		long finishTime = System.currentTimeMillis();
//		long totalTime = finishTime - initTime; 
//		FileHandler.printForDebug("nodeID: " + this.ID + " " + sensorReadingsLoadedFromFile.size() + " lines loading process in " + (totalTime) + " millis.");
	}

	@Override
	public void neighborhoodChange() {

	}

	@Override
	public void postStep() {

	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {

	}
	
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

/*		
		try
		{
			parserTxtToNode(roundAtual, sizeTimeSlot, dataSensedType, wsnMsgResp);
		}
		catch(IOException ex)
		{
			// Tratar exceção
		}
*/
//		double timeAtual = Global.currentTime;
//		int roundAtual = (int)timeAtual;

//		for (int i=roundAtual; i<(roundAtual+sizeTimeSlot); i++)
//		{
			//wsnMsgResp.addDataRecordItens(type, dr.value, dr.time);
//		}
		
		// PAREI AQUI!!!
		
	}//private void prepararMensagem(WsnMsgResponse wsnMsgResp, Integer sizeTimeSlot, String dataSensedType)
	
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
	 * collected from the <code>SensorReadingsLoadedFromFile</code> list. </p>
	 * <p>In the case that the list is
	 * empty, it is filled with sensor readings loaded from the sensor readings file.</p>
	 */
	public String performSensorReading()
	{
		//TODO Implementar método para simular o sensoriamento de dados físicos
		//se a lista estiver vazia, carregar leituras do arquivo
		//entao coletar proxima leitura da lista
		if (sensorReadingsLoadedFromFile != null && sensorReadingsLoadedFromFile.isEmpty())
		{
			loadSensorReadingsFromFile();
		}
		if (sensorReadingsLoadedFromFile != null && !sensorReadingsLoadedFromFile.isEmpty())
		{
			String data = sensorReadingsLoadedFromFile.remove();
			return data;
		}
		return null;
	}
	
	/**
	 * Fills the <code>SensorReadingsLoadedFromFile</code> list with sensor readings from the file.
	 * The amount of readings (file lines) to be loaded is informed in the <code>Config.xml</code> file (<code>SensorReadingsLoadBlockSize</code>) tag.
	 */
	public void loadSensorReadingsFromFile(){
		try {
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
						sensorReadingsLoadedFromFile.add(line); //loads the line to the memory
						line = bufferedReader.readLine();
						lineCounter++;
					}
					break;
				}
				lineCounter++;
				line = bufferedReader.readLine();
			}			
			bufferedReader.close();
		if (sensorReadingsLoadedFromFile.size() < sensorReadingsLoadBlockSize) {
			System.err.println("NodeID: " + this.ID + " has already read all the sensor readings of the file. " +
					"\n It has only " + sensorReadingsLoadedFromFile.size() + " readings in its memory (sensorReadingsLoadedFromFile list)");
		}

		lastLineLoadedFromSensorReadingsFile = lastLineLoadedFromSensorReadingsFile + lineCounter; //updates the last line read from the file
		
		} catch (CorruptConfigurationEntryException e) {
			System.out.println("Problems while loading variable sensorReadingsFilePath at simpleNode.loadSensorReadingFromFile()");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Problems while reading lines (fileReader.readLine()) from  sensorReadingsFilePath at simpleNode.loadSensorReadingFromFile()");
			e.printStackTrace();
		}
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

	
	public class DataRecord
	{
		double value;
		double time;
	}
	
	private boolean isBetweenRoundNumber(String data, int minRound, int maxRound)
	{
		if(!data.equals("")){
			int roundRead = Integer.parseInt(data);
			if (roundRead >= minRound && roundRead < maxRound)
				return true;
		}
		return false;
	}

	private boolean isGreaterRoundNumber(String data, int maxRound)
	{
		if(!data.equals("")){
			int roundRead = Integer.parseInt(data);
			if (roundRead > maxRound)
				return true;
		}
		return false;
	}
	
//	private boolean comparaSensorId(String data, int sensor)
//	{
//		if(!data.equals("")){
//			int idReceived = Integer.parseInt(data);
//			if (sensor == idReceived && idReceived < 55)
//				return true;
//		}
//		return false;
//	}

//	private boolean isGreaterSensorId(String data, int sensor)
//	{
//		if(!data.equals("")){
//			int idReceived = Integer.parseInt(data);
//			if (idReceived > sensor)
//				return true;
//		}
//		return false;
//	}
	
//	private boolean isBetween(String data, int round)
//	{
//		if(!data.equals("")){
//			int roundReceived = Integer.parseInt(data);
//			if (round == roundReceived)
//				return true;
//		}
//		return false;
//	}
	
//	private Date parserCalendarHoras(String AnoMesDia, String hora)
//	{
//		String[] datas = AnoMesDia.split("-");
//		String[] horas = hora.split(":");
//		String certo = "";
//		String millesegundos = "";
//		for (String mille : horas){
//			if(mille.contains(".")){
//				String correto = mille.substring(0,mille.indexOf("."));
//				millesegundos = mille.substring(mille.indexOf(".")+1, mille.length());
//				certo = correto;
//			}
//		}
//		horas[2] = certo;
//		GregorianCalendar gc = new GregorianCalendar(Integer.parseInt(datas[0]), Integer.parseInt(datas[1]) -1, Integer.parseInt(datas[2]),Integer.parseInt(horas[0]),Integer.parseInt(horas[1]), Integer.parseInt(horas[2]));
//		Date data = new Date(gc.getTimeInMillis() + Long.parseLong(millesegundos)/1000);
//		return data;
//	}

	//public synchronized void parserTxtToNode(int round, Integer sizeTimeSlot, String tipo, WsnMsgResponse wsnMsgResp) throws IOException
//	public void parserTxtToNode(int round, Integer sizeTimeSlot, String tipo, WsnMsgResponse wsnMsgResp) throws IOException
//	{
//		System.out.println("ID do noh: "+this.ID);
//		BufferedReader leitura = getBufferedReader("C:/Users/Fernando/Documents/My Classes/UFC/Doutorado/Doutorado - PPGIA/Artigos/Redes de Sensores/Ferramentas/data/data.txt");
//		String linha = leitura.readLine();
//		int sensorId = this.ID;
//		int medida = identificarTipo(tipo);
//		ultimoRoundLido = round;
//		double value;
//		double quantTime;
//		int cont = 0;
//		char type;
//		if (tipo!=null && !tipo.equals(""))
//		{
//			type = tipo.charAt(0);
//		}
//		else
//		{
//			type = ' ';
//		}
//		while ((linha != null) && (cont<sizeTimeSlot))
//		{
//			System.out.println("Entrou no while ((linha != null) && (!found)): linha = "+linha);
//			String linhas[] = linha.split(" ");
//			System.out.println("round = "+round);
//			System.out.println("sensorId = "+sensorId);
//			System.out.println("ultimoRoundLido = "+ultimoRoundLido);
//			System.out.println("(ultimoRoundLido + sizeTimeSlot) = "+(ultimoRoundLido + sizeTimeSlot));
//			System.out.println("cont = "+cont);
//			System.out.println("");
//			if (linhas.length > 4 && comparaSensorId(linhas[3], sensorId) && isBetweenRoundNumber(linhas[2], ultimoRoundLido, (ultimoRoundLido + sizeTimeSlot)))
//			{
//				System.out.println("Entrou no if (linhas.length > 4 ...");
//				cont++;
//				value = Double.parseDouble(linhas[medida]);
//				quantTime = parseCalendarHoras(linhas[0], linhas[1]);
//				wsnMsgResp.addDataRecordItens(type, value, quantTime);
//			}
//			if (isGreaterSensorId(linhas[3], sensorId) || (comparaSensorId(linhas[3], sensorId) && isGreaterRoundNumber(linhas[2], (ultimoRoundLido + sizeTimeSlot))))
//			{
//				System.out.println("\n ENTROU NO BREAK !!! \n");
//				break;
//			}
//			linha = leitura.readLine();
//		}
		
//		if ((found) && (linha != null))
//		{
//			System.out.println("\nEntrou no if ((found) && (linha != null)) ***\n");
//			int maxRound = (round + sizeTimeSlot);
//			
//			String linhas[] = linha.split(" ");
//			while (linhas.length > 4 && comparaSensorId(linhas[3], sensorId) && (menorQueRoundNumber(linhas[2], maxRound)))
//			{
//				System.out.println("Entrou no while ... (menorQueRoundNumber(linhas[2], maxRound), maxRound = "+maxRound);
//				value = Double.parseDouble(linhas[medida]);
//				quantTime = parseCalendarHoras(linhas[0], linhas[1]);
//				wsnMsgResp.addDataRecordItens(type, value, quantTime);
//				cont++;
//				linha = leitura.readLine();
//				linhas = linha.split(" ");
//			}
//		}
//		//CASO ELE NAO TENHA NADA NESTE ROUND.
//		if (cont==0)
//			System.out.println("Nao existe leitura neste round.");
//		leitura.close();
//	}
	
/*	
	@Override
	public void handleMessages(Inbox inbox) {
	}
*/

}
