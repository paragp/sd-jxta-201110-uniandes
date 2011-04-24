package co.edu.unaindes.sd.seguridad;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.X509V3CertificateGenerator;

public class generadorCertificado {
	
  public X509Certificate generateV3Certificate(KeyPair pair) throws InvalidKeyException,
      NoSuchProviderException, SignatureException {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

    X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

    certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
    certGen.setIssuerDN(new X500Principal("CN=JXTA Certificate"));
    certGen.setNotBefore(new Date(System.currentTimeMillis() - 10000));
    certGen.setNotAfter(new Date(System.currentTimeMillis() + 10000));
    certGen.setSubjectDN(new X500Principal("CN=JXTA Certificate"));
    certGen.setPublicKey(pair.getPublic());
    certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

    certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
    certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature
        | KeyUsage.keyEncipherment));
    certGen.addExtension(X509Extensions.ExtendedKeyUsage, true, new ExtendedKeyUsage(
        KeyPurposeId.id_kp_serverAuth));

    certGen.addExtension(X509Extensions.SubjectAlternativeName, false, new GeneralNames(
        new GeneralName(GeneralName.rfc822Name, "je.soto40@uniandes.edu.co")));

    return certGen.generateX509Certificate(pair.getPrivate(), "BC");
  }

  public KeyPair generateRSAKeyPair() throws Exception {
    KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA", "BC");
    kpGen.initialize(1024, new SecureRandom());
    return kpGen.generateKeyPair();
  }
}