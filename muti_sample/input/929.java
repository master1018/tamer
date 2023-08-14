public class X11FontManager extends SunFontManager {
    private static final int FOUNDRY_FIELD = 1;
    private static final int FAMILY_NAME_FIELD = 2;
    private static final int WEIGHT_NAME_FIELD = 3;
    private static final int SLANT_FIELD = 4;
    private static final int SETWIDTH_NAME_FIELD = 5;
    private static final int ADD_STYLE_NAME_FIELD = 6;
    private static final int PIXEL_SIZE_FIELD = 7;
    private static final int POINT_SIZE_FIELD = 8;
    private static final int RESOLUTION_X_FIELD = 9;
    private static final int RESOLUTION_Y_FIELD = 10;
    private static final int SPACING_FIELD = 11;
    private static final int AVERAGE_WIDTH_FIELD = 12;
    private static final int CHARSET_REGISTRY_FIELD = 13;
    private static final int CHARSET_ENCODING_FIELD = 14;
    private static Map fontNameMap = new HashMap();
    private static Map xlfdMap = new HashMap();
     private static Map xFontDirsMap;
     private static HashSet<String> fontConfigDirs = null;
    HashMap<String, String> oblmap = null;
     private static HashMap registeredDirs = new HashMap();
     private static String[] fontdirs = null;
    private static String[] defaultPlatformFont = null;
    private FontConfigManager fcManager = null;
    public static X11FontManager getInstance() {
        return (X11FontManager) SunFontManager.getInstance();
    }
    @Override
    public String getFileNameFromPlatformName(String platName) {
        if (platName.startsWith("/")) {
            return platName;
        }
        String fileName = null;
        String fontID = specificFontIDForName(platName);
        fileName = super.getFileNameFromPlatformName(platName);
        if (fileName != null) {
            if (isHeadless() && fileName.startsWith("-")) {
                    return null;
            }
            if (fileName.startsWith("/")) {
                Vector xVal = (Vector) xlfdMap.get(fileName);
                if (xVal == null) {
                    if (getFontConfiguration().needToSearchForFile(fileName)) {
                        fileName = null;
                    }
                    if (fileName != null) {
                        xVal = new Vector();
                        xVal.add(platName);
                        xlfdMap.put(fileName, xVal);
                    }
                } else {
                    if (!xVal.contains(platName)) {
                        xVal.add(platName);
                    }
                }
            }
            if (fileName != null) {
                fontNameMap.put(fontID, fileName);
                return fileName;
            }
        }
        if (fontID != null) {
            fileName = (String)fontNameMap.get(fontID);
            if (fileName == null && FontUtilities.isLinux && !isOpenJDK()) {
                if (oblmap == null) {
                    initObliqueLucidaFontMap();
                }
                String oblkey = getObliqueLucidaFontID(fontID);
                if (oblkey != null) {
                    fileName = oblmap.get(oblkey);
                }
            }
            if (fontPath == null &&
                (fileName == null || !fileName.startsWith("/"))) {
                if (FontUtilities.debugFonts()) {
                    FontUtilities.getLogger()
                          .warning("** Registering all font paths because " +
                                   "can't find file for " + platName);
                }
                fontPath = getPlatformFontPath(noType1Font);
                registerFontDirs(fontPath);
                if (FontUtilities.debugFonts()) {
                    FontUtilities.getLogger()
                            .warning("** Finished registering all font paths");
                }
                fileName = (String)fontNameMap.get(fontID);
            }
            if (fileName == null && !isHeadless()) {
                fileName = getX11FontName(platName);
            }
            if (fileName == null) {
                fontID = switchFontIDForName(platName);
                fileName = (String)fontNameMap.get(fontID);
            }
            if (fileName != null) {
                fontNameMap.put(fontID, fileName);
            }
        }
        return fileName;
    }
    @Override
    protected String[] getNativeNames(String fontFileName,
            String platformName) {
        Vector nativeNames;
        if ((nativeNames=(Vector)xlfdMap.get(fontFileName))==null) {
            if (platformName == null) {
                return null;
            } else {
                String []natNames = new String[1];
                natNames[0] = platformName;
                return natNames;
            }
        } else {
            int len = nativeNames.size();
            return (String[])nativeNames.toArray(new String[len]);
        }
    }
    @Override
    protected void registerFontDir(String path) {
        if (FontUtilities.debugFonts()) {
            FontUtilities.getLogger().info("ParseFontDir " + path);
        }
        File fontsDotDir = new File(path + File.separator + "fonts.dir");
        FileReader fr = null;
        try {
            if (fontsDotDir.canRead()) {
                fr = new FileReader(fontsDotDir);
                BufferedReader br = new BufferedReader(fr, 8192);
                StreamTokenizer st = new StreamTokenizer(br);
                st.eolIsSignificant(true);
                int ttype = st.nextToken();
                if (ttype == StreamTokenizer.TT_NUMBER) {
                    int numEntries = (int)st.nval;
                    ttype = st.nextToken();
                    if (ttype == StreamTokenizer.TT_EOL) {
                        st.resetSyntax();
                        st.wordChars(32, 127);
                        st.wordChars(128 + 32, 255);
                        st.whitespaceChars(0, 31);
                        for (int i=0; i < numEntries; i++) {
                            ttype = st.nextToken();
                            if (ttype == StreamTokenizer.TT_EOF) {
                                break;
                            }
                            if (ttype != StreamTokenizer.TT_WORD) {
                                break;
                            }
                            int breakPos = st.sval.indexOf(' ');
                            if (breakPos <= 0) {
                                numEntries++;
                                ttype = st.nextToken();
                                if (ttype != StreamTokenizer.TT_EOL) {
                                    break;
                                }
                                continue;
                            }
                            if (st.sval.charAt(0) == '!') {
                                numEntries++;
                                ttype = st.nextToken();
                                if (ttype != StreamTokenizer.TT_EOL) {
                                    break;
                                }
                                continue;
                            }
                            String fileName = st.sval.substring(0, breakPos);
                            int lastColon = fileName.lastIndexOf(':');
                            if (lastColon > 0) {
                                if (lastColon+1 >= fileName.length()) {
                                    continue;
                                }
                                fileName = fileName.substring(lastColon+1);
                            }
                            String fontPart = st.sval.substring(breakPos+1);
                            String fontID = specificFontIDForName(fontPart);
                            String sVal = (String) fontNameMap.get(fontID);
                            if (FontUtilities.debugFonts()) {
                                PlatformLogger logger = FontUtilities.getLogger();
                                logger.info("file=" + fileName +
                                            " xlfd=" + fontPart);
                                logger.info("fontID=" + fontID +
                                            " sVal=" + sVal);
                            }
                            String fullPath = null;
                            try {
                                File file = new File(path,fileName);
                                if (xFontDirsMap == null) {
                                    xFontDirsMap = new HashMap();
                                }
                                xFontDirsMap.put(fontID, path);
                                fullPath = file.getCanonicalPath();
                            } catch (IOException e) {
                                fullPath = path + File.separator + fileName;
                            }
                            Vector xVal = (Vector) xlfdMap.get(fullPath);
                            if (FontUtilities.debugFonts()) {
                                FontUtilities.getLogger()
                                      .info("fullPath=" + fullPath +
                                            " xVal=" + xVal);
                            }
                            if ((xVal == null || !xVal.contains(fontPart)) &&
                                (sVal == null) || !sVal.startsWith("/")) {
                                if (FontUtilities.debugFonts()) {
                                    FontUtilities.getLogger()
                                          .info("Map fontID:"+fontID +
                                                "to file:" + fullPath);
                                }
                                fontNameMap.put(fontID, fullPath);
                                if (xVal == null) {
                                    xVal = new Vector();
                                    xlfdMap.put (fullPath, xVal);
                                }
                                xVal.add(fontPart);
                            }
                            ttype = st.nextToken();
                            if (ttype != StreamTokenizer.TT_EOL) {
                                break;
                            }
                        }
                    }
                }
                fr.close();
            }
        } catch (IOException ioe1) {
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                }  catch (IOException ioe2) {
                }
            }
        }
    }
    @Override
    public void loadFonts() {
        super.loadFonts();
        xFontDirsMap = null;
        xlfdMap = new HashMap(1);
        fontNameMap = new HashMap(1);
    }
    private String getObliqueLucidaFontID(String fontID) {
        if (fontID.startsWith("-lucidasans-medium-i-normal") ||
            fontID.startsWith("-lucidasans-bold-i-normal") ||
            fontID.startsWith("-lucidatypewriter-medium-i-normal") ||
            fontID.startsWith("-lucidatypewriter-bold-i-normal")) {
            return fontID.substring(0, fontID.indexOf("-i-"));
        } else {
            return null;
        }
    }
    private static String getX11FontName(String platName) {
        String xlfd = platName.replaceAll("%d", "*");
        if (NativeFont.fontExists(xlfd)) {
            return xlfd;
        } else {
            return null;
        }
    }
    private void initObliqueLucidaFontMap() {
        oblmap = new HashMap<String, String>();
        oblmap.put("-lucidasans-medium",
                   jreLibDirName+"/fonts/LucidaSansRegular.ttf");
        oblmap.put("-lucidasans-bold",
                   jreLibDirName+"/fonts/LucidaSansDemiBold.ttf");
        oblmap.put("-lucidatypewriter-medium",
                   jreLibDirName+"/fonts/LucidaTypewriterRegular.ttf");
        oblmap.put("-lucidatypewriter-bold",
                   jreLibDirName+"/fonts/LucidaTypewriterBold.ttf");
    }
    private boolean isHeadless() {
        GraphicsEnvironment ge =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        return GraphicsEnvironment.isHeadless();
    }
    private String specificFontIDForName(String name) {
        int[] hPos = new int[14];
        int hyphenCnt = 1;
        int pos = 1;
        while (pos != -1 && hyphenCnt < 14) {
            pos = name.indexOf('-', pos);
            if (pos != -1) {
                hPos[hyphenCnt++] = pos;
                    pos++;
            }
        }
        if (hyphenCnt != 14) {
            if (FontUtilities.debugFonts()) {
                FontUtilities.getLogger()
                    .severe("Font Configuration Font ID is malformed:" + name);
            }
            return name; 
        }
        StringBuffer sb =
            new StringBuffer(name.substring(hPos[FAMILY_NAME_FIELD-1],
                                            hPos[SETWIDTH_NAME_FIELD]));
        sb.append(name.substring(hPos[CHARSET_REGISTRY_FIELD-1]));
        String retval = sb.toString().toLowerCase (Locale.ENGLISH);
        return retval;
    }
    private String switchFontIDForName(String name) {
        int[] hPos = new int[14];
        int hyphenCnt = 1;
        int pos = 1;
        while (pos != -1 && hyphenCnt < 14) {
            pos = name.indexOf('-', pos);
            if (pos != -1) {
                hPos[hyphenCnt++] = pos;
                    pos++;
            }
        }
        if (hyphenCnt != 14) {
            if (FontUtilities.debugFonts()) {
                FontUtilities.getLogger()
                    .severe("Font Configuration Font ID is malformed:" + name);
            }
            return name; 
        }
        String slant = name.substring(hPos[SLANT_FIELD-1]+1,
                                           hPos[SLANT_FIELD]);
        String family = name.substring(hPos[FAMILY_NAME_FIELD-1]+1,
                                           hPos[FAMILY_NAME_FIELD]);
        String registry = name.substring(hPos[CHARSET_REGISTRY_FIELD-1]+1,
                                           hPos[CHARSET_REGISTRY_FIELD]);
        String encoding = name.substring(hPos[CHARSET_ENCODING_FIELD-1]+1);
        if (slant.equals("i")) {
            slant = "o";
        } else if (slant.equals("o")) {
            slant = "i";
        }
        if (family.equals("itc zapfdingbats")
            && registry.equals("sun")
            && encoding.equals("fontspecific")){
            registry = "adobe";
        }
        StringBuffer sb =
            new StringBuffer(name.substring(hPos[FAMILY_NAME_FIELD-1],
                                            hPos[SLANT_FIELD-1]+1));
        sb.append(slant);
        sb.append(name.substring(hPos[SLANT_FIELD],
                                 hPos[SETWIDTH_NAME_FIELD]+1));
        sb.append(registry);
        sb.append(name.substring(hPos[CHARSET_ENCODING_FIELD-1]));
        String retval = sb.toString().toLowerCase (Locale.ENGLISH);
        return retval;
    }
    public String getFileNameFromXLFD(String name) {
        String fileName = null;
        String fontID = specificFontIDForName(name);
        if (fontID != null) {
            fileName = (String)fontNameMap.get(fontID);
            if (fileName == null) {
                fontID = switchFontIDForName(name);
                fileName = (String)fontNameMap.get(fontID);
            }
            if (fileName == null) {
                fileName = getDefaultFontFile();
            }
        }
        return fileName;
    }
    @Override
    protected void registerFontDirs(String pathName) {
        StringTokenizer parser = new StringTokenizer(pathName,
                                                     File.pathSeparator);
        try {
            while (parser.hasMoreTokens()) {
                String dirPath = parser.nextToken();
                if (dirPath != null && !registeredDirs.containsKey(dirPath)) {
                    registeredDirs.put(dirPath, null);
                    registerFontDir(dirPath);
                }
            }
        } catch (NoSuchElementException e) {
        }
    }
    @Override
    protected void addFontToPlatformFontPath(String platformName) {
        getPlatformFontPathFromFontConfig();
        if (xFontDirsMap != null) {
            String fontID = specificFontIDForName(platformName);
            String dirName = (String)xFontDirsMap.get(fontID);
            if (dirName != null) {
                fontConfigDirs.add(dirName);
            }
        }
        return;
    }
    private void getPlatformFontPathFromFontConfig() {
        if (fontConfigDirs == null) {
            fontConfigDirs = getFontConfiguration().getAWTFontPathSet();
            if (FontUtilities.debugFonts() && fontConfigDirs != null) {
                String[] names = fontConfigDirs.toArray(new String[0]);
                for (int i=0;i<names.length;i++) {
                    FontUtilities.getLogger().info("awtfontpath : " + names[i]);
                }
            }
        }
    }
    @Override
    protected void registerPlatformFontsUsedByFontConfiguration() {
        getPlatformFontPathFromFontConfig();
        if (fontConfigDirs == null) {
            return;
        }
        if (FontUtilities.isLinux) {
            fontConfigDirs.add(jreLibDirName+File.separator+"oblique-fonts");
        }
        fontdirs = (String[])fontConfigDirs.toArray(new String[0]);
    }
    protected FontConfiguration createFontConfiguration() {
        FontConfiguration mFontConfig = new MFontConfiguration(this);
        if (FontUtilities.isOpenSolaris ||
            (FontUtilities.isLinux &&
             (!mFontConfig.foundOsSpecificFile() ||
              !mFontConfig.fontFilesArePresent()) ||
             (FontUtilities.isSolaris && !mFontConfig.fontFilesArePresent()))) {
            FcFontConfiguration fcFontConfig =
                new FcFontConfiguration(this);
            if (fcFontConfig.init()) {
                return fcFontConfig;
            }
        }
        mFontConfig.init();
        return mFontConfig;
    }
    public FontConfiguration
        createFontConfiguration(boolean preferLocaleFonts,
                                boolean preferPropFonts) {
        return new MFontConfiguration(this,
                                      preferLocaleFonts, preferPropFonts);
    }
    public synchronized native String getFontPathNative(boolean noType1Fonts);
    protected synchronized String getFontPath(boolean noType1Fonts) {
        isHeadless(); 
        return getFontPathNative(noType1Fonts);
    }
    public String[] getDefaultPlatformFont() {
        if (defaultPlatformFont != null) {
            return defaultPlatformFont;
        }
        String[] info = new String[2];
        getFontConfigManager().initFontConfigFonts(false);
        FontConfigManager.FcCompFont[] fontConfigFonts =
            getFontConfigManager().getFontConfigFonts();
        for (int i=0; i<fontConfigFonts.length; i++) {
            if ("sans".equals(fontConfigFonts[i].fcFamily) &&
                0 == fontConfigFonts[i].style) {
                info[0] = fontConfigFonts[i].firstFont.familyName;
                info[1] = fontConfigFonts[i].firstFont.fontFile;
                break;
            }
        }
        if (info[0] == null) {
            if (fontConfigFonts.length > 0 &&
                fontConfigFonts[0].firstFont.fontFile != null) {
                info[0] = fontConfigFonts[0].firstFont.familyName;
                info[1] = fontConfigFonts[0].firstFont.fontFile;
            } else {
                info[0] = "Dialog";
                info[1] = "/dialog.ttf";
            }
        }
        defaultPlatformFont = info;
        return defaultPlatformFont;
    }
    public synchronized FontConfigManager getFontConfigManager() {
        if (fcManager == null) {
            fcManager = new FontConfigManager();
        }
        return fcManager;
    }
    @Override
    protected FontUIResource getFontConfigFUIR(String family, int style, int size) {
        CompositeFont font2D = getFontConfigManager().getFontConfigFont(family, style);
        if (font2D == null) { 
           return new FontUIResource(family, style, size);
        }
        FontUIResource fuir =
            new FontUIResource(font2D.getFamilyName(null), style, size);
        FontAccess.getFontAccess().setFont2D(fuir, font2D.handle);
        FontAccess.getFontAccess().setCreatedFont(fuir);
        return fuir;
    }
}
