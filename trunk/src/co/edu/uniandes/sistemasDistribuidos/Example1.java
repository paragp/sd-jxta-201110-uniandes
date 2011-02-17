package co.edu.uniandes.sistemasDistribuidos;

import java.io.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Enumeration;
import net.jxta.document.*;
import net.jxta.peergroup.*;
import net.jxta.exception.*;
import net.jxta.impl.peergroup.*;
import net.jxta.id.*;
import net.jxta.discovery.*;
import net.jxta.pipe.*;
import net.jxta.protocol.*;
import java.net.MalformedURLException;
import java.net.URL;
import net.jxta.endpoint.*;
import java.lang.reflect.InvocationTargetException;
import net.jxta.share.*;
import net.jxta.share.client.*;

public class Example1 extends JFrame {

	static PeerGroup netPeerGroup = null;
	private DiscoveryService myDiscoveryService = null;
	private PipeService myPipeService = null;
	private JTextArea displayArea;
	private JButton sendButton;
	private CMS myCms = null;
	private ListRequestor myListRequestor = null;
	private final static MimeMediaType XMLMIMETYPE = new MimeMediaType(
			"text/xml");

	public static void main(String args[]) 
	{
		Example1 myapp = new Example1();
		myapp.addWindowListener(new WindowAdapter() 
		{
			public void windowClosing(WindowEvent e) 
			{
				System.exit(0);
			}
		});
		myapp.launchJXTA();
		myapp.getServices();
		myapp.run();
	}

	public Example1() 
	{
		super("client");
		Container c = getContentPane();
		sendButton = new JButton("Send Search");
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendData();
			}
		});
		c.add(sendButton, BorderLayout.NORTH);
		displayArea = new JTextArea();
		c.add(new JScrollPane(displayArea), BorderLayout.CENTER);
		setSize(300, 150);
		show();
	}

	public void run() {
	displayArea.append("Click on Button to send data...\n");
	try {
	myCms.getContentManager().share(new File("image.jpg"));
	} catch (IOException ex) {
	System.out.println("Share command failed.");
	}
	}

	private void launchJXTA() {
	displayArea.append("Launching Peer into JXTA Network...\n");
	try {
	netPeerGroup = PeerGroupFactory.newNetPeerGroup();
	} catch (PeerGroupException e) {
	System.out.println("Unable to create PeerGroup - Failure");
	e.printStackTrace();
	System.exit(1);
	}
	}

	private void getServices() {
		displayArea.append("Getting Services...\n");
		myDiscoveryService = netPeerGroup.getDiscoveryService();
		myPipeService = netPeerGroup.getPipeService();
		try {
			myCms = new CMS();
			myCms.init(netPeerGroup, null, null);
			if (myCms.startApp(new File("client")) == -1) {
				System.out.println("CMS initialization failed");
				System.exit(-1);
			}
		} catch (Exception e) {
			System.out.println("CMS init failure");
			System.exit(-1);
		}
	}

	public interface ContentListener {
		public void finishedRetrieve(String url);
	}

	class GetRequestor extends GetContentRequest {
		private ContentAdvertisement searchResult = null;
		private String url = null;
		private ContentListener listener;

		public GetRequestor(PeerGroup pg, ContentAdvertisement res,
				File tmpFile, ContentListener listener)
				throws InvocationTargetException {
			super(pg, res, tmpFile);
			searchResult = res;
			url = tmpFile.getAbsolutePath();
			this.listener = listener;
		}

		public ContentAdvertisement getContentAdvertisement() {
			return searchResult;
		}

		public void notifyDone() {
			listener.finishedRetrieve(url);
		}
	}

	class ListRequestor extends CachedListContentRequest {
		boolean gotOne = false;

		public ListRequestor(PeerGroup group, String inSubStr) {
			super(group, inSubStr);
		}

		public void notifyMoreResults() {
			System.out.println("Search Done");
			ContentAdvertisement[] result =
			myListRequestor.getResults();
			if ( result != null ) {
			displayArea.append("Length = " + result.length + "\n");
			for (int i=0;i<result.length;i++) {
			ContentAdvertisement myAdv = result[i];
			displayArea.append(myAdv.getName() + "\n");
			if (!gotOne) {
			displayArea.append("Starting Download\n");
			File tmpFile = new File( "file" + myAdv.getName());
			
			ContentListener myListener = new ContentListener() {
			public void finishedRetrieve(String url) {
			displayArea.append("File Download Finished\n");
			}
			};
			try {
			GetRequestor request = new GetRequestor(
			netPeerGroup, result[i], tmpFile, myListener );
			} catch ( InvocationTargetException e ) {
			e.printStackTrace();
			}
			gotOne = true;
			}
			}
			}
			else {
			System.out.println("No results");
			}
			}
	}

	private void sendData() {
			myListRequestor = new ListRequestor(netPeerGroup, "jpg");
			myListRequestor.activateRequest();
		}
	}


