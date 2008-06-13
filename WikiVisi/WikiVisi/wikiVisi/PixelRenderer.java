package wikiVisi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

import prefuse.Constants;
import prefuse.data.Edge;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.GraphicsLib;
import prefuse.util.StringLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableAggregateItem;

public class PixelRenderer extends LabelRenderer 
{
	protected int pixelSize = 1;
	protected int textSize = 1;
	
	public PixelRenderer()
	{
		super();
	}
	
	public PixelRenderer(String n, int ps, int ts)
	{
		super(n);
		pixelSize = ps;
		textSize = ts;
	}
	
	public void setTextSize(int ts)
	{
		textSize = ts;
	}
	
	public void render(Graphics2D g, VisualItem item) 
	{
		    if (item.get("type").equals("pixel"))
	        {
	        	int x = ((Integer)item.get("xCor")).intValue();
	        	int y = ((Integer)item.get("yCor")).intValue();
		        double val = ((Double)item.get("color")).doubleValue();
		        drawPixel(g, val, x, y);
	        }
	        else if (item.get("type").equals("glyph"))
	        {
	        	int x = ((Integer)item.get("xCor")).intValue();
	        	int y = ((Integer)item.get("yCor")).intValue();
	        	if (item.canGetString("author") && textSize > 0)
	        	{
		        	String desc = (String)(item.get("author"));
		        	if (desc.length() > 10)
		        		desc = desc.substring(0, 10) + ".";
		        	g.setColor(Color.black);
		        	g.setFont(new Font("Arial", Font.PLAIN, textSize - 2));
		        	g.drawString(desc, x + 1, y + textSize - 1);
	        	}
	        }
	}
	
	public void drawPixel(Graphics2D gr, double v, int pX, int pY)
	{
		int red, green, blue;
		if (v >= 0)
		{
			red = (int)(v * 255);
			green = (int)(Math.abs(v - 0.5d) * 510);
			blue = (int)((1.0d - v) * 255);
		}
		else
		{
			red = 0;
			green = 255;
			blue = 0;
		}
		// Test
		double a1, a2, a3;
		
		if (v <= 0.333d)
		{
			a1 = (double)(3.0d * (double)v);
			a2 = 0.0d;
			a3 = 0.0d;
		}
		else if (v > 0.333d && v <= 0.666d)
		{
			a1 = 1.0d;
			a2 = 3.0d * (double)v -1.0d;
			a3 = 0.0d;
		}
		else
		{
			a1 = 1.0d;
			a2 = 1.0d;
			a3 = 3.0d * (double)v - 2.0d;
		}
		red = (int)(255 * a1);
		green = (int)(255 * a2);
		blue = (int)(255 * a3);
		if (red < 0)
			red = 0;
		if (green < 0)
			green = 0;
		if (blue < 0)
			blue = 0;
		gr.setColor(new Color(red, green, blue));
		gr.fillRect(pX, pY, pixelSize, pixelSize);
	}
} // end of class LabelRenderer
