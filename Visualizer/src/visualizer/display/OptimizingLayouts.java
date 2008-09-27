package visualizer.display;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import prefuse.visual.VisualItem;

/**
 * This class contains static layout algorithms similar
 * to the Layouts class. But OptimizingLayouts contains
 * methods that try to arrange objects using optimization 
 * algorithms.
 * 
 * @author Rene Wegener
 *
 */
public class OptimizingLayouts 
{
	/**
	 * create a table that puts items with high values
	 * to the top; if the data is 2D than a clustering is
	 * performed to arrange similar items together;
	 * this is not implemented for 3D data yet
	 * @param v an ArrayList containing all VisualItems to arrange;
	 * if the data structure is 3D then the items are supposed to be ordered
	 * row by row in the ArrayList
	 * @param startX x-coordinate at which to start the layout
	 * @param startY y-coordinate at which to start the layout
	 * @param itemWidth width of the VisualItems
	 * @param itemHeight height of the VisualItems
	 * @param gt GlyphTable that holds the VisualItems
	 */
	public static void createOrderedTableLayout(ArrayList<VisualItem> v, int startX, int startY, 
			int itemWidth, int itemHeight, GlyphTable gt)
	{
		if (gt.isCube())
			orderedTable3D(v, startX, startY, itemWidth, itemHeight, gt);
		else 
			orderedTable2D(v, startX, startY, itemWidth, itemHeight, gt);
	}
	
	// create the ordered table for 3D data structure
	protected static void orderedTable3D(ArrayList<VisualItem> v, int startX, int startY, 
			int itemWidth, int itemHeight, GlyphTable gt)
	{
		int tableWidth;
		int tableHeight;
		
		tableWidth = (int)(Math.sqrt(v.size()));
		tableHeight = tableWidth;//gt.getYAxisCount();
		
		// create several ArrayLists, each containing 
		// the items of one row
		ArrayList<ArrayList<VisualItem>> rows = new ArrayList<ArrayList<VisualItem>>();
		// store row indices in a HashMap
		HashMap<ArrayList<VisualItem>, Integer> rowIndices = new HashMap<ArrayList<VisualItem>, Integer>();
		for (int i = 0; i < tableHeight; i++)
		{
			rows.add(new ArrayList<VisualItem>());
			for (int j = 0; j < tableWidth; j++)
				rows.get(i).add(v.remove(0));
			// store row index
			rowIndices.put(rows.get(i), i);
		}
		// same for the columns
		ArrayList<ArrayList<VisualItem>> columns = new ArrayList<ArrayList<VisualItem>>();
		for (int i = 0; i < tableWidth; i++)
		{
			columns.add(new ArrayList<VisualItem>());
			for (int j = 0; j < tableHeight; j++)
				columns.get(i).add(rows.get(j).get(i));
		}
		
		// now sort the rows
		java.util.Collections.sort(rows, new ClusterComparator(rows));
		java.util.Collections.reverse(rows);
		// sort columns with the help of the 
		//former row indices
		Object[] colArr = new Object[tableWidth];
		for (int i = 0; i < tableHeight; i++)
		{
			// wich index did row i formely have?
			int actRow = rowIndices.get(rows.get(i));
			colArr[i] = columns.get(actRow);
		}
		
		// position update
		for (int y = 0; y < tableHeight; y++)
			for (int x = 0; x < tableWidth; x++)
			{
				VisualItem actItem = rows.get(y).get(x);
				actItem.set("yCor", new Integer(startY + y * itemHeight));
			}
		
		for (int x = 0; x < tableWidth; x++)
			for (int y = 0; y < tableHeight; y++)
			{
				VisualItem actItem = ((ArrayList<VisualItem>)colArr[x]).get(y);
				actItem.set("xCor", new Integer(startX + x * itemWidth));
			}
	}
	
	// create the ordered table for 2D data structure
	protected static void orderedTable2D(ArrayList<VisualItem> v, int startX, int startY, 
			int itemWidth, int itemHeight, GlyphTable gt)
	{
		int tableWidth;
		int tableHeight;
		
		tableWidth = (int)Math.sqrt(v.size());
		tableHeight = (int)Math.ceil((double)v.size() / (double)tableWidth);
		
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
		boolean optimized = true;
		if (!optimized) // sort by mean value
		{
			java.util.Collections.sort(v, new ItemComparator());
			java.util.Collections.reverse(v);
		}
		else // try to optimize
		{
			// get ten clusters
			ArrayList<ArrayList<VisualItem>> cluster = cluster(v, 10);
			v = new ArrayList<VisualItem>();
			// sort the clusters themselves by their means
			// (their centers)
			java.util.Collections.sort(cluster, new ClusterComparator(cluster));
			java.util.Collections.reverse(cluster);
			for (int i = 0; i < 10; i++)
			{
				ArrayList<double[]> vector = new ArrayList<double[]>();
				for (int j = 0; j < cluster.get(i).size(); j++)
					vector.add((double[])cluster.get(i).get(j).get("value"));
				// sort within the clusters
				java.util.Collections.sort(cluster.get(i), new ItemComparator());
				java.util.Collections.reverse(cluster.get(i));
				v.addAll(cluster.get(i));
			}
		}
		// set positions
		int next = 0;
		for (int x = 0; x < tableWidth; x++)
			for (int y = 0; y < tableHeight; y++)
			{
				if (next >= v.size())
					break;
				VisualItem actItem = v.get(next);
				actItem.set("xCor", new Integer(startX + x * itemWidth));
				actItem.set("yCor", new Integer(startY + y * itemHeight));
				next++;
			}
	}
	
	// perform a k-means clustering; this method returns
	// an ArrayList where each object of the list is one
	// cluster (an ArrayList itself) containing its VisualItems
	protected static ArrayList<ArrayList<VisualItem>> cluster(ArrayList<VisualItem> items, int k)
	{
		ArrayList<ArrayList<VisualItem>> cluster = new ArrayList<ArrayList<VisualItem>>();
		ArrayList<double[]> clusterCenter = new ArrayList<double[]>();//double[k][dim];
		int numberOfItems = items.size();
		// now create k clusters
		for (int i = 0; i < k; i++)
		{
			// new cluster with index i
			cluster.add(new ArrayList());	
		}
		
		// assign the items to the clusters
		while(true)
		{
			for (int i = 0; i < k; i++)
			{
				if (items.size() == 0)
					break;
				
				// add new element to cluster
				cluster.get(i).add((VisualItem)items.remove((int)(Math.random() * items.size())));
			}
			if (items.size() == 0)
				break;
		}
		// update the centers
		for (int i = 0; i < k; i++)
			if (cluster.get(i).size() > 0)
				clusterCenter.add(getClusterCenter(cluster.get(i)));
		
		boolean changed = true;
		int runs = 0;
		int maxRuns = 200;
		while (changed && runs < maxRuns)
		{
			changed = false;
			runs++;
		
			// Assign items to best fitting cluster
			for (int i = 0; i < k; i++)
				for (int j = 0; j < cluster.get(i).size(); j++)
				{
					// get an item
					VisualItem actItem = cluster.get(i).get(j);
					int bestCluster = i;
					double bestDist = distToCenter(actItem, clusterCenter.get(i));
					
					// compare it to all clusters
					for (int l = 0; l < clusterCenter.size(); l++)
					{
						double dist = distToCenter(actItem, clusterCenter.get(l));
						if (dist < bestDist)
						{
							bestDist = dist;
							bestCluster = l;
						}
					}
					
					// put item into best fitting cluster
					if (bestCluster != i)
					{
						cluster.get(bestCluster).add(cluster.get(i).remove(j));
						changed = true;
					}
				}
			
			// update all clusters' centers
			clusterCenter = new ArrayList<double[]>();//double[k][dim]; 
			for (int i = 0; i < k; i++)
			{
				if (cluster.get(i).size() > 0)
					clusterCenter.add(getClusterCenter(cluster.get(i)));
			}
		} // End of re-clustering loop
		
		return cluster;
	}
	
	/**
	 * create an layout that puts all VisualItems on a
	 * grid; similar items are tried to be put close together;
	 * @param v an ArrayList containing all VisualItems to arrange
	 * @param startX x-coordinate at which to start the layout
	 * @param startY y-coordinate at which to start the layout
	 * @param itemWidth width of the VisualItems
	 * @param itemHeight height of the VisualItems
	 * @param gt GlyphTable that holds the VisualItems
	 */
	public static void createJigsawLayout(ArrayList<VisualItem> v, int startX, int startY, 
			int itemWidth, int itemHeight, GlyphTable gt)
	{
		createClusterLayout(v, startX, startY, itemWidth, itemHeight, gt, false);
	}
	
	/**
	 * create an layout that places all VisualItems on the screen,
	 * trying to keep similar items close together
	 * @param v an ArrayList containing all VisualItems to arrange
	 * @param startX x-coordinate at which to start the layout
	 * @param startY y-coordinate at which to start the layout
	 * @param itemWidth width of the VisualItems
	 * @param itemHeight height of the VisualItems
	 * @param gt GlyphTable that holds the VisualItems
	 */
	public static void createMDSLayout(ArrayList v, int startX, int startY, 
			int itemWidth, int itemHeight, GlyphTable gt)
	{
		createClusterLayout(v, startX, startY, itemWidth, itemHeight, gt, true);
	}
	
	// create the mds (parameter dim2D = true) or jigsaw 
	// (parameter dim2D = false) layout
	protected static void createClusterLayout(ArrayList<VisualItem> items, int startX, int startY, 
			int itemWidth, int itemHeight, GlyphTable gt, boolean dim2D)
	{
		// Optimization: all elements below threshold
		// are put on one place
		double threshold = 0;
		for (int i = 0; i < items.size(); i++)
		{
			VisualItem actItem = items.get(i);
			double scaledMean = ((Double)(actItem.get("scaledMean"))).doubleValue();
			if (scaledMean <= threshold)
			{
				actItem.set("xCor", new Integer(-100));
				actItem.set("yCor", new Integer(-100));
				items.remove(i);
				i--;
			}
		}
		// I)
		// first part of this algorithm is a k means 
		// clustering.
		// Usually we use 10 diffferent clusters but if
		// there are only a few items, then we don't have to
		// worry about performance and can use just one cluster.
		int k = 10;
		if (items.size() < 20)
			k = 1;
		int dim = gt.getPixelCount();
		ArrayList<ArrayList<VisualItem>> cluster = cluster(items, k);//new ArrayList<ArrayList<VisualItem>>();
		ArrayList<double[]> clusterCenter = new ArrayList<double[]>();//double[k][dim];
		for (int i = 0; i < cluster.size(); i++)
			if (cluster.get(i).size() > 0)
				clusterCenter.add(getClusterCenter(cluster.get(i)));
		
		// II)
		// second part of this algorithm is multi-dimensional
		// scaling.
		// first the clusters themselves are arranged.
		if (dim2D) // for mds layout with cluttering
			createMDSMapping2D(cluster, clusterCenter, itemWidth, itemHeight, 0, 0, 500);//, startX, startY, 300, gt);
		else // for jigsaw layout
		{
			createMDSMapping1D(cluster, clusterCenter);
		}
		
		// now arrange within the clusters.
		// first get the attribute vectors (values)
		// of all VisualItem
		for (int i = 0; i < cluster.size(); i++)
		{
			ArrayList<double[]> vectors = new ArrayList<double[]>();//[v.size()][gt.getPixelCount()];
			for (int n = 0; n < cluster.get(i).size(); n++)
			{
				vectors.add(new double[dim]);
				VisualItem actItem = cluster.get(i).get(n);
				double[] values = (double[])actItem.get("value");
				for (int j = 0; j < values.length; j++)
					vectors.get(n)[j] = values[j];
			}
			
			if (cluster.get(i).size() > 0)
			{
				if (dim2D)
				{
					int stX, stY;
					stX = (int)((double[])clusterCenter.get(i))[0];
					stY = (int)((double[])clusterCenter.get(i))[1];
					createMDSMapping2D(cluster.get(i), vectors, itemWidth, itemHeight, stX, stY, 100);//, 50, gt);	
				}
				else
				{
					createMDSMapping1D(cluster.get(i), vectors);
				}
			}
		}
		
		// if Jigsaw layout, then now use z-curve
		if (!dim2D)
		{
			// create z-curve
			ArrayList<Point> newList = new ArrayList();
			for (int i = 0; i < k; i++)
				for (int j = 0; j < cluster.get(i).size(); j++)
					newList.add(new Point(0, 0));//pointX, pointY));
			Layouts.createFlexibleZLayout(newList, startX, startY, itemWidth, itemHeight);
			
			// update the visual items' positions
			int counter = 0;
			for (int i = 0; i < cluster.size(); i++)
				for (int j = 0; j < cluster.get(i).size(); j++)
				{
					cluster.get(i).get(j).set("xCor", newList.get(counter).getX());
					cluster.get(i).get(j).set("yCor", newList.get(counter).getY());
					counter++;
				}
		}
	}
	
	// return the centers of several clusters
	protected static double[] getClusterCenter(ArrayList<VisualItem> items)
	{
		int dim = ((double[])items.get(0).get("value")).length;
		double arr[] = new double[dim];
		for (int i = 0; i < dim; i++)
		{
			for (int j = 0; j < items.size(); j++)
			{
				VisualItem actItem = items.get(j);
				arr[i] += ((double[])actItem.get("value"))[i];
			}
			arr[i] = arr[i] / items.size();
		}
		
		return arr;
	}
	
	// return the distance of one VisualItem to a cluster
	// of VisualItems
	protected static double distToCenter(VisualItem item, double[] center)
	{
		double dist = 0;
		double val[] = (double[])item.get("value");
		for (int i = 0; i < val.length; i++)
			dist += Math.pow(Math.abs(val[i] - center[i]), 2);
		dist = Math.sqrt(dist);
		return dist;
	}
	
	// create the normal MDS layout with cluttering
	protected static void createMDSMapping2D(ArrayList objects, ArrayList<double[]> vectors, 
			int itemWidth, int itemHeight, int startX,
			int startY, int range)//, GlyphTable gt)
	{
		// create the matrices
		double[][] dissimilarities;
		double[][] distances;
		dissimilarities = getDissimMatrix(vectors);
		distances = getDistanceMatrix(dissimilarities, range);
		// setup start configuration
		Point[] points = new Point[objects.size()];
		for (int i = 0; i < objects.size(); i++)
				points[i] = new Point(startX + (int)(Math.random() * range) - range / 2, 
						startY + (int)(Math.random() * range) - range / 2);
		// now optimize
		int maxLoops = 500;
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
							stressBefor = getStress(distances, points, i);
							points[i].setLocation(points[i].getX() + xMove, 
									points[i].getY() + yMove);
							stressAfter = getStress(distances, points, i);
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
		if (objects.size() > 0 && objects.get(0) instanceof VisualItem)
			for (int i = 0; i < objects.size(); i++)
			{
				VisualItem actItem = (VisualItem)objects.get(i);
				actItem.set("xCor", new Integer((int)points[i].getX() - itemWidth/2));
				actItem.set("yCor", new Integer((int)points[i].getY() - itemHeight/2));
			}
		else if (vectors.size() > 0)
			for (int i = 0; i < vectors.size(); i++)
			{
				double[] actItem = (double[])vectors.get(i);
				actItem[0] = new Integer((int)points[i].getX());
				actItem[1] = new Integer((int)points[i].getY());
			}
	}
	
	// create the Jigsaw layout but without cluttering (also 
	// based on MDS, but the items are put into one-
	// dimensional order)
	protected static void createMDSMapping1D(ArrayList objects, ArrayList<double[]> vectors)
	{
		// create the matrices
		double[][] dissimilarities;
		dissimilarities = getDissimMatrix(vectors);
		// setup start configuration:
		// every item is represented by a point.
		// these points are arranged in linear order.
		Point[] points = new Point[objects.size()];
		for (int i = 0; i < objects.size(); i++)
				points[i] = new Point((int)(i), 1);
		// now optimize
		int maxLoops = 50;
		// initialize an array that stores where in the
		// dissimilarity matrix to find an object
		// (for the indices wouldn't fit after swapping
		// objects)
		int[] matrix = new int[objects.size()];
		for (int i = 0; i < objects.size(); i++)
			matrix[i] = i;
		
		for (int loop = 0; loop < maxLoops; loop++)
		{
			for (int i = 0; i < objects.size(); i++)
				for (int j = 0; j < objects.size(); j++)
				{
					if (i != j)
					{
						double dissimBefor, dissimAfter;
						
						// how good is item i placed?
						if (i > 0 && i < objects.size() - 1)
							dissimBefor = dissimilarities[matrix[i]][matrix[i-1]] +
										dissimilarities[matrix[i]][matrix[i+1]];
						else if (i > 0)
							dissimBefor = 2 * dissimilarities[matrix[i]][matrix[i-1]];
						else		
							dissimBefor = 2 * dissimilarities[matrix[i]][matrix[i+1]];
						
						// how good is item j placed?
						if (j > 0 && j < objects.size() - 1)
							dissimBefor += dissimilarities[matrix[j]][matrix[j-1]] +
										dissimilarities[matrix[j]][matrix[j+1]];
						else if (j > 0)
							dissimBefor += 2 * dissimilarities[matrix[j]][matrix[j-1]];
						else		
							dissimBefor += 2 * dissimilarities[matrix[j]][matrix[j+1]];
						
						// how good would they be placed
						// after they've been swapped?
						if (i > 0 && i < objects.size() - 1)
							dissimAfter = dissimilarities[matrix[j]][matrix[i-1]] +
										dissimilarities[matrix[j]][matrix[i+1]];
						else if (i > 0)
							dissimAfter = 2 * dissimilarities[matrix[j]][matrix[i-1]];
						else		
							dissimAfter = 2 * dissimilarities[matrix[j]][matrix[i+1]];
						
						if (j > 0 && j < objects.size() - 1)
							dissimAfter += dissimilarities[matrix[i]][matrix[j-1]] +
										dissimilarities[matrix[i]][matrix[j+1]];
						else if (j > 0)
							dissimAfter += 2 * dissimilarities[matrix[i]][matrix[j-1]];
						else		
							dissimAfter += 2 * dissimilarities[matrix[i]][matrix[j+1]];
						
						if (dissimAfter < dissimBefor)
						{
							java.util.Collections.swap(objects, i, j);
							// store the items' new positions
							int store = matrix[i];
							matrix[i] = matrix[j];
							matrix[j] = store;
						}
					}
				}
		}
	}
	
	
	// Returns the mean value of a row of VisualItems; 
	// ArrayList table contains all items, index tells which
	// row to choose
	protected static double getRowMean(ArrayList table, int index,
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
	
	// get Euclidian distance between two glyphs
	protected static double getEuclidianDistance(VisualItem item1, VisualItem item2)
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
	
	// get Euclidian distance between two vectors
	// given as ArrayLists of type Double
	protected static double getEuclidianDistance(ArrayList<Double> v1, ArrayList<Double> v2)
	{
		double[] arr1 = new double[v1.size()];
		double[] arr2 = new double[v2.size()];
		for (int i = 0; i < arr1.length; i++)
		{
			arr1[i] = ((Double)v1.get(i)).doubleValue();
			arr2[i] = ((Double)v2.get(i)).doubleValue();
		}
		
		return getEuclidianDistance(arr1, arr2);
	}
	
	// get Euclidian distance between two vectors
	// given as double arrays
	protected static double getEuclidianDistance(double[] v1, double[] v2)
	{
		double dist = 0;
		for (int i = 0; i < v1.length; i++)
			dist += (double)(Math.pow(v1[i] - v2[i], 2)); 
		dist = Math.sqrt(dist);
		return dist;
	}
	
	// get Euclidian distance between two points in
	// two-dimensional space
	protected static double getEuclidianDistance(Point p1, Point p2)
	{
		return Math.sqrt((double)Math.pow(p1.getX() - p2.getX(), 2) 
				+ (double)Math.pow(p1.getY() - p2.getY(), 2));
	}
	
	// get the matrix of dissimilarities between a set of
	// vectors, each vector given as a double array
	protected static double[][] getDissimMatrix(ArrayList<double[]> vectors)
	{
		// first fill the similarities with the 
		// euclidian distances which are dissimilarities 
		double[][] dis = new double[vectors.size()][vectors.size()];
		double highest = 0;
		for (int i = 0; i < dis.length; i++)
			for (int j = 0; j < dis.length; j++)
			{
				if (i == j)
					dis[i][j] = 0;
				else
				{
					double[] o1 = vectors.get(i);
					double[] o2 = vectors.get(j);
					dis[i][j] = getEuclidianDistance(o1, o2);
					
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
	
	// get the on-screen-distance in pixels
	// resulting from a specific dissimilarity;
	// the parameter range determines how far apart
	// the objects will be placed on screen
	protected static double getDistance(double dissim, int range)
	{
		// simple function:
		return (dissim * range);
	}
	
	// get the matrix of distances that fit to the
	// given dissimilarities
	protected static double[][] getDistanceMatrix(double[][] dissim, int range)
	{
		for (int i = 0; i < dissim.length; i++)
			for (int j = 0; j < dissim[0].length; j++)
				dissim[i][j] = getDistance(dissim[i][j], range);
		return dissim;
	}
	
	/*public static double getFStress(double[][] dist, Point[] points)
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
	}*/
	
	// get stress; this value reflects how good a specific
	// point is actually placed on the screen
	public static double getStress(double[][] dist, Point[] points, int index)
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
