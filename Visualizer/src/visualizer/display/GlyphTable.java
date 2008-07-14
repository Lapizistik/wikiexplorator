package visualizer.display;
/**
 * 
 */

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
	protected String data, xAxisTitle, yAxisTitle, zAxisTitle;
	protected int[] x;
	protected int[] y;
	protected int width, height, xAxisCount, yAxisCount, zAxisCount;
	protected String[] pixelDesc;
	
	public GlyphTable(Visualization vis, String str)
	{
		super(vis, str);
	}
	
	public void init(String dat, int xAxis, int yAxis, int zAxis)
	{
		data = dat;
		xAxisCount = xAxis;
		yAxisCount = yAxis;
		zAxisCount = zAxis;
		int numberOfValues;
		if (data.equals(StringConstants.Data3D))
			numberOfValues = zAxisCount;
		else
			numberOfValues = xAxisCount;
		x = new int[numberOfValues];
		y = new int[numberOfValues];
		pixelDesc = new String[numberOfValues];
		
		for (int i = 0; i < numberOfValues; i++)
		{
			x[i] = 0;
			y[i] = 0;
			pixelDesc[i] = "";
		}
		width = 0;
		height = 0;
	}
	
	public void setXAt(int val, int index)
	{
		x[index] = val;
	}
	
	public int getXAt(int index)
	{
		return x[index];
	}
	
	public void setYAt(int val, int index)
	{
		y[index] = val;
	}
	
	public int getYAt(int index)
	{
		return y[index];
	}
	
	public void setDescAt(String desc, int index)
	{
		pixelDesc[index] = desc;
	}
	
	public String getZDescAt(int index)
	{
		return pixelDesc[index];
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
		return pixelDesc.length;
	}
	
	public void setWidth(int w)
	{
		width = w;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public void updateSize(int start, int stop)
	{
		int highestX = 0, highestY =  0;
		
		for (int i = start; i <= stop; i++)
		{
			if (getXAt(i) > highestX)
				highestX = getXAt(i);
			else if (getYAt(i) > highestY)
				highestY = getYAt(i);
		}
		width = highestX + 1;
		height = highestY + 1;
	}
	
	public void setHeight(int h)
	{
		height = h;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public String getDataType()
	{
		return data;
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
}