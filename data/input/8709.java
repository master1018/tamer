final class P11KeyStore extends KeyStoreSpi {
    private static final CK_ATTRIBUTE ATTR_CLASS_CERT =
                        new CK_ATTRIBUTE(CKA_CLASS, CKO_CERTIFICATE);
    private static final CK_ATTRIBUTE ATTR_CLASS_PKEY =
                        new CK_ATTRIBUTE(CKA_CLASS, CKO_PRIVATE_KEY);
    private static final CK_ATTRIBUTE ATTR_CLASS_SKEY =
                        new CK_ATTRIBUTE(CKA_CLASS, CKO_SECRET_KEY);
    private static final CK_ATTRIBUTE ATTR_X509_CERT_TYPE =
                        new CK_ATTRIBUTE(CKA_CERTIFICATE_TYPE, CKC_X_509);
    private static final CK_ATTRIBUTE ATTR_TOKEN_TRUE =
                        new CK_ATTRIBUTE(CKA_TOKEN, true);
    private static CK_ATTRIBUTE ATTR_SKEY_TOKEN_TRUE = ATTR_TOKEN_TRUE;
    private static final CK_ATTRIBUTE ATTR_TRUSTED_TRUE =
                        new CK_ATTRIBUTE(CKA_TRUSTED, true);
    private static final CK_ATTRIBUTE ATTR_PRIVATE_TRUE =
                        new CK_ATTRIBUTE(CKA_PRIVATE, true);
    private static final long NO_HANDLE = -1;
    private static final long FINDOBJECTS_MAX = 100;
    private static final String ALIAS_SEP = "/";
    private static final boolean NSS_TEST = false;
    private static final Debug debug =
                        Debug.getInstance("pkcs11keystore");
    private static boolean CKA_TRUSTED_SUPPORTED = true;
    private final Token token;
    private boolean writeDisabled = false;
    private HashMap<String, AliasInfo> aliasMap;
    private final boolean useSecmodTrust;
    private Secmod.TrustType nssTrustType;
    private static class AliasInfo {
        private CK_ATTRIBUTE type = null;
        private String label = null;
        private byte[] id = null;
        private boolean trusted = false;
        private X509Certificate cert = null;
        private X509Certificate chain[] = null;
        private boolean matched = false;
        public AliasInfo(String label) {
            this.type = ATTR_CLASS_SKEY;
            this.label = label;
        }
        public AliasInfo(String label,
                        byte[] id,
                        boolean trusted,
                        X509Certificate cert) {
            this.type = ATTR_CLASS_PKEY;
            this.label = label;
            this.id = id;
            this.trusted = trusted;
            this.cert = cert;
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (type == ATTR_CLASS_PKEY) {
                sb.append("\ttype=[private key]\n");
            } else if (type == ATTR_CLASS_SKEY) {
                sb.append("\ttype=[secret key]\n");
            } else if (type == ATTR_CLASS_CERT) {
                sb.append("\ttype=[trusted cert]\n");
            }
            sb.append("\tlabel=[" + label + "]\n");
            if (id == null) {
                sb.append("\tid=[null]\n");
            } else {
                sb.append("\tid=" + P11KeyStore.getID(id) + "\n");
            }
            sb.append("\ttrusted=[" + trusted + "]\n");
            sb.append("\tmatched=[" + matched + "]\n");
            if (cert == null) {
                sb.append("\tcert=[null]\n");
            } else {
                sb.append("\tcert=[\tsubject: " +
                        cert.getSubjectX500Principal() +
                        "\n\t\tissuer: " +
                        cert.getIssuerX500Principal() +
                        "\n\t\tserialNum: " +
                        cert.getSerialNumber().toString() +
                        "]");
            }
            return sb.toString();
        }
    }
    private static class PasswordCallbackHandler implements CallbackHandler {
        private char[] password;
        private PasswordCallbackHandler(char[] password) {
            if (password != null) {
                this.password = (char[])password.clone();
            }
        }
        public void handle(Callback[] callbacks)
                throws IOException, UnsupportedCallbackException {
            if (!(callbacks[0] instanceof PasswordCallback)) {
                throw new UnsupportedCallbackException(callbacks[0]);
            }
            PasswordCallback pc = (PasswordCallback)callbacks[0];
            pc.setPassword(password);  
        }
        protected void finalize() throws Throwable {
            if (password != null) {
                Arrays.fill(password, ' ');
            }
            super.finalize();
        }
    }
    private static class THandle {
        private final long handle;              
        private final CK_ATTRIBUTE type;        
        private THandle(long handle, CK_ATTRIBUTE type) {
            this.handle = handle;
            this.type = type;
        }
    }
    P11KeyStore(Token token) {
        this.token = token;
        this.useSecmodTrust = token.provider.nssUseSecmodTrust;
    }
    public synchronized Key engineGetKey(String alias, char[] password)
                throws NoSuchAlgorithmException, UnrecoverableKeyException {
        token.ensureValid();
        if (password != null && !token.config.getKeyStoreCompatibilityMode()) {
            throw new NoSuchAlgorithmException("password must be null");
        }
        AliasInfo aliasInfo = aliasMap.get(alias);
        if (aliasInfo == null || aliasInfo.type == ATTR_CLASS_CERT) {
            return null;
        }
        Session session = null;
        try {
            session = token.getOpSession();
            if (aliasInfo.type == ATTR_CLASS_PKEY) {
                THandle h = getTokenObject(session,
                                        aliasInfo.type,
                                        aliasInfo.id,
                                        null);
                if (h.type == ATTR_CLASS_PKEY) {
                    return loadPkey(session, h.handle);
                }
            } else {
                THandle h = getTokenObject(session,
                                        ATTR_CLASS_SKEY,
                                        null,
                                        alias);
                if (h.type == ATTR_CLASS_SKEY) {
                    return loadSkey(session, h.handle);
                }
            }
            return null;
        } catch (PKCS11Exception pe) {
            throw new ProviderException(pe);
        } catch (KeyStoreException ke) {
            throw new ProviderException(ke);
        } finally {
            token.releaseSession(session);
        }
    }
    public synchronized Certificate[] engineGetCertificateChain(String alias) {
        token.ensureValid();
        AliasInfo aliasInfo = aliasMap.get(alias);
        if (aliasInfo == null || aliasInfo.type != ATTR_CLASS_PKEY) {
            return null;
        }
        return aliasInfo.chain;
    }
    public synchronized Certificate engineGetCertificate(String alias) {
        token.ensureValid();
        AliasInfo aliasInfo = aliasMap.get(alias);
        if (aliasInfo == null) {
            return null;
        }
        return aliasInfo.cert;
    }
    public Date engineGetCreationDate(String alias) {
        token.ensureValid();
        throw new ProviderException(new UnsupportedOperationException());
    }
    public synchronized void engineSetKeyEntry(String alias, Key key,
                                   char[] password,
                                   Certificate[] chain)
                throws KeyStoreException {
        token.ensureValid();
        checkWrite();
        if (!(key instanceof PrivateKey) && !(key instanceof SecretKey)) {
            throw new KeyStoreException("key must be PrivateKey or SecretKey");
        } else if (key instanceof PrivateKey && chain == null) {
            throw new KeyStoreException
                ("PrivateKey must be accompanied by non-null chain");
        } else if (key instanceof SecretKey && chain != null) {
            throw new KeyStoreException
                ("SecretKey must be accompanied by null chain");
        } else if (password != null &&
                    !token.config.getKeyStoreCompatibilityMode()) {
            throw new KeyStoreException("Password must be null");
        }
        KeyStore.Entry entry = null;
        try {
            if (key instanceof PrivateKey) {
                entry = new KeyStore.PrivateKeyEntry((PrivateKey)key, chain);
            } else if (key instanceof SecretKey) {
                entry = new KeyStore.SecretKeyEntry((SecretKey)key);
            }
        } catch (NullPointerException npe) {
            throw new KeyStoreException(npe);
        } catch (IllegalArgumentException iae) {
            throw new KeyStoreException(iae);
        }
        engineSetEntry(alias, entry, new KeyStore.PasswordProtection(password));
    }
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain)
                throws KeyStoreException {
        token.ensureValid();
        throw new ProviderException(new UnsupportedOperationException());
    }
    public synchronized void engineSetCertificateEntry
        (String alias, Certificate cert) throws KeyStoreException {
        token.ensureValid();
        checkWrite();
        if (cert == null) {
            throw new KeyStoreException("invalid null certificate");
        }
        KeyStore.Entry entry = null;
        entry = new KeyStore.TrustedCertificateEntry(cert);
        engineSetEntry(alias, entry, null);
    }
    public synchronized void engineDeleteEntry(String alias)
                throws KeyStoreException {
        token.ensureValid();
        if (token.isWriteProtected()) {
            throw new KeyStoreException("token write-protected");
        }
        checkWrite();
        deleteEntry(alias);
    }
    private boolean deleteEntry(String alias) throws KeyStoreException {
        AliasInfo aliasInfo = aliasMap.get(alias);
        if (aliasInfo != null) {
            aliasMap.remove(alias);
            try {
                if (aliasInfo.type == ATTR_CLASS_CERT) {
                    return destroyCert(aliasInfo.id);
                } else if (aliasInfo.type == ATTR_CLASS_PKEY) {
                    return destroyPkey(aliasInfo.id) &&
                                destroyChain(aliasInfo.id);
                } else if (aliasInfo.type == ATTR_CLASS_SKEY) {
                    return destroySkey(alias);
                } else {
                    throw new KeyStoreException("unexpected entry type");
                }
            } catch (PKCS11Exception pe) {
                throw new KeyStoreException(pe);
            } catch (CertificateException ce) {
                throw new KeyStoreException(ce);
            }
        }
        return false;
    }
    public synchronized Enumeration<String> engineAliases() {
        token.ensureValid();
        return Collections.enumeration(new HashSet<String>(aliasMap.keySet()));
    }
    public synchronized boolean engineContainsAlias(String alias) {
        token.ensureValid();
        return aliasMap.containsKey(alias);
    }
    public synchronized int engineSize() {
        token.ensureValid();
        return aliasMap.size();
    }
    public synchronized boolean engineIsKeyEntry(String alias) {
        token.ensureValid();
        AliasInfo aliasInfo = aliasMap.get(alias);
        if (aliasInfo == null || aliasInfo.type == ATTR_CLASS_CERT) {
            return false;
        }
        return true;
    }
    public synchronized boolean engineIsCertificateEntry(String alias) {
        token.ensureValid();
        AliasInfo aliasInfo = aliasMap.get(alias);
        if (aliasInfo == null || aliasInfo.type != ATTR_CLASS_CERT) {
            return false;
        }
        return true;
    }
    public synchronized String engineGetCertificateAlias(Certificate cert) {
        token.ensureValid();
        Enumeration<String> e = engineAliases();
        while (e.hasMoreElements()) {
            String alias = e.nextElement();
            Certificate tokenCert = engineGetCertificate(alias);
            if (tokenCert != null && tokenCert.equals(cert)) {
                return alias;
            }
        }
        return null;
    }
    public synchronized void engineStore(OutputStream stream, char[] password)
        throws IOException, NoSuchAlgorithmException, CertificateException {
        token.ensureValid();
        if (stream != null && !token.config.getKeyStoreCompatibilityMode()) {
            throw new IOException("output stream must be null");
        }
        if (password != null && !token.config.getKeyStoreCompatibilityMode()) {
            throw new IOException("password must be null");
        }
    }
    public synchronized void engineStore(KeyStore.LoadStoreParameter param)
        throws IOException, NoSuchAlgorithmException, CertificateException {
        token.ensureValid();
        if (param != null) {
            throw new IllegalArgumentException
                ("LoadStoreParameter must be null");
        }
    }
    public synchronized void engineLoad(InputStream stream, char[] password)
        throws IOException, NoSuchAlgorithmException, CertificateException {
        token.ensureValid();
        if (NSS_TEST) {
            ATTR_SKEY_TOKEN_TRUE = new CK_ATTRIBUTE(CKA_TOKEN, false);
        }
        if (stream != null && !token.config.getKeyStoreCompatibilityMode()) {
            throw new IOException("input stream must be null");
        }
        if (useSecmodTrust) {
            nssTrustType = Secmod.TrustType.ALL;
        }
        try {
            if (password == null) {
                login(null);
            } else {
                login(new PasswordCallbackHandler(password));
            }
            if (mapLabels() == true) {
                writeDisabled = true;
            }
            if (debug != null) {
                dumpTokenMap();
            }
        } catch (LoginException le) {
            IOException ioe = new IOException("load failed");
            ioe.initCause(le);
            throw ioe;
        } catch (KeyStoreException kse) {
            IOException ioe = new IOException("load failed");
            ioe.initCause(kse);
            throw ioe;
        } catch (PKCS11Exception pe) {
            IOException ioe = new IOException("load failed");
            ioe.initCause(pe);
            throw ioe;
        }
    }
    public synchronized void engineLoad(KeyStore.LoadStoreParameter param)
                throws IOException, NoSuchAlgorithmException,
                CertificateException {
        token.ensureValid();
        if (NSS_TEST) {
            ATTR_SKEY_TOKEN_TRUE = new CK_ATTRIBUTE(CKA_TOKEN, false);
        }
        if (param == null) {
            throw new IllegalArgumentException
                        ("invalid null LoadStoreParameter");
        }
        if (useSecmodTrust) {
            if (param instanceof Secmod.KeyStoreLoadParameter) {
                nssTrustType = ((Secmod.KeyStoreLoadParameter)param).getTrustType();
            } else {
                nssTrustType = Secmod.TrustType.ALL;
            }
        }
        CallbackHandler handler;
        KeyStore.ProtectionParameter pp = param.getProtectionParameter();
        if (pp instanceof PasswordProtection) {
            char[] password = ((PasswordProtection)pp).getPassword();
            if (password == null) {
                handler = null;
            } else {
                handler = new PasswordCallbackHandler(password);
            }
        } else if (pp instanceof CallbackHandlerProtection) {
            handler = ((CallbackHandlerProtection)pp).getCallbackHandler();
        } else {
            throw new IllegalArgumentException
                        ("ProtectionParameter must be either " +
                        "PasswordProtection or CallbackHandlerProtection");
        }
        try {
            login(handler);
            if (mapLabels() == true) {
                writeDisabled = true;
            }
            if (debug != null) {
                dumpTokenMap();
            }
        } catch (LoginException e) {
            throw new IOException("load failed", e);
        } catch (KeyStoreException e) {
            throw new IOException("load failed", e);
        } catch (PKCS11Exception e) {
            throw new IOException("load failed", e);
        }
    }
    private void login(CallbackHandler handler) throws LoginException {
        if ((token.tokenInfo.flags & CKF_PROTECTED_AUTHENTICATION_PATH) == 0) {
            token.provider.login(null, handler);
        } else {
            if (handler != null &&
                !token.config.getKeyStoreCompatibilityMode()) {
                throw new LoginException("can not specify password if token " +
                                "supports protected authentication path");
            }
            token.provider.login(null, null);
        }
    }
    public synchronized KeyStore.Entry engineGetEntry(String alias,
                        KeyStore.ProtectionParameter protParam)
                throws KeyStoreException, NoSuchAlgorithmException,
                UnrecoverableEntryException {
        token.ensureValid();
        if (protParam != null &&
            protParam instanceof KeyStore.PasswordProtection &&
            ((KeyStore.PasswordProtection)protParam).getPassword() != null &&
            !token.config.getKeyStoreCompatibilityMode()) {
            throw new KeyStoreException("ProtectionParameter must be null");
        }
        AliasInfo aliasInfo = aliasMap.get(alias);
        if (aliasInfo == null) {
            if (debug != null) {
                debug.println("engineGetEntry did not find alias [" +
                        alias +
                        "] in map");
            }
            return null;
        }
        Session session = null;
        try {
            session = token.getOpSession();
            if (aliasInfo.type == ATTR_CLASS_CERT) {
                if (debug != null) {
                    debug.println("engineGetEntry found trusted cert entry");
                }
                return new KeyStore.TrustedCertificateEntry(aliasInfo.cert);
            } else if (aliasInfo.type == ATTR_CLASS_SKEY) {
                if (debug != null) {
                    debug.println("engineGetEntry found secret key entry");
                }
                THandle h = getTokenObject
                        (session, ATTR_CLASS_SKEY, null, aliasInfo.label);
                if (h.type != ATTR_CLASS_SKEY) {
                    throw new KeyStoreException
                        ("expected but could not find secret key");
                } else {
                    SecretKey skey = loadSkey(session, h.handle);
                    return new KeyStore.SecretKeyEntry(skey);
                }
            } else {
                if (debug != null) {
                    debug.println("engineGetEntry found private key entry");
                }
                THandle h = getTokenObject
                        (session, ATTR_CLASS_PKEY, aliasInfo.id, null);
                if (h.type != ATTR_CLASS_PKEY) {
                    throw new KeyStoreException
                        ("expected but could not find private key");
                } else {
                    PrivateKey pkey = loadPkey(session, h.handle);
                    Certificate[] chain = aliasInfo.chain;
                    if ((pkey != null) && (chain != null)) {
                        return new KeyStore.PrivateKeyEntry(pkey, chain);
                    } else {
                        if (debug != null) {
                            debug.println
                                ("engineGetEntry got null cert chain or private key");
                        }
                    }
                }
            }
            return null;
        } catch (PKCS11Exception pe) {
            throw new KeyStoreException(pe);
        } finally {
            token.releaseSession(session);
        }
    }
    public synchronized void engineSetEntry(String alias, KeyStore.Entry entry,
                        KeyStore.ProtectionParameter protParam)
                throws KeyStoreException {
        token.ensureValid();
        checkWrite();
        if (protParam != null &&
            protParam instanceof KeyStore.PasswordProtection &&
            ((KeyStore.PasswordProtection)protParam).getPassword() != null &&
            !token.config.getKeyStoreCompatibilityMode()) {
            throw new KeyStoreException(new UnsupportedOperationException
                                ("ProtectionParameter must be null"));
        }
        if (token.isWriteProtected()) {
            throw new KeyStoreException("token write-protected");
        }
        if (entry instanceof KeyStore.TrustedCertificateEntry) {
            if (useSecmodTrust == false) {
                throw new KeyStoreException(new UnsupportedOperationException
                                    ("trusted certificates may only be set by " +
                                    "token initialization application"));
            }
            Module module = token.provider.nssModule;
            if ((module.type != ModuleType.KEYSTORE) && (module.type != ModuleType.FIPS)) {
                throw new KeyStoreException("Trusted certificates can only be "
                    + "added to the NSS KeyStore module");
            }
            Certificate cert = ((TrustedCertificateEntry)entry).getTrustedCertificate();
            if (cert instanceof X509Certificate == false) {
                throw new KeyStoreException("Certificate must be an X509Certificate");
            }
            X509Certificate xcert = (X509Certificate)cert;
            AliasInfo info = aliasMap.get(alias);
            if (info != null) {
                deleteEntry(alias);
            }
            try {
                storeCert(alias, xcert);
                module.setTrust(token, xcert);
                mapLabels();
            } catch (PKCS11Exception e) {
                throw new KeyStoreException(e);
            } catch (CertificateException e) {
                throw new KeyStoreException(e);
            }
        } else {
            if (entry instanceof KeyStore.PrivateKeyEntry) {
                PrivateKey key =
                        ((KeyStore.PrivateKeyEntry)entry).getPrivateKey();
                if (!(key instanceof P11Key) &&
                    !(key instanceof RSAPrivateKey) &&
                    !(key instanceof DSAPrivateKey) &&
                    !(key instanceof DHPrivateKey) &&
                    !(key instanceof ECPrivateKey)) {
                    throw new KeyStoreException("unsupported key type: " +
                                                key.getClass().getName());
                }
                Certificate[] chain =
                    ((KeyStore.PrivateKeyEntry)entry).getCertificateChain();
                if (!(chain instanceof X509Certificate[])) {
                    throw new KeyStoreException
                        (new UnsupportedOperationException
                                ("unsupported certificate array type: " +
                                chain.getClass().getName()));
                }
                try {
                    boolean updatedAlias = false;
                    Set<String> aliases = aliasMap.keySet();
                    for (String oldAlias : aliases) {
                        AliasInfo aliasInfo = aliasMap.get(oldAlias);
                        if (aliasInfo.type == ATTR_CLASS_PKEY &&
                            aliasInfo.cert.getPublicKey().equals
                                        (chain[0].getPublicKey())) {
                            updatePkey(alias,
                                        aliasInfo.id,
                                        (X509Certificate[])chain,
                                        !aliasInfo.cert.equals(chain[0]));
                            updatedAlias = true;
                            break;
                        }
                    }
                    if (!updatedAlias) {
                        engineDeleteEntry(alias);
                        storePkey(alias, (KeyStore.PrivateKeyEntry)entry);
                    }
                } catch (PKCS11Exception pe) {
                    throw new KeyStoreException(pe);
                } catch (CertificateException ce) {
                    throw new KeyStoreException(ce);
                }
            } else if (entry instanceof KeyStore.SecretKeyEntry) {
                KeyStore.SecretKeyEntry ske = (KeyStore.SecretKeyEntry)entry;
                SecretKey skey = ske.getSecretKey();
                try {
                    AliasInfo aliasInfo = aliasMap.get(alias);
                    if (aliasInfo != null) {
                        engineDeleteEntry(alias);
                    }
                    storeSkey(alias, ske);
                } catch (PKCS11Exception pe) {
                    throw new KeyStoreException(pe);
                }
            } else {
                throw new KeyStoreException(new UnsupportedOperationException
                    ("unsupported entry type: " + entry.getClass().getName()));
            }
            try {
                mapLabels();
                if (debug != null) {
                    dumpTokenMap();
                }
            } catch (PKCS11Exception pe) {
                throw new KeyStoreException(pe);
            } catch (CertificateException ce) {
                throw new KeyStoreException(ce);
            }
        }
        if (debug != null) {
            debug.println
                ("engineSetEntry added new entry for [" +
                alias +
                "] to token");
        }
    }
    public synchronized boolean engineEntryInstanceOf
                (String alias, Class<? extends KeyStore.Entry> entryClass) {
        token.ensureValid();
        return super.engineEntryInstanceOf(alias, entryClass);
    }
    private X509Certificate loadCert(Session session, long oHandle)
                throws PKCS11Exception, CertificateException {
        CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[]
                        { new CK_ATTRIBUTE(CKA_VALUE) };
        token.p11.C_GetAttributeValue(session.id(), oHandle, attrs);
        byte[] bytes = attrs[0].getByteArray();
        if (bytes == null) {
            throw new CertificateException
                        ("unexpectedly retrieved null byte array");
        }
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate)cf.generateCertificate
                        (new ByteArrayInputStream(bytes));
    }
    private X509Certificate[] loadChain(Session session,
                                        X509Certificate endCert)
                throws PKCS11Exception, CertificateException {
        ArrayList<X509Certificate> lChain = null;
        if (endCert.getSubjectX500Principal().equals
            (endCert.getIssuerX500Principal())) {
            return new X509Certificate[] { endCert };
        } else {
            lChain = new ArrayList<X509Certificate>();
            lChain.add(endCert);
        }
        X509Certificate next = endCert;
        while (true) {
            CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] {
                        ATTR_TOKEN_TRUE,
                        ATTR_CLASS_CERT,
                        new CK_ATTRIBUTE(CKA_SUBJECT,
                                next.getIssuerX500Principal().getEncoded()) };
            long[] ch = findObjects(session, attrs);
            if (ch == null || ch.length == 0) {
                break;
            } else {
                if (debug != null && ch.length > 1) {
                    debug.println("engineGetEntry found " +
                                ch.length +
                                " certificate entries for subject [" +
                                next.getIssuerX500Principal().toString() +
                                "] in token - using first entry");
                }
                next = loadCert(session, ch[0]);
                lChain.add(next);
                if (next.getSubjectX500Principal().equals
                    (next.getIssuerX500Principal())) {
                    break;
                }
            }
        }
        return lChain.toArray(new X509Certificate[lChain.size()]);
    }
    private SecretKey loadSkey(Session session, long oHandle)
                throws PKCS11Exception {
        CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] {
                        new CK_ATTRIBUTE(CKA_KEY_TYPE) };
        token.p11.C_GetAttributeValue(session.id(), oHandle, attrs);
        long kType = attrs[0].getLong();
        String keyType = null;
        int keyLength = -1;
        if (kType == CKK_DES || kType == CKK_DES3) {
            if (kType == CKK_DES) {
                keyType = "DES";
                keyLength = 64;
            } else if (kType == CKK_DES3) {
                keyType = "DESede";
                keyLength = 192;
            }
        } else {
            if (kType == CKK_AES) {
                keyType = "AES";
            } else if (kType == CKK_BLOWFISH) {
                keyType = "Blowfish";
            } else if (kType == CKK_RC4) {
                keyType = "ARCFOUR";
            } else {
                if (debug != null) {
                    debug.println("unknown key type [" +
                                kType +
                                "] - using 'Generic Secret'");
                }
                keyType = "Generic Secret";
            }
            if (NSS_TEST) {
                keyLength = 128;
            } else {
                attrs = new CK_ATTRIBUTE[] { new CK_ATTRIBUTE(CKA_VALUE_LEN) };
                token.p11.C_GetAttributeValue(session.id(), oHandle, attrs);
                keyLength = (int)attrs[0].getLong();
            }
        }
        return P11Key.secretKey(session, oHandle, keyType, keyLength, null);
    }
    private PrivateKey loadPkey(Session session, long oHandle)
        throws PKCS11Exception, KeyStoreException {
        CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] {
                        new CK_ATTRIBUTE(CKA_KEY_TYPE) };
        token.p11.C_GetAttributeValue(session.id(), oHandle, attrs);
        long kType = attrs[0].getLong();
        String keyType = null;
        int keyLength = 0;
        if (kType == CKK_RSA) {
            keyType = "RSA";
            attrs = new CK_ATTRIBUTE[] { new CK_ATTRIBUTE(CKA_MODULUS) };
            token.p11.C_GetAttributeValue(session.id(), oHandle, attrs);
            BigInteger modulus = attrs[0].getBigInteger();
            keyLength = modulus.bitLength();
            try {
                RSAKeyFactory.checkKeyLengths(keyLength, null,
                    -1, Integer.MAX_VALUE);
            } catch (InvalidKeyException e) {
                throw new KeyStoreException(e.getMessage());
            }
            return P11Key.privateKey(session,
                                oHandle,
                                keyType,
                                keyLength,
                                null);
        } else if (kType == CKK_DSA) {
            keyType = "DSA";
            attrs = new CK_ATTRIBUTE[] { new CK_ATTRIBUTE(CKA_PRIME) };
            token.p11.C_GetAttributeValue(session.id(), oHandle, attrs);
            BigInteger prime = attrs[0].getBigInteger();
            keyLength = prime.bitLength();
            return P11Key.privateKey(session,
                                oHandle,
                                keyType,
                                keyLength,
                                null);
        } else if (kType == CKK_DH) {
            keyType = "DH";
            attrs = new CK_ATTRIBUTE[] { new CK_ATTRIBUTE(CKA_PRIME) };
            token.p11.C_GetAttributeValue(session.id(), oHandle, attrs);
            BigInteger prime = attrs[0].getBigInteger();
            keyLength = prime.bitLength();
            return P11Key.privateKey(session,
                                oHandle,
                                keyType,
                                keyLength,
                                null);
        } else if (kType == CKK_EC) {
            attrs = new CK_ATTRIBUTE[] {
                new CK_ATTRIBUTE(CKA_EC_PARAMS),
            };
            token.p11.C_GetAttributeValue(session.id(), oHandle, attrs);
            byte[] encodedParams = attrs[0].getByteArray();
            try {
                ECParameterSpec params = ECParameters.decodeParameters(encodedParams);
                keyLength = params.getCurve().getField().getFieldSize();
            } catch (IOException e) {
                throw new KeyStoreException("Unsupported parameters", e);
            }
            return P11Key.privateKey(session, oHandle, "EC", keyLength, null);
        } else {
            if (debug != null) {
                debug.println("unknown key type [" + kType + "]");
            }
            throw new KeyStoreException("unknown key type");
        }
    }
    private void updatePkey(String alias,
                        byte[] cka_id,
                        X509Certificate[] chain,
                        boolean replaceCert) throws
                KeyStoreException, CertificateException, PKCS11Exception {
        replaceCert = true;
        Session session = null;
        try {
            session = token.getOpSession();
            THandle h = getTokenObject(session, ATTR_CLASS_PKEY, cka_id, null);
            long pKeyHandle;
            if (h.type == ATTR_CLASS_PKEY) {
                pKeyHandle = h.handle;
            } else {
                throw new KeyStoreException
                        ("expected but could not find private key " +
                        "with CKA_ID " +
                        getID(cka_id));
            }
            h = getTokenObject(session, ATTR_CLASS_CERT, cka_id, null);
            if (h.type != ATTR_CLASS_CERT) {
                throw new KeyStoreException
                        ("expected but could not find certificate " +
                        "with CKA_ID " +
                        getID(cka_id));
            } else {
                if (replaceCert) {
                    destroyChain(cka_id);
                } else {
                    CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] {
                        new CK_ATTRIBUTE(CKA_LABEL, alias),
                        new CK_ATTRIBUTE(CKA_ID, alias) };
                    token.p11.C_SetAttributeValue
                        (session.id(), h.handle, attrs);
                }
            }
            if (replaceCert) {
                storeChain(alias, chain);
            } else {
                storeCaCerts(chain, 1);
            }
            CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] {
                                new CK_ATTRIBUTE(CKA_ID, alias) };
            token.p11.C_SetAttributeValue(session.id(), pKeyHandle, attrs);
            if (debug != null) {
                debug.println("updatePkey set new alias [" +
                                alias +
                                "] for private key entry");
            }
        } finally {
            token.releaseSession(session);
        }
    }
    private void updateP11Pkey(String alias, CK_ATTRIBUTE attribute, P11Key key)
                throws PKCS11Exception {
        Session session = null;
        try {
            session = token.getOpSession();
            if (key.tokenObject == true) {
                CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] {
                                new CK_ATTRIBUTE(CKA_ID, alias) };
                token.p11.C_SetAttributeValue
                                (session.id(), key.keyID, attrs);
                if (debug != null) {
                    debug.println("updateP11Pkey set new alias [" +
                                alias +
                                "] for key entry");
                }
            } else {
                CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] {
                    ATTR_TOKEN_TRUE,
                    new CK_ATTRIBUTE(CKA_ID, alias),
                };
                if (attribute != null) {
                    attrs = addAttribute(attrs, attribute);
                }
                token.p11.C_CopyObject(session.id(), key.keyID, attrs);
                if (debug != null) {
                    debug.println("updateP11Pkey copied private session key " +
                                "for [" +
                                alias +
                                "] to token entry");
                }
            }
        } finally {
            token.releaseSession(session);
        }
    }
    private void storeCert(String alias, X509Certificate cert)
                throws PKCS11Exception, CertificateException {
        ArrayList<CK_ATTRIBUTE> attrList = new ArrayList<CK_ATTRIBUTE>();
        attrList.add(ATTR_TOKEN_TRUE);
        attrList.add(ATTR_CLASS_CERT);
        attrList.add(ATTR_X509_CERT_TYPE);
        attrList.add(new CK_ATTRIBUTE(CKA_SUBJECT,
                                cert.getSubjectX500Principal().getEncoded()));
        attrList.add(new CK_ATTRIBUTE(CKA_ISSUER,
                                cert.getIssuerX500Principal().getEncoded()));
        attrList.add(new CK_ATTRIBUTE(CKA_SERIAL_NUMBER,
                                cert.getSerialNumber().toByteArray()));
        attrList.add(new CK_ATTRIBUTE(CKA_VALUE, cert.getEncoded()));
        if (alias != null) {
            attrList.add(new CK_ATTRIBUTE(CKA_LABEL, alias));
            attrList.add(new CK_ATTRIBUTE(CKA_ID, alias));
        } else {
            attrList.add(new CK_ATTRIBUTE(CKA_ID,
                        getID(cert.getSubjectX500Principal().getName
                                        (X500Principal.CANONICAL), cert)));
        }
        Session session = null;
        try {
            session = token.getOpSession();
            token.p11.C_CreateObject(session.id(),
                        attrList.toArray(new CK_ATTRIBUTE[attrList.size()]));
        } finally {
            token.releaseSession(session);
        }
    }
    private void storeChain(String alias, X509Certificate[] chain)
                throws PKCS11Exception, CertificateException {
        storeCert(alias, chain[0]);
        storeCaCerts(chain, 1);
    }
    private void storeCaCerts(X509Certificate[] chain, int start)
                throws PKCS11Exception, CertificateException {
        Session session = null;
        HashSet<X509Certificate> cacerts = new HashSet<X509Certificate>();
        try {
            session = token.getOpSession();
            CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] {
                        ATTR_TOKEN_TRUE,
                        ATTR_CLASS_CERT };
            long[] handles = findObjects(session, attrs);
            for (long handle : handles) {
                cacerts.add(loadCert(session, handle));
            }
        } finally {
            token.releaseSession(session);
        }
        for (int i = start; i < chain.length; i++) {
            if (!cacerts.contains(chain[i])) {
                storeCert(null, chain[i]);
            } else if (debug != null) {
                debug.println("ignoring duplicate CA cert for [" +
                        chain[i].getSubjectX500Principal() +
                        "]");
            }
        }
    }
    private void storeSkey(String alias, KeyStore.SecretKeyEntry ske)
                throws PKCS11Exception, KeyStoreException {
        SecretKey skey = ske.getSecretKey();
        CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] {
            ATTR_SKEY_TOKEN_TRUE,
            ATTR_PRIVATE_TRUE,
            new CK_ATTRIBUTE(CKA_LABEL, alias),
        };
        try {
            P11SecretKeyFactory.convertKey(token, skey, null, attrs);
        } catch (InvalidKeyException ike) {
            throw new KeyStoreException("Cannot convert to PKCS11 keys", ike);
        }
        aliasMap.put(alias, new AliasInfo(alias));
        if (debug != null) {
            debug.println("storeSkey created token secret key for [" +
                          alias + "]");
        }
    }
    private static CK_ATTRIBUTE[] addAttribute(CK_ATTRIBUTE[] attrs, CK_ATTRIBUTE attr) {
        int n = attrs.length;
        CK_ATTRIBUTE[] newAttrs = new CK_ATTRIBUTE[n + 1];
        System.arraycopy(attrs, 0, newAttrs, 0, n);
        newAttrs[n] = attr;
        return newAttrs;
    }
    private void storePkey(String alias, KeyStore.PrivateKeyEntry pke)
        throws PKCS11Exception, CertificateException, KeyStoreException  {
        PrivateKey key = pke.getPrivateKey();
        CK_ATTRIBUTE[] attrs = null;
        if (key instanceof P11Key) {
            P11Key p11Key = (P11Key)key;
            if (p11Key.tokenObject && (p11Key.token == this.token)) {
                updateP11Pkey(alias, null, p11Key);
                storeChain(alias, (X509Certificate[])pke.getCertificateChain());
                return;
            }
        }
        boolean useNDB = token.config.getNssNetscapeDbWorkaround();
        PublicKey publicKey = pke.getCertificate().getPublicKey();
        if (key instanceof RSAPrivateKey) {
            X509Certificate cert = (X509Certificate)pke.getCertificate();
            attrs = getRsaPrivKeyAttrs
                (alias, (RSAPrivateKey)key, cert.getSubjectX500Principal());
        } else if (key instanceof DSAPrivateKey) {
            DSAPrivateKey dsaKey = (DSAPrivateKey)key;
            CK_ATTRIBUTE[] idAttrs = getIdAttributes(key, publicKey, false, useNDB);
            if (idAttrs[0] == null) {
                idAttrs[0] = new CK_ATTRIBUTE(CKA_ID, alias);
            }
            attrs = new CK_ATTRIBUTE[] {
                ATTR_TOKEN_TRUE,
                ATTR_CLASS_PKEY,
                ATTR_PRIVATE_TRUE,
                new CK_ATTRIBUTE(CKA_KEY_TYPE, CKK_DSA),
                idAttrs[0],
                new CK_ATTRIBUTE(CKA_PRIME, dsaKey.getParams().getP()),
                new CK_ATTRIBUTE(CKA_SUBPRIME, dsaKey.getParams().getQ()),
                new CK_ATTRIBUTE(CKA_BASE, dsaKey.getParams().getG()),
                new CK_ATTRIBUTE(CKA_VALUE, dsaKey.getX()),
            };
            if (idAttrs[1] != null) {
                attrs = addAttribute(attrs, idAttrs[1]);
            }
            attrs = token.getAttributes
                (TemplateManager.O_IMPORT, CKO_PRIVATE_KEY, CKK_DSA, attrs);
            if (debug != null) {
                debug.println("storePkey created DSA template");
            }
        } else if (key instanceof DHPrivateKey) {
            DHPrivateKey dhKey = (DHPrivateKey)key;
            CK_ATTRIBUTE[] idAttrs = getIdAttributes(key, publicKey, false, useNDB);
            if (idAttrs[0] == null) {
                idAttrs[0] = new CK_ATTRIBUTE(CKA_ID, alias);
            }
            attrs = new CK_ATTRIBUTE[] {
                ATTR_TOKEN_TRUE,
                ATTR_CLASS_PKEY,
                ATTR_PRIVATE_TRUE,
                new CK_ATTRIBUTE(CKA_KEY_TYPE, CKK_DH),
                idAttrs[0],
                new CK_ATTRIBUTE(CKA_PRIME, dhKey.getParams().getP()),
                new CK_ATTRIBUTE(CKA_BASE, dhKey.getParams().getG()),
                new CK_ATTRIBUTE(CKA_VALUE, dhKey.getX()),
            };
            if (idAttrs[1] != null) {
                attrs = addAttribute(attrs, idAttrs[1]);
            }
            attrs = token.getAttributes
                (TemplateManager.O_IMPORT, CKO_PRIVATE_KEY, CKK_DH, attrs);
        } else if (key instanceof ECPrivateKey) {
            ECPrivateKey ecKey = (ECPrivateKey)key;
            CK_ATTRIBUTE[] idAttrs = getIdAttributes(key, publicKey, false, useNDB);
            if (idAttrs[0] == null) {
                idAttrs[0] = new CK_ATTRIBUTE(CKA_ID, alias);
            }
            byte[] encodedParams = ECParameters.encodeParameters(ecKey.getParams());
            attrs = new CK_ATTRIBUTE[] {
                ATTR_TOKEN_TRUE,
                ATTR_CLASS_PKEY,
                ATTR_PRIVATE_TRUE,
                new CK_ATTRIBUTE(CKA_KEY_TYPE, CKK_EC),
                idAttrs[0],
                new CK_ATTRIBUTE(CKA_VALUE, ecKey.getS()),
                new CK_ATTRIBUTE(CKA_EC_PARAMS, encodedParams),
            };
            if (idAttrs[1] != null) {
                attrs = addAttribute(attrs, idAttrs[1]);
            }
            attrs = token.getAttributes
                (TemplateManager.O_IMPORT, CKO_PRIVATE_KEY, CKK_EC, attrs);
            if (debug != null) {
                debug.println("storePkey created EC template");
            }
        } else if (key instanceof P11Key) {
            P11Key p11Key = (P11Key)key;
            if (p11Key.token != this.token) {
                throw new KeyStoreException
                    ("Cannot move sensitive keys across tokens");
            }
            CK_ATTRIBUTE netscapeDB = null;
            if (useNDB) {
                CK_ATTRIBUTE[] idAttrs = getIdAttributes(key, publicKey, false, true);
                netscapeDB = idAttrs[1];
            }
            updateP11Pkey(alias, netscapeDB, p11Key);
            storeChain(alias, (X509Certificate[])pke.getCertificateChain());
            return;
        } else {
            throw new KeyStoreException("unsupported key type: " + key);
        }
        Session session = null;
        try {
            session = token.getOpSession();
            token.p11.C_CreateObject(session.id(), attrs);
            if (debug != null) {
                debug.println("storePkey created token key for [" +
                                alias +
                                "]");
            }
        } finally {
            token.releaseSession(session);
        }
        storeChain(alias, (X509Certificate[])pke.getCertificateChain());
    }
    private CK_ATTRIBUTE[] getRsaPrivKeyAttrs(String alias,
                                RSAPrivateKey key,
                                X500Principal subject) throws PKCS11Exception {
        CK_ATTRIBUTE[] attrs = null;
        if (key instanceof RSAPrivateCrtKey) {
            if (debug != null) {
                debug.println("creating RSAPrivateCrtKey attrs");
            }
            RSAPrivateCrtKey rsaKey = (RSAPrivateCrtKey)key;
            attrs = new CK_ATTRIBUTE[] {
                ATTR_TOKEN_TRUE,
                ATTR_CLASS_PKEY,
                ATTR_PRIVATE_TRUE,
                new CK_ATTRIBUTE(CKA_KEY_TYPE, CKK_RSA),
                new CK_ATTRIBUTE(CKA_ID, alias),
                new CK_ATTRIBUTE(CKA_MODULUS,
                                rsaKey.getModulus()),
                new CK_ATTRIBUTE(CKA_PRIVATE_EXPONENT,
                                rsaKey.getPrivateExponent()),
                new CK_ATTRIBUTE(CKA_PUBLIC_EXPONENT,
                                rsaKey.getPublicExponent()),
                new CK_ATTRIBUTE(CKA_PRIME_1,
                                rsaKey.getPrimeP()),
                new CK_ATTRIBUTE(CKA_PRIME_2,
                                rsaKey.getPrimeQ()),
                new CK_ATTRIBUTE(CKA_EXPONENT_1,
                                rsaKey.getPrimeExponentP()),
                new CK_ATTRIBUTE(CKA_EXPONENT_2,
                                rsaKey.getPrimeExponentQ()),
                new CK_ATTRIBUTE(CKA_COEFFICIENT,
                                rsaKey.getCrtCoefficient()) };
            attrs = token.getAttributes
                (TemplateManager.O_IMPORT, CKO_PRIVATE_KEY, CKK_RSA, attrs);
        } else {
            if (debug != null) {
                debug.println("creating RSAPrivateKey attrs");
            }
            RSAPrivateKey rsaKey = (RSAPrivateKey)key;
            attrs = new CK_ATTRIBUTE[] {
                ATTR_TOKEN_TRUE,
                ATTR_CLASS_PKEY,
                ATTR_PRIVATE_TRUE,
                new CK_ATTRIBUTE(CKA_KEY_TYPE, CKK_RSA),
                new CK_ATTRIBUTE(CKA_ID, alias),
                new CK_ATTRIBUTE(CKA_MODULUS,
                                rsaKey.getModulus()),
                new CK_ATTRIBUTE(CKA_PRIVATE_EXPONENT,
                                rsaKey.getPrivateExponent()) };
            attrs = token.getAttributes
                (TemplateManager.O_IMPORT, CKO_PRIVATE_KEY, CKK_RSA, attrs);
        }
        return attrs;
    }
    private CK_ATTRIBUTE[] getIdAttributes(PrivateKey privateKey,
            PublicKey publicKey, boolean id, boolean netscapeDb) {
        CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[2];
        if ((id || netscapeDb) == false) {
            return attrs;
        }
        String alg = privateKey.getAlgorithm();
        if (id && alg.equals("RSA") && (publicKey instanceof RSAPublicKey)) {
            BigInteger n = ((RSAPublicKey)publicKey).getModulus();
            attrs[0] = new CK_ATTRIBUTE(CKA_ID, sha1(getMagnitude(n)));
        } else if (alg.equals("DSA") && (publicKey instanceof DSAPublicKey)) {
            BigInteger y = ((DSAPublicKey)publicKey).getY();
            if (id) {
                attrs[0] = new CK_ATTRIBUTE(CKA_ID, sha1(getMagnitude(y)));
            }
            if (netscapeDb) {
                attrs[1] = new CK_ATTRIBUTE(CKA_NETSCAPE_DB, y);
            }
        } else if (alg.equals("DH") && (publicKey instanceof DHPublicKey)) {
            BigInteger y = ((DHPublicKey)publicKey).getY();
            if (id) {
                attrs[0] = new CK_ATTRIBUTE(CKA_ID, sha1(getMagnitude(y)));
            }
            if (netscapeDb) {
                attrs[1] = new CK_ATTRIBUTE(CKA_NETSCAPE_DB, y);
            }
        } else if (alg.equals("EC") && (publicKey instanceof ECPublicKey)) {
            ECPublicKey ecPub = (ECPublicKey)publicKey;
            ECPoint point = ecPub.getW();
            ECParameterSpec params = ecPub.getParams();
            byte[] encodedPoint = ECParameters.encodePoint(point, params.getCurve());
            if (id) {
                attrs[0] = new CK_ATTRIBUTE(CKA_ID, sha1(encodedPoint));
            }
            if (netscapeDb) {
                attrs[1] = new CK_ATTRIBUTE(CKA_NETSCAPE_DB, encodedPoint);
            }
        } else {
            throw new RuntimeException("Unknown key algorithm " + alg);
        }
        return attrs;
    }
    private boolean destroyCert(byte[] cka_id)
                throws PKCS11Exception, KeyStoreException {
        Session session = null;
        try {
            session = token.getOpSession();
            THandle h = getTokenObject(session, ATTR_CLASS_CERT, cka_id, null);
            if (h.type != ATTR_CLASS_CERT) {
                return false;
            }
            token.p11.C_DestroyObject(session.id(), h.handle);
            if (debug != null) {
                debug.println("destroyCert destroyed cert with CKA_ID [" +
                                                getID(cka_id) +
                                                "]");
            }
            return true;
        } finally {
            token.releaseSession(session);
        }
    }
    private boolean destroyChain(byte[] cka_id)
        throws PKCS11Exception, CertificateException, KeyStoreException {
        Session session = null;
        try {
            session = token.getOpSession();
            THandle h = getTokenObject(session, ATTR_CLASS_CERT, cka_id, null);
            if (h.type != ATTR_CLASS_CERT) {
                if (debug != null) {
                    debug.println("destroyChain could not find " +
                        "end entity cert with CKA_ID [0x" +
                        Functions.toHexString(cka_id) +
                        "]");
                }
                return false;
            }
            X509Certificate endCert = loadCert(session, h.handle);
            token.p11.C_DestroyObject(session.id(), h.handle);
            if (debug != null) {
                debug.println("destroyChain destroyed end entity cert " +
                        "with CKA_ID [" +
                        getID(cka_id) +
                        "]");
            }
            X509Certificate next = endCert;
            while (true) {
                if (next.getSubjectX500Principal().equals
                    (next.getIssuerX500Principal())) {
                    break;
                }
                CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] {
                        ATTR_TOKEN_TRUE,
                        ATTR_CLASS_CERT,
                        new CK_ATTRIBUTE(CKA_SUBJECT,
                                  next.getIssuerX500Principal().getEncoded()) };
                long[] ch = findObjects(session, attrs);
                if (ch == null || ch.length == 0) {
                    break;
                } else {
                    if (debug != null && ch.length > 1) {
                        debug.println("destroyChain found " +
                                ch.length +
                                " certificate entries for subject [" +
                                next.getIssuerX500Principal() +
                                "] in token - using first entry");
                    }
                    next = loadCert(session, ch[0]);
                    attrs = new CK_ATTRIBUTE[] {
                        ATTR_TOKEN_TRUE,
                        ATTR_CLASS_CERT,
                        new CK_ATTRIBUTE(CKA_ISSUER,
                                next.getSubjectX500Principal().getEncoded()) };
                    long[] issuers = findObjects(session, attrs);
                    boolean destroyIt = false;
                    if (issuers == null || issuers.length == 0) {
                        destroyIt = true;
                    } else if (issuers.length == 1) {
                        X509Certificate iCert = loadCert(session, issuers[0]);
                        if (next.equals(iCert)) {
                            destroyIt = true;
                        }
                    }
                    if (destroyIt) {
                        token.p11.C_DestroyObject(session.id(), ch[0]);
                        if (debug != null) {
                            debug.println
                                ("destroyChain destroyed cert in chain " +
                                "with subject [" +
                                next.getSubjectX500Principal() + "]");
                        }
                    } else {
                        if (debug != null) {
                            debug.println("destroyChain did not destroy " +
                                "shared cert in chain with subject [" +
                                next.getSubjectX500Principal() + "]");
                        }
                    }
                }
            }
            return true;
        } finally {
            token.releaseSession(session);
        }
    }
    private boolean destroySkey(String alias)
                throws PKCS11Exception, KeyStoreException {
        Session session = null;
        try {
            session = token.getOpSession();
            THandle h = getTokenObject(session, ATTR_CLASS_SKEY, null, alias);
            if (h.type != ATTR_CLASS_SKEY) {
                if (debug != null) {
                    debug.println("destroySkey did not find secret key " +
                        "with CKA_LABEL [" +
                        alias +
                        "]");
                }
                return false;
            }
            token.p11.C_DestroyObject(session.id(), h.handle);
            return true;
        } finally {
            token.releaseSession(session);
        }
    }
    private boolean destroyPkey(byte[] cka_id)
                throws PKCS11Exception, KeyStoreException {
        Session session = null;
        try {
            session = token.getOpSession();
            THandle h = getTokenObject(session, ATTR_CLASS_PKEY, cka_id, null);
            if (h.type != ATTR_CLASS_PKEY) {
                if (debug != null) {
                    debug.println
                        ("destroyPkey did not find private key with CKA_ID [" +
                        getID(cka_id) +
                        "]");
                }
                return false;
            }
            token.p11.C_DestroyObject(session.id(), h.handle);
            return true;
        } finally {
            token.releaseSession(session);
        }
    }
    private String getID(String alias, X509Certificate cert) {
        X500Principal issuer = cert.getIssuerX500Principal();
        BigInteger serialNum = cert.getSerialNumber();
        return alias +
                ALIAS_SEP +
                issuer.getName(X500Principal.CANONICAL) +
                ALIAS_SEP +
                serialNum.toString();
    }
    private static String getID(byte[] bytes) {
        boolean printable = true;
        for (int i = 0; i < bytes.length; i++) {
            if (!DerValue.isPrintableStringChar((char)bytes[i])) {
                printable = false;
                break;
            }
        }
        if (!printable) {
            return "0x" + Functions.toHexString(bytes);
        } else {
            try {
                return new String(bytes, "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                return "0x" + Functions.toHexString(bytes);
            }
        }
    }
    private THandle getTokenObject(Session session,
                                CK_ATTRIBUTE type,
                                byte[] cka_id,
                                String cka_label)
                throws PKCS11Exception, KeyStoreException {
        CK_ATTRIBUTE[] attrs;
        if (type == ATTR_CLASS_SKEY) {
            attrs = new CK_ATTRIBUTE[] {
                        ATTR_SKEY_TOKEN_TRUE,
                        new CK_ATTRIBUTE(CKA_LABEL, cka_label),
                        type };
        } else {
            attrs = new CK_ATTRIBUTE[] {
                        ATTR_TOKEN_TRUE,
                        new CK_ATTRIBUTE(CKA_ID, cka_id),
                        type };
        }
        long[] h = findObjects(session, attrs);
        if (h.length == 0) {
            if (debug != null) {
                if (type == ATTR_CLASS_SKEY) {
                    debug.println("getTokenObject did not find secret key " +
                                "with CKA_LABEL [" +
                                cka_label +
                                "]");
                } else if (type == ATTR_CLASS_CERT) {
                    debug.println
                        ("getTokenObject did not find cert with CKA_ID [" +
                        getID(cka_id) +
                        "]");
                } else {
                    debug.println("getTokenObject did not find private key " +
                        "with CKA_ID [" +
                        getID(cka_id) +
                        "]");
                }
            }
        } else if (h.length == 1) {
            return new THandle(h[0], type);
        } else {
            if (type == ATTR_CLASS_SKEY) {
                ArrayList<THandle> list = new ArrayList<THandle>(h.length);
                for (int i = 0; i < h.length; i++) {
                    CK_ATTRIBUTE[] label = new CK_ATTRIBUTE[]
                                        { new CK_ATTRIBUTE(CKA_LABEL) };
                    token.p11.C_GetAttributeValue(session.id(), h[i], label);
                    if (label[0].pValue != null &&
                        cka_label.equals(new String(label[0].getCharArray()))) {
                        list.add(new THandle(h[i], ATTR_CLASS_SKEY));
                    }
                }
                if (list.size() == 1) {
                    return list.get(0);
                } else {
                    throw new KeyStoreException("invalid KeyStore state: " +
                        "found " +
                        list.size() +
                        " secret keys sharing CKA_LABEL [" +
                        cka_label +
                        "]");
                }
            } else if (type == ATTR_CLASS_CERT) {
                throw new KeyStoreException("invalid KeyStore state: " +
                        "found " +
                        h.length +
                        " certificates sharing CKA_ID " +
                        getID(cka_id));
            } else {
                throw new KeyStoreException("invalid KeyStore state: " +
                        "found " +
                        h.length +
                        " private keys sharing CKA_ID " +
                        getID(cka_id));
            }
        }
        return new THandle(NO_HANDLE, null);
    }
    private boolean mapLabels() throws
                PKCS11Exception, CertificateException, KeyStoreException {
        CK_ATTRIBUTE[] trustedAttr = new CK_ATTRIBUTE[] {
                                new CK_ATTRIBUTE(CKA_TRUSTED) };
        Session session = null;
        try {
            session = token.getOpSession();
            ArrayList<byte[]> pkeyIDs = new ArrayList<byte[]>();
            CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] {
                ATTR_TOKEN_TRUE,
                ATTR_CLASS_PKEY,
            };
            long[] handles = findObjects(session, attrs);
            for (long handle : handles) {
                attrs = new CK_ATTRIBUTE[] { new CK_ATTRIBUTE(CKA_ID) };
                token.p11.C_GetAttributeValue(session.id(), handle, attrs);
                if (attrs[0].pValue != null) {
                    pkeyIDs.add(attrs[0].getByteArray());
                }
            }
            HashMap<String, HashSet<AliasInfo>> certMap =
                                new HashMap<String, HashSet<AliasInfo>>();
            attrs = new CK_ATTRIBUTE[] {
                ATTR_TOKEN_TRUE,
                ATTR_CLASS_CERT,
            };
            handles = findObjects(session, attrs);
            for (long handle : handles) {
                attrs = new CK_ATTRIBUTE[] { new CK_ATTRIBUTE(CKA_LABEL) };
                String cka_label = null;
                byte[] cka_id = null;
                try {
                    token.p11.C_GetAttributeValue(session.id(), handle, attrs);
                    if (attrs[0].pValue != null) {
                        cka_label = new String(attrs[0].getCharArray());
                    }
                } catch (PKCS11Exception pe) {
                    if (pe.getErrorCode() != CKR_ATTRIBUTE_TYPE_INVALID) {
                        throw pe;
                    }
                }
                attrs = new CK_ATTRIBUTE[] { new CK_ATTRIBUTE(CKA_ID) };
                token.p11.C_GetAttributeValue(session.id(), handle, attrs);
                if (attrs[0].pValue == null) {
                    if (cka_label == null) {
                        continue;
                    }
                } else {
                    if (cka_label == null) {
                        cka_label = getID(attrs[0].getByteArray());
                    }
                    cka_id = attrs[0].getByteArray();
                }
                X509Certificate cert = loadCert(session, handle);
                boolean cka_trusted = false;
                if (useSecmodTrust) {
                    cka_trusted = Secmod.getInstance().isTrusted(cert, nssTrustType);
                } else {
                    if (CKA_TRUSTED_SUPPORTED) {
                        try {
                            token.p11.C_GetAttributeValue
                                    (session.id(), handle, trustedAttr);
                            cka_trusted = trustedAttr[0].getBoolean();
                        } catch (PKCS11Exception pe) {
                            if (pe.getErrorCode() == CKR_ATTRIBUTE_TYPE_INVALID) {
                                CKA_TRUSTED_SUPPORTED = false;
                                if (debug != null) {
                                    debug.println
                                            ("CKA_TRUSTED attribute not supported");
                                }
                            }
                        }
                    }
                }
                HashSet<AliasInfo> infoSet = certMap.get(cka_label);
                if (infoSet == null) {
                    infoSet = new HashSet<AliasInfo>(2);
                    certMap.put(cka_label, infoSet);
                }
                infoSet.add(new AliasInfo
                                (cka_label,
                                cka_id,
                                cka_trusted,
                                cert));
            }
            HashMap<String, AliasInfo> sKeyMap =
                    new HashMap<String, AliasInfo>();
            attrs = new CK_ATTRIBUTE[] {
                ATTR_SKEY_TOKEN_TRUE,
                ATTR_CLASS_SKEY,
            };
            handles = findObjects(session, attrs);
            for (long handle : handles) {
                attrs = new CK_ATTRIBUTE[] { new CK_ATTRIBUTE(CKA_LABEL) };
                token.p11.C_GetAttributeValue(session.id(), handle, attrs);
                if (attrs[0].pValue != null) {
                    String cka_label = new String(attrs[0].getCharArray());
                    if (sKeyMap.get(cka_label) == null) {
                        sKeyMap.put(cka_label, new AliasInfo(cka_label));
                    } else {
                        throw new KeyStoreException("invalid KeyStore state: " +
                                "found multiple secret keys sharing same " +
                                "CKA_LABEL [" +
                                cka_label +
                                "]");
                    }
                }
            }
            ArrayList<AliasInfo> matchedCerts =
                                mapPrivateKeys(pkeyIDs, certMap);
            boolean sharedLabel = mapCerts(matchedCerts, certMap);
            mapSecretKeys(sKeyMap);
            return sharedLabel;
        } finally {
            token.releaseSession(session);
        }
    }
    private ArrayList<AliasInfo> mapPrivateKeys(ArrayList<byte[]> pkeyIDs,
                        HashMap<String, HashSet<AliasInfo>> certMap)
                throws PKCS11Exception, CertificateException {
        aliasMap = new HashMap<String, AliasInfo>();
        ArrayList<AliasInfo> matchedCerts = new ArrayList<AliasInfo>();
        for (byte[] pkeyID : pkeyIDs) {
            boolean foundMatch = false;
            Set<String> certLabels = certMap.keySet();
            for (String certLabel : certLabels) {
                HashSet<AliasInfo> infoSet = certMap.get(certLabel);
                for (AliasInfo aliasInfo : infoSet) {
                    if (Arrays.equals(pkeyID, aliasInfo.id)) {
                        if (infoSet.size() == 1) {
                            aliasInfo.matched = true;
                            aliasMap.put(certLabel, aliasInfo);
                        } else {
                            aliasInfo.matched = true;
                            aliasMap.put(getID(certLabel, aliasInfo.cert),
                                        aliasInfo);
                        }
                        matchedCerts.add(aliasInfo);
                        foundMatch = true;
                        break;
                    }
                }
                if (foundMatch) {
                    break;
                }
            }
            if (!foundMatch) {
                if (debug != null) {
                    debug.println
                        ("did not find match for private key with CKA_ID [" +
                        getID(pkeyID) +
                        "] (ignoring entry)");
                }
            }
        }
        return matchedCerts;
    }
    private boolean mapCerts(ArrayList<AliasInfo> matchedCerts,
                        HashMap<String, HashSet<AliasInfo>> certMap)
                throws PKCS11Exception, CertificateException {
        for (AliasInfo aliasInfo : matchedCerts) {
            Session session = null;
            try {
                session = token.getOpSession();
                aliasInfo.chain = loadChain(session, aliasInfo.cert);
            } finally {
                token.releaseSession(session);
            }
        }
        boolean sharedLabel = false;
        Set<String> certLabels = certMap.keySet();
        for (String certLabel : certLabels) {
            HashSet<AliasInfo> infoSet = certMap.get(certLabel);
            for (AliasInfo aliasInfo : infoSet) {
                if (aliasInfo.matched == true) {
                    aliasInfo.trusted = false;
                    continue;
                }
                if (CKA_TRUSTED_SUPPORTED) {
                    if (aliasInfo.trusted) {
                        if (mapTrustedCert
                                (certLabel, aliasInfo, infoSet) == true) {
                            sharedLabel = true;
                        }
                    }
                    continue;
                }
            }
        }
        return sharedLabel;
    }
    private boolean mapTrustedCert(String certLabel,
                                AliasInfo aliasInfo,
                                HashSet<AliasInfo> infoSet) {
        boolean sharedLabel = false;
        aliasInfo.type = ATTR_CLASS_CERT;
        aliasInfo.trusted = true;
        if (infoSet.size() == 1) {
            aliasMap.put(certLabel, aliasInfo);
        } else {
            sharedLabel = true;
            aliasMap.put(getID(certLabel, aliasInfo.cert), aliasInfo);
        }
        return sharedLabel;
    }
    private void mapSecretKeys(HashMap<String, AliasInfo> sKeyMap)
                throws KeyStoreException {
        for (String label : sKeyMap.keySet()) {
            if (aliasMap.containsKey(label)) {
                throw new KeyStoreException("invalid KeyStore state: " +
                        "found secret key sharing CKA_LABEL [" +
                        label +
                        "] with another token object");
            }
        }
        aliasMap.putAll(sKeyMap);
    }
    private void dumpTokenMap() {
        Set<String> aliases = aliasMap.keySet();
        System.out.println("Token Alias Map:");
        if (aliases.size() == 0) {
            System.out.println("  [empty]");
        } else {
            for (String s : aliases) {
                System.out.println("  " + s + aliasMap.get(s));
            }
        }
    }
    private void checkWrite() throws KeyStoreException {
        if (writeDisabled) {
            throw new KeyStoreException
                ("This PKCS11KeyStore does not support write capabilities");
        }
    }
    private final static long[] LONG0 = new long[0];
    private static long[] findObjects(Session session, CK_ATTRIBUTE[] attrs)
            throws PKCS11Exception {
        Token token = session.token;
        long[] handles = LONG0;
        token.p11.C_FindObjectsInit(session.id(), attrs);
        while (true) {
            long[] h = token.p11.C_FindObjects(session.id(), FINDOBJECTS_MAX);
            if (h.length == 0) {
                break;
            }
            handles = P11Util.concat(handles, h);
        }
        token.p11.C_FindObjectsFinal(session.id());
        return handles;
    }
}
