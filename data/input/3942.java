public class Type1Font extends FileFont {
     private static class T1DisposerRecord  implements DisposerRecord {
        String fileName = null;
        T1DisposerRecord(String name) {
            fileName = name;
        }
        public synchronized void dispose() {
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        if (fileName != null) {
                            (new java.io.File(fileName)).delete();
                        }
                        return null;
                    }
             });
        }
    }
    WeakReference bufferRef = new WeakReference(null);
    private String psName = null;
    static private HashMap styleAbbreviationsMapping;
    static private HashSet styleNameTokes;
    static {
        styleAbbreviationsMapping = new HashMap();
        styleNameTokes = new HashSet();
        String nm[] = {"Black", "Bold", "Book", "Demi", "Heavy", "Light",
                       "Meduium", "Nord", "Poster", "Regular", "Super", "Thin",
                       "Compressed", "Condensed", "Compact", "Extended", "Narrow",
                       "Inclined", "Italic", "Kursiv", "Oblique", "Upright", "Sloped",
                       "Semi", "Ultra", "Extra",
                       "Alternate", "Alternate", "Deutsche Fraktur", "Expert", "Inline", "Ornaments",
                       "Outline", "Roman", "Rounded", "Script", "Shaded", "Swash", "Titling", "Typewriter"};
        String abbrv[] = {"Blk", "Bd", "Bk", "Dm", "Hv", "Lt",
                          "Md", "Nd", "Po", "Rg", "Su", "Th",
                          "Cm", "Cn", "Ct", "Ex", "Nr",
                          "Ic", "It", "Ks", "Obl", "Up", "Sl",
                          "Sm", "Ult", "X",
                          "A", "Alt", "Dfr", "Exp", "In", "Or",
                          "Ou", "Rm", "Rd", "Scr", "Sh", "Sw", "Ti", "Typ"};
       String styleTokens[] = {"Black", "Bold", "Book", "Demi", "Heavy", "Light",
                       "Medium", "Nord", "Poster", "Regular", "Super", "Thin",
                       "Compressed", "Condensed", "Compact", "Extended", "Narrow",
                       "Inclined", "Italic", "Kursiv", "Oblique", "Upright", "Sloped", "Slanted",
                       "Semi", "Ultra", "Extra"};
        for(int i=0; i<nm.length; i++) {
            styleAbbreviationsMapping.put(abbrv[i], nm[i]);
        }
        for(int i=0; i<styleTokens.length; i++) {
            styleNameTokes.add(styleTokens[i]);
        }
        }
    public Type1Font(String platname, Object nativeNames)
        throws FontFormatException {
        this(platname, nativeNames, false);
    }
    public Type1Font(String platname, Object nativeNames, boolean createdCopy)
        throws FontFormatException {
        super(platname, nativeNames);
        fontRank = Font2D.TYPE1_RANK;
        checkedNatives = true;
        try {
            verify();
        } catch (Throwable t) {
            if (createdCopy) {
                T1DisposerRecord ref = new T1DisposerRecord(platname);
                Disposer.addObjectRecord(bufferRef, ref);
                bufferRef = null;
            }
            if (t instanceof FontFormatException) {
                throw (FontFormatException)t;
            } else {
                throw new FontFormatException("Unexpected runtime exception.");
            }
        }
    }
    private synchronized ByteBuffer getBuffer() throws FontFormatException {
        MappedByteBuffer mapBuf = (MappedByteBuffer)bufferRef.get();
        if (mapBuf == null) {
            try {
                RandomAccessFile raf = (RandomAccessFile)
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction() {
                        public Object run() {
                            try {
                                return new RandomAccessFile(platName, "r");
                            } catch (FileNotFoundException ffne) {
                            }
                            return null;
                    }
                });
                FileChannel fc = raf.getChannel();
                fileSize = (int)fc.size();
                mapBuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
                mapBuf.position(0);
                bufferRef = new WeakReference(mapBuf);
                fc.close();
            } catch (NullPointerException e) {
                throw new FontFormatException(e.toString());
            } catch (ClosedChannelException e) {
                Thread.interrupted();
                return getBuffer();
            } catch (IOException e) {
                throw new FontFormatException(e.toString());
            }
        }
        return mapBuf;
    }
    protected void close() {
    }
    void readFile(ByteBuffer buffer) {
        RandomAccessFile raf = null;
        FileChannel fc;
        try {
            raf = (RandomAccessFile)
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction() {
                        public Object run() {
                            try {
                                return new RandomAccessFile(platName, "r");
                            } catch (FileNotFoundException fnfe) {
                            }
                            return null;
                    }
            });
            fc = raf.getChannel();
            while (buffer.remaining() > 0 && fc.read(buffer) != -1) {}
        } catch (NullPointerException npe) {
        } catch (ClosedChannelException e) {
            try {
                if (raf != null) {
                    raf.close();
                    raf = null;
                }
            } catch (IOException ioe) {
            }
            Thread.interrupted();
            readFile(buffer);
        } catch (IOException e) {
        } finally  {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                }
            }
        }
    }
    public synchronized ByteBuffer readBlock(int offset, int length) {
        ByteBuffer mappedBuf = null;
        try {
            mappedBuf = getBuffer();
            if (offset > fileSize) {
                offset = fileSize;
            }
            mappedBuf.position(offset);
            return mappedBuf.slice();
        } catch (FontFormatException e) {
            return null;
        }
    }
    private void verify() throws FontFormatException {
        ByteBuffer bb = getBuffer();
        if (bb.capacity() < 6) {
            throw new FontFormatException("short file");
        }
        int val = bb.get(0) & 0xff;
        if ((bb.get(0) & 0xff) == 0x80) {
            verifyPFB(bb);
            bb.position(6);
        } else {
            verifyPFA(bb);
            bb.position(0);
        }
        initNames(bb);
        if (familyName == null || fullName == null) {
            throw new FontFormatException("Font name not found");
        }
        setStyle();
    }
    public int getFileSize() {
        if (fileSize == 0) {
            try {
                getBuffer();
            } catch (FontFormatException e) {
            }
        }
        return fileSize;
    }
    private void verifyPFA(ByteBuffer bb) throws FontFormatException {
        if (bb.getShort() != 0x2521) { 
            throw new FontFormatException("bad pfa font");
        }
    }
    private void verifyPFB(ByteBuffer bb) throws FontFormatException {
        int pos = 0;
        while (true) {
            try {
                int segType = bb.getShort(pos) & 0xffff;
                if (segType == 0x8001 || segType == 0x8002) {
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                    int segLen = bb.getInt(pos+2);
                    bb.order(ByteOrder.BIG_ENDIAN);
                    if (segLen <= 0) {
                        throw new FontFormatException("bad segment length");
                    }
                    pos += segLen+6;
                } else if (segType == 0x8003) {
                    return;
                } else {
                    throw new FontFormatException("bad pfb file");
                }
            } catch (BufferUnderflowException bue) {
                throw new FontFormatException(bue.toString());
            } catch (Exception e) {
                throw new FontFormatException(e.toString());
            }
        }
    }
    private static final int PSEOFTOKEN = 0;
    private static final int PSNAMETOKEN = 1;
    private static final int PSSTRINGTOKEN = 2;
    private void initNames(ByteBuffer bb) throws FontFormatException {
        boolean eof = false;
        String fontType = null;
        try {
            while ((fullName == null || familyName == null || psName == null || fontType == null) && !eof) {
                int tokenType = nextTokenType(bb);
                if (tokenType == PSNAMETOKEN) {
                    int pos = bb.position();
                    if (bb.get(pos) == 'F') {
                        String s = getSimpleToken(bb);
                        if ("FullName".equals(s)) {
                            if (nextTokenType(bb)==PSSTRINGTOKEN) {
                                fullName = getString(bb);
                            }
                        } else if ("FamilyName".equals(s)) {
                            if (nextTokenType(bb)==PSSTRINGTOKEN) {
                                familyName = getString(bb);
                            }
                        } else if ("FontName".equals(s)) {
                            if (nextTokenType(bb)==PSNAMETOKEN) {
                                psName = getSimpleToken(bb);
                            }
                        } else if ("FontType".equals(s)) {
                            String token = getSimpleToken(bb);
                            if ("def".equals(getSimpleToken(bb))) {
                                fontType = token;
                        }
                        }
                    } else {
                        while (bb.get() > ' '); 
                    }
                } else if (tokenType == PSEOFTOKEN) {
                    eof = true;
                }
            }
        } catch (Exception e) {
                throw new FontFormatException(e.toString());
        }
        if (!"1".equals(fontType)) {
            throw new FontFormatException("Unsupported font type");
        }
    if (psName == null) { 
                bb.position(0);
                if (bb.getShort() != 0x2521) { 
                    bb.position(8);
                }
                String formatType = getSimpleToken(bb);
                if (!formatType.startsWith("FontType1-") && !formatType.startsWith("PS-AdobeFont-")) {
                        throw new FontFormatException("Unsupported font format [" + formatType + "]");
                }
                psName = getSimpleToken(bb);
        }
        if (eof) {
            if (fullName != null) {
                familyName = fullName2FamilyName(fullName);
            } else if (familyName != null) {
                fullName = familyName;
            } else { 
                fullName = psName2FullName(psName);
                familyName = psName2FamilyName(psName);
            }
        }
    }
    private String fullName2FamilyName(String name) {
        String res, token;
        int len, start, end; 
        end = name.length();
        while (end > 0) {
            start = end - 1;
            while (start > 0 && name.charAt(start) != ' ')
              start--;
                        if (!isStyleToken(name.substring(start+1, end))) {
                                return name.substring(0, end);
            }
                        end = start;
        }
                return name; 
        }
    private String expandAbbreviation(String abbr) {
        if (styleAbbreviationsMapping.containsKey(abbr))
                        return (String) styleAbbreviationsMapping.get(abbr);
        return abbr;
    }
    private boolean isStyleToken(String token) {
        return styleNameTokes.contains(token);
    }
    private String psName2FullName(String name) {
        String res;
        int pos;
        pos = name.indexOf("-");
        if (pos >= 0) {
            res =  expandName(name.substring(0, pos), false);
            res += " " + expandName(name.substring(pos+1), true);
        } else {
            res = expandName(name, false);
        }
        return res;
    }
    private String psName2FamilyName(String name) {
        String tmp = name;
        if (tmp.indexOf("-") > 0) {
            tmp = tmp.substring(0, tmp.indexOf("-"));
        }
        return expandName(tmp, false);
    }
    private int nextCapitalLetter(String s, int off) {
        for (; (off >=0) && off < s.length(); off++) {
            if (s.charAt(off) >= 'A' && s.charAt(off) <= 'Z')
                return off;
        }
        return -1;
    }
    private String expandName(String s, boolean tryExpandAbbreviations) {
        StringBuffer res = new StringBuffer(s.length() + 10);
        int start=0, end;
        while(start < s.length()) {
            end = nextCapitalLetter(s, start + 1);
            if (end < 0) {
                end = s.length();
            }
            if (start != 0) {
                res.append(" ");
            }
            if (tryExpandAbbreviations) {
                res.append(expandAbbreviation(s.substring(start, end)));
            } else {
                res.append(s.substring(start, end));
            }
            start = end;
                }
        return res.toString();
    }
    private byte skip(ByteBuffer bb) {
        byte b = bb.get();
        while (b == '%') {
            while (true) {
                b = bb.get();
                if (b == '\r' || b == '\n') {
                    break;
                }
            }
        }
        while (b <= ' ') {
            b = bb.get();
        }
        return b;
    }
    private int nextTokenType(ByteBuffer bb) {
        try {
            byte b = skip(bb);
            while (true) {
                if (b == (byte)'/') { 
                    return PSNAMETOKEN;
                } else if (b == (byte)'(') { 
                    return PSSTRINGTOKEN;
                } else if ((b == (byte)'\r') || (b == (byte)'\n')) {
                b = skip(bb);
                } else {
                    b = bb.get();
                }
            }
        } catch (BufferUnderflowException e) {
            return PSEOFTOKEN;
        }
    }
    private String getSimpleToken(ByteBuffer bb) {
        while (bb.get() <= ' ');
        int pos1 = bb.position()-1;
        while (bb.get() > ' ');
        int pos2 = bb.position();
        byte[] nameBytes = new byte[pos2-pos1-1];
        bb.position(pos1);
        bb.get(nameBytes);
        try {
            return new String(nameBytes, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            return new String(nameBytes);
        }
    }
    private String getString(ByteBuffer bb) {
        int pos1 = bb.position();
        while (bb.get() != ')');
        int pos2 = bb.position();
        byte[] nameBytes = new byte[pos2-pos1-1];
        bb.position(pos1);
        bb.get(nameBytes);
        try {
            return new String(nameBytes, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            return new String(nameBytes);
        }
    }
    public String getPostscriptName() {
        return psName;
    }
    protected synchronized FontScaler getScaler() {
        if (scaler == null) {
            scaler = FontScaler.getScaler(this, 0, false, fileSize);
        }
        return scaler;
    }
    CharToGlyphMapper getMapper() {
        if (mapper == null) {
            mapper = new Type1GlyphMapper(this);
        }
        return mapper;
    }
    public int getNumGlyphs() {
        try {
            return getScaler().getNumGlyphs();
        } catch (FontScalerException e) {
            scaler = FontScaler.getNullScaler();
            return getNumGlyphs();
        }
    }
    public int getMissingGlyphCode() {
        try {
            return getScaler().getMissingGlyphCode();
        } catch (FontScalerException e) {
            scaler = FontScaler.getNullScaler();
            return getMissingGlyphCode();
        }
    }
    public int getGlyphCode(char charCode) {
        try {
            return getScaler().getGlyphCode(charCode);
        } catch (FontScalerException e) {
            scaler = FontScaler.getNullScaler();
            return getGlyphCode(charCode);
        }
    }
    public String toString() {
        return "** Type1 Font: Family="+familyName+ " Name="+fullName+
            " style="+style+" fileName="+getPublicFileName();
    }
}
