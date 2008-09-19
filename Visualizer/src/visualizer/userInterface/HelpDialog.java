package visualizer.userInterface;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Rene Wegener
 *
 * The program's main help function
 */
public class HelpDialog extends JDialog implements HyperlinkListener
{
	// testing
	public static void main(String[] args) 
	  {
		 HelpDialog brow = new HelpDialog("help/overview.html");
	  }

	  private JEditorPane overviewPane, contentPane;
	  private URL pageURL;
	  private String name;
	  
	  /**
	   * Open new HelpDialog
	   * @param n name of the dialog's frame
	   */
	  public HelpDialog(String n) 
	  {
	    super();
	    addWindowListener(new WindowAdapter() 
	    	{
	        	public void windowClosing(WindowEvent arg0) 
	        	{
	        		dispose();
	        	}
	        });
	    name = n;
	    pageURL = getClass().getClassLoader().getResource(name);
	    
	    try
	    {
	    	overviewPane = new JEditorPane(pageURL);
	    	contentPane = new JEditorPane();
	    } catch(IOException ioe) {}
	    overviewPane.setEditable(false);
	    contentPane.setEditable(false);
	    overviewPane.addHyperlinkListener(this);
	    JScrollPane scrollPane1 = new JScrollPane(overviewPane);
	    JScrollPane scrollPane2 = new JScrollPane(contentPane);
	    scrollPane1.setPreferredSize(new Dimension(200, 800));
	    scrollPane2.setPreferredSize(new Dimension(800, 800));
	    Container cp = getContentPane();
		FormLayout layout = new FormLayout("left:default, 10px, left:default:grow",
		"top:default:grow");
		cp.setLayout(layout);
		CellConstraints cc = new CellConstraints();
		
	    cp.add(scrollPane1, cc.xy(1, 1));
	    cp.add(scrollPane2, cc.xy(3, 1));
	    
	    int width = 800;
	    int height = 600;
	    setBounds(width/8, height/8, width, height);
	    setVisible(true);
	  }

	  public void hyperlinkUpdate(HyperlinkEvent event) 
	  {
		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) 
	    {
	      try 
	      {
	    	  contentPane.setPage(event.getURL());
	      } catch(IOException ioe) {}
	    }
	  }
}
