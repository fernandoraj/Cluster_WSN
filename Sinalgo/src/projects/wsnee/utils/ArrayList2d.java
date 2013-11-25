package projects.wsnee.utils;

import java.util.ArrayList;

public class ArrayList2d<Type>
{
	ArrayList<ArrayList<Type>>	array;
 
	public ArrayList2d()
	{
		array = new ArrayList<ArrayList<Type>>();
	}
 
	/**
	 * Garante a capacidade mínima do número de linhas.Nota-se que isso não garante que existem muitas linhas.<p>  
	 * [Eng]Ensures a minimum capacity of num rows. Note that this does not guarantee
	 * that there are that many rows.
	 * 
	 * @param num
	 */
	public void ensureCapacity(int num){
		array.ensureCapacity(num);
	}
 
	/**Garante que a determinada linha tem ao menos uma determinada capacidade.<p>
	 * [Eng]Ensures that the given row has at least the given capacity. Note that
	 * Esse método irá também garantir a getNumRows() >= row<p>
	 * [Eng]this method will also ensure that getNumRows() >= row
	 * 
	 * @param row
	 * @param num
	 */
	public void ensureCapacity(int row, int num){
		ensureCapacity(row);
		while (getNumRows() <= row){
			array.add(new ArrayList<Type>());
		}
		array.get(row).ensureCapacity(num);
	}
 
	/**Adiciona um item ao fim da linha especificada. Isso irá garantir que ao menos linhas existem.<p>
	 * [Eng]Adds an item at the end of the specified row. This will guarantee that at least row rows exist.
	 */
	public void add(Type data, int row){
		ensureCapacity(row);
		while(getNumRows() <= row){
			array.add(new ArrayList<Type>());
		}
		array.get(row).add(data);
	}
 
	/**
	 * Retorna o elemento à posição especificada(row, col) na lista2d, sem estar removendo da lista2d.<p>
	 * [Eng]Returns the element at the specified position (row,col) in this list2d, without removing it from list2d
	 * @param row Line number in which the element is 
	 * @param col Column number in which the element is
	 * @return The element required
	 */
	public Type get(int row, int col)
	{
		return array.get(row).get(col);
	}
 
	/**
	 * Retorna o arraylist dos elementos da linha especificada(row) na lista2d, sem remover da lista 2d.<p>
	 * Returns the arrayList of elements at the specified line (row) in this list2d, without removing it from list2d.
	 * @param row Line number in which the element is 
	 * @return The arrayList of elements required
	 */
	public ArrayList<Type> get(int row)
	{
		return array.get(row);
	}
	/**
	 * Trocar o elemento da coluna(col) na linha(row) para "data"(novo dado).<p>
	 * [Eng]Change the element of the line to date column.
	 * @param row
	 * @param col
	 * @param data
	 */
	public void set(int row, int col, Type data)
	{
		array.get(row).set(col,data);
	}
 /**
  * Adiciona um elemento novo para a coluna(col) da linha(row).<p>
  * [Eng] Add a new element to the collum(col) in the line(row).
  * @param row
  * @param col
  * @param data
  */
	public void add(int row, int col, Type data)
	{
		array.get(row).add(col,data);
	}
/**
 * Remove coluna(col) da linha(row) passados como parâmetro.<p>
 * [Eng] Remove the collum(col) by the line(row) passed as parameter.
 * @param row
 * @param col
 */
	public void remove(int row, int col)
	{
		array.get(row).remove(col);
	}
	/**
	 * Remove a linha especificada passado como parâmetro<p>
	 * [Eng] Remove the specific line passed as parameter.
	 * @param row
	 */
	public void remove(int row)
	{
		array.remove(row);
	}
	/**
	 * Transfere uma linha(row) de um objeto "A" para um objeto "B"(target).<p>
	 * [Eng] transfers a line)row) by a object "A" to an object "B"(target).
	 * @param row
	 * @param target
	 */
	public void transferRowTo(int row, ArrayList2d<Type> target)
	{
		target.array.add(array.get(row));
		array.remove(row);
	}
 /**
  * Move uma coluna de uma determinada linha(row) para outra coluna( da mesma linha) em um array.<p>
  * [Eng] Move a colummn by a determinated line(row) to another collum(from the same line) in an array.
  * @param row
  * @param colSource
  * @param colDest
  */
	public void move(int row, int colSource, int colDest)
	{
		array.get(row).add(colDest, array.get(row).remove(colSource)); // To be tested!? Be carefull, when the colSource be must bigger then colDest it can became a trouble.
	}
/**
 * Método responsável por saber se o elemento passado como parâmetro existe em todas as linhas.<p>
 * [Eng] Method responsible for whether the passed in element exists in all lines.
 * @param data
 * @return
 */
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
 /**
  * Método responsável por sabe se na linha especificada como parâmetro("i") existe o elemento passado como parâmetro.<p>
  * [Eng]Method responsible for know if the specified as a parameter line ("i") there is the passed in element.
  * @param data
  * @param i
  * @return
  */
	public boolean containsInRow(Type data, int i)
	{
		if (array.get(i).contains(data))
		{
			return true;
		}
		return false;
	}
	/**
	 * Retorna o número de linhas no array.<p>
	 * [Eng]return the number of lines on array.
 	 * @return
	 */
	public int getNumRows()
	{
		return array.size();
	}
 /**
  * Retorna o número de colunas encontrados na linha especificada como parâmetro(row).<p>
  * [Eng] Return the number of collums finded in the specified line as parameter(row).
  * @param row
  * @return
  */
	public int getNumCols(int row)
	{
		return array.get(row).size();
	}
}