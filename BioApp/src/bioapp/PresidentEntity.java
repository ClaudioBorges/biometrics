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
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 *
 * @author Claudio
 */
public class PresidentEntity {
    
    private final Connection conn;
 
    private static final int F_CREATED_AT = 2;
    private static final int F_REGISTERED_IN_AT = 3;
    private static final int F_REGISTERED_IN_CPF = 4;    
    private static final int F_REGISTERED_IN_BY = 5;
    private static final int F_REGISTERED_OUT_AT = 6;
    private static final int F_REGISTERED_OUT_CPF = 7;    
    private static final int F_REGISTERED_OUT_BY = 8;
    private static final int F_CPF = 9;
    private static final int F_PHOTO = 10;
    
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
                        + "registered_in_at TIMESTAMP,"
                        + "registered_in_cpf VARCHAR(14),"
                        + "registered_in_by TEXT CHECK(registered_in_by IN ('candidate', 'president')),"
                        + "registered_out_at TIMESTAMP,"
                        + "registered_out_cpf VARCHAR(14),"
                        + "registered_out_by TEXT CHECK(registered_out_by IN ('candidate', 'president')),"
                        + "cpf VARCHAR(14) UNIQUE NOT NULL,"
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
                    "INSERT INTO BioPresidentEntity(created_at, cpf) "
                    + "VALUES (?, ?)");
            
            stmt.setString(1, getDateTime(date));
            stmt.setString(2, cpf);
            stmt.execute();
            
        } catch (SQLException ex) {
            Logger.getLogger(PresidentEntity.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    public void registerCandidateIn(String cpf, String photo, String presidentCpf) {
        try {
            PreparedStatement stmt = this.conn.prepareStatement(
                    "UPDATE BioPresidentEntity "
                    + "SET registered_in_at = ? " 
                    + ", registered_in_cpf = ? "
                    + ", registered_in_by = ? "
                    + ", photo = ? "
                    + "WHERE cpf = ? ");
            
            Date date = new Date();
            
            stmt.setString(1, getDateTime(date));
            if (presidentCpf == null) {
                stmt.setString(2, cpf);
                stmt.setString(3, "candidate");
            } else {
                stmt.setString(2, presidentCpf);
                stmt.setString(3, "president");
            }
            stmt.setString(4, photo);            
            stmt.setString(5, cpf);
            
            stmt.execute();
            
        } catch (SQLException ex) {
            Logger.getLogger(PresidentEntity.class.getName())
                    .log(Level.SEVERE, null, ex);
        }  
    }
    
    public void registerCandidateIn(String cpf, String photo) {
        registerCandidateIn(cpf, photo, null);     
    }
    
    public void registerCandidateOut(String cpf, String presidentCpf) {
        try {
            Date date = new Date();
            
            PreparedStatement stmt = this.conn.prepareStatement(
                    "UPDATE BioPresidentEntity "
                    + "SET registered_out_at = ?"
                    + ", registered_out_cpf = ?"
                    + ", registered_out_by = ?"        
                    + " WHERE cpf = ? ");
            
            stmt.setString(1, getDateTime(date));
            if (presidentCpf == null) {
                stmt.setString(2, cpf);
                stmt.setString(3, "candidate");
            } else {
                stmt.setString(2, presidentCpf);
                stmt.setString(3, "president");
            }
            stmt.setString(4, cpf);
            stmt.execute();
            
        } catch (SQLException ex) {
            Logger.getLogger(PresidentEntity.class.getName())
                    .log(Level.SEVERE, null, ex);
        }          
    }
    
    public void registerCandidateOut(String cpf) {
        registerCandidateOut(cpf, null);      
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
    
    public enum CANDIDATE_STATE {
        NOT_VERIFIED,
        REGISTERED_IN,
        REGISTERED_OUT,
        UNKNOWN
    }
    
    public CANDIDATE_STATE getCandidateState(String cpf) {
        
        CANDIDATE_STATE state = CANDIDATE_STATE.UNKNOWN;
        PreparedStatement stmt = null;
        ResultSet rs = null;
                
        try {
            stmt = this.conn.prepareStatement(
                    "SELECT registered_in_at, registered_out_at "
                            + "from BioPresidentEntity WHERE cpf = ?;");
            
            stmt.setString(1, cpf);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                String registered_in_at = rs.getString(1);
                String registered_out_at = rs.getString(2);                
                
                if (registered_in_at == null)
                    state = CANDIDATE_STATE.NOT_VERIFIED;
                else if (registered_out_at == null)
                    state = CANDIDATE_STATE.REGISTERED_IN;
                else
                    state = CANDIDATE_STATE.REGISTERED_OUT;
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
        String cpf,
        String created_at, 
        String registered_in_at, String registered_in_cpf, String registered_in_by, 
        String registered_out_at, String registered_out_cpf, String registered_out_by) {
        
        JSONObject jsonObj = new JSONObject();
        
        jsonObj.put("cpf",                  cpf);
        jsonObj.put("created_at",           created_at);
        jsonObj.put("registered_in_at",     registered_in_at);
        jsonObj.put("registered_in_cpf",    registered_in_cpf);
        jsonObj.put("registered_in_by",     registered_in_by);        
        jsonObj.put("registered_out_at",     registered_out_at);
        jsonObj.put("registered_out_cpf",    registered_out_cpf);
        jsonObj.put("registered_out_by",     registered_out_by);
        
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
        JSONArray jsonArray = new JSONArray();
        
        try {
            //  Get row data
            while (rs.next()) {
                
                String cpf = "";
                String created_at = "";
                String registered_in_at = "";
                String registered_in_cpf = "";
                String registered_in_by = "";                
                String registered_out_at = "";
                String registered_out_cpf = "";
                String registered_out_by = "";
                
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
                            
                        case F_REGISTERED_IN_AT:
                            registered_in_at = data;
                            break;

                        case F_REGISTERED_IN_CPF:
                            registered_in_cpf = data;
                            break;
                        case F_REGISTERED_IN_BY:
                            registered_in_by = data;
                            break;
                            
                        case F_REGISTERED_OUT_AT:
                            registered_out_at = data;
                            break;

                        case F_REGISTERED_OUT_CPF:
                            registered_out_cpf = data;
                            break;
                        case F_REGISTERED_OUT_BY:
                            registered_out_by = data;
                            break;
                            
                        case F_CPF:
                            cpf = data;
                            break;
                    }
                }
                
                JSONObject candidateJson = buildJSON_Candidate(
                        cpf,
                        created_at, 
                        registered_in_at, registered_in_cpf, registered_in_by, 
                        registered_out_at, registered_out_cpf, registered_out_by);
                
                jsonArray.put(candidateJson);
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
        
        jsonObj.put("candidates", jsonArray);
        
        return jsonObj;
    }
    
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        PresidentEntity log = new PresidentEntity("C:\\Teste\\BioPresidentLog.db");
        
        log.prepareCandidate("38502729829");
        log.prepareCandidate("28895589831");
        
//        log.registerCandidateIn("38502729829", "C:\\Teste\\camera.jeg");        
//        log.registerCandidateIn("38502729800", "C:\\Teste\\camera1.jeg");               
//        log.registerCandidateIn("38502729801", "C:\\Teste\\camera1.jeg");
//        log.registerCandidateIn("38502729829", null);
//        
//        log.registerCandidateOut("385");
//        log.registerCandidateOut("38502729829");
//        log.registerCandidateOut("38502729800");
    } 
}
