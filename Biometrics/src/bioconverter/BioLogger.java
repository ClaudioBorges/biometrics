/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioconverter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Claudio
 */
public class BioLogger {
    
    private final Connection conn;
    private final int source;
    
    public BioLogger(String filename, int source) 
            throws ClassNotFoundException, SQLException {
        
        Class.forName("org.sqlite.JDBC");
        this.conn = DriverManager.getConnection("jdbc:sqlite:" + filename);
        
        this.source = source;
        
        initDB();
    }
    
    public void close() {
        try {
            if (this.conn != null) this.conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(BioEntity.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    private void initDB() {
        try (Statement stmt = this.conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS BioLogger ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "date VARCHAR(10) NOT NULL,"
                        + "time VARCHAR(8)  NOT NULL,"
                        + "source INTEGER NOT NULL,"
                        + "cpf VARCHAR(14),"
                        + "credentialSent VARCHAR(1) CHECK(credentialSent IN ('C', 'E', 'P', 'U')) DEFAULT 'U',"
                        + "credentialFromDB VARCHAR(1) CHECK(credentialFromDB IN ('C', 'E', 'P', 'U')) DEFAULT 'U',"
                        + "state INTEGER);");
        } catch (SQLException ex) {
            Logger.getLogger(BioLogger.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    private static String getDate(Date date) {
        SimpleDateFormat dateFormat = 
                new SimpleDateFormat("yyyy-MM-dd") ;
        return dateFormat.format(date);
    } 

    private static String getTime(Date date) {
        SimpleDateFormat dateFormat = 
                new SimpleDateFormat("HH-mm-ss") ;
        return dateFormat.format(date);
    } 

    public void addBioEvent(
            String cpf, 
            Biometrics.BIOMETRIC_STATE state) {
        
        try {
            Date date = new Date();
            
            PreparedStatement stmt = this.conn.prepareStatement(
                    "INSERT INTO BioLogger(date, time, source, cpf, state) "
                    + "VALUES (?, ?, ?, ?, ?)");
            
            stmt.setString(1, getDate(date));
            stmt.setString(2, getTime(date));
            stmt.setInt(3, this.source);
            stmt.setString(4, cpf);
            stmt.setString(5, state.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            Logger.getLogger(BioLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addBioEvent(
            String cpf, 
            BioEntity.PERSON_CREDENTIAL credentialSent, 
            BioEntity.PERSON_CREDENTIAL credentialFromDB, 
            Biometrics.BIOMETRIC_STATE state) {
        
        try {
            Date date = new Date();
            
            PreparedStatement stmt = this.conn.prepareStatement(
                    "INSERT INTO BioLogger(date, time, source, cpf, credentialSent, credentialFromDB, state) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)");
            
            stmt.setString(1, getDate(date));
            stmt.setString(2, getTime(date));
            stmt.setInt(3, this.source);
            stmt.setString(4, cpf);
            stmt.setString(5, credentialSent.getValue());
            stmt.setString(6, credentialFromDB.getValue());
            stmt.setString(7, state.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            Logger.getLogger(BioLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        BioLogger logger = new BioLogger("C:\\Teste\\BioLogger.db", 0);
        
        logger.addBioEvent(
                "38502729829", 
                BioEntity.PERSON_CREDENTIAL.EXAMINER, 
                BioEntity.PERSON_CREDENTIAL.EXAMINER, 
                Biometrics.BIOMETRIC_STATE.TIMEOUT);
        logger.addBioEvent(
                "38502729829", 
                BioEntity.PERSON_CREDENTIAL.EXAMINER, 
                BioEntity.PERSON_CREDENTIAL.EXAMINER, 
                Biometrics.BIOMETRIC_STATE.TIMEOUT);
        logger.addBioEvent(
                "38502729829", 
                BioEntity.PERSON_CREDENTIAL.EXAMINER,
                BioEntity.PERSON_CREDENTIAL.EXAMINER, 
                Biometrics.BIOMETRIC_STATE.TIMEOUT);

        logger.addBioEvent(
                "38502729829",  
                Biometrics.BIOMETRIC_STATE.TIMEOUT);    
    }    
}
