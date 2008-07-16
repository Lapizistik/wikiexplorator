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
		/*for (int i = 0; i < tableHeight; i++)
			for (int j = 0; j < tableHeight - 1; j++)
			{
				double val1 = getRowMean(v, j, tableWidth);
				double val2 = getRowMean(v, j + 1, tableWidth);
				if (val1 > val2)
				{
					moveRow(v, j, j + 1, tableWidth);
					moveColumn(v, j, j + 1, tableWidth, tableHeight);
				}
			}*/
		
		// optimize the layout by moving rows/columns
		int maxLoops = 100;
		for (int loop = 0; loop < maxLoops; loop++)
		{
			boolean moveRow = true;
			double newDist, bestDist, actDist;
			int bestOld = 0, bestNew = 0;
			actDist = getTableDistortion(v, tableWidth, tableHeight);
			bestDist = actDist;
			for (int y = 0; y < tableHeight - 1; y++)
			{
				// move where?
				for (int newY = y + 1; newY < tableHeight; newY++)
				{
					moveRow(v, y, newY, tableWidth);
					moveColumn(v, y, newY, tableWidth, tableHeight);
					newDist = getTableDistortion(v, tableWidth, tableHeight);
					if (newDist < bestDist)
					{
						bestDist = newDist;
						bestOld = y;
						bestNew = newY;
					}
					// undo the changes
					moveRow(v, newY, y, tableWidth);
					moveColumn(v, newY, y, tableWidth, tableHeight);
				}
			}
		
			// now perform the best action
			if (bestDist < actDist)
			{
				moveRow(v, bestOld, bestNew, tableWidth);
				moveColumn(v, bestOld, bestNew, tableWidth, tableHeight);
			}
			else // no improvement was possible
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
	
	public static void createMDSLayout(Vector v, int itemWidth, int itemHeight, GlyphTable gt)
	{
		// create the matrices
		double[][] dissimilarities;
		double[][] distances;
		dissimilarities = getDissimMatrix(v);
		distances = getDistanceMatrix(dissimilarities);
		// setup start configuration
		Point[] points = new Point[v.size()];
		for (int i = 0; i < v.size(); i++)
				points[i] = new Point((int)(Math.random() * 500), 
						(int)(Math.random() * 500));
		// now optimize
		int maxLoops = 10000;
		double actStress, bestStress;
		int bestPoint = 0, dirX = 0, dirY = 0;
		for (int loop = 0; loop < maxLoops; loop++)
		{
			actStress = getFStress(distances, points);
			bestStress = actStress;
			/*for (int i = 0; i < points.length; i++)
			{
				// try to move point i in any direction 
				// and measure the new stress
				int step = 10;
				for (int xMove = -step; xMove <= step; xMove += step)
					for (int yMove = -step; yMove <= step; yMove += step)
						if (xMove != 0 || yMove != 0)
						{
							points[i].setLocation(points[i].getX() + xMove, 
									points[i].getY() + yMove);
							double testStress = getFStress(distances, points);
							if (testStress < bestStress)
							{
								bestStress = testStress;
								bestPoint = i;
								dirX = xMove;
								dirY = yMove;
							}
							// undo the changes
							points[i].setLocation(points[i].getX() - xMove, 
									points[i].getY() - yMove);
						}
			}*/
			// very simple simulated annealing test
			int next = (int)(Math.random() * points.length);
			int step = 10;
			dirX = (int)(Math.random() * 2 * step) - step;
			dirY = (int)(Math.random() * 2 * step) - step;
			points[next].setLocation(points[next].getX() + dirX, 
					points[next].getY() + dirY);
			double testStress = getFStress(distances, points);
			if (testStress > actStress)
			{
				points[next].setLocation(points[next].getX() - dirX, 
					points[next].getY() - dirY);
			}		
			// choose best action
			//if (bestStress < actStress)
			//{
			//	points[bestPoint].setLocation(points[bestPoint].getX() + dirX, 
			//			points[bestPoint].getY() + dirY);
			//	System.out.println("Durchlauf Nr. " + loop);
			//}
			//else // no improvement possible
			//	break;
		}
		
		// position update
		for (int i = 0; i < v.size(); i++)
		{
			VisualItem actItem = (VisualItem)v.get(i);
			actItem.set("xCor", new Integer((int)points[i].getX()));
			actItem.set("yCor", new Integer((int)points[i].getY()));
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
			val += ((Double)item.get("scaledMean")).doubleValue();
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
			val += ((Double)item.get("scaledMean")).doubleValue();
		}
		val /= tableHeight;
		return val;
	}
	
	public static double getTableDistortion(Vector table, int tableWidth,
			int tableHeight)
	{
		double distortion = 0;
		for (int i = 0; i < tableHeight - 1; i++)
		{
			distortion += getRowEuclidianDistance(table, i, i + 1,
					tableWidth);// * getRowMean(table, i, tableWidth)
					// * getRowMean(table, i + 1, tableWidth);
		}
		distortion /= (tableHeight - 1);
		return distortion;
	}

	public static double getMeanSim(VisualItem item1, VisualItem item2)
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
		double threshold = 0.75;
		double mean = ((Double)item.get("scaledMean")).doubleValue();
		if (mean >= threshold)
			return true;
		else
			return false;
	}
	
	public static double getRowEuclidianDistance(Vector table, int index1, int index2,
			int tableWidth)
	{
		Vector v1 = new Vector();
		Vector v2 = new Vector();
		for (int i = 0; i < tableWidth; i++)
		{
			VisualItem item1 = (VisualItem)table.get(index1 * tableWidth + i);
			VisualItem item2 = (VisualItem)table.get(index2 * tableWidth + i);
			double mean1 = ((Double)item1.get("scaledMean")).doubleValue();
			double mean2 = ((Double)item2.get("scaledMean")).doubleValue();
			v1.add(new Double(mean1 * mean1));
			v2.add(new Double(mean2 * mean2));
		}
		return getEuclidianDistance(v1, v2);
	}
	
	public static double getEuclidianDistance(VisualItem item1, VisualItem item2)
	{
		Vector v1 = new Vector();
		Vector v2 = new Vector();
		double[] values1 = ((double[])item1.get("scaledValue"));
		double[] values2 = ((double[])item2.get("scaledValue"));
		for (int i = 0; i < values1.length; i++)
		{
			v1.add(new Double(values1[i]));
			v2.add(new Double(values2[i]));	
		}
		return getEuclidianDistance(v1, v2);
	}
	
	public static double getEuclidianDistance(Vector v1, Vector v2)
	{
		double dist = 0;
		for (int i = 0; i < v1.size(); i++)
		{
			double val1 = ((Double)v1.get(i)).doubleValue();
			double val2 = ((Double)v2.get(i)).doubleValue();
			dist += (double)(Math.pow(val1 - val2, 2)); 
		}
		dist = Math.sqrt(dist);
		return dist;
	}
	
	public static double getEuclidianDistance(Point p1, Point p2)
	{
		return Math.sqrt((double)Math.pow(p1.getX() - p2.getX(), 2) 
				+ (double)Math.pow(p1.getY() - p2.getY(), 2));
	}
	
	public static double[][] getDissimMatrix(Vector authors)
	{
		// first fill the similarities with the 
		// euclidian distances which are dissimilarities 
		double[][] dis = new double[authors.size()][authors.size()];
		double highest = 0;
		for (int i = 0; i < dis.length; i++)
			for (int j = 0; j < dis.length; j++)
			{
				if (i == j)
					dis[i][j] = 0;
				else
				{
					VisualItem item1 = (VisualItem)authors.get(i);
					VisualItem item2 = (VisualItem)authors.get(j);
					dis[i][j] = getEuclidianDistance(item1, item2);
					if (dis[i][j] > highest)
						highest = dis[i][j];
				}
			}
		
		for (int i = 0; i < dis.length; i++)
			for (int j = 0; j < dis.length; j++)
			{
				if (i != j)
					dis[i][j] = dis[i][j] / highest;
			}
		
		return dis;
	}
	
	public static double getDistance(double dissim)
	{
		// simple function:
		int maxDist = 500;
		return (dissim * maxDist + 10);
	}
	
	public static double[][] getDistanceMatrix(double[][] dissim)
	{
		for (int i = 0; i < dissim.length; i++)
			for (int j = 0; j < dissim[0].length; j++)
				dissim[i][j] = getDistance(dissim[i][j]);
		return dissim;
	}
	
	public static double getFStress(double[][] dist, Point[] points)
	{
		double sum1 = 0, sum2 = 0;
		for (int i = 0; i < dist.length; i++)
			for (int j = i + 1; j < dist.length; j++)
			{
				sum1 += (double)Math.pow(dist[i][j] - 
						getEuclidianDistance(points[i], points[j]), 2);
				sum2 += (double)Math.pow(dist[i][j], 2);
			}
		return (Math.sqrt(sum1 / sum2));
	}
}
