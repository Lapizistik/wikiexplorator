/**
 * 
 */
package visualizer.ruby;

import visualizer.data.DataTable;

/**
 * @author rene
 *
 */
public class FlatTable extends DataTable
{
	String[] xs, ys;
	int xl,yl;
	double[] data;
	    
	public FlatTable(double[] data, String[] xs, String[] ys) 
	{
		this.xs = xs;
		this.ys = ys;
		xl = xs.length;
		yl = ys.length;
		this.data = data;
	}
	    
	public int getXAxisCount() { return xl; }
	public int getYAxisCount() { return yl; }
	
	public String getXAxisNameAt(int i) { return xs[i]; }
	public String getYAxisNameAt(int i) { return ys[i]; }
	
	public double getValueAt(int x, int y) 
	{
	   	return data[y * xl + x];
	}
}
