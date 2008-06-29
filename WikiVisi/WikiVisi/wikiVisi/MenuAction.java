/**
 * 
 */
package wikiVisi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;

/**
 * @author rene
 *
 */
public class MenuAction implements ActionListener
{
	protected PixelFrame frame;
	
	public MenuAction(PixelFrame f)
	{
		super();
		frame = f;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		String source = ((JMenuItem)e.getSource()).getParent().getName();
		String selection = ((JMenuItem)e.getSource()).getText();
		if (source.equals("glyphMenu"))
		{
			frame.setGlyph(selection);
			frame.updateGlyphLayout();
			frame.updateVisu();
		}
		else if (source.equals("pixelMenu"))
		{
			frame.setPixel(selection);
			frame.updatePixelLayout();
			frame.updateGlyphLayout();
			frame.updateVisu();
		}
		else if (source.equals("prefMenu"))
		{
			frame.setPref(selection);
			frame.updateColors();
			frame.updateVisu();
		}
	}
}
