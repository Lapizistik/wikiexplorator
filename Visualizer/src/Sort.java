
import java.util.Vector;

import prefuse.visual.VisualItem;

/**
 * Currently not in use. This class will be used to 
 * sort a set of items.
 * 
 * @author Rene Wegener
 *
 */
public class Sort 
{
	public static void sort(Vector v, String byValue)
	{
		// bubble sort
		for (int i = 0; i < v.size() - 1; i++)
			for (int j = 0; j < v.size() - 1; j++)
			{
				VisualItem v1 = ((VisualItem)v.get(j));
				VisualItem v2 = ((VisualItem)v.get(j + 1));
				if (v1.canGetDouble(byValue))
				{
					double f1 = ((Double)v1.get(byValue)).doubleValue();
					double f2 = ((Double)v2.get(byValue)).doubleValue();
					if (f1 > f2)
					{
						v.setElementAt(v2, j);
						v.setElementAt(v1, j+1);
					}
				}
				else if (v1.canGetString(byValue))
				{
					String f1, f2;
					f1 = ((String)v1.get(byValue));
					f2 = ((String)v2.get(byValue));
					if (f1.compareTo(f2) > 0)
					{
						v.setElementAt(v2, j);
						v.setElementAt(v1, j+1);
					}
				}	
		}
	}
}
