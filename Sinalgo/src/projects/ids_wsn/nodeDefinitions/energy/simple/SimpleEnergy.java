package projects.ids_wsn.nodeDefinitions.energy.simple;

import java.util.Hashtable;

import projects.ids_wsn.nodeDefinitions.energy.EnergyMode;
import projects.ids_wsn.nodeDefinitions.energy.IEnergy;
import sinalgo.tools.Tools;

public class SimpleEnergy implements IEnergy {
	
	private Float sleep;
	private Float transmission;
	private Float receive;
	private Float processing;
	private Float listen;
	private Float totalEnergy;
	
	private Hashtable<Double, Float> energyPerRound = new Hashtable<Double, Float>();
	
	public Hashtable<Double, Float> getEnergyPerRound(){
		return energyPerRound;
	}
	
	public SimpleEnergy (){
		this.sleep = Float.valueOf(0);
		this.transmission = Float.valueOf(0);
		this.receive = Float.valueOf(0);
		this.processing = Float.valueOf(0);
		this.listen = Float.valueOf(0);
		this.totalEnergy = 80000f;
	}
	
	public Float getTotalSpentEnergy(){
		return sleep + transmission + receive + processing + listen;
	}
	
	private void calculateEnergyPerRound(Float value){
		Double round = Tools.getGlobalTime();
		if (energyPerRound.containsKey(round)){
			Float tmp = energyPerRound.get(round);
			energyPerRound.put(round, tmp + value);
		}else{
			energyPerRound.put(round, value);
		}
	}
	
	public void spend(EnergyMode mode){
		switch (mode) {
		case LISTEN:
			listen += Config.ENERG_ESCUTA;
			calculateEnergyPerRound(Config.ENERG_ESCUTA);
			break;
		case RECEIVE:
			receive += Config.ENERG_RECEPCAO;
			calculateEnergyPerRound(Config.ENERG_RECEPCAO);
			break;
		case SEND:
			transmission += Config.ENERG_TRANSMISSAO;
			calculateEnergyPerRound(Config.ENERG_TRANSMISSAO);
			break;
		case SLEEP:
			sleep += Config.ENERG_SLEEP;
			calculateEnergyPerRound(Config.ENERG_SLEEP);
			break;
		case PROCESSING:
			processing += 0;
			calculateEnergyPerRound(0f);
			break;
		case MONITOR:			
			break;
		default:
			break;
		}		
	}

	public Float getEnergy() {
		return totalEnergy - getTotalSpentEnergy();
	}
	
	public Float getInitialEnergy(){
		return this.totalEnergy;
	}

	@Override
	public void setTotalEnergy(Float totalEnergy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restarEnergySpentPerRound() {
		// TODO Auto-generated method stub
		
	}
}
