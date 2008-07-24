package visualizer;



import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Vector;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.controls.PanControl;
import prefuse.render.DefaultRendererFactory;
import prefuse.visual.VisualTable;
import visualizer.data.DataCube;
import visualizer.data.DataLoader;
import visualizer.data.DataSet;
import visualizer.data.DataTable;
import visualizer.display.GlyphTable;
import visualizer.display.Layouts;
import visualizer.display.OptimizingLayouts;
import visualizer.display.PixelRenderer;
import visualizer.ruby.FileLoader;
import visualizer.userInterface.PixelFrame;
import visualizer.userInterface.PixelSelector;
import visualizer.userInterface.ZoomControler;

/**
 * The main class of the program. VisuMain creates all
 * necessary objects. If you start the main() method,
 * the visualization will launch with a TestCube.
 * If you have created a DataCube or DataTable yourself,
 * you need to run the method init with your DataSet
 * as parameter.
 * 
 * @author Rene Wegener
 *
 */
public class VisuMain 
{
	 protected Visualization vis;
	 protected GlyphTable glyphTable;
	 protected VisualTable labelTable;
	 protected DataSet ds = new DataSet();
	 protected Display dis;
	 protected int textSize = 0;
     protected int pixelSize = 1;
     protected PixelRenderer r;
     protected String pixelLayout, glyphLayout, glyphSorting;
     protected PixelFrame frame;
     
     
     public void init(String file)
     {
    	 DataSet set = FileLoader.load(file);
    	 if (set != null)
    		 init(set);
     }
     
     /**
      * Initialize the whole visualization. All that is needed
      * is a DataCube or DataTable.  
      */
	 public void init(DataSet data)
	 {
		 // setup the visualization
		 vis = new Visualization();
	     glyphTable = new GlyphTable(vis, "glyphTable");
	     //labelTable = new VisualTable(vis, "labelTable");
	     vis.add("glyphTable", glyphTable);
	     //vis.add("labelTable", labelTable);
	     
	     // load data and create the glyph and pixel tables
	     ds = data;
	     if (ds instanceof DataTable)
	    	 DataLoader.loadTable((DataTable)ds, glyphTable, labelTable, pixelSize, textSize);
	     else if (ds instanceof DataCube)
	    	 DataLoader.loadCube((DataCube)ds, glyphTable, labelTable, pixelSize, textSize);
	        
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
	     dis.addControlListener(new ZoomControler(dis, glyphTable));
	     
	     if (frame == null)
	    	 frame = new PixelFrame("Visualisierung");
	     r.setFrame(frame);
	     // add a control listener
	     dis.addControlListener(new PixelSelector(frame, glyphTable, this, dis));
	     
	     frame.setSize(frame.getMaximumSize());
	     frame.init(this, dis, r, glyphTable);
	     frame.pack();           // layout components in window
	     frame.setVisible(true); // show the window
	     
	     // set start layout
	     pixelLayout = StringConstants.RowLayout;
	     glyphLayout = StringConstants.RowLayout;
	     glyphSorting = "author";
	     updatePixelLayout(pixelLayout);
	     updateGlyphSize();
	     updateGlyphLayout(glyphLayout);
	     updatePixelLayout(pixelLayout);
	     // is the display big enough?
	     Rectangle2D rect = vis.getBounds("glyphTable");
	     double newWidth = dis.getWidth();
	     double newHeight = dis.getHeight();
	     if (rect.getWidth() > dis.getWidth())
	    	 newWidth = rect.getWidth();
	     if (rect.getHeight() > dis.getHeight())
	    	 newHeight = rect.getHeight();
	     dis.setSize((int)newWidth, (int)newHeight);
	     //Iterator iter = glyphTable.tuples();
	     //Rectangle2D rect = (DisplayLib.getBounds(iter, 10));
	     //dis.setSize((int)rect.getWidth(), (int)rect.getHeight());
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
		int startX = 0;
		int startY = 0;
		int space = 3;
		glyphLayout = layout;
		ArrayList v = new ArrayList();
		if (!layout.equals(StringConstants.OptimizedTableLayout)
				&& !layout.equals(StringConstants.MDSLayout))
		    for (int i = 0; i < glyphTable.getRowCount(); i++)
		    	v.add(new Point(((Integer)glyphTable.getItem(i).get("xCor")).intValue(), 
		    			((Integer)glyphTable.getItem(i).get("yCor")).intValue()));
		else
		    for (int i = 0; i < glyphTable.getRowCount(); i++)
		    	v.add(glyphTable.getItem(i));
		//Sort.sort(v, glyphSorting);
	    if (layout.equals(StringConstants.ZLayout))
	    	Layouts.createZLayout(v, startX, startY, glyphTable.getGlyphWidth() + space, glyphTable.getGlyphHeight() + space);
	    else if (layout.equals(StringConstants.MyZLayout))
	    	Layouts.createFlexibleZLayout(v, startX, startY, glyphTable.getGlyphWidth() + space, glyphTable.getGlyphHeight() + space);
	    else if (layout.equals(StringConstants.RowLayout) || 
	    		layout.equals(StringConstants.TableLayout))
	    {	
	    	if (glyphTable.isCube())
	    		Layouts.createRowLayout(v, startX, startY, glyphTable.getGlyphWidth() + space, glyphTable.getGlyphHeight() + space,
	   			glyphTable.getXAxisCount(), glyphTable.getYAxisCount());
	    	else
	    	{
	    		if (layout.equals(StringConstants.RowLayout)) 
	    			Layouts.createRowLayout(v, startX, startY, glyphTable.getGlyphWidth() + space, glyphTable.getGlyphHeight() + space, 0, 0);
	    		else
	    			Layouts.createRowLayout(v, startX, startY, glyphTable.getGlyphWidth() + space, glyphTable.getGlyphHeight() + space, 1, 0);
	    	}
	    }
	    else if (layout.equals(StringConstants.OptimizedTableLayout))
	    {
	    	OptimizingLayouts.createOrderedTableLayout(v, startX, startY, glyphTable.getGlyphWidth() + space, 
	    			glyphTable.getGlyphHeight() + space, glyphTable);
	    }
	    else if (layout.equals(StringConstants.MDSLayout))
	    {
	    	OptimizingLayouts.createMDSLayout(v, glyphTable.getGlyphWidth() + space, 
	    			glyphTable.getGlyphHeight() + space, glyphTable);
	    }
	    if (!layout.equals(StringConstants.OptimizedTableLayout)
	    		&& !layout.equals(StringConstants.MDSLayout))
		    for (int i = 0; i < v.size(); i++)
			{
				Point p = ((Point)v.get(i));
				glyphTable.getItem(i).set("xCor", new Integer((int)p.getX()));
				glyphTable.getItem(i).set("yCor", new Integer((int)p.getY()));
			}
	    // update the labeling:
	    // if data is ordered by author names and layout
	    // is simple layout, we have got a table layout
	    // which means we show the labeling!
	    //if (glyphSorting.equals("author") && layout.equals("Simple Layout"))
	    //{
	    	//Vector v2 = new Vector();
	    	//for (int i = 0; i < labelTable.getRowCount(); i++)
		    //  	v2.add(labelTable.getItem(i));
		    //Sort.sort(v2, "author");
		    //Layouts.createLabelLayout(v2, v, space);
		    //r.setTableLabeling(true);
	    //}
	    //else
	        //r.setTableLabeling(false);
	}
	
	public void updatePixelLayout(String layout)
	{
		if (layout.equals(StringConstants.RowLayout))
			updatePixelLayout(layout, glyphTable.getGlyphWidth(), 0);
		else if (layout.equals(StringConstants.ColumnLayout))
			updatePixelLayout(layout, 0, glyphTable.getGlyphHeight());
		else
			updatePixelLayout(layout, 0, 0);
	}
	
	public void updatePixelLayout(String layout, int matrixWidth, int matrixHeight)
	{
		pixelLayout = layout;
		// create a Vector of Points which represent the 
		// pixels
		ArrayList pixels = new ArrayList();
		for (int i = 0; i < glyphTable.getPixelCount(); i++)
		{
			if (i >= frame.getStartIndex() && i <= frame.getStopIndex())
				pixels.add(new Point(glyphTable.getXCorAt(i),
					glyphTable.getYCorAt(i)));
		}
		
		// assign the layout
		if (layout.equals(StringConstants.ZLayout))
	    	Layouts.createZLayout(pixels, 0, textSize, 1, 1);
	    else if (layout.equals(StringConstants.MyZLayout))
	    	Layouts.createFlexibleZLayout(pixels, 
	    	0, textSize, 1, 1);
	    else if (layout.equals(StringConstants.RowLayout))
	    	Layouts.createRowLayout(pixels, 
	    	0, textSize, 1, 1, matrixWidth, matrixHeight);
	    else if (layout.equals(StringConstants.ColumnLayout))
	    	Layouts.createColumnLayout(pixels, 
	    	0, textSize, 1, 1, matrixWidth, matrixHeight);
	    else if (layout.equals(StringConstants.FatRowLayout))
	    	Layouts.createLineLayout(pixels, 
	    	0, textSize, 1, 1);
	    else if (layout.equals(StringConstants.HilbertLayout))
	    	Layouts.createHilbertLayout(pixels, 
	    	0, textSize, 1, 1);
	    
		// now the pixels must adapt the 
		// points' positions
		for (int i = 0; i < pixels.size(); i++)
		{
			Point p = ((Point)pixels.get(i));
			glyphTable.setXCorAt((int)p.getX(), frame.getStartIndex() + i);
			glyphTable.setYCorAt((int)p.getY(), frame.getStartIndex() + i);
		}
	}
	
	public void updateGlyphSize()
	{
		glyphTable.updateSize(frame.getStartIndex(), frame.getStopIndex());
	}
	
	public void updateVisu()
	{
		//for (int i = 0; i < glyphTable.getRowCount(); i++)
		//	glyphTable.getItem(i).set("xCor", new Integer(((Integer)glyphTable.getItem(i).get("xCor")).intValue()));
		//vis.run("update");
		dis.pan(0, 0);
		dis.repaint();
		//vis.repaint();
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
    	VisuMain visuMain1 = new VisuMain();
    	visuMain1.init(new TestTable());
    }
}
