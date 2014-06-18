package projects.wsneeFD.nodes.messages;

/**
 * Class (structure) to store important data from sersors readings, like: <p>
 * char type (Type of sensor data, e.g.: t=Temp., h=Hum., l=Lum. or v=Volt.), <br>
 * double value (Absolute value), <br>
 * double time (Date/time from value reading), <br> 
 * double batLevel (Battery power level sensor) and <br> 
 * int round (Round number) 
 * @author Fernando Rodrigues
 * 
 */

public class DataRecord
{
	public int[] typs;
	public double[] values;
	public double time;
	public double batLevel;
	public int round;
} // end dataRecord
