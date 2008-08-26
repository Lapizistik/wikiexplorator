/**
 * 
 */
package visualizer.display;

import java.util.ArrayList;
import java.util.Comparator;

import prefuse.visual.VisualItem;

/**
 * @author rene
 *
 */
public class ClusterComparator implements Comparator<ArrayList<VisualItem>>
{
	public int compare(ArrayList<VisualItem> arg0, ArrayList<VisualItem> arg1) 
	{
		double mean1 = 0, mean2 = 0;
		double[] values1 = new double[0];
		double[] values2 = new double[0];
		for (int i = 0; i < arg0.size(); i++)
		{
			values1 = ((double[])arg0.get(i).get("value"));
			for (int j = 0; j < values1.length; j++)
				mean1 += values1[j];
		}
		
		for (int i = 0; i < arg1.size(); i++)
		{
			values2 = ((double[])arg1.get(i).get("value"));
			for (int j = 0; j < values2.length; j++)
				mean2 += values2[j];
		}
		mean1 /= (arg0.size() * values1.length);
		mean2 /= (arg1.size() * values2.length);
		
		if (mean1 < mean2)
			return -1;
		else if (mean1 > mean2)
			return 1;
		else
			return 0;
	}

}
