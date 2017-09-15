/*
 * Pantheon Integration Program
 */
import javax.swing.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;       

public class Pantheon {
	
	protected JFrame frame;
	
	protected JPanel tokenTextFieldPanel;
	protected JLabel label;
	protected JTextField tokenTextField;
	
	
	/**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public void createAndShowGUI() {
	
        // Create Window
        frame = new JFrame("Pantheon Utilities");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//----------------------------------------------------
		// Create Container, Label and Input for Access Token
		//----------------------------------------------------
		// Panel
		tokenTextFieldPanel = new JPanel();
		frame.getContentPane().add(tokenTextFieldPanel);
		// Label
		label = new JLabel("Pantheon Access Token");
		tokenTextFieldPanel.add(label);
		
		// Text Field
		tokenTextField = new JTextField(20);
		tokenTextFieldPanel.add(tokenTextField);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
		
    }
	
	/**
	* See http://www.mkyong.com/java/java-properties-file-examples/
	**/
	public void saveConfigValue(String key, String value){
		
		Properties prop = new Properties();
		OutputStream output = null;
		
		// Try saving config
		try {
			output = new FileOutputStream("config.properties");
			prop.setProperty(key, value);
			prop.store(output, null);
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				Pantheon pantheon = new Pantheon();
                pantheon.createAndShowGUI();
            }
        });
    }
}
