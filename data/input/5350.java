public class FcFontConfiguration extends FontConfiguration {
    private static final String fileVersion = "1";
    private String fcInfoFileName = null;
    private FcCompFont[] fcCompFonts = null;
    public FcFontConfiguration(SunFontManager fm) {
        super(fm);
        init();
    }
    public FcFontConfiguration(SunFontManager fm,
                               boolean preferLocaleFonts,
                               boolean preferPropFonts) {
        super(fm, preferLocaleFonts, preferPropFonts);
        init();
    }
    @Override
    public synchronized boolean init() {
        if (fcCompFonts != null) {
            return true;
        }
        setFontConfiguration();
        readFcInfo();
        X11FontManager fm = (X11FontManager) fontManager;
        FontConfigManager fcm = fm.getFontConfigManager();
        if (fcCompFonts == null) {
            fcCompFonts = fcm.loadFontConfig();
            if (fcCompFonts != null) {
                try {
                    writeFcInfo();
                } catch (Exception e) {
                    if (FontUtilities.debugFonts()) {
                        warning("Exception writing fcInfo " + e);
                    }
                }
            } else if (FontUtilities.debugFonts()) {
                warning("Failed to get info from libfontconfig");
            }
        } else {
            fcm.populateFontConfig(fcCompFonts);
        }
        if (fcCompFonts == null) {
            return false; 
        }
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            throw new Error("java.home property not set");
        }
        String javaLib = javaHome + File.separator + "lib";
        getInstalledFallbackFonts(javaLib);
        return true;
    }
    @Override
    public String getFallbackFamilyName(String fontName,
                                        String defaultFallback) {
        String compatibilityName = getCompatibilityFamilyName(fontName);
        if (compatibilityName != null) {
            return compatibilityName;
        }
        return defaultFallback;
    }
    @Override
    protected String
        getFaceNameFromComponentFontName(String componentFontName) {
        return null;
    }
    @Override
    protected String
        getFileNameFromComponentFontName(String componentFontName) {
        return null;
    }
    @Override
    public String getFileNameFromPlatformName(String platformName) {
        return null;
    }
    @Override
    protected Charset getDefaultFontCharset(String fontName) {
        return Charset.forName("ISO8859_1");
    }
    @Override
    protected String getEncoding(String awtFontName,
                                 String characterSubsetName) {
        return "default";
    }
    @Override
    protected void initReorderMap() {
        reorderMap = new HashMap();
    }
    @Override
    public FontDescriptor[] getFontDescriptors(String fontName, int style) {
        return new FontDescriptor[0];
    }
    @Override
    public int getNumberCoreFonts() {
        return 1;
    }
    @Override
    public String[] getPlatformFontNames() {
        HashSet<String> nameSet = new HashSet<String>();
        X11FontManager fm = (X11FontManager) fontManager;
        FontConfigManager fcm = fm.getFontConfigManager();
        FcCompFont[] fcCompFonts = fcm.loadFontConfig();
        for (int i=0; i<fcCompFonts.length; i++) {
            for (int j=0; j<fcCompFonts[i].allFonts.length; j++) {
                nameSet.add(fcCompFonts[i].allFonts[j].fontFile);
            }
        }
        return nameSet.toArray(new String[0]);
    }
    @Override
    public String getExtraFontPath() {
        return null;
    }
    @Override
    public boolean needToSearchForFile(String fileName) {
        return false;
    }
    private FontConfigFont[] getFcFontList(FcCompFont[] fcFonts,
                                           String fontname, int style) {
        if (fontname.equals("dialog")) {
            fontname = "sansserif";
        } else if (fontname.equals("dialoginput")) {
            fontname = "monospaced";
        }
        for (int i=0; i<fcFonts.length; i++) {
            if (fontname.equals(fcFonts[i].jdkName) &&
                style == fcFonts[i].style) {
                return fcFonts[i].allFonts;
            }
        }
        return fcFonts[0].allFonts;
    }
    @Override
    public CompositeFontDescriptor[] get2DCompositeFontInfo() {
        X11FontManager fm = (X11FontManager) fontManager;
        FontConfigManager fcm = fm.getFontConfigManager();
        FcCompFont[] fcCompFonts = fcm.loadFontConfig();
        CompositeFontDescriptor[] result =
                new CompositeFontDescriptor[NUM_FONTS * NUM_STYLES];
        for (int fontIndex = 0; fontIndex < NUM_FONTS; fontIndex++) {
            String fontName = publicFontNames[fontIndex];
            for (int styleIndex = 0; styleIndex < NUM_STYLES; styleIndex++) {
                String faceName = fontName + "." + styleNames[styleIndex];
                FontConfigFont[] fcFonts =
                    getFcFontList(fcCompFonts,
                                  fontNames[fontIndex], styleIndex);
                int numFonts = fcFonts.length;
                if (installedFallbackFontFiles != null) {
                    numFonts += installedFallbackFontFiles.length;
                }
                String[] fileNames = new String[numFonts];
                int index;
                for (index = 0; index < fcFonts.length; index++) {
                    fileNames[index] = fcFonts[index].fontFile;
                }
                if (installedFallbackFontFiles != null) {
                    System.arraycopy(installedFallbackFontFiles, 0,
                                     fileNames, fcFonts.length,
                                     installedFallbackFontFiles.length);
                }
                result[fontIndex * NUM_STYLES + styleIndex]
                        = new CompositeFontDescriptor(
                            faceName,
                            1,
                            null,
                            fileNames,
                            null, null);
            }
        }
        return result;
    }
    private String getVersionString(File f){
        try {
            Scanner sc  = new Scanner(f);
            return sc.findInLine("(\\d)+((\\.)(\\d)+)*");
        }
        catch (Exception e){
        }
        return null;
    }
    @Override
    protected void setOsNameAndVersion() {
        super.setOsNameAndVersion();
        if (!osName.equals("Linux")) {
            return;
        }
        try {
            File f;
            if ((f = new File("/etc/lsb-release")).canRead()) {
                    Properties props = new Properties();
                    props.load(new FileInputStream(f));
                    osName = props.getProperty("DISTRIB_ID");
                    osVersion =  props.getProperty("DISTRIB_RELEASE");
            } else if ((f = new File("/etc/redhat-release")).canRead()) {
                osName = "RedHat";
                osVersion = getVersionString(f);
            } else if ((f = new File("/etc/SuSE-release")).canRead()) {
                osName = "SuSE";
                osVersion = getVersionString(f);
            } else if ((f = new File("/etc/turbolinux-release")).canRead()) {
                osName = "Turbo";
                osVersion = getVersionString(f);
            } else if ((f = new File("/etc/fedora-release")).canRead()) {
                osName = "Fedora";
                osVersion = getVersionString(f);
            }
        } catch (Exception e) {
            if (FontUtilities.debugFonts()) {
                warning("Exception identifying Linux distro.");
            }
        }
    }
    private File getFcInfoFile() {
        if (fcInfoFileName == null) {
            String hostname;
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                hostname = "localhost";
            }
            String userDir = System.getProperty("user.home");
            String version = System.getProperty("java.version");
            String fs = File.separator;
            String dir = userDir+fs+".java"+fs+"fonts"+fs+version;
            String lang = SunToolkit.getStartupLocale().getLanguage();
            String name = "fcinfo-"+fileVersion+"-"+hostname+"-"+
                osName+"-"+osVersion+"-"+lang+".properties";
            fcInfoFileName = dir+fs+name;
        }
        return new File(fcInfoFileName);
    }
    private void writeFcInfo() {
        Properties props = new Properties();
        props.setProperty("version", fileVersion);
        X11FontManager fm = (X11FontManager) fontManager;
        FontConfigManager fcm = fm.getFontConfigManager();
        FontConfigInfo fcInfo = fcm.getFontConfigInfo();
        props.setProperty("fcversion", Integer.toString(fcInfo.fcVersion));
        if (fcInfo.cacheDirs != null) {
            for (int i=0;i<fcInfo.cacheDirs.length;i++) {
                if (fcInfo.cacheDirs[i] != null) {
                   props.setProperty("cachedir."+i,  fcInfo.cacheDirs[i]);
                }
            }
        }
        for (int i=0; i<fcCompFonts.length; i++) {
            FcCompFont fci = fcCompFonts[i];
            String styleKey = fci.jdkName+"."+fci.style;
            props.setProperty(styleKey+".length",
                              Integer.toString(fci.allFonts.length));
            for (int j=0; j<fci.allFonts.length; j++) {
                props.setProperty(styleKey+"."+j+".family",
                                  fci.allFonts[j].familyName);
                props.setProperty(styleKey+"."+j+".file",
                                  fci.allFonts[j].fontFile);
            }
        }
        try {
            File fcInfoFile = getFcInfoFile();
            File dir = fcInfoFile.getParentFile();
            dir.mkdirs();
            File tempFile = File.createTempFile("fcinfo", null, dir);
            FileOutputStream fos = new FileOutputStream(tempFile);
            props.store(fos,
                      "JDK Font Configuration Generated File: *Do Not Edit*");
            fos.close();
            boolean renamed = tempFile.renameTo(fcInfoFile);
            if (!renamed && FontUtilities.debugFonts()) {
                System.out.println("rename failed");
                warning("Failed renaming file to "+ getFcInfoFile());
            }
        } catch (Exception e) {
            if (FontUtilities.debugFonts()) {
                warning("IOException writing to "+ getFcInfoFile());
            }
        }
    }
    private void readFcInfo() {
        File fcFile = getFcInfoFile();
        if (!fcFile.exists()) {
            return;
        }
        Properties props = new Properties();
        X11FontManager fm = (X11FontManager) fontManager;
        FontConfigManager fcm = fm.getFontConfigManager();
        try {
            FileInputStream fis = new FileInputStream(fcFile);
            props.load(fis);
            fis.close();
        } catch (IOException e) {
            if (FontUtilities.debugFonts()) {
                warning("IOException reading from "+fcFile.toString());
            }
            return;
        }
        String version = (String)props.get("version");
        if (version == null || !version.equals(fileVersion)) {
            return;
        }
        String fcVersionStr = (String)props.get("fcversion");
        if (fcVersionStr != null) {
            int fcVersion;
            try {
                fcVersion = Integer.parseInt(fcVersionStr);
                if (fcVersion != 0 &&
                    fcVersion != fcm.getFontConfigVersion()) {
                    return;
                }
            } catch (Exception e) {
                if (FontUtilities.debugFonts()) {
                    warning("Exception parsing version " + fcVersionStr);
                }
                return;
            }
        }
        long lastModified = fcFile.lastModified();
        int cacheDirIndex = 0;
        while (cacheDirIndex<4) { 
            String dir = (String)props.get("cachedir."+cacheDirIndex);
            if (dir == null) {
                break;
            }
            File dirFile = new File(dir);
            if (dirFile.exists() && dirFile.lastModified() > lastModified) {
                return;
            }
            cacheDirIndex++;
        }
        String[] names = { "sansserif", "serif", "monospaced" };
        String[] fcnames = { "sans", "serif", "monospace" };
        int namesLen = names.length;
        int numStyles = 4;
        FcCompFont[] fci = new FcCompFont[namesLen*numStyles];
        try {
            for (int i=0; i<namesLen; i++) {
                for (int s=0; s<numStyles; s++) {
                    int index = i*numStyles+s;
                    fci[index] = new FcCompFont();
                    String key = names[i]+"."+s;
                    fci[index].jdkName = names[i];
                    fci[index].fcFamily = fcnames[i];
                    fci[index].style = s;
                    String lenStr = (String)props.get(key+".length");
                    int nfonts = Integer.parseInt(lenStr);
                    if (nfonts <= 0) {
                        return; 
                    }
                    fci[index].allFonts = new FontConfigFont[nfonts];
                    for (int f=0; f<nfonts; f++) {
                        fci[index].allFonts[f] = new FontConfigFont();
                        String fkey = key+"."+f+".family";
                        String family = (String)props.get(fkey);
                        fci[index].allFonts[f].familyName = family;
                        fkey = key+"."+f+".file";
                        String file = (String)props.get(fkey);
                        if (file == null) {
                            return; 
                        }
                        fci[index].allFonts[f].fontFile = file;
                    }
                    fci[index].firstFont =  fci[index].allFonts[0];
                }
            }
            fcCompFonts = fci;
        } catch (Throwable t) {
            if (FontUtilities.debugFonts()) {
                warning(t.toString());
            }
        }
    }
    private static void warning(String msg) {
        PlatformLogger logger = PlatformLogger.getLogger("sun.awt.FontConfiguration");
        logger.warning(msg);
    }
}
