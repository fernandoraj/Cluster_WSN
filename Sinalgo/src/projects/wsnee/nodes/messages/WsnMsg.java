package projects.wsnee.nodes.messages;

import java.util.Stack;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class WsnMsg extends Message {
	
	/**
	 *  Identificador da mensagem
	 */
	public Integer sequenceID;
	
	/**
	 * Tempo de vida do Pacote
	 */
	public Integer ttl;
	
	/**
	 * Nó de destino
	 */
	public Node target;
	
	/**
	 * Nó de origem
	 */
	public Node source;
	
	/**
	 * Nó que vai reencaminhar a mensagem
	 */
	public Node forwardingHop;
	
	/**
	 * Número de saltos até o destino
	 */
	public Integer hopsToTarget;
	
	/**
	 * Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados
	 */
	public Integer typeMsg = 0;
	
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
	private double thresholdError = 0.0;
	
	public double getThresholdError()
	{
		return thresholdError;
	}
	
	/**
	 * Number/Identifier of cluster from node (target)
	 */
	private Integer clusterId = -1;
	
	public Integer getClusterId()
	{
		return this.clusterId;
	}
	
	/**
	 * Number/Identifier of sensor node that manages/represents the cluster of node (target)
	 */
	private Integer clusterHeadId = -1;
	
	public Integer getClusterHeadId()
	{
		return this.clusterHeadId;
	}
	
	/**
	 * Representa os coeficientes (A e B) da equação (de regressão) a serem enviados para o nó correspondente
	 * @author Fernando
	 */
	public class coefEquation
	{
		Double coefA;
		Double coefB;
	}
	
	/**
	 * Indica se a mensagem teve seus coeficientes da equação de regressão configurados
	 */
	private boolean hasCoefs = false;
	
	/**
	 * Método "get" para o parâmetro hasCoefs
	 * @return Valor de hasCoefs, indicando se a mensagem já teve seus coeficientes configurados
	 */
	public boolean hasCoefs()
	{
		return hasCoefs;
	}
	
	/**
	 * Armazena os coeficientes (A e B) da equação (de regressão)
	 */
	private coefEquation coefs;
	
	/**
	 * Lê o coeficiente A da equação
	 * @return valor do coeficiente A
	 */
	public Double getCoefA()
	{
		if (coefs != null)
		{
			return coefs.coefA;
		}
		return null;
	}
	
	/**
	 * Lê o coeficiente B da equação
	 * @return valor do coeficiente B
	 */
	public Double getCoefB()
	{
		if (coefs != null)
		{
			return coefs.coefB;
		}
		return null;
	}
	
	/**
	 * Configura os valores dos coeficientes (A e B) da equação
	 * @param A valor do coeficiente A
	 * @param B valor do coeficiente B
	 */
	public void setCoefs(Double A, Double B)
	{
		if (coefs == null)
		{
			coefs = new coefEquation();
		}
		hasCoefs = true;
		coefs.coefA = A;
		coefs.coefB = B;
	}

	/**
	 * Remove os valores dos coeficientes (A e B) da equação
	 */
	public void removeCoefs()
	{
		if (coefs != null)
		{
			coefs = null;
		}
		hasCoefs = false;
	}
	
	/**
	 * Caminho de nós do nó que envia a mensagem de resposta até o sink node, em forma de pilha
	 */
	private Stack<Integer> pathToSenderNode;
	
	/**
	 * Desempilha um nó do caminho de nós
	 * @return Nó desempilhado
	 */
	public Integer popFromPath()
	{
		if (pathToSenderNode == null || pathToSenderNode.isEmpty())
		{
			return null;
		}
		if (this.hopsToTarget != null && this.hopsToTarget > 0)
		{
			this.hopsToTarget--; // Decrementa o contador de saltos (passos) de caminho (de rota) de nós
		}
		return pathToSenderNode.pop(); // Remove/Desempilha o próximo nó (noID) do caminho (path) para o nó de origem e o retorna
	}
	
	public void setPathToSenderNode(Stack<Integer> pathToSenderNode)
	{
		this.pathToSenderNode = pathToSenderNode;
	}

	/**
	 * Construtor básico da Classe
	 * @param seqID Identificador da mensagem
	 * @param origem No de origem
	 * @param destino No de destino
	 * @param forwardingHop No que vai reencaminhar a mensagem
	 * @param tipo Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados
	 */
	public WsnMsg(Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo) {
		this.sequenceID = seqID;
		this.source = origem;
		this.target = destino;
		this.forwardingHop = forwardingHop;
		this.typeMsg = tipo;
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
	public WsnMsg(Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Integer sizeTS, String dataSensedType) {
		this.sequenceID = seqID;
		this.source = origem;
		this.target = destino;
		this.forwardingHop = forwardingHop;
		this.typeMsg = tipo;
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
	public WsnMsg(Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Integer sizeTS, String dataSensedType, double thresholdEr) {
		this.sequenceID = seqID;
		this.source = origem;
		this.target = destino;
		this.forwardingHop = forwardingHop;
		this.typeMsg = tipo;
		this.sizeTimeSlot = sizeTS;
		this.dataSensedType = dataSensedType;
		this.thresholdError = thresholdEr;
	}
	
	@Override
	public Message clone() {
		WsnMsg msg = new WsnMsg(this.sequenceID, this.source, this.target, this.forwardingHop, this.typeMsg);
		msg.ttl = this.ttl;
		msg.hopsToTarget = this.hopsToTarget;
		msg.sizeTimeSlot = this.sizeTimeSlot;
		msg.dataSensedType = this.dataSensedType;
		msg.thresholdError = this.thresholdError;
		msg.coefs = this.coefs;
		msg.pathToSenderNode = this.pathToSenderNode;
		return msg;
	}

}
