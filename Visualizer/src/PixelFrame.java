/**
 * 
 */

import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import prefuse.Display;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JRangeSlider;

/**
 * The PixelFrame holds the whole visualization.
 * 
 * @author rene
 *
 */
public class PixelFrame extends JFrame
{
	protected Button bPos, bVorlage, bLoad, bSave;
	protected VisuMain vis;
	protected JLabel info, startLabel, stopLabel;
	protected JRangeSlider timeSlider;
	protected PixelRenderer render;
	protected int startIndex, stopIndex;
	protected JMenuBar menuBar;
	protected JMenu fileMenu, glyphMenu, pixelMenu, prefMenu, helpMenu;
	protected MenuAction ma;
	protected String glyph, pixel ,pref;
	protected GlyphTable gt;
	protected boolean bordersOn = true;
	
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
	
	/**
	 * Set up the PixelFrame. It will need data from the 
	 * VisuMain, the Display, PixelRenderer and GlyphTable,
	 * so references to these objects have to be delivered.
	 */
	public void init(VisuMain v, Display dis, PixelRenderer ren,
			GlyphTable tab)
	{
		vis = v;
		render = ren;
		ma = new MenuAction(this);
		gt = tab;
		// set layout and create the choices
		//FormLayout layout = new FormLayout("10px, left:default, 10px, left:default, 10px, left:default, 10px, left:default, 10px, left:default:grow, 10px, left:default, 10px", 
		//"10px, center:default, 10px, center:default, 10px, center:default, 10px, center:default:, 10px, center:default:grow, 10px, center:default, 10px, center:default, 10px");
		FormLayout layout = new FormLayout("10px, left:default:grow, 10px, right:default, 10px, left:default, 10px",
				"10px, center:default, 10px, center:default, 10px, center:default, 10px, center:default, 10px, center:default, 10px, center:default, 10px, center:default, 10px, center:default:grow, 10px, center:default, 10px, center:default, 10px");
		setLayout(layout);
		CellConstraints cc = new CellConstraints();
		// the labels
		info = new JLabel("nothing");
		info.setForeground(Color.gray);
		startLabel = new JLabel();
		stopLabel = new JLabel();
		setStartIndex(0);
		setStopIndex(gt.getZAxisCount() - 1);
		JLabel place = new JLabel("<<<<<<Platzhalter>>>>>>");
		// the slider
		timeSlider = new JRangeSlider(0, stopIndex, 0, stopIndex, SwingConstants.VERTICAL);
		timeSlider.addMouseListener(new MouseAdapter() 
		{
            public void mousePressed(MouseEvent e) 
            {
            }

            public void mouseReleased(MouseEvent e) 
            {
              	setStartIndex(timeSlider.getLowValue());
            	setStopIndex(timeSlider.getHighValue());
            	vis.updatePixelLayout(pixel);
        		vis.updateGlyphSize();
        		vis.updateGlyphLayout(glyph);
        		vis.updateVisu();
            }
        });
		timeSlider.addMouseMotionListener(new MouseAdapter() 
		{
            public void mouseDragged(MouseEvent e) 
            {
               	setStartIndex(timeSlider.getLowValue());
            	setStopIndex(timeSlider.getHighValue());
            }
        });
		// Panel that holds the display
		JScrollPane panel = new JScrollPane();
		panel.getViewport().add(dis);
		// add all elements
		//add(cSort, cc.xy(2, 2));
		//add(cGlyphs, cc.xy(2, 2));
		//add(cPixels, cc.xy(4, 2));
		//add(cText, cc.xy(6, 2));
		//add(cColor, cc.xy(8, 2));
		add(info, cc.xy(6, 8));
		add(panel, cc.xywh(2, 4, 3, 14));
		add(place, cc.xy(6, 6));
		add(timeSlider, cc.xyw(2, 20, 3));
		add(startLabel, cc.xy(2, 18));
		add(stopLabel, cc.xy(4, 18));
		// menu test
		menuBar = new JMenuBar();
		menuBar.setLayout(new FlowLayout(FlowLayout.LEFT));
		fileMenu = new JMenu("Datei");
		fileMenu.getPopupMenu().setName("fileMenu");
		glyphMenu = new JMenu("Glyphen Layout");
		glyphMenu.getPopupMenu().setName("glyphMenu");
		pixelMenu = new JMenu("Pixel Layout");
		pixelMenu.getPopupMenu().setName("pixelMenu");
		prefMenu = new JMenu("Darstellung");
		prefMenu.getPopupMenu().setName("prefMenu");
		helpMenu = new JMenu("Hilfe");
		helpMenu.getPopupMenu().setName("helpMenu");
		JMenuItem[] glyphItem = new JMenuItem[3];
		JMenuItem[] pixelItem = new JMenuItem[5];
		JMenuItem[] prefItem = new JMenuItem[3];
		glyphItem[0] = new JMenuItem(ConstantStrings.RowLayout, new ImageIcon(getClass().getResource("/pics/row.gif")));
		glyphItem[1] = new JMenuItem(ConstantStrings.ZLayout, new ImageIcon(getClass().getResource("/pics/zcurve.gif")));
		glyphItem[2] = new JMenuItem(ConstantStrings.MyZLayout, new ImageIcon(getClass().getResource("/pics/myz.gif")));
		pixelItem[0] = new JMenuItem(ConstantStrings.RowLayout, new ImageIcon(getClass().getResource("/pics/row.gif")));
		pixelItem[1] = new JMenuItem(ConstantStrings.ZLayout, new ImageIcon(getClass().getResource("/pics/zcurve.gif")));
		pixelItem[2] = new JMenuItem(ConstantStrings.MyZLayout, new ImageIcon(getClass().getResource("/pics/myz.gif")));
		pixelItem[3] = new JMenuItem(ConstantStrings.HilbertLayout, new ImageIcon(getClass().getResource("/pics/hilbert.gif")));
		pixelItem[4] = new JMenuItem(ConstantStrings.FatRowLayout, new ImageIcon(getClass().getResource("/pics/fatrow.gif")));
		prefItem[0] = new JMenuItem(ConstantStrings.GrayScale, new ImageIcon(getClass().getResource("/pics/gray.gif")));
		prefItem[1] = new JMenuItem(ConstantStrings.HeatScale, new ImageIcon(getClass().getResource("/pics/heat.gif")));
		prefItem[2] = new JCheckBoxMenuItem(ConstantStrings.GlyphBorders);
		prefItem[2].setSelected(true);
		for (int i = 0; i < glyphItem.length; i++)
			addItemTo(glyphItem[i], glyphMenu, i < glyphItem.length - 1);
		for (int i = 0; i < pixelItem.length; i++)
			addItemTo(pixelItem[i], pixelMenu, i < pixelItem.length - 1);
		for (int i = 0; i < prefItem.length; i++)
			addItemTo(prefItem[i], prefMenu, i < prefItem.length - 1);
		menuBar.add(fileMenu);
		menuBar.add(new JSeparator(SwingConstants.VERTICAL));
		menuBar.add(glyphMenu);
		menuBar.add(new JSeparator(SwingConstants.VERTICAL));
		menuBar.add(pixelMenu);
		menuBar.add(new JSeparator(SwingConstants.VERTICAL));
		menuBar.add(prefMenu);
		menuBar.add(new JSeparator(SwingConstants.VERTICAL));
		menuBar.add(helpMenu);
		glyph = ConstantStrings.RowLayout;
		pixel = ConstantStrings.RowLayout;
		pref = ConstantStrings.GrayScale;
		setJMenuBar(menuBar);
		info.setVisible(true);
		pack();
	}
	
	/**
	 * Return the index where to start visualizing the data.
	 */
	public int getStartIndex()
	{
		return startIndex;
	}
	
	/**
	 * Return the index where to stop visualizing the data.
	 */
	public int getStopIndex()
	{
		return stopIndex;
	}
	
	/**
	 * Return whether borders of thy glyphs are to be drawn
	 * or not.
	 */
	public boolean isBorderOn()
	{
		return bordersOn;
	}
	
	/**
	 * True if the glyph borders shall be drawn, false else.
	 */
	public void setBorders(boolean bol)
	{
		bordersOn = bol;
	}
	
	/**
	 * Refresh visualization.
	 */
	public void updateVisu()
	{
		vis.updateVisu();
	}
	
	/**
	 * Re-arrange the glyphs.
	 */
	public void updateGlyphLayout()
	{
		vis.updateGlyphLayout(glyph);
	}
	
	/**
	 * Re-arrange the pixels. 
	 */
	public void updatePixelLayout()
	{
		vis.updatePixelLayout(pixel);
		vis.updateGlyphSize();
		vis.updateGlyphLayout(glyph);
	}
	
	/**
	 * Tell the Renderer to switch the color mode.
	 */
	public void updateColors()
	{
		render.setColorMode(pref);
	}
	
	/**
	 * Set content of the info label.
	 */
	public void setInfo(String s)
	{
		info.setText(s);
	}
	
	public void setGlyph(String s)
	{
		glyph = s;
	}
	
	public void setPixel(String s)
	{
		pixel = s;
	}
	
	public void setPref(String s)
	{
		pref = s;
	}
	
	public void setStartIndex(int start)
	{
		startIndex = start;
		startLabel.setText(gt.getZDescAt(start));
	}
	
	public void setStopIndex(int stop)
	{
		stopIndex = stop;
		stopLabel.setText(gt.getZDescAt(stop));
	}
	
	public void setRange(int start, int stop)
	{
		setStartIndex(start);
		setStopIndex(stop);
		// move the range slider
		timeSlider.setLowValue(start);
		timeSlider.setHighValue(stop);
		startLabel.setText(gt.getZDescAt(start));
		stopLabel.setText(gt.getZDescAt(stop));
	}
	
	public void addItemTo(JMenuItem it, JMenu men, boolean sep)
	{
		it.addActionListener(ma);
		men.add(it);
		if (sep)
			men.add(new JSeparator(SwingConstants.HORIZONTAL));
	}
}

