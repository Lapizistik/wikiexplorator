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
 * @author rene
 *
 */
public class DataLoader 
{
	public static void loadTable(DataTable dt, AggregateTable at,
			VisualTable vt, int pixelSize, int textSize) 
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
        	currentIndex++;
        }
        
        // Now create the pixels in the glyphs
        vt.addColumn("xCor", Integer.class);
    	vt.addColumn("yCor", Integer.class);
    	vt.addColumn("type", String.class);
    	vt.addColumn("width", Integer.class);
    	vt.addColumn("height", Integer.class);
    	vt.addColumn("value", Double.class);
    	vt.addColumn("color", Double.class);
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
	        	newItem.set("type", new String("pixel"));
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
    }
	
	
	
	public static void loadCube(DataCube dc, AggregateTable at,
			VisualTable vt, int pixelSize, int textSize) 
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
        at.addColumn("index", int.class);
        at.addColumn("author1", String.class);
        at.addColumn("author2", String.class);
        at.addColumn("author", String.class);
        at.addColumn("xCor", int.class);
        at.addColumn("yCor", int.class);
        at.addColumn("width", int.class);
        at.addColumn("height", int.class);
        at.addColumn("mean", double.class);
        at.addColumn("type", String.class);
        // columns for the pixelss
        vt.addColumn("xCor", Integer.class);
    	vt.addColumn("yCor", Integer.class);
    	vt.addColumn("type", String.class);
    	vt.addColumn("width", Integer.class);
    	vt.addColumn("height", Integer.class);
    	vt.addColumn("value", Double.class);
    	vt.addColumn("color", Double.class);
    	
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
	        	AggregateItem newItem = (AggregateItem)at.addItem();
	        	newItem.set("index", new Integer(currentIndex));
	        	newItem.set("author1", dc.getXAxisNameAt(x));
	        	newItem.set("author2", dc.getYAxisNameAt(y));
	        	newItem.set("author", dc.getXAxisNameAt(x) + " & " + dc.getYAxisNameAt(y));
	        	newItem.set("xCor", new Integer(0));
	        	newItem.set("yCor", new Integer(0));
	        	newItem.set("width", new Integer(squareWidth));
	        	newItem.set("height", new Integer(squareWidth + textSize));
	        	newItem.set("type", new String("glyph"));
	        	
	        	// create pixels for this glyph
	        	// the values for the pixels are assumed to be stored
	        	// in the z-axis
	        	for (int z = 0; z < zCount; z++)
		       	{
	            	VisualItem newPixel = vt.addItem();
		           	newPixel.set("value", new Double(dc.getValueAt(x, y, z)));
		           	newPixel.set("color", new Double(dc.getValueAt(x, y, z) / highest));
		           	newPixel.set("width", new Integer(pixelSize));
		        	newPixel.set("height", new Integer(pixelSize));
		        	newPixel.set("xCor", new Integer(0));
		        	newPixel.set("yCor", new Integer(0));
		        	newPixel.set("type", new String("pixel"));
		        	at.addToAggregate(currentIndex, newPixel);
		        }
	            // calculate mean value of the glyph
	            double mean = 0;
	            int numberValues = 0;
	            Iterator iter = at.aggregatedTuples(currentIndex);
	        	while (iter.hasNext())
	        	{
	        		double d = ((Double)((VisualItem)(iter.next())).get("value")).doubleValue();
	        		mean += d;
	        		numberValues++;
	        	}
	        	mean = mean / numberValues;
	        	at.getItem(currentIndex).set("mean", new Double(mean));
	        	
	        	currentIndex++;
	     	}// end of for y
        }// end of for x
    } // end of loadCube
}
