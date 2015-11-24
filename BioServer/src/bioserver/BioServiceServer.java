///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package bioserver;
//
//import com.sun.net.httpserver.Headers;
//import com.sun.net.httpserver.HttpExchange;
//import com.sun.net.httpserver.HttpHandler;
//import com.sun.net.httpserver.HttpsConfigurator;
//import com.sun.net.httpserver.HttpsParameters;
//import com.sun.net.httpserver.HttpsServer;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.InetSocketAddress;
//import java.net.SocketTimeoutException;
//import java.net.URL;
//import java.net.URLDecoder;
//import java.nio.charset.Charset;
//import java.security.KeyStore;
//import java.security.NoSuchAlgorithmException;
//import java.util.Locale;
//import java.util.concurrent.TimeUnit;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.net.ssl.KeyManagerFactory;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLEngine;
//import javax.net.ssl.SSLParameters;
//import org.apache.http.ConnectionClosedException;
//import org.apache.http.ExceptionLogger;
//import org.apache.http.HttpConnection;
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpEntityEnclosingRequest;
//import org.apache.http.HttpException;
//import org.apache.http.HttpRequest;
//import org.apache.http.HttpResponse;
//import org.apache.http.HttpStatus;
//import org.apache.http.MethodNotSupportedException;
//import org.apache.http.config.SocketConfig;
//import org.apache.http.entity.ContentType;
//import org.apache.http.entity.FileEntity;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.bootstrap.HttpServer;
//import org.apache.http.impl.bootstrap.ServerBootstrap;
//import org.apache.http.protocol.HttpContext;
//import org.apache.http.protocol.HttpCoreContext;
//import org.apache.http.protocol.HttpRequestHandler;
//import org.apache.http.ssl.SSLContexts;
//import org.apache.http.util.EntityUtils;
//
///**
// *
// * @author Claudio
// */
//public class BioServiceServer {
//    private HttpsServer httpsServer;
//   
//    public BioServiceServer(int port) throws Exception {
//        SSLContext sslcontext = null;
//        if (port == 8888) {
//            // Initialize SSL context
// 
//            sslcontext = SSLContexts.custom()
//                    .loadKeyMaterial(
//                            new File("keystore.jks"), 
//                            "12345678".toCharArray(), 
//                            "12345678".toCharArray())
//                    .build();
//        }
//        
//        SocketConfig socketConfig = SocketConfig.custom()
//            .setSoTimeout(15000)
//            .setTcpNoDelay(true)
//            .build();
//
//        final HttpServer server = ServerBootstrap.bootstrap()
//                .setListenerPort(port)
//                .setServerInfo("Test/1.1")
//                .setSocketConfig(socketConfig)
//                .setSslContext(sslcontext)
//                .setExceptionLogger(new StdErrorExceptionLogger())
//                .registerHandler("*", new HttpFileHandler("C:\\"))
//                .create();
//
//        server.start();
//        server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);       
//    }
//    
//    public class StdErrorExceptionLogger implements ExceptionLogger {
//        @Override
//        public void log(final Exception ex) {
//            if (ex instanceof SocketTimeoutException) {
//                System.err.println("Connection timed out");
//            } else if (ex instanceof ConnectionClosedException) {
//                System.err.println(ex.getMessage());
//            } else {
//                ex.printStackTrace();
//            }
//        }
//
//    }
//    
//    private class HttpFileHandler implements HttpRequestHandler  {
//
//        private final String docRoot;
//
//        public HttpFileHandler(final String docRoot) {
//            super();
//            this.docRoot = docRoot;
//        }
//
//        @Override
//        public void handle(
//                final HttpRequest request,
//                final HttpResponse response,
//                final HttpContext context) throws HttpException, IOException {
//
//            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ROOT);
//            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
//                throw new MethodNotSupportedException(method + " method not supported");
//            }
//            String target = request.getRequestLine().getUri();
//
//            if (request instanceof HttpEntityEnclosingRequest) {
//                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
//                byte[] entityContent = EntityUtils.toByteArray(entity);
//                System.out.println("Incoming entity content (bytes): " + entityContent.length);
//            }
//            
//            //Header[] hdrs = request.getHeaders("Content-Disposition"");
//            //hdrs.
//            
//            response.setStatusCode(HttpStatus.SC_OK);
//            StringEntity entity = new StringEntity(
//                    "<html><body><h1>OK</h1></body></html>",
//                    ContentType.create("text/html", "UTF-8"));
//            response.setEntity(entity);
//          
//            
//            /*HttpCoreContext coreContext = HttpCoreContext.adapt(context);
//            HttpConnection conn = coreContext.getConnection(HttpConnection.class);
//            response.setStatusCode(HttpStatus.SC_OK);
//            FileEntity body = new FileEntity(file, ContentType.create("text/html", (Charset) null));
//            response.setEntity(body);
//            System.out.println(conn + ": serving file " + file.getPath());*/
//        }
//    }
//    
//    @SuppressWarnings("empty-statement")
//    public static void main(String[] args) {
//        {
//            try {
//                BioServiceServer httpsServer = new BioServiceServer(8888);
//                BufferedReader stdIn = new BufferedReader(
//                            new InputStreamReader(System.in));
//                while(stdIn.readLine().contains("exit") == false);
//
//            } catch (Exception ex) {
//                Logger.getLogger(
//                        BioServiceServer.class.getName()).log(Level.SEVERE, null, ex); 
//            }
//        }
//    }
//
//}
