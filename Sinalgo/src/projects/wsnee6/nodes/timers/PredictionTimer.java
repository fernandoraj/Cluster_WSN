package projects.wsnee6.nodes.timers;

import projects.wsnee6.nodes.nodeImplementations.SimpleNode;
import sinalgo.nodes.timers.Timer;

public class PredictionTimer extends Timer {

	private String dataType;
	private double A;
	private double B;
	private double error;
	
	public PredictionTimer(String dataSensedType, double coefA, double coefB, double maxError)
	{
		this.dataType = dataSensedType;
		this.A = coefA;
		this.B = coefB;
		this.error = maxError;
	}
	
	@Override
	public void fire() {
		((SimpleNode)node).triggerPrediction(dataType, A, B, error);
	}

}
