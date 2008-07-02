/**
 * 
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;

/**
 * This implementation of an ActionListener gets active
 * if a menu or something else in the PixelFrame has been 
 * activated.
 * 
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
	
	/**
	 * Perform the desired action for the ActionEvent.
	 */
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
			if (selection.equals(ConstantStrings.GlyphBorders))
			{
				frame.setBorders(((JCheckBoxMenuItem)e.getSource()).isSelected());
				frame.updateVisu();
			}
			else
			{
				frame.setPref(selection);
				frame.updateColors();
				frame.updateVisu();
			}
		}
	}
}
