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
import prefuse.visual.AggregateTable;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableAggregateItem;

public class PixelRenderer extends LabelRenderer 
{
	protected int pixelSize = 1;
	protected int textSize = 1;
	protected boolean table = false;
	protected AggregateTable at;
	
	public PixelRenderer(String n, AggregateTable agg, int ps, int ts)
	{
		super(n);
		at = agg;
		pixelSize = ps;
		textSize = ts;
	}
	
	public void setTextSize(int ts)
	{
		textSize = ts;
	}
	
	public void render(Graphics2D g, VisualItem item) 
	{
		    if (item.get("type").equals("pixel") && !((Boolean)(item.get("invisible"))).booleanValue())
	        {
		    	// get coordinates from the parent aggregate
		    	VisualItem parent = at.getItem(((Integer)item.get("parentIndex")).intValue());
		    	int startX = ((Integer)parent.get("xCor")).intValue();
		    	int startY = ((Integer)parent.get("yCor")).intValue();
		    	int x = ((Integer)item.get("xCor")).intValue();
	        	int y = ((Integer)item.get("yCor")).intValue();
	        	double val = ((Double)item.get("color")).doubleValue();
		        drawPixel(g, val, startX + x, startY + y);
	        }
	        else if (item.get("type").equals("glyph"))
	        {
	        	int x = ((Integer)item.get("xCor")).intValue();
	        	int y = ((Integer)item.get("yCor")).intValue();
	        	if (item.canGetString("author") && textSize > 0 &&
	        			!table)
	        	{
		        	String desc = (String)(item.get("author"));
		        	if (desc.length() > 10)
		        		desc = desc.substring(0, 10) + ".";
		        	g.setColor(Color.black);
		        	g.setFont(new Font("Arial", Font.PLAIN, textSize - 2));
		        	g.drawString(desc, x + 1, y + textSize - 1);
	        	}
	        }
	        else if (item.get("type").equals("label") && table)
	        {
	        	int x = ((Integer)item.get("xCor")).intValue();
	        	int y = ((Integer)item.get("yCor")).intValue();
	        	String text = (String)(item.get("text"));
		        if (!text.equals(""))
		        {
		        	g.setColor(Color.black);
		        	g.setFont(new Font("Arial", Font.PLAIN, textSize));
		        	g.drawString(text, x, y);
		        }
	        }
	}
	
	public void setTableLabeling(boolean bol)
	{
		table = bol;
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
		/*double a1, a2, a3;
		
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
		blue = (int)(255 * a3);*/
		if (red < 0)
			red = 0;
		if (green < 0)
			green = 0;
		if (blue < 0)
			blue = 0;
		gr.setColor(new Color(red, green, blue));
		gr.fillRect(pX, pY, pixelSize, pixelSize);
	}
	
	 protected Shape getRawShape(VisualItem item) 
	 {
		 int x, y, width = 0, height = 0;
		 x = ((Integer)item.get("xCor")).intValue();
		 y = ((Integer)item.get("yCor")).intValue();
		 if (item.get("type").equals("pixel") && !((Boolean)(item.get("invisible"))).booleanValue())
		 {
			 VisualItem parent = at.getItem(((Integer)item.get("parentIndex")).intValue());
		     int parentX = ((Integer)parent.get("xCor")).intValue();
		     int parentY = ((Integer)parent.get("yCor")).intValue();
		     x += parentX;
		     y += parentY;
			 width = 1;
			 height = 1;
		 }
		 else if (item.get("type").equals("glyph"))
		 {
			 width = ((Integer)item.get("width")).intValue();
			 height = ((Integer)item.get("height")).intValue();
		 }
		 // get bounding box dimensions
	     m_bbox.setFrame(x, y, width, height);
	     
	     return m_bbox;
	 }
} // end of class LabelRenderer
