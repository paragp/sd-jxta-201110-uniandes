/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package co.edu.uniandes.sistemasDistribuidos;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.AdvertisementFactory;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaServerSocket;
import net.jxta.socket.JxtaSocket;

/**
 *
 * @author asistente
 */
public class Publicador{

    //Es el ID usado para el pipe. Estos IDs pueden ser generados usando el metodo newPipeID de la clase IDFactory de JXTA
    public final static String SOCKET_ID = "urn:jxta:uuid-59616261646162614E5047205032503393B5C2F6CA7A41FBB0F890173088E79404";

    private JxtaServerSocket mySocketPipe;
    
    public Publicador(){

        //Inicializa Jxta
        NetworkManager manager = null;
        try {
            manager = new NetworkManager(NetworkManager.ConfigMode.ADHOC, "Publicador", new File(new File(".cache"), "Publicador").toURI());

            manager.startNetwork();
        } catch (Exception ex) {
            Logger.getLogger(Publicador.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Registra el advertisement
        AdvertisementFactory.registerAdvertisementInstance(AdvertisementEjemplo.getAdvertisementType(),new AdvertEjemplo.Instantiator());

        //Inicializa los servicios
        PeerGroup netPeerGroup = manager.getNetPeerGroup();
        DiscoveryService discovery = netPeerGroup.getDiscoveryService();

        //Crea y publica un pipe advertisement
        PeerGroupID id = netPeerGroup.getPeerGroupID();
        PipeID idPipe = createNewPipeID(id);
        PipeAdvertisement pipeAdv = createPipeAdvertisement(idPipe);
        System.out.println("Id del Pipe: "+pipeAdv.getID().toString());

        AdvertisementEjemplo advertEjemplo = new AdvertEjemplo();
        advertEjemplo.setNombreAdvertisement("ejemplo");
        advertEjemplo.setPipeAdv(pipeAdv);

        AdvertisementEjemplo advertEjemplo2 = new AdvertEjemplo();
        advertEjemplo2.setNombreAdvertisement("prueba1");
        advertEjemplo2.setPipeAdv(pipeAdv);

        AdvertisementEjemplo advertEjemplo3 = new AdvertEjemplo();
        advertEjemplo3.setNombreAdvertisement("prueba2");
        advertEjemplo3.setPipeAdv(pipeAdv);
        try {
            discovery.publish(advertEjemplo);
            discovery.publish(advertEjemplo2);
            discovery.publish(advertEjemplo3);
        } catch (IOException ex) {
            Logger.getLogger(Publicador.class.getName()).log(Level.SEVERE, null, ex);
        }
        


        //Crea el server socket por el cual va a recibir conexiones
        try {
            mySocketPipe = new JxtaServerSocket(netPeerGroup, pipeAdv);
        } catch (Exception ex) {
            Logger.getLogger(Publicador.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void empezar(){
        try {
            JxtaSocket socket = (JxtaSocket) mySocketPipe.accept();
            System.out.println("Acepto una conexion");
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println(input.readLine());

            input.close();
            socket.close();
        } catch (Exception ex) {
            System.out.println("Error al conectarse");
        }
    }

    private static PipeID createNewPipeID(PeerGroupID pgID) {
        PipeID socketID = null;

        try {
            socketID = (PipeID) IDFactory.fromURI(new URI(SOCKET_ID));
        } catch (URISyntaxException ex) {

        }
        System.out.println("El Socket Id es: "+SOCKET_ID.toString());
        return socketID;
    }

    private PipeAdvertisement createPipeAdvertisement(ID pipeId) {

        PipeAdvertisement advertisement = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());

        advertisement.setPipeID(pipeId);
        advertisement.setType(PipeService.UnicastType);
        advertisement.setName("Pipe comunicacion");
        return advertisement;
    }

    public static void main(String[] args) {
        Publicador p = new Publicador();
        p.empezar();
    }
}
