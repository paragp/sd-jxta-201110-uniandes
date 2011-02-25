package co.edu.uniandes.sistemasDistribuidos;

/*
 * Created on 02/03/2005
 *
 */

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.List;
import java.awt.Panel;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.share.CMS;
import net.jxta.share.Content;
import net.jxta.share.SearchListener;

/**
 * Simple application that demonstrates how to share a file using CMS.
 * 
 * @see SearchDemo
 * @see MetadataShareDemo
 * @see net.jxta.share.ContentManager
 * @see net.jxta.share.CMS
 * @version $Revision: 1.1 $
 */
public class ShareDemo {

    private PeerGroup netPeerGroup = null;
    private CMS cms = null;

    static public void main(String args[]) {
        // start ShareDemo
        new ShareDemo();
    }

    public ShareDemo() {
        startJxta();

        // adds a search listener that monitors incoming list requests
        cms.addSearchListener(new MySearchListener());

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

            // uncomment the following line if you want to start the app defined
            // the NetPeerGroup Advertisement (by default it's the shell)
            // in this case we want use jxta directly.

            // netPeerGroup.startApp(null);

            // instanciate and initialize a content management service for
            // the NetPeerGroup
            cms = new CMS();
            cms.init(netPeerGroup, null, null);

            // set up a ShareDemo directory inside the JXTA_HOME directory
            String homedir = System.getProperty("JXTA_HOME");
            homedir = (homedir != null) ? homedir + "ShareDemo" : "ShareDemo";

            // start CMS, creating a directory named ShareDemo to store the
            // ContentAdvertisement cache in.
            if (cms.startApp(new File(homedir)) == -1) {
                System.out.println("CMS initialization failed");
                System.exit(-1);
            }

        } catch (PeerGroupException e) {
            // could not instanciate the group, print the stack and exit
            System.out.println("fatal error : group creation failure");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * A simple implementation of a SearchListener.
     */
    class MySearchListener implements SearchListener {

        /**
         * This method is called every time a list request is recieved. This implementation prints the query string to standard output when it is called.
         */
        public void queryReceived(String queryString) {
            System.out.println("List request with query \"" + queryString + "\" received.");
        }
    }

    /**
     * Inner class that defines the ShareDemo GUI
     */
    public class ShareWindow extends Frame implements ActionListener {
        JFileChooser fc = new JFileChooser(new File("."));
        Button shareButton;
        List fileList;

        public ShareWindow() {
            super("Share Demo");
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

            // immediately fill the list with content that is being shared
            updateLocalFiles();
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println(e.getActionCommand());

            // handle the event of the "Share" button being clicked
            if (e.getSource().equals(shareButton)) {
                // prompt the user to choose a file to share
                int returnVal = fc.showOpenDialog(this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();

                    // this is where a real application would open the file.
                    System.out.println("Sharing: " + file.getName() + ".");
                    try {
                        // ContentManager.share() will automatically create a
                        // ContentAdvertisement for a file and begin sharing it.
                        // If more control in the construction of the
                        // ContentAdvertisement is needed, there are also
                        // overloaded versions of share() that allow the
                        // advertised name, content type, and other metadata to
                        // in the advertisement to be specified.
                        cms.getContentManager().share(file);

                        // update the list of shared content
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
            // ContentManager.getContent() retrieves all of the content that is
            // being shared by this peer.
            Content[] content = cms.getContentManager().getContent();

            // erase the list of shared content...
            fileList.removeAll();

            // ...and repopulate it
            for (int i = 0; i < content.length; i++) {
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

