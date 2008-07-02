/**
 * This class represents a three dimensional set of 
 * double values (think of it as a cube).
 * It extends the DataSet class by some methods for the 
 * z-axis and a method to retrieve the data values
 * from the cube. You will have to overwrite the method
 * getValueAt(x,y,z) so that it delivers the value at 
 * position x/y/z in your cube. In addition you
 * must overwrite getZAxisCount (and getZAxisNameAt(z)
 * is recommended, too).
 * Other methods to overwrite are explained in the
 * superclass DataSet!
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
     * PLEASE OVERWRITE: Get the total number of entries 
     * of the z-axis.
     */
	public int getZAxisCount()
	{
		return 0;
	}
	
    /**
     * PLEASE OVERWRITE: Get the double value at a 
     * specific position in the cube.
     */
    public double getValueAt(int x, int y, int z)
	{
		return 0;
	}
	
    /**
     * get the title of the z-axis 
     */
    public String getZAxisTitle()
	{
		return "No Z-Axis Title Yet";
	}
	
   /**
    * get the name of a specific entry of the z-axis
    */
    public String getZAxisNameAt(int z)
	{
		return "No z axis name at " + z;
	}	
}
