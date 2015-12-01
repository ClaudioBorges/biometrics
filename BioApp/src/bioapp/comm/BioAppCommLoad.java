/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioapp.comm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author Claudio
 */
public class BioAppCommLoad {

    private String appDatabasePath;
    private String appDatabaseName;
    
    private String commProtocol;
    private String commHost;
    private int commPort;
    private String commTrail;
    private String commUsr;
    private String commPwd;
    
    private String keystorePath;
    private String keystoreStorePwd;
    private String keystoreKeyPwd;

    public String getAppDatabasePath() {
        return appDatabasePath;
    }

    public String getAppDatabaseName() {
        return appDatabaseName;
    }
    
    public String getCommProtocol() {
        return commProtocol;
    }

    public String getCommHost() {
        return commHost;
    }
    
    public int getCommPort() {
        return commPort;
    }
    
    public String getCommTrail() {
        return commTrail;
    }
    
    public String getCommUsr() {
        return commUsr;
    }

    public String getCommPwd() {
        return commPwd;
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

    public BioAppCommLoad(String filename)
            throws FileNotFoundException,
            NumberFormatException,
            IOException {
        
        try (InputStream is = new FileInputStream(filename)) {
            Properties prop = new Properties();
            prop.load(is);
            
            appDatabasePath = prop.getProperty("BIO_APP_DATABASE_PATH");
            appDatabaseName = prop.getProperty("BIO_APP_DATABASE_NAME");
            
            commProtocol = prop.getProperty("BIO_APP_COMM_PROTOCOL");
            commHost = prop.getProperty("BIO_APP_COMM_HOST");
            commPort = Integer.parseInt(prop.getProperty("BIO_APP_COMM_PORT"));
            commTrail = prop.getProperty("BIO_APP_COMM_TRAIL");
            commUsr = prop.getProperty("BIO_APP_COMM_USR");
            commPwd = prop.getProperty("BIO_APP_COMM_PWD");

            keystorePath = prop.getProperty("BIO_APP_KEYSTORE_PATH");
            keystoreStorePwd = prop.getProperty("BIO_APP_KEYSTORE_STORE_PWD");
            keystoreKeyPwd = prop.getProperty("BIO_APP_KEYSTORE_KEY_PWD");
        }
    }
}
