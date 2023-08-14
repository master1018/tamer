public abstract class FontManager {
    boolean NOT_IMP = false;
    public String[] allFamilies;
    public static final String DEFAULT_NAME = "Default";  
    public static final String DIALOG_NAME = "Dialog";   
    public static final byte  FAMILY_NAME_ID  = 1;      
    public static final byte  FONT_NAME_ID  = 4;        
    public static final byte  POSTSCRIPT_NAME_ID = 6;   
    public static final short ENGLISH_LANGID = 0x0409;  
    public static final byte  FONT_TYPE_TT  = 4;        
    public static final byte  FONT_TYPE_T1  = 2;        
    public static final byte  FONT_TYPE_UNDEF  = 0;     
    static final int DIALOG = 3;        
    static final int SANSSERIF = 1;     
    static final int DIALOGINPUT = 4;   
    static final int MONOSPACED = 2;    
    static final int SERIF = 0;         
    public static final String PLATFORM_FONT_NAME = "PlatformFontName"; 
    public static final String LOGICAL_FONT_NAME = "LogicalFontName"; 
    public static final String COMPONENT_INDEX = "ComponentIndex"; 
    public static final String STYLE_INDEX = "StyleIndex"; 
    public static final String[] FONT_MAPPING_KEYS = {
            "LogicalFontName.StyleName.ComponentIndex", "LogicalFontName.ComponentIndex" 
    };
    public static final String FONT_CHARACTER_ENCODING = "fontcharset.LogicalFontName.ComponentIndex"; 
    public static final String EXCLUSION_RANGES = "exclusion.LogicalFontName.ComponentIndex"; 
    public static final String FONT_FILE_NAME = "filename.PlatformFontName"; 
    public static final String[] LOGICAL_FONT_FAMILIES = {
            "Serif", "SansSerif", "Monospaced", "Dialog", "DialogInput" 
    };
    public static final String[] LOGICAL_FONT_NAMES = {
            "serif", "serif.plain", "serif.bold", "serif.italic", "serif.bolditalic", 
            "sansserif", "sansserif.plain", "sansserif.bold", "sansserif.italic", "sansserif.bolditalic", 
            "monospaced", "monospaced.plain", "monospaced.bold", "monospaced.italic", "monospaced.bolditalic", 
            "dialog", "dialog.plain", "dialog.bold", "dialog.italic", "dialog.bolditalic", 
            "dialoginput", "dialoginput.plain", "dialoginput.bold", "dialoginput.italic", "dialoginput.bolditalic" 
    };
    public static final String[] LOGICAL_FONT_FACES = {
            "Serif", "Serif.plain", "Serif.bold", "Serif.italic", "Serif.bolditalic", 
            "Sansserif", "Sansserif.plain", "Sansserif.bold", "Sansserif.italic", "Sansserif.bolditalic", 
            "Monospaced", "Monospaced.plain", "Monospaced.bold", "Monospaced.italic", "Monospaced.bolditalic", 
            "Dialog", "Dialog.plain", "Dialog.bold", "Dialog.italic", "Dialog.bolditalic", 
            "Dialoginput", "Dialoginput.plain", "Dialoginput.bold", "Dialoginput.italic", "Dialoginput.bolditalic" 
    };
    public static final String[] STYLE_NAMES = {
            "plain", "bold", "italic", "bolditalic" 
    };
    private static final Hashtable<String, Integer> style_keys = new Hashtable<String, Integer>(4);
    static {
        for (int i = 0; i < STYLE_NAMES.length; i++){
            style_keys.put(STYLE_NAMES[i], Integer.valueOf(i));
        }
    }
    public static int getLogicalStyle(String lName){
        Integer value = style_keys.get(lName);
        return value != null ? value.intValue(): -1;
    }
    public static final String[] OS_VALUES = {
            "NT", "98", "2000", "Me", "XP", 
            "Redhat", "Turbo", "SuSE"       
    };
    public static final String[] FP_FILE_NAMES = {
            "/lib/font.properties.Language_Country_Encoding.OSVersion", 
            "/lib/font.properties.Language_Country_Encoding.OS", 
            "/lib/font.properties.Language_Country_Encoding.Version", 
            "/lib/font.properties.Language_Country_Encoding", 
            "/lib/font.properties.Language_Country.OSVersion", 
            "/lib/font.properties.Language_Country.OS", 
            "/lib/font.properties.Language_Country.Version", 
            "/lib/font.properties.Language_Country", 
            "/lib/font.properties.Language_Encoding.OSVersion", 
            "/lib/font.properties.Language_Encoding.OS", 
            "/lib/font.properties.Language_Encoding.Version", 
            "/lib/font.properties.Language_Encoding", 
            "/lib/font.properties.Language.OSVersion", 
            "/lib/font.properties.Language.OS", 
            "/lib/font.properties.Language.Version", 
            "/lib/font.properties.Language", 
            "/lib/font.properties.Encoding.OSVersion", 
            "/lib/font.properties.Encoding.OS", 
            "/lib/font.properties.Encoding.Version", 
            "/lib/font.properties.Encoding", 
            "/lib/font.properties.OSVersion", 
            "/lib/font.properties.OS", 
            "/lib/font.properties.Version", 
            "/lib/font.properties" 
    };
    public Hashtable<String, Vector<FontProperty>> fProperties = new Hashtable<String, Vector<FontProperty>>();
    public FontManager(){
        allFamilies = getAllFamilies();
    }
    public static final int EMPTY_FONTS_CAPACITY = 10;
    Hashtable<String, Short> tableLCID = new Hashtable<String, Short>();
    public Hashtable<String, HashMapReference> fontsTable = new Hashtable<String, HashMapReference>();
    public ReferenceQueue<FontPeer> queue = new ReferenceQueue<FontPeer>();
    public final static FontManager inst = CommonGraphics2DFactory.inst.getFontManager();
    public static FontManager getInstance() {
        return inst;
    }
    public FontPeer getFontPeer(String fontName, int _fontStyle, int size) {
        updateFontsTable();
        FontPeer peer = null;
        String key; 
        String name;
        int fontStyle = _fontStyle;
        int logicalIndex = getLogicalFaceIndex(fontName);
        if (logicalIndex != -1){
            name = getLogicalFaceFromFont(fontStyle, logicalIndex);
            fontStyle = getStyleFromLogicalFace(name);
            key = name.concat(String.valueOf(size));
        } else {
            name = fontName;
            key = name.concat(String.valueOf(fontStyle)).
                    concat(String.valueOf(size));
        }
        HashMapReference hmr   = fontsTable.get(key);
        if (hmr != null) {
            peer = hmr.get();
        }
        if (peer == null) {
            peer = createFontPeer(name, fontStyle, size, logicalIndex);
            if (peer == null){
                peer = getFontPeer(DIALOG_NAME, fontStyle, size);
            }
            fontsTable.put(key, new HashMapReference(key, peer, queue));
        }
        return peer;
    }
    private FontPeer createFontPeer(String name, int style, int size, int logicalIndex){
        FontPeer peer;
        if (logicalIndex != -1){
            peer = createLogicalFontPeer(name, style, size);
        }else {
            peer = createPhysicalFontPeer(name, style, size);
        }
        return peer;
    }
    public String getFamilyFromLogicalFace(String faceName){
        int pos = faceName.indexOf("."); 
        if (pos == -1){
            return faceName;
        }
        return faceName.substring(0, pos);
    }
    private FontPeer createLogicalFontPeer(String faceName, int style, int size){
        String family = getFamilyFromLogicalFace(faceName);
        FontProperty[] fps = getFontProperties(family.toLowerCase() + "." + style); 
        if (fps != null){
            int numFonts = fps.length;
            FontPeerImpl[] physicalFonts = new FontPeerImpl[numFonts];
            for (int i = 0; i < numFonts; i++){
                FontProperty fp = fps[i];
                String name = fp.getName();
                int fpStyle = fp.getStyle();
                String key = name.concat(String.valueOf(fpStyle)).
                    concat(String.valueOf(size));
                HashMapReference hmr   = fontsTable.get(key);
                if (hmr != null) {
                    physicalFonts[i] = (FontPeerImpl)hmr.get();
                }
                if (physicalFonts[i] == null){
                    physicalFonts[i] = (FontPeerImpl)createPhysicalFontPeer(name, fpStyle, size);
                    fontsTable.put(key, new HashMapReference(key, physicalFonts[i], queue));
                }
                if (physicalFonts[i] == null){
                    physicalFonts[i] = (FontPeerImpl)getDefaultFont(style, size);
                }
            }
            return new CompositeFont(family, faceName, style, size, fps, physicalFonts); 
        }
        FontPeerImpl peer = (FontPeerImpl)getDefaultFont(style, size);
        return peer;
    }
    public abstract FontPeer createPhysicalFontPeer(String name, int style, int size);
    public FontPeer getDefaultFont(int style, int size){
        updateFontsTable();
        FontPeer peer = null;
        String key = DEFAULT_NAME.concat(String.valueOf(style)).
                    concat(String.valueOf(size));
        HashMapReference hmr   = fontsTable.get(key);
        if (hmr != null) {
            peer = hmr.get();
        }
        if (peer == null) {
            peer = createDefaultFont(style, size);
            ((FontPeerImpl)peer).setFamily(DEFAULT_NAME);
            ((FontPeerImpl)peer).setPSName(DEFAULT_NAME);
            ((FontPeerImpl)peer).setFontName(DEFAULT_NAME);
            fontsTable.put(key, new HashMapReference(key, peer, queue));
        }
        return peer;
    }
    public abstract FontPeer createDefaultFont(int style, int size);
    public String getLogicalFaceFromFont(int fontStyle, int logicalIndex){
        int style = 0;
        String name = LOGICAL_FONT_FACES[logicalIndex];
        int pos = name.indexOf("."); 
        if (pos == -1){
            return createLogicalFace(name, fontStyle);
        }
        String styleName = name.substring(pos+1);
        name = name.substring(0, pos);
        style = fontStyle | getLogicalStyle(styleName);
        return createLogicalFace(name, style);
    }
    public int getStyleFromLogicalFace(String name){
        int style;
        int pos = name.indexOf("."); 
        if (pos == -1){
            return Font.PLAIN;
        }
        String styleName = name.substring(pos+1);
        style = getLogicalStyle(styleName);
        return style;
    }
    public String createLogicalFace(String family, int styleIndex){
        return family + "." + STYLE_NAMES[styleIndex]; 
    }
    public Short getLCID(Locale l){
        if (this.tableLCID.size() == 0){
            initLCIDTable();
        }
        return tableLCID.get(l.toString());
    }
    public abstract void initLCIDTable();
    private class DisposeNativeHook extends Thread {
        @Override
        public void run() {
            try{
                Enumeration<String> kEnum = fontsTable.keys();
                while(kEnum.hasMoreElements()){
                    Object key = kEnum.nextElement();
                    HashMapReference hmr = fontsTable.remove(key);
                    FontPeerImpl delPeer = (FontPeerImpl)hmr.get();
                    if ((delPeer != null) && (delPeer.getClass() != CompositeFont.class)){
                        delPeer.dispose();
                    }
                }
            } catch (Throwable t){
                throw new RuntimeException(t);
            }
        }
      }
    public File getTempFontFile()throws IOException{
        if(NOT_IMP)
            throw new NotImplementedException("getTempFontFile not Implemented");
        return null;
    }
    public static File getFontPropertyFile(){
        File file = null;
        String javaHome = System.getProperty("java.home"); 
        Locale l = Locale.getDefault();
        String language = l.getLanguage();
        String country = l.getCountry();
        String fileEncoding = System.getProperty("file.encoding"); 
        String os = System.getProperty("os.name"); 
        int i = 0;
        for (; i < OS_VALUES.length; i++){
            if (os.endsWith(OS_VALUES[i])){
                os = OS_VALUES[i];
                break;
            }
        }
        if (i == OS_VALUES.length){
            os = null;
        }
        String version = System.getProperty("os.version"); 
        String pathname;
        for (i = 0; i < FP_FILE_NAMES.length; i++){
            pathname = FP_FILE_NAMES[i];
            if (os != null){
                pathname = pathname.replaceFirst("OS", os); 
            }
            pathname = javaHome + pathname;
            pathname = pathname.replaceAll("Language", language). 
                                replaceAll("Country", country). 
                                replaceAll("Encoding", fileEncoding). 
                                replaceAll("Version", version); 
            file = new File(pathname);
            if (file.exists()){
                break;
            }
        }
        return file.exists() ? file : null;
    }
    public static int[] parseIntervals(String exclusionString){
        int[] results = null;
        if (exclusionString == null){
            return null;
        }
        String[] intervals = exclusionString.split(","); 
        if (intervals != null){
            int num = intervals.length;
            if (num > 0){
                results = new int[intervals.length << 1];
                for (int i = 0; i < intervals.length; i++){
                    String ranges[] = intervals[i].split("-"); 
                    results[i*2] = Integer.parseInt(ranges[0], 16);
                    results[i*2+1] = Integer.parseInt(ranges[1], 16);
                }
            }
        }
        return results;
    }
    public static Properties getProperties(File file){
        Properties props = null;
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(file);
            props = new Properties();
            props.load(fis);
        } catch (Exception e){
            System.out.println(e);
        }
        return props;
    }
    public FontProperty[] getFontProperties(String fpName){
        Vector<FontProperty> props = fProperties.get(fpName);
        if (props == null){
            return null;
        }
        int size =  props.size();
        if (size == 0){
            return null;
        }
        FontProperty[] fps = new FontProperty[size];
        for (int i=0; i < fps.length; i++){
            fps[i] = props.elementAt(i);
        }
        return fps;
    }
    public static int getLogicalFaceIndex(String fontName){
        for (int i=0; i<LOGICAL_FONT_NAMES.length; i++ ){
            if (LOGICAL_FONT_NAMES[i].equalsIgnoreCase(fontName)){
                return i;
            }
        }
        return -1;
    }
    public boolean isFamilyExist(String familyName){
        return (getFamilyIndex(familyName) != -1);
    }
    public int getFamilyIndex(String familyName){
        for (int i=0; i<allFamilies.length; i++ ){
            if (familyName.equalsIgnoreCase(allFamilies[i])){
                return i;
            }
        }
        return -1;
    }
    public String getFamily(int index){
        return allFamilies[index];
    }
    public int getFaceIndex(String faceName){
        return -1;
    }
    public abstract String[] getAllFamilies();
    public abstract Font[] getAllFonts();
    private class HashMapReference extends SoftReference<FontPeer> {
        private final String key;
        public HashMapReference(final String key, final FontPeer value,
                              final ReferenceQueue<FontPeer> queue) {
            super(value, queue);
            this.key = key;
        }
        public Object getKey() {
            return key;
        }
    }
    private void updateFontsTable() {
        HashMapReference r;
    }
}
