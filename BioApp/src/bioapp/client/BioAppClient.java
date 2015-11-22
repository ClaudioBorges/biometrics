/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioapp.client;

import bioapp.PresidentEntity;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import org.json.JSONObject;

/**
 *
 * @author Claudio
 */
public class BioAppClient {
    String ksName = "keystore.jks";
    char ksPass[] = "12345678".toCharArray();
    char ctPass[] = "000000".toCharArray();
    
    private SSLSocketFactory sslFactory = null;
    
    public BioAppClient() {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");            
            ks.load(new FileInputStream(ksName), ksPass);     
            
            TrustManagerFactory tmf = 
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tmf.getTrustManagers(), null);
            sslFactory = ctx.getSocketFactory();
        } catch (KeyStoreException 
                | NoSuchAlgorithmException 
                | CertificateException 
                | IOException 
                | KeyManagementException ex) {
            Logger.getLogger(BioAppClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public HttpURLConnection openConnection(String hostname, int port) {
        HttpsURLConnection conn = null;
        
        try {
            String https_url = "https://" + hostname + ":" + String.valueOf(port) + "/";
            
            URL url = new URL(https_url);
            conn = (HttpsURLConnection)url.openConnection();
            conn.setSSLSocketFactory(sslFactory);

        } catch (MalformedURLException e) {
           e.printStackTrace();
        } catch (IOException e) {
           e.printStackTrace();
        }
        
        return conn;
    }

    private JSONObject preperaLogin(String usr, String pwd) {
        
        JSONObject json = new JSONObject();
        
        json.put("username", usr);
        json.put("password", pwd);
        
        return json;
    }
    
    private JSONObject prepareLog(String fDB, String usr, String pwd) {
        
        JSONObject json = new JSONObject();
        
        try {
            PresidentEntity entity = new PresidentEntity(fDB);
            
            json.put("login", preperaLogin(usr, pwd));
            json.put("president_log", entity.buildJSON(entity.getAllCandidates()));
            
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(BioAppClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return json;
    }

    public boolean sendLog(HttpURLConnection conn, String db, String usr, String pwd) {
        JSONObject json = prepareLog(db, usr, pwd);
        
        System.out.println(json.toString(4));
        
        return sendPost(conn, prepareLog(db, usr, pwd).toString(0).getBytes(), 5000);
    }
    
    private boolean sendPost(HttpURLConnection conn, byte[] datas, int ms) {        
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(datas.length));
            conn.setRequestProperty("Content-Language", "en-US");
            
            conn.setConnectTimeout(ms);
            
            conn.setDoOutput(true);
            conn.getOutputStream().write(datas);
            
            return (conn.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (ProtocolException ex) {
            Logger.getLogger(BioAppClient.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (SocketTimeoutException ex) {
            Logger.getLogger(BioAppClient.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(BioAppClient.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    private byte[] preparePost(Map<String, Object> params) throws UnsupportedEncodingException {
        byte[] postDataBytes = null;
        
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) 
                postData.append('&');
            
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        postDataBytes = postData.toString().getBytes("UTF-8");
        
        return postDataBytes;
    }
    
    private void testIt(){
        String https_url = "https://localhost:8888/";
        URL url;
        try {

           url = new URL(https_url);
           HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
           con.setSSLSocketFactory(sslFactory);

           //dumpl all cert info
           print_https_cert(con);

           //dump all the content
           print_content(con);

        } catch (MalformedURLException e) {
           e.printStackTrace();
        } catch (IOException e) {
           e.printStackTrace();
        }
    }
	
    private void print_https_cert(HttpsURLConnection con){

    if (con != null) {

        try {
            System.out.println("Response Code : " + con.getResponseCode());
            System.out.println("Cipher Suite : " + con.getCipherSuite());
            System.out.println("\n");

            Certificate[] certs = con.getServerCertificates();
            for(Certificate cert : certs){
                System.out.println("Cert Type : " + cert.getType());
                System.out.println("Cert Hash Code : " + cert.hashCode());
                System.out.println("Cert Public Key Algorithm : " 
                                             + cert.getPublicKey().getAlgorithm());
                System.out.println("Cert Public Key Format : " 
                                             + cert.getPublicKey().getFormat());
                System.out.println("\n");
            }

            } catch (SSLPeerUnverifiedException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }
        }	
    }
	
    private void print_content(HttpsURLConnection con) {
        if (con!=null) {
            try {
               System.out.println("****** Content of the URL ********");			
               BufferedReader br = 
                new BufferedReader(
                    new InputStreamReader(con.getInputStream()));

               String input;

               while ((input = br.readLine()) != null){
                  System.out.println(input);
               }
               br.close();

            } catch (IOException e) {
               e.printStackTrace();
            }
        }
    }
    
    static {
	    //for localhost testing only
	    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
	    new javax.net.ssl.HostnameVerifier(){

            @Override
	        public boolean verify(String hostname,
	                javax.net.ssl.SSLSession sslSession) {
	            if (hostname.equals("localhost")) {
	                return true;
	            }
	            return false;
	        }
	    });
	}

    public static void main(String[] args)
    {
        BioAppClient client = new BioAppClient();
        
        HttpURLConnection conn = client.openConnection("localhost", 8888);
        System.out.print(client.sendLog(conn, "", "Claudio", "123"));
    }
}
