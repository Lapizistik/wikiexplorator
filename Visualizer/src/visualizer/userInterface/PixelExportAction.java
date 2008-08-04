/**
 * 
 */
package visualizer.userInterface;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import prefuse.Display;
import prefuse.util.display.ExportDisplayAction;
import prefuse.util.display.ScaleSelector;
import prefuse.util.io.IOLib;
import prefuse.util.io.SimpleFileFilter;

/**
 * @author rene
 *
 */
public class PixelExportAction extends AbstractAction
{

    private Display display;
    private JFileChooser chooser;
    //private ScaleSelector scaler;
    
    /**
     * Create a new ExportDisplayAction for the given Display.
     * @param display the Display to capture
     */
    public PixelExportAction(Display display)
    {
        this.display = display;
    }
    
    private void init() 
    {
        //scaler  = new ScaleSelector();
        chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setDialogTitle("Export Prefuse Display...");
        chooser.setAcceptAllFileFilterUsed(false);
        
        HashSet seen = new HashSet();
        String[] fmts = ImageIO.getWriterFormatNames();
        for ( int i=0; i<fmts.length; i++ ) 
        {
            String s = fmts[i].toLowerCase();
            if ( s.length() == 3 && !seen.contains(s) ) 
            {
                seen.add(s);
                chooser.setFileFilter(new SimpleFileFilter(s, 
                        s.toUpperCase()+" Image (*."+s+")"));
            }
        }
        seen.clear(); seen = null;
        //chooser.setAccessory(scaler);
    }
    
    /**
     * Shows the image export dialog and processes the results.
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) 
    {
        // lazy initialization
        if ( chooser == null )
            init();
        
        // open image save dialog
        File f = null;
        //scaler.setImage(display.getOffscreenBuffer());
        int returnVal = chooser.showSaveDialog(display);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
           f = chooser.getSelectedFile();
        } else {
            return;
        }
        String format = 
            ((SimpleFileFilter)chooser.getFileFilter()).getExtension();
        String ext = IOLib.getExtension(f);        
        if ( !format.equals(ext) ) {
            f = new File(f.toString()+"."+format);
        }
        
        double scale = 1.0;//scaler.getScale();
        
        // save image
        boolean success = false;
        try 
        {
            OutputStream out = new BufferedOutputStream(
                                new FileOutputStream(f));
            System.out.print("Saving image "+f.getName()+", "+
                             format+" format...");
            success = saveImage(out, format, scale);
            out.flush();
            out.close();
            System.out.println("\tDONE");
        } catch ( Exception e ) {success = false;}
        // show result dialog on failure
        if (!success) 
        {
            JOptionPane.showMessageDialog(display,
                    "Error Saving Image!",
                    "Image Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean saveImage(OutputStream output, String format, double scale)
    {
        try 
        {
            // get an image to draw into
            Dimension d = new Dimension((int)(scale * display.getWidth()),
                                        (int)(scale * display.getHeight()));
            BufferedImage img = new BufferedImage(d.width, d.height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = (Graphics2D)img.getGraphics();
            
            // set up the display, render, then revert to normal settings
            Point2D p = new Point2D.Double(0,0);
            display.zoom(p, scale); // also takes care of damage report
            display.paintDisplay(g, d);
            display.zoom(p, 1/scale); // also takes care of damage report
            
            // save the image and return
            ImageIO.write(img, format, output);
            return true;
        } catch ( Exception e ) {
            e.printStackTrace();
            return false;
        }
    }
}
