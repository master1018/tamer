package edu.clemson.cs.r2jt.archiving;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;
import edu.clemson.cs.r2jt.compilereport.CompileReport;
import edu.clemson.cs.r2jt.init.CompileEnvironment;

public class GuiWrapper {

    private GuiSetup gui;

    private String type;

    private String guiName;

    private String facilityName;

    private String programFacilityName;

    private String javaLocation;

    private String entryClass;

    private String packageName;

    private StringBuffer sb = new StringBuffer();

    public GuiWrapper(String type, String targetFile, String workspaceDir, CompileEnvironment instanceEnvironment) {
        gui = new GuiSetup();
        this.type = type;
        String facilityFileName = analyzeTargetFile(targetFile, workspaceDir) + ".fa";
        javaLocation = targetFile.replaceAll(facilityFileName, guiName + ".java");
        CompileReport myReport = instanceEnvironment.getCompileReport();
        if (!myReport.getFacilityName().equals("")) {
            programFacilityName = myReport.getFacilityName();
        } else {
            programFacilityName = facilityName;
        }
    }

    public boolean generateCode() {
        if (type.equals("gui")) {
            generateSwing();
        } else if (type.equals("applet")) {
            generateApplet();
        } else {
            return false;
        }
        return true;
    }

    private void generateSwing() {
        SwingGui sg = new SwingGui(guiName, facilityName, programFacilityName, packageName, gui);
        sb = sg.generateCode();
    }

    private void generateApplet() {
    }

    public String getJavaPath() {
        return javaLocation;
    }

    public String getEntryClass() {
        return entryClass;
    }

    public boolean createJavaFile() {
        boolean created = false;
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(javaLocation));
            out.write(sb.toString());
            out.close();
            created = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return created;
    }

    private String analyzeTargetFile(String targetFile, String workspaceDir) {
        int lastSlash = targetFile.lastIndexOf(File.separator);
        int dot = targetFile.lastIndexOf(".");
        String path = targetFile.substring(0, lastSlash);
        String packagePath = path.substring(workspaceDir.length(), path.length());
        packageName = packagePath.replaceAll(Pattern.quote(File.separator), ".");
        facilityName = targetFile.substring(lastSlash + 1, dot);
        guiName = facilityName + "_Gui";
        entryClass = packageName + "." + guiName;
        return facilityName;
    }
}
