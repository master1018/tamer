public class DefaultPaths {
    private static final String INSTALL_PATH_RESOURCE_NAME = "com/sun/jdmk/defaults/install.path";
    private DefaultPaths() {
    }
    public static String getInstallDir() {
        if (installDir == null)
            return useRessourceFile();
        else
            return installDir;
    }
    public static String getInstallDir(String dirname) {
        if (installDir == null) {
            if (dirname == null) {
                return getInstallDir();
            } else {
                return getInstallDir() + File.separator + dirname;
            }
        } else {
            if (dirname == null) {
                return installDir;
            } else {
                return installDir + File.separator + dirname;
            }
        }
    }
    public static void setInstallDir(String dirname) {
        installDir = dirname;
    }
    public static String getEtcDir() {
        if (etcDir == null)
            return getInstallDir("etc");
        else
            return etcDir;
    }
    public static String getEtcDir(String dirname) {
        if (etcDir == null) {
            if (dirname == null) {
                return getEtcDir();
            } else {
                return getEtcDir() + File.separator + dirname;
            }
        } else {
            if (dirname == null) {
                return etcDir;
            } else {
                return etcDir + File.separator + dirname;
            }
        }
    }
    public static void setEtcDir(String dirname) {
        etcDir = dirname;
    }
    public static String getTmpDir() {
         if (tmpDir == null)
            return getInstallDir("tmp");
        else
            return tmpDir;
    }
    public static String getTmpDir(String dirname) {
        if (tmpDir == null) {
            if (dirname == null) {
                return getTmpDir();
            } else {
                return getTmpDir() + File.separator + dirname;
            }
        } else {
            if (dirname == null) {
                return tmpDir;
            } else {
                return tmpDir + File.separator + dirname;
            }
        }
    }
    public static void setTmpDir(String dirname) {
        tmpDir = dirname;
    }
    private static String useRessourceFile() {
        InputStream in = null;
        BufferedReader r = null;
        try {
            in =
                DefaultPaths.class.getClassLoader().getResourceAsStream(INSTALL_PATH_RESOURCE_NAME);
            if(in == null) return null;
            r = new BufferedReader(new InputStreamReader(in));
            installDir = r.readLine();
        }catch(Exception e) {
        }
        finally {
            try {
                if(in != null) in.close();
                if(r != null) r.close();
            }catch(Exception e) {}
        }
        return installDir;
    }
    private static String etcDir;
    private static String tmpDir;
    private static String installDir;
}
