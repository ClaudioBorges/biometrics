/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioapp.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.json.JSONObject;

/**
 *
 * @author Claudio
 */
public class BioAppHttpsServer implements HttpHandler {
    private HttpsServer httpsServer;
   
    public BioAppHttpsServer() {
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
                        Logger.getLogger(BioAppHttpsServer.class.getName())
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
    
    public static void main(String[] args) {
        {
            try {
                BioAppHttpsServer httpsServer = new BioAppHttpsServer();
                BufferedReader stdIn = new BufferedReader(
                            new InputStreamReader(System.in));
                while(stdIn.readLine().contains("exit") == false);

            } catch (Exception ex) {
                Logger.getLogger(
                        BioAppHttpsServer.class.getName()).log(Level.SEVERE, null, ex); 
            }
        }
    }
    
    /**
    * returns the url parameters in a map
    * @param query
    * @return map
    */
    public static Map<String, String> queryToMap(String query){
        Map<String, String> result = new HashMap<>();        
        try {
            for (String param : query.split("&")) {
                String pair[] = param.split("=");

                if (pair.length > 1) {
                    result.put(pair[0], pair[1]);
                }else{
                    result.put(pair[0], "");
                }
            }
        } catch (Exception e) {
            
        }
        
        return result;
    }

    private JSONObject buildJSON(byte[] data) {
        String str = new String(data, StandardCharsets.UTF_8);
        
        JSONObject json = new JSONObject(str);
        
        return json;
    }
    
    private void saveJSON(JSONObject json) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream("log.json");
            out.write(json.toString().getBytes());
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BioAppHttpsServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BioAppHttpsServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(BioAppHttpsServer.class.getName()).log(Level.SEVERE, null, ex);
            }
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
        
        saveJSON(buildJSON(qry.getBytes()));
        
        /*
        // parse the query
        Map<String, List<String>> parms = new HashMap<>();
        String defs[] = qry.split("[&]");
        for (String def: defs) {
            int ix = def.indexOf('=');
            String name;
            String value;
            if (ix < 0) {
                name = URLDecoder.decode(def, encoding);
                value = "";
            } else {
                name = URLDecoder.decode(def.substring(0, ix), encoding);
                value = URLDecoder.decode(def.substring(ix+1), encoding);
            }
            
            System.out.println("name = " + name);            
            System.out.println("value = " + value);
            
            List<String> list = parms.get(name);
            if (list == null) {
                list = new ArrayList<String>();
                parms.put(name, list);
            }
            list.add(value);
        }*/
        
        exchange.sendResponseHeaders(200, 0);
    }
}
