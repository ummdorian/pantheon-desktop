/*
 * Pantheon Integration Program
 */
import javax.swing.*;
import java.io.*;
import java.util.Properties;
import java.awt.*;
import java.awt.event.*;  

public class Pantheon {
	
	protected JFrame frame;
	protected JPanel mainPanel;
	protected JPanel tokenTextFieldPanel;
	protected JLabel tokenTextFieldLabel;
	protected JTextField tokenTextField;
	protected JPanel upstreamPanel;
	protected JLabel upstreamLabel;
	protected JList upstreamList;
	
	protected InputStream input = null;
	protected Properties prop = new Properties();
	
	
	/**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public void createAndShowGUI(Pantheon pantheon) {
	
        // Create Window
        frame = new JFrame("Pantheon Utilities");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		frame.add(mainPanel);
		
		//----------------------------------------------------
		// Create Container, Label and Input for Access Token
		//----------------------------------------------------
		
		// Panel
		tokenTextFieldPanel = new JPanel();
		mainPanel.add(tokenTextFieldPanel);
		
		// Label
		tokenTextFieldLabel = new JLabel("Pantheon Access Token");
		tokenTextFieldPanel.add(tokenTextFieldLabel);
		
		// Text Field
		tokenTextField = new JTextField(20);
		tokenTextFieldPanel.add(tokenTextField);
		// Load text field value
		tokenTextField.setText(loadConfig("accessToken"));  
		
		// Save Token Button
		JButton tokenTextFieldSaveButton = new JButton("Save/Update Token");
		tokenTextFieldPanel.add(tokenTextFieldSaveButton);
		// Button Submit Function
		tokenTextFieldSaveButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				// Creating another instance of the pantheon class to use it's saveConfigValue
				// method, because it wouldn't let me use the one in this instance
				Pantheon pantheonTemp = new Pantheon();
				pantheonTemp.saveConfigValue("accessToken",tokenTextField.getText());
			}
		});
		
		// Authenticate Button
		JButton authenticateButton = new JButton("Authenticate");
		tokenTextFieldPanel.add(authenticateButton);
		// Button Submit Function
		authenticateButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					Runtime rt = Runtime.getRuntime();
					Process pr = rt.exec("cscript ./terminus/vendor/bin/ terminus auth:login --machine-token="+tokenTextField.getText());
					BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
					String line=null;
					while((line=input.readLine()) != null) {
						System.out.println(line);
					}
				}
				catch(IOException ex){
					ex.printStackTrace();
				}
			}
		});

		//------------------------------------------------
		// Create Container, Label and Input for Upstream
		//------------------------------------------------
		// Panel
		upstreamPanel = new JPanel();
		mainPanel.add(upstreamPanel);
		
		// Label
		upstreamLabel = new JLabel("Select an Upstream");
		upstreamPanel.add(upstreamLabel);
		
		// Upstream Select
		final DefaultListModel upstreams = new DefaultListModel();
		upstreams.addElement("Drupal 7");
		upstreams.addElement("Drupal 8");
		upstreamList = new JList(upstreams); //data has type Object[]
		upstreamList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		upstreamList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		upstreamList.setVisibleRowCount(-1);
		upstreamList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		upstreamPanel.add(upstreamList);
		
		// Upstream Refresh Button
		JButton upstreamRefreshButton = new JButton("Refresh Upstream Options");
		upstreamPanel.add(upstreamRefreshButton);
		// Button Submit Function
		upstreamRefreshButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					Runtime rt = Runtime.getRuntime();
					Process pr = rt.exec("terminus/vendor/bin/terminus upstream:list");
					BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
					String line=null;
					while((line=input.readLine()) != null) {
						System.out.println(line);
					}
				}
				catch(IOException ex){
					ex.printStackTrace();
				}
			}
		});
		
        //Display the window.
        frame.pack();
        frame.setVisible(true);
		
    }
	
	
	public String loadConfig(String key){
		try {
			input = new FileInputStream("config.properties");
			
			// load a properties file
			prop.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return prop.getProperty(key);
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
				// Create a new instance of this class so we can call non static functions
				Pantheon pantheon = new Pantheon();
				// Create and show GUI
                pantheon.createAndShowGUI(pantheon);
            }
        });
		
    }
}
