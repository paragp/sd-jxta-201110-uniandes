package co.edu.unaindes.sd.seguridad;

import java.io.File;

public class ArchivoCifrado {
	
	File archivo; //archivo txt cifrado
	String descripcion; // lo que va en la descripcion del archivo
	byte[] arrayCipherText; //arreglo con mensaje cifrado con llave simetrica
	
	public ArchivoCifrado(File archivo, String descripcion, byte[] arrayCipherText) {
		super();
		this.archivo = archivo;
		this.descripcion = descripcion;
		this.arrayCipherText = arrayCipherText;
	}

	public byte[] getArrayCipherText() {
		return arrayCipherText;
	}

	public void setArrayCipherText(byte[] arrayCipherText) {
		this.arrayCipherText = arrayCipherText;
	}

	public File getArchivo() {
		return archivo;
	}

	public void setArchivo(File archivo) {
		this.archivo = archivo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	
	

}
