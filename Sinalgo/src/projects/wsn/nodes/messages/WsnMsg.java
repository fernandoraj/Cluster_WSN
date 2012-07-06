package projects.wsn.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class WsnMsg extends Message {
	
	//Identificador da mensagem
	public Integer sequenceID;
	
	//Tempo de vida do Pacote
	public Integer ttl;
	
	//No de destino
	public Node destino;
	
	//No de origem
	public Node origem;
	
	//No que vai reencaminhar a mensagem
	public Node forwardingHop;
	
	//Numero de saltos até o destino
	public Integer saltosAteDestino;
	
	//Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados
	public Integer tipoMsg = 0;
	
	//Numero de dados sensoreados por time slot (Tamanho do time slot) 
	public Integer sizeTimeSlot = 0;
	
	//Tipo de dado a ser sensoreado (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem
	public String typeDataSensed = null;
	
	//Percentual do limiar de erro aceitável para as leituras dos nós sensores, que pode estar entre 0.0 (não aceita erros) e 1.0(aceita todo e qualquer erro)
	public double thresholdError = 0.0;
	
	//Construtor básico da Classe
	public WsnMsg(Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo) {
		this.sequenceID = seqID;
		this.origem = origem;
		this.destino = destino;
		this.forwardingHop = forwardingHop;
		this.tipoMsg = tipo;
	}

	//Construtor mediano da Classe
	public WsnMsg(Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Integer sizeTS, String typeDS) {
		this.sequenceID = seqID;
		this.origem = origem;
		this.destino = destino;
		this.forwardingHop = forwardingHop;
		this.tipoMsg = tipo;
		this.sizeTimeSlot = sizeTS;
		this.typeDataSensed = typeDS;
	}

	//Construtor estendido da Classe
	public WsnMsg(Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Integer sizeTS, String typeDS, double thresholdEr) {
		this.sequenceID = seqID;
		this.origem = origem;
		this.destino = destino;
		this.forwardingHop = forwardingHop;
		this.tipoMsg = tipo;
		this.sizeTimeSlot = sizeTS;
		this.typeDataSensed = typeDS;
		this.thresholdError = thresholdEr;
	}
	
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		WsnMsg msg = new WsnMsg(this.sequenceID, this.origem, this.destino, this.forwardingHop, this.tipoMsg);
		msg.ttl = this.ttl;
		msg.saltosAteDestino = this.saltosAteDestino;
		msg.sizeTimeSlot = this.sizeTimeSlot;
		msg.typeDataSensed = this.typeDataSensed;
		msg.thresholdError = this.thresholdError;
		return msg;
	}

}
