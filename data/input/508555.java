public class KeyManagerImpl extends X509ExtendedKeyManager {
    private final Hashtable<String, PrivateKeyEntry> hash;
    public KeyManagerImpl(KeyStore keyStore, char[] pwd) {
        super();
        this.hash = new Hashtable<String, PrivateKeyEntry>();
        final Enumeration<String> aliases;
        try {
            aliases = keyStore.aliases();
        } catch (KeyStoreException e) {
            return;
        }
        for (; aliases.hasMoreElements();) {
            final String alias = aliases.nextElement();
            try {
                if (keyStore.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class)) {
                    final KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore
                            .getEntry(alias, new KeyStore.PasswordProtection(pwd));
                    hash.put(alias, entry);
                }
            } catch (KeyStoreException e) {
                continue;
            } catch (UnrecoverableEntryException e) {
                continue;
            } catch (NoSuchAlgorithmException e) {
                continue;
            }
        }
    }
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        final String[] al = chooseAlias(keyType, issuers);
        return (al == null ? null : al[0]);
    }
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        final String[] al = chooseAlias(new String[] { keyType }, issuers);
        return (al == null ? null : al[0]);
    }
    public X509Certificate[] getCertificateChain(String alias) {
        if (alias == null) {
            return null;
        }
        if (hash.containsKey(alias)) {
            Certificate[] certs = hash.get(alias).getCertificateChain();
            if (certs[0] instanceof X509Certificate) {
                X509Certificate[] xcerts = new X509Certificate[certs.length];
                for (int i = 0; i < certs.length; i++) {
                    xcerts[i] = (X509Certificate) certs[i];
                }
                return xcerts;
            }
        }
        return null;
    }
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return chooseAlias(new String[] { keyType }, issuers);
    }
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return chooseAlias(new String[] { keyType }, issuers);
    }
    public PrivateKey getPrivateKey(String alias) {
        if (alias == null) {
            return null;
        }
        if (hash.containsKey(alias)) {
            return hash.get(alias).getPrivateKey();
        }
        return null;
    }
    @Override
    public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
        final String[] al = chooseAlias(keyType, issuers);
        return (al == null ? null : al[0]);
    }
    @Override
    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
        final String[] al = chooseAlias(new String[] { keyType }, issuers);
        return (al == null ? null : al[0]);
    }
    private String[] chooseAlias(String[] keyType, Principal[] issuers) {
        if (keyType == null || keyType.length == 0) {
            return null;
        }
        Vector<String> found = new Vector<String>();
        for (Enumeration<String> aliases = hash.keys(); aliases.hasMoreElements();) {
            final String alias = aliases.nextElement();
            final KeyStore.PrivateKeyEntry entry = hash.get(alias);
            final Certificate[] certs = entry.getCertificateChain();
            final String alg = certs[0].getPublicKey().getAlgorithm();
            for (int i = 0; i < keyType.length; i++) {
                if (alg.equals(keyType[i])) {
                    if (issuers != null && issuers.length != 0) {
                        loop: for (int ii = 0; ii < certs.length; ii++) {
                            if (certs[ii] instanceof X509Certificate) {
                                X500Principal issuer = ((X509Certificate) certs[ii])
                                        .getIssuerX500Principal();
                                for (int iii = 0; iii < issuers.length; iii++) {
                                    if (issuer.equals(issuers[iii])) {
                                        found.add(alias);
                                        break loop;
                                    }
                                }
                            }
                        }
                    } else {
                        found.add(alias);
                    }
                }
            }
        }
        if (!found.isEmpty()) {
            return found.toArray(new String[found.size()]);
        }
        return null;
    }
}
