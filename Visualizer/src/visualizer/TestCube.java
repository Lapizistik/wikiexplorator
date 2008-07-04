package visualizer;

import visualizer.data.DataCube;

/**
 * 
 */

/**
 * Implementation of a DataCube for testing purposes.
 * 
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
		return 300;
	}
	
	public int getXAxisCount()
	{
		return 60;
	}
	
	public int getYAxisCount()
	{
		return 60;
	}
	
    /**
     * get the double value at a specific position in the cube
     */
    public double getValueAt(int x, int y, int z)
	{
    	return (x * y * z);
	}
	
   /**
     * get the name of a specific entry of the z-axis
     */
    public String getZAxisNameAt(int z)
	{
		return "Tag " + z;
	}
    
    public String getXAxisNameAt(int x)
    {
    	if (x < 5)
    		return "Autor Nr. " + x;
    	else if (x < 12)
    		return "Autorin Nr. " + x;
    	else
    		return "MitarbeiterIn " + x;
    }
    
    public String getYAxisNameAt(int y)
    {
    	return getXAxisNameAt(y);
    }
}
