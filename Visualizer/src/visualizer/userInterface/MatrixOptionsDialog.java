/**
 * 
 */
package visualizer.userInterface;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import visualizer.StringConstants;

/**
 * @author rene
 *
 */
public class MatrixOptionsDialog extends JDialog implements ActionListener
{
	PixelFrame frame;
	private JTextField tfWidth, tfHeight;
	private JRadioButton radRows, radColumns;
	
	public MatrixOptionsDialog(PixelFrame pFrame, String name, boolean mod)
	{
		super(pFrame, name, mod);
		frame = pFrame;
		init();
	}
	
	public void init()
	{
		radRows  = new JRadioButton(StringConstants.RowLayout);
		radColumns  = new JRadioButton(StringConstants.ColumnLayout);
		ButtonGroup bg = new ButtonGroup();
		bg.add(radRows);
		bg.add(radColumns);
		radRows.setSelected(true);
		JLabel labWidth = new JLabel("Breite");
		JLabel labHeight = new JLabel("Hoehe");
		JTextArea taInfo = new JTextArea(3, 30);
		taInfo.setLineWrap(true);
		taInfo.setWrapStyleWord(true);
		taInfo.setText("Bitte waehlen Sie, ob die Werte zeilen- oder spaltenweise angeordnet werden sollen.");
		taInfo.append("Sie koennen die Breite bzw. Hoehe selbst angeben.");
		taInfo.append("Werte, die Sie nicht angeben, werden automatisch errechnet");
		taInfo.setSize(taInfo.getMaximumSize());
		taInfo.setEditable(false);
		taInfo.setBackground(Color.WHITE);
		tfWidth = new JTextField(3);
		tfHeight = new JTextField(3);
		JButton okButton = new JButton("OK");
		okButton.addActionListener(this);
		Container cp = getContentPane();
		FormLayout layout = new FormLayout("left:default:grow, center:default:grow, center:default:grow",
		"center:default:grow, center:default:grow, center:default:grow, center:default:grow");
		cp.setLayout(layout);
		CellConstraints cc = new CellConstraints();
		cp.add(taInfo, cc.xyw(1, 1, 3));
		cp.add(radRows, cc.xy(1, 2));
		cp.add(radColumns, cc.xy(1, 3));
		cp.add(labWidth, cc.xy(2, 2));
		cp.add(tfWidth, cc.xy(3, 2));
		cp.add(labHeight, cc.xy(2, 3));
		cp.add(tfHeight, cc.xy(3, 3));
		cp.add(okButton, cc.xyw(1, 4, 3));
	}
	
	public void actionPerformed(ActionEvent e)
	{
		String selection;
		int matrixWidth = 0, matrixHeight = 0;
			
		if (radRows.isSelected())
			selection = StringConstants.RowLayout;
		else
			selection = StringConstants.ColumnLayout;
				
		// get the width value
		try
		{
			matrixWidth = Integer.parseInt(tfWidth.getText());
		} catch (NumberFormatException nfe) 
		{
			matrixWidth = 0;
		};
				
		// get the height value
		try
		{
			matrixHeight = Integer.parseInt(tfHeight.getText());
		} catch (NumberFormatException nfe) 
		{
			matrixHeight = 0;
		};
				
		frame.setPixelLayout(selection);
		frame.updatePixelLayout(matrixWidth, matrixHeight);
		frame.updateGlyphLayout();
		frame.updateVisu();
		
		dispose();
	}
}
