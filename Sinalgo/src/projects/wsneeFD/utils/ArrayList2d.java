package projects.wsneeFD.utils;

import java.util.ArrayList;

/**
 * Class to represents an array (ArrayList) of "MyStructure" objects
 * @author Fernando Rodrigues <a href="mailto:fernandorodrigues.ufc@gmail.com"> e-mail </a>
 *
 * @param <T> Type of the "Key" (fractal dimension - fracDim) of each "MyStructure" object
 * @param <E> Type of the "Elements" from arrayList (cluster) of each "MyStructure" object
 */
public class ArrayList2d<T, E> implements Cloneable
{
	private ArrayList< MyStructure<T, E>> array;
//	private Class<E> eClass;
 
	public ArrayList2d(Class<E> eClass) {
//		this.eClass = eClass;
		array = new ArrayList< MyStructure<T, E>>();
	}
	
	public ArrayList2d() {
		array = new ArrayList< MyStructure<T, E>>();
	}

	/**
	 * Class to represents a structure that stores a key (e.g.: Fractal Dimension as a Double) and an array list of elements (e.g.: a cluster of SimpleNode)
	 * @author Fernando Rodrigues <a href="mailto:fernandorodrigues.ufc@gmail.com"> e-mail </a>
	 *
	 * @param <K> Type of the fractal dimension (fracDim or "Key") of object
	 * @param <V> Type of the elements ("Values") from cluster (ArrayList)
	 */
	class MyStructure<K, V> implements Cloneable {
		K fracDim;
		ArrayList<V> cluster;
		public MyStructure() {
			//fracDim = new K();
			cluster = new ArrayList<V>();
		}
		@Override
		public String toString() {
			return "MyStructure [fracDim=" + fracDim + ", cluster=" + cluster + "]";
		}
		@Override
		public MyStructure<K, V> clone() {
			MyStructure<K, V> cloneObject = new MyStructure<>();
			cloneObject.fracDim = this.fracDim;
			for (int i = 0; i < this.cluster.size(); i++) {
				if (this.cluster != null && this.cluster.get(i) != null) {
					cloneObject.cluster.add(this.cluster.get(i));
				}
				else {
					System.out.println("Error: this.cluster = "+this.cluster);
				}
			}
//			cloneObject.cluster = (ArrayList<V>)this.cluster.clone(); // It could replace the loop "for" above!
			return cloneObject;
		}
	}
	
	/**
	 * Returns the myStructure object of line "row"
	 * @param row Line of myStructure required
	 * @return myStructure object of line required (indicated in "row")
	 */
	public MyStructure<T, E> getMyStructure(int row)
	{
		return array.get(row);
	} // end getMyStructure(int row)

	@Override
	public ArrayList< MyStructure<T, E>> clone() {
		ArrayList< MyStructure<T, E>> cloneArray = new ArrayList< MyStructure<T, E>>();
		for (int i = 0; i < this.array.size() ; i++) {
			if (this.array != null && this.array.get(i) != null) {
				cloneArray.add(this.array.get(i).clone());
			}
		}
		return cloneArray;
	} // end clone()
	
	public ArrayList2d<T, E> clone2() {
		if (this.array == null) {
			return null;
		}
		ArrayList2d<T, E> cloneArray = new ArrayList2d<T, E>();
		for (int i = 0; i < this.array.size() ; i++) {
			if (this.array.get(i) != null) {
				cloneArray.array.add(this.array.get(i).clone());
			}
		}
		return cloneArray;
	} // end clone2()
	
	@Override
	public String toString() {
		String output = "";
		for (int i = 0; i < array.size(); i++) {
			MyStructure<T, E> arrayList = array.get(i);
			output = output+"Line "+i+" "+arrayList+"\n";
		}
		return output;
	} // end toString()

	/**
	 * Ensures a minimum capacity of num rows. Note that this does not guarantee
	 * that there are that many rows.
	 * 
	 * @param num
	 */
	public void ensureCapacity(int num) {
		array.ensureCapacity(num);
	} // end ensureCapacity(int num)
 
	/**
	 * Ensures that the given row has at least the given capacity. Note that
	 * this method will also ensure that getNumRows() >= row
	 * 
	 * @param row
	 * @param num
	 */
	public void ensureCapacity(int row, int num)
	{
		ensureCapacity(row);
		while (row < getNumRows())
		{
			array.add(new MyStructure<T, E>());
		}
//		array.get(row). ensureCapacity(num);
	} // end ensureCapacity(int row, int num)
 
	/**
	 * Adds an item at the end of the specified row. This will guarantee that at least row rows exist.
	 * @deprecated
	 */

//	public void add(T data, int row, double qq)
//	{
//		ensureCapacity(row);
//		while(row >= getNumRows())
//		{
//			MyStructure<E, T> doubleNodesMap = new MyStructure<E, T>();
//			try {
//				Constructor<E> constructor = eClass.getConstructor();
//				E newInstance = constructor.newInstance();
//				
//				doubleNodesMap.put(newInstance, new ArrayList<T>());
//				array.add(doubleNodesMap);
//				
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		if (array.get(row).get(0.0) != null) {
//			array.get(row).get(0.0).add(data);
//		}
//	}

	
	/**
	 * Adds an item at the end of the specified row. This will guarantee that at least row rows exist.<code> ArrayList2d.add</code>
	 * @param data Data item to be add at row indicated
	 * @param row Line number to add the data passed by
	 * @param key The value of "key" (fracDim) of line to be set
	 */
	public void add(E data, int row, T key)
	{
		ensureCapacity(row);
		while (row >= getNumRows())
		{
			MyStructure<T, E> myStruc = new MyStructure<T, E>();
			array.add(myStruc);
//			array.add(new MyStructure<E, T>());
		}
		if (array.get(row) != null) {
			if (array.get(row).cluster == null) {
				array.get(row).cluster = new ArrayList<E>();
			}
			array.get(row).cluster.add(data);
			array.get(row).fracDim = key;
		}
		else {
			MyStructure<T, E> myStruc = new MyStructure<T, E>();
			myStruc.cluster = new ArrayList<E>();
			myStruc.cluster.add(data);
			myStruc.fracDim = key;
			array.set(row, myStruc);
/*
			ArrayList<E> aList = new ArrayList<E>();
			array.get(row).put(key, aList);
			array.get(row).get(key).add(data);
*/
		}
	} // end add(E data, int row, T key)

	/**
	 * Adds an item ("data") at the column ("col") of the specified row ("row"), without guarantee that this col (column number) exists.
	 * @param row Line number to add the data
	 * @param col Column number to add the data
	 * @param data Data to be add
	 */
	public void add(int row, int col, E data)
	{
		if (array.get(row) != null) {
			array.get(row).cluster.add(col, data);
		}
	} // end add(int row, int col, E data)

	/**
	 * Adds an item ("data") at the end of the specified row ("row"), with guarantee that at least row rows exists.
	 * @param data Data to be add
	 * @param row Line number to add the data
	 */
	public void add(E data, int row)
	{
		ensureCapacity(row);
		while (row >= getNumRows())
		{
			array.add(new MyStructure<T, E>());
		}
		if (array.get(row) != null) {
			if (array.get(row).cluster == null) {
				array.get(row).cluster = new ArrayList<E>();
			}
			array.get(row).cluster.add(data);
		}
	} // end add(E data, int row)

	/**
	 * Adds an item ("data") at the end of the specified row ("row"), without any guarantee that this row (line number) exists.
	 * @param row Line number to add the data
	 * @param data Data to be add
	 */
	public void add(int row, E data)
	{
		array.get(row).cluster.add(data);
	} // end add(int row, E data)

	/**
	 * Returns the element at the specified position (row, col) in this list2d, without removing it from list2d
	 * @param row Line number in which the element is 
	 * @param col Column number in which the element is
	 * @return The element required
	 */
	public E get(int row, int col)
	{
		return array.get(row).cluster.get(col);
	}
	
	/**
	 * Returns the element at the specified position (row, col) in this list2d, without removing it from list2d
	 * @param row Line number in which the element is 
	 * @param col Column number in which the element is
	 * @return The element required
	 */
	public ArrayList<E> get(int row)
	{
		return array.get(row).cluster;
	}

	/**
	 * Returns the element at the specified position (row,col) in this list2d, without removing it from list2d
	 * @param row Line number in which the element is 
	 * @param col Column number in which the element is
	 * @return The element required
	 */
/*	// Doesn't matter! - Cause of previous
	public E get(int row, int col, T key)
	{
		return array.get(row).cluster.get(col);
	}
*/
	
	/**
	 * Returns the arrayList of elements at the specified line (row) in this list2d, without removing it from list2d
	 * @param row Line number in which the element is 
	 * @return The arrayList of elements required
	 */
/*
	public ArrayList<E> get(int row, E key)
	{
		return array.get(row).cluster;
	}
*/

	/**
	 * Returns the key of the arrayList of elements at the specified line (row) in this list2d, without removing it from list2d
	 * @param row Line number in which the element is 
	 * @return The key of arrayList of elements required
	 */
/*
	public E getKey(int row)
	{
		TreeSet<E> set = (TreeSet<E>) array.get(row).keySet();
		
		return set.first();
	}
*/
	
	/**
	 * Returns the key of the arrayList of elements at the specified line (row) in this list2d, without removing it from list2d
	 * @param row Line number in which the element is 
	 * @return The key of arrayList of elements required
	 */
	public T getKey(int row) {
	    return array.get(row).fracDim;
	}

	/**
	 * Sets the key of the arrayList of elements at the specified line (row) in this list2d
	 * @param row Line number in which the element is to be set
	 * @return The key of arrayList of elements required
	 */
	public void setKey(int row, T key) {
	    array.get(row).fracDim = key;
	}

	/**
	 * Replaces the element at the specified position (row, col) in this list with the specified element.
	 * @param row Row of data to be set
	 * @param col Column of data to be set
	 * @param data Data to be set
	 */
	public void set(int row, int col, E data)
	{
		if (array.get(row) != null && array.get(row).cluster.get(col) != null) {
			array.get(row).cluster.set(col,data);
		}
	}
 
	/**
	 * Remove the data from column ("col") and row ("row")
	 * @param row Line number
	 * @param col Column number
	 */
	public void remove(int row, int col)
	{
		if (array.get(row) != null && array.get(row).cluster.get(col) != null) {
			array.get(row).cluster.remove(col);
		}
	}
	
	/**
	 * Remove the array of data (all line / myStructure) from row ("row")
	 * @param row Line number
	 * @param col Column number
	 */
	public void remove(int row)
	{
		if (array.get(row) != null) {
			array.remove(row);
		}
	}
	
	/**
	 * Transfer the row indicated ("row") from this object to the "target" object
	 * @param row Line number to be transfered
	 * @param target Object to receive the line from "this" object
	 */
	public void transferRowTo(int row, ArrayList2d<T, E> target)
	{
		target.array.add(array.get(row));
		array.remove(row);
	}
 
	public void move(int row, int colSource, int colDest)
	{
		if (array.get(row) != null && array.get(row).cluster.get(colSource) != null && colDest < array.get(row).cluster.size()) {
			array.get(row).cluster.add(colDest, array.get(row).cluster.remove(colSource)); // To be tested!?
		}
	}

	public boolean contains(E data)
	{
		for (int i = 0; i < array.size(); i++)
		{
			if (array.get(i) != null && array.get(i).cluster.contains(data))
			{
				return true;
			}
		}
		return false;
	}
 
	public boolean containsInRow(E data, int row)
	{
		if (array.get(row) != null && array.get(row).cluster.contains(data))
		{
			return true;
		}
		return false;
	}
	
	public int getNumRows()
	{
		return array.size();
	}
 
	public int getNumCols(int row)
	{
		if (array.get(row) != null) {
			return array.get(row).cluster.size();
		}
		return 0;
	}
}
