public abstract class URLConnection {
    protected URL url;
    private String contentType;
    private static boolean defaultAllowUserInteraction;
    private static boolean defaultUseCaches = true;
    ContentHandler defaultHandler = new DefaultContentHandler();
    private long lastModified = -1;
    protected long ifModifiedSince;
    protected boolean useCaches = defaultUseCaches;
    protected boolean connected;
    protected boolean doOutput;
    protected boolean doInput = true;
    protected boolean allowUserInteraction = defaultAllowUserInteraction;
    private static ContentHandlerFactory contentHandlerFactory;
    private int readTimeout = 0;
    private int connectTimeout = 0;
    static Hashtable<String, Object> contentHandlers = new Hashtable<String, Object>();
    private static FileNameMap fileNameMap;
    protected URLConnection(URL url) {
        this.url = url;
    }
    public abstract void connect() throws IOException;
    public boolean getAllowUserInteraction() {
        return allowUserInteraction;
    }
    public Object getContent() throws java.io.IOException {
        if (!connected) {
            connect();
        }
        if ((contentType = getContentType()) == null) {
            if ((contentType = guessContentTypeFromName(url.getFile())) == null) {
                contentType = guessContentTypeFromStream(getInputStream());
            }
        }
        if (contentType != null) {
            return getContentHandler(contentType).getContent(this);
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    public Object getContent(Class[] types) throws IOException {
        if (!connected) {
            connect();
        }
        if ((contentType = getContentType()) == null) {
            if ((contentType = guessContentTypeFromName(url.getFile())) == null) {
                contentType = guessContentTypeFromStream(getInputStream());
            }
        }
        if (contentType != null) {
            return getContentHandler(contentType).getContent(this, types);
        }
        return null;
    }
    public String getContentEncoding() {
        return getHeaderField("Content-Encoding"); 
    }
    private ContentHandler getContentHandler(String type) throws IOException {
        final String typeString = parseTypeString(type.replace('/', '.'));
        Object cHandler = contentHandlers.get(type);
        if (cHandler != null) {
            return (ContentHandler) cHandler;
        }
        if (contentHandlerFactory != null) {
            cHandler = contentHandlerFactory.createContentHandler(type);
            contentHandlers.put(type, cHandler);
            return (ContentHandler) cHandler;
        }
        String packageList = AccessController
                .doPrivileged(new PriviAction<String>(
                        "java.content.handler.pkgs")); 
        if (packageList != null) {
            final StringTokenizer st = new StringTokenizer(packageList, "|"); 
            while (st.countTokens() > 0) {
                try {
                    Class<?> cl = Class.forName(st.nextToken() + "." 
                            + typeString, true, ClassLoader
                            .getSystemClassLoader());
                    cHandler = cl.newInstance();
                } catch (ClassNotFoundException e) {
                } catch (IllegalAccessException e) {
                } catch (InstantiationException e) {
                }
            }
        }
        if (cHandler == null) {
            cHandler = AccessController
                    .doPrivileged(new PrivilegedAction<Object>() {
                        public Object run() {
                            try {
                                String className = "org.apache.harmony.awt.www.content." 
                                        + typeString;
                                return Class.forName(className).newInstance();
                            } catch (ClassNotFoundException e) {
                            } catch (IllegalAccessException e) {
                            } catch (InstantiationException e) {
                            }
                            return null;
                        }
                    });
        }
        if (cHandler != null) {
            if (!(cHandler instanceof ContentHandler)) {
                throw new UnknownServiceException();
            }
            contentHandlers.put(type, cHandler); 
            return (ContentHandler) cHandler;
        }
        return defaultHandler;
    }
    public int getContentLength() {
        return getHeaderFieldInt("Content-Length", -1); 
    }
    public String getContentType() {
        return getHeaderField("Content-Type"); 
    }
    public long getDate() {
        return getHeaderFieldDate("Date", 0); 
    }
    public static boolean getDefaultAllowUserInteraction() {
        return defaultAllowUserInteraction;
    }
    @Deprecated
    public static String getDefaultRequestProperty(String field) {
        return null;
    }
    public boolean getDefaultUseCaches() {
        return defaultUseCaches;
    }
    public boolean getDoInput() {
        return doInput;
    }
    public boolean getDoOutput() {
        return doOutput;
    }
    public long getExpiration() {
        return getHeaderFieldDate("Expires", 0); 
    }
    public static FileNameMap getFileNameMap() {
        synchronized (URLConnection.class) {
            if (fileNameMap == null) {
                fileNameMap = new MimeTable();
            }
            return fileNameMap;
        }
    }
    public String getHeaderField(int pos) {
        return null;
    }
    public Map<String, List<String>> getHeaderFields() {
        return Collections.emptyMap();
    }
    public Map<String, List<String>> getRequestProperties() {
        if (connected) {
            throw new IllegalStateException(Msg.getString("K0037")); 
        }
        return Collections.emptyMap();
    }
    public void addRequestProperty(String field, String newValue) {
        if (connected) {
            throw new IllegalStateException(Msg.getString("K0037")); 
        }
        if (field == null) {
            throw new NullPointerException(Msg.getString("KA007")); 
        }
    }
    public String getHeaderField(String key) {
        return null;
    }
    @SuppressWarnings("deprecation")
    public long getHeaderFieldDate(String field, long defaultValue) {
        String date = getHeaderField(field);
        if (date == null) {
            return defaultValue;
        }
        try {
            return Date.parse(date);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    public int getHeaderFieldInt(String field, int defaultValue) {
        try {
            return Integer.parseInt(getHeaderField(field));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    public String getHeaderFieldKey(int posn) {
        return null;
    }
    public long getIfModifiedSince() {
        return ifModifiedSince;
    }
    public InputStream getInputStream() throws IOException {
        throw new UnknownServiceException(Msg.getString("K004d")); 
    }
    public long getLastModified() {
        if (lastModified != -1) {
            return lastModified;
        }
        return lastModified = getHeaderFieldDate("Last-Modified", 0); 
    }
    public OutputStream getOutputStream() throws IOException {
        throw new UnknownServiceException(Msg.getString("K005f")); 
    }
    public java.security.Permission getPermission() throws IOException {
        return new java.security.AllPermission();
    }
    public String getRequestProperty(String field) {
        if (connected) {
            throw new IllegalStateException(Msg.getString("K0037")); 
        }
        return null;
    }
    public URL getURL() {
        return url;
    }
    public boolean getUseCaches() {
        return useCaches;
    }
    public static String guessContentTypeFromName(String url) {
        return getFileNameMap().getContentTypeFor(url);
    }
    @SuppressWarnings("nls")
    public static String guessContentTypeFromStream(InputStream is)
            throws IOException {
        if (!is.markSupported()) {
            return null;
        }
        is.mark(64);
        byte[] bytes = new byte[64];
        int length = is.read(bytes);
        is.reset();
        String encoding = "ASCII";
        int start = 0;
        if (length > 1) {
            if ((bytes[0] == (byte) 0xFF) && (bytes[1] == (byte) 0xFE)) {
                encoding = "UTF-16LE";
                start = 2;
                length -= length & 1;
            }
            if ((bytes[0] == (byte) 0xFE) && (bytes[1] == (byte) 0xFF)) {
                encoding = "UTF-16BE";
                start = 2;
                length -= length & 1;
            }
            if (length > 2) {
                if ((bytes[0] == (byte) 0xEF) && (bytes[1] == (byte) 0xBB)
                        && (bytes[2] == (byte) 0xBF)) {
                    encoding = "UTF-8";
                    start = 3;
                }
                if (length > 3) {
                    if ((bytes[0] == (byte) 0x00) && (bytes[1] == (byte) 0x00)
                            && (bytes[2] == (byte) 0xFE)
                            && (bytes[3] == (byte) 0xFF)) {
                        encoding = "UTF-32BE";
                        start = 4;
                        length -= length & 3;
                    }
                    if ((bytes[0] == (byte) 0xFF) && (bytes[1] == (byte) 0xFE)
                            && (bytes[2] == (byte) 0x00)
                            && (bytes[3] == (byte) 0x00)) {
                        encoding = "UTF-32LE";
                        start = 4;
                        length -= length & 3;
                    }
                }
            }
        }
        String header = new String(bytes, start, length - start, encoding);
        if (header.startsWith("PK")) {
            return "application/zip";
        }
        if (header.startsWith("GI")) {
            return "image/gif";
        }
        String textHeader = header.trim().toUpperCase();
        if (textHeader.startsWith("<!DOCTYPE HTML") ||
                textHeader.startsWith("<HTML") ||
                textHeader.startsWith("<HEAD") ||
                textHeader.startsWith("<BODY") ||
                textHeader.startsWith("<HEAD")) {
            return "text/html";
        }
        if (textHeader.startsWith("<?XML")) {
            return "application/xml";
        }
        return null;
    }
    private String parseTypeString(String typeString) {
        StringBuilder typeStringBuffer = new StringBuilder(typeString);
        for (int i = 0; i < typeStringBuffer.length(); i++) {
            char c = typeStringBuffer.charAt(i);
            if (!(Character.isLetter(c) || Character.isDigit(c) || c == '.')) {
                typeStringBuffer.setCharAt(i, '_');
            }
        }
        return typeStringBuffer.toString();
    }
    public void setAllowUserInteraction(boolean newValue) {
        if (connected) {
            throw new IllegalStateException(Msg.getString("K0037")); 
        }
        this.allowUserInteraction = newValue;
    }
    public static synchronized void setContentHandlerFactory(
            ContentHandlerFactory contentFactory) {
        if (contentHandlerFactory != null) {
            throw new Error(Msg.getString("K004e")); 
        }
        SecurityManager sManager = System.getSecurityManager();
        if (sManager != null) {
            sManager.checkSetFactory();
        }
        contentHandlerFactory = contentFactory;
    }
    public static void setDefaultAllowUserInteraction(boolean allows) {
        defaultAllowUserInteraction = allows;
    }
    @Deprecated
    public static void setDefaultRequestProperty(String field, String value) {
    }
    public void setDefaultUseCaches(boolean newValue) {
        defaultUseCaches = newValue;
    }
    public void setDoInput(boolean newValue) {
        if (connected) {
            throw new IllegalStateException(Msg.getString("K0037")); 
        }
        this.doInput = newValue;
    }
    public void setDoOutput(boolean newValue) {
        if (connected) {
            throw new IllegalStateException(Msg.getString("K0037")); 
        }
        this.doOutput = newValue;
    }
    public static void setFileNameMap(FileNameMap map) {
        SecurityManager manager = System.getSecurityManager();
        if (manager != null) {
            manager.checkSetFactory();
        }
        synchronized (URLConnection.class) {
            fileNameMap = map;
        }
    }
    public void setIfModifiedSince(long newValue) {
        if (connected) {
            throw new IllegalStateException(Msg.getString("K0037")); 
        }
        this.ifModifiedSince = newValue;
    }
    public void setRequestProperty(String field, String newValue) {
        if (connected) {
            throw new IllegalStateException(Msg.getString("K0037")); 
        }
        if (field == null) {
            throw new NullPointerException(Msg.getString("KA007")); 
        }
    }
    public void setUseCaches(boolean newValue) {
        if (connected) {
            throw new IllegalStateException(Msg.getString("K0037")); 
        }
        this.useCaches = newValue;
    }
    public void setConnectTimeout(int timeout) {
        if (0 > timeout) {
            throw new IllegalArgumentException(Msg.getString("K0036")); 
        }
        this.connectTimeout = timeout;
    }
    public int getConnectTimeout() {
        return connectTimeout;
    }
    public void setReadTimeout(int timeout) {
        if (0 > timeout) {
            throw new IllegalArgumentException(Msg.getString("K0036")); 
        }
        this.readTimeout = timeout;
    }
    public int getReadTimeout() {
        return readTimeout;
    }
    @Override
    public String toString() {
        return getClass().getName() + ":" + url.toString(); 
    }
    static class DefaultContentHandler extends java.net.ContentHandler {
        @Override
        public Object getContent(URLConnection u) throws IOException {
            return u.getInputStream();
        }
    }
}
