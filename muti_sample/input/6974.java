public class MimeEntry implements Cloneable {
    private String typeName;    
    private String tempFileNameTemplate;
    private int action;
    private String command;
    private String description;
    private String imageFileName;
    private String fileExtensions[];
    boolean starred;
    public static final int             UNKNOWN                 = 0;
    public static final int             LOAD_INTO_BROWSER       = 1;
    public static final int             SAVE_TO_FILE            = 2;
    public static final int             LAUNCH_APPLICATION      = 3;
    static final String[] actionKeywords = {
        "unknown",
        "browser",
        "save",
        "application",
    };
    public MimeEntry(String type) {
        this(type, UNKNOWN, null, null, null);
    }
    MimeEntry(String type, String imageFileName, String extensionString) {
        typeName = type.toLowerCase();
        action = UNKNOWN;
        command = null;
        this.imageFileName = imageFileName;
        setExtensions(extensionString);
        starred = isStarred(typeName);
    }
    MimeEntry(String typeName, int action, String command,
              String tempFileNameTemplate) {
        this.typeName = typeName.toLowerCase();
        this.action = action;
        this.command = command;
        this.imageFileName = null;
        this.fileExtensions = null;
        this.tempFileNameTemplate = tempFileNameTemplate;
    }
    MimeEntry(String typeName, int action, String command,
              String imageFileName, String fileExtensions[]) {
        this.typeName = typeName.toLowerCase();
        this.action = action;
        this.command = command;
        this.imageFileName = imageFileName;
        this.fileExtensions = fileExtensions;
        starred = isStarred(typeName);
    }
    public synchronized String getType() {
        return typeName;
    }
    public synchronized void setType(String type) {
        typeName = type.toLowerCase();
    }
    public synchronized int getAction() {
        return action;
    }
    public synchronized void setAction(int action, String command) {
        this.action = action;
        this.command = command;
    }
    public synchronized void setAction(int action) {
        this.action = action;
    }
    public synchronized String getLaunchString() {
        return command;
    }
    public synchronized void setCommand(String command) {
        this.command = command;
    }
    public synchronized String getDescription() {
        return (description != null ? description : typeName);
    }
    public synchronized void setDescription(String description) {
        this.description = description;
    }
    public String getImageFileName() {
        return imageFileName;
    }
    public synchronized void setImageFileName(String filename) {
        File file = new File(filename);
        if (file.getParent() == null) {
            imageFileName = System.getProperty(
                                     "java.net.ftp.imagepath."+filename);
        }
        else {
            imageFileName = filename;
        }
        if (filename.lastIndexOf('.') < 0) {
            imageFileName = imageFileName + ".gif";
        }
    }
    public String getTempFileTemplate() {
        return tempFileNameTemplate;
    }
    public synchronized String[] getExtensions() {
        return fileExtensions;
    }
    public synchronized String getExtensionsAsList() {
        String extensionsAsString = "";
        if (fileExtensions != null) {
            for (int i = 0; i < fileExtensions.length; i++) {
                extensionsAsString += fileExtensions[i];
                if (i < (fileExtensions.length - 1)) {
                    extensionsAsString += ",";
                }
            }
        }
        return extensionsAsString;
    }
    public synchronized void setExtensions(String extensionString) {
        StringTokenizer extTokens = new StringTokenizer(extensionString, ",");
        int numExts = extTokens.countTokens();
        String extensionStrings[] = new String[numExts];
        for (int i = 0; i < numExts; i++) {
            String ext = (String)extTokens.nextElement();
            extensionStrings[i] = ext.trim();
        }
        fileExtensions = extensionStrings;
    }
    private boolean isStarred(String typeName) {
        return (typeName != null)
            && (typeName.length() > 0)
            && (typeName.endsWith("
    public Object launch(java.net.URLConnection urlc, InputStream is, MimeTable mt) throws ApplicationLaunchException {
        switch (action) {
        case SAVE_TO_FILE:
            try {
                return is;
            } catch(Exception e) {
                return "Load to file failed:\n" + e;
            }
        case LOAD_INTO_BROWSER:
            try {
                return urlc.getContent();
            } catch (Exception e) {
                return null;
            }
        case LAUNCH_APPLICATION:
            {
                String threadName = command;
                int fst = threadName.indexOf(' ');
                if (fst > 0) {
                    threadName = threadName.substring(0, fst);
                }
                return new MimeLauncher(this, urlc, is,
                                        mt.getTempFileTemplate(), threadName);
            }
        case UNKNOWN:
            return null;
        }
        return null;
    }
    public boolean matches(String type) {
        if (starred) {
          return type.startsWith(typeName);
        } else {
            return type.equals(typeName);
        }
    }
    public Object clone() {
        MimeEntry theClone = new MimeEntry(typeName);
        theClone.action = action;
        theClone.command = command;
        theClone.description = description;
        theClone.imageFileName = imageFileName;
        theClone.tempFileNameTemplate = tempFileNameTemplate;
        theClone.fileExtensions = fileExtensions;
        return theClone;
    }
    public synchronized String toProperty() {
        StringBuffer buf = new StringBuffer();
        String separator = "; ";
        boolean needSeparator = false;
        int action = getAction();
        if (action != MimeEntry.UNKNOWN) {
            buf.append("action=" + actionKeywords[action]);
            needSeparator = true;
        }
        String command = getLaunchString();
        if (command != null && command.length() > 0) {
            if (needSeparator) {
                buf.append(separator);
            }
            buf.append("application=" + command);
            needSeparator = true;
        }
        if (getImageFileName() != null) {
            if (needSeparator) {
                buf.append(separator);
            }
            buf.append("icon=" + getImageFileName());
            needSeparator = true;
        }
        String extensions = getExtensionsAsList();
        if (extensions.length() > 0) {
            if (needSeparator) {
                buf.append(separator);
            }
            buf.append("file_extensions=" + extensions);
            needSeparator = true;
        }
        String description = getDescription();
        if (description != null && !description.equals(getType())) {
            if (needSeparator) {
                buf.append(separator);
            }
            buf.append("description=" + description);
        }
        return buf.toString();
    }
    public String toString() {
        return "MimeEntry[contentType=" + typeName
            + ", image=" + imageFileName
            + ", action=" + action
            + ", command=" + command
            + ", extensions=" + getExtensionsAsList()
            + "]";
    }
}
