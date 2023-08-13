final class CipherSuiteList {
    private final Collection<CipherSuite> cipherSuites;
    private String[] suiteNames;
    private volatile Boolean containsEC;
    CipherSuiteList(Collection<CipherSuite> cipherSuites) {
        this.cipherSuites = cipherSuites;
    }
    CipherSuiteList(CipherSuite suite) {
        cipherSuites = new ArrayList<CipherSuite>(1);
        cipherSuites.add(suite);
    }
    CipherSuiteList(String[] names) {
        if (names == null) {
            throw new IllegalArgumentException("CipherSuites may not be null");
        }
        cipherSuites = new ArrayList<CipherSuite>(names.length);
        boolean refreshed = false;
        for (int i = 0; i < names.length; i++) {
            String suiteName = names[i];
            CipherSuite suite = CipherSuite.valueOf(suiteName);
            if (suite.isAvailable() == false) {
                if (refreshed == false) {
                    clearAvailableCache();
                    refreshed = true;
                }
                if (suite.isAvailable() == false) {
                    throw new IllegalArgumentException("Cannot support "
                        + suiteName + " with currently installed providers");
                }
            }
            cipherSuites.add(suite);
        }
    }
    CipherSuiteList(HandshakeInStream in) throws IOException {
        byte[] bytes = in.getBytes16();
        if ((bytes.length & 1) != 0) {
            throw new SSLException("Invalid ClientHello message");
        }
        cipherSuites = new ArrayList<CipherSuite>(bytes.length >> 1);
        for (int i = 0; i < bytes.length; i += 2) {
            cipherSuites.add(CipherSuite.valueOf(bytes[i], bytes[i+1]));
        }
    }
    boolean contains(CipherSuite suite) {
        return cipherSuites.contains(suite);
    }
    boolean containsEC() {
        if (containsEC == null) {
            for (CipherSuite c : cipherSuites) {
                switch (c.keyExchange) {
                case K_ECDH_ECDSA:
                case K_ECDH_RSA:
                case K_ECDHE_ECDSA:
                case K_ECDHE_RSA:
                case K_ECDH_ANON:
                    containsEC = true;
                    return true;
                default:
                    break;
                }
            }
            containsEC = false;
        }
        return containsEC;
    }
    Iterator<CipherSuite> iterator() {
        return cipherSuites.iterator();
    }
    Collection<CipherSuite> collection() {
        return cipherSuites;
    }
    int size() {
        return cipherSuites.size();
    }
    synchronized String[] toStringArray() {
        if (suiteNames == null) {
            suiteNames = new String[cipherSuites.size()];
            int i = 0;
            for (CipherSuite c : cipherSuites) {
                suiteNames[i++] = c.name;
            }
        }
        return suiteNames.clone();
    }
    public String toString() {
        return cipherSuites.toString();
    }
    void send(HandshakeOutStream s) throws IOException {
        byte[] suiteBytes = new byte[cipherSuites.size() * 2];
        int i = 0;
        for (CipherSuite c : cipherSuites) {
            suiteBytes[i] = (byte)(c.id >> 8);
            suiteBytes[i+1] = (byte)c.id;
            i += 2;
        }
        s.putBytes16(suiteBytes);
    }
    static synchronized void clearAvailableCache() {
        if (CipherSuite.DYNAMIC_AVAILABILITY) {
            CipherSuite.BulkCipher.clearAvailableCache();
            JsseJce.clearEcAvailable();
        }
    }
}
