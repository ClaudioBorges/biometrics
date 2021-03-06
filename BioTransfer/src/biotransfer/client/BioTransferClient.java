/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biotransfer.client;

import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.HttpEntity;
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

import biotransfer.*;
import java.net.ConnectException;
import org.apache.http.client.ClientProtocolException;

/**
 *
 * @author Claudio
 */
public class BioTransferClient {
    
    private final CloseableHttpClient httpclient;
    
    public BioTransferClient(
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
            BioTransferClient.logMsg("HTTPS IS NOT PRESENT!!!");
            httpclient = HttpClients.custom().build();
        } else {
            throw new IllegalArgumentException("Unrecognised communication protocol");
        }      
    }
    
    static private void logMsg(String msg) {
        System.out.println("[DEBUG] " + msg);
    }
    
    private String formatHostName(String protocol, String hostname, int port) {
        return new String()
                .concat(protocol)
                .concat("://")
                .concat(hostname)
                .concat(":")
                .concat(Integer.toString(port))
                .concat("/");
    }
    
    public void close() {
        if (httpclient != null) try {
            httpclient.close();
        } catch (IOException ex) {
            Logger.getLogger(BioTransferClient.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    public CloseableHttpResponse sendFile(
            String protocol, String hostname, int port,
            String username, String password,
            String filename) 
            throws IOException, ClientProtocolException {
        
        HttpPost httppost = new HttpPost(formatHostName(protocol, hostname, port));
        
        HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("type", 
                            new StringBody("db_upload", ContentType.TEXT_PLAIN))
                    .addPart("username", 
                            new StringBody(username, ContentType.TEXT_PLAIN))
                    .addPart("password", 
                            new StringBody(password, ContentType.TEXT_PLAIN))
                    .addPart("data",  
                            new FileBody(new File(filename)))
                    .build();
        
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

    public static void main(String[] args) throws IOException
    {
        String filename;
        
        if (args.length > 1) {
            System.out.println("usage: \"config_file\"");
            return;
        } else if (args.length == 0) {
            filename = "../_default_files/BioTransfer/BioTransfer.properties";
        } else {
            filename = args[0];
        }
        
        try {
            LoadConfig configs = new LoadConfig(filename);
            
            BioTransferClient client = new BioTransferClient(
                    configs.getCommProtocol(), configs.getCommHost(),
                    configs.getKeystorePath(), configs.getKeystoreStorePwd());
            
            client.verifyResponse(
                    client.sendFile(
                            configs.getCommProtocol(), configs.getCommHost(), configs.getCommPort(),
                            configs.getCommUsr(), configs.getCommPwd(),
                            configs.getClientDBPath()));
            
        } catch (ClientProtocolException | ConnectException e) {
            BioTransferClient.logMsg(e.getLocalizedMessage());
        }
    }
}
