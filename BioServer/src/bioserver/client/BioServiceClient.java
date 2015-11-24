///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package bioserver.client;
//
//import java.io.*;
//import java.security.KeyManagementException;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.cert.CertificateException;
//import java.security.cert.X509Certificate;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLSession;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
//import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
//import org.apache.http.conn.ssl.TrustStrategy;
//import org.apache.http.entity.ContentType;
//import org.apache.http.entity.mime.MultipartEntityBuilder;
//import org.apache.http.entity.mime.content.FileBody;
//import org.apache.http.entity.mime.content.StringBody;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.ssl.SSLContexts;
//import org.apache.http.util.EntityUtils;
//
///**
// *
// * @author Claudio
// */
//public class BioServiceClient {
//    
//    private CloseableHttpClient httpclient = null;
//    
//    public BioServiceClient() {
//        try {
//            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
//                @Override
//                public boolean verify(String string, SSLSession ssls) {
//                    return true;
//                }
//            };
//                        
//            // Trust own CA and all self-signed certs
//            SSLContext sslcontext = SSLContexts.custom()
//                    .loadTrustMaterial(new File("keystore.jks"), "12345678".toCharArray(),
//                            new TrustSelfSignedStrategy())
//                    .build();
//            
//            // Allow TLSv1 protocol only
//            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
//                    sslcontext,
//                    new String[] {"TLSv1"},
//                    null,
//                    hostnameVerifier);
//            
//            httpclient = HttpClients.custom()
//                    .setSSLSocketFactory(sslsf)
//                    .build();
//            
//            
//        } catch (NoSuchAlgorithmException 
//                | KeyStoreException 
//                | CertificateException 
//                | IOException 
//                | KeyManagementException ex) {
//            Logger.getLogger(BioServiceClient.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//    
//    public void close() {
//        if (httpclient != null) try {
//            httpclient.close();
//        } catch (IOException ex) {
//            Logger.getLogger(BioServiceClient.class.getName())
//                    .log(Level.SEVERE, null, ex);
//        }
//    }
//    
//    public CloseableHttpResponse sendFile(String host, File file, String usr, String pwd) 
//            throws IOException {
//        
//        HttpPost httppost = new HttpPost(host);
//        
//        HttpEntity reqEntity = MultipartEntityBuilder.create()
//                    .addPart("comment",     new StringBody("database", ContentType.TEXT_PLAIN))
//                    .addPart("username",    new StringBody(usr, ContentType.TEXT_PLAIN))
//                    .addPart("password",    new StringBody(pwd, ContentType.TEXT_PLAIN))
//                    .addPart("data",        new FileBody(file))
//                    .build();
//        
//        httppost.setEntity(reqEntity);
//        System.out.println("executing request " + httppost.getRequestLine());
//  
//        return httpclient.execute(httppost);
//    }
//    
//    public boolean verifyResponse(CloseableHttpResponse response) throws IOException {
//        try {
//            System.out.println("----------------------------------------");
//            System.out.println(response.getStatusLine());
//            HttpEntity resEntity = response.getEntity();
//            if (resEntity != null) {
//                System.out.println("Response content length: " + resEntity.getContentLength());
//            }
//            EntityUtils.consume(resEntity);
//        } finally {
//            response.close();
//        }
//        
//        return true;
//    }
//    
//
//    
//    static {
//	    //for localhost testing only
//	    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
//	    new javax.net.ssl.HostnameVerifier(){
//
//            @Override
//	        public boolean verify(String hostname,
//	                javax.net.ssl.SSLSession sslSession) {
//	            if (hostname.equals("localhost")) {
//	                return true;
//	            }
//	            return false;
//	        }
//	    });
//	}
//
//    public static void main(String[] args) throws IOException
//    {
//        BioServiceClient client = new BioServiceClient();
//        
//        client.verifyResponse(
//            client.sendFile(
//                    "https://localhost:8888/", 
//                    new File("file.pdf"), 
//                    "Claudio", 
//                    "123"));
//    }
//}
