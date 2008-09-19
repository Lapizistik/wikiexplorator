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
			double random = Math.random();
			int streuung = 20;
			if (author % 4 == 0)
				for (int time = 0; time < getXAxisCount(); time++)
					val[author][time] = 100 - time;// + (int)(Math.random() * streuung);
			else if (author % 4 == 1)
				for (int time = 0; time < getXAxisCount(); time++)
					val[author][time] = time + (int)(Math.random() * streuung);//Math.random();
			else if (author % 4 == 2)
				for (int time = 0; time < getXAxisCount(); time++)
				{
					if (time >= 50)
						val[author][time] = 100 - time;// + (int)(Math.random() * streuung);//Math.random();
					else 
						val[author][time] = time + (int)(Math.random() * streuung);//Math.random();
				}
			else
				for (int time = 0; time < getXAxisCount(); time++)
					val[author][time] = 50 + (int)(Math.random() * streuung);//Math.random();
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
		return 1;
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
    
    public String getYAxisNameAt(int y)
    {
    	return "Autor " + y;
    }
}
