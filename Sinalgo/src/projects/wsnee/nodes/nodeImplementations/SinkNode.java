package projects.wsnee.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
//import java.util.Iterator;

import projects.wsnee.nodes.messages.WsnMsg;
import projects.wsnee.nodes.messages.WsnMsgResponse;
import projects.wsnee.nodes.timers.WsnMessageTimer;
import projects.wsnee.utils.Utils;
import projects.wsnee.utils.ArrayList2d;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;

public class SinkNode extends SimpleNode 
{

	/**
	 * Numero de dados sensoreados por time slot (Tamanho do time slot) 
	 */
	private Integer sizeTimeSlot = 100;
	
	/**
	 * Tipo de dado a ser sensoreado (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem
	 */
	private String dataSensedType = "h";
	
	/**
	 * Percentual do limiar de erro temporal aceitável para as leituras dos nós sensores, que pode estar entre 0.0 (não aceita erros) e 1.0 (aceita todo e qualquer erro)
	 */
	private double thresholdError = 0.01;
	
	/**
	 * Percentual do limiar de erro espacial aceitável para as leituras dos nós sensores, que pode estar entre 0.0 (não aceita erros) e 1.0 (aceita todo e qualquer erro)
	 */
	private double spacialThresholdError = 0.01;
	
	/**
	 * Maximum distance acceptable to the formation of clusters. If it is equal to zero (0.0), ignoring
	 */
	private double maxDistance = 0.0;
	
	/**
	 * Array 2D (clusters) from sensors (Messages from sensors = WsnMsgResponse).
	 */
	private ArrayList2d<WsnMsgResponse> messageGroups;
	
	/**
	 * Number of messages received by sink node from all other sensors nodes
	 */
	private int numMessagesReceived = 0;
	
	public SinkNode()
	{
		super();
		this.setColor(Color.RED);
		Utils.printForDebug("The size of time slot is "+sizeTimeSlot);
		Utils.printForDebug("The type of data sensed is "+dataSensedType);
		Utils.printForDebug("The threshold of error (max error) is "+thresholdError);
		Utils.printForDebug("The size of sliding window is "+SimpleNode.slidingWindowSize);
		
//		if(LogL.ROUND_DETAIL){
			Global.log.logln("\nThe size of time slot is "+sizeTimeSlot);
			Global.log.logln("The type of data sensed is "+dataSensedType);
			Global.log.logln("The threshold of error (max error) is "+thresholdError);
			Global.log.logln("The size of sliding window is "+SimpleNode.slidingWindowSize+"\n");
//		}
	}

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		String text = "S";
		super.drawNodeAsSquareWithText(g, pt, highlight, text, 1, Color.WHITE);
	}
	
	@NodePopupMethod(menuText="Definir Sink como Raiz de Roteamento")
	public void construirRoteamento(){
		this.proximoNoAteEstacaoBase = this;
		//WsnMsg wsnMessage = new WsnMsg(1, this, null, this, 0); //Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo
		WsnMsg wsnMessage = new WsnMsg(1, this, null, this, 0, sizeTimeSlot, dataSensedType); 
		WsnMessageTimer timer = new WsnMessageTimer(wsnMessage);
		timer.startRelative(1, this);
	}
	
	@Override
	public void handleMessages(Inbox inbox) 
	{
		while (inbox.hasNext())
		{
			Message message = inbox.next();
			if (message instanceof WsnMsgResponse)
			{
				this.setColor(Color.YELLOW);
				WsnMsgResponse wsnMsgResp = (WsnMsgResponse) message;
				classifyNodeInClusterByMessage(wsnMsgResp);
				numMessagesReceived++;
				if (numMessagesReceived >= 54)
				{
					classifyRepresentativeNodesByResidualEnergy();
				}
//				receiveMessage(wsnMsgResp, wsnMsgResp.sizeTimeSlot, wsnMsgResp.dataSensedType);
			} //if (message instanceof WsnMsg)
		} //while (inbox.hasNext())
	} //public void handleMessages
	
	/**
	 * It selects the Representative Node for each line (group) from sensors by the max residual energy and puts him in the first position (in line)
	 */
	private void classifyRepresentativeNodesByResidualEnergy()
	{
		if (messageGroups != null) // If there is a message group created
		{
			for (int line=0; line < messageGroups.getNumRows(); line++)
			{
				double maxBatLevel = 0.0;
				int maxIndex = 0;
				for (int col=0; col < messageGroups.getNumCols(line); col++)
				{
					WsnMsgResponse currentWsnMsgResp = messageGroups.get(line, col);
					double currentBatLevel = currentWsnMsgResp.batLevel;
					int currentIndex = col;
					if (currentBatLevel > maxBatLevel)
					{
						maxIndex = currentIndex;
					}
					 
				}
				if (maxIndex != 0)
				{
					changeMessageMaxBatLevelPosition(line, maxIndex);
				}
			}
		}
	}
	
	/**
	 * Change the message with maximum battery level for the first position [0] in that line from array
	 * @param line
	 * @param maxIndex
	 */
	private void changeMessageMaxBatLevelPosition(int line, int maxIndex)
	{
		messageGroups.move(line, maxIndex, 0);
	}
	
	/**
	 * Each line in "messageGroups" (ArrayList2d of objects WsnMsgResponse) represents a cluster of sensors (WsnMsgResponse.origem), 
	 * classified by Dissimilarity Measure from yours data sensed, stored on WsnMsgResponse.dataRecordItens
	 *  
	 * @param wsnMsgResp Message to be used for classify the sensor node
	 */
	private void classifyNodeInClusterByMessage(WsnMsgResponse newWsnMsgResp)
	{
		if (messageGroups == null) // If there isn't a message group yet, then it does create one and adds the message to it
		{
			messageGroups = new ArrayList2d<WsnMsgResponse>();
			messageGroups.ensureCapacity(54); // Ensure the capacity as the total number of sensors (nodes) in the data set
			messageGroups.add(newWsnMsgResp, 0); // Add the initial message to the group (ArrayList2d of WsnMsgResponse)
		}
		else
		{
			boolean found = false;
			int line = 0;
			while ((!found) && (line < messageGroups.getNumRows()))
			{
				int col = 0;
				boolean continueThisLine = true;
				while ((continueThisLine) && (col < messageGroups.getNumCols(line)))
				{
					WsnMsgResponse currentWsnMsgResp = messageGroups.get(line, col);
					if (testDistanceBetweenSensorPositions(currentWsnMsgResp, newWsnMsgResp))
					{
						if (testDissimilarityMeasureWithPairRounds(currentWsnMsgResp, newWsnMsgResp)) // If this (new)message (with sensor readings) already is dissimilar to current message
						{
							continueThisLine = false; // Then this (new)message doesn't belong to this cluster / line / group
						}
					}
					col++;
				}
				if ((continueThisLine) && (col == messageGroups.getNumCols(line)))
				{
					found = true;
					messageGroups.add(newWsnMsgResp, line);
				}
				else
				{
					line++;
				}
			}
			if (!found)
			{
				messageGroups.add(newWsnMsgResp, messageGroups.getNumRows()); // It adds the new message "wsnMsgResp" in a new line (cluster) of messageGroup 
			}
/*				
			for (int line = 0; line < messageGroups.getNumRows(); line++)
			{
				for (int col = 0; col < messageGroups.getNumCols(line); col++)
				{
					
				}
			}
*/			
		}
	}
	
	private boolean testDistanceBetweenSensorPositions(WsnMsgResponse currentWsnMsg, WsnMsgResponse newWsnMsg)
	{
		boolean distanceOK = false;
		if (maxDistance > 0.0)
		{
			if (currentWsnMsg.spatialPos.distanceTo(newWsnMsg.spatialPos) <= maxDistance)
			{
				distanceOK = true;
			}
/*
			else
			{
				distanceOK = false;
			}
*/
			// distanceOK = (currentWsnMsg.spatialPos.distanceTo(newWsnMsg.spatialPos) <= maxDistance); //Another form for the "if" (above) 
		}
		else if (maxDistance == 0.0) // Case the distance between sensors should be ignored
		{
			distanceOK = true;
		}
		return distanceOK;
	}
	
/*
	private boolean testDissimilarityMeasureWithoutPairRounds(WsnMsgResponse currentWsnMsg, WsnMsgResponse newWsnMsg)
	{
		return true;
	}
*/
	
	/**
	 * It tests if there is dissimilarity (lack of similarity) between the 2 set of measure from 2 sensor brought by the 2 messages
	 * @param currentWsnMsg Represents the current message from the group of messages (messageGroups) in a "ArrayList2d<WsnMsgResponse>" structure
	 * @param newWsnMsg Represents the recently arrived message in the sink node, sent from the source sensor node
	 * @return True case the two messages are DISsimilar, i.e., from different clusters (or "groups"); False, otherwise
	 */
	
	private boolean testDissimilarityMeasureWithPairRounds(WsnMsgResponse currentWsnMsg, WsnMsgResponse newWsnMsg)
	{
//		boolean sameSize = true;
		boolean mDissimilarityMagnitudeFound = false;
		boolean tDissimilarityTrendFound = false;
		
		int currentSize = currentWsnMsg.dataRecordItens.size();
		double[] currentValues = new double[currentSize];
/*
		double[] currentTimes = new double[currentSize];
		char[] currentTypes = new char[currentSize];
		double[] currentBatLevel = new double[currentSize];
*/
		int[] currentRound = new int[currentSize];
		
		//Data read from current sensor (from ArrayList2d)
		currentValues = currentWsnMsg.getDataRecordValues();
/*
		currentTimes = currentWsnMsg.getDataRecordTimes();
		currentTypes = currentWsnMsg.getDataRecordTypes();
		currentBatLevel = currentWsnMsg.getDataRecordBatLevels();
*/
		currentRound = currentWsnMsg.getDataRecordRounds();

		
		int newSize = newWsnMsg.dataRecordItens.size();
		double[] newValues = new double[newSize];
/*
		double[] newTimes = new double[newSize];
		char[] newTypes = new char[newSize];
		double[] newBatLevel = new double[newSize];
*/
		int[] newRound = new int[newSize];
		
		//Data read from new sensor (from message received)
		newValues = newWsnMsg.getDataRecordValues();
/*
		newTimes = newWsnMsg.getDataRecordTimes();
		newTypes = newWsnMsg.getDataRecordTypes();
		newBatLevel = newWsnMsg.getDataRecordBatLevels();
*/
		newRound = newWsnMsg.getDataRecordRounds();

		HashMap<Integer, Double> hashCurrentMsg, hashNewMsg;
		
		hashCurrentMsg = new HashMap<Integer, Double>();
		hashNewMsg = new HashMap<Integer, Double>();
		
		// Populates 2 HashMaps with the values from currentWsnMsg and newWsnMsg
		for (int i=0,j=0; (i < currentSize || j < newSize); i++, j++)
		{
			if (i < currentSize)
			{	
				hashCurrentMsg.put(currentRound[i], currentValues[i]);
			}
			if (j < newSize)
			{
				hashNewMsg.put(newRound[j], newValues[j]);
			}
		}


/*
		Iterator<Double> it = hashCurrentMsg.values().iterator();
		
		while (it.hasNext())
		{
		    System.out.println(it.next());
		}
*/	
		
		for (int i : hashCurrentMsg.keySet())
		{
			if (hashNewMsg.containsKey(i) && (Math.abs(hashCurrentMsg.get(i) - hashNewMsg.get(i)) > spacialThresholdError))
			{
				mDissimilarityMagnitudeFound = true;
				return mDissimilarityMagnitudeFound;
/*
				if (Math.abs(hashCurrentMsg.get(i) - hashNewMsg.get(i)) > spacialThresholdError)
				{
					mDissimilarityMagnitudeFound = true;
				}
*/				
			}
		}
		
		int contQ1 = 0;
		int contQ = currentSize; // = newSize; // Total size of sensed values from node
		for (int i=1,j=1; (i < currentSize && j < newSize); i++, j++)
		{
			double difX, difY;			
			difX = (currentValues[i] - currentValues[i-1]);
			difY = (newValues[j] - newValues[j-1]);
			if ((difX * difY) >= 0)
			{
				contQ1++;
			}
		}
		if (contQ1/contQ < thresholdError)
		{
			tDissimilarityTrendFound = true;
			return tDissimilarityTrendFound;
		}
		
/*
		if (currentSize != newSize)
		{
			sameSize = false; // Size from (2) data sets are different
		}
		
		if (sameSize && compareDataSetValuesPairToPair(currentValues, newValues, currentSize))
		{
			
		}
*/	
		return (mDissimilarityMagnitudeFound || tDissimilarityTrendFound);
	}

	private boolean compareDataSetValuesPairToPair(double[] valuesC, double[] valuesN, int size)
	{
		boolean ok = true;
		int cont = 0;
		while (ok && (cont<size))
		{
			if (Math.abs(valuesC[cont] - valuesN[cont]) > spacialThresholdError)
			{
				ok = false;
			}
			cont++;
		}
		return ok;
	}
	
	private void receiveMessage(WsnMsgResponse wsnMsgResp, Integer sizeTimeSlot, String dataSensedType)
	{
		if (wsnMsgResp != null && wsnMsgResp.dataRecordItens != null)
		{
			int size = wsnMsgResp.dataRecordItens.size();
			double[] valores = new double[size];
			double[] tempos = new double[size];
//			char[] tipos = new char[size];
			//Dados lidos do sensor correspondente
			valores = wsnMsgResp.getDataRecordValues();
			tempos = wsnMsgResp.getDataRecordTimes();
//			tipos = wsnMsgResp.getDataRecordTypes();
			//Coeficientes de regressão linear com os vetores acima
			double coeficienteA, coeficienteB;
			double mediaTempos, mediaValores;
			//Médias dos valores de leitura e tempos
			mediaTempos = calculaMedia(tempos);
			mediaValores = calculaMedia(valores);
			//Cálculos dos coeficientes de regressão linear com os vetores acima
			coeficienteB = calculaB(valores, tempos, mediaValores, mediaTempos);
			coeficienteA = calculaA(mediaValores, mediaTempos, coeficienteB);
			sendCoefficients(wsnMsgResp, coeficienteA, coeficienteB);
		}
	}
	
	/**
	 * Calcula e retorna a média aritmética dos valores reais passados
	 * @param values Array de valores reais de entrada 
	 * @return Média dos valores reais de entrada
	 */
	private double calculaMedia(double[] values)
	{
		double mean = 0, sum = 0;
		for (int i=0; i<values.length; i++)
		{
			sum += values[i];
		}
		if (values.length > 0)
		{
			mean = sum/values.length;
		}
		return mean;
	}
			
	/**
	 * Calcula o coeficiente B da equação de regressão
	 * @param valores Array de valores (grandezas) das medições dos sensores
	 * @param tempos Array de tempos das medições dos sensores
	 * @param mediaValores Média dos valores
	 * @param mediaTempos Média dos tempos
	 * @return Valor do coeficiente B da equação de regressão
	 */
	private double calculaB(double[] valores, double[] tempos, double mediaValores, double mediaTempos)
	{
		double numerador = 0.0, denominador = 0.0, x;
		for (int i = 0; i < tempos.length; i++)
		{
			x = tempos[i] - mediaTempos;
			numerador += x*(valores[i] - mediaValores);
			denominador += x*x;
		}
		if (denominador != 0)
		{
			return (numerador/denominador);
		}
		return 0.0;
	}
	
	/**
	 * Calcula o coeficiente A da equação de regressão
	 * @param mediaValores Média dos valores lidos pelos sensores
	 * @param mediaTempos Média dos tempos de leitura dos valores pelos sensores
	 * @param B Valor do coeficiente B da equação de regressão
	 * @return Valor do coeficiente A
	 */
	private double calculaA(double mediaValores, double mediaTempos, double B)
	{
		return (mediaValores - B*mediaTempos);
	}
	
	private void sendCoefficients(WsnMsgResponse wsnMsgResp, double coeficienteA, double coeficienteB)
	{
		WsnMsg wsnMessage = new WsnMsg(1, this, wsnMsgResp.origem , this, 1, 1, dataSensedType, thresholdError);
		wsnMessage.setCoefs(coeficienteA, coeficienteB);
		wsnMessage.setPathToSenderNode(wsnMsgResp.clonePath());
		sendToNextNodeInPath(wsnMessage);
	}
}
