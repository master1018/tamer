public class JarEntry extends ZipEntry {
    private Attributes attributes;
    JarFile parentJar;
    CodeSigner signers[];
    private CertificateFactory factory;
    private boolean isFactoryChecked = false;
    public JarEntry(String name) {
        super(name);
    }
    public JarEntry(ZipEntry entry) {
        super(entry);
    }
    public Attributes getAttributes() throws IOException {
        if (attributes != null || parentJar == null) {
            return attributes;
        }
        Manifest manifest = parentJar.getManifest();
        if (manifest == null) {
            return null;
        }
        return attributes = manifest.getAttributes(getName());
    }
    public Certificate[] getCertificates() {
        if (null == parentJar) {
            return null;
        }
        JarVerifier jarVerifier = parentJar.verifier;
        if (null == jarVerifier) {
            return null;
        }
        return jarVerifier.getCertificates(getName());
    }
    void setAttributes(Attributes attrib) {
        attributes = attrib;
    }
    public JarEntry(JarEntry je) {
        super(je);
        parentJar = je.parentJar;
        attributes = je.attributes;
        signers = je.signers;
    }
    public CodeSigner[] getCodeSigners() {
        if (null == signers) {
            signers = getCodeSigners(getCertificates());
        }
        if (null == signers) {
            return null;
        }
        CodeSigner[] tmp = new CodeSigner[signers.length];
        System.arraycopy(signers, 0, tmp, 0, tmp.length);
        return tmp;
    }
    private CodeSigner[] getCodeSigners(Certificate[] certs) {
        if (null == certs) {
            return null;
        }
        X500Principal prevIssuer = null;
        ArrayList<Certificate> list = new ArrayList<Certificate>(certs.length);
        ArrayList<CodeSigner> asigners = new ArrayList<CodeSigner>();
        for (Certificate element : certs) {
            if (!(element instanceof X509Certificate)) {
                continue;
            }
            X509Certificate x509 = (X509Certificate) element;
            if (null != prevIssuer) {
                X500Principal subj = x509.getSubjectX500Principal();
                if (!prevIssuer.equals(subj)) {
                    addCodeSigner(asigners, list);
                    list.clear();
                }
            }
            prevIssuer = x509.getIssuerX500Principal();
            list.add(x509);
        }
        if (!list.isEmpty()) {
            addCodeSigner(asigners, list);
        }
        if (asigners.isEmpty()) {
            return null;
        }
        CodeSigner[] tmp = new CodeSigner[asigners.size()];
        asigners.toArray(tmp);
        return tmp;
    }
    private void addCodeSigner(ArrayList<CodeSigner> asigners,
            List<Certificate> list) {
        CertPath certPath = null;
        if (!isFactoryChecked) {
            try {
                factory = CertificateFactory.getInstance("X.509"); 
            } catch (CertificateException ex) {
            } finally {
                isFactoryChecked = true;
            }
        }
        if (null == factory) {
            return;
        }
        try {
            certPath = factory.generateCertPath(list);
        } catch (CertificateException ex) {
        }
        if (null != certPath) {
            asigners.add(new CodeSigner(certPath, null));
        }
    }
}
