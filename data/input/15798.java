public class KeyStore {
    private static final String KEYSTORE_TYPE = "keystore.type";
    private String type;
    private Provider provider;
    private KeyStoreSpi keyStoreSpi;
    private boolean initialized = false;
    public static interface LoadStoreParameter {
        public ProtectionParameter getProtectionParameter();
    }
    public static interface ProtectionParameter { }
    public static class PasswordProtection implements
                ProtectionParameter, javax.security.auth.Destroyable {
        private final char[] password;
        private volatile boolean destroyed = false;
        public PasswordProtection(char[] password) {
            this.password = (password == null) ? null : password.clone();
        }
        public synchronized char[] getPassword() {
            if (destroyed) {
                throw new IllegalStateException("password has been cleared");
            }
            return password;
        }
        public synchronized void destroy()
                throws javax.security.auth.DestroyFailedException {
            destroyed = true;
            if (password != null) {
                Arrays.fill(password, ' ');
            }
        }
        public synchronized boolean isDestroyed() {
            return destroyed;
        }
    }
    public static class CallbackHandlerProtection
            implements ProtectionParameter {
        private final CallbackHandler handler;
        public CallbackHandlerProtection(CallbackHandler handler) {
            if (handler == null) {
                throw new NullPointerException("handler must not be null");
            }
            this.handler = handler;
        }
        public CallbackHandler getCallbackHandler() {
            return handler;
        }
    }
    public static interface Entry { }
    public static final class PrivateKeyEntry implements Entry {
        private final PrivateKey privKey;
        private final Certificate[] chain;
        public PrivateKeyEntry(PrivateKey privateKey, Certificate[] chain) {
            if (privateKey == null || chain == null) {
                throw new NullPointerException("invalid null input");
            }
            if (chain.length == 0) {
                throw new IllegalArgumentException
                                ("invalid zero-length input chain");
            }
            Certificate[] clonedChain = chain.clone();
            String certType = clonedChain[0].getType();
            for (int i = 1; i < clonedChain.length; i++) {
                if (!certType.equals(clonedChain[i].getType())) {
                    throw new IllegalArgumentException
                                ("chain does not contain certificates " +
                                "of the same type");
                }
            }
            if (!privateKey.getAlgorithm().equals
                        (clonedChain[0].getPublicKey().getAlgorithm())) {
                throw new IllegalArgumentException
                                ("private key algorithm does not match " +
                                "algorithm of public key in end entity " +
                                "certificate (at index 0)");
            }
            this.privKey = privateKey;
            if (clonedChain[0] instanceof X509Certificate &&
                !(clonedChain instanceof X509Certificate[])) {
                this.chain = new X509Certificate[clonedChain.length];
                System.arraycopy(clonedChain, 0,
                                this.chain, 0, clonedChain.length);
            } else {
                this.chain = clonedChain;
            }
        }
        public PrivateKey getPrivateKey() {
            return privKey;
        }
        public Certificate[] getCertificateChain() {
            return chain.clone();
        }
        public Certificate getCertificate() {
            return chain[0];
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Private key entry and certificate chain with "
                + chain.length + " elements:\r\n");
            for (Certificate cert : chain) {
                sb.append(cert);
                sb.append("\r\n");
            }
            return sb.toString();
        }
    }
    public static final class SecretKeyEntry implements Entry {
        private final SecretKey sKey;
        public SecretKeyEntry(SecretKey secretKey) {
            if (secretKey == null) {
                throw new NullPointerException("invalid null input");
            }
            this.sKey = secretKey;
        }
        public SecretKey getSecretKey() {
            return sKey;
        }
        public String toString() {
            return "Secret key entry with algorithm " + sKey.getAlgorithm();
        }
    }
    public static final class TrustedCertificateEntry implements Entry {
        private final Certificate cert;
        public TrustedCertificateEntry(Certificate trustedCert) {
            if (trustedCert == null) {
                throw new NullPointerException("invalid null input");
            }
            this.cert = trustedCert;
        }
        public Certificate getTrustedCertificate() {
            return cert;
        }
        public String toString() {
            return "Trusted certificate entry:\r\n" + cert.toString();
        }
    }
    protected KeyStore(KeyStoreSpi keyStoreSpi, Provider provider, String type)
    {
        this.keyStoreSpi = keyStoreSpi;
        this.provider = provider;
        this.type = type;
    }
    public static KeyStore getInstance(String type)
        throws KeyStoreException
    {
        try {
            Object[] objs = Security.getImpl(type, "KeyStore", (String)null);
            return new KeyStore((KeyStoreSpi)objs[0], (Provider)objs[1], type);
        } catch (NoSuchAlgorithmException nsae) {
            throw new KeyStoreException(type + " not found", nsae);
        } catch (NoSuchProviderException nspe) {
            throw new KeyStoreException(type + " not found", nspe);
        }
    }
    public static KeyStore getInstance(String type, String provider)
        throws KeyStoreException, NoSuchProviderException
    {
        if (provider == null || provider.length() == 0)
            throw new IllegalArgumentException("missing provider");
        try {
            Object[] objs = Security.getImpl(type, "KeyStore", provider);
            return new KeyStore((KeyStoreSpi)objs[0], (Provider)objs[1], type);
        } catch (NoSuchAlgorithmException nsae) {
            throw new KeyStoreException(type + " not found", nsae);
        }
    }
    public static KeyStore getInstance(String type, Provider provider)
        throws KeyStoreException
    {
        if (provider == null)
            throw new IllegalArgumentException("missing provider");
        try {
            Object[] objs = Security.getImpl(type, "KeyStore", provider);
            return new KeyStore((KeyStoreSpi)objs[0], (Provider)objs[1], type);
        } catch (NoSuchAlgorithmException nsae) {
            throw new KeyStoreException(type + " not found", nsae);
        }
    }
    public final static String getDefaultType() {
        String kstype;
        kstype = AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return Security.getProperty(KEYSTORE_TYPE);
            }
        });
        if (kstype == null) {
            kstype = "jks";
        }
        return kstype;
    }
    public final Provider getProvider()
    {
        return this.provider;
    }
    public final String getType()
    {
        return this.type;
    }
    public final Key getKey(String alias, char[] password)
        throws KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableKeyException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineGetKey(alias, password);
    }
    public final Certificate[] getCertificateChain(String alias)
        throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineGetCertificateChain(alias);
    }
    public final Certificate getCertificate(String alias)
        throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineGetCertificate(alias);
    }
    public final Date getCreationDate(String alias)
        throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineGetCreationDate(alias);
    }
    public final void setKeyEntry(String alias, Key key, char[] password,
                                  Certificate[] chain)
        throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        if ((key instanceof PrivateKey) &&
            (chain == null || chain.length == 0)) {
            throw new IllegalArgumentException("Private key must be "
                                               + "accompanied by certificate "
                                               + "chain");
        }
        keyStoreSpi.engineSetKeyEntry(alias, key, password, chain);
    }
    public final void setKeyEntry(String alias, byte[] key,
                                  Certificate[] chain)
        throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        keyStoreSpi.engineSetKeyEntry(alias, key, chain);
    }
    public final void setCertificateEntry(String alias, Certificate cert)
        throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        keyStoreSpi.engineSetCertificateEntry(alias, cert);
    }
    public final void deleteEntry(String alias)
        throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        keyStoreSpi.engineDeleteEntry(alias);
    }
    public final Enumeration<String> aliases()
        throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineAliases();
    }
    public final boolean containsAlias(String alias)
        throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineContainsAlias(alias);
    }
    public final int size()
        throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineSize();
    }
    public final boolean isKeyEntry(String alias)
        throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineIsKeyEntry(alias);
    }
    public final boolean isCertificateEntry(String alias)
        throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineIsCertificateEntry(alias);
    }
    public final String getCertificateAlias(Certificate cert)
        throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineGetCertificateAlias(cert);
    }
    public final void store(OutputStream stream, char[] password)
        throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        keyStoreSpi.engineStore(stream, password);
    }
    public final void store(LoadStoreParameter param)
                throws KeyStoreException, IOException,
                NoSuchAlgorithmException, CertificateException {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        keyStoreSpi.engineStore(param);
    }
    public final void load(InputStream stream, char[] password)
        throws IOException, NoSuchAlgorithmException, CertificateException
    {
        keyStoreSpi.engineLoad(stream, password);
        initialized = true;
    }
    public final void load(LoadStoreParameter param)
                throws IOException, NoSuchAlgorithmException,
                CertificateException {
        keyStoreSpi.engineLoad(param);
        initialized = true;
    }
    public final Entry getEntry(String alias, ProtectionParameter protParam)
                throws NoSuchAlgorithmException, UnrecoverableEntryException,
                KeyStoreException {
        if (alias == null) {
            throw new NullPointerException("invalid null input");
        }
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineGetEntry(alias, protParam);
    }
    public final void setEntry(String alias, Entry entry,
                        ProtectionParameter protParam)
                throws KeyStoreException {
        if (alias == null || entry == null) {
            throw new NullPointerException("invalid null input");
        }
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        keyStoreSpi.engineSetEntry(alias, entry, protParam);
    }
    public final boolean
        entryInstanceOf(String alias,
                        Class<? extends KeyStore.Entry> entryClass)
        throws KeyStoreException
    {
        if (alias == null || entryClass == null) {
            throw new NullPointerException("invalid null input");
        }
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineEntryInstanceOf(alias, entryClass);
    }
    public static abstract class Builder {
        static final int MAX_CALLBACK_TRIES = 3;
        protected Builder() {
        }
        public abstract KeyStore getKeyStore() throws KeyStoreException;
        public abstract ProtectionParameter getProtectionParameter(String alias)
            throws KeyStoreException;
        public static Builder newInstance(final KeyStore keyStore,
                final ProtectionParameter protectionParameter) {
            if ((keyStore == null) || (protectionParameter == null)) {
                throw new NullPointerException();
            }
            if (keyStore.initialized == false) {
                throw new IllegalArgumentException("KeyStore not initialized");
            }
            return new Builder() {
                private volatile boolean getCalled;
                public KeyStore getKeyStore() {
                    getCalled = true;
                    return keyStore;
                }
                public ProtectionParameter getProtectionParameter(String alias)
                {
                    if (alias == null) {
                        throw new NullPointerException();
                    }
                    if (getCalled == false) {
                        throw new IllegalStateException
                            ("getKeyStore() must be called first");
                    }
                    return protectionParameter;
                }
            };
        }
        public static Builder newInstance(String type, Provider provider,
                File file, ProtectionParameter protection) {
            if ((type == null) || (file == null) || (protection == null)) {
                throw new NullPointerException();
            }
            if ((protection instanceof PasswordProtection == false) &&
                (protection instanceof CallbackHandlerProtection == false)) {
                throw new IllegalArgumentException
                ("Protection must be PasswordProtection or " +
                 "CallbackHandlerProtection");
            }
            if (file.isFile() == false) {
                throw new IllegalArgumentException
                    ("File does not exist or it does not refer " +
                     "to a normal file: " + file);
            }
            return new FileBuilder(type, provider, file, protection,
                AccessController.getContext());
        }
        private static final class FileBuilder extends Builder {
            private final String type;
            private final Provider provider;
            private final File file;
            private ProtectionParameter protection;
            private ProtectionParameter keyProtection;
            private final AccessControlContext context;
            private KeyStore keyStore;
            private Throwable oldException;
            FileBuilder(String type, Provider provider, File file,
                    ProtectionParameter protection,
                    AccessControlContext context) {
                this.type = type;
                this.provider = provider;
                this.file = file;
                this.protection = protection;
                this.context = context;
            }
            public synchronized KeyStore getKeyStore() throws KeyStoreException
            {
                if (keyStore != null) {
                    return keyStore;
                }
                if (oldException != null) {
                    throw new KeyStoreException
                        ("Previous KeyStore instantiation failed",
                         oldException);
                }
                PrivilegedExceptionAction<KeyStore> action =
                        new PrivilegedExceptionAction<KeyStore>() {
                    public KeyStore run() throws Exception {
                        if (protection instanceof CallbackHandlerProtection == false) {
                            return run0();
                        }
                        int tries = 0;
                        while (true) {
                            tries++;
                            try {
                                return run0();
                            } catch (IOException e) {
                                if ((tries < MAX_CALLBACK_TRIES)
                                        && (e.getCause() instanceof UnrecoverableKeyException)) {
                                    continue;
                                }
                                throw e;
                            }
                        }
                    }
                    public KeyStore run0() throws Exception {
                        KeyStore ks;
                        if (provider == null) {
                            ks = KeyStore.getInstance(type);
                        } else {
                            ks = KeyStore.getInstance(type, provider);
                        }
                        InputStream in = null;
                        char[] password = null;
                        try {
                            in = new FileInputStream(file);
                            if (protection instanceof PasswordProtection) {
                                password =
                                ((PasswordProtection)protection).getPassword();
                                keyProtection = protection;
                            } else {
                                CallbackHandler handler =
                                    ((CallbackHandlerProtection)protection)
                                    .getCallbackHandler();
                                PasswordCallback callback = new PasswordCallback
                                    ("Password for keystore " + file.getName(),
                                    false);
                                handler.handle(new Callback[] {callback});
                                password = callback.getPassword();
                                if (password == null) {
                                    throw new KeyStoreException("No password" +
                                                                " provided");
                                }
                                callback.clearPassword();
                                keyProtection = new PasswordProtection(password);
                            }
                            ks.load(in, password);
                            return ks;
                        } finally {
                            if (in != null) {
                                in.close();
                            }
                        }
                    }
                };
                try {
                    keyStore = AccessController.doPrivileged(action, context);
                    return keyStore;
                } catch (PrivilegedActionException e) {
                    oldException = e.getCause();
                    throw new KeyStoreException
                        ("KeyStore instantiation failed", oldException);
                }
            }
            public synchronized ProtectionParameter
                        getProtectionParameter(String alias) {
                if (alias == null) {
                    throw new NullPointerException();
                }
                if (keyStore == null) {
                    throw new IllegalStateException
                        ("getKeyStore() must be called first");
                }
                return keyProtection;
            }
        }
        public static Builder newInstance(final String type,
                final Provider provider, final ProtectionParameter protection) {
            if ((type == null) || (protection == null)) {
                throw new NullPointerException();
            }
            final AccessControlContext context = AccessController.getContext();
            return new Builder() {
                private volatile boolean getCalled;
                private IOException oldException;
                private final PrivilegedExceptionAction<KeyStore> action
                        = new PrivilegedExceptionAction<KeyStore>() {
                    public KeyStore run() throws Exception {
                        KeyStore ks;
                        if (provider == null) {
                            ks = KeyStore.getInstance(type);
                        } else {
                            ks = KeyStore.getInstance(type, provider);
                        }
                        LoadStoreParameter param = new SimpleLoadStoreParameter(protection);
                        if (protection instanceof CallbackHandlerProtection == false) {
                            ks.load(param);
                        } else {
                            int tries = 0;
                            while (true) {
                                tries++;
                                try {
                                    ks.load(param);
                                    break;
                                } catch (IOException e) {
                                    if (e.getCause() instanceof UnrecoverableKeyException) {
                                        if (tries < MAX_CALLBACK_TRIES) {
                                            continue;
                                        } else {
                                            oldException = e;
                                        }
                                    }
                                    throw e;
                                }
                            }
                        }
                        getCalled = true;
                        return ks;
                    }
                };
                public synchronized KeyStore getKeyStore()
                        throws KeyStoreException {
                    if (oldException != null) {
                        throw new KeyStoreException
                            ("Previous KeyStore instantiation failed",
                             oldException);
                    }
                    try {
                        return AccessController.doPrivileged(action);
                    } catch (PrivilegedActionException e) {
                        Throwable cause = e.getCause();
                        throw new KeyStoreException
                            ("KeyStore instantiation failed", cause);
                    }
                }
                public ProtectionParameter getProtectionParameter(String alias)
                {
                    if (alias == null) {
                        throw new NullPointerException();
                    }
                    if (getCalled == false) {
                        throw new IllegalStateException
                            ("getKeyStore() must be called first");
                    }
                    return protection;
                }
            };
        }
    }
    static class SimpleLoadStoreParameter implements LoadStoreParameter {
        private final ProtectionParameter protection;
        SimpleLoadStoreParameter(ProtectionParameter protection) {
            this.protection = protection;
        }
        public ProtectionParameter getProtectionParameter() {
            return protection;
        }
    }
}
