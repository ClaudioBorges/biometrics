/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioserver;

import bioconverter.Biometrics;
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
        
        switch (bio.verifyBiometric(cpf)) {
        case MATCHED:
            return BIO_CHECK_ERROR.MATCHED;

        case NOT_MATCHED:
            return BIO_CHECK_ERROR.NOT_MATCHED;

        case UNKNOWN_CPF:
            return BIO_CHECK_ERROR.UNKNOWN_CPF;

        case TIMEOUT:
            return BIO_CHECK_ERROR.TIMEOUT;

        case UNKNOWN:
        default:
            return BIO_CHECK_ERROR.UNKNOWN;
        }       
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
            bio = new Biometrics("C:\\Teste\\biodb.db");
            if (bio.openScanner() == false)
                throw new IOException("Finger Scanner not found.");            
            
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
                bioServer.run();

            } catch (Exception ex) {
                Logger.getLogger(
                        BioServer.class.getName()).log(Level.SEVERE, null, ex); 
            }
        }
    }
}