public abstract class Charset implements Comparable<Charset> {
    private static final String PROVIDER_CONFIGURATION_FILE_NAME = "META-INF/services/java.nio.charset.spi.CharsetProvider"; 
    private static final String PROVIDER_CONFIGURATION_FILE_ENCODING = "UTF-8"; 
    private static final String PROVIDER_CONFIGURATION_FILE_COMMENT = "#"; 
    private static ClassLoader systemClassLoader;
    private static final CharsetProviderICU _builtInProvider;
    private static TreeMap<String, Charset> _builtInCharsets = null;
    private final String canonicalName;
    private final HashSet<String> aliasesSet;
    private final static HashMap<String, Charset> cachedCharsetTable = new HashMap<String, Charset>();
    private static boolean inForNameInternal = false;
    static {
        _builtInProvider = AccessController
                .doPrivileged(new PrivilegedAction<CharsetProviderICU>() {
                    public CharsetProviderICU run() {
                         return new CharsetProviderICU();
                   }
                });
    }
    protected Charset(String canonicalName, String[] aliases) {
        if (null == canonicalName) {
            throw new NullPointerException();
        }
        checkCharsetName(canonicalName);
        this.canonicalName = canonicalName;
        this.aliasesSet = new HashSet<String>();
        if (null != aliases) {
            for (int i = 0; i < aliases.length; i++) {
                checkCharsetName(aliases[i]);
                this.aliasesSet.add(aliases[i]);
            }
        }
    }
    private static boolean isSpecial(char c) {
        return ('-' == c || '.' == c || ':' == c || '_' == c);
    }
    private static boolean isLetter(char c) {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
    }
    private static boolean isDigit(char c) {
        return ('0' <= c && c <= '9');
    }
    private static void checkCharsetName(String name) {
        if (name.length() == 0) {
            throw new IllegalCharsetNameException(name);
        }
        int length = name.length();
        for (int i = 0; i < length; i++) {
            char c = name.charAt(i);
            if (!isLetter(c) && !isDigit(c) && !isSpecial(c)) {
                throw new IllegalCharsetNameException(name);
            }
        }
    }
    private static ClassLoader getContextClassLoader() {
        final Thread t = Thread.currentThread();
        return AccessController
                .doPrivileged(new PrivilegedAction<ClassLoader>() {
                    public ClassLoader run() {
                        return t.getContextClassLoader();
                    }
                });
    }
    private static void getSystemClassLoader() {
        if (null == systemClassLoader) {
            systemClassLoader = AccessController
                    .doPrivileged(new PrivilegedAction<ClassLoader>() {
                        public ClassLoader run() {
                            return ClassLoader.getSystemClassLoader();
                        }
                    });
        }
    }
    private static void addCharsets(CharsetProvider cp,
            TreeMap<String, Charset> charsets) {
        Iterator<Charset> it = cp.charsets();
        while (it.hasNext()) {
            Charset cs = it.next();
            if (!charsets.containsKey(cs.name())) {
                charsets.put(cs.name(), cs);
            }
        }
    }
    private static String trimClassName(String name) {
        String trimedName = name;
        int index = name.indexOf(PROVIDER_CONFIGURATION_FILE_COMMENT);
        if (index != -1) {
            trimedName = name.substring(0, index);
        }
        return trimedName.trim();
    }
    private static void loadConfiguredCharsets(URL configFile,
            ClassLoader contextClassLoader, TreeMap<String, Charset> charsets) {
        BufferedReader reader = null;
        try {
            InputStream is = configFile.openStream();
            reader = new BufferedReader(new InputStreamReader(is,
                            PROVIDER_CONFIGURATION_FILE_ENCODING), 8192);
            String providerClassName = reader.readLine();
            while (null != providerClassName) {
                providerClassName = trimClassName(providerClassName);
                if (providerClassName.length() > 0) { 
                    Object cp = null;
                    try {
                        Class<?> c = Class.forName(providerClassName, true,
                                contextClassLoader);
                        cp = c.newInstance();
                    } catch (Exception ex) {
                        try {
                            getSystemClassLoader();
                            Class<?> c = Class.forName(providerClassName, true,
                                    systemClassLoader);
                            cp = c.newInstance();
                        } catch (Exception e) {
                            throw new Error(e.getMessage(), e);
                        }
                    }
                    addCharsets((CharsetProvider) cp, charsets);
                }
                providerClassName = reader.readLine();
            }
        } catch (IOException ex) {
        } finally {
            try {
                if (null != reader) {
                    reader.close();
                }
            } catch (IOException ex) {
            }
        }
    }
    @SuppressWarnings("unchecked")
    public static SortedMap<String, Charset> availableCharsets() {
        if (null == _builtInCharsets) {
            synchronized (Charset.class) {
                if (null == _builtInCharsets) {
                    _builtInCharsets = new TreeMap<String, Charset>(
                            IgnoreCaseComparator.getInstance());
                    _builtInProvider.putCharsets(_builtInCharsets);
                }
            }
        }
        TreeMap<String, Charset> charsets = (TreeMap<String, Charset>) _builtInCharsets
                .clone();
        ClassLoader contextClassLoader = getContextClassLoader();
        Enumeration<URL> e = null;
        try {
            if (null != contextClassLoader) {
                e = contextClassLoader
                        .getResources(PROVIDER_CONFIGURATION_FILE_NAME);
            } else {
                getSystemClassLoader();
                e = systemClassLoader
                        .getResources(PROVIDER_CONFIGURATION_FILE_NAME);
            }
            while (e.hasMoreElements()) {
                loadConfiguredCharsets(e.nextElement(), contextClassLoader,
                        charsets);
            }
        } catch (IOException ex) {
        }
        return Collections.unmodifiableSortedMap(charsets);
    }
    private static Charset searchConfiguredCharsets(String charsetName,
            ClassLoader contextClassLoader, URL configFile) {
        BufferedReader reader = null;
        try {
            InputStream is = configFile.openStream();
            reader = new BufferedReader(new InputStreamReader(is,
                            PROVIDER_CONFIGURATION_FILE_ENCODING), 8192);
            String providerClassName = reader.readLine();
            while (null != providerClassName) {
                providerClassName = trimClassName(providerClassName);
                if (providerClassName.length() > 0) { 
                    Object cp = null;
                    try {
                        Class<?> c = Class.forName(providerClassName, true,
                                contextClassLoader);
                        cp = c.newInstance();
                    } catch (Exception ex) {
                        try {
                            getSystemClassLoader();
                            Class<?> c = Class.forName(providerClassName, true,
                                    systemClassLoader);
                            cp = c.newInstance();
                        } catch (SecurityException e) {
                        } catch (Exception e) {
                            throw new Error(e.getMessage(), e);
                        }
                    }
                    if (cp != null) {
                        Charset cs = ((CharsetProvider) cp)
                                .charsetForName(charsetName);
                        if (null != cs) {
                            return cs;
                        }
                    }
                }
                providerClassName = reader.readLine();
            }
            return null;
        } catch (IOException ex) {
            return null;
        } finally {
            try {
                if (null != reader) {
                    reader.close();
                }
            } catch (IOException ex) {
            }
        }
    }
    private synchronized static Charset forNameInternal(String charsetName)
            throws IllegalCharsetNameException {
        Charset cs = cachedCharsetTable.get(charsetName);
        if (null != cs) {
            return cs;
        }
        if (null == charsetName) {
            throw new IllegalArgumentException();
        }
        checkCharsetName(charsetName);
        cs = _builtInProvider.charsetForName(charsetName);
        if (null != cs) {
            cacheCharset(cs);
            return cs;
        }
        ClassLoader contextClassLoader = getContextClassLoader();
        Enumeration<URL> e = null;
        try {
            if (null != contextClassLoader) {
                e = contextClassLoader
                        .getResources(PROVIDER_CONFIGURATION_FILE_NAME);
            } else {
                getSystemClassLoader();
                if (systemClassLoader == null) {
                    e = new Vector<URL>().elements();
                } else {
                    e = systemClassLoader
                            .getResources(PROVIDER_CONFIGURATION_FILE_NAME);
                }
            }
            while (e.hasMoreElements()) {
			     inForNameInternal = true;
			     cs = searchConfiguredCharsets(charsetName, contextClassLoader,
                        e.nextElement());
				 inForNameInternal = false;
                if (null != cs) {
                    cacheCharset(cs);
                    return cs;
                }
            }
        } catch (IOException ex) {
        } finally {
		    inForNameInternal = false;
        }
        return null;
    }
    private static void cacheCharset(Charset cs) {
        if (!cachedCharsetTable.containsKey(cs.name())){
            cachedCharsetTable.put(cs.name(), cs);  
        }
        Set<String> aliasesSet = cs.aliases();
        if (null != aliasesSet) {
            Iterator<String> iter = aliasesSet.iterator();
            while (iter.hasNext()) {
                String alias = iter.next();
                if (!cachedCharsetTable.containsKey(alias)) {
                    cachedCharsetTable.put(alias, cs); 
                }
            }
        }
    }
    public static Charset forName(String charsetName) {
        Charset c = forNameInternal(charsetName);
        if (null == c) {
            throw new UnsupportedCharsetException(charsetName);
        }
        return c;
    }
    public static synchronized boolean isSupported(String charsetName) {
        if (inForNameInternal) {
            Charset cs = cachedCharsetTable.get(charsetName);
            if (null != cs) {
                return true;
            }
            if (null == charsetName) {
                throw new IllegalArgumentException();
            }
            checkCharsetName(charsetName);
            cs = _builtInProvider.charsetForName(charsetName);
            if (null != cs) {
                cacheCharset(cs);
                return true;
            }
            return false;
        } else {
            Charset cs = forNameInternal(charsetName);
            return (null != cs);
        }
    }
    public abstract boolean contains(Charset charset);
    public abstract CharsetEncoder newEncoder();
    public abstract CharsetDecoder newDecoder();
    public final String name() {
        return this.canonicalName;
    }
    public final Set<String> aliases() {
        return Collections.unmodifiableSet(this.aliasesSet);
    }
    public String displayName() {
        return this.canonicalName;
    }
    public String displayName(Locale l) {
        return this.canonicalName;
    }
    public final boolean isRegistered() {
        return !canonicalName.startsWith("x-") 
                && !canonicalName.startsWith("X-"); 
    }
    public boolean canEncode() {
        return true;
    }
    public final ByteBuffer encode(CharBuffer buffer) {
        try {
            return this.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE).encode(
                            buffer);
        } catch (CharacterCodingException ex) {
            throw new Error(ex.getMessage(), ex);
        }
    }
    public final ByteBuffer encode(String s) {
        return encode(CharBuffer.wrap(s));
    }
    public final CharBuffer decode(ByteBuffer buffer) {
        try {
            return this.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE).decode(
                            buffer);
        } catch (CharacterCodingException ex) {
            throw new Error(ex.getMessage(), ex);
        }
    }
    public final int compareTo(Charset charset) {
        return this.canonicalName.compareToIgnoreCase(charset.canonicalName);
    }
    @Override
    public final boolean equals(Object obj) {
        if (obj instanceof Charset) {
            Charset that = (Charset) obj;
            return this.canonicalName.equals(that.canonicalName);
        }
        return false;
    }
    @Override
    public final int hashCode() {
        return this.canonicalName.hashCode();
    }
    @Override
    public final String toString() {
        return "Charset[" + this.canonicalName + "]"; 
    }
    public static Charset defaultCharset() {
        Charset defaultCharset = null;
        String encoding = AccessController
                .doPrivileged(new PrivilegedAction<String>() {
                    public String run() {
                        return System.getProperty("file.encoding"); 
                    }
                });
        try {
            defaultCharset = Charset.forName(encoding);
        } catch (UnsupportedCharsetException e) {
            defaultCharset = Charset.forName("UTF-8"); 
        }
        return defaultCharset;
    }
    static class IgnoreCaseComparator implements Comparator<String> {
        private static Comparator<String> c = new IgnoreCaseComparator();
        private IgnoreCaseComparator() {
        }
        public static Comparator<String> getInstance() {
            return c;
        }
        public int compare(String s1, String s2) {
            return s1.compareToIgnoreCase(s2);
        }
    }
}
