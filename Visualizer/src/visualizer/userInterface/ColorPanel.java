
package visualizer.userInterface;

import java.awt.Graphics;
import javax.swing.JPanel;

import visualizer.display.PixelRenderer;

/**
 * @author Rene Wegener
 *
 * The ColorPanel is just a bar showing the current 
 * color scale
 */
public class ColorPanel extends JPanel
{
	protected PixelRenderer pixRen;
	
	public ColorPanel() 
	{
	}
	
	public void setRenderer(PixelRenderer ren)
	{
		pixRen = ren;
	}

    public void paintComponent(Graphics g) 
    {
    	super.paintComponent(g);       
        // draw the color scale
        if (pixRen != null)
	        for (int x = 0; x <= 100; x++)
	        {
	        	double v = (double)x / 100d;
	        	g.setColor(pixRen.getColor(v));
	        	g.fillRect(x * 2, 0, 2, 100);
	        }
    }  
}
