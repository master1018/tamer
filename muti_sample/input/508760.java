public class JarInputStream extends ZipInputStream {
    private Manifest manifest;
    private boolean eos = false;
    private JarEntry mEntry;
    private JarEntry jarEntry;
    private boolean isMeta;
    private JarVerifier verifier;
    private OutputStream verStream;
    public JarInputStream(InputStream stream, boolean verify)
            throws IOException {
        super(stream);
        if (verify) {
            verifier = new JarVerifier("JarInputStream"); 
        }
        if ((mEntry = getNextJarEntry()) == null) {
            return;
        }
        String name = Util.toASCIIUpperCase(mEntry.getName());
        if (name.equals(JarFile.META_DIR)) {
            mEntry = null; 
            closeEntry();
            mEntry = getNextJarEntry();
            name = mEntry.getName().toUpperCase();
        }
        if (name.equals(JarFile.MANIFEST_NAME)) {
            mEntry = null;
            manifest = new Manifest(this, verify);
            closeEntry();
            if (verify) {
                verifier.setManifest(manifest);
                if (manifest != null) {
                    verifier.mainAttributesEnd = manifest
                            .getMainAttributesEnd();
                }
            }
        } else {
            Attributes temp = new Attributes(3);
            temp.map.put("hidden", null); 
            mEntry.setAttributes(temp);
            verifier = null;
        }
    }
    public JarInputStream(InputStream stream) throws IOException {
        this(stream, true);
    }
    public Manifest getManifest() {
        return manifest;
    }
    public JarEntry getNextJarEntry() throws IOException {
        return (JarEntry) getNextEntry();
    }
    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (mEntry != null) {
            return -1;
        }
        int r = super.read(buffer, offset, length);
        if (verStream != null && !eos) {
            if (r == -1) {
                eos = true;
                if (verifier != null) {
                    if (isMeta) {
                        verifier.addMetaEntry(jarEntry.getName(),
                                ((ByteArrayOutputStream) verStream)
                                        .toByteArray());
                        try {
                            verifier.readCertificates();
                        } catch (SecurityException e) {
                            verifier = null;
                            throw e;
                        }
                    } else {
                        ((JarVerifier.VerifierEntry) verStream).verify();
                    }
                }
            } else {
                verStream.write(buffer, offset, r);
            }
        }
        return r;
    }
    @Override
    public ZipEntry getNextEntry() throws IOException {
        if (mEntry != null) {
            jarEntry = mEntry;
            mEntry = null;
            jarEntry.setAttributes(null);
        } else {
            jarEntry = (JarEntry) super.getNextEntry();
            if (jarEntry == null) {
                return null;
            }
            if (verifier != null) {
                isMeta = Util.toASCIIUpperCase(jarEntry.getName()).startsWith(
                        JarFile.META_DIR);
                if (isMeta) {
                    verStream = new ByteArrayOutputStream();
                } else {
                    verStream = verifier.initEntry(jarEntry.getName());
                }
            }
        }
        eos = false;
        return jarEntry;
    }
    @Override
    protected ZipEntry createZipEntry(String name) {
        JarEntry entry = new JarEntry(name);
        if (manifest != null) {
            entry.setAttributes(manifest.getAttributes(name));
        }
        return entry;
    }
}
