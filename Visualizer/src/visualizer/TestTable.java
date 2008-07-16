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
	double[][] val;
	
	public TestTable()
	{
		val = new double[getXAxisCount()][getYAxisCount()];
		for (int author = 0; author < getYAxisCount(); author++)
		{
			double d = Math.random();
			if (d > 0.9)
				for (int time = 0; time < getXAxisCount(); time++)
					val[time][author] = 0.9 + Math.random() / 10;
			else if (d > 0.7)
				for (int time = 0; time < getXAxisCount(); time++)
					val[time][author] = Math.random() / 3;
			else if (d > 0.4)
				for (int time = 0; time < getXAxisCount(); time++)
					val[time][author] = (double)time / (double)getXAxisCount() + Math.random() / 5;
			else
				for (int time = 0; time < getXAxisCount(); time++)
					val[time][author] = 1 - (double)time / (double)getXAxisCount() + Math.random() / 5;
		}
	}
	
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
    	return val[x][y];
	}
	
   /**
     * get the name of a specific entry of the z-axis
     */
    public String getXAxisNameAt(int x)
	{
		return "Tag " + x;
	}	
}
