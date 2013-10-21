package projects.wsn.nodes.messages;

import java.util.Stack;
import java.util.Vector;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class WsnMsgResponse extends Message {
	
	/**
	 *  Identificador da mensagem<p>
	 *  [Eng] Identifier of the message
	 */
	public Integer sequenceID;
	
	/**
	 * Tempo de vida do Pacote<p>
	 * [Eng] Lifetime of the package
	 */
	public Integer ttl;
	
	/**
	 * No de destino<p>
	 * [Eng] Target node
	 */
	public Node target;
	
	/**
	 * No de origem<p>
	 * [Eng] Source node
	 * 
	 */
	public Node source;
	
	/**
	 * No que vai reencaminhar a mensagem<p>
	 * [Eng] Node which will forward the message
	 */
	public Node forwardingHop;
	
	/**
	 * Numero de saltos até o destino<p>
	 * [Eng] Number of hops to the target
	 */
	public Integer hopsToTarget;
	
	/**
	 * Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados<p>
	 * [Eng] Package type: 0 to Establishing Routes and 1 to data packages
	 */
	public Integer typeMsg = 0;
	
	/**
	 * Numero de dados sensoreados por time slot (Tamanho do time slot)<p>
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
	public double thresholdError = 0.0;
	
	/**
	 * Caminho de nós do nó que envia a mensagem de resposta até o sink node, em forma de pilha<p>
	 * [Eng]Path nodes of the node that sends the reply message to the sink node, in the form of stack
	 */
	private Stack<Integer> pathToSenderNode;
	
	/**
	 * Empilha um nó no caminho de nós<p>
	 * [Eng] Stack one node on path nodes
	 * @param no Nó a ser empilhado
	 */
	public void pushToPath(Integer no)
	{
		if (pathToSenderNode == null)
		{
			pathToSenderNode = new Stack<Integer>();
		}
		pathToSenderNode.push(no);
	}
	
	/**
	 * Retorna o caminho para o nó que enviou a mensagem de resposta<p>
	 * [Eng] Returns the path to the node which sends the reply message
	 * @return Caminho para o nó sender <p> [Eng] Path to sender node
	 */
	public Stack<Integer> clonePath()
	{
		Stack<Integer> temp = pathToSenderNode;
		pathToSenderNode = null;
		return temp;
//		return (Stack<Node>)pathToSenderNode.clone();
	}
	
	private boolean naoLido = true;
	
	private double[] values;
	
	private double[] times;
	
	private char[] types;
	
	public class DataRecord
	{
		char type;
		double value;
		double time;
	}
	
	public Vector<DataRecord> dataRecordItens;
	
	public void addDataRecordItens(char typ, double val, double tim)
	{
		if (this.dataRecordItens == null)
		{
			this.dataRecordItens = new Vector<DataRecord>();
		}
		DataRecord dr = new DataRecord();
		
		dr.type = typ;
		dr.value = val;
		dr.time = tim;
		
		dataRecordItens.add(dr);
		naoLido = true;
	}
	
	private void lerDados()
	{
		if (naoLido)
		{
			int tam = 0;
			if (dataRecordItens != null)
			{
				tam = dataRecordItens.size();
			}
			values = new double[tam];
			times = new double[tam];
			types = new char[tam];
			
			for (int i=0; i<tam; i++)
			{
				if (dataRecordItens.get(i) != null)
				{
					values[i] = ((DataRecord)dataRecordItens.get(i)).value;
					times[i] = ((DataRecord)dataRecordItens.get(i)).time;
					types[i] = ((DataRecord)dataRecordItens.get(i)).type;
				}
				else
				{
					values[i] = 0.0;
					times[i] = 0.0;
					types[i] = ' ';
				}
			}
			
			naoLido = false;
		}
	}
	
	public double[] getDataRecordValues()
	{
		lerDados();
		return values;
	}

	public double[] getDataRecordTimes()
	{
		lerDados();
		return times;
	}

	public char[] getDataRecordTypes()
	{
		lerDados();
		return types;
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
	public WsnMsgResponse(Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo) {
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
	public WsnMsgResponse(Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Integer sizeTS, String dataSensedType) {
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
	public WsnMsgResponse(Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Integer sizeTS, String dataSensedType, double thresholdEr) {
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
		WsnMsgResponse msg = new WsnMsgResponse(this.sequenceID, this.source, this.target, this.forwardingHop, this.typeMsg);
		msg.ttl = this.ttl;
		msg.hopsToTarget = this.hopsToTarget;
		msg.sizeTimeSlot = this.sizeTimeSlot;
		msg.dataSensedType = this.dataSensedType;
		msg.thresholdError = this.thresholdError;
		msg.dataRecordItens = this.dataRecordItens;
		msg.pathToSenderNode = this.pathToSenderNode;
		return msg;
	}

}
