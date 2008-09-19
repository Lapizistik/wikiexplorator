package visualizer.display;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import color.LabColorSpace;
import prefuse.render.LabelRenderer;
import prefuse.visual.VisualItem;
import visualizer.StringConstants;
import visualizer.userInterface.PixelFrame;

/**
 * The PixelRenderer draws the items onto the screen
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
	
	/**
	 * 
	 * @param n name of the VisualItem group
	 * @param tab the GlyphTable belonging to this renderer
	 */
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
	
	/**
	 * 
	 * @return the color scale as a double array; the
	 * indices range from 0 to 100
	 */
	public double[] getColors()
	{
		return color;
	}
	
	/**
	 * set the whole color scale
	 * @param col new scale
	 */
	public void setColors(double[] col)
	{
		color = col;
	}
	
	/**
	 * 
	 * @param pf the PixelFrame using this PixelRenderer
	 */
	public void setFrame(PixelFrame pf)
	{
		frame = pf;
	}
	
	/**
	 * draw a VisualItem onto a given graphic context
	 */
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
	
	/** 
	 * tell the Renderer whether to draw table descriptions
	 * or not
	 * @param bol true if row/column names (3D data) or the 
	 * names of the items (2D data) should bee drawn
	 */
	public void setTableLabeling(boolean bol)
	{
		table = bol;
	}
	
	// draw a single pixel
	protected void drawPixel(Graphics2D gr, double v, int pX, int pY)
	{
		gr.setColor(getColor(v));
		gr.fillRect(pX, pY, 1, 1);
	}
	
	// return a grey color matching the given value
	protected Color getGreyColor(float l)
	{
		float[] lab = new LabColorSpace().toRGB(new float[]{l, 0, 0});
		return new Color(lab[0], lab[1], lab[2]);
	}
	
	// Convert linear rgb values into Java's nonlinear
	// sRGB scale
	protected float[] getSRGB(float[] rgb)
	{
		float[] sRGB = new float[3];
		for (int i = 0; i <= 2; i++)
			// gamma transformation
			sRGB[i] = 1.055f * (float)Math.pow(rgb[i], 1f/2.4f) - 0.055f;
		return sRGB;
	}
	
	/**
	 * get the color matching the given data value
	 * @param v value of the data
	 * @return matching color
	 */
	public Color getColor(double v)
	{
		int red, green, blue;
		double a1, a2, a3;
		Color c;
		
		// first get the color 'weight' from the table
		v = color[(int)Math.round(v * 100d)];
		// invert color if necessary
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
			
			float sRGB[] = getSRGB(new float[]{(float)a1, (float)a2, (float)a3});
			red = (int)(255 * sRGB[0]);
			green = (int)(255 * sRGB[1]);
			blue = (int)(255 * sRGB[2]);
			if (red < 0)
				red = 0;
			if (green < 0)
				green = 0;
			if (blue < 0)
				blue = 0;
			c = new Color(red, green, blue);
		}
		else 
		{
			c = getGreyColor((float)v * 100f);
		}
		return c;
	}
	
	// get the shape of a glyph
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
	 
	 // initialize a linear color scale; 
	 protected void createColorList()
	 {
		 for (int i = 0; i <= 100; i++)
			 color[i] = (double)i / 100d;
	 }
}
