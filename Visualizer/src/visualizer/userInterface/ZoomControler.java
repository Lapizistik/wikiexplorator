package visualizer.userInterface;


import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import prefuse.Display;
import prefuse.controls.WheelZoomControl;
import prefuse.util.display.DisplayLib;
import prefuse.visual.VisualItem;
import visualizer.display.GlyphTable;

/**
 * The Zoom Controller zooms the display and resizes it
 * if the mouse wheel is used.
 * 
 * @author Rene Wegener
 *
 */
public class ZoomControler extends WheelZoomControl
{
	protected Display dis; 
	protected GlyphTable gt;
	
	public ZoomControler(Display d, GlyphTable table)
	{
		super();
		dis = d;
		gt = table;
	}
	
	public void itemWheelMoved(VisualItem item,
            java.awt.event.MouseWheelEvent e)
	{
		super.itemWheelMoved(item, e);
		resizeDisplay();
	}

	public void mouseWheelMoved(java.awt.event.MouseWheelEvent e)
	{
		super.mouseWheelMoved(e);
		resizeDisplay();
	}

	private void resizeDisplay()
	{
		double scale = dis.getScale();
		Iterator iter = gt.tuples();
	    Rectangle2D rect = (DisplayLib.getBounds(iter, 10));
	    int sizeX = (int)(rect.getWidth() * scale);
	    int sizeY = (int)(rect.getHeight() * scale);
	    //dis.panToAbs(new Point(0,0));//sizeX/2, sizeY/2));
	    if (sizeX < 900)
	    	sizeX = 900;
	    if (sizeY < 600)
	    	sizeY = 600;
	    dis.setSize(sizeX, sizeY);
	    //DisplayLib.fitViewToBounds(dis, rect, new Point(sizeX/2,sizeY/2), 0);
	}
}
