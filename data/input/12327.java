public class URLJarFile extends JarFile {
    private static URLJarFileCallBack callback = null;
    private URLJarFileCloseController closeController = null;
    private static int BUF_SIZE = 2048;
    private Manifest superMan;
    private Attributes superAttr;
    private Map<String, Attributes> superEntries;
    static JarFile getJarFile(URL url) throws IOException {
        return getJarFile(url, null);
    }
    static JarFile getJarFile(URL url, URLJarFileCloseController closeController) throws IOException {
        if (isFileURL(url))
            return new URLJarFile(url, closeController);
        else {
            return retrieve(url, closeController);
        }
    }
    public URLJarFile(File file) throws IOException {
        this(file, null);
    }
    public URLJarFile(File file, URLJarFileCloseController closeController) throws IOException {
        super(file, true, ZipFile.OPEN_READ | ZipFile.OPEN_DELETE);
        this.closeController = closeController;
    }
    private URLJarFile(URL url, URLJarFileCloseController closeController) throws IOException {
        super(ParseUtil.decode(url.getFile()));
        this.closeController = closeController;
    }
    private static boolean isFileURL(URL url) {
        if (url.getProtocol().equalsIgnoreCase("file")) {
            String host = url.getHost();
            if (host == null || host.equals("") || host.equals("~") ||
                host.equalsIgnoreCase("localhost"))
                return true;
        }
        return false;
    }
    protected void finalize() throws IOException {
        close();
    }
    public ZipEntry getEntry(String name) {
        ZipEntry ze = super.getEntry(name);
        if (ze != null) {
            if (ze instanceof JarEntry)
                return new URLJarFileEntry((JarEntry)ze);
            else
                throw new InternalError(super.getClass() +
                                        " returned unexpected entry type " +
                                        ze.getClass());
        }
        return null;
    }
    public Manifest getManifest() throws IOException {
        if (!isSuperMan()) {
            return null;
        }
        Manifest man = new Manifest();
        Attributes attr = man.getMainAttributes();
        attr.putAll((Map)superAttr.clone());
        if (superEntries != null) {
            Map<String, Attributes> entries = man.getEntries();
            for (String key : superEntries.keySet()) {
                Attributes at = superEntries.get(key);
                entries.put(key, (Attributes) at.clone());
            }
        }
        return man;
    }
    public void close() throws IOException {
        if (closeController != null) {
                closeController.close(this);
        }
        super.close();
    }
    private synchronized boolean isSuperMan() throws IOException {
        if (superMan == null) {
            superMan = super.getManifest();
        }
        if (superMan != null) {
            superAttr = superMan.getMainAttributes();
            superEntries = superMan.getEntries();
            return true;
        } else
            return false;
    }
    private static JarFile retrieve(final URL url) throws IOException {
        return retrieve(url, null);
    }
     private static JarFile retrieve(final URL url, final URLJarFileCloseController closeController) throws IOException {
        if (callback != null)
        {
            return callback.retrieve(url);
        }
        else
        {
            JarFile result = null;
            try (final InputStream in = url.openConnection().getInputStream()) {
                result = AccessController.doPrivileged(
                    new PrivilegedExceptionAction<JarFile>() {
                        public JarFile run() throws IOException {
                            Path tmpFile = Files.createTempFile("jar_cache", null);
                            try {
                                Files.copy(in, tmpFile, StandardCopyOption.REPLACE_EXISTING);
                                JarFile jarFile = new URLJarFile(tmpFile.toFile(), closeController);
                                tmpFile.toFile().deleteOnExit();
                                return jarFile;
                            } catch (Throwable thr) {
                                try {
                                    Files.delete(tmpFile);
                                } catch (IOException ioe) {
                                    thr.addSuppressed(ioe);
                                }
                                throw thr;
                            }
                        }
                    });
            } catch (PrivilegedActionException pae) {
                throw (IOException) pae.getException();
            }
            return result;
        }
    }
    public static void setCallBack(URLJarFileCallBack cb)
    {
        callback = cb;
    }
    private class URLJarFileEntry extends JarEntry {
        private JarEntry je;
        URLJarFileEntry(JarEntry je) {
            super(je);
            this.je=je;
        }
        public Attributes getAttributes() throws IOException {
            if (URLJarFile.this.isSuperMan()) {
                Map<String, Attributes> e = URLJarFile.this.superEntries;
                if (e != null) {
                    Attributes a = e.get(getName());
                    if (a != null)
                        return  (Attributes)a.clone();
                }
            }
            return null;
        }
        public java.security.cert.Certificate[] getCertificates() {
            Certificate[] certs = je.getCertificates();
            return certs == null? null: certs.clone();
        }
        public CodeSigner[] getCodeSigners() {
            CodeSigner[] csg = je.getCodeSigners();
            return csg == null? null: csg.clone();
        }
    }
    public interface URLJarFileCloseController {
        public void close(JarFile jarFile);
    }
}
