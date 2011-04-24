package co.edu.uniandes.sd.proy02;

import java.awt.*;  
import java.awt.event.*;  
import javax.swing.*; 
import org.apache.log4j.*;


import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import co.edu.unaindes.sd.seguridad.ArchivoCifrado;
import co.edu.unaindes.sd.seguridad.FileToArray;
import co.edu.unaindes.sd.seguridad.PublicKeyCryptography;
import co.edu.unaindes.sd.seguridad.generadorCertificado;
import co.edu.uniandes.sistemasDistribuidos.AdvertEjemplo;
import co.edu.uniandes.sistemasDistribuidos.AdvertisementEjemplo;
import co.edu.uniandes.sistemasDistribuidos.Publicador;
  
import java.lang.reflect.InvocationTargetException;  
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.io.BufferedReader;
import java.io.File;  
import java.io.FileOutputStream;
import java.io.InputStream;  
import java.io.IOException;  
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Vector;  

//import java.util.logging.Level;
//import java.util.logging.Logger;
  
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;  
import net.jxta.document.Advertisement;  
  
import net.jxta.peergroup.PeerGroup;  
import net.jxta.peergroup.PeerGroupFactory;  
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.exception.PeerGroupException;  
  
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.impl.peergroup.Platform;  
import net.jxta.impl.peergroup.GenericPeerGroup;  
  
import net.jxta.share.*;  
import net.jxta.share.client.*;  
import net.jxta.share.metadata.*;
import net.jxta.socket.JxtaServerSocket;
import net.jxta.socket.JxtaSocket;
  
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
    
    private Mutex mutex;
    
    KeyPair pair;
    X509Certificate cert;
    
    //logger de log4j
    //usar un flag para para prender o apagar el guardado de estado con if , 
    //si es falso no enviar  al alog , hacer log cuando es true
    static final Logger logger = Logger.getLogger(Main.class);
    boolean traza= false;
    
    static Date Hora;
    
        

    static public void main(String args[]) {  

    	//start DownloadDemo  
    	//configuracion inicial de log4j    	
    	BasicConfigurator.configure();
    	HTMLLayout layout = new HTMLLayout();
    	//Writter Appender para crear archivo de traza
    	WriterAppender appender = null;
        try {
           FileOutputStream output = new FileOutputStream("LogJXTA.html");
           appender = new WriterAppender(layout,output);
        } catch(Exception e) {}

        logger.addAppender(appender);
        logger.setLevel((Level) Level.INFO);

        Hora=timeMarker();
        logger.info(Hora+"Inciciando Applicacion");
        
         
        
    	new Main();  
    }  
  
    public Main() {  
    	
    	/* 1. generar la llave publica, la llave privada, certificado */
    	generadorCertificado gc = new generadorCertificado();
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        try{
	        pair = gc.generateRSAKeyPair();
	        cert = gc.generateV3Certificate(pair);
	        cert.checkValidity(new Date());
	        cert.verify(cert.getPublicKey());
        }
        catch(Exception ex){
        	ex.printStackTrace();
        }
        
    startJxta();  
    
    mutex = new Mutex();
    
    SearchWindow window = new SearchWindow();  
    window.setVisible(true);  
    }  
  
    //Marca de tiempo
    public static Date timeMarker () {
    	
    	Date time = null;
    	try
    	{
    	NTPUDPClient timeClient = new NTPUDPClient();
    	InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
    	TimeInfo timeInfo = timeClient.getTime(inetAddress);
    	long returnTime = timeInfo.getReturnTime();
    	time = new Date(returnTime);
    	System.out.println("Time from " + TIME_SERVER + ": " + time);
    	logger.info("timeMark:"+time);
    	}
    	catch(Exception ex)
    	{
    		System.out.println("Failed to get UTP time"); 
    	}
		return (time);
	}
    
    
    /** 
     * initializes NetPeerGroup and the CMS 
     */  
    
    private void startJxta() {  
    try {  
    	//log
        logger.info("Inciciando JXTA APP3");
        
    	// create, and Start the default jxta NetPeerGroup  
        netPeerGroup = PeerGroupFactory.newNetPeerGroup();  
          
        //uncomment the following line if you want to start the app defined  
        // the NetPeerGroup Advertisement (by default it's the shell)  
        // in this case we want use jxta directly.  
          
        // netPeerGroup.startApp(null);
        
      //instanciate and initialize a content management service for   
        //the NetPeerGroup  
      //log
        logger.info("Creando el CMS");
        
        cms = new CMS();  
        cms.init(netPeerGroup, null, null);  
        logger.info("NetPeerGroup: "+netPeerGroup);
  
        //set up a MetadataShareDemo directory inside the JXTA_HOME dir  
        String homedir = System.getProperty("JXTA_HOME");  
        homedir = (homedir != null) ? homedir + "DownloadCache"   
        : "DownloadCache";  
          
        //start CMS, creating a directory named ShareDemo to store the  
        // ContentAdvertisement cache in.  
        if(cms.startApp(new File(homedir)) == -1) {  
        System.out.println("CMS initialization failed");  
        System.exit(-1);
      //log
        logger.debug("CMS Fallo en creación de CMS");
        
        }
              
      //log
        Hora=timeMarker();
        logger.debug("CMS creado"+Hora);
        
        
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
    Button stateButton;
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

        //GUARDADR ESTADO
        stateButton = new Button("save State");  
        stateButton.addActionListener(this);  
        toolbar.add(stateButton);  

        
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
              //comparte
            try {  
            //ContentManager.share() will share and advertise a  
            // file using a ContentAdvertisement containing the  
            // metadata that was just created. Passing in nulls for  
            // the name and content type will cause the content to  
            // be advertised under the name of the file prefix, and  
            // advertised as being the content type that is  
            // determined by this ContentManager's getMimeType()  
            // function.  
            
            	//proyecto 2 archivo sin cifrar
            	//cms.getContentManager().share(file, description);  
            	
            	//proyecto 3 archivo cifrado
            	/*2. al subir el archivo firmado lo que subo es:
            	un archivo txt que adentro tiene los bytes cifrados con mi llave publica
            	la descripcion siempre es:
            	nombre.extension - String que indica el mensaje a cifrar para el digest*/
                
            	PublicKeyCryptography pkc = new PublicKeyCryptography();
            	ArchivoCifrado arch = pkc.cifrarArchivo(file, cert, pair);
            	description += file.getName()+"-"+ netPeerGroup.getPeerID() +"-"+arch.getDescripcion()+"-"+arch.getArrayCipherText();
            	
            	cms.getContentManager().share(arch.getArchivo(), description);
            	
            	logger.info("Descripcion del archivo subido" + description);
            
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
        else if(e.getSource().equals(stateButton))
        {
        	Publicador();
        	 
        }
        else if (e.getSource().equals(downloadButton)) 
        {  
  
        //figure out which content advertisement is selected  
        int selectedIndex = resultList.getSelectedIndex();
        
        logger.info("results: " + results);
        
        if((results != null) && (selectedIndex != -1)  
           && (results[selectedIndex] != null)) {  
              
            JFileChooser saveDialog = new JFileChooser();  
            saveDialog.setLocation(300, 200);  
  
            //set the default save path to the name of the content  
            File savePath  
            = new File(results[selectedIndex].getName());  
            
            System.out.println("path " + results[selectedIndex].getName());
            
            saveDialog.setSelectedFile(savePath);  
            int returnVal = saveDialog.showSaveDialog(this);  
            if (returnVal == JFileChooser.APPROVE_OPTION) {  
            savePath = saveDialog.getSelectedFile();  
              
            //start up a GetContentRequest for the selected content  
            //advertisement.
            
            mutex.lock();
            
            //TODO: mensajes para solicitud de archivo
            //proyecto3. encio de mensajes
            /*3. cuando se piden un archivo se mandan:
            	certificado 509x de quien me lo pide puede venir en un archivo file.txt
            	la descripcion siempre es:
            	S - el nombre.extension - id de quien lo pide - id del dueño
            */
            
            /*Convertir el certificado en un archivo*/
            File archivo = null;
            try {
                // Get the encoded form which is suitable for exporting
                byte[] buf = cert.getEncoded();
                archivo = new File("certificado.txt");
                
                FileOutputStream os = new FileOutputStream(archivo);
               
                    // Write in text form
                    Writer wr = new OutputStreamWriter(os, Charset.forName("UTF-8"));
                    wr.write("-----BEGIN CERTIFICATE-----\n");
                    wr.write(new sun.misc.BASE64Encoder().encode(buf));
                    wr.write("\n-----END CERTIFICATE-----\n");
                    wr.flush();
                
                os.close();
                
                
                //sacar el id del dueño de la descripcion
                String des = results[selectedIndex].getDescription();
                
                String[] partes = des.split("-");
                logger.info("Descripcion de lo que seleccione" + des);
                logger.info("partes" + partes);
                
                String description = "";
               // description += "S-" + results[selectedIndex].getName() +"-"+netPeerGroup.getPeerID().toString()+"-"+partes[2];
            	
            	//cms.getContentManager().share(archivo, description); 
            
            	logger.info("Descripcion del mensaje de solicitud" + description);
                
            } catch (Exception ex) {
            	logger.info("error en el mensaje de solicitud");
                
            	ex.printStackTrace();
            }

            
            
            
            //Proyecto 2 bajar el archivo directamente
            
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
            
            mutex.unlock();
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
        	System.out.println(results[i].getDescription());
        	if(bySize == false)
        	{
        		if(resultQuery !=null && !resultQuery.equals(""))
            	{
            		String description = results[i].getDescription();
                	if(description != null)
                	{
                		description.trim();
                		if (description.equals("guardarEstado")){
                			System.out.println("aqui es donde debo guardar el estado");
                			
                		}else
                		{		
                			//TODO: leer si el mensaje empieza con S- y si de un archivo MIO
                			//si es un mensaje de solicitud de archivo
                			// bajo el archivo que me pidieron
                			//descifro el archivo
                			// lo cifro con el certificado que me mandan
                			// envio tres mensajes 
                			//EC- (envio de mi certificado), 
                			//EB-Envio de los bytes cifrados, 
                			//EF- envio del archivo como tal
                			
                			//TODO: leer si el mensaje es un EC- EB- EF- y si yo soy el Destinatario
                			//si es un mensaje de entrega de archivo
                			// Tengo que poder leer los tres mensajes al tiempo o sino no sirve
                			// primero leo el certificado del que me lo envio
                			// luego leo los bytes cifrados
                			// luego el archivo cifrado
                			// con estos 3 descifro y armo el archivo
                			// saco un box para guardar
                			
	                    	//si es un archivo publicado
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
            			resultList.add(results[i].getName() + " " + 

results[i].getDescription());            			
            		}
        			
        		}
        		else
        		{
        			resultList.add(results[i].getName() + " " + 

results[i].getDescription());
        		}
        	}
        }  
    }
  
 //metodos para publicacion de advertisement de log
    
    private void Publicador(){
    	try {  
            //ContentManager.share() will share and advertise a  
            // file using a ContentAdvertisement containing the  
            // metadata that was just created. Passing in nulls for  
            // the name and content type will cause the content to  
            // be advertised under the name of the file prefix, and  
            // advertised as being the content type that is  
            // determined by this ContentManager's getMimeType()  
            // function.  
            cms.getContentManager().share(null, "guardarEstado");  
            
            //update the list of shared content  
    
            updateLocalFiles();  
            
            } catch (IOException ex) {  
            System.out.println("Share command failed.");  
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
  
    
    
    //fin metodos publicador
    
    class WindowMonitor extends WindowAdapter {  
    public void windowClosing(WindowEvent e) {  
        Window w = e.getWindow();  
        w.setVisible(false);  
        w.dispose();  
        System.exit(0);  
    }  
    }  
}  