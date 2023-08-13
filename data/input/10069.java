public abstract class DataTransferer {
    public static final Class charArrayClass;
    public static final Class byteArrayClass;
    public static final DataFlavor plainTextStringFlavor;
    public static final DataFlavor javaTextEncodingFlavor;
    private static class StandardEncodingsHolder {
        private static final SortedSet standardEncodings = load();
        private static SortedSet load() {
            final Comparator comparator =
                    new CharsetComparator(IndexedComparator.SELECT_WORST);
            final SortedSet tempSet = new TreeSet(comparator);
            tempSet.add("US-ASCII");
            tempSet.add("ISO-8859-1");
            tempSet.add("UTF-8");
            tempSet.add("UTF-16BE");
            tempSet.add("UTF-16LE");
            tempSet.add("UTF-16");
            tempSet.add(getDefaultTextCharset());
            return Collections.unmodifiableSortedSet(tempSet);
        }
    }
    private static final Map textMIMESubtypeCharsetSupport;
    private static String defaultEncoding;
    private static final Set textNatives =
        Collections.synchronizedSet(new HashSet());
    private static final Map nativeCharsets =
        Collections.synchronizedMap(new HashMap());
    private static final Map nativeEOLNs =
        Collections.synchronizedMap(new HashMap());
    private static final Map nativeTerminators =
        Collections.synchronizedMap(new HashMap());
    private static final String DATA_CONVERTER_KEY = "DATA_CONVERTER_KEY";
    private static DataTransferer transferer;
    private static final PlatformLogger dtLog = PlatformLogger.getLogger("sun.awt.datatransfer.DataTransfer");
    static {
        Class tCharArrayClass = null, tByteArrayClass = null;
        try {
            tCharArrayClass = Class.forName("[C");
            tByteArrayClass = Class.forName("[B");
        } catch (ClassNotFoundException cannotHappen) {
        }
        charArrayClass = tCharArrayClass;
        byteArrayClass = tByteArrayClass;
        DataFlavor tPlainTextStringFlavor = null;
        try {
            tPlainTextStringFlavor = new DataFlavor
                ("text/plain;charset=Unicode;class=java.lang.String");
        } catch (ClassNotFoundException cannotHappen) {
        }
        plainTextStringFlavor = tPlainTextStringFlavor;
        DataFlavor tJavaTextEncodingFlavor = null;
        try {
            tJavaTextEncodingFlavor = new DataFlavor
                ("application/x-java-text-encoding;class=\"[B\"");
        } catch (ClassNotFoundException cannotHappen) {
        }
        javaTextEncodingFlavor = tJavaTextEncodingFlavor;
        Map tempMap = new HashMap(17);
        tempMap.put("sgml", Boolean.TRUE);
        tempMap.put("xml", Boolean.TRUE);
        tempMap.put("html", Boolean.TRUE);
        tempMap.put("enriched", Boolean.TRUE);
        tempMap.put("richtext", Boolean.TRUE);
        tempMap.put("uri-list", Boolean.TRUE);
        tempMap.put("directory", Boolean.TRUE);
        tempMap.put("css", Boolean.TRUE);
        tempMap.put("calendar", Boolean.TRUE);
        tempMap.put("plain", Boolean.TRUE);
        tempMap.put("rtf", Boolean.FALSE);
        tempMap.put("tab-separated-values", Boolean.FALSE);
        tempMap.put("t140", Boolean.FALSE);
        tempMap.put("rfc822-headers", Boolean.FALSE);
        tempMap.put("parityfec", Boolean.FALSE);
        textMIMESubtypeCharsetSupport = Collections.synchronizedMap(tempMap);
    }
    public static DataTransferer getInstance() {
        synchronized (DataTransferer.class) {
            if (transferer == null) {
                final String name = SunToolkit.getDataTransfererClassName();
                if (name != null) {
                    PrivilegedAction<DataTransferer> action = new PrivilegedAction<DataTransferer>()
                    {
                      public DataTransferer run() {
                          Class cls = null;
                          Method method = null;
                          DataTransferer ret = null;
                          try {
                              cls = Class.forName(name);
                          } catch (ClassNotFoundException e) {
                              ClassLoader cl = ClassLoader.
                                  getSystemClassLoader();
                              if (cl != null) {
                                  try {
                                      cls = cl.loadClass(name);
                                  } catch (ClassNotFoundException ee) {
                                      ee.printStackTrace();
                                      throw new AWTError("DataTransferer not found: " + name);
                                  }
                              }
                          }
                          if (cls != null) {
                              try {
                                  method = cls.getDeclaredMethod("getInstanceImpl");
                                  method.setAccessible(true);
                              } catch (NoSuchMethodException e) {
                                  e.printStackTrace();
                                  throw new AWTError("Cannot instantiate DataTransferer: " + name);
                              } catch (SecurityException e) {
                                  e.printStackTrace();
                                  throw new AWTError("Access is denied for DataTransferer: " + name);
                              }
                          }
                          if (method != null) {
                              try {
                                  ret = (DataTransferer) method.invoke(null);
                              } catch (InvocationTargetException e) {
                                  e.printStackTrace();
                                  throw new AWTError("Cannot instantiate DataTransferer: " + name);
                              } catch (IllegalAccessException e) {
                                  e.printStackTrace();
                                  throw new AWTError("Cannot access DataTransferer: " + name);
                              }
                          }
                          return ret;
                      }
                    };
                    transferer = AccessController.doPrivileged(action);
                }
            }
        }
        return transferer;
    }
    public static String canonicalName(String encoding) {
        if (encoding == null) {
            return null;
        }
        try {
            return Charset.forName(encoding).name();
        } catch (IllegalCharsetNameException icne) {
            return encoding;
        } catch (UnsupportedCharsetException uce) {
            return encoding;
        }
    }
    public static String getTextCharset(DataFlavor flavor) {
        if (!isFlavorCharsetTextType(flavor)) {
            return null;
        }
        String encoding = flavor.getParameter("charset");
        return (encoding != null) ? encoding : getDefaultTextCharset();
    }
    public static String getDefaultTextCharset() {
        if (defaultEncoding != null) {
            return defaultEncoding;
        }
        return defaultEncoding = Charset.defaultCharset().name();
    }
    public static boolean doesSubtypeSupportCharset(DataFlavor flavor) {
        if (dtLog.isLoggable(PlatformLogger.FINE)) {
            if (!"text".equals(flavor.getPrimaryType())) {
                dtLog.fine("Assertion (\"text\".equals(flavor.getPrimaryType())) failed");
            }
        }
        String subType = flavor.getSubType();
        if (subType == null) {
            return false;
        }
        Object support = textMIMESubtypeCharsetSupport.get(subType);
        if (support != null) {
            return (support == Boolean.TRUE);
        }
        boolean ret_val = (flavor.getParameter("charset") != null);
        textMIMESubtypeCharsetSupport.put
            (subType, (ret_val) ? Boolean.TRUE : Boolean.FALSE);
        return ret_val;
    }
    public static boolean doesSubtypeSupportCharset(String subType,
                                                    String charset)
    {
        Object support = textMIMESubtypeCharsetSupport.get(subType);
        if (support != null) {
            return (support == Boolean.TRUE);
        }
        boolean ret_val = (charset != null);
        textMIMESubtypeCharsetSupport.put
            (subType, (ret_val) ? Boolean.TRUE : Boolean.FALSE);
        return ret_val;
    }
    public static boolean isFlavorCharsetTextType(DataFlavor flavor) {
        if (DataFlavor.stringFlavor.equals(flavor)) {
            return true;
        }
        if (!"text".equals(flavor.getPrimaryType()) ||
            !doesSubtypeSupportCharset(flavor))
        {
            return false;
        }
        Class rep_class = flavor.getRepresentationClass();
        if (flavor.isRepresentationClassReader() ||
            String.class.equals(rep_class) ||
            flavor.isRepresentationClassCharBuffer() ||
            DataTransferer.charArrayClass.equals(rep_class))
        {
            return true;
        }
        if (!(flavor.isRepresentationClassInputStream() ||
              flavor.isRepresentationClassByteBuffer() ||
              DataTransferer.byteArrayClass.equals(rep_class))) {
            return false;
        }
        String charset = flavor.getParameter("charset");
        return (charset != null)
            ? DataTransferer.isEncodingSupported(charset)
            : true; 
    }
    public static boolean isFlavorNoncharsetTextType(DataFlavor flavor) {
        if (!"text".equals(flavor.getPrimaryType()) ||
            doesSubtypeSupportCharset(flavor))
        {
            return false;
        }
        return (flavor.isRepresentationClassInputStream() ||
                flavor.isRepresentationClassByteBuffer() ||
                DataTransferer.byteArrayClass.
                    equals(flavor.getRepresentationClass()));
    }
    public static boolean isEncodingSupported(String encoding) {
        if (encoding == null) {
            return false;
        }
        try {
            return Charset.isSupported(encoding);
        } catch (IllegalCharsetNameException icne) {
            return false;
        }
    }
    public static boolean isRemote(Class<?> type) {
        return RMI.isRemote(type);
    }
    public static Iterator standardEncodings() {
        return StandardEncodingsHolder.standardEncodings.iterator();
    }
    public static FlavorTable adaptFlavorMap(final FlavorMap map) {
        if (map instanceof FlavorTable) {
            return (FlavorTable)map;
        }
        return new FlavorTable() {
                public Map getNativesForFlavors(DataFlavor[] flavors) {
                    return map.getNativesForFlavors(flavors);
                }
                public Map getFlavorsForNatives(String[] natives) {
                    return map.getFlavorsForNatives(natives);
                }
                public List getNativesForFlavor(DataFlavor flav) {
                    Map natives =
                        getNativesForFlavors(new DataFlavor[] { flav } );
                    String nat = (String)natives.get(flav);
                    if (nat != null) {
                        List list = new ArrayList(1);
                        list.add(nat);
                        return list;
                    } else {
                        return Collections.EMPTY_LIST;
                    }
                }
                public List getFlavorsForNative(String nat) {
                    Map flavors =
                        getFlavorsForNatives(new String[] { nat } );
                    DataFlavor flavor = (DataFlavor)flavors.get(nat);
                    if (flavor != null) {
                        List list = new ArrayList(1);
                        list.add(flavor);
                        return list;
                    } else {
                        return Collections.EMPTY_LIST;
                    }
                }
            };
    }
    public abstract String getDefaultUnicodeEncoding();
    public void registerTextFlavorProperties(String nat, String charset,
                                             String eoln, String terminators) {
        Long format = getFormatForNativeAsLong(nat);
        textNatives.add(format);
        nativeCharsets.put(format, (charset != null && charset.length() != 0)
            ? charset : getDefaultTextCharset());
        if (eoln != null && eoln.length() != 0 && !eoln.equals("\n")) {
            nativeEOLNs.put(format, eoln);
        }
        if (terminators != null && terminators.length() != 0) {
            Integer iTerminators = Integer.valueOf(terminators);
            if (iTerminators.intValue() > 0) {
                nativeTerminators.put(format, iTerminators);
            }
        }
    }
    protected boolean isTextFormat(long format) {
        return textNatives.contains(Long.valueOf(format));
    }
    protected String getCharsetForTextFormat(Long lFormat) {
        return (String)nativeCharsets.get(lFormat);
    }
    public abstract boolean isLocaleDependentTextFormat(long format);
    public abstract boolean isFileFormat(long format);
    public abstract boolean isImageFormat(long format);
    protected boolean isURIListFormat(long format) {
        return false;
    }
    public SortedMap getFormatsForTransferable(Transferable contents,
                                               FlavorTable map) {
        DataFlavor[] flavors = contents.getTransferDataFlavors();
        if (flavors == null) {
            return new TreeMap();
        }
        return getFormatsForFlavors(flavors, map);
    }
    public SortedMap getFormatsForFlavor(DataFlavor flavor, FlavorTable map) {
        return getFormatsForFlavors(new DataFlavor[] { flavor },
                                    map);
    }
    public SortedMap getFormatsForFlavors(DataFlavor[] flavors, FlavorTable map) {
        Map formatMap = new HashMap(flavors.length);
        Map textPlainMap = new HashMap(flavors.length);
        Map indexMap = new HashMap(flavors.length);
        Map textPlainIndexMap = new HashMap(flavors.length);
        int currentIndex = 0;
        for (int i = flavors.length - 1; i >= 0; i--) {
            DataFlavor flavor = flavors[i];
            if (flavor == null) continue;
            if (flavor.isFlavorTextType() ||
                flavor.isFlavorJavaFileListType() ||
                DataFlavor.imageFlavor.equals(flavor) ||
                flavor.isRepresentationClassSerializable() ||
                flavor.isRepresentationClassInputStream() ||
                flavor.isRepresentationClassRemote())
            {
                List natives = map.getNativesForFlavor(flavor);
                currentIndex += natives.size();
                for (Iterator iter = natives.iterator(); iter.hasNext(); ) {
                    Long lFormat =
                        getFormatForNativeAsLong((String)iter.next());
                    Integer index = Integer.valueOf(currentIndex--);
                    formatMap.put(lFormat, flavor);
                    indexMap.put(lFormat, index);
                    if (("text".equals(flavor.getPrimaryType()) &&
                         "plain".equals(flavor.getSubType())) ||
                        flavor.equals(DataFlavor.stringFlavor))
                    {
                        textPlainMap.put(lFormat, flavor);
                        textPlainIndexMap.put(lFormat, index);
                    }
                }
                currentIndex += natives.size();
            }
        }
        formatMap.putAll(textPlainMap);
        indexMap.putAll(textPlainIndexMap);
        Comparator comparator =
            new IndexOrderComparator(indexMap, IndexedComparator.SELECT_WORST);
        SortedMap sortedMap = new TreeMap(comparator);
        sortedMap.putAll(formatMap);
        return sortedMap;
    }
    public long[] getFormatsForTransferableAsArray(Transferable contents,
                                                   FlavorTable map) {
        return keysToLongArray(getFormatsForTransferable(contents, map));
    }
    public long[] getFormatsForFlavorAsArray(DataFlavor flavor,
                                             FlavorTable map) {
        return keysToLongArray(getFormatsForFlavor(flavor, map));
    }
    public long[] getFormatsForFlavorsAsArray(DataFlavor[] flavors,
                                              FlavorTable map) {
        return keysToLongArray(getFormatsForFlavors(flavors, map));
    }
    public Map getFlavorsForFormat(long format, FlavorTable map) {
        return getFlavorsForFormats(new long[] { format }, map);
    }
    public Map getFlavorsForFormats(long[] formats, FlavorTable map) {
        Map flavorMap = new HashMap(formats.length);
        Set mappingSet = new HashSet(formats.length);
        Set flavorSet = new HashSet(formats.length);
        for (int i = 0; i < formats.length; i++) {
            long format = formats[i];
            String nat = getNativeForFormat(format);
            List flavors = map.getFlavorsForNative(nat);
            for (Iterator iter = flavors.iterator(); iter.hasNext(); ) {
                DataFlavor flavor = (DataFlavor)iter.next();
                if (flavor.isFlavorTextType() ||
                    flavor.isFlavorJavaFileListType() ||
                    DataFlavor.imageFlavor.equals(flavor) ||
                    flavor.isRepresentationClassSerializable() ||
                    flavor.isRepresentationClassInputStream() ||
                    flavor.isRepresentationClassRemote())
                {
                    Long lFormat = Long.valueOf(format);
                    Object mapping =
                        DataTransferer.createMapping(lFormat, flavor);
                    flavorMap.put(flavor, lFormat);
                    mappingSet.add(mapping);
                    flavorSet.add(flavor);
                }
            }
        }
        for (Iterator flavorIter = flavorSet.iterator();
             flavorIter.hasNext(); ) {
            DataFlavor flavor = (DataFlavor)flavorIter.next();
            List natives = map.getNativesForFlavor(flavor);
            for (Iterator nativeIter = natives.iterator();
                 nativeIter.hasNext(); ) {
                Long lFormat =
                    getFormatForNativeAsLong((String)nativeIter.next());
                Object mapping = DataTransferer.createMapping(lFormat, flavor);
                if (mappingSet.contains(mapping)) {
                    flavorMap.put(flavor, lFormat);
                    break;
                }
            }
        }
        return flavorMap;
    }
    public Set getFlavorsForFormatsAsSet(long[] formats, FlavorTable map) {
        Set flavorSet = new HashSet(formats.length);
        for (int i = 0; i < formats.length; i++) {
            String nat = getNativeForFormat(formats[i]);
            List flavors = map.getFlavorsForNative(nat);
            for (Iterator iter = flavors.iterator(); iter.hasNext(); ) {
                DataFlavor flavor = (DataFlavor)iter.next();
                if (flavor.isFlavorTextType() ||
                    flavor.isFlavorJavaFileListType() ||
                    DataFlavor.imageFlavor.equals(flavor) ||
                    flavor.isRepresentationClassSerializable() ||
                    flavor.isRepresentationClassInputStream() ||
                    flavor.isRepresentationClassRemote())
                {
                    flavorSet.add(flavor);
                }
            }
        }
        return flavorSet;
    }
    public DataFlavor[] getFlavorsForFormatAsArray(long format,
                                                   FlavorTable map) {
        return getFlavorsForFormatsAsArray(new long[] { format }, map);
    }
    public DataFlavor[] getFlavorsForFormatsAsArray(long[] formats,
                                                    FlavorTable map) {
        return setToSortedDataFlavorArray(getFlavorsForFormatsAsSet(formats, map));
    }
    private static Object createMapping(Object key, Object value) {
        return Arrays.asList(new Object[] { key, value });
    }
    protected abstract Long getFormatForNativeAsLong(String str);
    protected abstract String getNativeForFormat(long format);
    private String getBestCharsetForTextFormat(Long lFormat,
        Transferable localeTransferable) throws IOException
    {
        String charset = null;
        if (localeTransferable != null &&
            isLocaleDependentTextFormat(lFormat) &&
            localeTransferable.isDataFlavorSupported(javaTextEncodingFlavor))
        {
            try {
                charset = new String(
                    (byte[])localeTransferable.getTransferData(javaTextEncodingFlavor),
                    "UTF-8"
                );
            } catch (UnsupportedFlavorException cannotHappen) {
            }
        } else {
            charset = getCharsetForTextFormat(lFormat);
        }
        if (charset == null) {
            charset = getDefaultTextCharset();
        }
        return charset;
    }
    private byte[] translateTransferableString(String str,
                                               long format) throws IOException
    {
        Long lFormat = Long.valueOf(format);
        String charset = getBestCharsetForTextFormat(lFormat, null);
        String eoln = (String)nativeEOLNs.get(lFormat);
        if (eoln != null) {
            int length = str.length();
            StringBuffer buffer =
                new StringBuffer(length * 2); 
            for (int i = 0; i < length; i++) {
                if (str.startsWith(eoln, i)) {
                    buffer.append(eoln);
                    i += eoln.length() - 1;
                    continue;
                }
                char c = str.charAt(i);
                if (c == '\n') {
                    buffer.append(eoln);
                } else {
                    buffer.append(c);
                }
            }
            str = buffer.toString();
        }
        byte[] bytes = str.getBytes(charset);
        Integer terminators = (Integer)nativeTerminators.get(lFormat);
        if (terminators != null) {
            int numTerminators = terminators.intValue();
            byte[] terminatedBytes =
                new byte[bytes.length + numTerminators];
            System.arraycopy(bytes, 0, terminatedBytes, 0, bytes.length);
            for (int i = bytes.length; i < terminatedBytes.length; i++) {
                terminatedBytes[i] = 0x0;
            }
            bytes = terminatedBytes;
        }
        return bytes;
    }
    private String translateBytesOrStreamToString(InputStream str,  byte[] bytes,
                                                    long format,
                                                    Transferable localeTransferable)
            throws IOException
    {
        if (bytes == null) {
            bytes = inputStreamToByteArray(str);
        }
        str.close();
        Long lFormat = Long.valueOf(format);
        String charset = getBestCharsetForTextFormat(lFormat, localeTransferable);
        String eoln = (String)nativeEOLNs.get(lFormat);
        Integer terminators = (Integer)nativeTerminators.get(lFormat);
        int count;
        if (terminators != null) {
            int numTerminators = terminators.intValue();
search:
            for (count = 0; count < (bytes.length - numTerminators + 1); count += numTerminators) {
                for (int i = count; i < count + numTerminators; i++) {
                    if (bytes[i] != 0x0) {
                        continue search;
                    }
                }
                break search;
            }
        } else {
            count = bytes.length;
        }
        String converted = new String(bytes, 0, count, charset);
        if (eoln != null) {
            char[] buf = converted.toCharArray();
            char[] eoln_arr = eoln.toCharArray();
            converted = null;
            int j = 0;
            boolean match;
            for (int i = 0; i < buf.length; ) {
                if (i + eoln_arr.length > buf.length) {
                    buf[j++] = buf[i++];
                    continue;
                }
                match = true;
                for (int k = 0, l = i; k < eoln_arr.length; k++, l++) {
                    if (eoln_arr[k] != buf[l]) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    buf[j++] = '\n';
                    i += eoln_arr.length;
                } else {
                    buf[j++] = buf[i++];
                }
            }
            converted = new String(buf, 0, j);
        }
        return converted;
    }
    public byte[] translateTransferable(Transferable contents,
                                        DataFlavor flavor,
                                        long format) throws IOException
    {
        Object obj;
        boolean stringSelectionHack;
        try {
            obj = contents.getTransferData(flavor);
            if (obj == null) {
                return null;
            }
            if (flavor.equals(DataFlavor.plainTextFlavor) &&
                !(obj instanceof InputStream))
            {
                obj = contents.getTransferData(DataFlavor.stringFlavor);
                if (obj == null) {
                    return null;
                }
                stringSelectionHack = true;
            } else {
                stringSelectionHack = false;
            }
        } catch (UnsupportedFlavorException e) {
            throw new IOException(e.getMessage());
        }
        if (stringSelectionHack ||
            (String.class.equals(flavor.getRepresentationClass()) &&
             isFlavorCharsetTextType(flavor) && isTextFormat(format))) {
            String str = removeSuspectedData(flavor, contents, (String)obj);
            return translateTransferableString(
                str,
                format);
        } else if (flavor.isRepresentationClassReader()) {
            if (!(isFlavorCharsetTextType(flavor) && isTextFormat(format))) {
                throw new IOException
                    ("cannot transfer non-text data as Reader");
            }
            Reader r = (Reader)obj;
            StringBuffer buf = new StringBuffer();
            int c;
            while ((c = r.read()) != -1) {
                buf.append((char)c);
            }
            r.close();
            return translateTransferableString(
                buf.toString(),
                format);
        } else if (flavor.isRepresentationClassCharBuffer()) {
            if (!(isFlavorCharsetTextType(flavor) && isTextFormat(format))) {
                throw new IOException
                    ("cannot transfer non-text data as CharBuffer");
            }
            CharBuffer buffer = (CharBuffer)obj;
            int size = buffer.remaining();
            char[] chars = new char[size];
            buffer.get(chars, 0, size);
            return translateTransferableString(
                new String(chars),
                format);
        } else if (charArrayClass.equals(flavor.getRepresentationClass())) {
            if (!(isFlavorCharsetTextType(flavor) && isTextFormat(format))) {
                throw new IOException
                    ("cannot transfer non-text data as char array");
            }
            return translateTransferableString(
                new String((char[])obj),
                format);
        } else if (flavor.isRepresentationClassByteBuffer()) {
            ByteBuffer buffer = (ByteBuffer)obj;
            int size = buffer.remaining();
            byte[] bytes = new byte[size];
            buffer.get(bytes, 0, size);
            if (isFlavorCharsetTextType(flavor) && isTextFormat(format)) {
                String sourceEncoding = DataTransferer.getTextCharset(flavor);
                return translateTransferableString(
                    new String(bytes, sourceEncoding),
                    format);
            } else {
                return bytes;
            }
        } else if (byteArrayClass.equals(flavor.getRepresentationClass())) {
            byte[] bytes = (byte[])obj;
            if (isFlavorCharsetTextType(flavor) && isTextFormat(format)) {
                String sourceEncoding = DataTransferer.getTextCharset(flavor);
                return translateTransferableString(
                    new String(bytes, sourceEncoding),
                    format);
            } else {
                return bytes;
            }
        } else if (DataFlavor.imageFlavor.equals(flavor)) {
            if (!isImageFormat(format)) {
                throw new IOException("Data translation failed: " +
                                      "not an image format");
            }
            Image image = (Image)obj;
            byte[] bytes = imageToPlatformBytes(image, format);
            if (bytes == null) {
                throw new IOException("Data translation failed: " +
                    "cannot convert java image to native format");
            }
            return bytes;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (isFileFormat(format)) {
            if (!DataFlavor.javaFileListFlavor.equals(flavor)) {
                throw new IOException("data translation failed");
            }
            final List list = (List)obj;
            final ProtectionDomain userProtectionDomain = getUserProtectionDomain(contents);
            final ArrayList<String> fileList = castToFiles(list, userProtectionDomain);
            bos = convertFileListToBytes(fileList);
        } else if (isURIListFormat(format)) {
            if (!DataFlavor.javaFileListFlavor.equals(flavor)) {
                throw new IOException("data translation failed");
            }
            String nat = getNativeForFormat(format);
            String targetCharset = null;
            if (nat != null) {
                try {
                    targetCharset = new DataFlavor(nat).getParameter("charset");
                } catch (ClassNotFoundException cnfe) {
                    throw new IOException(cnfe);
                }
            }
            if (targetCharset == null) {
                targetCharset = "UTF-8";
            }
            final List list = (List)obj;
            final ProtectionDomain userProtectionDomain = getUserProtectionDomain(contents);
            final ArrayList<String> fileList = castToFiles(list, userProtectionDomain);
            final ArrayList<String> uriList = new ArrayList<String>(fileList.size());
            for (String fileObject : fileList) {
                final URI uri = new File(fileObject).toURI();
                try {
                    uriList.add(new URI(uri.getScheme(), "", uri.getPath(), uri.getFragment()).toString());
                } catch (URISyntaxException uriSyntaxException) {
                    throw new IOException(uriSyntaxException);
                  }
              }
            byte[] eoln = "\r\n".getBytes(targetCharset);
            for (int i = 0; i < uriList.size(); i++) {
                byte[] bytes = uriList.get(i).getBytes(targetCharset);
                bos.write(bytes, 0, bytes.length);
                bos.write(eoln, 0, eoln.length);
            }
        } else if (flavor.isRepresentationClassInputStream()) {
            InputStream is = (InputStream)obj;
            boolean eof = false;
            int avail = is.available();
            byte[] tmp = new byte[avail > 8192 ? avail : 8192];
            do {
                int ret;
                if (!(eof = (ret = is.read(tmp, 0, tmp.length)) == -1)) {
                    bos.write(tmp, 0, ret);
                }
            } while (!eof);
            is.close();
            if (isFlavorCharsetTextType(flavor) && isTextFormat(format)) {
                byte[] bytes = bos.toByteArray();
                bos.close();
                String sourceEncoding = DataTransferer.getTextCharset(flavor);
                return translateTransferableString(
                    new String(bytes, sourceEncoding),
                    format);
            }
        } else if (flavor.isRepresentationClassRemote()) {
            Object mo = RMI.newMarshalledObject(obj);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(mo);
            oos.close();
        } else if (flavor.isRepresentationClassSerializable()) {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.close();
        } else {
            throw new IOException("data translation failed");
        }
        byte[] ret = bos.toByteArray();
        bos.close();
        return ret;
    }
    protected abstract ByteArrayOutputStream convertFileListToBytes(ArrayList<String> fileList) throws IOException;
    private String removeSuspectedData(DataFlavor flavor, final Transferable contents, final String str)
            throws IOException
    {
        if (null == System.getSecurityManager()
            || !flavor.isMimeTypeEqual("text/uri-list"))
        {
            return str;
        }
        String ret_val = "";
        final ProtectionDomain userProtectionDomain = getUserProtectionDomain(contents);
        try {
            ret_val = (String) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() {
                        StringBuffer allowedFiles = new StringBuffer(str.length());
                        String [] uriArray = str.split("(\\s)+");
                        for (String fileName : uriArray)
                        {
                            File file = new File(fileName);
                            if (file.exists() &&
                                !(isFileInWebstartedCache(file) ||
                                isForbiddenToRead(file, userProtectionDomain)))
                            {
                                if (0 != allowedFiles.length())
                                {
                                    allowedFiles.append("\\r\\n");
                                }
                                allowedFiles.append(fileName);
                            }
                        }
                        return allowedFiles.toString();
                    }
                });
        } catch (PrivilegedActionException pae) {
            throw new IOException(pae.getMessage(), pae);
        }
        return ret_val;
    }
    private static ProtectionDomain getUserProtectionDomain(Transferable contents) {
        return contents.getClass().getProtectionDomain();
    }
    private boolean isForbiddenToRead (File file, ProtectionDomain protectionDomain)
    {
        if (null == protectionDomain) {
            return false;
        }
        try {
            FilePermission filePermission =
                    new FilePermission(file.getCanonicalPath(), "read, delete");
            if (protectionDomain.implies(filePermission)) {
                return false;
            }
        } catch (IOException e) {}
        return true;
    }
    private ArrayList<String> castToFiles(final List files,
                                          final ProtectionDomain userProtectionDomain) throws IOException
    {
        final ArrayList<String> fileList = new ArrayList<String>();
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws IOException {
                    for (Object fileObject : files)
                    {
                        File file = castToFile(fileObject);
                        if (file != null &&
                            (null == System.getSecurityManager() ||
                            !(isFileInWebstartedCache(file) ||
                            isForbiddenToRead(file, userProtectionDomain))))
                        {
                            fileList.add(file.getCanonicalPath());
                        }
                    }
                    return null;
                }
            });
        } catch (PrivilegedActionException pae) {
            throw new IOException(pae.getMessage());
        }
        return fileList;
    }
    private File castToFile(Object fileObject) throws IOException {
        String filePath = null;
        if (fileObject instanceof File) {
            filePath = ((File)fileObject).getCanonicalPath();
        } else if (fileObject instanceof String) {
           filePath = (String) fileObject;
        } else {
           return null;
        }
        return new File(filePath);
    }
    private final static String[] DEPLOYMENT_CACHE_PROPERTIES = {
        "deployment.system.cachedir",
        "deployment.user.cachedir",
        "deployment.javaws.cachedir",
        "deployment.javapi.cachedir"
    };
    private final static ArrayList <File> deploymentCacheDirectoryList =
            new ArrayList<File>();
    private static boolean isFileInWebstartedCache(File f) {
        if (deploymentCacheDirectoryList.isEmpty()) {
            for (String cacheDirectoryProperty : DEPLOYMENT_CACHE_PROPERTIES) {
                String cacheDirectoryPath = System.getProperty(cacheDirectoryProperty);
                if (cacheDirectoryPath != null) {
                    try {
                        File cacheDirectory = (new File(cacheDirectoryPath)).getCanonicalFile();
                        if (cacheDirectory != null) {
                            deploymentCacheDirectoryList.add(cacheDirectory);
                        }
                    } catch (IOException ioe) {}
                }
            }
        }
        for (File deploymentCacheDirectory : deploymentCacheDirectoryList) {
            for (File dir = f; dir != null; dir = dir.getParentFile()) {
                if (dir.equals(deploymentCacheDirectory)) {
                    return true;
                }
            }
        }
        return false;
    }
    public Object translateBytes(byte[] bytes, DataFlavor flavor,
                                 long format, Transferable localeTransferable)
        throws IOException
    {
        return translateBytesOrStream(null, bytes, flavor, format,
                                      localeTransferable);
    }
    public Object translateStream(InputStream str, DataFlavor flavor,
                                  long format, Transferable localeTransferable)
        throws IOException
    {
        return translateBytesOrStream(str, null, flavor, format,
                                      localeTransferable);
    }
    protected Object translateBytesOrStream(InputStream str, byte[] bytes,
                                            DataFlavor flavor, long format,
                                            Transferable localeTransferable)
        throws IOException
    {
        if (str == null) {
            str = new ByteArrayInputStream(bytes);
        }
        if (isFileFormat(format)) {
            if (!DataFlavor.javaFileListFlavor.equals(flavor)) {
                throw new IOException("data translation failed");
            }
            if (bytes == null) {
                bytes = inputStreamToByteArray(str);
            }
            String[] filenames = dragQueryFile(bytes);
            if (filenames == null) {
                str.close();
                return null;
            }
            File[] files = new File[filenames.length];
            for (int i = 0; i < filenames.length; i++) {
                files[i] = new File(filenames[i]);
            }
            str.close();
            return Arrays.asList(files);
        } else if (isURIListFormat(format) && DataFlavor.javaFileListFlavor.equals(flavor)) {
            try {
                URI uris[] = dragQueryURIs(str, bytes, format, localeTransferable);
                if (uris == null) {
                    return null;
                }
                ArrayList files = new ArrayList();
                for (URI uri : uris) {
                    try {
                        files.add(new File(uri));
                    } catch (IllegalArgumentException illegalArg) {
                    }
                }
                return files;
            } finally {
                str.close();
            }
        } else if (String.class.equals(flavor.getRepresentationClass()) &&
                   isFlavorCharsetTextType(flavor) && isTextFormat(format)) {
            return translateBytesOrStreamToString(
                str, bytes,
                format, localeTransferable);
        } else if (DataFlavor.plainTextFlavor.equals(flavor)) {
            return new StringReader(translateBytesOrStreamToString(
                        str, bytes,
                        format, localeTransferable));
        } else if (flavor.isRepresentationClassInputStream()) {
            return translateBytesOrStreamToInputStream(str, flavor, format,
                                                       localeTransferable);
        } else if (flavor.isRepresentationClassReader()) {
            if (!(isFlavorCharsetTextType(flavor) && isTextFormat(format))) {
                throw new IOException
                    ("cannot transfer non-text data as Reader");
            }
            InputStream is = (InputStream)
                translateBytesOrStreamToInputStream
                    (str, DataFlavor.plainTextFlavor, format,
                     localeTransferable);
            String unicode =
                DataTransferer.getTextCharset(DataFlavor.plainTextFlavor);
            Reader reader = new InputStreamReader(is, unicode);
            return constructFlavoredObject(reader, flavor, Reader.class);
        } else if (flavor.isRepresentationClassCharBuffer()) {
            if (!(isFlavorCharsetTextType(flavor) && isTextFormat(format))) {
                throw new IOException
                    ("cannot transfer non-text data as CharBuffer");
            }
            CharBuffer buffer = CharBuffer.wrap(translateBytesOrStreamToString(
                str, bytes,
                format, localeTransferable));
            return constructFlavoredObject(buffer, flavor, CharBuffer.class);
        } else if (charArrayClass.equals(flavor.getRepresentationClass())) {
            if (!(isFlavorCharsetTextType(flavor) && isTextFormat(format))) {
                throw new IOException
                    ("cannot transfer non-text data as char array");
            }
            return translateBytesOrStreamToString(
                    str, bytes,
                    format, localeTransferable).toCharArray();
        } else if (flavor.isRepresentationClassByteBuffer()) {
            if (isFlavorCharsetTextType(flavor) && isTextFormat(format)) {
                bytes = translateBytesOrStreamToString(
                    str, bytes,
                    format, localeTransferable
                ).getBytes(
                    DataTransferer.getTextCharset(flavor)
                );
            } else {
                if (bytes == null) {
                    bytes = inputStreamToByteArray(str);
                }
            }
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            return constructFlavoredObject(buffer, flavor, ByteBuffer.class);
        } else if (byteArrayClass.equals(flavor.getRepresentationClass())) {
            if (isFlavorCharsetTextType(flavor) && isTextFormat(format)) {
                return translateBytesOrStreamToString(
                    str, bytes,
                    format, localeTransferable
                ).getBytes(
                    DataTransferer.getTextCharset(flavor)
                );
            } else {
                return (bytes != null) ? bytes : inputStreamToByteArray(str);
            }
        } else if (flavor.isRepresentationClassRemote()) {
            try {
                byte[] ba = inputStreamToByteArray(str);
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ba));
                Object ret = RMI.getMarshalledObject(ois.readObject());
                ois.close();
                str.close();
                return ret;
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
        } else if (flavor.isRepresentationClassSerializable()) {
            try {
                byte[] ba = inputStreamToByteArray(str);
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ba));
                Object ret = ois.readObject();
                ois.close();
                str.close();
                return ret;
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
        } else if (DataFlavor.imageFlavor.equals(flavor)) {
            if (!isImageFormat(format)) {
                throw new IOException("data translation failed");
            }
            Image image = platformImageBytesOrStreamToImage(str, bytes, format);
            str.close();
            return image;
        }
        throw new IOException("data translation failed");
    }
    private Object translateBytesOrStreamToInputStream
        (InputStream str, DataFlavor flavor, long format,
         Transferable localeTransferable) throws IOException
    {
        if (isFlavorCharsetTextType(flavor) && isTextFormat(format)) {
            str = new ReencodingInputStream
                (str, format, DataTransferer.getTextCharset(flavor),
                 localeTransferable);
        }
        return constructFlavoredObject(str, flavor, InputStream.class);
    }
    private Object constructFlavoredObject(Object arg, DataFlavor flavor,
                                           Class clazz)
        throws IOException
    {
        final Class dfrc = flavor.getRepresentationClass();
        if (clazz.equals(dfrc)) {
            return arg; 
        } else {
            Constructor[] constructors = null;
            try {
                constructors = (Constructor[])
                    AccessController.doPrivileged(new PrivilegedAction() {
                            public Object run() {
                                return dfrc.getConstructors();
                            }
                        });
            } catch (SecurityException se) {
                throw new IOException(se.getMessage());
            }
            Constructor constructor = null;
            for (int j = 0; j < constructors.length; j++) {
                if (!Modifier.isPublic(constructors[j].getModifiers())) {
                    continue;
                }
                Class[] ptypes = constructors[j].getParameterTypes();
                if (ptypes != null && ptypes.length == 1 &&
                    clazz.equals(ptypes[0])) {
                    constructor = constructors[j];
                    break;
                }
            }
            if (constructor == null) {
                throw new IOException("can't find <init>(L"+ clazz +
                                      ";)V for class: " + dfrc.getName());
            }
            try {
                return constructor.newInstance(new Object[] { arg } );
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
        }
    }
    public class ReencodingInputStream extends InputStream {
        protected BufferedReader wrapped;
        protected final char[] in = new char[1];
        protected byte[] out;
        protected CharsetEncoder encoder;
        protected CharBuffer inBuf;
        protected ByteBuffer outBuf;
        protected char[] eoln;
        protected int numTerminators;
        protected boolean eos;
        protected int index, limit;
        public ReencodingInputStream(InputStream bytestream, long format,
                                     String targetEncoding,
                                     Transferable localeTransferable)
            throws IOException
        {
            Long lFormat = Long.valueOf(format);
            String sourceEncoding = null;
            if (isLocaleDependentTextFormat(format) &&
                localeTransferable != null &&
                localeTransferable.
                    isDataFlavorSupported(javaTextEncodingFlavor))
            {
                try {
                    sourceEncoding = new String((byte[])localeTransferable.
                                       getTransferData(javaTextEncodingFlavor),
                                       "UTF-8");
                } catch (UnsupportedFlavorException cannotHappen) {
                }
            } else {
                sourceEncoding = getCharsetForTextFormat(lFormat);
            }
            if (sourceEncoding == null) {
                sourceEncoding = getDefaultTextCharset();
            }
            wrapped = new BufferedReader
                (new InputStreamReader(bytestream, sourceEncoding));
            if (targetEncoding == null) {
                throw new NullPointerException("null target encoding");
            }
            try {
                encoder = Charset.forName(targetEncoding).newEncoder();
                out = new byte[(int)(encoder.maxBytesPerChar() + 0.5)];
                inBuf = CharBuffer.wrap(in);
                outBuf = ByteBuffer.wrap(out);
            } catch (IllegalCharsetNameException e) {
                throw new IOException(e.toString());
            } catch (UnsupportedCharsetException e) {
                throw new IOException(e.toString());
            } catch (UnsupportedOperationException e) {
                throw new IOException(e.toString());
            }
            String sEoln = (String)nativeEOLNs.get(lFormat);
            if (sEoln != null) {
                eoln = sEoln.toCharArray();
            }
            Integer terminators = (Integer)nativeTerminators.get(lFormat);
            if (terminators != null) {
                numTerminators = terminators.intValue();
            }
        }
        public int read() throws IOException {
            if (eos) {
                return -1;
            }
            if (index >= limit) {
                int c = wrapped.read();
                if (c == -1) { 
                    eos = true;
                    return -1;
                }
                if (numTerminators > 0 && c == 0) {
                    eos = true;
                    return -1;
                } else if (eoln != null && matchCharArray(eoln, c)) {
                    c = '\n' & 0xFFFF;
                }
                in[0] = (char)c;
                inBuf.rewind();
                outBuf.rewind();
                encoder.encode(inBuf, outBuf, false);
                outBuf.flip();
                limit = outBuf.limit();
                index = 0;
                return read();
            } else {
                return out[index++] & 0xFF;
            }
        }
        public int available() throws IOException {
            return ((eos) ? 0 : (limit - index));
        }
        public void close() throws IOException {
            wrapped.close();
        }
        private boolean matchCharArray(char[] array, int c)
            throws IOException
        {
            wrapped.mark(array.length);  
            int count = 0;
            if ((char)c == array[0]) {
                for (count = 1; count < array.length; count++) {
                    c = wrapped.read();
                    if (c == -1 || ((char)c) != array[count]) {
                        break;
                    }
                }
            }
            if (count == array.length) {
                return true;
            } else {
                wrapped.reset();
                return false;
            }
        }
    }
    protected abstract String[] dragQueryFile(byte[] bytes);
    protected URI[] dragQueryURIs(InputStream stream,
                                  byte[] bytes,
                                  long format,
                                  Transferable localeTransferable)
      throws IOException
    {
        throw new IOException(
            new UnsupportedOperationException("not implemented on this platform"));
    }
    protected abstract Image platformImageBytesOrStreamToImage(InputStream str,
                                                               byte[] bytes,
                                                               long format)
      throws IOException;
    protected Image standardImageBytesOrStreamToImage(InputStream inputStream,
                                                      byte[] bytes,
                                                      String mimeType)
      throws IOException {
        if (inputStream == null) {
            inputStream = new ByteArrayInputStream(bytes);
        }
        Iterator readerIterator = ImageIO.getImageReadersByMIMEType(mimeType);
        if (!readerIterator.hasNext()) {
            throw new IOException("No registered service provider can decode " +
                                  " an image from " + mimeType);
        }
        IOException ioe = null;
        while (readerIterator.hasNext()) {
            ImageReader imageReader = (ImageReader)readerIterator.next();
            try {
                ImageInputStream imageInputStream =
                    ImageIO.createImageInputStream(inputStream);
                try {
                    ImageReadParam param = imageReader.getDefaultReadParam();
                    imageReader.setInput(imageInputStream, true, true);
                    BufferedImage bufferedImage =
                        imageReader.read(imageReader.getMinIndex(), param);
                    if (bufferedImage != null) {
                        return bufferedImage;
                    }
                } finally {
                    imageInputStream.close();
                    imageReader.dispose();
                }
            } catch (IOException e) {
                ioe = e;
                continue;
            }
        }
        if (ioe == null) {
            ioe = new IOException("Registered service providers failed to decode"
                                  + " an image from " + mimeType);
        }
        throw ioe;
    }
    protected abstract byte[] imageToPlatformBytes(Image image, long format)
      throws IOException;
    protected byte[] imageToStandardBytes(Image image, String mimeType)
      throws IOException {
        IOException originalIOE = null;
        Iterator writerIterator = ImageIO.getImageWritersByMIMEType(mimeType);
        if (!writerIterator.hasNext()) {
            throw new IOException("No registered service provider can encode " +
                                  " an image to " + mimeType);
        }
        if (image instanceof RenderedImage) {
            try {
                return imageToStandardBytesImpl((RenderedImage)image, mimeType);
            } catch (IOException ioe) {
                originalIOE = ioe;
            }
        }
        int width = 0;
        int height = 0;
        if (image instanceof ToolkitImage) {
            ImageRepresentation ir = ((ToolkitImage)image).getImageRep();
            ir.reconstruct(ImageObserver.ALLBITS);
            width = ir.getWidth();
            height = ir.getHeight();
        } else {
            width = image.getWidth(null);
            height = image.getHeight(null);
        }
        ColorModel model = ColorModel.getRGBdefault();
        WritableRaster raster =
            model.createCompatibleWritableRaster(width, height);
        BufferedImage bufferedImage =
            new BufferedImage(model, raster, model.isAlphaPremultiplied(),
                              null);
        Graphics g = bufferedImage.getGraphics();
        try {
            g.drawImage(image, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        try {
            return imageToStandardBytesImpl(bufferedImage, mimeType);
        } catch (IOException ioe) {
            if (originalIOE != null) {
                throw originalIOE;
            } else {
                throw ioe;
            }
        }
    }
    protected byte[] imageToStandardBytesImpl(RenderedImage renderedImage,
                                              String mimeType)
        throws IOException {
        Iterator writerIterator = ImageIO.getImageWritersByMIMEType(mimeType);
        ImageTypeSpecifier typeSpecifier =
            new ImageTypeSpecifier(renderedImage);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOException ioe = null;
        while (writerIterator.hasNext()) {
            ImageWriter imageWriter = (ImageWriter)writerIterator.next();
            ImageWriterSpi writerSpi = imageWriter.getOriginatingProvider();
            if (!writerSpi.canEncodeImage(typeSpecifier)) {
                continue;
            }
            try {
                ImageOutputStream imageOutputStream =
                    ImageIO.createImageOutputStream(baos);
                try {
                    imageWriter.setOutput(imageOutputStream);
                    imageWriter.write(renderedImage);
                    imageOutputStream.flush();
                } finally {
                    imageOutputStream.close();
                }
            } catch (IOException e) {
                imageWriter.dispose();
                baos.reset();
                ioe = e;
                continue;
            }
            imageWriter.dispose();
            baos.close();
            return baos.toByteArray();
        }
        baos.close();
        if (ioe == null) {
            ioe = new IOException("Registered service providers failed to encode "
                                  + renderedImage + " to " + mimeType);
        }
        throw ioe;
    }
    private Object concatData(Object obj1, Object obj2) {
        InputStream str1 = null;
        InputStream str2 = null;
        if (obj1 instanceof byte[]) {
            byte[] arr1 = (byte[])obj1;
            if (obj2 instanceof byte[]) {
                byte[] arr2 = (byte[])obj2;
                byte[] ret = new byte[arr1.length + arr2.length];
                System.arraycopy(arr1, 0, ret, 0, arr1.length);
                System.arraycopy(arr2, 0, ret, arr1.length, arr2.length);
                return ret;
            } else {
                str1 = new ByteArrayInputStream(arr1);
                str2 = (InputStream)obj2;
            }
        } else {
            str1 = (InputStream)obj1;
            if (obj2 instanceof byte[]) {
                str2 = new ByteArrayInputStream((byte[])obj2);
            } else {
                str2 = (InputStream)obj2;
            }
        }
        return new SequenceInputStream(str1, str2);
    }
    public byte[] convertData(final Object source,
                              final Transferable contents,
                              final long format,
                              final Map formatMap,
                              final boolean isToolkitThread)
        throws IOException
    {
        byte[] ret = null;
        if (isToolkitThread) try {
            final Stack stack = new Stack();
            final Runnable dataConverter = new Runnable() {
                private boolean done = false;
                public void run() {
                    if (done) {
                        return;
                    }
                    byte[] data = null;
                    try {
                        DataFlavor flavor = (DataFlavor)formatMap.get(Long.valueOf(format));
                        if (flavor != null) {
                            data = translateTransferable(contents, flavor, format);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        data = null;
                    }
                    try {
                        getToolkitThreadBlockedHandler().lock();
                        stack.push(data);
                        getToolkitThreadBlockedHandler().exit();
                    } finally {
                        getToolkitThreadBlockedHandler().unlock();
                        done = true;
                    }
                }
            };
            final AppContext appContext = SunToolkit.targetToAppContext(source);
            getToolkitThreadBlockedHandler().lock();
            if (appContext != null) {
                appContext.put(DATA_CONVERTER_KEY, dataConverter);
            }
            SunToolkit.executeOnEventHandlerThread(source, dataConverter);
            while (stack.empty()) {
                getToolkitThreadBlockedHandler().enter();
            }
            if (appContext != null) {
                appContext.remove(DATA_CONVERTER_KEY);
            }
            ret = (byte[])stack.pop();
        } finally {
            getToolkitThreadBlockedHandler().unlock();
        } else {
            DataFlavor flavor = (DataFlavor)
                formatMap.get(Long.valueOf(format));
            if (flavor != null) {
                ret = translateTransferable(contents, flavor, format);
            }
        }
        return ret;
    }
    public void processDataConversionRequests() {
        if (EventQueue.isDispatchThread()) {
            AppContext appContext = AppContext.getAppContext();
            getToolkitThreadBlockedHandler().lock();
            try {
                Runnable dataConverter =
                    (Runnable)appContext.get(DATA_CONVERTER_KEY);
                if (dataConverter != null) {
                    dataConverter.run();
                    appContext.remove(DATA_CONVERTER_KEY);
                }
            } finally {
                getToolkitThreadBlockedHandler().unlock();
            }
        }
    }
    public abstract ToolkitThreadBlockedHandler
        getToolkitThreadBlockedHandler();
    public static long[] keysToLongArray(SortedMap map) {
        Set keySet = map.keySet();
        long[] retval = new long[keySet.size()];
        int i = 0;
        for (Iterator iter = keySet.iterator(); iter.hasNext(); i++) {
            retval[i] = ((Long)iter.next()).longValue();
        }
        return retval;
    }
    public static DataFlavor[] keysToDataFlavorArray(Map map) {
        return setToSortedDataFlavorArray(map.keySet(), map);
    }
    public static DataFlavor[] setToSortedDataFlavorArray(Set flavorsSet) {
        DataFlavor[] flavors = new DataFlavor[flavorsSet.size()];
        flavorsSet.toArray(flavors);
        final Comparator comparator =
                new DataFlavorComparator(IndexedComparator.SELECT_WORST);
        Arrays.sort(flavors, comparator);
        return flavors;
    }
    public static DataFlavor[] setToSortedDataFlavorArray
        (Set flavorsSet, Map flavorToNativeMap)
    {
        DataFlavor[] flavors = new DataFlavor[flavorsSet.size()];
        flavorsSet.toArray(flavors);
        Comparator comparator =
            new DataFlavorComparator(flavorToNativeMap,
                                     IndexedComparator.SELECT_WORST);
        Arrays.sort(flavors, comparator);
        return flavors;
    }
    protected static byte[] inputStreamToByteArray(InputStream str)
        throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len = 0;
        byte[] buf = new byte[8192];
        while ((len = str.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        return baos.toByteArray();
    }
    public List getPlatformMappingsForNative(String nat) {
        return new ArrayList();
    }
    public List getPlatformMappingsForFlavor(DataFlavor df) {
        return new ArrayList();
    }
    public abstract static class IndexedComparator implements Comparator {
        public static final boolean SELECT_BEST = true;
        public static final boolean SELECT_WORST = false;
        protected final boolean order;
        public IndexedComparator() {
            this(SELECT_BEST);
        }
        public IndexedComparator(boolean order) {
            this.order = order;
        }
        protected static int compareIndices(Map indexMap,
                                            Object obj1, Object obj2,
                                            Integer fallbackIndex) {
            Integer index1 = (Integer)indexMap.get(obj1);
            Integer index2 = (Integer)indexMap.get(obj2);
            if (index1 == null) {
                index1 = fallbackIndex;
            }
            if (index2 == null) {
                index2 = fallbackIndex;
            }
            return index1.compareTo(index2);
        }
        protected static int compareLongs(Map indexMap,
                                          Object obj1, Object obj2,
                                          Long fallbackIndex) {
            Long index1 = (Long)indexMap.get(obj1);
            Long index2 = (Long)indexMap.get(obj2);
            if (index1 == null) {
                index1 = fallbackIndex;
            }
            if (index2 == null) {
                index2 = fallbackIndex;
            }
            return index1.compareTo(index2);
        }
    }
    public static class CharsetComparator extends IndexedComparator {
        private static final Map charsets;
        private static String defaultEncoding;
        private static final Integer DEFAULT_CHARSET_INDEX = Integer.valueOf(2);
        private static final Integer OTHER_CHARSET_INDEX = Integer.valueOf(1);
        private static final Integer WORST_CHARSET_INDEX = Integer.valueOf(0);
        private static final Integer UNSUPPORTED_CHARSET_INDEX =
            Integer.valueOf(Integer.MIN_VALUE);
        private static final String UNSUPPORTED_CHARSET = "UNSUPPORTED";
        static {
            HashMap charsetsMap = new HashMap(8, 1.0f);
            charsetsMap.put(canonicalName("UTF-16LE"), Integer.valueOf(4));
            charsetsMap.put(canonicalName("UTF-16BE"), Integer.valueOf(5));
            charsetsMap.put(canonicalName("UTF-8"), Integer.valueOf(6));
            charsetsMap.put(canonicalName("UTF-16"), Integer.valueOf(7));
            charsetsMap.put(canonicalName("US-ASCII"), WORST_CHARSET_INDEX);
            String defEncoding = DataTransferer.canonicalName
                (DataTransferer.getDefaultTextCharset());
            if (charsetsMap.get(defaultEncoding) == null) {
                charsetsMap.put(defaultEncoding, DEFAULT_CHARSET_INDEX);
            }
            charsetsMap.put(UNSUPPORTED_CHARSET, UNSUPPORTED_CHARSET_INDEX);
            charsets = Collections.unmodifiableMap(charsetsMap);
        }
        public CharsetComparator() {
            this(SELECT_BEST);
        }
        public CharsetComparator(boolean order) {
            super(order);
        }
        public int compare(Object obj1, Object obj2) {
            String charset1 = null;
            String charset2 = null;
            if (order == SELECT_BEST) {
                charset1 = (String)obj1;
                charset2 = (String)obj2;
            } else {
                charset1 = (String)obj2;
                charset2 = (String)obj1;
            }
            return compareCharsets(charset1, charset2);
        }
        protected int compareCharsets(String charset1, String charset2) {
            charset1 = getEncoding(charset1);
            charset2 = getEncoding(charset2);
            int comp = compareIndices(charsets, charset1, charset2,
                                      OTHER_CHARSET_INDEX);
            if (comp == 0) {
                return charset2.compareTo(charset1);
            }
            return comp;
        }
        protected static String getEncoding(String charset) {
            if (charset == null) {
                return null;
            } else if (!DataTransferer.isEncodingSupported(charset)) {
                return UNSUPPORTED_CHARSET;
            } else {
                String canonicalName = DataTransferer.canonicalName(charset);
                return (charsets.containsKey(canonicalName))
                    ? canonicalName
                    : charset;
            }
        }
    }
    public static class DataFlavorComparator extends IndexedComparator {
        protected final Map flavorToFormatMap;
        private final CharsetComparator charsetComparator;
        private static final Map exactTypes;
        private static final Map primaryTypes;
        private static final Map nonTextRepresentations;
        private static final Map textTypes;
        private static final Map decodedTextRepresentations;
        private static final Map encodedTextRepresentations;
        private static final Integer UNKNOWN_OBJECT_LOSES =
            Integer.valueOf(Integer.MIN_VALUE);
        private static final Integer UNKNOWN_OBJECT_WINS =
            Integer.valueOf(Integer.MAX_VALUE);
        private static final Long UNKNOWN_OBJECT_LOSES_L =
            Long.valueOf(Long.MIN_VALUE);
        private static final Long UNKNOWN_OBJECT_WINS_L =
            Long.valueOf(Long.MAX_VALUE);
        static {
            {
                HashMap exactTypesMap = new HashMap(4, 1.0f);
                exactTypesMap.put("application/x-java-file-list",
                                  Integer.valueOf(0));
                exactTypesMap.put("application/x-java-serialized-object",
                                  Integer.valueOf(1));
                exactTypesMap.put("application/x-java-jvm-local-objectref",
                                  Integer.valueOf(2));
                exactTypesMap.put("application/x-java-remote-object",
                                  Integer.valueOf(3));
                exactTypes = Collections.unmodifiableMap(exactTypesMap);
            }
            {
                HashMap primaryTypesMap = new HashMap(1, 1.0f);
                primaryTypesMap.put("application", Integer.valueOf(0));
                primaryTypes = Collections.unmodifiableMap(primaryTypesMap);
            }
            {
                HashMap nonTextRepresentationsMap = new HashMap(3, 1.0f);
                nonTextRepresentationsMap.put(java.io.InputStream.class,
                                              Integer.valueOf(0));
                nonTextRepresentationsMap.put(java.io.Serializable.class,
                                              Integer.valueOf(1));
                Class<?> remoteClass = RMI.remoteClass();
                if (remoteClass != null) {
                    nonTextRepresentationsMap.put(remoteClass,
                                                  Integer.valueOf(2));
                }
                nonTextRepresentations =
                    Collections.unmodifiableMap(nonTextRepresentationsMap);
            }
            {
                HashMap textTypesMap = new HashMap(16, 1.0f);
                textTypesMap.put("text/plain", Integer.valueOf(0));
                textTypesMap.put("application/x-java-serialized-object",
                                Integer.valueOf(1));
                textTypesMap.put("text/calendar", Integer.valueOf(2));
                textTypesMap.put("text/css", Integer.valueOf(3));
                textTypesMap.put("text/directory", Integer.valueOf(4));
                textTypesMap.put("text/parityfec", Integer.valueOf(5));
                textTypesMap.put("text/rfc822-headers", Integer.valueOf(6));
                textTypesMap.put("text/t140", Integer.valueOf(7));
                textTypesMap.put("text/tab-separated-values", Integer.valueOf(8));
                textTypesMap.put("text/uri-list", Integer.valueOf(9));
                textTypesMap.put("text/richtext", Integer.valueOf(10));
                textTypesMap.put("text/enriched", Integer.valueOf(11));
                textTypesMap.put("text/rtf", Integer.valueOf(12));
                textTypesMap.put("text/html", Integer.valueOf(13));
                textTypesMap.put("text/xml", Integer.valueOf(14));
                textTypesMap.put("text/sgml", Integer.valueOf(15));
                textTypes = Collections.unmodifiableMap(textTypesMap);
            }
            {
                HashMap decodedTextRepresentationsMap = new HashMap(4, 1.0f);
                decodedTextRepresentationsMap.put
                    (DataTransferer.charArrayClass, Integer.valueOf(0));
                decodedTextRepresentationsMap.put
                    (java.nio.CharBuffer.class, Integer.valueOf(1));
                decodedTextRepresentationsMap.put
                    (java.lang.String.class, Integer.valueOf(2));
                decodedTextRepresentationsMap.put
                    (java.io.Reader.class, Integer.valueOf(3));
                decodedTextRepresentations =
                    Collections.unmodifiableMap(decodedTextRepresentationsMap);
            }
            {
                HashMap encodedTextRepresentationsMap = new HashMap(3, 1.0f);
                encodedTextRepresentationsMap.put
                    (DataTransferer.byteArrayClass, Integer.valueOf(0));
                encodedTextRepresentationsMap.put
                    (java.nio.ByteBuffer.class, Integer.valueOf(1));
                encodedTextRepresentationsMap.put
                    (java.io.InputStream.class, Integer.valueOf(2));
                encodedTextRepresentations =
                    Collections.unmodifiableMap(encodedTextRepresentationsMap);
            }
        }
        public DataFlavorComparator() {
            this(SELECT_BEST);
        }
        public DataFlavorComparator(boolean order) {
            super(order);
            charsetComparator = new CharsetComparator(order);
            flavorToFormatMap = Collections.EMPTY_MAP;
        }
        public DataFlavorComparator(Map map) {
            this(map, SELECT_BEST);
        }
        public DataFlavorComparator(Map map, boolean order) {
            super(order);
            charsetComparator = new CharsetComparator(order);
            HashMap hashMap = new HashMap(map.size());
            hashMap.putAll(map);
            flavorToFormatMap = Collections.unmodifiableMap(hashMap);
        }
        public int compare(Object obj1, Object obj2) {
            DataFlavor flavor1 = null;
            DataFlavor flavor2 = null;
            if (order == SELECT_BEST) {
                flavor1 = (DataFlavor)obj1;
                flavor2 = (DataFlavor)obj2;
            } else {
                flavor1 = (DataFlavor)obj2;
                flavor2 = (DataFlavor)obj1;
            }
            if (flavor1.equals(flavor2)) {
                return 0;
            }
            int comp = 0;
            String primaryType1 = flavor1.getPrimaryType();
            String subType1 = flavor1.getSubType();
            String mimeType1 = primaryType1 + "/" + subType1;
            Class class1 = flavor1.getRepresentationClass();
            String primaryType2 = flavor2.getPrimaryType();
            String subType2 = flavor2.getSubType();
            String mimeType2 = primaryType2 + "/" + subType2;
            Class class2 = flavor2.getRepresentationClass();
            if (flavor1.isFlavorTextType() && flavor2.isFlavorTextType()) {
                comp = compareIndices(textTypes, mimeType1, mimeType2,
                                      UNKNOWN_OBJECT_LOSES);
                if (comp != 0) {
                    return comp;
                }
                if (doesSubtypeSupportCharset(flavor1)) {
                    comp = compareIndices(decodedTextRepresentations, class1,
                                          class2, UNKNOWN_OBJECT_LOSES);
                    if (comp != 0) {
                        return comp;
                    }
                    comp = charsetComparator.compareCharsets
                        (DataTransferer.getTextCharset(flavor1),
                         DataTransferer.getTextCharset(flavor2));
                    if (comp != 0) {
                        return comp;
                    }
                }
                comp = compareIndices(encodedTextRepresentations, class1,
                                      class2, UNKNOWN_OBJECT_LOSES);
                if (comp != 0) {
                    return comp;
                }
            } else {
                comp = compareIndices(primaryTypes, primaryType1, primaryType2,
                                      UNKNOWN_OBJECT_LOSES);
                if (comp != 0) {
                    return comp;
                }
                comp = compareIndices(exactTypes, mimeType1, mimeType2,
                                      UNKNOWN_OBJECT_WINS);
                if (comp != 0) {
                    return comp;
                }
                comp = compareIndices(nonTextRepresentations, class1, class2,
                                      UNKNOWN_OBJECT_LOSES);
                if (comp != 0) {
                    return comp;
                }
            }
            return compareLongs(flavorToFormatMap, flavor1, flavor2,
                                UNKNOWN_OBJECT_LOSES_L);
        }
    }
    public static class IndexOrderComparator extends IndexedComparator {
        private final Map indexMap;
        private static final Integer FALLBACK_INDEX =
            Integer.valueOf(Integer.MIN_VALUE);
        public IndexOrderComparator(Map indexMap) {
            super(SELECT_BEST);
            this.indexMap = indexMap;
        }
        public IndexOrderComparator(Map indexMap, boolean order) {
            super(order);
            this.indexMap = indexMap;
        }
        public int compare(Object obj1, Object obj2) {
            if (order == SELECT_WORST) {
                return -compareIndices(indexMap, obj1, obj2, FALLBACK_INDEX);
            } else {
                return compareIndices(indexMap, obj1, obj2, FALLBACK_INDEX);
            }
        }
    }
    private static class RMI {
        private static final Class<?> remoteClass = getClass("java.rmi.Remote");
        private static final Class<?> marshallObjectClass =
            getClass("java.rmi.MarshalledObject");
        private static final Constructor<?> marshallCtor =
            getConstructor(marshallObjectClass, Object.class);
        private static final Method marshallGet =
            getMethod(marshallObjectClass, "get");
        private static Class<?> getClass(String name) {
            try {
                return Class.forName(name, true, null);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        private static Constructor<?> getConstructor(Class<?> c, Class<?>... types) {
            try {
                return (c == null) ? null : c.getDeclaredConstructor(types);
            } catch (NoSuchMethodException x) {
                throw new AssertionError(x);
            }
        }
        private static Method getMethod(Class<?> c, String name, Class<?>... types) {
            try {
                return (c == null) ? null : c.getMethod(name, types);
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        }
        static boolean isRemote(Class<?> c) {
            return (remoteClass == null) ? null : remoteClass.isAssignableFrom(c);
        }
        static Class<?> remoteClass() {
            return remoteClass;
        }
        static Object newMarshalledObject(Object obj) throws IOException {
            try {
                return marshallCtor.newInstance(obj);
            } catch (InstantiationException x) {
                throw new AssertionError(x);
            } catch (IllegalAccessException x) {
                throw new AssertionError(x);
            } catch (InvocationTargetException  x) {
                Throwable cause = x.getCause();
                if (cause instanceof IOException)
                    throw (IOException)cause;
                throw new AssertionError(x);
            }
        }
        static Object getMarshalledObject(Object obj)
            throws IOException, ClassNotFoundException
        {
            try {
                return marshallGet.invoke(obj);
            } catch (IllegalAccessException x) {
                throw new AssertionError(x);
            } catch (InvocationTargetException x) {
                Throwable cause = x.getCause();
                if (cause instanceof IOException)
                    throw (IOException)cause;
                if (cause instanceof ClassNotFoundException)
                    throw (ClassNotFoundException)cause;
                throw new AssertionError(x);
            }
        }
    }
}
