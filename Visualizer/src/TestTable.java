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
	
    /**
     * get the title of the x-axis 
     */
    public String getXAxisTitle()
	{
		return "Zeit";
	}
	
    /**
     * get an array with the names of all entries
     * of the x-axis
     */
    public String[] getXAxisNames()
	{
    	return new String[]{"Tag1", "Tag2", "Tag3", "Tag4",
    			"Tag5", "Tag6", "Tag7"};
	}
	
    /**
     * get the name of a specific entry of the x-axis
     */
    public String getXAxisNameAt(int x)
	{
		return getXAxisNames()[x];
	}
	
    /**
     * get the title of the y-axis 
     */
    public String getYAxisTitle()
	{
		return "Autor";
	}
	
	/**
     * get an array with the names of all entries 
     * of the y-axis
     */
    public String[] getYAxisNames()
	{
      	return new String[]{"Alfred", "Berta", "Christian",
      			"Daniel", "Erick", "Fritz", "Gustaf", "Hans",
      			"Ina", "Jochen"};
	}
	
	/**
    * get the name of a specific entry of the y-axis
    */
    public String getYAxisNameAt(int y)
	{
    	return getYAxisNames()[y];
    }	
	
    /**
     * get the total number of entries of the x-axis
     */
   public int getXAxisCount()
	{
		return getXAxisNames().length;
	}
	
   /**
    * get the total number of entries of the y-axis
    */
    public int getYAxisCount()
	{
		return getYAxisNames().length;
	}	
    
	/**
     * get the value at specific x and y index
     */
    public double getValueAt(int x, int y)
	{
		double[][] d = new double[][]
		    {{1, 2, 3, 4, 5, 6, 7},
		    	{0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75},
		    	{0.1, 0.1, 0.2, 0.2, 0.3, 0.3, 0.4},
		    	{0.1, 0.2, 0.3, 0.2, 0.1, 0.1, 0.2},
		    	{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7},
		    	{0.1, 0.6, 0.3, 0.4, 0.2, 0.6, 0.7},
		    	{0.1, 0.2, 0.3, 0.4, 0.8, 0.6, 0.6},
		    	{0.1, 0.2, 0.4, 0.4, 0.5, 0.3, 0.7},
		    	{0.1, 0.2, 0.3, 0.4, 0.5, 0.4, 0.7},
		    	{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7}};
		return d[y][x];
	}
	
    /**
     * get a double array with the y values of a 
     * specific x index
     */
    public double[] getYValuesAt(int x)
	{
    	double[] d = new double[getYAxisCount()];
		
    	for (int i = 0; i < getYAxisCount(); i++)
    		d[i] = getValueAt(x, i);
    	
    	return d;
	}
	
    /**
     * get a double array with the x values of a 
     * specific y index
     */
    public double[] getXValuesAt(int y)
	{
    	double[] d = new double[getXAxisCount()];
		
    	for (int i = 0; i < getXAxisCount(); i++)
    		d[i] = getValueAt(i, y);
    	
    	return d;
	}
}
