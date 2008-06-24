/**
 * 
 */
package wikiVisi;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
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
	protected AggregateTable at;
	protected VisualItem selectedItem1, selectedItem2;
	
	public PixelSelector(PixelFrame pf, AggregateTable tab)
	{
		super();
		frame = pf;
		at = tab;
	}
	
	public void itemClicked(VisualItem item,
            java.awt.event.MouseEvent e)
	{
		if (e.getButton() >= 2) // right click
		{
			selection = "nothing";
			selectedItem1 = null;
			selectedItem2 = null;
			frame.setInfo(selection);
		}
		else
		{
			if (selection.equals("nothing"))
			{
				frame.setInfo("from " + (String)item.get("desc") + " to ");
				selection = (String)item.get("desc");
				selectedItem1 = item;
			}
			else
			{
				// the user has selected two pixels and now
				// all pixels that don't lie between them 
				// shouls be removed from th visualization
				selectedItem2 = item;
				int startIndex = ((Integer)(selectedItem1.get("index"))).intValue();
				int stopIndex = ((Integer)(selectedItem2.get("index"))).intValue();
				if (startIndex > stopIndex)
				{
					int var = startIndex;
					startIndex = stopIndex;
					stopIndex = var;
				}
				for (int i = 0; i < at.getRowCount(); i++)
				{
					Iterator iter = at.aggregatedTuples(i);
			       	int currentIndex = 0;
					while (iter.hasNext())
			       	{
			       		VisualItem pixel = (VisualItem)iter.next();
			       		if (currentIndex < startIndex || currentIndex > stopIndex)
			       			pixel.set("invisible", new Boolean(true));
			       		currentIndex++;
			       	}
				}
				selection = "nothing";
				frame.setInfo(selection);
				selectedItem1 = null;
				selectedItem2 = null;
				frame.updateVisu();
				/*Iterator iter = at.aggregatedTuples(((Integer)(item.get("parentIndex"))).intValue());
		       	while (iter.hasNext())
		       	{
		       		VisualItem pixel = (VisualItem)iter.next();
		       		if (pixel.get("desc").equals(selection))
		       		{
		       			//pixel.set("color", new Double(0d));
			       		while (iter.hasNext())
		       			{
		       				VisualItem anotherPixel = (VisualItem)iter.next();
				       		//anotherPixel.set("color", new Double(0d));
				       		if (anotherPixel.get("desc").equals(item.get("desc")))
				       			break;
				       	}
		       			break;
		       		}
		       	}*/
			}
		}
	}
	
	public void itemEntered(VisualItem item,
            java.awt.event.MouseEvent e)
	{
		Display d = (Display)e.getSource();
        d.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        if (item.get("type").equals("pixel"))
        {
        	if (selection.equals("nothing"))
        	{
        		frame.setInfo((String)item.get("desc"));
        	}
        	else
        	{
        		frame.setInfo("from " + selection + " to " + (String)item.get("desc"));
        	}
        }
    }
	
	public void itemExited(VisualItem item,
            java.awt.event.MouseEvent e)
	{
		Display d = (Display)e.getSource();
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
			selectedItem1 = null;
			selectedItem2 = null;
			selection = "nothing";
			frame.setInfo(selection);
		}
	}
}
