package main;

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JProgressBar;

import net.jxta.peergroup.PeerGroup;
import net.jxta.share.ContentAdvertisement;
import net.jxta.share.client.GetContentRequest;

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
            , File destination, PeerGroup netPeerGroup){  
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
