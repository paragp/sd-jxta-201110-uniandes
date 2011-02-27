package co.edu.uniandes.Servidor;



import java.awt.*;  
import java.awt.event.*;  
import javax.swing.*;  
  
import java.lang.reflect.InvocationTargetException;  
import java.io.File;  
import java.io.IOException;  
  
import net.jxta.peergroup.PeerGroup;  
import net.jxta.peergroup.PeerGroupFactory;  
import net.jxta.exception.PeerGroupException;  
  
import net.jxta.impl.peergroup.Platform;  
import net.jxta.impl.peergroup.GenericPeerGroup;  
  
import net.jxta.share.*;  
import net.jxta.share.metadata.*;  
  
/** 
 * Simple application that demonstrates how to share a file using CMS and  
 * annotate it using they Keywords and Description implementations of 
 * ContentMetadata. 
 * 
 * @see MetadataSearchDemo 
 * @see ShareDemo 
 * @see net.jxta.share.metadata.ContentMetadata 
 * @see net.jxta.share.metadata.Keywords 
 * @see net.jxta.share.metadata.Description 
 * @see net.jxta.share.ContentManager 
 * @version $Revision: 1.3 $ 
 */  
public class MetadataSearchDemo {  
  
    private PeerGroup netPeerGroup  = null;  
    private CMS cms = null;  
  
    private JFileChooser fc = new JFileChooser(new File("."));  
  
    static public void main(String args[]) {  
    //start MetadataShareDemo  
        new MetadataSearchDemo();  
    }  
    public MetadataSearchDemo() {  
        startJxta();  
  
        ShareWindow window = new ShareWindow();  
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
        homedir = (homedir != null) ? homedir + "MetadataShareDemo"   
        : "MetadataShareDemo";  
          
        //start CMS, creating a directory named ShareDemo to store the  
        // ContentAdvertisement cache in.  
        if(cms.startApp(new File(homedir)) == -1) {  
        System.out.println("CMS initialization failed");  
        System.exit(-1);  
        }  
          
    } catch ( PeerGroupException e) {  
        // could not instanciate the group, print the stack and exit  
        System.out.println("fatal error : group creation failure");  
        e.printStackTrace();  
        System.exit(1);  
    }   
    }  
  
    /** 
     * Inner class that defines the MetadataShareDemo GUI  
     */  
    public class ShareWindow extends Frame implements ActionListener {  
  
        Button shareButton;  
        List fileList;  
  
        public ShareWindow() {  
            super("Metadata Share Demo");  
            setSize(450, 250);  
            addWindowListener(new WindowMonitor());  
  
            Panel toolbar = new Panel();  
            toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));  
  
            shareButton = new Button("Share");  
            shareButton.addActionListener(this);  
            toolbar.add(shareButton);  
  
            add(toolbar, BorderLayout.NORTH);  
  
            fileList = new java.awt.List();  
            add(fileList, BorderLayout.CENTER);  
              
            //immediately fill the list with content that is being shared  
            updateLocalFiles();  
        }  
  
    public void actionPerformed(ActionEvent e) {  
        System.out.println(e.getActionCommand());  
          
        //handle the event of the "Share" button being clicked  
        if (e.getSource().equals(shareButton)) {  
        //prompt the user to choose a file to share  
        int returnVal = fc.showOpenDialog(this);  
          
        if (returnVal == JFileChooser.APPROVE_OPTION) {  
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
            if(input != null) {  
            if(input.equals("keywords")) {  
  
                input = JOptionPane.showInputDialog(this, "Enter a comma-separated list of keywords describing the file");  
  
                if(input != null) {  
                mdata = new ContentMetadata[1];
                System.out.println("Keywords mdata:_ "+mdata);
                  
                //create a metadata element using the  
                //"keywords" scheme  
                mdata[0] = new Keywords(input);  
                }  
            } else if(input.equals("description")) {  
                input = JOptionPane.showInputDialog(this, "Enter a description of the file");  
                if(input != null) {  
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
            	System.out.println("Voy a intentar compaartir ");
            cms.getContentManager().share(file,null,null,mdata);  
            //cms.getContentManager().notifyAll();  
            //update the list of shared content  
            System.out.println("actualizo archivos ");
            updateLocalFiles();  
            } catch (IOException ex) {  
            System.out.println("Share command failed.");  
            }                         
        } else {  
            System.out.println("Share command cancelled by user.");  
        }  
        }  
    }  
      
    /** 
     * Refreshes the list of shared content 
     */  
        private void updateLocalFiles() {  
        //ContentManager.getContent() retrieves all of the content that is  
        // being shared by this peer.  
            Content[] content = cms.getContentManager().getContent();  
          
        //erase the list of shared content...  
            fileList.removeAll();  
  
        //...and repopulate it  
            for (int i=0; i<content.length; i++) {  
                fileList.add(content[i].getContentAdvertisement().getName());  
            }  
        }  
    }  
  
    /** 
     * A window adapter to take care of cleanup  
     */  
    class WindowMonitor extends WindowAdapter {  
        public void windowClosing(WindowEvent e) {  
            Window w = e.getWindow();  
            w.setVisible(false);  
            w.dispose();  
            System.exit(0);  
        }  
    }  
}  