package visualizer.userInterface;
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

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import prefuse.Display;
import prefuse.util.display.ExportDisplayAction;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JRangeSlider;
import visualizer.StringConstants;
import visualizer.VisuMain;
import visualizer.display.GlyphTable;
import visualizer.display.PixelRenderer;

/**
 * The PixelFrame holds the whole visualization.
 * 
 * @author rene
 *
 */
public class PixelFrame extends JFrame
{
	protected int index;
	protected JButton zoomButton, duplicateButton, refButton;
	protected VisuMain vis;
	protected JLabel xHeader, yHeader, zHeader, pixelHeader,
			  startLabel, stopLabel, place;
	protected JTextField  xValue, yValue, zValue, pixelValue,
				tfSlidingWindow, tfHighest, tfReference;
	protected JRangeSlider timeSlider;
	protected JSlider gammaSlider, curveSlider;
	protected JScrollPane panel;
	protected ColorPanel colorPanel;
	protected PixelRenderer render;
	protected int startIndex, stopIndex, rowSize, columnSize;
	protected JMenuBar menuBar;
	protected JMenu fileMenu, glyphMenu, pixelMenu, prefMenu, helpMenu;
	protected JMenuItem[] fileItem, pixelItem, glyphItem, prefItem;
	protected MenuAction ma;
	//protected String pref;
	protected GlyphTable gt;
	//protected boolean bordersOn = true;
	//protected boolean inverted = true;
	protected int  space = 3;
	protected CellConstraints cc;
	protected double highestValue;
	
	public PixelFrame(String title, int number)
	{
		super(title);
		index = number;
		this.addWindowListener(new WindowAdapter()
	    {
	    	public void windowClosing(WindowEvent we)
	    	{
	    		vis.disposeFrame(index);
	    		dispose();
	    	}
	    });
		setupLayout();
	}
	
	public void setIndex(int newIndex)
	{
		index = newIndex;
	}
	
	public void setupLayout()
	{
		FormLayout layout = new FormLayout("10px, left:default:grow, 10px, right:default, 10px, left:default, 10px, left:default, 10px",
		"10px, center:default, 10px, center:default, 10px, center:default, 10px, center:default, 10px, center:default, 10px, center:default, 10px, center:default, 10px, center:default, 10px, center:default, 10px, center:default, 10px, center:default, 10px, center:default:grow, 10px, center:default, 10px, center:default, 10px");
		setLayout(layout);
		cc = new CellConstraints();
		xHeader = new JLabel(StringConstants.Nothing);
		xHeader.setForeground(Color.gray);
		xValue = new JTextField(StringConstants.Nothing);
		xValue.setColumns(15);
		yHeader = new JLabel(StringConstants.Nothing);
		yHeader.setForeground(Color.gray);
		yValue = new JTextField(StringConstants.Nothing);
		yValue.setColumns(15);
		zHeader = new JLabel(StringConstants.Nothing);
		zHeader.setForeground(Color.gray);
		zValue = new JTextField(StringConstants.Nothing);
		zValue.setColumns(15);
		tfSlidingWindow = new JTextField("0");
		tfSlidingWindow.setColumns(2);
		tfHighest = new JTextField();
		tfHighest.setColumns(5);
		tfHighest.setEditable(false);
		tfReference = new JTextField();
		tfReference.setColumns(5);
		pixelHeader = new JLabel("Wert");
		pixelHeader.setForeground(Color.gray);
		pixelValue = new JTextField(StringConstants.Nothing);
		pixelValue.setColumns(15);
		
		startLabel = new JLabel();
		stopLabel = new JLabel();
		place = new JLabel("Informationen:");
		
		timeSlider = new JRangeSlider(0, stopIndex, 0, stopIndex, SwingConstants.VERTICAL);
		gammaSlider = new JSlider(0, 100, 50);
		curveSlider = new JSlider(0, 10, 0);
		zoomButton = new JButton("zoom 1:1 <--> 4:1");
		duplicateButton = new JButton("duplizieren");
		panel = new JScrollPane();
		colorPanel = new ColorPanel();
		colorPanel.setPreferredSize(new Dimension(202, 40));
		
		//add(xHeader, cc.xy(6, 8));
		add(xValue, cc.xy(6, 10));
		//add(yHeader, cc.xy(6, 12));
		add(yValue, cc.xy(6, 12));
		//add(zHeader, cc.xy(6, 16));
		add(zValue, cc.xy(6, 14));
		add(pixelHeader, cc.xy(6, 16));
		add(pixelValue, cc.xy(6, 18));
		add(tfHighest, cc.xy(6, 20));
		add(tfReference, cc.xy(6, 22));
		add(gammaSlider, cc.xy(6, 26));
		add(curveSlider, cc.xy(6, 28));
		add(tfSlidingWindow, cc.xy(8, 28));
		add(colorPanel, cc.xy(6, 24));
		add(zoomButton, cc.xy(2, 2));
		add(duplicateButton, cc.xy(4, 2));
		//add(centerButton, cc.xy(6, 2));
		add(panel, cc.xywh(2, 4, 3, 22));
		add(place, cc.xy(6, 6));
		add(timeSlider, cc.xyw(2, 28, 3));
		add(startLabel, cc.xy(2, 26));
		add(stopLabel, cc.xy(4, 26));
		
		// menu 
		menuBar = new JMenuBar();
		ma = new MenuAction(this);
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
		fileItem = new JMenuItem[3];
		glyphItem = new JMenuItem[7];
		pixelItem = new JMenuItem[5];
		prefItem = new JMenuItem[7];
		fileItem[0] = new JMenuItem("Als Bilddatei exportieren");
		fileItem[1] = new JMenuItem("Datei laden");
		fileItem[2] = new JMenuItem("Beenden");
		addItemTo(fileItem[0], fileMenu, false);
		addItemTo(fileItem[1], fileMenu, true);
		addItemTo(fileItem[2], fileMenu, false);
		glyphItem[0] = new JRadioButtonMenuItem(StringConstants.RowLayout, new ImageIcon(getClass().getResource("/pics/row.gif")));
		glyphItem[1] = new JRadioButtonMenuItem(StringConstants.ZLayout, new ImageIcon(getClass().getResource("/pics/zcurve.gif")));
		glyphItem[2] = new JRadioButtonMenuItem(StringConstants.MyZLayout, new ImageIcon(getClass().getResource("/pics/myz.gif")));
		glyphItem[3] = new JRadioButtonMenuItem(StringConstants.TableLayout, new ImageIcon(getClass().getResource("/pics/table.gif")));
		glyphItem[4] = new JRadioButtonMenuItem(StringConstants.OptimizedTableLayout, new ImageIcon(getClass().getResource("/pics/table.gif")));
		glyphItem[5] = new JRadioButtonMenuItem(StringConstants.MDSLayout);
		glyphItem[6] = new JRadioButtonMenuItem(StringConstants.JigsawLayout);
		ButtonGroup glyphGroup = new ButtonGroup();
		for (int i = 0; i < glyphItem.length; i++)
			glyphGroup.add(glyphItem[i]);
		glyphItem[0].setSelected(true);
		pixelItem[0] = new JRadioButtonMenuItem(StringConstants.RowLayout, new ImageIcon(getClass().getResource("/pics/row.gif")));
		pixelItem[1] = new JRadioButtonMenuItem(StringConstants.ColumnLayout, new ImageIcon(getClass().getResource("/pics/row.gif")));
		pixelItem[2] = new JRadioButtonMenuItem(StringConstants.ZLayout, new ImageIcon(getClass().getResource("/pics/zcurve.gif")));
		pixelItem[3] = new JRadioButtonMenuItem(StringConstants.MyZLayout, new ImageIcon(getClass().getResource("/pics/myz.gif")));
		pixelItem[4] = new JRadioButtonMenuItem(StringConstants.HilbertLayout, new ImageIcon(getClass().getResource("/pics/hilbert.gif")));
		ButtonGroup pixelGroup = new ButtonGroup();
		pixelGroup.add(pixelItem[0]);
		pixelGroup.add(pixelItem[1]);
		pixelGroup.add(pixelItem[2]);
		pixelGroup.add(pixelItem[3]);
		pixelGroup.add(pixelItem[4]);
		pixelItem[0].setSelected(true);
		prefItem[0] = new JRadioButtonMenuItem(StringConstants.GrayScale, new ImageIcon(getClass().getResource("/pics/gray.gif")));
		prefItem[1] = new JRadioButtonMenuItem(StringConstants.HeatScale, new ImageIcon(getClass().getResource("/pics/heat.gif")));
		ButtonGroup colorGroup = new ButtonGroup();
		colorGroup.add(prefItem[0]);
		colorGroup.add(prefItem[1]);
		prefItem[0].setSelected(true);
		prefItem[2] = new JCheckBoxMenuItem(StringConstants.GlyphBorders);
		prefItem[2].setSelected(true);
		prefItem[3] = new JMenu(StringConstants.GlyphSpaces);
		((JMenu)prefItem[3]).getPopupMenu().setName("spaceMenu");
		addItemTo(new JMenuItem("0"), (JMenu)prefItem[3], false);
		addItemTo(new JMenuItem("1"), (JMenu)prefItem[3], false);
		addItemTo(new JMenuItem("2"), (JMenu)prefItem[3], false);
		addItemTo(new JMenuItem("3"), (JMenu)prefItem[3], false);
		addItemTo(new JMenuItem("4"), (JMenu)prefItem[3], false);
		addItemTo(new JMenuItem("5"), (JMenu)prefItem[3], false);
		prefItem[4] = new JMenu(StringConstants.BackgroundColor);
		((JMenu)prefItem[4]).getPopupMenu().setName("backgroundMenu");
		addItemTo(new JMenuItem(StringConstants.BackWhite), (JMenu)prefItem[4], false);
		addItemTo(new JMenuItem(StringConstants.BackGray), (JMenu)prefItem[4], false);
		addItemTo(new JMenuItem(StringConstants.BackBlue), (JMenu)prefItem[4], false);
		prefItem[5] = new JCheckBoxMenuItem(StringConstants.ColorsInverted);
		prefItem[5].setSelected(true);
		prefItem[6] = new JMenuItem(StringConstants.AuthorFilter);
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
		//pref = StringConstants.GrayScale;
		setJMenuBar(menuBar);
		xHeader.setVisible(true);
		xValue.setVisible(true);
		yHeader.setVisible(true);
		yValue.setVisible(true);
		zHeader.setVisible(true);
		zValue.setVisible(true);
		pixelHeader.setVisible(true);
		pixelValue.setVisible(true);
		pack();
		
		
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
		gt = tab;
		final Display display = dis;
		colorPanel.setRenderer(render);
		xHeader.setText(gt.getXAxisTitle());
		yHeader.setText(gt.getYAxisTitle());
		zHeader.setText(gt.getZAxisTitle());
		tfHighest.setText(Double.toString(gt.getHighest()));
		tfReference.setText(Double.toString(gt.getReference()));
		// set layout and create the choices
		//FormLayout layout = new FormLayout("10px, left:default, 10px, left:default, 10px, left:default, 10px, left:default, 10px, left:default:grow, 10px, left:default, 10px", 
		//"10px, center:default, 10px, center:default, 10px, center:default, 10px, center:default:, 10px, center:default:grow, 10px, center:default, 10px, center:default, 10px");
		// the labels
		setStartIndex(0);
		setStopIndex(gt.getPixelCount() - 1);
		setRange(startIndex, stopIndex);
		timeSlider.setSize(startIndex, stopIndex);
		timeSlider.setMaximum(stopIndex);
		timeSlider.setHighValue(stopIndex);
		// remove old listeners
		if (timeSlider.getMouseListeners().length > 1)
		{
			timeSlider.removeMouseListener(timeSlider.getMouseListeners()[1]);
			timeSlider.removeMouseMotionListener(timeSlider.getMouseMotionListeners()[1]);
			gammaSlider.removeMouseListener(gammaSlider.getMouseListeners()[1]);
			curveSlider.removeMouseListener(curveSlider.getMouseListeners()[1]);
			curveSlider.removeMouseMotionListener(curveSlider.getMouseMotionListeners()[1]);
			if (zoomButton.getActionListeners().length > 0)
			{
				zoomButton.removeActionListener(zoomButton.getActionListeners()[0]);
				duplicateButton.removeActionListener(duplicateButton.getActionListeners()[0]);
				tfReference.removeActionListener(tfReference.getActionListeners()[0]);
			}
		}
		// the time slider
		timeSlider.addMouseListener(new MouseAdapter() 
		{
            public void mousePressed(MouseEvent e) 
            {
            }

            public void mouseReleased(MouseEvent e) 
            {
              	setStartIndex(timeSlider.getLowValue());
            	setStopIndex(timeSlider.getHighValue());
            	gt.updateMeans(startIndex, stopIndex);
             	updateGlyphTable();
            	gt.updateVisu();
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
		// curve slider
		curveSlider.addMouseListener(new MouseAdapter() 
		{
            public void mousePressed(MouseEvent e) 
            {
            	tfSlidingWindow.setText(Integer.toString(curveSlider.getValue()));
            }

            public void mouseReleased(MouseEvent e) 
            {
            	tfSlidingWindow.setText(Integer.toString(curveSlider.getValue()));
                gt.updateSlidingWindow(curveSlider.getValue());
             	gt.updateVisu();
        	}
        });
		curveSlider.addMouseMotionListener(new MouseAdapter() 
		{
            public void mouseDragged(MouseEvent e) 
            {
               	tfSlidingWindow.setText(Integer.toString(curveSlider.getValue()));
            }
        });
		// gamma slider
		gammaSlider.addMouseListener(new MouseAdapter() 
		{
            public void mousePressed(MouseEvent e) 
            {
            }

            public void mouseReleased(MouseEvent e) 
            {
              	gt.updateVisu();
              	updateColorPanel();
        	}
        });
		// the reference value textfield
		tfReference.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
            {
				try
				{
					double newRef = Double.parseDouble(tfReference.getText());
					gt.setReference(newRef);
					gt.updateSlidingWindow(curveSlider.getValue());
				} catch (Exception exc) {tfReference.setText(tfHighest.getText());}
				updateVisu();
            }
		});
		// Button for zooming
		zoomButton.addActionListener(new ActionListener() 
		{
			//Display d = dis;
            public void actionPerformed(ActionEvent e) 
            {
            	double scale = display.getScale();
            	if (scale == 1) // zoom in
            		display.zoomAbs(display.getLocation(), 4d/scale);
            	else // zoom to 1:1
            		display.zoomAbs(display.getLocation(), 1d/scale);
            	updateVisu();
            }
		});
		// the duplicate button
		duplicateButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
            {
            	vis.duplicate(index);
            }
		});
		// Panel that holds the display
		panel.getViewport().add(dis);
		// reset some values
		resetFrame();
	}
	
	public GlyphTable getGlyphTable()
	{
		return gt;
	}
	
	public void setSlidingRange(int slidingRange)
	{
		curveSlider.setValue(slidingRange);
	}
	
	public int getSlidingRange()
	{
		return curveSlider.getValue();
	}
	
	public void updateGlyphTable()
	{
		gt.updatePixelLayout(this);
		gt.updateGlyphLayout(this);
	}
	
	public void resetFrame()
	{
		glyphMenu.getItem(0).setSelected(true);
		pixelMenu.getItem(0).setSelected(true);
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
		return prefItem[2].isSelected();
	}
	
	public void setBorderOn(boolean bol)
	{
		prefItem[2].setSelected(bol);
	}
	
	/**
	 * True if the glyph borders shall be drawn, false else.
	 */
	/*public void setBorders(boolean bol)
	{
		return prefItem[2].isSelected();
	}*/
	
	/**
	 * Refresh visualization.
	 */
	public void updateVisu()
	{
		gt.updateVisu();
	}
	
	public void updateColorPanel()
	{
		colorPanel.repaint();
	}
	
	/**
	 * Re-arrange the glyphs.
	 */
	public void updateGlyphLayout()
	{
		gt.updateGlyphLayout(this);
	}
	
	/**
	 * Re-arrange the pixels. 
	 */
	public void updatePixelLayout()
	{
		gt.updatePixelLayout(this);
		gt.updateGlyphLayout(this);
	}
	
	public String getColor()
	{
		String col;
		if (prefMenu.getItem(0).isSelected())
			 col = (StringConstants.GrayScale);
		else
			col = (StringConstants.HeatScale);
		
		return col;
	}
	
	/**
	 * Set content of the glyphDesc label.
	 */
	public void setXDesc(String s)
	{
		if (s.length() > 25)
			s = s.substring(0, 25);
		xValue.setText(s);
	}
	
	/**
	 * Set content of the glyphDesc label.
	 */
	public void setYDesc(String s)
	{
		if (s.length() > 25)
			s = s.substring(0, 25);
		yValue.setText(s);
	}
	
	/**
	 * Set content of the glyphDesc label.
	 */
	public void setZDesc(String s)
	{
		if (s.length() > 25)
			s = s.substring(0, 25);
		zValue.setText(s);
	}
	
	/**
	 * Set content of the glyphDesc label.
	 */
	public void setPixelValue(double d)
	{
		pixelValue.setText(Double.toString(d));
	}
	
	public String getGlyphLayout()
	{
		String selection = "";
		for (int i = 0; i < glyphItem.length; i++)
			if (glyphItem[i].isSelected())
				selection = glyphItem[i].getText();
		return selection;
	}
	
	public void setSelectedColorScale(int index)
	{
		prefItem[index].setSelected(true);
	}
	
	public void setSelectedGlyphLayoutIndex(int index)
	{
		glyphItem[index].setSelected(true);
	}
	
	public void setSelectedPixelLayoutIndex(int index)
	{
		pixelItem[index].setSelected(true);
	}
	
	public void setInverted(boolean inv)
	{
		prefItem[5].setSelected(inv);
	}
	
	public int getSelectedPixelLayoutIndex()
	{
		int selected = 0;
		
		for (int i = 0; i < pixelItem.length; i++)
			if (pixelItem[i].isSelected())
				selected = i;
		
		return selected;
	}
	
	public int getSelectedGlyphLayoutIndex()
	{
		int selected = 0;
		
		for (int i = 0; i < glyphItem.length; i++)
			if (glyphItem[i].isSelected())
				selected = i;
		
		return selected;
	}
	
	public String getPixelLayout()
	{
		String selection = "";
		for (int i = 0; i < pixelItem.length; i++)
			if (pixelItem[i].isSelected())
				selection = pixelItem[i].getText();
		
		return selection;
	}
	
	/*public void setPref(String s)
	{
		pref = s;
	}*/
	
	public void setStartIndex(int start)
	{
		startIndex = start;
		startLabel.setText(gt.getPixelDescAt(start));
	}
	
	public void setStopIndex(int stop)
	{
		stopIndex = stop;
		stopLabel.setText(gt.getPixelDescAt(stop));
	}
	
	public void setRange(int start, int stop)
	{
		setStartIndex(start);
		setStopIndex(stop);
		// move the range slider
		timeSlider.setLowValue(start);
		timeSlider.setHighValue(stop);
		startLabel.setText(gt.getPixelDescAt(start));
		stopLabel.setText(gt.getPixelDescAt(stop));
	}
	
	public void addItemTo(JMenuItem it, JMenu men, boolean sep)
	{
		it.addActionListener(ma);
		men.add(it);
		if (sep)
			men.add(new JSeparator(SwingConstants.HORIZONTAL));
	}
	
	public void export()
	{
		PixelExportAction export = new PixelExportAction(gt.getVisualization().getDisplay(0));
		export.actionPerformed(new ActionEvent(this, 0, ""));
	}
	
	public void initReload(String filename)
	{
		vis.init(filename, index);
		//dispose();
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public void setSpace(int s)
	{
		space = s;
	}
	
	public int getSpace()
	{
		return space;
	}
	
	public void setBackColor(Color c)
	{
		gt.getVisualization().getDisplay(0).setBackground(c);
	}
	
	/*public void setInverted(boolean inv)
	{
		inverted = inv;
	}*/
	
	public boolean getInverted()
	{
		return prefItem[5].isSelected();
	}
	
	public double getGamma()
	{
		return (gammaSlider.getValue() / 100d);
	}
	
	public void setRowSize(int rs)
	{
		rowSize = rs;
	}
	
	public void setColumnSize(int cs)
	{
		columnSize = cs;
	}
	
	public int getRowSize()
	{
		return rowSize;
	}
	
	public int getColumnSize()
	{
		return columnSize;
	}
}

