/**
 * 
 */
package visualizer.userInterface;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import visualizer.display.GlyphTable;
import visualizer.display.PixelRenderer;

/**
 * @author rene
 *
 */
public class DistroPanel extends JPanel
{
	protected GlyphTable gt;
	
	public DistroPanel(GlyphTable tab) 
	{
		gt = tab;
	}
	
	public void paintComponent(Graphics g) 
    {
    	super.paintComponent(g);       
        // draw the probability function
    	int[] dist = gt.getDistribution();
    	int highest = 0;
    	// find the highest value
    	for (int i = 1; i <= 100; i++)
        	if (dist[i] > highest)
        		highest = dist[i];
    	// draw the function
    	g.setColor(Color.white);
    	g.fillRect(0, 0, 200, 100);
     	g.setColor(Color.black);
     	int length;
     	for (int i = 0; i <= 100; i++)
    	{
        	length = (int)((double)dist[i] / (double)highest * 100d);
        	if (length == 0 && dist[i] > 0)
        		length = 1;
    		g.fillRect(i * 2, 100 - length, 2, length);
    		//g.drawLine(i-1, 100-lastLength, i, 100-length);
        }
    }  
}