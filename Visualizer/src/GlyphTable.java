/**
 * 
 */

import prefuse.Visualization;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;

/**
 * One of the central classes of the program. GlyphTable
 * is a VisualTable whcih means that it holds all VisualItems
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
	protected int width, height;
	protected String[] pixelDesc;
	
	public GlyphTable(Visualization vis, String str)
	{
		super(vis, str);
	}
	
	public void init(String dat, int zAxisCount)
	{
		data = dat;
		x = new int[zAxisCount];
		y = new int[zAxisCount];
		pixelDesc = new String[zAxisCount];
		for (int i = 0; i < zAxisCount; i++)
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
	
	public void setZDescAt(String desc, int index)
	{
		pixelDesc[index] = desc;
	}
	
	public String getZDescAt(int index)
	{
		return pixelDesc[index];
	}
	
	public int getZAxisCount()
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
	
	public static boolean isGlyph(VisualItem item)
	{
		if (item.getGroup().equals("glyphTable"))
			return true;
		else
			return false;
	}
}