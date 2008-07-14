/**
 * 
 */
package visualizer.display;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Vector;

import prefuse.visual.VisualItem;

/**
 * This class contains static layout algorithms similar
 * to the Layouts class. But OptimizingLayouts contains
 * methods that try to arrange objects using optimization 
 * algorithms e.g. for a more intuitive table.
 * 
 * @author Rene Wegener
 *
 */
public class OptimizingLayouts 
{
	public static void createOrderedTableLayout(Vector v, int startX, int startY, 
			int itemWidth, int itemHeight, GlyphTable gt)
	{
		int tableWidth = gt.getXAxisCount();
		int tableHeight = gt.getYAxisCount();
		
		// bubblesort the rows
		for (int i = 0; i < tableHeight; i++)
			for (int j = 0; j < tableHeight - 1; j++)
			{
				double val1 = getRowMean(v, j, tableWidth);
				double val2 = getRowMean(v, j + 1, tableWidth);
				if (val1 > val2)
				{
					moveRow(v, j, j + 1, tableWidth);
					moveColumn(v, j, j + 1, tableWidth, tableHeight);
				}
			}
		
		// optimize the layout by moving rows/columns
		int maxLoops = 100;
		for (int loop = 0; loop < maxLoops; loop++)
		{
			boolean moveRow = true;
			double newSim, bestSim, actSim;
			int bestOld = 0, bestNew = 0;
			actSim = getTableSim(v, tableWidth, tableHeight);
			bestSim = actSim;
			for (int y = 0; y < tableHeight - 1; y++)
			{
				// move where?
				for (int newY = y + 1; newY < tableHeight; newY++)
				{
					moveRow(v, y, newY, tableWidth);
					moveColumn(v, y, newY, tableWidth, tableHeight);
					newSim = getTableSim(v, tableWidth, tableHeight);
					if (newSim < bestSim)
					{
						bestSim = newSim;
						bestOld = y;
						bestNew = newY;
					}
					// undo the changes
					moveRow(v, newY, y, tableWidth);
					moveColumn(v, newY, y, tableWidth, tableHeight);
				}
			}
		
			// now perform the best action
			if (bestSim < actSim)
			{
				System.out.println("moved " + bestOld + " to " + bestNew);
				moveRow(v, bestOld, bestNew, tableWidth);
				moveColumn(v, bestOld, bestNew, tableWidth, tableHeight);
			}
			else
				break;
		} // loop
		
		// position update
		for (int y = 0; y < tableHeight; y++)
			for (int x = 0; x < tableWidth; x++)
			{
				VisualItem actItem = (VisualItem)v.get(y * tableWidth + x);
				actItem.set("xCor", new Integer(startX + x * itemWidth));
				actItem.set("yCor", new Integer(startY + y * itemHeight));
			}
	}
	
	/**
	 * Move a specific row under another one.
	 * oldIndex: the index of the row that shall be moved
	 * newIndex: the index of the row under which it will
	 * be moved
	 */
	public static void moveRow(Vector table, int oldIndex, int newIndex,
			int tableWidth)
	{
		Vector store = new Vector();
		// remove the row and store it in a vector
		for (int i = 0; i < tableWidth; i++)
			store.add(table.remove(oldIndex * tableWidth));
		// insert the row at the right place
		for (int i = 0; i < tableWidth; i++)
			table.insertElementAt(store.get(i), newIndex * tableWidth + i);
	}
	
	/**
	 * Move a specific column to the right of another one.
	 * oldIndex: the index of the column that shall be moved
	 * newIndex: the index of the column where it will
	 * be moved
	 */
	public static void moveColumn(Vector table, int oldIndex, int newIndex,
			int tableWidth, int tableHeight)
	{
		Vector store = new Vector();
		// remove the column and store it in a vector
		for (int i = 0; i < tableHeight; i++)
			store.add(table.remove(oldIndex + i * tableWidth - i));
		// insert the column at the right place
		for (int i = 0; i < tableHeight; i++)
			table.insertElementAt(store.get(i), newIndex + i * tableWidth);
	}
	
	/**
	 * Returns the mean difference between two rows.
	 */
	public static double getRowDiff(Vector table, int index1, 
			int index2, int tableWidth)
	{
		return Math.abs((getRowMean(table, index1, tableWidth)) -
				(getRowMean(table, index2, tableWidth)));
	}
	
	/**
	 * Returns the mean difference between two columns.
	 */
	public static double getColumnDiff(Vector table, int index1, 
			int index2, int tableWidth, int tableHeight)
	{
		return Math.abs((getColumnMean(table, index1, tableWidth, tableHeight)) -
				(getColumnMean(table, index2, tableWidth, tableHeight)));
	}
	
	/**
	 * Returns the mean value of a row.
	 */
	public static double getRowMean(Vector table, int index,
			int tableWidth)
	{
		double val = 0;
		
		for (int i = 0; i < tableWidth; i++)
		{
			VisualItem item = (VisualItem)table.get(index * tableWidth + i);
			val += ((Double)item.get("mean")).doubleValue();
		}
		val /= tableWidth;
		return val;
	}
	
	/**
	 * Returns the mean value of a column.
	 */
	public static double getColumnMean(Vector table, int index,
			int tableWidth, int tableHeight)
	{
		double val = 0;
		
		for (int i = 0; i < tableHeight; i++)
		{
			VisualItem item = (VisualItem)table.get(index + tableWidth * i);
			val += ((Double)item.get("mean")).doubleValue();
		}
		val /= tableHeight;
		return val;
	}
	
	public static double getTableSim(Vector table, int tableWidth,
			int tableHeight)
	{
		double val = 0;
		Vector items = new Vector();
		Vector pos = new Vector();
		for (int i = 0; i < table.size(); i++)
		{
			VisualItem item = (VisualItem)table.get(i);
			if (isImportant(item))
			{
				items.add(item);
				Point point = new Point(i % tableWidth, i / tableWidth);
				pos.add(point);
			}
		}
		for (int i = 0; i < items.size(); i++)
			for (int j = 0; j < items.size(); j++)
			{
				if (i != j)
				{
					VisualItem item1, item2;
					item1 = (VisualItem)items.get(i);
					item2 = (VisualItem)items.get(j);
					Point p1 = (Point)pos.get(i);
					Point p2 = (Point)pos.get(j);
					val += Math.sqrt(((p1.getX()-p2.getX())*(p1.getX()-p2.getX())) + 
						((p1.getY()-p2.getY())*(p1.getY()-p2.getY()))) 
						* getSim(item1, item2) * getMean(item1, item2);   
				}
			}
		return val;
	}

	public static double getSim(VisualItem item1, VisualItem item2)
	{
		double mean1 = ((Double)item1.get("scaledMean")).doubleValue();
		double mean2 = ((Double)item2.get("scaledMean")).doubleValue();
		double sim = 1.0d - Math.abs(mean1 - mean2);
		return sim;
	}
	
	public static double getMean(VisualItem item1, VisualItem item2)
	{
		double mean1 = ((Double)item1.get("scaledMean")).doubleValue();
		double mean2 = ((Double)item2.get("scaledMean")).doubleValue();
		double mean = (mean1 + mean2) / 2;
		return mean;
	}
	
	public static boolean isImportant(VisualItem item)
	{
		double threshold = 0.7;
		double mean = ((Double)item.get("scaledMean")).doubleValue();
		if (mean >= threshold)
			return true;
		else
			return false;
	}
}
