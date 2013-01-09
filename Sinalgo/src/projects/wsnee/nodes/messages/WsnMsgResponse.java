package projects.wsnee.nodes.messages;

import java.util.Stack;
import java.util.Vector;

import projects.wsnee.utils.Utils;

import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.nodes.messages.Message;

public class WsnMsgResponse extends Message {
	
	/**
	 *  Identificador da mensagem - Message ID
	 */
	public Integer sequenceID;
	
	/**
	 * Tempo de vida do Pacote - Time to live from package
	 */
	public Integer ttl;
	
	/**
	 * Nó de destino - Destination node
	 */
	public Node destino;
	
	/**
	 * Nó de origem - Source node
	 */
	public Node origem;
	
	/**
	 * Nó que vai reencaminhar a mensagem
	 */
	public Node forwardingHop;
	
	/**
	 * Número de saltos até o destino
	 */
	public Integer saltosAteDestino;
	
	/**
	 * Tipo do Pacote: 0 para Estabelecimento de Rotas, 1 para pacotes de dados, 2 para Info: #erros de predição excedido e 3 para Info: #timeSlot de predição excedido
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
	 * Percentual do limiar de erro espacial aceitável para as leituras dos nós sensores, que pode estar entre 0.0 (não aceita erros) e 1.0 (aceita todo e qualquer erro)
	 */
	public double spacialThresholdError = 0.0;
	
	/**
	 * Spatial position from the source node from message
	 */
	public Position spatialPos;

	/**
	 * Battery level from the sensor at moment
	 */
	public double batLevel;
	
	/**
	 * Caminho de nós do nó que envia a mensagem de resposta até o sink node, em forma de pilha
	 */
	private Stack<Integer> pathToSenderNode;
	
	/**
	 * Empilha um nó no caminho de nós
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
	 * Retorna o path para o nó que enviou a mensagem de resposta
	 * @return Caminho para o nó sender
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
	 * The sizeTimeSlot of Representative Node will be inversely proportional to the number of sensors in the same cluster -> number 'L' in documentation
	 * @param globalTimeSlot Initial Size of Time Slot from the sink node
	 * @param numSensorsInThisCLuster Number of nodes (sensors) in that cluster (group)
	 */
	public void calculatesTheSizeTimeSlotFromRepresentativeNode(int globalTimeSlot, int numSensorsInThisCLuster)
	{
		Utils.printForDebug("wsnMsgResponseRepresentative.sizeTimeSlot = "+this.sizeTimeSlot);
		this.sizeTimeSlot = (int)(globalTimeSlot / numSensorsInThisCLuster);
		Utils.printForDebug("New wsnMsgResponseRepresentative.sizeTimeSlot = "+this.sizeTimeSlot);
	}

	
	/**
	 * Construtor básico da Classe
	 * @param seqID Identificador da mensagem
	 * @param origem No de origem
	 * @param destino No de destino
	 * @param forwardingHop No que vai reencaminhar a mensagem
	 * @param tipo Tipo do Pacote: 0 para Estabelecimento de Rotas, 1 para pacotes de dados, 2 para Info: #erros de predição excedido e 3 para Info: #timeSlot de predição excedido
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
	 * @param tipo Tipo do Pacote: 0 para Estabelecimento de Rotas, 1 para pacotes de dados, 2 para Info: #erros de predição excedido e 3 para Info: #timeSlot de predição excedido
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
		this.saltosAteDestino = 0;
	}

	/**
	 * Construtor estendido da Classe
	 * @param seqID Identificador da mensagem
	 * @param origem No de origem
	 * @param destino No de destino
	 * @param forwardingHop No que vai reencaminhar a mensagem
	 * @param tipo Tipo do Pacote: 0 para Estabelecimento de Rotas, 1 para pacotes de dados, 2 para Info: #erros de predição excedido e 3 para Info: #timeSlot de predição excedido
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

	/**
	 * Construtor estendido da Classe
	 * @param seqID Identificador da mensagem
	 * @param origem No de origem
	 * @param destino No de destino
	 * @param forwardingHop No que vai reencaminhar a mensagem
	 * @param tipo Tipo do Pacote: 0 para Estabelecimento de Rotas, 1 para pacotes de dados, 2 para Info: #erros de predição excedido e 3 para Info: #timeSlot de predição excedido
	 * @param sizeTS Numero de dados sensoreados por time slot (Tamanho do time slot)
	 * @param dataSensedType Tipo de dado a ser sensoreado (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem
	 * @param thresholdEr Limiar de erro aceitável
	 * @param spatialThresholdEr Limiar de erro espacial aceitável
	 */
	public WsnMsgResponse(Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo, Integer sizeTS, String dataSensedType, double thresholdEr, double spatialThresholdEr, double batLevel) {
		this.sequenceID = seqID;
		this.origem = origem;
		this.destino = destino;
		this.forwardingHop = forwardingHop;
		this.tipoMsg = tipo;
		this.sizeTimeSlot = sizeTS;
		this.dataSensedType = dataSensedType;
		this.thresholdError = thresholdEr;
		this.spacialThresholdError = spatialThresholdEr;
		this.batLevel = batLevel;
	}

	@Override
	public Message clone() {
		WsnMsgResponse msg = new WsnMsgResponse(this.sequenceID, this.origem, this.destino, this.forwardingHop, this.tipoMsg);
		msg.ttl = this.ttl;
		msg.saltosAteDestino = this.saltosAteDestino;
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
