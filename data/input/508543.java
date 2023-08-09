public class JarFile extends ZipFile {
    public static final String MANIFEST_NAME = "META-INF/MANIFEST.MF"; 
    static final String META_DIR = "META-INF/"; 
    private Manifest manifest;
    private ZipEntry manifestEntry;
    JarVerifier verifier;
    private boolean closed = false;
    static final class JarFileInputStream extends FilterInputStream {
        private long count;
        private ZipEntry zipEntry;
        private JarVerifier.VerifierEntry entry;
        private boolean done = false;
        JarFileInputStream(InputStream is, ZipEntry ze,
                JarVerifier.VerifierEntry e) {
            super(is);
            zipEntry = ze;
            count = zipEntry.getSize();
            entry = e;
        }
        @Override
        public int read() throws IOException {
            if (done) {
                return -1;
            }
            if (count > 0) {
                int r = super.read();
                if (r != -1) {
                    entry.write(r);
                    count--;
                } else {
                    count = 0;
                }
                if (count == 0) {
                    done = true;
                    entry.verify();
                }
                return r;
            } else {
                done = true;
                entry.verify();
                return -1;
            }
        }
        @Override
        public int read(byte[] buf, int off, int nbytes) throws IOException {
            if (done) {
                return -1;
            }
            if (count > 0) {
                int r = super.read(buf, off, nbytes);
                if (r != -1) {
                    int size = r;
                    if (count < size) {
                        size = (int) count;
                    }
                    entry.write(buf, off, size);
                    count -= size;
                } else {
                    count = 0;
                }
                if (count == 0) {
                    done = true;
                    entry.verify();
                }
                return r;
            } else {
                done = true;
                entry.verify();
                return -1;
            }
        }
        @Override
        public int available() throws IOException {
            if (done) {
                return 0;
            }
            return super.available();
        }
        @Override
        public long skip(long nbytes) throws IOException {
            long cnt = 0, rem = 0;
            byte[] buf = new byte[(int)Math.min(nbytes, 2048L)];
            while (cnt < nbytes) {
                int x = read(buf, 0,
                        (rem = nbytes - cnt) > buf.length ? buf.length
                                : (int) rem);
                if (x == -1) {
                    return cnt;
                }
                cnt += x;
            }
            return cnt;
        }
    }
    public JarFile(File file) throws IOException {
        this(file, true);
    }
    public JarFile(File file, boolean verify) throws IOException {
        super(file);
        if (verify) {
            verifier = new JarVerifier(file.getPath());
        }
        readMetaEntries();
    }
    public JarFile(File file, boolean verify, int mode) throws IOException {
        super(file, mode);
        if (verify) {
            verifier = new JarVerifier(file.getPath());
        }
        readMetaEntries();
    }
    public JarFile(String filename) throws IOException {
        this(filename, true);
    }
    public JarFile(String filename, boolean verify) throws IOException {
        super(filename);
        if (verify) {
            verifier = new JarVerifier(filename);
        }
        readMetaEntries();
    }
    @Override
    public Enumeration<JarEntry> entries() {
        class JarFileEnumerator implements Enumeration<JarEntry> {
            Enumeration<? extends ZipEntry> ze;
            JarFile jf;
            JarFileEnumerator(Enumeration<? extends ZipEntry> zenum, JarFile jf) {
                ze = zenum;
                this.jf = jf;
            }
            public boolean hasMoreElements() {
                return ze.hasMoreElements();
            }
            public JarEntry nextElement() {
                JarEntry je = new JarEntry(ze.nextElement());
                je.parentJar = jf;
                return je;
            }
        }
        return new JarFileEnumerator(super.entries(), this);
    }
    public JarEntry getJarEntry(String name) {
        return (JarEntry) getEntry(name);
    }
    public Manifest getManifest() throws IOException {
        if (closed) {
            throw new IllegalStateException(Messages.getString("archive.35")); 
        }
        if (manifest != null) {
            return manifest;
        }
        try {
            InputStream is = super.getInputStream(manifestEntry);
            if (verifier != null) {
                verifier.addMetaEntry(manifestEntry.getName(),
                        InputStreamHelper.readFullyAndClose(is));
                is = super.getInputStream(manifestEntry);
            }
            try {
                manifest = new Manifest(is, verifier != null);
            } finally {
                is.close();
            }
            manifestEntry = null;  
        } catch (NullPointerException e) {
            manifestEntry = null;
        }
        return manifest;
    }
    private void readMetaEntries() throws IOException {
        ZipEntry[] metaEntries = getMetaEntriesImpl();
        if (metaEntries == null) {
            verifier = null;
            return;
        }
        boolean signed = false;
        for (ZipEntry entry : metaEntries) {
            String entryName = entry.getName();
            if (manifestEntry == null
                    && Util.asciiEqualsIgnoreCase(MANIFEST_NAME, entryName)) {
                manifestEntry = entry;
                if (verifier == null) {
                    break;
                }
            } else {
                if (verifier != null
                        && (Util.asciiEndsWithIgnoreCase(entryName, ".SF")
                                || Util.asciiEndsWithIgnoreCase(entryName, ".DSA")
                                || Util.asciiEndsWithIgnoreCase(entryName, ".RSA"))) {
                    signed = true;
                    InputStream is = super.getInputStream(entry);
                    byte[] buf = InputStreamHelper.readFullyAndClose(is);
                    verifier.addMetaEntry(entryName, buf);
                }
            }
        }
        if (!signed) {
            verifier = null;
        }
    }
    @Override
    public InputStream getInputStream(ZipEntry ze) throws IOException {
        if (manifestEntry != null) {
            getManifest();
        }
        if (verifier != null) {
            verifier.setManifest(getManifest());
            if (manifest != null) {
                verifier.mainAttributesEnd = manifest.getMainAttributesEnd();
            }
            if (verifier.readCertificates()) {
                verifier.removeMetaEntries();
                if (manifest != null) {
                    manifest.removeChunks();
                }
                if (!verifier.isSignedJar()) {
                    verifier = null;
                }
            }
        }
        InputStream in = super.getInputStream(ze);
        if (in == null) {
            return null;
        }
        if (verifier == null || ze.getSize() == -1) {
            return in;
        }
        JarVerifier.VerifierEntry entry = verifier.initEntry(ze.getName());
        if (entry == null) {
            return in;
        }
        return new JarFileInputStream(in, ze, entry);
    }
    @Override
    public ZipEntry getEntry(String name) {
        ZipEntry ze = super.getEntry(name);
        if (ze == null) {
            return ze;
        }
        JarEntry je = new JarEntry(ze);
        je.parentJar = this;
        return je;
    }
    private ZipEntry[] getMetaEntriesImpl() {
        List<ZipEntry> list = new ArrayList<ZipEntry>(8);
        Enumeration<? extends ZipEntry> allEntries = entries();
        while (allEntries.hasMoreElements()) {
            ZipEntry ze = allEntries.nextElement();
            if (ze.getName().startsWith(META_DIR)
                    && ze.getName().length() > META_DIR.length()) {
                list.add(ze);
            }
        }
        if (list.size() == 0) {
            return null;
        }
        ZipEntry[] result = new ZipEntry[list.size()];
        list.toArray(result);
        return result;
    }
    @Override
    public void close() throws IOException {
        super.close();
        closed = true;
    }
}
