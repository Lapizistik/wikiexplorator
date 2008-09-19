/**
 * 
 */
package visualizer.display;

import java.util.Comparator;

import prefuse.visual.VisualItem;

/**
 * @author Rene Wegener
 *
 *Class used to compare two VisualItems by their means
 */
public class ItemComparator implements Comparator<VisualItem>
{
	public int compare(VisualItem arg0, VisualItem arg1) 
	{
		double[] values1 = ((double[])arg0.get("value"));
		double[] values2 = ((double[])arg1.get("value"));
		double val1 = 0, val2 = 0; 
		for (int i = 0; i < values1.length; i++)
		{
			val1 += values1[i];
			val2 += values2[i];
		}
		if (val1 < val2)
			return -1;
		else if (val1 > val2)
			return 1;
		else
			return 0;
	}

}
