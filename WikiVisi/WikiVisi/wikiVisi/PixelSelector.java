/**
 * 
 */
package wikiVisi;
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
/**
 * @author rene
 *
 */
public class PixelSelector extends SubtreeDragControl
{
	protected PixelFrame frame;
	protected String selection = "nothing";
	protected GlyphTable at;
	protected int select1, select2;
	protected VisuMain vis;
	protected Display dis;
	
	public PixelSelector(PixelFrame pf, GlyphTable tab, VisuMain v, Display d)
	{
		super();
		frame = pf;
		at = tab;
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
			selection = "nothing";
			select1 = -1;
			select2 = -1;
			frame.setInfo(selection);
		}
		else
		{
			// Test: Berechnung des Pixels anhand der
			// Koordinaten
			int clickX = e.getX();
			int clickY = e.getY();
			Point2D p = dis.getAbsoluteCoordinate(new Point(clickX, clickY), null);
			int absX = (int)p.getX();
			int absY = (int)p.getY();
			absX -= ((Integer)item.get("xCor")).intValue();
			absY -= ((Integer)item.get("yCor")).intValue();
			int pixel = 0;
			for (int i = 0; i < at.getZAxisCount(); i++)
			{
				if (at.getXAt(i) == absX && at.getYAt(i) == absY)
					pixel = i;
			}
			//frame.setInfo("Clicked pixel: " + pixel);//clickX + ", " + clickY + " - abs: " + p.getX() + ", " + p.getY());
			if (selection.equals("nothing"))
			{
				if (pixel >= frame.getStartIndex() && pixel <= frame.getStopIndex())
				{
					frame.setInfo("from " + at.getZDescAt(pixel) + " to ");
					selection = (at.getZDescAt(pixel));
					select1 = pixel;
				}
			}
			else if (pixel >= frame.getStartIndex() && pixel <= frame.getStopIndex())
	        {
				// the user has selected two pixels and now
				// all pixels that don't lie between them 
				// should be removed from th visualization
				select2 = pixel;
				int startIndex = select1;
				int stopIndex = select2;
				if (startIndex > stopIndex)
				{
					int var = startIndex;
					startIndex = stopIndex;
					stopIndex = var;
				}
				
				selection = "nothing";
				frame.setInfo(selection);
				select1 = -1;
				select2 = -1;
				frame.setRange(startIndex, stopIndex);
				frame.updateVisu();
			}
		}
	}
	
	public void itemEntered(VisualItem item,
            java.awt.event.MouseEvent e)
	{
		Display d = (Display)e.getSource();
        if (GlyphTable.isGlyph(item))
        {
        	d.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            int clickX = e.getX();
			int clickY = e.getY();
			Point2D p = dis.getAbsoluteCoordinate(new Point(clickX, clickY), null);
			int absX = (int)p.getX();
			int absY = (int)p.getY();
			absX -= ((Integer)item.get("xCor")).intValue();
			absY -= ((Integer)item.get("yCor")).intValue();
			int pixel = 0;
			for (int i = 0; i < at.getZAxisCount(); i++)
			{
				if (at.getXAt(i) == absX && at.getYAt(i) == absY)
					pixel = i;
			}
			
        	if (selection.equals("nothing"))
        	{
        		if (pixel >= frame.getStartIndex() && pixel <= frame.getStopIndex())
        			frame.setInfo(at.getZDescAt(pixel));
        	}
        	else
        	{
        		if (pixel >= frame.getStartIndex() && pixel <= frame.getStopIndex())
        			frame.setInfo("from " + selection + " to " + at.getZDescAt(pixel));
        	}
        }
    }
	
	public void itemMoved(VisualItem item,
            java.awt.event.MouseEvent e)
	{
		Display d = (Display)e.getSource();
        if (GlyphTable.isGlyph(item))
        {
        	d.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            int clickX = e.getX();
			int clickY = e.getY();
			Point2D p = dis.getAbsoluteCoordinate(new Point(clickX, clickY), null);
			int absX = (int)p.getX();
			int absY = (int)p.getY();
			absX -= ((Integer)item.get("xCor")).intValue();
			absY -= ((Integer)item.get("yCor")).intValue();
			int pixel = 0;
			for (int i = 0; i < at.getZAxisCount(); i++)
			{
				if (at.getXAt(i) == absX && at.getYAt(i) == absY)
					pixel = i;
			}
			
        	if (selection.equals("nothing"))
        	{
        		if (pixel >= frame.getStartIndex() && pixel <= frame.getStopIndex())
        			frame.setInfo(at.getZDescAt(pixel));
        	}
        	else
        	{
        		if (pixel >= frame.getStartIndex() && pixel <= frame.getStopIndex())
        			frame.setInfo("from " + selection + " to " + at.getZDescAt(pixel));
        	}
        }
    }
	
	public void itemExited(VisualItem item,
            java.awt.event.MouseEvent e)
	{
		Display d = (Display)e.getSource();
	    if (GlyphTable.isGlyph(item))
	    	d.setCursor(Cursor.getDefaultCursor());
	    if (selection.equals("nothing"))
        	frame.setInfo("nothing");
        else
        	frame.setInfo("from " + selection + " to ");
   }
	
	
	public void mouseClicked(VisualItem item,
            java.awt.event.MouseEvent e)
	{
		if (e.getButton() >= 2) // right click
		{
			select1 = -1;
			select2 = -1;
			selection = "nothing";
			frame.setInfo(selection);
		}
	}
}
