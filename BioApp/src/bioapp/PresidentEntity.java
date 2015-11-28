/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioapp;

import static cpflib.CPFLib.formatCPF_onlyNumbers;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

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
    
    private static final int F_CREATED_AT = 2;
    private static final int F_REGISTERED_AT = 3;
    private static final int F_FINISHED_AT = 4;
    private static final int F_CPF = 5;
    private static final int F_STATE = 6;
    private static final int F_PHOTO = 7;
    
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
    
    public void registerCandidateIn(String cpf, String photo) {
        try {
            Date date = new Date();
            
            PreparedStatement stmt = this.conn.prepareStatement(
                    "UPDATE BioPresidentEntity SET registered_at = ?, state = ?, photo = ?"
                    + "WHERE cpf = ? ");
            
            stmt.setString(1, getDateTime(date));            
            stmt.setInt(2, CANDIDATE_STATE.IN_TEST.ordinal());
            stmt.setString(3, photo);
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
    
    public void updatePhoto(String cpf, String photo) {
        try {
            PreparedStatement stmt = this.conn.prepareStatement(
                    "UPDATE BioPresidentEntity SET photo = ?"
                    + "WHERE cpf = ? ");
            
            stmt.setString(1, photo);
            stmt.setString(2, cpf);
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
    
    public ResultSet getCandidateFromCPF(String cpf) throws SQLException {
        Statement stmt = this.conn.createStatement();
        String query = "SELECT * from BioPresidentEntity WHERE cpf = " 
                        + formatCPF_onlyNumbers(cpf)
                        + ";";
        
        ResultSet rs = stmt.executeQuery(query);
        
        return rs;
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
    
    public JSONObject buildJSON_Candidate(
        String created_at,
        String registered_at,
        String finished_at,
        String status) {
        
        JSONObject jsonObj = new JSONObject();
        
        jsonObj.put("created_at",       created_at);
        jsonObj.put("registered_at",    registered_at);
        jsonObj.put("finished_at",      finished_at);
        jsonObj.put("status",           status);
        
        return jsonObj;
    }
    
    public HashMap<String, String> getPhotosPath(ResultSet rs) {
        HashMap<String, String> map = new HashMap<>();
        
        try {
            //  Get row data
            while (rs.next()) {
                
                String cpf = "";
                String photo = "";
                
                int columns = rs.getMetaData().getColumnCount();
                
                for (int i = 1; i <= columns; i++) {
                    Object o = rs.getObject(i);
                    String data = "";
                    
                    if (o != null) {
                        if (o instanceof String) {
                            data = (String)o;
                        } else if (o instanceof Integer) {
                            data = ((Integer) o).toString();
                        }
                    }
                    
                    switch (i) {                            
                        case F_CPF:
                            cpf = data;
                            break;
                            
                        case F_PHOTO:
                            photo = data;
                            break;
                    }
                }
                
                map.put(cpf, photo);
            }
        } catch (SQLException ex) {
            Logger.getLogger(PresidentEntity.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(PresidentEntity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return map;
    }
    
    public JSONObject buildJSON(ResultSet rs) {
        JSONObject jsonObj = new JSONObject();
        
        try {
            //  Get row data
            while (rs.next()) {
                
                String cpf = "";
                String created_at = "";
                String registered_at = "";
                String finished_at = "";
                String state = "";
                
                int columns = rs.getMetaData().getColumnCount();
                
                for (int i = 1; i <= columns; i++) {
                    Object o = rs.getObject(i);
                    String data = "";
                    
                    if (o != null) {
                        if (o instanceof String) {
                            data = (String)o;
                        } else if (o instanceof Integer) {
                            data = ((Integer) o).toString();
                        }
                    }
                    
                    switch (i) {
                        case F_CREATED_AT:
                            created_at = data;
                            break;
                            
                        case F_REGISTERED_AT:
                            registered_at = data;
                            break;
                            
                        case F_FINISHED_AT:
                            finished_at = data;
                            break;
                            
                        case F_CPF:
                            cpf = data;
                            break;
                            
                        case F_STATE:
                            state = data;
                            break;
                    }
                }
                
                JSONObject candidateJson = buildJSON_Candidate(
                            created_at, registered_at, finished_at, state);
                    
                jsonObj.put(cpf, candidateJson);
            }
        } catch (SQLException ex) {
            Logger.getLogger(PresidentEntity.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(PresidentEntity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return jsonObj;
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
