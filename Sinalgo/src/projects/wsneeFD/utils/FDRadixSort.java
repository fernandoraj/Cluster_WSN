package projects.wsneeFD.utils;

import java.util.*;
import java.math.*;

/**
 * Sort a two-dimensional (multidimensional) array of integers using RadixSort algorithm.
 * @author Fernando Rodrigues - fernandorodrigues.ufc@gmail.com - Date: 24/03/2014
 * A two-dimensional integer array (String of example) for test: 10 23 2 5 / 3 15 1 20 / 33 5 18 12 / 10 23 2 1 / 33 5 18 12 / 10 15 1 10 / 33 0 10 1
 * In Eclipse IDE, select the menu option "Run", then select "Run Configurations..." and in the opened window, select the option "Arguments". Finally, put
 * the string (as in the example above) in the "Program arguments" box, then click in "Apply" and in "Run" button. 
 */

public class FDRadixSort {
	
	/**
	 * Class to represents each element (Integer array and a marker to indicate if this element have already been ordered (enqueued) in current time / position)
	 * @author fernando1
	 *
	 */
	static class myElem {
		Integer[] intArray; // Array of integers - indivisible
		Boolean marker; // Marker to indicates if this element have already been ordered // Boolean Markers to set if that position of queue has already been passed by algorithm
	} // end myElem

	static class myLongElem {
		Long[] longArray; // Array of longs - indivisible
		Boolean marker; // Marker to indicates if this element have already been ordered // Boolean Markers to set if that position of queue has already been passed by algorithm
	} // end myLongElem

	static class myBigElem {
		BigInteger[] longArray; // Array of longs - indivisible
		Boolean marker; // Marker to indicates if this element have already been ordered // Boolean Markers to set if that position of queue has already been passed by algorithm
	} // end myBigElem

	
     /**
     * Sort a double Integer array (with two-dimensions) using binary RadixSort
     * @param v Integer array to be ordered
     */
    public static Integer[][] radixSort2(Integer[][] v) {
    	final int sizeRepresentation = 2;
		Queue<myElem> queues[] = new LinkedList[sizeRepresentation];
		//Queue<Boolean> metaQueues[] = new LinkedList[sizeRepresentation]; 
		int integerSize = Integer.SIZE;
		
		//System.out.println("integerSize = "+integerSize);

		// 3 Etapas:
		// 1a) Para todas as linhas (i = 0 -> v.length) pegando a coluna mais a direita (j = v[0].length - 1) e o dígito / algarismo mais a direita (pos = integerSize - 1)
		// 2a) Para todas as linhas (i = 0 -> v.length) da coluna mais a direita (j = v[0].length - 1) pegando os demais dígitos, da direita pra esquerda (pos = integerSize - 2 -> 0)
		// 3a) Para todas as linhas (i = 0 -> v.length) da 2a. coluna mais a direita até a 1a. coluna (j = (v[0].length - 2) -> 0) pegango todos os dígitos, da direita pra esquerda (pos = integerSize - 1 -> 0)

		// 1a) Para todas as linhas (i = 0 -> v.length) pegando a coluna mais a direita (j = v[0].length - 1) e o dígito / algarismo mais a direita (pos = integerSize - 1)

		// pos = 0; // j = v[0].length - 1 // i = 0; i < v.length
		for (int i = 0; i < v.length; i++) { // Para todos as linhas = dataLines
			int pos = (integerSize - 1);
			int j = (v[0].length-1);

//			System.out.println("i = "+i+" j = "+j+" v[i][j] = "+v[i][j]);
			
			int q = queueNo(v[i][j], pos);
			if (q >= 0) {
				if (queues[q] == null) {
					queues[q] = new LinkedList<myElem>();
				}
				myElem me = new myElem();
				me.intArray = v[i];
				me.marker = Boolean.FALSE;
				queues[q].add(me);
			}
		}
		
//		printQueues(queues);

		// 2a) Para todas as linhas (i = 0 -> v.length) da coluna mais a direita (j = v[0].length - 1) pegando os demais dígitos, da direita pra esquerda (pos = integerSize - 2 -> 0)
		
//		for (int pos = 0; pos < integerSize; pos++) {
		for (int pos = integerSize - 2; pos >= 0; pos--) {

//			System.out.println("pos = "+pos);
//			System.out.println("v.length = "+v.length);
			
//			for (int j = v[0].length - 2; j >= 0; j--) { // Para todas as colunas = embed_dim
//			for (int j = 0; j < v[0].length; j++) { // Para todas as colunas = embed_dim
			int j = v[0].length - 1;
			for (int i = 0; i < queues.length; i++) { // Para todos as linhas = dataLines
				if (queues[i] != null) {
					while (!queues[i].isEmpty() && !queues[i].peek().marker) {
						myElem currentElement = queues[i].poll(); // removes and retrieve the head of queue
						if (currentElement != null) {
							//int q = queueNoTen(queues[i].peek()[j] , pos);
							int q = queueNo(currentElement.intArray[j] , pos);
							if (q >= 0) {
//								System.out.println("q = "+q);
								if (queues[q] == null) {
									queues[q] = new LinkedList<myElem>();
								} // end if (queues[q] == null)
								currentElement.marker = Boolean.TRUE; // Marked as already visited
								queues[q].add(currentElement);
//								System.out.println("queues["+q+"].add("+v[i]+")");
							} // end if (q >= 0)
							else {
								System.err.println("Binary conversion error!");
							} // end else of if (q >= 0)
							
						} // end if (currentElement != null)
					
					} // end while (!queues[i].isEmpty() && !queues[i].peek().marker)
				} // end if (queues[i] != null) {
			} // end for (int i = 0; i < queues.length; i++) {
				
		} // end for (int pos = integerSize - 2; pos >= 0; pos--)
		
//		printQueues(queues);

//		System.out.println("* * * Doing element's marker as FALSE!");
		doMarkersAsFalse(queues);
//		System.out.println("& & & Finishing element's marker as FALSE!");
		
//		printQueues(queues);
		
		// 3a) Para todas as linhas (i = 0 -> v.length) da 2a. coluna mais a direita até a 1a. coluna (j = (v[0].length - 2) -> 0) pegango todos os dígitos, da direita pra esquerda (pos = integerSize - 1 -> 0)
		for (int j = v[0].length - 2; j >= 0; j--) { // Para todas as colunas = embed_dim
//			System.out.println("j = "+j);
			for (int pos = integerSize - 1; pos >= 0; pos--) { // Para todas as posições de dígitos
//				System.out.println("pos = "+pos);
				for (int i = 0; i < queues.length; i++) { // Para todos as linhas = dataLines
//					System.out.println("i = "+i);
			
					if (queues[i] != null) {
						while (!queues[i].isEmpty() && !queues[i].peek().marker) {
							myElem currentElement = queues[i].poll(); // removes and retrieve the head of queue
							if (currentElement != null) {
								//int q = queueNoTen(queues[i].peek()[j] , pos);
								int q = queueNo(currentElement.intArray[j] , pos);
								if (q >= 0) {
//											System.out.println("q = "+q);
									if (queues[q] == null) {
										queues[q] = new LinkedList<myElem>();
									} // end if (queues[q] == null)
									currentElement.marker = Boolean.TRUE; // Marked as already visited
									queues[q].add(currentElement);
//											System.out.println("queues["+q+"].add("+v[i]+")");
								} // end if (q >= 0)
								else {
									System.err.println("Binary conversion error!");
								} // end else of if (q >= 0)
								
							} // end if (currentElement != null)
						
						} // end while (!queues[i].isEmpty() && !queues[i].peek().marker)
					} // end if (queues[i] != null) {

				} // end for (int i = 0; i < queues.length; i++)
				doMarkersAsFalse(queues);
			} // end  for (int pos = integerSize - 1; pos >= 0; pos--)
//			printQueues(queues);
			
		} // end for (int j = v[0].length - 2; j >= 0; j--)
		
		restoreMyElem(queues, v);
//		printIntegerArray(v);
		return (v);
	} // end radixSort2(Integer[][] v)

    
    public static Long[][] radixSort2(Long[][] v) {
    	final int sizeRepresentation = 2;
		Queue<myLongElem> queues[] = new LinkedList[sizeRepresentation]; /* ARRAY OF TWO QUEUES FOR THE RADIX SORT */
		int longSize = Long.SIZE;
		//System.out.println("integerSize = "+longSize);

		// 3 Etapas:
		// 1a) Para todas as linhas (i = 0 -> v.length) pegando a coluna mais a direita (j = v[0].length - 1) e o dígito / algarismo mais a direita (pos = integerSize - 1)
		// 2a) Para todas as linhas (i = 0 -> v.length) da coluna mais a direita (j = v[0].length - 1) pegando os demais dígitos, da direita pra esquerda (pos = integerSize - 2 -> 0)
		// 3a) Para todas as linhas (i = 0 -> v.length) da 2a. coluna mais a direita até a 1a. coluna (j = (v[0].length - 2) -> 0) pegango todos os dígitos, da direita pra esquerda (pos = integerSize - 1 -> 0)

		// 1a) Para todas as linhas (i = 0 -> v.length) pegando a coluna mais a direita (j = v[0].length - 1) e o dígito / algarismo mais a direita (pos = integerSize - 1)

		// pos = 0; // j = v[0].length - 1 // i = 0; i < v.length
		for (int i = 0; i < v.length; i++) { // Para todos as linhas = dataLines
			int pos = (longSize - 1);
			int j = (v[0].length-1);

//			System.out.println("i = "+i+" j = "+j+" v[i][j] = "+v[i][j]);
			
			int q = queueNo(v[i][j], pos);
			if (q >= 0) {
				if (queues[q] == null) {
					queues[q] = new LinkedList<myLongElem>();
				}
				myLongElem me = new myLongElem();
				me.longArray = v[i];
				me.marker = Boolean.FALSE;
				queues[q].add(me);
			}
		}
		
//		printQueues(queues);

		// 2a) Para todas as linhas (i = 0 -> v.length) das colunas da 2a. da direita até a mais à esquerda (int j = v[0].length - 2; j >= 0; j--), pegando somente o dígito mais a direita (pos = (longSize - 1))
		
//		for (int pos = 0; pos < integerSize; pos++) {
//		for (int pos = longSize - 2; pos >= 0; pos--) {
		int pos = (longSize - 1);
//			System.out.println("pos = "+pos);
//			System.out.println("v.length = "+v.length);
			
		for (int j = v[0].length - 2; j >= 0; j--) { // Para todas as colunas (= embed_dim) restantes, ou seja, excluindo a primeira -> laço anterior
//			for (int j = 0; j < v[0].length; j++) { // Para todas as colunas = embed_dim
//			int j = v[0].length - 1;
			for (int i = 0; i < queues.length; i++) { // Para todos as linhas = dataLines
				if (queues[i] != null) {
					while (!queues[i].isEmpty() && !queues[i].peek().marker) {
						myLongElem currentElement = queues[i].poll(); // removes and retrieve the head of queue
						if (currentElement != null) {
							//int q = queueNoTen(queues[i].peek()[j] , pos);
							int q = queueNo(currentElement.longArray[j] , pos);
							if (q >= 0) {
//								System.out.println("q = "+q);
								if (queues[q] == null) {
									queues[q] = new LinkedList<myLongElem>();
								} // end if (queues[q] == null)
								currentElement.marker = Boolean.TRUE; // Marked as already visited
								queues[q].add(currentElement);
//								System.out.println("queues["+q+"].add("+v[i]+")");
							} // end if (q >= 0)
							else {
								System.err.println("Binary conversion error!");
							} // end else of if (q >= 0)
							
						} // end if (currentElement != null)
					
					} // end while (!queues[i].isEmpty() && !queues[i].peek().marker)
				} // end if (queues[i] != null) {
			} // end for (int i = 0; i < queues.length; i++) {
				
		} // end for (int j = v[0].length - 2; j >= 0; j--)
		
//		printQueues(queues);

//		System.out.println("* * * Doing element's marker as FALSE!");
		doMarkersLongAsFalse(queues);
//		System.out.println("& & & Finishing element's marker as FALSE!");
		
//		printQueues(queues);
		
		// 3a) Para todas as linhas (i = 0 -> v.length) de todas as colunas (j = (v[0].length - 2) -> 0) pegango os dígitos restantes, da direita pra esquerda (pos = integerSize - 1 -> 0)
		for (pos = longSize - 2; pos >= 0; pos--) { // Para todas as posições de dígitos restantes
//			System.out.println("pos = "+pos);
			for (int j = v[0].length - 1; j >= 0; j--) { // Para todas as colunas = embed_dim
//				System.out.println("j = "+j);
				for (int i = 0; i < queues.length; i++) { // Para todos as linhas = dataLines
//					System.out.println("i = "+i);
			
					if (queues[i] != null) {
						while (!queues[i].isEmpty() && !queues[i].peek().marker) {
							myLongElem currentElement = queues[i].poll(); // removes and retrieve the head of queue
							if (currentElement != null) {
								//int q = queueNoTen(queues[i].peek()[j] , pos);
								int q = queueNo(currentElement.longArray[j] , pos);
								if (q >= 0) {
//											System.out.println("q = "+q);
									if (queues[q] == null) {
										queues[q] = new LinkedList<myLongElem>();
									} // end if (queues[q] == null)
									currentElement.marker = Boolean.TRUE; // Marked as already visited
									queues[q].add(currentElement);
//											System.out.println("queues["+q+"].add("+v[i]+")");
								} // end if (q >= 0)
								else {
									System.err.println("Binary conversion error!");
								} // end else of if (q >= 0)
								
							} // end if (currentElement != null)
						
						} // end while (!queues[i].isEmpty() && !queues[i].peek().marker)
					} // end if (queues[i] != null) {

				} // end for (int i = 0; i < queues.length; i++)
				doMarkersLongAsFalse(queues);
			} // end  for (int pos = integerSize - 1; pos >= 0; pos--)
//			printQueues(queues);
			
		} // end for (int j = v[0].length - 2; j >= 0; j--)
		
		restoreMyLongElem(queues, v);
//		printIntegerArray(v);
		return (v);
	} // end radixSort2(Long[][] v)

    public static BigInteger[][] radixSort2(BigInteger[][] v) {
    	final int sizeRepresentation = 2;
		Queue<myBigElem> queues[] = new LinkedList[sizeRepresentation]; /* ARRAY OF TWO QUEUES FOR THE RADIX SORT */
		int longSize = Long.SIZE;
		//System.out.println("integerSize = "+longSize);

		// 3 Etapas:
		// 1a) Para todas as linhas (i = 0 -> v.length) pegando a coluna mais a direita (j = v[0].length - 1) e o dígito / algarismo mais a direita (pos = integerSize - 1)
		// 2a) Para todas as linhas (i = 0 -> v.length) da coluna mais a direita (j = v[0].length - 1) pegando os demais dígitos, da direita pra esquerda (pos = integerSize - 2 -> 0)
		// 3a) Para todas as linhas (i = 0 -> v.length) da 2a. coluna mais a direita até a 1a. coluna (j = (v[0].length - 2) -> 0) pegango todos os dígitos, da direita pra esquerda (pos = integerSize - 1 -> 0)

		// 1a) Para todas as linhas (i = 0 -> v.length) pegando a coluna mais a direita (j = v[0].length - 1) e o dígito / algarismo mais a direita (pos = integerSize - 1)

		// pos = 0; // j = v[0].length - 1 // i = 0; i < v.length
		for (int i = 0; i < v.length; i++) { // Para todos as linhas = dataLines
			int pos = (longSize - 1);
			int j = (v[0].length-1);

//			System.out.println("i = "+i+" j = "+j+" v[i][j] = "+v[i][j]);
			
			int q = queueNoSort(v[i][j], pos);
			if (q >= 0) {
				if (queues[q] == null) {
					queues[q] = new LinkedList<myBigElem>();
				}
				myBigElem me = new myBigElem();
				me.longArray = v[i];
				me.marker = Boolean.FALSE;
				queues[q].add(me);
			}
		}
		
//		printQueues(queues);

		//* 2a) Para todas as linhas (i = 0 -> v.length) das colunas da 2a. da direita até a mais à esquerda (int j = v[0].length - 2; j >= 0; j--), pegando somente o dígito mais a direita (pos = (longSize - 1))
		//& 2a) Para todas as linhas (i = 0 -> v.length) da coluna mais a direita (j = v[0].length - 1) pegando os demais dígitos, da direita pra esquerda (pos = integerSize - 2 -> 0)
		
//		for (int pos = 0; pos < integerSize; pos++) {
//*		for (int pos = longSize - 2; pos >= 0; pos--) { 
		int pos = (longSize - 1); //&
//			System.out.println("pos = "+pos);
//			System.out.println("v.length = "+v.length);
			
		for (int j = v[0].length - 2; j >= 0; j--) { //& Para todas as colunas (= embed_dim) restantes, ou seja, excluindo a primeira -> laço anterior
//			for (int j = 0; j < v[0].length; j++) { // Para todas as colunas = embed_dim
//*			int j = v[0].length - 1; 
			for (int i = 0; i < queues.length; i++) { // Para todos as linhas = dataLines
				if (queues[i] != null) {
					while (!queues[i].isEmpty() && !queues[i].peek().marker) {
						myBigElem currentElement = queues[i].poll(); // removes and retrieve the head of queue
						if (currentElement != null) {
							//int q = queueNoTen(queues[i].peek()[j] , pos);
							int q = queueNoSort(currentElement.longArray[j] , pos);
							if (q >= 0) {
//								System.out.println("q = "+q);
								if (queues[q] == null) {
									queues[q] = new LinkedList<myBigElem>();
								} // end if (queues[q] == null)
								currentElement.marker = Boolean.TRUE; // Marked as already visited
								queues[q].add(currentElement);
//								System.out.println("queues["+q+"].add("+v[i]+")");
							} // end if (q >= 0)
							else {
								System.err.println("Binary conversion error!");
							} // end else of if (q >= 0)
							
						} // end if (currentElement != null)
					
					} // end while (!queues[i].isEmpty() && !queues[i].peek().marker)
				} // end if (queues[i] != null) {
			} // end for (int i = 0; i < queues.length; i++) {
				
		} // end for (int j = v[0].length - 2; j >= 0; j--)
		
//		printQueues(queues);

//		System.out.println("* * * Doing element's marker as FALSE!");
		doMarkersBigAsFalse(queues);
//		System.out.println("& & & Finishing element's marker as FALSE!");
		
//		printQueues(queues);
		
		//* 3a) Para todas as linhas (i = 0 -> v.length) de todas as colunas (j = (v[0].length - 2) -> 0) pegango os dígitos restantes, da direita pra esquerda (pos = integerSize - 1 -> 0)
		//& 3a) Para todas as linhas (i = 0 -> v.length) da 2a. coluna mais a direita até a 1a. coluna (j = (v[0].length - 2) -> 0) pegango todos os dígitos, da direita pra esquerda (pos = integerSize - 1 -> 0)
//&		for (int j = v[0].length - 2; j >= 0; j--) { //* Para todas as colunas = embed_dim

//&			for (int pos = longSize - 1; pos >= 0; pos--) { //* Para todas as posições de dígitos

		for (pos = longSize - 2; pos >= 0; pos--) { //& Para todas as posições de dígitos restantes
//			System.out.println("pos = "+pos);

			for (int j = v[0].length - 1; j >= 0; j--) { //& Para todas as colunas = embed_dim

				//				System.out.println("j = "+j);
				for (int i = 0; i < queues.length; i++) { // Para todos as linhas = dataLines
//					System.out.println("i = "+i);
			
					if (queues[i] != null) {
						while (!queues[i].isEmpty() && !queues[i].peek().marker) {
							myBigElem currentElement = queues[i].poll(); // removes and retrieve the head of queue
							if (currentElement != null) {
								//int q = queueNoTen(queues[i].peek()[j] , pos);
								int q = queueNoSort(currentElement.longArray[j] , pos);
								if (q >= 0) {
//											System.out.println("q = "+q);
									if (queues[q] == null) {
										queues[q] = new LinkedList<myBigElem>();
									} // end if (queues[q] == null)
									currentElement.marker = Boolean.TRUE; // Marked as already visited
									queues[q].add(currentElement);
//											System.out.println("queues["+q+"].add("+v[i]+")");
								} // end if (q >= 0)
								else {
									System.err.println("Binary conversion error!");
								} // end else of if (q >= 0)
								
							} // end if (currentElement != null)
						
						} // end while (!queues[i].isEmpty() && !queues[i].peek().marker)
					} // end if (queues[i] != null) {

				} // end for (int i = 0; i < queues.length; i++)
				doMarkersBigAsFalse(queues);
			} // end  for (int pos = integerSize - 1; pos >= 0; pos--)
//			printQueues(queues);
			
		} // end for (int j = v[0].length - 2; j >= 0; j--)
		
		restoreMyBigElem(queues, v);
//		printIntegerArray(v);
		return (v);
	} // end radixSort2(BigInteger[][] v)

    
    /**
     * Sort a two-dimensional integer array using base 10
     * @param v Integer array to be ordered
     */
    private static void radixSort3(Integer[][] v) {
    	final int sizeRepresentation = 10;
		Queue<myElem> queues[] = new LinkedList[sizeRepresentation];
		//Queue<Boolean> metaQueues[] = new LinkedList[sizeRepresentation]; // Boolean Markers to set if that position of queue has already been passed by algorithm
		int integerSize = Integer.SIZE;
		//System.out.println("integerSize = "+integerSize);

		// 3 Etapas:
		// 1a) Para todas as linhas (i = 0 -> v.length) pegando a coluna mais a direita (j = v[0].length - 1) e o dígito / algarismo mais a direita (pos = integerSize - 1)
		// 2a) Para todas as linhas (i = 0 -> v.length) da coluna mais a direita (j = v[0].length - 1) pegando os demais dígitos, da direita pra esquerda (pos = integerSize - 2 -> 0)
		// 3a) Para todas as linhas (i = 0 -> v.length) da 2a. coluna mais a direita até a 1a. coluna (j = (v[0].length - 2) -> 0) pegango todos os dígitos, da direita pra esquerda (pos = integerSize - 1 -> 0)

		// 1a) Para todas as linhas (i = 0 -> v.length) pegando a coluna mais a direita (j = v[0].length - 1) e o dígito / algarismo mais a direita (pos = integerSize - 1)

		// pos = 0; // j = v[0].length - 1 // i = 0; i < v.length
		for (int i = 0; i < v.length; i++) { // Para todos as linhas = dataLines
			int pos = (integerSize - 1);
			int j = (v[0].length-1);

//			System.out.println("i = "+i+" j = "+j+" v[i][j] = "+v[i][j]);
			
			int q = queueNoTen(v[i][j], pos);
			if (q >= 0) {
				if (queues[q] == null) {
					queues[q] = new LinkedList<myElem>();
				}
				myElem me = new myElem();
				me.intArray = v[i];
				me.marker = Boolean.FALSE;
				queues[q].add(me);
			}
		}
		
//		printQueues(queues);

		// 2a) Para todas as linhas (i = 0 -> v.length) da coluna mais a direita (j = v[0].length - 1) pegando os demais dígitos, da direita pra esquerda (pos = integerSize - 2 -> 0)
		
//		for (int pos = 0; pos < integerSize; pos++) {
		for (int pos = integerSize - 2; pos >= 0; pos--) {

//			System.out.println("pos = "+pos);
//			System.out.println("v.length = "+v.length);
			
//			for (int j = v[0].length - 2; j >= 0; j--) { // Para todas as colunas = embed_dim
//			for (int j = 0; j < v[0].length; j++) { // Para todas as colunas = embed_dim
			int j = v[0].length - 1;
			for (int i = 0; i < queues.length; i++) { // Para todos as linhas = dataLines
				if (queues[i] != null) {
					while (!queues[i].isEmpty() && !queues[i].peek().marker) {
						myElem currentElement = queues[i].poll(); // removes and retrieve the head of queue
						if (currentElement != null) {
							//int q = queueNoTen(queues[i].peek()[j] , pos);
							int q = queueNoTen(currentElement.intArray[j] , pos);
							if (q >= 0) {
//								System.out.println("q = "+q);
								if (queues[q] == null) {
									queues[q] = new LinkedList<myElem>();
								} // end if (queues[q] == null)
								currentElement.marker = Boolean.TRUE; // Marked as already visited
								queues[q].add(currentElement);
//								System.out.println("queues["+q+"].add("+v[i]+")");
							} // end if (q >= 0)
							else {
								System.err.println("Binary conversion error!");
							} // end else of if (q >= 0)
							
						} // end if (currentElement != null)
					
					} // end while (!queues[i].isEmpty() && !queues[i].peek().marker)
				} // end if (queues[i] != null) {
			} // end for (int i = 0; i < queues.length; i++) {
				
		} // end for (int pos = integerSize - 2; pos >= 0; pos--)
		
//		printQueues(queues);

//		System.out.println("* * * Doing element's marker as FALSE!");
		doMarkersAsFalse(queues);
//		System.out.println("& & & Finishing element's marker as FALSE!");
		
//		printQueues(queues);
		
		// 3a) Para todas as linhas (i = 0 -> v.length) da 2a. coluna mais a direita até a 1a. coluna (j = (v[0].length - 2) -> 0) pegango todos os dígitos, da direita pra esquerda (pos = integerSize - 1 -> 0)
		for (int j = v[0].length - 2; j >= 0; j--) { // Para todas as colunas = embed_dim
//			System.out.println("j = "+j);
			for (int pos = integerSize - 1; pos >= 0; pos--) { // Para todas as posições de dígitos
//				System.out.println("pos = "+pos);
				for (int i = 0; i < queues.length; i++) { // Para todos as linhas = dataLines
//					System.out.println("i = "+i);
			
					if (queues[i] != null) {
						while (!queues[i].isEmpty() && !queues[i].peek().marker) {
							myElem currentElement = queues[i].poll(); // removes and retrieve the head of queue
							if (currentElement != null) {
								//int q = queueNoTen(queues[i].peek()[j] , pos);
								int q = queueNoTen(currentElement.intArray[j] , pos);
								if (q >= 0) {
//											System.out.println("q = "+q);
									if (queues[q] == null) {
										queues[q] = new LinkedList<myElem>();
									} // end if (queues[q] == null)
									currentElement.marker = Boolean.TRUE; // Marked as already visited
									queues[q].add(currentElement);
//											System.out.println("queues["+q+"].add("+v[i]+")");
								} // end if (q >= 0)
								else {
									System.err.println("Binary conversion error!");
								} // end else of if (q >= 0)
								
							} // end if (currentElement != null)
						
						} // end while (!queues[i].isEmpty() && !queues[i].peek().marker)
					} // end if (queues[i] != null) {

				} // end for (int i = 0; i < queues.length; i++)
				doMarkersAsFalse(queues);
			} // end  for (int pos = integerSize - 1; pos >= 0; pos--)
//			printQueues(queues);
			
		} // end for (int j = v[0].length - 2; j >= 0; j--)
		
		restoreMyElem(queues, v);
//		printIntegerArray(v);

	} // end radixSort3(Integer[][] v)

    /**
     * It marks all elements in all queues as "not visited" -> marker = Boolean.FALSE
     * @param queues Queues to be marked
     */
    private static void doMarkersAsFalse(Queue<myElem>[] queues) {
    	for (int i = 0; i < queues.length; i++) { // Para todos as linhas = dataLines
			if (queues[i] != null) {
				while (queues[i].peek() != null && queues[i].peek().marker) {
					myElem currentElement = queues[i].poll(); // removes and retrieve the head of queue
					if (currentElement != null) {
						currentElement.marker = Boolean.FALSE; // Marked as not visited yet
						queues[i].add(currentElement);
//						printIntegerArray(currentElement.intArray);
					} // end if (currentElement != null)
				} // end while (queues[i].peek().marker)
			} // end if (queues[i] != null)
		} // end for (int i = 0; i < queues.length; i++)
    } // end doMarkersAsFalse(Queue<myElem>[] queues)

    /**
     * It marks all elements in all queues as "not visited" -> marker = Boolean.FALSE
     * @param queues Queues to be marked
     */
    private static void doMarkersLongAsFalse(Queue<myLongElem>[] queues) {
    	for (int i = 0; i < queues.length; i++) { // Para todos as linhas = dataLines
			if (queues[i] != null) {
				while (queues[i].peek() != null && queues[i].peek().marker) {
					myLongElem currentElement = queues[i].poll(); // removes and retrieve the head of queue
					if (currentElement != null) {
						currentElement.marker = Boolean.FALSE; // Marked as not visited yet
						queues[i].add(currentElement);
//						printIntegerArray(currentElement.intArray);
					} // end if (currentElement != null)
				} // end while (queues[i].peek().marker)
			} // end if (queues[i] != null)
		} // end for (int i = 0; i < queues.length; i++)
    } // end doMarkersLongAsFalse(Queue<myLongElem>[] queues)

    /**
     * It marks all elements in all queues as "not visited" -> marker = Boolean.FALSE
     * @param queues Queues to be marked
     */
    private static void doMarkersBigAsFalse(Queue<myBigElem>[] queues) {
    	for (int i = 0; i < queues.length; i++) { // Para todos as linhas = dataLines
			if (queues[i] != null) {
				while (queues[i].peek() != null && queues[i].peek().marker) {
					myBigElem currentElement = queues[i].poll(); // removes and retrieve the head of queue
					if (currentElement != null) {
						currentElement.marker = Boolean.FALSE; // Marked as not visited yet
						queues[i].add(currentElement);
//						printIntegerArray(currentElement.intArray);
					} // end if (currentElement != null)
				} // end while (queues[i].peek().marker)
			} // end if (queues[i] != null)
		} // end for (int i = 0; i < queues.length; i++)
    } // end doMarkersBigAsFalse(Queue<myBigElem>[] queues)

    
	private static void restore(Queue<Integer>[] qs, Integer[] v) {
		int contv = 0;
//		System.out.println("qs.length = "+qs.length);
		for (int q = 0; q < qs.length; q++) {
//			System.out.println("qs["+q+"].size() = "+qs[q].size());
			while (qs[q] != null && qs[q].size() > 0) {
				v[contv++] = qs[q].remove();
//				System.out.println("qs["+q+"].size() = "+qs[q].size());
//				System.out.println("restore: v["+contv+"] = qs["+q+"].remove() = "+v[contv-1]);
			}
		}
	} // end restore(Queue<Integer>[] qs, Integer[] v)

	private static void restore2DimInteger(Queue<Integer[]>[] qs, Integer[][] v) {
		int contv = 0;
//		System.out.println("qs.length = "+qs.length);
		for (int q = 0; q < qs.length; q++) {
//			System.out.println("qs["+q+"].size() = "+qs[q].size());
			while (qs[q] != null && qs[q].size() > 0) {
				v[contv++] = qs[q].remove();
//				System.out.println("qs["+q+"].size() = "+qs[q].size());
//				System.out.println("restore: v["+contv+"] = qs["+q+"].remove() = "+v[contv-1]);
			}
		}
	} // end restore2DimInteger(Queue<Integer[]>[] qs, Integer[][] v)
	
	private static void restoreMyElem(Queue<myElem>[] qs, Integer[][] v) {
		int contv = 0;
//		System.out.println("qs.length = "+qs.length);
		for (int q = 0; q < qs.length; q++) {
//			System.out.println("qs["+q+"].size() = "+qs[q].size());
			while (qs[q] != null && qs[q].size() > 0) {
				v[contv++] = qs[q].remove().intArray;
//				System.out.println("qs["+q+"].size() = "+qs[q].size());
//				System.out.println("restore: v["+contv+"] = qs["+q+"].remove() = "+v[contv-1]);
			}
		}
	} // end restoreMyElem(Queue<myElem>[] qs, Integer[][] v)

	private static void restoreMyLongElem(Queue<myLongElem>[] qs, Long[][] v) {
		int contv = 0;
//		System.out.println("qs.length = "+qs.length);
		for (int q = 0; q < qs.length; q++) {
//			System.out.println("qs["+q+"].size() = "+qs[q].size());
			while (qs[q] != null && qs[q].size() > 0) {
				v[contv++] = qs[q].remove().longArray;
//				System.out.println("qs["+q+"].size() = "+qs[q].size());
//				System.out.println("restore: v["+contv+"] = qs["+q+"].remove() = "+v[contv-1]);
			}
		}
	} // end restoreMyLongElem(Queue<myLongElem>[] qs, Long[][] v)

	private static void restoreMyBigElem(Queue<myBigElem>[] qs, BigInteger[][] v) {
		int contv = 0;
//		System.out.println("qs.length = "+qs.length);
		for (int q = 0; q < qs.length; q++) {
//			System.out.println("qs["+q+"].size() = "+qs[q].size());
			while (qs[q] != null && qs[q].size() > 0) {
				v[contv++] = qs[q].remove().longArray;
//				System.out.println("qs["+q+"].size() = "+qs[q].size());
//				System.out.println("restore: v["+contv+"] = qs["+q+"].remove() = "+v[contv-1]);
			}
		}
	} // end restoreMyBigElem(Queue<myBigElem>[] qs, BigInteger[][] v)

	
/*
	private static Queue<Integer>[] createQueues() {
		LinkedList<Integer>[] result = new LinkedList[MAX_CHARS];
		for (int i = 0; i < MAX_CHARS; i++) {
			result[i] = new LinkedList<Integer>();
		}
		return result;
	}
*/
	
	public static int queueNo(Integer intValue, int pos) {
//		System.out.println("The value "+intValue+" as binary is "+strValue);
		String strValue = String.format("%32s", Integer.toBinaryString(intValue)).replace(' ', '0');		

		return queueNo(strValue, pos);
	} // end queueNo(Integer intValue, int pos

	public static int queueNo(String value, int pos) {
		
		if (pos >= value.length()) {
			return 0;
		}
		char ch = value.charAt(pos);
		if (ch == '0') {
			return 0;
		}
		else if (ch == '1') {
			return 1;
		}
		return -1;

	} // end queueNo(String value, int pos)
	
	public static int queueNo(Long intValue, int pos) {
//		System.out.println("The value "+intValue+" as binary is "+strValue);
		String strValue = String.format("%64s", Long.toBinaryString(intValue)).replace(' ', '0');		
		
		return queueNo(strValue, pos);
	} // end queueNo(Long intValue, int pos)

	public static int queueNoSort(BigInteger intValue, int pos) {
//		System.out.println("The value "+intValue+" as binary is "+strValue);
		String strValue = String.format("%64s", Long.toBinaryString(intValue.longValue())).replace(' ', '0');	

		return queueNo(strValue, pos);
	} // end queueNoSort(BigInteger intValue, int pos)

	
	public static int queueNo(BigInteger intValue, int pos) {
//		System.out.println("The value "+intValue+" as binary is "+strValue);
		String strValue = String.format("%64s", Long.toBinaryString(intValue.longValue())).replace(' ', '0');	
		int complementPos = (63 - pos);
/*
		String strValue = Integer.toBinaryString(intValue);
		for (int i = strValue.length(); i < (Integer.SIZE); i++) {
			strValue = "0"+strValue;
		}
*/
//		System.out.println("The new full size binary is "+strValue);
		if (pos >= strValue.length()) {
			return 0;
		}
		char ch = strValue.charAt(complementPos);
		if (ch == '0') {
			return 0;
		}
		else if (ch == '1') {
			return 1;
		}
		return -1;
	} // end queueNo(BigInteger intValue, int pos)

	
	private static int queueNoTen(Integer intValue, int pos) {
//		System.out.println("The value "+intValue+" as binary is "+strValue);
		String strValue = String.format("%32s", intValue).replace(' ', '0');		
/*
		String strValue = Integer.toBinaryString(intValue);
		for (int i = strValue.length(); i < (Integer.SIZE); i++) {
			strValue = "0"+strValue;
		}
*/
//		System.out.println("The new full size binary is "+strValue);
		if (pos >= strValue.length()) {
			return -1;
		}
		char ch = strValue.charAt(pos);
		switch (ch) {
			case '0': 	return 0;
			case '1': 	return 1;
			case '2': 	return 2;
			case '3': 	return 3;
			case '4': 	return 4;
			case '5': 	return 5;
			case '6': 	return 6;
			case '7': 	return 7;
			case '8': 	return 8;
			case '9': 	return 9;
			default:	return -1;
		}
	} // end queueNoTen(Integer intValue, int pos)

	
	public static void printIntegerArray(Integer[] arrToPrint) {
		for (int i = 0; i < arrToPrint.length; i++) {
			System.out.print(arrToPrint[i]+" ");
		}
		System.out.println();
	} // end printIntegerArray(Integer[] arrToPrint)

	public static void printQueues(Queue<myElem>[] queuesToPrint) {
		System.out.println("\nprintQueues - Begin");
		for (int i = 0; i < queuesToPrint.length; i++) {
			if (queuesToPrint[i] == null) {
				continue;
			}
			myElem[] current = queuesToPrint[i].toArray(new myElem[queuesToPrint[i].size()]); //poll();
			for (int l = 0; l < current.length; l++) {
				printIntegerArray(current[l].intArray);
			}
			System.out.println();
		}
		System.out.println("printQueues - End");
	} // end printQueues(Queue<myElem>[] queuesToPrint)
	
	public static void printIntegerArray(Integer[][] arrToPrint) {
		for (int i = 0; i < arrToPrint.length; i++) {
			for (int j = 0; j < arrToPrint[i].length; j++) {
				System.out.print(arrToPrint[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println();
	} // end printIntegerArray(Integer[][] arrToPrint)

	public static void printBigIntegerArray(BigInteger[][] arrToPrint) {
		for (int i = 0; i < arrToPrint.length; i++) {
			for (int j = 0; j < arrToPrint[i].length; j++) {
				System.out.print(arrToPrint[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println();
	} // end printBigIntegerArray(BigInteger[][] arrToPrint)

	
	public static Integer[] converteStrToIntArray(String[] strArray) {
		Integer[] result = new Integer[strArray.length];
		for (int i = 0; i < strArray.length; i++) {
			result[i] = Integer.valueOf(strArray[i]);
		}
		return result;
	} // end converteStrToIntArray(String[] strArray)
	
	public static Integer[][] converteStrToDoubleIntArray(String[] strArray) {
		int contLin = 1; 
		int contcol = 0;
		boolean lineEnd = false;
		for (int i = 0; i < strArray.length; i++) {
			if (strArray[i].equals("/")) {
				contLin++;
				lineEnd = true;
			}
			else if (!lineEnd) {
				contcol++;
			}
		}
		Integer[][] result = new Integer[contLin][contcol];
		int lin = 0;
		int col = 0;
		for (int i = 0; i < strArray.length; i++) {
			if (strArray[i].equals("/")) {
				lin++;
				col = 0;
			}
			else {
				result[lin][col] = Integer.valueOf(strArray[i]);
				col++;
			}
		}
		return result;
	} // end converteStrToDoubleIntArray(String[] strArray)

	public static BigInteger[][] converteStrToBigIntArray(String[] strArray) {
		int contLin = 1; 
		int contcol = 0;
		boolean lineEnd = false;
		for (int i = 0; i < strArray.length; i++) {
			if (strArray[i].equals("/")) {
				contLin++;
				lineEnd = true;
			}
			else if (!lineEnd) {
				contcol++;
			}
		}
		BigInteger[][] result = new BigInteger[contLin][contcol];
		int lin = 0;
		int col = 0;
		for (int i = 0; i < strArray.length; i++) {
			if (strArray[i].equals("/")) {
				lin++;
				col = 0;
			}
			else {
				result[lin][col] = new BigInteger(strArray[i]);
				col++;
			}
		}
		return result;
	} // end converteStrToBigIntArray(String[] strArray)

	/**
	 * @param args Array of strings (set of words) to be sorted (ordered) - Must be passed as parameters
	 */
	public static void main(String[] args) {
//		Integer[] inputIntArr = converteStrToIntArray(args);
		Integer[][] inputIntArr2 = converteStrToDoubleIntArray(args);
//		BigInteger[][] inputBigIntArr = converteStrToBigIntArray(args);

		System.out.print("Input: ");
//		printIntegerArray(inputIntArr);
		printIntegerArray(inputIntArr2);
//		printBigIntegerArray(inputBigIntArr);

//		radixSort(inputIntArr);
		radixSort2(inputIntArr2);
//		radixSort2(inputBigIntArr);
//		radixSort3(inputIntArr2);

		System.out.print("\nOutput: ");
//		printIntegerArray(inputIntArr);
		printIntegerArray(inputIntArr2);
//		printBigIntegerArray(inputBigIntArr);
	}

}
