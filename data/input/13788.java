public abstract class SunFontManager implements FontSupport, FontManagerForSGE {
    private static class TTFilter implements FilenameFilter {
        public boolean accept(File dir,String name) {
            int offset = name.length()-4;
            if (offset <= 0) { 
                return false;
            } else {
                return(name.startsWith(".ttf", offset) ||
                       name.startsWith(".TTF", offset) ||
                       name.startsWith(".ttc", offset) ||
                       name.startsWith(".TTC", offset) ||
                       name.startsWith(".otf", offset) ||
                       name.startsWith(".OTF", offset));
            }
        }
    }
    private static class T1Filter implements FilenameFilter {
        public boolean accept(File dir,String name) {
            if (noType1Font) {
                return false;
            }
            int offset = name.length()-4;
            if (offset <= 0) { 
                return false;
            } else {
                return(name.startsWith(".pfa", offset) ||
                       name.startsWith(".pfb", offset) ||
                       name.startsWith(".PFA", offset) ||
                       name.startsWith(".PFB", offset));
            }
        }
    }
     private static class TTorT1Filter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            int offset = name.length()-4;
            if (offset <= 0) { 
                return false;
            } else {
                boolean isTT =
                    name.startsWith(".ttf", offset) ||
                    name.startsWith(".TTF", offset) ||
                    name.startsWith(".ttc", offset) ||
                    name.startsWith(".TTC", offset) ||
                    name.startsWith(".otf", offset) ||
                    name.startsWith(".OTF", offset);
                if (isTT) {
                    return true;
                } else if (noType1Font) {
                    return false;
                } else {
                    return(name.startsWith(".pfa", offset) ||
                           name.startsWith(".pfb", offset) ||
                           name.startsWith(".PFA", offset) ||
                           name.startsWith(".PFB", offset));
                }
            }
        }
    }
     public static final int FONTFORMAT_NONE = -1;
     public static final int FONTFORMAT_TRUETYPE = 0;
     public static final int FONTFORMAT_TYPE1 = 1;
     public static final int FONTFORMAT_T2K = 2;
     public static final int FONTFORMAT_TTC = 3;
     public static final int FONTFORMAT_COMPOSITE = 4;
     public static final int FONTFORMAT_NATIVE = 5;
     private static final int CHANNELPOOLSIZE = 20;
     private int lastPoolIndex = 0;
     private FileFont fontFileCache[] = new FileFont[CHANNELPOOLSIZE];
    private int maxCompFont = 0;
    private CompositeFont [] compFonts = new CompositeFont[20];
    private ConcurrentHashMap<String, CompositeFont>
        compositeFonts = new ConcurrentHashMap<String, CompositeFont>();
    private ConcurrentHashMap<String, PhysicalFont>
        physicalFonts = new ConcurrentHashMap<String, PhysicalFont>();
    private ConcurrentHashMap<String, PhysicalFont>
        registeredFonts = new ConcurrentHashMap<String, PhysicalFont>();
    private ConcurrentHashMap<String, Font2D>
        fullNameToFont = new ConcurrentHashMap<String, Font2D>();
    private HashMap<String, TrueTypeFont> localeFullNamesToFont;
    private PhysicalFont defaultPhysicalFont;
    static boolean longAddresses;
    private boolean loaded1dot0Fonts = false;
    boolean loadedAllFonts = false;
    boolean loadedAllFontFiles = false;
    HashMap<String,String> jreFontMap;
    HashSet<String> jreLucidaFontFiles;
    String[] jreOtherFontFiles;
    boolean noOtherJREFontFiles = false; 
    public static final String lucidaFontName = "Lucida Sans Regular";
    public static String jreLibDirName;
    public static String jreFontDirName;
    private static HashSet<String> missingFontFiles = null;
    private String defaultFontName;
    private String defaultFontFileName;
    protected HashSet registeredFontFiles = new HashSet();
    private ArrayList badFonts;
    protected String fontPath;
    private FontConfiguration fontConfig;
    private boolean discoveredAllFonts = false;
    private static final FilenameFilter ttFilter = new TTFilter();
    private static final FilenameFilter t1Filter = new T1Filter();
    private Font[] allFonts;
    private String[] allFamilies; 
    private Locale lastDefaultLocale;
    public static boolean noType1Font;
    private static String[] STR_ARRAY = new String[0];
    private boolean usePlatformFontMetrics = false;
    public static SunFontManager getInstance() {
        FontManager fm = FontManagerFactory.getInstance();
        return (SunFontManager) fm;
    }
    public FilenameFilter getTrueTypeFilter() {
        return ttFilter;
    }
    public FilenameFilter getType1Filter() {
        return t1Filter;
    }
    @Override
    public boolean usingPerAppContextComposites() {
        return _usingPerAppContextComposites;
    }
    private void initJREFontMap() {
        jreFontMap = new HashMap<String,String>();
        jreLucidaFontFiles = new HashSet<String>();
        if (isOpenJDK()) {
            return;
        }
        jreFontMap.put("lucida sans0",   "LucidaSansRegular.ttf");
        jreFontMap.put("lucida sans1",   "LucidaSansDemiBold.ttf");
        jreFontMap.put("lucida sans regular0", "LucidaSansRegular.ttf");
        jreFontMap.put("lucida sans regular1", "LucidaSansDemiBold.ttf");
        jreFontMap.put("lucida sans bold1", "LucidaSansDemiBold.ttf");
        jreFontMap.put("lucida sans demibold1", "LucidaSansDemiBold.ttf");
        jreFontMap.put("lucida sans typewriter0",
                       "LucidaTypewriterRegular.ttf");
        jreFontMap.put("lucida sans typewriter1", "LucidaTypewriterBold.ttf");
        jreFontMap.put("lucida sans typewriter regular0",
                       "LucidaTypewriter.ttf");
        jreFontMap.put("lucida sans typewriter regular1",
                       "LucidaTypewriterBold.ttf");
        jreFontMap.put("lucida sans typewriter bold1",
                       "LucidaTypewriterBold.ttf");
        jreFontMap.put("lucida sans typewriter demibold1",
                       "LucidaTypewriterBold.ttf");
        jreFontMap.put("lucida bright0", "LucidaBrightRegular.ttf");
        jreFontMap.put("lucida bright1", "LucidaBrightDemiBold.ttf");
        jreFontMap.put("lucida bright2", "LucidaBrightItalic.ttf");
        jreFontMap.put("lucida bright3", "LucidaBrightDemiItalic.ttf");
        jreFontMap.put("lucida bright regular0", "LucidaBrightRegular.ttf");
        jreFontMap.put("lucida bright regular1", "LucidaBrightDemiBold.ttf");
        jreFontMap.put("lucida bright regular2", "LucidaBrightItalic.ttf");
        jreFontMap.put("lucida bright regular3", "LucidaBrightDemiItalic.ttf");
        jreFontMap.put("lucida bright bold1", "LucidaBrightDemiBold.ttf");
        jreFontMap.put("lucida bright bold3", "LucidaBrightDemiItalic.ttf");
        jreFontMap.put("lucida bright demibold1", "LucidaBrightDemiBold.ttf");
        jreFontMap.put("lucida bright demibold3","LucidaBrightDemiItalic.ttf");
        jreFontMap.put("lucida bright italic2", "LucidaBrightItalic.ttf");
        jreFontMap.put("lucida bright italic3", "LucidaBrightDemiItalic.ttf");
        jreFontMap.put("lucida bright bold italic3",
                       "LucidaBrightDemiItalic.ttf");
        jreFontMap.put("lucida bright demibold italic3",
                       "LucidaBrightDemiItalic.ttf");
        for (String ffile : jreFontMap.values()) {
            jreLucidaFontFiles.add(ffile);
        }
    }
    static {
        java.security.AccessController.doPrivileged(
                                    new java.security.PrivilegedAction() {
           public Object run() {
               FontManagerNativeLibrary.load();
               initIDs();
               switch (StrikeCache.nativeAddressSize) {
               case 8: longAddresses = true; break;
               case 4: longAddresses = false; break;
               default: throw new RuntimeException("Unexpected address size");
               }
               noType1Font =
                   "true".equals(System.getProperty("sun.java2d.noType1Font"));
               jreLibDirName =
                   System.getProperty("java.home","") + File.separator + "lib";
               jreFontDirName = jreLibDirName + File.separator + "fonts";
               File lucidaFile =
                   new File(jreFontDirName + File.separator + FontUtilities.LUCIDA_FILE_NAME);
               return null;
           }
        });
    }
    public TrueTypeFont getEUDCFont() {
        return null;
    }
    private static native void initIDs();
    @SuppressWarnings("unchecked")
    protected SunFontManager() {
        initJREFontMap();
        java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        File badFontFile =
                            new File(jreFontDirName + File.separator +
                                     "badfonts.txt");
                        if (badFontFile.exists()) {
                            FileInputStream fis = null;
                            try {
                                badFonts = new ArrayList();
                                fis = new FileInputStream(badFontFile);
                                InputStreamReader isr = new InputStreamReader(fis);
                                BufferedReader br = new BufferedReader(isr);
                                while (true) {
                                    String name = br.readLine();
                                    if (name == null) {
                                        break;
                                    } else {
                                        if (FontUtilities.debugFonts()) {
                                            FontUtilities.getLogger().warning("read bad font: " +
                                                           name);
                                        }
                                        badFonts.add(name);
                                    }
                                }
                            } catch (IOException e) {
                                try {
                                    if (fis != null) {
                                        fis.close();
                                    }
                                } catch (IOException ioe) {
                                }
                            }
                        }
                        if (FontUtilities.isLinux) {
                            registerFontDir(jreFontDirName);
                        }
                        registerFontsInDir(jreFontDirName, true, Font2D.JRE_RANK,
                                           true, false);
                        fontConfig = createFontConfiguration();
                        if (isOpenJDK()) {
                            String[] fontInfo = getDefaultPlatformFont();
                            defaultFontName = fontInfo[0];
                            defaultFontFileName = fontInfo[1];
                        }
                        String extraFontPath = fontConfig.getExtraFontPath();
                        boolean prependToPath = false;
                        boolean appendToPath = false;
                        String dbgFontPath =
                            System.getProperty("sun.java2d.fontpath");
                        if (dbgFontPath != null) {
                            if (dbgFontPath.startsWith("prepend:")) {
                                prependToPath = true;
                                dbgFontPath =
                                    dbgFontPath.substring("prepend:".length());
                            } else if (dbgFontPath.startsWith("append:")) {
                                appendToPath = true;
                                dbgFontPath =
                                    dbgFontPath.substring("append:".length());
                            }
                        }
                        if (FontUtilities.debugFonts()) {
                            PlatformLogger logger = FontUtilities.getLogger();
                            logger.info("JRE font directory: " + jreFontDirName);
                            logger.info("Extra font path: " + extraFontPath);
                            logger.info("Debug font path: " + dbgFontPath);
                        }
                        if (dbgFontPath != null) {
                            fontPath = getPlatformFontPath(noType1Font);
                            if (extraFontPath != null) {
                                fontPath =
                                    extraFontPath + File.pathSeparator + fontPath;
                            }
                            if (appendToPath) {
                                fontPath =
                                    fontPath + File.pathSeparator + dbgFontPath;
                            } else if (prependToPath) {
                                fontPath =
                                    dbgFontPath + File.pathSeparator + fontPath;
                            } else {
                                fontPath = dbgFontPath;
                            }
                            registerFontDirs(fontPath);
                        } else if (extraFontPath != null) {
                            registerFontDirs(extraFontPath);
                        }
                        if (FontUtilities.isSolaris && Locale.JAPAN.equals(Locale.getDefault())) {
                            registerFontDir("/usr/openwin/lib/locale/ja/X11/fonts/TT");
                        }
                        initCompositeFonts(fontConfig, null);
                        return null;
                    }
                });
        boolean platformFont = AccessController.doPrivileged(
                        new PrivilegedAction<Boolean>() {
                                public Boolean run() {
                                        String prop =
                                                System.getProperty("java2d.font.usePlatformFont");
                                        String env = System.getenv("JAVA2D_USEPLATFORMFONT");
                                        return "true".equals(prop) || env != null;
                                }
                        });
        if (platformFont) {
            usePlatformFontMetrics = true;
            System.out.println("Enabling platform font metrics for win32. This is an unsupported option.");
            System.out.println("This yields incorrect composite font metrics as reported by 1.1.x releases.");
            System.out.println("It is appropriate only for use by applications which do not use any Java 2");
            System.out.println("functionality. This property will be removed in a later release.");
        }
    }
    public Font2DHandle getNewComposite(String family, int style,
                                        Font2DHandle handle) {
        if (!(handle.font2D instanceof CompositeFont)) {
            return handle;
        }
        CompositeFont oldComp = (CompositeFont)handle.font2D;
        PhysicalFont oldFont = oldComp.getSlotFont(0);
        if (family == null) {
            family = oldFont.getFamilyName(null);
        }
        if (style == -1) {
            style = oldComp.getStyle();
        }
        Font2D newFont = findFont2D(family, style, NO_FALLBACK);
        if (!(newFont instanceof PhysicalFont)) {
            newFont = oldFont;
        }
        PhysicalFont physicalFont = (PhysicalFont)newFont;
        CompositeFont dialog2D =
            (CompositeFont)findFont2D("dialog", style, NO_FALLBACK);
        if (dialog2D == null) { 
            return handle;
        }
        CompositeFont compFont = new CompositeFont(physicalFont, dialog2D);
        Font2DHandle newHandle = new Font2DHandle(compFont);
        return newHandle;
    }
    protected void registerCompositeFont(String compositeName,
                                      String[] componentFileNames,
                                      String[] componentNames,
                                      int numMetricsSlots,
                                      int[] exclusionRanges,
                                      int[] exclusionMaxIndex,
                                      boolean defer) {
        CompositeFont cf = new CompositeFont(compositeName,
                                             componentFileNames,
                                             componentNames,
                                             numMetricsSlots,
                                             exclusionRanges,
                                             exclusionMaxIndex, defer, this);
        addCompositeToFontList(cf, Font2D.FONT_CONFIG_RANK);
        synchronized (compFonts) {
            compFonts[maxCompFont++] = cf;
        }
    }
    protected static void registerCompositeFont(String compositeName,
                                                String[] componentFileNames,
                                                String[] componentNames,
                                                int numMetricsSlots,
                                                int[] exclusionRanges,
                                                int[] exclusionMaxIndex,
                                                boolean defer,
                                                ConcurrentHashMap<String, Font2D>
                                                altNameCache) {
        CompositeFont cf = new CompositeFont(compositeName,
                                             componentFileNames,
                                             componentNames,
                                             numMetricsSlots,
                                             exclusionRanges,
                                             exclusionMaxIndex, defer,
                                             SunFontManager.getInstance());
        Font2D oldFont = (Font2D)
            altNameCache.get(compositeName.toLowerCase(Locale.ENGLISH));
        if (oldFont instanceof CompositeFont) {
            oldFont.handle.font2D = cf;
        }
        altNameCache.put(compositeName.toLowerCase(Locale.ENGLISH), cf);
    }
    private void addCompositeToFontList(CompositeFont f, int rank) {
        if (FontUtilities.isLogging()) {
            FontUtilities.getLogger().info("Add to Family "+ f.familyName +
                        ", Font " + f.fullName + " rank="+rank);
        }
        f.setRank(rank);
        compositeFonts.put(f.fullName, f);
        fullNameToFont.put(f.fullName.toLowerCase(Locale.ENGLISH), f);
        FontFamily family = FontFamily.getFamily(f.familyName);
        if (family == null) {
            family = new FontFamily(f.familyName, true, rank);
        }
        family.setFont(f, f.style);
    }
    private PhysicalFont addToFontList(PhysicalFont f, int rank) {
        String fontName = f.fullName;
        String familyName = f.familyName;
        if (fontName == null || "".equals(fontName)) {
            return null;
        }
        if (compositeFonts.containsKey(fontName)) {
            return null;
        }
        f.setRank(rank);
        if (!physicalFonts.containsKey(fontName)) {
            if (FontUtilities.isLogging()) {
                FontUtilities.getLogger().info("Add to Family "+familyName +
                            ", Font " + fontName + " rank="+rank);
            }
            physicalFonts.put(fontName, f);
            FontFamily family = FontFamily.getFamily(familyName);
            if (family == null) {
                family = new FontFamily(familyName, false, rank);
                family.setFont(f, f.style);
            } else if (family.getRank() >= rank) {
                family.setFont(f, f.style);
            }
            fullNameToFont.put(fontName.toLowerCase(Locale.ENGLISH), f);
            return f;
        } else {
            PhysicalFont newFont = f;
            PhysicalFont oldFont = physicalFonts.get(fontName);
            if (oldFont == null) {
                return null;
            }
            if (oldFont.getRank() >= rank) {
                if (oldFont.mapper != null && rank > Font2D.FONT_CONFIG_RANK) {
                    return oldFont;
                }
                if (oldFont.getRank() == rank) {
                    if (oldFont instanceof TrueTypeFont &&
                        newFont instanceof TrueTypeFont) {
                        TrueTypeFont oldTTFont = (TrueTypeFont)oldFont;
                        TrueTypeFont newTTFont = (TrueTypeFont)newFont;
                        if (oldTTFont.fileSize >= newTTFont.fileSize) {
                            return oldFont;
                        }
                    } else {
                        return oldFont;
                    }
                }
                if (oldFont.platName.startsWith(jreFontDirName)) {
                    if (FontUtilities.isLogging()) {
                        FontUtilities.getLogger()
                              .warning("Unexpected attempt to replace a JRE " +
                                       " font " + fontName + " from " +
                                        oldFont.platName +
                                       " with " + newFont.platName);
                    }
                    return oldFont;
                }
                if (FontUtilities.isLogging()) {
                    FontUtilities.getLogger()
                          .info("Replace in Family " + familyName +
                                ",Font " + fontName + " new rank="+rank +
                                " from " + oldFont.platName +
                                " with " + newFont.platName);
                }
                replaceFont(oldFont, newFont);
                physicalFonts.put(fontName, newFont);
                fullNameToFont.put(fontName.toLowerCase(Locale.ENGLISH),
                                   newFont);
                FontFamily family = FontFamily.getFamily(familyName);
                if (family == null) {
                    family = new FontFamily(familyName, false, rank);
                    family.setFont(newFont, newFont.style);
                } else if (family.getRank() >= rank) {
                    family.setFont(newFont, newFont.style);
                }
                return newFont;
            } else {
                return oldFont;
            }
        }
    }
    public Font2D[] getRegisteredFonts() {
        PhysicalFont[] physFonts = getPhysicalFonts();
        int mcf = maxCompFont; 
        Font2D[] regFonts = new Font2D[physFonts.length+mcf];
        System.arraycopy(compFonts, 0, regFonts, 0, mcf);
        System.arraycopy(physFonts, 0, regFonts, mcf, physFonts.length);
        return regFonts;
    }
    protected PhysicalFont[] getPhysicalFonts() {
        return physicalFonts.values().toArray(new PhysicalFont[0]);
    }
    private static final class FontRegistrationInfo {
        String fontFilePath;
        String[] nativeNames;
        int fontFormat;
        boolean javaRasterizer;
        int fontRank;
        FontRegistrationInfo(String fontPath, String[] names, int format,
                             boolean useJavaRasterizer, int rank) {
            this.fontFilePath = fontPath;
            this.nativeNames = names;
            this.fontFormat = format;
            this.javaRasterizer = useJavaRasterizer;
            this.fontRank = rank;
        }
    }
    private final ConcurrentHashMap<String, FontRegistrationInfo>
        deferredFontFiles =
        new ConcurrentHashMap<String, FontRegistrationInfo>();
    private final ConcurrentHashMap<String, Font2DHandle>
        initialisedFonts = new ConcurrentHashMap<String, Font2DHandle>();
    protected synchronized void initialiseDeferredFonts() {
        for (String fileName : deferredFontFiles.keySet()) {
            initialiseDeferredFont(fileName);
        }
    }
    protected synchronized void registerDeferredJREFonts(String jreDir) {
        for (FontRegistrationInfo info : deferredFontFiles.values()) {
            if (info.fontFilePath != null &&
                info.fontFilePath.startsWith(jreDir)) {
                initialiseDeferredFont(info.fontFilePath);
            }
        }
    }
    public boolean isDeferredFont(String fileName) {
        return deferredFontFiles.containsKey(fileName);
    }
    public
     PhysicalFont findJREDeferredFont(String name, int style) {
        PhysicalFont physicalFont;
        String nameAndStyle = name.toLowerCase(Locale.ENGLISH) + style;
        String fileName = jreFontMap.get(nameAndStyle);
        if (fileName != null) {
            fileName = jreFontDirName + File.separator + fileName;
            if (deferredFontFiles.get(fileName) != null) {
                physicalFont = initialiseDeferredFont(fileName);
                if (physicalFont != null &&
                    (physicalFont.getFontName(null).equalsIgnoreCase(name) ||
                     physicalFont.getFamilyName(null).equalsIgnoreCase(name))
                    && physicalFont.style == style) {
                    return physicalFont;
                }
            }
        }
        if (noOtherJREFontFiles) {
            return null;
        }
        synchronized (jreLucidaFontFiles) {
            if (jreOtherFontFiles == null) {
                HashSet<String> otherFontFiles = new HashSet<String>();
                for (String deferredFile : deferredFontFiles.keySet()) {
                    File file = new File(deferredFile);
                    String dir = file.getParent();
                    String fname = file.getName();
                    if (dir == null ||
                        !dir.equals(jreFontDirName) ||
                        jreLucidaFontFiles.contains(fname)) {
                        continue;
                    }
                    otherFontFiles.add(deferredFile);
                }
                jreOtherFontFiles = otherFontFiles.toArray(STR_ARRAY);
                if (jreOtherFontFiles.length == 0) {
                    noOtherJREFontFiles = true;
                }
            }
            for (int i=0; i<jreOtherFontFiles.length;i++) {
                fileName = jreOtherFontFiles[i];
                if (fileName == null) {
                    continue;
                }
                jreOtherFontFiles[i] = null;
                physicalFont = initialiseDeferredFont(fileName);
                if (physicalFont != null &&
                    (physicalFont.getFontName(null).equalsIgnoreCase(name) ||
                     physicalFont.getFamilyName(null).equalsIgnoreCase(name))
                    && physicalFont.style == style) {
                    return physicalFont;
                }
            }
        }
        return null;
    }
    private PhysicalFont findOtherDeferredFont(String name, int style) {
        for (String fileName : deferredFontFiles.keySet()) {
            File file = new File(fileName);
            String dir = file.getParent();
            String fname = file.getName();
            if (dir != null &&
                dir.equals(jreFontDirName) &&
                jreLucidaFontFiles.contains(fname)) {
                continue;
            }
            PhysicalFont physicalFont = initialiseDeferredFont(fileName);
            if (physicalFont != null &&
                (physicalFont.getFontName(null).equalsIgnoreCase(name) ||
                physicalFont.getFamilyName(null).equalsIgnoreCase(name)) &&
                physicalFont.style == style) {
                return physicalFont;
            }
        }
        return null;
    }
    private PhysicalFont findDeferredFont(String name, int style) {
        PhysicalFont physicalFont = findJREDeferredFont(name, style);
        if (physicalFont != null) {
            return physicalFont;
        } else {
            return findOtherDeferredFont(name, style);
        }
    }
    public void registerDeferredFont(String fileNameKey,
                                     String fullPathName,
                                     String[] nativeNames,
                                     int fontFormat,
                                     boolean useJavaRasterizer,
                                     int fontRank) {
        FontRegistrationInfo regInfo =
            new FontRegistrationInfo(fullPathName, nativeNames, fontFormat,
                                     useJavaRasterizer, fontRank);
        deferredFontFiles.put(fileNameKey, regInfo);
    }
    public synchronized
         PhysicalFont initialiseDeferredFont(String fileNameKey) {
        if (fileNameKey == null) {
            return null;
        }
        if (FontUtilities.isLogging()) {
            FontUtilities.getLogger()
                            .info("Opening deferred font file " + fileNameKey);
        }
        PhysicalFont physicalFont;
        FontRegistrationInfo regInfo = deferredFontFiles.get(fileNameKey);
        if (regInfo != null) {
            deferredFontFiles.remove(fileNameKey);
            physicalFont = registerFontFile(regInfo.fontFilePath,
                                            regInfo.nativeNames,
                                            regInfo.fontFormat,
                                            regInfo.javaRasterizer,
                                            regInfo.fontRank);
            if (physicalFont != null) {
                initialisedFonts.put(fileNameKey, physicalFont.handle);
            } else {
                initialisedFonts.put(fileNameKey,
                                     getDefaultPhysicalFont().handle);
            }
        } else {
            Font2DHandle handle = initialisedFonts.get(fileNameKey);
            if (handle == null) {
                physicalFont = getDefaultPhysicalFont();
            } else {
                physicalFont = (PhysicalFont)(handle.font2D);
            }
        }
        return physicalFont;
    }
    public boolean isRegisteredFontFile(String name) {
        return registeredFonts.containsKey(name);
    }
    public PhysicalFont getRegisteredFontFile(String name) {
        return registeredFonts.get(name);
    }
    public PhysicalFont registerFontFile(String fileName,
                                         String[] nativeNames,
                                         int fontFormat,
                                         boolean useJavaRasterizer,
                                         int fontRank) {
        PhysicalFont regFont = registeredFonts.get(fileName);
        if (regFont != null) {
            return regFont;
        }
        PhysicalFont physicalFont = null;
        try {
            String name;
            switch (fontFormat) {
            case FONTFORMAT_TRUETYPE:
                int fn = 0;
                TrueTypeFont ttf;
                do {
                    ttf = new TrueTypeFont(fileName, nativeNames, fn++,
                                           useJavaRasterizer);
                    PhysicalFont pf = addToFontList(ttf, fontRank);
                    if (physicalFont == null) {
                        physicalFont = pf;
                    }
                }
                while (fn < ttf.getFontCount());
                break;
            case FONTFORMAT_TYPE1:
                Type1Font t1f = new Type1Font(fileName, nativeNames);
                physicalFont = addToFontList(t1f, fontRank);
                break;
            case FONTFORMAT_NATIVE:
                NativeFont nf = new NativeFont(fileName, false);
                physicalFont = addToFontList(nf, fontRank);
            default:
            }
            if (FontUtilities.isLogging()) {
                FontUtilities.getLogger()
                      .info("Registered file " + fileName + " as font " +
                            physicalFont + " rank="  + fontRank);
            }
        } catch (FontFormatException ffe) {
            if (FontUtilities.isLogging()) {
                FontUtilities.getLogger().warning("Unusable font: " +
                               fileName + " " + ffe.toString());
            }
        }
        if (physicalFont != null &&
            fontFormat != FONTFORMAT_NATIVE) {
            registeredFonts.put(fileName, physicalFont);
        }
        return physicalFont;
    }
    public void registerFonts(String[] fileNames,
                              String[][] nativeNames,
                              int fontCount,
                              int fontFormat,
                              boolean useJavaRasterizer,
                              int fontRank, boolean defer) {
        for (int i=0; i < fontCount; i++) {
            if (defer) {
                registerDeferredFont(fileNames[i],fileNames[i], nativeNames[i],
                                     fontFormat, useJavaRasterizer, fontRank);
            } else {
                registerFontFile(fileNames[i], nativeNames[i],
                                 fontFormat, useJavaRasterizer, fontRank);
            }
        }
    }
    public PhysicalFont getDefaultPhysicalFont() {
        if (defaultPhysicalFont == null) {
            defaultPhysicalFont = (PhysicalFont)
                findFont2D("Lucida Sans Regular", Font.PLAIN, NO_FALLBACK);
            if (defaultPhysicalFont == null) {
                defaultPhysicalFont = (PhysicalFont)
                    findFont2D("Arial", Font.PLAIN, NO_FALLBACK);
            }
            if (defaultPhysicalFont == null) {
                Iterator i = physicalFonts.values().iterator();
                if (i.hasNext()) {
                    defaultPhysicalFont = (PhysicalFont)i.next();
                } else {
                    throw new Error("Probable fatal error:No fonts found.");
                }
            }
        }
        return defaultPhysicalFont;
    }
    public CompositeFont getDefaultLogicalFont(int style) {
        return (CompositeFont)findFont2D("dialog", style, NO_FALLBACK);
    }
    private static String dotStyleStr(int num) {
        switch(num){
          case Font.BOLD:
            return ".bold";
          case Font.ITALIC:
            return ".italic";
          case Font.ITALIC | Font.BOLD:
            return ".bolditalic";
          default:
            return ".plain";
        }
    }
    protected void
        populateFontFileNameMap(HashMap<String,String> fontToFileMap,
                                HashMap<String,String> fontToFamilyNameMap,
                                HashMap<String,ArrayList<String>>
                                familyToFontListMap,
                                Locale locale) {
    }
    private HashMap<String,String> fontToFileMap = null;
    private HashMap<String,String> fontToFamilyNameMap = null;
    private HashMap<String,ArrayList<String>> familyToFontListMap= null;
    private String[] pathDirs = null;
    private boolean haveCheckedUnreferencedFontFiles;
    private String[] getFontFilesFromPath(boolean noType1) {
        final FilenameFilter filter;
        if (noType1) {
            filter = ttFilter;
        } else {
            filter = new TTorT1Filter();
        }
        return (String[])AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                if (pathDirs.length == 1) {
                    File dir = new File(pathDirs[0]);
                    String[] files = dir.list(filter);
                    if (files == null) {
                        return new String[0];
                    }
                    for (int f=0; f<files.length; f++) {
                        files[f] = files[f].toLowerCase();
                    }
                    return files;
                } else {
                    ArrayList<String> fileList = new ArrayList<String>();
                    for (int i = 0; i< pathDirs.length; i++) {
                        File dir = new File(pathDirs[i]);
                        String[] files = dir.list(filter);
                        if (files == null) {
                            continue;
                        }
                        for (int f=0; f<files.length ; f++) {
                            fileList.add(files[f].toLowerCase());
                        }
                    }
                    return fileList.toArray(STR_ARRAY);
                }
            }
        });
    }
    private void resolveWindowsFonts() {
        ArrayList<String> unmappedFontNames = null;
        for (String font : fontToFamilyNameMap.keySet()) {
            String file = fontToFileMap.get(font);
            if (file == null) {
                if (font.indexOf("  ") > 0) {
                    String newName = font.replaceFirst("  ", " ");
                    file = fontToFileMap.get(newName);
                    if (file != null &&
                        !fontToFamilyNameMap.containsKey(newName)) {
                        fontToFileMap.remove(newName);
                        fontToFileMap.put(font, file);
                    }
                } else if (font.equals("marlett")) {
                    fontToFileMap.put(font, "marlett.ttf");
                } else if (font.equals("david")) {
                    file = fontToFileMap.get("david regular");
                    if (file != null) {
                        fontToFileMap.remove("david regular");
                        fontToFileMap.put("david", file);
                    }
                } else {
                    if (unmappedFontNames == null) {
                        unmappedFontNames = new ArrayList<String>();
                    }
                    unmappedFontNames.add(font);
                }
            }
        }
        if (unmappedFontNames != null) {
            HashSet<String> unmappedFontFiles = new HashSet<String>();
            HashMap<String,String> ffmapCopy =
                (HashMap<String,String>)(fontToFileMap.clone());
            for (String key : fontToFamilyNameMap.keySet()) {
                ffmapCopy.remove(key);
            }
            for (String key : ffmapCopy.keySet()) {
                unmappedFontFiles.add(ffmapCopy.get(key));
                fontToFileMap.remove(key);
            }
            resolveFontFiles(unmappedFontFiles, unmappedFontNames);
            if (unmappedFontNames.size() > 0) {
                ArrayList<String> registryFiles = new ArrayList<String>();
                for (String regFile : fontToFileMap.values()) {
                    registryFiles.add(regFile.toLowerCase());
                }
                for (String pathFile : getFontFilesFromPath(true)) {
                    if (!registryFiles.contains(pathFile)) {
                        unmappedFontFiles.add(pathFile);
                    }
                }
                resolveFontFiles(unmappedFontFiles, unmappedFontNames);
            }
            if (unmappedFontNames.size() > 0) {
                int sz = unmappedFontNames.size();
                for (int i=0; i<sz; i++) {
                    String name = unmappedFontNames.get(i);
                    String familyName = fontToFamilyNameMap.get(name);
                    if (familyName != null) {
                        ArrayList family = familyToFontListMap.get(familyName);
                        if (family != null) {
                            if (family.size() <= 1) {
                                familyToFontListMap.remove(familyName);
                            }
                        }
                    }
                    fontToFamilyNameMap.remove(name);
                    if (FontUtilities.isLogging()) {
                        FontUtilities.getLogger()
                                             .info("No file for font:" + name);
                    }
                }
            }
        }
    }
    private synchronized void checkForUnreferencedFontFiles() {
        if (haveCheckedUnreferencedFontFiles) {
            return;
        }
        haveCheckedUnreferencedFontFiles = true;
        if (!FontUtilities.isWindows) {
            return;
        }
        ArrayList<String> registryFiles = new ArrayList<String>();
        for (String regFile : fontToFileMap.values()) {
            registryFiles.add(regFile.toLowerCase());
        }
        HashMap<String,String> fontToFileMap2 = null;
        HashMap<String,String> fontToFamilyNameMap2 = null;
        HashMap<String,ArrayList<String>> familyToFontListMap2 = null;;
        for (String pathFile : getFontFilesFromPath(false)) {
            if (!registryFiles.contains(pathFile)) {
                if (FontUtilities.isLogging()) {
                    FontUtilities.getLogger()
                                 .info("Found non-registry file : " + pathFile);
                }
                PhysicalFont f = registerFontFile(getPathName(pathFile));
                if (f == null) {
                    continue;
                }
                if (fontToFileMap2 == null) {
                    fontToFileMap2 = new HashMap<String,String>(fontToFileMap);
                    fontToFamilyNameMap2 =
                        new HashMap<String,String>(fontToFamilyNameMap);
                    familyToFontListMap2 = new
                        HashMap<String,ArrayList<String>>(familyToFontListMap);
                }
                String fontName = f.getFontName(null);
                String family = f.getFamilyName(null);
                String familyLC = family.toLowerCase();
                fontToFamilyNameMap2.put(fontName, family);
                fontToFileMap2.put(fontName, pathFile);
                ArrayList<String> fonts = familyToFontListMap2.get(familyLC);
                if (fonts == null) {
                    fonts = new ArrayList<String>();
                } else {
                    fonts = new ArrayList<String>(fonts);
                }
                fonts.add(fontName);
                familyToFontListMap2.put(familyLC, fonts);
            }
        }
        if (fontToFileMap2 != null) {
            fontToFileMap = fontToFileMap2;
            familyToFontListMap = familyToFontListMap2;
            fontToFamilyNameMap = fontToFamilyNameMap2;
        }
    }
    private void resolveFontFiles(HashSet<String> unmappedFiles,
                                  ArrayList<String> unmappedFonts) {
        Locale l = SunToolkit.getStartupLocale();
        for (String file : unmappedFiles) {
            try {
                int fn = 0;
                TrueTypeFont ttf;
                String fullPath = getPathName(file);
                if (FontUtilities.isLogging()) {
                    FontUtilities.getLogger()
                                   .info("Trying to resolve file " + fullPath);
                }
                do {
                    ttf = new TrueTypeFont(fullPath, null, fn++, false);
                    String fontName = ttf.getFontName(l).toLowerCase();
                    if (unmappedFonts.contains(fontName)) {
                        fontToFileMap.put(fontName, file);
                        unmappedFonts.remove(fontName);
                        if (FontUtilities.isLogging()) {
                            FontUtilities.getLogger()
                                  .info("Resolved absent registry entry for " +
                                        fontName + " located in " + fullPath);
                        }
                    }
                }
                while (fn < ttf.getFontCount());
            } catch (Exception e) {
            }
        }
    }
    public static class FamilyDescription {
        public String familyName;
        public String plainFullName;
        public String boldFullName;
        public String italicFullName;
        public String boldItalicFullName;
        public String plainFileName;
        public String boldFileName;
        public String italicFileName;
        public String boldItalicFileName;
    }
    static HashMap<String, FamilyDescription> platformFontMap;
    public HashMap<String, FamilyDescription> populateHardcodedFileNameMap() {
        return new HashMap<String, FamilyDescription>(0);
    }
    Font2D findFontFromPlatformMap(String lcName, int style) {
        if (platformFontMap == null) {
            platformFontMap = populateHardcodedFileNameMap();
        }
        if (platformFontMap == null || platformFontMap.size() == 0) {
            return null;
        }
        int spaceIndex = lcName.indexOf(' ');
        String firstWord = lcName;
        if (spaceIndex > 0) {
            firstWord = lcName.substring(0, spaceIndex);
        }
        FamilyDescription fd = platformFontMap.get(firstWord);
        if (fd == null) {
            return null;
        }
        int styleIndex = -1;
        if (lcName.equalsIgnoreCase(fd.plainFullName)) {
            styleIndex = 0;
        } else if (lcName.equalsIgnoreCase(fd.boldFullName)) {
            styleIndex = 1;
        } else if (lcName.equalsIgnoreCase(fd.italicFullName)) {
            styleIndex = 2;
        } else if (lcName.equalsIgnoreCase(fd.boldItalicFullName)) {
            styleIndex = 3;
        }
        if (styleIndex == -1 && !lcName.equalsIgnoreCase(fd.familyName)) {
            return null;
        }
        String plainFile = null, boldFile = null,
            italicFile = null, boldItalicFile = null;
        boolean failure = false;
         getPlatformFontDirs(noType1Font);
        if (fd.plainFileName != null) {
            plainFile = getPathName(fd.plainFileName);
            if (plainFile == null) {
                failure = true;
            }
        }
        if (fd.boldFileName != null) {
            boldFile = getPathName(fd.boldFileName);
            if (boldFile == null) {
                failure = true;
            }
        }
        if (fd.italicFileName != null) {
            italicFile = getPathName(fd.italicFileName);
            if (italicFile == null) {
                failure = true;
            }
        }
        if (fd.boldItalicFileName != null) {
            boldItalicFile = getPathName(fd.boldItalicFileName);
            if (boldItalicFile == null) {
                failure = true;
            }
        }
        if (failure) {
            if (FontUtilities.isLogging()) {
                FontUtilities.getLogger().
                    info("Hardcoded file missing looking for " + lcName);
            }
            platformFontMap.remove(firstWord);
            return null;
        }
        final String[] files = {
            plainFile, boldFile, italicFile, boldItalicFile } ;
        failure = java.security.AccessController.doPrivileged(
                 new java.security.PrivilegedAction<Boolean>() {
                     public Boolean run() {
                         for (int i=0; i<files.length; i++) {
                             if (files[i] == null) {
                                 continue;
                             }
                             File f = new File(files[i]);
                             if (!f.exists()) {
                                 return Boolean.TRUE;
                             }
                         }
                         return Boolean.FALSE;
                     }
                 });
        if (failure) {
            if (FontUtilities.isLogging()) {
                FontUtilities.getLogger().
                    info("Hardcoded file missing looking for " + lcName);
            }
            platformFontMap.remove(firstWord);
            return null;
        }
        Font2D font = null;
        for (int f=0;f<files.length;f++) {
            if (files[f] == null) {
                continue;
            }
            PhysicalFont pf =
                registerFontFile(files[f], null,
                                 FONTFORMAT_TRUETYPE, false, Font2D.TTF_RANK);
            if (f == styleIndex) {
                font = pf;
            }
        }
        FontFamily fontFamily = FontFamily.getFamily(fd.familyName);
        if (fontFamily != null) {
            if (font == null) {
                font = fontFamily.getFont(style);
                if (font == null) {
                    font = fontFamily.getClosestStyle(style);
                }
            } else if (style > 0 && style != font.style) {
                style |= font.style;
                font = fontFamily.getFont(style);
                if (font == null) {
                    font = fontFamily.getClosestStyle(style);
                }
            }
        }
        return font;
    }
    private synchronized HashMap<String,String> getFullNameToFileMap() {
        if (fontToFileMap == null) {
            pathDirs = getPlatformFontDirs(noType1Font);
            fontToFileMap = new HashMap<String,String>(100);
            fontToFamilyNameMap = new HashMap<String,String>(100);
            familyToFontListMap = new HashMap<String,ArrayList<String>>(50);
            populateFontFileNameMap(fontToFileMap,
                                    fontToFamilyNameMap,
                                    familyToFontListMap,
                                    Locale.ENGLISH);
            if (FontUtilities.isWindows) {
                resolveWindowsFonts();
            }
            if (FontUtilities.isLogging()) {
                logPlatformFontInfo();
            }
        }
        return fontToFileMap;
    }
    private void logPlatformFontInfo() {
        PlatformLogger logger = FontUtilities.getLogger();
        for (int i=0; i< pathDirs.length;i++) {
            logger.info("fontdir="+pathDirs[i]);
        }
        for (String keyName : fontToFileMap.keySet()) {
            logger.info("font="+keyName+" file="+ fontToFileMap.get(keyName));
        }
        for (String keyName : fontToFamilyNameMap.keySet()) {
            logger.info("font="+keyName+" family="+
                        fontToFamilyNameMap.get(keyName));
        }
        for (String keyName : familyToFontListMap.keySet()) {
            logger.info("family="+keyName+ " fonts="+
                        familyToFontListMap.get(keyName));
        }
    }
    protected String[] getFontNamesFromPlatform() {
        if (getFullNameToFileMap().size() == 0) {
            return null;
        }
        checkForUnreferencedFontFiles();
        ArrayList<String> fontNames = new ArrayList<String>();
        for (ArrayList<String> a : familyToFontListMap.values()) {
            for (String s : a) {
                fontNames.add(s);
            }
        }
        return fontNames.toArray(STR_ARRAY);
    }
    public boolean gotFontsFromPlatform() {
        return getFullNameToFileMap().size() != 0;
    }
    public String getFileNameForFontName(String fontName) {
        String fontNameLC = fontName.toLowerCase(Locale.ENGLISH);
        return fontToFileMap.get(fontNameLC);
    }
    private PhysicalFont registerFontFile(String file) {
        if (new File(file).isAbsolute() &&
            !registeredFonts.contains(file)) {
            int fontFormat = FONTFORMAT_NONE;
            int fontRank = Font2D.UNKNOWN_RANK;
            if (ttFilter.accept(null, file)) {
                fontFormat = FONTFORMAT_TRUETYPE;
                fontRank = Font2D.TTF_RANK;
            } else if
                (t1Filter.accept(null, file)) {
                fontFormat = FONTFORMAT_TYPE1;
                fontRank = Font2D.TYPE1_RANK;
            }
            if (fontFormat == FONTFORMAT_NONE) {
                return null;
            }
            return registerFontFile(file, null, fontFormat, false, fontRank);
        }
        return null;
    }
    protected void registerOtherFontFiles(HashSet registeredFontFiles) {
        if (getFullNameToFileMap().size() == 0) {
            return;
        }
        for (String file : fontToFileMap.values()) {
            registerFontFile(file);
        }
    }
    public boolean
        getFamilyNamesFromPlatform(TreeMap<String,String> familyNames,
                                   Locale requestedLocale) {
        if (getFullNameToFileMap().size() == 0) {
            return false;
        }
        checkForUnreferencedFontFiles();
        for (String name : fontToFamilyNameMap.values()) {
            familyNames.put(name.toLowerCase(requestedLocale), name);
        }
        return true;
    }
    private String getPathName(final String s) {
        File f = new File(s);
        if (f.isAbsolute()) {
            return s;
        } else if (pathDirs.length==1) {
            return pathDirs[0] + File.separator + s;
        } else {
            String path = java.security.AccessController.doPrivileged(
                 new java.security.PrivilegedAction<String>() {
                     public String run() {
                         for (int p=0; p<pathDirs.length; p++) {
                             File f = new File(pathDirs[p] +File.separator+ s);
                             if (f.exists()) {
                                 return f.getAbsolutePath();
                             }
                         }
                         return null;
                     }
                });
            if (path != null) {
                return path;
            }
        }
        return s; 
    }
    private Font2D findFontFromPlatform(String lcName, int style) {
        if (getFullNameToFileMap().size() == 0) {
            return null;
        }
        ArrayList<String> family = null;
        String fontFile = null;
        String familyName = fontToFamilyNameMap.get(lcName);
        if (familyName != null) {
            fontFile = fontToFileMap.get(lcName);
            family = familyToFontListMap.get
                (familyName.toLowerCase(Locale.ENGLISH));
        } else {
            family = familyToFontListMap.get(lcName); 
            if (family != null && family.size() > 0) {
                String lcFontName = family.get(0).toLowerCase(Locale.ENGLISH);
                if (lcFontName != null) {
                    familyName = fontToFamilyNameMap.get(lcFontName);
                }
            }
        }
        if (family == null || familyName == null) {
            return null;
        }
        String [] fontList = (String[])family.toArray(STR_ARRAY);
        if (fontList.length == 0) {
            return null;
        }
        for (int f=0;f<fontList.length;f++) {
            String fontNameLC = fontList[f].toLowerCase(Locale.ENGLISH);
            String fileName = fontToFileMap.get(fontNameLC);
            if (fileName == null) {
                if (FontUtilities.isLogging()) {
                    FontUtilities.getLogger()
                          .info("Platform lookup : No file for font " +
                                fontList[f] + " in family " +familyName);
                }
                return null;
            }
        }
        PhysicalFont physicalFont = null;
        if (fontFile != null) {
            physicalFont = registerFontFile(getPathName(fontFile), null,
                                            FONTFORMAT_TRUETYPE, false,
                                            Font2D.TTF_RANK);
        }
        for (int f=0;f<fontList.length;f++) {
            String fontNameLC = fontList[f].toLowerCase(Locale.ENGLISH);
            String fileName = fontToFileMap.get(fontNameLC);
            if (fontFile != null && fontFile.equals(fileName)) {
                continue;
            }
            registerFontFile(getPathName(fileName), null,
                             FONTFORMAT_TRUETYPE, false, Font2D.TTF_RANK);
        }
        Font2D font = null;
        FontFamily fontFamily = FontFamily.getFamily(familyName);
        if (physicalFont != null) {
            style |= physicalFont.style;
        }
        if (fontFamily != null) {
            font = fontFamily.getFont(style);
            if (font == null) {
                font = fontFamily.getClosestStyle(style);
            }
        }
        return font;
    }
    private ConcurrentHashMap<String, Font2D> fontNameCache =
        new ConcurrentHashMap<String, Font2D>();
    public Font2D findFont2D(String name, int style, int fallback) {
        String lowerCaseName = name.toLowerCase(Locale.ENGLISH);
        String mapName = lowerCaseName + dotStyleStr(style);
        Font2D font;
        if (_usingPerAppContextComposites) {
            ConcurrentHashMap<String, Font2D> altNameCache =
                (ConcurrentHashMap<String, Font2D>)
                AppContext.getAppContext().get(CompositeFont.class);
            if (altNameCache != null) {
                font = (Font2D)altNameCache.get(mapName);
            } else {
                font = null;
            }
        } else {
            font = fontNameCache.get(mapName);
        }
        if (font != null) {
            return font;
        }
        if (FontUtilities.isLogging()) {
            FontUtilities.getLogger().info("Search for font: " + name);
        }
        if (FontUtilities.isWindows) {
            if (lowerCaseName.equals("ms sans serif")) {
                name = "sansserif";
            } else if (lowerCaseName.equals("ms serif")) {
                name = "serif";
            }
        }
        if (lowerCaseName.equals("default")) {
            name = "dialog";
        }
        FontFamily family = FontFamily.getFamily(name);
        if (family != null) {
            font = family.getFontWithExactStyleMatch(style);
            if (font == null) {
                font = findDeferredFont(name, style);
            }
            if (font == null) {
                font = family.getFont(style);
            }
            if (font == null) {
                font = family.getClosestStyle(style);
            }
            if (font != null) {
                fontNameCache.put(mapName, font);
                return font;
            }
        }
        font = fullNameToFont.get(lowerCaseName);
        if (font != null) {
            if (font.style == style || style == Font.PLAIN) {
                fontNameCache.put(mapName, font);
                return font;
            } else {
                family = FontFamily.getFamily(font.getFamilyName(null));
                if (family != null) {
                    Font2D familyFont = family.getFont(style|font.style);
                    if (familyFont != null) {
                        fontNameCache.put(mapName, familyFont);
                        return familyFont;
                    } else {
                        familyFont = family.getClosestStyle(style|font.style);
                        if (familyFont != null) {
                            if (familyFont.canDoStyle(style|font.style)) {
                                fontNameCache.put(mapName, familyFont);
                                return familyFont;
                            }
                        }
                    }
                }
            }
        }
        if (FontUtilities.isWindows) {
            font = findFontFromPlatformMap(lowerCaseName, style);
            if (FontUtilities.isLogging()) {
                FontUtilities.getLogger()
                    .info("findFontFromPlatformMap returned " + font);
            }
            if (font != null) {
                fontNameCache.put(mapName, font);
                return font;
            }
            if (deferredFontFiles.size() > 0) {
                font = findJREDeferredFont(lowerCaseName, style);
                if (font != null) {
                    fontNameCache.put(mapName, font);
                    return font;
                }
            }
            font = findFontFromPlatform(lowerCaseName, style);
            if (font != null) {
                if (FontUtilities.isLogging()) {
                    FontUtilities.getLogger()
                          .info("Found font via platform API for request:\"" +
                                name + "\":, style="+style+
                                " found font: " + font);
                }
                fontNameCache.put(mapName, font);
                return font;
            }
        }
        if (deferredFontFiles.size() > 0) {
            font = findDeferredFont(name, style);
            if (font != null) {
                fontNameCache.put(mapName, font);
                return font;
            }
        }
        if (FontUtilities.isSolaris &&!loaded1dot0Fonts) {
            if (lowerCaseName.equals("timesroman")) {
                font = findFont2D("serif", style, fallback);
                fontNameCache.put(mapName, font);
            }
            register1dot0Fonts();
            loaded1dot0Fonts = true;
            Font2D ff = findFont2D(name, style, fallback);
            return ff;
        }
        if (fontsAreRegistered || fontsAreRegisteredPerAppContext) {
            Hashtable<String, FontFamily> familyTable = null;
            Hashtable<String, Font2D> nameTable;
            if (fontsAreRegistered) {
                familyTable = createdByFamilyName;
                nameTable = createdByFullName;
            } else {
                AppContext appContext = AppContext.getAppContext();
                familyTable =
                    (Hashtable<String,FontFamily>)appContext.get(regFamilyKey);
                nameTable =
                    (Hashtable<String,Font2D>)appContext.get(regFullNameKey);
            }
            family = familyTable.get(lowerCaseName);
            if (family != null) {
                font = family.getFontWithExactStyleMatch(style);
                if (font == null) {
                    font = family.getFont(style);
                }
                if (font == null) {
                    font = family.getClosestStyle(style);
                }
                if (font != null) {
                    if (fontsAreRegistered) {
                        fontNameCache.put(mapName, font);
                    }
                    return font;
                }
            }
            font = nameTable.get(lowerCaseName);
            if (font != null) {
                if (fontsAreRegistered) {
                    fontNameCache.put(mapName, font);
                }
                return font;
            }
        }
        if (!loadedAllFonts) {
            if (FontUtilities.isLogging()) {
                FontUtilities.getLogger()
                                       .info("Load fonts looking for:" + name);
            }
            loadFonts();
            loadedAllFonts = true;
            return findFont2D(name, style, fallback);
        }
        if (!loadedAllFontFiles) {
            if (FontUtilities.isLogging()) {
                FontUtilities.getLogger()
                                  .info("Load font files looking for:" + name);
            }
            loadFontFiles();
            loadedAllFontFiles = true;
            return findFont2D(name, style, fallback);
        }
        if ((font = findFont2DAllLocales(name, style)) != null) {
            fontNameCache.put(mapName, font);
            return font;
        }
        if (FontUtilities.isWindows) {
            String compatName =
                getFontConfiguration().getFallbackFamilyName(name, null);
            if (compatName != null) {
                font = findFont2D(compatName, style, fallback);
                fontNameCache.put(mapName, font);
                return font;
            }
        } else if (lowerCaseName.equals("timesroman")) {
            font = findFont2D("serif", style, fallback);
            fontNameCache.put(mapName, font);
            return font;
        } else if (lowerCaseName.equals("helvetica")) {
            font = findFont2D("sansserif", style, fallback);
            fontNameCache.put(mapName, font);
            return font;
        } else if (lowerCaseName.equals("courier")) {
            font = findFont2D("monospaced", style, fallback);
            fontNameCache.put(mapName, font);
            return font;
        }
        if (FontUtilities.isLogging()) {
            FontUtilities.getLogger().info("No font found for:" + name);
        }
        switch (fallback) {
        case PHYSICAL_FALLBACK: return getDefaultPhysicalFont();
        case LOGICAL_FALLBACK: return getDefaultLogicalFont(style);
        default: return null;
        }
    }
    public boolean usePlatformFontMetrics() {
        return usePlatformFontMetrics;
    }
    public int getNumFonts() {
        return physicalFonts.size()+maxCompFont;
    }
    private static boolean fontSupportsEncoding(Font font, String encoding) {
        return FontUtilities.getFont2D(font).supportsEncoding(encoding);
    }
    protected abstract String getFontPath(boolean noType1Fonts);
    private Thread fileCloser = null;
    Vector<File> tmpFontFiles = null;
    public Font2D createFont2D(File fontFile, int fontFormat,
                               boolean isCopy, CreatedFontTracker tracker)
    throws FontFormatException {
        String fontFilePath = fontFile.getPath();
        FileFont font2D = null;
        final File fFile = fontFile;
        final CreatedFontTracker _tracker = tracker;
        try {
            switch (fontFormat) {
            case Font.TRUETYPE_FONT:
                font2D = new TrueTypeFont(fontFilePath, null, 0, true);
                break;
            case Font.TYPE1_FONT:
                font2D = new Type1Font(fontFilePath, null, isCopy);
                break;
            default:
                throw new FontFormatException("Unrecognised Font Format");
            }
        } catch (FontFormatException e) {
            if (isCopy) {
                java.security.AccessController.doPrivileged(
                     new java.security.PrivilegedAction() {
                          public Object run() {
                              if (_tracker != null) {
                                  _tracker.subBytes((int)fFile.length());
                              }
                              fFile.delete();
                              return null;
                          }
                });
            }
            throw(e);
        }
        if (isCopy) {
            font2D.setFileToRemove(fontFile, tracker);
            synchronized (FontManager.class) {
                if (tmpFontFiles == null) {
                    tmpFontFiles = new Vector<File>();
                }
                tmpFontFiles.add(fontFile);
                if (fileCloser == null) {
                    final Runnable fileCloserRunnable = new Runnable() {
                      public void run() {
                         java.security.AccessController.doPrivileged(
                         new java.security.PrivilegedAction() {
                         public Object run() {
                            for (int i=0;i<CHANNELPOOLSIZE;i++) {
                                if (fontFileCache[i] != null) {
                                    try {
                                        fontFileCache[i].close();
                                    } catch (Exception e) {
                                    }
                                }
                            }
                            if (tmpFontFiles != null) {
                                File[] files = new File[tmpFontFiles.size()];
                                files = tmpFontFiles.toArray(files);
                                for (int f=0; f<files.length;f++) {
                                    try {
                                        files[f].delete();
                                    } catch (Exception e) {
                                    }
                                }
                            }
                            return null;
                          }
                          });
                      }
                    };
                    java.security.AccessController.doPrivileged(
                       new java.security.PrivilegedAction() {
                          public Object run() {
                              ThreadGroup tg =
                                  Thread.currentThread().getThreadGroup();
                              for (ThreadGroup tgn = tg;
                                   tgn != null;
                                   tg = tgn, tgn = tg.getParent());
                              fileCloser = new Thread(tg, fileCloserRunnable);
                              fileCloser.setContextClassLoader(null);
                              Runtime.getRuntime().addShutdownHook(fileCloser);
                              return null;
                          }
                    });
                }
            }
        }
        return font2D;
    }
    public synchronized String getFullNameByFileName(String fileName) {
        PhysicalFont[] physFonts = getPhysicalFonts();
        for (int i=0;i<physFonts.length;i++) {
            if (physFonts[i].platName.equals(fileName)) {
                return (physFonts[i].getFontName(null));
            }
        }
        return null;
    }
    public synchronized void deRegisterBadFont(Font2D font2D) {
        if (!(font2D instanceof PhysicalFont)) {
            return;
        } else {
            if (FontUtilities.isLogging()) {
                FontUtilities.getLogger()
                                     .severe("Deregister bad font: " + font2D);
            }
            replaceFont((PhysicalFont)font2D, getDefaultPhysicalFont());
        }
    }
    public synchronized void replaceFont(PhysicalFont oldFont,
                                         PhysicalFont newFont) {
        if (oldFont.handle.font2D != oldFont) {
            return;
        }
        if (oldFont == newFont) {
            if (FontUtilities.isLogging()) {
                FontUtilities.getLogger()
                      .severe("Can't replace bad font with itself " + oldFont);
            }
            PhysicalFont[] physFonts = getPhysicalFonts();
            for (int i=0; i<physFonts.length;i++) {
                if (physFonts[i] != newFont) {
                    newFont = physFonts[i];
                    break;
                }
            }
            if (oldFont == newFont) {
                if (FontUtilities.isLogging()) {
                    FontUtilities.getLogger()
                           .severe("This is bad. No good physicalFonts found.");
                }
                return;
            }
        }
        oldFont.handle.font2D = newFont;
        physicalFonts.remove(oldFont.fullName);
        fullNameToFont.remove(oldFont.fullName.toLowerCase(Locale.ENGLISH));
        FontFamily.remove(oldFont);
        if (localeFullNamesToFont != null) {
            Map.Entry[] mapEntries =
                (Map.Entry[])localeFullNamesToFont.entrySet().
                toArray(new Map.Entry[0]);
            for (int i=0; i<mapEntries.length;i++) {
                if (mapEntries[i].getValue() == oldFont) {
                    try {
                        mapEntries[i].setValue(newFont);
                    } catch (Exception e) {
                        localeFullNamesToFont.remove(mapEntries[i].getKey());
                    }
                }
            }
        }
        for (int i=0; i<maxCompFont; i++) {
            if (newFont.getRank() > Font2D.FONT_CONFIG_RANK) {
                compFonts[i].replaceComponentFont(oldFont, newFont);
            }
        }
    }
    private synchronized void loadLocaleNames() {
        if (localeFullNamesToFont != null) {
            return;
        }
        localeFullNamesToFont = new HashMap<String, TrueTypeFont>();
        Font2D[] fonts = getRegisteredFonts();
        for (int i=0; i<fonts.length; i++) {
            if (fonts[i] instanceof TrueTypeFont) {
                TrueTypeFont ttf = (TrueTypeFont)fonts[i];
                String[] fullNames = ttf.getAllFullNames();
                for (int n=0; n<fullNames.length; n++) {
                    localeFullNamesToFont.put(fullNames[n], ttf);
                }
                FontFamily family = FontFamily.getFamily(ttf.familyName);
                if (family != null) {
                    FontFamily.addLocaleNames(family, ttf.getAllFamilyNames());
                }
            }
        }
    }
    private Font2D findFont2DAllLocales(String name, int style) {
        if (FontUtilities.isLogging()) {
            FontUtilities.getLogger()
                           .info("Searching localised font names for:" + name);
        }
        if (localeFullNamesToFont == null) {
            loadLocaleNames();
        }
        String lowerCaseName = name.toLowerCase();
        Font2D font = null;
        FontFamily family = FontFamily.getLocaleFamily(lowerCaseName);
        if (family != null) {
          font = family.getFont(style);
          if (font == null) {
            font = family.getClosestStyle(style);
          }
          if (font != null) {
              return font;
          }
        }
        synchronized (this) {
            font = localeFullNamesToFont.get(name);
        }
        if (font != null) {
            if (font.style == style || style == Font.PLAIN) {
                return font;
            } else {
                family = FontFamily.getFamily(font.getFamilyName(null));
                if (family != null) {
                    Font2D familyFont = family.getFont(style);
                    if (familyFont != null) {
                        return familyFont;
                    } else {
                        familyFont = family.getClosestStyle(style);
                        if (familyFont != null) {
                            if (!familyFont.canDoStyle(style)) {
                                familyFont = null;
                            }
                            return familyFont;
                        }
                    }
                }
            }
        }
        return font;
    }
    private static final Object altJAFontKey       = new Object();
    private static final Object localeFontKey       = new Object();
    private static final Object proportionalFontKey = new Object();
    private boolean _usingPerAppContextComposites = false;
    private boolean _usingAlternateComposites = false;
    private static boolean gAltJAFont = false;
    private boolean gLocalePref = false;
    private boolean gPropPref = false;
    public boolean maybeUsingAlternateCompositeFonts() {
       return _usingAlternateComposites || _usingPerAppContextComposites;
    }
    public boolean usingAlternateCompositeFonts() {
        return (_usingAlternateComposites ||
                (_usingPerAppContextComposites &&
                AppContext.getAppContext().get(CompositeFont.class) != null));
    }
    private static boolean maybeMultiAppContext() {
        Boolean appletSM = (Boolean)
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                        public Object run() {
                            SecurityManager sm = System.getSecurityManager();
                            return new Boolean
                                (sm instanceof sun.applet.AppletSecurity);
                        }
                    });
        return appletSM.booleanValue();
    }
    public synchronized void useAlternateFontforJALocales() {
        if (FontUtilities.isLogging()) {
            FontUtilities.getLogger()
                .info("Entered useAlternateFontforJALocales().");
        }
        if (!FontUtilities.isWindows) {
            return;
        }
        if (!maybeMultiAppContext()) {
            gAltJAFont = true;
        } else {
            AppContext appContext = AppContext.getAppContext();
            appContext.put(altJAFontKey, altJAFontKey);
        }
    }
    public boolean usingAlternateFontforJALocales() {
        if (!maybeMultiAppContext()) {
            return gAltJAFont;
        } else {
            AppContext appContext = AppContext.getAppContext();
            return appContext.get(altJAFontKey) == altJAFontKey;
        }
    }
    public synchronized void preferLocaleFonts() {
        if (FontUtilities.isLogging()) {
            FontUtilities.getLogger().info("Entered preferLocaleFonts().");
        }
        if (!FontConfiguration.willReorderForStartupLocale()) {
            return;
        }
        if (!maybeMultiAppContext()) {
            if (gLocalePref == true) {
                return;
            }
            gLocalePref = true;
            createCompositeFonts(fontNameCache, gLocalePref, gPropPref);
            _usingAlternateComposites = true;
        } else {
            AppContext appContext = AppContext.getAppContext();
            if (appContext.get(localeFontKey) == localeFontKey) {
                return;
            }
            appContext.put(localeFontKey, localeFontKey);
            boolean acPropPref =
                appContext.get(proportionalFontKey) == proportionalFontKey;
            ConcurrentHashMap<String, Font2D>
                altNameCache = new ConcurrentHashMap<String, Font2D> ();
            appContext.put(CompositeFont.class, altNameCache);
            _usingPerAppContextComposites = true;
            createCompositeFonts(altNameCache, true, acPropPref);
        }
    }
    public synchronized void preferProportionalFonts() {
        if (FontUtilities.isLogging()) {
            FontUtilities.getLogger()
                .info("Entered preferProportionalFonts().");
        }
        if (!FontConfiguration.hasMonoToPropMap()) {
            return;
        }
        if (!maybeMultiAppContext()) {
            if (gPropPref == true) {
                return;
            }
            gPropPref = true;
            createCompositeFonts(fontNameCache, gLocalePref, gPropPref);
            _usingAlternateComposites = true;
        } else {
            AppContext appContext = AppContext.getAppContext();
            if (appContext.get(proportionalFontKey) == proportionalFontKey) {
                return;
            }
            appContext.put(proportionalFontKey, proportionalFontKey);
            boolean acLocalePref =
                appContext.get(localeFontKey) == localeFontKey;
            ConcurrentHashMap<String, Font2D>
                altNameCache = new ConcurrentHashMap<String, Font2D> ();
            appContext.put(CompositeFont.class, altNameCache);
            _usingPerAppContextComposites = true;
            createCompositeFonts(altNameCache, acLocalePref, true);
        }
    }
    private static HashSet<String> installedNames = null;
    private static HashSet<String> getInstalledNames() {
        if (installedNames == null) {
           Locale l = getSystemStartupLocale();
           SunFontManager fontManager = SunFontManager.getInstance();
           String[] installedFamilies =
               fontManager.getInstalledFontFamilyNames(l);
           Font[] installedFonts = fontManager.getAllInstalledFonts();
           HashSet<String> names = new HashSet<String>();
           for (int i=0; i<installedFamilies.length; i++) {
               names.add(installedFamilies[i].toLowerCase(l));
           }
           for (int i=0; i<installedFonts.length; i++) {
               names.add(installedFonts[i].getFontName(l).toLowerCase(l));
           }
           installedNames = names;
        }
        return installedNames;
    }
    private static final Object regFamilyKey  = new Object();
    private static final Object regFullNameKey = new Object();
    private Hashtable<String,FontFamily> createdByFamilyName;
    private Hashtable<String,Font2D>     createdByFullName;
    private boolean fontsAreRegistered = false;
    private boolean fontsAreRegisteredPerAppContext = false;
    public boolean registerFont(Font font) {
        if (font == null) {
            return false;
        }
        synchronized (regFamilyKey) {
            if (createdByFamilyName == null) {
                createdByFamilyName = new Hashtable<String,FontFamily>();
                createdByFullName = new Hashtable<String,Font2D>();
            }
        }
        if (! FontAccess.getFontAccess().isCreatedFont(font)) {
            return false;
        }
        HashSet<String> names = getInstalledNames();
        Locale l = getSystemStartupLocale();
        String familyName = font.getFamily(l).toLowerCase();
        String fullName = font.getFontName(l).toLowerCase();
        if (names.contains(familyName) || names.contains(fullName)) {
            return false;
        }
        Hashtable<String,FontFamily> familyTable;
        Hashtable<String,Font2D> fullNameTable;
        if (!maybeMultiAppContext()) {
            familyTable = createdByFamilyName;
            fullNameTable = createdByFullName;
            fontsAreRegistered = true;
        } else {
            AppContext appContext = AppContext.getAppContext();
            familyTable =
                (Hashtable<String,FontFamily>)appContext.get(regFamilyKey);
            fullNameTable =
                (Hashtable<String,Font2D>)appContext.get(regFullNameKey);
            if (familyTable == null) {
                familyTable = new Hashtable<String,FontFamily>();
                fullNameTable = new Hashtable<String,Font2D>();
                appContext.put(regFamilyKey, familyTable);
                appContext.put(regFullNameKey, fullNameTable);
            }
            fontsAreRegisteredPerAppContext = true;
        }
        Font2D font2D = FontUtilities.getFont2D(font);
        int style = font2D.getStyle();
        FontFamily family = familyTable.get(familyName);
        if (family == null) {
            family = new FontFamily(font.getFamily(l));
            familyTable.put(familyName, family);
        }
        if (fontsAreRegistered) {
            removeFromCache(family.getFont(Font.PLAIN));
            removeFromCache(family.getFont(Font.BOLD));
            removeFromCache(family.getFont(Font.ITALIC));
            removeFromCache(family.getFont(Font.BOLD|Font.ITALIC));
            removeFromCache(fullNameTable.get(fullName));
        }
        family.setFont(font2D, style);
        fullNameTable.put(fullName, font2D);
        return true;
    }
    private void removeFromCache(Font2D font) {
        if (font == null) {
            return;
        }
        String[] keys = (String[])(fontNameCache.keySet().toArray(STR_ARRAY));
        for (int k=0; k<keys.length;k++) {
            if (fontNameCache.get(keys[k]) == font) {
                fontNameCache.remove(keys[k]);
            }
        }
    }
    public TreeMap<String, String> getCreatedFontFamilyNames() {
        Hashtable<String,FontFamily> familyTable;
        if (fontsAreRegistered) {
            familyTable = createdByFamilyName;
        } else if (fontsAreRegisteredPerAppContext) {
            AppContext appContext = AppContext.getAppContext();
            familyTable =
                (Hashtable<String,FontFamily>)appContext.get(regFamilyKey);
        } else {
            return null;
        }
        Locale l = getSystemStartupLocale();
        synchronized (familyTable) {
            TreeMap<String, String> map = new TreeMap<String, String>();
            for (FontFamily f : familyTable.values()) {
                Font2D font2D = f.getFont(Font.PLAIN);
                if (font2D == null) {
                    font2D = f.getClosestStyle(Font.PLAIN);
                }
                String name = font2D.getFamilyName(l);
                map.put(name.toLowerCase(l), name);
            }
            return map;
        }
    }
    public Font[] getCreatedFonts() {
        Hashtable<String,Font2D> nameTable;
        if (fontsAreRegistered) {
            nameTable = createdByFullName;
        } else if (fontsAreRegisteredPerAppContext) {
            AppContext appContext = AppContext.getAppContext();
            nameTable =
                (Hashtable<String,Font2D>)appContext.get(regFullNameKey);
        } else {
            return null;
        }
        Locale l = getSystemStartupLocale();
        synchronized (nameTable) {
            Font[] fonts = new Font[nameTable.size()];
            int i=0;
            for (Font2D font2D : nameTable.values()) {
                fonts[i++] = new Font(font2D.getFontName(l), Font.PLAIN, 1);
            }
            return fonts;
        }
    }
    protected String[] getPlatformFontDirs(boolean noType1Fonts) {
        if (pathDirs != null) {
            return pathDirs;
        }
        String path = getPlatformFontPath(noType1Fonts);
        StringTokenizer parser =
            new StringTokenizer(path, File.pathSeparator);
        ArrayList<String> pathList = new ArrayList<String>();
        try {
            while (parser.hasMoreTokens()) {
                pathList.add(parser.nextToken());
            }
        } catch (NoSuchElementException e) {
        }
        pathDirs = pathList.toArray(new String[0]);
        return pathDirs;
    }
    public abstract String[] getDefaultPlatformFont();
    private void addDirFonts(String dirName, File dirFile,
                             FilenameFilter filter,
                             int fontFormat, boolean useJavaRasterizer,
                             int fontRank,
                             boolean defer, boolean resolveSymLinks) {
        String[] ls = dirFile.list(filter);
        if (ls == null || ls.length == 0) {
            return;
        }
        String[] fontNames = new String[ls.length];
        String[][] nativeNames = new String[ls.length][];
        int fontCount = 0;
        for (int i=0; i < ls.length; i++ ) {
            File theFile = new File(dirFile, ls[i]);
            String fullName = null;
            if (resolveSymLinks) {
                try {
                    fullName = theFile.getCanonicalPath();
                } catch (IOException e) {
                }
            }
            if (fullName == null) {
                fullName = dirName + File.separator + ls[i];
            }
            if (registeredFontFiles.contains(fullName)) {
                continue;
            }
            if (badFonts != null && badFonts.contains(fullName)) {
                if (FontUtilities.debugFonts()) {
                    FontUtilities.getLogger()
                                         .warning("skip bad font " + fullName);
                }
                continue; 
            }
            registeredFontFiles.add(fullName);
            if (FontUtilities.debugFonts()
                && FontUtilities.getLogger().isLoggable(PlatformLogger.INFO)) {
                String message = "Registering font " + fullName;
                String[] natNames = getNativeNames(fullName, null);
                if (natNames == null) {
                    message += " with no native name";
                } else {
                    message += " with native name(s) " + natNames[0];
                    for (int nn = 1; nn < natNames.length; nn++) {
                        message += ", " + natNames[nn];
                    }
                }
                FontUtilities.getLogger().info(message);
            }
            fontNames[fontCount] = fullName;
            nativeNames[fontCount++] = getNativeNames(fullName, null);
        }
        registerFonts(fontNames, nativeNames, fontCount, fontFormat,
                         useJavaRasterizer, fontRank, defer);
        return;
    }
    protected String[] getNativeNames(String fontFileName,
                                      String platformName) {
        return null;
    }
    protected String getFileNameFromPlatformName(String platformFontName) {
        return fontConfig.getFileNameFromPlatformName(platformFontName);
    }
    public FontConfiguration getFontConfiguration() {
        return fontConfig;
    }
    public String getPlatformFontPath(boolean noType1Font) {
        if (fontPath == null) {
            fontPath = getFontPath(noType1Font);
        }
        return fontPath;
    }
    public static boolean isOpenJDK() {
        return FontUtilities.isOpenJDK;
    }
    protected void loadFonts() {
        if (discoveredAllFonts) {
            return;
        }
        synchronized (this) {
            if (FontUtilities.debugFonts()) {
                Thread.dumpStack();
                FontUtilities.getLogger()
                            .info("SunGraphicsEnvironment.loadFonts() called");
            }
            initialiseDeferredFonts();
            java.security.AccessController.doPrivileged(
                                    new java.security.PrivilegedAction() {
                public Object run() {
                    if (fontPath == null) {
                        fontPath = getPlatformFontPath(noType1Font);
                        registerFontDirs(fontPath);
                    }
                    if (fontPath != null) {
                        if (! gotFontsFromPlatform()) {
                            registerFontsOnPath(fontPath, false,
                                                Font2D.UNKNOWN_RANK,
                                                false, true);
                            loadedAllFontFiles = true;
                        }
                    }
                    registerOtherFontFiles(registeredFontFiles);
                    discoveredAllFonts = true;
                    return null;
                }
            });
        }
    }
    protected void registerFontDirs(String pathName) {
        return;
    }
    private void registerFontsOnPath(String pathName,
                                     boolean useJavaRasterizer, int fontRank,
                                     boolean defer, boolean resolveSymLinks) {
        StringTokenizer parser = new StringTokenizer(pathName,
                File.pathSeparator);
        try {
            while (parser.hasMoreTokens()) {
                registerFontsInDir(parser.nextToken(),
                        useJavaRasterizer, fontRank,
                        defer, resolveSymLinks);
            }
        } catch (NoSuchElementException e) {
        }
    }
    public void registerFontsInDir(String dirName) {
        registerFontsInDir(dirName, true, Font2D.JRE_RANK, true, false);
    }
    private void registerFontsInDir(String dirName, boolean useJavaRasterizer,
                                    int fontRank,
                                    boolean defer, boolean resolveSymLinks) {
        File pathFile = new File(dirName);
        addDirFonts(dirName, pathFile, ttFilter,
                    FONTFORMAT_TRUETYPE, useJavaRasterizer,
                    fontRank==Font2D.UNKNOWN_RANK ?
                    Font2D.TTF_RANK : fontRank,
                    defer, resolveSymLinks);
        addDirFonts(dirName, pathFile, t1Filter,
                    FONTFORMAT_TYPE1, useJavaRasterizer,
                    fontRank==Font2D.UNKNOWN_RANK ?
                    Font2D.TYPE1_RANK : fontRank,
                    defer, resolveSymLinks);
    }
    protected void registerFontDir(String path) {
    }
    public synchronized String getDefaultFontFile() {
        if (defaultFontFileName == null) {
            initDefaultFonts();
        }
        return defaultFontFileName;
    }
    private void initDefaultFonts() {
        if (!isOpenJDK()) {
            defaultFontName = lucidaFontName;
            if (useAbsoluteFontFileNames()) {
                defaultFontFileName =
                    jreFontDirName + File.separator + FontUtilities.LUCIDA_FILE_NAME;
            } else {
                defaultFontFileName = FontUtilities.LUCIDA_FILE_NAME;
            }
        }
    }
    protected boolean useAbsoluteFontFileNames() {
        return true;
    }
    protected abstract FontConfiguration createFontConfiguration();
    public abstract FontConfiguration
    createFontConfiguration(boolean preferLocaleFonts,
                            boolean preferPropFonts);
    public synchronized String getDefaultFontFaceName() {
        if (defaultFontName == null) {
            initDefaultFonts();
        }
        return defaultFontName;
    }
    public void loadFontFiles() {
        loadFonts();
        if (loadedAllFontFiles) {
            return;
        }
        synchronized (this) {
            if (FontUtilities.debugFonts()) {
                Thread.dumpStack();
                FontUtilities.getLogger().info("loadAllFontFiles() called");
            }
            java.security.AccessController.doPrivileged(
                                    new java.security.PrivilegedAction() {
                public Object run() {
                    if (fontPath == null) {
                        fontPath = getPlatformFontPath(noType1Font);
                    }
                    if (fontPath != null) {
                        registerFontsOnPath(fontPath, false,
                                            Font2D.UNKNOWN_RANK,
                                            false, true);
                    }
                    loadedAllFontFiles = true;
                    return null;
                }
            });
        }
    }
    private void
        initCompositeFonts(FontConfiguration fontConfig,
                           ConcurrentHashMap<String, Font2D>  altNameCache) {
        if (FontUtilities.isLogging()) {
            FontUtilities.getLogger()
                            .info("Initialising composite fonts");
        }
        int numCoreFonts = fontConfig.getNumberCoreFonts();
        String[] fcFonts = fontConfig.getPlatformFontNames();
        for (int f=0; f<fcFonts.length; f++) {
            String platformFontName = fcFonts[f];
            String fontFileName =
                getFileNameFromPlatformName(platformFontName);
            String[] nativeNames = null;
            if (fontFileName == null
                || fontFileName.equals(platformFontName)) {
                fontFileName = platformFontName;
            } else {
                if (f < numCoreFonts) {
                    addFontToPlatformFontPath(platformFontName);
                }
                nativeNames = getNativeNames(fontFileName, platformFontName);
            }
            registerFontFile(fontFileName, nativeNames,
                             Font2D.FONT_CONFIG_RANK, true);
        }
        registerPlatformFontsUsedByFontConfiguration();
        CompositeFontDescriptor[] compositeFontInfo
                = fontConfig.get2DCompositeFontInfo();
        for (int i = 0; i < compositeFontInfo.length; i++) {
            CompositeFontDescriptor descriptor = compositeFontInfo[i];
            String[] componentFileNames = descriptor.getComponentFileNames();
            String[] componentFaceNames = descriptor.getComponentFaceNames();
            if (missingFontFiles != null) {
                for (int ii=0; ii<componentFileNames.length; ii++) {
                    if (missingFontFiles.contains(componentFileNames[ii])) {
                        componentFileNames[ii] = getDefaultFontFile();
                        componentFaceNames[ii] = getDefaultFontFaceName();
                    }
                }
            }
            if (altNameCache != null) {
                SunFontManager.registerCompositeFont(
                    descriptor.getFaceName(),
                    componentFileNames, componentFaceNames,
                    descriptor.getCoreComponentCount(),
                    descriptor.getExclusionRanges(),
                    descriptor.getExclusionRangeLimits(),
                    true,
                    altNameCache);
            } else {
                registerCompositeFont(descriptor.getFaceName(),
                                      componentFileNames, componentFaceNames,
                                      descriptor.getCoreComponentCount(),
                                      descriptor.getExclusionRanges(),
                                      descriptor.getExclusionRangeLimits(),
                                      true);
            }
            if (FontUtilities.debugFonts()) {
                FontUtilities.getLogger()
                               .info("registered " + descriptor.getFaceName());
            }
        }
    }
    protected void addFontToPlatformFontPath(String platformFontName) {
    }
    protected void registerFontFile(String fontFileName, String[] nativeNames,
                                    int fontRank, boolean defer) {
        if (registeredFontFiles.contains(fontFileName)) {
            return;
        }
        int fontFormat;
        if (ttFilter.accept(null, fontFileName)) {
            fontFormat = FONTFORMAT_TRUETYPE;
        } else if (t1Filter.accept(null, fontFileName)) {
            fontFormat = FONTFORMAT_TYPE1;
        } else {
            fontFormat = FONTFORMAT_NATIVE;
        }
        registeredFontFiles.add(fontFileName);
        if (defer) {
            registerDeferredFont(fontFileName, fontFileName, nativeNames,
                                 fontFormat, false, fontRank);
        } else {
            registerFontFile(fontFileName, nativeNames, fontFormat, false,
                             fontRank);
        }
    }
    protected void registerPlatformFontsUsedByFontConfiguration() {
    }
    protected void addToMissingFontFileList(String fileName) {
        if (missingFontFiles == null) {
            missingFontFiles = new HashSet<String>();
        }
        missingFontFiles.add(fileName);
    }
    private boolean isNameForRegisteredFile(String fontName) {
        String fileName = getFileNameForFontName(fontName);
        if (fileName == null) {
            return false;
        }
        return registeredFontFiles.contains(fileName);
    }
    public void
        createCompositeFonts(ConcurrentHashMap<String, Font2D> altNameCache,
                             boolean preferLocale,
                             boolean preferProportional) {
        FontConfiguration fontConfig =
            createFontConfiguration(preferLocale, preferProportional);
        initCompositeFonts(fontConfig, altNameCache);
    }
    public Font[] getAllInstalledFonts() {
        if (allFonts == null) {
            loadFonts();
            TreeMap fontMapNames = new TreeMap();
            Font2D[] allfonts = getRegisteredFonts();
            for (int i=0; i < allfonts.length; i++) {
                if (!(allfonts[i] instanceof NativeFont)) {
                    fontMapNames.put(allfonts[i].getFontName(null),
                                     allfonts[i]);
                }
            }
            String[] platformNames = getFontNamesFromPlatform();
            if (platformNames != null) {
                for (int i=0; i<platformNames.length; i++) {
                    if (!isNameForRegisteredFile(platformNames[i])) {
                        fontMapNames.put(platformNames[i], null);
                    }
                }
            }
            String[] fontNames = null;
            if (fontMapNames.size() > 0) {
                fontNames = new String[fontMapNames.size()];
                Object [] keyNames = fontMapNames.keySet().toArray();
                for (int i=0; i < keyNames.length; i++) {
                    fontNames[i] = (String)keyNames[i];
                }
            }
            Font[] fonts = new Font[fontNames.length];
            for (int i=0; i < fontNames.length; i++) {
                fonts[i] = new Font(fontNames[i], Font.PLAIN, 1);
                Font2D f2d = (Font2D)fontMapNames.get(fontNames[i]);
                if (f2d  != null) {
                    FontAccess.getFontAccess().setFont2D(fonts[i], f2d.handle);
                }
            }
            allFonts = fonts;
        }
        Font []copyFonts = new Font[allFonts.length];
        System.arraycopy(allFonts, 0, copyFonts, 0, allFonts.length);
        return copyFonts;
    }
    public String[] getInstalledFontFamilyNames(Locale requestedLocale) {
        if (requestedLocale == null) {
            requestedLocale = Locale.getDefault();
        }
        if (allFamilies != null && lastDefaultLocale != null &&
            requestedLocale.equals(lastDefaultLocale)) {
                String[] copyFamilies = new String[allFamilies.length];
                System.arraycopy(allFamilies, 0, copyFamilies,
                                 0, allFamilies.length);
                return copyFamilies;
        }
        TreeMap<String,String> familyNames = new TreeMap<String,String>();
        String str;
        str = Font.SERIF;         familyNames.put(str.toLowerCase(), str);
        str = Font.SANS_SERIF;    familyNames.put(str.toLowerCase(), str);
        str = Font.MONOSPACED;    familyNames.put(str.toLowerCase(), str);
        str = Font.DIALOG;        familyNames.put(str.toLowerCase(), str);
        str = Font.DIALOG_INPUT;  familyNames.put(str.toLowerCase(), str);
        if (requestedLocale.equals(getSystemStartupLocale()) &&
            getFamilyNamesFromPlatform(familyNames, requestedLocale)) {
            getJREFontFamilyNames(familyNames, requestedLocale);
        } else {
            loadFontFiles();
            Font2D[] physicalfonts = getPhysicalFonts();
            for (int i=0; i < physicalfonts.length; i++) {
                if (!(physicalfonts[i] instanceof NativeFont)) {
                    String name =
                        physicalfonts[i].getFamilyName(requestedLocale);
                    familyNames.put(name.toLowerCase(requestedLocale), name);
                }
            }
        }
        String[] retval =  new String[familyNames.size()];
        Object [] keyNames = familyNames.keySet().toArray();
        for (int i=0; i < keyNames.length; i++) {
            retval[i] = (String)familyNames.get(keyNames[i]);
        }
        if (requestedLocale.equals(Locale.getDefault())) {
            lastDefaultLocale = requestedLocale;
            allFamilies = new String[retval.length];
            System.arraycopy(retval, 0, allFamilies, 0, allFamilies.length);
        }
        return retval;
    }
    public void register1dot0Fonts() {
        java.security.AccessController.doPrivileged(
                            new java.security.PrivilegedAction() {
            public Object run() {
                String type1Dir = "/usr/openwin/lib/X11/fonts/Type1";
                registerFontsInDir(type1Dir, true, Font2D.TYPE1_RANK,
                                   false, false);
                return null;
            }
        });
    }
    protected void getJREFontFamilyNames(TreeMap<String,String> familyNames,
                                         Locale requestedLocale) {
        registerDeferredJREFonts(jreFontDirName);
        Font2D[] physicalfonts = getPhysicalFonts();
        for (int i=0; i < physicalfonts.length; i++) {
            if (!(physicalfonts[i] instanceof NativeFont)) {
                String name =
                    physicalfonts[i].getFamilyName(requestedLocale);
                familyNames.put(name.toLowerCase(requestedLocale), name);
            }
        }
    }
    private static Locale systemLocale = null;
    private static Locale getSystemStartupLocale() {
        if (systemLocale == null) {
            systemLocale = (Locale)
                java.security.AccessController.doPrivileged(
                                    new java.security.PrivilegedAction() {
            public Object run() {
                String fileEncoding = System.getProperty("file.encoding", "");
                String sysEncoding = System.getProperty("sun.jnu.encoding");
                if (sysEncoding != null && !sysEncoding.equals(fileEncoding)) {
                    return Locale.ROOT;
                }
                String language = System.getProperty("user.language", "en");
                String country  = System.getProperty("user.country","");
                String variant  = System.getProperty("user.variant","");
                return new Locale(language, country, variant);
            }
        });
        }
        return systemLocale;
    }
    void addToPool(FileFont font) {
        FileFont fontFileToClose = null;
        int freeSlot = -1;
        synchronized (fontFileCache) {
            for (int i=0;i<CHANNELPOOLSIZE;i++) {
                if (fontFileCache[i] == font) {
                    return;
                }
                if (fontFileCache[i] == null && freeSlot < 0) {
                    freeSlot = i;
                }
            }
            if (freeSlot >= 0) {
                fontFileCache[freeSlot] = font;
                return;
            } else {
                fontFileToClose = fontFileCache[lastPoolIndex];
                fontFileCache[lastPoolIndex] = font;
                lastPoolIndex = (lastPoolIndex+1) % CHANNELPOOLSIZE;
            }
        }
        if (fontFileToClose != null) {
            fontFileToClose.close();
        }
    }
    protected FontUIResource getFontConfigFUIR(String family, int style,
                                               int size)
    {
        return new FontUIResource(family, style, size);
    }
}
