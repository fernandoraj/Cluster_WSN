package projects.wsnSccs.nodes.nodeImplementations;

import projects.wsnSccs.nodes.messages.WsnMsg;
import projects.wsnSccs.nodes.timers.WsnMessageTimer;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

public class SimpleNode extends Node {

	//Armazenar o nó que sera usado para alcançar a Estacao-Base
	private Node proximoNoAteEstacaoBase;
	
	//Armazena o número de sequencia da última mensagem recebida
	private Integer sequenceNumber = 0;
	
	private enum NodeType {CH, GW, EXT, MEM, INI, GWR, CHC};
	
	private NodeType nodeState;
	
	//Construtor a ser utilizado para inicializar o status de todos os nós como INI
	protected SimpleNode()
	{
		super();
		this.nodeState = NodeType.INI; // Inicializa o status de todos os nós como INI
	}
	
	private Double readValue()
	{
		//java.util.Random rand = sinalgo.tools.Tools.getRandomNumberGenerator();
		return (Double)(Math.random() * 100); // Gera um valor randômico entre 0 e 100, para representar a temperatura ou luminosidade lida
	}
	
	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()){
			Message message = inbox.next();
			if (message instanceof WsnMsg){
				Boolean encaminhar = Boolean.TRUE;
				WsnMsg wsnMessage = (WsnMsg) message;
				if (wsnMessage.forwardingHop.equals(this)){ // A mensagem voltou. O nó deve descarta-la
					encaminhar = Boolean.FALSE;
				}else if (wsnMessage.tipoMsg == 0){ // A mensagem é um flood. Devemos atualizar a rota
					if (proximoNoAteEstacaoBase == null){
						proximoNoAteEstacaoBase = inbox.getSender();
						sequenceNumber = wsnMessage.sequenceID;
					}else if (sequenceNumber < wsnMessage.sequenceID){ 
					//Recurso simples para evitar loop.
					//Exemplo: Noh A transmite em brodcast. Nó B recebe a msg e retransmite em broadcast.
					//Consequentemente, nó A irá receber a msg. Sem esse condicional, nó A iria retransmitir novamente, gerando um loop.
						sequenceNumber = wsnMessage.sequenceID;
					}else{
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
	
	@NodePopupMethod(menuText="Construir Arvore de Roteamento")
	public void construirRoteamento(){
		this.proximoNoAteEstacaoBase = this;
		WsnMsg wsnMessage = new WsnMsg(1, this, null, this, 0, this.readValue());
//		WsnMsg wsnMessage = new WsnMsg(1, this, null, this, 0);
		WsnMessageTimer timer = new WsnMessageTimer(wsnMessage);
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

}
