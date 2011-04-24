package co.edu.unaindes.sd.seguridad;

import java.io.*;
import javax.net.ssl.*;
import java.io.*;
import java.net.*;

public class AutorizarEntrada 
{
	int port;
	String host;
	String cadena;
	
	public AutorizarEntrada(String ht, String user, String pass)
	{
		port = 2500;
		host = ht;
		cadena = user + "," + pass;		
	}
	
	public String authorize()
	{
		try 
		{ 
			System.out.println("Inicio del cliente");
			SSLSocketFactory sslFact = (SSLSocketFactory)SSLSocketFactory.getDefault();
			SSLSocket s = (SSLSocket) sslFact.createSocket(host, port);
			
			//Cipher
			final String[] enabledCipherSuites = { "SSL_DH_anon_WITH_RC4_128_MD5" };
			s.setEnabledCipherSuites(enabledCipherSuites);
			
			
			OutputStream os = s.getOutputStream();
			PrintWriter out = new PrintWriter(os, true);
			InputStream is = s.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			out.println(cadena) ;
			cadena = in.readLine();
			System.out.println("Se leyo del socket: " +cadena);
			in.close();
			out.close();
			s.close();
		}
		catch(IOException e) 
		{ 
			System.out.println("Error de lectura: " + e.getMessage());
		}
		
		return cadena;
		
	}
	
	/*
	public static void main(String args[] ) 
	{
		int port = 2500;
		String host = "192.168.0.15";
		String cadena;
		try 
		{ 
			System.out.println("Inicio del cliente");
			SSLSocketFactory sslFact = (SSLSocketFactory)SSLSocketFactory.getDefault();
			SSLSocket s = (SSLSocket) sslFact.createSocket(host, port);
			
			//Cipher
			final String[] enabledCipherSuites = { "SSL_DH_anon_WITH_RC4_128_MD5" };
			s.setEnabledCipherSuites(enabledCipherSuites);
			
			
			OutputStream os = s.getOutputStream();
			PrintWriter out = new PrintWriter(os, true);
			InputStream is = s.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			out.println("Nicolas,44ni55co") ;
			cadena = in.readLine();
			System.out.println("Se leyo del socket: " +cadena);
			in.close();
			out.close();
			s.close();
		}
		catch(IOException e) 
		{ 
			System.out.println("Error de lectura: " + e.getMessage());
		}
	}*/
}