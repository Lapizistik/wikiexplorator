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
		val = new double[getYAxisCount()][getXAxisCount()];
		for (int author = 0; author < getYAxisCount(); author++)
		{
			if (Math.random() > 0.5)
				for (int time = 0; time < getXAxisCount(); time++)
				//if (time < 15)
					val[author][time] = 100 - time;//Math.random();
			else
				for (int time = 0; time < getXAxisCount(); time++)
						//if (time < 15)
					val[author][time] = time;//Math.random();
					
					//else
					//val[time][author] = 20 + Math.random();
		}
	}
	
	/**
     * get the data set's title
     */
    public String getTitle()
	{
		return "Tabelle mit Testwerten";
	}
	
    public String getXAxisTitle()
    {
    	return "X-Achse";
    }
    
    public String getYAxisTitle()
    {
    	return "Y-Achse";
    }
    
    public int getXAxisCount()
	{
		return 100;
	}
	
	public int getYAxisCount()
	{
		return 40;
	}
	
    /**
     * get the double value at a specific position in the cube
     */
    public double getValueAt(int x, int y)
	{
    	return val[y][x];
	}
	
   /**
     * get the name of a specific entry of the z-axis
     */
    public String getXAxisNameAt(int x)
	{
		return "Tag " + x;
	}	
}
