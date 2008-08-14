package visualizer.display;
/**
 * 
 */

import java.awt.Font;
import java.awt.Graphics2D;

import prefuse.Visualization;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import visualizer.StringConstants;

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
	
	public GlyphTable(Visualization vis, String str)
	{
		super(vis, str);
		distribution = new int[101];
	}
	
	public boolean isCube()
	{
		if (zAxisCount > 0)
			return true;
		else
			return false;
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
	
	public void updateSize(int start, int stop)
	{
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
}