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
		
		tableWidth = (int)Math.sqrt(v.size());//gt.getXAxisCount();
		tableHeight = (int)Math.sqrt(v.size());//gt.getYAxisCount();
		
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
	
	public static void orderedTable2D(ArrayList<VisualItem> v, int startX, int startY, 
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
			java.util.Collections.sort(cluster, new ClusterComparator());
			java.util.Collections.reverse(cluster);
			for (int i = 0; i < 10; i++)
			{
				ArrayList<double[]> vector = new ArrayList<double[]>();
				for (int j = 0; j < cluster.get(i).size(); j++)
					vector.add((double[])cluster.get(i).get(j).get("value"));
				// sort within the clusters
				createMDSMapping1D(cluster.get(i), vector);
				v.addAll(cluster.get(i));
				// add to ArrayList
				//for (int j = 0; j < cluster.get(i).size(); j++)
				//	v.add(cluster.get(i).get(j));
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
	
	public static ArrayList<ArrayList<VisualItem>> cluster(ArrayList<VisualItem> items, int k)
	{
		int dim = ((double[])items.get(0).get("value")).length;
		ArrayList<ArrayList<VisualItem>> cluster = new ArrayList<ArrayList<VisualItem>>();
		ArrayList<double[]> clusterCenter = new ArrayList<double[]>();//double[k][dim];
		int numberOfItems = items.size();
		int elements = (int)Math.ceil((double)numberOfItems / (double)k);
		// now create k clusters
		for (int i = 0; i < k; i++)
		{
			// new cluster with index i
			cluster.add(new ArrayList());
			clusterCenter.add(new double[dim]);
			// add elements from v
			if (items.size() < elements)
				elements = items.size();
			for (int add = 0; add < elements; add++)
			{
				// add new element to cluster
				cluster.get(i).add((VisualItem)items.remove((int)(Math.random() * items.size())));
				double[] val = (double[])cluster.get(i).get(cluster.get(i).size()-1).get("value");
				// update cluster's center
				for (int dims = 0; dims < dim; dims++)
					clusterCenter.get(i)[dims] += val[dims] / (double)elements;
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
				for (int j = 0; j < cluster.get(i).size(); j++)
				{
					// get an item
					VisualItem actItem = cluster.get(i).get(j);
					int bestCluster = i;
					double bestDist = distToCenter(actItem, clusterCenter.get(i));
					
					// compare it to all clusters
					for (int l = 0; l < k; l++)
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
				clusterCenter.add(getClusterCenter(cluster.get(i)));
			}
				//clusterCenter.add(new double[dim]);
				// get an item
				//for (int l = 0; l < cluster.get(i).size(); l++)
				//{
				//	VisualItem actItem = cluster.get(i).get(l);
				//	double[] val = (double[])actItem.get("value");
					// update center position
				//	for (int j = 0; j < dim; j++)
				//		clusterCenter.get(i)[j] += val[j] / cluster.get(i).size(); 
				//}	
			//}
		} // End of re-clustering loop
		
		return cluster;
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
	
	public static void createClusterLayout(ArrayList<VisualItem> items, int startX, int startY, 
			int itemWidth, int itemHeight, GlyphTable gt, boolean dim2D)
	{
		// Delete one of the symmetric halfs of a
		// relationships table
		if (gt.isCube())
		{
			int n = (int)Math.sqrt(items.size());
			int removed = 0;
			for (int i = 0; i < n; i++)
				for (int j = 0; j <= i; j++)
				{
					VisualItem actItem = items.get(n * i + j - removed);
					actItem.set("xCor", new Integer(-100));
					actItem.set("yCor", new Integer(-100));
					items.remove(n * i + j - removed);
					removed++;
				}
		}
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
		int numberOfItems = items.size();
		
		// I)
		// first part of this algorithm is a k means 
		// clustering.
		int k = 10;
		int dim = gt.getPixelCount();
		ArrayList<ArrayList<VisualItem>> cluster = cluster(items, k);//new ArrayList<ArrayList<VisualItem>>();
		ArrayList<double[]> clusterCenter = new ArrayList<double[]>();//double[k][dim];
		for (int i = 0; i < k; i++)
			clusterCenter.add(getClusterCenter(cluster.get(i)));
		/*int elements = (int)Math.ceil((double)numberOfItems / (double)k);
		// now create k clusters
		for (int i = 0; i < k; i++)
		{
			// new cluster with index i
			cluster.add(new ArrayList());
			clusterCenter.add(new double[dim]);
			// add elements from v
			if (items.size() < elements)
				elements = items.size();
			for (int add = 0; add < elements; add++)
			{
				// add new element to cluster
				cluster.get(i).add((VisualItem)items.remove((int)(Math.random() * items.size())));
				double[] val = (double[])cluster.get(i).get(cluster.get(i).size()-1).get("value");
				// update cluster's center
				for (int dims = 0; dims < dim; dims++)
					clusterCenter.get(i)[dims] += val[dims] / (double)elements;
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
				for (int j = 0; j < cluster.get(i).size(); j++)
				{
					// get an item
					VisualItem actItem = cluster.get(i).get(j);
					int bestCluster = i;
					double bestDist = distToCenter(actItem, clusterCenter.get(i));
					
					// compare it to all clusters
					for (int l = 0; l < k; l++)
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
			
			// update all cluster centers
			clusterCenter = new ArrayList<double[]>();//double[k][dim]; 
			for (int i = 0; i < k; i++)
			{
				clusterCenter.add(new double[dim]);
				// get an item
				for (int l = 0; l < cluster.get(i).size(); l++)
				{
					VisualItem actItem = cluster.get(i).get(l);
					double[] val = (double[])actItem.get("value");
					// update center position
					for (int j = 0; j < dim; j++)
						clusterCenter.get(i)[j] += val[j] / cluster.get(i).size(); 
				}	
			}
		} // End of re-clustering loop*/
		
		
		
		// II)
		// second part of this algorithm is multi-dimensional
		// scaling.
		// first the clusters themselves are arranged.
		/*ArrayList clus = new ArrayList();
		for (int i = 0; i < k; i++)
		{
			double[] d = new double[dim];
			for (int j = 0; j < dim; j++)
				d[j] = clusterCenter.get(i)[j];
			clus.add(d);
		}*/
		if (dim2D) // for mds layout with cluttering
			createMDSMapping2D(cluster, clusterCenter, itemWidth, itemHeight, 0, 0, 500);//, startX, startY, 300, gt);
		else // for jigsaw layout
		{
			createMDSMapping1D(cluster, clusterCenter);
		}
		
		// now arrange within the clusters.
		// first get the attribute vectors (values)
		// of all VisualItem
		for (int i = 0; i < k; i++)
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
			for (int i = 0; i < k; i++)
				for (int j = 0; j < cluster.get(i).size(); j++)
				{
					cluster.get(i).get(j).set("xCor", newList.get(counter).getX());
					cluster.get(i).get(j).set("yCor", newList.get(counter).getY());
					counter++;
				}
		}
	}
	
	public static double[] getClusterCenter(ArrayList<VisualItem> items)
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
	
	
	public static double distToCenter(VisualItem item, double[] center)
	{
		double dist = 0;
		double val[] = (double[])item.get("value");
		for (int i = 0; i < val.length; i++)
			dist += Math.pow(Math.abs(val[i] - center[i]), 2);
		dist = Math.sqrt(dist);
		return dist;
	}
	
	public static void createMDSMapping2D(ArrayList objects, ArrayList<double[]> vectors, 
			int itemWidth, int itemHeight, int startX,
			int startY, int range)//, GlyphTable gt)
	{
		// Optimization: all elements below threshold
		// are put on one place
		/*double threshold = 0;
		for (int i = 0; i < v.size(); i++)
			if (v.get(i) instanceof VisualItem)
			{
				VisualItem actItem = (VisualItem)v.get(i);
				double scaledMean = ((Double)(actItem.get("scaledMean"))).doubleValue();
				if (scaledMean <= threshold)
				{
					actItem.set("xCor", new Integer(-100));
					actItem.set("yCor", new Integer(-100));
					v.remove(i);
					i--;
				}
			}*/
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
		//int accuracy = 50;
		int maxLoops = 500;//v.size() * accuracy;
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
	
	public static void createMDSMapping1D(ArrayList objects, ArrayList<double[]> vectors)
	{
		// create the matrices
		double[][] dissimilarities;
		double[][] targetDistances;
		dissimilarities = getDissimMatrix(vectors);
		targetDistances = getDistanceMatrix(dissimilarities, 1);
		// setup start configuration:
		// every item is represented by a point.
		// these points are arranged in linear order.
		Point[] points = new Point[objects.size()];
		for (int i = 0; i < objects.size(); i++)
				points[i] = new Point((int)(i), 1);
		// now optimize
		//int accuracy = 30;
		int maxLoops = 500;//objects.size() * accuracy;
		double stressBefore, stressAfter,
		stressImprovement, bestImpro = 0;
		int bestI = 0, bestJ = 0;
		for (int loop = 0; loop < maxLoops; loop++)
		{
			// get two points by random
			int i = loop % objects.size();//(int)(Math.random() * points.length);
			int j;// = (int)(Math.random() * points.length);
			int start = i - 1;
			int stop = i + 1;
			if (start < 0)
				start = 0;
			if (stop >= points.length)
				stop = points.length - 1;
			for (j = start; j <= stop; j++)
			{
				if (i != j)
				{
			// switch points and measure the new stress
			stressBefore = getSingleStress(targetDistances, points, i) + 
						  getSingleStress(targetDistances, points, j);
			double store = points[i].getX();
			points[i].setLocation(points[j].getX(), 
									points[i].getY());
			points[j].setLocation(store, 
					points[j].getY());

			stressAfter = getSingleStress(targetDistances, points, i) +
						  getSingleStress(targetDistances, points, j);
			stressImprovement = stressBefore - stressAfter;
			if (stressImprovement > bestImpro) // no improvement
			{
				bestImpro = stressImprovement;
				bestI = i;
				bestJ = j;
			}
				// undo the changes
				store = points[i].getX();
				points[i].setLocation(points[j].getX(), 
									points[i].getY());
				points[j].setLocation(store, 
					points[j].getY());
				}
			//}
			// if there was an improvement, switch the objects 
			// represented by points i and j
			}
			//	else  
			//{
			if (bestImpro > 0)
			{
				Object obj1 = objects.get(bestI);
				Object obj2 = objects.get(bestJ);
				objects.set(bestI, obj2);
				objects.set(bestJ, obj1);
			}
			bestImpro = 0;
			//}
			//} // Ende for j
		}
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
		double[] arr1 = new double[v1.size()];
		double[] arr2 = new double[v2.size()];
		for (int i = 0; i < arr1.length; i++)
		{
			arr1[i] = ((Double)v1.get(i)).doubleValue();
			arr2[i] = ((Double)v2.get(i)).doubleValue();
		}
		
		return getEuclidianDistance(arr1, arr2);
	}
	
	public static double getEuclidianDistance(double[] v1, double[] v2)
	{
		double dist = 0;
		for (int i = 0; i < v1.length; i++)
			dist += (double)(Math.pow(v1[i] - v2[i], 2)); 
		dist = Math.sqrt(dist);
		return dist;
	}
	
	public static double getEuclidianDistance(Point p1, Point p2)
	{
		return Math.sqrt((double)Math.pow(p1.getX() - p2.getX(), 2) 
				+ (double)Math.pow(p1.getY() - p2.getY(), 2));
	}
	
	public static double[][] getDissimMatrix(ArrayList<double[]> vectors)
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
