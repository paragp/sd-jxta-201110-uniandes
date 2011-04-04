package co.edu.uniandes.sd.proy02;

import net.jxta.document.Advertisement;  
import net.jxta.document.MimeMediaType;  
import java.awt.event.WindowAdapter;  
import java.awt.event.WindowEvent;  
import java.awt.Frame;  
import java.awt.TextArea;  
import java.awt.Window;  
import java.io.InputStream;  
import java.io.IOException;  
  
  
/** A window for viewing JXTA Advertisements 
 *@author $Author: hamada $ 
 */  
class AdvertisementViewer extends Frame {  
  
     TextArea text = new TextArea();  
  
     /** 
      * @param adv the advertisement to display 
      */  
     public AdvertisementViewer(Advertisement adv) {  
     super(adv.getClass().getName());  
     text.setEditable(false);  
     MimeMediaType mmt = new MimeMediaType("text/xml");  
     StringBuffer sb;  
     InputStream in;  
     try {  
         sb = new StringBuffer();  
         in = adv.getDocument(mmt).getStream();
         int a;  
         while((a = in.read()) != -1)  
         sb.append((char)a);  
         text.append(sb.toString());  
         in.close();  
     } catch(IOException ioe) {  
         text.append("error reading advertisement");  
     }  
     addWindowListener(new WindowAdapter() {  
         public void windowClosing(WindowEvent we) {  
             Window source = we.getWindow();  
             source.setVisible(false);  
             source.dispose();  
         }  
         });  
     add(text);  
     setSize(400, 400);  
     setVisible(true);  
     }    
}  