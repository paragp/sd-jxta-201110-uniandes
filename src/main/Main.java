package main;

import java.awt.*;  
import java.awt.event.*;  
import javax.swing.*;  

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
  
import java.lang.reflect.InvocationTargetException;  
import java.net.InetAddress;
import java.io.File;  
import java.io.InputStream;  
import java.io.IOException;  
import java.util.Date;
import java.util.Vector;  
  
import net.jxta.document.MimeMediaType;  
import net.jxta.document.Advertisement;  
  
import net.jxta.peergroup.PeerGroup;  
import net.jxta.peergroup.PeerGroupFactory;  
import net.jxta.exception.PeerGroupException;  
  
import net.jxta.impl.peergroup.Platform;  
import net.jxta.impl.peergroup.GenericPeerGroup;  
  
import net.jxta.share.*;  
import net.jxta.share.client.*;  
import net.jxta.share.metadata.*;  
  
/** 
 * An extended version of SearchDemo that also has download capabilities. 
 *  
 * @see SearchDemo 
 * @see ShareDemo 
 * @see net.jxta.share.client.ListContentRequest 
 *  
 * @version $Revision: 1.3 $ 
 */  
public class Main {  
  
    private PeerGroup netPeerGroup  = null;
    private CMS cms = null;
    private JFileChooser fc = new JFileChooser(new File("./data"));
    public static final String TIME_SERVER = "time-a.nist.gov";
      
    static public void main(String args[]) {  
    //start DownloadDemo  
    new Main();  
    }  
  
    public Main() {  
    startJxta();  
      
    SearchWindow window = new SearchWindow();  
    window.setVisible(true);  
    }  
  
    /** 
     * initializes NetPeerGroup and the CMS 
     */  
    private void startJxta() {  
    try {  
        // create, and Start the default jxta NetPeerGroup  
        netPeerGroup = PeerGroupFactory.newNetPeerGroup();  
          
        //uncomment the following line if you want to start the app defined  
        // the NetPeerGroup Advertisement (by default it's the shell)  
        // in this case we want use jxta directly.  
          
        // netPeerGroup.startApp(null);
        
      //instanciate and initialize a content management service for   
        //the NetPeerGroup  
        cms = new CMS();  
        cms.init(netPeerGroup, null, null);  
          
        //set up a MetadataShareDemo directory inside the JXTA_HOME dir  
        String homedir = System.getProperty("JXTA_HOME");  
        homedir = (homedir != null) ? homedir + "DownloadCache"   
        : "DownloadCache";  
          
        //start CMS, creating a directory named ShareDemo to store the  
        // ContentAdvertisement cache in.  
        if(cms.startApp(new File(homedir)) == -1) {  
        System.out.println("CMS initialization failed");  
        System.exit(-1);
        }
              
    } catch (PeerGroupException e) {  
        // could not instanciate the group, print the stack and exit  
        System.out.println("fatal error : group creation failure");  
        e.printStackTrace();  
        System.exit(-1);  
    }  
    }  
  
    /** 
     * SearchWindow serves as the GUI for MetadataSearchDemo 
     */  
    public class SearchWindow extends Frame implements ActionListener {  
      
    Button shareButton;
    Button searchButton;
    Button searchSizeButton;
    Button viewButton;  
    Button downloadButton;  
    List resultList;
    List downloadList;
    String resultQuery;
    boolean bySize = false;
    long size;
    long limit;
  
    MetadataQuery descQuery;  
    MetadataQuery keywdQuery;  
      
    //A ListContentRequest is needed to query other peers for  
    //ContentAdvertisements  
    ListContentRequest request = null;  
  
    //an array is needed to store ContentAdvertisements returned by the  
    //ListContentRequest  
    ContentAdvertisement[] results = null;  
  
    /** 
     * Initializes & arranges the window and its components. 
     */  
    public SearchWindow() {  
        super("Collective Media for Secret agents (CMS)");  
        setSize(450, 350);  
        addWindowListener(new WindowMonitor());  
  
        Panel toolbar = new Panel();  
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));  
        
        shareButton = new Button("Share");  
        shareButton.addActionListener(this);  
        toolbar.add(shareButton);
        
        searchButton = new Button("Search");  
        searchButton.addActionListener(this);  
        toolbar.add(searchButton);
        
        searchSizeButton = new Button("Search by Size");  
        searchSizeButton.addActionListener(this);  
        toolbar.add(searchSizeButton);
          
        viewButton = new Button("View Advertisement");  
        viewButton.addActionListener(this);  
        toolbar.add(viewButton);  
          
        downloadButton = new Button("Download");  
        downloadButton.addActionListener(this);  
        toolbar.add(downloadButton);  
  
        add(toolbar, BorderLayout.NORTH);
        
        Panel resultsPane = new Panel();
        resultsPane.setLayout(new BorderLayout());
        
        JLabel labelResults = new JLabel("Search Results");
        resultsPane.add(labelResults, BorderLayout.NORTH);
          
        resultList = new List();  
        resultsPane.add(resultList, BorderLayout.CENTER);
        add(resultsPane, BorderLayout.CENTER);
        
        Panel downloads = new Panel();
        downloads.setLayout(new BorderLayout());
        
        JLabel labelDownloads = new JLabel("Downloaded Files");
        downloads.add(labelDownloads, BorderLayout.NORTH);
          
        downloadList = new List();  
        downloads.add(downloadList, BorderLayout.CENTER);
        add(downloads, BorderLayout.SOUTH);        
    }  
      
    public void actionPerformed(ActionEvent e) 
    {  
        System.out.println(e.getActionCommand());  
        //handle the event caused by the "Search" button being clicked  
        if (e.getSource().equals(shareButton)) 
        {  
        	//prompt the user to choose a file to share  
        	int returnVal = fc.showOpenDialog(this);  
          
        	if (returnVal == JFileChooser.APPROVE_OPTION) 
        	{  
        		File file = fc.getSelectedFile();  
        		              
        		String input = (String)JOptionPane.showInputDialog(this, "Enter a comma-separated list of keywords describing the file");  
              
        		String description = "";
        		
        		if(input != null) 
        		{  
        			description = "Keywords:" + input;
        			Date time = null;
        			try
        			{
        			NTPUDPClient timeClient = new NTPUDPClient();
        			InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
        			TimeInfo timeInfo = timeClient.getTime(inetAddress);
        			long returnTime = timeInfo.getReturnTime();
        			time = new Date(returnTime);
        			System.out.println("Time from " + TIME_SERVER + ": " + time);
        			}
        			catch(Exception ex)
        			{
        				System.out.println("Failed to get UTP time");        				
        			}
        			if(time != null)
        			{
        				description += " Date:" + time;
        			}
        			else
        			{
        				description += " Date:Not alvailable";
        			}
        		}  
              
            try {  
            //ContentManager.share() will share and advertise a  
            // file using a ContentAdvertisement containing the  
            // metadata that was just created. Passing in nulls for  
            // the name and content type will cause the content to  
            // be advertised under the name of the file prefix, and  
            // advertised as being the content type that is  
            // determined by this ContentManager's getMimeType()  
            // function.  
            cms.getContentManager().share(file, description);  
              
            //update the list of shared content  
            updateLocalFiles();  
            } catch (IOException ex) {  
            System.out.println("Share command failed.");  
            }                         
        } else {  
            System.out.println("Share command cancelled by user.");  
        }  
        }        
        else if (e.getSource().equals(searchButton)) {  
        if (request != null) {  
            request.cancel();  
        }
        bySize = false;
  
        //prompt the user for a search string  
        String searchString = JOptionPane  
            .showInputDialog(this, "Enter a string to search for:");  
          
        //the user clicked "cancel"; exit this function  
        if(searchString == null) return;
        
        resultQuery = searchString;
        
        System.out.println("query is " + resultQuery);
          
        //Initialize a ListContentRequest containing the search string  
        // that was entered.  
        //request = new MyListRequest(netPeerGroup, searchString, this);
        request = new MyListRequest(netPeerGroup, null, this);
          
        //send the list request and wait for results to be returned  
        request.activateRequest();        
        }else if (e.getSource().equals(viewButton)) {  
        //handle the event caused by the "View Advertisement" button  
        // being clicked.  
  
        //figure out which content advertisement is selected, then  
        //display it in an AdvertisementViewer  
        int selectedIndex = resultList.getSelectedIndex();  
        if((results != null) && (selectedIndex != -1)  
           && (results[selectedIndex] != null)) {  
            new AdvertisementViewer(results[selectedIndex]);  
        }  
        }
        else if(e.getSource().equals(searchSizeButton))
        {
        	bySize = true;
        	
        	JOptionPane.showMessageDialog(null, "Please select the document that will be used as a size reference");
        	
        	fc = new JFileChooser(new File("./data/text"));
        	int returnVal = fc.showOpenDialog(this);  
            
        	if (returnVal == JFileChooser.APPROVE_OPTION) 
        	{  
        		File file = fc.getSelectedFile();
        		
        		size = file.length();
        		
        		System.out.println(size);
        		
        		String limiter = JOptionPane.showInputDialog(this, "Please state a search boundary (in bytes)");
        		
        		limit = Long.parseLong(limiter);
        		
        		System.out.println(limit);
        		
        		if (request != null) {  
                    request.cancel();  
                }
        		
        		request = new MyListRequest(netPeerGroup, null, this);
                
                //send the list request and wait for results to be returned  
                request.activateRequest();        		
        	}
        	else
        	{
        		size = 0;
        		limit = 0;
        	}
        	
        }
        else if (e.getSource().equals(downloadButton)) 
        {  
  
        //figure out which content advertisement is selected  
        int selectedIndex = resultList.getSelectedIndex();
        
        if((results != null) && (selectedIndex != -1)  
           && (results[selectedIndex] != null)) {  
              
            JFileChooser saveDialog = new JFileChooser();  
            saveDialog.setLocation(300, 200);  
  
            //set the default save path to the name of the content  
            File savePath  
            = new File(results[selectedIndex].getName());  
              
            saveDialog.setSelectedFile(savePath);  
            int returnVal = saveDialog.showSaveDialog(this);  
            if (returnVal == JFileChooser.APPROVE_OPTION) {  
            savePath = saveDialog.getSelectedFile();  
              
            //start up a GetContentRequest for the selected content  
            //advertisement.  
            new VisibleContentRequest(this, results[selectedIndex] ,savePath, netPeerGroup);
            Date time = null;
			try
			{
			NTPUDPClient timeClient = new NTPUDPClient();
			InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
			TimeInfo timeInfo = timeClient.getTime(inetAddress);
			long returnTime = timeInfo.getReturnTime();
			time = new Date(returnTime);
			System.out.println("Time from " + TIME_SERVER + ": " + time);
			}
			catch(Exception ex)
			{
				System.out.println("Failed to get UTP time");        				
			}
			if(time != null)
			{
				downloadList.add(savePath.getName() + " " + time);
			}
			else
			{
				downloadList.add(savePath.getName() + " Time of download not alvailable");
			}
            
            } else {  
            System.out.println("save canceled");  
            }  
        }  
        }  
    }  
      
    /** 
     * This method filters through advertisements returned by other peers 
     * and then displays the matches in the list. 
     */  
    protected void updateResults(ContentAdvertisement[] results) {  
        this.results = results;  
          
        //erase all of the old results  
        resultList.removeAll();  
          
        //insert the updated results into the list  
        for (int i=0; i<results.length; i++)
        {  
        	if(bySize == false)
        	{
        		if(resultQuery !=null && !resultQuery.equals(""))
            	{
            		String description = results[i].getDescription();
                	if(description != null)
                	{
                		description.trim();
                    	if(description.split("Date:").length > 1)
                    	{
                    		String key = description.split("Date:")[0];
                    		key = key.replaceFirst("Keywords:", "");
                    		String[] keyword = key.split(",");
                    		boolean added = false;
                    		for(int k =0; k<keyword.length && !added;k++)
                    		{
                    			System.out.println(keyword[k]);
                    			if(keyword[k].trim().equals(resultQuery))
                    			{
                    				resultList.add(results[i].getName() + " " + results[i].getDescription());
                    				added = true;
                    			}                   			
                    		}
                    	}
                	}        		
            	}
            	else
            	{
            		resultList.add(results[i].getName() + " " + results[i].getDescription());
            	}        		
        	}
        	else
        	{
        		if(size != 0)
        		{
        			long length = results[i].getLength();
        			System.out.println(length);
            		if(length <= size + limit && length >= size - limit)
            		{
            			resultList.add(results[i].getName() + " " + results[i].getDescription());            			
            		}
        			
        		}
        		else
        		{
        			resultList.add(results[i].getName() + " " + results[i].getDescription());
        		}
        	}
        }  
    }
    
    private void updateLocalFiles() {  
        //ContentManager.getContent() retrieves all of the content that is  
        // being shared by this peer.  
            Content[] content = cms.getContentManager().getContent();
            
                      
        //erase the list of shared content...  
            resultList.removeAll();  
  
        //...and repopulate it  
            for (int i=0; i<content.length; i++) {  
                resultList.add(content[i].getContentAdvertisement().getName());  
            }  
        }
    }  
  
           
  
    class WindowMonitor extends WindowAdapter {  
    public void windowClosing(WindowEvent e) {  
        Window w = e.getWindow();  
        w.setVisible(false);  
        w.dispose();  
        System.exit(0);  
    }  
    }  
}  