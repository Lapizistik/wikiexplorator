package visualizer.ruby;

public static class FileLoader {
    public static DataSet load(String filename) {
	String[] xs, ys, zs;
	double[] data;
	// Fehlerbehandlung fehlt!
	BufferedReader linereader =
	    new BufferedReader(new FileReader(filename));
	while((String line = linereader.readLine()) != null) {
	    Scanner sc = new Scanner(line);
	    sc.findInLine("^(.*?) *= *(.*)$");
	    MatchResult m = sc.match();
	    String name = m.group(1);
	    String values = m.group(2);
	    if (name.equals("xn") || name.equals("xs"))
		xs = toStringArray(values);
	    else if (name.equals("yn") || name.equals("ys"))
		ys = toStringArray(values);
	    else if (name.equals("zn") || name.equals("zs"))
		zs = toStringArray(values);
	    else if (name.equals("data"))
		data = toDoubleValues(values);
	}
	if (zs == null)
	    return null; // new FlatTable(data, xs, ys);
	else
	    return new FlatCube(data, xs, ys, zs);
    }

    static String[] toStringArray(String s) {
	Scanner sc = new Scanner(s);
	ArrayList<String> al = new ArrayList<String>();
	while (sc.findInLine("\"(.*?)\"") != null) {
	    MatchResult m = sc.match();
	    al.add(m.group(1));
	}
	return al.toArray(new String[0]);
    }

    static double[] toDoubleArray(String s) {
	Scanner sc = new Scanner(s);
	sc.useDelimiter(" *, *");
	ArrayList<double> al = new ArrayList<double>();
	while (sc.hasNextDouble())
	    al.add(sc.nextDouble());
	return al.toArray(new double[0]);
    }

}
