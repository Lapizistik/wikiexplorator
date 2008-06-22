/**
 * 
 */
package wikiVisi;
import java.awt.Cursor;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import prefuse.Display;
import prefuse.controls.FocusControl;
import prefuse.controls.SubtreeDragControl;
import prefuse.visual.AggregateItem;
import prefuse.visual.VisualItem;
/**
 * @author rene
 *
 */
public class PixelSelector extends SubtreeDragControl
{
	protected PixelFrame frame;
	
	public PixelSelector(PixelFrame pf)
	{
		super();
		frame = pf;
	}
	
	public void itemClicked(VisualItem item,
            java.awt.event.MouseEvent e)
	{
		System.out.println(item.get("type") + " was clicked");// + " no " + item.get("index") + " was clicked");
	}
	
	public void itemEntered(VisualItem item,
            java.awt.event.MouseEvent e)
	{
		Display d = (Display)e.getSource();
        d.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        if (item.get("type").equals("pixel"))
        {
        	frame.setInfo((String)item.get("desc"));
        }
    }
	
	public void itemExited(VisualItem item,
            java.awt.event.MouseEvent e)
	{
		  Display d = (Display)e.getSource();
	      d.setCursor(Cursor.getDefaultCursor());
	      frame.setInfo("nothing selected");
	}
}
