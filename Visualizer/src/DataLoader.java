/**
 * 
 */

import java.util.Iterator;

import prefuse.Visualization;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;

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
			VisualTable label, int pixelSize, int textSize) 
    {
        // size in pixels needed to show all
        // values of one author as Z or Hilbert Layout
        int squareWidth = Layouts.curveSize(dt.getXAxisCount());
        squareWidth *= pixelSize;
        // add all columns
        gt.addColumn("index", int.class);
        gt.addColumn("desc", String.class);
        gt.addColumn("xCor", int.class);
        gt.addColumn("yCor", int.class);
        gt.addColumn("mean", double.class);
        gt.addColumn("value", double[].class);
        gt.addColumn("scaledValue", double[].class);
        // first create all Glyphs  
        double highest = 0;
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
        	newItem.set("desc", dt.getYAxisNameAt(y));
        	newItem.set("xCor", new Integer(0));
        	newItem.set("yCor", new Integer(0));
        	
        	double[] val, scalVal;
        	double sum = 0;
        	val = new double[dt.getXAxisCount()];
        	scalVal = new double[dt.getXAxisCount()];
        	
        	for (int x = 0; x < dt.getXAxisCount(); x++)
        	{
	        	// create pixels for this glyph
	        	// the values for the pixels are assumed to be stored
	        	// in the z-axis
	        	val[x] = dt.getValueAt(x, y);
	        	scalVal[x] = val[x] / highest;
	        	sum += val[x];
		    }
           	sum /= (double)dt.getXAxisCount();
        	newItem.set("value", val);
	        newItem.set("scaledValue", scalVal);
	        newItem.set("mean", new Double(sum));
	        	
	        currentIndex++;
	     }
         
         // create z axis description
	     gt.init(ConstantStrings.Data2D, dt.getXAxisCount());
	     for (int x = 0; x < dt.getXAxisCount(); x++)
	     {
	    	 gt.setZDescAt(dt.getXAxisNameAt(x), x);
	     }
    }
	
	
	/**
	 *
	 *Loads data from a given DataCube object. It will
	 *load the data into a given GlyphTable and a VisualTable
	 *(currently not in use!) for the labels. 
	 */
	public static void loadCube(DataCube dc, GlyphTable gt,
			VisualTable label, int pixelSize, int textSize) 
    {
        // size in pixels needed to show all
        // z values
        int squareWidth = Layouts.curveSize(dc.getZAxisCount());
        squareWidth *= pixelSize;
        // add all columns
        gt.addColumn("index", int.class);
        gt.addColumn("desc", String.class);
        gt.addColumn("xCor", int.class);
        gt.addColumn("yCor", int.class);
        gt.addColumn("mean", double.class);
        gt.addColumn("value", double[].class);
        gt.addColumn("scaledValue", double[].class);
        // first create all Glyphs  
    	// to create a value for the color that's between
    	// 0 and 1, we need to know the highest value
    	// of all pixels
    	double highest = 0;
    	for (int x = 0; x < dc.getXAxisCount(); x++)
     	{
      	    for (int y = 0; y < dc.getYAxisCount(); y++)
      	    	for (int z = 0; z < dc.getZAxisCount(); z++)
      	    		if (dc.getValueAt(x, y, z) > highest)
      	    			highest = dc.getValueAt(x, y, z);
      	}
    	
        int currentIndex = 0;
        for (int x = 0; x < dc.getXAxisCount(); x++)
        {
        	for (int y = 0; y < dc.getYAxisCount(); y++)
        	{
	        	VisualItem newItem = gt.addItem();
	        	newItem.set("index", new Integer(currentIndex));
	        	newItem.set("author1", dc.getXAxisNameAt(x));
	        	newItem.set("author2", dc.getYAxisNameAt(y));
	        	newItem.set("xCor", new Integer(0));
	        	newItem.set("yCor", new Integer(0));
	        	// create pixels for this glyph
	        	// the values for the pixels are assumed to be stored
	        	// in the z-axis
	        	double[] val, scalVal;
	        	double sum = 0;
	        	val = new double[dc.getZAxisCount()];
	        	scalVal = new double[dc.getZAxisCount()];
	        	for (int z = 0; z < dc.getZAxisCount(); z++)
		       	{
	            	val[z] = dc.getValueAt(x, y, z);
	        		scalVal[z] = val[z] / highest;
	        		sum += val[z];
		       	}
	        	sum /= (double)dc.getZAxisCount();
	        	newItem.set("value", val);
	        	newItem.set("scaledValue", scalVal);
	        	newItem.set("mean", new Double(sum));
	        	
	        	currentIndex++;
	     	}// end of for y
        }// end of for x
        
         // create the labeling table
	     /*label.addColumn("xCor", int.class);
	     label.addColumn("yCor", int.class);
	     label.addColumn("type", String.class);
	     label.addColumn("text", String.class);
	     label.addColumn("position", String.class);
	     // create headers
	     for (int i = 0; i < dc.getXAxisCount(); i++)
	     {
	    	 VisualItem newItem = label.addItem();
	         newItem.set("xCor", new Integer(0));
	         newItem.set("yCor", new Integer(0));
	         newItem.set("type", "label");
	         newItem.set("position", "header");
	         newItem.set("text", dc.getXAxisNameAt(i));
	     }
	     // create labeling on left side
	     for (int i = 0; i < dc.getYAxisCount(); i++)
	     {
	    	 VisualItem newItem = label.addItem();
	         newItem.set("xCor", new Integer(0));
	         newItem.set("yCor", new Integer(0));
	         newItem.set("type", "label");
	         newItem.set("position", "side");
	         newItem.set("text", dc.getYAxisNameAt(i));
	     }*/
	     // create z axis description
	     gt.init(ConstantStrings.Data3D, dc.getZAxisCount());
	     for (int z = 0; z < dc.getZAxisCount(); z++)
	     {
	    	 gt.setZDescAt(dc.getZAxisNameAt(z), z);
	     }
    } // end of loadCube
}
