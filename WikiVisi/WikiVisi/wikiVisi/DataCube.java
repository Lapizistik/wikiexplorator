package wikiVisi;
/**
 * This class represents a three dimensional 
 * set of double values (think of it as a cube).
 * It extends the DataSet class by some methods 
 * specific for this kind of data set.
 * 
 * @author Rene Wegener
 */
public class DataCube extends DataSet 
{
	/**
     * create new DataCube Object
     */
	public DataCube()
	{
	}
    
	/**
     * get the total number of entries of the z-axis
     */
	public int getZAxisCount()
	{
		return getZAxisNames().length;
	}
	
    /**
     * get the double value at a specific position in the cube
     */
    public double getValueAt(int x, int y, int z)
	{
		return getZValuesAt(x, y)[z];
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
}
