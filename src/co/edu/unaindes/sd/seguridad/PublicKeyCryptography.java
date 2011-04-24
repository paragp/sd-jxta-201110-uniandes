package co.edu.unaindes.sd.seguridad;

import java.io.File;
import java.security.*;
import java.security.cert.*;
import javax.crypto.*;
import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;


/**
 * 
 * @author Joe Prasanna Kumar
 *  * @ Modificado por Javier Soto para Sistemas Distribuidos Uniandes 2011
 * 1. Encrypt the data using a Symmetric Key
 * 2. Encrypt the Symmetric key using the Receivers public key
 * 3. Create a Message Digest of the data to be transmitted
 * 4. Sign the message to be transmitted
 * 5. Send the data over to an unsecured channel
 * 6. Validate the Signature
 * 7. Decrypt the message using Recv private Key to get the Symmetric Key
 * 8. Decrypt the data using the Symmetric Key
 * 9. Compute MessageDigest of data + Signed message
 * 10.Validate if the Message Digest of the Decrypted Text matches the Message Digest of the Original Message
 * 
 * 
 */

public class PublicKeyCryptography {

	/**
	 * @param args
	 */
	
	public ArchivoCifrado cifrarArchivo(File archivo, X509Certificate cert, KeyPair claves){
		
		FileToArray fta = new FileToArray();
		
		SymmetricEncrypt encryptUtil = new SymmetricEncrypt();
		byte[] byteDataToTransmit = fta.ArchivoAArreglo(archivo);

		// Generating a SecretKey for Symmetric Encryption
		SecretKey senderSecretKey = SymmetricEncrypt.getSecret();
		
		//1. Encrypt the data using a Symmetric Key
		byte[] byteCipherText = encryptUtil.encryptData(byteDataToTransmit,senderSecretKey,"AES");
		String strCipherText = new BASE64Encoder().encode(byteCipherText);
		
		
		//2. Encrypt the Symmetric key using the Receivers public key
		try{
	    
			// 2.2 Creating an X509 Certificate of the Receiver
		    X509Certificate recvcert ;
		    MessageDigest md = MessageDigest.getInstance("MD5");
		    recvcert = cert;
		    // 2.3 Getting the Receivers public Key from the Certificate
		    PublicKey pubKeyReceiver = recvcert.getPublicKey();
		    
		    // 2.4 Encrypting the SecretKey with the Receivers public Key
		    byte[] byteEncryptWithPublicKey = encryptUtil.encryptData(senderSecretKey.getEncoded(),pubKeyReceiver,"RSA/ECB/PKCS1Padding");
		    String strSenbyteEncryptWithPublicKey = new BASE64Encoder().encode(byteEncryptWithPublicKey);
		        
		    // 3. Create a Message Digest of the Data to be transmitted
		    md.update(byteDataToTransmit);
			byte byteMDofDataToTransmit[] = md.digest();
			
			String strMDofDataToTransmit = new String();
			for (int i = 0; i < byteMDofDataToTransmit.length; i++){
				strMDofDataToTransmit = strMDofDataToTransmit + Integer.toHexString((int)byteMDofDataToTransmit[i] & 0xFF) ;
		             }
			
		    // 3.1 Message to be Signed = Encrypted Secret Key + MAC of the data to be transmitted
			String strMsgToSign = strSenbyteEncryptWithPublicKey + "|" + strMDofDataToTransmit;
		    
		    // 4. Sign the message
		    char[] keypassword = "send123".toCharArray();
		    PrivateKey myPrivateKey = claves.getPrivate();
		    
		    // 4.2 Sign the message
		    Signature mySign = Signature.getInstance("MD5withRSA");
		    mySign.initSign(myPrivateKey);
		    mySign.update(strMsgToSign.getBytes());
		    byte[] byteSignedData = mySign.sign();
		    
		 // 5. The Values byteSignedData (the signature) and strMsgToSign (the data which was signed) can be sent across to the receiver
			
		    File archivoFinal;
		    String descripcion;
		    
		    archivoFinal = fta.ArregloAArchivo(byteSignedData, archivo.getName() + ".txt" );
		    descripcion = archivo.getName() + "-" + strMsgToSign;
		    
		    return new ArchivoCifrado(archivoFinal,descripcion,new String(byteCipherText ));
		}
	    catch(Exception exp)
		{
			System.out.println(" Exception caught " + exp);
			exp.printStackTrace();
			return null;
		}

	}
	
	public File descifrarArchivo(File archivo, X509Certificate certSender, KeyPair claves, String strMsgToSign, String byteCipherText){
	
		FileToArray fta = new FileToArray();
		byte[] array = fta.ArchivoAArreglo(archivo);
		SymmetricEncrypt encryptUtil = new SymmetricEncrypt();
		SecretKey senderSecretKey = SymmetricEncrypt.getSecret();
		byte[] byteEncryptWithPublicKey = encryptUtil.encryptData(senderSecretKey.getEncoded(),claves.getPublic(),"RSA/ECB/PKCS1Padding");
	    String strSenbyteEncryptWithPublicKey = new BASE64Encoder().encode(byteEncryptWithPublicKey);
	    
		
		
		try{
			
			// 6.Validate the Signature
		    // 6.1 Extracting the Senders public Key from his certificate
			X509Certificate sendercert ;
			sendercert = certSender;
		    PublicKey pubKeySender = sendercert.getPublicKey();
		    
		    // 6.2 Verifying the Signature
		    Signature myVerifySign = Signature.getInstance("MD5withRSA");
		    myVerifySign.initVerify(pubKeySender);
		    myVerifySign.update(strMsgToSign.getBytes());
		    
		    boolean verifySign = myVerifySign.verify(array);
		    if (verifySign == false)
		    {
		    	System.out.println(" Error validando la Firma del Archivo ");
		    }
		    else
		    {
		    	System.out.println(" Firma Validada Correctamente ");
		    }
		    
		    // 7. Decrypt the message using Recv private Key to get the Symmetric Key
		    PrivateKey recvPrivateKey = claves.getPrivate();
		    
		    // Parsing the MessageDigest and the encrypted value
		    String strRecvSignedData = new String (array);
		    String[] strRecvSignedDataArray = new String [10];
		    strRecvSignedDataArray = strMsgToSign.split("|");
		    int intindexofsep = strMsgToSign.indexOf("|");
		    String strEncryptWithPublicKey = strMsgToSign.substring(0,intindexofsep);
		    String strHashOfData = strMsgToSign.substring(intindexofsep+1);

		    // Decrypting to get the symmetric key
		    byte[] bytestrEncryptWithPublicKey = new BASE64Decoder().decodeBuffer(strEncryptWithPublicKey);
		    byte[] byteDecryptWithPrivateKey = encryptUtil.decryptData(byteEncryptWithPublicKey,recvPrivateKey,"RSA/ECB/PKCS1Padding");
		    
		    // 8. Decrypt the data using the Symmetric Key
		    javax.crypto.spec.SecretKeySpec secretKeySpecDecrypted = new javax.crypto.spec.SecretKeySpec(byteDecryptWithPrivateKey,"AES");
		    byte[] byteDecryptText = encryptUtil.decryptData(byteCipherText.getBytes(),secretKeySpecDecrypted,"AES");
		    String strDecryptedText = new String(byteDecryptText);
		    System.out.println(" Decrypted data is " +strDecryptedText);
		    
		    // 9. Compute MessageDigest of data + Signed message
		    MessageDigest recvmd = MessageDigest.getInstance("MD5");
		    recvmd.update(byteDecryptText);
			byte byteHashOfRecvSignedData[] = recvmd.digest();

			String strHashOfRecvSignedData = new String();
				
			for (int i = 0; i < byteHashOfRecvSignedData.length; i++){
				strHashOfRecvSignedData = strHashOfRecvSignedData + Integer.toHexString((int)byteHashOfRecvSignedData[i] & 0xFF) ;
		             }
			// 10. Validate if the Message Digest of the Decrypted Text matches the Message Digest of the Original Message
			if (!strHashOfRecvSignedData.equals(strHashOfData))
			{
				System.out.println(" El mensaje fue modificado ");
			}
			
			File fil = fta.ArregloAArchivo(byteDecryptText, "descifrado.txt");
			return fil;
		}
		catch(Exception exp)
		{
			System.out.println(" Exception " + exp);
			exp.printStackTrace();
			return null;
		}
			

	}
	
}
