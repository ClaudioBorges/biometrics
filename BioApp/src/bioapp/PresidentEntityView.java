/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioapp;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Claudio
 */
public class PresidentEntityView {
    
    private static JTable showResultSet(ResultSet rs) {
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
                        } else {
                            row.addElement(new String(""));
                        }
                    } else {
                        row.addElement(new String(""));
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
        
        
        return newTable;
        //if (table != null)
        //    scrollPane.getViewport().remove(table);
        //scrollPane.getViewport().add(newTable);
        //table = newTable;
    }

    public static JTable showAllCandidates(PresidentEntity entity) {
        JTable table = null;
        
        try {
            ResultSet rs = entity.getAllCandidates();
            table = showResultSet(rs);
        } catch (Exception e) {
            System.out.println(e);
        }
        
        return table;
    }
    
    public static JTable showNotVerifiedCandidates(PresidentEntity entity) {
        JTable table = null;
        
        try {
            ResultSet rs = entity.getNotVerifiedCandidates();
            table = showResultSet(rs);
        } catch (Exception e) {
            System.out.println(e);
        }
        
        return table;
    }
    
    public static JTable showRegisteredCandidates(PresidentEntity entity) {
        JTable table = null;
        
        try {
            ResultSet rs = entity.getRegisteredCandidates();
            table = showResultSet(rs);
        } catch (Exception e) {
            System.out.println(e);
        }
        
        return table;
    }

    public static JTable showFinishedCandidates(PresidentEntity entity) {
        JTable table = null;
        
        try {
            ResultSet rs = entity.getFinishedCandidates();
            table = showResultSet(rs);
        } catch (Exception e) {
            System.out.println(e);
        }
        
        return table;
    }
    
    public static JTable showCandidateFromCPF(PresidentEntity entity, String cpf) {
        JTable table = null;
        
        try {
            ResultSet rs = entity.getCandidateFromCPF(cpf);
            table = showResultSet(rs);
        } catch (Exception e) {
            System.out.println(e);
        }
        
        return table;
    }
}
