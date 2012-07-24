package projects.ids_wsn.comparators;

import java.util.Comparator;

import projects.ids_wsn.nodeDefinitions.Monitor.DataMessage;

public class IntervalComparator implements Comparator<DataMessage> {
	
	private Integer clockMin = 2;
	private Integer clockMax = 4;
	
	public IntervalComparator() {}
	
	public IntervalComparator(Integer min, Integer max){
		this.clockMin = min;
		this.clockMax = max;
	}

	/**
	 * Comparar se duas mensagens estão dentro do intervalo máximo permitido.
	 * return 0 - mensagem ok; 
	 * return -1 - violou o intervalo minimo;
	 * return 1 - violou o intervalo máximo
	 */	
	public int compare(DataMessage d1, DataMessage d2) {
		int resultado = 0;
		
		Integer id1 = d1.getIdMsg();
		Integer id2 = d2.getIdMsg();
		Integer clock1;
		Integer clock2;
		Integer difClock;
		
		if (id2 > id1){
			//We have to check if id2 is id1 plus 1 (In this case, d2 is a sequencial message of d1)
			if ((id1 + 1) == id2){
				clock1 = d1.getClock();
				clock2 = d2.getClock();
				
				difClock = clock2 - clock1;
				
				if (difClock < clockMin ){
					resultado = -1;
				}else if (difClock > clockMax){
					//resultado = 1; ---- We have to define better what is the best Max Value
				}
			}
		}else if (id1 > id2){ //Source sent the message out of order
			//TODO: do something when the source sends messages out of order
		}
		
		return resultado;
	}

}
