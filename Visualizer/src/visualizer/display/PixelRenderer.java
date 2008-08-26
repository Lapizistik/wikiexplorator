package visualizer.display;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

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
	protected boolean table = false;
	protected GlyphTable gt;
	protected PixelFrame frame;
	protected float XYZn[];
	protected double[] color;
	
	public PixelRenderer(String n, GlyphTable tab)
	{
		super(n);
		gt = tab;
		// create reference white
		float white[] = new float[] {1f, 1f, 1f};
		XYZn = new Color(0, 0, 0).getColorSpace().toCIEXYZ(white);
		color = new double[101];
		createColorList();
	}
	
	public double[] getColors()
	{
		return color;
	}
	
	public void setColors(double[] col)
	{
		color = col;
	}
	
	public void setFrame(PixelFrame pf)
	{
		frame = pf;
	}
	
	public void render(Graphics2D g, VisualItem item) 
	{
		Boolean vis = ((Boolean)item.get("visible")).booleanValue();
		if (vis)
		{
			int startX = ((Integer)item.get("xCor")).intValue();
	        int startY = ((Integer)item.get("yCor")).intValue();
	        int w = gt.getGlyphWidth() + 2;
	        int h = gt.getGlyphHeight() + 2;
	    	// if self answer then mark it with 'x'
	    	if (gt.isCube() && ((String)item.get("x-desc")).equals((String)item.get("y-desc")))
	        {
	        	g.setColor(Color.white);
	        	g.fillRect(startX, startY, w - 2, h - 2);
	        	g.setColor(Color.DARK_GRAY);
	        	g.drawLine(startX + 2, startY + 2, startX + w - 4, startY + h - 4);
	        	g.drawLine(startX + 2, startY + h - 4, startX + w - 4, startY + 2);
		    }  
	        // draw a bounding rectangle
	    	else 
	        {
	    		if (frame.isBorderOn())
	    		{
	    			if (frame.getPixelLayout().equals(StringConstants.HilbertLayout))
	    			{
	    				boolean black = true;
	    				boolean startRowBlack = false;
	    				for (int i = startX - 1; i < startX + w - 1; i++)
	    				{
	    					startRowBlack = !startRowBlack;
	    					black = startRowBlack;
	    					for (int j = startY - 1; j < startY + h - 1; j++)
	    					{
	    						if (frame.getColor().equals(StringConstants.HeatScale))
	    							g.setColor(Color.LIGHT_GRAY);
	    						else if (black)
	    							g.setColor(Color.black);
	    						else
	    							g.setColor(new Color(130, 130, 130));
	    						g.fillRect(i, j, 1, 1);
	    						black = !black;
	    					}
	    				}
	    			}
	    			else
	    			{
	    				boolean[][] bounds = gt.getGlyphBounds();
	    				boolean startRowBlack = false;
	    				boolean black = false;
	    				for (int i = startX; i < startX + w; i++)
	    				{
	    					startRowBlack = !startRowBlack;
	    					black = startRowBlack;
	    					for (int j = startY; j < startY + h; j++)
	    					{
	    						if (frame.getColor().equals(StringConstants.HeatScale))
	    							g.setColor(Color.LIGHT_GRAY);
	    						else if (black)
	    							g.setColor(Color.black);
	    						else
	    							g.setColor(new Color(130, 130, 130));
	    						int boundX = i - startX;
	    						int boundY = j - startY;
	    						if (bounds[boundX][boundY])
	    							g.fillRect(i-1, j-1, 1, 1);
	    						black = !black;
	    					}
	    				}
	    			}
	    			/*boolean startRowBlack = true;
		        	boolean black;
		        	int notFilled = (w - 2) * (h - 2) - frame.getStopIndex();
		        	//System.out.println(notFilled);
		        	for (int j = startY - 1; j < startY + h - 1; j++)
		        	{
		        		black = startRowBlack;
		        		for (int i = startX - 1; i < startX + w - 1; i++)
		        		{
		        			if (frame.getColor().equals(StringConstants.HeatScale))
		    	       			g.setColor(Color.lightGray);
		    	       		else
		    	       		{
		    	       			if (black)
		    	       				g.setColor(Color.black);
		    	       			else
		    	       				g.setColor(new Color(130, 130, 130));
		    	       		}
	        			
		        			if (j < (startY + h - 2) || i < startX + w - notFilled ||
		        				!frame.getPixelLayout().equals(StringConstants.RowLayout))//||
	    	       				//gt.isCube() && ((String)item.get("x-desc")).equals((String)item.get("y-desc")))
		        				g.fillRect(i, j, 1, 1);
		        			black = !black;
		        		}
		        		startRowBlack = !startRowBlack;
		        	}*/
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
        		int fontHeight;
        		if (gt.getGlyphWidth() < gt.getGlyphHeight())
        			fontHeight = gt.getGlyphWidth() - 1;
        		else
        			fontHeight = gt.getGlyphHeight() - 1;
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
	        	if (x == 0 || !gt.isCube())
	        	{
	        		Font font =  new Font("Arial", Font.PLAIN, fontHeight);
	        		String desc;
	        		desc = (String)(item.get("y-desc"));
	        		g.setColor(Color.black);
			       	g.setFont(font);
			       	int stringWidth = (int)(font.getStringBounds(desc, g.getFontRenderContext())).getWidth();
			       	g.drawString(desc, x - stringWidth - 3, y + fontHeight/2 + gt.getGlyphHeight()/2 + 1);
		       	}
	        }
		}
	}
	
	public void setTableLabeling(boolean bol)
	{
		table = bol;
	}
	
	private void drawPixel(Graphics2D gr, double v, int pX, int pY)
	{
		gr.setColor(getColor(v));
		gr.fillRect(pX, pY, 1, 1);
	}
	
	public Color getColor(double v)
	{
		int red, green, blue;
		double a1, a2, a3;
		Color c;
		
		// first adjust the value according to gamma
		// and other optimizations
		v = color[(int)Math.round(v * 100d)];
		//v = getGammaCorrectedValue(v);
		if (frame.getInverted())
			v = 1d - v;
		
		if (frame.getColor().equals(StringConstants.HeatScale))
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
			if (a1 > 1)
				a1 = 1.0d;
			else if (a1 < 0)
				a1 = 0.0d;
			if (a2 > 1)
				a2 = 1.0d;
			else if (a2 < 0)
				a2 = 0.0d;
			if (a3 > 1)
				a3 = 1.0d;
			else if (a3 < 0)
				a3 = 0.0d;
			
			red = (int)(255 * a1);
			green = (int)(255 * a2);
			blue = (int)(255 * a3);
			if (red < 0)
				red = 0;
			if (green < 0)
				green = 0;
			if (blue < 0)
				blue = 0;
			c = new Color(red, green, blue);
		}
		else //if (colorMode.equals(StringConstants.GrayScale))
		{
			// define matching grey color in lab space
			float l = (float)v * 100f;
			if (l > 100)
				l = 100f;
			float a = 0;
			float b = 0;
			// convert lab --> xyz
			float delta = 0;//6f / 29f;
			float fY = (l + 16f) / 116f;
			float fX = fY + (a / 55f);
			float fZ = fY - (b / 200f);
			float X, Y, Z;
			// Y
			if (fY > delta)
				Y = XYZn[1] * (float)Math.pow(fY, 3);
			else
				Y = fY - (16f / 116f) * 3 * delta * delta * XYZn[1];
			// X
			if (fX > delta)
				X = XYZn[0] * (float)Math.pow(fX, 3);
			else
				X = fX - (16f / 116f) * 3 * delta * delta * XYZn[0];
			// Z
			if (fZ > delta)
				Z = XYZn[2] * (float)Math.pow(fZ, 3);
			else
				Z = fZ - (16f / 116f) * 3 * delta * delta * XYZn[2];
			// convert XYZ --> RGB
			float RGB[];
			RGB = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ).toRGB(new float[] {X, Y, Z});
			c = new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), RGB, 1);
		}
		return c;
	}
	
	public Color getSRGB(float[] values)
	{
		// first use a linear mapping to convert 
		// to linear rgb
		float[] newValues = new float[3];
		newValues[0] = 3.240479f * values[0] - 0.969256f * values[1] + 0.055648f * values[2];
		newValues[1] = -1.537150f * values[0] + 1.875992f * values[1] - 0.204043f * values[2];
		newValues[2] = -0.498535f * values[0] + 0.041556f * values[1] + 1.057311f * values[2];
		// now use a gamma correction for non-linear rgb
		for (int i = 0; i <= 2; i++)
		{
			if (newValues[i] > 0.0031308)
				newValues[i] = (float)(Math.pow(newValues[i], 1f/2.4f) * 1.055f - 0.055f);
			else
				newValues[i] = newValues[i] * 12.92f;
		}
		return new Color(newValues[0] * 255, newValues[1] * 255, newValues[2] * 255);
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
	 
	 //public double getGammaCorrectedValue(double val)
	 //{
	 //	 return Math.pow(val, frame.getGamma());
	 //}
	 
	 public void createColorList()
	 {
		 for (int i = 0; i <=100; i++)
			 color[i] = (double)i / 100d;
		 /*ArrayList<Point> colorList = new ArrayList<Point>();
		 int[] dist = gt.getDistribution();
		 //for (int i = 0; i < dist.length; i++)
			// System.out.println("Wert " + i + ": " + dist[i]);
		  // initialize linear list
		 for (int i = 0; i < dist.length; i++)
			 if (dist[i] > 0)
				 colorList.add(new Point(i, i));
		  // calculate new distances
		 double[] distanceX = new double[colorList.size() - 1];
		 double sum = 0;
		 for (int i = 1; i < colorList.size(); i++)
		 {
			 distanceX[i - 1] = getDesiredDistance(colorList.get(i - 1), colorList.get(i)); 
			 sum += distanceX[i - 1];
		 }
		 // calculate new positions
		 double counter = 0;
		 for (int i = 1; i < colorList.size() - 1; i++)
		 {
			 double x, y;
			 y = (int)colorList.get(i).getY();
			 counter += distanceX[i - 1];
			 x = (counter / sum) * 100;
			 colorList.get(i).setLocation(x, y);
		 }
		 
		 for (int i = 0; i < colorList.size(); i++)
		 {
			 double x, y;
			 y = (int)colorList.get(i).getY();
			 x = (int)colorList.get(i).getX();
			 color[(int)y] = (int)x;
		 }
		 
		 for (int i = 1; i < color.length; i++)
		 {
			 if (color[i] == 0)
				 color[i] = color[i - 1];
		 }*/
		 // Test
		 //for (int i = 0; i < color.length; i++)
		//	 System.out.println("Wert " + i + " liegt bei Farbe " + color[i]);
	 }
	 
	 protected double getDesiredDistance(Point a, Point b)
	 {
		 return (Math.sqrt(b.getX() - a.getX()));
	 }
} // end of class LabelRenderer
