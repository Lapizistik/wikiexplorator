/**
 * 
 */
package wikiVisi;

import java.util.Iterator;

import prefuse.Visualization;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;

/**
 * @author Rene Wegener
 *
 */
public class DataLoader 
{
	public static void loadTable(DataTable dt, AggregateTable at,
			VisualTable vt, VisualTable label, int pixelSize, int textSize) 
    {
        // assumed: authors are on y-axis
        final int authors = dt.getYAxisCount();
        // number ofvalues per author      
        int numberOfValues = dt.getXAxisCount(); 
        // size in pixels needed to show all
        // values of one author as Z or Hilbert Layout
        int squareWidth = Layouts.curveSize(numberOfValues);
        squareWidth *= pixelSize;
        // add all columns
        at.addColumn("index", int.class);
        at.addColumn("author", String.class);
        at.addColumn("xCor", int.class);
        at.addColumn("yCor", int.class);
        at.addColumn("width", int.class);
        at.addColumn("height", int.class);
        at.addColumn("mean", double.class);
        at.addColumn("type", String.class);
        at.addColumn("invisible", boolean.class);
        // first create all Glyphs  
        int currentIndex = 0;
        for (int i = 0; i < authors; i++)
        {
        	AggregateItem newItem = (AggregateItem)at.addItem();
        	newItem.set("index", new Integer(currentIndex));
        	newItem.set("author", dt.getYAxisNameAt(i));
        	newItem.set("xCor", new Integer(0));
        	newItem.set("yCor", new Integer(0 + currentIndex * 50));
        	newItem.set("width", new Integer(squareWidth));
        	// the height is 12 pixels extra for writings
        	newItem.set("height", new Integer(squareWidth + textSize));
        	newItem.set("type", new String("glyph"));
        	newItem.set("included", new Boolean(false));
        	currentIndex++;
        }
        
        // Now create the pixels in the glyphs
        vt.addColumn("xCor", Integer.class);
    	vt.addColumn("yCor", Integer.class);
    	vt.addColumn("type", String.class);
    	vt.addColumn("width", Integer.class);
    	vt.addColumn("height", Integer.class);
    	vt.addColumn("parentIndex", Integer.class);
    	vt.addColumn("value", Double.class);
    	vt.addColumn("color", Double.class);
    	vt.addColumn("desc", String.class);
    	vt.addColumn("index", Integer.class);
    	vt.addColumn("invisible", boolean.class);
        // to create a value for the color that's between
    	// 0 and 1, we need to know the highest value
    	// of all pixels
    	double highest = 0;
    	for (int author = 0; author < authors; author++)
     	{
      	    for (int time = 0; time < numberOfValues; time++)
      	    	if (dt.getValueAt(time, author) > highest)
      	    		highest = dt.getValueAt(time, author);
      	}
    	
    	for (int author = 0; author < authors; author++)
     	{
      	    for (int time = 0; time < numberOfValues; time++)
	       	{
            	VisualItem newItem = vt.addItem();
	           	newItem.set("value", new Double(dt.getValueAt(time, author)));
	           	newItem.set("color", new Double(dt.getValueAt(time, author) / highest));
	            newItem.set("width", new Integer(pixelSize));
	        	newItem.set("height", new Integer(pixelSize));
	        	newItem.set("xCor", new Integer(10));
	        	newItem.set("yCor", new Integer(10));
	        	newItem.set("parentIndex", new Integer(author));
	        	newItem.set("desc", dt.getXAxisNameAt(time));
	        	newItem.set("type", new String("pixel"));
	        	newItem.set("index", new Integer(time));
	        	newItem.set("invisible", new Boolean(false));
	        	at.addToAggregate(author, newItem);
	        }
            // calculate mean value of the glyph
            double mean = 0;
            int numberValues = 0;
            Iterator iter = at.aggregatedTuples(author);
        	while (iter.hasNext())
        	{
        		double d = ((Double)((VisualItem)(iter.next())).get("value")).doubleValue();
        		mean += d;
        		numberValues++;
        	}
        	mean = mean / numberValues;
        	at.getItem(author).set("mean", new Double(mean));
     	}
    	
    	 // create the labeling table
    	 // (although for a data table this doesn't make
    	 // any sense at all)
	     label.addColumn("xCor", int.class);
	     label.addColumn("yCor", int.class);
	     label.addColumn("typ", String.class);
	     label.addColumn("text", String.class);
	     for (int i = 0; i < authors; i++)
	     {
	    	 VisualItem newItem = label.addItem();
	         newItem.set("xCor", new Integer(0));
	         newItem.set("yCor", new Integer(0));
	         newItem.set("typ", "label");
	         newItem.set("text", "");
	     }
    }
	
	
	
	public static void loadCube(DataCube dc, GlyphTable gt,
			VisualTable label, int pixelSize, int textSize) 
    {
        // assumed: authors are on x- and y-axis
		final int xCount = dc.getXAxisCount();
        final int yCount = dc.getYAxisCount();
        final int zCount = dc.getZAxisCount(); 
        // size in pixels needed to show all
        // z values
        int squareWidth = Layouts.curveSize(zCount);
        squareWidth *= pixelSize;
        // add all columns
        gt.addColumn("index", int.class);
        gt.addColumn("author1", String.class);
        gt.addColumn("author2", String.class);
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
    	for (int x = 0; x < xCount; x++)
     	{
      	    for (int y = 0; y < yCount; y++)
      	    	for (int z = 0; z < zCount; z++)
      	    		if (dc.getValueAt(x, y, z) > highest)
      	    			highest = dc.getValueAt(x, y, z);
      	}
    	
        int currentIndex = 0;
        for (int x = 0; x < xCount; x++)
        {
        	for (int y = 0; y < yCount; y++)
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
	        	for (int z = 0; z < zCount; z++)
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
