/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioapp;

import java.awt.BorderLayout;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Claudio
 */
public class PresidentEntityView extends JPanel {
    
    private final PresidentEntity entity;
    private JScrollPane scrollPane = null;
    private JTable table = null;
    
    public PresidentEntityView(PresidentEntity entity) {    
        this.entity = entity;
        
        scrollPane = new JScrollPane();
        add(scrollPane);
    }
    
    void showResultSet(ResultSet rs) {
        Vector<String> columnNames = new Vector();
        Vector<Vector<String>> data = new Vector();

        try {
            ResultSetMetaData md = rs.getMetaData();
            
            int columns = md.getColumnCount();

            //  Get column names
            for (int i = 1; i <= columns; i++) {
                columnNames.addElement(md.getColumnName(i));
            }

            //  Get row data
            while (rs.next()) {
                Vector<String> row = new Vector(columns);

                for (int i = 1; i <= columns; i++) {
                    Object o = rs.getObject(i);
                    if (o != null) {
                        if (o instanceof String) {
                            row.addElement((String) o);
                        } else if (o instanceof Integer) {
                            row.addElement(((Integer) o).toString());                        
                        }                    
                    }
                }

                data.addElement(row);
            }

            rs.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        
        TableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public Class getColumnClass(int column) {
                for (int row = 0; row < getRowCount(); row++) {
                    Object o = getValueAt(row, column);

                    if (o != null) {
                        return o.getClass();
                    }
                }

                return Object.class;
            }
        };
        
        //  Create table with database data
        JTable newTable = new JTable(model) {
            @Override
            public Class getColumnClass(int column) {
                for (int row = 0; row < getRowCount(); row++) {
                    Object o = getValueAt(row, column);

                    if (o != null) {
                        return o.getClass();
                    }
                }

                return Object.class;
            }
        };
        RowSorter<TableModel> sorter = new TableRowSorter<>(model);
        newTable.setRowSorter(sorter);
        
        if (table != null)
            scrollPane.getViewport().remove(table);
        scrollPane.getViewport().add(newTable);
        table = newTable;
    }

    void showAllCandidates() {
        try {
            ResultSet rs = entity.getAllCandidates();
            showResultSet(rs);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    void showNotVerifiedCandidates() {
        try {
            ResultSet rs = entity.getNotVerifiedCandidates();
            showResultSet(rs);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    void showRegisteredCandidates() {
        try {
            ResultSet rs = entity.getRegisteredCandidates();
            showResultSet(rs);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    void showFinishedCandidates() {
        try {
            ResultSet rs = entity.getFinishedCandidates();
            showResultSet(rs);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    public static void main(String[] args) {
    javax.swing.SwingUtilities.invokeLater(() -> {
        try {
            JFrame frame = new JFrame("any");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            PresidentEntity log1 = new PresidentEntity("C:\\Teste\\BioPresidentLog.db");
            PresidentEntityView newContentPane = new PresidentEntityView(log1);
            newContentPane.showFinishedCandidates();
            newContentPane.showAllCandidates();
            newContentPane.showNotVerifiedCandidates();            
            newContentPane.showRegisteredCandidates();
            newContentPane.setOpaque(true);
            frame.setContentPane(newContentPane);
            frame.pack();
            frame.setVisible(true);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(PresidentEntityView.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    });

    }

}
