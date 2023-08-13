public class DomainNameValidatorTest extends AndroidTestCase {
    private static final int ALT_UNKNOWN = 0;
    private static final int ALT_DNS_NAME = 2;
    private static final int ALT_IPA_NAME = 7;
    public void testMatch() {
        checkMatch("11", new StubX509Certificate("cn=imap.g.com"), "imap.g.com", true);
        checkMatch("12", new StubX509Certificate("cn=imap2.g.com"), "imap.g.com", false);
        checkMatch("13", new StubX509Certificate("cn=sub.imap.g.com"), "imap.g.com", false);
        checkMatch("21", new StubX509Certificate("")
                .addSubjectAlternativeName(ALT_DNS_NAME, "a.y.com")
                , "imap.g.com", false);
        checkMatch("22", new StubX509Certificate("cn=imap.g.com") 
                .addSubjectAlternativeName(ALT_DNS_NAME, "a.y.com")
                , "imap.g.com", false);
        checkMatch("23", new StubX509Certificate("")
                .addSubjectAlternativeName(ALT_DNS_NAME, "imap.g.com")
                , "imap.g.com", true);
        checkMatch("24", new StubX509Certificate("")
                .addSubjectAlternativeName(ALT_DNS_NAME, "*.g.com")
                , "imap.g.com", true);
        checkMatch("31", new StubX509Certificate("")
                .addSubjectAlternativeName(ALT_IPA_NAME, "1.2.3.4")
                , "1.2.3.4", true);
        checkMatch("32", new StubX509Certificate("")
                .addSubjectAlternativeName(ALT_IPA_NAME, "1.2.3.4")
                , "1.2.3.5", false);
        checkMatch("32", new StubX509Certificate("")
                .addSubjectAlternativeName(ALT_IPA_NAME, "1.2.3.4")
                .addSubjectAlternativeName(ALT_IPA_NAME, "192.168.100.1")
                , "192.168.100.1", true);
        checkMatch("41", new StubX509Certificate("")
                .addSubjectAlternativeName(ALT_UNKNOWN, "random string 1")
                .addSubjectAlternativeName(ALT_UNKNOWN,  "random string 2")
                .addSubjectAlternativeName(ALT_DNS_NAME, "a.b.c.d")
                .addSubjectAlternativeName(ALT_DNS_NAME, "*.google.com")
                .addSubjectAlternativeName(ALT_DNS_NAME, "imap.g.com")
                .addSubjectAlternativeName(ALT_IPA_NAME, "2.33.44.55")
                .addSubjectAlternativeName(ALT_UNKNOWN, "random string 3")
                , "imap.g.com", true);
        checkMatch("42", new StubX509Certificate("")
                .addSubjectAlternativeName(ALT_UNKNOWN, "random string 1")
                .addSubjectAlternativeName(ALT_UNKNOWN, "random string 2")
                .addSubjectAlternativeName(ALT_DNS_NAME, "a.b.c.d")
                .addSubjectAlternativeName(ALT_DNS_NAME, "*.google.com")
                .addSubjectAlternativeName(ALT_DNS_NAME, "imap.g.com")
                .addSubjectAlternativeName(ALT_IPA_NAME, "2.33.44.55")
                .addSubjectAlternativeName(ALT_UNKNOWN, "random string 3")
                , "2.33.44.55", true);
        checkMatch("43", new StubX509Certificate("")
                .addSubjectAlternativeName(ALT_UNKNOWN, "random string 1")
                .addSubjectAlternativeName(ALT_UNKNOWN, "random string 2")
                .addSubjectAlternativeName(ALT_DNS_NAME, "a.b.c.d")
                .addSubjectAlternativeName(ALT_DNS_NAME, "*.google.com")
                .addSubjectAlternativeName(ALT_DNS_NAME, "imap.g.com")
                .addSubjectAlternativeName(ALT_IPA_NAME, "2.33.44.55")
                .addSubjectAlternativeName(ALT_UNKNOWN, "random string 3")
                , "g.com", false);
        checkMatch("44", new StubX509Certificate("")
                .addSubjectAlternativeName(ALT_UNKNOWN, "random string 1")
                .addSubjectAlternativeName(ALT_UNKNOWN, "random string 2")
                .addSubjectAlternativeName(ALT_DNS_NAME, "a.b.c.d")
                .addSubjectAlternativeName(ALT_DNS_NAME, "*.google.com")
                .addSubjectAlternativeName(ALT_DNS_NAME, "imap.g.com")
                .addSubjectAlternativeName(ALT_IPA_NAME, "2.33.44.55")
                .addSubjectAlternativeName(ALT_UNKNOWN, "random string 3")
                , "2.33.44.1", false);
    }
    private void checkMatch(String message, X509Certificate certificate, String thisDomain,
            boolean expected) {
        Boolean actual = DomainNameValidator.match(certificate, thisDomain);
        assertEquals(message, (Object) expected, (Object) actual);
    }
    public void testMatchDns() {
        checkMatchDns("11", "a.b.c.d", "a.b.c.d", true);
        checkMatchDns("12", "a.b.c.d", "*.b.c.d", true);
        checkMatchDns("13", "b.c.d", "*.b.c.d", true);
        checkMatchDns("14", "b.c.d", "b*.c.d", true);
        checkMatchDns("15", "a.b.c.d", "*.*.c.d", false);
        checkMatchDns("16", "a.b.c.d", "*.c.d", false);
        checkMatchDns("21", "imap.google.com", "imap.google.com", true);
        checkMatchDns("22", "imap2.google.com", "imap.google.com", false);
        checkMatchDns("23", "imap.google.com", "*.google.com", true);
        checkMatchDns("24", "imap2.google.com", "*.google.com", true);
        checkMatchDns("25", "imap.google.com", "*.googl.com", false);
        checkMatchDns("26", "imap2.google2.com", "*.google3.com", false);
        checkMatchDns("27", "imap.google.com", "ima*.google.com", true);
        checkMatchDns("28", "imap.google.com", "imap*.google.com", true);
        checkMatchDns("29", "imap.google.com", "*.imap.google.com", true);
        checkMatchDns("41", "imap.google.com", "a*.google.com", false);
        checkMatchDns("42", "imap.google.com", "ix*.google.com", false);
        checkMatchDns("51", "imap.google.com", "iMap.Google.Com", true);
    }
    private void checkMatchDns(String message, String thisDomain, String thatDomain,
            boolean expected) {
        boolean actual = DomainNameValidator.matchDns(thisDomain, thatDomain);
        assertEquals(message, expected, actual);
    }
    public void testWithActualCert() throws Exception {
        checkWithActualCert("11", R.raw.subject_only, "www.example.com", true);
        checkWithActualCert("12", R.raw.subject_only, "www2.example.com", false);
        checkWithActualCert("21", R.raw.subject_alt_only, "www.example.com", true);
        checkWithActualCert("22", R.raw.subject_alt_only, "www2.example.com", false);
        checkWithActualCert("31", R.raw.subject_with_alt_names, "www.example.com", false);
        checkWithActualCert("32", R.raw.subject_with_alt_names, "www2.example.com", true);
        checkWithActualCert("33", R.raw.subject_with_alt_names, "www3.example.com", true);
        checkWithActualCert("34", R.raw.subject_with_alt_names, "www4.example.com", false);
        checkWithActualCert("41", R.raw.subject_with_wild_alt_name, "www.example.com", false);
        checkWithActualCert("42", R.raw.subject_with_wild_alt_name, "www2.example.com", false);
        checkWithActualCert("43", R.raw.subject_with_wild_alt_name, "www.example2.com", true);
        checkWithActualCert("44", R.raw.subject_with_wild_alt_name, "abc.example2.com", true);
        checkWithActualCert("45", R.raw.subject_with_wild_alt_name, "www.example3.com", false);
        checkWithActualCert("51", R.raw.wild_alt_name_only, "www.example.com", true);
        checkWithActualCert("52", R.raw.wild_alt_name_only, "www2.example.com", true);
        checkWithActualCert("53", R.raw.wild_alt_name_only, "www.example2.com", false);
        checkWithActualCert("61", R.raw.alt_ip_only, "192.168.10.1", true);
        checkWithActualCert("61", R.raw.alt_ip_only, "192.168.10.2", false);
    }
    private void checkWithActualCert(String message, int resId, String domain,
            boolean expected) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X509");
        InputStream certStream = getContext().getResources().openRawResource(resId);
        X509Certificate certificate = (X509Certificate) factory.generateCertificate(certStream);
        checkMatch(message, certificate, domain, expected);
    }
    private static class StubX509Certificate extends X509Certificate {
        private final X500Principal subjectX500Principal;
        private Collection<List<?>> subjectAlternativeNames;
        public StubX509Certificate(String subjectDn) {
            subjectX500Principal = new X500Principal(subjectDn);
            subjectAlternativeNames = null;
        }
        public StubX509Certificate addSubjectAlternativeName(int type, String name) {
            if (subjectAlternativeNames == null) {
                subjectAlternativeNames = new ArrayList<List<?>>();
            }
            LinkedList<Object> entry = new LinkedList<Object>();
            entry.add(type);
            entry.add(name);
            subjectAlternativeNames.add(entry);
            return this;
        }
        @Override
        public Collection<List<?>> getSubjectAlternativeNames() throws CertificateParsingException {
            return subjectAlternativeNames;
        }
        @Override
        public X500Principal getSubjectX500Principal() {
            return subjectX500Principal;
        }
        @Override
        public void checkValidity() throws CertificateExpiredException,
                CertificateNotYetValidException {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public void checkValidity(Date date) throws CertificateExpiredException,
                CertificateNotYetValidException {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public int getBasicConstraints() {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public Principal getIssuerDN() {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public boolean[] getIssuerUniqueID() {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public boolean[] getKeyUsage() {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public Date getNotAfter() {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public Date getNotBefore() {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public BigInteger getSerialNumber() {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public String getSigAlgName() {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public String getSigAlgOID() {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public byte[] getSigAlgParams() {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public byte[] getSignature() {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public Principal getSubjectDN() {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public boolean[] getSubjectUniqueID() {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public byte[] getTBSCertificate() throws CertificateEncodingException {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public int getVersion() {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public byte[] getEncoded() throws CertificateEncodingException {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public PublicKey getPublicKey() {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public String toString() {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException, SignatureException {
            throw new RuntimeException("Method not implemented");
        }
        @Override
        public void verify(PublicKey key, String sigProvider) throws CertificateException,
                NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException,
                SignatureException {
            throw new RuntimeException("Method not implemented");
        }
        public Set<String> getCriticalExtensionOIDs() {
            throw new RuntimeException("Method not implemented");
        }
        public byte[] getExtensionValue(String oid) {
            throw new RuntimeException("Method not implemented");
        }
        public Set<String> getNonCriticalExtensionOIDs() {
            throw new RuntimeException("Method not implemented");
        }
        public boolean hasUnsupportedCriticalExtension() {
            throw new RuntimeException("Method not implemented");
        }
    }
}
