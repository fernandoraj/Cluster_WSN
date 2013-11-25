package projects.wsnee.nodes.messages;

import java.util.Stack;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

/**
 * Classe que representa uma mensagem enviada a um (ou mais) do sensor (s) com um pedido <p>
 * [Eng] Class that represents a message sent to one (or more) sensor(s) with a request
 * 
 * @author Fernando Rodrigues
 * 
 */
public class WsnMsg extends Message {
	
	/**
	 *  Identificador da mensagem <p>
	 *  [Eng] Message identifier
	 */
	public Integer sequenceID;
	
	/**
	 * Tempo de vida do Pacote <p>
	 * [Eng] Lifetime Package
	 */	public Integer ttl;
	
	/**
	 * Nó de destino <p>
	 * [Eng] Destination node
	 */
	public Node target;
	
	/**
	 * Nó de origem <p>
	 * [Eng] Source node
	 */
	public Node source;
	
	/**
	 * Nó que vai reencaminhar a mensagem <p>
	 * [Eng] Node that will forward the message
	 */
	public Node forwardingHop;
	
	/**
	 * Número de saltos até o destino <p>
	 * [Eng] Number of hops to the destination
	 */
	public Integer hopsToTarget;
	
	/**
	 * Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados <p>
	 * [Eng] Package type: 0 for Establishment of Routes 1 and for data packets
	 */
	public Integer typeMsg = 0;
	
	/**
	 * Numero de dados sensoreados por time slot (Tamanho do time slot) <p>
	 * [Eng] Number of sensed data per time slot (time slot Size)
	 */
	public Integer sizeTimeSlot = 0;
	
	/**
	 * Tipo de dado a ser sensoreado (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem <p>
	 * [Eng] Type of data to be sensed (read the sensor nodes), which can be: "T" = temperature, "h" = humidity, "l" = light or "v" = voltage
	 */
	public String dataSensedType = null;
	
	/**
	 * Percentual do limiar de erro aceitável para as leituras dos nós sensores, que pode estar entre 0.0 (não aceita erros) e 1.0(aceita todo e qualquer erro) <p>
	 * [Eng] Percentage threshold of error to the readings of sensor nodes, which can be between 0.0 (no errors accepted) and 1.0 (accepts all and any errors)
	 */
	private double thresholdError = 0.0;
	
	public double getThresholdError()
	{
		return thresholdError;
	}
	
	/**
	 * Número / Id de cluster head do nó (alvo) <p>
	 * [Eng] Number/Id of cluster head from node (target)
	 */
	private Integer clusterHeadId = -1;
	
	public Integer getClusterHeadId()
	{
		return this.clusterHeadId;
	}
	
	/**
	 * Nó sensor que gere / representa o cluster do nó (alvo) <p>
	 * [Eng] Sensor node that manages/represents the cluster of node (target)
	 */
	private Node clusterHead;
	
	public Node getClusterHead()
	{
		return this.clusterHead;
	}
	
	/**
	 * Representa os coeficientes (A e B) da equação (de regressão) a serem enviados para o nó correspondente <p>
	 * [Eng] Represents the coefficients (A e B) of equation (regression) to be sent to the corresponding node
	 * @author Fernando
	 */
	public class coefEquation
	{
		Double coefA;
		Double coefB;
	}
	
	/**
	 * Indica se a mensagem teve seus coeficientes da equação de regressão configurados <p>
	 * [Eng] Indicates whether the message had its coefficients of the regression equation configured
	 */
	private boolean hasCoefs = false;
	
	/**
	 * Método "get" para o parâmetro hasCoefs <p>
	 * [Eng]   Method "get"  for parameter hasCoefs
	 * @return Valor de hasCoefs, indicando se a mensagem já teve seus coeficientes configurados <p>[Eng] HasCoefs value, indicating whether to message has already had its coefficients configured
	 */
	public boolean hasCoefs()
	{
		return hasCoefs;
	}
	
	/**
	 * Armazena os coeficientes (A e B) da equação (de regressão) <p>
	 * [Eng] Store the coefficients (A and B) of equation (regression)
	 */
	private coefEquation coefs;
	
	/**
	 * Lê o coeficiente A da equação <p>
	 * [Eng] Reads the coefficient A in the equation
	 * @return valor do coeficiente A <p>[Eng] value of the coefficient A
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
	 * Lê o coeficiente B da equação <p>
	 * [Eng] Reads the coefficient B in the equation
	 * @return valor do coeficiente B <p>[Eng] value of the coefficient B
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
	 * Configura os valores dos coeficientes (A e B) da equação <p>
	 * [Eng] Configures the values ??of the coefficients (A and B) of the equation
	 * @param A valor do coeficiente A <p>[Eng] value of the coefficient A
	 * @param B valor do coeficiente B <p>[Eng] value of the coefficient B
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
	 * Remove os valores dos coeficientes (A e B) da equação <p>
	 * [Eng] Remove the values ??of the coefficients (A and B) of the equation
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
	 * Caminho de nós do nó que envia a mensagem de resposta até o sink node, em forma de pilha <p>
	 * [Eng] Path nodes of the node that sends the reply message until the sink node in the form of stack
	 */
	private Stack<Integer> pathToSenderNode;
	
	/**
	 * Desempilha um nó do caminho de nós <p>
	 * [Eng] Pops a node of nodes
	 * @return Nó desempilhado <p>[Eng] popped node
	 */
	public Integer popFromPath()
	{
		if (pathToSenderNode == null || pathToSenderNode.isEmpty())
		{
			return null;
		}
/*
		if (this.hopsToTarget != null && this.hopsToTarget > 0)
		{
			this.hopsToTarget--; // Decrementa o contador de saltos (passos) de caminho (de rota) de nós
		}
*/		
		return pathToSenderNode.pop(); // Remove/Desempilha o próximo nó (noID) do caminho (path) para o nó de origem e o retorna
	}
	
	
	@SuppressWarnings("unchecked")
	public void setPathToSenderNode(Stack<Integer> pathToSenderNode, Integer hopsToTarget)
	{
		this.pathToSenderNode = (Stack<Integer>)pathToSenderNode.clone();
		this.hopsToTarget = hopsToTarget;
	}

	/**
	 * Construtor básico da Classe <p>
	 * [Eng] Basic constructor class
	 * @param seqID Identificador da mensagem <P>[Eng] Message identifier <b> seqID </b>
	 * @param origem Nó de origem <p>[Eng] Node the source <b> origem </b>
	 * @param destino Nó de destino <p>[Eng] Node of destination <b> destino </b>
	 * @param forwardingHop Nó que vai reencaminhar a mensagem <p>[Eng] Node that will forward the message <b> forwardingHop </b>
	 * @param tipo Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados <p>[Eng] Package type: 0 for Establishment of Routes 1 and for data packets <b> tipo </b>
	 */
	public WsnMsg(Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo) {
		this.sequenceID = seqID;
		this.source = origem;
		this.target = destino;
		this.forwardingHop = forwardingHop;
		this.typeMsg = tipo;
	}

	/**
	 * Construtor mediano da Classe //Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Integer sizeTS <p>
	 * [Eng] Median Class Constructor // Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Integer sizeTS
	 * @param seqID Identificador da mensagem <p>[Eng] Message identifier <b> seqID </b>
	 * @param origem Nó de origem <p>[Eng] Node the source <b> origem </b>
	 * @param destino Nó de destino <p>[Eng] Node of destination <b> destino </b>
	 * @param forwardingHop Nó que vai reencaminhar a mensagem <p>[Eng] Node that will forward the message <b> forwardingHop </b>
	 * @param tipo Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados <p>[Eng] Package type: 0 for Establishment of Routes 1 and for data packets <b> tipo </b>
	 * @param sizeTS Número de dados sensoreados por time slot (Tamanho do time slot) <p>[Eng] Number of sensed data per time slot (time slot Size) <b> sizeTS </b>
	 * @param dataSensedType Tipo de dado a ser sensoreado (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem <p>[Eng] Type of data to be sensoreado (read the sensor nodes), which can be: "T" = temperature, "h" = humidity, "l" = light or "v" = voltage <b> dataSensedType </b>
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
	 * Construtor estendido da Classe <p>
	 * [Eng] Extended Constructor Class
	 * @param seqID Identificador da mensagem <p>[Eng] Message identifier <b> seqID </b>
	 * @param origem No de origem <p>[Eng] Node the source <b> origem </b>
	 * @param destino No de destino <p>[Eng] Node of destination <b> destino </b>
	 * @param forwardingHop No que vai reencaminhar a mensagem <p>[Eng] Node that will forward the message <b> forwardingHop </b>
	 * @param tipo Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados <p>[Eng] Package type: 0 for Establishment of Routes 1 and for data packets <b> tipo </b>
	 * @param sizeTS Numero de dados sensoreados por time slot (Tamanho do time slot) <p>[Eng] Number of sensed data per time slot (time slot Size) <b> sizeTS </b>
	 * @param dataSensedType Tipo de dado a ser sensoreado (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem <p>[Eng] Type of data to be sensoreado (read the sensor nodes), which can be: "T" = temperature, "h" = humidity, "l" = light or "v" = voltage <b> dataSensedType </b>
	 * @param thresholdEr Limiar de erro aceitavel <p>[Eng] Threshold of acceptable error <b> thresholdEr </b>
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
	
	/**
	 * Construtor estendido da Classe
	 * @param seqID Identificador da mensagem <p>[Eng] Message identifier <b> seqID </b>
	 * @param origem No de origem <p>[Eng] Node the source <b> origem </b>
	 * @param destino No de destino <p>[Eng] Node of destination <b> destino </b>
	 * @param forwardingHop No que vai reencaminhar a mensagem <p>[Eng] Node that will forward the message <b> forwardingHop </b>
	 * @param tipo Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados <p>[Eng] Package type: 0 for Establishment of Routes 1 and for data packets <b> tipo </b>
	 * @param sizeTS Numero de dados sensoreados por time slot (Tamanho do time slot) <p>[Eng] Number of sensed data per time slot (time slot Size) <b> sizeTS </b>
	 * @param dataSensedType Tipo de dado a ser sensoreado (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem <p>[Eng] Type of data to be sensoreado (read the sensor nodes), which can be: "T" = temperature, "h" = humidity, "l" = light or "v" = voltage <b> dataSensedType </b>
	 * @param thresholdEr Limiar de erro aceitavel <p> [Eng] Threshold of acceptable error <b> thresholdEr </b>
	 * @param clusterHeadNode Cluster Head do cluster do nó que receberá a mensagem <p> [Eng] Cluster Head cluster node to receive the message <b> clusterHeadNode </b>
	 */
	public WsnMsg(Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Integer sizeTS, String dataSensedType, double thresholdEr, Node clusterHeadNode) {
		this.sequenceID = seqID;
		this.source = origem;
		this.target = destino;
		this.forwardingHop = forwardingHop;
		this.typeMsg = tipo;
		this.sizeTimeSlot = sizeTS;
		this.dataSensedType = dataSensedType;
		this.thresholdError = thresholdEr;
		this.clusterHead = clusterHeadNode;
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
		msg.hasCoefs = this.hasCoefs;
		msg.pathToSenderNode = this.pathToSenderNode;
		msg.clusterHead = this.clusterHead;
		return msg;
	}

}