package wikiVisi;


import java.awt.Color;
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
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
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

public class Jigsaw 
{
	 protected Visualization vis;
	 protected AggregateTable glyphTable;
	 protected VisualTable pixelTable;
	 protected DataSet ds = new DataSet();
	 protected Display dis;
	 protected int textSize = 12;
     protected int pixelSize = 3;
     protected PixelRenderer r;
     protected String currentPixelLayout;
     
	 public void init(DataSet data)
	 {
		 // setup the visualization
		 vis = new Visualization();
	     glyphTable = new AggregateTable(vis, "glyphTable");
	     pixelTable = new VisualTable(vis, "pixelTable");
	     vis.add("glyphTable", glyphTable);
	     vis.add("pixelTable", pixelTable);
	    
	     // load data
	     ds = data;
	     if (ds instanceof DataTable)
	    	 DataLoader.loadTable((DataTable)ds, glyphTable, pixelTable, pixelSize, textSize);
	     else if (ds instanceof DataCube)
	    	 DataLoader.loadCube((DataCube)ds, glyphTable, pixelTable, pixelSize, textSize);
	     	
	     // draw the "name" label for NodeItems
	     r = new PixelRenderer("label", pixelSize,
	    		 textSize);
	        
	     // create a new default renderer factory
	     DefaultRendererFactory drf = new DefaultRendererFactory(r);
	     vis.setRendererFactory(drf);
	      
	     // setup display and controls
	     dis = new Display(vis);
	     dis.setSize(980, 720); 
	     // drag individual items around
	     dis.addControlListener(new DragControl());
	     // pan with left-click drag on background
	     dis.addControlListener(new PanControl()); 
	     // zoom with right-click drag
	     dis.addControlListener(new ZoomControl());
	        
	     // create a new window to hold the visualization
	     PixelFrame frame = new PixelFrame("PixelBased Data Visualization");
	     // ensure application exits when window is closed
	     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	     frame.init(this);
	     frame.add(dis);
	     frame.pack();           // layout components in window
	     frame.setVisible(true); // show the window
	     
	     // set start layout
	     currentPixelLayout = "Simple Layout";
	     updateGlyphLayout("Simple Layout", "author");
	     updatePixelLayout(currentPixelLayout);
	}
	 
	public void sort(String sort)
	{
		Vector v = new Vector();
		for (int i = 0; i < glyphTable.getRowCount(); i++)
	       	v.add(glyphTable.getItem(i));
	    Sort.sort(v, sort);
	}
	
	public void updateGlyphLayout(String layout, String sort)
	{
		int startX = 10;
		int startY = 10;
		int space = 5;
		
		Vector v = new Vector();
	    for (int i = 0; i < glyphTable.getRowCount(); i++)
	      	v.add(glyphTable.getItem(i));
	    Sort.sort(v, sort);
	    if (layout.equals("Morton Z-Curve"))
	    	Layouts.createZLayout(v, startX, startY, space);
	    else if (layout.equals("Flexible Z-Curve"))
	    	Layouts.createFlexibleZLayout(v, startX, startY, space);
	    else if (layout.equals("Simple Layout"))
	    	Layouts.createSimpleLayout(v, startX, startY, space);
	    // because the glyph layout has changed,
	    // the pixel coordinates have to be updated
	    updatePixelLayout(currentPixelLayout);
	}
	
	public void updatePixelLayout(String layout)
	{
		currentPixelLayout = layout;
		for (int i = 0; i < glyphTable.getRowCount(); i++)
	    {
	       	VisualItem item = (VisualItem)glyphTable.getItem(i);
	        Vector pixels = new Vector();
	       	Iterator iter = glyphTable.aggregatedTuples(i);
	       	while (iter.hasNext())
	       		pixels.add(iter.next());
	       	if (layout.equals("Morton Z-Curve"))
	       	{
	       		Layouts.createZLayout(pixels, 
	       			((Integer)item.get("xCor")).intValue(), 
	        		((Integer)item.get("yCor")).intValue() + textSize, 0);
	       	}
	       	else if (layout.equals("Flexible Z-Curve"))
	       	{
	       		Layouts.createFlexibleZLayout(pixels, 
	       			((Integer)item.get("xCor")).intValue(), 
	        		((Integer)item.get("yCor")).intValue() + textSize, 0);
	       	}
	       	else if (layout.equals("Simple Layout"))
	       	{
	       		Layouts.createSimpleLayout(pixels, 
	       			((Integer)item.get("xCor")).intValue(), 
	        		((Integer)item.get("yCor")).intValue() + textSize, 0);
	       	}
	   }
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
    	Jigsaw jigsaw = new Jigsaw();
    	TestCube test = new TestCube();
    	jigsaw.init(test);
    }
}
