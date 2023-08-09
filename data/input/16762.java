class MimeLauncher extends Thread {
    java.net.URLConnection uc;
    MimeEntry m;
    String genericTempFileTemplate;
    InputStream is;
    String execPath;
    MimeLauncher (MimeEntry M, java.net.URLConnection uc,
                  InputStream is, String tempFileTemplate, String threadName) throws ApplicationLaunchException {
        super(threadName);
        m = M;
        this.uc = uc;
        this.is = is;
        genericTempFileTemplate = tempFileTemplate;
        String launchString = m.getLaunchString();
        if (!findExecutablePath(launchString)) {
            String appName;
            int index = launchString.indexOf(' ');
            if (index != -1) {
                appName = launchString.substring(0, index);
            }
            else {
                appName = launchString;
            }
            throw new ApplicationLaunchException(appName);
        }
    }
    protected String getTempFileName(URL url, String template) {
        String tempFilename = template;
        int wildcard = tempFilename.lastIndexOf("%s");
        String prefix = tempFilename.substring(0, wildcard);
        String suffix = "";
        if (wildcard < tempFilename.length() - 2) {
            suffix = tempFilename.substring(wildcard + 2);
        }
        long timestamp = System.currentTimeMillis()/1000;
        int argIndex = 0;
        while ((argIndex = prefix.indexOf("%s")) >= 0) {
            prefix = prefix.substring(0, argIndex)
                + timestamp
                + prefix.substring(argIndex + 2);
        }
        String filename = url.getFile();
        String extension = "";
        int dot = filename.lastIndexOf('.');
        if (dot >= 0 && dot > filename.lastIndexOf('/')) {
            extension = filename.substring(dot);
        }
        filename = "HJ" + url.hashCode();
        tempFilename = prefix + filename + timestamp + extension + suffix;
        return tempFilename;
    }
    public void run() {
        try {
            String ofn = m.getTempFileTemplate();
            if (ofn == null) {
                ofn = genericTempFileTemplate;
            }
            ofn = getTempFileName(uc.getURL(), ofn);
            try {
                OutputStream os = new FileOutputStream(ofn);
                byte buf[] = new byte[2048];
                int i = 0;
                try {
                    while ((i = is.read(buf)) >= 0) {
                        os.write(buf, 0, i);
                    }
                } catch(IOException e) {
                } finally {
                    os.close();
                    is.close();
                }
            } catch(IOException e) {
            }
            int inx = 0;
            String c = execPath;
            while ((inx = c.indexOf("%t")) >= 0) {
                c = c.substring(0, inx) + uc.getContentType()
                    + c.substring(inx + 2);
            }
            boolean substituted = false;
            while ((inx = c.indexOf("%s")) >= 0) {
                c = c.substring(0, inx) + ofn + c.substring(inx + 2);
                substituted = true;
            }
            if (!substituted)
                c = c + " <" + ofn;
            Runtime.getRuntime().exec(c);
        } catch(IOException e) {
        }
    }
    private boolean findExecutablePath(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        String command;
        int index = str.indexOf(' ');
        if (index != -1) {
            command = str.substring(0, index);
        }
        else {
            command = str;
        }
        File f = new File(command);
        if (f.isFile()) {
            execPath = str;
            return true;
        }
        String execPathList;
        execPathList = java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("exec.path"));
        if (execPathList == null) {
            return false;
        }
        StringTokenizer iter = new StringTokenizer(execPathList, "|");
        while (iter.hasMoreElements()) {
            String prefix = (String)iter.nextElement();
            String fullCmd = prefix + File.separator + command;
            f = new File(fullCmd);
            if (f.isFile()) {
                execPath = prefix + File.separator + str;
                return true;
            }
        }
        return false; 
    }
}
