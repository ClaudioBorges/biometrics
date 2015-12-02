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
import util.FilesLib;

/**
 *
 * @author Claudio
 */
public class BioApp {
    
    public final PropertyValues prop;
    
    public BioApp(String f) throws IOException {
        prop = new PropertyValues(f);
    }
    
    public final class PropertyValues {
        private String fDatabasePath = "";
        private String fPhotoPath = "";
        private String fPreparePath = "";
        
        public String getfDatabasePath() {
            return fDatabasePath;
        }

        public String getfPhotoPath() {
            return fPhotoPath;
        }

        public String getfPreparePath() {
            return fPreparePath;
        }
        
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
                fPhotoPath      = prop.getProperty("BIO_APP_PHOTO_PATH");
                fPreparePath    = prop.getProperty("BIO_APP_PREPARE_CANDIDATE_PATH");
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

        String configFile; 
        
        if (args.length > 1) {
            System.out.println("usage: \"config_file\"");
            return;
        } else if (args.length == 0) {
            configFile = "../_default_files/BioApp/BioApp.properties";
            System.out.println("Config file: " + configFile);
        } else {
            configFile = args[0];
        }  
        
        BioApp app;
        
        try {
            app = new BioApp(configFile);
            
            FilesLib.creteDir(app.prop.fDatabasePath);
            FilesLib.creteDir(app.prop.fPhotoPath);
 
            BioAppForm bioAppForm = new BioAppForm(
                app.prop.getfDatabasePath(), 
                app.prop.getfDatabasePath(),
                app.prop.getfPreparePath());
            
        bioAppForm.setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(BioApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
