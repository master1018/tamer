public class AndroidFontManager extends FontManager {
    String faces[];
    public static final String[] LINUX_WEIGHT_NAMES = {
            "black", "bold", "demibold", "medium", "light" 
    };
    public static final String[] LINUX_SLANT_NAMES = {
            "i", "o", "r" 
    };
    public static final AndroidFontManager inst = new AndroidFontManager();
    private AndroidFontManager() {
        super();
        faces = new String[] { "NORMAL", "BOLD", "ITALIC", "BOLDITALIC"};
        initFontProperties();
    }
    public void initLCIDTable(){
    	throw new RuntimeException("Not implemented!");
    }
    public File getTempFontFile()throws IOException{
        File fontFile = File.createTempFile("jFont", ".ttf", new File(System.getProperty("user.home") +"/.fonts")); 
        fontFile.deleteOnExit();
        return fontFile;
    }
    public boolean initFontProperties(){
        File fpFile = getFontPropertyFile();
        if (fpFile == null){
            return false;
        }
        Properties props = getProperties(fpFile);
        if (props == null){
            return false;
        }
        for (int i=0; i < LOGICAL_FONT_NAMES.length; i++){
            String lName = LOGICAL_FONT_NAMES[i];
            for (int j=0; j < STYLE_NAMES.length; j++){
                String styleName = STYLE_NAMES[j];
                Vector propsVector = new Vector();
                int numComp = 0;
                boolean moreEntries = true;
                String value = null;
                while(moreEntries){
                    String property = FONT_MAPPING_KEYS[0].replaceAll("LogicalFontName", lName).replaceAll("StyleName", styleName).replaceAll("ComponentIndex", String.valueOf(numComp)); 
                    value = props.getProperty(property);
                    if ((j == 0) && (value == null)){
                        property = FONT_MAPPING_KEYS[1].replaceAll("LogicalFontName", lName).replaceAll("ComponentIndex", String.valueOf(numComp)); 
                        value = props.getProperty(property);
                    }
                    if (value != null){
                        String[] fields = parseXLFD(value);
                        if (fields == null){
                            throw new RuntimeException(Messages.getString("awt.08", value)); 
                        }
                        String fontName = fields[1];
                        String weight = fields[2];
                        String italic = fields[3];
                        int style = getBoldStyle(weight) | getItalicStyle(italic);
                        String encoding = props.getProperty(FONT_CHARACTER_ENCODING.replaceAll("LogicalFontName", lName).replaceAll("ComponentIndex", String.valueOf(numComp))); 
                        String exclString = props.getProperty(EXCLUSION_RANGES.replaceAll("LogicalFontName", lName).replaceAll("ComponentIndex", String.valueOf(numComp))); 
                        int[] exclRange = parseIntervals(exclString);
                        FontProperty fp = new AndroidFontProperty(lName, styleName, null, fontName, value, style, exclRange, encoding);
                        propsVector.add(fp);
                        numComp++;
                    } else {
                        moreEntries = false;
                    }
                }
                fProperties.put(LOGICAL_FONT_NAMES[i] + "." + j, propsVector); 
            }
        }
        return true;
    }
    private int getBoldStyle(String str){
        for (int i = 0; i < LINUX_WEIGHT_NAMES.length;i++){
            if (str.equalsIgnoreCase(LINUX_WEIGHT_NAMES[i])){
                return (i < 3) ? Font.BOLD : Font.PLAIN;
            }
        }
        return Font.PLAIN;
    }
    private int getItalicStyle(String str){
        for (int i = 0; i < LINUX_SLANT_NAMES.length;i++){
            if (str.equalsIgnoreCase(LINUX_SLANT_NAMES[i])){
                return (i < 2) ? Font.ITALIC : Font.PLAIN;
            }
        }
        return Font.PLAIN;
    }
    public static String[] parseXLFD(String xlfd){
        int fieldsCount = 14;
        String fieldsDelim = "-"; 
        String[] res = new String[fieldsCount];
        if (!xlfd.startsWith(fieldsDelim)){
            return null;
        }
        xlfd = xlfd.substring(1);
        int i=0;
        int pos;
        for (i=0; i < fieldsCount-1; i++){
            pos = xlfd.indexOf(fieldsDelim);
            if (pos != -1){
                res[i] = xlfd.substring(0, pos);
                xlfd = xlfd.substring(pos + 1);
            } else {
                return null;
            }
        }
        pos = xlfd.indexOf(fieldsDelim);
        if(pos != -1){
            return null;
        }
        res[fieldsCount-1] = xlfd;
        return res;
    }
    public int getFaceIndex(String faceName){
        for (int i = 0; i < faces.length; i++) {
            if(faces[i].equals(faceName)){
                return i;
            }
        }
        return -1;
    }
    public String[] getAllFamilies(){
        if (allFamilies == null){
        	allFamilies = new String[]{"sans-serif", "serif", "monospace"};
        }
        return allFamilies;
    }
    public Font[] getAllFonts(){
        Font[] fonts = new Font[faces.length];
        for (int i =0; i < fonts.length;i++){
            fonts[i] = new Font(faces[i], Font.PLAIN, 1);
        }
        return fonts;
    }
    public FontPeer createPhysicalFontPeer(String name, int style, int size) {
        AndroidFont peer;
        int familyIndex = getFamilyIndex(name);
        if (familyIndex != -1){
            peer = new AndroidFont(getFamily(familyIndex), style, size);
            peer.setFamily(getFamily(familyIndex));
            return peer;
        }
        int faceIndex = getFaceIndex(name); 
        if (faceIndex != -1){
            peer = new AndroidFont(name, style, size);
            return peer;
        }
        return null;
    }
    public FontPeer createDefaultFont(int style, int size) {
    	Log.i("DEFAULT FONT", Integer.toString(style));
        return new AndroidFont(DEFAULT_NAME, style, size);
    }
}
