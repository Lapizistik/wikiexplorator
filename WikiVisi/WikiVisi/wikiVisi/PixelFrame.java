/**
 * 
 */
package wikiVisi;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import prefuse.Display;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JRangeSlider;

/**
 * @author rene
 *
 */
public class PixelFrame extends JFrame
{
	protected Button bPos, bVorlage, bLoad, bSave;
	protected Choice cSort, cGlyphs, cPixels, cText;
	protected VisuMain vis;
	protected JLabel info;
	protected JRangeSlider timeSlider;
	
	public PixelFrame(String title)
	{
		super(title);
		this.addWindowListener(new WindowAdapter()
	    {
	    	public void windowClosing(WindowEvent we)
	    	{
	    		System.exit(0);
	    	}
	    });
	}
	
	public void init(VisuMain v, Display dis)
	{
		vis = v;
		// set layout and create the choices
		FormLayout layout = new FormLayout("10px, left:default, 10px, left:default, 10px, left:default, 10px, left:default, 10px, left:default:grow, 10px, left:default, 10px", 
		"10px, center:default, 10px, center:default, 10px, center:default, 10px, center:default:, 10px, center:default:grow, 10px, center:default, 10px, center:default, 10px");
		setLayout(layout);
		CellConstraints cc = new CellConstraints();
		cSort = new Choice();
		cSort.setName("cSort");
		cGlyphs = new Choice();
		cGlyphs.setName("cGlyphs");
		cPixels = new Choice();
		cPixels.setName("cPixels");
		cText = new Choice();
		cText.setName("cText");
		// the labels
		info = new JLabel("nothing");
		info.setForeground(Color.gray);
		JLabel place = new JLabel("<<<<<<Platzhalter>>>>>>");
		// the slider
		timeSlider = new JRangeSlider(1, 100, 1, 99, SwingConstants.VERTICAL);
		// add entries to the choices
		cSort.addItem("author");
		cSort.addItem("mean");
		cSort.select("author");
		cGlyphs.addItem("Simple Layout");
		cGlyphs.addItem("Morton Z-Curve");
		cGlyphs.addItem("Flexible Z-Curve");
		cGlyphs.select("Simple Layout");
		cPixels.addItem("Simple Layout");
		cPixels.addItem("Morton Z-Curve");
		cPixels.addItem("Flexible Z-Curve");
		cPixels.addItem("Hilbert Curve");
		cPixels.addItem("Line Layout");
		cPixels.select("Simple Layout");
		cText.addItem("0");
		cText.addItem("3");
		cText.addItem("6");
		cText.addItem("9");
		cText.addItem("12");
		cText.addItem("15");
		cText.select("12");
		// Panel that holds the display
		JScrollPane panel = new JScrollPane();
		panel.getViewport().add(dis);
		// add all elements
		add(cSort, cc.xy(2, 2));
		add(cGlyphs, cc.xy(4, 2));
		add(cPixels, cc.xy(6, 2));
		add(cText, cc.xy(8, 2));
		add(info, cc.xyw(10, 2, 3));
		add(panel, cc.xywh(2, 4, 9, 8));
		add(place, cc.xy(12, 6));
		add(timeSlider, cc.xyw(2, 14, 9));
		info.setVisible(true);
		pack();
	}
	
	public boolean action(Event ev, Object obj)
	{
		if (ev.target instanceof Choice)
		{
			Choice c = (Choice)ev.target;
			String name = c.getName();
			String selection = c.getSelectedItem();
			
			if (name.equals("cGlyphs"))
			{
				vis.updateGlyphLayout(selection);
				vis.updatePixelLayout(cPixels.getSelectedItem());
				vis.updateVisu();
			}
			else if (name.equals("cPixels"))
			{
				vis.updatePixelLayout(selection);
				vis.updateGlyphSize();
				vis.updateGlyphLayout(cGlyphs.getSelectedItem());
				vis.updateVisu();
			}
			else if (name.equals("cSort"))
			{
				vis.setSort(selection);
				vis.updateGlyphLayout(cGlyphs.getSelectedItem());
				vis.updatePixelLayout(cPixels.getSelectedItem());
				vis.updateVisu();
			}
			else if (name.equals("cText"))
			{
				vis.setTextSize(Integer.parseInt(selection));		
				vis.updateVisu();
			}
		}
		return true;
	}
	
	public void updateVisu()
	{
		vis.updateVisu();
	}
	
	public void setInfo(String s)
	{
		info.setText(s);
	}
}
