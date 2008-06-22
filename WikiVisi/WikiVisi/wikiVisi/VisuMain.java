package wikiVisi;


import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFrame;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.RandomLayout;
import prefuse.action.layout.SpecifiedLayout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.graph.FruchtermanReingoldLayout;
import prefuse.activity.Activity;
import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ToolTipControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import prefuse.visual.tuple.TableAggregateItem;
import prefuse.visual.tuple.TableVisualItem;

public class VisuMain 
{
	 protected Visualization vis;
	 protected AggregateTable glyphTable;
	 protected VisualTable pixelTable, labelTable;
	 protected DataSet ds = new DataSet();
	 protected Display dis;
	 protected int textSize = 0;
     protected int pixelSize = 1;
     protected PixelRenderer r;
     protected String pixelLayout, glyphLayout, glyphSorting;
     protected PixelFrame frame;
     
	 public void init(DataSet data)
	 {
		 // setup the visualization
		 vis = new Visualization();
	     glyphTable = new AggregateTable(vis, "glyphTable");
	     pixelTable = new VisualTable(vis, "pixelTable");
	     labelTable = new VisualTable(vis, "labelTable");
	     vis.add("glyphTable", glyphTable);
	     vis.add("pixelTable", pixelTable);
	     vis.add("labelTable", labelTable);
	     
	     // load data and create the glyph and pixel tables
	     ds = data;
	     if (ds instanceof DataTable)
	    	 DataLoader.loadTable((DataTable)ds, glyphTable, pixelTable, labelTable, pixelSize, textSize);
	     else if (ds instanceof DataCube)
	    	 DataLoader.loadCube((DataCube)ds, glyphTable, pixelTable, labelTable, pixelSize, textSize);
	        
	     // setup renderer
	     r = new PixelRenderer("label", glyphTable, pixelSize,
	    		 textSize);
	        
	     // create a new default renderer factory
	     DefaultRendererFactory drf = new DefaultRendererFactory(r);
	     vis.setRendererFactory(drf);
	     // setup display and controls
	     dis = new Display(vis);
	     dis.setSize(900, 600); 
	     //dis.setBackground(Color.blue);
	     // pan with left-click drag on background
	     dis.addControlListener(new PanControl()); 
	     // zoom with mousewheel
	     dis.addControlListener(new WheelZoomControl());
	     
	     // create a new window to hold the visualization
	     frame = new PixelFrame("PixelBased Data Visualization");
	     // add a control listener
	     dis.addControlListener(new PixelSelector(frame));
	     
	     frame.setSize(frame.getMaximumSize());
	     frame.init(this, dis);
	     frame.pack();           // layout components in window
	     frame.setVisible(true); // show the window
	     
	     // set start layout
	     pixelLayout = "Simple Layout";
	     glyphLayout = "Simple Layout";
	     glyphSorting = "author";
	     updatePixelLayout(pixelLayout);
	     updateGlyphSize();
	     updateGlyphLayout(glyphLayout);
	     updatePixelLayout(pixelLayout);
	     updateVisu();
	 }
	 
	/*public void sort(String sort)
	{
		Vector v = new Vector();
		for (int i = 0; i < glyphTable.getRowCount(); i++)
	       	v.add(glyphTable.getItem(i));
	    Sort.sort(v, sort);
	}*/
	public void setSort(String s)
	{
		glyphSorting = s;
	}
	
	public void updateGlyphLayout(String layout)
	{
		int startX = 10;
		int startY = 10;
		int space = 2;
		glyphLayout = layout;
		Vector v = new Vector();
	    for (int i = 0; i < glyphTable.getRowCount(); i++)
	    	v.add(glyphTable.getItem(i));
	    Sort.sort(v, glyphSorting);
	    if (layout.equals("Morton Z-Curve"))
	    	Layouts.createZLayout(v, startX, startY, space);
	    else if (layout.equals("Flexible Z-Curve"))
	    	Layouts.createFlexibleZLayout(v, startX, startY, space);
	    else if (layout.equals("Simple Layout"))
	    	Layouts.createSimpleLayout(v, startX, startY, space);
	    // update the labeling:
	    // if data is ordered by author names and layout
	    // is simple layout, we have got a table layout
	    // which means we show the labeling!
	    if (glyphSorting.equals("author") && layout.equals("Simple Layout"))
	    {
	    	Vector v2 = new Vector();
	    	for (int i = 0; i < labelTable.getRowCount(); i++)
		      	v2.add(labelTable.getItem(i));
		    Sort.sort(v2, "author");
		    Layouts.createLabelLayout(v2, v, space);
		    r.setTableLabeling(true);
	    }
	    else
	        r.setTableLabeling(false);
	}
	
	public void updatePixelLayout(String layout)
	{
		pixelLayout = layout;
		for (int i = 0; i < glyphTable.getRowCount(); i++)
	    {
	       	VisualItem item = (VisualItem)glyphTable.getItem(i);
	        Vector pixels = new Vector();
	       	Iterator iter = glyphTable.aggregatedTuples(i);
	       	while (iter.hasNext())
	       		pixels.add(iter.next());
	       	if (layout.equals("Morton Z-Curve"))
	       	{
	       		Layouts.createZLayout(pixels, 0, textSize, 0);
	       	}
	       	else if (layout.equals("Flexible Z-Curve"))
	       	{
	       		Layouts.createFlexibleZLayout(pixels, 
	       			0, textSize, 0);
	       	}
	       	else if (layout.equals("Simple Layout"))
	       	{
	       		Layouts.createSimpleLayout(pixels, 
	       			0, textSize, 0);
	       	}
	    	else if (layout.equals("Line Layout"))
	       	{
	       		Layouts.createLineLayout(pixels, 
	       			0, textSize, 0);
	       	}
	    	else if (layout.equals("Hilbert Curve"))
	       	{
	       		Layouts.createHilbertLayout(pixels, 
	       			0, textSize, 0);
	       	}
	   }
	    // after the pixel layout has changed, the glyph 
		// width and height must be updated 
		//updateGlyphSize();
		// now update the glyph layout
		//updateGlyphLayout(glyphLayout);
	}
	
	public void updateGlyphSize()
	{
		// It's assumed that all glyphs have the same size
		// so we can just get the first glyph item to check
		// all glyph's number of pixels
		int newWidth = 0, newHeight = 0;
		Iterator iter = glyphTable.aggregatedTuples(0);
       	while (iter.hasNext())
       	{
       		VisualItem item = (VisualItem)iter.next();
       		if (((Integer)item.get("xCor")).intValue() > newWidth)
       			newWidth = ((Integer)item.get("xCor")).intValue();
       		if (((Integer)item.get("yCor")).intValue() > newHeight)
       			newHeight = ((Integer)item.get("yCor")).intValue();
       	}
       	
		for (int i = 0 ; i < glyphTable.getRowCount(); i++)
		{
			VisualItem item = glyphTable.getItem(i);
			item.set("width", new Integer(newWidth));
			item.set("height", new Integer(newHeight));
		}
	}
	
	public void updateVisu()
	{
		dis.repaint();
		vis.repaint();
	}
	
	public void setPixelSize(int ps)
	{
		pixelSize = ps;
	}
	
	public void setTextSize(int ts)
	{
		textSize = ts;
		r.setTextSize(ts);
	}
	
    public static void main(String[] args) 
    {
    	VisuMain visuMain = new VisuMain();
    	TestCube test = new TestCube();
    	visuMain.init(test);
    }
}
