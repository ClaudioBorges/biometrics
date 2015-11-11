/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioapp;

import bioclient.BioClient;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

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
    
    private final REG_USR_MODE mode;
    private final String cpf;
    private final PresidentEntity entity;
    private final String picturePath;
    
    private final Timer timer = new Timer();
    private final Object myself;

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
    
    private static final String sBiometric_Waiting = "Aguardando comando de biometria...";
    private static final String sBiometric_Working = "Aguardando biometria...";
    private static final String sBiometric_Matched = "Biometria reconhecida";
    private static final String sBiometric_NotMatched = "Falha ao reconhecida biometria";
    private static final String sBiometric_UnknownCPF = "Candidato não reconhecido";        
    private static final String sBiometric_Unknown = "Erro ao processar a informação de biometria";
    /**
     * Creates new form BioForm
     */
    public BioRegUsr(
            REG_USR_MODE mode, 
            String cpf, 
            PresidentEntity entity, 
            String picturePath) {
        super();
        
        this.myself = this;

        this.mode = mode;
        this.cpf = cpf;
        this.entity = entity;
        this.picturePath = picturePath;
        
        openWebcam(Webcam.getDefault());

        myInitComponents();
    }

    public void openWebcam(Webcam webcam) {
        this.webcam = webcam;
        this.webcam.setViewSize(WebcamResolution.VGA.getSize());
        this.webcam.open();

        webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setFPSDisplayed(false);
        webcamPanel.setDisplayDebugInfo(false);
        webcamPanel.setImageSizeDisplayed(false);
        webcamPanel.setFPSLimited(true);
        webcamPanel.setFPSLimit(20);
        webcamPanel.setMirrored(false);
        webcamPanel.start();
    }
    
    @Override
    public void dispose() {
        closeWebcam();
        
        super.dispose();
    }

    public void closeWebcam() {
        this.webcam.close();
        this.webcam = null;
    }

    void myInitComponents() {
        JLabel_Msg = new javax.swing.JLabel();
        jBtn_RegUsr = new javax.swing.JButton();
        jBtn_TakePicture = new javax.swing.JButton();
        jOptPanel_Photo = new javax.swing.JOptionPane();

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
        int choice = jOptPanel_Photo.showOptionDialog(
                        null, 
                        sPicture_Confirm, 
                        sPicture_ConfirmTitle,
                        jOptPanel_Photo.YES_NO_OPTION, 
                        jOptPanel_Photo.INFORMATION_MESSAGE,
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
                                entity.finishCandidate(cpf);                              
                                dispatchEvent(new WindowEvent(
                                        (Window) myself, 
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
