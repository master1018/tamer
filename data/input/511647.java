public abstract class KeyStoreSpi {
    public abstract Key engineGetKey(String alias, char[] password)
            throws NoSuchAlgorithmException, UnrecoverableKeyException;
    public abstract Certificate[] engineGetCertificateChain(String alias);
    public abstract Certificate engineGetCertificate(String alias);
    public abstract Date engineGetCreationDate(String alias);
    public abstract void engineSetKeyEntry(String alias, Key key,
            char[] password, Certificate[] chain) throws KeyStoreException;
    public abstract void engineSetKeyEntry(String alias, byte[] key,
            Certificate[] chain) throws KeyStoreException;
    public abstract void engineSetCertificateEntry(String alias,
            Certificate cert) throws KeyStoreException;
    public abstract void engineDeleteEntry(String alias)
            throws KeyStoreException;
    public abstract Enumeration<String> engineAliases();
    public abstract boolean engineContainsAlias(String alias);
    public abstract int engineSize();
    public abstract boolean engineIsKeyEntry(String alias);
    public abstract boolean engineIsCertificateEntry(String alias);
    public abstract String engineGetCertificateAlias(Certificate cert);
    public abstract void engineStore(OutputStream stream, char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException;
    public void engineStore(KeyStore.LoadStoreParameter param)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        throw new UnsupportedOperationException(Messages.getString("security.33")); 
    }
    public abstract void engineLoad(InputStream stream, char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException;
    public void engineLoad(KeyStore.LoadStoreParameter param)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        if (param == null) {
            engineLoad(null, null);
            return;
        }
        char[] pwd;
        KeyStore.ProtectionParameter pp = param.getProtectionParameter();
        if (pp instanceof KeyStore.PasswordProtection) {
            try {
                pwd = ((KeyStore.PasswordProtection) pp).getPassword();
                engineLoad(null, pwd);
                return;
            } catch (IllegalStateException e) {
                throw new IllegalArgumentException(e);
            }
        }
        if (pp instanceof KeyStore.CallbackHandlerProtection) {
            try {
                pwd = getPasswordFromCallBack(pp);
                engineLoad(null, pwd);
                return;
            } catch (UnrecoverableEntryException e) {
                throw new IllegalArgumentException(e);
            }
        }
        throw new UnsupportedOperationException(
                Messages.getString("security.35")); 
    }
    public KeyStore.Entry engineGetEntry(String alias,
            KeyStore.ProtectionParameter protParam) throws KeyStoreException,
            NoSuchAlgorithmException, UnrecoverableEntryException {
        if (!engineContainsAlias(alias)) {
            return null;
        }
        if (engineIsCertificateEntry(alias)) {
            return new KeyStore.TrustedCertificateEntry(
                    engineGetCertificate(alias));
        }
        char[] passW = null;
        if (protParam != null) {
            if (protParam instanceof KeyStore.PasswordProtection) {
                try {
                    passW = ((KeyStore.PasswordProtection) protParam)
                            .getPassword();
                } catch (IllegalStateException ee) {
                    throw new KeyStoreException(Messages.getString("security.36"), ee); 
                }
            } else if (protParam instanceof KeyStore.CallbackHandlerProtection) {
                passW = getPasswordFromCallBack(protParam);
            } else {
                throw new UnrecoverableEntryException(
                        Messages.getString("security.37", 
                                protParam.toString()));
            }
        }
        if (engineIsKeyEntry(alias)) {
            try {
                Key key = engineGetKey(alias, passW);
                if (key instanceof PrivateKey) {
                    return new KeyStore.PrivateKeyEntry((PrivateKey) key,
                            engineGetCertificateChain(alias));
                }
                if (key instanceof SecretKey) {
                    return new KeyStore.SecretKeyEntry((SecretKey) key);
                }
            } catch (UnrecoverableKeyException e) {
                throw new KeyStoreException(e);
            }
        }
        throw new NoSuchAlgorithmException(Messages.getString("security.38")); 
    }
    public void engineSetEntry(String alias, KeyStore.Entry entry,
            KeyStore.ProtectionParameter protParam) throws KeyStoreException {
        if (entry == null) {
            throw new KeyStoreException(Messages.getString("security.39")); 
        }
        if (engineContainsAlias(alias)) {
            engineDeleteEntry(alias);
        }
        if (entry instanceof KeyStore.TrustedCertificateEntry) {
            KeyStore.TrustedCertificateEntry trE = (KeyStore.TrustedCertificateEntry) entry;
            engineSetCertificateEntry(alias, trE.getTrustedCertificate());
            return;
        }
        char[] passW = null;
        if (protParam instanceof KeyStore.PasswordProtection) {
            try {
                passW = ((KeyStore.PasswordProtection) protParam).getPassword();
            } catch (IllegalStateException ee) {
                throw new KeyStoreException(Messages.getString("security.36"), ee); 
            }
        } else {
            if (protParam instanceof KeyStore.CallbackHandlerProtection) {
                try {
                    passW = getPasswordFromCallBack(protParam);
                } catch (Exception e) {
                    throw new KeyStoreException(e);
                }
            } else {
                throw new KeyStoreException(
                        Messages.getString("security.3A")); 
            }
        }
        if (entry instanceof KeyStore.PrivateKeyEntry) {
            KeyStore.PrivateKeyEntry prE = (KeyStore.PrivateKeyEntry) entry;
            engineSetKeyEntry(alias, prE.getPrivateKey(), passW, prE
                    .getCertificateChain());
            return;
        }
        if (entry instanceof KeyStore.SecretKeyEntry) {
            KeyStore.SecretKeyEntry skE = (KeyStore.SecretKeyEntry) entry;
            engineSetKeyEntry(alias, skE.getSecretKey(), passW, null);
            return;
        }
        throw new KeyStoreException(
                Messages.getString("security.3B", entry.toString())); 
    }
    public boolean engineEntryInstanceOf(String alias,
            Class<? extends KeyStore.Entry> entryClass) {
        if (!engineContainsAlias(alias)) {
            return false;
        }
        try {
            if (engineIsCertificateEntry(alias)) {
                return entryClass
                        .isAssignableFrom(Class
                                .forName("java.security.KeyStore$TrustedCertificateEntry")); 
            }
            if (engineIsKeyEntry(alias)) {
                if (entryClass.isAssignableFrom(Class
                        .forName("java.security.KeyStore$PrivateKeyEntry"))) { 
                    return engineGetCertificate(alias) != null;
                }
                if (entryClass.isAssignableFrom(Class
                        .forName("java.security.KeyStore$SecretKeyEntry"))) { 
                    return engineGetCertificate(alias) == null;
                }
            }
        } catch (ClassNotFoundException ignore) {}
        return false;
    }
    static char[] getPasswordFromCallBack(KeyStore.ProtectionParameter protParam)
            throws UnrecoverableEntryException {
        if (protParam == null) {
            return null;
        }
        if (!(protParam instanceof KeyStore.CallbackHandlerProtection)) {
            throw new UnrecoverableEntryException(
                    Messages.getString("security.3C")); 
        }
        String clName = Security
                .getProperty("auth.login.defaultCallbackHandler"); 
        if (clName == null) {
            throw new UnrecoverableEntryException(
                    Messages.getString("security.3D")); 
        }
        try {
            Class<?> cl = Class.forName(clName);
            CallbackHandler cbHand = (CallbackHandler) cl.newInstance();
            PasswordCallback[] pwCb = { new PasswordCallback("password: ", true) }; 
            cbHand.handle(pwCb);
            return pwCb[0].getPassword();
        } catch (Exception e) {
            throw new UnrecoverableEntryException(e.toString());
        }
    }
}
