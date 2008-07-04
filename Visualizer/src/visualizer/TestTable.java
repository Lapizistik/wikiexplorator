package visualizer;

import visualizer.data.DataTable;

/**
 * 
 */

/**
 * Implementation of a DataTable for testing purposes.
 * 
 * @author rene
 *
 */
public class TestTable extends DataTable
{
	/**
     * get the data set's title
     */
    public String getTitle()
	{
		return "Tabelle mit Testwerten";
	}
	
    public int getXAxisCount()
	{
		return 200;
	}
	
	public int getYAxisCount()
	{
		return 50;
	}
	
    /**
     * get the double value at a specific position in the cube
     */
    public double getValueAt(int x, int y)
	{
    	return x * y;
	}
	
   /**
     * get the name of a specific entry of the z-axis
     */
    public String getXAxisNameAt(int x)
	{
		return "Tag " + x;
	}	
}
