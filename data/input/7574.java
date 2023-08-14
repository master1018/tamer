public final class Secmod {
    private final static boolean DEBUG = false;
    private final static Secmod INSTANCE;
    static {
        sun.security.pkcs11.wrapper.PKCS11.loadNative();
        INSTANCE = new Secmod();
    }
    private final static String NSS_LIB_NAME = "nss3";
    private final static String SOFTTOKEN_LIB_NAME = "softokn3";
    private final static String TRUST_LIB_NAME = "nssckbi";
    private long nssHandle;
    private boolean supported;
    private List<Module> modules;
    private String configDir;
    private String nssLibDir;
    private Secmod() {
    }
    public static Secmod getInstance() {
        return INSTANCE;
    }
    private boolean isLoaded() {
        if (nssHandle == 0) {
            nssHandle = nssGetLibraryHandle(System.mapLibraryName(NSS_LIB_NAME));
            if (nssHandle != 0) {
                fetchVersions();
            }
        }
        return (nssHandle != 0);
    }
    private void fetchVersions() {
        supported = nssVersionCheck(nssHandle, "3.7");
    }
    public synchronized boolean isInitialized() throws IOException {
        if (isLoaded() == false) {
            return false;
        }
        if (supported == false) {
            throw new IOException
                ("An incompatible version of NSS is already loaded, "
                + "3.7 or later required");
        }
        return true;
    }
    String getConfigDir() {
        return configDir;
    }
    String getLibDir() {
        return nssLibDir;
    }
    public void initialize(String configDir, String nssLibDir)
            throws IOException {
        initialize(DbMode.READ_WRITE, configDir, nssLibDir);
    }
    public synchronized void initialize(DbMode dbMode, String configDir, String nssLibDir)
            throws IOException {
        if (isInitialized()) {
            throw new IOException("NSS is already initialized");
        }
        if (dbMode == null) {
            throw new NullPointerException();
        }
        if ((dbMode != DbMode.NO_DB) && (configDir == null)) {
            throw new NullPointerException();
        }
        String platformLibName = System.mapLibraryName("nss3");
        String platformPath;
        if (nssLibDir == null) {
            platformPath = platformLibName;
        } else {
            File base = new File(nssLibDir);
            if (base.isDirectory() == false) {
                throw new IOException("nssLibDir must be a directory:" + nssLibDir);
            }
            File platformFile = new File(base, platformLibName);
            if (platformFile.isFile() == false) {
                throw new FileNotFoundException(platformFile.getPath());
            }
            platformPath = platformFile.getPath();
        }
        if (configDir != null) {
            File configBase = new File(configDir);
            if (configBase.isDirectory() == false ) {
                throw new IOException("configDir must be a directory: " + configDir);
            }
            File secmodFile = new File(configBase, "secmod.db");
            if (secmodFile.isFile() == false) {
                throw new FileNotFoundException(secmodFile.getPath());
            }
        }
        if (DEBUG) System.out.println("lib: " + platformPath);
        nssHandle = nssLoadLibrary(platformPath);
        if (DEBUG) System.out.println("handle: " + nssHandle);
        fetchVersions();
        if (supported == false) {
            throw new IOException
                ("The specified version of NSS is incompatible, "
                + "3.7 or later required");
        }
        if (DEBUG) System.out.println("dir: " + configDir);
        boolean initok = nssInit(dbMode.functionName, nssHandle, configDir);
        if (DEBUG) System.out.println("init: " + initok);
        if (initok == false) {
            throw new IOException("NSS initialization failed");
        }
        this.configDir = configDir;
        this.nssLibDir = nssLibDir;
    }
    public synchronized List<Module> getModules() {
        try {
            if (isInitialized() == false) {
                throw new IllegalStateException("NSS not initialized");
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        if (modules == null) {
            List<Module> modules = (List<Module>)nssGetModuleList(nssHandle,
                nssLibDir);
            this.modules = Collections.unmodifiableList(modules);
        }
        return modules;
    }
    private static byte[] getDigest(X509Certificate cert, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return md.digest(cert.getEncoded());
        } catch (GeneralSecurityException e) {
            throw new ProviderException(e);
        }
    }
    boolean isTrusted(X509Certificate cert, TrustType trustType) {
        Bytes bytes = new Bytes(getDigest(cert, "SHA-1"));
        TrustAttributes attr = getModuleTrust(ModuleType.KEYSTORE, bytes);
        if (attr == null) {
            attr = getModuleTrust(ModuleType.FIPS, bytes);
            if (attr == null) {
                attr = getModuleTrust(ModuleType.TRUSTANCHOR, bytes);
            }
        }
        return (attr == null) ? false : attr.isTrusted(trustType);
    }
    private TrustAttributes getModuleTrust(ModuleType type, Bytes bytes) {
        Module module = getModule(type);
        TrustAttributes t = (module == null) ? null : module.getTrust(bytes);
        return t;
    }
    public static enum ModuleType {
        CRYPTO,
        KEYSTORE,
        FIPS,
        TRUSTANCHOR,
        EXTERNAL,
    }
    public Module getModule(ModuleType type) {
        for (Module module : getModules()) {
            if (module.getType() == type) {
                return module;
            }
        }
        return null;
    }
    static final String TEMPLATE_EXTERNAL =
        "library = %s\n"
        + "name = \"%s\"\n"
        + "slotListIndex = %d\n";
    static final String TEMPLATE_TRUSTANCHOR =
        "library = %s\n"
        + "name = \"NSS Trust Anchors\"\n"
        + "slotListIndex = 0\n"
        + "enabledMechanisms = { KeyStore }\n"
        + "nssUseSecmodTrust = true\n";
    static final String TEMPLATE_CRYPTO =
        "library = %s\n"
        + "name = \"NSS SoftToken Crypto\"\n"
        + "slotListIndex = 0\n"
        + "disabledMechanisms = { KeyStore }\n";
    static final String TEMPLATE_KEYSTORE =
        "library = %s\n"
        + "name = \"NSS SoftToken KeyStore\"\n"
        + "slotListIndex = 1\n"
        + "nssUseSecmodTrust = true\n";
    static final String TEMPLATE_FIPS =
        "library = %s\n"
        + "name = \"NSS FIPS SoftToken\"\n"
        + "slotListIndex = 0\n"
        + "nssUseSecmodTrust = true\n";
    public static final class Module {
        final String libraryName;
        final String commonName;
        final int slot;
        final ModuleType type;
        private String config;
        private SunPKCS11 provider;
        private Map<Bytes,TrustAttributes> trust;
        Module(String libraryDir, String libraryName, String commonName,
                boolean fips, int slot) {
            ModuleType type;
            if ((libraryName == null) || (libraryName.length() == 0)) {
                libraryName = System.mapLibraryName(SOFTTOKEN_LIB_NAME);
                if (fips == false) {
                    type = (slot == 0) ? ModuleType.CRYPTO : ModuleType.KEYSTORE;
                } else {
                    type = ModuleType.FIPS;
                    if (slot != 0) {
                        throw new RuntimeException
                            ("Slot index should be 0 for FIPS slot");
                    }
                }
            } else {
                if (libraryName.endsWith(System.mapLibraryName(TRUST_LIB_NAME))
                        || commonName.equals("Builtin Roots Module")) {
                    type = ModuleType.TRUSTANCHOR;
                } else {
                    type = ModuleType.EXTERNAL;
                }
                if (fips) {
                    throw new RuntimeException("FIPS flag set for non-internal "
                        + "module: " + libraryName + ", " + commonName);
                }
            }
            this.libraryName = (new File(libraryDir, libraryName)).getPath();
            this.commonName = commonName;
            this.slot = slot;
            this.type = type;
            initConfiguration();
        }
        private void initConfiguration() {
            switch (type) {
            case EXTERNAL:
                config = String.format(TEMPLATE_EXTERNAL, libraryName,
                                            commonName + " " + slot, slot);
                break;
            case CRYPTO:
                config = String.format(TEMPLATE_CRYPTO, libraryName);
                break;
            case KEYSTORE:
                config = String.format(TEMPLATE_KEYSTORE, libraryName);
                break;
            case FIPS:
                config = String.format(TEMPLATE_FIPS, libraryName);
                break;
            case TRUSTANCHOR:
                config = String.format(TEMPLATE_TRUSTANCHOR, libraryName);
                break;
            default:
                throw new RuntimeException("Unknown module type: " + type);
            }
        }
        @Deprecated
        public synchronized String getConfiguration() {
            return config;
        }
        @Deprecated
        public synchronized void setConfiguration(String config) {
            if (provider != null) {
                throw new IllegalStateException("Provider instance already created");
            }
            this.config = config;
        }
        public String getLibraryName() {
            return libraryName;
        }
        public ModuleType getType() {
            return type;
        }
        @Deprecated
        public synchronized Provider getProvider() {
            if (provider == null) {
                provider = newProvider();
            }
            return provider;
        }
        synchronized boolean hasInitializedProvider() {
            return provider != null;
        }
        void setProvider(SunPKCS11 p) {
            if (provider != null) {
                throw new ProviderException("Secmod provider already initialized");
            }
            provider = p;
        }
        private SunPKCS11 newProvider() {
            try {
                InputStream in = new ByteArrayInputStream(config.getBytes("UTF8"));
                return new SunPKCS11(in);
            } catch (Exception e) {
                throw new ProviderException(e);
            }
        }
        synchronized void setTrust(Token token, X509Certificate cert) {
            Bytes bytes = new Bytes(getDigest(cert, "SHA-1"));
            TrustAttributes attr = getTrust(bytes);
            if (attr == null) {
                attr = new TrustAttributes(token, cert, bytes, CKT_NETSCAPE_TRUSTED_DELEGATOR);
                trust.put(bytes, attr);
            } else {
                if (attr.isTrusted(TrustType.ALL) == false) {
                    throw new ProviderException("Cannot change existing trust attributes");
                }
            }
        }
        TrustAttributes getTrust(Bytes hash) {
            if (trust == null) {
                synchronized (this) {
                    SunPKCS11 p = provider;
                    if (p == null) {
                        p = newProvider();
                    }
                    try {
                        trust = Secmod.getTrust(p);
                    } catch (PKCS11Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return trust.get(hash);
        }
        public String toString() {
            return
            commonName + " (" + type + ", " + libraryName + ", slot " + slot + ")";
        }
    }
    public static enum TrustType {
        ALL,
        CLIENT_AUTH,
        SERVER_AUTH,
        CODE_SIGNING,
        EMAIL_PROTECTION,
    }
    public static enum DbMode {
        READ_WRITE("NSS_InitReadWrite"),
        READ_ONLY ("NSS_Init"),
        NO_DB     ("NSS_NoDB_Init");
        final String functionName;
        DbMode(String functionName) {
            this.functionName = functionName;
        }
    }
    public static final class KeyStoreLoadParameter implements LoadStoreParameter {
        final TrustType trustType;
        final ProtectionParameter protection;
        public KeyStoreLoadParameter(TrustType trustType, char[] password) {
            this(trustType, new PasswordProtection(password));
        }
        public KeyStoreLoadParameter(TrustType trustType, ProtectionParameter prot) {
            if (trustType == null) {
                throw new NullPointerException("trustType must not be null");
            }
            this.trustType = trustType;
            this.protection = prot;
        }
        public ProtectionParameter getProtectionParameter() {
            return protection;
        }
        public TrustType getTrustType() {
            return trustType;
        }
    }
    static class TrustAttributes {
        final long handle;
        final long clientAuth, serverAuth, codeSigning, emailProtection;
        final byte[] shaHash;
        TrustAttributes(Token token, X509Certificate cert, Bytes bytes, long trustValue) {
            Session session = null;
            try {
                session = token.getOpSession();
                CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] {
                    new CK_ATTRIBUTE(CKA_TOKEN, true),
                    new CK_ATTRIBUTE(CKA_CLASS, CKO_NETSCAPE_TRUST),
                    new CK_ATTRIBUTE(CKA_NETSCAPE_TRUST_SERVER_AUTH, trustValue),
                    new CK_ATTRIBUTE(CKA_NETSCAPE_TRUST_CODE_SIGNING, trustValue),
                    new CK_ATTRIBUTE(CKA_NETSCAPE_TRUST_EMAIL_PROTECTION, trustValue),
                    new CK_ATTRIBUTE(CKA_NETSCAPE_TRUST_CLIENT_AUTH, trustValue),
                    new CK_ATTRIBUTE(CKA_NETSCAPE_CERT_SHA1_HASH, bytes.b),
                    new CK_ATTRIBUTE(CKA_NETSCAPE_CERT_MD5_HASH, getDigest(cert, "MD5")),
                    new CK_ATTRIBUTE(CKA_ISSUER, cert.getIssuerX500Principal().getEncoded()),
                    new CK_ATTRIBUTE(CKA_SERIAL_NUMBER, cert.getSerialNumber().toByteArray()),
                };
                handle = token.p11.C_CreateObject(session.id(), attrs);
                shaHash = bytes.b;
                clientAuth = trustValue;
                serverAuth = trustValue;
                codeSigning = trustValue;
                emailProtection = trustValue;
            } catch (PKCS11Exception e) {
                throw new ProviderException("Could not create trust object", e);
            } finally {
                token.releaseSession(session);
            }
        }
        TrustAttributes(Token token, Session session, long handle)
                        throws PKCS11Exception {
            this.handle = handle;
            CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] {
                new CK_ATTRIBUTE(CKA_NETSCAPE_TRUST_SERVER_AUTH),
                new CK_ATTRIBUTE(CKA_NETSCAPE_TRUST_CODE_SIGNING),
                new CK_ATTRIBUTE(CKA_NETSCAPE_TRUST_EMAIL_PROTECTION),
                new CK_ATTRIBUTE(CKA_NETSCAPE_CERT_SHA1_HASH),
            };
            token.p11.C_GetAttributeValue(session.id(), handle, attrs);
            serverAuth = attrs[0].getLong();
            codeSigning = attrs[1].getLong();
            emailProtection = attrs[2].getLong();
            shaHash = attrs[3].getByteArray();
            attrs = new CK_ATTRIBUTE[] {
                new CK_ATTRIBUTE(CKA_NETSCAPE_TRUST_CLIENT_AUTH),
            };
            long c;
            try {
                token.p11.C_GetAttributeValue(session.id(), handle, attrs);
                c = attrs[0].getLong();
            } catch (PKCS11Exception e) {
                c = serverAuth;
            }
            clientAuth = c;
        }
        Bytes getHash() {
            return new Bytes(shaHash);
        }
        boolean isTrusted(TrustType type) {
            switch (type) {
            case CLIENT_AUTH:
                return isTrusted(clientAuth);
            case SERVER_AUTH:
                return isTrusted(serverAuth);
            case CODE_SIGNING:
                return isTrusted(codeSigning);
            case EMAIL_PROTECTION:
                return isTrusted(emailProtection);
            case ALL:
                return isTrusted(TrustType.CLIENT_AUTH)
                    && isTrusted(TrustType.SERVER_AUTH)
                    && isTrusted(TrustType.CODE_SIGNING)
                    && isTrusted(TrustType.EMAIL_PROTECTION);
            default:
                return false;
            }
        }
        private boolean isTrusted(long l) {
            return (l == CKT_NETSCAPE_TRUSTED_DELEGATOR);
        }
    }
    private static class Bytes {
        final byte[] b;
        Bytes(byte[] b) {
            this.b = b;
        }
        public int hashCode() {
            return Arrays.hashCode(b);
        }
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof Bytes == false) {
                return false;
            }
            Bytes other = (Bytes)o;
            return Arrays.equals(this.b, other.b);
        }
    }
    private static Map<Bytes,TrustAttributes> getTrust(SunPKCS11 provider)
            throws PKCS11Exception {
        Map<Bytes,TrustAttributes> trustMap = new HashMap<Bytes,TrustAttributes>();
        Token token = provider.getToken();
        Session session = null;
        try {
            session = token.getOpSession();
            int MAX_NUM = 8192;
            CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] {
                new CK_ATTRIBUTE(CKA_CLASS, CKO_NETSCAPE_TRUST),
            };
            token.p11.C_FindObjectsInit(session.id(), attrs);
            long[] handles = token.p11.C_FindObjects(session.id(), MAX_NUM);
            token.p11.C_FindObjectsFinal(session.id());
            if (DEBUG) System.out.println("handles: " + handles.length);
            for (long handle : handles) {
                TrustAttributes trust = new TrustAttributes(token, session, handle);
                trustMap.put(trust.getHash(), trust);
            }
        } finally {
            token.releaseSession(session);
        }
        return trustMap;
    }
    private static native long nssGetLibraryHandle(String libraryName);
    private static native long nssLoadLibrary(String name) throws IOException;
    private static native boolean nssVersionCheck(long handle, String minVersion);
    private static native boolean nssInit(String functionName, long handle, String configDir);
    private static native Object nssGetModuleList(long handle, String libDir);
}
