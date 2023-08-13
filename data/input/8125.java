public final class SystemFlavorMap implements FlavorMap, FlavorTable {
    private static String JavaMIME = "JAVA_DATAFLAVOR:";
    private static final WeakHashMap flavorMaps = new WeakHashMap();
    private static final String keyValueSeparators = "=: \t\r\n\f";
    private static final String strictKeyValueSeparators = "=:";
    private static final String whiteSpaceChars = " \t\r\n\f";
    private static final String[] UNICODE_TEXT_CLASSES = {
        "java.io.Reader", "java.lang.String", "java.nio.CharBuffer", "\"[C\""
    };
    private static final String[] ENCODED_TEXT_CLASSES = {
        "java.io.InputStream", "java.nio.ByteBuffer", "\"[B\""
    };
    private static final String TEXT_PLAIN_BASE_TYPE = "text/plain";
    private static final boolean SYNTHESIZE_IF_NOT_FOUND = true;
    private Map nativeToFlavor = new HashMap();
    private Map getNativeToFlavor() {
        if (!isMapInitialized) {
            initSystemFlavorMap();
        }
        return nativeToFlavor;
    }
    private Map flavorToNative = new HashMap();
    private synchronized Map getFlavorToNative() {
        if (!isMapInitialized) {
            initSystemFlavorMap();
        }
        return flavorToNative;
    }
    private boolean isMapInitialized = false;
    private Map getNativesForFlavorCache = new HashMap();
    private Map getFlavorsForNativeCache = new HashMap();
    private Set disabledMappingGenerationKeys = new HashSet();
    public static FlavorMap getDefaultFlavorMap() {
        ClassLoader contextClassLoader =
            Thread.currentThread().getContextClassLoader();
        if (contextClassLoader == null) {
            contextClassLoader = ClassLoader.getSystemClassLoader();
        }
        FlavorMap fm;
        synchronized(flavorMaps) {
            fm = (FlavorMap)flavorMaps.get(contextClassLoader);
            if (fm == null) {
                fm = new SystemFlavorMap();
                flavorMaps.put(contextClassLoader, fm);
            }
        }
        return fm;
    }
    private SystemFlavorMap() {
    }
    private void initSystemFlavorMap() {
        if (isMapInitialized) {
            return;
        }
        isMapInitialized = true;
        BufferedReader flavormapDotProperties =
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<BufferedReader>() {
                    public BufferedReader run() {
                        String fileName =
                            System.getProperty("java.home") +
                            File.separator +
                            "lib" +
                            File.separator +
                            "flavormap.properties";
                        try {
                            return new BufferedReader
                                (new InputStreamReader
                                    (new File(fileName).toURI().toURL().openStream(), "ISO-8859-1"));
                        } catch (MalformedURLException e) {
                            System.err.println("MalformedURLException:" + e + " while loading default flavormap.properties file:" + fileName);
                        } catch (IOException e) {
                            System.err.println("IOException:" + e + " while loading default flavormap.properties file:" + fileName);
                        }
                        return null;
                    }
                });
        BufferedReader flavormapURL =
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<BufferedReader>() {
                    public BufferedReader run() {
                        String url = Toolkit.getProperty("AWT.DnD.flavorMapFileURL", null);
                        if (url == null) {
                            return null;
                        }
                        try {
                            return new BufferedReader
                                (new InputStreamReader
                                    (new URL(url).openStream(), "ISO-8859-1"));
                        } catch (MalformedURLException e) {
                            System.err.println("MalformedURLException:" + e + " while reading AWT.DnD.flavorMapFileURL:" + url);
                        } catch (IOException e) {
                            System.err.println("IOException:" + e + " while reading AWT.DnD.flavorMapFileURL:" + url);
                        }
                        return null;
                    }
                });
        if (flavormapDotProperties != null) {
            try {
                parseAndStoreReader(flavormapDotProperties);
            } catch (IOException e) {
                System.err.println("IOException:" + e + " while parsing default flavormap.properties file");
            }
        }
        if (flavormapURL != null) {
            try {
                parseAndStoreReader(flavormapURL);
            } catch (IOException e) {
                System.err.println("IOException:" + e + " while parsing AWT.DnD.flavorMapFileURL");
            }
        }
    }
    private void parseAndStoreReader(BufferedReader in) throws IOException {
        while (true) {
            String line = in.readLine();
            if (line == null) {
                return;
            }
            if (line.length() > 0) {
                char firstChar = line.charAt(0);
                if (firstChar != '#' && firstChar != '!') {
                    while (continueLine(line)) {
                        String nextLine = in.readLine();
                        if (nextLine == null) {
                            nextLine = "";
                        }
                        String loppedLine =
                            line.substring(0, line.length() - 1);
                        int startIndex = 0;
                        for(; startIndex < nextLine.length(); startIndex++) {
                            if (whiteSpaceChars.
                                    indexOf(nextLine.charAt(startIndex)) == -1)
                            {
                                break;
                            }
                        }
                        nextLine = nextLine.substring(startIndex,
                                                      nextLine.length());
                        line = loppedLine+nextLine;
                    }
                    int len = line.length();
                    int keyStart = 0;
                    for(; keyStart < len; keyStart++) {
                        if(whiteSpaceChars.
                               indexOf(line.charAt(keyStart)) == -1) {
                            break;
                        }
                    }
                    if (keyStart == len) {
                        continue;
                    }
                    int separatorIndex = keyStart;
                    for(; separatorIndex < len; separatorIndex++) {
                        char currentChar = line.charAt(separatorIndex);
                        if (currentChar == '\\') {
                            separatorIndex++;
                        } else if (keyValueSeparators.
                                       indexOf(currentChar) != -1) {
                            break;
                        }
                    }
                    int valueIndex = separatorIndex;
                    for (; valueIndex < len; valueIndex++) {
                        if (whiteSpaceChars.
                                indexOf(line.charAt(valueIndex)) == -1) {
                            break;
                        }
                    }
                    if (valueIndex < len) {
                        if (strictKeyValueSeparators.
                                indexOf(line.charAt(valueIndex)) != -1) {
                            valueIndex++;
                        }
                    }
                    while (valueIndex < len) {
                        if (whiteSpaceChars.
                                indexOf(line.charAt(valueIndex)) == -1) {
                            break;
                        }
                        valueIndex++;
                    }
                    String key = line.substring(keyStart, separatorIndex);
                    String value = (separatorIndex < len)
                        ? line.substring(valueIndex, len)
                        : "";
                    key = loadConvert(key);
                    value = loadConvert(value);
                    try {
                        MimeType mime = new MimeType(value);
                        if ("text".equals(mime.getPrimaryType())) {
                            String charset = mime.getParameter("charset");
                            if (DataTransferer.doesSubtypeSupportCharset
                                    (mime.getSubType(), charset))
                            {
                                DataTransferer transferer =
                                    DataTransferer.getInstance();
                                if (transferer != null) {
                                    transferer.registerTextFlavorProperties
                                        (key, charset,
                                         mime.getParameter("eoln"),
                                         mime.getParameter("terminators"));
                                }
                            }
                            mime.removeParameter("charset");
                            mime.removeParameter("class");
                            mime.removeParameter("eoln");
                            mime.removeParameter("terminators");
                            value = mime.toString();
                        }
                    } catch (MimeTypeParseException e) {
                        e.printStackTrace();
                        continue;
                    }
                    DataFlavor flavor;
                    try {
                        flavor = new DataFlavor(value);
                    } catch (Exception e) {
                        try {
                            flavor = new DataFlavor(value, (String)null);
                        } catch (Exception ee) {
                            ee.printStackTrace();
                            continue;
                        }
                    }
                    if ("text".equals(flavor.getPrimaryType())) {
                        store(value, key, getFlavorToNative());
                        store(key, value, getNativeToFlavor());
                    } else {
                        store(flavor, key, getFlavorToNative());
                        store(key, flavor, getNativeToFlavor());
                    }
                }
            }
        }
    }
    private boolean continueLine (String line) {
        int slashCount = 0;
        int index = line.length() - 1;
        while((index >= 0) && (line.charAt(index--) == '\\')) {
            slashCount++;
        }
        return (slashCount % 2 == 1);
    }
    private String loadConvert(String theString) {
        char aChar;
        int len = theString.length();
        StringBuilder outBuffer = new StringBuilder(len);
        for (int x = 0; x < len; ) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                          case '0': case '1': case '2': case '3': case '4':
                          case '5': case '6': case '7': case '8': case '9': {
                             value = (value << 4) + aChar - '0';
                             break;
                          }
                          case 'a': case 'b': case 'c':
                          case 'd': case 'e': case 'f': {
                             value = (value << 4) + 10 + aChar - 'a';
                             break;
                          }
                          case 'A': case 'B': case 'C':
                          case 'D': case 'E': case 'F': {
                             value = (value << 4) + 10 + aChar - 'A';
                             break;
                          }
                          default: {
                              throw new IllegalArgumentException(
                                           "Malformed \\uxxxx encoding.");
                          }
                        }
                    }
                    outBuffer.append((char)value);
                } else {
                    if (aChar == 't') {
                        aChar = '\t';
                    } else if (aChar == 'r') {
                        aChar = '\r';
                    } else if (aChar == 'n') {
                        aChar = '\n';
                    } else if (aChar == 'f') {
                        aChar = '\f';
                    }
                    outBuffer.append(aChar);
                }
            } else {
                outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }
    private void store(Object hashed, Object listed, Map map) {
        List list = (List)map.get(hashed);
        if (list == null) {
            list = new ArrayList(1);
            map.put(hashed, list);
        }
        if (!list.contains(listed)) {
            list.add(listed);
        }
    }
    private List nativeToFlavorLookup(String nat) {
        List flavors = (List)getNativeToFlavor().get(nat);
        if (nat != null && !disabledMappingGenerationKeys.contains(nat)) {
            DataTransferer transferer = DataTransferer.getInstance();
            if (transferer != null) {
                List platformFlavors =
                    transferer.getPlatformMappingsForNative(nat);
                if (!platformFlavors.isEmpty()) {
                    if (flavors != null) {
                        platformFlavors.removeAll(new HashSet(flavors));
                        platformFlavors.addAll(flavors);
                    }
                    flavors = platformFlavors;
                }
            }
        }
        if (flavors == null && isJavaMIMEType(nat)) {
            String decoded = decodeJavaMIMEType(nat);
            DataFlavor flavor = null;
            try {
                flavor = new DataFlavor(decoded);
            } catch (Exception e) {
                System.err.println("Exception \"" + e.getClass().getName() +
                                   ": " + e.getMessage()  +
                                   "\"while constructing DataFlavor for: " +
                                   decoded);
            }
            if (flavor != null) {
                flavors = new ArrayList(1);
                getNativeToFlavor().put(nat, flavors);
                flavors.add(flavor);
                getFlavorsForNativeCache.remove(nat);
                getFlavorsForNativeCache.remove(null);
                List natives = (List)getFlavorToNative().get(flavor);
                if (natives == null) {
                    natives = new ArrayList(1);
                    getFlavorToNative().put(flavor, natives);
                }
                natives.add(nat);
                getNativesForFlavorCache.remove(flavor);
                getNativesForFlavorCache.remove(null);
            }
        }
        return (flavors != null) ? flavors : new ArrayList(0);
    }
    private List flavorToNativeLookup(final DataFlavor flav,
                                      final boolean synthesize) {
        List natives = (List)getFlavorToNative().get(flav);
        if (flav != null && !disabledMappingGenerationKeys.contains(flav)) {
            DataTransferer transferer = DataTransferer.getInstance();
            if (transferer != null) {
                List platformNatives =
                    transferer.getPlatformMappingsForFlavor(flav);
                if (!platformNatives.isEmpty()) {
                    if (natives != null) {
                        platformNatives.removeAll(new HashSet(natives));
                        platformNatives.addAll(natives);
                    }
                    natives = platformNatives;
                }
            }
        }
        if (natives == null) {
            if (synthesize) {
                String encoded = encodeDataFlavor(flav);
                natives = new ArrayList(1);
                getFlavorToNative().put(flav, natives);
                natives.add(encoded);
                getNativesForFlavorCache.remove(flav);
                getNativesForFlavorCache.remove(null);
                List flavors = (List)getNativeToFlavor().get(encoded);
                if (flavors == null) {
                    flavors = new ArrayList(1);
                    getNativeToFlavor().put(encoded, flavors);
                }
                flavors.add(flav);
                getFlavorsForNativeCache.remove(encoded);
                getFlavorsForNativeCache.remove(null);
            } else {
                natives = new ArrayList(0);
            }
        }
        return natives;
    }
    public synchronized List<String> getNativesForFlavor(DataFlavor flav) {
        List retval = null;
        SoftReference ref = (SoftReference)getNativesForFlavorCache.get(flav);
        if (ref != null) {
            retval = (List)ref.get();
            if (retval != null) {
                return new ArrayList(retval);
            }
        }
        if (flav == null) {
            retval = new ArrayList(getNativeToFlavor().keySet());
        } else if (disabledMappingGenerationKeys.contains(flav)) {
            retval = flavorToNativeLookup(flav, !SYNTHESIZE_IF_NOT_FOUND);
        } else if (DataTransferer.isFlavorCharsetTextType(flav)) {
            if ("text".equals(flav.getPrimaryType())) {
                retval = (List)getFlavorToNative().get(flav.mimeType.getBaseType());
                if (retval != null) {
                    retval = new ArrayList(retval);
                }
            }
            List textPlainList = (List)getFlavorToNative().get(TEXT_PLAIN_BASE_TYPE);
            if (textPlainList != null && !textPlainList.isEmpty()) {
                textPlainList = new ArrayList(textPlainList);
                if (retval != null && !retval.isEmpty()) {
                    textPlainList.removeAll(new HashSet(retval));
                    retval.addAll(textPlainList);
                } else {
                    retval = textPlainList;
                }
            }
            if (retval == null || retval.isEmpty()) {
                retval = flavorToNativeLookup(flav, SYNTHESIZE_IF_NOT_FOUND);
            } else {
                List explicitList =
                    flavorToNativeLookup(flav, !SYNTHESIZE_IF_NOT_FOUND);
                if (!explicitList.isEmpty()) {
                    explicitList = new ArrayList(explicitList);
                    explicitList.removeAll(new HashSet(retval));
                    retval.addAll(explicitList);
                }
            }
        } else if (DataTransferer.isFlavorNoncharsetTextType(flav)) {
            retval = (List)getFlavorToNative().get(flav.mimeType.getBaseType());
            if (retval == null || retval.isEmpty()) {
                retval = flavorToNativeLookup(flav, SYNTHESIZE_IF_NOT_FOUND);
            } else {
                List explicitList =
                    flavorToNativeLookup(flav, !SYNTHESIZE_IF_NOT_FOUND);
                if (!explicitList.isEmpty()) {
                    retval = new ArrayList(retval);
                    explicitList = new ArrayList(explicitList);
                    explicitList.removeAll(new HashSet(retval));
                    retval.addAll(explicitList);
                }
            }
        } else {
            retval = flavorToNativeLookup(flav, SYNTHESIZE_IF_NOT_FOUND);
        }
        getNativesForFlavorCache.put(flav, new SoftReference(retval));
        return new ArrayList(retval);
    }
    public synchronized List<DataFlavor> getFlavorsForNative(String nat) {
        SoftReference ref = (SoftReference)getFlavorsForNativeCache.get(nat);
        if (ref != null) {
            ArrayList retval = (ArrayList)ref.get();
            if (retval != null) {
                return (List)retval.clone();
            }
        }
        LinkedList retval = new LinkedList();
        if (nat == null) {
            List natives = getNativesForFlavor(null);
            HashSet dups = new HashSet(natives.size());
            for (Iterator natives_iter = natives.iterator();
                 natives_iter.hasNext(); )
            {
                List flavors =
                    getFlavorsForNative((String)natives_iter.next());
                for (Iterator flavors_iter = flavors.iterator();
                     flavors_iter.hasNext(); )
                {
                    Object flavor = flavors_iter.next();
                    if (dups.add(flavor)) {
                        retval.add(flavor);
                    }
                }
            }
        } else {
            List flavors = nativeToFlavorLookup(nat);
            if (disabledMappingGenerationKeys.contains(nat)) {
                return flavors;
            }
            HashSet dups = new HashSet(flavors.size());
            List flavorsAndbaseTypes = nativeToFlavorLookup(nat);
            for (Iterator flavorsAndbaseTypes_iter =
                     flavorsAndbaseTypes.iterator();
                 flavorsAndbaseTypes_iter.hasNext(); )
            {
                Object value = flavorsAndbaseTypes_iter.next();
                if (value instanceof String) {
                    String baseType = (String)value;
                    String subType = null;
                    try {
                        MimeType mimeType = new MimeType(baseType);
                        subType = mimeType.getSubType();
                    } catch (MimeTypeParseException mtpe) {
                        assert(false);
                    }
                    if (DataTransferer.doesSubtypeSupportCharset(subType,
                                                                 null)) {
                        if (TEXT_PLAIN_BASE_TYPE.equals(baseType) &&
                            dups.add(DataFlavor.stringFlavor))
                        {
                            retval.add(DataFlavor.stringFlavor);
                        }
                        for (int i = 0; i < UNICODE_TEXT_CLASSES.length; i++) {
                            DataFlavor toAdd = null;
                            try {
                                toAdd = new DataFlavor
                                    (baseType + ";charset=Unicode;class=" +
                                     UNICODE_TEXT_CLASSES[i]);
                            } catch (ClassNotFoundException cannotHappen) {
                            }
                            if (dups.add(toAdd)) {
                                retval.add(toAdd);
                            }
                        }
                        for (Iterator charset_iter =
                                 DataTransferer.standardEncodings();
                             charset_iter.hasNext(); )
                        {
                            String charset = (String)charset_iter.next();
                            for (int i = 0; i < ENCODED_TEXT_CLASSES.length;
                                 i++)
                            {
                                DataFlavor toAdd = null;
                                try {
                                    toAdd = new DataFlavor
                                        (baseType + ";charset=" + charset +
                                         ";class=" + ENCODED_TEXT_CLASSES[i]);
                                } catch (ClassNotFoundException cannotHappen) {
                                }
                                if (toAdd.equals(DataFlavor.plainTextFlavor)) {
                                    toAdd = DataFlavor.plainTextFlavor;
                                }
                                if (dups.add(toAdd)) {
                                    retval.add(toAdd);
                                }
                            }
                        }
                        if (TEXT_PLAIN_BASE_TYPE.equals(baseType) &&
                            dups.add(DataFlavor.plainTextFlavor))
                        {
                            retval.add(DataFlavor.plainTextFlavor);
                        }
                    } else {
                        for (int i = 0; i < ENCODED_TEXT_CLASSES.length; i++) {
                            DataFlavor toAdd = null;
                            try {
                                toAdd = new DataFlavor(baseType +
                                     ";class=" + ENCODED_TEXT_CLASSES[i]);
                            } catch (ClassNotFoundException cannotHappen) {
                            }
                            if (dups.add(toAdd)) {
                                retval.add(toAdd);
                            }
                        }
                    }
                } else {
                    DataFlavor flavor = (DataFlavor)value;
                    if (dups.add(flavor)) {
                        retval.add(flavor);
                    }
                }
            }
        }
        ArrayList arrayList = new ArrayList(retval);
        getFlavorsForNativeCache.put(nat, new SoftReference(arrayList));
        return (List)arrayList.clone();
    }
    public synchronized Map<DataFlavor,String>
        getNativesForFlavors(DataFlavor[] flavors)
    {
        if (flavors == null) {
            List flavor_list = getFlavorsForNative(null);
            flavors = new DataFlavor[flavor_list.size()];
            flavor_list.toArray(flavors);
        }
        HashMap retval = new HashMap(flavors.length, 1.0f);
        for (int i = 0; i < flavors.length; i++) {
            List natives = getNativesForFlavor(flavors[i]);
            String nat = (natives.isEmpty()) ? null : (String)natives.get(0);
            retval.put(flavors[i], nat);
        }
        return retval;
    }
    public synchronized Map<String,DataFlavor>
        getFlavorsForNatives(String[] natives)
    {
        if (natives == null) {
            List native_list = getNativesForFlavor(null);
            natives = new String[native_list.size()];
            native_list.toArray(natives);
        }
        HashMap retval = new HashMap(natives.length, 1.0f);
        for (int i = 0; i < natives.length; i++) {
            List flavors = getFlavorsForNative(natives[i]);
            DataFlavor flav = (flavors.isEmpty())
                ? null : (DataFlavor)flavors.get(0);
            retval.put(natives[i], flav);
        }
        return retval;
    }
    public synchronized void addUnencodedNativeForFlavor(DataFlavor flav,
                                                         String nat) {
        if (flav == null || nat == null) {
            throw new NullPointerException("null arguments not permitted");
        }
        List natives = (List)getFlavorToNative().get(flav);
        if (natives == null) {
            natives = new ArrayList(1);
            getFlavorToNative().put(flav, natives);
        } else if (natives.contains(nat)) {
            return;
        }
        natives.add(nat);
        getNativesForFlavorCache.remove(flav);
        getNativesForFlavorCache.remove(null);
    }
    public synchronized void setNativesForFlavor(DataFlavor flav,
                                                 String[] natives) {
        if (flav == null || natives == null) {
            throw new NullPointerException("null arguments not permitted");
        }
        getFlavorToNative().remove(flav);
        for (int i = 0; i < natives.length; i++) {
            addUnencodedNativeForFlavor(flav, natives[i]);
        }
        disabledMappingGenerationKeys.add(flav);
        getNativesForFlavorCache.remove(flav);
        getNativesForFlavorCache.remove(null);
    }
    public synchronized void addFlavorForUnencodedNative(String nat,
                                                         DataFlavor flav) {
        if (nat == null || flav == null) {
            throw new NullPointerException("null arguments not permitted");
        }
        List flavors = (List)getNativeToFlavor().get(nat);
        if (flavors == null) {
            flavors = new ArrayList(1);
            getNativeToFlavor().put(nat, flavors);
        } else if (flavors.contains(flav)) {
            return;
        }
        flavors.add(flav);
        getFlavorsForNativeCache.remove(nat);
        getFlavorsForNativeCache.remove(null);
    }
    public synchronized void setFlavorsForNative(String nat,
                                                 DataFlavor[] flavors) {
        if (nat == null || flavors == null) {
            throw new NullPointerException("null arguments not permitted");
        }
        getNativeToFlavor().remove(nat);
        for (int i = 0; i < flavors.length; i++) {
            addFlavorForUnencodedNative(nat, flavors[i]);
        }
        disabledMappingGenerationKeys.add(nat);
        getFlavorsForNativeCache.remove(nat);
        getFlavorsForNativeCache.remove(null);
    }
    public static String encodeJavaMIMEType(String mimeType) {
        return (mimeType != null)
            ? JavaMIME + mimeType
            : null;
    }
    public static String encodeDataFlavor(DataFlavor flav) {
        return (flav != null)
            ? SystemFlavorMap.encodeJavaMIMEType(flav.getMimeType())
            : null;
    }
    public static boolean isJavaMIMEType(String str) {
        return (str != null && str.startsWith(JavaMIME, 0));
    }
    public static String decodeJavaMIMEType(String nat) {
        return (isJavaMIMEType(nat))
            ? nat.substring(JavaMIME.length(), nat.length()).trim()
            : null;
    }
    public static DataFlavor decodeDataFlavor(String nat)
        throws ClassNotFoundException
    {
        String retval_str = SystemFlavorMap.decodeJavaMIMEType(nat);
        return (retval_str != null)
            ? new DataFlavor(retval_str)
            : null;
    }
}
