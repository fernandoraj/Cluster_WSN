package projects.wsnSccs.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class WsnMsg extends Message {
	
	//Identificador da mensagem
	public Integer sequenceID;
	
	//Tempo de vida do Pacote
	public Integer ttl;
	
	//Nó de destino
	public Node destino;
	
	//Nó de origem
	public Node origem;
	
	//Nó que vai reencaminhar a mensagem
	public Node forwardingHop;
	
	//Número de saltos até o destino
	public Integer saltosAteDestino;
	
	//Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados
	public Integer tipoMsg = 0;
	
	//Média dos valores lidos pelo nó
	public Double mediaValor = 0.0;
	
	//Construtor1 da Classe
	public WsnMsg(Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo) {
		this.sequenceID = seqID;
		this.origem = origem;
		this.destino = destino;
		this.forwardingHop = forwardingHop;
		this.tipoMsg = tipo;
	}

	//Construtor2 da Classe
	public WsnMsg(Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Double media) {
		this.sequenceID = seqID;
		this.origem = origem;
		this.destino = destino;
		this.forwardingHop = forwardingHop;
		this.tipoMsg = tipo;
		this.mediaValor = media;
	}
	
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		WsnMsg msg = new WsnMsg(this.sequenceID, this.origem, this.destino, this.forwardingHop, this.tipoMsg);
		msg.ttl = this.ttl;
		msg.saltosAteDestino = this.saltosAteDestino;
		return msg;
	}

}
