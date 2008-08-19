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
	 protected ArrayList<Visualization> vis;
	 protected ArrayList<GlyphTable> glyphTable;
	 protected DataSet ds = new DataSet();
	 protected ArrayList<Display> dis;
	 protected ArrayList<PixelRenderer> render;
     protected ArrayList<PixelFrame> frame;
     protected int frameCount = 0;
     
     
     public int getNumberOfFrames()
     {
    	 return frameCount;
     }
     
     public void initNew()
     {
    	 vis = new ArrayList<Visualization>();
    	 glyphTable = new ArrayList<GlyphTable>();
    	 dis = new ArrayList<Display>();
    	 render = new ArrayList<PixelRenderer>();
         frame = new ArrayList<PixelFrame>(); 	 
     }
     
     public void init(String file, int index)
     {
    	 DataSet set = FileLoader.load(file);
    	 if (set != null)
    		 init(set, index);
     }
     
     /**
      * Initialize the whole visualization. All that is needed
      * is a DataCube or DataTable.  
      */
	 public void init(DataSet data, int index)
	 {
		 // is this the first initialization?
		 if (vis == null) 
			 initNew();
		 // if the new visualization is put into an already
		 // existing frame (which means a new file is loaded),
		 // there is an old visualization that must be deleted.
		 if (index < frameCount)
		 {
			 vis.remove(index);
	    	 glyphTable.remove(index);
	    	 dis.remove(index);
	    	 render.remove(index);
		 }
		 // if a new frame must be created, update 
		 // the frameCount.
		 if (index == frameCount) 
		 {
			 frameCount++;
		 }
		 
		 // setup the visualization
		 vis.add(index, new Visualization());
		 glyphTable.add(index, new GlyphTable(vis.get(index), "glyphTable"));
		 //vis.add(new Visualization());
	     //glyphTable.add(new GlyphTable(vis.get(index), "glyphTable"));
	     //labelTable = new VisualTable(vis, "labelTable");
	     vis.get(index).add("glyphTable", glyphTable.get(index));
	     //vis.add("labelTable", labelTable);
	     
	     // load data and create the glyph and pixel tables
	     ds = data;
	     if (ds instanceof DataTable)
	    	 DataLoader.loadTable((DataTable)ds, glyphTable.get(index));
	     else if (ds instanceof DataCube)
	    	 DataLoader.loadCube((DataCube)ds, glyphTable.get(index));
	        
	     // setup renderer
	     render.add(index, new PixelRenderer("label", glyphTable.get(index)));
	      // create a new default renderer factory
	     DefaultRendererFactory drf = new DefaultRendererFactory(render.get(index));
	     vis.get(index).setRendererFactory(drf);
	     // setup display and controls
	     dis.add(index, new Display(vis.get(index)));
	     dis.get(index).setSize(900, 600);
	     //dis.setBackground(Color.blue);
	     // pan with left-click drag on background
	     dis.get(index).addControlListener(new PanControl()); 
	     // zoom with mousewheel
	     dis.get(index).addControlListener(new ZoomControler(dis.get(index), glyphTable.get(index)));
	     
	     if (index >= frameCount || frame.size() == 0)
	    	 frame.add(index, new PixelFrame("Visualisierung", index));
	     render.get(index).setFrame(frame.get(index));
	     // add a control listener
	     dis.get(index).addControlListener(new PixelSelector(frame.get(index), glyphTable.get(index), this, dis.get(index)));
	     
	     frame.get(index).setSize(frame.get(index).getMaximumSize());
	     frame.get(index).init(this, dis.get(index), render.get(index), glyphTable.get(index));
	     frame.get(index).pack();           // layout components in window
	     frame.get(index).setVisible(true); // show the window?
	     
	     // set start layout
	     glyphTable.get(index).updatePixelLayout(frame.get(index));
	     glyphTable.get(index).updateGlyphLayout(frame.get(index));
	     glyphTable.get(index).updatePixelLayout(frame.get(index));
	     // is the display big enough?
	     Rectangle2D rect = vis.get(index).getBounds("glyphTable");
	     double newWidth = dis.get(index).getWidth();
	     double newHeight = dis.get(index).getHeight();
	     if (rect.getWidth() > dis.get(index).getWidth())
	    	 newWidth = rect.getWidth();
	     if (rect.getHeight() > dis.get(index).getHeight())
	    	 newHeight = rect.getHeight();
	     dis.get(index).setSize((int)newWidth, (int)newHeight);
	     glyphTable.get(index).updateVisu();
	}
	 
	 public void duplicate(int oldIndex)
	 {
		 int index = frameCount;
		 frameCount++;
		 
		 // setup the visualization
		 vis.add(new Visualization());
	     glyphTable.add(glyphTable.get(oldIndex).duplicate(vis.get(index)));
		 vis.get(index).add("glyphTable", glyphTable.get(index));
	     
	     // setup renderer
	     render.add(new PixelRenderer("label", glyphTable.get(index)));
	        
	     // create a new default renderer factory
	     DefaultRendererFactory drf = new DefaultRendererFactory(render.get(index));
	     vis.get(index).setRendererFactory(drf);
	     // setup display and controls
	     dis.add(new Display(vis.get(index)));
	     dis.get(index).setSize(900, 600);
	     // pan with left-click drag on background
	     dis.get(index).addControlListener(new PanControl()); 
	     // zoom with mousewheel
	     dis.get(index).addControlListener(new ZoomControler(dis.get(index), glyphTable.get(index)));
	     
	     if (index >= frame.size())
	    	 frame.add(new PixelFrame("Visualisierung", index));
	     render.get(index).setFrame(frame.get(index));
	     // add a control listener
	     dis.get(index).addControlListener(new PixelSelector(frame.get(index), glyphTable.get(index), this, dis.get(index)));
	     
	     frame.get(index).setSize(frame.get(index).getMaximumSize());
	     frame.get(index).init(this, dis.get(index), render.get(index), glyphTable.get(index));
	     frame.get(index).pack();           // layout components in window
	     frame.get(index).setVisible(true); // show the window?
	     
	     // copy frame properties
	     frame.get(index).setInverted(frame.get(oldIndex).getInverted());
	     frame.get(index).setSelectedGlyphLayoutIndex(frame.get(oldIndex).getSelectedGlyphLayoutIndex());
	     frame.get(index).setSelectedPixelLayoutIndex(frame.get(oldIndex).getSelectedPixelLayoutIndex());
	     if (frame.get(oldIndex).getColor().equals(StringConstants.GrayScale))
	    	 frame.get(index).setSelectedColorScale(0);
	     else
	    	 frame.get(index).setSelectedColorScale(1);
	     frame.get(index).setBorderOn(frame.get(oldIndex).isBorderOn());
	     frame.get(index).setSpace(frame.get(oldIndex).getSpace());
	     frame.get(index).setRange(frame.get(oldIndex).getStartIndex(), frame.get(oldIndex).getStopIndex());
	     //frame.get(index).setStartIndex(frame.get(oldIndex).getStartIndex());
	     //frame.get(index).setStopIndex(frame.get(oldIndex).getStopIndex());
	     frame.get(index).setSlidingRange(frame.get(oldIndex).getSlidingRange());
	     dis.get(index).setBackground(dis.get(oldIndex).getBackground());
	     dis.get(index).setSize(dis.get(oldIndex).getSize());//(int)newWidth, (int)newHeight);
	     glyphTable.get(index).setGlyphBounds(glyphTable.get(oldIndex).getGlyphBounds());
	     glyphTable.get(index).updateVisu();     
	}
	 
	 public void disposeFrame(int index)
	 {
		 for (int i = index + 1; i < frameCount; i++)
			 frame.get(i).setIndex(i - 1);
		 frameCount--; 
		 vis.remove(index);
		 glyphTable.remove(index);
		 dis.remove(index);
		 render.remove(index);
	     frame.remove(index);
	 }
	 
	public static void main(String[] args) 
    {
    	VisuMain visuMain1 = new VisuMain();
	    if (args.length == 1)
        	visuMain1.init(args[0], 0);
	    else
	    	visuMain1.init("cube1.txt", 0);//new TestTable(), 0);
    }
}
