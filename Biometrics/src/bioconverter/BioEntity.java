/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioconverter;

import com.neurotec.biometrics.NSubject;
import com.neurotec.io.NBuffer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.FilesLib;
/**
 *
 * @author Claudio
 */
public class BioEntity {
    
    private Connection conn = null;

    public enum PERSON_CREDENTIAL {
        CANDIDATE("C"),
        EXAMINER("E"),
        PRESIDENT("P"),
        UNKNOWN("U");
        
        private final String text;
               
        private PERSON_CREDENTIAL(final String text) {
            this.text = text;
        }
        
        @Override        
        public String toString() {
            return text;
        }
        
        public String getValue() {
            return text;
        }
        
        public static PERSON_CREDENTIAL getEnum(String value) {
            for(PERSON_CREDENTIAL v : values()) {
                if(v.getValue().equalsIgnoreCase(value)) {
                    return v;
                }
            }
            
            throw new IllegalArgumentException();
        }
    }
    
    private Connection BioGetConnection(String filename) 
            throws ClassNotFoundException, SQLException {
        
        Connection dbConn = null;
        
        try {            
            FilesLib.creteFile(filename);
            
            Class.forName("org.sqlite.JDBC");
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + filename);

        } catch (IOException ex) {
            Logger.getLogger(BioEntity.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return dbConn;
    }
    
    
    public BioEntity(String filename) 
            throws ClassNotFoundException, SQLException {
        
        this.conn = BioGetConnection(filename);
        initDB();
    }
    
    public void close() {
        if (this.conn != null) try {
            this.conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(BioEntity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void cloneFrom(String filename) throws IOException, ClassNotFoundException {
        Connection fromConn = null;
        
        try {
            fromConn = BioGetConnection(filename);
            
            removeAll();
            
            String query = "SELECT * FROM People;";
        
            Statement fromStmt = fromConn.createStatement(); 
            ResultSet rs = fromStmt.executeQuery(query);
            
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();

            String columnsIDs = "";
            String identifiers = "";
            for (int i = 1; i <= columns; i++) {
                String name = md.getColumnName(i);
                if (i == columns) {
                    columnsIDs = columnsIDs.concat(name);
                    identifiers += "?";
                }
                else {
                    columnsIDs = columnsIDs.concat(name + ",");
                    identifiers += "?,";
                }
            }
            
            String stmtString = "INSERT INTO People(" + columnsIDs + ") VALUES (" + identifiers + ")";
            while (rs.next()) {
                try (PreparedStatement stmt = this.conn.prepareStatement(stmtString)) {

                    for (int i = 1; i <= columns; i++) {
                        stmt.setObject(i, rs.getObject(i));                 
                    }
                    stmt.execute();
                }
            }
        } catch (SQLException ex) {
            throw new IOException("Can not open origin database: " + filename);
        } finally {
            if (fromConn != null) {
                try {
                    fromConn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(BioEntity.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }        
    }
    
    private void initDB() {
        try (Statement stmt = this.conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS People ("
                        + "cpf varchar(14) PRIMARY KEY NOT NULL,"
                        + "name varchar(70) NOT NULL,"
                        + "data BLOB,"
                        + "credential TEXT CHECK(credential IN ('C', 'I', 'P')) NOT NULL DEFAULT 'C');");
        } catch (SQLException ex) {
            Logger.getLogger(BioEntity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean isOpen() {
        try {
            return (this.conn.isClosed() == false);
        } catch (SQLException ex) {
            Logger.getLogger(BioEntity.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    public boolean insert(
            String cpf, String name, byte[] data, PERSON_CREDENTIAL credential) {
        
        if (this.isEntry(cpf) == true)
            return false;
        
        try (PreparedStatement stmt = this.conn.prepareStatement(
                "INSERT INTO People(cpf, name, data, credential) "
                        + "VALUES (?, ?, ?, ?)")) {
            
            stmt.setString(1, cpf);
            stmt.setString(2, name);
            stmt.setBinaryStream(
                    3, new ByteArrayInputStream(data), data.length);
            stmt.setString(4, credential.toString());
            stmt.execute();
            
            return true;            
        } catch (SQLException | NullPointerException e) {
        }
        
        return false;
    }
    
    public void remove(String cpf) {
        try (Statement stmt = this.conn.createStatement()) {            
            stmt.executeUpdate(
                    "DELETE FROM People WHERE cpf = \"" + cpf + "\";");
        } catch (SQLException e) {
            
        }
    }
    
    public void removeAll() {
        try (Statement stmt = this.conn.createStatement()) {            
            stmt.executeUpdate(
                    "DELETE FROM People;");
        } catch (SQLException e) {
            
        } 
    }
       
    public boolean isEntry(String cpf) {
        String query = "SELECT cpf FROM People WHERE cpf = \"" + cpf + "\";";
        
        try (Statement stmt = this.conn.createStatement(); 
            ResultSet rs = stmt.executeQuery(query)) { 
            
            return (rs.isClosed() == false);
            
        } catch (SQLException e) {
            
        }
        
        return false;
    }
    
    public String getName(String cpf) {
        String query = "SELECT name FROM People WHERE cpf = \"" + cpf + "\";";
        String name = null;
        
        try (Statement stmt = this.conn.createStatement(); 
            ResultSet rs = stmt.executeQuery(query)) { 
            
            if (rs.next())
                name = rs.getString(1);
            
        } catch (SQLException ex) {
            Logger.getLogger(
                BioEntity.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return name;
    }
    
    public PERSON_CREDENTIAL getCredential(String cpf) {
        String query = "SELECT credential FROM People WHERE cpf = \"" + cpf + "\";";
        PERSON_CREDENTIAL credential = PERSON_CREDENTIAL.UNKNOWN;
        
        try (Statement stmt = this.conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next())
                credential = PERSON_CREDENTIAL.getEnum(rs.getString(1));
        } catch (SQLException | IllegalArgumentException ex) {
            Logger.getLogger(
                BioEntity.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return credential;
    }
    
    public byte[] getData(String cpf) {
        String query = "SELECT data FROM People WHERE cpf = \"" + cpf + "\";";
        byte[] data  = null;
        
        try (Statement stmt = this.conn.createStatement(); 
            ResultSet rs = stmt.executeQuery(query)) {            
            if (rs.next())
                data = rs.getBytes(1);
            
        } catch (SQLException ex) {
            Logger.getLogger(
                BioEntity.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return data;      
    }
    
    public boolean isCandidate(String CPF) {
        return (getCredential(CPF) == PERSON_CREDENTIAL.CANDIDATE);
    }
    
    public boolean isExaminer(String CPF) {
        return (getCredential(CPF) == PERSON_CREDENTIAL.EXAMINER);
    }
    
    public boolean isPresident(String CPF) {
        return (getCredential(CPF) == PERSON_CREDENTIAL.PRESIDENT);
    }
    
    boolean addPerson(String cpf, NSubject subject) {
        byte[] data = subject.getTemplate().save().toByteArray();        
        return insert(cpf, "", data, PERSON_CREDENTIAL.CANDIDATE);
    }
    
    NSubject getPerson(String cpf) {
        NSubject subject = null;

        byte[] data = getData(cpf);
        if (data != null) {
            subject = new NSubject();

            subject.setTemplateBuffer(new NBuffer(data));
            subject.setId(cpf);
        }

        return subject;
    }
}
