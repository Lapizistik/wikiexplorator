package visualizer.data;
/**
 * 
 */


import java.util.Iterator;

import prefuse.Visualization;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import visualizer.StringConstants;
import visualizer.display.GlyphTable;
import visualizer.display.Layouts;

/**
 * DataLoader contains two methods to load data from a
 * DataSet into a GlyphTable. The first one, loadTable,
 * gets the data from a DataTable, the second one, loadCube,
 * receives the data from a DataCube.
 * 
 * @author Rene Wegener
 *
 */
public class DataLoader 
{
	/**
	 *
	 *Loads data from a given DataTable object. It will
	 *load the data into a given GlyphTable and a VisualTable
	 *(currently not in use!) for the labels. 
	 */
	public static void loadTable(DataTable dt, GlyphTable gt,
			int pixelSize, int textSize) 
    {
        // size in pixels needed to show all
        // values of one author as Z or Hilbert Layout
        int squareWidth = Layouts.curveSize(dt.getXAxisCount());
        squareWidth *= pixelSize;
        // add all columns
        gt.addColumn("index", int.class);
        gt.addColumn("y-desc", String.class);
        gt.addColumn("xCor", int.class);
        gt.addColumn("yCor", int.class);
        gt.addColumn("mean", double.class);
        gt.addColumn("scaledMean", double.class);
        gt.addColumn("value", double[].class);
        gt.addColumn("scaledValue", double[].class);
        // first create all Glyphs  
        double highest = dt.getValueAt(0, 0);
        for (int x = 0; x < dt.getXAxisCount(); x++)
     	{
      	    for (int y = 0; y < dt.getYAxisCount(); y++)
      	    	if (dt.getValueAt(x, y) > highest)
      	    		highest = dt.getValueAt(x, y);
      	}
    	
    	int currentIndex = 0;
        for (int y = 0; y < dt.getYAxisCount(); y++)
        {
        	VisualItem newItem = gt.addItem();
        	newItem.set("index", new Integer(currentIndex));
        	newItem.set("y-desc", dt.getYAxisNameAt(y));
        	newItem.set("xCor", new Integer(0));
        	newItem.set("yCor", new Integer(0));
        	
        	double[] val, scalVal;
        	double sum = 0, scalMean = 0;
        	val = new double[dt.getXAxisCount()];
        	scalVal = new double[dt.getXAxisCount()];
        	
        	for (int x = 0; x < dt.getXAxisCount(); x++)
        	{
	        	// create pixels for this glyph
	        	// the values for the pixels are assumed to be stored
	        	// in the z-axis
	        	val[x] = dt.getValueAt(x, y);
	        	scalVal[x] = val[x] / highest;
	        	gt.addToDistribution(scalVal[x]);
      	    	sum += val[x];
	        	scalMean += scalVal[x];
		    }
           	sum /= (double)dt.getXAxisCount();
        	scalMean /= (double)dt.getXAxisCount();
           	newItem.set("value", val);
	        newItem.set("scaledValue", scalVal);
	        newItem.set("mean", new Double(sum));
	        newItem.set("scaledMean", new Double(sum));
	        	
	        currentIndex++;
	     }
         
        gt.init(dt.getXAxisCount(), dt.getYAxisCount(), 0);
        // create axis description
        gt.setAxisTitles(dt.getXAxisTitle(), dt.getYAxisTitle());
	    for (int x = 0; x < dt.getXAxisCount(); x++)
	    	gt.setXAxisDescAt(dt.getXAxisNameAt(x), x);
	    for (int y = 0; y < dt.getYAxisCount(); y++)	
	    	gt.setYAxisDescAt(dt.getYAxisNameAt(y), y);
	}
	
	
	/**
	 *
	 *Loads data from a given DataCube object. It will
	 *load the data into a given GlyphTable and a VisualTable
	 *(currently not in use!) for the labels. 
	 */
	public static void loadCube(DataCube dc, GlyphTable gt,
			int pixelSize, int textSize) 
    {
        // size in pixels needed to show all
        // z values
        int squareWidth = Layouts.curveSize(dc.getZAxisCount());
        squareWidth *= pixelSize;
        // add all columns
        gt.addColumn("index", int.class);
        gt.addColumn("x-desc", String.class);
        gt.addColumn("y-desc", String.class);
        gt.addColumn("xCor", int.class);
        gt.addColumn("yCor", int.class);
        gt.addColumn("mean", double.class);
        gt.addColumn("scaledMean", double.class);
        gt.addColumn("value", double[].class);
        gt.addColumn("scaledValue", double[].class);
        // first create all Glyphs  
    	// to create a value for the color that's between
    	// 0 and 1, we need to know the highest value
    	// of all pixels
    	double highest = dc.getValueAt(0, 0, 0);
    	for (int x = 0; x < dc.getXAxisCount(); x++)
     	{
      	    for (int y = 0; y < dc.getYAxisCount(); y++)
      	    	for (int z = 0; z < dc.getZAxisCount(); z++)
      	    		if (dc.getValueAt(x, y, z) > highest && (x != y))
      	    			highest = dc.getValueAt(x, y, z);
      	}
    	  	
        int currentIndex = 0;
        for (int y = 0; y < dc.getYAxisCount(); y++)
        {
        	for (int x = 0; x < dc.getXAxisCount(); x++)
        	{
	        	VisualItem newItem = gt.addItem();
	        	newItem.set("index", new Integer(currentIndex));
	        	newItem.set("x-desc", dc.getXAxisNameAt(x));
	        	newItem.set("y-desc", dc.getYAxisNameAt(y));
	        	newItem.set("xCor", new Integer(0));
	        	newItem.set("yCor", new Integer(0));
	        	// create pixels for this glyph
	        	// the values for the pixels are assumed to be stored
	        	// in the z-axis
	        	double[] val, scalVal;
	        	double sum = 0, scaledMean = 0;
	        	val = new double[dc.getZAxisCount()];
	        	scalVal = new double[dc.getZAxisCount()];
	        	for (int z = 0; z < dc.getZAxisCount(); z++)
		       	{
	            	val[z] = dc.getValueAt(x, y, z);
	        		scalVal[z] = val[z] / highest;
	        		if (x != y)
	        			gt.addToDistribution(scalVal[z]);
	        		sum += val[z];
	        		scaledMean += scalVal[z];
		       	}
	        	sum /= (double)dc.getZAxisCount();
	        	scaledMean /= (double)dc.getZAxisCount();
	        	newItem.set("value", val);
	        	newItem.set("scaledValue", scalVal);
	        	newItem.set("mean", new Double(sum));
	        	newItem.set("scaledMean", new Double(scaledMean));
	        	
	        	currentIndex++;
	     	}// end of for y
        }// end of for x
        
         gt.setAxisTitles(dc.getXAxisTitle(), dc.getYAxisTitle(),
        		dc.getZAxisTitle());
	     // create axis description
	     gt.init(dc.getXAxisCount(), dc.getYAxisCount(), dc.getZAxisCount());
	     for (int x = 0; x < dc.getXAxisCount(); x++)
	     	 gt.setXAxisDescAt(dc.getXAxisNameAt(x), x);
	     for (int y = 0; y < dc.getYAxisCount(); y++)
	     	 gt.setYAxisDescAt(dc.getYAxisNameAt(y), y);
	     for (int z = 0; z < dc.getZAxisCount(); z++)
	     	 gt.setZAxisDescAt(dc.getZAxisNameAt(z), z);
	} // end of loadCube
}
