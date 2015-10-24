/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioserver;

import bioconverter.Biometrics;
import cpflib.CPFLib;
import encoder.EncControl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOException;

/**
 *
 * @author Claudio
 */
public class BioServer {

    private final int SERVER_PORT = 1765;
    private final String HOSTNAME = "localhost";

    private final String init = "HELLO";
    private final String ping = "PING";
    private final String pong = "PONG";
    private final String reqBioCpf = "REQ_biometric_from_cpf:";
    private final String rspBioCpf = "RSP_biometric_from_cpf:";
    private final String eoc = "BYE";
    
    public enum BIO_CHECK_ERROR {
        MATCHED,
        NOT_MATCHED,
        UNKNOWN_CPF,
        TIMEOUT,
        CONNECTION_LOST,
        CONNECTION_UNREACHED,
        UNKNOWN
    };

    private void dbgMsg(String s) {
        System.out.println("[DEBUG] " + s);
    }
    
    private BIO_CHECK_ERROR validateBiometric(
            String cpf, 
            Biometrics bio) {
        
        BIO_CHECK_ERROR err = BIO_CHECK_ERROR.UNKNOWN_CPF;
         
        switch (bio.verifyBiometric(cpf)) {
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
            err =BIO_CHECK_ERROR.TIMEOUT;
            break;
            
        case UNKNOWN:
        default:
            err = BIO_CHECK_ERROR.UNKNOWN;
            break;
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

            out.println(init);

            String fromClient;

            if ((fromClient = in.readLine()) != null) {
                if (fromClient.contains(eoc)) {
                    clientSocket.close();
                    dbgMsg(eoc);
                }
                else if (fromClient.equals(reqBioCpf)) {
                    dbgMsg(reqBioCpf);

                    fromClient = in.readLine();
                    if (fromClient.contains(eoc)) {
                        clientSocket.close();
                        dbgMsg(eoc);
                    }

                    dbgMsg("CPF: " + fromClient);
                    
                    BIO_CHECK_ERROR result;
                    result = validateBiometric(fromClient, bio);

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
        
        try {            
            dbgMsg("Openning Biometrics...");
            bio = new Biometrics("C:\\Teste\\biodb.db");
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