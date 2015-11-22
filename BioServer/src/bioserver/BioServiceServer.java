/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

/**
 *
 * @author Claudio
 */
public class BioServiceServer implements HttpHandler {
    private HttpsServer httpsServer;
   
    public BioServiceServer() {
        String ksName = "keystore.jks";
        char ksPass[] = "12345678".toCharArray();
        char ctPass[] = "000000".toCharArray();
        
        try {            
            // initialise the HTTPS server
            httpsServer = HttpsServer.create(new InetSocketAddress("localhost", 8888), 0);

            // initialise the keystore
            KeyStore ks = KeyStore.getInstance("JKS");            
            ks.load(new FileInputStream(ksName), ksPass);
            
            // setup the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, ctPass);
            
            // setup the HTTPS context and parameters
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(kmf.getKeyManagers(), null, null);
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sc) {
                @Override
                public void configure (HttpsParameters params) {
                    try {
                        // initialise the SSL context
                        SSLContext c = SSLContext.getDefault();
                        SSLEngine engine = c.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        // get the default parameters
                        SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSSLParameters);
                    } catch (NoSuchAlgorithmException ex) {
                        Logger.getLogger(BioServiceServer.class.getName())
                                .log(Level.SEVERE, null, ex);
                    }
                }
            });
            
            httpsServer.createContext("/", this);
            httpsServer.setExecutor(null);
            httpsServer.start();
            System.out.println("Server started:");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {      
        
        // determine encoding
        Headers reqHeaders = exchange.getRequestHeaders();
        String contentType = reqHeaders.getFirst("Content-Type");
        String encoding = "ISO-8859-1";
        
        // read the query string from the request body
        String qry;
        InputStream in = exchange.getRequestBody();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte buf[] = new byte[4096];
            for (int n = in.read(buf); n > 0; n = in.read(buf)) {
                out.write(buf, 0, n);
            }
            qry = new String(out.toByteArray(), encoding);
        } finally {
            in.close();
        }
        
        exchange.sendResponseHeaders(200, 0);
    }
    
    @SuppressWarnings("empty-statement")
    public static void main(String[] args) {
        {
            try {
                BioServiceServer httpsServer = new BioServiceServer();
                BufferedReader stdIn = new BufferedReader(
                            new InputStreamReader(System.in));
                while(stdIn.readLine().contains("exit") == false);

            } catch (Exception ex) {
                Logger.getLogger(
                        BioServiceServer.class.getName()).log(Level.SEVERE, null, ex); 
            }
        }
    }

}
