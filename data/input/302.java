public abstract class URLConnection {
    protected URL url;
    protected boolean doInput = true;
    protected boolean doOutput = false;
    private static boolean defaultAllowUserInteraction = false;
    protected boolean allowUserInteraction = defaultAllowUserInteraction;
    private static boolean defaultUseCaches = true;
    protected boolean useCaches = defaultUseCaches;
    protected long ifModifiedSince = 0;
    protected boolean connected = false;
    private int connectTimeout;
    private int readTimeout;
    private MessageHeader requests;
    private static FileNameMap fileNameMap;
    private static boolean fileNameMapLoaded = false;
    public static synchronized FileNameMap getFileNameMap() {
        if ((fileNameMap == null) && !fileNameMapLoaded) {
            fileNameMap = sun.net.www.MimeTable.loadTable();
            fileNameMapLoaded = true;
        }
        return new FileNameMap() {
            private FileNameMap map = fileNameMap;
            public String getContentTypeFor(String fileName) {
                return map.getContentTypeFor(fileName);
            }
        };
    }
    public static void setFileNameMap(FileNameMap map) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkSetFactory();
        fileNameMap = map;
    }
    abstract public void connect() throws IOException;
    public void setConnectTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout can not be negative");
        }
        connectTimeout = timeout;
    }
    public int getConnectTimeout() {
        return connectTimeout;
    }
    public void setReadTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout can not be negative");
        }
        readTimeout = timeout;
    }
    public int getReadTimeout() {
        return readTimeout;
    }
    protected URLConnection(URL url) {
        this.url = url;
    }
    public URL getURL() {
        return url;
    }
    public int getContentLength() {
        long l = getContentLengthLong();
        if (l > Integer.MAX_VALUE)
            return -1;
        return (int) l;
    }
    public long getContentLengthLong() {
        return getHeaderFieldLong("content-length", -1);
    }
    public String getContentType() {
        return getHeaderField("content-type");
    }
    public String getContentEncoding() {
        return getHeaderField("content-encoding");
    }
    public long getExpiration() {
        return getHeaderFieldDate("expires", 0);
    }
    public long getDate() {
        return getHeaderFieldDate("date", 0);
    }
    public long getLastModified() {
        return getHeaderFieldDate("last-modified", 0);
    }
    public String getHeaderField(String name) {
        return null;
    }
    public Map<String,List<String>> getHeaderFields() {
        return Collections.EMPTY_MAP;
    }
    public int getHeaderFieldInt(String name, int Default) {
        String value = getHeaderField(name);
        try {
            return Integer.parseInt(value);
        } catch (Exception e) { }
        return Default;
    }
    public long getHeaderFieldLong(String name, long Default) {
        String value = getHeaderField(name);
        try {
            return Long.parseLong(value);
        } catch (Exception e) { }
        return Default;
    }
    public long getHeaderFieldDate(String name, long Default) {
        String value = getHeaderField(name);
        try {
            return Date.parse(value);
        } catch (Exception e) { }
        return Default;
    }
    public String getHeaderFieldKey(int n) {
        return null;
    }
    public String getHeaderField(int n) {
        return null;
    }
    public Object getContent() throws IOException {
        getInputStream();
        return getContentHandler().getContent(this);
    }
    public Object getContent(Class[] classes) throws IOException {
        getInputStream();
        return getContentHandler().getContent(this, classes);
    }
    public Permission getPermission() throws IOException {
        return SecurityConstants.ALL_PERMISSION;
    }
    public InputStream getInputStream() throws IOException {
        throw new UnknownServiceException("protocol doesn't support input");
    }
    public OutputStream getOutputStream() throws IOException {
        throw new UnknownServiceException("protocol doesn't support output");
    }
    public String toString() {
        return this.getClass().getName() + ":" + url;
    }
    public void setDoInput(boolean doinput) {
        if (connected)
            throw new IllegalStateException("Already connected");
        doInput = doinput;
    }
    public boolean getDoInput() {
        return doInput;
    }
    public void setDoOutput(boolean dooutput) {
        if (connected)
            throw new IllegalStateException("Already connected");
        doOutput = dooutput;
    }
    public boolean getDoOutput() {
        return doOutput;
    }
    public void setAllowUserInteraction(boolean allowuserinteraction) {
        if (connected)
            throw new IllegalStateException("Already connected");
        allowUserInteraction = allowuserinteraction;
    }
    public boolean getAllowUserInteraction() {
        return allowUserInteraction;
    }
    public static void setDefaultAllowUserInteraction(boolean defaultallowuserinteraction) {
        defaultAllowUserInteraction = defaultallowuserinteraction;
    }
    public static boolean getDefaultAllowUserInteraction() {
        return defaultAllowUserInteraction;
    }
    public void setUseCaches(boolean usecaches) {
        if (connected)
            throw new IllegalStateException("Already connected");
        useCaches = usecaches;
    }
    public boolean getUseCaches() {
        return useCaches;
    }
    public void setIfModifiedSince(long ifmodifiedsince) {
        if (connected)
            throw new IllegalStateException("Already connected");
        ifModifiedSince = ifmodifiedsince;
    }
    public long getIfModifiedSince() {
        return ifModifiedSince;
    }
    public boolean getDefaultUseCaches() {
        return defaultUseCaches;
    }
    public void setDefaultUseCaches(boolean defaultusecaches) {
        defaultUseCaches = defaultusecaches;
    }
    public void setRequestProperty(String key, String value) {
        if (connected)
            throw new IllegalStateException("Already connected");
        if (key == null)
            throw new NullPointerException ("key is null");
        if (requests == null)
            requests = new MessageHeader();
        requests.set(key, value);
    }
    public void addRequestProperty(String key, String value) {
        if (connected)
            throw new IllegalStateException("Already connected");
        if (key == null)
            throw new NullPointerException ("key is null");
        if (requests == null)
            requests = new MessageHeader();
        requests.add(key, value);
    }
    public String getRequestProperty(String key) {
        if (connected)
            throw new IllegalStateException("Already connected");
        if (requests == null)
            return null;
        return requests.findValue(key);
    }
    public Map<String,List<String>> getRequestProperties() {
        if (connected)
            throw new IllegalStateException("Already connected");
        if (requests == null)
            return Collections.EMPTY_MAP;
        return requests.getHeaders(null);
    }
    @Deprecated
    public static void setDefaultRequestProperty(String key, String value) {
    }
    @Deprecated
    public static String getDefaultRequestProperty(String key) {
        return null;
    }
    static ContentHandlerFactory factory;
    public static synchronized void setContentHandlerFactory(ContentHandlerFactory fac) {
        if (factory != null) {
            throw new Error("factory already defined");
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        factory = fac;
    }
    private static Hashtable handlers = new Hashtable();
    synchronized ContentHandler getContentHandler()
    throws UnknownServiceException
    {
        String contentType = stripOffParameters(getContentType());
        ContentHandler handler = null;
        if (contentType == null)
            throw new UnknownServiceException("no content-type");
        try {
            handler = (ContentHandler) handlers.get(contentType);
            if (handler != null)
                return handler;
        } catch(Exception e) {
        }
        if (factory != null)
            handler = factory.createContentHandler(contentType);
        if (handler == null) {
            try {
                handler = lookupContentHandlerClassFor(contentType);
            } catch(Exception e) {
                e.printStackTrace();
                handler = UnknownContentHandler.INSTANCE;
            }
            handlers.put(contentType, handler);
        }
        return handler;
    }
    private String stripOffParameters(String contentType)
    {
        if (contentType == null)
            return null;
        int index = contentType.indexOf(';');
        if (index > 0)
            return contentType.substring(0, index);
        else
            return contentType;
    }
    private static final String contentClassPrefix = "sun.net.www.content";
    private static final String contentPathProp = "java.content.handler.pkgs";
    private ContentHandler lookupContentHandlerClassFor(String contentType)
        throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String contentHandlerClassName = typeToPackageName(contentType);
        String contentHandlerPkgPrefixes =getContentHandlerPkgPrefixes();
        StringTokenizer packagePrefixIter =
            new StringTokenizer(contentHandlerPkgPrefixes, "|");
        while (packagePrefixIter.hasMoreTokens()) {
            String packagePrefix = packagePrefixIter.nextToken().trim();
            try {
                String clsName = packagePrefix + "." + contentHandlerClassName;
                Class cls = null;
                try {
                    cls = Class.forName(clsName);
                } catch (ClassNotFoundException e) {
                    ClassLoader cl = ClassLoader.getSystemClassLoader();
                    if (cl != null) {
                        cls = cl.loadClass(clsName);
                    }
                }
                if (cls != null) {
                    ContentHandler handler =
                        (ContentHandler)cls.newInstance();
                    return handler;
                }
            } catch(Exception e) {
            }
        }
        return UnknownContentHandler.INSTANCE;
    }
    private String typeToPackageName(String contentType) {
        contentType = contentType.toLowerCase();
        int len = contentType.length();
        char nm[] = new char[len];
        contentType.getChars(0, len, nm, 0);
        for (int i = 0; i < len; i++) {
            char c = nm[i];
            if (c == '/') {
                nm[i] = '.';
            } else if (!('A' <= c && c <= 'Z' ||
                       'a' <= c && c <= 'z' ||
                       '0' <= c && c <= '9')) {
                nm[i] = '_';
            }
        }
        return new String(nm);
    }
    private String getContentHandlerPkgPrefixes() {
        String packagePrefixList = AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction(contentPathProp, ""));
        if (packagePrefixList != "") {
            packagePrefixList += "|";
        }
        return packagePrefixList + contentClassPrefix;
    }
    public static String guessContentTypeFromName(String fname) {
        return getFileNameMap().getContentTypeFor(fname);
    }
    static public String guessContentTypeFromStream(InputStream is)
                        throws IOException {
        if (!is.markSupported())
            return null;
        is.mark(16);
        int c1 = is.read();
        int c2 = is.read();
        int c3 = is.read();
        int c4 = is.read();
        int c5 = is.read();
        int c6 = is.read();
        int c7 = is.read();
        int c8 = is.read();
        int c9 = is.read();
        int c10 = is.read();
        int c11 = is.read();
        int c12 = is.read();
        int c13 = is.read();
        int c14 = is.read();
        int c15 = is.read();
        int c16 = is.read();
        is.reset();
        if (c1 == 0xCA && c2 == 0xFE && c3 == 0xBA && c4 == 0xBE) {
            return "application/java-vm";
        }
        if (c1 == 0xAC && c2 == 0xED) {
            return "application/x-java-serialized-object";
        }
        if (c1 == '<') {
            if (c2 == '!'
                || ((c2 == 'h' && (c3 == 't' && c4 == 'm' && c5 == 'l' ||
                                   c3 == 'e' && c4 == 'a' && c5 == 'd') ||
                (c2 == 'b' && c3 == 'o' && c4 == 'd' && c5 == 'y'))) ||
                ((c2 == 'H' && (c3 == 'T' && c4 == 'M' && c5 == 'L' ||
                                c3 == 'E' && c4 == 'A' && c5 == 'D') ||
                (c2 == 'B' && c3 == 'O' && c4 == 'D' && c5 == 'Y')))) {
                return "text/html";
            }
            if (c2 == '?' && c3 == 'x' && c4 == 'm' && c5 == 'l' && c6 == ' ') {
                return "application/xml";
            }
        }
        if (c1 == 0xef &&  c2 == 0xbb &&  c3 == 0xbf) {
            if (c4 == '<' &&  c5 == '?' &&  c6 == 'x') {
                return "application/xml";
            }
        }
        if (c1 == 0xfe && c2 == 0xff) {
            if (c3 == 0 && c4 == '<' && c5 == 0 && c6 == '?' &&
                c7 == 0 && c8 == 'x') {
                return "application/xml";
            }
        }
        if (c1 == 0xff && c2 == 0xfe) {
            if (c3 == '<' && c4 == 0 && c5 == '?' && c6 == 0 &&
                c7 == 'x' && c8 == 0) {
                return "application/xml";
            }
        }
        if (c1 == 0x00 &&  c2 == 0x00 &&  c3 == 0xfe &&  c4 == 0xff) {
            if (c5  == 0 && c6  == 0 && c7  == 0 && c8  == '<' &&
                c9  == 0 && c10 == 0 && c11 == 0 && c12 == '?' &&
                c13 == 0 && c14 == 0 && c15 == 0 && c16 == 'x') {
                return "application/xml";
            }
        }
        if (c1 == 0xff &&  c2 == 0xfe &&  c3 == 0x00 &&  c4 == 0x00) {
            if (c5  == '<' && c6  == 0 && c7  == 0 && c8  == 0 &&
                c9  == '?' && c10 == 0 && c11 == 0 && c12 == 0 &&
                c13 == 'x' && c14 == 0 && c15 == 0 && c16 == 0) {
                return "application/xml";
            }
        }
        if (c1 == 'G' && c2 == 'I' && c3 == 'F' && c4 == '8') {
            return "image/gif";
        }
        if (c1 == '#' && c2 == 'd' && c3 == 'e' && c4 == 'f') {
            return "image/x-bitmap";
        }
        if (c1 == '!' && c2 == ' ' && c3 == 'X' && c4 == 'P' &&
                        c5 == 'M' && c6 == '2') {
            return "image/x-pixmap";
        }
        if (c1 == 137 && c2 == 80 && c3 == 78 &&
                c4 == 71 && c5 == 13 && c6 == 10 &&
                c7 == 26 && c8 == 10) {
            return "image/png";
        }
        if (c1 == 0xFF && c2 == 0xD8 && c3 == 0xFF) {
            if (c4 == 0xE0) {
                return "image/jpeg";
            }
            if ((c4 == 0xE1) &&
                (c7 == 'E' && c8 == 'x' && c9 == 'i' && c10 =='f' &&
                 c11 == 0)) {
                return "image/jpeg";
            }
            if (c4 == 0xEE) {
                return "image/jpg";
            }
        }
        if (c1 == 0xD0 && c2 == 0xCF && c3 == 0x11 && c4 == 0xE0 &&
            c5 == 0xA1 && c6 == 0xB1 && c7 == 0x1A && c8 == 0xE1) {
            if (checkfpx(is)) {
                return "image/vnd.fpx";
            }
        }
        if (c1 == 0x2E && c2 == 0x73 && c3 == 0x6E && c4 == 0x64) {
            return "audio/basic";  
        }
        if (c1 == 0x64 && c2 == 0x6E && c3 == 0x73 && c4 == 0x2E) {
            return "audio/basic";  
        }
        if (c1 == 'R' && c2 == 'I' && c3 == 'F' && c4 == 'F') {
            return "audio/x-wav";
        }
        return null;
    }
    static private boolean checkfpx(InputStream is) throws IOException {
        is.mark(0x100);
        long toSkip = (long)0x1C;
        long posn;
        if ((posn = skipForward(is, toSkip)) < toSkip) {
          is.reset();
          return false;
        }
        int c[] = new int[16];
        if (readBytes(c, 2, is) < 0) {
            is.reset();
            return false;
        }
        int byteOrder = c[0];
        posn+=2;
        int uSectorShift;
        if (readBytes(c, 2, is) < 0) {
            is.reset();
            return false;
        }
        if(byteOrder == 0xFE) {
            uSectorShift = c[0];
            uSectorShift += c[1] << 8;
        }
        else {
            uSectorShift = c[0] << 8;
            uSectorShift += c[1];
        }
        posn += 2;
        toSkip = (long)0x30 - posn;
        long skipped = 0;
        if ((skipped = skipForward(is, toSkip)) < toSkip) {
          is.reset();
          return false;
        }
        posn += skipped;
        if (readBytes(c, 4, is) < 0) {
            is.reset();
            return false;
        }
        int sectDirStart;
        if(byteOrder == 0xFE) {
            sectDirStart = c[0];
            sectDirStart += c[1] << 8;
            sectDirStart += c[2] << 16;
            sectDirStart += c[3] << 24;
        } else {
            sectDirStart =  c[0] << 24;
            sectDirStart += c[1] << 16;
            sectDirStart += c[2] << 8;
            sectDirStart += c[3];
        }
        posn += 4;
        is.reset(); 
        toSkip = 0x200L + (long)(1<<uSectorShift)*sectDirStart + 0x50L;
        if (toSkip < 0) {
            return false;
        }
        is.mark((int)toSkip+0x30);
        if ((skipForward(is, toSkip)) < toSkip) {
            is.reset();
            return false;
        }
        if (readBytes(c, 16, is) < 0) {
            is.reset();
            return false;
        }
        if (byteOrder == 0xFE &&
            c[0] == 0x00 && c[2] == 0x61 && c[3] == 0x56 &&
            c[4] == 0x54 && c[5] == 0xC1 && c[6] == 0xCE &&
            c[7] == 0x11 && c[8] == 0x85 && c[9] == 0x53 &&
            c[10]== 0x00 && c[11]== 0xAA && c[12]== 0x00 &&
            c[13]== 0xA1 && c[14]== 0xF9 && c[15]== 0x5B) {
            is.reset();
            return true;
        }
        else if (c[3] == 0x00 && c[1] == 0x61 && c[0] == 0x56 &&
            c[5] == 0x54 && c[4] == 0xC1 && c[7] == 0xCE &&
            c[6] == 0x11 && c[8] == 0x85 && c[9] == 0x53 &&
            c[10]== 0x00 && c[11]== 0xAA && c[12]== 0x00 &&
            c[13]== 0xA1 && c[14]== 0xF9 && c[15]== 0x5B) {
            is.reset();
            return true;
        }
        is.reset();
        return false;
    }
    static private int readBytes(int c[], int len, InputStream is)
                throws IOException {
        byte buf[] = new byte[len];
        if (is.read(buf, 0, len) < len) {
            return -1;
        }
        for (int i = 0; i < len; i++) {
             c[i] = buf[i] & 0xff;
        }
        return 0;
    }
    static private long skipForward(InputStream is, long toSkip)
                throws IOException {
        long eachSkip = 0;
        long skipped = 0;
        while (skipped != toSkip) {
            eachSkip = is.skip(toSkip - skipped);
            if (eachSkip <= 0) {
                if (is.read() == -1) {
                    return skipped ;
                } else {
                    skipped++;
                }
            }
            skipped += eachSkip;
        }
        return skipped;
    }
}
class UnknownContentHandler extends ContentHandler {
    static final ContentHandler INSTANCE = new UnknownContentHandler();
    public Object getContent(URLConnection uc) throws IOException {
        return uc.getInputStream();
    }
}
