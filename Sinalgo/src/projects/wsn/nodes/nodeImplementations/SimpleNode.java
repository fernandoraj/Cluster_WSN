package projects.wsn.nodes.nodeImplementations;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import projects.wsn.nodes.messages.WsnMsg;
import projects.wsn.nodes.messages.WsnMsgResponse;
import projects.wsn.nodes.timers.WsnMessageResponseTimer;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;

public class SimpleNode extends Node 
{
	
	//Armazenar o nó que será usado para alcançar a Estação-Base
	protected Node proximoNoAteEstacaoBase;
	
	//Armazena o número de sequencia da última mensagem recebida
	protected Integer sequenceNumber = 0;
	
	protected int ultimoRoundLido;
	
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
				if (encaminhar)
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
/*	
	@Override
	public void handleMessages(Inbox inbox) {
	}
*/
		
	@Override
	public void preStep() {

	}

	@Override
	public void init() {

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
		double timeAtual = Global.currentTime;
		int roundAtual = (int)timeAtual;
		
//		for (int i=roundAtual; i<(roundAtual+sizeTimeSlot); i++)
//		{
			try
			{
				parserTxtToNode(roundAtual, sizeTimeSlot, dataSensedType, wsnMsgResp);
			}
			catch(IOException ex)
			{
				// Tratar exceção
			}
			//wsnMsgResp.addDataRecordItens(type, dr.value, dr.time);
//		}
		
		// PAREI AQUI!!!
		
	}
	
	//private synchronized BufferedReader obterReader(String FileLocation)
	private BufferedReader obterReader(String FileLocation)
	{
		try {
			File file = new File(FileLocation);
			FileReader fr = new FileReader(file);
			BufferedReader ler = new BufferedReader(fr);
			return ler;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean comparaSensorId(String data, int sensor)
	{
		if(!data.equals("")){
			int idReceived = Integer.parseInt(data);
			if (sensor == idReceived && idReceived < 55)
				return true;
		}
		return false;
	}
	
//	private boolean isBetween(String data, int round)
//	{
//		if(!data.equals("")){
//			int roundReceived = Integer.parseInt(data);
//			if (round == roundReceived)
//				return true;
//		}
//		return false;
//	}
	
	private boolean isBetweenRoundNumber(String data, int minRound, int maxRound)
	{
		if(!data.equals("")){
			int roundRead = Integer.parseInt(data);
			if (roundRead >= minRound && roundRead < maxRound)
				return true;
		}
		return false;
	}

	public class DataRecord
	{
		double value;
		double time;
	}

	//public synchronized void parserTxtToNode(int round, Integer sizeTimeSlot, String tipo, WsnMsgResponse wsnMsgResp) throws IOException
	public void parserTxtToNode(int round, Integer sizeTimeSlot, String tipo, WsnMsgResponse wsnMsgResp) throws IOException
	{
		System.out.println("ID do noh: "+this.ID);
		BufferedReader leitura = obterReader("C:/Users/Fernando/Documents/My Classes/UFC/Doutorado/Doutorado - PPGIA/Artigos/Redes de Sensores/Ferramentas/data/data.txt");
		String linha = leitura.readLine();
		int sensorId = this.ID;
		int medida = identificarTipo(tipo);
		ultimoRoundLido = round;
		double value;
		double quantTime;
		int cont = 0;
		char type;
		if (tipo!=null && !tipo.equals(""))
		{
			type = tipo.charAt(0);
		}
		else
		{
			type = ' ';
		}
		while ((linha != null) && (cont<sizeTimeSlot))
		{
			System.out.println("Entrou no while ((linha != null) && (!found)): linha ="+linha);
			String linhas[] = linha.split(" ");
			System.out.println("round = "+round);
			System.out.println("sensorId = "+sensorId);
			System.out.println("");
			if (linhas.length > 4 && comparaSensorId(linhas[3], sensorId) && isBetweenRoundNumber(linhas[2], ultimoRoundLido, (ultimoRoundLido + sizeTimeSlot)))
			{
				System.out.println("Entrou no if (linhas.length > 4 ...");
				cont++;
				value = Double.parseDouble(linhas[medida]);
				quantTime = parseCalendarHoras(linhas[0], linhas[1]);
				wsnMsgResp.addDataRecordItens(type, value, quantTime);
			}
			linha = leitura.readLine();
		}
		
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
		//CASO ELE NAO TENHA NADA NESTE ROUND.
		if (cont==0)
			System.out.println("Nao existe leitura neste round.");
		leitura.close();
	}

	private int identificarTipo(String tipo) {
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
	
	private Date parserCalendarHoras(String AnoMesDia, String hora){
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
		Date data = new Date(gc.getTimeInMillis() + Long.parseLong(millesegundos)/1000);
		return data;
	}

	private long parseCalendarHoras(String AnoMesDia, String hora){
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
}
