package visualizer.ruby;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.MatchResult;

import visualizer.data.DataSet;

public class FileLoader 
{
    public static DataSet load(String filename) 
    {
    	String[] xs, ys, zs;
    	xs = new String[0];
    	ys = new String[0];
    	zs = new String[0];
    	double[] data = new double[0];
    	
    	try
	    {
	    	BufferedReader linereader = new BufferedReader(new FileReader(filename));
	    	String line;
	    	while((line = linereader.readLine()) != null) 
	    	{
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
	    			data = toDoubleArray(values);
	    	}
    	} catch (Exception e) {System.out.println("Fehler beim Laden der Daten: " + e.getMessage());}
    	
    	if (zs.length == 0)
    		return new FlatTable(data, xs, ys);
    	else
    		return new FlatCube(data, xs, ys, zs);
    }

    static String[] toStringArray(String s) 
    {
    	Scanner sc = new Scanner(s);
    	ArrayList <String> al = new ArrayList <String>();
    	while (sc.findInLine("\"(.*?)\"") != null) 
    	{
    		MatchResult m = sc.match();
    		al.add(m.group(1));
    	}
    	return al.toArray(new String[0]);
    }

    static double[] toDoubleArray(String s) 
    {
    	Scanner sc = new Scanner(s);
    	sc.useDelimiter(" *, *");
    	ArrayList <Double> al = new ArrayList <Double>();
    	while (sc.hasNextDouble())
    		al.add(sc.nextDouble());
    	Double[] arr = al.toArray(new Double[0]);
    	double[] d = new double[arr.length];
    	for (int i = 0; i < d.length; i++)
    		d[i] = arr[i].doubleValue();
    	return d;
    }

}
