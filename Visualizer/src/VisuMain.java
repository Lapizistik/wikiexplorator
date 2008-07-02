

import java.awt.Point;
import java.util.Vector;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.controls.PanControl;
import prefuse.render.DefaultRendererFactory;
import prefuse.visual.VisualTable;

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
	     //if (ds instanceof DataTable)
	    //	 DataLoader.loadTable((DataTable)ds, glyphTable, labelTable, pixelSize, textSize);
	     /*else*/ if (ds instanceof DataCube)
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
	     
	     frame = new PixelFrame("PixelBased Data Visualization");
	     r.setFrame(frame);
	     // add a control listener
	     dis.addControlListener(new PixelSelector(frame, glyphTable, this, dis));
	     
	     frame.setSize(frame.getMaximumSize());
	     frame.init(this, dis, r, glyphTable);
	     frame.pack();           // layout components in window
	     frame.setVisible(true); // show the window
	     
	     // set start layout
	     pixelLayout = ConstantStrings.RowLayout;
	     glyphLayout = ConstantStrings.RowLayout;
	     glyphSorting = "author";
	     updatePixelLayout(pixelLayout);
	     updateGlyphSize();
	     updateGlyphLayout(glyphLayout);
	     updatePixelLayout(pixelLayout);
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
		int startX = 10;
		int startY = 10;
		int space = 5;
		glyphLayout = layout;
		Vector v = new Vector();
	    for (int i = 0; i < glyphTable.getRowCount(); i++)
	    {
	    	v.add(new Point(((Integer)glyphTable.getItem(i).get("xCor")).intValue(), 
	    			((Integer)glyphTable.getItem(i).get("yCor")).intValue()));
	    }
	    //Sort.sort(v, glyphSorting);
	    if (layout.equals(ConstantStrings.ZLayout))
	    	Layouts.createZLayout(v, startX, startY, glyphTable.getWidth() + space, glyphTable.getHeight() + space);
	    else if (layout.equals(ConstantStrings.MyZLayout))
	    	Layouts.createFlexibleZLayout(v, startX, startY, glyphTable.getWidth() + space, glyphTable.getHeight() + space);
	    else if (layout.equals(ConstantStrings.RowLayout))
	    	Layouts.createSimpleLayout(v, startX, startY, glyphTable.getWidth() + space, glyphTable.getHeight() + space);
		
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
		pixelLayout = layout;
		// create a Vector of Points which represent the 
		// pixels
		Vector pixels = new Vector();
		for (int i = 0; i < glyphTable.getZAxisCount(); i++)
		{
			if (i >= frame.getStartIndex() && i <= frame.getStopIndex())
				pixels.add(new Point(glyphTable.getXAt(i),
					glyphTable.getYAt(i)));
		}
		
		// assign the layout
		if (layout.equals(ConstantStrings.ZLayout))
	    	Layouts.createZLayout(pixels, 0, textSize, 1, 1);
	    else if (layout.equals(ConstantStrings.MyZLayout))
	    	Layouts.createFlexibleZLayout(pixels, 
	    	0, textSize, 1, 1);
	    else if (layout.equals(ConstantStrings.RowLayout))
	    	Layouts.createSimpleLayout(pixels, 
	    	0, textSize, 1, 1);
	    else if (layout.equals(ConstantStrings.FatRowLayout))
	    	Layouts.createLineLayout(pixels, 
	    	0, textSize, 1, 1);
	    else if (layout.equals(ConstantStrings.HilbertLayout))
	    	Layouts.createHilbertLayout(pixels, 
	    	0, textSize, 1, 1);
	    
		// now the pixels must adapt the 
		// points' positions
		for (int i = 0; i < pixels.size(); i++)
		{
			Point p = ((Point)pixels.get(i));
			glyphTable.setXAt((int)p.getX(), frame.getStartIndex() + i);
			glyphTable.setYAt((int)p.getY(), frame.getStartIndex() + i);
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
    	VisuMain visuMain = new VisuMain();
    	TestCube test = new TestCube();
    	visuMain.init(test);
    }
}
