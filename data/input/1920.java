final class JceSecurity {
    static final SecureRandom RANDOM = new SecureRandom();
    private static CryptoPermissions defaultPolicy = null;
    private static CryptoPermissions exemptPolicy = null;
    private final static Map verificationResults = new IdentityHashMap();
    private final static Map verifyingProviders = new IdentityHashMap();
    private static boolean isRestricted = true;
    private JceSecurity() {
    }
    static {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    setupJurisdictionPolicies();
                    return null;
                }
            });
            isRestricted = defaultPolicy.implies(
                CryptoAllPermission.INSTANCE) ? false : true;
        } catch (Exception e) {
            SecurityException se =
                new SecurityException(
                    "Can not initialize cryptographic mechanism");
            se.initCause(e);
            throw se;
        }
    }
    static Instance getInstance(String type, Class clazz, String algorithm,
            String provider) throws NoSuchAlgorithmException,
            NoSuchProviderException {
        Service s = GetInstance.getService(type, algorithm, provider);
        Exception ve = getVerificationResult(s.getProvider());
        if (ve != null) {
            String msg = "JCE cannot authenticate the provider " + provider;
            throw (NoSuchProviderException)
                                new NoSuchProviderException(msg).initCause(ve);
        }
        return GetInstance.getInstance(s, clazz);
    }
    static Instance getInstance(String type, Class clazz, String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        Service s = GetInstance.getService(type, algorithm, provider);
        Exception ve = JceSecurity.getVerificationResult(provider);
        if (ve != null) {
            String msg = "JCE cannot authenticate the provider "
                + provider.getName();
            throw new SecurityException(msg, ve);
        }
        return GetInstance.getInstance(s, clazz);
    }
    static Instance getInstance(String type, Class clazz, String algorithm)
            throws NoSuchAlgorithmException {
        List services = GetInstance.getServices(type, algorithm);
        NoSuchAlgorithmException failure = null;
        for (Iterator t = services.iterator(); t.hasNext(); ) {
            Service s = (Service)t.next();
            if (canUseProvider(s.getProvider()) == false) {
                continue;
            }
            try {
                Instance instance = GetInstance.getInstance(s, clazz);
                return instance;
            } catch (NoSuchAlgorithmException e) {
                failure = e;
            }
        }
        throw new NoSuchAlgorithmException("Algorithm " + algorithm
                + " not available", failure);
    }
    static CryptoPermissions verifyExemptJar(URL codeBase) throws Exception {
        JarVerifier jv = new JarVerifier(codeBase, true);
        jv.verify();
        return jv.getPermissions();
    }
    static void verifyProviderJar(URL codeBase) throws Exception {
        JarVerifier jv = new JarVerifier(codeBase, false);
        jv.verify();
    }
    private final static Object PROVIDER_VERIFIED = Boolean.TRUE;
    static synchronized Exception getVerificationResult(Provider p) {
        Object o = verificationResults.get(p);
        if (o == PROVIDER_VERIFIED) {
            return null;
        } else if (o != null) {
            return (Exception)o;
        }
        if (verifyingProviders.get(p) != null) {
            return new NoSuchProviderException("Recursion during verification");
        }
        try {
            verifyingProviders.put(p, Boolean.FALSE);
            URL providerURL = getCodeBase(p.getClass());
            verifyProviderJar(providerURL);
            verificationResults.put(p, PROVIDER_VERIFIED);
            return null;
        } catch (Exception e) {
            verificationResults.put(p, e);
            return e;
        } finally {
            verifyingProviders.remove(p);
        }
    }
    static boolean canUseProvider(Provider p) {
        return getVerificationResult(p) == null;
    }
    private static final URL NULL_URL;
    static {
        try {
            NULL_URL = new URL("http:
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static final Map codeBaseCacheRef = new WeakHashMap();
    static URL getCodeBase(final Class clazz) {
        URL url = (URL)codeBaseCacheRef.get(clazz);
        if (url == null) {
            url = (URL)AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    ProtectionDomain pd = clazz.getProtectionDomain();
                    if (pd != null) {
                        CodeSource cs = pd.getCodeSource();
                        if (cs != null) {
                            return cs.getLocation();
                        }
                    }
                    return NULL_URL;
                }
            });
            codeBaseCacheRef.put(clazz, url);
        }
        return (url == NULL_URL) ? null : url;
    }
    private static void setupJurisdictionPolicies() throws Exception {
        String javaHomeDir = System.getProperty("java.home");
        String sep = File.separator;
        String pathToPolicyJar = javaHomeDir + sep + "lib" + sep +
            "security" + sep;
        File exportJar = new File(pathToPolicyJar, "US_export_policy.jar");
        File importJar = new File(pathToPolicyJar, "local_policy.jar");
        URL jceCipherURL = ClassLoader.getSystemResource
                ("javax/crypto/Cipher.class");
        if ((jceCipherURL == null) ||
                !exportJar.exists() || !importJar.exists()) {
            throw new SecurityException
                                ("Cannot locate policy or framework files!");
        }
        CryptoPermissions defaultExport = new CryptoPermissions();
        CryptoPermissions exemptExport = new CryptoPermissions();
        loadPolicies(exportJar, defaultExport, exemptExport);
        CryptoPermissions defaultImport = new CryptoPermissions();
        CryptoPermissions exemptImport = new CryptoPermissions();
        loadPolicies(importJar, defaultImport, exemptImport);
        if (defaultExport.isEmpty() || defaultImport.isEmpty()) {
            throw new SecurityException("Missing mandatory jurisdiction " +
                                        "policy files");
        }
        defaultPolicy = defaultExport.getMinimum(defaultImport);
        if (exemptExport.isEmpty())  {
            exemptPolicy = exemptImport.isEmpty() ? null : exemptImport;
        } else {
            exemptPolicy = exemptExport.getMinimum(exemptImport);
        }
    }
    private static void loadPolicies(File jarPathName,
                                     CryptoPermissions defaultPolicy,
                                     CryptoPermissions exemptPolicy)
        throws Exception {
        JarFile jf = new JarFile(jarPathName);
        Enumeration entries = jf.entries();
        while (entries.hasMoreElements()) {
            JarEntry je = (JarEntry)entries.nextElement();
            InputStream is = null;
            try {
                if (je.getName().startsWith("default_")) {
                    is = jf.getInputStream(je);
                    defaultPolicy.load(is);
                } else if (je.getName().startsWith("exempt_")) {
                    is = jf.getInputStream(je);
                    exemptPolicy.load(is);
                } else {
                    continue;
                }
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            JarVerifier.verifyPolicySigned(je.getCertificates());
        }
        jf.close();
        jf = null;
    }
    static CryptoPermissions getDefaultPolicy() {
        return defaultPolicy;
    }
    static CryptoPermissions getExemptPolicy() {
        return exemptPolicy;
    }
    static boolean isRestricted() {
        return isRestricted;
    }
}
