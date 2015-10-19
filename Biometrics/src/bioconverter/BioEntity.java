/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioconverter;

import com.neurotec.biometrics.NSubject;
import com.neurotec.io.NBuffer;
import java.io.ByteArrayInputStream;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Claudio
 */
public class BioEntity {
    
    private Connection conn = null;
    
    public BioEntity(String filename) 
            throws ClassNotFoundException, SQLException {
        
        Class.forName("org.sqlite.JDBC");
        this.conn = DriverManager.getConnection("jdbc:sqlite:" + filename);
        
        initDB();
    }
    
    public void close() {
        try {
            if (conn != null) this.conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(BioEntity.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    private void initDB() {
        try (Statement stmt = this.conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS People ("
                        + "cpf varchar(14) PRIMARY KEY NOT NULL,"
                        + "name varchar(70) NOT NULL,"
                        + "data BLOB);");
        } catch (SQLException e) {
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
        
    public boolean insert(String cpf, String name, byte[] data) {
        if (this.isEntry(cpf) == true)
            return false;
        
        try (PreparedStatement stmt = this.conn.prepareStatement(
                "INSERT INTO People(cpf, name, data) "
                        + "VALUES (?, ?, ?)")) {
            
            stmt.setString(1, cpf);
            stmt.setString(2, name);
            stmt.setBinaryStream(
                    3, new ByteArrayInputStream(data), data.length);
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
            
        } catch (SQLException e) {
            
        }
        
        return name;
    }
    
    public byte[] getData(String cpf) {
        String query = "SELECT data FROM People WHERE cpf = \"" + cpf + "\";";
        byte[] data  = null;
        
        try (Statement stmt = this.conn.createStatement(); 
            ResultSet rs = stmt.executeQuery(query)) {            
            if (rs.next())
                data = rs.getBytes(1);
            
        } catch (SQLException e) {
            
        }
        
        return data;      
    }
    
    boolean addPerson(String cpf, NSubject subject) {
        byte[] data = subject.getTemplate().save().toByteArray();        
        return insert(cpf, "", data);
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
