package projects.wsnee6.utils;

import java.util.ArrayList;

public class ArrayList2d<Type>
{
	ArrayList<ArrayList<Type>> array;
 
	public ArrayList2d()
	{
		array = new ArrayList<ArrayList<Type>>();
	}
 
	/**
	 * ensures a minimum capacity of num rows. Note that this does not guarantee
	 * that there are that many rows.
	 * 
	 * @param num
	 */
	public void ensureCapacity(int num)
	{
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
			array.add(new ArrayList<Type>());
		}
		array.get(row).ensureCapacity(num);
	}
 
	/**
	 * Adds an item at the end of the specified row. This will guarantee that at least row rows exist.
	 */
	public void add(Type data, int row)
	{
		ensureCapacity(row);
		while(row >= getNumRows())
		{
			array.add(new ArrayList<Type>());
		}
		array.get(row).add(data);
	}
 
	/**
	 * Returns the element at the specified position (row,col) in this list2d, without removing it from list2d
	 * @param row Line number in which the element is 
	 * @param col Column number in which the element is
	 * @return The element required
	 */
	public Type get(int row, int col)
	{
		return array.get(row).get(col);
	}
 
	/**
	 * Returns the arrayList of elements at the specified line (row) in this list2d, without removing it from list2d
	 * @param row Line number in which the element is 
	 * @return The arrayList of elements required
	 */
	public ArrayList<Type> get(int row)
	{
		return array.get(row);
	}
	
	public void set(int row, int col, Type data)
	{
		array.get(row).set(col,data);
	}
 
	public void add(int row, int col, Type data)
	{
		array.get(row).add(col,data);
	}

	public void remove(int row, int col)
	{
		array.get(row).remove(col);
	}
	
	public void remove(int row)
	{
		array.remove(row);
	}
	
	public void transferRowTo(int row, ArrayList2d<Type> target)
	{
		target.array.add(array.get(row));
		array.remove(row);
	}
 
	public void move(int row, int colSource, int colDest)
	{
		array.get(row).add(colDest, array.get(row).remove(colSource)); // To be tested!?
	}

	public boolean contains(Type data)
	{
		for (int i = 0; i < array.size(); i++)
		{
			if (array.get(i).contains(data))
			{
				return true;
			}
		}
		return false;
	}
 
	public boolean containsInRow(Type data, int i)
	{
		if (array.get(i).contains(data))
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
		return array.get(row).size();
	}
}