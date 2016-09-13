package projects.wsneeFD.nodes.messages;

import java.util.Stack;
import java.util.Vector;

import projects.wsnee6.nodes.nodeImplementations.Cluster;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.nodes.messages.Message;

/**
 * Class that represents a response message sent, usually to the sink (or another sensor) in response to a request <p>
 * [Eng] Classe que representa uma mensagem de resposta enviada, normalmente para o sink (ou um outro sensor) em resposta a uma requisição
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
	 * [Eng] Target node
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
	 * [Eng] Number of hops to the target
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
	 * Tipos de dados a serem sensoriados (lidos nos nós sensores), que, para os dados do "Intel Lab Data", podem ser: temperatura = 4, 
	 * umidade = 5, luminosidade = 6 ou voltagem = 7. <br>
	 * [Eng] Types of data to be sensed (read in the sensor nodes), which, for Intel Lab Data, can be: 
	 * temperature = 4; humidity = 5; brightness("lum") = 6 or voltage = 7;
	 */
	public int[] dataSensedTypes = null;
	
	/**
	 * Percentual do limiar de erro aceitável para as leituras dos nós sensores, que pode estar entre 0.0 (não aceita erros) e 1.0(aceita todo e qualquer erro) <p>
	 * [Eng] Percentage threshold of error to the readings of sensor nodes, which can be between 0.0 (no errors accepted) and 1.0 (accepts all and any errors)
	 */
	public double thresholdError = 0.0;

	/**
	 * Percentual do limiar de erro espacial aceitável para as leituras dos nós sensores, que pode estar entre 0.0 (não aceita erros) e 1.0(aceita todo e qualquer erro) <p>
	 * [Eng] Percentage spacial threshold of error to the readings of sensor nodes, which can be between 0.0 (no errors accepted) and 1.0 (accepts all and any errors)
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
	 * Caminho do nó que envia a mensagem de resposta até o sink node, em forma de pilha <p>
	 * [Eng] Path from the message sender node until the sink node as a stack
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
	
	private int[][] types2;
	
	private double[][] values2;
	
	private double[] times;
	
	private double[] batLevels;
	
	private int[] rounds;
		
	/*
	public Vector<DataRecord> dataRecordItens;

	public class MessageItens {
		public Node sourceNode;
		Vector<DataRecord> dataRecordItens;
		
		public MessageItens() {
			dataRecordItens = new Vector<DataRecord>();
		}
	}
*/	
	public MessageItem messageItemToCH;
	
	public MessageItem messageItemsToSink;

/*
	public void addDataRecordItens(int[] typs, double[] vals, double tim, double bat, int rnd)
	{
		if (this.messageItens == null)
		{
			this.messageItens = new MessageItens();
		}
		DataRecord dr = new DataRecord();
		
		dr.typs = typs;
		dr.values = vals;
		dr.time = tim;
		dr.batLevel = bat;
		dr.round = rnd;
		
		messageItens.dataRecordItens.add(dr);
		naoLido = true;
	}
*/	
/*	
	private void lerDados()
	{
		if (naoLido)
		{
			int tam = 0;
			if (dataRecordItens != null)
			{
				tam = dataRecordItens.size();
			}
			types2 = new int[tam][];
			values2 = new double[tam][];
			times = new double[tam];
			batLevels = new double[tam];
			rounds = new int[tam];
			
			for (int i=0; i<tam; i++)
			{
				if (dataRecordItens.get(i) != null)
				{
					types2[i] = ((DataRecord)dataRecordItens.get(i)).typs;
					values2[i] = ((DataRecord)dataRecordItens.get(i)).values;
					times[i] = ((DataRecord)dataRecordItens.get(i)).time;
					batLevels[i] = ((DataRecord)dataRecordItens.get(i)).batLevel;
					rounds[i] = ((DataRecord)dataRecordItens.get(i)).round;
				}
				else
				{
					types2[i] = null;
					values2[i] = null;
					times[i] = 0.0;
					batLevels[i] = 0.0;
					rounds[i] = 0;
				}
			}
			
			naoLido = false;
		}
	}
	
	public int[][] getDataRecordTypes()
	{
		lerDados();
		return types2;
	}

	public double[][] getDataRecordValues()
	{
		lerDados();
		return values2;
	}

	public double[] getDataRecordTimes()
	{
		lerDados();
		return times;
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
*/

	/**
	 * Construtor básico da Classe <p>
	 * [Eng] Basic constructor class
	 * @param seqID Identificador da mensagem <p>[Eng] Message identifier 
	 * @param source Nó de origem <p>[Eng] Source node 
	 * @param target Nó de destino <p>[Eng] Target node 
	 * @param forwardingHop Nó que vai reencaminhar a mensagem <p>[Eng] Node that will forward the message 
	 * @param type Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados <p>[Eng] Package type: 0 for Establishment of Routes 1 and for data packets 
	 */
	public WsnMsgResponse(Integer seqID, Node source, Node target, Node forwardingHop, Integer type) {
		this.sequenceID = seqID;
		this.source = source;
		this.target = target;
		this.forwardingHop = forwardingHop;
		this.typeMsg = type;
	}

	/**
	 * Construtor mediano da Classe //Integer seqID, Node source, Node target, Node forwardingHop, Integer type, Integer sizeTS <p>
	 * [Eng] Median Class Constructor // Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Integer sizeTS
	 * @param seqID Identificador da mensagem <p>[Eng] Message identifier 
	 * @param source Nó de origem <p>[Eng] Source node 
	 * @param target Nó de destino <p>[Eng] Target node
	 * @param forwardingHop Nó que vai reencaminhar a mensagem <p>[Eng] Node that will forward the message 
	 * @param type Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados <p>[Eng] Package type: 0 for Establishment of Routes 1 and for data packets 
	 * @param sizeTS Número de dados sensoreados por time slot (Tamanho do time slot) <p>[Eng] Number of sensed data per time slot (time slot Size) 
	 * @param dataSensedTypes Tipos de dados a serem sensoreados (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem <p>[Eng] Type of data to be sensoreado (read the sensor nodes), which can be: "T" = temperature, "h" = humidity, "l" = light or "v" = voltage 
	 */
	public WsnMsgResponse(Integer seqID, Node source, Node target, Node forwardingHop, Integer type, Integer sizeTS, int[] dataSensedTypes) {
		this.sequenceID = seqID;
		this.source = source;
		this.target = target;
		this.forwardingHop = forwardingHop;
		this.typeMsg = type;
		this.sizeTimeSlot = sizeTS;
		this.dataSensedTypes = dataSensedTypes;
		this.hopsToTarget = 0;
	}

	/**
	 * Construtor estendido da Classe <p>
	 * [Eng] Extended Constructor Class
	 * @param seqID Identificador da mensagem <p>[Eng] Message identifier 
	 * @param source Nó de origem <p>[Eng] Source node 
	 * @param target Nó de destino <p>[Eng] Target node
	 * @param forwardingHop Nó que vai reencaminhar a mensagem <p>[Eng] Node that will forward the message 
	 * @param type Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados <p>[Eng] Package type: 0 for Establishment of Routes 1 and for data packets 
	 * @param sizeTS Número de dados sensoreados por time slot (Tamanho do time slot) <p>[Eng] Number of sensed data per time slot (time slot Size) 
	 * @param dataSensedTypes Tipos de dados a serem sensoreados (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem <p>[Eng] Type of data to be sensoreado (read the sensor nodes), which can be: "T" = temperature, "h" = humidity, "l" = light or "v" = voltage 
	 * @param thresholdEr Limiar de erro aceitavel <p>[Eng] Threshold of acceptable error <b> thresholdEr </b>
	 */
	public WsnMsgResponse(Integer seqID, Node source, Node target, Node forwardingHop, Integer type, Integer sizeTS, int[] dataSensedTypes, double thresholdEr) {
		this.sequenceID = seqID;
		this.source = source;
		this.target = target;
		this.forwardingHop = forwardingHop;
		this.typeMsg = type;
		this.sizeTimeSlot = sizeTS;
		this.dataSensedTypes = dataSensedTypes;
		this.thresholdError = thresholdEr;
	}

	/**
	 * Construtor estendido da Classe <p>
	 * [Eng] Extended Constructor Class
	 * @param seqID Identificador da mensagem <p>[Eng] Message identifier 
	 * @param source Nó de origem <p>[Eng] Source node 
	 * @param target Nó de destino <p>[Eng] Target node
	 * @param forwardingHop Nó que vai reencaminhar a mensagem <p>[Eng] Node that will forward the message 
	 * @param type Tipo do Pacote: 0 para Estabelecimento de Rotas e 1 para pacotes de dados <p>[Eng] Package type: 0 for Establishment of Routes 1 and for data packets 
	 * @param sizeTS Número de dados sensoreados por time slot (Tamanho do time slot) <p>[Eng] Number of sensed data per time slot (time slot Size) 
	 * @param dataSensedTypes Tipos de dados a serem sensoreados (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem <p>[Eng] Type of data to be sensoreado (read the sensor nodes), which can be: "T" = temperature, "h" = humidity, "l" = light or "v" = voltage 
	 * @param thresholdEr Limiar de erro aceitavel <p>[Eng] Threshold of acceptable error 
	 * @param spatialThresholdEr Limiar de erro espacial aceitável <p>[Eng] Threshold of acceptable spatial error
	 * @param batLevel Caminho do nó que envia a mensagem de resposta até o sink node, em forma de pilha <p>[Eng] Path from the message sender node until the sink node as a stack
	 */
	 
	 
	public WsnMsgResponse(Integer seqID, Node source, Node target, Node forwardingHop, Integer type, Integer sizeTS, int[] dataSensedTypes, double thresholdEr, double spatialThresholdEr, double batLevel) {
		this.sequenceID = seqID;
		this.source = source;
		this.target = target;
		this.forwardingHop = forwardingHop;
		this.typeMsg = type;
		this.sizeTimeSlot = sizeTS;
		this.dataSensedTypes = dataSensedTypes;
		this.thresholdError = thresholdEr;
		this.spacialThresholdError = spatialThresholdEr;
		this.batLevel = batLevel;
	}
	
	/**
	 * Cria ums cópia da mensagem <p>
	 * [Eng] Create a copy of the message
	 */
	@Override
	public Message clone() {
		WsnMsgResponse msg = new WsnMsgResponse(this.sequenceID, this.source, this.target, this.forwardingHop, this.typeMsg);
		msg.ttl = this.ttl;
		msg.hopsToTarget = this.hopsToTarget;
		msg.sizeTimeSlot = this.sizeTimeSlot;
		msg.dataSensedTypes = this.dataSensedTypes;
		msg.thresholdError = this.thresholdError;
		msg.messageItemToCH = this.messageItemToCH;
		msg.pathToSenderNode = this.pathToSenderNode;
		msg.spacialThresholdError = this.spacialThresholdError;
		msg.spatialPos = this.spatialPos;
		msg.batLevel = this.batLevel;
		msg.messageItemsToSink = this.messageItemsToSink;
		return msg;
	}

}
