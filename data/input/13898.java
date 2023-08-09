final class SSLSecurity {
    private SSLSecurity() {
    }
    private static Service getService(String type, String alg) {
        ProviderList list = Providers.getProviderList();
        for (Provider p : list.providers()) {
            Service s = p.getService(type, alg);
            if (s != null) {
                return s;
            }
        }
        return null;
    }
    private static Object[] getImpl1(String algName, String engineType,
            Service service) throws NoSuchAlgorithmException
    {
        Provider provider = service.getProvider();
        String className = service.getClassName();
        Class implClass;
        try {
            ClassLoader cl = provider.getClass().getClassLoader();
            if (cl == null) {
                implClass = Class.forName(className);
            } else {
                implClass = cl.loadClass(className);
            }
        } catch (ClassNotFoundException e) {
            throw new NoSuchAlgorithmException("Class " + className +
                                                " configured for " +
                                                engineType +
                                                " not found: " +
                                                e.getMessage());
        } catch (SecurityException e) {
            throw new NoSuchAlgorithmException("Class " + className +
                                                " configured for " +
                                                engineType +
                                                " cannot be accessed: " +
                                                e.getMessage());
        }
        try {   
            Class typeClassJavax;
            Class typeClassCom;
            Object obj = null;
            if (((typeClassJavax = Class.forName("javax.net.ssl." +
                    engineType + "Spi")) != null) &&
                    (checkSuperclass(implClass, typeClassJavax))) {
                if (engineType.equals("SSLContext")) {
                    obj = new SSLContextSpiWrapper(algName, provider);
                } else if (engineType.equals("TrustManagerFactory")) {
                    obj = new TrustManagerFactorySpiWrapper(algName, provider);
                } else if (engineType.equals("KeyManagerFactory")) {
                    obj = new KeyManagerFactorySpiWrapper(algName, provider);
                } else {
                    throw new IllegalStateException(
                        "Class " + implClass.getName() +
                        " unknown engineType wrapper:" + engineType);
                }
            } else if (((typeClassCom = Class.forName("com.sun.net.ssl." +
                        engineType + "Spi")) != null) &&
                        (checkSuperclass(implClass, typeClassCom))) {
                obj = service.newInstance(null);
            }
            if (obj != null) {
                return new Object[] { obj, provider };
            } else {
                throw new NoSuchAlgorithmException(
                    "Couldn't locate correct object or wrapper: " +
                    engineType + " " + algName);
            }
        } catch (ClassNotFoundException e) {
            IllegalStateException exc = new IllegalStateException(
                "Engine Class Not Found for " + engineType);
            exc.initCause(e);
            throw exc;
        }
    }
    static Object[] getImpl(String algName, String engineType, String provName)
        throws NoSuchAlgorithmException, NoSuchProviderException
    {
        Service service;
        if (provName != null) {
            ProviderList list = Providers.getProviderList();
            Provider prov = list.getProvider(provName);
            if (prov == null) {
                throw new NoSuchProviderException("No such provider: " +
                                                  provName);
            }
            service = prov.getService(engineType, algName);
        } else {
            service = getService(engineType, algName);
        }
        if (service == null) {
            throw new NoSuchAlgorithmException("Algorithm " + algName
                                               + " not available");
        }
        return getImpl1(algName, engineType, service);
    }
    static Object[] getImpl(String algName, String engineType, Provider prov)
        throws NoSuchAlgorithmException
    {
        Service service = prov.getService(engineType, algName);
        if (service == null) {
            throw new NoSuchAlgorithmException("No such algorithm: " +
                                               algName);
        }
        return getImpl1(algName, engineType, service);
    }
    private static boolean checkSuperclass(Class subclass, Class superclass) {
        if ((subclass == null) || (superclass == null))
                return false;
        while (!subclass.equals(superclass)) {
            subclass = subclass.getSuperclass();
            if (subclass == null) {
                return false;
            }
        }
        return true;
    }
    static Object[] truncateArray(Object[] oldArray, Object[] newArray) {
        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = oldArray[i];
        }
        return newArray;
    }
}
final class SSLContextSpiWrapper extends SSLContextSpi {
    private javax.net.ssl.SSLContext theSSLContext;
    SSLContextSpiWrapper(String algName, Provider prov) throws
            NoSuchAlgorithmException {
        theSSLContext = javax.net.ssl.SSLContext.getInstance(algName, prov);
    }
    protected void engineInit(KeyManager[] kma, TrustManager[] tma,
            SecureRandom sr) throws KeyManagementException {
        int dst;
        int src;
        javax.net.ssl.KeyManager[] kmaw;
        javax.net.ssl.TrustManager[] tmaw;
        if (kma != null) {
            kmaw = new javax.net.ssl.KeyManager[kma.length];
            for (src = 0, dst = 0; src < kma.length; ) {
                if (!(kma[src] instanceof javax.net.ssl.KeyManager)) {
                    if (kma[src] instanceof X509KeyManager) {
                        kmaw[dst] = (javax.net.ssl.KeyManager)
                            new X509KeyManagerJavaxWrapper(
                            (X509KeyManager)kma[src]);
                        dst++;
                    }
                } else {
                    kmaw[dst] = (javax.net.ssl.KeyManager)kma[src];
                    dst++;
                }
                src++;
            }
            if (dst != src) {
                    kmaw = (javax.net.ssl.KeyManager [])
                        SSLSecurity.truncateArray(kmaw,
                            new javax.net.ssl.KeyManager [dst]);
            }
        } else {
            kmaw = null;
        }
        if (tma != null) {
            tmaw = new javax.net.ssl.TrustManager[tma.length];
            for (src = 0, dst = 0; src < tma.length; ) {
                if (!(tma[src] instanceof javax.net.ssl.TrustManager)) {
                    if (tma[src] instanceof X509TrustManager) {
                        tmaw[dst] = (javax.net.ssl.TrustManager)
                            new X509TrustManagerJavaxWrapper(
                            (X509TrustManager)tma[src]);
                        dst++;
                    }
                } else {
                    tmaw[dst] = (javax.net.ssl.TrustManager)tma[src];
                    dst++;
                }
                src++;
            }
            if (dst != src) {
                tmaw = (javax.net.ssl.TrustManager [])
                    SSLSecurity.truncateArray(tmaw,
                        new javax.net.ssl.TrustManager [dst]);
            }
        } else {
            tmaw = null;
        }
        theSSLContext.init(kmaw, tmaw, sr);
    }
    protected javax.net.ssl.SSLSocketFactory
            engineGetSocketFactory() {
        return theSSLContext.getSocketFactory();
    }
    protected javax.net.ssl.SSLServerSocketFactory
            engineGetServerSocketFactory() {
        return theSSLContext.getServerSocketFactory();
    }
}
final class TrustManagerFactorySpiWrapper extends TrustManagerFactorySpi {
    private javax.net.ssl.TrustManagerFactory theTrustManagerFactory;
    TrustManagerFactorySpiWrapper(String algName, Provider prov) throws
            NoSuchAlgorithmException {
        theTrustManagerFactory =
            javax.net.ssl.TrustManagerFactory.getInstance(algName, prov);
    }
    protected void engineInit(KeyStore ks) throws KeyStoreException {
        theTrustManagerFactory.init(ks);
    }
    protected TrustManager[] engineGetTrustManagers() {
        int dst;
        int src;
        javax.net.ssl.TrustManager[] tma =
            theTrustManagerFactory.getTrustManagers();
        TrustManager[] tmaw = new TrustManager[tma.length];
        for (src = 0, dst = 0; src < tma.length; ) {
            if (!(tma[src] instanceof com.sun.net.ssl.TrustManager)) {
                if (tma[src] instanceof javax.net.ssl.X509TrustManager) {
                    tmaw[dst] = (TrustManager)
                        new X509TrustManagerComSunWrapper(
                        (javax.net.ssl.X509TrustManager)tma[src]);
                    dst++;
                }
            } else {
                tmaw[dst] = (TrustManager)tma[src];
                dst++;
            }
            src++;
        }
        if (dst != src) {
            tmaw = (TrustManager [])
                SSLSecurity.truncateArray(tmaw, new TrustManager [dst]);
        }
        return tmaw;
    }
}
final class KeyManagerFactorySpiWrapper extends KeyManagerFactorySpi {
    private javax.net.ssl.KeyManagerFactory theKeyManagerFactory;
    KeyManagerFactorySpiWrapper(String algName, Provider prov) throws
            NoSuchAlgorithmException {
        theKeyManagerFactory =
            javax.net.ssl.KeyManagerFactory.getInstance(algName, prov);
    }
    protected void engineInit(KeyStore ks, char[] password)
            throws KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableKeyException {
        theKeyManagerFactory.init(ks, password);
    }
    protected KeyManager[] engineGetKeyManagers() {
        int dst;
        int src;
        javax.net.ssl.KeyManager[] kma =
            theKeyManagerFactory.getKeyManagers();
        KeyManager[] kmaw = new KeyManager[kma.length];
        for (src = 0, dst = 0; src < kma.length; ) {
            if (!(kma[src] instanceof com.sun.net.ssl.KeyManager)) {
                if (kma[src] instanceof javax.net.ssl.X509KeyManager) {
                    kmaw[dst] = (KeyManager)
                        new X509KeyManagerComSunWrapper(
                        (javax.net.ssl.X509KeyManager)kma[src]);
                    dst++;
                }
            } else {
                kmaw[dst] = (KeyManager)kma[src];
                dst++;
            }
            src++;
        }
        if (dst != src) {
            kmaw = (KeyManager [])
                SSLSecurity.truncateArray(kmaw, new KeyManager [dst]);
        }
        return kmaw;
    }
}
final class X509KeyManagerJavaxWrapper implements
        javax.net.ssl.X509KeyManager {
    private X509KeyManager theX509KeyManager;
    X509KeyManagerJavaxWrapper(X509KeyManager obj) {
        theX509KeyManager = obj;
    }
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return theX509KeyManager.getClientAliases(keyType, issuers);
    }
    public String chooseClientAlias(String[] keyTypes, Principal[] issuers,
            Socket socket) {
        String retval;
        if (keyTypes == null) {
            return null;
        }
        for (int i = 0; i < keyTypes.length; i++) {
            if ((retval = theX509KeyManager.chooseClientAlias(keyTypes[i],
                    issuers)) != null)
                return retval;
        }
        return null;
    }
    public String chooseEngineClientAlias(
            String[] keyTypes, Principal[] issuers,
            javax.net.ssl.SSLEngine engine) {
        String retval;
        if (keyTypes == null) {
            return null;
        }
        for (int i = 0; i < keyTypes.length; i++) {
            if ((retval = theX509KeyManager.chooseClientAlias(keyTypes[i],
                    issuers)) != null)
                return retval;
        }
        return null;
    }
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return theX509KeyManager.getServerAliases(keyType, issuers);
    }
    public String chooseServerAlias(String keyType, Principal[] issuers,
            Socket socket) {
        if (keyType == null) {
            return null;
        }
        return theX509KeyManager.chooseServerAlias(keyType, issuers);
    }
    public String chooseEngineServerAlias(
            String keyType, Principal[] issuers,
            javax.net.ssl.SSLEngine engine) {
        if (keyType == null) {
            return null;
        }
        return theX509KeyManager.chooseServerAlias(keyType, issuers);
    }
    public java.security.cert.X509Certificate[]
            getCertificateChain(String alias) {
        return theX509KeyManager.getCertificateChain(alias);
    }
    public PrivateKey getPrivateKey(String alias) {
        return theX509KeyManager.getPrivateKey(alias);
    }
}
final class X509TrustManagerJavaxWrapper implements
        javax.net.ssl.X509TrustManager {
    private X509TrustManager theX509TrustManager;
    X509TrustManagerJavaxWrapper(X509TrustManager obj) {
        theX509TrustManager = obj;
    }
    public void checkClientTrusted(
            java.security.cert.X509Certificate[] chain, String authType)
        throws java.security.cert.CertificateException {
        if (!theX509TrustManager.isClientTrusted(chain)) {
            throw new java.security.cert.CertificateException(
                "Untrusted Client Certificate Chain");
        }
    }
    public void checkServerTrusted(
            java.security.cert.X509Certificate[] chain, String authType)
        throws java.security.cert.CertificateException {
        if (!theX509TrustManager.isServerTrusted(chain)) {
            throw new java.security.cert.CertificateException(
                "Untrusted Server Certificate Chain");
        }
    }
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return theX509TrustManager.getAcceptedIssuers();
    }
}
final class X509KeyManagerComSunWrapper implements X509KeyManager {
    private javax.net.ssl.X509KeyManager theX509KeyManager;
    X509KeyManagerComSunWrapper(javax.net.ssl.X509KeyManager obj) {
        theX509KeyManager = obj;
    }
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return theX509KeyManager.getClientAliases(keyType, issuers);
    }
    public String chooseClientAlias(String keyType, Principal[] issuers) {
        String [] keyTypes = new String [] { keyType };
        return theX509KeyManager.chooseClientAlias(keyTypes, issuers, null);
    }
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return theX509KeyManager.getServerAliases(keyType, issuers);
    }
    public String chooseServerAlias(String keyType, Principal[] issuers) {
        return theX509KeyManager.chooseServerAlias(keyType, issuers, null);
    }
    public java.security.cert.X509Certificate[]
            getCertificateChain(String alias) {
        return theX509KeyManager.getCertificateChain(alias);
    }
    public PrivateKey getPrivateKey(String alias) {
        return theX509KeyManager.getPrivateKey(alias);
    }
}
final class X509TrustManagerComSunWrapper implements X509TrustManager {
    private javax.net.ssl.X509TrustManager theX509TrustManager;
    X509TrustManagerComSunWrapper(javax.net.ssl.X509TrustManager obj) {
        theX509TrustManager = obj;
    }
    public boolean isClientTrusted(
            java.security.cert.X509Certificate[] chain) {
        try {
            theX509TrustManager.checkClientTrusted(chain, "UNKNOWN");
            return true;
        } catch (java.security.cert.CertificateException e) {
            return false;
        }
    }
    public boolean isServerTrusted(
            java.security.cert.X509Certificate[] chain) {
        try {
            theX509TrustManager.checkServerTrusted(chain, "UNKNOWN");
            return true;
        } catch (java.security.cert.CertificateException e) {
            return false;
        }
    }
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return theX509TrustManager.getAcceptedIssuers();
    }
}
