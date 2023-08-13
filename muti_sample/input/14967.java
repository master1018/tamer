public class FontConfigManager {
    static boolean fontConfigFailed = false;
    private static final FontConfigInfo fcInfo = new FontConfigInfo();
    public static class FontConfigFont {
        public String familyName;        
        public String styleStr;          
        public String fullName;          
        public String fontFile;          
    }
    public static class FcCompFont {
        public String fcName;            
        public String fcFamily;          
        public String jdkName;           
        public int style;                
        public FontConfigFont firstFont;
        public FontConfigFont[] allFonts;
        public CompositeFont compFont;   
    }
    public static class FontConfigInfo {
        public int fcVersion;
        public String[] cacheDirs = new String[4];
    }
    private static String[] fontConfigNames = {
        "sans:regular:roman",
        "sans:bold:roman",
        "sans:regular:italic",
        "sans:bold:italic",
        "serif:regular:roman",
        "serif:bold:roman",
        "serif:regular:italic",
        "serif:bold:italic",
        "monospace:regular:roman",
        "monospace:bold:roman",
        "monospace:regular:italic",
        "monospace:bold:italic",
    };
    private FcCompFont[] fontConfigFonts;
    public FontConfigManager() {
    }
    public static String[] getFontConfigNames() {
        return fontConfigNames;
    }
    public static Object getFontConfigAAHint() {
        return getFontConfigAAHint("sans");
    }
    public static Object getFontConfigAAHint(String fcFamily) {
        if (FontUtilities.isWindows) {
            return null;
        } else {
            int hint = getFontConfigAASettings(getFCLocaleStr(), fcFamily);
            if (hint < 0) {
                return null;
            } else {
                return SunHints.Value.get(SunHints.INTKEY_TEXT_ANTIALIASING,
                                          hint);
            }
        }
    }
    private static String getFCLocaleStr() {
        Locale l = SunToolkit.getStartupLocale();
        String localeStr = l.getLanguage();
        String country = l.getCountry();
        if (!country.equals("")) {
            localeStr = localeStr + "-" + country;
        }
        return localeStr;
    }
    public static native int getFontConfigVersion();
    public synchronized void initFontConfigFonts(boolean includeFallbacks) {
        if (fontConfigFonts != null) {
            if (!includeFallbacks || (fontConfigFonts[0].allFonts != null)) {
                return;
            }
        }
        if (FontUtilities.isWindows || fontConfigFailed) {
            return;
        }
        long t0 = 0;
        if (FontUtilities.isLogging()) {
            t0 = System.nanoTime();
        }
        String[] fontConfigNames = FontConfigManager.getFontConfigNames();
        FcCompFont[] fontArr = new FcCompFont[fontConfigNames.length];
        for (int i = 0; i< fontArr.length; i++) {
            fontArr[i] = new FcCompFont();
            fontArr[i].fcName = fontConfigNames[i];
            int colonPos = fontArr[i].fcName.indexOf(':');
            fontArr[i].fcFamily = fontArr[i].fcName.substring(0, colonPos);
            fontArr[i].jdkName = FontUtilities.mapFcName(fontArr[i].fcFamily);
            fontArr[i].style = i % 4; 
        }
        getFontConfig(getFCLocaleStr(), fcInfo, fontArr, includeFallbacks);
        FontConfigFont anyFont = null;
        for (int i = 0; i< fontArr.length; i++) {
            FcCompFont fci = fontArr[i];
            if (fci.firstFont == null) {
                if (FontUtilities.isLogging()) {
                    PlatformLogger logger = FontUtilities.getLogger();
                    logger.info("Fontconfig returned no font for " +
                                fontArr[i].fcName);
                }
                fontConfigFailed = true;
            } else if (anyFont == null) {
                anyFont = fci.firstFont;
            }
        }
        if (anyFont == null) {
            if (FontUtilities.isLogging()) {
                PlatformLogger logger = FontUtilities.getLogger();
                logger.info("Fontconfig returned no fonts at all.");
            }
            fontConfigFailed = true;
            return;
        } else if (fontConfigFailed) {
            for (int i = 0; i< fontArr.length; i++) {
                if (fontArr[i].firstFont == null) {
                    fontArr[i].firstFont = anyFont;
                }
            }
        }
        fontConfigFonts = fontArr;
        if (FontUtilities.isLogging()) {
            PlatformLogger logger = FontUtilities.getLogger();
            long t1 = System.nanoTime();
            logger.info("Time spent accessing fontconfig="
                        + ((t1 - t0) / 1000000) + "ms.");
            for (int i = 0; i< fontConfigFonts.length; i++) {
                FcCompFont fci = fontConfigFonts[i];
                logger.info("FC font " + fci.fcName+" maps to family " +
                            fci.firstFont.familyName +
                            " in file " + fci.firstFont.fontFile);
                if (fci.allFonts != null) {
                    for (int f=0;f<fci.allFonts.length;f++) {
                        FontConfigFont fcf = fci.allFonts[f];
                        logger.info("Family=" + fcf.familyName +
                                    " Style="+ fcf.styleStr +
                                    " Fullname="+fcf.fullName +
                                    " File="+fcf.fontFile);
                    }
                }
            }
        }
    }
    public PhysicalFont registerFromFcInfo(FcCompFont fcInfo) {
        SunFontManager fm = SunFontManager.getInstance();
        String fontFile = fcInfo.firstFont.fontFile;
        int offset = fontFile.length()-4;
        if (offset <= 0) {
            return null;
        }
        String ext = fontFile.substring(offset).toLowerCase();
        boolean isTTC = ext.equals(".ttc");
        PhysicalFont physFont = fm.getRegisteredFontFile(fontFile);
        if (physFont != null) {
            if (isTTC) {
                Font2D f2d = fm.findFont2D(fcInfo.firstFont.familyName,
                                           fcInfo.style,
                                           FontManager.NO_FALLBACK);
                if (f2d instanceof PhysicalFont) { 
                    return (PhysicalFont)f2d;
                } else {
                    return null;
                }
            } else {
                return physFont;
            }
        }
        physFont = fm.findJREDeferredFont(fcInfo.firstFont.familyName,
                                          fcInfo.style);
        if (physFont == null &&
            fm.isDeferredFont(fontFile) == true) {
            physFont = fm.initialiseDeferredFont(fcInfo.firstFont.fontFile);
            if (physFont != null) {
                if (isTTC) {
                    Font2D f2d = fm.findFont2D(fcInfo.firstFont.familyName,
                                               fcInfo.style,
                                               FontManager.NO_FALLBACK);
                    if (f2d instanceof PhysicalFont) { 
                        return (PhysicalFont)f2d;
                    } else {
                        return null;
                    }
                } else {
                    return physFont;
                }
            }
        }
        if (physFont == null) {
            int fontFormat = SunFontManager.FONTFORMAT_NONE;
            int fontRank = Font2D.UNKNOWN_RANK;
            if (ext.equals(".ttf") || isTTC) {
                fontFormat = SunFontManager.FONTFORMAT_TRUETYPE;
                fontRank = Font2D.TTF_RANK;
            } else if (ext.equals(".pfa") || ext.equals(".pfb")) {
                fontFormat = SunFontManager.FONTFORMAT_TYPE1;
                fontRank = Font2D.TYPE1_RANK;
            }
            physFont = fm.registerFontFile(fcInfo.firstFont.fontFile, null,
                                      fontFormat, true, fontRank);
        }
        return physFont;
    }
    public CompositeFont getFontConfigFont(String name, int style) {
        name = name.toLowerCase();
        initFontConfigFonts(false);
        FcCompFont fcInfo = null;
        for (int i=0; i<fontConfigFonts.length; i++) {
            if (name.equals(fontConfigFonts[i].fcFamily) &&
                style == fontConfigFonts[i].style) {
                fcInfo = fontConfigFonts[i];
                break;
            }
        }
        if (fcInfo == null) {
            fcInfo = fontConfigFonts[0];
        }
        if (FontUtilities.isLogging()) {
            FontUtilities.getLogger()
                          .info("FC name=" + name + " style=" + style +
                                " uses " + fcInfo.firstFont.familyName +
                                " in file: " + fcInfo.firstFont.fontFile);
        }
        if (fcInfo.compFont != null) {
            return fcInfo.compFont;
        }
        FontManager fm = FontManagerFactory.getInstance();
        CompositeFont jdkFont = (CompositeFont)
            fm.findFont2D(fcInfo.jdkName, style, FontManager.LOGICAL_FALLBACK);
        if (fcInfo.firstFont.familyName == null ||
            fcInfo.firstFont.fontFile == null) {
            return (fcInfo.compFont = jdkFont);
        }
        FontFamily family = FontFamily.getFamily(fcInfo.firstFont.familyName);
        PhysicalFont physFont = null;
        if (family != null) {
            Font2D f2D = family.getFontWithExactStyleMatch(fcInfo.style);
            if (f2D instanceof PhysicalFont) {
                physFont = (PhysicalFont)f2D;
            }
        }
        if (physFont == null ||
            !fcInfo.firstFont.fontFile.equals(physFont.platName)) {
            physFont = registerFromFcInfo(fcInfo);
            if (physFont == null) {
                return (fcInfo.compFont = jdkFont);
            }
            family = FontFamily.getFamily(physFont.getFamilyName(null));
        }
        for (int i=0; i<fontConfigFonts.length; i++) {
            FcCompFont fc = fontConfigFonts[i];
            if (fc != fcInfo &&
                physFont.getFamilyName(null).equals(fc.firstFont.familyName) &&
                !fc.firstFont.fontFile.equals(physFont.platName) &&
                family.getFontWithExactStyleMatch(fc.style) == null) {
                registerFromFcInfo(fontConfigFonts[i]);
            }
        }
        return (fcInfo.compFont = new CompositeFont(physFont, jdkFont));
    }
    public FcCompFont[] getFontConfigFonts() {
        return fontConfigFonts;
    }
    private static native void getFontConfig(String locale,
                                             FontConfigInfo fcInfo,
                                             FcCompFont[] fonts,
                                             boolean includeFallbacks);
    void populateFontConfig(FcCompFont[] fcInfo) {
        fontConfigFonts = fcInfo;
    }
    FcCompFont[] loadFontConfig() {
        initFontConfigFonts(true);
        return fontConfigFonts;
    }
    FontConfigInfo getFontConfigInfo() {
        initFontConfigFonts(true);
        return fcInfo;
    }
    private static native int
    getFontConfigAASettings(String locale, String fcFamily);
}
