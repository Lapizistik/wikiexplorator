package wikiVisi;
/**
 * This is the superclass for the classes DataMatrix and 
 * DataCube. It contains methods for both types of data sets
 * like returning their title.
 * 
 * @author Rene Wegener
 */
public class DataSet 
{
	/**
     * create new DataSet Object
     */
    public DataSet()
	{
	}
	
    /**
     * get the data set's title
     */
    public String getTitle()
	{
		return "No Title Yet";
	}
	
    /**
     * get the title of the x-axis 
     */
    public String getXAxisTitle()
	{
		return "No X-Axis Title Yet";
	}
	
    /**
     * get an array with the names of all entries
     * of the x-axis
     */
    public String[] getXAxisNames()
	{
    	String names[] = new String[getXAxisCount()];
    	
    	for (int i = 0; i < getXAxisCount(); i++)
    		names[i] = getXAxisNameAt(i);
		
    	return names;
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
		return "No Y-Axis Title Yet";
	}
	
	/**
     * get an array with the names of all entries 
     * of the y-axis
     */
    public String[] getYAxisNames()
	{
    	String names[] = new String[getYAxisCount()];
    	
    	for (int i = 0; i < getYAxisCount(); i++)
    		names[i] = getYAxisNameAt(i);
		
    	return names;
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
}
