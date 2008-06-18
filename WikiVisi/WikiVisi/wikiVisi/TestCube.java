/**
 * 
 */
package wikiVisi;

/**
 * @author rene
 *
 */
public class TestCube extends DataCube
{
	/**
     * create new TestCube Object
     */
	public TestCube()
	{
	}
    
	/**
     * get the total number of entries of the z-axis
     */
	public int getZAxisCount()
	{
		return 600;
	}
	
	public int getXAxisCount()
	{
		return 4;
	}
	
	public int getYAxisCount()
	{
		return 4;
	}
	
    /**
     * get the double value at a specific position in the cube
     */
    public double getValueAt(int x, int y, int z)
	{
    	if (x < y)
    		return z;
    	else
    		return (getZAxisCount() - z);
	}
	
    /**
     * get a double array with all values of the z-axis
     * at a specific x and y index
     */
    public double[] getZValuesAt(int x, int y)
	{
    	double[] d = new double[getZAxisCount()]; 
		
    	for (int i = 0; i < getZAxisCount(); i++)
    		d[i] = getValueAt(x, y, i);
    		
    	return d;
	}
	
    /**
     * get the title of the z-axis 
     */
    public String getZAxisTitle()
	{
		return "No Z-Axis Title Yet";
	}
	
    /**
     * get an array with the names of all entries
     * of the z-axis
     */
    public String[] getZAxisNames()
	{
    	String[] names = new String[getZAxisCount()];
    	
    	for (int i = 0; i < getZAxisCount(); i++)
    		names[i] = getZAxisNameAt(i);
    	
    	return names;
	}
	
    /**
     * get the name of a specific entry of the z-axis
     */
    public String getZAxisNameAt(int z)
	{
		return getZAxisNames()[z];
	}	
    
    public String[] getXAxisNames()
	{
		return new String[] {"Achim", "Hans", "Laura",
				"Tanja"};//, "Horst", "Andy", "Stefan", "Georg",
				//"Florian", "Kili"};
	}	
    
    public String[] getYAxisNames()
	{
    	return new String[] {"Achim", "Hans", "Laura",
				"Tanja"};//, "Horst", "Andy", "Stefan", "Georg",
				//"Florian", "Kili"};
	}	
}
