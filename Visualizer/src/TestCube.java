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
		return 100;
	}
	
	public int getXAxisCount()
	{
		return 10;
	}
	
	public int getYAxisCount()
	{
		return 10;
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
     * get the name of a specific entry of the z-axis
     */
    public String getZAxisNameAt(int z)
	{
		return "Tag " + z;
	}	
}
