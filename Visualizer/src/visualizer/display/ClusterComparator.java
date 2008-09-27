package visualizer.display;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import prefuse.visual.VisualItem;

/**
 *
 * This class is used to decide which of two clusters 
 * of glyphs has got the higher mean value. 
 * So clusters like the glyph rows of a sorted  
 * table may be compared.
 * 
 * @author Rene Wegener
 */
public class ClusterComparator implements Comparator<ArrayList<VisualItem>>
{
	// this TreeMap stores a cluster and its mean value (center)
	protected HashMap<ArrayList<VisualItem>, Double> mean;
	
	/**
	 * The constructor needs an ArrayList of all clusters that 
	 * might be compared. These clusters themselves are
	 * ArrayLists of VisualItems. 
	 * By delivering the clusters to the constructor as
	 * parameters, the calculation of the their mean
	 * values can be done one time instead of every time
	 * a comparison is needed. 
	 */
	public ClusterComparator(ArrayList<ArrayList<VisualItem>> clusters)
	{
		mean = new HashMap<ArrayList<VisualItem>, Double>();
		for (int i = 0; i < clusters.size(); i++)
		{
			double val = 0;
			for (int j = 0; j < clusters.get(i).size(); j++)
				 val += (Double)clusters.get(i).get(j).get("mean");
			val /= clusters.get(i).size();
			mean.put(clusters.get(i), val);
		}
	}
	
	/**
	 * Compare two clusters of VisualItems by their
	 * mean values.
	 */
	public int compare(ArrayList<VisualItem> arg0, ArrayList<VisualItem> arg1) 
	{
		double mean1 = mean.get(arg0);
		double mean2 = mean.get(arg1);
		
		if (mean1 < mean2)
			return -1;
		else if (mean1 > mean2)
			return 1;
		else
			return 0;
	}
}
