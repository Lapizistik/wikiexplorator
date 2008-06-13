package wikiVisi;
/**
 * This class represents a normal two dimensional 
 * table of double values.
 * It extends the DataSet class by some methods 
 * specific for this kind of data set.
 * 
 * @author Rene Wegener
 */
public class DataTable extends DataSet
{
	/**
     * create new DataTable Object
     */
    public DataTable()
	{
	}
	
    /**
     * get the value at specific x and y index
     */
    public double getValueAt(int x, int y)
	{
		return getYValuesAt(x)[y];
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
