/**
 * 
 */
package visualizer.userInterface;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import visualizer.display.PixelRenderer;

/**
 * 
 * This panel lets the user select the color scale
 * 
 * @author Rene Wegener
 */
public class ColorCurvePanel extends JPanel implements MouseListener, MouseMotionListener
{
	protected TreeMap<Integer, Integer> coord;
	protected int moveflag = -1;	//flag to notify if user is moving a point
	protected int[] colors = new int[101]; 
	protected PixelRenderer pixRen;
	protected ColorPanel colPan;
	
	/**
	 * create new ColorCurvePanel
	 * @param pan the colorPanel which shows the color scale the user selects
	 * @param ren the visualization's PixelRenderer
	 */
	public ColorCurvePanel(ColorPanel pan, PixelRenderer ren) 
	{
		colPan = pan;
		pixRen = ren;
		coord = new TreeMap<Integer, Integer>();
		coord.put(0, 200);
		coord.put(200, 0);
		//updateValues();
		for (int i = 0; i <= 100; i++)
			colors[i] = (int)(pixRen.getColors()[i] * 200);
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public void paintComponent(Graphics gr) 
    {
    	super.paintComponent(gr);     
    	Graphics2D g = (Graphics2D)gr;
		//prepare screen
		g.setColor(Color.white);
		g.fillRect(0,0,getWidth(), getHeight());
		g.setColor(Color.black);
		//Draw control points and polygon
		Object[] keys = coord.keySet().toArray();
		Object[] values = coord.values().toArray();
		for(int i=0;i < coord.size();i++) 
		{
			g.fillRect((Integer)keys[i]-3, (Integer)values[i]-3,
					5, 5);
				
			if (i < coord.size() - 1)
				g.drawLine((Integer)keys[i],(Integer)values[i],
						(Integer)keys[i+1], (Integer)values[i+1]);
		}
		g.setColor(Color.red);
		for (int i = 0; i <= 100; i++)
			g.fillRect(i*2, 200 - colors[i], 2, 2);
		if (colPan != null)
			colPan.repaint();
	}
    
	// get the key of the point at x, y
    protected int getPoint(int x, int y)
	{
		int key = -1;
		
		for (int i = -3; i <= 3; i++)
			if (coord.containsKey(x + i))
			{
				if (coord.get(x + i) >= y - 3 &&
						coord.get(x + i) <= y + 3)
					key = x + i;
			}
		
		return key;
	}

    public void mouseClicked(MouseEvent evt) 
	{
    	int x = evt.getX();
		int y = evt.getY();
		int key = getPoint(x, y);
		//if there are less than four points, add another one
		if(!evt.isMetaDown()) // left button 
		{
			if (key == -1)
				coord.put(x, y);
			else
				moveflag = key;
		}
		//otherwise, check if user is trying to click on old point
		else // right button
		{
			if (key > 0 && key < 200)
				coord.remove(key);
		}
		updateValues();
		repaint();
	}

	public void mouseEntered(MouseEvent arg0) 
	{
	}

	public void mouseExited(MouseEvent arg0) 
	{
	}

	public void mousePressed(MouseEvent evt)
	{
		int x = evt.getX();
		int y = evt.getY();
		int key = getPoint(x, y);
		if(!evt.isMetaDown()) // left button 
		{
			moveflag = key;
			repaint();
		}
	}

	public void mouseReleased(MouseEvent arg0) 
	{
		moveflag = -1;
		updateValues();
		repaint();
	}

	public void mouseDragged(MouseEvent arg0) 
	{
		int x = arg0.getX();
		int y = arg0.getY();
		
		//check if user is trying to drag an old point
		if(moveflag > -1) 
		{
			coord.remove(moveflag);
			if (x < 0)
				x = 0;
			if (x > 200)
				x = 200;
			if (y < 0)
				y = 0;
			if (y > 200)
				y = 200;
			coord.put(x, y);
			moveflag = x;
			repaint();
		}
	}

	public void mouseMoved(MouseEvent arg0) 
	{
		
	}  
	
	// create curve from the coordinates
	protected void updateValues()
	{
		if (!coord.containsKey(0))
			coord.put(0, 200);
		if (!coord.containsKey(200))
			coord.put(200, 0);
		Object[] keys = coord.keySet().toArray();
		Object[] values = coord.values().toArray();
		int actX1 = (Integer)keys[0];
		int actY1 = (Integer)values[0];
		int actX2 = (Integer)keys[1];
		int actY2 = (Integer)values[1];
		
		double step = ((double)(200-actY2) - (double)(200-actY1)) / ((double)actX2 - (double)actX1);
		for(int i = 1; i < 200; i ++) 
		{
			if (coord.containsKey(i))
			{
				actX1 = i;
				actY1 = coord.get(i);
				actX2 = coord.higherKey(i);
				actY2 = coord.get(actX2);
				step = ((double)(200-actY2) - (double)(200-actY1)) / ((double)actX2 - (double)actX1);
			}
			colors[i/2] = (200-actY1) + (int)((double)(i - actX1) * step); 
		}	
		
		acceptValues();
	}
	
	// create a gamma curve
	protected void drawGamma(double gamma)
	{
		for (int i = 0; i <= 100; i++)
			colors[i] = (int)(200 * Math.pow((double)i/100d, gamma));
		acceptValues();
		repaint();
	}
	
	// accept the selected color scale
	protected void acceptValues()
	{
		double[] arr = new double[colors.length];
		for (int i = 0; i < colors.length; i++)
		{
			arr[i] = (double)colors[i] / 200d;
		}
		if (pixRen != null)
			pixRen.setColors(arr);
	}
}