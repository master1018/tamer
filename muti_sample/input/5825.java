final class EphemeralKeyManager {
    private final static int INDEX_RSA512 = 0;
    private final static int INDEX_RSA1024 = 1;
    private final EphemeralKeyPair[] keys = new EphemeralKeyPair[] {
        new EphemeralKeyPair(null),
        new EphemeralKeyPair(null),
    };
    EphemeralKeyManager() {
    }
    KeyPair getRSAKeyPair(boolean export, SecureRandom random) {
        int length, index;
        if (export) {
            length = 512;
            index = INDEX_RSA512;
        } else {
            length = 1024;
            index = INDEX_RSA1024;
        }
        synchronized (keys) {
            KeyPair kp = keys[index].getKeyPair();
            if (kp == null) {
                try {
                    KeyPairGenerator kgen = JsseJce.getKeyPairGenerator("RSA");
                    kgen.initialize(length, random);
                    keys[index] = new EphemeralKeyPair(kgen.genKeyPair());
                    kp = keys[index].getKeyPair();
                } catch (Exception e) {
                }
            }
            return kp;
        }
    }
    private static class EphemeralKeyPair {
        private final static int MAX_USE = 200;
        private final static long USE_INTERVAL = 3600*1000;
        private KeyPair keyPair;
        private int uses;
        private long expirationTime;
        private EphemeralKeyPair(KeyPair keyPair) {
            this.keyPair = keyPair;
            expirationTime = System.currentTimeMillis() + USE_INTERVAL;
        }
        private boolean isValid() {
            return (keyPair != null) && (uses < MAX_USE)
                   && (System.currentTimeMillis() < expirationTime);
        }
        private KeyPair getKeyPair() {
            if (isValid() == false) {
                keyPair = null;
                return null;
            }
            uses++;
            return keyPair;
        }
    }
}
