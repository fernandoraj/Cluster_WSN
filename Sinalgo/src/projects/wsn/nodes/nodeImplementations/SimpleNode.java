package projects.wsn.nodes.nodeImplementations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import projects.wsn.nodes.messages.WsnMsg;
import projects.wsn.nodes.timers.WsnMessageTimer;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

public class SimpleNode extends Node 
{
	
	private double value;
	private Date data;

	//Armazenar o nó que será usado para alcançar a Estação-Base
	private Node proximoNoAteEstacaoBase;
	
	//Armazena o número de sequencia da última mensagem recebida
	private Integer sequenceNumber = 0;
	
	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()){
			Message message = inbox.next();
			if (message instanceof WsnMsg){
				Boolean encaminhar = Boolean.TRUE;
				WsnMsg wsnMessage = (WsnMsg) message;
				if (wsnMessage.forwardingHop.equals(this)){ // A mensagem voltou. O nó deve descarta-la
					encaminhar = Boolean.FALSE;
				}
				else if (wsnMessage.tipoMsg == 0)// A mensagem é um flood. Devemos atualizar a rota
				{ 
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
				if (encaminhar){
					//Devemos alterar o campo forwardingHop(da mensagem) para armazenar o noh que vai encaminhar a mensagem.
					wsnMessage.forwardingHop = this; 
					broadcast(wsnMessage);
				} //if (encaminhar)
			} //if (message instanceof WsnMsg)
		} //while (inbox.hasNext())
	} //public void handleMessages
/*	
	@Override
	public void handleMessages(Inbox inbox) {
		// TODO Auto-generated method stub - A ser implementado

	}
*/
	
	@NodePopupMethod(menuText="Definir Sink Node - Raiz de Roteamento")
	public void construirRoteamento(){
		this.proximoNoAteEstacaoBase = this;
		//WsnMsg wsnMessage = new WsnMsg(1, this, null, this, 0); //Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo
		WsnMsg wsnMessage = new WsnMsg(1, this, null, this, 0, 100, "t"); //Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo
		WsnMessageTimer timer = new WsnMessageTimer(wsnMessage);
		SinkNode sink = new SinkNode();
		//sink = this;
		timer.startRelative(1, this);
	}
	
	@Override
	public void preStep() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void neighborhoodChange() {
		// TODO Auto-generated method stub

	}

	@Override
	public void postStep() {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub

	}
	
	private  BufferedReader obterReader(String FileLocation) {
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
	
	private  boolean sensorId(String data, int sensor){
		if(!data.equals("")){
			int idReceived = Integer.parseInt(data);
			if (sensor == idReceived && idReceived < 55)
				return true;
		}
		return false;
	}
	
	private  boolean roundNumber(String data, int round){
		if(!data.equals("")){
			int roundReceived = Integer.parseInt(data);
			if (round == roundReceived)
				return true;
		}
		return false;
	}
	
	public void parserTxtToNode(int round, String tipo) throws IOException{
		BufferedReader leitura = obterReader("Sinalgo/data/data.txt");
		String linha = leitura.readLine();
		int sensorId = this.ID;
		boolean found = false;
		int medida = identificarTipo(tipo);
		while (linha != null){
			String linhas[] = linha.split(" ");
			if (linhas.length > 5 &&  roundNumber(linhas[2], round) && sensorId(linhas[3], sensorId)){
				value = Double.parseDouble(linhas[medida]);
				data = parserCalendarHoras(linhas[0], linhas[1]);
				found = true;
				break;
			}
			linha = leitura.readLine();
		}
		if (!found)
			//CASO ELE NAO TENHA NADA NESTE ROUND.
			System.out.println("Nao existe leitura neste round.");
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

}
