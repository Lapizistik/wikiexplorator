package visualizer.display;


import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import prefuse.Visualization;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import visualizer.StringConstants;
import visualizer.userInterface.PixelFrame;

/**
 * One of the central classes of the program. GlyphTable
 * is a VisualTable which means that it holds all VisualItems
 * that represent glyph icons. In addition it contains
 * general data like the z axis description and the pixel
 * positions. So the GlyphTable class maps between data
 * and display.
 * 
 * @author Rene Wegener
 *
 */
public class GlyphTable extends VisualTable
{
	protected String title, xAxisTitle, yAxisTitle, zAxisTitle;
	protected int[] pixelX; // x-coordinates of the pixels
	protected int[] pixelY; // x-coordinates of the pixels
	protected int glyphWidth, glyphHeight, xAxisCount, 
				yAxisCount, zAxisCount;
	protected String[] xAxisDesc, yAxisDesc, zAxisDesc;
	protected int[] distribution; // stores the frequency distribution
	// reference value is used as value for the last color of the color scale 
	protected double highest = 0, reference = 0; 
	// stores whether at a position in a glyph there's
	// a pixel (value) or whether it's empty
	protected boolean[][] glyphBounds;
	
	/**
	 * Create an new GlyphTable 
	 */
	public GlyphTable(Visualization vis, String str)
	{
		super(vis, str);
		distribution = new int[101];
	}
	
	/**
	 * set the highest data value
	 * @param h the new highest value in the whole data
	 */
	public void setHighest(double h)
	{
		highest = h;
	}
	
	/**
	 * @return the highest data value
	 */
	public double getHighest()
	{
		return highest;
	}
	
	/**
	 * set the reference value; this will change the color
	 * scale
	 * @param r the new reference value
	 */
	public void setReference(double r)
	{
		reference = r;
	}
	
	/**
	 * @return the reference value
	 */
	public double getReference()
	{
		return reference;
	}
	
	/**
	 * @return true if this GlyphTable contains 
	 * three-dimensional relationship data or 
	 * false if it contains two-dimensional author data
	 */
	public boolean isCube()
	{
		if (zAxisCount > 0)
			return true;
		else
			return false;
	}
	
	/**
	 * @return the number of authors
	 */
	public int getAuthorCount()
	{
		return yAxisCount;
	}
	
	/**
	 * creates the x-, y- and z-axis of this GlyphTable
	 * @param xAxis number of x-axis entries
	 * @param yAxis number of y-axis entries
	 * @param zAxis number of z-axis entries (0 if no z-axis exists)
	 * 
	 */
	public void init(int xAxis, int yAxis, int zAxis)
	{
		xAxisCount = xAxis;
		yAxisCount = yAxis;
		zAxisCount = zAxis;
		xAxisDesc = new String[xAxis];
		yAxisDesc = new String[yAxis];
		if (zAxis > 0) // cube
		{
			pixelX = new int[zAxisCount];
			pixelY = new int[zAxisCount];
			zAxisDesc = new String[zAxis];
		}
		else // table
		{
			pixelX = new int[xAxisCount];
			pixelY = new int[xAxisCount];
		}
		glyphWidth = 0;
		glyphHeight = 0;
	}
	
	/** 
	 * @return the maximum amount of pixels needed to
	 * display the author's name
	 */
	public int getMaxStringLength()
	{
		int fontHeight = getGlyphHeight() - 1;
		int space = 0;
		for (int y = 0; y < yAxisCount; y++)
		{
			String desc = yAxisDesc[y];
			Font font =  new Font("Arial", Font.PLAIN, fontHeight);
			Graphics2D g = (Graphics2D)getVisualization().getDisplay(0).getGraphics();
			int stringWidth = (int)(font.getStringBounds(desc, g.getFontRenderContext())).getWidth();
			if (stringWidth > space)
				space = stringWidth;
		}
		return space;
	}
	
	/** 
	 * set the x-coordinates of all pixels
	 * @param pX array containing the new x-coordinates
	 */
	public void setPixelX(int[] pX)
	{
		pixelX = pX;
	}
	
	/** 
	 * set the y-coordinates of all pixels
	 * @param pY array containing the new y-coordinates
	 */public void setPixelY(int[] pY)
	{
		pixelY = pY;
	}
	
	/** 
	 * set description of the x-axis entries
	 * @param desc array with the new descriptions
	 */
	public void setXAxisDesc(String[] desc)
	{
		xAxisDesc = desc;
	}
	
	/** 
	 * set description of the y-axis entries
	 * @param desc array with the new descriptions
	 */
	public void setYAxisDesc(String[] desc)
	{
		yAxisDesc = desc;
	}
	
	/** 
	 * set description of the z-axis entries
	 * @param desc array with the new descriptions
	 */
	public void setZAxisDesc(String[] desc)
	{
		zAxisDesc = desc;
	}
	
	/** 
	 * set titles of x- and y-axis
	 * @param xTitle name of the x-axis
	 * @param yTitle name of the y-axis
	 */
	public void setAxisTitles(String xTitle, String yTitle)
	{
		xAxisTitle = xTitle;
		yAxisTitle = yTitle;
	}
	
	/** 
	 * set titles of all axis
	 * @param xTitle name of the x-axis
	 * @param yTitle name of the y-axis
	 * @param zTitle name of the z-axis
	 */
	public void setAxisTitles(String xTitle, String yTitle, String zTitle)
	{
		setAxisTitles(xTitle, yTitle);
		zAxisTitle = zTitle;
	}
	
	// set the width and height of each glyph
	protected void setGlyphSize(int width, int height)
	{
		glyphWidth = width;
		glyphHeight = height; 
	}
	
	/**
	 * @return the x-axis title
	 */
	public String getXAxisTitle()
	{
		return xAxisTitle;
	}
	
	/**
	 * @return the y-axis title
	 */
	public String getYAxisTitle()
	{
		return yAxisTitle;
	}
	
	/**
	 * @return the z-axis title
	 */
	public String getZAxisTitle()
	{
		return zAxisTitle;
	}
	
	/**
	 * set x-coordinate of a specific value
	 * @param val new x-coordinate
	 * @param index index of the value 
	 */
	protected void setXCorAt(int val, int index)
	{
		pixelX[index] = val;
	}
	
	/**
	 * @return x-coordinate of pixel at index 
	 */
	public int getXCorAt(int index)
	{
		return pixelX[index];
	}
	
	/**
	 * set y-coordinate of a specific value
	 * @param val new y-coordinate
	 * @param index index of the value 
	 */
	public void setYCorAt(int val, int index)
	{
		pixelY[index] = val;
	}
	
	/**
	 * @return y-coordinate of pixel at index 
	 */
	public int getYCorAt(int index)
	{
		return pixelY[index];
	}
	
	// update the bounds of the glyphs. This is necessary
	// after the pixel layout or number of visible pixels
	// has changed
	protected void updateGlyphBounds(PixelFrame frame)
	{
		// this algorithm determines for each pixel in the
		// glyphs whether it is mapped to a data value
		// or empty.
		glyphBounds = new boolean[glyphWidth + 2][glyphHeight + 2];
		int start = frame.getStartIndex();
		int stop = frame.getStopIndex();
		// of course all positions belonging to data
		// values can't be bounds
		for (int i = start; i <= stop; i++)
			glyphBounds[getXCorAt(i) + 1][getYCorAt(i) + 1] = true;
		// left and upper side of the glyphs are
		// always bounds
		for (int i = 0; i < glyphBounds.length; i++)
			glyphBounds[i][0] = true;
		for (int i = 0; i < glyphBounds[0].length; i++)
			glyphBounds[0][i] = true;
		// now determine the bounds at the bottom
		// and to the right
		for (int i = 1; i < glyphBounds.length; i++)
			for (int j = 1; j <= glyphBounds[0].length; j++)
			{
				if (!glyphBounds[i][j-1])
				{
					glyphBounds[i][j-1] = true;
					break;
				}
			}
		for (int j = 1; j < glyphBounds[0].length; j++)
			for (int i = 1; i <= glyphBounds.length; i++)
			{
				if (!glyphBounds[i-1][j])
				{
					glyphBounds[i-1][j] = true;
					break;
				}
			}
	}
	
	/**
	 * @return the glyph bounds
	 */
	public boolean[][] getGlyphBounds()
	{
		return glyphBounds;
	}
	
	/**
	 * set the glyph bounds
	 * @param bounds array containing the new bounds
	 */
	public void setGlyphBounds(boolean[][] bounds)
	{
		glyphBounds = bounds;
	}
	
	/**
	 * set description of x-axis entry 
	 * @param desc new description
	 * @param index x-axis index
	 */
	public void setXAxisDescAt(String desc, int index)
	{
		xAxisDesc[index] = desc;
	}
	
	/**
	 * @param index x-axis index
	 * @return x-axis description at index
	 */
	public String getXAxisDescAt(int index)
	{
		return xAxisDesc[index];
	}
	
	/**
	 * set description of y-axis entry
	 * @param desc new description
	 * @param index y-axis index
	 */
	public void setYAxisDescAt(String desc, int index)
	{
		yAxisDesc[index] = desc;
	}
	
	/**
	 * @param index y-axis index
	 * @return y-axis description at index
	 */
	public String getYAxisDescAt(int index)
	{
		return yAxisDesc[index];
	}
	
	/**
	 * set description of z-axis entry 
	 * @param desc new description
	 * @param index z-axis index
	 */
	public void setZAxisDescAt(String desc, int index)
	{
		zAxisDesc[index] = desc;
	}
	
	/**
	 * @param index z-axis index
	 * @return z-axis description at index
	 */
	public String getZAxisDescAt(int index)
	{
		return zAxisDesc[index];
	}
	
	/**
	 * set description of a specific pixel/data value,
	 * no matter which axis contains the data values
	 * @param desc new description
	 * @param index the values' index
	 */
	public void setPixelDescAt(String desc, int index)
	{
		if (isCube())
			zAxisDesc[index] = desc;
		else
			xAxisDesc[index] = desc;
	}
	
	/**
	 * @param index index of the pixel/data value
	 * @return description of that value
	 */
	public String getPixelDescAt(int index)
	{
		if (isCube())
			return zAxisDesc[index];
		else
			return xAxisDesc[index];
	}
	
	/**
	 * 
	 * @return number of x-axis entries
	 */
	public int getXAxisCount()
	{
		return xAxisCount;
	}
	
	/**
	 * 
	 * @return number of y-axis entries
	 */
	public int getYAxisCount()
	{
		return yAxisCount;
	}
	
	/**
	 * 
	 * @return number of z-axis entries
	 */
	public int getZAxisCount()
	{
		return zAxisCount;
	}
	
	/**
	 * 
	 * @return number of pixels/data values
	 */
	public int getPixelCount()
	{
		if (isCube())
			return zAxisDesc.length;
		else
			return xAxisDesc.length;
	}
	
	protected void setGlyphWidth(int w)
	{
		glyphWidth = w;
	}
	
	protected int getGlyphWidth()
	{
		return glyphWidth;
	}
	
	protected void setGlyphHeight(int h)
	{
		glyphHeight = h;
	}
	
	protected int getGlyphHeight()
	{
		return glyphHeight;
	}
	
	/**
	 * re-calculate the size of the glyphs
	 * @param frame the PixelFrame for which the update is
	 * performed
	 */
	public void updateGlyphSize(PixelFrame frame)
	{
		int start = frame.getStartIndex();
		int stop = frame.getStopIndex();
		int highestX = 0, highestY =  0;
		
		for (int i = start; i <= stop; i++)
		{
			if (getXCorAt(i) > highestX)
				highestX = getXCorAt(i);
			else if (getYCorAt(i) > highestY)
				highestY = getYCorAt(i);
		}
		glyphWidth = highestX + 1;
		glyphHeight = highestY + 1;
	}
	
	/**
	 * re-calcualte the mean values of the glyphs
	 * @param start index of first visible data value
	 * @param stop index of last visible data value
	 */
	public void updateMeans(int start, int stop)
	{
		for (int i = 0; i < getRowCount(); i++)
		{
			VisualItem item = getItem(i);
			double val = 0, scalVal = 0;
			for (int j = start; j <= stop; j++)
			{
				val += ((double[])item.get("value"))[j];
				scalVal += ((double[])item.get("scaledValue"))[j];
			}
			val /= (stop - start + 1);
			scalVal /= (stop - start + 1);
			item.set("mean", new Double(val));
			item.set("scaledMean", new Double(scalVal));
		}
	}
	
	/**
	 * add a value to the frequency distribution
	 * (they are scaled to values ranging from 0 to 100)
	 * @param val value to add
	 */
	public void addToDistribution(double val)
	{
		 int index = (int)Math.round(val * 100d);
		 distribution[index]++;
	}
	
	/**
	 * 
	 * @return array caontaining the frequency distribution
	 */
	public int[] getDistribution()
	{
		return distribution;
	}
	
	/**
	 * set whole frequency distribution
	 * @param dis array with the new distribution
	 */
	public void setDistribution(int[] dis)
	{
		distribution = dis;
	}
	
	/**
	 * re-arrange the glyphs 
	 * @param frame PixelFrame for which to update the layout
	 */
	public void updateGlyphLayout(PixelFrame frame)
	{
		String layout = frame.getGlyphLayout(); 
		int startX = 0;
		int startY = 0;
		int space = frame.getSpace();
		ArrayList v = new ArrayList();
		ArrayList<Integer> mapping = new ArrayList();
		// Layouts contained in OptimizedLayouts depend
		// on the data and thus need the VisualItems themselves.
		// The other layouts like z-curve don't need any data
		// and are performed on points representing the
		// the VisualItems.
		if (!layout.equals(StringConstants.OptimizedTableLayout)
				&& !layout.equals(StringConstants.MDSLayout)
				&& !layout.equals(StringConstants.JigsawLayout))
		    for (int i = 0; i < getRowCount(); i++)
		    {
		    	VisualItem actItem = getItem(i);
		    	boolean vis = ((Boolean)actItem.get("visible")).booleanValue();
		    	if (vis)
		    	{
		    		v.add(new Point(0, 0));//((Integer)glyphTable.getItem(i).get("xCor")).intValue(), 
		    			//((Integer)glyphTable.getItem(i).get("yCor")).intValue()));
		    		mapping.add(new Integer(i));
		    	}
		    	else
		    		actItem.set("xCor", new Integer(-1000));
		    }
		else
			for (int i = 0; i < getRowCount(); i++)
			{
		    	VisualItem actItem = getItem(i);
		    	boolean vis = ((Boolean)actItem.get("visible")).booleanValue();
		    	if (vis)
		    		v.add(getItem(i));
		    	else
		    		actItem.set("xCor", new Integer(-1000));
			}
		
		if (layout.equals(StringConstants.ZLayout))
	    	Layouts.createZLayout(v, startX, startY, getGlyphWidth() + space, getGlyphHeight() + space);
	    else if (layout.equals(StringConstants.MyZLayout))
	    	Layouts.createFlexibleZLayout(v, startX, startY, getGlyphWidth() + space, getGlyphHeight() + space);
	    else if (layout.equals(StringConstants.RowLayout) || 
	    		layout.equals(StringConstants.TableLayout))
	    {	
	    	if (isCube())
	    		Layouts.createRowLayout(v, startX, startY, getGlyphWidth() + space, getGlyphHeight() + space,
	   			(int)Math.sqrt(v.size()), (int)Math.sqrt(v.size()));//getXAxisCount(), getYAxisCount());
	    	else
	    	{
	    		if (layout.equals(StringConstants.RowLayout)) 
	    			Layouts.createRowLayout(v, startX, startY, getGlyphWidth() + space, getGlyphHeight() + space, 0, 0);
	    		else
	    			Layouts.createTable2D(v, startX, startY, getGlyphWidth() + space, 
	    					getGlyphHeight() + space, this);
	    	}
	    }
	    else if (layout.equals(StringConstants.OptimizedTableLayout))
	    {
	    	OptimizingLayouts.createOrderedTableLayout(v, startX, startY, getGlyphWidth() + space, 
	    			getGlyphHeight() + space, this);
	    }
	    else if (layout.equals(StringConstants.JigsawLayout))
	    {
	    	OptimizingLayouts.createJigsawLayout(v, startX, startY, getGlyphWidth() + space, 
	    			getGlyphHeight() + space, this);
	    }
	    else if (layout.equals(StringConstants.MDSLayout))
	    {
	    	OptimizingLayouts.createMDSLayout(v, startX, startY, getGlyphWidth() + space, 
	    			getGlyphHeight() + space, this);
	    }
		
	    if (!layout.equals(StringConstants.OptimizedTableLayout)
	    		&& !layout.equals(StringConstants.MDSLayout)
	    		&& !layout.equals(StringConstants.JigsawLayout))
		    for (int i = 0; i < v.size(); i++)
			{
				Point p = ((Point)v.get(i));
				int itemIndex = mapping.get(i).intValue();
				getItem(itemIndex).set("xCor", new Integer((int)p.getX()));
				getItem(itemIndex).set("yCor", new Integer((int)p.getY()));
			}
	}
	
	/**
	 * re-arrange the pixels
	 * @param frame PixelFrame for which to update the layout
	 */
	public void updatePixelLayout(PixelFrame frame)
	{
		String layout = frame.getPixelLayout(); 
		//int index = frame.getIndex();
		// create a Vector of Points which represent the 
		// pixels
		ArrayList pixels = new ArrayList();
		for (int i = 0; i < getPixelCount(); i++)
		{
			if (i >= frame.getStartIndex() && i <= frame.getStopIndex())
				pixels.add(new Point(getXCorAt(i),
					getYCorAt(i)));
		}
		
		// assign the layout
		int matrixWidth = frame.getRowSize();
		int matrixHeight = frame.getColumnSize();
		if (layout.equals(StringConstants.ZLayout))
	    	Layouts.createZLayout(pixels, 0, 0, 1, 1);
	    else if (layout.equals(StringConstants.MyZLayout))
	    	Layouts.createFlexibleZLayout(pixels, 
	    	0, 0, 1, 1);
	    else if (layout.equals(StringConstants.RowLayout))
	    	Layouts.createRowLayout(pixels, 
	    	0, 0, 1, 1, matrixWidth, matrixHeight);
	    else if (layout.equals(StringConstants.ColumnLayout))
	    	Layouts.createColumnLayout(pixels, 
	    	0, 0, 1, 1, matrixWidth, matrixHeight);
	    else if (layout.equals(StringConstants.HilbertLayout))
	    	Layouts.createHilbertLayout(pixels, 
	    	0, 0, 1, 1);
	    
		// now the pixels must adapt the 
		// points' positions
		for (int i = 0; i < pixels.size(); i++)
		{
			Point p = ((Point)pixels.get(i));
			setXCorAt((int)p.getX(), frame.getStartIndex() + i);
			setYCorAt((int)p.getY(), frame.getStartIndex() + i);
		}
		
		updateGlyphSize(frame);
		updateGlyphBounds(frame);
	}
	
	/**
	 * request repaint
	 *
	 */
	public void updateVisu()
	{
		getVisualization().getDisplay(0).pan(0, 0);
		getVisualization().getDisplay(0).repaint();
	}
	
	/**
	 * duplicate this GlyphTable
	 * @param visu Visualization context of the duplicated GlyphTable
	 * @return the cloned GlyphTable
	 */
	public GlyphTable duplicate(Visualization visu)
	{
		GlyphTable gt = new GlyphTable(visu, "glyphTable");
		
		// set the duplicate's values 
		gt.addColumn("index", int.class);
		if (isCube())
			gt.addColumn("x-desc", String.class);
        gt.addColumn("y-desc", String.class);
        gt.addColumn("xCor", int.class);
        gt.addColumn("yCor", int.class);
        gt.addColumn("visible", Boolean.class);
        gt.addColumn("mean", double.class);
        gt.addColumn("scaledMean", double.class);
        gt.addColumn("value", double[].class);
        gt.addColumn("scaledValue", double[].class);
        
        int currentIndex = 0;
        int xCount, yCount;
        if (isCube())
        	xCount = xAxisCount;
        else
        	xCount = 1;
        yCount = yAxisCount;
        for (int y = 0; y < yCount; y++)
        {
        	for (int x = 0; x < xCount; x++)
        	{
        		VisualItem actItem = getItem(currentIndex);
	        	VisualItem newItem = gt.addItem();
	        	newItem.set("index", new Integer(currentIndex));
	        	if (isCube())
	        		newItem.set("x-desc", getXAxisDescAt(x));
	        	newItem.set("y-desc", getYAxisDescAt(y));
	        	newItem.set("xCor", new Integer((Integer)actItem.get("xCor")));
	        	newItem.set("yCor", new Integer((Integer)actItem.get("yCor")));
	        	newItem.set("visible", actItem.get("visible"));
	        	double[] val = ((double[])actItem.get("value")).clone();
	        	double[] scalVal = ((double[])actItem.get("scaledValue")).clone();
	        	double mean = (Double)actItem.get("mean");
	        	double scaledMean = (Double)actItem.get("scaledMean");
	        	gt.setDistribution(getDistribution().clone());
	        	newItem.set("value", val);
		        newItem.set("scaledValue", scalVal);
		        newItem.set("mean", new Double(mean));
		        newItem.set("scaledMean", new Double(scaledMean));
	        	currentIndex++;
	     	}
        }
        
        gt.init(getXAxisCount(), getYAxisCount(), getZAxisCount());
	    gt.setAxisTitles(xAxisTitle, yAxisTitle, zAxisTitle);
	    gt.setXAxisDesc(xAxisDesc);
	    gt.setYAxisDesc(yAxisDesc);
	    gt.setZAxisDesc(zAxisDesc);
	    gt.setGlyphSize(glyphWidth, glyphHeight);
	    gt.setPixelX(pixelX);
	    gt.setPixelY(pixelY);
	    gt.setHighest(highest);
	    gt.setReference(reference);
	    
		return gt;
	}
	
	/**
	 * re-scale the data values depending on a sliding window
	 * @param width size of one half of the window; the 
	 * actual sliding window width is 2 * width + 1
	 */
	public void updateSlidingWindow(int width)
	{
		for (int i = 0; i < getRowCount(); i++)
		{
			VisualItem actItem = getItem(i);
        	double[] values = ((double[])actItem.get("value")).clone();
			double[] scaledValues = new double[getPixelCount()];
        	for (int j = 0; j < getPixelCount(); j++)
        	{
        		scaledValues[j] = 0;
        		int numberOfNeighbours = 0;
        		for (int k = j - width; k <= j + width; k++)
        		{
        			if (k >= 0 && k < getPixelCount())
        			{	
        				scaledValues[j] += (values[k] / reference);
        				numberOfNeighbours++;
        			}
        		}
        		scaledValues[j] /= (double)numberOfNeighbours;
        	}
			actItem.set("scaledValue", scaledValues);
		}
	}
}