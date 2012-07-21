package projects.wsn.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class WsnMsgResponse extends WsnMsg {
	
	/**
	 *  Identificador da mensagem
	 */
	public Integer sequenceID;
	
	/**
	 * Tempo de vida do Pacote
	 */
	public Integer ttl;
	
	/**
	 * No de destino
	 */
	public Node destino;
	
	/**
	 * No de origem
	 */
	public Node origem;
	
	/**
	 * No que vai reencaminhar a mensagem
	 */
	public Node forwardingHop;
	
	/**
	 * Numero de saltos até o destino
	 */
	public Integer saltosAteDestino;
	
	/**
	 * Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados
	 */
	public Integer tipoMsg = 0;
	
	/**
	 * Numero de dados sensoreados por time slot (Tamanho do time slot) 
	 */
	public Integer sizeTimeSlot = 0;
	
	/**
	 * Tipo de dado a ser sensoreado (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem
	 */
	public String dataSensedType = null;
	
	/**
	 * Percentual do limiar de erro aceitável para as leituras dos nós sensores, que pode estar entre 0.0 (não aceita erros) e 1.0(aceita todo e qualquer erro)
	 */
	public double thresholdError = 0.0;
	
	/**
	 * Construtor básico da Classe
	 * @param seqID Identificador da mensagem
	 * @param origem No de origem
	 * @param destino No de destino
	 * @param forwardingHop No que vai reencaminhar a mensagem
	 * @param tipo Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados
	 */
	public WsnMsgResponse(Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo) {
		this.sequenceID = seqID;
		this.origem = origem;
		this.destino = destino;
		this.forwardingHop = forwardingHop;
		this.tipoMsg = tipo;
	}

	/**
	 * Construtor mediano da Classe //Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Integer
	 * @param seqID Identificador da mensagem
	 * @param origem No de origem
	 * @param destino No de destino
	 * @param forwardingHop No que vai reencaminhar a mensagem
	 * @param tipo Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados
	 * @param sizeTS Numero de dados sensoreados por time slot (Tamanho do time slot)
	 * @param dataSensedType Tipo de dado a ser sensoreado (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem
	 */
	public WsnMsgResponse(Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Integer sizeTS, String dataSensedType) {
		this.sequenceID = seqID;
		this.origem = origem;
		this.destino = destino;
		this.forwardingHop = forwardingHop;
		this.tipoMsg = tipo;
		this.sizeTimeSlot = sizeTS;
		this.dataSensedType = dataSensedType;
	}

	/**
	 * Construtor estendido da Classe
	 * @param seqID Identificador da mensagem
	 * @param origem No de origem
	 * @param destino No de destino
	 * @param forwardingHop No que vai reencaminhar a mensagem
	 * @param tipo Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados
	 * @param sizeTS Numero de dados sensoreados por time slot (Tamanho do time slot)
	 * @param dataSensedType Tipo de dado a ser sensoreado (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem
	 * @param thresholdEr Limiar de erro aceitavel
	 */
	public WsnMsgResponse(Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Integer sizeTS, String dataSensedType, double thresholdEr) {
		this.sequenceID = seqID;
		this.origem = origem;
		this.destino = destino;
		this.forwardingHop = forwardingHop;
		this.tipoMsg = tipo;
		this.sizeTimeSlot = sizeTS;
		this.dataSensedType = dataSensedType;
		this.thresholdError = thresholdEr;
	}
	
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		WsnMsgResponse msg = new WsnMsgResponse(this.sequenceID, this.origem, this.destino, this.forwardingHop, this.tipoMsg);
		msg.ttl = this.ttl;
		msg.saltosAteDestino = this.saltosAteDestino;
		msg.sizeTimeSlot = this.sizeTimeSlot;
		msg.dataSensedType = this.dataSensedType;
		msg.thresholdError = this.thresholdError;
		return msg;
	}

}
