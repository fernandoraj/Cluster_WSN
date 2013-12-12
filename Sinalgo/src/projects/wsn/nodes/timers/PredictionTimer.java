package projects.wsn.nodes.timers;

import projects.wsn.nodes.nodeImplementations.SimpleNode;
import sinalgo.nodes.timers.Timer;

public class PredictionTimer extends Timer {

	private String dataType;
	private double A;
	private double B;
	private double error;
	private int contVersion;
	
	public PredictionTimer(String dataSensedType, double coefA, double coefB, double maxError, int version)
	{
		this.dataType = dataSensedType;
		this.A = coefA;
		this.B = coefB;
		this.error = maxError;
		this.contVersion = version;
	}
	
	@Override
	public void fire() {
		((SimpleNode)node).triggerPrediction(dataType, A, B, error, contVersion);
	}

}
