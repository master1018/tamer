public class LDAPCertStore extends CertStoreSpi {
    private static final Debug debug = Debug.getInstance("certpath");
    private final static boolean DEBUG = false;
    private static final String USER_CERT = "userCertificate;binary";
    private static final String CA_CERT = "cACertificate;binary";
    private static final String CROSS_CERT = "crossCertificatePair;binary";
    private static final String CRL = "certificateRevocationList;binary";
    private static final String ARL = "authorityRevocationList;binary";
    private static final String DELTA_CRL = "deltaRevocationList;binary";
    private final static String[] STRING0 = new String[0];
    private final static byte[][] BB0 = new byte[0][];
    private final static Attributes EMPTY_ATTRIBUTES = new BasicAttributes();
    private final static int DEFAULT_CACHE_SIZE = 750;
    private final static int DEFAULT_CACHE_LIFETIME = 30;
    private final static int LIFETIME;
    private final static String PROP_LIFETIME =
                            "sun.security.certpath.ldap.cache.lifetime";
    static {
        String s = AccessController.doPrivileged(
                                new GetPropertyAction(PROP_LIFETIME));
        if (s != null) {
            LIFETIME = Integer.parseInt(s); 
        } else {
            LIFETIME = DEFAULT_CACHE_LIFETIME;
        }
    }
    private CertificateFactory cf;
    private DirContext ctx;
    private boolean prefetchCRLs = false;
    private final Cache valueCache;
    private int cacheHits = 0;
    private int cacheMisses = 0;
    private int requests = 0;
    public LDAPCertStore(CertStoreParameters params)
            throws InvalidAlgorithmParameterException {
        super(params);
        if (!(params instanceof LDAPCertStoreParameters))
          throw new InvalidAlgorithmParameterException(
            "parameters must be LDAPCertStoreParameters");
        LDAPCertStoreParameters lparams = (LDAPCertStoreParameters) params;
        createInitialDirContext(lparams.getServerName(), lparams.getPort());
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new InvalidAlgorithmParameterException(
                "unable to create CertificateFactory for X.509");
        }
        if (LIFETIME == 0) {
            valueCache = Cache.newNullCache();
        } else if (LIFETIME < 0) {
            valueCache = Cache.newSoftMemoryCache(DEFAULT_CACHE_SIZE);
        } else {
            valueCache = Cache.newSoftMemoryCache(DEFAULT_CACHE_SIZE, LIFETIME);
        }
    }
    private static final Cache certStoreCache = Cache.newSoftMemoryCache(185);
    static synchronized CertStore getInstance(LDAPCertStoreParameters params)
        throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        CertStore lcs = (CertStore) certStoreCache.get(params);
        if (lcs == null) {
            lcs = CertStore.getInstance("LDAP", params);
            certStoreCache.put(params, lcs);
        } else {
            if (debug != null) {
                debug.println("LDAPCertStore.getInstance: cache hit");
            }
        }
        return lcs;
    }
    private void createInitialDirContext(String server, int port)
            throws InvalidAlgorithmParameterException {
        String url = "ldap:
        Hashtable<String,Object> env = new Hashtable<String,Object>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, url);
        try {
            ctx = new InitialDirContext(env);
            Hashtable<?,?> currentEnv = ctx.getEnvironment();
            if (currentEnv.get(Context.REFERRAL) == null) {
                ctx.addToEnvironment(Context.REFERRAL, "follow");
            }
        } catch (NamingException e) {
            if (debug != null) {
                debug.println("LDAPCertStore.engineInit about to throw "
                    + "InvalidAlgorithmParameterException");
                e.printStackTrace();
            }
            Exception ee = new InvalidAlgorithmParameterException
                ("unable to create InitialDirContext using supplied parameters");
            ee.initCause(e);
            throw (InvalidAlgorithmParameterException)ee;
        }
    }
    private class LDAPRequest {
        private final String name;
        private Map<String, byte[][]> valueMap;
        private final List<String> requestedAttributes;
        LDAPRequest(String name) {
            this.name = name;
            requestedAttributes = new ArrayList<String>(5);
        }
        String getName() {
            return name;
        }
        void addRequestedAttribute(String attrId) {
            if (valueMap != null) {
                throw new IllegalStateException("Request already sent");
            }
            requestedAttributes.add(attrId);
        }
        byte[][] getValues(String attrId) throws NamingException {
            if (DEBUG && ((cacheHits + cacheMisses) % 50 == 0)) {
                System.out.println("Cache hits: " + cacheHits + "; misses: "
                        + cacheMisses);
            }
            String cacheKey = name + "|" + attrId;
            byte[][] values = (byte[][])valueCache.get(cacheKey);
            if (values != null) {
                cacheHits++;
                return values;
            }
            cacheMisses++;
            Map<String, byte[][]> attrs = getValueMap();
            values = attrs.get(attrId);
            return values;
        }
        private Map<String, byte[][]> getValueMap() throws NamingException {
            if (valueMap != null) {
                return valueMap;
            }
            if (DEBUG) {
                System.out.println("Request: " + name + ":" + requestedAttributes);
                requests++;
                if (requests % 5 == 0) {
                    System.out.println("LDAP requests: " + requests);
                }
            }
            valueMap = new HashMap<String, byte[][]>(8);
            String[] attrIds = requestedAttributes.toArray(STRING0);
            Attributes attrs;
            try {
                attrs = ctx.getAttributes(name, attrIds);
            } catch (NameNotFoundException e) {
                attrs = EMPTY_ATTRIBUTES;
            }
            for (String attrId : requestedAttributes) {
                Attribute attr = attrs.get(attrId);
                byte[][] values = getAttributeValues(attr);
                cacheAttribute(attrId, values);
                valueMap.put(attrId, values);
            }
            return valueMap;
        }
        private void cacheAttribute(String attrId, byte[][] values) {
            String cacheKey = name + "|" + attrId;
            valueCache.put(cacheKey, values);
        }
        private byte[][] getAttributeValues(Attribute attr)
                throws NamingException {
            byte[][] values;
            if (attr == null) {
                values = BB0;
            } else {
                values = new byte[attr.size()][];
                int i = 0;
                NamingEnumeration<?> enum_ = attr.getAll();
                while (enum_.hasMore()) {
                    Object obj = enum_.next();
                    if (debug != null) {
                        if (obj instanceof String) {
                            debug.println("LDAPCertStore.getAttrValues() "
                                + "enum.next is a string!: " + obj);
                        }
                    }
                    byte[] value = (byte[])obj;
                    values[i++] = value;
                }
            }
            return values;
        }
    }
    private Collection<X509Certificate> getCertificates(LDAPRequest request,
        String id, X509CertSelector sel) throws CertStoreException {
        byte[][] encodedCert;
        try {
            encodedCert = request.getValues(id);
        } catch (NamingException namingEx) {
            throw new CertStoreException(namingEx);
        }
        int n = encodedCert.length;
        if (n == 0) {
            return Collections.<X509Certificate>emptySet();
        }
        List<X509Certificate> certs = new ArrayList<X509Certificate>(n);
        for (int i = 0; i < n; i++) {
            ByteArrayInputStream bais = new ByteArrayInputStream(encodedCert[i]);
            try {
                Certificate cert = cf.generateCertificate(bais);
                if (sel.match(cert)) {
                  certs.add((X509Certificate)cert);
                }
            } catch (CertificateException e) {
                if (debug != null) {
                    debug.println("LDAPCertStore.getCertificates() encountered "
                        + "exception while parsing cert, skipping the bad data: ");
                    HexDumpEncoder encoder = new HexDumpEncoder();
                    debug.println(
                        "[ " + encoder.encodeBuffer(encodedCert[i]) + " ]");
                }
            }
        }
        return certs;
    }
    private Collection<X509CertificatePair> getCertPairs(
        LDAPRequest request, String id) throws CertStoreException {
        byte[][] encodedCertPair;
        try {
            encodedCertPair = request.getValues(id);
        } catch (NamingException namingEx) {
            throw new CertStoreException(namingEx);
        }
        int n = encodedCertPair.length;
        if (n == 0) {
            return Collections.<X509CertificatePair>emptySet();
        }
        List<X509CertificatePair> certPairs =
                                new ArrayList<X509CertificatePair>(n);
        for (int i = 0; i < n; i++) {
            try {
                X509CertificatePair certPair =
                    X509CertificatePair.generateCertificatePair(encodedCertPair[i]);
                certPairs.add(certPair);
            } catch (CertificateException e) {
                if (debug != null) {
                    debug.println(
                        "LDAPCertStore.getCertPairs() encountered exception "
                        + "while parsing cert, skipping the bad data: ");
                    HexDumpEncoder encoder = new HexDumpEncoder();
                    debug.println(
                        "[ " + encoder.encodeBuffer(encodedCertPair[i]) + " ]");
                }
            }
        }
        return certPairs;
    }
    private Collection<X509Certificate> getMatchingCrossCerts(
            LDAPRequest request, X509CertSelector forward,
            X509CertSelector reverse)
            throws CertStoreException {
        Collection<X509CertificatePair> certPairs =
                                getCertPairs(request, CROSS_CERT);
        ArrayList<X509Certificate> matchingCerts =
                                        new ArrayList<X509Certificate>();
        for (X509CertificatePair certPair : certPairs) {
            X509Certificate cert;
            if (forward != null) {
                cert = certPair.getForward();
                if ((cert != null) && forward.match(cert)) {
                    matchingCerts.add(cert);
                }
            }
            if (reverse != null) {
                cert = certPair.getReverse();
                if ((cert != null) && reverse.match(cert)) {
                    matchingCerts.add(cert);
                }
            }
        }
        return matchingCerts;
    }
    public synchronized Collection<X509Certificate> engineGetCertificates
            (CertSelector selector) throws CertStoreException {
        if (debug != null) {
            debug.println("LDAPCertStore.engineGetCertificates() selector: "
                + String.valueOf(selector));
        }
        if (selector == null) {
            selector = new X509CertSelector();
        }
        if (!(selector instanceof X509CertSelector)) {
            throw new CertStoreException("LDAPCertStore needs an X509CertSelector " +
                                         "to find certs");
        }
        X509CertSelector xsel = (X509CertSelector) selector;
        int basicConstraints = xsel.getBasicConstraints();
        String subject = xsel.getSubjectAsString();
        String issuer = xsel.getIssuerAsString();
        HashSet<X509Certificate> certs = new HashSet<X509Certificate>();
        if (debug != null) {
            debug.println("LDAPCertStore.engineGetCertificates() basicConstraints: "
                + basicConstraints);
        }
        if (subject != null) {
            if (debug != null) {
                debug.println("LDAPCertStore.engineGetCertificates() "
                    + "subject is not null");
            }
            LDAPRequest request = new LDAPRequest(subject);
            if (basicConstraints > -2) {
                request.addRequestedAttribute(CROSS_CERT);
                request.addRequestedAttribute(CA_CERT);
                request.addRequestedAttribute(ARL);
                if (prefetchCRLs) {
                    request.addRequestedAttribute(CRL);
                }
            }
            if (basicConstraints < 0) {
                request.addRequestedAttribute(USER_CERT);
            }
            if (basicConstraints > -2) {
                certs.addAll(getMatchingCrossCerts(request, xsel, null));
                if (debug != null) {
                    debug.println("LDAPCertStore.engineGetCertificates() after "
                        + "getMatchingCrossCerts(subject,xsel,null),certs.size(): "
                        + certs.size());
                }
                certs.addAll(getCertificates(request, CA_CERT, xsel));
                if (debug != null) {
                    debug.println("LDAPCertStore.engineGetCertificates() after "
                        + "getCertificates(subject,CA_CERT,xsel),certs.size(): "
                        + certs.size());
                }
            }
            if (basicConstraints < 0) {
                certs.addAll(getCertificates(request, USER_CERT, xsel));
                if (debug != null) {
                    debug.println("LDAPCertStore.engineGetCertificates() after "
                        + "getCertificates(subject,USER_CERT, xsel),certs.size(): "
                        + certs.size());
                }
            }
        } else {
            if (debug != null) {
                debug.println
                    ("LDAPCertStore.engineGetCertificates() subject is null");
            }
            if (basicConstraints == -2) {
                throw new CertStoreException("need subject to find EE certs");
            }
            if (issuer == null) {
                throw new CertStoreException("need subject or issuer to find certs");
            }
        }
        if (debug != null) {
            debug.println("LDAPCertStore.engineGetCertificates() about to "
                + "getMatchingCrossCerts...");
        }
        if ((issuer != null) && (basicConstraints > -2)) {
            LDAPRequest request = new LDAPRequest(issuer);
            request.addRequestedAttribute(CROSS_CERT);
            request.addRequestedAttribute(CA_CERT);
            request.addRequestedAttribute(ARL);
            if (prefetchCRLs) {
                request.addRequestedAttribute(CRL);
            }
            certs.addAll(getMatchingCrossCerts(request, null, xsel));
            if (debug != null) {
                debug.println("LDAPCertStore.engineGetCertificates() after "
                    + "getMatchingCrossCerts(issuer,null,xsel),certs.size(): "
                    + certs.size());
            }
            certs.addAll(getCertificates(request, CA_CERT, xsel));
            if (debug != null) {
                debug.println("LDAPCertStore.engineGetCertificates() after "
                    + "getCertificates(issuer,CA_CERT,xsel),certs.size(): "
                    + certs.size());
            }
        }
        if (debug != null) {
            debug.println("LDAPCertStore.engineGetCertificates() returning certs");
        }
        return certs;
    }
    private Collection<X509CRL> getCRLs(LDAPRequest request, String id,
            X509CRLSelector sel) throws CertStoreException {
        byte[][] encodedCRL;
        try {
            encodedCRL = request.getValues(id);
        } catch (NamingException namingEx) {
            throw new CertStoreException(namingEx);
        }
        int n = encodedCRL.length;
        if (n == 0) {
            return Collections.<X509CRL>emptySet();
        }
        List<X509CRL> crls = new ArrayList<X509CRL>(n);
        for (int i = 0; i < n; i++) {
            try {
                CRL crl = cf.generateCRL(new ByteArrayInputStream(encodedCRL[i]));
                if (sel.match(crl)) {
                    crls.add((X509CRL)crl);
                }
            } catch (CRLException e) {
                if (debug != null) {
                    debug.println("LDAPCertStore.getCRLs() encountered exception"
                        + " while parsing CRL, skipping the bad data: ");
                    HexDumpEncoder encoder = new HexDumpEncoder();
                    debug.println("[ " + encoder.encodeBuffer(encodedCRL[i]) + " ]");
                }
            }
        }
        return crls;
    }
    public synchronized Collection<X509CRL> engineGetCRLs(CRLSelector selector)
            throws CertStoreException {
        if (debug != null) {
            debug.println("LDAPCertStore.engineGetCRLs() selector: "
                + selector);
        }
        if (selector == null) {
            selector = new X509CRLSelector();
        }
        if (!(selector instanceof X509CRLSelector)) {
            throw new CertStoreException("need X509CRLSelector to find CRLs");
        }
        X509CRLSelector xsel = (X509CRLSelector) selector;
        HashSet<X509CRL> crls = new HashSet<X509CRL>();
        Collection<Object> issuerNames;
        X509Certificate certChecking = xsel.getCertificateChecking();
        if (certChecking != null) {
            issuerNames = new HashSet<Object>();
            X500Principal issuer = certChecking.getIssuerX500Principal();
            issuerNames.add(issuer.getName(X500Principal.RFC2253));
        } else {
            issuerNames = xsel.getIssuerNames();
            if (issuerNames == null) {
                throw new CertStoreException("need issuerNames or certChecking to "
                    + "find CRLs");
            }
        }
        for (Object nameObject : issuerNames) {
            String issuerName;
            if (nameObject instanceof byte[]) {
                try {
                    X500Principal issuer = new X500Principal((byte[])nameObject);
                    issuerName = issuer.getName(X500Principal.RFC2253);
                } catch (IllegalArgumentException e) {
                    continue;
                }
            } else {
                issuerName = (String)nameObject;
            }
            Collection<X509CRL> entryCRLs = Collections.<X509CRL>emptySet();
            if (certChecking == null || certChecking.getBasicConstraints() != -1) {
                LDAPRequest request = new LDAPRequest(issuerName);
                request.addRequestedAttribute(CROSS_CERT);
                request.addRequestedAttribute(CA_CERT);
                request.addRequestedAttribute(ARL);
                if (prefetchCRLs) {
                    request.addRequestedAttribute(CRL);
                }
                try {
                    entryCRLs = getCRLs(request, ARL, xsel);
                    if (entryCRLs.isEmpty()) {
                        prefetchCRLs = true;
                    } else {
                        crls.addAll(entryCRLs);
                    }
                } catch (CertStoreException e) {
                    if (debug != null) {
                        debug.println("LDAPCertStore.engineGetCRLs non-fatal error "
                            + "retrieving ARLs:" + e);
                        e.printStackTrace();
                    }
                }
            }
            if (entryCRLs.isEmpty() || certChecking == null) {
                LDAPRequest request = new LDAPRequest(issuerName);
                request.addRequestedAttribute(CRL);
                entryCRLs = getCRLs(request, CRL, xsel);
                crls.addAll(entryCRLs);
            }
        }
        return crls;
    }
    static LDAPCertStoreParameters getParameters(URI uri) {
        String host = uri.getHost();
        if (host == null) {
            return new SunLDAPCertStoreParameters();
        } else {
            int port = uri.getPort();
            return (port == -1
                    ? new SunLDAPCertStoreParameters(host)
                    : new SunLDAPCertStoreParameters(host, port));
        }
    }
    private static class SunLDAPCertStoreParameters
        extends LDAPCertStoreParameters {
        private volatile int hashCode = 0;
        SunLDAPCertStoreParameters(String serverName, int port) {
            super(serverName, port);
        }
        SunLDAPCertStoreParameters(String serverName) {
            super(serverName);
        }
        SunLDAPCertStoreParameters() {
            super();
        }
        public boolean equals(Object obj) {
            if (!(obj instanceof LDAPCertStoreParameters)) {
                return false;
            }
            LDAPCertStoreParameters params = (LDAPCertStoreParameters) obj;
            return (getPort() == params.getPort() &&
                    getServerName().equalsIgnoreCase(params.getServerName()));
        }
        public int hashCode() {
            if (hashCode == 0) {
                int result = 17;
                result = 37*result + getPort();
                result = 37*result + getServerName().toLowerCase().hashCode();
                hashCode = result;
            }
            return hashCode;
        }
    }
    static class LDAPCertSelector extends X509CertSelector {
        private X500Principal certSubject;
        private X509CertSelector selector;
        private X500Principal subject;
        LDAPCertSelector(X509CertSelector selector, X500Principal certSubject,
            String ldapDN) throws IOException {
            this.selector = selector == null ? new X509CertSelector() : selector;
            this.certSubject = certSubject;
            this.subject = new X500Name(ldapDN).asX500Principal();
        }
        public X509Certificate getCertificate() {
            return selector.getCertificate();
        }
        public BigInteger getSerialNumber() {
            return selector.getSerialNumber();
        }
        public X500Principal getIssuer() {
            return selector.getIssuer();
        }
        public String getIssuerAsString() {
            return selector.getIssuerAsString();
        }
        public byte[] getIssuerAsBytes() throws IOException {
            return selector.getIssuerAsBytes();
        }
        public X500Principal getSubject() {
            return subject;
        }
        public String getSubjectAsString() {
            return subject.getName();
        }
        public byte[] getSubjectAsBytes() throws IOException {
            return subject.getEncoded();
        }
        public byte[] getSubjectKeyIdentifier() {
            return selector.getSubjectKeyIdentifier();
        }
        public byte[] getAuthorityKeyIdentifier() {
            return selector.getAuthorityKeyIdentifier();
        }
        public Date getCertificateValid() {
            return selector.getCertificateValid();
        }
        public Date getPrivateKeyValid() {
            return selector.getPrivateKeyValid();
        }
        public String getSubjectPublicKeyAlgID() {
            return selector.getSubjectPublicKeyAlgID();
        }
        public PublicKey getSubjectPublicKey() {
            return selector.getSubjectPublicKey();
        }
        public boolean[] getKeyUsage() {
            return selector.getKeyUsage();
        }
        public Set<String> getExtendedKeyUsage() {
            return selector.getExtendedKeyUsage();
        }
        public boolean getMatchAllSubjectAltNames() {
            return selector.getMatchAllSubjectAltNames();
        }
        public Collection<List<?>> getSubjectAlternativeNames() {
            return selector.getSubjectAlternativeNames();
        }
        public byte[] getNameConstraints() {
            return selector.getNameConstraints();
        }
        public int getBasicConstraints() {
            return selector.getBasicConstraints();
        }
        public Set<String> getPolicy() {
            return selector.getPolicy();
        }
        public Collection<List<?>> getPathToNames() {
            return selector.getPathToNames();
        }
        public boolean match(Certificate cert) {
            selector.setSubject(certSubject);
            boolean match = selector.match(cert);
            selector.setSubject(subject);
            return match;
        }
    }
    static class LDAPCRLSelector extends X509CRLSelector {
        private X509CRLSelector selector;
        private Collection<X500Principal> certIssuers;
        private Collection<X500Principal> issuers;
        private HashSet<Object> issuerNames;
        LDAPCRLSelector(X509CRLSelector selector,
            Collection<X500Principal> certIssuers, String ldapDN)
            throws IOException {
            this.selector = selector == null ? new X509CRLSelector() : selector;
            this.certIssuers = certIssuers;
            issuerNames = new HashSet<Object>();
            issuerNames.add(ldapDN);
            issuers = new HashSet<X500Principal>();
            issuers.add(new X500Name(ldapDN).asX500Principal());
        }
        public Collection<X500Principal> getIssuers() {
            return Collections.unmodifiableCollection(issuers);
        }
        public Collection<Object> getIssuerNames() {
            return Collections.unmodifiableCollection(issuerNames);
        }
        public BigInteger getMinCRL() {
            return selector.getMinCRL();
        }
        public BigInteger getMaxCRL() {
            return selector.getMaxCRL();
        }
        public Date getDateAndTime() {
            return selector.getDateAndTime();
        }
        public X509Certificate getCertificateChecking() {
            return selector.getCertificateChecking();
        }
        public boolean match(CRL crl) {
            selector.setIssuers(certIssuers);
            boolean match = selector.match(crl);
            selector.setIssuers(issuers);
            return match;
        }
    }
}
