class URICertStore extends CertStoreSpi {
    private static final Debug debug = Debug.getInstance("certpath");
    private final static int CHECK_INTERVAL = 30 * 1000;
    private final static int CACHE_SIZE = 185;
    private final CertificateFactory factory;
    private Collection<X509Certificate> certs =
        Collections.<X509Certificate>emptySet();
    private X509CRL crl;
    private long lastChecked;
    private long lastModified;
    private URI uri;
    private boolean ldap = false;
    private CertStore ldapCertStore;
    private String ldapPath;
    private static class LDAP {
        private static final String CERT_STORE_HELPER =
            "sun.security.provider.certpath.ldap.LDAPCertStoreHelper";
        private static final CertStoreHelper helper =
            AccessController.doPrivileged(
                new PrivilegedAction<CertStoreHelper>() {
                    public CertStoreHelper run() {
                        try {
                            Class<?> c = Class.forName(CERT_STORE_HELPER, true, null);
                            return (CertStoreHelper)c.newInstance();
                        } catch (ClassNotFoundException cnf) {
                            return null;
                        } catch (InstantiationException e) {
                            throw new AssertionError(e);
                        } catch (IllegalAccessException e) {
                            throw new AssertionError(e);
                        }
                    }});
        static CertStoreHelper helper() {
            return helper;
        }
    }
    URICertStore(CertStoreParameters params)
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        super(params);
        if (!(params instanceof URICertStoreParameters)) {
            throw new InvalidAlgorithmParameterException
                ("params must be instanceof URICertStoreParameters");
        }
        this.uri = ((URICertStoreParameters) params).uri;
        if (uri.getScheme().toLowerCase(Locale.ENGLISH).equals("ldap")) {
            if (LDAP.helper() == null)
                throw new NoSuchAlgorithmException("LDAP not present");
            ldap = true;
            ldapCertStore = LDAP.helper().getCertStore(uri);
            ldapPath = uri.getPath();
            if (ldapPath.charAt(0) == '/') {
                ldapPath = ldapPath.substring(1);
            }
        }
        try {
            factory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new RuntimeException();
        }
    }
    private static final Cache certStoreCache =
        Cache.newSoftMemoryCache(CACHE_SIZE);
    static synchronized CertStore getInstance(URICertStoreParameters params)
        throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        if (debug != null) {
            debug.println("CertStore URI:" + params.uri);
        }
        CertStore ucs = (CertStore) certStoreCache.get(params);
        if (ucs == null) {
            ucs = new UCS(new URICertStore(params), null, "URI", params);
            certStoreCache.put(params, ucs);
        } else {
            if (debug != null) {
                debug.println("URICertStore.getInstance: cache hit");
            }
        }
        return ucs;
    }
    static CertStore getInstance(AccessDescription ad) {
        if (!ad.getAccessMethod().equals(AccessDescription.Ad_CAISSUERS_Id)) {
            return null;
        }
        GeneralNameInterface gn = ad.getAccessLocation().getName();
        if (!(gn instanceof URIName)) {
            return null;
        }
        URI uri = ((URIName) gn).getURI();
        try {
            return URICertStore.getInstance
                (new URICertStore.URICertStoreParameters(uri));
        } catch (Exception ex) {
            if (debug != null) {
                debug.println("exception creating CertStore: " + ex);
                ex.printStackTrace();
            }
            return null;
        }
    }
    public synchronized Collection<X509Certificate> engineGetCertificates
        (CertSelector selector) throws CertStoreException {
        if (ldap) {
            X509CertSelector xsel = (X509CertSelector) selector;
            try {
                xsel = LDAP.helper().wrap(xsel, xsel.getSubject(), ldapPath);
            } catch (IOException ioe) {
                throw new CertStoreException(ioe);
            }
            return (Collection<X509Certificate>)
                ldapCertStore.getCertificates(xsel);
        }
        long time = System.currentTimeMillis();
        if (time - lastChecked < CHECK_INTERVAL) {
            if (debug != null) {
                debug.println("Returning certificates from cache");
            }
            return getMatchingCerts(certs, selector);
        }
        lastChecked = time;
        InputStream in = null;
        try {
            URLConnection connection = uri.toURL().openConnection();
            if (lastModified != 0) {
                connection.setIfModifiedSince(lastModified);
            }
            in = connection.getInputStream();
            long oldLastModified = lastModified;
            lastModified = connection.getLastModified();
            if (oldLastModified != 0) {
                if (oldLastModified == lastModified) {
                    if (debug != null) {
                        debug.println("Not modified, using cached copy");
                    }
                    return getMatchingCerts(certs, selector);
                } else if (connection instanceof HttpURLConnection) {
                    HttpURLConnection hconn = (HttpURLConnection) connection;
                    if (hconn.getResponseCode()
                                == HttpURLConnection.HTTP_NOT_MODIFIED) {
                        if (debug != null) {
                            debug.println("Not modified, using cached copy");
                        }
                        return getMatchingCerts(certs, selector);
                    }
                }
            }
            if (debug != null) {
                debug.println("Downloading new certificates...");
            }
            certs = (Collection<X509Certificate>)
                factory.generateCertificates(in);
            return getMatchingCerts(certs, selector);
        } catch (IOException e) {
            if (debug != null) {
                debug.println("Exception fetching certificates:");
                e.printStackTrace();
            }
        } catch (CertificateException e) {
            if (debug != null) {
                debug.println("Exception fetching certificates:");
                e.printStackTrace();
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        lastModified = 0;
        certs = Collections.<X509Certificate>emptySet();
        return certs;
    }
    private static Collection<X509Certificate> getMatchingCerts
        (Collection<X509Certificate> certs, CertSelector selector) {
        if (selector == null) {
            return certs;
        }
        List<X509Certificate> matchedCerts =
            new ArrayList<X509Certificate>(certs.size());
        for (X509Certificate cert : certs) {
            if (selector.match(cert)) {
                matchedCerts.add(cert);
            }
        }
        return matchedCerts;
    }
    public synchronized Collection<X509CRL> engineGetCRLs(CRLSelector selector)
        throws CertStoreException {
        if (ldap) {
            X509CRLSelector xsel = (X509CRLSelector) selector;
            try {
                xsel = LDAP.helper().wrap(xsel, null, ldapPath);
            } catch (IOException ioe) {
                throw new CertStoreException(ioe);
            }
            return (Collection<X509CRL>) ldapCertStore.getCRLs(xsel);
        }
        long time = System.currentTimeMillis();
        if (time - lastChecked < CHECK_INTERVAL) {
            if (debug != null) {
                debug.println("Returning CRL from cache");
            }
            return getMatchingCRLs(crl, selector);
        }
        lastChecked = time;
        InputStream in = null;
        try {
            URLConnection connection = uri.toURL().openConnection();
            if (lastModified != 0) {
                connection.setIfModifiedSince(lastModified);
            }
            in = connection.getInputStream();
            long oldLastModified = lastModified;
            lastModified = connection.getLastModified();
            if (oldLastModified != 0) {
                if (oldLastModified == lastModified) {
                    if (debug != null) {
                        debug.println("Not modified, using cached copy");
                    }
                    return getMatchingCRLs(crl, selector);
                } else if (connection instanceof HttpURLConnection) {
                    HttpURLConnection hconn = (HttpURLConnection) connection;
                    if (hconn.getResponseCode()
                                == HttpURLConnection.HTTP_NOT_MODIFIED) {
                        if (debug != null) {
                            debug.println("Not modified, using cached copy");
                        }
                        return getMatchingCRLs(crl, selector);
                    }
                }
            }
            if (debug != null) {
                debug.println("Downloading new CRL...");
            }
            crl = (X509CRL) factory.generateCRL(in);
            return getMatchingCRLs(crl, selector);
        } catch (IOException e) {
            if (debug != null) {
                debug.println("Exception fetching CRL:");
                e.printStackTrace();
            }
        } catch (CRLException e) {
            if (debug != null) {
                debug.println("Exception fetching CRL:");
                e.printStackTrace();
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        lastModified = 0;
        crl = null;
        return Collections.<X509CRL>emptyList();
    }
    private static Collection<X509CRL> getMatchingCRLs
        (X509CRL crl, CRLSelector selector) {
        if (selector == null || (crl != null && selector.match(crl))) {
            return Collections.<X509CRL>singletonList(crl);
        } else {
            return Collections.<X509CRL>emptyList();
        }
    }
    static class URICertStoreParameters implements CertStoreParameters {
        private final URI uri;
        private volatile int hashCode = 0;
        URICertStoreParameters(URI uri) {
            this.uri = uri;
        }
        public boolean equals(Object obj) {
            if (!(obj instanceof URICertStoreParameters)) {
                return false;
            }
            URICertStoreParameters params = (URICertStoreParameters) obj;
            return uri.equals(params.uri);
        }
        public int hashCode() {
            if (hashCode == 0) {
                int result = 17;
                result = 37*result + uri.hashCode();
                hashCode = result;
            }
            return hashCode;
        }
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                throw new InternalError(e.toString());
            }
        }
    }
    private static class UCS extends CertStore {
        protected UCS(CertStoreSpi spi, Provider p, String type,
            CertStoreParameters params) {
            super(spi, p, type, params);
        }
    }
}
