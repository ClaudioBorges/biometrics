/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioserver;

import bioconverter.BioEntity.PERSON_CREDENTIAL;
import bioconverter.Biometrics;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Claudio
 */
public class BioServer {

    private static final String configPath = "C:\\Teste\\Configs\\BioServerConfig.properties";
    
    private static final int SERVER_PORT = 1765;
    private static final String reqBioCpf = "REQ_biometric_from_cpf:";
    private static final String rspBioCpf = "RSP_biometric_from_cpf:";
    
    public enum BIO_CHECK_ERROR {
        MATCHED,
        NOT_MATCHED,
        CREDENTIAL_FAIL,
        UNKNOWN_CPF,
        TIMEOUT,
        CONNECTION_LOST,
        CONNECTION_UNREACHED,
        UNKNOWN
    };

    private void dbgMsg(String s) {
        System.out.println("[DEBUG] " + s);
    }
    
    private class PropertyValues {
        private String bioDB = null;
        private String bioLoggerDB = null;
        private int source = 0;
        
        public String getBioDB() {
            return this.bioDB;
        }
        
        public String getBioLoggerDB() {
            return this.bioLoggerDB;
        }
        
        public int getSource() {
            return this.source;
        }
        
        public void updatePropValues(String filename) throws IOException {
            InputStream is = null;
                    
            try {
                Properties prop = new Properties();
                is = new FileInputStream(filename);
                
                if (is != null) {
                    prop.load(is);
                } else {
                    throw new FileNotFoundException(
                            "property file '" + filename + "' not found");
                }
                
                bioDB = prop.getProperty("Biometric_DB");
                bioLoggerDB = prop.getProperty("Biometric_Logger_DB");
                source = Integer.parseInt(prop.getProperty("Biometric_Source"));
            } catch (IOException | NumberFormatException ex) {
                Logger.getLogger(
                        BioServer.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (is != null) is.close();
            }
        }
    }
    
    private BIO_CHECK_ERROR validateBiometric(
            String sCredential,
            String cpf,
            Biometrics bio) {
        
        BIO_CHECK_ERROR err = BIO_CHECK_ERROR.UNKNOWN;
         
        try {
            PERSON_CREDENTIAL credential 
                    =  PERSON_CREDENTIAL.getEnum(sCredential);
            
            switch (bio.verifyBiometric(cpf, credential)) {
            case MATCHED:
                err = BIO_CHECK_ERROR.MATCHED;
                break;

            case NOT_MATCHED:
                err = BIO_CHECK_ERROR.NOT_MATCHED;
                break;

            case UNKNOWN_CPF:
                err = BIO_CHECK_ERROR.UNKNOWN_CPF;
                break;

            case TIMEOUT:
                err = BIO_CHECK_ERROR.TIMEOUT;
                break;

            case WRONG_CREDENTIAL:
                err = BIO_CHECK_ERROR.CREDENTIAL_FAIL;
                break;

            case UNKNOWN:
            default:
                err = BIO_CHECK_ERROR.UNKNOWN;
                break;
            }
        } catch (IllegalArgumentException ex) {
            err = BIO_CHECK_ERROR.CREDENTIAL_FAIL;
        }

        
        return err;
    }

    public void bioHandleClient(
        Socket clientSocket, Biometrics bio) {
        
        try {
            PrintWriter out =
                    new PrintWriter(
                            clientSocket.getOutputStream(),
                            true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));

            String fromClient;

            if ((fromClient = in.readLine()) != null) {
                if (fromClient.equals(reqBioCpf)) {
                    dbgMsg(reqBioCpf);

                    String credential = in.readLine();
                    dbgMsg("Credential: " + credential);
                    
                    String cpf = in.readLine();
                    dbgMsg("CPF: " + cpf);
                    
                    BIO_CHECK_ERROR result;
                    result = validateBiometric(credential, cpf, bio);

                    out.println(rspBioCpf);
                    out.println(result);

                    dbgMsg(rspBioCpf);
                    dbgMsg(String.format("%s\n", result));
                }
            }
        } catch (SocketTimeoutException e) {
            dbgMsg("SocketTimeoutException");
        } catch (IOException e) {
            dbgMsg("IOException");
                    
            Logger.getLogger(
                    BioServer.class.getName()).log(Level.SEVERE, null, e);
        } catch (Exception e) {
            dbgMsg("Exception");
            
            Logger.getLogger(
                    BioServer.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (clientSocket != null) clientSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(
                        BioServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public boolean run() throws ClassNotFoundException, SQLException, IOException {
        ServerSocket socket = null;
        Socket clientSocket = null;
        Biometrics bio = null;
        PropertyValues propVals = null;
        
        try {            
            dbgMsg("Openning Biometrics...");
            
            propVals = new PropertyValues();
            propVals.updatePropValues(configPath);           
            
            bio = new Biometrics(
                    propVals.getBioDB(),
                    propVals.getBioLoggerDB(),
                    propVals.getSource());
            if (bio.openScanner() == false)
                throw new IOException("Finger Scanner not found.");            
            
            dbgMsg("Openning server port...");
            socket = new ServerSocket(SERVER_PORT);
            dbgMsg("Server listenning to: " + SERVER_PORT);
                        
            while (true) {
                dbgMsg("Waiting for client...");

                clientSocket = socket.accept();
                dbgMsg("Client connected: "
                        + clientSocket.getInetAddress()
                                    .getHostAddress());

                clientSocket.setSoTimeout(60 * 1000);
                clientSocket.setKeepAlive(true);

                bioHandleClient(clientSocket, bio);
            }                
        } finally {
            try {
                if (socket != null) socket.close();
                if (bio != null) bio.close();
            } catch (IOException ex) {
                Logger.getLogger(
                        BioServer.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                socket  = null;
                bio     = null;
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        BioServer bioServer = new BioServer();
        
        while (true) {
            try {
                bioServer.dbgMsg("System running...");
                bioServer.run();

            } catch (Exception ex) {
                Logger.getLogger(
                        BioServer.class.getName()).log(Level.SEVERE, null, ex); 
            }
            
            bioServer.dbgMsg("Sleeping...");
            
            try {            
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(BioServer.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }
}