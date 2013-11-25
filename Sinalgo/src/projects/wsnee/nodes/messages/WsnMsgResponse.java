package projects.wsnee.nodes.messages;

import java.util.Stack;
import java.util.Vector;

import projects.wsnee.nodes.nodeImplementations.SimpleNode;
import projects.wsnee.nodes.nodeImplementations.Cluster;
import projects.wsnee.utils.Utils;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.nodes.messages.Message;

/**
 * Class that represents a response message sent, usually to the sink (or
 * another sensor) in response to a request
 * 
 * @author Fernando Rodrigues
 * 
 */
public class WsnMsgResponse extends Message {
	
	/**
	 *  Identificador da mensagem <p>
	 *  [Eng] Message identifier
	 */
	public Integer sequenceID;
	/**
	 * Tempo de vida do Pacote <p>
	 * [Eng] Lifetime Package
	 */
	public Integer ttl;
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
	 * Percentual do limiar de erro aceitável para as leituras dos nós sensores, que pode estar entre 0.0 (não aceita erros) e 1.0(aceita todo e qualquer erro)
	 */
	public double thresholdError = 0.0;

	/**
	 * Percentual do limiar de erro aceitável para as leituras dos nós sensores, que pode estar entre 0.0 (não aceita erros) e 1.0(aceita todo e qualquer erro) <p>
	 * [Eng] Percentage threshold of error to the readings of sensor nodes, which can be between 0.0 (no errors accepted) and 1.0 (accepts all and any errors)
	 */
	public double spacialThresholdError = 0.0;
	
	/**
	 * Posição espacial a partir do nó fonte da mensagem <p>
	 * [Eng] Spatial position from the source node from message
	 */
	public Position spatialPos;

	/**
	 * Nível da bateria do sensor no momento <p>
	 * [Eng] Battery level from the sensor at moment
	 */
	public double batLevel;
	
	/**
	 * Caminho de nós do nó que envia a mensagem de resposta até o sink node, em forma de pilha <p>
	 * [Eng] Path nodes of the node that sends the reply message until the sink node in the form of stack
	 */
	private Stack<Integer> pathToSenderNode;
	
	/**
	 * Cluster que o nó de origem pertence <p>
	 * [Eng] Cluster which the source node belongs to
	 */
	public Cluster cluster;
	
	/**
	 * Empilha um nó no caminho de nós <p>
	 * [Eng] Stacks a node on the way nodes 
	 * @param no Nó a ser empilhado <p>[Eng] Node to be stacked <b> no </b>
	 */
	public void pushToPath(Integer no)
	{
		boolean thisNodeAlreadyExists = false;
		if (pathToSenderNode == null) { // Se o atributo "pathToSenderNode" (path) ainda não existe (não foi instanciado)
			pathToSenderNode = new Stack<Integer>(); // Então instancia tal atributo
		}
		else if (pathToSenderNode.search(no) > -1) { // Se "pathToSenderNode" já existe e o nó a ser empilhado (inserido) já for encontrado no mesmo (path)
			thisNodeAlreadyExists = true; // Flag indica que o nó em questão (noID) já existe (está empilhado)
		}
		
		if (!thisNodeAlreadyExists) { // Se o nó em questão (noID) ainda não existe (na pilha)
			hopsToTarget++; // Incrementa o contador de saltos (passos) de caminho (de rota) de nós
			pathToSenderNode.push(no); // Adiciona/empilha o nó passado (noID) ao caminho (path) para o nó de origem
		}
	}
	
	/**
	 * Retorna o path para o nó que enviou a mensagem de resposta <p>
	 * [Eng] Returns the path to the node that sent the reply message 
	 * @return Caminho para o nó sender <p>[Eng] Path to the sender node
	 */
	public Stack<Integer> clonePath()
	{
		Stack<Integer> temp = pathToSenderNode;
//		pathToSenderNode = null;
		return temp;
//		return (Stack<Node>)pathToSenderNode.clone();
	}
	
	private boolean naoLido = true;
	
	private double[] values;
	
	private double[] times;
	
	private char[] types;
	
	private double[] batLevels;
	
	private int[] rounds;
	
	public class DataRecord
	{
		char type;
		double value;
		double time;
		double batLevel;
		int round;
	}
	
	public Vector<DataRecord> dataRecordItens;
	
	public void addDataRecordItens(char typ, double val, double tim, double bat, int rnd)
	{
		if (this.dataRecordItens == null)
		{
			this.dataRecordItens = new Vector<DataRecord>();
		}
		DataRecord dr = new DataRecord();
		
		dr.type = typ;
		dr.value = val;
		dr.time = tim;
		dr.batLevel = bat;
		dr.round = rnd;
		
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
			batLevels = new double[tam];
			rounds = new int[tam];
			
			for (int i=0; i<tam; i++)
			{
				if (dataRecordItens.get(i) != null)
				{
					values[i] = ((DataRecord)dataRecordItens.get(i)).value;
					times[i] = ((DataRecord)dataRecordItens.get(i)).time;
					types[i] = ((DataRecord)dataRecordItens.get(i)).type;
					batLevels[i] = ((DataRecord)dataRecordItens.get(i)).batLevel;
					rounds[i] = ((DataRecord)dataRecordItens.get(i)).round;
				}
				else
				{
					values[i] = 0.0;
					times[i] = 0.0;
					types[i] = ' ';
					batLevels[i] = 0.0;
					rounds[i] = 0;
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
	
	public double[] getDataRecordBatLevels()
	{
		lerDados();
		return batLevels;
	}
	
	public int[] getDataRecordRounds()
	{
		lerDados();
		return rounds;
	}
	
	/**
	 * O sizeTimeSlot do Representante Node será inversamente proporcional ao número de sensores no mesmo cluster -> número 'L' na documentação <p>
	 * [Eng] The sizeTimeSlot of Representative Node will be inversely proportional to the number of sensors in the same cluster -> number 'L' in documentation
	 * @param globalTimeSlot Tamanho inicial do Time slot do sink nó <p>[Eng] Initial Size of Time Slot from the sink node <b> globalTimeSlot </b>
	 * @param numSensorsInThisCLuster Número de nós (sensores) no cluster (grupo) <p>[Eng] Number of nodes (sensors) in that cluster (group)
	 */
	public void calculatesTheSizeTimeSlotFromRepresentativeNode(int globalTimeSlot, int numSensorsInThisCLuster)
	{
		Utils.printForDebug("wsnMsgResponseRepresentative.sizeTimeSlot = "+this.sizeTimeSlot);
		
		if (numSensorsInThisCLuster == 0) {
			numSensorsInThisCLuster = 1;
		}
			
		this.sizeTimeSlot = (int)(globalTimeSlot / numSensorsInThisCLuster);
		//this.sizeTimeSlot = SimpleNode.maxErrorsPerCluster; // O NR realizará os testes de novidades de tal forma que, apenas quando o número máximo de ciclos (rounds) for atingido (maxErrorsPerCluster) um novo NR será calculado;
		
		if (this.sizeTimeSlot < 1) { // sizeTimeSlot shouldn't be equal to 0 (or less than one)
			this.sizeTimeSlot = 1;
		}
		Utils.printForDebug("New wsnMsgResponseRepresentative.sizeTimeSlot = "+this.sizeTimeSlot+"\n");
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
	public WsnMsgResponse(Integer seqID, Node source, Node target, Node forwardingHop, Integer type) {
		this.sequenceID = seqID;
		this.source = source;
		this.target = target;
		this.forwardingHop = forwardingHop;
		this.typeMsg = type;
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
	public WsnMsgResponse(Integer seqID, Node source, Node target, Node forwardingHop, Integer type, Integer sizeTS, String dataSensedType) {
		this.sequenceID = seqID;
		this.source = source;
		this.target = target;
		this.forwardingHop = forwardingHop;
		this.typeMsg = type;
		this.sizeTimeSlot = sizeTS;
		this.dataSensedType = dataSensedType;
		this.hopsToTarget = 0;
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
	public WsnMsgResponse(Integer seqID, Node source, Node target, Node forwardingHop, Integer type, Integer sizeTS, String dataSensedType, double thresholdEr) {
		this.sequenceID = seqID;
		this.source = source;
		this.target = target;
		this.forwardingHop = forwardingHop;
		this.typeMsg = type;
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
	 * @param thresholdEr Limiar de erro aceitavel <p>[Eng] Threshold of acceptable error <b> thresholdEr </b>
	 * @param spatialThresholdEr Limiar de erro espacial aceitável <p>[Eng] Threshold of acceptable spatial error <b>  spatialThresholdEr </b>
	 */
	 
	 
	public WsnMsgResponse(Integer seqID, Node source, Node target, Node forwardingHop, Integer type, Integer sizeTS, String dataSensedType, double thresholdEr, double spatialThresholdEr, double batLevel) {
		this.sequenceID = seqID;
		this.source = source;
		this.target = target;
		this.forwardingHop = forwardingHop;
		this.typeMsg = type;
		this.sizeTimeSlot = sizeTS;
		this.dataSensedType = dataSensedType;
		this.thresholdError = thresholdEr;
		this.spacialThresholdError = spatialThresholdEr;
		this.batLevel = batLevel;
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
		msg.spacialThresholdError = this.spacialThresholdError;
		msg.spatialPos = this.spatialPos;
		msg.batLevel = this.batLevel;
		return msg;
	}

}