/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package co.edu.uniandes.sistemasDistribuidos;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Document;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredDocumentUtils;
import net.jxta.document.StructuredTextDocument;
import net.jxta.document.TextElement;
import net.jxta.protocol.PipeAdvertisement;

/**
 *
 * @author asistente
 */
public class AdvertEjemplo extends AdvertisementEjemplo{

    private final static String mimeType= "text/xml";


    public static final String nombreAdvertisementTag = "NombreRecurso";
    public static final String pipeAdvertisementTag="IdRecurso";

    /**
     * Indexable fields.  Advertisements must define the indexables, in order
     * to properly index and retrieve these advertisements locally and on the
     * network
     */
    private final static String[] fields = {nombreAdvertisementTag};

    public AdvertEjemplo(InputStream  stream) throws IOException{
        super();

        StructuredTextDocument document= (StructuredTextDocument) StructuredDocumentFactory.newStructuredDocument( new MimeMediaType(mimeType), stream);
        readAdvertisement(document);
    }

    public AdvertEjemplo(Element document)
    {
        super();
        readAdvertisement((TextElement)document);
    }

    public Document getDocument(MimeMediaType asMimeType) throws IllegalArgumentException
    {
        if((null!= getNombreAdvertisement()) && (null!= getPipeAdv()))
        {
            StructuredDocument document= (StructuredDocument) StructuredDocumentFactory.newStructuredDocument(asMimeType, getAdvertisementType());
            Element element;

            //añadir hijos al documento:

            element= document.createElement(nombreAdvertisementTag, getNombreAdvertisement());
            document.appendChild(element);

            PipeAdvertisement pipe = getPipeAdv();
            if(pipe!=null){
                StructuredTextDocument advDoc = (StructuredTextDocument) pipe.getDocument(asMimeType);
                StructuredDocumentUtils.copyElements(document, document, advDoc);
            }


            return document;
        }
        else
        {
            throw new IllegalArgumentException("faltan datos para hacer advert");
        }
    }
    public void readAdvertisement(TextElement document)throws IllegalArgumentException
    {
        if(document.getName().equals(getAdvertisementType()))
        {
            Enumeration elements= document.getChildren();

            while(elements.hasMoreElements())
            {
                TextElement element= (TextElement) elements.nextElement();

                if(element.getName().equals(PipeAdvertisement.getAdvertisementType()))
                {
                    PipeAdvertisement pipe = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(element);

                    setPipeAdv(pipe);
                    continue;
                }

                 if(element.getName().equals(nombreAdvertisementTag))
                {
                    setNombreAdvertisement(element.getTextValue());
                    continue;
                }
            }
        }
        else
        {
            throw new IllegalArgumentException("no es el advertisement de recurso");
        }
    }

    public String toString()
    {
        try {
            StringWriter out = new StringWriter();
            StructuredTextDocument doc = (StructuredTextDocument) getDocument(new MimeMediaType(mimeType));
            doc.sendToWriter(out);
            return out.toString();
        } catch (IOException ex) {
            Logger.getLogger(AdvertEjemplo.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }
    public AdvertEjemplo() {
       super();
    }

    @Override
    public String[] getIndexFields() {
        return fields;
    }

    public static class Instantiator implements AdvertisementFactory.Instantiator{

        public String getAdvertisementType() {
           return AdvertisementEjemplo.getAdvertisementType();
        }

        public Advertisement newInstance() {
            return new AdvertEjemplo();
        }

        public Advertisement newInstance(Element root) {
            return new AdvertEjemplo(root);
        }

    };
}
