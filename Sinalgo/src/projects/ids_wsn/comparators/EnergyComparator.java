package projects.ids_wsn.comparators;

import java.util.Comparator;

import projects.ids_wsn.enumerators.Order;
import projects.ids_wsn.nodeDefinitions.BasicNode;

/**
 * Comparar em ordem crescente ou decrescente
 * @author marvin
 *
 */
public class EnergyComparator implements Comparator<BasicNode> {
	
	private Order order;
	
	public EnergyComparator(Order order){
		this.order = order;
	}

	public int compare(BasicNode r1, BasicNode r2) {
		int resultado = 0;
		
		switch (order) {
		case ASC:
			resultado = compareAsc(r1, r2);
			break;

		case DESC:
			resultado = compareDesc(r1, r2);			
			break;
		}
		
		return resultado;
		
	}
	
	private int compareAsc(BasicNode r1, BasicNode r2){
		int resultado = 0;
		/*		
		if (r1.getBateria().getEnergy() < r2.getBateria().getEnergy()){
			resultado = -1;
		}else if( r1.getBateria().getEnergy() > r2.getBateria().getEnergy()){
			resultado=1;
			
		}else{
			resultado=0;
		}
		*/
		
		resultado = (r1.getBateria().getEnergy().compareTo(r2.getBateria().getEnergy()));
		
		return resultado;
	}
	
	private int compareDesc(BasicNode r1, BasicNode r2){
		int resultado = 0;
		if (r1.getBateria().getEnergy() > r2.getBateria().getEnergy()){
			resultado = -1;
		}else if( r1.getBateria().getEnergy() < r2.getBateria().getEnergy()){
			resultado=1;
			
		}else{
			resultado=0;
		}
		
		return resultado;
	}

}
