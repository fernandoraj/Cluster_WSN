package projects.wsn.nodes.messages;

import java.util.Stack;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class WsnMsg extends Message {
	
	/**
	 *  Identificador da mensagem<p>
	 *  [Eng] Identifier of the message
	 */
	public Integer sequenceID;
	
	/**
	 * Tempo de vida do Pacote<p>
	 * lifetime of the package
	 */
	public Integer ttl;
	
	/**
	 * Nó de destino<p>
	 * [Eng] Target node
	 */
	public Node target;
	
	/**
	 * Nó de origem<p>
	 * [Eng] Source node
	 */
	public Node source;
	
	/**
	 * Nó que vai reencaminhar a mensagem<p>
	 * [Eng] Node which will forward the message
	 */
	public Node forwardingHop;
	
	/**
	 * Número de saltos até o destino<p>
	 * [Eng] Number of hops to the target
	 */
	public Integer hopsToTarget;
	
	/**
	 * Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados<p>
	 * [Eng] Package type: 0 to Establishing Routes and 1 to data packages
	 */
	public Integer typeMsg = 0;
	
	/**
	 * Número de dados sensoreados por time slot (Tamanho do time slot) <p>
	 * [Eng] Number of sensed data per time slot (Time slot size)
	 */
	public Integer sizeTimeSlot = 0;
	
	/**
	 * Tipo de dado a ser sensoreado (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=umidade, "l"=luminosidade ou "v"=voltagem<p>
	 * [Eng] Data type to be sensorized (read on sensor nodes), which can be: "t"=temperature, "h" =moisture, "l"=luminosity or "v"=voltage
	 */
	public String dataSensedType = null;
	
	/**
	 * Percentual do limiar de erro aceitável para as leituras dos nós sensores, que pode estar entre 0.0 (não aceita erros) e 1.0(aceita todo e qualquer erro)<p>
	 * [Eng] Percentage threshold of error to the readings of sensor nodes, which can be between 0.0 (no errors accepted) and 1.0 (accepts any and all errors)
	 */
	private double thresholdError = 0.0;
	
	/**
	 * Retorna o percentual do limiar de erro aceitável para as leituras dos nós sensores<p>
	 * [Eng] Return percentage threshold of error to the readings of sensor nodes
	 * @return
	 */
	public double getThresholdError()
	{
		return thresholdError;
	}
	
	/**
	 * Representa os coeficientes (A e B) da equação (de regressão) a serem enviados para o nó correspondente<p>
	 * [Eng] Represents the coefficients (A and B) of equation (regression) to be sent to the corresponding node
	 * 
	 * @author Fernando
	 */
	public class coefEquation
	{
		Double coefA;
		Double coefB;
	}
	
	/**
	 * Indica se a mensagem teve seus coeficientes da equação de regressão configurados<p>
	 * [Eng] Indicates whether the message had its coefficients of the regression equation set
	 */
	private boolean hasCoefs = false;
	
	/**
	 * Método "get" para o parâmetro hasCoefs<p>
	 * [Eng] "get" method for parameter hasCoefs
	 * @return Valor de hasCoefs, indicando se a mensagem já teve seus coeficientes configurados
	 */
	public boolean isCoefs()
	{
		return hasCoefs;
	}
	
	/**
	 * Armazena os coeficientes (A e B) da equação (de regressão)<p>
	 * [Eng] Stores the coefficients (A, B) of equation (regression)
	 */
	private coefEquation coefs;
	
	/**
	 * Lê o coeficiente A da equação<p>
	 * [Eng] Reads the coefficient A of equation
	 *
	 * @return valor do coeficiente A <p> [Eng] value of coefficient A
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
	 * Lê o coeficiente B da equação<p> 
	 * [Eng] Reads the coefficient B of equation
	 * @return valor do coeficiente B <p> [Eng] value of coefficient B
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
	 * Configura os valores dos coeficientes (A e B) da equação<p>
	 * [Eng] Configures the values of the coefficients(A and B) of equation
	 * @param A valor do coeficiente A <p> [Eng] <b>A</b> value of coefficient A
	 * @param B valor do coeficiente B <p> [Eng] <b>B</b> value of coefficient B
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
	 * Caminho de nós do nó que envia a mensagem de resposta até o sink node, em forma de pilha<p>
	 * [Eng] Path nodes of the node that sends the reply message to the sink node, in the form of stack
	 */
	private Stack<Integer> pathToSenderNode;
	
	/**
	 * Desempilha um nó do caminho de nós<p>
	 * [Eng] Unstack one node of path nodes
	 * @return Nó desempilhado <p> [Eng] Unstacked node
	 */
	public Integer popFromPath()
	{
		if (pathToSenderNode == null || pathToSenderNode.isEmpty())
		{
			return null;
		}
		return pathToSenderNode.pop();
	}
	
	public void setPathToSenderNode(Stack<Integer> pathToSenderNode)
	{
		this.pathToSenderNode = pathToSenderNode;
	}

	/**
	 * Construtor básico da Classe //Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo<p>
	 * [Eng] Basic constructor of the class //Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo
	 * @param seqID Identificador da mensagem <p> [Eng] <b>seqID</b> Identifier of the message
	 * @param origem No de origem <p> [Eng] <b>origem</b> Source node
	 * @param destino No de destino <p> [Eng] <b>destino</b> Destination node
	 * @param forwardingHop No que vai reencaminhar a mensagem <p> [Eng] <b>forwardingHop</b> Node which will forward the message
	 * @param tipo Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados <p> [Eng] <b>tipo</b> Package type: 0 to Establishing Routes and 1 to data packages
	 */
	public WsnMsg(Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo) {
		this.sequenceID = seqID;
		this.source = origem;
		this.target = destino;
		this.forwardingHop = forwardingHop;
		this.typeMsg = tipo;
	}

	/**
	 * Construtor mediano da Classe //Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Integer sizeTS, String dataSensedType<p>
	 * [Eng] Median constructor of the class //Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Integer sizeTS, String dataSensedType
	 * @param seqID Identificador da mensagem <p> [Eng] <b>seqID</b> Identifier of the message
	 * @param origem No de origem <p> [Eng] <b>origem</b> Source node
	 * @param destino No de destino <p> [Eng] <b>destino</b> Destination node
	 * @param forwardingHop No que vai reencaminhar a mensagem <p> [Eng] <b>forwardingHop</b> Node which will forward the message
	 * @param tipo Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados <p> [Eng] <b>tipo</b> Package type: 0 to Establishing Routes and 1 to data packages
	 * @param sizeTS Numero de dados sensoreados por time slot (Tamanho do time slot) <p> [Eng] <b>sizeTS</b> Number of sensed data per time slot
	 * @param dataSensedType Tipo de dado a ser sensoreado (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=umidade, "l"=luminosidade ou "v"=voltagem <p> [Eng] <b>dataSensedType</b> Data type to be sensorized (read on sensor nodes), which can be: "t"=temperature, "h" =moisture, "l"=luminosity or "v"=voltage
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
	 * Construtor estendido da Classe //Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Integer sizeTS, String dataSensedType, double thresholdEr<p>
	 * [Eng] Extended constructor of the class //Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Integer sizeTS, String dataSensedType, double thresholdEr
	 * @param seqID Identificador da mensagem <p> [Eng] <b>seqID</b> Identifier of the message
	 * @param origem No de origem <p> [Eng] <b>origem</b> Source node
	 * @param destino No de destino <p> [Eng] <b>destino</b> Destination node
	 * @param forwardingHop No que vai reencaminhar a mensagem <p> [Eng] <b>forwardingHop</b> Node which will forward the message
	 * @param tipo Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados <p> [Eng] <b>tipo</b> Package type: 0 to Establishing Routes and 1 to data packages
	 * @param sizeTS Numero de dados sensoreados por time slot (Tamanho do time slot) <p> [Eng] <b>sizeTS</b> Number of sensed data per time slot
	 * @param dataSensedType Tipo de dado a ser sensoreado (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem <p> [Eng] <b>dataSensedType</b> Data type to be sensorized (read on sensor nodes), which can be: "t"=temperature, "h" =moisture, "l"=luminosity or "v"=voltage
	 * @param thresholdEr Limiar de erro aceitavel <p> [Eng] <b>tresholdEr</b> Acceptable error threshold
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
