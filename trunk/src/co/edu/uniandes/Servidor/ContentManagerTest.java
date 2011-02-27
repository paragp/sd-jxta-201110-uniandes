package co.edu.uniandes.Servidor;

import net.jxta.share.ContentManager;  
import net.jxta.share.ContentManagerImpl;  
import net.jxta.share.Content;  
import net.jxta.share.FileContent;  
import net.jxta.share.ContentAdvertisement;  
import net.jxta.share.ContentId;  
import net.jxta.share.ContentIdImpl;  
import net.jxta.document.Document;  
import net.jxta.document.MimeMediaType;  
  
import java.io.File;  
import java.io.FileInputStream;  
import java.io.FileOutputStream;  
  
public class ContentManagerTest {  
    private String[] args;  
  
    public static void main(String[] args) throws Throwable {  
    ContentManagerTest cmt = new ContentManagerTest(args);  
    cmt.run();  
    }  
  
    ContentManagerTest(String[] args) {  
    this.args = args;  
    }  
  
    public void run() throws Throwable {  
    File dir = new File(args[0]);  
    if (!dir.isDirectory()) {  
        p("Directory does not exist: " + dir);  
        return;  
    }  
    String[] l = dir.list();  
    for (int i = 0; i < l.length; i++) {  
        new File(dir, l[i]).delete();  
    }  
    ContentManager cm = new ContentManagerImpl(dir);  
    for (int i = 1; i < args.length; i++) {  
        File f = new File(args[i]);  
        FileContent fc = cm.share(f);  
        p("Sharing content: " + fc);  
    }  
    p("Restoring content");  
    cm = new ContentManagerImpl(dir);  
    Content[] cs = cm.getContent();  
    for (int i = 0; i < cs.length; i++) {  
        p("Shared content: " + cs[i]);  
        ContentAdvertisement ca = cs[i].getContentAdvertisement();  
        Document d = (Document)  
        ca.getDocument(new MimeMediaType("text/xml"));  
        d.sendToStream(System.out);  
    }  
    ContentAdvertisement ca = cs[0].getContentAdvertisement();  
    ContentId id = ca.getContentId();  
    String s = id.toString();  
    ContentId id2 = new ContentIdImpl(s);  
    if (!id.equals(id2)) {  
        p("ContentId equality test failed:");  
        p("    id1 = " + id);  
        p("    id2 = " + id2);  
    }  
    }  
  
    private static void p(String s) {  
    System.out.println(s);  
    }  
}  