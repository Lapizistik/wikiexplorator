package visualizer.userInterface;

import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.TreeMap;

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
 * @author rene
 *
 */
public class ColorDialog extends JDialog implements ActionListener
{
	private PixelFrame frame;
	private GlyphTable gt;
	private JTextField tfGammaDesc, tfGamma;
	private ColorCurvePanel drawPanel;
	private ColorPanel colorPanel;
	private DistroPanel distPanel;
	private JButton okButton, gammaButton;
	
	public ColorDialog(PixelFrame pFrame, String name)
	{
		super(pFrame, name);
		frame = pFrame;
		gt = frame.getGlyphTable();
		init();
	}
	
	public void init()
	{
		Container cp = getContentPane();
		FormLayout layout = new FormLayout("center:default, 10px, center:default, 10px, center:default",
		"center:default, 10px, center:default:grow, 10px, center:default:grow, 10px, center:default, 10px, center:default");
		cp.setLayout(layout);
		CellConstraints cc = new CellConstraints();
		
		tfGammaDesc = new JTextField("Gamma");
		tfGammaDesc.setEditable(false);
		tfGamma = new JTextField("1.0");
		tfGamma.setColumns(3);
		cp.add(tfGammaDesc, cc.xy(1, 1));
		cp.add(tfGamma, cc.xy(3, 1));
		gammaButton = new JButton("OK");
		gammaButton.addActionListener(this);
		cp.add(gammaButton, cc.xy(5, 1));
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cp.add(okButton, cc.xyw(1, 9, 5));
		
		distPanel = new DistroPanel(gt);
		distPanel.setPreferredSize(new Dimension(200, 100));
		distPanel.setMaximumSize(new Dimension(200, 100));
		cp.add(distPanel, cc.xy(3, 3));
		
		colorPanel = new ColorPanel();
		colorPanel.setRenderer(frame.getRenderer());
		colorPanel.setPreferredSize(new Dimension(200, 100));
		colorPanel.setMaximumSize(new Dimension(200, 200));
		cp.add(colorPanel, cc.xy(3, 7));
		
		drawPanel = new ColorCurvePanel(colorPanel, frame.getRenderer());
		//drawPanel.setRenderer(frame.getRenderer());
		drawPanel.setPreferredSize(new Dimension(200, 200));
		drawPanel.setMaximumSize(new Dimension(200, 200));
		cp.add(drawPanel, cc.xy(3, 5));
		//add(new JPanel().add(okButton));
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == gammaButton)
		{
			double gamma = 1.0;
			try
			{
				gamma = Double.parseDouble(tfGamma.getText());
			} catch (Exception exc) {}
			drawPanel.drawGamma(gamma);
			colorPanel.repaint();
		}
		else if (e.getSource() == okButton)
		{
			frame.updateColorPanel();
			frame.updateVisu();
			dispose();
		}
	}
}