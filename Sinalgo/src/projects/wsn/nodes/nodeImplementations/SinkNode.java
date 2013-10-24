package projects.wsn.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

import projects.wsn.nodes.messages.WsnMsg;
import projects.wsn.nodes.messages.WsnMsgResponse;
import projects.wsn.nodes.timers.WsnMessageTimer;
import projects.wsn.utils.Utils;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;

public class SinkNode extends SimpleNode 
{

	/**
	 * Numero de dados sensoriados por time slot (Tamanho do time slot) <p> 
	 * [Eng] Sensed data number per slot (size of the time slot)
	 */
	private Integer sizeTimeSlot = 100;
	
	/**
	 * Tipo de dado a ser sensoriado (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=umidade, "l"=luminosidade ou "v"=voltagem<p>
	 * [Eng] Data type to be sensed (read in the sensor nodes), it can be: "t"=temperature, "h"=humidity, "l"=luminosity or "v"=voltage
	 */
	private String dataSensedType = "t"; // type (abbrev.)
	
	/**
	 * Percentual do limiar de erro aceitável para as leituras dos nós sensores, que pode estar entre 0.0 (não aceita erros) e 1.0 (aceita todo e qualquer erro)<p>
	 * [Eng] Percentage of the possible threshold error to the sensor nodes readings, it can be between 0.0 (don't accept errors) and 1.0 (accept all and any errors)
	 */
	private double thresholdError = 0.05; // te (abbrev.)
	
	/**
	 * Indica que tipo de aproximação o sink node deve sinalizar que todos os outros nós devem seguir (Adaga-P*, continuamente sensorando-e-enviando (naive sabendo ou não os tempos iniciais) <p>
	 * [Eng] Indicates what type of approach the sink node should signal to all other nodes must follow (Adaga-P*, continuously sensing-and-send (naive with or without learning initial time))
	 */
	private Integer approachType = 0; // 0(default) = temporal correlation (Adaga-P*); 2 = Naive
	
	public SinkNode()
	{
		super();
		this.setColor(Color.RED);
		Utils.printForDebug("The size of time slot is "+sizeTimeSlot);
		Utils.printForDebug("The type of data sensed is "+dataSensedType);
		Utils.printForDebug("The threshold of error (max error) is "+thresholdError);
		Utils.printForDebug("The size of sliding window is "+SimpleNode.slidingWindowSize);
		Utils.printForDebug("The size of delay to send novelties is "+SimpleNode.limitPredictionError);
		Utils.printForDebug("The approach type is "+approachType+" (0 = temporal correlation (Adaga-P*); 2 = Naive)");
		
//		if(LogL.ROUND_DETAIL){
			Global.log.logln("\nThe size of time slot is "+sizeTimeSlot);
			Global.log.logln("The type of data sensed is "+dataSensedType);
			Global.log.logln("The threshold of error (max error) is "+thresholdError);
			Global.log.logln("The size of sliding window is "+SimpleNode.slidingWindowSize);
			Global.log.logln("The size of delay to send novelties is "+SimpleNode.limitPredictionError+"\n");
			Global.log.logln("The approach type is "+approachType+" (0 = temporal correlation (Adaga-P*); 2 = Naive)");
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
		WsnMsg wsnMessage = new WsnMsg(1, this, null, this, approachType, sizeTimeSlot, dataSensedType); 
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
				receiveMessage(wsnMsgResp, wsnMsgResp.sizeTimeSlot, wsnMsgResp.dataSensedType);
			} //if (message instanceof WsnMsg)
		} //while (inbox.hasNext())
	} //public void handleMessages
	
	/**
	 * Recebe a mensagem passada. Caso a abordagem utilizada seja a de correlação
	 * temporal (Adaga-P*), lê os parâmetros (itens) no dataRecordItens,
	 * calcula os coeficientes A e B de acordo com estes parâmetros e envia tais
	 * coeficientes para o nó sensor de origem; Caso contrário (Aborbagem naive ->
	 * approachType = 2), "descarta" a mensagem.
	 * <p>
	 * [Eng] Receives the message, reads the parameters (items) in
	 * dataRecordItens, calculates the coefficients A and B according to these
	 * parameters and sends these coefficients for the sensor node of origin
	 * @param wsnMsgResp Mensagem recebida com os parâmetros a serem lidos <p> [Eng] <b>wsnMsgResp</b> Received message with the param to be read
	 * @param sizeTimeSlot Tamanho do vetor de dados (série temporal) da mensagem <p> [Eng] <b>sizeTimeSlot</b> Size of the data array (time serie) of the message
	 * @param dataSensedType Tipo de dados (da série temporal) da mensagem <p> [Eng] <b>dataSensedType</b> Data type (of the time serie) of the message
	 */
	private void receiveMessage(WsnMsgResponse wsnMsgResp, Integer sizeTimeSlot, String dataSensedType)
	{
		if (approachType == 2){ // Abordagem Naive
			// Tratar mensagem recebida com dados do sensor
		}
		else if (wsnMsgResp != null && wsnMsgResp.dataRecordItens != null){
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
	 * Calcula e retorna a média aritmética dos valores reais passados <p>
	 * [Eng] Calculate and return the arithmetic average of the passed double values
	 * @param values Array de valores reais de entrada <p> [Eng] <b>values</b> Array of the input double values 
	 * @return Média dos valores reais de entrada <p> [Eng] Average of the input double values
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
	 * Calcula o coeficiente B da equação de regressão <p>
	 * [Eng] Calculate the coefficient B of the regression equation
	 * @param valores Array de valores (grandezas) das medições dos sensores <p> [Eng] <b>valores</b> Array of values of the sensor measures
	 * @param tempos Array de tempos das medições dos sensores <p> [Eng] <b>tempos</b> Array of times of the sensor measures
	 * @param mediaValores Média dos valores <p> [Eng] <b>mediaValores</b> Average of the values
	 * @param mediaTempos Média dos tempos <p> [Eng] <b>mediaTempos</b> Average of the times
	 * @return Valor do coeficiente B da equação de regressão <p> [Eng] Value of the B coefficient of the regression equation
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
	 * Calcula o coeficiente A da equação de regressão <p>
	 * [Eng] Calculate the A coefficient of the regression equation
	 * @param mediaValores Média dos valores lidos pelos sensores <p> [Eng] <b>mediaValores</b> Average of the values read by the sensors
	 * @param mediaTempos Média dos tempos de leitura dos valores pelos sensores <p> [Eng] <b>mediaTempos</b> Average of the times of value readings by the sensors
	 * @param B Valor do coeficiente B da equação de regressão <p> [Eng] <b>B</b> Value of the coefficient B of the regression equation
	 * @return Valor do coeficiente A <p> [Eng] Value of the A coefficient
	 */
	private double calculaA(double mediaValores, double mediaTempos, double B)
	{
		return (mediaValores - B*mediaTempos);
	}
	
	private void sendCoefficients(WsnMsgResponse wsnMsgResp, double coeficienteA, double coeficienteB)
	{
		WsnMsg wsnMessage = new WsnMsg(1, this, wsnMsgResp.source , this, 1, 1, dataSensedType, thresholdError);
		wsnMessage.setCoefs(coeficienteA, coeficienteB);
		wsnMessage.setPathToSenderNode(wsnMsgResp.clonePath());
		sendToNextNodeInPath(wsnMessage);
	}
}
