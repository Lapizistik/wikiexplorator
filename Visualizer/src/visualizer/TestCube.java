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
	double[][][] arr;
	/**
     * create new TestCube Object
     */
	public TestCube()
	{
		arr = new double[getXAxisCount()][getYAxisCount()][getZAxisCount()];
		for (int y = 0; y < getYAxisCount(); y++)
			for (int x = y; x < getXAxisCount(); x++)
				for (int z = 0; z < getZAxisCount(); z++)
				{
					if (x == y)
						arr[x][y][z] = 0;
					else if (z == 0)
					{
						//if (Math.random() > 0.95)
						//	arr[x][y][z] = 1;
						//else
						//	arr[x][y][z] = 0;
						arr[x][y][z] = Math.random();
					}
					else
						arr[x][y][z] = arr[x][y][z-1] + 
							(Math.random() / 20) - 0.025d;
					if (arr[x][y][z] < 0)
						arr[x][y][z] = 0;
					arr[y][x][z] = arr[x][y][z];
				}
	}
    
	/**
     * get the total number of entries of the z-axis
     */
	public int getZAxisCount()
	{
		return 250;
	}
	
	public int getXAxisCount()
	{
		return 20;
	}
	
	public int getYAxisCount()
	{
		return 20;
	}
	
    /**
     * get the double value at a specific position in the cube
     */
    public double getValueAt(int x, int y, int z)
	{
    	return arr[x][y][z];
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
