package visualizer.userInterface;


import java.awt.Color;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;
import visualizer.StringConstants;


/**
 * This implementation of an ActionListener gets active
 * if a menu or something else in the PixelFrame has been 
 * activated.
 * 
 * @author Rene Wegener
 *
 */
public class MenuAction implements ActionListener
{
	protected PixelFrame frame;
	
	/**
	 * create a new MenuAction
	 * @param f the frame which will trigger this ActionListener
	 */
	public MenuAction(PixelFrame f)
	{
		super();
		frame = f;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		String source = ((JMenuItem)e.getSource()).getParent().getName();
		String selection = ((JMenuItem)e.getSource()).getText();
		if (source.equals("fileMenu"))
		{
			if (selection.equals("Als Bilddatei exportieren"))
				frame.export();
			else if (selection.equals("Datei laden"))
			{
				FileDialog dialog = new FileDialog(frame, "Datei öffnen");
				dialog.setVisible(true);
				String filename = dialog.getFile();
				if (filename != null) 
				{
					frame.loadFile(dialog.getDirectory() + filename);
				}
				dialog.dispose();
			}
		}
		else if (source.equals("glyphMenu"))
		{
			frame.updateGlyphLayout();
			frame.updateVisu();
		}
		else if (source.equals("pixelMenu"))
		{
			// if matrix layout was chosen
			// open a dialog box for the options
			if (selection.equals(StringConstants.RowLayout) ||
				selection.equals(StringConstants.ColumnLayout))
			{
				 JDialog dialog = new MatrixOptionsDialog(frame, selection);
			     dialog.setSize(400,300);
			     dialog.setLocation(300, 200);
			     dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			     dialog.setResizable(false);
			     dialog.setVisible(true);
			}
			else
			{
				frame.updatePixelLayout();
				frame.updateGlyphLayout();
				frame.updateVisu();
			}
		}
		else if (source.equals("prefMenu"))
		{
			if (selection.equals(StringConstants.GlyphBorders))
			{
				//frame.setBorders(((JCheckBoxMenuItem)e.getSource()).isSelected());
				frame.updateVisu();
			}
			else if (selection.equals(StringConstants.ColorsInverted))
			{
				//frame.setInverted(((JCheckBoxMenuItem)e.getSource()).isSelected());
				frame.updateColorPanel();
				frame.updateVisu();
			}
			else if (selection.equals(StringConstants.AuthorFilter))
			{
				 JDialog dialog = new AuthorSelectionDialog(frame, selection);
			     dialog.setSize(300,400);
			     dialog.setLocation(300, 200);
			     dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			     dialog.setResizable(true);
			     dialog.setVisible(true);
			}
			else if (selection.equals(StringConstants.ColorChooser))
			{
				 JDialog dialog = new ColorDialog(frame, selection);
			     dialog.setSize(350,550);
			     dialog.setLocation(300, 200);
			     dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			     dialog.setResizable(true);
			     dialog.setVisible(true);
			}
			else
			{
				frame.updateColorPanel();
				frame.updateVisu();
			}
		}
		else if (source.equals("spaceMenu"))
		{
			int space = Integer.parseInt(selection);
			frame.setSpace(space);
			frame.updateGlyphLayout();
			frame.updateVisu();
		}
		else if (source.equals("backgroundMenu"))
		{
			Color c;
			if (selection.equals(StringConstants.BackWhite))
				c = Color.white;
			else if (selection.equals(StringConstants.BackGray))
				c = Color.lightGray;
			else 
				c = new Color(50, 150, 250);
			frame.setBackColor(c);
			frame.updateVisu();
		}
		else if (source.equals("helpMenu"))
		{
			 JDialog dialog = new HelpDialog("help/overview.html");
		     dialog.setSize(800,600);
		     dialog.setLocation(0, 0);
		     dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		     dialog.setResizable(true);
		     dialog.setVisible(true);
		}
	}
}
