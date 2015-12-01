/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biotransfer.server;

import bioconverter.BioEntity;
import biotransfer.LoadConfig;
import com.sun.net.httpserver.Headers;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.http.ConnectionClosedException;
import org.apache.http.ExceptionLogger;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.ssl.SSLContexts;
import util.FilesLib;

/**
 *
 * @author Claudio
 */
public class BioTransferServer {
    
    private final LoadConfig configs; 
    final HttpServer server;
    
    public BioTransferServer(String fileConfig) 
            throws NumberFormatException, 
            IOException, 
            InterruptedException {
        
        configs = new LoadConfig(fileConfig);
        
        SSLContext sslcontext = null;
        
        if ("https".equals(configs.getCommProtocol())) {
                        
            try {
                
                sslcontext = SSLContexts.custom()
                        .loadKeyMaterial(
                                new File(configs.getKeystorePath()),
                                configs.getKeystoreStorePwd().toCharArray(),
                                configs.getKeystoreKeyPwd().toCharArray())
                        .build();
                
            } catch (NoSuchAlgorithmException 
                    | KeyStoreException 
                    | UnrecoverableKeyException 
                    | CertificateException 
                    | KeyManagementException ex) {
                
                throw new IOException("Failed to set up ssl context.");
            }
        } else if ("http".equals(configs.getCommProtocol())) {
            BioTransferServer.logMsg("HTTPS IS NOT PRESENT!!!");
        } else {
            throw new IllegalArgumentException("Unrecognised communication protocol");
        } 
        
        SocketConfig socketConfig = SocketConfig.custom()
            .setSoTimeout(15000)
            .setTcpNoDelay(true)
            .build();
        
        if (sslcontext != null) {
            server = ServerBootstrap.bootstrap()
                .setListenerPort(configs.getCommPort())
                .setServerInfo("Test/1.1")
                .setSocketConfig(socketConfig)
                .setSslContext(sslcontext)
                .setExceptionLogger(new StdErrorExceptionLogger())
                .registerHandler("*", new HttpFileHandler("C:\\"))
                .create();
        } else {
            server = ServerBootstrap.bootstrap()
                .setListenerPort(configs.getCommPort())
                .setServerInfo("Test/1.1")
                .setSocketConfig(socketConfig)
                .setExceptionLogger(new StdErrorExceptionLogger())
                .registerHandler("*", new HttpFileHandler("C:\\"))
                .create();
        }

        server.start();
        
        BioTransferServer.logMsg("Server is running: ");
        BioTransferServer.logMsg("Protocol: " + configs.getCommProtocol());
        BioTransferServer.logMsg("Port: " + configs.getCommPort()); 
        
        server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }
    
    public void close() {
        if (server != null) {
            server.stop();
        }
    }
    
    static private void logMsg(String msg) {
        System.out.println("[DEBUG] " + msg);
    }
    
    public class StdErrorExceptionLogger implements ExceptionLogger {
        @Override
        public void log(final Exception ex) {
            if (ex instanceof SocketTimeoutException) {
                System.err.println("Connection timed out");
            } else if (ex instanceof ConnectionClosedException) {
                System.err.println(ex.getMessage());
            } else {
                ex.printStackTrace();
            }
        }

    }
    
    private class HttpFileHandler implements HttpRequestHandler  {

        private final String docRoot;

        public HttpFileHandler(final String docRoot) {
            super();
            this.docRoot = docRoot;
        }

        @Override
        public void handle(
                final HttpRequest request,
                final HttpResponse response,
                final HttpContext context) throws HttpException, IOException {

            int status = HttpStatus.SC_BAD_REQUEST;
            
            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ROOT);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }
            
            BioTransferServer.logMsg("");
            BioTransferServer.logMsg("Client request!");
            
            String target = request.getRequestLine().getUri();

            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                
                try {
                    String boundary = "";
                    Header hdr = request.getFirstHeader("Content-Type");
                    HeaderElement[] els = hdr.getElements();
                    for (HeaderElement el : els) {
                        NameValuePair nvp = el.getParameterByName("boundary");
                        if (nvp != null) {
                            boundary = nvp.getValue();
                            break;
                        }
                    }

                    @SuppressWarnings("deprecation")
                    MultipartStream multipartStream =
                            new MultipartStream(entity.getContent(), boundary.getBytes());

                    String database = getContentBody(multipartStream, "type");
                    if ("db_upload".equals(database)) {
                        
                        BioTransferServer.logMsg("Request type: " + database); 
                        
                        String username = getContentBody(multipartStream, "username");
                        if (configs.getCommUsr().equals(username)) {
                            String password = getContentBody(multipartStream, "password");
                            if (configs.getCommPwd().equals(password)) {
                                saveDatabase(
                                        multipartStream,
                                        configs.getServerStorePath());
                                
                                BioTransferServer.logMsg("Request OK"); 
                                status = HttpStatus.SC_OK;
                            } else {
                                BioTransferServer.logMsg("Password UNAUTHORIZED"); 
                                status = HttpStatus.SC_UNAUTHORIZED;
                            }
                        } else {
                            BioTransferServer.logMsg("Username UNAUTHORIZED"); 
                            status = HttpStatus.SC_UNAUTHORIZED;
                        }
                    } else {
                        
                        BioTransferServer.logMsg("BAD_REQUEST Comment=database not founded.");                        
                        status = HttpStatus.SC_BAD_REQUEST;
                    }
                } catch (ParseException | IOException | UnsupportedOperationException ex) {
                    BioTransferServer.logMsg(ex.getLocalizedMessage());
                }
            }
            
            response.setStatusCode(status);
            
            String msg;
            if (status == HttpStatus.SC_OK) msg = "OK";
            else if (status == HttpStatus.SC_BAD_REQUEST) msg = "BAD REQUEST";
            else msg = "UNAUTHORIZED";
            
            StringEntity entity = new StringEntity(
                    "<html><body><h1>" + msg + "</h1></body></html>",
                    ContentType.create("text/html", "UTF-8"));
            response.setEntity(entity);
        }
    }
    
    String getUsername(MultipartStream stream) {
        return getContentBody(stream, "username");
    }
    
    String getPassword(MultipartStream stream) {
        return getContentBody(stream, "password");
    }
    
    void cloneDatabase(String db_from, String db_to) throws IOException {
        
        BioEntity bioEntity = null;
        
        try {
            bioEntity = new BioEntity(db_to);
            
            bioEntity.cloneFrom(db_from);
            
        } catch (ClassNotFoundException | SQLException | IOException ex) {
            throw new IOException("Impossible to clone databases: " + db_from + "--->" + db_to);
        } finally {
            if (bioEntity != null)  bioEntity.close();
        }
    }
    
    void saveDatabase(MultipartStream stream, String filePath) {
        String[] names = new String[]{"name", "filename"};
        String[] contents = getContents(stream, names);
        
        String tmpFilePath = filePath + ".tmp";
        
        if ("data".equals(contents[0])) {
            FileOutputStream oFile = null;
            try {
                FilesLib.creteFile(tmpFilePath);
                
                oFile = new FileOutputStream(tmpFilePath);
                oFile.write(getBodyData(stream).toByteArray());
                
            } catch (FileNotFoundException ex) {
                Logger.getLogger(BioTransferServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {                
                Logger.getLogger(BioTransferServer.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (oFile != null) oFile.close();
                } catch (IOException ex) {
                    Logger.getLogger(BioTransferServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            try {
                cloneDatabase(tmpFilePath, filePath);
            } catch (IOException ex) {
                Logger.getLogger(BioTransferServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            {
                try {
                    FilesLib.deleteFile(tmpFilePath);
                } catch (IOException ex) {
                    Logger.getLogger(BioTransferServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    ByteArrayOutputStream getBodyData(MultipartStream stream) throws IOException {
        ByteArrayOutputStream builder = new ByteArrayOutputStream();
        
        stream.readBodyData(builder);
        
        return builder;
    }
    
    String[] getContents(MultipartStream stream, String[] contents) {
        String[] datas = new String[contents.length];
        
        Headers headers = headersBuilder(stream);
        HashMap<String, String> maps = getContentDisposition(headers);
        
        int i = 0;
        for (String content : contents) {
            String id = maps.get(content);
            
            datas[i] = id;
                    
            ++i;
        }
        
        return datas;
    }    

    String getContentBody(MultipartStream stream, String contentName) {
        String data = "";
        Headers headers = headersBuilder(stream);
        HashMap<String, String> maps = getContentDisposition(headers);
        
        String name = maps.get("name");
        if (name != null) {
            if (name.equals(contentName)) {
                try {
                    ByteArrayOutputStream builder = new ByteArrayOutputStream();
                    stream.readBodyData(builder);
                    data = builder.toString();
                } catch (IOException ex) {
                    Logger.getLogger(BioTransferServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return data; 
    }
    
    boolean checkPreamble(MultipartStream stream) {
        String data = getContentBody(stream, "comment");
        
        return data.equals("database");        
    }
    
    Headers headersBuilder(MultipartStream stream) {
        try {
            String readHdr = stream.readHeaders();
            String[] lines = readHdr.split("\\r?\\n");
            
            Headers hdrs = new Headers();
            
            for (String line : lines) {
                int pos = line.indexOf(":");
                
                if (pos > 0)
                    hdrs.add(
                            line.subSequence(0, pos).toString(),
                            line.subSequence(pos + 1, line.length()).toString());
            }
            
            return hdrs;
        } catch (FileUploadBase.FileUploadIOException 
                | MultipartStream.MalformedStreamException ex) {
            Logger.getLogger(BioTransferServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    HashMap<String, String> getContentDisposition(Headers headers) {
        HashMap<String, String> map = new HashMap<>();
        
        String content = headers.getFirst("Content-Disposition");
        content = content.replaceAll("\\s+","");
        String[] params = content.split(";");
        
        for (String param : params) {
            int idx = param.lastIndexOf("=");
            
            if (idx >= 0)
                map.put(
                        param.substring(0, idx), 
                        param.substring(idx + 1).replaceAll("^\"|\"$", ""));
        }
        
        return map;
    }

    public static void main(String[] args) {
        String filename;
        
        if (args.length > 1) {
            System.out.println("usage: \"config_file\"");
            return;
        } else if (args.length == 0) {
            filename = "../_default_files/BioTransfer/BioTransfer.properties";
        } else {
            filename = args[0];
        }
        
        BioTransferServer server = null;
        try {
            
            server = new BioTransferServer(filename);
            
        } catch (NumberFormatException | IOException | InterruptedException ex) {
            
            BioTransferServer.logMsg(ex.getLocalizedMessage());
        } finally {
            
            if (server != null) server.close();
        }
    }
}
