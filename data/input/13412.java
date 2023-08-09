public class TrueTypeFont extends FileFont {
    public static final int cmapTag = 0x636D6170; 
    public static final int glyfTag = 0x676C7966; 
    public static final int headTag = 0x68656164; 
    public static final int hheaTag = 0x68686561; 
    public static final int hmtxTag = 0x686D7478; 
    public static final int locaTag = 0x6C6F6361; 
    public static final int maxpTag = 0x6D617870; 
    public static final int nameTag = 0x6E616D65; 
    public static final int postTag = 0x706F7374; 
    public static final int os_2Tag = 0x4F532F32; 
    public static final int GDEFTag = 0x47444546; 
    public static final int GPOSTag = 0x47504F53; 
    public static final int GSUBTag = 0x47535542; 
    public static final int mortTag = 0x6D6F7274; 
    public static final int fdscTag = 0x66647363; 
    public static final int fvarTag = 0x66766172; 
    public static final int featTag = 0x66656174; 
    public static final int EBLCTag = 0x45424C43; 
    public static final int gaspTag = 0x67617370; 
    public static final int ttcfTag = 0x74746366; 
    public static final int v1ttTag = 0x00010000; 
    public static final int trueTag = 0x74727565; 
    public static final int ottoTag = 0x4f54544f; 
    public static final int MS_PLATFORM_ID = 3;
    public static final short ENGLISH_LOCALE_ID = 0x0409; 
    public static final int FAMILY_NAME_ID = 1;
    public static final int FULL_NAME_ID = 4;
    public static final int POSTSCRIPT_NAME_ID = 6;
    private static final short US_LCID = 0x0409;  
    private static Map<String, Short> lcidMap;
    class DirectoryEntry {
        int tag;
        int offset;
        int length;
    }
    private static class TTDisposerRecord implements DisposerRecord {
        FileChannel channel = null;
        public synchronized void dispose() {
            try {
                if (channel != null) {
                    channel.close();
                }
            } catch (IOException e) {
            } finally {
                channel = null;
            }
        }
    }
    TTDisposerRecord disposerRecord = new TTDisposerRecord();
    int fontIndex = 0;
    int directoryCount = 1;
    int directoryOffset; 
    int numTables;
    DirectoryEntry []tableDirectory;
    private boolean supportsJA;
    private boolean supportsCJK;
    private Locale nameLocale;
    private String localeFamilyName;
    private String localeFullName;
    public TrueTypeFont(String platname, Object nativeNames, int fIndex,
                 boolean javaRasterizer)
        throws FontFormatException {
        super(platname, nativeNames);
        useJavaRasterizer = javaRasterizer;
        fontRank = Font2D.TTF_RANK;
        try {
            verify();
            init(fIndex);
        } catch (Throwable t) {
            close();
            if (t instanceof FontFormatException) {
                throw (FontFormatException)t;
            } else {
                throw new FontFormatException("Unexpected runtime exception.");
            }
        }
        Disposer.addObjectRecord(this, disposerRecord);
    }
    @Override
    protected boolean checkUseNatives() {
        if (checkedNatives) {
            return useNatives;
        }
        if (!FontUtilities.isSolaris || useJavaRasterizer ||
            FontUtilities.useT2K || nativeNames == null ||
            getDirectoryEntry(EBLCTag) != null ||
            GraphicsEnvironment.isHeadless()) {
            checkedNatives = true;
            return false; 
        } else if (nativeNames instanceof String) {
            String name = (String)nativeNames;
            if (name.indexOf("8859") > 0) {
                checkedNatives = true;
                return false;
            } else if (NativeFont.hasExternalBitmaps(name)) {
                nativeFonts = new NativeFont[1];
                try {
                    nativeFonts[0] = new NativeFont(name, true);
                    useNatives = true;
                } catch (FontFormatException e) {
                    nativeFonts = null;
                }
            }
        } else if (nativeNames instanceof String[]) {
            String[] natNames = (String[])nativeNames;
            int numNames = natNames.length;
            boolean externalBitmaps = false;
            for (int nn = 0; nn < numNames; nn++) {
                if (natNames[nn].indexOf("8859") > 0) {
                    checkedNatives = true;
                    return false;
                } else if (NativeFont.hasExternalBitmaps(natNames[nn])) {
                    externalBitmaps = true;
                }
            }
            if (!externalBitmaps) {
                checkedNatives = true;
                return false;
            }
            useNatives = true;
            nativeFonts = new NativeFont[numNames];
            for (int nn = 0; nn < numNames; nn++) {
                try {
                    nativeFonts[nn] = new NativeFont(natNames[nn], true);
                } catch (FontFormatException e) {
                    useNatives = false;
                    nativeFonts = null;
                }
            }
        }
        if (useNatives) {
            glyphToCharMap = new char[getMapper().getNumGlyphs()];
        }
        checkedNatives = true;
        return useNatives;
    }
    private synchronized FileChannel open() throws FontFormatException {
        if (disposerRecord.channel == null) {
            if (FontUtilities.isLogging()) {
                FontUtilities.getLogger().info("open TTF: " + platName);
            }
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
                disposerRecord.channel = raf.getChannel();
                fileSize = (int)disposerRecord.channel.size();
                FontManager fm = FontManagerFactory.getInstance();
                if (fm instanceof SunFontManager) {
                    ((SunFontManager) fm).addToPool(this);
                }
            } catch (NullPointerException e) {
                close();
                throw new FontFormatException(e.toString());
            } catch (ClosedChannelException e) {
                Thread.interrupted();
                close();
                open();
            } catch (IOException e) {
                close();
                throw new FontFormatException(e.toString());
            }
        }
        return disposerRecord.channel;
    }
    protected synchronized void close() {
        disposerRecord.dispose();
    }
    int readBlock(ByteBuffer buffer, int offset, int length) {
        int bread = 0;
        try {
            synchronized (this) {
                if (disposerRecord.channel == null) {
                    open();
                }
                if (offset + length > fileSize) {
                    if (offset >= fileSize) {
                        if (FontUtilities.isLogging()) {
                            String msg = "Read offset is " + offset +
                                " file size is " + fileSize+
                                " file is " + platName;
                            FontUtilities.getLogger().severe(msg);
                        }
                        return -1;
                    } else {
                        length = fileSize - offset;
                    }
                }
                buffer.clear();
                disposerRecord.channel.position(offset);
                while (bread < length) {
                    int cnt = disposerRecord.channel.read(buffer);
                    if (cnt == -1) {
                        String msg = "Unexpected EOF " + this;
                        int currSize = (int)disposerRecord.channel.size();
                        if (currSize != fileSize) {
                            msg += " File size was " + fileSize +
                                " and now is " + currSize;
                        }
                        if (FontUtilities.isLogging()) {
                            FontUtilities.getLogger().severe(msg);
                        }
                        if (bread > length/2 || bread > 16384) {
                            buffer.flip();
                            if (FontUtilities.isLogging()) {
                                msg = "Returning " + bread +
                                    " bytes instead of " + length;
                                FontUtilities.getLogger().severe(msg);
                            }
                        } else {
                            bread = -1;
                        }
                        throw new IOException(msg);
                    }
                    bread += cnt;
                }
                buffer.flip();
                if (bread > length) { 
                    bread = length;
                }
            }
        } catch (FontFormatException e) {
            if (FontUtilities.isLogging()) {
                FontUtilities.getLogger().severe(
                                       "While reading " + platName, e);
            }
            bread = -1; 
            deregisterFontAndClearStrikeCache();
        } catch (ClosedChannelException e) {
            Thread.interrupted();
            close();
            return readBlock(buffer, offset, length);
        } catch (IOException e) {
            if (FontUtilities.isLogging()) {
                FontUtilities.getLogger().severe(
                                       "While reading " + platName, e);
            }
            if (bread == 0) {
                bread = -1; 
                deregisterFontAndClearStrikeCache();
            }
        }
        return bread;
    }
    ByteBuffer readBlock(int offset, int length) {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        try {
            synchronized (this) {
                if (disposerRecord.channel == null) {
                    open();
                }
                if (offset + length > fileSize) {
                    if (offset > fileSize) {
                        return null; 
                    } else {
                        buffer = ByteBuffer.allocate(fileSize-offset);
                    }
                }
                disposerRecord.channel.position(offset);
                disposerRecord.channel.read(buffer);
                buffer.flip();
            }
        } catch (FontFormatException e) {
            return null;
        } catch (ClosedChannelException e) {
            Thread.interrupted();
            close();
            readBlock(buffer, offset, length);
        } catch (IOException e) {
            return null;
        }
        return buffer;
    }
    byte[] readBytes(int offset, int length) {
        ByteBuffer buffer = readBlock(offset, length);
        if (buffer.hasArray()) {
            return buffer.array();
        } else {
            byte[] bufferBytes = new byte[buffer.limit()];
            buffer.get(bufferBytes);
            return bufferBytes;
        }
    }
    private void verify() throws FontFormatException {
        open();
    }
    private static final int TTCHEADERSIZE = 12;
    private static final int DIRECTORYHEADERSIZE = 12;
    private static final int DIRECTORYENTRYSIZE = 16;
    protected void init(int fIndex) throws FontFormatException  {
        int headerOffset = 0;
        ByteBuffer buffer = readBlock(0, TTCHEADERSIZE);
        try {
            switch (buffer.getInt()) {
            case ttcfTag:
                buffer.getInt(); 
                directoryCount = buffer.getInt();
                if (fIndex >= directoryCount) {
                    throw new FontFormatException("Bad collection index");
                }
                fontIndex = fIndex;
                buffer = readBlock(TTCHEADERSIZE+4*fIndex, 4);
                headerOffset = buffer.getInt();
                break;
            case v1ttTag:
            case trueTag:
            case ottoTag:
                break;
            default:
                throw new FontFormatException("Unsupported sfnt " +
                                              getPublicFileName());
            }
            buffer = readBlock(headerOffset+4, 2);
            numTables = buffer.getShort();
            directoryOffset = headerOffset+DIRECTORYHEADERSIZE;
            ByteBuffer bbuffer = readBlock(directoryOffset,
                                           numTables*DIRECTORYENTRYSIZE);
            IntBuffer ibuffer = bbuffer.asIntBuffer();
            DirectoryEntry table;
            tableDirectory = new DirectoryEntry[numTables];
            for (int i=0; i<numTables;i++) {
                tableDirectory[i] = table = new DirectoryEntry();
                table.tag   =  ibuffer.get();
                 ibuffer.get();
                table.offset = ibuffer.get();
                table.length = ibuffer.get();
                if (table.offset + table.length > fileSize) {
                    throw new FontFormatException("bad table, tag="+table.tag);
                }
            }
            initNames();
        } catch (Exception e) {
            if (FontUtilities.isLogging()) {
                FontUtilities.getLogger().severe(e.toString());
            }
            if (e instanceof FontFormatException) {
                throw (FontFormatException)e;
            } else {
                throw new FontFormatException(e.toString());
            }
        }
        if (familyName == null || fullName == null) {
            throw new FontFormatException("Font name not found");
        }
        ByteBuffer os2_Table = getTableBuffer(os_2Tag);
        setStyle(os2_Table);
        setCJKSupport(os2_Table);
    }
    static final String encoding_mapping[] = {
        "cp1252",    
        "cp1250",    
        "cp1251",    
        "cp1253",    
        "cp1254",    
        "cp1255",    
        "cp1256",    
        "cp1257",    
        "",          
        "",          
        "",          
        "",          
        "",          
        "",          
        "",          
        "",          
        "ms874",     
        "ms932",     
        "gbk",       
        "ms949",     
        "ms950",     
        "ms1361",    
        "",          
        "",          
        "",          
        "",          
        "",          
        "",          
        "",          
        "",          
        "",          
        "",          
    };
    private static final String languages[][] = {
        { "en", "ca", "da", "de", "es", "fi", "fr", "is", "it",
          "nl", "no", "pt", "sq", "sv", },
        { "cs", "cz", "et", "hr", "hu", "nr", "pl", "ro", "sk",
          "sl", "sq", "sr", },
        { "bg", "mk", "ru", "sh", "uk" },
        { "el" },
        { "tr" },
        { "he" },
        { "ar" },
        { "et", "lt", "lv" },
        { "th" },
        { "ja" },
        { "zh", "zh_CN", },
        { "ko" },
        { "zh_HK", "zh_TW", },
        { "ko" },
    };
    private static final String codePages[] = {
        "cp1252",
        "cp1250",
        "cp1251",
        "cp1253",
        "cp1254",
        "cp1255",
        "cp1256",
        "cp1257",
        "ms874",
        "ms932",
        "gbk",
        "ms949",
        "ms950",
        "ms1361",
    };
    private static String defaultCodePage = null;
    static String getCodePage() {
        if (defaultCodePage != null) {
            return defaultCodePage;
        }
        if (FontUtilities.isWindows) {
            defaultCodePage =
                (String)java.security.AccessController.doPrivileged(
                   new sun.security.action.GetPropertyAction("file.encoding"));
        } else {
            if (languages.length != codePages.length) {
                throw new InternalError("wrong code pages array length");
            }
            Locale locale = sun.awt.SunToolkit.getStartupLocale();
            String language = locale.getLanguage();
            if (language != null) {
                if (language.equals("zh")) {
                    String country = locale.getCountry();
                    if (country != null) {
                        language = language + "_" + country;
                    }
                }
                for (int i=0; i<languages.length;i++) {
                    for (int l=0;l<languages[i].length; l++) {
                        if (language.equals(languages[i][l])) {
                            defaultCodePage = codePages[i];
                            return defaultCodePage;
                        }
                    }
                }
            }
        }
        if (defaultCodePage == null) {
            defaultCodePage = "";
        }
        return defaultCodePage;
    }
    public static final int reserved_bits1 = 0x80000000;
    public static final int reserved_bits2 = 0x0000ffff;
    @Override
    boolean supportsEncoding(String encoding) {
        if (encoding == null) {
            encoding = getCodePage();
        }
        if ("".equals(encoding)) {
            return false;
        }
        encoding = encoding.toLowerCase();
        if (encoding.equals("gb18030")) {
            encoding = "gbk";
        } else if (encoding.equals("ms950_hkscs")) {
            encoding = "ms950";
        }
        ByteBuffer buffer = getTableBuffer(os_2Tag);
        if (buffer == null || buffer.capacity() < 86) {
            return false;
        }
        int range1 = buffer.getInt(78); 
        int range2 = buffer.getInt(82); 
        for (int em=0; em<encoding_mapping.length; em++) {
            if (encoding_mapping[em].equals(encoding)) {
                if (((1 << em) & range1) != 0) {
                    return true;
                }
            }
        }
        return false;
    }
    private void setCJKSupport(ByteBuffer os2Table) {
        if (os2Table == null || os2Table.capacity() < 50) {
            return;
        }
        int range2 = os2Table.getInt(46); 
        supportsCJK = ((range2 & 0x29bf0000) != 0);
        supportsJA = ((range2 & 0x60000) != 0);
    }
    boolean supportsJA() {
        return supportsJA;
    }
     ByteBuffer getTableBuffer(int tag) {
        DirectoryEntry entry = null;
        for (int i=0;i<numTables;i++) {
            if (tableDirectory[i].tag == tag) {
                entry = tableDirectory[i];
                break;
            }
        }
        if (entry == null || entry.length == 0 ||
            entry.offset+entry.length > fileSize) {
            return null;
        }
        int bread = 0;
        ByteBuffer buffer = ByteBuffer.allocate(entry.length);
        synchronized (this) {
            try {
                if (disposerRecord.channel == null) {
                    open();
                }
                disposerRecord.channel.position(entry.offset);
                bread = disposerRecord.channel.read(buffer);
                buffer.flip();
            } catch (ClosedChannelException e) {
                Thread.interrupted();
                close();
                return getTableBuffer(tag);
            } catch (IOException e) {
                return null;
            } catch (FontFormatException e) {
                return null;
            }
            if (bread < entry.length) {
                return null;
            } else {
                return buffer;
            }
        }
    }
    long getLayoutTableCache() {
        try {
          return getScaler().getLayoutTableCache();
        } catch(FontScalerException fe) {
            return 0L;
        }
    }
    @Override
    byte[] getTableBytes(int tag) {
        ByteBuffer buffer = getTableBuffer(tag);
        if (buffer == null) {
            return null;
        } else if (buffer.hasArray()) {
            try {
                return buffer.array();
            } catch (Exception re) {
            }
        }
        byte []data = new byte[getTableSize(tag)];
        buffer.get(data);
        return data;
    }
    int getTableSize(int tag) {
        for (int i=0;i<numTables;i++) {
            if (tableDirectory[i].tag == tag) {
                return tableDirectory[i].length;
            }
        }
        return 0;
    }
    int getTableOffset(int tag) {
        for (int i=0;i<numTables;i++) {
            if (tableDirectory[i].tag == tag) {
                return tableDirectory[i].offset;
            }
        }
        return 0;
    }
    DirectoryEntry getDirectoryEntry(int tag) {
        for (int i=0;i<numTables;i++) {
            if (tableDirectory[i].tag == tag) {
                return tableDirectory[i];
            }
        }
        return null;
    }
    boolean useEmbeddedBitmapsForSize(int ptSize) {
        if (!supportsCJK) {
            return false;
        }
        if (getDirectoryEntry(EBLCTag) == null) {
            return false;
        }
        ByteBuffer eblcTable = getTableBuffer(EBLCTag);
        int numSizes = eblcTable.getInt(4);
        for (int i=0;i<numSizes;i++) {
            int ppemY = eblcTable.get(8+(i*48)+45) &0xff;
            if (ppemY == ptSize) {
                return true;
            }
        }
        return false;
    }
    public String getFullName() {
        return fullName;
    }
    @Override
    protected void setStyle() {
        setStyle(getTableBuffer(os_2Tag));
    }
    private static final int fsSelectionItalicBit  = 0x00001;
    private static final int fsSelectionBoldBit    = 0x00020;
    private static final int fsSelectionRegularBit = 0x00040;
    private void setStyle(ByteBuffer os_2Table) {
        if (os_2Table == null || os_2Table.capacity() < 64) {
            super.setStyle();
            return;
        }
        int fsSelection = os_2Table.getChar(62) & 0xffff;
        int italic  = fsSelection & fsSelectionItalicBit;
        int bold    = fsSelection & fsSelectionBoldBit;
        int regular = fsSelection & fsSelectionRegularBit;
        if (regular!=0 && ((italic|bold)!=0)) {
            super.setStyle();
            return;
        } else if ((regular|italic|bold) == 0) {
            super.setStyle();
            return;
        }
        switch (bold|italic) {
        case fsSelectionItalicBit:
            style = Font.ITALIC;
            break;
        case fsSelectionBoldBit:
            if (FontUtilities.isSolaris && platName.endsWith("HG-GothicB.ttf")) {
                style = Font.PLAIN;
            } else {
                style = Font.BOLD;
            }
            break;
        case fsSelectionBoldBit|fsSelectionItalicBit:
            style = Font.BOLD|Font.ITALIC;
        }
    }
    private float stSize, stPos, ulSize, ulPos;
    private void setStrikethroughMetrics(ByteBuffer os_2Table, int upem) {
        if (os_2Table == null || os_2Table.capacity() < 30 || upem < 0) {
            stSize = .05f;
            stPos = -.4f;
            return;
        }
        ShortBuffer sb = os_2Table.asShortBuffer();
        stSize = sb.get(13) / (float)upem;
        stPos = -sb.get(14) / (float)upem;
    }
    private void setUnderlineMetrics(ByteBuffer postTable, int upem) {
        if (postTable == null || postTable.capacity() < 12 || upem < 0) {
            ulSize = .05f;
            ulPos = .1f;
            return;
        }
        ShortBuffer sb = postTable.asShortBuffer();
        ulSize = sb.get(5) / (float)upem;
        ulPos = -sb.get(4) / (float)upem;
    }
    @Override
    public void getStyleMetrics(float pointSize, float[] metrics, int offset) {
        if (ulSize == 0f && ulPos == 0f) {
            ByteBuffer head_Table = getTableBuffer(headTag);
            int upem = -1;
            if (head_Table != null && head_Table.capacity() >= 18) {
                ShortBuffer sb = head_Table.asShortBuffer();
                upem = sb.get(9) & 0xffff;
            }
            ByteBuffer os2_Table = getTableBuffer(os_2Tag);
            setStrikethroughMetrics(os2_Table, upem);
            ByteBuffer post_Table = getTableBuffer(postTag);
            setUnderlineMetrics(post_Table, upem);
        }
        metrics[offset] = stPos * pointSize;
        metrics[offset+1] = stSize * pointSize;
        metrics[offset+2] = ulPos * pointSize;
        metrics[offset+3] = ulSize * pointSize;
    }
    private String makeString(byte[] bytes, int len, short encoding) {
        if (encoding >=2 && encoding <= 6) {
             byte[] oldbytes = bytes;
             int oldlen = len;
             bytes = new byte[oldlen];
             len = 0;
             for (int i=0; i<oldlen; i++) {
                 if (oldbytes[i] != 0) {
                     bytes[len++] = oldbytes[i];
                 }
             }
         }
        String charset;
        switch (encoding) {
            case 1:  charset = "UTF-16";  break; 
            case 0:  charset = "UTF-16";  break; 
            case 2:  charset = "SJIS";    break;
            case 3:  charset = "GBK";     break;
            case 4:  charset = "MS950";   break;
            case 5:  charset = "EUC_KR";  break;
            case 6:  charset = "Johab";   break;
            default: charset = "UTF-16";  break;
        }
        try {
            return new String(bytes, 0, len, charset);
        } catch (UnsupportedEncodingException e) {
            if (FontUtilities.isLogging()) {
                FontUtilities.getLogger().warning(e + " EncodingID=" + encoding);
            }
            return new String(bytes, 0, len);
        } catch (Throwable t) {
            return null;
        }
    }
    protected void initNames() {
        byte[] name = new byte[256];
        ByteBuffer buffer = getTableBuffer(nameTag);
        if (buffer != null) {
            ShortBuffer sbuffer = buffer.asShortBuffer();
            sbuffer.get(); 
            short numRecords = sbuffer.get();
            int stringPtr = sbuffer.get() & 0xffff;
            nameLocale = sun.awt.SunToolkit.getStartupLocale();
            short nameLocaleID = getLCIDFromLocale(nameLocale);
            for (int i=0; i<numRecords; i++) {
                short platformID = sbuffer.get();
                if (platformID != MS_PLATFORM_ID) {
                    sbuffer.position(sbuffer.position()+5);
                    continue; 
                }
                short encodingID = sbuffer.get();
                short langID     = sbuffer.get();
                short nameID     = sbuffer.get();
                int nameLen    = ((int) sbuffer.get()) & 0xffff;
                int namePtr    = (((int) sbuffer.get()) & 0xffff) + stringPtr;
                String tmpName = null;
                switch (nameID) {
                case FAMILY_NAME_ID:
                    if (familyName == null || langID == ENGLISH_LOCALE_ID ||
                        langID == nameLocaleID)
                    {
                        buffer.position(namePtr);
                        buffer.get(name, 0, nameLen);
                        tmpName = makeString(name, nameLen, encodingID);
                        if (familyName == null || langID == ENGLISH_LOCALE_ID){
                            familyName = tmpName;
                        }
                        if (langID == nameLocaleID) {
                            localeFamilyName = tmpName;
                        }
                    }
                    break;
                case FULL_NAME_ID:
                    if (fullName == null || langID == ENGLISH_LOCALE_ID ||
                        langID == nameLocaleID)
                    {
                        buffer.position(namePtr);
                        buffer.get(name, 0, nameLen);
                        tmpName = makeString(name, nameLen, encodingID);
                        if (fullName == null || langID == ENGLISH_LOCALE_ID) {
                            fullName = tmpName;
                        }
                        if (langID == nameLocaleID) {
                            localeFullName = tmpName;
                        }
                    }
                    break;
                }
            }
            if (localeFamilyName == null) {
                localeFamilyName = familyName;
            }
            if (localeFullName == null) {
                localeFullName = fullName;
            }
        }
    }
    protected String lookupName(short findLocaleID, int findNameID) {
        String foundName = null;
        byte[] name = new byte[1024];
        ByteBuffer buffer = getTableBuffer(nameTag);
        if (buffer != null) {
            ShortBuffer sbuffer = buffer.asShortBuffer();
            sbuffer.get(); 
            short numRecords = sbuffer.get();
            int stringPtr = ((int) sbuffer.get()) & 0xffff;
            for (int i=0; i<numRecords; i++) {
                short platformID = sbuffer.get();
                if (platformID != MS_PLATFORM_ID) {
                    sbuffer.position(sbuffer.position()+5);
                    continue; 
                }
                short encodingID = sbuffer.get();
                short langID     = sbuffer.get();
                short nameID     = sbuffer.get();
                int   nameLen    = ((int) sbuffer.get()) & 0xffff;
                int   namePtr    = (((int) sbuffer.get()) & 0xffff) + stringPtr;
                if (nameID == findNameID &&
                    ((foundName == null && langID == ENGLISH_LOCALE_ID)
                     || langID == findLocaleID)) {
                    buffer.position(namePtr);
                    buffer.get(name, 0, nameLen);
                    foundName = makeString(name, nameLen, encodingID);
                    if (langID == findLocaleID) {
                        return foundName;
                    }
                }
            }
        }
        return foundName;
    }
    public int getFontCount() {
        return directoryCount;
    }
    protected synchronized FontScaler getScaler() {
        if (scaler == null) {
            scaler = FontScaler.getScaler(this, fontIndex,
                supportsCJK, fileSize);
        }
        return scaler;
    }
    @Override
    public String getPostscriptName() {
        String name = lookupName(ENGLISH_LOCALE_ID, POSTSCRIPT_NAME_ID);
        if (name == null) {
            return fullName;
        } else {
            return name;
        }
    }
    @Override
    public String getFontName(Locale locale) {
        if (locale == null) {
            return fullName;
        } else if (locale.equals(nameLocale) && localeFullName != null) {
            return localeFullName;
        } else {
            short localeID = getLCIDFromLocale(locale);
            String name = lookupName(localeID, FULL_NAME_ID);
            if (name == null) {
                return fullName;
            } else {
                return name;
            }
        }
    }
    private static void addLCIDMapEntry(Map<String, Short> map,
                                        String key, short value) {
        map.put(key, Short.valueOf(value));
    }
    private static synchronized void createLCIDMap() {
        if (lcidMap != null) {
            return;
        }
        Map<String, Short> map = new HashMap<String, Short>(200);
        addLCIDMapEntry(map, "ar", (short) 0x0401);
        addLCIDMapEntry(map, "bg", (short) 0x0402);
        addLCIDMapEntry(map, "ca", (short) 0x0403);
        addLCIDMapEntry(map, "zh", (short) 0x0404);
        addLCIDMapEntry(map, "cs", (short) 0x0405);
        addLCIDMapEntry(map, "da", (short) 0x0406);
        addLCIDMapEntry(map, "de", (short) 0x0407);
        addLCIDMapEntry(map, "el", (short) 0x0408);
        addLCIDMapEntry(map, "es", (short) 0x040a);
        addLCIDMapEntry(map, "fi", (short) 0x040b);
        addLCIDMapEntry(map, "fr", (short) 0x040c);
        addLCIDMapEntry(map, "iw", (short) 0x040d);
        addLCIDMapEntry(map, "hu", (short) 0x040e);
        addLCIDMapEntry(map, "is", (short) 0x040f);
        addLCIDMapEntry(map, "it", (short) 0x0410);
        addLCIDMapEntry(map, "ja", (short) 0x0411);
        addLCIDMapEntry(map, "ko", (short) 0x0412);
        addLCIDMapEntry(map, "nl", (short) 0x0413);
        addLCIDMapEntry(map, "no", (short) 0x0414);
        addLCIDMapEntry(map, "pl", (short) 0x0415);
        addLCIDMapEntry(map, "pt", (short) 0x0416);
        addLCIDMapEntry(map, "rm", (short) 0x0417);
        addLCIDMapEntry(map, "ro", (short) 0x0418);
        addLCIDMapEntry(map, "ru", (short) 0x0419);
        addLCIDMapEntry(map, "hr", (short) 0x041a);
        addLCIDMapEntry(map, "sk", (short) 0x041b);
        addLCIDMapEntry(map, "sq", (short) 0x041c);
        addLCIDMapEntry(map, "sv", (short) 0x041d);
        addLCIDMapEntry(map, "th", (short) 0x041e);
        addLCIDMapEntry(map, "tr", (short) 0x041f);
        addLCIDMapEntry(map, "ur", (short) 0x0420);
        addLCIDMapEntry(map, "in", (short) 0x0421);
        addLCIDMapEntry(map, "uk", (short) 0x0422);
        addLCIDMapEntry(map, "be", (short) 0x0423);
        addLCIDMapEntry(map, "sl", (short) 0x0424);
        addLCIDMapEntry(map, "et", (short) 0x0425);
        addLCIDMapEntry(map, "lv", (short) 0x0426);
        addLCIDMapEntry(map, "lt", (short) 0x0427);
        addLCIDMapEntry(map, "fa", (short) 0x0429);
        addLCIDMapEntry(map, "vi", (short) 0x042a);
        addLCIDMapEntry(map, "hy", (short) 0x042b);
        addLCIDMapEntry(map, "eu", (short) 0x042d);
        addLCIDMapEntry(map, "mk", (short) 0x042f);
        addLCIDMapEntry(map, "tn", (short) 0x0432);
        addLCIDMapEntry(map, "xh", (short) 0x0434);
        addLCIDMapEntry(map, "zu", (short) 0x0435);
        addLCIDMapEntry(map, "af", (short) 0x0436);
        addLCIDMapEntry(map, "ka", (short) 0x0437);
        addLCIDMapEntry(map, "fo", (short) 0x0438);
        addLCIDMapEntry(map, "hi", (short) 0x0439);
        addLCIDMapEntry(map, "mt", (short) 0x043a);
        addLCIDMapEntry(map, "se", (short) 0x043b);
        addLCIDMapEntry(map, "gd", (short) 0x043c);
        addLCIDMapEntry(map, "ms", (short) 0x043e);
        addLCIDMapEntry(map, "kk", (short) 0x043f);
        addLCIDMapEntry(map, "ky", (short) 0x0440);
        addLCIDMapEntry(map, "sw", (short) 0x0441);
        addLCIDMapEntry(map, "tt", (short) 0x0444);
        addLCIDMapEntry(map, "bn", (short) 0x0445);
        addLCIDMapEntry(map, "pa", (short) 0x0446);
        addLCIDMapEntry(map, "gu", (short) 0x0447);
        addLCIDMapEntry(map, "ta", (short) 0x0449);
        addLCIDMapEntry(map, "te", (short) 0x044a);
        addLCIDMapEntry(map, "kn", (short) 0x044b);
        addLCIDMapEntry(map, "ml", (short) 0x044c);
        addLCIDMapEntry(map, "mr", (short) 0x044e);
        addLCIDMapEntry(map, "sa", (short) 0x044f);
        addLCIDMapEntry(map, "mn", (short) 0x0450);
        addLCIDMapEntry(map, "cy", (short) 0x0452);
        addLCIDMapEntry(map, "gl", (short) 0x0456);
        addLCIDMapEntry(map, "dv", (short) 0x0465);
        addLCIDMapEntry(map, "qu", (short) 0x046b);
        addLCIDMapEntry(map, "mi", (short) 0x0481);
        addLCIDMapEntry(map, "ar_IQ", (short) 0x0801);
        addLCIDMapEntry(map, "zh_CN", (short) 0x0804);
        addLCIDMapEntry(map, "de_CH", (short) 0x0807);
        addLCIDMapEntry(map, "en_GB", (short) 0x0809);
        addLCIDMapEntry(map, "es_MX", (short) 0x080a);
        addLCIDMapEntry(map, "fr_BE", (short) 0x080c);
        addLCIDMapEntry(map, "it_CH", (short) 0x0810);
        addLCIDMapEntry(map, "nl_BE", (short) 0x0813);
        addLCIDMapEntry(map, "no_NO_NY", (short) 0x0814);
        addLCIDMapEntry(map, "pt_PT", (short) 0x0816);
        addLCIDMapEntry(map, "ro_MD", (short) 0x0818);
        addLCIDMapEntry(map, "ru_MD", (short) 0x0819);
        addLCIDMapEntry(map, "sr_CS", (short) 0x081a);
        addLCIDMapEntry(map, "sv_FI", (short) 0x081d);
        addLCIDMapEntry(map, "az_AZ", (short) 0x082c);
        addLCIDMapEntry(map, "se_SE", (short) 0x083b);
        addLCIDMapEntry(map, "ga_IE", (short) 0x083c);
        addLCIDMapEntry(map, "ms_BN", (short) 0x083e);
        addLCIDMapEntry(map, "uz_UZ", (short) 0x0843);
        addLCIDMapEntry(map, "qu_EC", (short) 0x086b);
        addLCIDMapEntry(map, "ar_EG", (short) 0x0c01);
        addLCIDMapEntry(map, "zh_HK", (short) 0x0c04);
        addLCIDMapEntry(map, "de_AT", (short) 0x0c07);
        addLCIDMapEntry(map, "en_AU", (short) 0x0c09);
        addLCIDMapEntry(map, "fr_CA", (short) 0x0c0c);
        addLCIDMapEntry(map, "sr_CS", (short) 0x0c1a);
        addLCIDMapEntry(map, "se_FI", (short) 0x0c3b);
        addLCIDMapEntry(map, "qu_PE", (short) 0x0c6b);
        addLCIDMapEntry(map, "ar_LY", (short) 0x1001);
        addLCIDMapEntry(map, "zh_SG", (short) 0x1004);
        addLCIDMapEntry(map, "de_LU", (short) 0x1007);
        addLCIDMapEntry(map, "en_CA", (short) 0x1009);
        addLCIDMapEntry(map, "es_GT", (short) 0x100a);
        addLCIDMapEntry(map, "fr_CH", (short) 0x100c);
        addLCIDMapEntry(map, "hr_BA", (short) 0x101a);
        addLCIDMapEntry(map, "ar_DZ", (short) 0x1401);
        addLCIDMapEntry(map, "zh_MO", (short) 0x1404);
        addLCIDMapEntry(map, "de_LI", (short) 0x1407);
        addLCIDMapEntry(map, "en_NZ", (short) 0x1409);
        addLCIDMapEntry(map, "es_CR", (short) 0x140a);
        addLCIDMapEntry(map, "fr_LU", (short) 0x140c);
        addLCIDMapEntry(map, "bs_BA", (short) 0x141a);
        addLCIDMapEntry(map, "ar_MA", (short) 0x1801);
        addLCIDMapEntry(map, "en_IE", (short) 0x1809);
        addLCIDMapEntry(map, "es_PA", (short) 0x180a);
        addLCIDMapEntry(map, "fr_MC", (short) 0x180c);
        addLCIDMapEntry(map, "sr_BA", (short) 0x181a);
        addLCIDMapEntry(map, "ar_TN", (short) 0x1c01);
        addLCIDMapEntry(map, "en_ZA", (short) 0x1c09);
        addLCIDMapEntry(map, "es_DO", (short) 0x1c0a);
        addLCIDMapEntry(map, "sr_BA", (short) 0x1c1a);
        addLCIDMapEntry(map, "ar_OM", (short) 0x2001);
        addLCIDMapEntry(map, "en_JM", (short) 0x2009);
        addLCIDMapEntry(map, "es_VE", (short) 0x200a);
        addLCIDMapEntry(map, "ar_YE", (short) 0x2401);
        addLCIDMapEntry(map, "es_CO", (short) 0x240a);
        addLCIDMapEntry(map, "ar_SY", (short) 0x2801);
        addLCIDMapEntry(map, "en_BZ", (short) 0x2809);
        addLCIDMapEntry(map, "es_PE", (short) 0x280a);
        addLCIDMapEntry(map, "ar_JO", (short) 0x2c01);
        addLCIDMapEntry(map, "en_TT", (short) 0x2c09);
        addLCIDMapEntry(map, "es_AR", (short) 0x2c0a);
        addLCIDMapEntry(map, "ar_LB", (short) 0x3001);
        addLCIDMapEntry(map, "en_ZW", (short) 0x3009);
        addLCIDMapEntry(map, "es_EC", (short) 0x300a);
        addLCIDMapEntry(map, "ar_KW", (short) 0x3401);
        addLCIDMapEntry(map, "en_PH", (short) 0x3409);
        addLCIDMapEntry(map, "es_CL", (short) 0x340a);
        addLCIDMapEntry(map, "ar_AE", (short) 0x3801);
        addLCIDMapEntry(map, "es_UY", (short) 0x380a);
        addLCIDMapEntry(map, "ar_BH", (short) 0x3c01);
        addLCIDMapEntry(map, "es_PY", (short) 0x3c0a);
        addLCIDMapEntry(map, "ar_QA", (short) 0x4001);
        addLCIDMapEntry(map, "es_BO", (short) 0x400a);
        addLCIDMapEntry(map, "es_SV", (short) 0x440a);
        addLCIDMapEntry(map, "es_HN", (short) 0x480a);
        addLCIDMapEntry(map, "es_NI", (short) 0x4c0a);
        addLCIDMapEntry(map, "es_PR", (short) 0x500a);
        lcidMap = map;
    }
    private static short getLCIDFromLocale(Locale locale) {
        if (locale.equals(Locale.US)) {
            return US_LCID;
        }
        if (lcidMap == null) {
            createLCIDMap();
        }
        String key = locale.toString();
        while (!"".equals(key)) {
            Short lcidObject = (Short) lcidMap.get(key);
            if (lcidObject != null) {
                return lcidObject.shortValue();
            }
            int pos = key.lastIndexOf('_');
            if (pos < 1) {
                return US_LCID;
            }
            key = key.substring(0, pos);
        }
        return US_LCID;
    }
    @Override
    public String getFamilyName(Locale locale) {
        if (locale == null) {
            return familyName;
        } else if (locale.equals(nameLocale) && localeFamilyName != null) {
            return localeFamilyName;
        } else {
            short localeID = getLCIDFromLocale(locale);
            String name = lookupName(localeID, FAMILY_NAME_ID);
            if (name == null) {
                return familyName;
            } else {
                return name;
            }
        }
    }
    public CharToGlyphMapper getMapper() {
        if (mapper == null) {
            mapper = new TrueTypeGlyphMapper(this);
        }
        return mapper;
    }
    protected void initAllNames(int requestedID, HashSet names) {
        byte[] name = new byte[256];
        ByteBuffer buffer = getTableBuffer(nameTag);
        if (buffer != null) {
            ShortBuffer sbuffer = buffer.asShortBuffer();
            sbuffer.get(); 
            short numRecords = sbuffer.get();
            int stringPtr = ((int) sbuffer.get()) & 0xffff;
            for (int i=0; i<numRecords; i++) {
                short platformID = sbuffer.get();
                if (platformID != MS_PLATFORM_ID) {
                    sbuffer.position(sbuffer.position()+5);
                    continue; 
                }
                short encodingID = sbuffer.get();
                short langID     = sbuffer.get();
                short nameID     = sbuffer.get();
                int   nameLen    = ((int) sbuffer.get()) & 0xffff;
                int   namePtr    = (((int) sbuffer.get()) & 0xffff) + stringPtr;
                if (nameID == requestedID) {
                    buffer.position(namePtr);
                    buffer.get(name, 0, nameLen);
                    names.add(makeString(name, nameLen, encodingID));
                }
            }
        }
    }
    String[] getAllFamilyNames() {
        HashSet aSet = new HashSet();
        try {
            initAllNames(FAMILY_NAME_ID, aSet);
        } catch (Exception e) {
        }
        return (String[])aSet.toArray(new String[0]);
    }
    String[] getAllFullNames() {
        HashSet aSet = new HashSet();
        try {
            initAllNames(FULL_NAME_ID, aSet);
        } catch (Exception e) {
        }
        return (String[])aSet.toArray(new String[0]);
    }
    @Override
    Point2D.Float getGlyphPoint(long pScalerContext,
                                int glyphCode, int ptNumber) {
        try {
            return getScaler().getGlyphPoint(pScalerContext,
                                             glyphCode, ptNumber);
        } catch(FontScalerException fe) {
            return null;
        }
    }
    private char[] gaspTable;
    private char[] getGaspTable() {
        if (gaspTable != null) {
            return gaspTable;
        }
        ByteBuffer buffer = getTableBuffer(gaspTag);
        if (buffer == null) {
            return gaspTable = new char[0];
        }
        CharBuffer cbuffer = buffer.asCharBuffer();
        char format = cbuffer.get();
        if (format > 1) { 
            return gaspTable = new char[0];
        }
        char numRanges = cbuffer.get();
        if (4+numRanges*4 > getTableSize(gaspTag)) { 
            return gaspTable = new char[0];
        }
        gaspTable = new char[2*numRanges];
        cbuffer.get(gaspTable);
        return gaspTable;
    }
    @Override
    public boolean useAAForPtSize(int ptsize) {
        char[] gasp = getGaspTable();
        if (gasp.length > 0) {
            for (int i=0;i<gasp.length;i+=2) {
                if (ptsize <= gasp[i]) {
                    return ((gasp[i+1] & 0x2) != 0); 
                }
            }
            return true;
        }
        if (style == Font.BOLD) {
            return true;
        } else {
            return ptsize <= 8 || ptsize >= 18;
        }
    }
    @Override
    public boolean hasSupplementaryChars() {
        return ((TrueTypeGlyphMapper)getMapper()).hasSupplementaryChars();
    }
    @Override
    public String toString() {
        return "** TrueType Font: Family="+familyName+ " Name="+fullName+
            " style="+style+" fileName="+getPublicFileName();
    }
}
