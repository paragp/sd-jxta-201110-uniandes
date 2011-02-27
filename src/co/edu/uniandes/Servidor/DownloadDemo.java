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
        super("Download Demo");  
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
          
        downloadButton = new Button("Download");  
        downloadButton.addActionListener(this);  
        toolbar.add(downloadButton);  
  
        add(toolbar, BorderLayout.NORTH);  
          
        resultList = new List();  
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