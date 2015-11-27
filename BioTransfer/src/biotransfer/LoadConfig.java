/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biotransfer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author Claudio
 */
public class LoadConfig {

    private String commProtocol;
    private String commHost;
    private int commPort;
    private String commUsr;
    private String commPwd;

    private String defaultPath;
    private String keystorePath;
    private String keystoreStorePwd;
    private String keystoreKeyPwd;

    private String serverStorePath;
    private String serverValidFile;

    private String clientDBPath;

    public String getCommProtocol() {
        return commProtocol;
    }

    public String getCommHost() {
        return commHost;
    }
    
    public int getCommPort() {
        return commPort;
    }

    public String getCommUsr() {
        return commUsr;
    }

    public String getCommPwd() {
        return commPwd;
    }

    public String getDefaultPath() {
        return defaultPath;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public String getKeystoreStorePwd() {
        return keystoreStorePwd;
    }

    public String getKeystoreKeyPwd() {
        return keystoreKeyPwd;
    }

    public String getServerStorePath() {
        return serverStorePath;
    }

    public String getServerValidFile() {
        return serverValidFile;
    }

    public String getClientDBPath() {
        return clientDBPath;
    }

    public LoadConfig(String filename)
            throws FileNotFoundException,
            NumberFormatException,
            IOException {
        
        try (InputStream is = new FileInputStream(filename)) {
            Properties prop = new Properties();
            prop.load(is);
            
            commProtocol = prop.getProperty("BIO_TRANSFER_COMM_PROTOCOL");
            commHost = prop.getProperty("BIO_TRANSFER_COMM_HOST");
            commPort = Integer.parseInt(prop.getProperty("BIO_TRANSFER_COMM_PORT"));
            commUsr = prop.getProperty("BIO_TRANSFER_COMM_USR");
            commPwd = prop.getProperty("BIO_TRANSFER_COMM_PWD");

            defaultPath = prop.getProperty("BIO_TRANSFER_DEFAULT_PATH");
            keystorePath = prop.getProperty("BIO_TRANSFER_KEYSTORE_PATH");
            keystoreStorePwd = prop.getProperty("BIO_TRANSFER_KEYSTORE_STORE_PWD");
            keystoreKeyPwd = prop.getProperty("BIO_TRANSFER_KEYSTORE_KEY_PWD");

            serverStorePath = prop.getProperty("BIO_TRANSFER_SERVER_STORE_PATH");
            serverValidFile = prop.getProperty("BIO_TRANSFER_SERVER_VALID_FILE");

            clientDBPath = prop.getProperty("BIO_TRANSFER_CLIENT_DB_PATH");
        }
    }
}
