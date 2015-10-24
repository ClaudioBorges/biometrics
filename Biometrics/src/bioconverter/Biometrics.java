/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bioconverter;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Scanner;

import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NFTemplate;
import com.neurotec.biometrics.NFinger;
import com.neurotec.biometrics.NMatchingSpeed;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NTemplate;
import com.neurotec.biometrics.NTemplateSize;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.standards.BDIFStandard;
import com.neurotec.biometrics.standards.FMRFingerView;
import com.neurotec.biometrics.standards.FMRecord;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceManager.DeviceCollection;
import com.neurotec.devices.NDeviceType;
import com.neurotec.devices.NFScanner;
import com.neurotec.io.NBuffer;
import com.neurotec.io.NFile;
import com.neurotec.lang.NCore;
import com.neurotec.licensing.NLicense;
import com.neurotec.util.NVersion;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Claudio
 */
public class Biometrics {
    
    public enum BIOMETRIC_STATE {
        MATCHED,
        NOT_MATCHED,
        UNKNOWN_CPF,
        TIMEOUT,
        UNKNOWN
    };

    private BioEntity bioEntity = null;    
    private final String bioComponents = 
            "Biometrics.Standards.FingerTemplates"
            + ",Biometrics.FingerExtraction"
            + ",Devices.FingerScanners"
            + ",Biometrics.FingerMatching";
    private NBiometricClient biometricClient = null;
    
    private final int enrollFingerTimeoutMs = 15000;
    
    public Biometrics(String fileDB) 
            throws ClassNotFoundException, SQLException, IOException {
        this.biometricClient = null;
        
        bioEntity = new BioEntity(fileDB);
        
        LibraryManager.initLibraryPath(); 
        
        if (!NLicense.obtainComponents("/local", 5000, bioComponents)) {
            System.err.println(
                    "Could not obtain licenses for components: " 
                    + bioComponents);
        }
        
        biometricClient = new NBiometricClient();
    }
    
    public boolean isScannerOpened() {
        return (biometricClient.getFingerScanner() != null);
    }
    
    public boolean openScanner() {
        biometricClient.setUseDeviceManager(true);

        NDeviceManager deviceManager = biometricClient.getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.FINGER_SCANNER));
        deviceManager.initialize();

        DeviceCollection devices = deviceManager.getDevices();
        if (devices.size() > 0) {
            System.out.format(
                    "Found %d fingerprint scanner\n", devices.size());
        } else {
            System.out.format(
                    "No scanners found\n");
            
            return false;
        }

                
        int selection = 0;
        /*
        if (devices.size() > 1)
            System.out.println(
                    "Please select finger scanner from the list:");
        if (devices.size() > 1) {
            try (Scanner scanner = new Scanner(System.in)) {
                selection = scanner.nextInt() - 1;
            }
        }*/
        
        for (int i = 0; i < devices.size(); i++)
            System.out.format(
                    "\t%d. %s\n", i + 1, 
                    devices.get(i).getDisplayName());
        if (devices.size() > 1) {
            System.out.println("Finger scanner " + selection + " selected.");
        }
        

        biometricClient.setFingerScanner((NFScanner) devices.get(selection));
        
        
        return true;
    }
    
    public void close() throws IOException {
        if (biometricClient != null) biometricClient.dispose();
        if (bioEntity != null) bioEntity.close();
        
        biometricClient = null;
        bioEntity = null;
        
        try {
            NLicense.releaseComponents(bioComponents);
            NCore.shutdown();
        } catch (IOException ex) {
            Logger.getLogger(Biometrics.class.getName())
                .log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean isEnrolled(String cpf) {
        boolean state = false;
        
        try {
            NSubject registered = bioEntity.getPerson(cpf);
            if (registered != null)
                state = true;
            
        } catch (Exception ex) {
            Logger.getLogger(Biometrics.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        
        return state;
    }
    
    public boolean isCandidate(String cpf) {
        boolean state = false;
        
        try {
            state = bioEntity.isCandidate(cpf);
            
        } catch (Exception ex) {
            Logger.getLogger(Biometrics.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        
        return state;
    }
    
    public BIOMETRIC_STATE verifyBiometric(String cpf) {
        BIOMETRIC_STATE state = BIOMETRIC_STATE.UNKNOWN;
        
        try {
            NSubject registered = bioEntity.getPerson(cpf);
            if (registered != null) {
                                
                NSubject candidate = enrollFinger();
                if (candidate == null)
                    state = BIOMETRIC_STATE.TIMEOUT;
                else {
                    NBiometricStatus status 
                            = verifyFinger(registered, candidate);

                    if (status == NBiometricStatus.OK) {
                        state = BIOMETRIC_STATE.MATCHED;
                    }
                    else if (status == NBiometricStatus.MATCH_NOT_FOUND) {
                        state = BIOMETRIC_STATE.NOT_MATCHED;
                    }
                    else {
                        state = BIOMETRIC_STATE.UNKNOWN;
                    }
                }
            }
            else {
                state = BIOMETRIC_STATE.UNKNOWN_CPF;
            }
        } catch (Exception ex) {
            Logger.getLogger(Biometrics.class.getName())
                    .log(Level.SEVERE, null, ex);

            state = BIOMETRIC_STATE.UNKNOWN;
        }
        
        return state;
    }
    
    private NSubject enrollFinger() {
        
        NSubject subject = null;
        NFinger finger = null;
        NBiometricStatus status = NBiometricStatus.NONE;

        try {	
            subject = new NSubject();
            finger = new NFinger();

            subject.getFingers().add(finger);

            System.out.println("Capturing....");
           
            biometricClient.setTimeout(enrollFingerTimeoutMs);
            status = biometricClient.capture(subject);
            
            if (status == NBiometricStatus.OK) {
                biometricClient.setFingersTemplateSize(NTemplateSize.LARGE);
                status = biometricClient.createTemplate(subject);
            }
            
            if (status == NBiometricStatus.OK) {
                System.out.println("Template extracted");
            } else {
                System.out.format("Extraction failed: %s\n", status);
            }

            //if (outputImageFilename != null) {
            //    subject.getFingers().get(0).getImage()
            //            .save(outputImageFilename);
            //    System.out.println("Fingerprint image saved successfully...\n");
            //}
            //if (outputTempleteFilename != null) {
            //    NFile.writeAllBytes(
            //            outputTempleteFilename, 
            //            subject.getTemplate().save());
                
            //    System.out.println("Template file saved successfully...");
            //}
        } catch (Exception ex) {
            Logger.getLogger(Biometrics.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            if (status != NBiometricStatus.OK) {
                if (finger != null) finger.dispose();
                if (subject != null) subject.dispose();
                
                subject = null;
            }
        }
        
        return subject;
    }

    private NBiometricStatus verifyFinger(
            NSubject reference,
            NSubject candidate) {
        
        NBiometricStatus status = NBiometricStatus.NONE;
                
        try {
            // Set matching threshold
            biometricClient.setMatchingThreshold(48);

            // Set matching speed
            biometricClient.setFingersMatchingSpeed(NMatchingSpeed.LOW);

            // Verify subjects
            status = biometricClient.verify(reference, candidate);

            if (status == NBiometricStatus.OK 
                || status == NBiometricStatus.MATCH_NOT_FOUND) {
            
                int score = reference.getMatchingResults().get(0).getScore();
                System.out.format("image scored %d, verification.. ", score);
                if (status == NBiometricStatus.OK) {
                        System.out.println("succeeded");
                } else {
                        System.out.println("failed");
                }
            } 
            else {
                System.out.format("Verification failed. Status: %s", status);
            }
        } catch (Exception ex) {
            Logger.getLogger(Biometrics.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {

        }       
        
        return status;
    }
    
    private NBiometricStatus verifyFinger(
            String referenceFilename, 
            String candidateFilename) throws IOException {

        NBiometricStatus status = NBiometricStatus.NONE;
        
        NSubject referenceSubject = null;
        NSubject candidateSubject = null; 
        
        try {
            // Create subjects with face
            referenceSubject = getNSubjectFromImage(referenceFilename);
            candidateSubject = getNSubjectFromImage(candidateFilename);
            
            status = verifyFinger(referenceSubject, candidateSubject);
        } catch (Exception ex) {
            Logger.getLogger(Biometrics.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            if (referenceSubject != null) referenceSubject.dispose();
            if (candidateSubject != null) candidateSubject.dispose();
        }        
        
        return status;
    }
    
    private static void saveNTemplate(
            String filename, NTemplate nTemplate) throws IOException {
        NFile.writeAllBytes(
           filename, 
           nTemplate.save());
    }
    
    private static NSubject getNSubjectFromNTemplate(
        NTemplate template,
        String id) {
        
        NSubject subject = new NSubject();
        
        subject.setTemplate(template);
        subject.setId(id);
        
        return subject;
    }
    
    private static NSubject getNSubjectFromNTemplate(
        String filename) throws IOException {

        NSubject nSubject = new NSubject();
        
        nSubject.setTemplateBuffer(NFile.readAllBytes(filename));
        nSubject.setId(filename);
        
        return nSubject;
    }
    
    private static NSubject getNSubjectFromImage(
        String filename) {

        NSubject subject = new NSubject();
        subject.setId(filename);
        NFinger finger = new NFinger();
        finger.setFileName(filename);
        subject.getFingers().add(finger);
        return subject;        
    }
    
    private static void standardFromNTemplate(
            String ntempleteFilename,
            String standardFilename,
            boolean flagUseNeurotecFields,
            BDIFStandard standard,
            NVersion version) throws IOException {
        
        NTemplate nTemplate = null;
        NFTemplate nfTemplate = null;
        FMRecord fmRecord = null;

        try {
            if (standard == BDIFStandard.UNSPECIFIED) {
                if (version == FMRecord.VERSION_ANSI_20
                        || version == FMRecord.VERSION_ANSI_35
                        || version == FMRecord.VERSION_ANSI_CURRENT) {
                    
                    standard = BDIFStandard.ANSI;
                }
                else {
                    standard = BDIFStandard.ISO;
                }               
            }
            else if (standard == BDIFStandard.ISO) {
                if (version == FMRecord.VERSION_ANSI_20
                        || version == FMRecord.VERSION_ANSI_35
                        || version == FMRecord.VERSION_ANSI_CURRENT) {
                    
                    throw new IllegalArgumentException(
                            "Standard and version is incompatible");
                }
            }
            else if (standard == BDIFStandard.ANSI) {
                if (version == FMRecord.VERSION_ISO_20
                        || version == FMRecord.VERSION_ISO_30
                        || version == FMRecord.VERSION_ISO_CURRENT) {
                    
                    throw new IllegalArgumentException(
                            "Standard and version is incompatible");
                }
            }
            
            NBuffer packedNTemplate = NFile.readAllBytes(ntempleteFilename);

            // Creating NTemplate object from packed NTemplate
            nTemplate = new NTemplate(packedNTemplate);

            // Retrieving NFTemplate object from NTemplate object
            nfTemplate = nTemplate.getFingers();

            if (nfTemplate != null) {
                // Creating FMRecord object from NFTemplate object
                fmRecord = new FMRecord(nfTemplate, standard, version);

                // Storing FMRecord object in memory
                NBuffer storedFmRecord;
                if (flagUseNeurotecFields) {
                        storedFmRecord = fmRecord.save(
                                FMRFingerView.FLAG_USE_NEUROTEC_FIELDS);
                } else {
                        storedFmRecord = fmRecord.save();
                }
                NFile.writeAllBytes(standardFilename, storedFmRecord);
            } 
            else {
                System.out.println("there are no NFRecords in NTemplate");
            }
        } finally {
            if (nTemplate != null) nTemplate.dispose();
            if (nfTemplate != null) nfTemplate.dispose();
            if (fmRecord != null) fmRecord.dispose();
        }
    }
    
    private static void ntemplateFromStandard(
            String standardFilename, 
            String outputFilename,
            BDIFStandard standard,
            boolean flagUseNeurotecFields) throws IOException {

        FMRecord fmRecord = null;
        NTemplate nTemplate = null;

        try {
            NBuffer storedFmRecord = NFile.readAllBytes(standardFilename);

            // Creating FMRecord object from FMRecord stored in memory
            if (flagUseNeurotecFields == true) {
                fmRecord = new FMRecord(
                        storedFmRecord, 
                        FMRFingerView.FLAG_USE_NEUROTEC_FIELDS, 
                        standard);
            } else {
                fmRecord = new FMRecord(
                        storedFmRecord, 
                        standard);
            }

            // Converting FMRecord object to NTemplate object
            nTemplate = fmRecord.toNTemplate();

            // Packing NTemplate object
            NBuffer packedNTemplate = nTemplate.save();

            NFile.writeAllBytes(outputFilename, packedNTemplate);
        } finally {            
            if (fmRecord != null) fmRecord.dispose();
            if (nTemplate != null) nTemplate.dispose();
        }
    }
    
    private static void convertFromISO(
            String isoFilename, 
            String outputFilename,
            boolean flagUseNeurotecFields) throws IOException {

        ntemplateFromStandard(
                isoFilename, 
                outputFilename, 
                BDIFStandard.ISO, 
                flagUseNeurotecFields);
    }

    private static void convertFromANSI(
            String isoFilename, 
            String outputFilename,
            boolean flagUseNeurotecFields) throws IOException {

        ntemplateFromStandard(
                isoFilename, 
                outputFilename, 
                BDIFStandard.ANSI, 
                flagUseNeurotecFields);
    }
    
    private void debugMakeBioEntity() {
        try {
            bioEntity.removeAll();
            
            bioEntity.addPerson(
                    "38502729829",
                    getNSubjectFromNTemplate("C:\\Teste\\Claudio.data"));
            bioEntity.addPerson(
                    "38502729839",
                    getNSubjectFromNTemplate("C:\\Teste\\JMedio.data"));
            bioEntity.addPerson(
                    "40297378899",
                    getNSubjectFromNTemplate("C:\\Teste\\Munir.data"));
            bioEntity.addPerson(
                    "34381895851",
                    getNSubjectFromNTemplate("C:\\Teste\\Roger.data"));
            bioEntity.addPerson(
                    "28895589831",
                    getNSubjectFromNTemplate("C:\\Teste\\Francis.data"));
            bioEntity.addPerson(
                    "31778173837",
                    getNSubjectFromNTemplate("C:\\Teste\\Buso.data"));
        } catch (IOException ex) {
            Logger.getLogger(Biometrics.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    public void enrollUserToDB(String cpf) {
        NSubject subject;
        subject = enrollFinger();
        
        // [TODO] Check cpf
        if (subject != null)
            bioEntity.addPerson(cpf, subject);
    }
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException, Throwable {
        Biometrics biometrics = new Biometrics("C:\\Teste\\biodb.db");
        
        biometrics.debugMakeBioEntity();
        biometrics.close();
        
        
        
        /* 
        //verifyFinger("C:\\Teste\\jPolegar.png", "C:\\Teste\\polegar.png");

        //NSubject subject = enrollFinger();
        //saveNTemplate("C:\\Teste\\Hoje.data", subject.getTemplate());

        standardFromNTemplate(
            "C:\\Teste\\Hoje.data",
            "C:\\Teste\\Hoje.iso20",
            false,
            BDIFStandard.ISO,
            FMRecord.VERSION_ISO_20); 
        
        convertFromISO(
            "C:\\Teste\\Hoje.iso20",
            "C:\\Teste\\Hoje.conv",
            false);        
        
        verifyFinger(getNSubjectFromNTemplate("C:\\Teste\\Hoje.data"),
                subject);
        verifyFinger(getNSubjectFromNTemplate("C:\\Teste\\Hoje.conv"),
                subject);
                
        //quality = evaluateFingerQuality(
        //        getNSubjectFromImage("C:\\Teste\\polegar.png"));
        //System.out.println(
        //        "Quality of " 
        //        + "C:\\Teste\\polegar.png"
        //        + " is " 
        //        + quality.toString());
        

 
        standardFromNTemplate(
            "C:\\Teste\\jPolegar.data",
            "C:\\Teste\\jPolegar.iso",
            false,
            BDIFStandard.ISO,
            FMRecord.VERSION_ISO_20);

        convertFromISO(
            "C:\\Teste\\jPolegar.iso",
            "C:\\Teste\\jPolegar.conv",
            false);*/
    }
}
