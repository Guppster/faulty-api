package ntua.gr.XMLRPC;

import java.io.FileOutputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;

public class CertificateHandler {
	
	protected CertificateHandler(String aURL){
		try{
		testConnectionTo(aURL);
		}catch(Exception e){
			e.printStackTrace();
		}
		System.setProperty("javax.net.ssl.trustStore", "MyCacertsFile");
	}
	
	public void testConnectionTo(String aURL) throws Exception {
        URL destinationURL = new URL(aURL);
        HttpsURLConnection conn = (HttpsURLConnection) destinationURL.openConnection();
        conn.connect();
        Certificate[] certs = conn.getServerCertificates();
        System.out.println("nb = " + certs.length);
        int i = 1;
        for (Certificate cert : certs) {
            System.out.println("");
            System.out.println("");
            System.out.println("");
            System.out.println("################################################################");
            System.out.println("");
            System.out.println("");
            System.out.println("");
            System.out.println("Certificate is: " + cert);
            if(cert instanceof X509Certificate) {
                try {
                    ( (X509Certificate) cert).checkValidity();
                    System.out.println("Certificate is active for current date");
                    FileOutputStream os = new FileOutputStream("/home/sebastien/Bureau/myCert"+i);
                    i++;
                    os.write(cert.getEncoded());
                    os.close();
                } catch(CertificateExpiredException cee) {
                    System.out.println("Certificate is expired");
                }
            } else {
                System.out.println("Unknown certificate type: " + cert);
            }
        }
    }
	
}
