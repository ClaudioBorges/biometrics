/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioapp;

import bioconverter.BioEntity;
import bioconverter.BioLogger;
import bioconverter.Biometrics;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import bioconverter.Biometrics.BIOMETRIC_STATE;

/**
 *
 * @author Claudio
 */
public class PresidentLog {
    private final Connection conn;
    
    public PresidentLog(String filename) 
            throws ClassNotFoundException, SQLException {
        
        Class.forName("org.sqlite.JDBC");
        this.conn = DriverManager.getConnection("jdbc:sqlite:" + filename);
        
        initDB();
    }
    
    public void close() {
        try {
            if (this.conn != null) this.conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(PresidentLog.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    private void initDB() {
        try (Statement stmt = this.conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS BioPresidentEntity ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "date VARCHAR(10) NOT NULL,"
                        + "time VARCHAR(8)  NOT NULL,"
                        + "cpf VARCHAR(14) NOT NULL,"
                        + "state INTEGER NOT NULL,"
                        + "photo TEXT);");
        } catch (SQLException ex) {
            Logger.getLogger(PresidentLog.class.getName())
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

    public void addEvent(
            String cpf, 
            BIOMETRIC_STATE state) {
        
        try {
            Date date = new Date();
            
            PreparedStatement stmt = this.conn.prepareStatement(
                    "INSERT INTO BioPresidentEntity(date, time, cpf, state) "
                    + "VALUES (?, ?, ?, ?)");
            
            stmt.setString(1, getDate(date));
            stmt.setString(2, getTime(date));
            stmt.setString(3, cpf);
            stmt.setString(4, state.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            Logger.getLogger(PresidentLog.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    public void addEvent(
            String cpf, 
            BIOMETRIC_STATE state,
            String photoPath) {
        
        try {
            Date date = new Date();
            
            PreparedStatement stmt = this.conn.prepareStatement(
                    "INSERT INTO BioPresidentEntity(date, time, cpf, state, photo) "
                    + "VALUES (?, ?, ?, ?, ?)");
            
            stmt.setString(1, getDate(date));
            stmt.setString(2, getTime(date));
            stmt.setString(3, cpf);
            stmt.setString(4, state.toString());
            stmt.setString(5, photoPath);
            stmt.execute();
            
        } catch (SQLException ex) {
            Logger.getLogger(PresidentLog.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        PresidentLog log = new PresidentLog("C:\\Teste\\BioPresidentLog.db");
        
        log.addEvent("38502729829", BIOMETRIC_STATE.UNKNOWN);
        log.addEvent("38502729829", BIOMETRIC_STATE.UNKNOWN, "C:\\Teste\\Image\\image.jpeg");
        log.addEvent("38502729829", BIOMETRIC_STATE.UNKNOWN);
        log.addEvent("38502729829", BIOMETRIC_STATE.UNKNOWN, "C:\\Teste\\Image\\image.jpeg");
        log.addEvent("38502729829", BIOMETRIC_STATE.UNKNOWN);
        log.addEvent("38502729829", BIOMETRIC_STATE.UNKNOWN, "C:\\Teste\\Image\\image.jpeg");
        log.addEvent("38502729829", BIOMETRIC_STATE.UNKNOWN);
        log.addEvent("38502729829", BIOMETRIC_STATE.UNKNOWN, "C:\\Teste\\Image\\image.jpeg");
   
    } 
}
