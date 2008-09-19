package visualizer.display;


import java.awt.Point;
import java.util.ArrayList;

/**
 * This class contains only static methods that will compute
 * a specific layout like a Morton Z-Curve for a set of items.
 * 
 * @author Rene Wegener
 *
 */
public class Layouts 
{
	/**
	 * create a layout row by row
	 * @param v an ArrayList holding the Points that will be
	 * arranged 
	 * @param startX x-coordinate at which the algorithm starts 
	 * @param startY y-coordinate at which the algorithm starts
	 * @param itemWidth width of the items 
	 * @param itemHeight height of the items
	 * @param matrixWidth number of items per row
	 * @param matrixHeight number of rows
	*/
	public static void createRowLayout(ArrayList<Point> v, int startX, int startY, int itemWidth,
			int itemHeight, int matrixWidth, int matrixHeight)
	{
		if (matrixWidth <= 0 && matrixHeight <= 0)
		{
			matrixWidth = (int)(Math.ceil(Math.sqrt(v.size())));
			matrixHeight = (int)(Math.ceil(Math.sqrt(v.size())));
		}
		else if (matrixWidth <= 0)
		{
			matrixWidth = (int)Math.ceil(v.size() / (double)matrixHeight);
		}
		else if (matrixHeight <= 0)
		{
			matrixHeight = (int)Math.ceil(v.size() / (double)matrixWidth);
		}
		int currentIndex = 0;
		for (int y = 0; y < matrixHeight; y++)
			for (int x = 0; x < matrixWidth; x++)
			{
				if (currentIndex >= v.size())
					break;
				Point actItem = v.get(currentIndex);
				actItem.setLocation(startX + x * itemWidth, 
						startY + y * itemHeight);
				currentIndex++;
			}
	}
	
	/**
	 * create a table layout for 2D data; similar to 
	 * row-by-row, but with
	 * descriptions each item
	 * @param v an ArrayList holding the Points that will be
	 * arranged 
	 * @param startX x-coordinate at which the algorithm starts 
	 * @param startY y-coordinate at which the algorithm starts
	 * @param itemWidth width of the items 
	 * @param itemHeight height of the items
	 * @param gt GlyphTable which holds the data
	*/
	public static void createTable2D(ArrayList<Point> v, int startX, int startY, 
			int itemWidth, int itemHeight, GlyphTable gt)
	{
		int tableWidth;
		int tableHeight;
		tableWidth = (int)Math.sqrt(v.size());
		tableHeight = (int)Math.ceil((double)v.size() / (double)tableWidth);
		
		// determine how much space is needed to show
		// the labels (authors' names)
		int space = gt.getMaxStringLength() + 10;
		itemWidth += space;
		
		// set positions
		int next = 0;
		for (int x = 0; x < tableWidth; x++)
			for (int y = 0; y < tableHeight; y++)
			{
				if (next >= v.size())
					break;
				Point point = v.get(next);
				point.setLocation(startX + x * itemWidth,
						startY + y * itemHeight);
				next++;
			}
	}
	
	/**
	 * create a layout column by column;
	 * @param v an ArrayList holding the Points that will be
	 * arranged 
	 * @param startX x-coordinate at which the algorithm starts 
	 * @param startY y-coordinate at which the algorithm starts
	 * @param itemWidth width of the items 
	 * @param itemHeight height of the items
	 * @param matrixWidth number of items per row
	 * @param matrixHeight number of rows
	*/
	public static void createColumnLayout(ArrayList<Point> v, int startX, int startY, int itemWidth,
			int itemHeight, int matrixWidth, int matrixHeight)
	{
		if (matrixWidth <= 0 && matrixHeight <= 0)
		{
			matrixWidth = (int)(Math.ceil(Math.sqrt(v.size())));
			matrixHeight = (int)(Math.ceil(Math.sqrt(v.size())));
		}
		else if (matrixWidth <= 0)
		{
			matrixWidth = (int)Math.ceil(v.size() / (double)matrixHeight);
		}
		else if (matrixHeight <= 0)
		{
			matrixHeight = (int)Math.ceil(v.size() / (double)matrixWidth);
		}
		int currentIndex = 0;
		for (int x = 0; x < matrixWidth; x++)
			for (int y = 0; y < matrixHeight; y++)
			{
				if (currentIndex >= v.size())
					break;
				Point actItem = v.get(currentIndex);
				actItem.setLocation(startX + x * itemWidth, 
						startY + y * itemHeight);
				currentIndex++;
			}
	}
	
	/**
	 * Create a recursive Morton Z-Curve; best results are 
	 * achieved if the number of points in 4^n 
	 * @param v an ArrayList holding the Points that will be
	 * arranged 
	 * @param startX x-coordinate at which the algorithm starts 
	 * @param startY y-coordinate at which the algorithm starts
	 * @param itemWidth width of the items 
	 * @param itemHeight height of the items
	 */
	public static void createZLayout(ArrayList<Point> v, int startX, int startY,
			int itemWidth, int itemHeight)
	{
		// how many space is needed for the whole
		// set of items?
		int squareWidth = curveSize(v.size());
		
		// if more then one item are left, divide them into
		// four quarters by recursion
		if (v.size() > 1)
		{
			ArrayList[] newVec = new ArrayList[4];
			int currentIndex = 0;
			int vecSize = squareWidth * squareWidth / 4;
			
			for (int i = 0; i < 4; i++)
			{
				newVec[i] = new ArrayList();
				
				for (int j = 0; j < vecSize; j++)
				{
					if (currentIndex >= v.size())
						break;
					newVec[i].add(v.get(currentIndex));
					currentIndex++;
				}
			}
			
			// Recursion
			if (newVec[0].size() > 0)
				createZLayout(newVec[0], startX, startY, itemWidth, itemHeight);
			if (newVec[1].size() > 0)
				createZLayout(newVec[1], startX + squareWidth / 2 * itemWidth, startY, itemWidth, itemHeight);
			if (newVec[2].size() > 0)
				createZLayout(newVec[2], startX, startY + squareWidth / 2 * itemHeight, itemWidth, itemHeight);
			if (newVec[3].size() > 0)
				createZLayout(newVec[3], startX + squareWidth / 2 * itemWidth, startY + squareWidth / 2 * itemHeight, itemWidth, itemHeight);
		} 
		else if (v.size() == 1)
		{
			Point actItem = v.get(0);
			actItem.setLocation(startX + 0 * itemWidth, startY);
		}
	}
	
	/**
	 * Create a recursive Z-Curve that tries to create
	 * a square even on item sets of a size that differs
	 * from 4^n
	 * @param v an ArrayList holding the Points that will be
	 * arranged 
	 * @param startX x-coordinate at which the algorithm starts 
	 * @param startY y-coordinate at which the algorithm starts
	 * @param itemWidth width of the items 
	 * @param itemHeight height of the items
	 */
	public static void createFlexibleZLayout(ArrayList<Point> v, int startX, int startY,
			int itemWidth, int itemHeight)
	{
		createFlexibleZLayout(v, startX, startY, itemWidth, itemHeight, 0, 0);
	}
	
	// the flexible z-curve method; desiredWidth 
	// and desiredHeight are parameters determining the
	// the size the z-curve should have
	protected static void createFlexibleZLayout(ArrayList<Point> v, int startX, int startY,
			int itemWidth, int itemHeight, int desiredWidth, int desiredHeight)
	{
		// how many space is needed for the whole
		// set of items?
		if (desiredWidth <= 0)
			desiredWidth = (int)(Math.ceil(Math.sqrt(v.size())));
		if (desiredHeight <= 0)
		{
			desiredHeight = desiredWidth;
			if ((desiredHeight-1) * desiredWidth >= v.size())
				desiredHeight--;
		}
		// if more then nine items are left, divide them into
		// four quarters by recursion
		if (v.size() > 1)
		{
			ArrayList<Point>[] newVec = new ArrayList[4];
			int[] width = new int[4];
			int[] height = new int[4];
			width[0] = (int)(Math.ceil(desiredWidth / 2d));
			height[0] = (int)(Math.ceil(desiredHeight / 2d));
			width[1] = desiredWidth - width[0];
			height[1] = height[0];
			width[2] = width[0];
			height[2] = desiredHeight - height[0];
			width[3] = width[1];
			height[3] = height[2];
			
			int currentIndex = 0;
			for (int i = 0; i < 4; i++)
			{
				newVec[i] = new ArrayList<Point>();
				
				for (int j = 0; j < width[i] * height[i]; j++)
				{
					if (currentIndex >= v.size())
						break;
					newVec[i].add(v.get(currentIndex));
					currentIndex++;
				}
			}
			
			// Recursion
			if (newVec[0].size() > 0)
				createFlexibleZLayout(newVec[0], startX, startY, itemWidth, itemHeight, width[0], height[0]);
			if (newVec[1].size() > 0)
				createFlexibleZLayout(newVec[1], startX + width[0] * itemWidth, startY,  itemWidth, itemHeight, width[1], height[1]);
			if (newVec[2].size() > 0)
				createFlexibleZLayout(newVec[2], startX, startY + height[0] * itemHeight,  itemWidth, itemHeight, width[2], height[2]);
			if (newVec[3].size() > 0)
				createFlexibleZLayout(newVec[3], startX + width[0] * itemWidth, startY + height[0] * itemHeight,  itemWidth, itemHeight, width[3], height[3]);
		} 
		else if (v.size() == 1)
		{
			Point actItem = v.get(0);
			actItem.setLocation(startX + 0 * itemWidth, startY + 0 * itemHeight);
		}
	}
	
	/**
	 * Create a recursive Hilbert Curve. Like the z-curve
	 * this method will work best on 4^n items.
	 * @param v an ArrayList holding the Points that will be
	 * arranged 
	 * @param startX x-coordinate at which the algorithm starts 
	 * @param startY y-coordinate at which the algorithm starts
	 * @param itemWidth width of the items 
	 * @param itemHeight height of the items
	 */
	public static void createHilbertLayout(ArrayList v, int startX, int startY,
			int itemWidth, int itemHeight)
	{
		createHilbertLayout(v, startX, startY, itemWidth, itemHeight, "down");
	}
	
	// creates the hilbert-curve; the paraneter dir tells
	// this recursive method which part of the hilbert curve
	// to create
	protected static void createHilbertLayout(ArrayList<Point> v, int startX, int startY,
			int itemWidth, int itemHeight, String dir)
	{
		int width = curveSize(v.size());
		if (width <= 2) 
		{
			Point actItem[] = new Point[v.size()];
	    	for (int i = 0; i < v.size(); i++)
	    		actItem[i] = (Point)v.get(i);
	    	int xLeft, xRight, yUp, yDown;
	    	xLeft = startX;
	    	xRight = startX + itemWidth;
	    	yUp = startY;
	    	yDown = startY + itemHeight;
	    	if (dir.equals("left"))
		    {
	    		actItem[0].setLocation(xLeft, yDown);
	    		if (v.size() > 1)
					actItem[1].setLocation(xRight, yDown);
				if (v.size() > 2)
					actItem[2].setLocation(xRight, yUp);
				if (v.size() > 3)
					actItem[3].setLocation(xLeft, yUp);
			}
		    else if (dir.equals("right"))
		    {
		    	actItem[0].setLocation(xRight, yUp);
				if (v.size() > 1)
					actItem[1].setLocation(xLeft, yUp);
				if (v.size() > 2)
					actItem[2].setLocation(xLeft, yDown);
				if (v.size() > 3)
					actItem[3].setLocation(xRight, yDown);
			}    
		    else if (dir.equals("up"))
		    {
		     	actItem[0].setLocation(xRight, yUp);
				if (v.size() > 1)
					actItem[1].setLocation(xRight, yDown);
				if (v.size() > 2)
					actItem[2].setLocation(xLeft, yDown);
				if (v.size() > 3)
					actItem[3].setLocation(xLeft, yUp);
			}
		    else if (dir.equals("down"))
		    {
		     	actItem[0].setLocation(xLeft, yDown);
				if (v.size() > 1)
					actItem[1].setLocation(xLeft, yUp);
				if (v.size() > 2)
					actItem[2].setLocation(xRight, yUp);
				if (v.size() > 3)
					actItem[3].setLocation(xRight, yDown);
			} 
		}
		else // Recursion
		{
			ArrayList<Point>[] newVec = new ArrayList[4];
			int currentIndex = 0;
			for (int i = 0; i < 4; i++)
			{
				newVec[i] = new ArrayList<Point>();
				int start = currentIndex;
				for (int j = currentIndex; j < start + ((width/2)*(width/2)); j++)
				{
					if (currentIndex >= v.size())
						break;
					else
						newVec[i].add(v.get(currentIndex));
					currentIndex++;
				}
			}
			int squareWidth = (width / 2) * itemWidth;
			int squareHeight = (width / 2) * itemHeight;
			
			if (dir.equals("left"))
			{
			      createHilbertLayout(newVec[0], startX, startY + squareHeight, itemWidth, itemHeight, "up");
			      if (newVec[1].size() > 0)
			    	  createHilbertLayout(newVec[1], startX + squareWidth, startY + squareHeight, itemWidth, itemHeight, "left");
			      if (newVec[2].size() > 0)
			    	  createHilbertLayout(newVec[2], startX + squareWidth, startY, itemWidth, itemHeight, "left");
			      if (newVec[3].size() > 0)
			    	  createHilbertLayout(newVec[3], startX, startY, itemWidth, itemHeight, "down");
			}
			else if (dir.equals("right"))
			{
			      createHilbertLayout(newVec[0], startX + squareWidth, startY, itemWidth, itemHeight, "down");
			      if (newVec[1].size() > 0)
			    	  createHilbertLayout(newVec[1], startX, startY, itemWidth, itemHeight, "right");
			      if (newVec[2].size() > 0)
				    	createHilbertLayout(newVec[2], startX, startY + squareHeight, itemWidth, itemHeight, "right");
			      if (newVec[3].size() > 0)
				    	createHilbertLayout(newVec[3], startX + squareWidth, startY + squareHeight, itemWidth, itemHeight, "up");
			}
			else if (dir.equals("up"))
			{
			      createHilbertLayout(newVec[0], startX + squareWidth, startY, itemWidth, itemHeight, "right");
			      if (newVec[1].size() > 0)
				    	createHilbertLayout(newVec[1], startX + squareWidth, startY + squareHeight, itemWidth, itemHeight, "up");
			      if (newVec[2].size() > 0)
				    	createHilbertLayout(newVec[2], startX, startY + squareHeight, itemWidth, itemHeight, "up");
			      if (newVec[3].size() > 0)
				    	createHilbertLayout(newVec[3], startX, startY, itemWidth, itemHeight, "left");
			}
			else if (dir.equals("down"))
			{
			      createHilbertLayout(newVec[0], startX, startY + squareHeight, itemWidth, itemHeight, "left");
			      if (newVec[1].size() > 0)
				    	createHilbertLayout(newVec[1], startX, startY, itemWidth, itemHeight, "down");
			      if (newVec[2].size() > 0)
				    	createHilbertLayout(newVec[2], startX + squareWidth, startY, itemWidth, itemHeight, "down");
			      if (newVec[3].size() > 0)
				    	createHilbertLayout(newVec[3], startX + squareWidth, startY + squareHeight, itemWidth, itemHeight, "right");
			}
		} 
	}
	
	// calculates the width/height of a square just big
	// enough to hold num data values; this square is 
	// supposed to have a side length of 2*n; the method is
	// used by z-curve and hilbert-curve to determine the
	// size they need to arrange the items
	protected static int curveSize(int num)
	{
		int counter = 1;
		
		while (true)
		{
			if (counter >= num)
				break;
			else
				counter = counter * 4;
		}
		
		return (int)(Math.sqrt(counter));
	}
}
