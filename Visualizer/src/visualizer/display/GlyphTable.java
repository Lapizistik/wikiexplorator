package visualizer.display;
/**
 * 
 */

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import prefuse.Visualization;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import visualizer.StringConstants;
import visualizer.userInterface.PixelFrame;

/**
 * One of the central classes of the program. GlyphTable
 * is a VisualTable which means that it holds all VisualItems
 * that represent glyph icons. In addition it contains
 * general data like the z axis description.
 * 
 * @author rene
 *
 */
public class GlyphTable extends VisualTable
{
	protected String title, xAxisTitle, yAxisTitle, zAxisTitle;
	protected int[] pixelX;
	protected int[] pixelY;
	protected int glyphWidth, glyphHeight, xAxisCount, yAxisCount, zAxisCount;
	protected String[] xAxisDesc, yAxisDesc, zAxisDesc;
	protected int[] distribution;
	protected double highest = 0, reference = 0;
	protected boolean[][] glyphBounds;
	
	public GlyphTable(Visualization vis, String str)
	{
		super(vis, str);
		distribution = new int[101];
	}
	
	public void setHighest(double h)
	{
		highest = h;
	}
	
	public double getHighest()
	{
		return highest;
	}
	
	public void setReference(double r)
	{
		reference = r;
	}
	
	public double getReference()
	{
		return reference;
	}
	
	public boolean isCube()
	{
		if (zAxisCount > 0)
			return true;
		else
			return false;
	}
	
	public int getAuthorCount()
	{
		return yAxisCount;
	}
	
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
	
	// return the maximum amount of pixels needed to
	// display alle authors' names
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
	
	public void setPixelX(int[] pX)
	{
		pixelX = pX;
	}
	
	public void setPixelY(int[] pY)
	{
		pixelY = pY;
	}
	
	public void setXAxisDesc(String[] desc)
	{
		xAxisDesc = desc;
	}
	
	public void setYAxisDesc(String[] desc)
	{
		yAxisDesc = desc;
	}
	
	public void setZAxisDesc(String[] desc)
	{
		zAxisDesc = desc;
	}
	
	public void setAxisTitles(String xTitle, String yTitle)
	{
		xAxisTitle = xTitle;
		yAxisTitle = yTitle;
	}
	
	public void setAxisTitles(String xTitle, String yTitle, String zTitle)
	{
		setAxisTitles(xTitle, yTitle);
		zAxisTitle = zTitle;
	}
	
	public void setGlyphSize(int width, int height)
	{
		glyphWidth = width;
		glyphHeight = height; 
	}
	
	public String getXAxisTitle()
	{
		return xAxisTitle;
	}
	
	public String getYAxisTitle()
	{
		return yAxisTitle;
	}
	
	public String getZAxisTitle()
	{
		return zAxisTitle;
	}
	
	public void setXCorAt(int val, int index)
	{
		pixelX[index] = val;
	}
	
	public int getXCorAt(int index)
	{
		return pixelX[index];
	}
	
	public void updateGlyphBounds(PixelFrame frame)
	{
		glyphBounds = new boolean[glyphWidth + 2][glyphHeight + 2];
		int start = frame.getStartIndex();
		int stop = frame.getStopIndex();
		for (int i = start; i <= stop; i++)
			glyphBounds[getXCorAt(i) + 1][getYCorAt(i) + 1] = true;
		for (int i = 0; i < glyphBounds.length; i++)
			glyphBounds[i][0] = true;
		for (int i = 0; i < glyphBounds[0].length; i++)
			glyphBounds[0][i] = true;
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
	
	public boolean[][] getGlyphBounds()
	{
		return glyphBounds;
	}
	
	public void setGlyphBounds(boolean[][] bounds)
	{
		glyphBounds = bounds;
	}
	
	public void setYCorAt(int val, int index)
	{
		pixelY[index] = val;
	}
	
	public int getYCorAt(int index)
	{
		return pixelY[index];
	}
	
	public void setXAxisDescAt(String desc, int index)
	{
		xAxisDesc[index] = desc;
	}
	
	public String getXAxisDescAt(int index)
	{
		return xAxisDesc[index];
	}
	
	public void setYAxisDescAt(String desc, int index)
	{
		yAxisDesc[index] = desc;
	}
	
	public String getYAxisDescAt(int index)
	{
		return yAxisDesc[index];
	}
	
	public void setZAxisDescAt(String desc, int index)
	{
		zAxisDesc[index] = desc;
	}
	
	public String getZAxisDescAt(int index)
	{
		return zAxisDesc[index];
	}
	
	public void setPixelDescAt(String desc, int index)
	{
		if (isCube())
			zAxisDesc[index] = desc;
		else
			xAxisDesc[index] = desc;
	}
	
	public String getPixelDescAt(int index)
	{
		if (isCube())
			return zAxisDesc[index];
		else
			return xAxisDesc[index];
	}
	
	public int getXAxisCount()
	{
		return xAxisCount;
	}
	
	public int getYAxisCount()
	{
		return yAxisCount;
	}
	
	public int getZAxisCount()
	{
		return zAxisCount;
	}
	
	public int getPixelCount()
	{
		if (isCube())
			return zAxisDesc.length;
		else
			return xAxisDesc.length;
	}
	
	public void setGlyphWidth(int w)
	{
		glyphWidth = w;
	}
	
	public int getGlyphWidth()
	{
		return glyphWidth;
	}
	
	public void setGlyphHeight(int h)
	{
		glyphHeight = h;
	}
	
	public int getGlyphHeight()
	{
		return glyphHeight;
	}
	
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
	
	public void addToDistribution(double val)
	{
		 int index = (int)Math.round(val * 100d);
		 distribution[index]++;
	}
	
	public int[] getDistribution()
	{
		return distribution;
	}
	
	public void setDistribution(int[] dis)
	{
		distribution = dis;
	}
	
	public void updateGlyphLayout(PixelFrame frame)
	{
		//int index = frame.getIndex();
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
	    else if (layout.equals(StringConstants.FatRowLayout))
	    	Layouts.createLineLayout(pixels, 
	    	0, 0, 1, 1);
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
	
	public void updateVisu()
	{
		this.getVisualization().getDisplay(0).pan(0, 0);
		this.getVisualization().getDisplay(0).repaint();
	}
	
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
	        	// create pixels for this glyph
	        	// the values for the pixels are assumed to be stored
	        	// in the z-axis
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
	     	}// end of for y
        }// end of for x
        
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
	    /*for (int x = 0; x < getXAxisCount(); x++)
	     	 gt.setXAxisDescAt(getXAxisDescAt(x), x);
	     for (int y = 0; y < getYAxisCount(); y++)
	     	 gt.setYAxisDescAt(getYAxisDescAt(y), y);
	     for (int z = 0; z < getZAxisCount(); z++)
	     	 gt.setZAxisDescAt(getZAxisDescAt(z), z);*/
	     
		return gt;
	}
	
	public void updateSlidingWindow(int size)
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
        		for (int k = j - size; k <= j + size; k++)
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