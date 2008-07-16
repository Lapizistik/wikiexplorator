package visualizer.ruby;

import visualizer.data.DataCube;


public class FlatCube extends DataCube {
    String[] xs, ys, zs;
    String title, xtitle, ytitle, ztitle;
    int xl,yl,zl;
    double[] data;
    public FlatCube(double[] data, String[] xs, String[] ys, String[] zs) {
	this.xs = xs;
	this.ys = ys;
	this.zs = zs;
	xl = xs.length;
	yl = ys.length;
	zl = zs.length;
	this.data = data;
	title = "FlatCube";
	xtitle = "X-Axis";
	ytitle = "Y-Axis";
	ztitle = "Z-Axis";
    }

    public FlatCube(double[] data, String[] xs, String[] ys, String[] zs,
		    String title,
		    String xtitle, String ytitle, String ztitle) {
	this(data, xs, ys, zs);
	this.title = title;
	this.xtitle = xtitle;
	this.ytitle = ytitle;
	this.ztitle = ztitle;

    }
    
    public int getXAxisCount() { return xl; }
    public int getYAxisCount() { return yl; }
    public int getZAxisCount() { return zl; }

    public String getXAxisNameAt(int i) { return xs[i]; }
    public String getYAxisNameAt(int i) { return ys[i]; }
    public String getZAxisNameAt(int i) { return zs[i]; }

    public String getTitle() { return title; }

    public String getXAxisTitle() { return xtitle; }
    public String getYAxisTitle() { return ytitle; }
    public String getZAxisTitle() { return ztitle; }

    public double getValueAt(int x, int y, int z) {
	return data[x*yl*zl+y*zl+z];
    }
	
}
