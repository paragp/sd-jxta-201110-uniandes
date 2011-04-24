package co.edu.unaindes.sd.seguridad;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

import javax.net.ssl.*;

public class ServidorEntrada 
{
	int port;
	SSLServerSocket s;
	String cadena;
	ArrayList<String> users;
	ArrayList<String> pass;
		
	public ServidorEntrada()
	{
		port = 2500;
		users = new ArrayList<String>();
		pass = new ArrayList<String>();
		
		try
		{
			
			BufferedReader in = new BufferedReader(new FileReader("data/nothing.txt"));
			String line = in.readLine();
			if(line.split(":")[0].equals("Users"))
			{
				String[] user = line.split(":")[1].split(",");
				for(int i = 0; i < user.length; i++)
				{
					users.add(user[i]);
				}
			}
			else
			{
				throw new Exception("Error reading from users");
			}
			line = in.readLine();
			if(line.split(":")[0].equals("Passwords"))
			{
				String[] pas = line.split(":")[1].split(",");
				for(int i = 0; i < pas.length; i++)
				{
					pass.add(pas[i]);
				}
			}
			else
			{
				throw new Exception("Error reading from passwords");
			}
	        
	        
	        in.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}		
	}
	
	public void run()
	{
		while(true)
		{
			try 
			{
				System.out.println("Inicio del servidor");
				SSLServerSocketFactory sslSrvFact =
				(SSLServerSocketFactory)
				SSLServerSocketFactory.getDefault();
				s =(SSLServerSocket)sslSrvFact.createServerSocket(port);
				
				//Cipher
				final String[] enabledCipherSuites = { "SSL_DH_anon_WITH_RC4_128_MD5" };
				s.setEnabledCipherSuites(enabledCipherSuites);
				
				
				SSLSocket c = (SSLSocket)s.accept();
				OutputStream os = c.getOutputStream();
				PrintWriter out = new PrintWriter(os, true);
				InputStream is = c.getInputStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(is));
				cadena = in.readLine();
				System.out.println(cadena);
				boolean succ = login(cadena);
				if(succ)
				{
					out.println("Accepted");
				}
				else
				{
					out.println("Rejected");
				}
				
				in.close();
				out.close();
				s.close();
			}
			catch(IOException e)
			{
				System.out.println("Error de lectura: " + e.getMessage());
			}
			
		}	
		
	}
	
	public boolean login(String cadena)
	{
		boolean log = false;
				
		String[] cad = cadena.split(",");
		for(int i = 0; i < users.size() && !log; i ++)
		{
			if(users.get(i).equals(cad[0])&& pass.get(i).equals(cad[1]))
			{
				log = true;				
			}				
		}
		
		return log;
	}
	
	public static void main(String args[] ) 
	{
		ServidorEntrada serv = new ServidorEntrada();
		
		serv.run();		
	}
}
