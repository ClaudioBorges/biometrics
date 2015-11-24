/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Claudio
 */
public class BioApp {
    
    private static final String configPath = "..\\_default_files\\BioApp\\BioApp.properties";
    
    public final PropertyValues prop;
    
    public BioApp(String f) throws IOException {
        prop = new PropertyValues(f);
    }
    
    public final class PropertyValues {
        public String fDatabasePath = "";
        public String fDatabaseName = "";
        public String fPhotoPath = "";
        
        public PropertyValues(String f) throws IOException {
            updatePropValues(f);
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
                
                fDatabasePath   = prop.getProperty("BIO_APP_DATABASE_PATH");
                fDatabaseName   = prop.getProperty("BIO_APP_DATABASE_NAME");
                fPhotoPath  = prop.getProperty("BIO_APP_PHOTO_PATH");
            } catch (IOException | NumberFormatException ex) {
                Logger.getLogger(
                        BioApp.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (is != null) is.close();
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        BioApp app;
        
        String fDatabase = "";
        String fPhotoPath = "";
                
        try {
            app = new BioApp(configPath);
            
            File dir = new File(app.prop.fDatabasePath);
            if (dir.exists()== false)
                dir.mkdirs();
            
            fDatabase = app.prop.fDatabasePath + "\\" + app.prop.fDatabaseName;
            fPhotoPath = app.prop.fPhotoPath;
            
            // Teste client
            //BioAppClient client = new BioAppClient();
            //System.out.print(
            //        client.sendLog(
            //                client.openConnection("localhost", 8888), fDatabase, "Claudio", "123"));
        } catch (IOException ex) {
            Logger.getLogger(BioApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Teste
        BioAppForm bioAppForm = new BioAppForm(fDatabase, fPhotoPath);
        bioAppForm.setVisible(true);
    }
    
}
