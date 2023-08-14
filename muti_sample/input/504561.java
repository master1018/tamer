public class MyKeyStoreSpi extends KeyStoreSpi {
    @SuppressWarnings("unused")
    public Key engineGetKey(String alias, char[] password)
            throws NoSuchAlgorithmException, UnrecoverableKeyException {
        return null;
    }
    public Certificate[] engineGetCertificateChain(String alias) {
        return null;
    }
    public Certificate engineGetCertificate(String alias) {
        if (alias.equals("test_engineEntryInstanceOf_Alias1")) {
            return new MyCertificate("TestType");
        }
        return null;
    }
    public Date engineGetCreationDate(String alias) {
        return new Date(0);
    }
    public void engineSetKeyEntry(String alias, Key key, char[] password,
            Certificate[] chain) throws KeyStoreException {
        throw new KeyStoreException(
                "engineSetKeyEntry is not supported in myKeyStoreSpi");
    }
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain)
            throws KeyStoreException {
        throw new KeyStoreException(
                "engineSetKeyEntry is not supported in myKeyStoreSpi");
    }
    public void engineSetCertificateEntry(String alias, Certificate cert)
            throws KeyStoreException {
        throw new KeyStoreException(
                "engineSetCertificateEntry is not supported in myKeyStoreSpi");
    }
    public void engineDeleteEntry(String alias) throws KeyStoreException {
        throw new KeyStoreException(
                "engineDeleteEntry is not supported in myKeyStoreSpi");
    }
    public Enumeration<String> engineAliases() {
        return null;
    }
    public boolean engineContainsAlias(String alias) {
        if (alias != null)
        {
            return alias.startsWith("test_engineEntry");
        }
        return false;
    }
    public int engineSize() {
        return 0;
    }
    public boolean engineIsKeyEntry(String alias) {
        if (alias.equals("test_engineEntryInstanceOf_Alias1")) {
            return true;
        } else {
            return false;
        }
    }
    public boolean engineIsCertificateEntry(String alias) {
        if (alias.equals("test_engineEntryInstanceOf_Alias2")) {
            return true;
        } else {
            return false;
        }
    }
    public String engineGetCertificateAlias(Certificate cert) {
        return "";
    }
    @SuppressWarnings("unused")
    public void engineStore(OutputStream stream, char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        if (!(stream instanceof ByteArrayOutputStream)) {
            throw new IOException("Incorrect stream");
        }
        if (((ByteArrayOutputStream) stream).size() == 0) {
            throw new IOException("Incorrect stream size ");
        }
    }
    @SuppressWarnings("unused")
    public void engineLoad(InputStream stream, char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException {
    }
    class MyCertificate extends Certificate {
        public MyCertificate(String type) {
            super(type);
        }
        public byte[] getEncoded() {
            return null;
        }
        public PublicKey getPublicKey() {
            return null;
        }
        public String toString() {
            return null;
        }
        public void verify(PublicKey key) {
        }
        public void verify(PublicKey key, String sigProvider) {
        }
    }
}