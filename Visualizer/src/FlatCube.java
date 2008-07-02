public class FlatCube extends DataCube {
    String[] xs, ys, zs;
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
    }
    public int getXAxisCount() { return xl; }
    public int getYAxisCount() { return yl; }
    public int getZAxisCount() { return zl; }

    public String getXAxisNameAt(int i) { return xs[i]; }
    public String getYAxisNameAt(int i) { return ys[i]; }
    public String getZAxisNameAt(int i) { return zs[i]; }

    public double getValueAt(int x, int y, int z) {
	return data[x*yl*zl+y*zl+z];
    }
}
