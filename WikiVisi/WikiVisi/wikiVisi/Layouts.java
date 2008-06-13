package wikiVisi;

import java.awt.Graphics2D;
import java.util.Vector;

import prefuse.action.layout.SpecifiedLayout;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;

public class Layouts extends SpecifiedLayout
{
	protected int pixelSize;
	
	public Layouts(String arg0, String arg1, String arg2,
			int ps) 
	{
		super(arg0, arg1, arg2);
		pixelSize = ps;
	}

	public static void createSimpleLayout(Vector v, int startX, int startY,
			int abstand)
	{
		VisualItem item = (VisualItem)v.get(0);
		int itemWidth = ((Integer)item.get("width")).intValue();
		int itemHeight = ((Integer)item.get("height")).intValue();
		itemWidth = itemWidth + abstand;
		itemHeight = itemHeight + abstand;
		// how many space is needed for the whole
		// set of items?
		int squareWidth = (int)(Math.ceil(Math.sqrt(v.size())));
		int currentIndex = 0;
		for (int i = 0; i < squareWidth; i++)
			for (int j = 0; j < squareWidth; j++)
			{
				if (currentIndex >= v.size())
					break;
				VisualItem actItem = ((VisualItem)v.get(currentIndex));
				int x = startX + j * itemWidth;
				int y = startY + i * itemHeight;
				actItem.set("xCor", new Integer(x));
				actItem.set("yCor", new Integer(y));
				currentIndex++;
			}
	}
	
	public static void createZLayout(Vector v, int startX, int startY,
			int abstand)
	{
		// get the desired size of each VisualItem
		VisualItem item = (VisualItem)v.get(0);
		int itemWidth = ((Integer)item.get("width")).intValue();
		int itemHeight = ((Integer)item.get("height")).intValue();
		itemWidth += abstand;
		itemHeight += abstand;
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
				createZLayout(newVec[0], startX, startY, abstand);
			if (newVec[1].size() > 0)
				createZLayout(newVec[1], startX + squareWidth / 2 * itemWidth, startY, abstand);
			if (newVec[2].size() > 0)
				createZLayout(newVec[2], startX, startY + squareWidth / 2 * itemHeight, abstand);
			if (newVec[3].size() > 0)
				createZLayout(newVec[3], startX + squareWidth / 2 * itemWidth, startY + squareWidth / 2 * itemHeight, abstand);
		} 
		else // <= 4 items
		{
			// first row
			for (int i = 0; i <= 1; i++)
			{
				if (i >= v.size())
					break;
				VisualItem actItem = ((VisualItem)v.get(i));
				actItem.set("xCor", new Integer(startX + i * itemWidth));
				actItem.set("yCor", new Integer(startY));
			}
			// second row
			if (v.size() > 2)
			{
				for (int i = 0; i <= 1; i++)
				{
					if (i + 2 >= v.size())
						break;
					VisualItem actItem = ((VisualItem)v.get(i + 2));
					actItem.set("xCor", new Integer(startX + i * itemWidth));
					actItem.set("yCor", new Integer(startY + itemHeight));
				}
			}
		}
	}
	
	public static void createFlexibleZLayout(Vector v, int startX, int startY,
			int abstand)
	{
		createFlexibleZLayout(v, startX, startY, abstand, 0, 0);
	}
	
	public static void createFlexibleZLayout(Vector v, int startX, int startY,
			int abstand, int desiredWidth, int desiredHeight)
	{
		// get the desired size of each VisualItem
		VisualItem item = (VisualItem)v.get(0);
		int itemWidth = ((Integer)item.get("width")).intValue();
		int itemHeight = ((Integer)item.get("height")).intValue();
		itemWidth += abstand;
		itemHeight += abstand;
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
				createFlexibleZLayout(newVec[0], startX, startY, abstand, width[0], height[0]);
			if (newVec[1].size() > 0)
				createFlexibleZLayout(newVec[1], startX + width[0] * itemWidth, startY, abstand, width[1], height[1]);
			if (newVec[2].size() > 0)
				createFlexibleZLayout(newVec[2], startX, startY + height[0] * itemHeight, abstand, width[2], height[2]);
			if (newVec[3].size() > 0)
				createFlexibleZLayout(newVec[3], startX + width[0] * itemWidth, startY + height[0] * itemHeight, abstand, width[3], height[3]);
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
					VisualItem actItem = ((VisualItem)v.get(currentIndex));
					actItem.set("xCor", new Integer(startX + x * itemWidth));
					actItem.set("yCor", new Integer(startY + y * itemHeight));
					currentIndex++;
				}
		}
	}
	
	public static void createRecursiveLayout(Vector v, int startX, int startY,
			int abstand)
	{
		// first level: 7 in a row (one week)
		// second level: 5 rows (one month)
		// third level: 3 in a row (one quarter)
		VisualItem item = (VisualItem)v.get(0);
		int itemWidth = ((Integer)item.get("width")).intValue();
		itemWidth = itemWidth + abstand;
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
