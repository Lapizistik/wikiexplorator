package visualizer.display;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.color.ColorSpace;
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
import visualizer.StringConstants;
import visualizer.userInterface.PixelFrame;

/**
 * The PixelRenderer draws the items onto the screen
 * and also calculates their borders.
 * 
 * @author Rene Wegener
 *
 */
public class PixelRenderer extends LabelRenderer 
{
	protected int pixelSize = 1;
	protected int textSize = 1;
	protected boolean table = false;
	protected GlyphTable gt;
	protected String colorMode = StringConstants.GrayScale;
	protected PixelFrame frame;
	
	public PixelRenderer(String n, GlyphTable tab, int ps, int ts)
	{
		super(n);
		gt = tab;
		pixelSize = ps;
		textSize = ts;
	}
	
	public void setFrame(PixelFrame pf)
	{
		frame = pf;
	}
	
	public void setTextSize(int ts)
	{
		textSize = ts;
	}
	
	public void render(Graphics2D g, VisualItem item) 
	{
			int startX = ((Integer)item.get("xCor")).intValue();
	        int startY = ((Integer)item.get("yCor")).intValue();
	        int w = gt.getGlyphWidth() + 2;
	        int h = gt.getGlyphHeight() + 2;
	        // fill it with the pixels or mark it as
	        // self answers
	        if (gt.isCube() && ((String)item.get("x-desc")).equals((String)item.get("y-desc")))
	        {
	        	g.setColor(Color.DARK_GRAY);
	        	g.drawLine(startX + 2, startY + 2, startX + w - 4, startY + h - 4);
	        	g.drawLine(startX + 2, startY + h - 4, startX + w - 4, startY + 2);
		    }
	        else
	        {
	        	// draw a bounding rectangle
		        if (frame.isBorderOn())
		        {
		        	g.setColor(Color.black);
		        	g.fillRect(startX - 1, startY - 1, w, h);
		        }
		        for (int i = frame.getStartIndex(); i <= frame.getStopIndex(); i++)
	        	{
	        		int pixelX = gt.getXCorAt(i);
	        		int pixelY = gt.getYCorAt(i);
	        		double val = ((double[])item.get("scaledValue"))[i];
	        		drawPixel(g, val, startX + pixelX, startY + pixelY);
	        	}
	        }
	        // now draw a label 
	        if (frame.getGlyphLayout().equals(StringConstants.TableLayout) ||
	        		frame.getGlyphLayout().equals(StringConstants.OptimizedTableLayout))
	        {
	        	int x = ((Integer)item.get("xCor")).intValue();
        		int y = ((Integer)item.get("yCor")).intValue();
        		// does this item belong to the first row?
        		int fontHeight;
        		if (gt.getGlyphWidth() < gt.getGlyphHeight())
        			fontHeight = gt.getGlyphWidth() - 2;
        		else
        			fontHeight = gt.getGlyphHeight() - 2;
        		if (fontHeight < 0)
        			fontHeight = 0;
        		if (y == 0 && gt.isCube())
	        	{
	        		AffineTransform fontAT = new AffineTransform();
	        		fontAT.rotate(Math.toRadians(270));
	        		Font font = (new Font("ARIAL", Font.PLAIN, fontHeight)).deriveFont(fontAT);
	        		String desc;
	        		desc = (String)(item.get("x-desc"));
	        		g.setColor(Color.black);
			       	g.setFont(font);
			       	g.drawString(desc, x + fontHeight/2 + gt.getGlyphWidth()/2, y - 2);
		       	}
	        	// does this item belong to the left column?
	        	if (x == 0)
	        	{
	        		Font font =  new Font("Arial", Font.PLAIN, fontHeight);
	        		String desc;
	        		desc = (String)(item.get("y-desc"));
	        		g.setColor(Color.black);
			       	g.setFont(font);
			       	int stringWidth = (int)(font.getStringBounds(desc, g.getFontRenderContext())).getWidth();
			       	g.drawString(desc, x - stringWidth - 2, y + fontHeight/2 + gt.getGlyphHeight()/2);
		       	}
	        }
	}
	
	public void setTableLabeling(boolean bol)
	{
		table = bol;
	}
	
	private void drawPixel(Graphics2D gr, double v, int pX, int pY)
	{
		int red, green, blue;
		double a1, a2, a3;
		//v = getGammaCorrectedValue(v);
		
		if (colorMode.equals(StringConstants.HeatScale))
		{
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
		}
		else //if (colorMode.equals(StringConstants.GrayScale))
		{
			gr.setColor(getColor(v));
			red = 255 - (int)(v * 255);
			green = 255 - (int)(v * 255);
			blue = 255 - (int)(v * 255);
		}
		
		gr.fillRect(pX, pY, pixelSize, pixelSize);
	}
	
	public Color getColor(double val)
	{
		Color c = new Color((int)(255 * val), (int)(255 * val), (int)(255 * val));
		float[] arr = new float[3];
		// val -> cie
		arr[0] = (float)getGammaCorrectedValue(val);
		arr[1] = (float)getGammaCorrectedValue(val);
		arr[2] = (float)getGammaCorrectedValue(val);
		// cie -> rgb
		arr = new Color(0, 0, 0).getColorSpace().fromCIEXYZ(arr);
		Color c1 = new Color(arr[0], arr[1], arr[2]);
		return c;		
	}
	
	 protected Shape getRawShape(VisualItem item) 
	 {
		 int x = 0, y = 0, width = 0, height = 0;
		 x = ((Integer)item.get("xCor")).intValue();
		 y = ((Integer)item.get("yCor")).intValue();
		 width = gt.getGlyphWidth();
		 height = gt.getGlyphHeight();
		 // get bounding box dimensions
	     m_bbox.setFrame(x, y, width, height);
	     
	     return m_bbox;
	 }
	 
	 public static double getGammaCorrectedValue(double val)
	 {
		 double gamma = 0.5;
		 return Math.pow(val, gamma);
	 }
	 
	 public void setColorMode(String s)
	 {
		 colorMode = s;
	 }
} // end of class LabelRenderer
