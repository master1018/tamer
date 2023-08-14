public abstract class PlatformFont implements FontPeer {
    static {
        NativeLibLoader.loadLibraries();
        initIDs();
    }
    protected FontDescriptor[] componentFonts;
    protected char defaultChar;
    protected FontConfiguration fontConfig;
    protected FontDescriptor defaultFont;
    protected String familyName;
    private Object[] fontCache;
    protected static int FONTCACHESIZE = 256;
    protected static int FONTCACHEMASK = PlatformFont.FONTCACHESIZE - 1;
    protected static String osVersion;
    public PlatformFont(String name, int style){
        SunFontManager sfm = SunFontManager.getInstance();
        if (sfm instanceof FontSupport) {
            fontConfig = ((FontSupport)sfm).getFontConfiguration();
        }
        if (fontConfig == null) {
            return;
        }
        familyName = name.toLowerCase(Locale.ENGLISH);
        if (!FontConfiguration.isLogicalFontFamilyName(familyName)) {
            familyName = fontConfig.getFallbackFamilyName(familyName, "sansserif");
        }
        componentFonts = fontConfig.getFontDescriptors(familyName, style);
        char missingGlyphCharacter = getMissingGlyphCharacter();
        defaultChar = '?';
        if (componentFonts.length > 0)
            defaultFont = componentFonts[0];
        for (int i = 0; i < componentFonts.length; i++){
            if (componentFonts[i].isExcluded(missingGlyphCharacter)) {
                continue;
            }
            if (componentFonts[i].encoder.canEncode(missingGlyphCharacter)) {
                defaultFont = componentFonts[i];
                defaultChar = missingGlyphCharacter;
                break;
            }
        }
    }
    protected abstract char getMissingGlyphCharacter();
    public CharsetString[] makeMultiCharsetString(String str){
        return makeMultiCharsetString(str.toCharArray(), 0, str.length(), true);
    }
    public CharsetString[] makeMultiCharsetString(String str, boolean allowdefault){
        return makeMultiCharsetString(str.toCharArray(), 0, str.length(), allowdefault);
    }
    public CharsetString[] makeMultiCharsetString(char str[], int offset, int len) {
        return makeMultiCharsetString(str, offset, len, true);
    }
    public CharsetString[] makeMultiCharsetString(char str[], int offset, int len,
                                                  boolean allowDefault) {
        if (len < 1) {
            return new CharsetString[0];
        }
        Vector mcs = null;
        char[] tmpStr = new char[len];
        char tmpChar = defaultChar;
        boolean encoded = false;
        FontDescriptor currentFont = defaultFont;
        for (int i = 0; i < componentFonts.length; i++) {
            if (componentFonts[i].isExcluded(str[offset])){
                continue;
            }
            if (componentFonts[i].encoder.canEncode(str[offset])){
                currentFont = componentFonts[i];
                tmpChar = str[offset];
                encoded = true;
                break;
            }
        }
        if (!allowDefault && !encoded) {
            return null;
        } else {
            tmpStr[0] = tmpChar;
        }
        int lastIndex = 0;
        for (int i = 1; i < len; i++){
            char ch = str[offset + i];
            FontDescriptor fd = defaultFont;
            tmpChar = defaultChar;
            encoded = false;
            for (int j = 0; j < componentFonts.length; j++){
                if (componentFonts[j].isExcluded(ch)){
                    continue;
                }
                if (componentFonts[j].encoder.canEncode(ch)){
                    fd = componentFonts[j];
                    tmpChar = ch;
                    encoded = true;
                    break;
                }
            }
            if (!allowDefault && !encoded) {
                return null;
            } else {
                tmpStr[i] = tmpChar;
            }
            if (currentFont != fd){
                if (mcs == null) {
                    mcs = new Vector(3);
                }
                mcs.addElement(new CharsetString(tmpStr, lastIndex,
                                                 i-lastIndex, currentFont));
                currentFont = fd;
                fd = defaultFont;
                lastIndex = i;
            }
        }
        CharsetString[] result;
        CharsetString cs = new CharsetString(tmpStr, lastIndex,
                                            len-lastIndex, currentFont);
        if (mcs == null) {
            result = new CharsetString[1];
            result[0] = cs;
        } else {
            mcs.addElement(cs);
            result = new CharsetString[mcs.size()];
            for (int i = 0; i < mcs.size(); i++){
                result[i] = (CharsetString)mcs.elementAt(i);
            }
        }
        return result;
    }
    public boolean mightHaveMultiFontMetrics() {
        return fontConfig != null;
    }
    public Object[] makeConvertedMultiFontString(String str)
    {
        return makeConvertedMultiFontChars(str.toCharArray(),0,str.length());
    }
    public Object[] makeConvertedMultiFontChars(char[] data,
                                                int start, int len)
    {
        Object[] result = new Object[2];
        Object[] workingCache;
        byte[] convertedData = null;
        int stringIndex = start;
        int convertedDataIndex = 0;
        int resultIndex = 0;
        int cacheIndex;
        FontDescriptor currentFontDescriptor = null;
        FontDescriptor lastFontDescriptor = null;
        char currentDefaultChar;
        PlatformFontCache theChar;
        int end = start + len;
        if (start < 0 || end > data.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if(stringIndex >= end) {
            return null;
        }
        while(stringIndex < end)
        {
            currentDefaultChar = data[stringIndex];
            cacheIndex = (int)(currentDefaultChar & this.FONTCACHEMASK);
            theChar = (PlatformFontCache)getFontCache()[cacheIndex];
            if(theChar == null || theChar.uniChar != currentDefaultChar)
            {
                currentFontDescriptor = defaultFont;
                currentDefaultChar = defaultChar;
                char ch = (char)data[stringIndex];
                int componentCount = componentFonts.length;
                for (int j = 0; j < componentCount; j++) {
                    FontDescriptor fontDescriptor = componentFonts[j];
                    fontDescriptor.encoder.reset();
                    if (fontDescriptor.isExcluded(ch)) {
                        continue;
                    }
                    if (fontDescriptor.encoder.canEncode(ch)) {
                        currentFontDescriptor = fontDescriptor;
                        currentDefaultChar = ch;
                        break;
                    }
                }
                try {
                    char[] input = new char[1];
                    input[0] = currentDefaultChar;
                    theChar = new PlatformFontCache();
                    if (currentFontDescriptor.useUnicode()) {
                        if (currentFontDescriptor.isLE) {
                            theChar.bb.put((byte)(input[0] & 0xff));
                            theChar.bb.put((byte)(input[0] >>8));
                        } else {
                            theChar.bb.put((byte)(input[0] >> 8));
                            theChar.bb.put((byte)(input[0] & 0xff));
                        }
                    }
                    else  {
                        currentFontDescriptor.encoder.encode(CharBuffer.wrap(input),
                                                             theChar.bb,
                                                             true);
                    }
                    theChar.fontDescriptor = currentFontDescriptor;
                    theChar.uniChar = data[stringIndex];
                    getFontCache()[cacheIndex] = theChar;
                } catch(Exception e){
                    System.err.println(e);
                    e.printStackTrace();
                    return null;
                }
            }
            if(lastFontDescriptor != theChar.fontDescriptor) {
                if(lastFontDescriptor != null) {
                    result[resultIndex++] = lastFontDescriptor;
                    result[resultIndex++] = convertedData;
                    if(convertedData != null) {
                        convertedDataIndex -= 4;
                        convertedData[0] = (byte)(convertedDataIndex >> 24);
                        convertedData[1] = (byte)(convertedDataIndex >> 16);
                        convertedData[2] = (byte)(convertedDataIndex >> 8);
                        convertedData[3] = (byte)convertedDataIndex;
                    }
                    if(resultIndex >= result.length) {
                        Object[] newResult = new Object[result.length * 2];
                        System.arraycopy(result, 0, newResult, 0,
                                         result.length);
                        result = newResult;
                    }
                }
                if (theChar.fontDescriptor.useUnicode()) {
                    convertedData = new byte[(end - stringIndex + 1) *
                                        (int)theChar.fontDescriptor.unicodeEncoder.maxBytesPerChar()
                                        + 4];
                }
                else  {
                    convertedData = new byte[(end - stringIndex + 1) *
                                        (int)theChar.fontDescriptor.encoder.maxBytesPerChar()
                                        + 4];
                }
                convertedDataIndex = 4;
                lastFontDescriptor = theChar.fontDescriptor;
            }
            byte[] ba = theChar.bb.array();
            int size = theChar.bb.position();
            if(size == 1) {
                convertedData[convertedDataIndex++] = ba[0];
            }
            else if(size == 2) {
                convertedData[convertedDataIndex++] = ba[0];
                convertedData[convertedDataIndex++] = ba[1];
            } else if(size == 3) {
                convertedData[convertedDataIndex++] = ba[0];
                convertedData[convertedDataIndex++] = ba[1];
                convertedData[convertedDataIndex++] = ba[2];
            } else if(size == 4) {
                convertedData[convertedDataIndex++] = ba[0];
                convertedData[convertedDataIndex++] = ba[1];
                convertedData[convertedDataIndex++] = ba[2];
                convertedData[convertedDataIndex++] = ba[3];
            }
            stringIndex++;
        }
        result[resultIndex++] = lastFontDescriptor;
        result[resultIndex] = convertedData;
        if(convertedData != null) {
            convertedDataIndex -= 4;
            convertedData[0] = (byte)(convertedDataIndex >> 24);
            convertedData[1] = (byte)(convertedDataIndex >> 16);
            convertedData[2] = (byte)(convertedDataIndex >> 8);
            convertedData[3] = (byte)convertedDataIndex;
        }
        return result;
    }
    protected final Object[] getFontCache() {
        if (fontCache == null) {
            fontCache = new Object[this.FONTCACHESIZE];
        }
        return fontCache;
    }
    private static native void initIDs();
    class PlatformFontCache
    {
        char uniChar;
        FontDescriptor fontDescriptor;
        ByteBuffer bb = ByteBuffer.allocate(4);
    }
}
