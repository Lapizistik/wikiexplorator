package visualizer.display;



import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Vector;

import prefuse.action.layout.SpecifiedLayout;
import prefuse.visual.AggregateItem;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;

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
	 * create a layout row by row. As for all methods, 
	 * several parameters have to be given:
	 * v is a vector holding the Points that will be
	 * arranged. startX and startY are the x- and y-
	 * coordinates at which the layout algorithm will
	 * start to place the points. itemWidth and itemHeight
	 * represent the size of the items that shall be placed.
	 * This algorithm tries to create a square matrix.
	*/
	public static void createRowLayout(Vector v, int startX, int startY, int itemWidth,
			int itemHeight)
	{
		int squareWidth = (int)(Math.ceil(Math.sqrt(v.size())));
		createRowLayout(v, startX, startY, itemWidth, itemHeight,
				squareWidth, squareWidth);
	}
	
	/**
	 * create a layout row by row. As for all methods, 
	 * several parameters have to be given:
	 * v is a vector holding the Points that will be
	 * arranged. startX and startY are the x- and y-
	 * coordinates at which the layout algorithm will
	 * start to place the points. itemWidth and itemHeight
	 * represent the size of the items that shall be placed.
	 * matrixWidth and matrixHeight define the desired size
	 * of the whole matrix.
	 */
	public static void createRowLayout(Vector v, int startX, int startY, int itemWidth,
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
				Point actItem = (Point)v.get(currentIndex);
				actItem.setLocation(startX + x * itemWidth, 
						startY + y * itemHeight);
				currentIndex++;
			}
	}
	
	/**
	 * create a layout column by column. As for all methods, 
	 * several parameters have to be given:
	 * v is a vector holding the Points that will be
	 * arranged. startX and startY are the x- and y-
	 * coordinates at which the layout algorithm will
	 * start to place the points. itemWidth and itemHeight
	 * represent the size of the items that shall be placed.
	 * matrixWidth and matrixHeight define the desired size
	 * of the whole matrix.
	 */
	public static void createColumnLayout(Vector v, int startX, int startY, int itemWidth,
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
				Point actItem = (Point)v.get(currentIndex);
				actItem.setLocation(startX + x * itemWidth, 
						startY + y * itemHeight);
				currentIndex++;
			}
	}
	/**
	 * Create a recursive Morton Z-Curve. Although the method
	 * will work on a set of items of any size, the best
	 * result is achieved if the size is a number 4^n.
	 * The meaning of the parameters is the same as for the 
	 * method createSimpleLayout.
	 */
	public static void createZLayout(Vector v, int startX, int startY,
			int itemWidth, int itemHeight)
	{
		// how many space is needed for the whole
		// set of items?
		int squareWidth = curveSize(v.size());
		
		// if more then four items are left, divide them into
		// four quarters by recursion
		if (v.size() > 4)
		{
			Vector[] newVec = new Vector[4];
			int currentIndex = 0;
			int vecSize = squareWidth * squareWidth / 4;
			
			for (int i = 0; i < 4; i++)
			{
				newVec[i] = new Vector();
				
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
		else // <= 4 items
		{
			// first row
			for (int i = 0; i <= 1; i++)
			{
				if (i >= v.size())
					break;
				Point actItem = (Point)v.get(i);
				actItem.setLocation(startX + i * itemWidth, startY);
			}
			// second row
			if (v.size() > 2)
			{
				for (int i = 0; i <= 1; i++)
				{
					if (i + 2 >= v.size())
						break;
					Point actItem = (Point)v.get(i + 2);
					actItem.setLocation(startX + i * itemWidth, startY + itemHeight);
				}
			}
		}
	}
	
	
	/**
	 * Create a recursive Z-Curve that tries to create
	 * a square even on item sets of a size that's far
	 * bigger or smaller than 4^n.
	 * The meaning of the parameters is the same as for the 
	 * method createSimpleLayout.
	 */
	public static void createFlexibleZLayout(Vector v, int startX, int startY,
			int itemWidth, int itemHeight)
	{
		createFlexibleZLayout(v, startX, startY, itemWidth, itemHeight, 0, 0);
	}
	
	private static void createFlexibleZLayout(Vector v, int startX, int startY,
			int itemWidth, int itemHeight, int desiredWidth, int desiredHeight)
	{
		// get the desired size of each VisualItem
		//itemWidth += abstand;
		//itemHeight += abstand;
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
		if (v.size() > 9)
		{
			Vector[] newVec = new Vector[4];
			int[] width = new int[4];
			int[] height = new int[4];
			width[0] = (int)(Math.ceil(desiredWidth / 2d));
			height[0] = (int)(Math.ceil(desiredWidth / 2d));
			width[1] = desiredWidth - width[0];
			height[1] = height[0];
			width[2] = width[0];
			height[2] = desiredHeight - height[0];
			width[3] = width[1];
			height[3] = height[2];
			
			int currentIndex = 0;
			for (int i = 0; i < 4; i++)
			{
				newVec[i] = new Vector();
				
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
		else // <= 9 items
		{
			if (desiredWidth <= 0)
				desiredWidth = (int)(Math.ceil(Math.sqrt(v.size())));
			if (desiredHeight <= 0)
				desiredHeight = (int)(Math.ceil(Math.sqrt(v.size())));
			// there are several possibilities:
			// 2x2 layout, 2x3, 3x3 and 3x3
			int currentIndex = 0;
			for (int y = 0; y < desiredHeight; y++)
				for (int x = 0; x < desiredWidth; x++)
				{
					if (currentIndex >= v.size())
						break;
					Point actItem = (Point)v.get(currentIndex);
					actItem.setLocation(startX + x * itemWidth, startY + y * itemHeight);
					currentIndex++;
				}
		}
	}
	
	/**
	 * Create a layout similar to a simple row layout
	 * but with rows of a width of four pixels. This might
	 * result in a more intuitive visualization than
	 * the simple row layout.
	 * The meaning of the parameters is the same as for the 
	 * method createSimpleLayout.
	 */
	public static void createLineLayout(Vector v, int startX, int startY,
			int itemHeight, int itemWidth)
	{
		int pixelsPerBlock = 4;
		// get the desired size of each VisualItem
		//itemWidth += abstand;
		//itemHeight += abstand;
		// how many space is needed for the whole
		// set of items?
		int width = (int)(Math.ceil(Math.sqrt(v.size())));
		int height = width;
		if (v.size() <= 16)
		{
			width = 4;
			height = 4;
		}
		else
		{
			while (width * (height - 1) >= v.size())
				height--;
			while (height % pixelsPerBlock != 0)
				height--;
			while (height * width < v.size())
				width++;
		}
		// now place the items
		boolean upDown = true;
		int currentIndex = 0;
		for (int y = 0; y < height/pixelsPerBlock; y++)
			for (int x = 0; x < width; x++)
			{
				for (int line = 0; line < pixelsPerBlock; line++)
				{
					if (currentIndex >= v.size())
						break;
					Point actItem = (Point)v.get(currentIndex);
					if (upDown)
						actItem.setLocation(startX + x * itemWidth, (startY + (y * pixelsPerBlock * itemHeight) + line * itemHeight));
					else
						actItem.setLocation(startX + x * itemWidth, (startY + ((y + 1) * pixelsPerBlock * itemHeight) - line * itemHeight - itemHeight));
					currentIndex++;
				}
				upDown = !upDown;
			}
	}
	
	/**
	 * At the moment not implemented correctly.
	 */
	public static void createRecursiveLayout(Vector v, int startX, int startY,
			int itemWidth, int itemHeight)
	{
		// first level: 7 in a row (one week)
		// second level: 5 rows (one month)
		// third level: 3 in a row (one quarter)
		VisualItem item = (VisualItem)v.get(0);
		//itemWidth = itemWidth + abstand;
		// row by row, column by column
		int posX, posY;
		int currentIndex = 0;
		int numberOfMonths = v.size() / 35 + 1;
		
		for (int month = 0; month <= numberOfMonths; month++)
		{
			if (currentIndex >= v.size())
				break;
			posX = startX + month * 7 * itemWidth;
			posY = startY;
			
			for (int week = 0; week<= 4; week++)
			{
				if (currentIndex >= v.size())
					break;
				for (int day = 0; day <= 6; day++)
				{
					if (currentIndex >= v.size())
						break;
					VisualItem actItem = ((VisualItem)v.get(currentIndex));
					actItem.set("xCor", new Integer(posX));
					actItem.set("yCor", new Integer(posY));
					posX += itemWidth;
					currentIndex++;
				}
				posX = posX - 7 * itemWidth;
				posY += itemWidth;
			}
		}
	}
	
	/**
	 * Create a recursive Hilbert Curve. Although the method
	 * will work on a set of items of any size, the best
	 * result is achieved if the size is a number 4^n.
	 * The meaning of the parameters is the same as for the 
	 * method createSimpleLayout.
	 */
	public static void createHilbertLayout(Vector v, int startX, int startY,
			int itemWidth, int itemHeight)
	{
		createHilbertLayout(v, startX, startY, itemWidth, itemHeight, "up");
	}
	
	private static void createHilbertLayout(Vector v, int startX, int startY,
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
	    		actItem[0].setLocation(xLeft, yUp);
	    		if (v.size() > 1)
					actItem[1].setLocation(xRight, yUp);
				if (v.size() > 2)
					actItem[2].setLocation(xRight, yDown);
				if (v.size() > 3)
					actItem[3].setLocation(xLeft, yDown);
			}
		    else if (dir.equals("right"))
		    {
		    	actItem[0].setLocation(xRight, yDown);
				if (v.size() > 1)
					actItem[1].setLocation(xLeft, yDown);
				if (v.size() > 2)
					actItem[2].setLocation(xLeft, yUp);
				if (v.size() > 3)
					actItem[3].setLocation(xRight, yUp);
			}    
		    else if (dir.equals("up"))
		    {
		     	actItem[0].setLocation(xLeft, yUp);
				if (v.size() > 1)
					actItem[1].setLocation(xLeft, yDown);
				if (v.size() > 2)
					actItem[2].setLocation(xRight, yDown);
				if (v.size() > 3)
					actItem[3].setLocation(xRight, yUp);
			}
		    else if (dir.equals("down"))
		    {
		     	actItem[0].setLocation(xRight, yDown);
				if (v.size() > 1)
					actItem[1].setLocation(xRight, yUp);
				if (v.size() > 2)
					actItem[2].setLocation(xLeft, yUp);
				if (v.size() > 3)
					actItem[3].setLocation(xLeft, yDown);
			} 
		}
		else // Recursion
		{
			Vector[] newVec = new Vector[4];
			int currentIndex = 0;
			for (int i = 0; i < 4; i++)
			{
				newVec[i] = new Vector();
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
			      createHilbertLayout(newVec[0], startX, startY, itemWidth, itemHeight, "up");
			      if (newVec[1].size() > 0)
			    	  createHilbertLayout(newVec[1], startX + squareWidth, startY, itemWidth, itemHeight, "left");
			      if (newVec[2].size() > 0)
			    	  createHilbertLayout(newVec[2], startX + squareWidth, startY + squareHeight, itemWidth, itemHeight, "left");
			      if (newVec[3].size() > 0)
			    	  createHilbertLayout(newVec[3], startX, startY + squareHeight, itemWidth, itemHeight, "down");
			}
			else if (dir.equals("right"))
			{
			      createHilbertLayout(newVec[0], startX + squareWidth, startY + squareHeight, itemWidth, itemHeight, "down");
			      if (newVec[1].size() > 0)
			    	  createHilbertLayout(newVec[1], startX, startY + squareHeight, itemWidth, itemHeight, "right");
			      if (newVec[2].size() > 0)
				    	createHilbertLayout(newVec[2], startX, startY, itemWidth, itemHeight, "right");
			      if (newVec[3].size() > 0)
				    	createHilbertLayout(newVec[3], startX + squareWidth, startY, itemWidth, itemHeight, "up");
			}
			else if (dir.equals("up"))
			{
			      createHilbertLayout(newVec[0], startX, startY, itemWidth, itemHeight, "left");
			      if (newVec[1].size() > 0)
				    	createHilbertLayout(newVec[1], startX, startY + squareHeight, itemWidth, itemHeight, "up");
			      if (newVec[2].size() > 0)
				    	createHilbertLayout(newVec[2], startX + squareWidth, startY + squareHeight, itemWidth, itemHeight, "up");
			      if (newVec[3].size() > 0)
				    	createHilbertLayout(newVec[3], startX + squareWidth, startY, itemWidth, itemHeight, "right");
			}
			else if (dir.equals("down"))
			{
			      createHilbertLayout(newVec[0], startX + squareWidth, startY + squareHeight, itemWidth, itemHeight, "right");
			      if (newVec[1].size() > 0)
				    	createHilbertLayout(newVec[1], startX + squareWidth, startY, itemWidth, itemHeight, "down");
			      if (newVec[2].size() > 0)
				    	createHilbertLayout(newVec[2], startX, startY, itemWidth, itemHeight, "down");
			      if (newVec[3].size() > 0)
				    	createHilbertLayout(newVec[3], startX, startY + squareHeight, itemWidth, itemHeight, "left");
			}
		} 
	}
	
	/**
	 * Create a layout for the labeling of the glyphs.
	 * At the moment this doesn't work correctly.
	 */
	public static void createLabelLayout(Vector label, Vector data, int abstand)
	{
		VisualItem first;
		first = (VisualItem)data.get(0);
		int headerX = ((Integer)first.get("xCor")).intValue();
		int headerY = ((Integer)first.get("yCor")).intValue();
		int itemWidth = ((Integer)first.get("width")).intValue() + abstand;
		int itemHeight = ((Integer)first.get("height")).intValue() + abstand;
		int sideX = headerX;
		int sideY = headerY + itemHeight / 2;
		int currentIndex = 0;
		VisualItem actItem = (VisualItem)label.get(currentIndex);
		// place headers
		while (((VisualItem)label.get(currentIndex)).get("position").equals("header"))
		{
			actItem = (VisualItem)label.get(currentIndex);
			actItem.set("xCor", new Integer(headerX));
			actItem.set("yCor", new Integer(headerY));
			headerX += itemWidth;
			currentIndex++;
		}
		// place left side labeling
		while (currentIndex < label.size())
		{
			actItem = (VisualItem)label.get(currentIndex);
			actItem.set("xCor", new Integer(sideX - itemWidth));
			actItem.set("yCor", new Integer(sideY));
			sideY += itemHeight;
			currentIndex++;
		}
	}
	
	/**
	 * Compute the width/height of s square that is 
	 * just big enough to hold num data values.
	 */
	public static int curveSize(int num)
	{
		int counter = 1;
		
		for (int i = 1; i < 12; i++)
		{
			if (counter >= num)
				break;
			else
				counter = counter * 4;
		}
		
		return (int)(Math.sqrt(counter));
	}
}
