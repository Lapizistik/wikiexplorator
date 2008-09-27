package visualizer.userInterface;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import visualizer.StringConstants;

/**
 *
 * This is a little dialog that pops up to allow the 
 * user to specify the exact size of a row-by-row or
 * column-by-column-layout
 * 
 * @author Rene Wegener
 */
public class MatrixOptionsDialog extends JDialog implements ActionListener
{
	PixelFrame frame;
	private JTextField tfWidth;
	private String type;
	
	/**
	 * create a new MatrixOptionsDialog
	 * @param pFrame the PixelFrame calling this dialog
	 * @param name name of the dialog's frame
	 */
	public MatrixOptionsDialog(PixelFrame pFrame, String name)
	{
		super(pFrame, name);
		frame = pFrame;
		type = name;
		init();
	}
	
	public void init()
	{
		if (type.equals(StringConstants.RowLayout))
			type = "Zeile";
		else
			type = "Spalte";
		JLabel labWidth = new JLabel("Groesse jeder " + type);
		JTextArea taInfo = new JTextArea(3, 30);
		taInfo.setLineWrap(true);
		taInfo.setWrapStyleWord(true);
		taInfo.setText("Bitte geben Sie eine Groesse fuer jede " + type + " an.");
		taInfo.append("Falls Sie keinen Wert angeben, wird dieser automatisch berechnet.");
		taInfo.setSize(taInfo.getMaximumSize());
		taInfo.setEditable(false);
		taInfo.setBackground(Color.WHITE);
		tfWidth = new JTextField(3);
		JButton okButton = new JButton("OK");
		okButton.addActionListener(this);
		Container cp = getContentPane();
		FormLayout layout = new FormLayout("left:default:grow, center:default:grow, center:default:grow",
		"center:default:grow, center:default:grow, center:default:grow, center:default:grow");
		cp.setLayout(layout);
		CellConstraints cc = new CellConstraints();
		cp.add(taInfo, cc.xyw(1, 1, 3));
		cp.add(labWidth, cc.xy(2, 2));
		cp.add(tfWidth, cc.xy(3, 2));
		cp.add(okButton, cc.xyw(1, 4, 3));
	}
	
	public void actionPerformed(ActionEvent e)
	{
		int width = 0, matrixWidth = 0, matrixHeight = 0;
			
		// get the width value
		try
		{
			width = Integer.parseInt(tfWidth.getText());
		} catch (NumberFormatException nfe) 
		{
			width = 0;
		};
		if (type.equals("Zeile"))
			matrixWidth = width;
		else
			matrixHeight = width;
		
		frame.setRowSize(matrixWidth);
		frame.setColumnSize(matrixHeight);
		frame.updatePixelLayout();
		frame.updateGlyphLayout();
		frame.updateVisu();

		dispose();
	}
}
