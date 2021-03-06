/*
 * Pantheon Integration Program
 */
import javax.swing.*;
import java.io.*;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
// https://github.com/stleary/JSON-java
// https://stackoverflow.com/a/16586100
import org.json.*;

public class Pantheon {
	
	protected JFrame frame;
	protected JPanel mainPanel;
	protected JPanel tokenTextFieldPanel;
	protected JLabel tokenTextFieldLabel;
	protected JTextField tokenTextField;
	
	protected InputStream input = null;
	
	protected Properties programSettings = new Properties();
	protected OutputStream propertiesOutputFileStream = null;
	
	
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
		tokenTextField.setText(programSettings.getProperty("accessToken"));  
		
		// Save Token Button
		JButton tokenTextFieldSaveButton = new JButton("Save/Update Token");
		tokenTextFieldPanel.add(tokenTextFieldSaveButton);
		// Button Submit Function
		tokenTextFieldSaveButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				programSettings.setProperty("accessToken",tokenTextField.getText());
				saveConfig();
			}
		});
		
		// Authenticate Button
		JButton authenticateButton = new JButton("Authenticate");
		tokenTextFieldPanel.add(authenticateButton);
		// Button Submit Function
		authenticateButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				// authenticate call here
				JSONObject authenticationResponse = new JSONObject(pantheonCall("/authorize/machine-token","POST"));
				
				// save returned user_id
				programSettings.setProperty("user_id",authenticationResponse.getString("user_id"));
				programSettings.setProperty("session",authenticationResponse.getString("session"));
				saveConfig();
			}
		});

		//-----------------------------------------------------
		// Create Container, Label and Input for Site to Clone
		//-----------------------------------------------------
		// Panel
		JPanel siteToClonePanel = new JPanel();
		mainPanel.add(siteToClonePanel);
		
		// Label
		JLabel siteCloneFieldLabel = new JLabel("Select a Site to Clone");
		siteToClonePanel.add(siteCloneFieldLabel);
		
		// Site To Clone Select List
		final DefaultListModel siteToCloneSelectOptions = new DefaultListModel();
		// Load user's sites into the list
		String userSites = programSettings.getProperty("userSites");
		if(userSites != null){
			JSONObject userSitesJsonObject = new JSONObject(userSites);
			for(Iterator iterator = userSitesJsonObject.keySet().iterator(); iterator.hasNext();) {
				siteToCloneSelectOptions.addElement((String) iterator.next());
			}
		}
		JList siteToCloneSelectList = new JList(siteToCloneSelectOptions);
		siteToCloneSelectList.setVisibleRowCount(3);
		siteToCloneSelectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		siteToClonePanel.add(new JScrollPane(siteToCloneSelectList));
		
		// Update Site Options Button
		JButton updateSiteOptionsButton = new JButton("Update Site Options");
		siteToClonePanel.add(updateSiteOptionsButton);
		// Button Submit Function
		updateSiteOptionsButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				// Update site options
				String response = pantheonCall("/users/"+programSettings.getProperty("user_id")+"/memberships/sites","GET");
				// Note that this is a json array returned as opposed to object.
				JSONArray responseJson = new JSONArray(response);
				// This is the java version of an associative array
				Map<String, String> userSites = new HashMap<String, String>();
				// Loop over returned sites adding to list
				for (int i=0; i < responseJson.length(); i++) {
					String siteName = responseJson.getJSONObject(i).getJSONObject("site").getString("name");
					String siteLabel = responseJson.getJSONObject(i).getJSONObject("site").getJSONObject("attributes").getString("label");
					siteToCloneSelectOptions.addElement(siteLabel);
					userSites.put(siteLabel, siteName);
				}
				// Save just the info about the sites we're interested in
				programSettings.setProperty("userSites",new JSONObject(userSites).toString());
				saveConfig();
			}
		});
		
        // Display the window.
        frame.pack();
        frame.setVisible(true);
		
    }
	
	
	public String pantheonCall(String endpoint, String httpMethod){
		HttpURLConnection connection = null;

		try {
			// Create connection
			URL url = new URL("https://terminus.pantheon.io/api"+endpoint);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(httpMethod);
			
			// Set HTTP Headers
			connection.setRequestProperty("Host", "terminus.pantheon.io");
			if(httpMethod == "POST"){
				connection.setRequestProperty("Expect", "null");
			}
			connection.setRequestProperty("Accept-Encoding", "null");
			connection.setRequestProperty("User-Agent", "Terminus/1.0.0-alpha (php_version=7.0.12&script=bin/terminus)");
			connection.setRequestProperty("Content-type", "application/json");
			if(httpMethod == "GET"){
				connection.setRequestProperty("Authorization","Bearer "+programSettings.getProperty("session"));
				connection.setRequestProperty("headers","Bearer "+programSettings.getProperty("session"));
			}
			connection.setRequestProperty("verify", "1");
			if(httpMethod == "POST"){
				connection.setRequestProperty("json", "terminus");
			}
			if(httpMethod == "GET"){
				connection.setRequestProperty("method", "get");
				connection.setRequestProperty("absolute_url", "");
			}
			connection.setRequestProperty("Accept", "null");

			connection.setUseCaches(false);
			connection.setDoOutput(true);

			//Send request
			if(httpMethod == "POST"){
				DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
				wr.writeBytes("{\"machine_token\":\""+tokenTextField.getText()+"\",\"client\":\"terminus\"}");
				wr.close();
			}

			//Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
		return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	public void loadConfig(){
		try {
			input = new FileInputStream("config.properties");
			
			// load a properties file
			programSettings.load(input);

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
		//return prop.getProperty(key);
	}
	
	
	//See http://www.mkyong.com/java/java-properties-file-examples/
	public void saveConfig(){
		
		// Try saving config
		try {
			propertiesOutputFileStream = new FileOutputStream("config.properties");			
			//programSettings.setProperty(key, value);
			programSettings.store(propertiesOutputFileStream, null);
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (propertiesOutputFileStream != null) {
				try {
					propertiesOutputFileStream.close();
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
				// Load Settings
				pantheon.loadConfig();
				// Create and show GUI
                pantheon.createAndShowGUI(pantheon);
            }
        });
		
    }
}
