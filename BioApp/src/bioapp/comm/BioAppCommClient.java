/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioapp.comm;

import bioapp.PresidentEntity;
import java.io.*;
import java.net.ConnectException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.swing.UIManager;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;


/**
 *
 * @author Claudio
 */
public class BioAppCommClient {
    
    private final CloseableHttpClient httpclient;
    
    public BioAppCommClient(
            String commProtocol, 
            String commHostname,
            String keyFilePath,
            String keyStorePwd) 
            throws IOException, IllegalArgumentException {
        
        if ("https".equals(commProtocol)) {
            try {
                HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                    @Override
                    public boolean verify(String string, SSLSession ssls) {
                        return "localhost".equals(commHostname);
                    }
                };
                
                SSLContext sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(
                            new File(keyFilePath), 
                            keyStorePwd.toCharArray(),
                            new TrustSelfSignedStrategy())
                    .build();
                
                // Allow TLSv1 protocol only
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                        sslcontext,
                        new String[] {"TLSv1"},
                        null,
                        hostnameVerifier);
                
                httpclient = HttpClients.custom()
                        .setSSLSocketFactory(sslsf) 
                        .build();
            } catch (NoSuchAlgorithmException 
                    | KeyManagementException 
                    | KeyStoreException 
                    | CertificateException ex) {
                
                throw new IOException("Failed to set up ssl context.");
            }
        } else if ("http".equals(commProtocol)) {
            BioAppCommClient.logMsg("HTTPS IS NOT PRESENT!!!");
            httpclient = HttpClients.custom().build();
        } else {
            throw new IllegalArgumentException("Unrecognised communication protocol");
        }      
    }
    
    static private void logMsg(String msg) {
        System.out.println("[DEBUG] " + msg);
    }
    
    private String formatHostName(String protocol, String hostname, int port, String trail) {
        return new String()
                .concat(protocol)
                .concat("://")
                .concat(hostname)
                .concat(":")
                .concat(Integer.toString(port))
                .concat("/")
                .concat(trail);
    }
    
    public void close() {
        if (httpclient != null) try {
            httpclient.close();
        } catch (IOException ex) {
            Logger.getLogger(BioAppCommClient.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    public CloseableHttpResponse sendLog(
            String protocol, String hostname, int port, String trail,
            String username, String password,
            JSONObject json, HashMap<String, String> photos) 
            
            throws IOException, ClientProtocolException  {
        
        String host = formatHostName(protocol, hostname, port, trail);
        
        logMsg("building post to: " + host);
                
        HttpPost httppost = new HttpPost(host);
        
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        
        builder.addPart("type", 
                new StringBody("president_db_upload", ContentType.TEXT_PLAIN));
        builder.addPart("username", 
                new StringBody(username, ContentType.TEXT_PLAIN));
        builder.addPart("password",
                new StringBody(password, ContentType.TEXT_PLAIN));
        builder.addPart("candidates_json", 
                new StringBody(json.toString(0), ContentType.TEXT_PLAIN));
        
        for (String key : photos.keySet()) {
            String path = photos.get(key);
            
            File file = new File(path);
            if (file.exists() && file.isFile() && file.canRead()) {
                builder.addPart("candidate_photo_" + key,  
                                new FileBody(file));
            } 
        }
        
        HttpEntity reqEntity = builder.build();
        
        httppost.setEntity(reqEntity);
        
        logMsg("executing request " + httppost.getRequestLine());
        
        return httpclient.execute(httppost);
    }
    
    public boolean verifyResponse(CloseableHttpResponse response) throws IOException {
        try {
            logMsg("----------------------------------------");
            logMsg(response.getStatusLine().toString());
            
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                logMsg("Response content length: " + resEntity.getContentLength());
            }
            
            EntityUtils.consume(resEntity);
        } finally {
            response.close();
        }
        
        return true;
    }

    private JSONObject preperaLogin(String usr, String pwd) {
        
        JSONObject json = new JSONObject();
        
        json.put("username", usr);
        json.put("password", pwd);
        
        return json;
    }
        public class LogBuilder {
        private final JSONObject json;

        public JSONObject getJson() {
            return json;
        }

        public HashMap<String, String> getMap() {
            return map;
        }
        private final HashMap<String, String> map;
        
        LogBuilder(String fDB, String usr, String pwd) throws IOException {
            PresidentEntity entity = null;
            
            try {
                entity = new PresidentEntity(fDB);

                json = new JSONObject();
                json.put("president_log", entity.buildJSON(entity.getAllCandidates()));
                
                boolean saveFile = true;
                if (saveFile) {
                    try (FileOutputStream file = new FileOutputStream("president_log.json")) {
                        file.write(json.toString(4).getBytes());
                    }
                }
                
                map = entity.getPhotosPath(entity.getAllCandidates());

            } catch (ClassNotFoundException | SQLException ex) {
                throw new IOException("SQL error");
            } finally {
                if (entity != null) entity.close();
            } 
        }
    }

    public static void main(String[] args)
    {
        String filename;
        
        if (args.length > 1) {
            System.out.println("usage: \"config_file\"");
            return;
        } else if (args.length == 0) {
            filename = "../_default_files/BioApp/BioApp.properties";
        } else {
            filename = args[0];
        }
        
        try {
            BioAppCommLoad configs = new BioAppCommLoad(filename);
            
            BioAppCommClient client = new BioAppCommClient(
                    configs.getCommProtocol(), configs.getCommHost(),
                    configs.getKeystorePath(), configs.getKeystoreStorePwd());
            
            BioAppCommClient.LogBuilder log 
                    = client.new LogBuilder(
                            configs.getAppDatabasePath() + configs.getAppDatabaseName(), 
                            configs.getCommUsr(), configs.getCommPwd());

            client.verifyResponse(
                    client.sendLog(
                            configs.getCommProtocol(), configs.getCommHost(), 
                            configs.getCommPort(), configs.getCommTrail(),
                            configs.getCommUsr(), configs.getCommPwd(),
                            log.getJson(), log.getMap()));
            
        } catch (ClientProtocolException | ConnectException e) {
            BioAppCommClient.logMsg(e.getLocalizedMessage());
        } catch (NumberFormatException | IOException e) {
            BioAppCommClient.logMsg(e.getLocalizedMessage());
        }
    }
}
