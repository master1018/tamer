public class Win32FontManager extends SunFontManager {
    private static String[] defaultPlatformFont = null;
    private static TrueTypeFont eudcFont;
    static {
        AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    String eudcFile = getEUDCFontFile();
                    if (eudcFile != null) {
                        try {
                            eudcFont = new TrueTypeFont(eudcFile, null, 0,
                                                        true);
                        } catch (FontFormatException e) {
                        }
                    }
                    return null;
                }
            });
    }
    private static native String getEUDCFontFile();
    public TrueTypeFont getEUDCFont() {
        return eudcFont;
    }
    public Win32FontManager() {
        super();
        AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    registerJREFontsWithPlatform(jreFontDirName);
                    return null;
                }
            });
    }
    protected boolean useAbsoluteFontFileNames() {
        return false;
    }
    protected void registerFontFile(String fontFileName, String[] nativeNames,
                                    int fontRank, boolean defer) {
        if (registeredFontFiles.contains(fontFileName)) {
            return;
        }
        registeredFontFiles.add(fontFileName);
        int fontFormat;
        if (getTrueTypeFilter().accept(null, fontFileName)) {
            fontFormat = SunFontManager.FONTFORMAT_TRUETYPE;
        } else if (getType1Filter().accept(null, fontFileName)) {
            fontFormat = SunFontManager.FONTFORMAT_TYPE1;
        } else {
            return;
        }
        if (fontPath == null) {
            fontPath = getPlatformFontPath(noType1Font);
        }
        String tmpFontPath = jreFontDirName+File.pathSeparator+fontPath;
        StringTokenizer parser = new StringTokenizer(tmpFontPath,
                                                     File.pathSeparator);
        boolean found = false;
        try {
            while (!found && parser.hasMoreTokens()) {
                String newPath = parser.nextToken();
                boolean isJREFont = newPath.equals(jreFontDirName);
                File theFile = new File(newPath, fontFileName);
                if (theFile.canRead()) {
                    found = true;
                    String path = theFile.getAbsolutePath();
                    if (defer) {
                        registerDeferredFont(fontFileName, path,
                                             nativeNames,
                                             fontFormat, isJREFont,
                                             fontRank);
                    } else {
                        registerFontFile(path, nativeNames,
                                         fontFormat, isJREFont,
                                         fontRank);
                    }
                    break;
                }
            }
        } catch (NoSuchElementException e) {
            System.err.println(e);
        }
        if (!found) {
            addToMissingFontFileList(fontFileName);
        }
    }
    @Override
    protected FontConfiguration createFontConfiguration() {
       FontConfiguration fc = new WFontConfiguration(this);
       fc.init();
       return fc;
    }
    @Override
    public FontConfiguration createFontConfiguration(boolean preferLocaleFonts,
            boolean preferPropFonts) {
        return new WFontConfiguration(this,
                                      preferLocaleFonts,preferPropFonts);
    }
    protected void
        populateFontFileNameMap(HashMap<String,String> fontToFileMap,
                                HashMap<String,String> fontToFamilyNameMap,
                                HashMap<String,ArrayList<String>>
                                familyToFontListMap,
                                Locale locale) {
        populateFontFileNameMap0(fontToFileMap, fontToFamilyNameMap,
                                 familyToFontListMap, locale);
    }
    private static native void
        populateFontFileNameMap0(HashMap<String,String> fontToFileMap,
                                 HashMap<String,String> fontToFamilyNameMap,
                                 HashMap<String,ArrayList<String>>
                                     familyToFontListMap,
                                 Locale locale);
    protected synchronized native String getFontPath(boolean noType1Fonts);
    public String[] getDefaultPlatformFont() {
        if (defaultPlatformFont != null) {
            return defaultPlatformFont;
        }
        String[] info = new String[2];
        info[0] = "Arial";
        info[1] = "c:\\windows\\fonts";
        final String[] dirs = getPlatformFontDirs(true);
        if (dirs.length > 1) {
            String dir = (String)
                AccessController.doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            for (int i=0; i<dirs.length; i++) {
                                String path =
                                    dirs[i] + File.separator + "arial.ttf";
                                File file = new File(path);
                                if (file.exists()) {
                                    return dirs[i];
                                }
                            }
                            return null;
                        }
                    });
            if (dir != null) {
                info[1] = dir;
            }
        } else {
            info[1] = dirs[0];
        }
        info[1] = info[1] + File.separator + "arial.ttf";
        defaultPlatformFont = info;
        return defaultPlatformFont;
    }
    static String fontsForPrinting = null;
    protected void registerJREFontsWithPlatform(String pathName) {
        fontsForPrinting = pathName;
    }
    public static void registerJREFontsForPrinting() {
        final String pathName;
        synchronized (Win32GraphicsEnvironment.class) {
            GraphicsEnvironment.getLocalGraphicsEnvironment();
            if (fontsForPrinting == null) {
                return;
            }
            pathName = fontsForPrinting;
            fontsForPrinting = null;
        }
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                    File f1 = new File(pathName);
                    String[] ls = f1.list(SunFontManager.getInstance().
                            getTrueTypeFilter());
                    if (ls == null) {
                        return null;
                    }
                    for (int i=0; i <ls.length; i++ ) {
                        File fontFile = new File(f1, ls[i]);
                        registerFontWithPlatform(fontFile.getAbsolutePath());
                    }
                    return null;
                }
         });
    }
    protected static native void registerFontWithPlatform(String fontName);
    protected static native void deRegisterFontWithPlatform(String fontName);
    @Override
    public HashMap<String, FamilyDescription> populateHardcodedFileNameMap() {
        HashMap<String, FamilyDescription> platformFontMap
            = new HashMap<String, FamilyDescription>();
        FamilyDescription fd;
        fd = new FamilyDescription();
        fd.familyName = "Segoe UI";
        fd.plainFullName = "Segoe UI";
        fd.plainFileName = "segoeui.ttf";
        fd.boldFullName = "Segoe UI Bold";
        fd.boldFileName = "segoeuib.ttf";
        fd.italicFullName = "Segoe UI Italic";
        fd.italicFileName = "segoeuii.ttf";
        fd.boldItalicFullName = "Segoe UI Bold Italic";
        fd.boldItalicFileName = "segoeuiz.ttf";
        platformFontMap.put("segoe", fd);
        fd = new FamilyDescription();
        fd.familyName = "Tahoma";
        fd.plainFullName = "Tahoma";
        fd.plainFileName = "tahoma.ttf";
        fd.boldFullName = "Tahoma Bold";
        fd.boldFileName = "tahomabd.ttf";
        platformFontMap.put("tahoma", fd);
        fd = new FamilyDescription();
        fd.familyName = "Verdana";
        fd.plainFullName = "Verdana";
        fd.plainFileName = "verdana.TTF";
        fd.boldFullName = "Verdana Bold";
        fd.boldFileName = "verdanab.TTF";
        fd.italicFullName = "Verdana Italic";
        fd.italicFileName = "verdanai.TTF";
        fd.boldItalicFullName = "Verdana Bold Italic";
        fd.boldItalicFileName = "verdanaz.TTF";
        platformFontMap.put("verdana", fd);
        fd = new FamilyDescription();
        fd.familyName = "Arial";
        fd.plainFullName = "Arial";
        fd.plainFileName = "ARIAL.TTF";
        fd.boldFullName = "Arial Bold";
        fd.boldFileName = "ARIALBD.TTF";
        fd.italicFullName = "Arial Italic";
        fd.italicFileName = "ARIALI.TTF";
        fd.boldItalicFullName = "Arial Bold Italic";
        fd.boldItalicFileName = "ARIALBI.TTF";
        platformFontMap.put("arial", fd);
        fd = new FamilyDescription();
        fd.familyName = "Symbol";
        fd.plainFullName = "Symbol";
        fd.plainFileName = "Symbol.TTF";
        platformFontMap.put("symbol", fd);
        fd = new FamilyDescription();
        fd.familyName = "WingDings";
        fd.plainFullName = "WingDings";
        fd.plainFileName = "WINGDING.TTF";
        platformFontMap.put("wingdings", fd);
        return platformFontMap;
    }
}
