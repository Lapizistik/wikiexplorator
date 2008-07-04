package visualizer.data;


/**
 * This class represents a two dimensional table of 
 * double values. Methods like getting the title of the
 * table are inherited from the superclass. DataTable
 * only adds methods to retrieve data values from the table.
 * If you want to use this class you have to overwrite
 * the method getValueAt(x, y) so that it delivers the right
 * value at position x/y.
 * Other methods to overwrite are explained in the
 * superclass DataSet!
 * 
 * @author Rene Wegener
 */
public class DataTable extends DataSet
{
	/**
     * create new DataTable Object
     */
    public DataTable()
	{
	}
	
    /**
     * PLEASE OVERWRITE: Get the value at a specific x and y index.
     * Recommended axis
     * usage for working with wikis:
     * x-axis: time
     * y-axis: authors
     */
    public double getValueAt(int x, int y)
	{
		return 0;
	}
}
