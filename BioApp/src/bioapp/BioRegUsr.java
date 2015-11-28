/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioapp;

import bioserver.BioClient;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.Color;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 *
 * @author Claudio
 */
public final class BioRegUsr
        extends javax.swing.JFrame {

    public enum REG_USR_MODE {
        REG_INPUT,
        REG_ONLY_PICTURE,
        REG_OUTPUT
    };
    
    private javax.swing.JButton jBtn_RegUsr;
    private javax.swing.JButton jBtn_TakePicture;
    private javax.swing.JLabel JLabel_Msg;
    private javax.swing.JOptionPane jOptPanel_Photo;
    private javax.swing.JMenu jMenu_Devices;
    private javax.swing.JMenuBar jMenuBar;
    
    private final REG_USR_MODE mode;
    private final String cpf;
    private final PresidentEntity entity;
    private final String picturePath;
    
    private final Timer timer = new Timer();
    
    private Webcam webcam = null;
    private WebcamPanel webcamPanel = null;
    private BufferedImage webcamImage = null;
    
    private static final String sBtn_RegUsr = "Colher biometria";
    private static final String sBtn_TakePicture = "Tirar foto";    

    private static final String sPicture_Waiting = "Aguardando foto...";
    private static final String sPicture_Confirm = "Salvar foto do candidato?";
    private static final String sPicture_ConfirmTitle = "Confirmação";
    private static final String sPicture_Yes = "Sim";
    private static final String sPicture_No = "Não";
    private static final String sPicture_Devices = "Dispositivos";
    
    private static final String sBiometric_Waiting = "Aguardando comando de biometria...";
    private static final String sBiometric_Working = "Aguardando biometria...";
    private static final String sBiometric_Matched = "Biometria reconhecida";
    private static final String sBiometric_NotMatched = "Falha ao reconhecida biometria";
    private static final String sBiometric_UnknownCPF = "Candidato não reconhecido";        
    private static final String sBiometric_Unknown = "Erro ao processar a informação de biometria";
    /**
     * Creates new form BioForm
     * @param mode
     * @param cpf
     * @param entity
     * @param picturePath
     */
    public BioRegUsr(
            REG_USR_MODE mode, 
            String cpf, 
            PresidentEntity entity, 
            String picturePath) {
        super();
        
        this.mode = mode;
        this.cpf = cpf;
        this.entity = entity;
        this.picturePath = picturePath;
        
        openWebcam(Webcam.getDefault());

        myInitComponents();
    }

    public void openWebcam(Webcam webcam) {
        if (this.webcam == webcam)
            return;
        
        closeWebcam();
        
        this.webcam = webcam;
        if (webcam != null) {
            this.webcam.setViewSize(WebcamResolution.VGA.getSize());
            this.webcam.open();
        }
        
        WebcamPanel oldWebPanel = webcamPanel;
  
        webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setFPSDisplayed(false);
        webcamPanel.setDisplayDebugInfo(false);
        webcamPanel.setImageSizeDisplayed(false);
        webcamPanel.setFPSLimited(true);
        webcamPanel.setFPSLimit(20);
        webcamPanel.setMirrored(false);
        webcamPanel.start();
        
        if (oldWebPanel != null) {
            LayoutManager lm = getContentPane().getLayout();
            if (lm instanceof GroupLayout) {
                GroupLayout gl = (GroupLayout)lm;
                gl.replace(oldWebPanel, webcamPanel);
            }            
        }
    }
    
    @Override
    public void dispose() {
        closeWebcam();
        
        super.dispose();
    }

    public void closeWebcam() {
        if (this.webcam != null) {
            this.webcam.close();
            this.webcam = null;
        }
    }

    void myInitComponents() {
        JLabel_Msg = new javax.swing.JLabel();
        jBtn_RegUsr = new javax.swing.JButton();
        jBtn_TakePicture = new javax.swing.JButton();
        jOptPanel_Photo = new javax.swing.JOptionPane();
        jMenu_Devices = new javax.swing.JMenu();
        jMenuBar = new javax.swing.JMenuBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jBtn_RegUsr.setText(sBtn_RegUsr);
        jBtn_RegUsr.addActionListener((java.awt.event.ActionEvent evt) -> {
            jBtn_RegUsr_ActionPerformed(evt);
        });
        jBtn_RegUsr.setEnabled(true);
        
        jBtn_TakePicture.setText(sBtn_TakePicture);
        jBtn_TakePicture.addActionListener((java.awt.event.ActionEvent evt) -> {
            jBtn_TakePicture_ActionPerformed(evt);
        });
        jBtn_TakePicture.setEnabled(false);
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(webcamPanel)
                    .addComponent(jBtn_RegUsr, 
                            javax.swing.GroupLayout.DEFAULT_SIZE, 
                            javax.swing.GroupLayout.DEFAULT_SIZE, 
                            Short.MAX_VALUE)
                    .addComponent(jBtn_TakePicture,
                            javax.swing.GroupLayout.DEFAULT_SIZE, 
                            javax.swing.GroupLayout.DEFAULT_SIZE, 
                            Short.MAX_VALUE)
                    .addGap(30)
                    .addComponent(JLabel_Msg, 
                            javax.swing.GroupLayout.DEFAULT_SIZE, 
                            javax.swing.GroupLayout.DEFAULT_SIZE, 
                            Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
           .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(webcamPanel)
                .addGap(25, 25, 25)
                .addComponent(jBtn_RegUsr)
                .addComponent(jBtn_TakePicture)
                .addGap(25, 25, 25)
                .addComponent(JLabel_Msg)                        
                .addContainerGap(15, Short.MAX_VALUE))
        );
        
        jMenu_Devices.setText(sPicture_Devices);
        jMenu_Devices.addMenuListener(new javax.swing.event.MenuListener() {
            @Override
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            @Override
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            @Override
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                jMenu_Devices_ActionListener(evt);
            }
        });
        jMenuBar.add(jMenu_Devices);
        setJMenuBar(jMenuBar);

        switch (this.mode) {
            case REG_INPUT:
                jBtn_RegUsr.setEnabled(true);
                jBtn_TakePicture.setEnabled(false);
                JLabel_Msg.setText(sBiometric_Waiting);
                break;
            
            case REG_ONLY_PICTURE:
                jBtn_RegUsr.setEnabled(false);
                jBtn_TakePicture.setEnabled(true);
                JLabel_Msg.setText(sPicture_Waiting);
                break;
            
            case REG_OUTPUT:
                jBtn_RegUsr.setEnabled(true);
                jBtn_TakePicture.setEnabled(false);
                JLabel_Msg.setText(sBiometric_Waiting);
                webcamPanel.stop();
                webcamPanel.setSize(1, 1);
                break;
        }
        
        pack();
    }
    
    private String[] getWebcamsNames() {
        List<Webcam> listWebcam = Webcam.getWebcams();
        
        String[] strings = new String[listWebcam.size()];
        
        int i = 0;
        for (Webcam webcam : Webcam.getWebcams()) {
            strings[i++] = webcam.getName();
        }
        
        return strings;
    }
    
    private Webcam getWebcamByName(String name) {
        for (Webcam webcam : Webcam.getWebcams()) {
            if (webcam.getName().equals(name))
                return webcam;
        }        
        return null;
    }
    
    private void selectMenuDeviceItem(JMenuItem item) {
        for (int i = 0; i < jMenu_Devices.getItemCount(); ++i) {
            JMenuItem tmpItem = jMenu_Devices.getItem(i);

            if (tmpItem == item) {
                tmpItem.setSelected(true);
            }
            else {
                tmpItem.setSelected(false);
            }
        }
    }
        
    private void updateMenuDevicesSelection() {
        if (this.webcam == null) {
            selectMenuDeviceItem(null);
        }
        
        for (int i = 0; i < jMenu_Devices.getItemCount(); ++i) {
            JMenuItem item = jMenu_Devices.getItem(i);
            
            if (item.getText().equals(webcam.getName())) {
                selectMenuDeviceItem(item);
                break;
            }
        }
    }
    
    private void updateMenuDevices() {        
        String[] names = getWebcamsNames(); 
        for (int i = 0; i < jMenu_Devices.getItemCount(); ++i) {
            JMenuItem item = jMenu_Devices.getItem(i);
            int j;
            for (j = 0; j < names.length; ++j) {
                if (item.getText().equals(names[j])) {
                    names[j] = null;
                    break;
                }
            }
            if (j >= names.length) {
                jMenu_Devices.remove(item);
            }
        }
        
        for (String name : names) {
            if (name != null) {
                JCheckBoxMenuItem item = new JCheckBoxMenuItem();
                item.setSelected(false);
                item.setText(name);
                item.addActionListener(this::jMenu_Item_ActionListener);
                jMenu_Devices.add(item);
            }
        }
        
        updateMenuDevicesSelection();
    }
    
    private void jMenu_Devices_ActionListener(javax.swing.event.MenuEvent evt) {
        updateMenuDevices();        
    }
    
    private void jMenu_Item_ActionListener(java.awt.event.ActionEvent evt) {
        if (evt.getSource() instanceof JCheckBoxMenuItem) {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem)evt.getSource();
            
            selectMenuDeviceItem(item);
            openWebcam(getWebcamByName(item.getText()));
        }
    }

    private void jBtn_RegUsr_ActionPerformed(java.awt.event.ActionEvent evt) {
        jBtn_RegUsr.setEnabled(false);        
        JLabel_Msg.setBackground(Color.YELLOW);
        JLabel_Msg.setOpaque(true);
        JLabel_Msg.setText(sBiometric_Working + " - " + cpf);

        Runnable reqCpf;
        reqCpf = () -> {
            BioClient.BIO_CHECK_ERROR err;
            err = BioClient.reqBioFromCpf(cpf);

            Biometric_IdentifyEvent(err);
        };
        new Thread(reqCpf).start();
    }
    
    public void jBtn_TakePicture_ActionPerformed(java.awt.event.ActionEvent evt) {
        webcamImage = webcam.getImage();
        webcamPanel.pause();
        
        Object[] options = {sPicture_Yes, sPicture_No};
        int choice = JOptionPane.showOptionDialog(
                        null, 
                        sPicture_Confirm, 
                        sPicture_ConfirmTitle,
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.INFORMATION_MESSAGE,
                        null, 
                        options, 
                        options[0]);
        
        if (choice == 0) {
            // Yes
            String filename = picturePath + cpf + ".jpg";
            File outputfile = new File(filename);
            try {
                ImageIO.write(webcamImage, "jpg", outputfile);
            } catch (IOException ex) {
                Logger.getLogger(BioRegUsr.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if (this.mode == REG_USR_MODE.REG_INPUT)
                entity.registerCandidateIn(cpf, filename);
            else if (this.mode == REG_USR_MODE.REG_ONLY_PICTURE)
                entity.updatePhoto(cpf, filename);
            
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
        else {
            // No
            webcamPanel.resume();
        }        
    }

    private synchronized void Biometric_IdentifyEvent(
            BioClient.BIO_CHECK_ERROR err) {
        
        // [DEBUG]
        err = BioClient.BIO_CHECK_ERROR.MATCHED;
        
        switch (err) {
            case MATCHED:
                Object myself = this;
                
                JLabel_Msg.setBackground(Color.GREEN);
                JLabel_Msg.setOpaque(true);
                JLabel_Msg.setText(sBiometric_Matched + " - " + cpf);
                
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        switch (mode) {
                            case REG_INPUT:
                                JLabel_Msg.setOpaque(false);
                                JLabel_Msg.setText(sPicture_Waiting);
                                
                                jBtn_TakePicture.setEnabled(true);
                                break;
                            
                            case REG_OUTPUT:
                                entity.registerCandidateOut(cpf);                                
                                dispatchEvent(new WindowEvent(
                                        (Window)myself, 
                                        WindowEvent.WINDOW_CLOSING));
                                break;
                            
                            default:
                                break;
                        }                        
                    }
                }, 2000);
                break;

            case NOT_MATCHED:
                JLabel_Msg.setBackground(Color.RED);
                JLabel_Msg.setOpaque(true);
                JLabel_Msg.setText(sBiometric_NotMatched + " - " + cpf);
                break;

            case UNKNOWN_CPF:                
                JLabel_Msg.setBackground(Color.RED);
                JLabel_Msg.setOpaque(true);
                JLabel_Msg.setText(sBiometric_UnknownCPF + " - " + cpf);
                break;

            case TIMEOUT:
            case CONNECTION_LOST:
            case CONNECTION_UNREACHED:
            case UNKNOWN:                
                JLabel_Msg.setBackground(Color.RED);
                JLabel_Msg.setOpaque(true);
                JLabel_Msg.setText(sBiometric_Unknown + " - " + cpf);
                break;
        }

        if (err != BioClient.BIO_CHECK_ERROR.MATCHED) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {

                    JLabel_Msg.setOpaque(false);
                    JLabel_Msg.setText(sBiometric_Waiting);
                    
                    jBtn_RegUsr.setEnabled(true);
                }
            }, 2000);
        }
    }
}
