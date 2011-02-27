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

public class MetadataSearchDemo {  
  
    private PeerGroup netPeerGroup  = null;  
      
    static public void main(String args[]) {  
    //start MetadataSearchDemo  
    new MetadataSearchDemo();  
    }  
      
    public MetadataSearchDemo() {  
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
          
        netPeerGroup.startApp(null);  
              
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
      
    Button searchButton;  
    Button viewButton;  
    List resultList;  
      
    MetadataQuery descQuery;  
    MetadataQuery keywdQuery;  
      
    //A ListContentRequest is needed to query other peers for  
    //ContentAdvertisements  
    ListContentRequest request = null;  
      
    //an array is needed to store ContentAdvertisements returned by the  
    //ListContentRequest  
    ContentAdvertisement[] results = null;  
      
    //this vector will store the ContentAdvertisements from results that  
    // were found to have metadata that matches the query  
    Vector matches = new Vector();  
      
    /** 
     * Initializes & arranges the window and its components. 
     */  
    public SearchWindow() {  
        super("Metadata Search Demo");  
        setSize(450, 250);  
        addWindowListener(new WindowMonitor());  
          
        Panel toolbar = new Panel();  
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));  
          
        searchButton = new Button("Search");  
        searchButton.addActionListener(this);  
        toolbar.add(searchButton);  
          
        viewButton = new Button("View Advertisement");  
        viewButton.addActionListener(this);  
        toolbar.add(viewButton);  
          
        add(toolbar, BorderLayout.NORTH);  
          
        resultList = new java.awt.List();  
        add(resultList, BorderLayout.CENTER);  
    }  
      
    public void actionPerformed(ActionEvent e) {  
        System.out.println(e.getActionCommand());  
          
        //handle the event caused by the "Search" button being clicked  
        if (e.getSource().equals(searchButton)) {  
        if (request != null) {  
            request.cancel();  
        }  
          
        //prompt the user for a search string  
        String searchString = JOptionPane  
            .showInputDialog(this, "Enter a string to search for:");  
          
        if(searchString == null) return;  
          
        //see the source of net.jxta.share.metadata.Keywords and  
        //net.jxta.share.metadata.Description to see  
        //how these MetadataQuery objects work  
          
        //this will generate a MetadataQuery object for querying a  
        //Description object  
        descQuery = Description.newQuery(searchString);  
          
        //generate a MetadataQuery for querying a Keywords object  
        keywdQuery = Keywords.newQuery(searchString);  
          
        //Initialize a ListContentRequest.  Note that an empty search  
        //string is passed in, causing every peer in netPeerGroup to  
        //return advertisements for all of the content they are sharing  
        request = new MyListRequest(netPeerGroup, "", this);  
          
        //send the list request and wait for results to be sent back  
        request.activateRequest();                
        }else if (e.getSource().equals(viewButton)) {  
        //handle the event caused by the "View Advertisement" button  
        // being clicked.  
          
        //figure out which content advertisement is selected, then  
        //display it in an AdvertisementViewer  
        int selectedIndex = resultList.getSelectedIndex();  
        if(selectedIndex != -1) {  
            ContentAdvertisement selAd =  
            (ContentAdvertisement)matches.elementAt(selectedIndex);  
            if(selAd != null)  
            new AdvertisementViewer(selAd);  
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
        matches = new Vector(results.length);  
          
        ContentMetadata[] melems;  
          
        for (int i=0; i<results.length; i++) {  
        //retrieve the metadata in a ContentAdvertisement  
        melems = results[i].getMetadata();  
          
        //check if the metadata matches the search criteria using a  
        // MetadataQuery of the appropriate type.  
          
        if(melems != null) {  
            for(int j= 0; j < melems.length; j++) {  
            try {  
                if(melems[j] instanceof Description) {  
                //add results with a description  
                //that contains the search string,  
                if(descQuery.queryMetadata(melems[j]) > 0) {  
                    resultList.add(results[i].getName());  
                    matches.addElement(results[i]);  
                    break;  
                }  
                  
                }else if(melems[j] instanceof Keywords) {  
                // or a keyword identical to the search string.  
                if(keywdQuery.queryMetadata(melems[j]) > 0) {  
                    resultList.add(results[i].getName());  
                    matches.addElement(results[i]);  
                    break;  
                }  
                }  
            }catch(IllegalArgumentException iae) {  
                //the ContentMetadata object passed into  
                // queryMetadata() wasn't formatted properly  
                System.out.println("malformed metadata");  
            }  
            }  
        }  
        }  
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