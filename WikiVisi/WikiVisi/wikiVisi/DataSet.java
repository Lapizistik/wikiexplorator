package wikiVisi;
/**
 * This is the superclass for the classes DataMatrix and 
 * DataCube. It doesn't contain the methods to actually get
 * values from the data set so you will have to use the 
 * extending classes DataTable and DataCube.
 * If you use one of the classes DataTable or DataCube
 * you will at least have to overwrite the methods
 * getXAxisCount and getYAxisCount. Of course it is
 * recommended to overwrite the methods for the titles of the
 * table and the axis titles and antries as well.
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
     * get the name of a specific entry of the x-axis
     */
    public String getXAxisNameAt(int x)
	{
		return "No x axis name at " + x;
	}
	
    /**
     * get the title of the y-axis 
     */
    public String getYAxisTitle()
	{
		return "No Y-Axis Title Yet";
	}
	
	/**
    * get the name of a specific entry of the y-axis
    */
    public String getYAxisNameAt(int y)
	{
    	return "No y axis name at " + y;
    }	
	
    /**
     * get the total number of entries of the x-axis
     */
    public int getXAxisCount()
	{
		return 0;
	}
	
   /**
    * get the total number of entries of the y-axis
    */
    public int getYAxisCount()
	{
		return 0;
	}	
}
