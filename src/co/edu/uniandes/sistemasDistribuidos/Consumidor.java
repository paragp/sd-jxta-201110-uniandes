/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package co.edu.uniandes.sistemasDistribuidos;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaSocket;

/**
 *
 * @author asistente
 */
public class Consumidor implements DiscoveryListener {

    private DiscoveryService discovery;
    private PeerGroup netPeerGroup;

    public Consumidor() {

        //Inicializa Jxta
        NetworkManager manager = null;
        try {
            manager = new NetworkManager(NetworkManager.ConfigMode.ADHOC, "Consumidor", new File(new File(".cache"), "Consumidor").toURI());

            manager.startNetwork();
        } catch (Exception ex) {
            Logger.getLogger(Publicador.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Registra el advertisement
        AdvertisementFactory.registerAdvertisementInstance(AdvertisementEjemplo.getAdvertisementType(), new AdvertEjemplo.Instantiator());

        netPeerGroup = manager.getNetPeerGroup();
        discovery = netPeerGroup.getDiscoveryService();
    }

    public void empezar() {

        System.out.println("Buscar advertisements");
        discovery.getRemoteAdvertisements(
                null, // no specific peer (propagate)
                DiscoveryService.ADV, // Adv type
                AdvertEjemplo.nombreAdvertisementTag, // Attribute = nombreAdvertisement
                "*j*", // busca advertisements que contengan j en el nombre
                3, // numero de advertisements que va a buscar
                this);//El listener

    }

    public void discoveryEvent(DiscoveryEvent de) {
        System.out.println("Encontro advertisements");
        Enumeration<Advertisement> respuestas = de.getSearchResults();
        while (respuestas.hasMoreElements()) {

            Object advertTemp = respuestas.nextElement();
            if (advertTemp instanceof AdvertisementEjemplo) {

                AdvertisementEjemplo advertEjemplo = (AdvertisementEjemplo) advertTemp;
                String nombre = advertEjemplo.getNombreAdvertisement();

                if (nombre != null) {
                    System.out.println("Nombre del advertisement: "+nombre);
                    PipeAdvertisement pipeAdv = advertEjemplo.getPipeAdv();
                    System.out.println("Id del Pipe encontrado: "+pipeAdv.getID().toString());
                    enviarMensaje(pipeAdv);
                }
            }
        }
    }

    public void enviarMensaje(PipeAdvertisement pipeAdv) {
        try {
            JxtaSocket socket = new JxtaSocket(netPeerGroup, pipeAdv);

            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println("Hola publicador");

            out.close();
            socket.close();
        } catch (IOException ex) {
            System.out.println("El advertisement ha expirado");
        }
    }

    public static void main(String[] args) {
        Consumidor c = new Consumidor();
        c.empezar();
    }
}
