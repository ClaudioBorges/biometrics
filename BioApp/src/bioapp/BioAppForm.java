/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioapp;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.text.MaskFormatter;

/**
 *
 * @author Claudio
 */
public class BioAppForm extends javax.swing.JFrame {
    
    private PresidentEntity entity = null;
    private String photoPath;
    
    private PresidentEntityView entityView = null;
    private String workingCpf = null;
    
    private boolean isEditingCandidate = false;    
   
    private static final String sRegisterUsr = "Registro de candidato";
    private static final String sRegisterUsr_In = "Registrar entrada";
    private static final String sRegisterUsr_Out = "Registrar saída";
    private static final String sRegisterUsr_Photo = "Tirar uma nova foto";
    
    private javax.swing.JPanel jPanel;
    private javax.swing.JFormattedTextField jTextEd_Cpf;
    private javax.swing.JMenu jMenu_File;
    private javax.swing.JMenu jMenu_Show;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenuItem jMenuItem_Exit;
    private javax.swing.JMenuItem jMenuItem_ShowDB;
    private javax.swing.JScrollPane jScroll_Usr;
    private javax.swing.JButton jBtn_Search;
    private javax.swing.JSeparator jSeparator;
    
    /**
     * Creates new form BioAppForm
     * @param logFilename
     * @param photoPath
     */
    public BioAppForm(String logFilename, String photoPath) {
        try {
            this.entity = new PresidentEntity(logFilename);
            this.photoPath = photoPath;
            
            initMyComponents();
            
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(BioAppForm.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    private void initMyComponents() {        

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel = new javax.swing.JPanel();        
        jBtn_Search = new javax.swing.JButton();
        jSeparator = new javax.swing.JSeparator();
        jScroll_Usr = new javax.swing.JScrollPane();
        jMenuBar = new javax.swing.JMenuBar();
        jMenu_File = new javax.swing.JMenu();
        jMenuItem_Exit = new javax.swing.JMenuItem();
        jMenu_Show = new javax.swing.JMenu();
        jMenuItem_ShowDB = new javax.swing.JMenuItem();
        
        jBtn_Search.setText("Buscar Candidato");
        jBtn_Search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSearchBtnActionPerformed(evt);
            }
        });

        try {
            MaskFormatter cpfFormatter;
            cpfFormatter = new MaskFormatter("###.###.###-##");
            cpfFormatter.setValidCharacters("0123456789");
            
            jTextEd_Cpf = new javax.swing.JFormattedTextField(cpfFormatter);
        } catch (ParseException ex) {
            Logger.getLogger(BioAppForm.class.getName()).log(Level.SEVERE, null, ex);
        }

        jMenu_File.setText("Arquivo");
        jMenuItem_Exit.setText("Sair");
        jMenuItem_Exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ExitActionPerformed(evt);
            }
        });
        jMenu_File.add(jMenuItem_Exit);
        jMenuBar.add(jMenu_File);
        jMenu_Show.setText("Exibir");
        jMenuItem_ShowDB.setText("Dados de candidatos");
        jMenuItem_ShowDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ShowDBActionPerformed(evt);
            }
        });
        jMenu_Show.add(jMenuItem_ShowDB);
        jMenuBar.add(jMenu_Show);
        setJMenuBar(jMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jPanel);
        jPanel.setLayout(layout);
        jPanel.setMinimumSize(new Dimension(500, 380));
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(152, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(
                        javax.swing.GroupLayout.Alignment.LEADING, 
                        false)
                    .addComponent(jBtn_Search, 
                            javax.swing.GroupLayout.DEFAULT_SIZE, 
                            javax.swing.GroupLayout.DEFAULT_SIZE, 
                            Short.MAX_VALUE)
                    .addComponent(jTextEd_Cpf))
                .addContainerGap(152, Short.MAX_VALUE))
            .addComponent(jScroll_Usr, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jSeparator, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jBtn_Search)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextEd_Cpf, 
                        javax.swing.GroupLayout.PREFERRED_SIZE, 
                        javax.swing.GroupLayout.DEFAULT_SIZE, 
                        javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator, 
                        javax.swing.GroupLayout.PREFERRED_SIZE, 
                        5, 
                        javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScroll_Usr, 
                        javax.swing.GroupLayout.DEFAULT_SIZE, 
                        186, 
                        Short.MAX_VALUE))
        );
        
        javax.swing.GroupLayout mainLayout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(mainLayout);
        mainLayout.setHorizontalGroup(
            mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel, 
                        javax.swing.GroupLayout.DEFAULT_SIZE, 
                        javax.swing.GroupLayout.DEFAULT_SIZE, 
                        Short.MAX_VALUE)
                .addContainerGap())
        );
        mainLayout.setVerticalGroup(
            mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel, 
                        javax.swing.GroupLayout.DEFAULT_SIZE, 
                        javax.swing.GroupLayout.DEFAULT_SIZE, 
                        Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }
    
     private void jMenuItem_ExitActionPerformed(java.awt.event.ActionEvent evt) {                                               
        // TODO add your handling code here:
    }                                              

    private void jMenuItem_ShowDBActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        if (entityView == null) {
            JFrame form = new PresidentEntityForm(entity);
            
            form.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    entityView = null;
                }
            });
            
            form.setVisible(true);
        }
    }                                                

    private void jSearchBtnActionPerformed(java.awt.event.ActionEvent evt) {                                           
        workingCpf = cpflib.CPFLib.formatCPF_onlyNumbers(jTextEd_Cpf.getText());
        
        if (cpflib.CPFLib.isCPF(workingCpf) ==  false)
            return;
        
        JTable table = PresidentEntityView.showCandidateFromCPF(entity, workingCpf);
        table.setEnabled(false);
        
        table.addMouseListener(new MouseAdapter() {
            @Override 
            public void mouseReleased(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                if (r >= 0 && r < table.getRowCount()) {
                    table.setRowSelectionInterval(r, r);
                } else {
                    table.clearSelection();
                }

                int rowindex = table.getSelectedRow();
                if (rowindex < 0)
                    return;
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
                    JPopupMenu popup = createCandidatePopup(entity);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            
            private void createFolder(String path) {

                File dir = new File(path);

                if (dir.exists() == false) {
                    dir.mkdirs(); 
                }
            }
            
            private JPopupMenu createCandidatePopup(PresidentEntity entity) {
                JPopupMenu popup = new JPopupMenu();
                
                ActionListener menuListener = (ActionEvent event) -> {

                    String completePhotoPath = photoPath + workingCpf + "\\";
                    createFolder(completePhotoPath);
                    
                    switch (event.getActionCommand()) {
                        case sRegisterUsr_In:                            
                            isEditingCandidate = true;                            
                            setEnabledWorkingComponents(false);
                            
                            BioRegUsr regUsrIn 
                                    = new BioRegUsr(
                                            BioRegUsr.REG_USR_MODE.REG_INPUT, 
                                            workingCpf, 
                                            entity, 
                                            completePhotoPath);
                            regUsrIn.setVisible(true);
                            
                            regUsrIn.addWindowListener(new java.awt.event.WindowAdapter() {
                                @Override
                                public void windowClosing(java.awt.event.WindowEvent e) {
                                    
                                    isEditingCandidate = false;
                                    setEnabledWorkingComponents(true);
                                }
                            });
                            break;
                            
                        case sRegisterUsr_Out:
                            isEditingCandidate = true;                            
                            setEnabledWorkingComponents(false);
                            
                            BioRegUsr regUsrOut 
                                    = new BioRegUsr(
                                            BioRegUsr.REG_USR_MODE.REG_OUTPUT, 
                                            workingCpf, 
                                            entity, 
                                            completePhotoPath);
                            regUsrOut.setVisible(true);
                            
                            regUsrOut.addWindowListener(new java.awt.event.WindowAdapter() {
                                @Override
                                public void windowClosing(java.awt.event.WindowEvent e) {
                                    
                                    isEditingCandidate = false;
                                    setEnabledWorkingComponents(true);
                                }
                            });
                            break;
                            
                        case sRegisterUsr_Photo:
                            isEditingCandidate = true;                            
                            setEnabledWorkingComponents(false);
                            
                            BioRegUsr regUsrPhoto 
                                    = new BioRegUsr(
                                            BioRegUsr.REG_USR_MODE.REG_ONLY_PICTURE, 
                                            workingCpf, 
                                            entity, 
                                            completePhotoPath);
                            regUsrPhoto.setVisible(true);
                            
                            regUsrPhoto.addWindowListener(new java.awt.event.WindowAdapter() {
                                @Override
                                public void windowClosing(java.awt.event.WindowEvent e) {
                                    
                                    isEditingCandidate = false;
                                    setEnabledWorkingComponents(true);
                                }
                            });
                            break;
                    }                    
                };
                
                PresidentEntity.CANDIDATE_STATE state = entity.getCandidateState(workingCpf);
                
                JMenuItem item;
                
                popup.add(item = new JMenuItem(sRegisterUsr_In));
                item.setHorizontalTextPosition(JMenuItem.LEFT);
                item.addActionListener(menuListener);
                if (isEditingCandidate == true 
                        || state != PresidentEntity.CANDIDATE_STATE.NOT_VERIFIED)
                    item.setEnabled(false);
                
                popup.add(item = new JMenuItem(sRegisterUsr_Out));
                item.setHorizontalTextPosition(JMenuItem.LEFT);           
                item.addActionListener(menuListener);
                if (isEditingCandidate == true 
                        || state != PresidentEntity.CANDIDATE_STATE.IN_TEST)
                    item.setEnabled(false);
                
                
                popup.add(item = new JMenuItem(sRegisterUsr_Photo));
                item.setHorizontalTextPosition(JMenuItem.LEFT);           
                item.addActionListener(menuListener);
                if (isEditingCandidate == true 
                    || state == PresidentEntity.CANDIDATE_STATE.UNKNOWN
                    || state == PresidentEntity.CANDIDATE_STATE.NOT_VERIFIED) {
                    
                    item.setEnabled(false);
                }
                                
                popup.setLabel(sRegisterUsr);
                popup.setBorder(new BevelBorder(BevelBorder.RAISED));
                
                return popup;
            }
            
            class ItemListener implements ActionListener  {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    System.out.println(
                            "Selected: " + ae.getActionCommand());
                }                
            }
        });
        
        jScroll_Usr.getViewport().removeAll();
        if (table != null) {
            jScroll_Usr.getViewport().add(table);
        }
    }                                          

    private void setEnabledWorkingComponents(boolean enabled) {
        for (Component component : jPanel.getComponents()) {
            if (component instanceof Component) {
                component.setEnabled(enabled);
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info 
                    : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException 
                | InstantiationException 
                | IllegalAccessException 
                | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BioAppForm.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new BioAppForm(
                    "C:\\Teste\\BioPresidentLog.db", 
                    "C:\\Teste\\")
                    .setVisible(true);
        });
    }
}
