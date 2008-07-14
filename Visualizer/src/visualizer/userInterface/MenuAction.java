package visualizer.userInterface;
/**
 * 
 */


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.WindowConstants;

import visualizer.StringConstants;


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
			frame.setGlyphLayout(selection);
			frame.updateGlyphLayout();
			frame.updateVisu();
		}
		else if (source.equals("pixelMenu"))
		{
			// if matrix layout was chosen
			// open a dialog box for the options
			if (selection.equals(StringConstants.MatrixLayout))
			{
				 JDialog dialog = new MatrixOptionsDialog(frame, "Matrix Optionen", true);
			     dialog.setSize(400,300);
			     dialog.setLocation(300, 200);
			     dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			     dialog.setResizable(false);
			     dialog.setVisible(true);
			}
			else
			{
				frame.setPixelLayout(selection);
				frame.updatePixelLayout();
				frame.updateGlyphLayout();
				frame.updateVisu();
			}
		}
		else if (source.equals("prefMenu"))
		{
			if (selection.equals(StringConstants.GlyphBorders))
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
