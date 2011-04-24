package co.edu.unaindes.sd.seguridad;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
 
public class FileToArray {
	
	public File ArregloAArchivo(byte[] array, String nombre){
		File archivo = new File(nombre);
        
		try{
			FileOutputStream fos = new FileOutputStream(archivo);
	        fos.write(array);
	        fos.flush();
	        fos.close();
	        
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
		return archivo;
		
	}
	
	public byte[] ArchivoAArreglo(File archivo){
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        try {
        	FileInputStream fis = new FileInputStream(archivo);
            
    		byte[] buf = new byte[(int)archivo.length()];
            
            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum); //no doubt here is 0
                //Writes len bytes from the specified byte array starting at offset off to this byte array output stream.
                System.out.println("read " + readNum + " bytes,");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        byte[] bytes = bos.toByteArray();
        return bytes;
	}
 
    
}
