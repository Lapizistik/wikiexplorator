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
		return 100;
	}
	
	public int getXAxisCount()
	{
		return 15;
	}
	
	public int getYAxisCount()
	{
		return 15;
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
     * get the title of the z-axis 
     */
    public String getZAxisTitle()
	{
		return "Time";
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
		return "dd";
	}	
    
    public String getYAxisNameAt(int y)
	{
    	return "aa";
	}	
}
