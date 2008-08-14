/**
 * 
 */
package visualizer.display;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
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
	public static void createOrderedTableLayout(ArrayList v, int startX, int startY, 
			int itemWidth, int itemHeight, GlyphTable gt)
	{
		if (gt.isCube())
			orderedTable3D(v, startX, startY, itemWidth, itemHeight, gt);
		else 
			orderedTable2D(v, startX, startY, itemWidth, itemHeight, gt);
	}
	
	public static void orderedTable3D(ArrayList v, int startX, int startY, 
			int itemWidth, int itemHeight, GlyphTable gt)
	{
		int tableWidth;
		int tableHeight;
		
		tableWidth = gt.getXAxisCount();
		tableHeight = gt.getYAxisCount();
		
		double[] arr = new double[tableHeight];
		for (int i = 0; i < tableHeight; i++)
			arr[i] = getRowMean(v, i, tableWidth);
			
		// sort the rows
		for (int i = 0; i < tableHeight; i++)
		{
			boolean switched = false;
			for (int j = 0; j < tableHeight - 1 - i; j++)
			{
				double val1, val2;
				val1 = arr[j];//getRowMean(v, j, tableWidth);
				val2 = arr[j+1];//getRowMean(v, j + 1, tableWidth);
				
				if (val1 < val2)
				{
					switched = true;
					double store = arr[j];
					arr[j] = arr[j+1];
					arr[j+1] = store;
					moveRow(v, j, j + 1, tableWidth);
					moveColumn(v, j, j + 1, tableWidth, tableHeight);
				}
			}
			if (!switched)
				break;
		}
		
		// position update
		for (int x = 0; x < tableWidth; x++)
			for (int y = 0; y < tableHeight; y++)
			{
				VisualItem actItem = (VisualItem)v.get(y * tableWidth + x);
				actItem.set("xCor", new Integer(startX + x * itemWidth));
				actItem.set("yCor", new Integer(startY + y * itemHeight));
			}
	}
	
	public static void orderedTable2D(ArrayList v, int startX, int startY, 
			int itemWidth, int itemHeight, GlyphTable gt)
	{
		int tableWidth;
		int tableHeight;
		
		tableWidth = (int)Math.sqrt(v.size());
		tableHeight = (int)Math.ceil(v.size() / tableWidth);
		
		// determine how much space is needed to show
		// the labels (authors' names)
		int fontHeight = gt.getGlyphHeight() - 1;
		int space = 0;
		for (int i = 0; i < v.size(); i++)
		{
			VisualItem actItem = (VisualItem)v.get(i);
			String desc = (String)actItem.get("y-desc");
			Font font =  new Font("Arial", Font.PLAIN, fontHeight);
			Graphics2D g = (Graphics2D)gt.getVisualization().getDisplay(0).getGraphics();
			int stringWidth = (int)(font.getStringBounds(desc, g.getFontRenderContext())).getWidth();
			if (stringWidth > space)
				space = stringWidth;
		}
		space = space + 10;
		itemWidth += space;
		// sort the authors
		java.util.Collections.sort(v, new ItemComparator());
		java.util.Collections.reverse(v);
		// set positions
		int next = 0;
		for (int x = 0; x < tableWidth; x++)
			for (int y = 0; y < tableHeight; y++)
			{
				if (next >= v.size())
					break;
				VisualItem actItem = (VisualItem)v.get(next);
				actItem.set("xCor", new Integer(startX + x * itemWidth));
				actItem.set("yCor", new Integer(startY + y * itemHeight));
				next++;
			}
	}
	
	public static void createJigsawLayout(ArrayList v, int startX, int startY, 
			int itemWidth, int itemHeight, GlyphTable gt)
	{
		createClusterLayout(v, startX, startY, itemWidth, itemHeight, gt, false);
	}
	
	public static void createMDSLayout(ArrayList v, int startX, int startY, 
			int itemWidth, int itemHeight, GlyphTable gt)
	{
		createClusterLayout(v, startX, startY, itemWidth, itemHeight, gt, true);
	}
	
	public static void createClusterLayout(ArrayList v, int startX, int startY, 
			int itemWidth, int itemHeight, GlyphTable gt, boolean dim2D)
	{
		int k = 10;
		VisualItem item = (VisualItem)v.get(0);
		int dim = ((double[])item.get("value")).length;
		ArrayList<VisualItem>[] cluster = new ArrayList[k];
		double[][] clusterCenter = new double[k][dim];
		int elements = v.size() / k;
		if (v.size() % k > 0)
			elements++;
		// first sort the items
		java.util.Collections.sort(v, new ItemComparator());
		// now create k clusters
		for (int i = 0; i < k; i++)
		{
			// new cluster with index i
			cluster[i] = new ArrayList();
			// add elements from v
			if (v.size() < elements)
				elements = v.size();
			for (int add = 0; add < elements; add++)
			{
				// add new element to cluster
				cluster[i].add((VisualItem)v.remove(0));
				double[] val = (double[])cluster[i].get(cluster[i].size()-1).get("value");
				// update cluster's center
				for (int dims = 0; dims < dim; dims++)
					clusterCenter[i][dims] += val[dims] / (double)elements;
			}
		}
		
		boolean changed = true;
		int runs = 0;
		int maxRuns = 200;
		while (changed && runs < maxRuns)
		{
			changed = false;
			runs++;
		
			// Assign items to best fitting cluster
			for (int i = 0; i < k; i++)
				for (int j = 0; j < cluster[i].size(); j++)
				{
					// get an item
					VisualItem actItem = cluster[i].get(j);
					int bestCluster = i;
					double bestDist = distToCenter(actItem, clusterCenter[i]);
					
					// compare it to all clusters
					for (int l = 0; l < k; l++)
					{
						double dist = distToCenter(actItem, clusterCenter[l]);
						if (dist < bestDist)
						{
							bestDist = dist;
							bestCluster = l;
						}
					}
					
					// put item into best fitting cluster
					if (bestCluster != i)
					{
						cluster[bestCluster].add(cluster[i].remove(j));
						changed = true;
					}
				}
			
			// update all cluster centers
			clusterCenter = new double[k][dim]; 
			for (int i = 0; i < k; i++)
			{
				// get an item
				for (int l = 0; l < cluster[i].size(); l++)
				{
					VisualItem actItem = cluster[i].get(l);
					double[] val = (double[])actItem.get("value");
					// update center position
					for (int j = 0; j < dim; j++)
						clusterCenter[i][j] += val[j] / cluster[i].size(); 
				}	
			}
		} // End of re-clustering loop
		
		// now arrange the clusters themselves via mds
		ArrayList clus = new ArrayList();
		for (int i = 0; i < k; i++)
		{
			double[] d = new double[dim];
			for (int j = 0; j < dim; j++)
				d[j] = clusterCenter[i][j];
			clus.add(d);
		}
		if (dim2D)
			createMDSMapping2D(clus, itemWidth, itemHeight, startX, startY, 300, gt);
		else
			createMDSMapping1D(clus, itemWidth, itemHeight, startX, startY, 250, gt);
		
		// now arrange within the clusters
		for (int i = 0; i < k; i++)
		{
			int stX, stY;
			stX = (int)((double[])clus.get(i))[0];
			stY = (int)((double[])clus.get(i))[1];
			if (cluster[i].size() > 0)
			{
				if (dim2D)
					createMDSMapping2D(cluster[i], itemWidth, itemHeight, stX, stY, 50, gt);
				else
					createMDSMapping1D(cluster[i], itemWidth, itemHeight, stX, stY, 1, gt);	
			}
		}
		
		// if Jigsaw layout, then use now z curve
		if (!dim2D)
		{
			ArrayList<Point> newList = new ArrayList();
			for (int i = 0; i < k; i++)
				for (int j = 0; j < cluster[i].size(); j++)
				{
					//int pointX = ((Integer)(cluster[i].get(j)).get("xCor")).intValue();
					//int pointY = ((Integer)(cluster[i].get(j)).get("yCor")).intValue();
					newList.add(new Point(0, 0));//pointX, pointY));
				}
			Layouts.createFlexibleZLayout(newList, startX, startY, itemWidth, itemHeight);
			int counter = 0;
			for (int i = 0; i < k; i++)
				for (int j = 0; j < cluster[i].size(); j++)
				{
					cluster[i].get(j).set("xCor", newList.get(counter).getX());
					cluster[i].get(j).set("yCor", newList.get(counter).getY());
					counter++;
				}
		}
	}
	
	public static double distToCenter(VisualItem item, double[] center)
	{
		double dist = 0;
		double val[] = (double[])item.get("value");
		for (int i = 0; i < val.length; i++)
			dist += Math.pow(Math.abs(val[i] - center[i]), 2);
		dist = Math.sqrt(dist);
		return dist;
	}
	
	public static void createMDSMapping2D(ArrayList v, int itemWidth, int itemHeight, int startX,
			int startY, int range, GlyphTable gt)
	{
		// Optimization: all elements below threshold
		// are put on one place
		double threshold = 0;
		for (int i = 0; i < v.size(); i++)
			if (v.get(i) instanceof VisualItem)
			{
				VisualItem actItem = (VisualItem)v.get(i);
				double scaledMean = ((Double)(actItem.get("scaledMean"))).doubleValue();
				if (scaledMean <= threshold)
				{
					actItem.set("xCor", new Integer(-1000));
					actItem.set("yCor", new Integer(-1000));
					v.remove(i);
					i--;
				}
			}
		
		// create the matrices
		double[][] dissimilarities;
		double[][] distances;
		dissimilarities = getDissimMatrix(v);
		distances = getDistanceMatrix(dissimilarities, range);
		// setup start configuration
		Point[] points = new Point[v.size()];
		for (int i = 0; i < v.size(); i++)
				points[i] = new Point((int)(startX + Math.random() * 50), 
						(int)(startY + Math.random() * 50));
		// now optimize
		int accuracy = 50;
		int maxLoops = v.size() * accuracy;
		double maxStressImprovement, stressBefor, stressAfter,
		stressImprovement;
		int bestPoint = 0, dirX = 0, dirY = 0;
		for (int loop = 0; loop < maxLoops; loop++)
		{
			maxStressImprovement = 0;
			int i = (int)(Math.random() * points.length);
				// try to move point i in any direction 
				// and measure the new stress
				int step = 10;
				for (int xMove = -step; xMove <= step; xMove += step)
					for (int yMove = -step; yMove <= step; yMove += step)
						if (xMove != 0 || yMove != 0)
						{
							stressBefor = getSingleStress(distances, points, i);
							points[i].setLocation(points[i].getX() + xMove, 
									points[i].getY() + yMove);
							stressAfter = getSingleStress(distances, points, i);
							stressImprovement = stressBefor - stressAfter;
							if (stressImprovement > maxStressImprovement)
							{
								maxStressImprovement = stressImprovement;
								bestPoint = i;
								dirX = xMove;
								dirY = yMove;
							}
							// undo the changes
							points[i].setLocation(points[i].getX() - xMove, 
									points[i].getY() - yMove);
						}
			// choose best action
			if (maxStressImprovement > 0)
			{
				points[bestPoint].setLocation(points[bestPoint].getX() + dirX, 
						points[bestPoint].getY() + dirY);
			}	
		}
		
		// position update
		if (v.size() > 0 && v.get(0) instanceof VisualItem)
			for (int i = 0; i < v.size(); i++)
			{
				VisualItem actItem = (VisualItem)v.get(i);
				actItem.set("xCor", new Integer((int)points[i].getX()));
				actItem.set("yCor", new Integer((int)points[i].getY()));
			}
		else if (v.size() > 0)
			for (int i = 0; i < v.size(); i++)
			{
				double[] actItem = (double[])v.get(i);
				actItem[0] = new Integer((int)points[i].getX());
				actItem[1] = new Integer((int)points[i].getY());
			}
	}
	
	public static void createMDSMapping1D(ArrayList v, int itemWidth, int itemHeight, int startX,
			int startY, int range, GlyphTable gt)
	{
		// Optimization: all elements below threshold
		// are put on one place
		double threshold = 0;
		for (int i = 0; i < v.size(); i++)
			if (v.get(i) instanceof VisualItem)
			{
				VisualItem actItem = (VisualItem)v.get(i);
				double scaledMean = ((Double)(actItem.get("scaledMean"))).doubleValue();
				if (scaledMean <= threshold)
				{
					actItem.set("xCor", new Integer(-1000));
					actItem.set("yCor", new Integer(-1000));
					v.remove(i);
					i--;
				}
			}
		
		// create the matrices
		double[][] dissimilarities;
		double[][] distances;
		dissimilarities = getDissimMatrix(v);
		distances = getDistanceMatrix(dissimilarities, range);
		// setup start configuration
		Point[] points = new Point[v.size()];
		for (int i = 0; i < v.size(); i++)
				points[i] = new Point((int)(startX + i * (itemWidth + 5)), 
						(int)(startY));
		// now optimize
		int accuracy = 0;
		int maxLoops = v.size() * accuracy;
		double stressBefor, stressAfter,
		stressImprovement;
		for (int loop = 0; loop < maxLoops; loop++)
		{
			int i = (int)(Math.random() * points.length);
			int j = (int)(Math.random() * points.length);
			// switch points and measure the new stress
			stressBefor = getSingleStress(distances, points, i) + 
						  getSingleStress(distances, points, j);
			double store = points[i].getX();
			points[i].setLocation(points[j].getX(), 
									points[i].getY());
			points[j].setLocation(store, 
					points[j].getY());

			stressAfter = getSingleStress(distances, points, i) +
						  getSingleStress(distances, points, j);
			stressImprovement = stressBefor - stressAfter;
			if (stressImprovement < 0)
			{
				// undo the changes
				store = points[i].getX();
				points[i].setLocation(points[j].getX(), 
									points[i].getY());
				points[j].setLocation(store, 
					points[j].getY());
			}
			else
			{
				v.add(i, v.remove(j));
				v.add(j, v.remove(i-1));
			}
		}
		
		// position update
		/*if (v.size() > 0 && v.get(0) instanceof VisualItem)
			for (int i = 0; i < v.size(); i++)
			{
				VisualItem actItem = (VisualItem)v.get(i);
				actItem.set("xCor", new Integer((int)points[i].getX()));
				actItem.set("yCor", new Integer((int)points[i].getY()));
			}
		else if (v.size() > 0)
			for (int i = 0; i < v.size(); i++)
			{
				double[] actItem = (double[])v.get(i);
				actItem[0] = new Integer((int)points[i].getX());
				actItem[1] = new Integer((int)points[i].getY());
			}*/
	}
	
	/**
	 * Move a specific row under another one.
	 * oldIndex: the index of the row that shall be moved
	 * newIndex: the index of the row under which it will
	 * be moved
	 */
	public static void moveRow(ArrayList table, int oldIndex, int newIndex,
			int tableWidth)
	{
		ArrayList store = new ArrayList();
		// remove the row and store it in a vector
		for (int i = 0; i < tableWidth; i++)
			store.add(table.remove(oldIndex * tableWidth));
		// insert the row at the right place
		for (int i = 0; i < tableWidth; i++)
			table.add/*table.insertElementAt*/(newIndex * tableWidth + i, store.get(i));
	}
	
	/**
	 * Move a specific column to the right of another one.
	 * oldIndex: the index of the column that shall be moved
	 * newIndex: the index of the column where it will
	 * be moved
	 */
	public static void moveColumn(ArrayList table, int oldIndex, int newIndex,
			int tableWidth, int tableHeight)
	{
		ArrayList store = new ArrayList();
		// remove the column and store it in a vector
		for (int i = 0; i < tableHeight; i++)
			store.add(table.remove(oldIndex + i * tableWidth - i));
		// insert the column at the right place
		for (int i = 0; i < tableHeight; i++)
			table.add(newIndex + i * tableWidth, store.get(i));
	}
	
	/**
	 * Returns the mean value of a row.
	 */
	public static double getRowMean(ArrayList table, int index,
			int tableWidth)
	{
		double val = 0;
		
		for (int i = 0; i < tableWidth; i++)
		{
			VisualItem item = (VisualItem)table.get(index * tableWidth + i);
			val += ((Double)item.get("mean")).doubleValue();
		}
		//val /= tableWidth;
		return val;
	}
		
	public static double getEuclidianDistance(VisualItem item1, VisualItem item2)
	{
		ArrayList v1 = new ArrayList();
		ArrayList v2 = new ArrayList();
		double[] values1 = ((double[])item1.get("scaledValue"));
		double[] values2 = ((double[])item2.get("scaledValue"));
		for (int i = 0; i < values1.length; i++)
		{
			v1.add(new Double(values1[i]));
			v2.add(new Double(values2[i]));	
		}
		return getEuclidianDistance(v1, v2);
	}
	
	public static double getEuclidianDistance(ArrayList v1, ArrayList v2)
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
	
	public static double[][] getDissimMatrix(ArrayList authors)
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
					Object o1 = authors.get(i);
					Object o2 = authors.get(j);
					if (o1 instanceof VisualItem)
					{
						VisualItem item1 = (VisualItem)authors.get(i);
						VisualItem item2 = (VisualItem)authors.get(j);
						dis[i][j] = getEuclidianDistance(item1, item2);
					}
					else if (o1 instanceof double[])
					{
						double[] item1 = (double[])authors.get(i);
						double[] item2 = (double[])authors.get(j);
						ArrayList list1 = new ArrayList();
						ArrayList list2 = new ArrayList();
						for (int l = 0; l < item1.length; l++)
						{
							list1.add(new Double(item1[l]));
							list2.add(new Double(item2[l]));
							dis[i][j] = getEuclidianDistance(list1, list2);
						}
					}
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
	
	public static double getDistance(double dissim, int range)
	{
		// simple function:
		return (dissim * range);
	}
	
	public static double[][] getDistanceMatrix(double[][] dissim, int range)
	{
		for (int i = 0; i < dissim.length; i++)
			for (int j = 0; j < dissim[0].length; j++)
				dissim[i][j] = getDistance(dissim[i][j], range);
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
	
	public static double getSingleStress(double[][] dist, Point[] points, int index)
	{
		double sum1 = 0, sum2 = 0;
		for (int i = 0; i < dist.length; i++)
		{
			sum1 += Math.pow(dist[index][i] - getEuclidianDistance(points[index], points[i]), 2);
			sum2 += (double)Math.pow(dist[index][i], 2);
		}
		return (sum1 / sum2);
	}
}
