abstract class TrustManagerFactoryImpl extends TrustManagerFactorySpi {
    private static final Debug debug = Debug.getInstance("ssl");
    private X509TrustManager trustManager = null;
    private boolean isInitialized = false;
    TrustManagerFactoryImpl() {
    }
    protected void engineInit(KeyStore ks) throws KeyStoreException {
        if (ks == null) {
            try {
                ks = getCacertsKeyStore("trustmanager");
            } catch (SecurityException se) {
                if (debug != null && Debug.isOn("trustmanager")) {
                    System.out.println(
                        "SunX509: skip default keystore: " + se);
                }
            } catch (Error err) {
                if (debug != null && Debug.isOn("trustmanager")) {
                    System.out.println(
                        "SunX509: skip default keystore: " + err);
                }
                throw err;
            } catch (RuntimeException re) {
                if (debug != null && Debug.isOn("trustmanager")) {
                    System.out.println(
                        "SunX509: skip default keystore: " + re);
                }
                throw re;
            } catch (Exception e) {
                if (debug != null && Debug.isOn("trustmanager")) {
                    System.out.println(
                        "SunX509: skip default keystore: " + e);
                }
                throw new KeyStoreException(
                    "problem accessing trust store" + e);
            }
        }
        trustManager = getInstance(ks);
        isInitialized = true;
    }
    abstract X509TrustManager getInstance(KeyStore ks) throws KeyStoreException;
    abstract X509TrustManager getInstance(ManagerFactoryParameters spec)
            throws InvalidAlgorithmParameterException;
    protected void engineInit(ManagerFactoryParameters spec) throws
            InvalidAlgorithmParameterException {
        trustManager = getInstance(spec);
        isInitialized = true;
    }
    protected TrustManager[] engineGetTrustManagers() {
        if (!isInitialized) {
            throw new IllegalStateException(
                        "TrustManagerFactoryImpl is not initialized");
        }
        return new TrustManager[] { trustManager };
    }
    private static FileInputStream getFileInputStream(final File file)
            throws Exception {
        return AccessController.doPrivileged(
                new PrivilegedExceptionAction<FileInputStream>() {
                    public FileInputStream run() throws Exception {
                        try {
                            if (file.exists()) {
                                return new FileInputStream(file);
                            } else {
                                return null;
                            }
                        } catch (FileNotFoundException e) {
                            return null;
                        }
                    }
                });
    }
    static KeyStore getCacertsKeyStore(String dbgname) throws Exception
    {
        String storeFileName = null;
        File storeFile = null;
        FileInputStream fis = null;
        String defaultTrustStoreType;
        String defaultTrustStoreProvider;
        final HashMap<String,String> props = new HashMap<>();
        final String sep = File.separator;
        KeyStore ks = null;
        AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
            public Void run() throws Exception {
                props.put("trustStore", System.getProperty(
                                "javax.net.ssl.trustStore"));
                props.put("javaHome", System.getProperty(
                                        "java.home"));
                props.put("trustStoreType", System.getProperty(
                                "javax.net.ssl.trustStoreType",
                                KeyStore.getDefaultType()));
                props.put("trustStoreProvider", System.getProperty(
                                "javax.net.ssl.trustStoreProvider", ""));
                props.put("trustStorePasswd", System.getProperty(
                                "javax.net.ssl.trustStorePassword", ""));
                return null;
            }
        });
        storeFileName = props.get("trustStore");
        if (!"NONE".equals(storeFileName)) {
            if (storeFileName != null) {
                storeFile = new File(storeFileName);
                fis = getFileInputStream(storeFile);
            } else {
                String javaHome = props.get("javaHome");
                storeFile = new File(javaHome + sep + "lib" + sep
                                                + "security" + sep +
                                                "jssecacerts");
                if ((fis = getFileInputStream(storeFile)) == null) {
                    storeFile = new File(javaHome + sep + "lib" + sep
                                                + "security" + sep +
                                                "cacerts");
                    fis = getFileInputStream(storeFile);
                }
            }
            if (fis != null) {
                storeFileName = storeFile.getPath();
            } else {
                storeFileName = "No File Available, using empty keystore.";
            }
        }
        defaultTrustStoreType = props.get("trustStoreType");
        defaultTrustStoreProvider = props.get("trustStoreProvider");
        if (debug != null && Debug.isOn(dbgname)) {
            System.out.println("trustStore is: " + storeFileName);
            System.out.println("trustStore type is : " +
                                defaultTrustStoreType);
            System.out.println("trustStore provider is : " +
                                defaultTrustStoreProvider);
        }
        if (defaultTrustStoreType.length() != 0) {
            if (debug != null && Debug.isOn(dbgname)) {
                System.out.println("init truststore");
            }
            if (defaultTrustStoreProvider.length() == 0) {
                ks = KeyStore.getInstance(defaultTrustStoreType);
            } else {
                ks = KeyStore.getInstance(defaultTrustStoreType,
                                        defaultTrustStoreProvider);
            }
            char[] passwd = null;
            String defaultTrustStorePassword = props.get("trustStorePasswd");
            if (defaultTrustStorePassword.length() != 0)
                passwd = defaultTrustStorePassword.toCharArray();
            ks.load(fis, passwd);
            if (passwd != null) {
                for (int i = 0; i < passwd.length; i++) {
                    passwd[i] = (char)0;
                }
            }
        }
        if (fis != null) {
            fis.close();
        }
        return ks;
    }
    public static final class SimpleFactory extends TrustManagerFactoryImpl {
        X509TrustManager getInstance(KeyStore ks) throws KeyStoreException {
            return new X509TrustManagerImpl(Validator.TYPE_SIMPLE, ks);
        }
        X509TrustManager getInstance(ManagerFactoryParameters spec)
                throws InvalidAlgorithmParameterException {
            throw new InvalidAlgorithmParameterException
                ("SunX509 TrustManagerFactory does not use "
                + "ManagerFactoryParameters");
        }
   }
    public static final class PKIXFactory extends TrustManagerFactoryImpl {
        X509TrustManager getInstance(KeyStore ks) throws KeyStoreException {
            return new X509TrustManagerImpl(Validator.TYPE_PKIX, ks);
        }
        X509TrustManager getInstance(ManagerFactoryParameters spec)
                throws InvalidAlgorithmParameterException {
            if (spec instanceof CertPathTrustManagerParameters == false) {
                throw new InvalidAlgorithmParameterException
                    ("Parameters must be CertPathTrustManagerParameters");
            }
            CertPathParameters params =
                ((CertPathTrustManagerParameters)spec).getParameters();
            if (params instanceof PKIXBuilderParameters == false) {
                throw new InvalidAlgorithmParameterException
                    ("Encapsulated parameters must be PKIXBuilderParameters");
            }
            PKIXBuilderParameters pkixParams = (PKIXBuilderParameters)params;
            return new X509TrustManagerImpl(Validator.TYPE_PKIX, pkixParams);
        }
    }
}
