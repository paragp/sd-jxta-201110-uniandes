package co.edu.uniandes.Servidor;



import java.net.InetAddress;
import java.util.Date;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

public class TimeLookUp {

	 // List of time servers: http://tf.nist.gov/service/time-servers.html
	 // Do not query time server more than once every 4 seconds
	 public static final String TIME_SERVER = "time-a.nist.gov" ;
	 

	 public static void main(String[] args) throws Exception {
		 NTPUDPClient timeClient = new NTPUDPClient();
		 InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
		 TimeInfo timeInfo = timeClient.getTime(inetAddress);
		 long returnTime = timeInfo.getReturnTime();
		 Date time = new Date(returnTime);
		 System.out.println( "Time from " + TIME_SERVER + ": " + time);
	 }


	
	
}
