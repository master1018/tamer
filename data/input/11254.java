final class X509KeyManagerImpl extends X509ExtendedKeyManager
        implements X509KeyManager {
    private static final Debug debug = Debug.getInstance("ssl");
    private final static boolean useDebug =
                            (debug != null) && Debug.isOn("keymanager");
    private static Date verificationDate;
    private final List<Builder> builders;
    private final AtomicLong uidCounter;
    private final Map<String,Reference<PrivateKeyEntry>> entryCacheMap;
    X509KeyManagerImpl(Builder builder) {
        this(Collections.singletonList(builder));
    }
    X509KeyManagerImpl(List<Builder> builders) {
        this.builders = builders;
        uidCounter = new AtomicLong();
        entryCacheMap = Collections.synchronizedMap
                        (new SizedMap<String,Reference<PrivateKeyEntry>>());
    }
    private static class SizedMap<K,V> extends LinkedHashMap<K,V> {
        @Override protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
            return size() > 10;
        }
    }
    public X509Certificate[] getCertificateChain(String alias) {
        PrivateKeyEntry entry = getEntry(alias);
        return entry == null ? null :
                (X509Certificate[])entry.getCertificateChain();
    }
    public PrivateKey getPrivateKey(String alias) {
        PrivateKeyEntry entry = getEntry(alias);
        return entry == null ? null : entry.getPrivateKey();
    }
    public String chooseClientAlias(String[] keyTypes, Principal[] issuers,
            Socket socket) {
        return chooseAlias(getKeyTypes(keyTypes), issuers, CheckType.CLIENT,
                        getAlgorithmConstraints(socket));
    }
    public String chooseEngineClientAlias(String[] keyTypes,
            Principal[] issuers, SSLEngine engine) {
        return chooseAlias(getKeyTypes(keyTypes), issuers, CheckType.CLIENT,
                        getAlgorithmConstraints(engine));
    }
    public String chooseServerAlias(String keyType,
            Principal[] issuers, Socket socket) {
        return chooseAlias(getKeyTypes(keyType), issuers, CheckType.SERVER,
                        getAlgorithmConstraints(socket));
    }
    public String chooseEngineServerAlias(String keyType,
            Principal[] issuers, SSLEngine engine) {
        return chooseAlias(getKeyTypes(keyType), issuers, CheckType.SERVER,
                        getAlgorithmConstraints(engine));
    }
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return getAliases(keyType, issuers, CheckType.CLIENT, null);
    }
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return getAliases(keyType, issuers, CheckType.SERVER, null);
    }
    private AlgorithmConstraints getAlgorithmConstraints(Socket socket) {
        if (socket != null && socket.isConnected() &&
                                        socket instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket)socket;
            SSLSession session = sslSocket.getHandshakeSession();
            if (session != null) {
                ProtocolVersion protocolVersion =
                    ProtocolVersion.valueOf(session.getProtocol());
                if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    String[] peerSupportedSignAlgs = null;
                    if (session instanceof ExtendedSSLSession) {
                        ExtendedSSLSession extSession =
                            (ExtendedSSLSession)session;
                        peerSupportedSignAlgs =
                            extSession.getPeerSupportedSignatureAlgorithms();
                    }
                    return new SSLAlgorithmConstraints(
                        sslSocket, peerSupportedSignAlgs, true);
                }
            }
            return new SSLAlgorithmConstraints(sslSocket, true);
        }
        return new SSLAlgorithmConstraints((SSLSocket)null, true);
    }
    private AlgorithmConstraints getAlgorithmConstraints(SSLEngine engine) {
        if (engine != null) {
            SSLSession session = engine.getHandshakeSession();
            if (session != null) {
                ProtocolVersion protocolVersion =
                    ProtocolVersion.valueOf(session.getProtocol());
                if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    String[] peerSupportedSignAlgs = null;
                    if (session instanceof ExtendedSSLSession) {
                        ExtendedSSLSession extSession =
                            (ExtendedSSLSession)session;
                        peerSupportedSignAlgs =
                            extSession.getPeerSupportedSignatureAlgorithms();
                    }
                    return new SSLAlgorithmConstraints(
                        engine, peerSupportedSignAlgs, true);
                }
            }
        }
        return new SSLAlgorithmConstraints(engine, true);
    }
    private String makeAlias(EntryStatus entry) {
        return uidCounter.incrementAndGet() + "." + entry.builderIndex + "."
                + entry.alias;
    }
    private PrivateKeyEntry getEntry(String alias) {
        if (alias == null) {
            return null;
        }
        Reference<PrivateKeyEntry> ref = entryCacheMap.get(alias);
        PrivateKeyEntry entry = (ref != null) ? ref.get() : null;
        if (entry != null) {
            return entry;
        }
        int firstDot = alias.indexOf('.');
        int secondDot = alias.indexOf('.', firstDot + 1);
        if ((firstDot == -1) || (secondDot == firstDot)) {
            return null;
        }
        try {
            int builderIndex = Integer.parseInt
                                (alias.substring(firstDot + 1, secondDot));
            String keyStoreAlias = alias.substring(secondDot + 1);
            Builder builder = builders.get(builderIndex);
            KeyStore ks = builder.getKeyStore();
            Entry newEntry = ks.getEntry
                    (keyStoreAlias, builder.getProtectionParameter(alias));
            if (newEntry instanceof PrivateKeyEntry == false) {
                return null;
            }
            entry = (PrivateKeyEntry)newEntry;
            entryCacheMap.put(alias, new SoftReference(entry));
            return entry;
        } catch (Exception e) {
            return null;
        }
    }
    private static class KeyType {
        final String keyAlgorithm;
        final String sigKeyAlgorithm;
        KeyType(String algorithm) {
            int k = algorithm.indexOf("_");
            if (k == -1) {
                keyAlgorithm = algorithm;
                sigKeyAlgorithm = null;
            } else {
                keyAlgorithm = algorithm.substring(0, k);
                sigKeyAlgorithm = algorithm.substring(k + 1);
            }
        }
        boolean matches(Certificate[] chain) {
            if (!chain[0].getPublicKey().getAlgorithm().equals(keyAlgorithm)) {
                return false;
            }
            if (sigKeyAlgorithm == null) {
                return true;
            }
            if (chain.length > 1) {
                return sigKeyAlgorithm.equals(
                        chain[1].getPublicKey().getAlgorithm());
            } else {
                X509Certificate issuer = (X509Certificate)chain[0];
                String sigAlgName = issuer.getSigAlgName().toUpperCase(ENGLISH);
                String pattern = "WITH" + sigKeyAlgorithm.toUpperCase(ENGLISH);
                return sigAlgName.contains(pattern);
            }
        }
    }
    private static List<KeyType> getKeyTypes(String ... keyTypes) {
        if ((keyTypes == null) ||
                (keyTypes.length == 0) || (keyTypes[0] == null)) {
            return null;
        }
        List<KeyType> list = new ArrayList<>(keyTypes.length);
        for (String keyType : keyTypes) {
            list.add(new KeyType(keyType));
        }
        return list;
    }
    private String chooseAlias(List<KeyType> keyTypeList, Principal[] issuers,
            CheckType checkType, AlgorithmConstraints constraints) {
        if (keyTypeList == null || keyTypeList.size() == 0) {
            return null;
        }
        Set<Principal> issuerSet = getIssuerSet(issuers);
        List<EntryStatus> allResults = null;
        for (int i = 0, n = builders.size(); i < n; i++) {
            try {
                List<EntryStatus> results = getAliases(i, keyTypeList,
                                    issuerSet, false, checkType, constraints);
                if (results != null) {
                    EntryStatus status = results.get(0);
                    if (status.checkResult == CheckResult.OK) {
                        if (useDebug) {
                            debug.println("KeyMgr: choosing key: " + status);
                        }
                        return makeAlias(status);
                    }
                    if (allResults == null) {
                        allResults = new ArrayList<EntryStatus>();
                    }
                    allResults.addAll(results);
                }
            } catch (Exception e) {
            }
        }
        if (allResults == null) {
            if (useDebug) {
                debug.println("KeyMgr: no matching key found");
            }
            return null;
        }
        Collections.sort(allResults);
        if (useDebug) {
            debug.println("KeyMgr: no good matching key found, "
                        + "returning best match out of:");
            debug.println(allResults.toString());
        }
        return makeAlias(allResults.get(0));
    }
    public String[] getAliases(String keyType, Principal[] issuers,
            CheckType checkType, AlgorithmConstraints constraints) {
        if (keyType == null) {
            return null;
        }
        Set<Principal> issuerSet = getIssuerSet(issuers);
        List<KeyType> keyTypeList = getKeyTypes(keyType);
        List<EntryStatus> allResults = null;
        for (int i = 0, n = builders.size(); i < n; i++) {
            try {
                List<EntryStatus> results = getAliases(i, keyTypeList,
                                    issuerSet, true, checkType, constraints);
                if (results != null) {
                    if (allResults == null) {
                        allResults = new ArrayList<EntryStatus>();
                    }
                    allResults.addAll(results);
                }
            } catch (Exception e) {
            }
        }
        if (allResults == null || allResults.size() == 0) {
            if (useDebug) {
                debug.println("KeyMgr: no matching alias found");
            }
            return null;
        }
        Collections.sort(allResults);
        if (useDebug) {
            debug.println("KeyMgr: getting aliases: " + allResults);
        }
        return toAliases(allResults);
    }
    private String[] toAliases(List<EntryStatus> results) {
        String[] s = new String[results.size()];
        int i = 0;
        for (EntryStatus result : results) {
            s[i++] = makeAlias(result);
        }
        return s;
    }
    private Set<Principal> getIssuerSet(Principal[] issuers) {
        if ((issuers != null) && (issuers.length != 0)) {
            return new HashSet<>(Arrays.asList(issuers));
        } else {
            return null;
        }
    }
    private static class EntryStatus implements Comparable<EntryStatus> {
        final int builderIndex;
        final int keyIndex;
        final String alias;
        final CheckResult checkResult;
        EntryStatus(int builderIndex, int keyIndex, String alias,
                Certificate[] chain, CheckResult checkResult) {
            this.builderIndex = builderIndex;
            this.keyIndex = keyIndex;
            this.alias = alias;
            this.checkResult = checkResult;
        }
        public int compareTo(EntryStatus other) {
            int result = this.checkResult.compareTo(other.checkResult);
            return (result == 0) ? (this.keyIndex - other.keyIndex) : result;
        }
        public String toString() {
            String s = alias + " (verified: " + checkResult + ")";
            if (builderIndex == 0) {
                return s;
            } else {
                return "Builder #" + builderIndex + ", alias: " + s;
            }
        }
    }
    private static enum CheckType {
        NONE(Collections.<String>emptySet()),
        CLIENT(new HashSet<String>(Arrays.asList(new String[] {
            "2.5.29.37.0", "1.3.6.1.5.5.7.3.2" }))),
        SERVER(new HashSet<String>(Arrays.asList(new String[] {
            "2.5.29.37.0", "1.3.6.1.5.5.7.3.1", "2.16.840.1.113730.4.1",
            "1.3.6.1.4.1.311.10.3.3" })));
        final Set<String> validEku;
        CheckType(Set<String> validEku) {
            this.validEku = validEku;
        }
        private static boolean getBit(boolean[] keyUsage, int bit) {
            return (bit < keyUsage.length) && keyUsage[bit];
        }
        CheckResult check(X509Certificate cert, Date date) {
            if (this == NONE) {
                return CheckResult.OK;
            }
            try {
                List<String> certEku = cert.getExtendedKeyUsage();
                if ((certEku != null) &&
                        Collections.disjoint(validEku, certEku)) {
                    return CheckResult.EXTENSION_MISMATCH;
                }
                boolean[] ku = cert.getKeyUsage();
                if (ku != null) {
                    String algorithm = cert.getPublicKey().getAlgorithm();
                    boolean kuSignature = getBit(ku, 0);
                    if (algorithm.equals("RSA")) {
                        if (kuSignature == false) {
                            if ((this == CLIENT) || (getBit(ku, 2) == false)) {
                                return CheckResult.EXTENSION_MISMATCH;
                            }
                        }
                    } else if (algorithm.equals("DSA")) {
                        if (kuSignature == false) {
                            return CheckResult.EXTENSION_MISMATCH;
                        }
                    } else if (algorithm.equals("DH")) {
                        if (getBit(ku, 4) == false) {
                            return CheckResult.EXTENSION_MISMATCH;
                        }
                    } else if (algorithm.equals("EC")) {
                        if (kuSignature == false) {
                            return CheckResult.EXTENSION_MISMATCH;
                        }
                        if ((this == SERVER) && (getBit(ku, 4) == false)) {
                            return CheckResult.EXTENSION_MISMATCH;
                        }
                    }
                }
            } catch (CertificateException e) {
                return CheckResult.EXTENSION_MISMATCH;
            }
            try {
                cert.checkValidity(date);
                return CheckResult.OK;
            } catch (CertificateException e) {
                return CheckResult.EXPIRED;
            }
        }
    }
    private static enum CheckResult {
        OK,                     
        EXPIRED,                
        EXTENSION_MISMATCH,     
    }
    private List<EntryStatus> getAliases(int builderIndex,
            List<KeyType> keyTypes, Set<Principal> issuerSet,
            boolean findAll, CheckType checkType,
            AlgorithmConstraints constraints) throws Exception {
        Builder builder = builders.get(builderIndex);
        KeyStore ks = builder.getKeyStore();
        List<EntryStatus> results = null;
        Date date = verificationDate;
        boolean preferred = false;
        for (Enumeration<String> e = ks.aliases(); e.hasMoreElements(); ) {
            String alias = e.nextElement();
            if (ks.isKeyEntry(alias) == false) {
                continue;
            }
            Certificate[] chain = ks.getCertificateChain(alias);
            if ((chain == null) || (chain.length == 0)) {
                continue;
            }
            boolean incompatible = false;
            for (Certificate cert : chain) {
                if (cert instanceof X509Certificate == false) {
                    incompatible = true;
                    break;
                }
            }
            if (incompatible) {
                continue;
            }
            int keyIndex = -1;
            int j = 0;
            for (KeyType keyType : keyTypes) {
                if (keyType.matches(chain)) {
                    keyIndex = j;
                    break;
                }
                j++;
            }
            if (keyIndex == -1) {
                if (useDebug) {
                    debug.println("Ignoring alias " + alias
                                + ": key algorithm does not match");
                }
                continue;
            }
            if (issuerSet != null) {
                boolean found = false;
                for (Certificate cert : chain) {
                    X509Certificate xcert = (X509Certificate)cert;
                    if (issuerSet.contains(xcert.getIssuerX500Principal())) {
                        found = true;
                        break;
                    }
                }
                if (found == false) {
                    if (useDebug) {
                        debug.println("Ignoring alias " + alias
                                    + ": issuers do not match");
                    }
                    continue;
                }
            }
            if (constraints != null &&
                    !conformsToAlgorithmConstraints(constraints, chain)) {
                if (useDebug) {
                    debug.println("Ignoring alias " + alias +
                            ": certificate list does not conform to " +
                            "algorithm constraints");
                }
                continue;
            }
            if (date == null) {
                date = new Date();
            }
            CheckResult checkResult =
                    checkType.check((X509Certificate)chain[0], date);
            EntryStatus status =
                    new EntryStatus(builderIndex, keyIndex,
                                        alias, chain, checkResult);
            if (!preferred && checkResult == CheckResult.OK && keyIndex == 0) {
                preferred = true;
            }
            if (preferred && (findAll == false)) {
                return Collections.singletonList(status);
            } else {
                if (results == null) {
                    results = new ArrayList<EntryStatus>();
                }
                results.add(status);
            }
        }
        return results;
    }
    private static boolean conformsToAlgorithmConstraints(
            AlgorithmConstraints constraints, Certificate[] chain) {
        AlgorithmChecker checker = new AlgorithmChecker(constraints);
        try {
            checker.init(false);
        } catch (CertPathValidatorException cpve) {
            return false;
        }
        for (int i = chain.length - 1; i >= 0; i--) {
            Certificate cert = chain[i];
            try {
                checker.check(cert, Collections.<String>emptySet());
            } catch (CertPathValidatorException cpve) {
                return false;
            }
        }
        return true;
    }
}
