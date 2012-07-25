package projects.ids_wsn.nodeDefinitions.energy;

public enum EnergyMode{
	LISTEN(0.050f), 
	MONITOR(0f), 
	PROCESSING(0f),
	RECEIVE(0.80f), 
	SEND(0.48375f),
	SLEEP(0f); 
		
	Float energySpent;

	private EnergyMode(Float energySpent) {
		this.energySpent = energySpent;
	}
	
	public Float getEnergySpent() {
		return energySpent;
	}
}