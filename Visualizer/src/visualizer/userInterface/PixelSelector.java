package visualizer.userInterface;
/**
 * 
 */

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import prefuse.Display;
import prefuse.controls.FocusControl;
import prefuse.controls.SubtreeDragControl;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;
import visualizer.StringConstants;
import visualizer.VisuMain;
import visualizer.display.GlyphTable;
/**
 * This class triggers events after actions like dragging
 * an item or just moving the mouse.
 * 
 * @author Rene Wegener
 *
 */
public class PixelSelector extends SubtreeDragControl
{
	protected PixelFrame frame;
	protected String selection = StringConstants.Nothing;
	protected GlyphTable gt;
	protected int select1, select2;
	protected VisuMain vis;
	protected Display dis;
	
	public PixelSelector(PixelFrame pf, GlyphTable tab, VisuMain v, Display d)
	{
		super();
		frame = pf;
		gt = tab;
		vis = v;
		dis = d;
		select1 = -1;
		select2 = -1;
	}
	
	public void itemClicked(VisualItem item,
            java.awt.event.MouseEvent e)
	{
		if (e.getButton() >= 2) // right click
		{
			selection = StringConstants.Nothing;
			select1 = -1;
			select2 = -1;
			frame.setZDesc(selection);
		}
		else
		{
			// Berechnung des Pixels anhand der
			// Koordinaten
			int clickX = e.getX();
			int clickY = e.getY();
			Point2D p = dis.getAbsoluteCoordinate(new Point(clickX, clickY), null);
			int absX = (int)p.getX();
			int absY = (int)p.getY();
			absX -= ((Integer)item.get("xCor")).intValue();
			absY -= ((Integer)item.get("yCor")).intValue();
			int pixel = -1;
			for (int i = frame.getStartIndex(); i <= frame.getStopIndex(); i++)
			{
				if (gt.getXCorAt(i) == absX && gt.getYCorAt(i) == absY)
					pixel = i;
			}
			//frame.setInfo("Clicked pixel: " + pixel);//clickX + ", " + clickY + " - abs: " + p.getX() + ", " + p.getY());
			if (selection.equals(StringConstants.Nothing) && pixel > -1)
			{
				frame.setZDesc(gt.getPixelDescAt(pixel) + " - ");
				selection = (gt.getPixelDescAt(pixel));
				select1 = pixel;
			}
			else if (pixel > -1)
	        {
				// the user has selected two pixels and now
				// all pixels that don't lie between them 
				// should be removed from the visualization
				select2 = pixel;
				int startIndex = select1;
				int stopIndex = select2;
				if (startIndex > stopIndex)
				{
					int var = startIndex;
					startIndex = stopIndex;
					stopIndex = var;
				}
				
				selection = StringConstants.Nothing;
				frame.setZDesc(selection);
				select1 = -1;
				select2 = -1;
				gt.updateMeans(startIndex, stopIndex);
				frame.setRange(startIndex, stopIndex);
				frame.updatePixelLayout();
				frame.updateGlyphLayout();
				frame.updateVisu();
			}
		}
	}
	
	public void itemEntered(VisualItem item,
            java.awt.event.MouseEvent e)
	{
		Display d = (Display)e.getSource();
		frame.setYDesc((String)item.get("y-desc"));
		if (gt.isCube())
			frame.setXDesc((String)item.get("x-desc"));
		d.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        int clickX = e.getX();
		int clickY = e.getY();
		Point2D p = dis.getAbsoluteCoordinate(new Point(clickX, clickY), null);
		int absX = (int)p.getX();
		int absY = (int)p.getY();
		absX -= ((Integer)item.get("xCor")).intValue();
		absY -= ((Integer)item.get("yCor")).intValue();
		int pixel = -1;
		for (int i = frame.getStartIndex(); i <= frame.getStopIndex(); i++)
		{
			if (gt.getXCorAt(i) == absX && gt.getYCorAt(i) == absY)
				pixel = i;
		}
		if (selection.equals(StringConstants.Nothing) && pixel > -1)
        {
        	frame.setPixelValue(((double[])item.get("value"))[pixel]);
        }
        else if (pixel > -1)
        {
        	frame.setZDesc(selection + " - " + gt.getPixelDescAt(pixel));
        }
    }
	
	public void itemMoved(VisualItem item,
            java.awt.event.MouseEvent e)
	{
		Display d = (Display)e.getSource();
        d.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        int clickX = e.getX();
		int clickY = e.getY();
		Point2D p = dis.getAbsoluteCoordinate(new Point(clickX, clickY), null);
		int absX = (int)p.getX();
		int absY = (int)p.getY();
		absX -= ((Integer)item.get("xCor")).intValue();
		absY -= ((Integer)item.get("yCor")).intValue();
		int pixel = -1;
		for (int i = frame.getStartIndex(); i <= frame.getStopIndex(); i++)
		{
			if (gt.getXCorAt(i) == absX && gt.getYCorAt(i) == absY)
				pixel = i;
		}
		if (selection.equals(StringConstants.Nothing) && pixel > -1)
        {
        	frame.setZDesc(gt.getPixelDescAt(pixel));
        	frame.setPixelValue(((double[])item.get("value"))[pixel]);
        }
        else if (pixel > -1)
        {
        	frame.setZDesc(selection + " - " + gt.getPixelDescAt(pixel));
        	frame.setPixelValue(((double[])item.get("value"))[pixel]);
        }
    }
	
	public void itemExited(VisualItem item,
            java.awt.event.MouseEvent e)
	{
		Display d = (Display)e.getSource();
	   	d.setCursor(Cursor.getDefaultCursor());
	   	frame.setXDesc(StringConstants.Nothing);
	   	frame.setYDesc(StringConstants.Nothing);
	   	frame.setZDesc(StringConstants.Nothing);
	   	frame.setPixelValue(0);
	    if (selection.equals(StringConstants.Nothing))
        	frame.setZDesc(StringConstants.Nothing);
        else
        	frame.setZDesc(selection + " - ");
    }
	
	public void mouseClicked(java.awt.event.MouseEvent e)
	{
		if (e.getButton() >= 2) // right click
		{
			select1 = -1;
			select2 = -1;
			selection = StringConstants.Nothing;
			frame.setZDesc(selection);
		}
	}
}
