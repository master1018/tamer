public final class TestCertUtils {
    private TestCertUtils() {
        throw new Error("statics only");
    }
    public static Certificate getCert() {
        return new TestCertificate();
    }
    public static Certificate[] getCertChain() {
        Certificate[] chain = { new TestCertificate(), new TestCertificate(),
                new TestCertificate() };
        return chain;
    }
    public static CertPath getCertPath() {
        return new TestCertPath();
    }
    public static CertPath genCertPath(int howMany, int startID) {
        Certificate[] certs = new Certificate[howMany];
        for (int i = 0; i < howMany; i++) {
            certs[i] = new TestCertificate(Integer.toString(startID + i));
        }
        return new TestCertPath(certs);
    }
    private static Provider provider = null;
    private static final String providerName = "TstPrvdr";
    public static final X500Principal rootPrincipal = new X500Principal(
            UniGen.rootName);
    public static final X509Certificate rootCA = new TestX509Certificate(
            rootPrincipal, rootPrincipal);
    public static void install_test_x509_factory() {
        if (provider == null) {
            provider = new TestProvider(providerName, 0.01,
                    "Test provider for serialization testing");
            Security.insertProviderAt(provider, 1);
        }
    }
    public static void uninstall_test_x509_factory() {
        if (provider != null) {
            Security.removeProvider(providerName);
            provider = null;
        }
    }
    public static final class TestCertPath extends CertPath implements
            Serializable {
        private static final byte[] encoded = new byte[] { 1, 2, 3, 4, 5, 6, 7,
                8, 9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF };
        private static final String serializedData = "Just a dummy string to be serialized instead of real data";
        private Certificate[] certs;
        public TestCertPath() {
            super("testCertPath");
            certs = getCertChain();
        }
        public TestCertPath(Certificate[] certs) {
            super("testCertPath");
            this.certs = certs;
        }
        public List<Certificate> getCertificates() {
            return Arrays.asList(certs);
        }
        public byte[] getEncoded() throws CertificateEncodingException {
            return encoded.clone();
        }
        public byte[] getEncoded(String encoding)
                throws CertificateEncodingException {
            return encoded.clone();
        }
        public Iterator<String> getEncodings() {
            Vector<String> v = new Vector<String>();
            v.add("myTestEncoding");
            return v.iterator();
        }
        public String toString() {
            StringBuffer buf = new StringBuffer(200);
            buf.append("TestCertPath. certs count=");
            if( certs == null ) {
                buf.append("0\n");
            }
            else {
                buf.append(certs.length).append("\n");
                for( int i=0; i<certs.length; i++) {
                    buf.append("\t").append(i).append(" ");
                    buf.append(certs[i]).append("\n");
                }
            }
            return buf.toString();
        }
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeUTF(serializedData);
            if (certs == null) {
                out.writeInt(0);
            } else {
                out.writeInt(certs.length);
                for (int i = 0; i < certs.length; i++) {
                    out.writeObject(certs[i]);
                }
            }
        }
        private void readObject(ObjectInputStream in) throws IOException,
                ClassNotFoundException {
            String s = in.readUTF();
            if (!serializedData.equals(s)) {
                throw new StreamCorruptedException("expect [" + serializedData
                        + "] got [" + s + "]");
            }
            int count = in.readInt();
            certs = new Certificate[count];
            for (int i = 0; i < count; i++) {
                certs[i] = (Certificate) in.readObject();
            }
        }
        protected Object writeReplace() {
            return this;
        }
        protected Object readResolve() {
            return this;
        }
    }
    public static final class TestPublicKey implements PublicKey {
        private static final String algo = "testPublicKeyAlgorithm";
        private static final byte[] encoded = new byte[] { 1, 2, 3, 4, 5, 6, 7,
                8, 9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF };
        private static final String format = "testPublicKeyFormat";
        public String getAlgorithm() {
            return algo;
        }
        public byte[] getEncoded() {
            return encoded.clone();
        }
        public String getFormat() {
            return format;
        }
    }
    public static class TestCertificate extends Certificate implements
            Serializable {
        private static final byte[] encoded = new byte[] { 1, 2, 3, 4, 5, 6, 7,
                8, 9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF };
        public static final String TYPE = "Test";
        private String diff = null;
        public TestCertificate() {
            super(TYPE);
        }
        public TestCertificate(String diff) {
            super(TYPE);
            this.diff = diff;
        }
        public TestCertificate(String diff, String type) {
            super(type);
            this.diff = diff;
        }
        public byte[] getEncoded() throws CertificateEncodingException {
            return encoded.clone();
        }
        public void verify(PublicKey key) throws CertificateException,
                NoSuchAlgorithmException, InvalidKeyException,
                NoSuchProviderException, SignatureException {
        }
        public void verify(PublicKey key, String sigProvider)
                throws CertificateException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException,
                SignatureException {
        }
        public String toString() {
            return "Test certificate - for unit testing only";
        }
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof TestCertificate)) {
                return false;
            }
            TestCertificate that = (TestCertificate) obj;
            if (this == that) {
                return true;
            }
            if (this.diff == null) {
                return that.diff == null;
            }
            return this.diff.equals(that.diff);
        }
        public PublicKey getPublicKey() {
            return new TestPublicKey();
        }
        private void writeObject(ObjectOutputStream out) throws IOException {
            if (diff == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(false);
                out.writeUTF(diff);
            }
        }
        private void readObject(ObjectInputStream in) throws IOException,
                ClassNotFoundException {
            boolean hasDiffString = in.readBoolean();
            if (hasDiffString) {
                diff = in.readUTF();
            }
        }
        protected Object writeReplace() {
            return this;
        }
        protected Object readResolve() {
            return this;
        }
    }
    public static class TestInvalidX509Certificate extends TestX509Certificate {
        public TestInvalidX509Certificate(X500Principal subj,
                X500Principal issuer) {
            super(subj, issuer);
        }
    }
    public static class TestX509Certificate extends X509Certificate {
        private X500Principal subject;
        private X500Principal issuer;
        public TestX509Certificate(X500Principal subj, X500Principal issuer) {
            this.subject = subj;
            this.issuer = issuer;
        }
        public X500Principal getIssuerX500Principal() {
            return issuer;
        }
        public X500Principal getSubjectX500Principal() {
            return subject;
        }
        public byte[] getEncoded() throws CertificateEncodingException {
            byte[] asubj = subject.getEncoded();
            byte[] aissuer = issuer.getEncoded();
            byte[] data = new byte[asubj.length + aissuer.length + 1];
            System.arraycopy(asubj, 0, data, 0, asubj.length);
            System
                    .arraycopy(aissuer, 0, data, asubj.length + 1,
                            aissuer.length);
            return data;
        }
        public void checkValidity() throws CertificateExpiredException,
                CertificateNotYetValidException {
        }
        public void checkValidity(Date date)
                throws CertificateExpiredException,
                CertificateNotYetValidException {
        }
        public int getBasicConstraints() {
            return 0;
        }
        public Principal getIssuerDN() {
            return null;
        }
        public boolean[] getIssuerUniqueID() {
            return null;
        }
        public boolean[] getKeyUsage() {
            return null;
        }
        public Date getNotAfter() {
            return null;
        }
        public Date getNotBefore() {
            return null;
        }
        public BigInteger getSerialNumber() {
            return null;
        }
        public String getSigAlgName() {
            return null;
        }
        public String getSigAlgOID() {
            return null;
        }
        public byte[] getSigAlgParams() {
            return null;
        }
        public byte[] getSignature() {
            return null;
        }
        public Principal getSubjectDN() {
            return null;
        }
        public boolean[] getSubjectUniqueID() {
            return null;
        }
        public byte[] getTBSCertificate() throws CertificateEncodingException {
            return null;
        }
        public int getVersion() {
            return 0;
        }
        public Set getCriticalExtensionOIDs() {
            return null;
        }
        public byte[] getExtensionValue(String oid) {
            return null;
        }
        public Set getNonCriticalExtensionOIDs() {
            return null;
        }
        public boolean hasUnsupportedCriticalExtension() {
            return false;
        }
        public PublicKey getPublicKey() {
            return null;
        }
        public String toString() {
            return null;
        }
        public void verify(PublicKey key, String sigProvider)
                throws CertificateException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException,
                SignatureException {
        }
        public void verify(PublicKey key) throws CertificateException,
                NoSuchAlgorithmException, InvalidKeyException,
                NoSuchProviderException, SignatureException {
        }
    }
    public static class TestProvider extends Provider {
        private Provider.Service serv;
        public TestProvider(String name, double version, String info) {
            super(name, version, info);
            serv = new Provider.Service(this, "CertificateFactory", "X.509",
                    TestFactorySpi.class.getName(), new ArrayList<String>(), null);
        }
        public synchronized Set<Provider.Service> getServices() {
            HashSet<Provider.Service> s = new HashSet<Service>();
            s.add(serv);
            return s;
        }
    }
    public static class TestFactorySpi extends CertificateFactorySpi {
        public Certificate engineGenerateCertificate(InputStream is)
                throws CertificateException {
            byte[] data = new byte[0];
            byte[] chunk = new byte[1024];
            int len;
            try {
                while ((len = is.read(chunk)) > 0) {
                    byte[] tmp = new byte[data.length + len];
                    System.arraycopy(data, 0, tmp, 0, data.length);
                    System.arraycopy(chunk, 0, tmp, data.length, len);
                    data = tmp;
                }
            } catch (IOException ex) {
                throw new CertificateException("IO problem", ex);
            }
            int pos = Arrays.binarySearch(data, (byte) 0);
            if (pos < 0) {
                throw new CertificateException("invalid format");
            }
            byte[] subjNameData = new byte[pos];
            System.arraycopy(data, 0, subjNameData, 0, subjNameData.length);
            byte[] issNameData = new byte[data.length - pos - 1];
            System.arraycopy(data, pos + 1, issNameData, 0, issNameData.length);
            X500Principal subjName = new X500Principal(subjNameData);
            X500Principal issName = new X500Principal(issNameData);
            return new TestX509Certificate(subjName, issName);
        }
        public Collection engineGenerateCertificates(InputStream inStream)
                throws CertificateException {
            throw new UnsupportedOperationException("not yet.");
        }
        public CRL engineGenerateCRL(InputStream inStream) throws CRLException {
            throw new UnsupportedOperationException("not yet.");
        }
        public Collection engineGenerateCRLs(InputStream inStream)
                throws CRLException {
            throw new UnsupportedOperationException("not yet.");
        }
        public CertPath engineGenerateCertPath(List certs)
                throws CertificateException {
            ArrayList<Certificate> validCerts = new ArrayList<Certificate>();
            for (Iterator i = certs.iterator(); i.hasNext();) {
                Certificate c = (Certificate) i.next();
                if (!(c instanceof X509Certificate)) {
                    throw new CertificateException("Not X509: " + c);
                }
                if (c instanceof TestInvalidX509Certificate) {
                    throw new CertificateException("Invalid (test) X509: " + c);
                }
                validCerts.add(c);
            }
            Certificate[] acerts = new Certificate[validCerts.size()];
            validCerts.toArray(acerts);
            return new TestCertPath(acerts);
        }
    }
    public static class UniGen {
        public static final String rootName = "CN=Alex Astapchuk, OU=SSG, O=Intel ZAO, C=RU";
        private static final String datasNames[] = { "CN", "OU", "O", "C" };
        private static final String datas[][] = {
                { "Alex Astapchuk", null, null, null },
                { "John Doe", null, null, null },
                { null, "SSG", null, null }, { null, "SSG/DRL", null, null },
                { null, null, "Intel ZAO", null },
                { null, null, "Intel Inc", null },
                { null, null, null, "RU" }, { null, null, null, "US" },
                { null, null, null, "GB" }, { null, null, null, "JA" },
                { null, null, null, "KO" }, { null, null, null, "TW" }, };
        private static String getData(int col, int startRow) {
            startRow = startRow % datas.length;
            for (int i = startRow; i < datas.length; i++) {
                if (datas[i][col] != null) {
                    return datas[i][col];
                }
            }
            for (int i = 0; i < datas.length; i++) {
                if (datas[i][col] != null) {
                    return datas[i][col];
                }
            }
            throw new Error();
        }
        private static boolean inc(int[] num, int base) {
            for (int i = 0; i < num.length; i++) {
                if ((++num[i]) >= base) {
                    num[i] = 0;
                } else {
                    return false;
                }
            }
            return true;
        }
        public static String[] genNames(int howMany) {
            int counts[] = new int[datasNames.length];
            ArrayList<String> al = new ArrayList<String>();
            for (int i = 0; i < howMany;) {
                StringBuffer buf = new StringBuffer();
                int j = 0;
                for (; j < datasNames.length - 1; j++) {
                    String name = datasNames[j];
                    String val = getData(j, counts[j]);
                    buf.append(name).append('=').append(val).append(",");
                }
                String name = datasNames[j];
                String val = getData(j, counts[j]);
                buf.append(name).append('=').append(val);
                name = buf.toString();
                if (!(rootName.equals(name) || al.contains(name))) {
                    ++i;
                    al.add(name);
                } else {
                }
                if (inc(counts, datas.length)) {
                    throw new Error(
                            "cant generate so many uniq names. sorry. add some more data.");
                }
            }
            return (String[]) al.toArray(new String[al.size()]);
        }
        public static X500Principal[] genX500s(int howMany) {
            String names[] = genNames(howMany);
            X500Principal[] ps = new X500Principal[howMany];
            for (int i = 0; i < howMany; i++) {
                ps[i] = new X500Principal(names[i]);
            }
            return ps;
        }
    }
}
