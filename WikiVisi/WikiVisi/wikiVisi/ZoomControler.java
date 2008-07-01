/**
 * 
 */
package wikiVisi;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.controls.WheelZoomControl;
import prefuse.util.display.DisplayLib;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;

/**
 * @author rene
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

	public void resizeDisplay()
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
