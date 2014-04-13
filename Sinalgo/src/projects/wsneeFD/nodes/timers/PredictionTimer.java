package projects.wsneeFD.nodes.timers;

import projects.wsneeFD.nodes.nodeImplementations.SimpleNode;
import sinalgo.nodes.timers.Timer;

public class PredictionTimer extends Timer {

	private int[] dataTypes;
	private double[] As;
	private double[] Bs;
	private double[] errors;
	
	public PredictionTimer(int[] dataSensedTypes, double[] coefsA, double[] coefsB, double[] maxErrors)
	{
		this.dataTypes = dataSensedTypes;
		this.As = coefsA;
		this.Bs = coefsB;
		this.errors = maxErrors;
	}
	
	@Override
	public void fire() {
		((SimpleNode)node).triggerPrediction(dataTypes, As, Bs, errors);
	}

}
