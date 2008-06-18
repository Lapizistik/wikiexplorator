/**
 * 
 */
package wikiVisi;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author rene
 *
 */
public class PixelFrame extends JFrame
{
	protected Button bPos, bVorlage, bLoad, bSave;
	protected Choice cSort, cGlyphs, cPixels, cText;
	protected Jigsaw jig;
	
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
	
	public void init(Jigsaw j)
	{
		jig = j;
		// set layout and create the choices
		FlowLayout layout = new FlowLayout();
		setLayout(layout);
		cSort = new Choice();
		cSort.setName("cSort");
		cGlyphs = new Choice();
		cGlyphs.setName("cGlyphs");
		cPixels = new Choice();
		cPixels.setName("cPixels");
		cText = new Choice();
		cText.setName("cText");
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
		// add all elements
		add(cSort);
		add(cGlyphs);
		add(cPixels);
		add(cText);
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
				jig.updateGlyphLayout(selection,
						cSort.getSelectedItem());
			else if (name.equals("cPixels"))
				jig.updatePixelLayout(selection);
			else if (name.equals("cText"))
				jig.setTextSize(Integer.parseInt(selection));		
		}
		return true;
	}
}
