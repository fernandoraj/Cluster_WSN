package projects.wsneeFD.utils;

import java.util.ArrayList;

import projects.wsneeFD.nodes.nodeImplementations.SimpleNode;

public class ArrayList2d<T, E>
{
//	ArrayList<MyStructure<Type>> array;
//	ArrayList<ArrayList<Type>> array;
	private ArrayList< MyStructure<T, E>> array;
//	private ArrayList<Map<E, ArrayList<T>>> array;
//	private Class<E> eClass;
 
	public ArrayList2d(Class<E> eClass) {
//		this.eClass = eClass;
		array = new ArrayList< MyStructure<T, E>>();
	}
	
	public ArrayList2d() {
		array = new ArrayList< MyStructure<T, E>>();
	}

	class MyStructure<K, V> {
		K fracDim;
		ArrayList<V> cluster;
		@Override
		public String toString() {
			return "MyStructure [fracDim=" + fracDim + ", cluster=" + cluster + "]";
		}
	}
	
	public MyStructure<T, E> getMyStructure(int row)
	{
		return array.get(row);
	}

	
	
	@Override
	public String toString() {
		String output = "";
		for (int i = 0; i < array.size(); i++) {
			MyStructure<T, E> arrayList = array.get(i);
			output = output+arrayList+"\n";
		}
		return output;
	}

	/**
	 * ensures a minimum capacity of num rows. Note that this does not guarantee
	 * that there are that many rows.
	 * 
	 * @param num
	 */
	public void ensureCapacity(int num) {
		array.ensureCapacity(num);
	}
 
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
	}
 
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

	public void add(int row, int col, E data)
	{
		if (array.get(row) != null) {
			array.get(row).cluster.add(col, data);
		}
	}

	/**
	 * Adds an item ("data") at the end of the specified row ("row"), with guarantee that at least row rows exist.
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
	}

	/**
	 * Adds an item ("data") at the end of the specified row ("row"), without guarantee that this row (line number) exist.
	 * @param row Line number to add the data
	 * @param data Data to be add
	 */
	public void add(int row, E data)
	{
		array.get(row).cluster.add(data);
	}

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
 
	public void remove(int row, int col)
	{
		if (array.get(row) != null && array.get(row).cluster.get(col) != null) {
			array.get(row).cluster.remove(col);
		}
	}
	
	public void remove(int row)
	{
		if (array.get(row) != null) {
			array.remove(row);
		}
	}
	
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