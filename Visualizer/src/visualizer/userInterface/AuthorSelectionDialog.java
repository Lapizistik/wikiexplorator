/**
 * 
 */
package visualizer.userInterface;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import prefuse.visual.VisualItem;

import visualizer.StringConstants;
import visualizer.display.GlyphTable;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 *
 * This class allows the user to select or deselect authors;
 * only selected authors will be visible
 * 
 * @author Rene Wegener
 */
public class AuthorSelectionDialog extends JDialog implements ActionListener
{
	private PixelFrame frame;
	private JCheckBox[] selected; 
	private GlyphTable gt;
	
	/**
	 * create a new dialog
	 * 
	 * @param pFrame the PixelFrame calling the dialog
	 * @param name the name of the dialog frame
	 */
	public AuthorSelectionDialog(PixelFrame pFrame, String name)
	{
		super(pFrame, name);
		frame = pFrame;
		gt = frame.getGlyphTable();
		init();
	}
	
	protected void init()
	{
		Container cp = getContentPane();
		FormLayout layout = new FormLayout("center:default:grow",
		"10px, center:default:grow, 10px, 30px, 10px");
		cp.setLayout(layout);
		CellConstraints cc = new CellConstraints();
		//setLayout(new GridLayout(2, 1));//gt.getAuthorCount() / 4 + 1, 4, 5, 5));
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(gt.getAuthorCount(), 1));
		JScrollPane sp = new JScrollPane(panel);
		cp.add(sp, cc.xy(1, 2));
		//panel.setPreferredSize(new Dimension(200, 300));
		sp.setPreferredSize(sp.getMaximumSize());
		//add(sp);
		String[] authors = new String[gt.getAuthorCount()];
		selected = new JCheckBox[gt.getAuthorCount()];
		for (int i = 0; i < authors.length; i++)
			authors[i] = gt.getYAxisDescAt(i);
		java.util.Arrays.sort(authors);
		for (int i = 0; i < authors.length; i++)
		{	
			selected[i] = new JCheckBox(authors[i]);
			panel.add(selected[i]);
			selected[i].setSelected(false);
			for (int j = 0; j < gt.getRowCount(); j++)
			{
				VisualItem actItem = gt.getItem(j);
				String author = (String)actItem.get("y-desc");
				if (selected[i].getText().equals(author))
				{
					boolean vis = ((Boolean)actItem.get("visible")).booleanValue();
					if (vis)
						selected[i].setSelected(true);
				}
			}
		}
		JButton okButton = new JButton("OK");
		okButton.addActionListener(this);
		cp.add(okButton, cc.xy(1, 4));
		//add(new JPanel().add(okButton));
	}
	
	public void actionPerformed(ActionEvent e)
	{
		for (int i = 0; i < gt.getRowCount(); i++)
		{
			VisualItem actItem = gt.getItem(i);
			String author = (String)actItem.get("y-desc");
			String author2 = "";
			if (gt.isCube())
				author2 = (String)actItem.get("x-desc");
			actItem.set("visible", new Boolean(true));
			for (int j = 0; j < selected.length; j++)
			{
				if (selected[j].getText().equals(author) ||
						selected[j].getText().equals(author2))
				{
					if (!selected[j].isSelected())
					{
						actItem.set("visible", new Boolean(false));
					}
				}
			}
		}
		frame.updateGlyphLayout();
		frame.updateVisu();
		dispose();
	}
}
