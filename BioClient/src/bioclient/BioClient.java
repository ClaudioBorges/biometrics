/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Claudio
 */
public class BioClient {

    public enum BIO_CLIENT_SM {
      INITIALIZING,
      REQ_FROM_CPF,
      WAITING_REQ_FROM_CPF,
      WAITING_RSP_FROM_CPF,
      FINISHING,
      DONE
    };
    
    public enum BIO_CHECK_ERROR {
      MATCHED,
      NOT_MATCHED,
      UNKNOWN_CPF,
      TIMEOUT,
      CONNECTION_LOST,
      CONNECTION_UNREACHED,
      UNKNOWN
    };
  
    private static final int SERVER_PORT = 1765;
    private static final String HOSTNAME = "localhost";
    
    private static final String init = "HELLO";
    private static final String ping = "PING";
    private static final String pong = "PONG";
    private static final String reqBioCpf = "REQ_biometric_from_cpf:";
    private static final String rspBioCpf = "RSP_biometric_from_cpf:";
    private static final String eoc = "BYE";
    
    static void dbgMsg(String s) {
        System.out.println("[DEBUG] " + s);
    }
    
    public static BIO_CHECK_ERROR reqBioFromCpf(String cpf) {
        BIO_CLIENT_SM clientSM = BIO_CLIENT_SM.INITIALIZING;
        BIO_CHECK_ERROR err = BIO_CHECK_ERROR.UNKNOWN;
        
        Socket socket = null;
        
        try {
            String fromServer;
            
            socket = new Socket(HOSTNAME, SERVER_PORT);
            socket.setSoTimeout(25000);
            
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                        socket.getInputStream()));        
        
            while (clientSM != BIO_CLIENT_SM.DONE) {
                switch (clientSM) {
                    case INITIALIZING:   
                        dbgMsg(clientSM.toString());
                        
                        clientSM = BIO_CLIENT_SM.REQ_FROM_CPF;
                        break;

                    case REQ_FROM_CPF:
                        dbgMsg(clientSM.toString());
                        
                        out.println(reqBioCpf);
                        out.println(cpf);

                        clientSM = BIO_CLIENT_SM.WAITING_REQ_FROM_CPF;
                        break;

                    case WAITING_REQ_FROM_CPF:
                        dbgMsg(clientSM.toString());
                        
                        fromServer = in.readLine();
                        if (fromServer.contains(rspBioCpf)) {
                            clientSM = BIO_CLIENT_SM.WAITING_RSP_FROM_CPF;
                        }
                        break;

                    case WAITING_RSP_FROM_CPF:
                        dbgMsg(clientSM.toString());
                        
                        fromServer = in.readLine();
                        for (BIO_CHECK_ERROR e : BIO_CHECK_ERROR.values()) {
                            if (fromServer.equals(e.toString())) {
                                err = e;
                                break;
                            }
                        }
                        
                        clientSM = BIO_CLIENT_SM.FINISHING;
                        break;
                    
                    case FINISHING:
                        dbgMsg(clientSM.toString());
                        
                        socket.close();
                        clientSM = BIO_CLIENT_SM.DONE;
                        break;
                    
                    default:
                    case DONE:
                        break;
                }
            }
        } catch (SocketTimeoutException e) {
            err = BIO_CHECK_ERROR.TIMEOUT;
        } catch (IOException e) {
            if (clientSM == BIO_CLIENT_SM.INITIALIZING)
                err =  BIO_CHECK_ERROR.CONNECTION_UNREACHED;
            else
                err = BIO_CHECK_ERROR.CONNECTION_LOST;
        } catch (Exception e) {
            err = BIO_CHECK_ERROR.UNKNOWN;
        } finally {
            if (socket != null) try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(
                        BioClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return err;
    }    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        while (true) {
            String fromUser;
            BufferedReader stdIn = new BufferedReader(
                            new InputStreamReader(System.in));

            System.out.println("Digite o cpf para consulta: ");
                        
            try {
                fromUser = stdIn.readLine();
                if (fromUser.contains("exit"))
                    break;

                System.out.format("RSP: %s", reqBioFromCpf(fromUser));
                System.out.println("\n\n");
            } 
            catch (IOException ex) {
                Logger.getLogger(BioClient.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }
}
