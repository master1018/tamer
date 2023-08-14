public class JarURLConnectionImpl extends JarURLConnection {
    static HashMap<URL, JarFile> jarCache = new HashMap<URL, JarFile>();
    private URL jarFileURL;
    private InputStream jarInput;
    private JarFile jarFile;
    private JarEntry jarEntry;
    private boolean closed;
    public JarURLConnectionImpl(URL url) throws MalformedURLException,
            IOException {
        super(url);
        jarFileURL = getJarFileURL();
        jarFileURLConnection = jarFileURL.openConnection();
    }
    @Override
    public void connect() throws IOException {
        if (!connected) {
            findJarFile(); 
            findJarEntry(); 
            connected = true;
        }
    }
    @Override
    public JarFile getJarFile() throws IOException {
        connect();
        return jarFile;
    }
    private void findJarFile() throws IOException {
        JarFile jar = null;
        if (getUseCaches()) {
            synchronized (jarCache) {
                jarFile = jarCache.get(jarFileURL);
            }
            if (jarFile == null) {
                jar = openJarFile();
                synchronized (jarCache) {
                    jarFile = jarCache.get(jarFileURL);
                    if (jarFile == null) {
                        jarCache.put(jarFileURL, jar);
                        jarFile = jar;
                    } else {
                        jar.close();
                    }
                }
            }
        } else {
            jarFile = openJarFile();
        }
        if (jarFile == null) {
            throw new IOException();
        }
    }
    @SuppressWarnings("nls")
    JarFile openJarFile() throws IOException {
        JarFile jar = null;
        if (jarFileURL.getProtocol().equals("file")) {
            jar = new JarFile(new File(Util.decode(jarFileURL.getFile(), false,
                    "UTF-8")), true, ZipFile.OPEN_READ);
        } else {
            final InputStream is = jarFileURL.openConnection().getInputStream();
            try {
                jar = AccessController
                        .doPrivileged(new PrivilegedAction<JarFile>() {
                            public JarFile run() {
                                try {
                                    File tempJar = File.createTempFile(
                                            "hyjar_", ".tmp", null);
                                    tempJar.deleteOnExit();
                                    FileOutputStream fos = new FileOutputStream(
                                            tempJar);
                                    byte[] buf = new byte[4096];
                                    int nbytes = 0;
                                    while ((nbytes = is.read(buf)) > -1) {
                                        fos.write(buf, 0, nbytes);
                                    }
                                    fos.close();
                                    return new JarFile(tempJar, true,
                                            ZipFile.OPEN_READ
                                                    | ZipFile.OPEN_DELETE);
                                } catch (IOException e) {
                                    return null;
                                }
                            }
                        });
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
        return jar;
    }
    @Override
    public JarEntry getJarEntry() throws IOException {
        connect();
        return jarEntry;
    }
    private void findJarEntry() throws IOException {
        if (getEntryName() == null) {
            return;
        }
        jarEntry = jarFile.getJarEntry(getEntryName());
        if (jarEntry == null) {
            throw new FileNotFoundException(getEntryName());
        }
    }
    @Override
    public InputStream getInputStream() throws IOException {
        if (closed) {
            throw new IllegalStateException(Msg.getString("KA027")); 
        }
        connect();
        if (jarInput != null) {
            return jarInput;
        }
        if (jarEntry == null) {
            throw new IOException(Msg.getString("K00fc")); 
        }
        return jarInput = new JarURLConnectionInputStream(jarFile
                .getInputStream(jarEntry), jarFile);
    }
    @Override
    public String getContentType() {
        if (url.getFile().endsWith("!/")) { 
            return "x-java/jar"; 
        }
        String cType = null;
        String entryName = getEntryName();
        if (entryName != null) {
            cType = guessContentTypeFromName(entryName);
        } else {
            try {
                connect();
                cType = jarFileURLConnection.getContentType();
            } catch (IOException ioe) {
            }
        }
        if (cType == null) {
            cType = "content/unknown"; 
        }
        return cType;
    }
    @Override
    public int getContentLength() {
        try {
            connect();
            if (jarEntry == null) {
                return jarFileURLConnection.getContentLength();
            }
            return (int) getJarEntry().getSize();
        } catch (IOException e) {
        }
        return -1;
    }
    @Override
    public Object getContent() throws IOException {
        connect();
        if (jarEntry == null) {
            return jarFile;
        }
        return super.getContent();
    }
    @Override
    public Permission getPermission() throws IOException {
        return jarFileURLConnection.getPermission();
    }
    @Override
    public boolean getUseCaches() {
        return jarFileURLConnection.getUseCaches();
    }
    @Override
    public void setUseCaches(boolean usecaches) {
        jarFileURLConnection.setUseCaches(usecaches);
    }
    @Override
    public boolean getDefaultUseCaches() {
        return jarFileURLConnection.getDefaultUseCaches();
    }
    @Override
    public void setDefaultUseCaches(boolean defaultusecaches) {
        jarFileURLConnection.setDefaultUseCaches(defaultusecaches);
    }
    public static void closeCachedFiles() {
        Set<Map.Entry<URL, JarFile>> s = jarCache.entrySet();
        synchronized (jarCache) {
            Iterator<Map.Entry<URL, JarFile>> i = s.iterator();
            while (i.hasNext()) {
                try {
                    ZipFile zip = i.next().getValue();
                    if (zip != null) {
                        zip.close();
                    }
                } catch (IOException e) {
                }
            }
        }
    }
    private class JarURLConnectionInputStream extends FilterInputStream {
        InputStream inputStream;
        JarFile jarFile;
        protected JarURLConnectionInputStream(InputStream in, JarFile file) {
            super(in);
            inputStream = in;
            jarFile = file;
        }
        @Override
        public void close() throws IOException {
            super.close();
            if (!getUseCaches()) {
                closed = true;
                jarFile.close();
            }
        }
        @Override
        public int read() throws IOException {
            return inputStream.read();
        }
        @Override
        public int read(byte[] buf, int off, int nbytes) throws IOException {
            return inputStream.read(buf, off, nbytes);
        }
        @Override
        public long skip(long nbytes) throws IOException {
            return inputStream.skip(nbytes);
        }
    }
}
