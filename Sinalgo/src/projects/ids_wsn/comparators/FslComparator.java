package projects.ids_wsn.comparators;

import java.util.Comparator;

import projects.ids_wsn.enumerators.Order;
import projects.ids_wsn.nodeDefinitions.routing.fuzzy.RoutingField;

/**
 * Comparar em ordem crescente ou decrescente
 * @author marvin
 *
 */
public class FslComparator implements Comparator<RoutingField> {
	
	private Order order;
	
	public FslComparator(Order order){
		this.order = order;
	}

	public int compare(RoutingField r1, RoutingField r2) {
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
	
	private int compareAsc(RoutingField r1, RoutingField r2){
		int resultado = 0;
		if (r1.getFsl() < r2.getFsl()){
			resultado = -1;
		}else if( r1.getFsl() > r2.getFsl()){
			resultado=1;
			
		}else{
			resultado=0;
		}
		
		return resultado;
	}
	
	private int compareDesc(RoutingField r1, RoutingField r2){
		int resultado = 0;
		if (r1.getFsl() > r2.getFsl()){
			resultado = -1;
		}else if( r1.getFsl() < r2.getFsl()){
			resultado=1;
			
		}else{
			resultado=0;
		}
		
		return resultado;
	}

}
