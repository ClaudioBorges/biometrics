/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioapp;

import bioconverter.BioEntity;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Claudio
 */
public class PresidentEntity {
    
    private final Connection conn;
    private ArrayList<PresidentEntityRow> localTable = null;
    
    public enum CANDIDATE_STATE {
        NOT_VERIFIED,
        IN_TEST,
        TEST_COMPLETED,
        UNKNOWN
    };
    
    public class PresidentEntityRow {
        public final String cpf;
        public final String created_at;
        public final String registered_at;
        public final String finished_at;
        public final CANDIDATE_STATE state;
        public final String photo;
        
        public PresidentEntityRow(
                String cpf, 
                String created_at, 
                String registered_at, 
                String finished_at,
                CANDIDATE_STATE state,
                String photo) {
            
            this.cpf = cpf;
            this.created_at = created_at;
            this.registered_at = registered_at;
            this.finished_at = finished_at;
            this.state = state;
            this.photo = photo;
        }
    };
    
    public PresidentEntity(String filename) 
            throws ClassNotFoundException, SQLException {
        
        Class.forName("org.sqlite.JDBC");
        this.conn = DriverManager.getConnection("jdbc:sqlite:" + filename);

        initDB();
    }
    
    public void close() {
        try {
            if (this.conn != null) this.conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(PresidentEntity.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    private void initDB() {
        try (Statement stmt = this.conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS BioPresidentEntity ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "created_at TIMESTAMP NOT NULL,"
                        + "registered_at TIMESTAMP,"
                        + "finished_at TIMESTAMP,"
                        + "cpf VARCHAR(14) UNIQUE NOT NULL,"
                        + "state INTEGER NOT NULL,"
                        + "photo TEXT);");
        } catch (SQLException ex) {
            Logger.getLogger(PresidentEntity.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    private static String getDateTime(Date date) {
        SimpleDateFormat dateFormat = 
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
        return dateFormat.format(date);
    }

    public void prepareCandidate(String cpf) {        
        try {
            Date date = new Date();
            
            PreparedStatement stmt = this.conn.prepareStatement(
                    "INSERT INTO BioPresidentEntity(created_at, cpf, state) "
                    + "VALUES (?, ?, ?)");
            
            stmt.setString(1, getDateTime(date));
            stmt.setString(2, cpf);
            stmt.setInt(3, CANDIDATE_STATE.NOT_VERIFIED.ordinal());
            stmt.execute();
            
        } catch (SQLException ex) {
            Logger.getLogger(PresidentEntity.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    public void registerCandidateIn(String cpf, String photoPath) {
        try {
            Date date = new Date();
            
            PreparedStatement stmt = this.conn.prepareStatement(
                    "UPDATE BioPresidentEntity SET registered_at = ?, state = ?, photo = ?"
                    + "WHERE cpf = ? ");
            
            stmt.setString(1, getDateTime(date));            
            stmt.setInt(2, CANDIDATE_STATE.IN_TEST.ordinal());
            stmt.setString(3, photoPath);
            stmt.setString(4, cpf);
            stmt.execute();
            
        } catch (SQLException ex) {
            Logger.getLogger(PresidentEntity.class.getName())
                    .log(Level.SEVERE, null, ex);
        }        
    }
    
    public void finishCandidate(String cpf) {
        try {
            Date date = new Date();
            
            PreparedStatement stmt = this.conn.prepareStatement(
                    "UPDATE BioPresidentEntity SET finished_at = ?, state = ?"
                    + "WHERE cpf = ? ");
            
            stmt.setString(1, getDateTime(date));            
            stmt.setInt(2, CANDIDATE_STATE.TEST_COMPLETED.ordinal());
            stmt.setString(3, cpf);
            stmt.execute();
            
        } catch (SQLException ex) {
            Logger.getLogger(PresidentEntity.class.getName())
                    .log(Level.SEVERE, null, ex);
        }          
    }
    
    public CANDIDATE_STATE getCandidateState(String cpf) {
        
        CANDIDATE_STATE state = CANDIDATE_STATE.UNKNOWN;
        PreparedStatement stmt = null;
        ResultSet rs = null;
                
        try {
            stmt = this.conn.prepareStatement(
                    "SELECT state from BioPresidentEntity WHERE cpf = ?;");
            
            stmt.setString(1, cpf);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                state = CANDIDATE_STATE.values()[rs.getInt(1)];
            }            
        } catch (SQLException ex) {
            Logger.getLogger(PresidentEntity.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(PresidentEntity.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
        
        return state;
    }
    
    public ResultSet getAllCandidates() throws SQLException {
        Statement stmt = this.conn.createStatement();
        String query = "SELECT * from BioPresidentEntity";
        
        ResultSet rs = stmt.executeQuery(query);
        
        return rs;
    }
    
    public ResultSet getFinishedCandidates() throws SQLException {
        Statement stmt = this.conn.createStatement();
        String query = "SELECT * from BioPresidentEntity WHERE state = 2";
        
        ResultSet rs = stmt.executeQuery(query);
        
        return rs;
    }

    public ResultSet getRegisteredCandidates() throws SQLException {
        Statement stmt = this.conn.createStatement();
        String query = "SELECT * from BioPresidentEntity WHERE state = 1";
        
        ResultSet rs = stmt.executeQuery(query);
        
        return rs;
    }
    
    public ResultSet getNotVerifiedCandidates() throws SQLException {
        Statement stmt = this.conn.createStatement();
        String query = "SELECT * from BioPresidentEntity WHERE state = 0";
        
        ResultSet rs = stmt.executeQuery(query);
        
        return rs;        
    }
    
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        PresidentEntity log = new PresidentEntity("C:\\Teste\\BioPresidentLog.db");
        
        log.prepareCandidate("38502729829");
        log.prepareCandidate("38502729800");
        
        log.registerCandidateIn("38502729829", "C:\\Teste\\camera.jeg");        
        log.registerCandidateIn("38502729800", "C:\\Teste\\camera1.jeg");               
        log.registerCandidateIn("38502729801", "C:\\Teste\\camera1.jeg");
        log.registerCandidateIn("38502729829", null);
        
        log.finishCandidate("385");
        log.finishCandidate("38502729829");
        log.finishCandidate("38502729800");
    } 
}
