package co.edu.uniandes.Servidor;

import java.awt.*;  
import java.awt.event.*;  
import javax.swing.*;  
  
import java.lang.reflect.InvocationTargetException;  
import java.io.File;  
import java.io.InputStream;  
import java.io.IOException;  
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
public class DownloadDemo {  
  
    private PeerGroup netPeerGroup  = null;
    private CMS cms = null;
    private JFileChooser fc = new JFileChooser(new File("."));
      
    static public void main(String args[]) {  
    //start DownloadDemo  
    new DownloadDemo();  
    }  
  
    public DownloadDemo() {  
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
        homedir = (homedir != null) ? homedir + "DownloadDemo"   
        : "DownloadDemo";  
          
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
    Button viewButton;  
    Button downloadButton;  
    List resultList;  
  
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
        setSize(450, 250);  
        addWindowListener(new WindowMonitor());  
  
        Panel toolbar = new Panel();  
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));  
        
        shareButton = new Button("Share");  
        shareButton.addActionListener(this);  
        toolbar.add(shareButton);
        
        searchButton = new Button("Search");  
        searchButton.addActionListener(this);  
        toolbar.add(searchButton);  
          
        viewButton = new Button("View Advertisement");  
        viewButton.addActionListener(this);  
        toolbar.add(viewButton);  
          
        downloadButton = new Button("Download");  
        downloadButton.addActionListener(this);  
        toolbar.add(downloadButton);  
  
        add(toolbar, BorderLayout.NORTH);  
          
        resultList = new List();  
        add(resultList, BorderLayout.CENTER);
        
        //immediately fill the list with content that is being shared  
        updateLocalFiles();
    }  
      
    public void actionPerformed(ActionEvent e) {  
        System.out.println(e.getActionCommand());  
        //handle the event caused by the "Search" button being clicked  
        if (e.getSource().equals(shareButton)) 
        {  
        	//prompt the user to choose a file to share  
        	int returnVal = fc.showOpenDialog(this);  
          
        	if (returnVal == JFileChooser.APPROVE_OPTION) 
        	{  
        		File file = fc.getSelectedFile();  
        		Object[] choices = { "keywords", "description"};  
              
        		//display a dialog asking which metadata scheme to use  
        		String input = (String)JOptionPane.showInputDialog(this, "Choose a metadata scheme to use to annotate the file", "", JOptionPane.QUESTION_MESSAGE, null, choices , choices[0]);  
              
        		//metadata must be passed into ContentManager.share() as  
        		// an array.  A null is passed in instead if no metadata  
        		// was specified  
        		ContentMetadata mdata[] = null;  
  
        		//construct the appropriate type of metadata depending on  
        		// the user's choice  
        		if(input != null) 
        		{  
        			if(input.equals("keywords")) 
        			{  
  
        				input = JOptionPane.showInputDialog(this, "Enter a comma-separated list of keywords describing the file");  
  
        				if(input != null) 
        				{  
        					mdata = new ContentMetadata[1];  
                  
        					//create a metadata element using the  
        					//"keywords" scheme  
        						mdata[0] = new Keywords(input);  
        				}  
        		} 
        		else if(input.equals("description")) 
        		{  
        			input = JOptionPane.showInputDialog(this, "Enter a description of the file");  
        			if(input != null) 
        			{  
        				mdata = new ContentMetadata[1];  
                  
        				//create a metadata element using the  
        				//"description" scheme  
        				mdata[0] = new Description(input);  
        			}  
        		}  
            }  
              
            //this is where a real application would open the file.  
            System.out.println("Sharing: " + file.getName() + ".");  
            try {  
            //ContentManager.share() will share and advertise a  
            // file using a ContentAdvertisement containing the  
            // metadata that was just created. Passing in nulls for  
            // the name and content type will cause the content to  
            // be advertised under the name of the file prefix, and  
            // advertised as being the content type that is  
            // determined by this ContentManager's getMimeType()  
            // function.  
            cms.getContentManager().share(file,null,null,mdata);  
              
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
  
        //prompt the user for a search string  
        String searchString = JOptionPane  
            .showInputDialog(this, "Enter a string to search for:");  
          
        //the user clicked "cancel"; exit this function  
        if(searchString == null) return;  
          
        //Initialize a ListContentRequest containing the search string  
        // that was entered.  
        request = new MyListRequest(netPeerGroup, searchString, this);  
          
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
        }else if (e.getSource().equals(downloadButton)) {  
  
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
            new VisibleContentRequest(this, results[selectedIndex]  
                           ,savePath);  
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
        for (int i=0; i<results.length; i++) {  
        resultList.add(results[i].getName());  
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
  
    /** 
     * VisibleContentRequest is a special type of GetContentRequest that 
     * displays a dialog with a progress bar as long as it is active.  The 
     * dialog also contains a "Stop" button to cancel the download if desired. 
     */  
    class VisibleContentRequest extends GetContentRequest  
    implements ActionListener{  
      
    JDialog dialog;  
    JProgressBar statusBar = new JProgressBar();  
    JButton cancelButton = new JButton("Stop");  
  
    /** 
     * Create, start, and display a new VisibleContentRequest as a child of 
     * a given Frame. 
     * 
     *@param parent the parent Frame object 
     *@param source an advertisement of the content to be downloaded. 
     *@param destination a file pointer to save the content to. 
     */  
    public VisibleContentRequest(Frame parent, ContentAdvertisement source  
                , File destination){  
        super(netPeerGroup, source, destination);  
          
        dialog = new JDialog(parent, "Downloading "+destination.getName());  
          
        dialog.setSize(240, 50);  
        dialog.setLocation(400,400);  
  
        statusBar.setStringPainted(true);  
      
        dialog.getContentPane()  
        .setLayout(new FlowLayout(FlowLayout.CENTER));  
        dialog.getContentPane().add(statusBar);  
          
        cancelButton.addActionListener(this);  
        dialog.getContentPane().add(cancelButton);  
        dialog.setVisible(true);  
    }  
  
    public void actionPerformed(ActionEvent ae) {  
        //handle the event caused by the "Stop" button being clicked  
        if(ae.getSource() == cancelButton) {  
        cancel();  
        System.out.println("download of " + getFile()  
                   + " cancelled by user.");  
        dialog.dispose();  
        dialog = null;  
        }  
    }  
  
    /** 
     * This method is called when the download is complete. 
     */  
    public void notifyDone() {  
        System.out.println("download of "+getFile()+" done.");
        try {
            Desktop.getDesktop().open(getFile());
       }catch (IOException ex) {
    	   System.out.println("Opening of "+getFile()+" failed.");
       }
        dialog.dispose();  
        dialog = null;  
    }  
  
    /** 
     * This method is called if the download fails. 
     */  
    public void notifyFailure() {  
        System.out.println("download of "+getFile()+" failed.");  
    }  
  
    /** 
     * This method as called as more of the file has been downloaded. 
     * 
     * @param percentage the percentage of the file that has been 
     * downloaded so far. 
     */  
    public void notifyUpdate(int percentage) {  
        statusBar.setValue(percentage);  
    }  
    }  
  
    /** 
     * An implementation of ListContentRequest that will automatically update 
     * a SearchWindow as ContentAdvertisements are returned. 
     *  
     * @see ListContentRequest 
     * @see CachedListContentRequest 
     */  
    class MyListRequest extends ListContentRequest {  
    SearchWindow searchWindow = null;  
      
    /** 
     * Initialize a list request that will be propagated throughout a given 
     * peer group.  Any ContentAdvertisement for which the string returned 
     * by getName() or getDescription() contains inSubStr 
     *  (case insensitive) is sent back in a list response. However, the 
     * list request isn't sent until activateRequest() is called. 
     *  
     * @see net.jxta.share.client.ListContentRequest 
     * @see net.jxta.share.client.ListContentRequest#ListContentRequest(net.jxta.peergroup.PeerGroup, java.lang.String) 
     */  
    public MyListRequest(PeerGroup group, String inSubStr  
                 ,SearchWindow searchWindow) {  
        super(group, inSubStr);  
        this.searchWindow = searchWindow;  
    }  
  
    /** 
     * This function is called each time more results are received. 
     */  
    public void notifyMoreResults() {  
        if (searchWindow != null) {  
        //note: getResults() returns all of the ContentAdvertisements  
        //received so far, not just the ones that were in the last list  
        //response.  
        searchWindow.updateResults(getResults());  
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