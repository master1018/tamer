public abstract class FontConfiguration {
    protected static String osVersion;
    protected static String osName;
    protected static String encoding; 
    protected static Locale startupLocale = null;
    protected static Hashtable localeMap = null;
    private static FontConfiguration fontConfig;
    private static PlatformLogger logger;
    protected static boolean isProperties = true;
    protected SunFontManager fontManager;
    protected boolean preferLocaleFonts;
    protected boolean preferPropFonts;
    private File fontConfigFile;
    private boolean foundOsSpecificFile;
    private boolean inited;
    private String javaLib;
    public FontConfiguration(SunFontManager fm) {
        if (FontUtilities.debugFonts()) {
            FontUtilities.getLogger()
                .info("Creating standard Font Configuration");
        }
        if (FontUtilities.debugFonts() && logger == null) {
            logger = PlatformLogger.getLogger("sun.awt.FontConfiguration");
        }
        fontManager = fm;
        setOsNameAndVersion();  
        setEncoding();          
        findFontConfigFile();
    }
    public synchronized boolean init() {
        if (!inited) {
            this.preferLocaleFonts = false;
            this.preferPropFonts = false;
            setFontConfiguration();
            readFontConfigFile(fontConfigFile);
            initFontConfig();
            inited = true;
        }
        return true;
    }
    public FontConfiguration(SunFontManager fm,
                             boolean preferLocaleFonts,
                             boolean preferPropFonts) {
        fontManager = fm;
        if (FontUtilities.debugFonts()) {
            FontUtilities.getLogger()
                .info("Creating alternate Font Configuration");
        }
        this.preferLocaleFonts = preferLocaleFonts;
        this.preferPropFonts = preferPropFonts;
        initFontConfig();
    }
    protected void setOsNameAndVersion() {
        osName = System.getProperty("os.name");
        osVersion = System.getProperty("os.version");
    }
    private void setEncoding() {
        encoding = Charset.defaultCharset().name();
        startupLocale = SunToolkit.getStartupLocale();
    }
    public boolean foundOsSpecificFile() {
        return foundOsSpecificFile;
    }
    public boolean fontFilesArePresent() {
        init();
        short fontNameID = compFontNameIDs[0][0][0];
        short fileNameID = getComponentFileID(fontNameID);
        final String fileName = mapFileName(getComponentFileName(fileNameID));
        Boolean exists = (Boolean)java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                 public Object run() {
                     try {
                         File f = new File(fileName);
                         return Boolean.valueOf(f.exists());
                     }
                     catch (Exception e) {
                         return false;
                     }
                 }
                });
        return exists.booleanValue();
    }
    private void findFontConfigFile() {
        foundOsSpecificFile = true; 
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            throw new Error("java.home property not set");
        }
        javaLib = javaHome + File.separator + "lib";
        String userConfigFile = System.getProperty("sun.awt.fontconfig");
        if (userConfigFile != null) {
            fontConfigFile = new File(userConfigFile);
        } else {
            fontConfigFile = findFontConfigFile(javaLib);
        }
    }
    private void readFontConfigFile(File f) {
        getInstalledFallbackFonts(javaLib);
        if (f != null) {
            try {
                FileInputStream in = new FileInputStream(f.getPath());
                if (isProperties) {
                    loadProperties(in);
                } else {
                    loadBinary(in);
                }
                in.close();
                if (FontUtilities.debugFonts()) {
                    logger.config("Read logical font configuration from " + f);
                }
            } catch (IOException e) {
                if (FontUtilities.debugFonts()) {
                    logger.config("Failed to read logical font configuration from " + f);
                }
            }
        }
        String version = getVersion();
        if (!"1".equals(version) && FontUtilities.debugFonts()) {
            logger.config("Unsupported fontconfig version: " + version);
        }
    }
    protected void getInstalledFallbackFonts(String javaLib) {
        String fallbackDirName = javaLib + File.separator +
            "fonts" + File.separator + "fallback";
        File fallbackDir = new File(fallbackDirName);
        if (fallbackDir.exists() && fallbackDir.isDirectory()) {
            String[] ttfs = fallbackDir.list(fontManager.getTrueTypeFilter());
            String[] t1s = fallbackDir.list(fontManager.getType1Filter());
            int numTTFs = (ttfs == null) ? 0 : ttfs.length;
            int numT1s = (t1s == null) ? 0 : t1s.length;
            int len = numTTFs + numT1s;
            if (numTTFs + numT1s == 0) {
                return;
            }
            installedFallbackFontFiles = new String[len];
            for (int i=0; i<numTTFs; i++) {
                installedFallbackFontFiles[i] =
                    fallbackDir + File.separator + ttfs[i];
            }
            for (int i=0; i<numT1s; i++) {
                installedFallbackFontFiles[i+numTTFs] =
                    fallbackDir + File.separator + t1s[i];
            }
            fontManager.registerFontsInDir(fallbackDirName);
        }
    }
    private File findImpl(String fname) {
        File f = new File(fname + ".properties");
        if (f.canRead()) {
            isProperties = true;
            return f;
        }
        f = new File(fname + ".bfc");
        if (f.canRead()) {
            isProperties = false;
            return f;
        }
        return null;
    }
    private File findFontConfigFile(String javaLib) {
        String baseName = javaLib + File.separator + "fontconfig";
        File configFile;
        if (osVersion != null && osName != null) {
            configFile = findImpl(baseName + "." + osName + "." + osVersion);
            if (configFile != null) {
                return configFile;
            }
        }
        if (osName != null) {
            configFile = findImpl(baseName + "." + osName);
            if (configFile != null) {
                return configFile;
            }
        }
        if (osVersion != null) {
            configFile = findImpl(baseName + "." + osVersion);
            if (configFile != null) {
                return configFile;
            }
        }
        foundOsSpecificFile = false;
        configFile = findImpl(baseName);
        if (configFile != null) {
            return configFile;
        }
        return null;
    }
    public static void loadBinary(InputStream inStream) throws IOException {
        DataInputStream in = new DataInputStream(inStream);
        head = readShortTable(in, HEAD_LENGTH);
        int[] tableSizes = new int[INDEX_TABLEEND];
        for (int i = 0; i < INDEX_TABLEEND; i++) {
            tableSizes[i] = head[i + 1] - head[i];
        }
        table_scriptIDs       = readShortTable(in, tableSizes[INDEX_scriptIDs]);
        table_scriptFonts     = readShortTable(in, tableSizes[INDEX_scriptFonts]);
        table_elcIDs          = readShortTable(in, tableSizes[INDEX_elcIDs]);
        table_sequences        = readShortTable(in, tableSizes[INDEX_sequences]);
        table_fontfileNameIDs = readShortTable(in, tableSizes[INDEX_fontfileNameIDs]);
        table_componentFontNameIDs = readShortTable(in, tableSizes[INDEX_componentFontNameIDs]);
        table_filenames       = readShortTable(in, tableSizes[INDEX_filenames]);
        table_awtfontpaths    = readShortTable(in, tableSizes[INDEX_awtfontpaths]);
        table_exclusions      = readShortTable(in, tableSizes[INDEX_exclusions]);
        table_proportionals   = readShortTable(in, tableSizes[INDEX_proportionals]);
        table_scriptFontsMotif   = readShortTable(in, tableSizes[INDEX_scriptFontsMotif]);
        table_alphabeticSuffix   = readShortTable(in, tableSizes[INDEX_alphabeticSuffix]);
        table_stringIDs       = readShortTable(in, tableSizes[INDEX_stringIDs]);
        stringCache = new String[table_stringIDs.length + 1];
        int len = tableSizes[INDEX_stringTable];
        byte[] bb = new byte[len * 2];
        table_stringTable = new char[len];
        in.read(bb);
        int i = 0, j = 0;
        while (i < len) {
           table_stringTable[i++] = (char)(bb[j++] << 8 | (bb[j++] & 0xff));
        }
        if (verbose) {
            dump();
        }
    }
    public static void saveBinary(OutputStream out) throws IOException {
        sanityCheck();
        DataOutputStream dataOut = new DataOutputStream(out);
        writeShortTable(dataOut, head);
        writeShortTable(dataOut, table_scriptIDs);
        writeShortTable(dataOut, table_scriptFonts);
        writeShortTable(dataOut, table_elcIDs);
        writeShortTable(dataOut, table_sequences);
        writeShortTable(dataOut, table_fontfileNameIDs);
        writeShortTable(dataOut, table_componentFontNameIDs);
        writeShortTable(dataOut, table_filenames);
        writeShortTable(dataOut, table_awtfontpaths);
        writeShortTable(dataOut, table_exclusions);
        writeShortTable(dataOut, table_proportionals);
        writeShortTable(dataOut, table_scriptFontsMotif);
        writeShortTable(dataOut, table_alphabeticSuffix);
        writeShortTable(dataOut, table_stringIDs);
        dataOut.writeChars(new String(table_stringTable));
        out.close();
        if (verbose) {
            dump();
        }
    }
    private static short stringIDNum;
    private static short[] stringIDs;
    private static StringBuilder stringTable;
    public static void loadProperties(InputStream in) throws IOException {
        stringIDNum = 1;
        stringIDs = new short[1000];
        stringTable = new StringBuilder(4096);
        if (verbose && logger == null) {
            logger = PlatformLogger.getLogger("sun.awt.FontConfiguration");
        }
        new PropertiesHandler().load(in);
        stringIDs = null;
        stringTable = null;
    }
    private void initFontConfig() {
        initLocale = startupLocale;
        initEncoding = encoding;
        if (preferLocaleFonts && !willReorderForStartupLocale()) {
            preferLocaleFonts = false;
        }
        initELC = getInitELC();
        initAllComponentFonts();
    }
    private short getInitELC() {
        if (initELC != -1) {
            return initELC;
        }
        HashMap <String, Integer> elcIDs = new HashMap<String, Integer>();
        for (int i = 0; i < table_elcIDs.length; i++) {
            elcIDs.put(getString(table_elcIDs[i]), i);
        }
        String language = initLocale.getLanguage();
        String country = initLocale.getCountry();
        String elc;
        if (elcIDs.containsKey(elc=initEncoding + "." + language + "." + country)
            || elcIDs.containsKey(elc=initEncoding + "." + language)
            || elcIDs.containsKey(elc=initEncoding)) {
            initELC = elcIDs.get(elc).shortValue();
        } else {
            initELC = elcIDs.get("NULL.NULL.NULL").shortValue();
        }
        int i = 0;
        while (i < table_alphabeticSuffix.length) {
            if (initELC == table_alphabeticSuffix[i]) {
                alphabeticSuffix = getString(table_alphabeticSuffix[i + 1]);
                return initELC;
            }
            i += 2;
        }
        return initELC;
    }
    public static boolean verbose;
    private short    initELC = -1;
    private Locale   initLocale;
    private String   initEncoding;
    private String   alphabeticSuffix;
    private short[][][] compFontNameIDs = new short[NUM_FONTS][NUM_STYLES][];
    private int[][][] compExclusions = new int[NUM_FONTS][][];
    private int[] compCoreNum = new int[NUM_FONTS];
    private Set<Short> coreFontNameIDs = new HashSet<Short>();
    private Set<Short> fallbackFontNameIDs = new HashSet<Short>();
    private void initAllComponentFonts() {
        short[] fallbackScripts = getFallbackScripts();
        for (int fontIndex = 0; fontIndex < NUM_FONTS; fontIndex++) {
            short[] coreScripts = getCoreScripts(fontIndex);
            compCoreNum[fontIndex] = coreScripts.length;
            int[][] exclusions = new int[coreScripts.length][];
            for (int i = 0; i < coreScripts.length; i++) {
                exclusions[i] = getExclusionRanges(coreScripts[i]);
            }
            compExclusions[fontIndex] = exclusions;
            for (int styleIndex = 0; styleIndex < NUM_STYLES; styleIndex++) {
                int index;
                short[] nameIDs = new short[coreScripts.length + fallbackScripts.length];
                for (index = 0; index < coreScripts.length; index++) {
                    nameIDs[index] = getComponentFontID(coreScripts[index],
                                               fontIndex, styleIndex);
                    if (preferLocaleFonts && localeMap != null &&
                            fontManager.usingAlternateFontforJALocales()) {
                        nameIDs[index] = remapLocaleMap(fontIndex, styleIndex,
                                                        coreScripts[index], nameIDs[index]);
                    }
                    if (preferPropFonts) {
                        nameIDs[index] = remapProportional(fontIndex, nameIDs[index]);
                    }
                    coreFontNameIDs.add(nameIDs[index]);
                }
                for (int i = 0; i < fallbackScripts.length; i++) {
                    short id = getComponentFontID(fallbackScripts[i],
                                               fontIndex, styleIndex);
                    if (preferLocaleFonts && localeMap != null &&
                            fontManager.usingAlternateFontforJALocales()) {
                        id = remapLocaleMap(fontIndex, styleIndex, fallbackScripts[i], id);
                    }
                    if (preferPropFonts) {
                        id = remapProportional(fontIndex, id);
                    }
                    if (contains(nameIDs, id, index)) {
                        continue;
                    }
                    fallbackFontNameIDs.add(id);
                    nameIDs[index++] = id;
                }
                if (index < nameIDs.length) {
                    short[] newNameIDs = new short[index];
                    System.arraycopy(nameIDs, 0, newNameIDs, 0, index);
                    nameIDs = newNameIDs;
                }
                compFontNameIDs[fontIndex][styleIndex] = nameIDs;
            }
        }
   }
   private short remapLocaleMap(int fontIndex, int styleIndex, short scriptID, short fontID) {
        String scriptName = getString(table_scriptIDs[scriptID]);
        String value = (String)localeMap.get(scriptName);
        if (value == null) {
            String fontName = fontNames[fontIndex];
            String styleName = styleNames[styleIndex];
            value = (String)localeMap.get(fontName + "." + styleName + "." + scriptName);
        }
        if (value == null) {
            return fontID;
        }
        for (int i = 0; i < table_componentFontNameIDs.length; i++) {
            String name = getString(table_componentFontNameIDs[i]);
            if (value.equalsIgnoreCase(name)) {
                fontID = (short)i;
                break;
            }
        }
        return fontID;
    }
    public static boolean hasMonoToPropMap() {
        return table_proportionals != null && table_proportionals.length != 0;
    }
    private short remapProportional(int fontIndex, short id) {
    if (preferPropFonts &&
        table_proportionals.length != 0 &&
        fontIndex != 2 &&         
        fontIndex != 4) {         
            int i = 0;
            while (i < table_proportionals.length) {
                if (table_proportionals[i] == id) {
                    return table_proportionals[i + 1];
                }
                i += 2;
            }
        }
        return id;
    }
    protected static final int NUM_FONTS = 5;
    protected static final int NUM_STYLES = 4;
    protected static final String[] fontNames
            = {"serif", "sansserif", "monospaced", "dialog", "dialoginput"};
    protected static final String[] publicFontNames
            = {Font.SERIF, Font.SANS_SERIF, Font.MONOSPACED, Font.DIALOG,
               Font.DIALOG_INPUT};
    protected static final String[] styleNames
            = {"plain", "bold", "italic", "bolditalic"};
    public static boolean isLogicalFontFamilyName(String fontName) {
        return isLogicalFontFamilyNameLC(fontName.toLowerCase(Locale.ENGLISH));
    }
    public static boolean isLogicalFontFamilyNameLC(String fontName) {
        for (int i = 0; i < fontNames.length; i++) {
            if (fontName.equals(fontNames[i])) {
                return true;
            }
        }
        return false;
    }
    private static boolean isLogicalFontStyleName(String styleName) {
        for (int i = 0; i < styleNames.length; i++) {
            if (styleName.equals(styleNames[i])) {
                return true;
            }
        }
        return false;
    }
    public static boolean isLogicalFontFaceName(String fontName) {
        return isLogicalFontFaceNameLC(fontName.toLowerCase(Locale.ENGLISH));
    }
    public static boolean isLogicalFontFaceNameLC(String fontName) {
        int period = fontName.indexOf('.');
        if (period >= 0) {
            String familyName = fontName.substring(0, period);
            String styleName = fontName.substring(period + 1);
            return isLogicalFontFamilyName(familyName) &&
                    isLogicalFontStyleName(styleName);
        } else {
            return isLogicalFontFamilyName(fontName);
        }
    }
    protected static int getFontIndex(String fontName) {
        return getArrayIndex(fontNames, fontName);
    }
    protected static int getStyleIndex(String styleName) {
        return getArrayIndex(styleNames, styleName);
    }
    private static int getArrayIndex(String[] names, String name) {
        for (int i = 0; i < names.length; i++) {
            if (name.equals(names[i])) {
                return i;
            }
        }
        assert false;
        return 0;
    }
    protected static int getStyleIndex(int style) {
        switch (style) {
            case Font.PLAIN:
                return 0;
            case Font.BOLD:
                return 1;
            case Font.ITALIC:
                return 2;
            case Font.BOLD | Font.ITALIC:
                return 3;
            default:
                return 0;
        }
    }
    protected static String getFontName(int fontIndex) {
        return fontNames[fontIndex];
    }
    protected static String getStyleName(int styleIndex) {
        return styleNames[styleIndex];
    }
    public static String getLogicalFontFaceName(String familyName, int style) {
        assert isLogicalFontFamilyName(familyName);
        return familyName.toLowerCase(Locale.ENGLISH) + "." + getStyleString(style);
    }
    public static String getStyleString(int style) {
        return getStyleName(getStyleIndex(style));
    }
    public abstract String getFallbackFamilyName(String fontName, String defaultFallback);
    protected String getCompatibilityFamilyName(String fontName) {
        fontName = fontName.toLowerCase(Locale.ENGLISH);
        if (fontName.equals("timesroman")) {
            return "serif";
        } else if (fontName.equals("helvetica")) {
            return "sansserif";
        } else if (fontName.equals("courier")) {
            return "monospaced";
        }
        return null;
    }
    protected static String[] installedFallbackFontFiles = null;
    protected String mapFileName(String fileName) {
        return fileName;
    }
    protected HashMap reorderMap = null;
    protected abstract void initReorderMap();
    private void shuffle(String[] seq, int src, int dst) {
        if (dst >= src) {
            return;
        }
        String tmp = seq[src];
        for (int i=src; i>dst; i--) {
            seq[i] = seq[i-1];
        }
        seq[dst] = tmp;
    }
    public static boolean willReorderForStartupLocale() {
        return getReorderSequence() != null;
    }
    private static Object getReorderSequence() {
        if (fontConfig.reorderMap == null) {
             fontConfig.initReorderMap();
        }
        HashMap reorderMap = fontConfig.reorderMap;
        String language = startupLocale.getLanguage();
        String country = startupLocale.getCountry();
        Object val = reorderMap.get(encoding + "." + language + "." + country);
        if (val == null) {
            val = reorderMap.get(encoding + "." + language);
        }
        if (val == null) {
            val = reorderMap.get(encoding);
        }
        return val;
    }
     private void reorderSequenceForLocale(String[] seq) {
        Object val =  getReorderSequence();
        if (val instanceof String) {
            for (int i=0; i< seq.length; i++) {
                if (seq[i].equals(val)) {
                    shuffle(seq, i, 0);
                    return;
                }
            }
        } else if (val instanceof String[]) {
            String[] fontLangs = (String[])val;
            for (int l=0; l<fontLangs.length;l++) {
                for (int i=0; i<seq.length;i++) {
                    if (seq[i].equals(fontLangs[l])) {
                        shuffle(seq, i, l);
                    }
                }
            }
        }
    }
    private static Vector splitSequence(String sequence) {
        Vector parts = new Vector();
        int start = 0;
        int end;
        while ((end = sequence.indexOf(',', start)) >= 0) {
            parts.add(sequence.substring(start, end));
            start = end + 1;
        }
        if (sequence.length() > start) {
            parts.add(sequence.substring(start, sequence.length()));
        }
        return parts;
    }
    protected String[] split(String sequence) {
        Vector v = splitSequence(sequence);
        return (String[])v.toArray(new String[0]);
    }
    private Hashtable charsetRegistry = new Hashtable(5);
    public FontDescriptor[] getFontDescriptors(String fontName, int style) {
        assert isLogicalFontFamilyName(fontName);
        fontName = fontName.toLowerCase(Locale.ENGLISH);
        int fontIndex = getFontIndex(fontName);
        int styleIndex = getStyleIndex(style);
        return getFontDescriptors(fontIndex, styleIndex);
    }
    private FontDescriptor[][][] fontDescriptors =
        new FontDescriptor[NUM_FONTS][NUM_STYLES][];
    private FontDescriptor[] getFontDescriptors(int fontIndex, int styleIndex) {
        FontDescriptor[] descriptors = fontDescriptors[fontIndex][styleIndex];
        if (descriptors == null) {
            descriptors = buildFontDescriptors(fontIndex, styleIndex);
            fontDescriptors[fontIndex][styleIndex] = descriptors;
        }
        return descriptors;
    }
    private FontDescriptor[] buildFontDescriptors(int fontIndex, int styleIndex) {
        String fontName = fontNames[fontIndex];
        String styleName = styleNames[styleIndex];
        short[] scriptIDs = getCoreScripts(fontIndex);
        short[] nameIDs = compFontNameIDs[fontIndex][styleIndex];
        String[] sequence = new String[scriptIDs.length];
        String[] names = new String[scriptIDs.length];
        for (int i = 0; i < sequence.length; i++) {
            names[i] = getComponentFontName(nameIDs[i]);
            sequence[i] = getScriptName(scriptIDs[i]);
            if (alphabeticSuffix != null && "alphabetic".equals(sequence[i])) {
                sequence[i] = sequence[i] + "/" + alphabeticSuffix;
            }
        }
        int[][] fontExclusionRanges = compExclusions[fontIndex];
        FontDescriptor[] descriptors = new FontDescriptor[names.length];
        for (int i = 0; i < names.length; i++) {
            String awtFontName;
            String encoding;
            awtFontName = makeAWTFontName(names[i], sequence[i]);
            encoding = getEncoding(names[i], sequence[i]);
            if (encoding == null) {
                encoding = "default";
            }
            CharsetEncoder enc
                    = getFontCharsetEncoder(encoding.trim(), awtFontName);
            int[] exclusionRanges = fontExclusionRanges[i];
            descriptors[i] = new FontDescriptor(awtFontName, enc, exclusionRanges);
        }
        return descriptors;
    }
    protected String makeAWTFontName(String platformFontName,
            String characterSubsetName) {
        return platformFontName;
    }
    protected abstract String getEncoding(String awtFontName,
            String characterSubsetName);
    private CharsetEncoder getFontCharsetEncoder(final String charsetName,
            String fontName) {
        Charset fc = null;
        if (charsetName.equals("default")) {
            fc = (Charset) charsetRegistry.get(fontName);
        } else {
            fc = (Charset) charsetRegistry.get(charsetName);
        }
        if (fc != null) {
            return fc.newEncoder();
        }
        if (!charsetName.startsWith("sun.awt.") && !charsetName.equals("default")) {
            fc = Charset.forName(charsetName);
        } else {
            Class fcc = (Class) AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        try {
                            return Class.forName(charsetName, true,
                                                 Thread.currentThread().getContextClassLoader());
                        } catch (ClassNotFoundException e) {
                        }
                        return null;
                    }
                });
            if (fcc != null) {
                try {
                    fc = (Charset) fcc.newInstance();
                } catch (Exception e) {
                }
            }
        }
        if (fc == null) {
            fc = getDefaultFontCharset(fontName);
        }
        if (charsetName.equals("default")){
            charsetRegistry.put(fontName, fc);
        } else {
            charsetRegistry.put(charsetName, fc);
        }
        return fc.newEncoder();
    }
    protected abstract Charset getDefaultFontCharset(
            String fontName);
    public HashSet<String> getAWTFontPathSet() {
        return null;
    }
    public CompositeFontDescriptor[] get2DCompositeFontInfo() {
        CompositeFontDescriptor[] result =
                new CompositeFontDescriptor[NUM_FONTS * NUM_STYLES];
        String defaultFontFile = fontManager.getDefaultFontFile();
        String defaultFontFaceName = fontManager.getDefaultFontFaceName();
        for (int fontIndex = 0; fontIndex < NUM_FONTS; fontIndex++) {
            String fontName = publicFontNames[fontIndex];
            int[][] exclusions = compExclusions[fontIndex];
            int numExclusionRanges = 0;
            for (int i = 0; i < exclusions.length; i++) {
                numExclusionRanges += exclusions[i].length;
            }
            int[] exclusionRanges = new int[numExclusionRanges];
            int[] exclusionRangeLimits = new int[exclusions.length];
            int exclusionRangeIndex = 0;
            int exclusionRangeLimitIndex = 0;
            for (int i = 0; i < exclusions.length; i++) {
                int[] componentRanges = exclusions[i];
                for (int j = 0; j < componentRanges.length; ) {
                    int value = componentRanges[j];
                    exclusionRanges[exclusionRangeIndex++] = componentRanges[j++];
                    exclusionRanges[exclusionRangeIndex++] = componentRanges[j++];
                }
                exclusionRangeLimits[i] = exclusionRangeIndex;
            }
            for (int styleIndex = 0; styleIndex < NUM_STYLES; styleIndex++) {
                int maxComponentFontCount = compFontNameIDs[fontIndex][styleIndex].length;
                boolean sawDefaultFontFile = false;
                if (installedFallbackFontFiles != null) {
                    maxComponentFontCount += installedFallbackFontFiles.length;
                }
                String faceName = fontName + "." + styleNames[styleIndex];
                String[] componentFaceNames = new String[maxComponentFontCount];
                String[] componentFileNames = new String[maxComponentFontCount];
                int index;
                for (index = 0; index < compFontNameIDs[fontIndex][styleIndex].length; index++) {
                    short fontNameID = compFontNameIDs[fontIndex][styleIndex][index];
                    short fileNameID = getComponentFileID(fontNameID);
                    componentFaceNames[index] = getFaceNameFromComponentFontName(getComponentFontName(fontNameID));
                    componentFileNames[index] = mapFileName(getComponentFileName(fileNameID));
                    if (componentFileNames[index] == null ||
                        needToSearchForFile(componentFileNames[index])) {
                        componentFileNames[index] = getFileNameFromComponentFontName(getComponentFontName(fontNameID));
                    }
                    if (!sawDefaultFontFile &&
                        defaultFontFile.equals(componentFileNames[index])) {
                        sawDefaultFontFile = true;
                    }
                }
                if (!sawDefaultFontFile) {
                    int len = 0;
                    if (installedFallbackFontFiles != null) {
                        len = installedFallbackFontFiles.length;
                    }
                    if (index + len == maxComponentFontCount) {
                        String[] newComponentFaceNames = new String[maxComponentFontCount + 1];
                        System.arraycopy(componentFaceNames, 0, newComponentFaceNames, 0, index);
                        componentFaceNames = newComponentFaceNames;
                        String[] newComponentFileNames = new String[maxComponentFontCount + 1];
                        System.arraycopy(componentFileNames, 0, newComponentFileNames, 0, index);
                        componentFileNames = newComponentFileNames;
                    }
                    componentFaceNames[index] = defaultFontFaceName;
                    componentFileNames[index] = defaultFontFile;
                    index++;
                }
                if (installedFallbackFontFiles != null) {
                    for (int ifb=0; ifb<installedFallbackFontFiles.length; ifb++) {
                        componentFaceNames[index] = null;
                        componentFileNames[index] = installedFallbackFontFiles[ifb];
                        index++;
                    }
                }
                if (index < maxComponentFontCount) {
                    String[] newComponentFaceNames = new String[index];
                    System.arraycopy(componentFaceNames, 0, newComponentFaceNames, 0, index);
                    componentFaceNames = newComponentFaceNames;
                    String[] newComponentFileNames = new String[index];
                    System.arraycopy(componentFileNames, 0, newComponentFileNames, 0, index);
                    componentFileNames = newComponentFileNames;
                }
                int[] clippedExclusionRangeLimits = exclusionRangeLimits;
                if (index != clippedExclusionRangeLimits.length) {
                    int len = exclusionRangeLimits.length;
                    clippedExclusionRangeLimits = new int[index];
                    System.arraycopy(exclusionRangeLimits, 0, clippedExclusionRangeLimits, 0, len);
                    for (int i = len; i < index; i++) {
                        clippedExclusionRangeLimits[i] = exclusionRanges.length;
                    }
                }
                result[fontIndex * NUM_STYLES + styleIndex]
                        = new CompositeFontDescriptor(
                            faceName,
                            compCoreNum[fontIndex],
                            componentFaceNames,
                            componentFileNames,
                            exclusionRanges,
                            clippedExclusionRangeLimits);
            }
        }
        return result;
    }
    protected abstract String getFaceNameFromComponentFontName(String componentFontName);
    protected abstract String getFileNameFromComponentFontName(String componentFontName);
    HashMap<String, Boolean> existsMap;
    public boolean needToSearchForFile(String fileName) {
        if (!FontUtilities.isLinux) {
            return false;
        } else if (existsMap == null) {
           existsMap = new HashMap<String, Boolean>();
        }
        Boolean exists = existsMap.get(fileName);
        if (exists == null) {
            getNumberCoreFonts();
            if (!coreFontFileNames.contains(fileName)) {
                exists = Boolean.TRUE;
            } else {
                exists = Boolean.valueOf((new File(fileName)).exists());
                existsMap.put(fileName, exists);
                if (FontUtilities.debugFonts() &&
                    exists == Boolean.FALSE) {
                    logger.warning("Couldn't locate font file " + fileName);
                }
            }
        }
        return exists == Boolean.FALSE;
    }
    private int numCoreFonts = -1;
    private String[] componentFonts = null;
    HashMap <String, String> filenamesMap = new HashMap<String, String>();
    HashSet <String> coreFontFileNames = new HashSet<String>();
    public int getNumberCoreFonts() {
        if (numCoreFonts == -1) {
            numCoreFonts = coreFontNameIDs.size();
            Short[] emptyShortArray = new Short[0];
            Short[] core = coreFontNameIDs.toArray(emptyShortArray);
            Short[] fallback = fallbackFontNameIDs.toArray(emptyShortArray);
            int numFallbackFonts = 0;
            int i;
            for (i = 0; i < fallback.length; i++) {
                if (coreFontNameIDs.contains(fallback[i])) {
                    fallback[i] = null;
                    continue;
                }
                numFallbackFonts++;
            }
            componentFonts = new String[numCoreFonts + numFallbackFonts];
            String filename = null;
            for (i = 0; i < core.length; i++) {
                short fontid = core[i];
                short fileid = getComponentFileID(fontid);
                componentFonts[i] = getComponentFontName(fontid);
                String compFileName = getComponentFileName(fileid);
                if (compFileName != null) {
                    coreFontFileNames.add(compFileName);
                }
                filenamesMap.put(componentFonts[i], mapFileName(compFileName));
            }
            for (int j = 0; j < fallback.length; j++) {
                if (fallback[j] != null) {
                    short fontid = fallback[j];
                    short fileid = getComponentFileID(fontid);
                    componentFonts[i] = getComponentFontName(fontid);
                    filenamesMap.put(componentFonts[i],
                                     mapFileName(getComponentFileName(fileid)));
                    i++;
                }
            }
        }
        return numCoreFonts;
    }
    public String[] getPlatformFontNames() {
        if (numCoreFonts == -1) {
            getNumberCoreFonts();
        }
        return componentFonts;
    }
    public String getFileNameFromPlatformName(String platformName) {
        return filenamesMap.get(platformName);
    }
    public String getExtraFontPath() {
        return getString(head[INDEX_appendedfontpath]);
    }
    public String getVersion() {
        return getString(head[INDEX_version]);
    }
    protected static FontConfiguration getFontConfiguration() {
        return fontConfig;
    }
    protected void setFontConfiguration() {
        fontConfig = this;      
    }
    private static final int HEAD_LENGTH = 20;
    private static final int INDEX_scriptIDs = 0;
    private static final int INDEX_scriptFonts = 1;
    private static final int INDEX_elcIDs = 2;
    private static final int INDEX_sequences = 3;
    private static final int INDEX_fontfileNameIDs = 4;
    private static final int INDEX_componentFontNameIDs = 5;
    private static final int INDEX_filenames = 6;
    private static final int INDEX_awtfontpaths = 7;
    private static final int INDEX_exclusions = 8;
    private static final int INDEX_proportionals = 9;
    private static final int INDEX_scriptFontsMotif = 10;
    private static final int INDEX_alphabeticSuffix = 11;
    private static final int INDEX_stringIDs = 12;
    private static final int INDEX_stringTable = 13;
    private static final int INDEX_TABLEEND = 14;
    private static final int INDEX_fallbackScripts = 15;
    private static final int INDEX_appendedfontpath = 16;
    private static final int INDEX_version = 17;
    private static short[] head;
    private static short[] table_scriptIDs;
    private static short[] table_scriptFonts;
    private static short[] table_elcIDs;
    private static short[] table_sequences;
    private static short[] table_fontfileNameIDs;
    private static short[] table_componentFontNameIDs;
    private static short[] table_filenames;
    protected static short[] table_awtfontpaths;
    private static short[] table_exclusions;
    private static short[] table_proportionals;
    private static short[] table_scriptFontsMotif;
    private static short[] table_alphabeticSuffix;
    private static short[] table_stringIDs;
    private static char[]  table_stringTable;
    private static void sanityCheck() {
        int errors = 0;
        String osName = (String)java.security.AccessController.doPrivileged(
                            new java.security.PrivilegedAction() {
            public Object run() {
                return System.getProperty("os.name");
            }
        });
        for (int ii = 1; ii < table_filenames.length; ii++) {
            if (table_filenames[ii] == -1) {
                if (osName.contains("Windows")) {
                    System.err.println("\n Error: <filename."
                                       + getString(table_componentFontNameIDs[ii])
                                       + "> entry is missing!!!");
                    errors++;
                } else {
                    if (verbose && !isEmpty(table_filenames)) {
                        System.err.println("\n Note: 'filename' entry is undefined for \""
                                           + getString(table_componentFontNameIDs[ii])
                                           + "\"");
                    }
                }
            }
        }
        for (int ii = 0; ii < table_scriptIDs.length; ii++) {
            short fid = table_scriptFonts[ii];
            if (fid == 0) {
                System.out.println("\n Error: <allfonts."
                                   + getString(table_scriptIDs[ii])
                                   + "> entry is missing!!!");
                errors++;
                continue;
            } else if (fid < 0) {
                fid = (short)-fid;
                for (int iii = 0; iii < NUM_FONTS; iii++) {
                    for (int iij = 0; iij < NUM_STYLES; iij++) {
                        int jj = iii * NUM_STYLES + iij;
                        short ffid = table_scriptFonts[fid + jj];
                        if (ffid == 0) {
                            System.err.println("\n Error: <"
                                           + getFontName(iii) + "."
                                           + getStyleName(iij) + "."
                                           + getString(table_scriptIDs[ii])
                                           + "> entry is missing!!!");
                            errors++;
                        }
                    }
                }
            }
        }
        if ("SunOS".equals(osName)) {
            for (int ii = 0; ii < table_awtfontpaths.length; ii++) {
                if (table_awtfontpaths[ii] == 0) {
                    String script = getString(table_scriptIDs[ii]);
                    if (script.contains("lucida") ||
                        script.contains("dingbats") ||
                        script.contains("symbol")) {
                        continue;
                    }
                    System.err.println("\nError: "
                                       + "<awtfontpath."
                                       + script
                                       + "> entry is missing!!!");
                    errors++;
                }
            }
        }
        if (errors != 0) {
            System.err.println("!!THERE ARE " + errors + " ERROR(S) IN "
                               + "THE FONTCONFIG FILE, PLEASE CHECK ITS CONTENT!!\n");
            System.exit(1);
        }
    }
    private static boolean isEmpty(short[] a) {
        for (short s : a) {
            if (s != -1) {
                return false;
            }
        }
        return true;
    }
    private static void dump() {
        System.out.println("\n----Head Table------------");
        for (int ii = 0; ii < HEAD_LENGTH; ii++) {
            System.out.println("  " + ii + " : " + head[ii]);
        }
        System.out.println("\n----scriptIDs-------------");
        printTable(table_scriptIDs, 0);
        System.out.println("\n----scriptFonts----------------");
        for (int ii = 0; ii < table_scriptIDs.length; ii++) {
            short fid = table_scriptFonts[ii];
            if (fid >= 0) {
                System.out.println("  allfonts."
                                   + getString(table_scriptIDs[ii])
                                   + "="
                                   + getString(table_componentFontNameIDs[fid]));
            }
        }
        for (int ii = 0; ii < table_scriptIDs.length; ii++) {
            short fid = table_scriptFonts[ii];
            if (fid < 0) {
                fid = (short)-fid;
                for (int iii = 0; iii < NUM_FONTS; iii++) {
                    for (int iij = 0; iij < NUM_STYLES; iij++) {
                        int jj = iii * NUM_STYLES + iij;
                        short ffid = table_scriptFonts[fid + jj];
                        System.out.println("  "
                                           + getFontName(iii) + "."
                                           + getStyleName(iij) + "."
                                           + getString(table_scriptIDs[ii])
                                           + "="
                                           + getString(table_componentFontNameIDs[ffid]));
                    }
                }
            }
        }
        System.out.println("\n----elcIDs----------------");
        printTable(table_elcIDs, 0);
        System.out.println("\n----sequences-------------");
        for (int ii = 0; ii< table_elcIDs.length; ii++) {
            System.out.println("  " + ii + "/" + getString((short)table_elcIDs[ii]));
            short[] ss = getShortArray(table_sequences[ii * NUM_FONTS + 0]);
            for (int jj = 0; jj < ss.length; jj++) {
                System.out.println("     " + getString((short)table_scriptIDs[ss[jj]]));
            }
        }
        System.out.println("\n----fontfileNameIDs-------");
        printTable(table_fontfileNameIDs, 0);
        System.out.println("\n----componentFontNameIDs--");
        printTable(table_componentFontNameIDs, 1);
        System.out.println("\n----filenames-------------");
        for (int ii = 0; ii < table_filenames.length; ii++) {
            if (table_filenames[ii] == -1) {
                System.out.println("  " + ii + " : null");
            } else {
                System.out.println("  " + ii + " : "
                   + getString(table_fontfileNameIDs[table_filenames[ii]]));
            }
        }
        System.out.println("\n----awtfontpaths---------");
        for (int ii = 0; ii < table_awtfontpaths.length; ii++) {
            System.out.println("  " + getString(table_scriptIDs[ii])
                               + " : "
                               + getString(table_awtfontpaths[ii]));
        }
        System.out.println("\n----proportionals--------");
        for (int ii = 0; ii < table_proportionals.length; ii++) {
            System.out.println("  "
                   + getString((short)table_componentFontNameIDs[table_proportionals[ii++]])
                   + " -> "
                   + getString((short)table_componentFontNameIDs[table_proportionals[ii]]));
        }
        int i = 0;
        System.out.println("\n----alphabeticSuffix----");
        while (i < table_alphabeticSuffix.length) {
          System.out.println("    " + getString(table_elcIDs[table_alphabeticSuffix[i++]])
                             + " -> " + getString(table_alphabeticSuffix[i++]));
        }
        System.out.println("\n----String Table---------");
        System.out.println("    stringID:    Num =" + table_stringIDs.length);
        System.out.println("    stringTable: Size=" + table_stringTable.length * 2);
        System.out.println("\n----fallbackScriptIDs---");
        short[] fbsIDs = getShortArray(head[INDEX_fallbackScripts]);
        for (int ii = 0; ii < fbsIDs.length; ii++) {
          System.out.println("  " + getString(table_scriptIDs[fbsIDs[ii]]));
        }
        System.out.println("\n----appendedfontpath-----");
        System.out.println("  " + getString(head[INDEX_appendedfontpath]));
        System.out.println("\n----Version--------------");
        System.out.println("  " + getString(head[INDEX_version]));
    }
    protected static short getComponentFontID(short scriptID, int fontIndex, int styleIndex) {
        short fid = table_scriptFonts[scriptID];
        if (fid >= 0) {
            return fid;
        } else {
            return table_scriptFonts[-fid + fontIndex * NUM_STYLES + styleIndex];
        }
    }
    protected static short getComponentFontIDMotif(short scriptID, int fontIndex, int styleIndex) {
        if (table_scriptFontsMotif.length == 0) {
            return 0;
        }
        short fid = table_scriptFontsMotif[scriptID];
        if (fid >= 0) {
            return fid;
        } else {
            return table_scriptFontsMotif[-fid + fontIndex * NUM_STYLES + styleIndex];
        }
    }
    private static int[] getExclusionRanges(short scriptID) {
        short exID = table_exclusions[scriptID];
        if (exID == 0) {
            return EMPTY_INT_ARRAY;
        } else {
            char[] exChar = getString(exID).toCharArray();
            int[] exInt = new int[exChar.length / 2];
            int i = 0;
            for (int j = 0; j < exInt.length; j++) {
                exInt[j] = (exChar[i++] << 16) + (exChar[i++] & 0xffff);
            }
            return exInt;
        }
    }
    private static boolean contains(short IDs[], short id, int limit) {
        for (int i = 0; i < limit; i++) {
            if (IDs[i] == id) {
                return true;
            }
        }
        return false;
    }
    protected static String getComponentFontName(short id) {
        if (id < 0) {
            return null;
        }
        return getString(table_componentFontNameIDs[id]);
    }
    private static String getComponentFileName(short id) {
        if (id < 0) {
            return null;
        }
        return getString(table_fontfileNameIDs[id]);
    }
    private static short getComponentFileID(short nameID) {
        return table_filenames[nameID];
    }
    private static String getScriptName(short scriptID) {
        return getString(table_scriptIDs[scriptID]);
    }
   private HashMap<String, Short> reorderScripts;
   protected short[] getCoreScripts(int fontIndex) {
        short elc = getInitELC();
        short[] scripts = getShortArray(table_sequences[elc * NUM_FONTS + fontIndex]);
        if (preferLocaleFonts) {
            if (reorderScripts == null) {
                reorderScripts = new HashMap<String, Short>();
            }
            String[] ss = new String[scripts.length];
            for (int i = 0; i < ss.length; i++) {
                ss[i] = getScriptName(scripts[i]);
                reorderScripts.put(ss[i], scripts[i]);
            }
            reorderSequenceForLocale(ss);
            for (int i = 0; i < ss.length; i++) {
                scripts[i] = reorderScripts.get(ss[i]);
            }
        }
         return scripts;
    }
    private static short[] getFallbackScripts() {
        return getShortArray(head[INDEX_fallbackScripts]);
    }
    private static void printTable(short[] list, int start) {
        for (int i = start; i < list.length; i++) {
            System.out.println("  " + i + " : " + getString(list[i]));
        }
    }
    private static short[] readShortTable(DataInputStream in, int len )
        throws IOException {
        if (len == 0) {
            return EMPTY_SHORT_ARRAY;
        }
        short[] data = new short[len];
        byte[] bb = new byte[len * 2];
        in.read(bb);
        int i = 0,j = 0;
        while (i < len) {
            data[i++] = (short)(bb[j++] << 8 | (bb[j++] & 0xff));
        }
        return data;
    }
    private static void writeShortTable(DataOutputStream out, short[] data)
        throws IOException {
        for (short val : data) {
            out.writeShort(val);
        }
    }
    private static short[] toList(HashMap<String, Short> map) {
        short[] list = new short[map.size()];
        Arrays.fill(list, (short) -1);
        for (Entry<String, Short> entry : map.entrySet()) {
            list[entry.getValue()] = getStringID(entry.getKey());
        }
        return list;
    }
    private static String[] stringCache;
    protected static String getString(short stringID) {
        if (stringID == 0)
            return null;
        if (stringCache[stringID] == null){
            stringCache[stringID] =
              new String (table_stringTable,
                          table_stringIDs[stringID],
                          table_stringIDs[stringID+1] - table_stringIDs[stringID]);
        }
        return stringCache[stringID];
    }
    private static short[] getShortArray(short shortArrayID) {
        String s = getString(shortArrayID);
        char[] cc = s.toCharArray();
        short[] ss = new short[cc.length];
        for (int i = 0; i < cc.length; i++) {
            ss[i] = (short)(cc[i] & 0xffff);
        }
        return ss;
    }
    private static short getStringID(String s) {
        if (s == null) {
            return (short)0;
        }
        short pos0 = (short)stringTable.length();
        stringTable.append(s);
        short pos1 = (short)stringTable.length();
        stringIDs[stringIDNum] = pos0;
        stringIDs[stringIDNum + 1] = pos1;
        stringIDNum++;
        if (stringIDNum + 1 >= stringIDs.length) {
            short[] tmp = new short[stringIDNum + 1000];
            System.arraycopy(stringIDs, 0, tmp, 0, stringIDNum);
            stringIDs = tmp;
        }
        return (short)(stringIDNum - 1);
    }
    private static short getShortArrayID(short sa[]) {
        char[] cc = new char[sa.length];
        for (int i = 0; i < sa.length; i ++) {
            cc[i] = (char)sa[i];
        }
        String s = new String(cc);
        return getStringID(s);
    }
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final short[] EMPTY_SHORT_ARRAY = new short[0];
    private static final String UNDEFINED_COMPONENT_FONT = "unknown";
    static class PropertiesHandler {
        public void load(InputStream in) throws IOException {
            initLogicalNameStyle();
            initHashMaps();
            FontProperties fp = new FontProperties();
            fp.load(in);
            initBinaryTable();
        }
        private void initBinaryTable() {
            head = new short[HEAD_LENGTH];
            head[INDEX_scriptIDs] = (short)HEAD_LENGTH;
            table_scriptIDs = toList(scriptIDs);
            head[INDEX_scriptFonts] = (short)(head[INDEX_scriptIDs]  + table_scriptIDs.length);
            int len = table_scriptIDs.length + scriptFonts.size() * 20;
            table_scriptFonts = new short[len];
            for (Entry<Short, Short> entry : scriptAllfonts.entrySet()) {
                table_scriptFonts[entry.getKey().intValue()] = entry.getValue();
            }
            int off = table_scriptIDs.length;
            for (Entry<Short, Short[]> entry : scriptFonts.entrySet()) {
                table_scriptFonts[entry.getKey().intValue()] = (short)-off;
                Short[] v = entry.getValue();
                for (int i = 0; i < 20; i++) {
                    if (v[i] != null) {
                        table_scriptFonts[off++] = v[i];
                    } else {
                        table_scriptFonts[off++] = 0;
                    }
                }
            }
            head[INDEX_elcIDs] = (short)(head[INDEX_scriptFonts]  + table_scriptFonts.length);
            table_elcIDs = toList(elcIDs);
            head[INDEX_sequences] = (short)(head[INDEX_elcIDs]  + table_elcIDs.length);
            table_sequences = new short[elcIDs.size() * NUM_FONTS];
            for (Entry<Short, short[]> entry : sequences.entrySet()) {
                int k = entry.getKey().intValue();
                short[] v = entry.getValue();
                if (v.length == 1) {
                    for (int i = 0; i < NUM_FONTS; i++) {
                        table_sequences[k * NUM_FONTS + i] = v[0];
                    }
                } else {
                    for (int i = 0; i < NUM_FONTS; i++) {
                        table_sequences[k * NUM_FONTS + i] = v[i];
                    }
                }
            }
            head[INDEX_fontfileNameIDs] = (short)(head[INDEX_sequences]  + table_sequences.length);
            table_fontfileNameIDs = toList(fontfileNameIDs);
            head[INDEX_componentFontNameIDs] = (short)(head[INDEX_fontfileNameIDs]  + table_fontfileNameIDs.length);
            table_componentFontNameIDs = toList(componentFontNameIDs);
            head[INDEX_filenames] = (short)(head[INDEX_componentFontNameIDs]  + table_componentFontNameIDs.length);
            table_filenames = new short[table_componentFontNameIDs.length];
            Arrays.fill(table_filenames, (short) -1);
            for (Entry<Short, Short> entry : filenames.entrySet()) {
                table_filenames[entry.getKey()] = entry.getValue();
            }
            head[INDEX_awtfontpaths] = (short)(head[INDEX_filenames]  + table_filenames.length);
            table_awtfontpaths = new short[table_scriptIDs.length];
            for (Entry<Short, Short> entry : awtfontpaths.entrySet()) {
                table_awtfontpaths[entry.getKey()] = entry.getValue();
            }
            head[INDEX_exclusions] = (short)(head[INDEX_awtfontpaths]  + table_awtfontpaths.length);
            table_exclusions = new short[scriptIDs.size()];
            for (Entry<Short, int[]> entry : exclusions.entrySet()) {
                int[] exI = entry.getValue();
                char[] exC = new char[exI.length * 2];
                int j = 0;
                for (int i = 0; i < exI.length; i++) {
                    exC[j++] = (char) (exI[i] >> 16);
                    exC[j++] = (char) (exI[i] & 0xffff);
                }
                table_exclusions[entry.getKey()] = getStringID(new String (exC));
            }
            head[INDEX_proportionals] = (short)(head[INDEX_exclusions]  + table_exclusions.length);
            table_proportionals = new short[proportionals.size() * 2];
            int j = 0;
            for (Entry<Short, Short> entry : proportionals.entrySet()) {
                table_proportionals[j++] = entry.getKey();
                table_proportionals[j++] = entry.getValue();
            }
            head[INDEX_scriptFontsMotif] = (short)(head[INDEX_proportionals] + table_proportionals.length);
            if (scriptAllfontsMotif.size() != 0 || scriptFontsMotif.size() != 0) {
                len = table_scriptIDs.length + scriptFontsMotif.size() * 20;
                table_scriptFontsMotif = new short[len];
                for (Entry<Short, Short> entry : scriptAllfontsMotif.entrySet()) {
                    table_scriptFontsMotif[entry.getKey().intValue()] =
                      (short)entry.getValue();
                }
                off = table_scriptIDs.length;
                for (Entry<Short, Short[]> entry : scriptFontsMotif.entrySet()) {
                    table_scriptFontsMotif[entry.getKey().intValue()] = (short)-off;
                    Short[] v = entry.getValue();
                    int i = 0;
                    while (i < 20) {
                        if (v[i] != null) {
                            table_scriptFontsMotif[off++] = v[i];
                        } else {
                            table_scriptFontsMotif[off++] = 0;
                        }
                        i++;
                    }
                }
            } else {
                table_scriptFontsMotif = EMPTY_SHORT_ARRAY;
            }
            head[INDEX_alphabeticSuffix] = (short)(head[INDEX_scriptFontsMotif] + table_scriptFontsMotif.length);
            table_alphabeticSuffix = new short[alphabeticSuffix.size() * 2];
            j = 0;
            for (Entry<Short, Short> entry : alphabeticSuffix.entrySet()) {
                table_alphabeticSuffix[j++] = entry.getKey();
                table_alphabeticSuffix[j++] = entry.getValue();
            }
            head[INDEX_fallbackScripts] = getShortArrayID(fallbackScriptIDs);
            head[INDEX_appendedfontpath] = getStringID(appendedfontpath);
            head[INDEX_version] = getStringID(version);
            head[INDEX_stringIDs] = (short)(head[INDEX_alphabeticSuffix] + table_alphabeticSuffix.length);
            table_stringIDs = new short[stringIDNum + 1];
            System.arraycopy(stringIDs, 0, table_stringIDs, 0, stringIDNum + 1);
            head[INDEX_stringTable] = (short)(head[INDEX_stringIDs] + stringIDNum + 1);
            table_stringTable = stringTable.toString().toCharArray();
            head[INDEX_TABLEEND] = (short)(head[INDEX_stringTable] + stringTable.length());
            stringCache = new String[table_stringIDs.length];
        }
        private HashMap<String, Short> scriptIDs;
        private HashMap<String, Short> elcIDs;
        private HashMap<String, Short> componentFontNameIDs;
        private HashMap<String, Short> fontfileNameIDs;
        private HashMap<String, Integer> logicalFontIDs;
        private HashMap<String, Integer> fontStyleIDs;
        private HashMap<Short, Short>  filenames;
        private HashMap<Short, short[]> sequences;
        private HashMap<Short, Short[]> scriptFonts;
        private HashMap<Short, Short> scriptAllfonts;
        private HashMap<Short, int[]> exclusions;
        private HashMap<Short, Short> awtfontpaths;
        private HashMap<Short, Short> proportionals;
        private HashMap<Short, Short> scriptAllfontsMotif;
        private HashMap<Short, Short[]> scriptFontsMotif;
        private HashMap<Short, Short> alphabeticSuffix;
        private short[] fallbackScriptIDs;
        private String version;
        private String appendedfontpath;
        private void initLogicalNameStyle() {
            logicalFontIDs = new HashMap<String, Integer>();
            fontStyleIDs = new HashMap<String, Integer>();
            logicalFontIDs.put("serif",      0);
            logicalFontIDs.put("sansserif",  1);
            logicalFontIDs.put("monospaced", 2);
            logicalFontIDs.put("dialog",     3);
            logicalFontIDs.put("dialoginput",4);
            fontStyleIDs.put("plain",      0);
            fontStyleIDs.put("bold",       1);
            fontStyleIDs.put("italic",     2);
            fontStyleIDs.put("bolditalic", 3);
        }
        private void initHashMaps() {
            scriptIDs = new HashMap<String, Short>();
            elcIDs = new HashMap<String, Short>();
            componentFontNameIDs = new HashMap<String, Short>();
            componentFontNameIDs.put("", Short.valueOf((short)0));
            fontfileNameIDs = new HashMap<String, Short>();
            filenames = new HashMap<Short, Short>();
            sequences = new HashMap<Short, short[]>();
            scriptFonts = new HashMap<Short, Short[]>();
            scriptAllfonts = new HashMap<Short, Short>();
            exclusions = new HashMap<Short, int[]>();
            awtfontpaths = new HashMap<Short, Short>();
            proportionals = new HashMap<Short, Short>();
            scriptFontsMotif = new HashMap<Short, Short[]>();
            scriptAllfontsMotif = new HashMap<Short, Short>();
            alphabeticSuffix = new HashMap<Short, Short>();
            fallbackScriptIDs = EMPTY_SHORT_ARRAY;
        }
        private int[] parseExclusions(String key, String exclusions) {
            if (exclusions == null) {
                return EMPTY_INT_ARRAY;
            }
            int numExclusions = 1;
            int pos = 0;
            while ((pos = exclusions.indexOf(',', pos)) != -1) {
                numExclusions++;
                pos++;
            }
            int[] exclusionRanges = new int[numExclusions * 2];
            pos = 0;
            int newPos = 0;
            for (int j = 0; j < numExclusions * 2; ) {
                String lower, upper;
                int lo = 0, up = 0;
                try {
                    newPos = exclusions.indexOf('-', pos);
                    lower = exclusions.substring(pos, newPos);
                    pos = newPos + 1;
                    newPos = exclusions.indexOf(',', pos);
                    if (newPos == -1) {
                        newPos = exclusions.length();
                    }
                    upper = exclusions.substring(pos, newPos);
                    pos = newPos + 1;
                    int lowerLength = lower.length();
                    int upperLength = upper.length();
                    if (lowerLength != 4 && lowerLength != 6
                        || upperLength != 4 && upperLength != 6) {
                        throw new Exception();
                    }
                    lo = Integer.parseInt(lower, 16);
                    up = Integer.parseInt(upper, 16);
                    if (lo > up) {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    if (FontUtilities.debugFonts() &&
                        logger != null) {
                        logger.config("Failed parsing " + key +
                                  " property of font configuration.");
                    }
                    return EMPTY_INT_ARRAY;
                }
                exclusionRanges[j++] = lo;
                exclusionRanges[j++] = up;
            }
            return exclusionRanges;
        }
        private Short getID(HashMap<String, Short> map, String key) {
            Short ret = map.get(key);
            if ( ret == null) {
                map.put(key, (short)map.size());
                return map.get(key);
            }
            return ret;
        }
        class FontProperties extends Properties {
            public synchronized Object put(Object k, Object v) {
                parseProperty((String)k, (String)v);
                return null;
            }
        }
        private void parseProperty(String key, String value) {
            if (key.startsWith("filename.")) {
                key = key.substring(9);
                if (!"MingLiU_HKSCS".equals(key)) {
                    key = key.replace('_', ' ');
                }
                Short faceID = getID(componentFontNameIDs, key);
                Short fileID = getID(fontfileNameIDs, value);
                filenames.put(faceID, fileID);
            } else if (key.startsWith("exclusion.")) {
                key = key.substring(10);
                exclusions.put(getID(scriptIDs,key), parseExclusions(key,value));
            } else if (key.startsWith("sequence.")) {
                key = key.substring(9);
                boolean hasDefault = false;
                boolean has1252 = false;
                String[] ss = (String[])splitSequence(value).toArray(EMPTY_STRING_ARRAY);
                short [] sa = new short[ss.length];
                for (int i = 0; i < ss.length; i++) {
                    if ("alphabetic/default".equals(ss[i])) {
                        ss[i] = "alphabetic";
                        hasDefault = true;
                    } else if ("alphabetic/1252".equals(ss[i])) {
                        ss[i] = "alphabetic";
                        has1252 = true;
                    }
                    sa[i] = getID(scriptIDs, ss[i]).shortValue();
                }
                short scriptArrayID = getShortArrayID(sa);
                Short elcID = null;
                int dot = key.indexOf('.');
                if (dot == -1) {
                    if ("fallback".equals(key)) {
                        fallbackScriptIDs = sa;
                        return;
                    }
                    if ("allfonts".equals(key)) {
                        elcID = getID(elcIDs, "NULL.NULL.NULL");
                    } else {
                        if (logger != null) {
                            logger.config("Error sequence def: <sequence." + key + ">");
                        }
                        return;
                    }
                } else {
                    elcID = getID(elcIDs, key.substring(dot + 1));
                    key = key.substring(0, dot);
                }
                short[] scriptArrayIDs = null;
                if ("allfonts".equals(key)) {
                    scriptArrayIDs = new short[1];
                    scriptArrayIDs[0] = scriptArrayID;
                } else {
                    scriptArrayIDs = sequences.get(elcID);
                    if (scriptArrayIDs == null) {
                       scriptArrayIDs = new short[5];
                    }
                    Integer fid = logicalFontIDs.get(key);
                    if (fid == null) {
                        if (logger != null) {
                            logger.config("Unrecognizable logicfont name " + key);
                        }
                        return;
                    }
                    scriptArrayIDs[fid.intValue()] = scriptArrayID;
                }
                sequences.put(elcID, scriptArrayIDs);
                if (hasDefault) {
                    alphabeticSuffix.put(elcID, getStringID("default"));
                } else
                if (has1252) {
                    alphabeticSuffix.put(elcID, getStringID("1252"));
                }
            } else if (key.startsWith("allfonts.")) {
                key = key.substring(9);
                if (key.endsWith(".motif")) {
                    key = key.substring(0, key.length() - 6);
                    scriptAllfontsMotif.put(getID(scriptIDs,key), getID(componentFontNameIDs,value));
                } else {
                    scriptAllfonts.put(getID(scriptIDs,key), getID(componentFontNameIDs,value));
                }
            } else if (key.startsWith("awtfontpath.")) {
                key = key.substring(12);
                awtfontpaths.put(getID(scriptIDs, key), getStringID(value));
            } else if ("version".equals(key)) {
                version = value;
            } else if ("appendedfontpath".equals(key)) {
                appendedfontpath = value;
            } else if (key.startsWith("proportional.")) {
                key = key.substring(13).replace('_', ' ');
                proportionals.put(getID(componentFontNameIDs, key),
                                  getID(componentFontNameIDs, value));
            } else {
                int dot1, dot2;
                boolean isMotif = false;
                dot1 = key.indexOf('.');
                if (dot1 == -1) {
                    if (logger != null) {
                        logger.config("Failed parsing " + key +
                                  " property of font configuration.");
                    }
                    return;
                }
                dot2 = key.indexOf('.', dot1 + 1);
                if (dot2 == -1) {
                    if (logger != null) {
                        logger.config("Failed parsing " + key +
                                  " property of font configuration.");
                    }
                    return;
                }
                if (key.endsWith(".motif")) {
                    key = key.substring(0, key.length() - 6);
                    isMotif = true;
                }
                Integer nameID = logicalFontIDs.get(key.substring(0, dot1));
                Integer styleID = fontStyleIDs.get(key.substring(dot1+1, dot2));
                Short scriptID = getID(scriptIDs, key.substring(dot2 + 1));
                if (nameID == null || styleID == null) {
                    if (logger != null) {
                        logger.config("unrecognizable logicfont name/style at " + key);
                    }
                    return;
                }
                Short[] pnids;
                if (isMotif) {
                    pnids = scriptFontsMotif.get(scriptID);
                } else {
                    pnids = scriptFonts.get(scriptID);
                }
                if (pnids == null) {
                    pnids =  new Short[20];
                }
                pnids[nameID.intValue() * NUM_STYLES + styleID.intValue()]
                  = getID(componentFontNameIDs, value);
                if (isMotif) {
                    scriptFontsMotif.put(scriptID, pnids);
                } else {
                    scriptFonts.put(scriptID, pnids);
                }
            }
        }
    }
}
