/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package co.edu.uniandes.sd.proy02;

import net.jxta.document.Advertisement;
import net.jxta.id.ID;
import net.jxta.protocol.PipeAdvertisement;

/**
 *
 * @author asistente
 */
public abstract class AdvertisementLog extends Advertisement{

    private final static String advertisementType = "AdvertisementsRecurso";
    private String nombreAdvertisement = null;
    private PipeAdvertisement pipeAdv = null;

    public static String getAdvertisementType() {
        return advertisementType;
    }

    public ID getID() {
        return ID.nullID;
    }

    public String getNombreAdvertisement() {
        return nombreAdvertisement;
    }

    public void setNombreAdvertisement(String nombreAdvertisement) {
        this.nombreAdvertisement = nombreAdvertisement;
    }

    public PipeAdvertisement getPipeAdv() {
        return pipeAdv;
    }

    public void setPipeAdv(PipeAdvertisement pipeAdv) {
        this.pipeAdv = pipeAdv;
    }

}
